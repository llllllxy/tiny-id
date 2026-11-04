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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    /**
     * 等待异步预加载号段的最长等待时间（毫秒）。
     * 取号段是单次 DB 查询，正常几十毫秒；超时说明预加载已异常，走同步兜底快速失败，
     * 不宜设太长，否则会长时间占用工作线程，并发高时拖垮整个服务。
     */
    private static final long ASYNC_LOAD_TIMEOUT_MILLIS = 5000L;

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
                // 「是否有预加载任务在跑」以 Future 是否完成（isDone）为唯一事实来源：
                // isDone 单调不可逆，无论任务成功、失败还是瞬间完成都不会出现状态错乱
                Future<?> preloadFuture = segmentId.getPreloadFuture();
                boolean preloading = preloadFuture != null && !preloadFuture.isDone();
                if (!queue.isEmpty()) {
                    // 当剩余不足时，异步预加载下一号段
                    if (!preloading && queue.size() <= (segmentId.getStep() * GlobalConstant.LOADING_PERCENT / 100)) {
                        try {
                            Future<?> future = this.asyncServiceExecutor.submit(() -> {
                                try {
                                    List<String> ids = generateNextIds(idCode);
                                    ids.forEach(queue::offer);
                                } catch (Exception e) {
                                    // 预加载失败只记录日志，任务结束即 isDone，
                                    // 由取号方感知队列为空后走同步兜底，无需在任务里复位任何标志
                                    logger.error("异步预加载号段失败, idCode={}", idCode, e);
                                }
                            });
                            segmentId.setPreloadFuture(future);
                        } catch (Exception e) {
                            // 线程池关闭或饱和导致提交失败：未设置 Future，下轮判定为非预加载中，走同步兜底
                            logger.error("提交号段预加载任务失败, idCode={}", idCode, e);
                        }
                    }
                    return queue.poll();
                } else {
                    if (preloading) {
                        // 等待异步线程返回，带超时，防止无限阻塞
                        try {
                            preloadFuture.get(ASYNC_LOAD_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                            // 任务正常完成，号已入队，继续下一轮从队列取号
                            continue;
                        } catch (InterruptedException e) {
                            // 恢复中断标志，不吞中断
                            Thread.currentThread().interrupt();
                            logger.error("等待号段预加载被中断, idCode={}", idCode, e);
                        } catch (ExecutionException | TimeoutException e) {
                            logger.error("等待号段预加载失败, idCode={}", idCode, e);
                        }
                        // 中断/失败/超时后不再等待，直接落到下面的同步兜底。
                        // 超时不 cancel：任务可能已更新完 DB，让它跑完把号 offer 进队列，避免已分配的号段丢失
                    }
                    // 第一次加载 / 预加载失败或超时后的兜底：同步取号段。
                    // 这里故意不 catch，配置错误时异常直接抛给调用方快速失败，而不是无限空转
                    List<String> ids = generateNextIds(idCode);
                    ids.forEach(queue::offer);
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
        // 校验步长有效性：step<=0 时 UPDATE 不前进且生成空列表，取号循环会变成打满 DB 的无限循环
        Integer idStep = idTable.getIdStep();
        if (idStep == null || idStep < 1) {
            throw new CoreException(CoreErrorCode.THE_ID_STEP_CONFIG_INVALID);
        }
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
