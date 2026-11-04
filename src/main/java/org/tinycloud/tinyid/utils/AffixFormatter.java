package org.tinycloud.tinyid.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * 流水号前后缀格式化工具（纯函数，无状态）
 * </p>
 *
 * <p>假设当前时间为2019年2月25日3时11分23秒，如果前缀或后缀包含下列字符串</p>
 * <ul>
 *     <li>[yyyy]：替换为 2019</li>
 *     <li>[yy]：替换为 19</li>
 *     <li>[MM]：替换为 02</li>
 *     <li>[dd]：替换为 25</li>
 *     <li>[HH]：替换为 03</li>
 *     <li>[mm]：替换为 11</li>
 *     <li>[ss]：替换为 23</li>
 * </ul>
 * <p>以上日期时间字符，yyyyMMddHHmmss，区分大小写</p>
 *
 * @author liuxingyu01
 * @since 2024-04-27 20:20
 */
public final class AffixFormatter {

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

    private AffixFormatter() {
    }

    /**
     * 完善前缀和后缀
     *
     * @param hasAffix 是否有前缀或者后缀 1有 0无
     * @param affix    前缀或者后缀内容
     * @return 转换后的前缀和后缀
     */
    public static String compoundAffix(Integer hasAffix, String affix) {
        if (hasAffix == null || hasAffix != 1 || affix == null || affix.isEmpty()) {
            return "";
        }
        LocalDateTime now = LocalDateTime.now();
        String result = affix.trim();
        for (Map.Entry<String, DateTimeFormatter> entry : AFFIX_FORMATTER_MAP.entrySet()) {
            result = result.replace(entry.getKey(), now.format(entry.getValue()));
        }
        return result;
    }

    /**
     * 获取指定位数的数字字符串，不足补0
     *
     * @param nextIdValue 数字
     * @param numLen      位数
     * @return 补零后的数字字符串
     */
    public static String getPaddedNumber(long nextIdValue, int numLen) {
        StringBuilder retStr = new StringBuilder();
        int needLen = numLen - Long.toString(nextIdValue).length();
        for (int i = 0; i < needLen; i++) {
            retStr.append("0");
        }
        return retStr + String.valueOf(nextIdValue);
    }
}
