package org.tinycloud.tinyid.enums;

public enum CoreErrorCode {
    THIS_IDCODE_IS_NOT_EXIST(1001, "此idCode不存在！"),
    THE_ID_LENGTH_IS_NOT_ENOUGH(1002, "编码长度不够，请增加编码长度！"),


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
