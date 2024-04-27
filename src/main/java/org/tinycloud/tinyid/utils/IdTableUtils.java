package org.tinycloud.tinyid.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinycloud.tinyid.bean.entity.TIdTable;
import org.tinycloud.tinyid.dao.IdTableDao;
import org.tinycloud.tinyid.enums.CoreErrorCode;
import org.tinycloud.tinyid.exception.CoreException;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

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


    private static final String[] AFFIX_FORMAT = {"yyyy", "yy", "MM", "dd", "HH", "mm", "ss"};


    public static final Map<String, ArrayBlockingQueue<String>> cacheMap = new ConcurrentHashMap<>();


    /**
     * 获取下一个流水号字符串
     *
     * @param idCode 流水号编码
     * @return String 流水号字符串
     */
    public synchronized static String nextId(String idCode) {
        return takeNextId(idCode);
    }

    /**
     * 获取下一个流水号字符串
     *
     * @param idCode 流水号编码
     * @return String 流水号字符串
     */
    public static String takeNextId(String idCode) {
        ArrayBlockingQueue<String> queue = cacheMap.get(idCode);
        // 没有这个队列的话，那就新建一个队列
        if (queue == null) {
            throw new CoreException(CoreErrorCode.THIS_IDCODE_IS_NOT_EXIST);
        }
        // 获取队列缓存的长度，判断是否大于0
        if (queue.size() > 0) {
            return queue.poll();
        }

        List<String> ids = generateNextIds(idCode);
        ids.forEach(queue::offer);

        // 递归调用，直到 queue.size() > 0
        return takeNextId(idCode);
    }


    /**
     * 批类构造流水号字符串
     *
     * @param idCode idCode
     * @return List<String> 流水号列表
     */
    private static List<String> generateNextIds(String idCode) {
        TIdTable idTable = SpringUtils.getBean(IdTableDao.class).getIdTableByIdCode(idCode);

        List<String> nextIds = new ArrayList<>();
        // 获取流水号的当前值
        long idValue = idTable.getIdValue();
        // 循环获取100个流水号，放入nextIds中去
        for (int i = idTable.getIdStep(); i >= 1; i--) {
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
            Date dateTime = new Date();

            // 是否有前缀 1有，0没有
            Integer hasPrefix = idTable.getHasPrefix();
            // 前缀内容
            String idPrefix = Optional.ofNullable(idTable.getIdPrefix()).orElse("").toString();
            idPrefix = compoundAffix(hasPrefix, idPrefix, dateTime);

            // 是否有后缀 1有，0没有
            Integer hasSuffix = idTable.getHasSuffix();
            String idSuffix = Optional.ofNullable(idTable.getIdSuffix()).orElse("").toString();
            // 后缀内容
            idSuffix = compoundAffix(hasSuffix, idSuffix, dateTime);

            // 长度
            int idLen = idTable.getIdLength() == null ? 10 : idTable.getIdLength();
            int numLen = idLen - idPrefix.length() - idSuffix.length();
            if (numLen < String.valueOf(nextIdValue).length()) {
                logger.error("generateNextId -- 编码长度不够，请增加编码长度!");
                throw new RuntimeException("编码长度不够，请增加编码长度!");
            } else {
                String maxIdStr = get0Str(nextIdValue, numLen);
                retStr = idPrefix + maxIdStr + idSuffix;
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("generateNextId - 获取业务流水号[" + idTable + "]出错,Exception = {e}", e);
            }
        }
        return retStr;
    }


    /**
     * 获取指定位数的数字字符串，不足补0
     *
     * @param nextIdValue 数字
     * @param numLen      位数
     * @return
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
     * @param isAffix  是否有前缀或者后缀 1有 0无
     * @param affix    前缀或者后缀内容
     * @param dateTime 当前时间
     *                 特别说明如下：
     *                 <p>假设当前时间为2019年2月25日3时11分23秒，如果前缀或后缀包含下列字符串</p>
     *                 <p>yyyy：生成的流水号将该字符串替换为2019</p>
     *                 <p>yy：生成的流水号将该字符串替换为19</p>
     *                 <p>MM：生成的流水号将该字符串替换为02</p>
     *                 <p>dd：生成的流水号将该字符串替换为25</p>
     *                 <p>HH：生成的流水号将该字符串替换为03</p>
     *                 <p>mm：生成的流水号将该字符串替换为11</p>
     *                 <p>ss：生成的流水号将该字符串替换为23</p>
     *                 <p>以上日期时间字符，yyyyMMddHHmmss，区分大小写</p>
     * @return
     */
    private static String compoundAffix(Integer isAffix, String affix, Date dateTime) {
        if (isAffix != null && isAffix.equals(1)) {
            if (StringUtils.isNotBlank(affix)) {
                affix = affix.trim();
                for (String format : AFFIX_FORMAT) {
                    affix = affix.replace(format, formatDate(dateTime, format));
                }
                return affix;
            }
        }
        return "";
    }


    /**
     * 得到日期字符串 默认格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
     */
    public static String formatDate(Date date, String pattern) {
        String formatDate = null;
        if (date != null) {
            if (StringUtils.isBlank(pattern)) {
                pattern = "yyyy-MM-dd";
            }
            formatDate = FastDateFormat.getInstance(pattern).format(date);
        }
        return formatDate;
    }
}
