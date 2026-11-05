package org.tinycloud.tinyid.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tinycloud.tinyid.bean.assist.SegmentBuffer;
import org.tinycloud.tinyid.bean.entity.TIdTable;
import org.tinycloud.tinyid.constant.GlobalConstant;
import org.tinycloud.tinyid.dao.IdTableDao;
import org.tinycloud.tinyid.enums.CoreErrorCode;
import org.tinycloud.tinyid.exception.CoreException;
import org.tinycloud.tinyid.utils.AffixFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 号段模式发号服务。
 *
 * <p>每个流水号标识在内存中持有一个 {@link SegmentBuffer} 游标；号段耗尽时通过
 * {@code UPDATE t_idtable SET id_value = id_value + id_step} 原子申请下一段，
 * 其余取号都是内存自增，不访问数据库。数据库只持久化「已推进到的最大值」，
 * 因此进程重启会跳过未发出的号，但不会重复或回退。</p>
 *
 * <p>采用游标而非「预生成整段字符串 + 异步预加载」：无异步依赖，不存在预加载
 * 任务失败/丢弃引发的取号阻塞；前后缀的时间占位符在每次取号时解析，跨天不会
 * 沿用旧日期。代价是号段耗尽时调用线程同步等待一次数据库往返，可通过调大
 * {@code id_step} 摊薄。</p>
 *
 * @author liuxingyu01
 * @since 2024-04-27 20:20
 */
@Service
public class SegmentIdService {
    private static final Logger logger = LoggerFactory.getLogger(SegmentIdService.class);

    /**
     * 流水号标识 -> 内存号段缓冲
     */
    private final Map<String, SegmentBuffer> segmentCacheMap = new ConcurrentHashMap<>();

    @Autowired
    private IdTableDao idTableDao;

    /**
     * 获取指定流水号标识的下一个号码。
     *
     * @param idCode 流水号标识编码
     * @return 形如「前缀 + 补零数字 + 后缀」的流水号
     */
    public String nextId(String idCode) {
        this.requireIdCode(idCode);
        return this.takeNextId(idCode);
    }

    /**
     * 批量获取指定流水号标识的号码，允许跨号段。
     *
     * @param idCode    流水号标识编码
     * @param batchSize 数量，取值范围 1~1000
     * @return 按取号顺序排列的流水号列表
     */
    public List<String> nextBatchId(String idCode, Integer batchSize) {
        this.requireIdCode(idCode);
        if (batchSize == null || batchSize < 1 || batchSize > GlobalConstant.MAX_BATCH_SIZE) {
            throw new CoreException(CoreErrorCode.THE_BATCH_SIZE_IS_INVALID);
        }
        List<String> ids = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            ids.add(this.takeNextId(idCode));
        }
        return ids;
    }

    /**
     * 使指定流水号标识的内存缓冲失效，仅用于单实例运维场景。
     *
     * <p>分布式部署下不应由管理台调用：只会清掉当前实例，其余实例仍按旧配置发号。
     * 配置变更的约定是各实例把已分配号段自然取完后，下一次续段再读回最新配置。</p>
     *
     * @param idCode 流水号标识编码
     */
    public void invalidate(String idCode) {
        if (!StringUtils.hasText(idCode)) {
            return;
        }
        SegmentBuffer buffer = this.segmentCacheMap.get(idCode);
        if (buffer != null) {
            buffer.markStale();
            this.segmentCacheMap.remove(idCode, buffer);
        }
    }

    /**
     * 校验流水号标识非空。
     *
     * @param idCode 流水号标识编码
     */
    private void requireIdCode(String idCode) {
        if (!StringUtils.hasText(idCode)) {
            throw new CoreException(CoreErrorCode.THIS_IDCODE_IS_NOT_EXIST);
        }
    }

    /**
     * 从内存缓冲取一个号码，必要时续段。
     *
     * <p>{@code computeIfAbsent} 只做纯内存创建，数据库访问全部放在按缓冲加锁之后，
     * 避免把慢操作放进 ConcurrentHashMap 的桶锁；{@code synchronized} 保证同一标识
     * 在本实例内只有一个线程续段，跨实例由数据库行锁保证。</p>
     *
     * @param idCode 流水号标识编码
     * @return 流水号
     */
    private String takeNextId(String idCode) {
        while (true) {
            SegmentBuffer buffer = this.segmentCacheMap.computeIfAbsent(idCode, key -> new SegmentBuffer());
            synchronized (buffer) {
                if (buffer.isStale()) {
                    this.segmentCacheMap.remove(idCode, buffer);
                    continue;
                }
                if (buffer.exhausted()) {
                    this.reload(idCode, buffer);
                }
                // 先按配置拼装成功再推进游标，配置错误时不会白白丢号
                long value = buffer.getCursor() + 1;
                String id;
                try {
                    id = this.format(buffer.getConfig(), value);
                } catch (RuntimeException e) {
                    // 格式化失败说明配置快照有问题：丢弃缓冲，下次请求重读最新配置，
                    // 避免坏快照一直留在内存里导致修复配置后也无法自愈
                    buffer.markStale();
                    this.segmentCacheMap.remove(idCode, buffer);
                    throw e;
                }
                buffer.next();
                return id;
            }
        }
    }

    /**
     * 向数据库申请新号段并装载到缓冲。
     *
     * @param idCode 流水号标识编码
     * @param buffer 该标识的内存缓冲
     */
    private void reload(String idCode, SegmentBuffer buffer) {
        try {
            TIdTable config = this.idTableDao.refreshByIdCode(idCode);
            Integer step = config.getIdStep();
            if (step == null || step < 1) {
                throw new CoreException(CoreErrorCode.THE_ID_STEP_CONFIG_INVALID);
            }
            Long max = config.getIdValue();
            if (max == null || max < step) {
                throw new CoreException(CoreErrorCode.SEGMENT_REFRESH_FAILED);
            }
            buffer.reset(max - step, max, config);
        } catch (RuntimeException e) {
            // 续段失败后不能留一个半初始化的缓冲，否则后续请求会一直用它尝试
            buffer.markStale();
            this.segmentCacheMap.remove(idCode, buffer);
            logger.error("刷新号段失败, idCode={}", idCode, e);
            throw e;
        }
    }

    /**
     * 按当前配置把数值拼装成完整流水号，并校验总长度是否放得下数字部分。
     *
     * @param config 最近一次续段读回的配置
     * @param value  流水号数值
     * @return 完整流水号
     */
    private String format(TIdTable config, long value) {
        if (config == null || config.getIdLength() == null) {
            throw new CoreException(CoreErrorCode.THE_ID_LENGTH_CONFIG_MISSING);
        }
        int totalLength = config.getIdLength();
        // 前后缀的时间占位符按取号时刻解析，跨天不会沿用号段装载时的旧日期
        String prefix = AffixFormatter.compoundAffix(config.getHasPrefix(), config.getIdPrefix());
        String suffix = AffixFormatter.compoundAffix(config.getHasSuffix(), config.getIdSuffix());
        int numericLength = totalLength - prefix.length() - suffix.length();
        if (Long.toString(value).length() > numericLength) {
            throw new CoreException(CoreErrorCode.THE_ID_LENGTH_IS_NOT_ENOUGH);
        }
        return prefix + AffixFormatter.getPaddedNumber(value, numericLength) + suffix;
    }
}
