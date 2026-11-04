package org.tinycloud.tinyid.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.tinycloud.tinyid.bean.assist.SegmentId;
import org.tinycloud.tinyid.bean.entity.TIdTable;
import org.tinycloud.tinyid.constant.GlobalConstant;
import org.tinycloud.tinyid.dao.IdTableDao;
import org.tinycloud.tinyid.enums.CoreErrorCode;
import org.tinycloud.tinyid.exception.CoreException;
import org.tinycloud.tinyid.utils.AffixFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

/**
 * <p>
 * 号段生成服务：负责按号段模式生成业务流水号
 * </p>
 *
 * <p>每个 idCode 维护一个号段队列，首次同步加载 step 个号，</p>
 * <p>之后当队列剩余低于阈值时异步预加载下一号段，避免取号阻塞在数据库上。</p>
 *
 * @author liuxingyu01
 * @since 2024-04-27 20:20
 */
@Service
public class SegmentIdService {
    private static final Logger logger = LoggerFactory.getLogger(SegmentIdService.class);

    @Autowired
    private IdTableDao idTableDao;

    @Qualifier("asyncServiceExecutor")
    @Autowired
    private ThreadPoolTaskExecutor asyncServiceExecutor;

    /**
     * idCode -> 号段缓存
     */
    private final Map<String, SegmentId> segmentIdCacheMap = new ConcurrentHashMap<>();

    /**
     * 获取下一个流水号字符串
     *
     * @param idCode 流水号编码
     * @return String 流水号字符串
     */
    public String nextId(String idCode) {
        return takeNextId(idCode);
    }

    /**
     * 批量获取流水号字符串
     *
     * @param idCode    流水号编码
     * @param batchSize 批量大小
     * @return 流水号列表
     */
    public List<String> nextBatchId(String idCode, Integer batchSize) {
        List<String> idList = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            idList.add(this.takeNextId(idCode));
        }
        return idList;
    }

    /**
     * 获取下一个流水号字符串
     *
     * @param idCode 流水号编码
     * @return String 流水号字符串
     */
    private String takeNextId(String idCode) {
        // 使用computeIfAbsent确保每个idCode只有一个SegmentId
        final SegmentId segmentId = this.segmentIdCacheMap.computeIfAbsent(idCode, key -> {
            TIdTable idTable = this.idTableDao.get(key);
            if (idTable == null) {
                throw new CoreException(CoreErrorCode.THIS_IDCODE_IS_NOT_EXIST);
            }
            ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
            return new SegmentId(queue, idTable.getIdStep());
        });

        synchronized (segmentId) {
            while (true) {
                final ConcurrentLinkedQueue<String> queue = segmentId.getQueue();
                if (!queue.isEmpty()) {
                    // 当剩余不足时，异步预加载下一号段
                    if (!segmentId.isPreloaded() && queue.size() <= (segmentId.getStep() * GlobalConstant.LOADING_PERCENT / 100)) {
                        Future<?> future = this.asyncServiceExecutor.submit(() -> {
                            List<String> ids = generateNextIds(idCode);
                            ids.forEach(queue::offer);
                            segmentId.setPreloaded(false);
                        });
                        segmentId.setPreloaded(true);
                        segmentId.setFuture(future);
                    }
                    return queue.poll();
                } else {
                    if (segmentId.isPreloaded()) {
                        // 等待异步线程返回
                        try {
                            segmentId.getFuture().get();
                        } catch (Exception e) {
                            logger.error("error query segmentId: {}", e.getMessage(), e);
                        }
                    } else {
                        // 第一次加载，按照步长，生成id推送到队列里
                        List<String> ids = generateNextIds(idCode);
                        ids.forEach(queue::offer);
                    }
                }
            }
        }
    }

    /**
     * 批量构造流水号字符串
     *
     * @param idCode idCode
     * @return 流水号List列表
     */
    private List<String> generateNextIds(String idCode) {
        // 刷新数据库里的步长
        TIdTable idTable = this.idTableDao.refreshByIdCode(idCode);
        List<String> nextIds = new ArrayList<>();
        // 获取流水号的当前值
        long idValue = idTable.getIdValue();
        // 循环获取step个流水号，放入nextIds中去
        for (int i = idTable.getIdStep() - 1; i >= 0; i--) {
            long nextIdValue = idValue - i;
            String nextId = generateNextId(idTable, nextIdValue);
            nextIds.add(nextId);
        }
        return nextIds;
    }

    /**
     * 根据规则构造流水号字符串
     *
     * @param idTable     流水号规则配置
     * @param nextIdValue 流水号当前数值
     * @return String 流水号字符串
     */
    private String generateNextId(TIdTable idTable, long nextIdValue) {
        try {
            // 1. 统一处理前缀和后缀
            String prefix = AffixFormatter.compoundAffix(idTable.getHasPrefix(), idTable.getIdPrefix());
            String suffix = AffixFormatter.compoundAffix(idTable.getHasSuffix(), idTable.getIdSuffix());
            // 2. 使用 Optional 安全地获取 ID 长度
            int totalLength = Optional.ofNullable(idTable.getIdLength()).orElseThrow(() -> new CoreException(CoreErrorCode.THE_ID_LENGTH_CONFIG_MISSING));
            // 3. 计算数字部分的长度
            int numericPartLength = totalLength - prefix.length() - suffix.length();
            // 4. 校验数字部分长度是否足够
            if (numericPartLength < Long.toString(nextIdValue).length()) {
                throw new CoreException(CoreErrorCode.THE_ID_LENGTH_IS_NOT_ENOUGH);
            }
            // 5. 拼接最终的流水号
            String paddedNumber = AffixFormatter.getPaddedNumber(nextIdValue, numericPartLength);
            return prefix + paddedNumber + suffix;
        } catch (CoreException e) {
            // 6. 只记录日志，直接抛出原始的业务异常，不进行包装
            logger.error("生成业务流水号[{}]失败: {}", idTable.getIdCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // 7. 捕获所有其他非预期异常，包装成系统级严重错误
            logger.error("生成业务流水号[{}]时发生未知错误", idTable.getIdCode(), e);
            throw new CoreException(CoreErrorCode.SERIOUSLY_ERROR);
        }
    }
}
