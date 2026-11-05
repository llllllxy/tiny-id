package org.tinycloud.tinyid.enums;

public enum CoreErrorCode {
    THIS_IDCODE_IS_NOT_EXIST(1001, "此idCode不存在！"),
    THE_ID_LENGTH_IS_NOT_ENOUGH(1002, "编码长度不够，请增加编码长度！"),
    THE_ID_LENGTH_CONFIG_MISSING(1003, "编码长度配置缺失！"),
    THE_ID_STEP_CONFIG_INVALID(1004, "步长配置无效，必须为大于0的整数！"),
    THE_BATCH_SIZE_IS_INVALID(1005, "批量大小无效，必须为1~1000之间的整数！"),
    THE_CAPTCHA_CODE_IS_INVALID(1006, "验证码错误或已过期，请重新输入！"),
    SEGMENT_REFRESH_FAILED(1007, "号段刷新失败，请检查流水号配置！"),

    USERNAME_OR_PASSWORD_MISMATCH(2001, "用户名或密码错误！"),
    USER_IS_DISABLED(2002, "用户已被停用！"),
    THE_ORIGINAL_PASSWORD_IS_WRONG(2003, "原始密码错误，请重新输入后再试！"),
    THE_NEWPASSWORD_ENTERED_TWICE_DOES_NOT_MATCH(2004, "两次输入的新密码不一致！"),

    SERIOUSLY_ERROR(9999, "致命错误，请联系系统管理员！"),
    ;

    private Integer code;
    private String desc;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    private CoreErrorCode(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private CoreErrorCode() {
    }
}
