package org.tinycloud.tinyid.model;


import org.tinycloud.tinyid.enums.CommonCode;

/**
 * API接口统一返回
 *
 * @author lipanlin
 */
public class ApiResult<T> {

    private Integer code;

    private T data;

    private String msg;

    private long time;

    public Integer getCode() {
        return this.code;
    }

    public T getData() {
        return this.data;
    }

    public String getMsg() {
        return this.msg;
    }

    public long getTime() {
        return this.time;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public ApiResult() {
        this.code = CommonCode.SUCCESS.getCode();
        this.msg = CommonCode.SUCCESS.getDesc();
        this.time = System.currentTimeMillis();
    }

    public ApiResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.time = System.currentTimeMillis();
    }

    public static <T> ApiResult<T> success() {
        return new ApiResult<>();
    }

    public static <T> ApiResult<T> success(T data) {
        return success(data, CommonCode.SUCCESS.getDesc());
    }

    public static <T> ApiResult<T> success(T data, String msg) {
        return new ApiResult<>(CommonCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> ApiResult<T> fail(String msg) {
        return new ApiResult<>(CommonCode.UNKNOWN_ERROR.getCode(), msg, null);
    }

    public static <T> ApiResult<T> fail(Integer code, String msg) {
        return new ApiResult<>(code, msg, null);
    }

    public static ApiResult<?> fail(CommonCode error) {
        return new ApiResult<>(error.getCode(), null, error.getDesc());
    }

    public static <T> ApiResult<T> fail(CommonCode error, T data) {
        return new ApiResult<>(error.getCode(), error.getDesc(), data);
    }

    public static <T> ApiResult<T> build(Integer code, String msg, T data) {
        return new ApiResult<>(code, msg, data);
    }
}