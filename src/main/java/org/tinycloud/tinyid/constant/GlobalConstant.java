package org.tinycloud.tinyid.constant;

/**
 * <p>
 * 全局常量类
 * </p>
 *
 * @author liuxingyu01
 * @since 2023-03-07 15:47:38
 */
public class GlobalConstant {

    /**
     * 已删除标记
     */
    public static final Integer DELETED = 1;

    /**
     * 未删除标记
     */
    public static final Integer NOT_DELETED = 0;

    /**
     * 正常在用
     */
    public static final Integer ENABLED = 0;

    /**
     * 已停用
     */
    public static final Integer DISABLED = 1;

    /**
     * session key
     */
    public static final String SESSION_KEY = "loginId";

    public static final String CAPTCHA_CODE_SESSION_KEY = "captchaCode_";

    /**
     * 预加载下个号段的百分比
     */
    public static final int LOADING_PERCENT = 20;
}
