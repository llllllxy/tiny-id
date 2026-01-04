package org.tinycloud.tinyid.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.tinycloud.tinyid.bean.assist.SegmentId;
import org.tinycloud.tinyid.bean.entity.TIdTable;
import org.tinycloud.tinyid.constant.GlobalConstant;
import org.tinycloud.tinyid.dao.IdTableDao;
import org.tinycloud.tinyid.enums.CoreErrorCode;
import org.tinycloud.tinyid.exception.CoreException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

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


    private static final String[] AFFIX_FORMAT_REGEX = {"[yyyy]", "[yy]", "[MM]", "[dd]", "[HH]", "[mm]", "[ss]"};

    private static final String[] AFFIX_FORMAT = {"yyyy", "yy", "MM", "dd", "HH", "mm", "ss"};

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
     * @return List<String> 流水号列表
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
     * @param idTable     流水号Map
     * @param nextIdValue 流水号当前值
     * @return String 流水号字符串
     */
    private static String generateNextId(TIdTable idTable, long nextIdValue) {
        String retStr = "";
        try {
            // 补充前缀内容
            Integer hasPrefix = idTable.getHasPrefix();
            String idPrefix = idTable.getIdPrefix() != null ? idTable.getIdPrefix() : "";
            idPrefix = compoundAffix(hasPrefix, idPrefix);

            // 补充后缀内容
            Integer hasSuffix = idTable.getHasSuffix();
            String idSuffix = idTable.getIdSuffix() != null ? idTable.getIdSuffix() : "";
            idSuffix = compoundAffix(hasSuffix, idSuffix);

            // id长度
            int numLen = idTable.getIdLength() - idPrefix.length() - idSuffix.length();
            if (numLen < String.valueOf(nextIdValue).length()) {
                throw new CoreException(CoreErrorCode.THE_ID_LENGTH_IS_NOT_ENOUGH);
            } else {
                String maxIdStr = get0Str(nextIdValue, numLen);
                retStr = idPrefix + maxIdStr + idSuffix;
            }
            return retStr;
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("generateNextId - 获取业务流水号[" + idTable.getIdCode() + "]出错,Exception: ", e);
            }
            if (e instanceof CoreException) {
                throw new CoreException(((CoreException) e).getCode(), ((CoreException) e).getMessage());
            } else {
                throw new CoreException(CoreErrorCode.SERIOUSLY_ERROR);
            }
        }
    }


    /**
     * 获取指定位数的数字字符串，不足补0
     *
     * @param nextIdValue 数字
     * @param numLen      位数
     * @return 补零后的数字字符串
     */
    private static String get0Str(long nextIdValue, int numLen) {
        StringBuilder retStr = new StringBuilder();
        int needLen = numLen - String.valueOf(nextIdValue).length();
        for (int i = 0; i < needLen; i++) {
            retStr.append("0");
        }
        return retStr + String.valueOf(nextIdValue);
    }


    /**
     * 完善前缀和后缀
     *
     * @param isAffix 是否有前缀或者后缀 1有 0无
     * @param affix   前缀或者后缀内容
     *                特别说明如下：
     *                <p>假设当前时间为2019年2月25日3时11分23秒，如果前缀或后缀包含下列字符串</p>
     *                <p>[yyyy]：生成的流水号将该字符串替换为2019</p>
     *                <p>[yy]：生成的流水号将该字符串替换为19</p>
     *                <p>[MM]：生成的流水号将该字符串替换为02</p>
     *                <p>[dd]：生成的流水号将该字符串替换为25</p>
     *                <p>[HH]：生成的流水号将该字符串替换为03</p>
     *                <p>[mm]：生成的流水号将该字符串替换为11</p>
     *                <p>[ss]：生成的流水号将该字符串替换为23</p>
     *                <p>以上日期时间字符，yyyyMMddHHmmss，区分大小写</p>
     * @return 转换后的前缀和后缀
     */
    private static String compoundAffix(Integer isAffix, String affix) {
        if (isAffix != null && isAffix.equals(1)) {
            if (StringUtils.isNotBlank(affix)) {
                Date dateTime = new Date();
                affix = affix.trim();
                for (int i = 0; i < AFFIX_FORMAT_REGEX.length; i++) {
                    affix = affix.replace(AFFIX_FORMAT_REGEX[i], formatDate(dateTime, AFFIX_FORMAT[i]));
                }
                return affix;
            }
        }
        return "";
    }


    /**
     * 得到日期字符串 默认格式（yyyyMMdd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
     */
    private static String formatDate(Date date, String pattern) {
        return FastDateFormat.getInstance(pattern).format(date);
    }
}
