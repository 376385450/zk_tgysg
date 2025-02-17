package com.sinohealth.framework.resttempplate.model;

/**
 * Controller的基础返回类
 *
 * @author phq
 * @since 2021-10-25
 */
public class BaseResponse<T> {

    /**
     * status
     */
    private Integer code;

    /**
     * message
     */
    private String msg;

    /**
     * data
     */
    private T data;

    public BaseResponse() {
    }

    public BaseResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private BaseResponse(T data) {
        this.code  = 0;
        this.data = data;
    }

    /**
     * Call this function if there is success
     *
     * @param data data
     * @param <T> type
     * @return resule
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(data);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }



}
