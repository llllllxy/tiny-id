package org.tinycloud.tinyid.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.tinycloud.tinyid.bean.assist.SegmentId;
import org.tinycloud.tinyid.bean.entity.TIdTable;
import org.tinycloud.tinyid.constant.GlobalConstant;
import org.tinycloud.tinyid.dao.IdTableDao;
import org.tinycloud.tinyid.enums.CoreErrorCode;
import org.tinycloud.tinyid.exception.CoreException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

/**
 * <p>
 * 自定义业务流水号工具类
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-04-2024/4/27 20:20
 */
public class IdTableUtils {
    final static Logger logger = LoggerFactory.getLogger(IdTableUtils.class);

    private static final Map<String, DateTimeFormatter> AFFIX_FORMATTER_MAP = new LinkedHashMap<>();

    static {
        AFFIX_FORMATTER_MAP.put("[yyyy]", DateTimeFormatter.ofPattern("yyyy"));
        AFFIX_FORMATTER_MAP.put("[yy]", DateTimeFormatter.ofPattern("yy"));
        AFFIX_FORMATTER_MAP.put("[MM]", DateTimeFormatter.ofPattern("MM"));
        AFFIX_FORMATTER_MAP.put("[dd]", DateTimeFormatter.ofPattern("dd"));
        AFFIX_FORMATTER_MAP.put("[HH]", DateTimeFormatter.ofPattern("HH"));
        AFFIX_FORMATTER_MAP.put("[mm]", DateTimeFormatter.ofPattern("mm"));
        AFFIX_FORMATTER_MAP.put("[ss]", DateTimeFormatter.ofPattern("ss"));
    }


    // 使用静态内部类实现延迟初始化
    private static class BeanHolder {
        static final IdTableDao ID_TABLE_DAO = initIdTableDao();
        static final ThreadPoolTaskExecutor ASYNC_EXECUTOR = initAsyncExecutor();

        private static IdTableDao initIdTableDao() {
            try {
                return SpringUtils.getBean(IdTableDao.class);
            } catch (Exception e) {
                logger.error("Failed to initialize IdTableDao", e);
                throw new IllegalStateException("IdTableDao initialization failed", e);
            }
        }

        private static ThreadPoolTaskExecutor initAsyncExecutor() {
            try {
                return SpringUtils.getBean("asyncServiceExecutor");
            } catch (Exception e) {
                logger.error("Failed to initialize asyncServiceExecutor", e);
                throw new IllegalStateException("asyncServiceExecutor initialization failed", e);
            }
        }
    }

    // 直接访问的方法
    private static IdTableDao getIdTableDao() {
        return BeanHolder.ID_TABLE_DAO;
    }

    private static ThreadPoolTaskExecutor getThreadPoolTaskExecutor() {
        return BeanHolder.ASYNC_EXECUTOR;
    }

    private static final Map<String, SegmentId> segmentIdCacheMap = new ConcurrentHashMap<>();


    /**
     * 获取下一个流水号字符串
     *
     * @param idCode 流水号编码
     * @return String 流水号字符串
     */
    public static String nextId(String idCode) {
        return takeNextId(idCode);
    }

    /**
     * 获取下一个流水号字符串
     *
     * @param idCode 流水号编码
     * @return String 流水号字符串
     */
    public static List<String> nextBatchId(String idCode, Integer batchSize) {
        List<String> idList = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            String id = takeNextId(idCode);
            idList.add(id);
        }
        return idList;
    }

    /**
     * 获取下一个流水号字符串
     *
     * @param idCode 流水号编码
     * @return String 流水号字符串
     */
    public static String takeNextId(String idCode) {
        // 使用computeIfAbsent确保每个idCode只有一个SegmentId
        final SegmentId segmentId = segmentIdCacheMap.computeIfAbsent(idCode, key -> {
            TIdTable idTable = getIdTableDao().get(key);
            if (idTable == null) {
                throw new CoreException(CoreErrorCode.THIS_IDCODE_IS_NOT_EXIST);
            }
            ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
            return new SegmentId(queue, idTable.getIdStep());
        });

        synchronized (segmentId) {
            while (true) {
                // 获取队列缓存的长度，判断是否大于0
                final ConcurrentLinkedQueue<String> queue = segmentId.getQueue();
                if (!queue.isEmpty()) {
                    // 当剩余不足时，异步预加载下一号段
                    if (!segmentId.isPreloaded() && queue.size() <= (segmentId.getStep() * GlobalConstant.LOADING_PERCENT / 100)) {
                        Future<?> future = getThreadPoolTaskExecutor().submit(() -> {
                            // 按照步长，生成id推送到队列里
                            List<String> ids = generateNextIds(idCode);
                            ids.forEach(queue::offer);
                            // 移除正在进行预加载的标志
                            segmentId.setPreloaded(false);
                        });
                        // 设置正在进行预加载的标志
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
     * 批类构造流水号字符串
     *
     * @param idCode idCode
     * @return 流水号List列表
     */
    private static List<String> generateNextIds(String idCode) {
        // 刷新数据库里的步长
        TIdTable idTable = getIdTableDao().refreshByIdCode(idCode);
        List<String> nextIds = new ArrayList<>();
        // 获取流水号的当前值
        long idValue = idTable.getIdValue();
        // 循环获取100个流水号，放入nextIds中去
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
    private static String generateNextId(TIdTable idTable, long nextIdValue) {
        try {
            // 1. 统一处理前缀和后缀
            String prefix = compoundAffix(idTable.getHasPrefix(), idTable.getIdPrefix());
            String suffix = compoundAffix(idTable.getHasSuffix(), idTable.getIdSuffix());
            // 2. 使用 Optional 安全地获取 ID 长度
            int totalLength = Optional.ofNullable(idTable.getIdLength()).orElseThrow(() -> new CoreException(CoreErrorCode.THE_ID_LENGTH_CONFIG_MISSING));
            // 3. 计算数字部分的长度
            int numericPartLength = totalLength - prefix.length() - suffix.length();
            // 4. 校验数字部分长度是否足够
            if (numericPartLength < Long.toString(nextIdValue).length()) {
                throw new CoreException(CoreErrorCode.THE_ID_LENGTH_IS_NOT_ENOUGH);
            }
            // 5. 拼接最终的流水号
            String paddedNumber = getPaddedNumber(nextIdValue, numericPartLength);
            return prefix + paddedNumber + suffix;
        } catch (CoreException e) {
            // 6. 只记录日志，直接抛出原始的业务异常，不进行包装
            logger.error("生成业务流水号[{}]失败: {}", idTable.getIdCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // 7. 捕获所有其他非预期异常，包装成系统级严重错误
            logger.error("生成业务流水号[" + idTable.getIdCode() + "]时发生未知错误", e);
            throw new CoreException(CoreErrorCode.SERIOUSLY_ERROR);
        }
    }


    /**
     * 获取指定位数的数字字符串，不足补0
     *
     * @param nextIdValue 数字
     * @param numLen      位数
     * @return 补零后的数字字符串
     */
    private static String getPaddedNumber(long nextIdValue, int numLen) {
        StringBuilder retStr = new StringBuilder();
        int needLen = numLen - Long.toString(nextIdValue).length();
        for (int i = 0; i < needLen; i++) {
            retStr.append("0");
        }
        return retStr + String.valueOf(nextIdValue);
    }


    /**
     * 完善前缀和后缀
     *
     * @param hasAffix 是否有前缀或者后缀 1有 0无
     * @param affix    前缀或者后缀内容
     *                 特别说明如下：
     *                 <p>假设当前时间为2019年2月25日3时11分23秒，如果前缀或后缀包含下列字符串</p>
     *                 <p>[yyyy]：生成的流水号将该字符串替换为2019</p>
     *                 <p>[yy]：生成的流水号将该字符串替换为19</p>
     *                 <p>[MM]：生成的流水号将该字符串替换为02</p>
     *                 <p>[dd]：生成的流水号将该字符串替换为25</p>
     *                 <p>[HH]：生成的流水号将该字符串替换为03</p>
     *                 <p>[mm]：生成的流水号将该字符串替换为11</p>
     *                 <p>[ss]：生成的流水号将该字符串替换为23</p>
     *                 <p>以上日期时间字符，yyyyMMddHHmmss，区分大小写</p>
     * @return 转换后的前缀和后缀
     */
    private static String compoundAffix(Integer hasAffix, String affix) {
        // 1. 前置条件检查：如果不需要处理或 affix 为空，则直接返回空字符串
        if (hasAffix == null || hasAffix != 1 || affix == null || affix.isEmpty()) {
            return "";
        }
        LocalDateTime now = LocalDateTime.now();
        String result = affix.trim();
        for (var entry : AFFIX_FORMATTER_MAP.entrySet()) {
            result = result.replace(entry.getKey(), now.format(entry.getValue()));
        }
        return result;
    }
}
