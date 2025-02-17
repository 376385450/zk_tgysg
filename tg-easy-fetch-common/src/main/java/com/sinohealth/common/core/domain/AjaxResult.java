package com.sinohealth.common.core.domain;

import cn.hutool.core.lang.Validator;
import cn.hutool.http.HttpStatus;

import java.util.HashMap;
import java.util.Objects;

/**
 * 操作消息提醒
 *
 * @author dataplatform
 */
public class AjaxResult<T> extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    public static final String CODE_TAG = "code";

    /**
     * 返回内容
     */
    public static final String MSG_TAG = "msg";

    /**
     * 数据对象
     */
    public static final String DATA_TAG = "data";

    public Integer getCode() {
        return (Integer) super.get(CODE_TAG);
    }

    public String getMsg() {
        return (String) super.get(MSG_TAG);
    }

    public T getData() {
        return (T) super.get(DATA_TAG);
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象，使其表示一个空消息。
     */
    public AjaxResult() {
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     */
    public AjaxResult(int code, String msg) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     * @param data 数据对象
     */
    public AjaxResult(int code, String msg, T data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (Validator.isNotNull(data)) {
            super.put(DATA_TAG, data);
        }
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static AjaxResult<Object> success() {
        return AjaxResult.success(null);
    }

    public static AjaxResult<Void> succeed() {
        return new AjaxResult<>(HttpStatus.HTTP_OK, null, null);
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息
     */
    public static <T> AjaxResult<T> success(T data) {
        return AjaxResult.success("操作成功", data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @return 成功消息
     */
    public static AjaxResult<Object> success(String msg) {
        return AjaxResult.success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> AjaxResult<T> success(String msg, T data) {
        return new AjaxResult(HttpStatus.HTTP_OK, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @return
     */
    public static AjaxResult<Object> error() {
        return AjaxResult.error("操作失败");
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
//    public static AjaxResult<Object> error(String msg) {
//        return AjaxResult.error(msg, null);
//    }
    public static <T> AjaxResult<T> error(String msg) {
        return AjaxResult.error(msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static <T> AjaxResult<T> error(String msg, T data) {
        return new AjaxResult(HttpStatus.HTTP_INTERNAL_ERROR, msg, data);
    }

    public static <T> AjaxResult<T> error(int code, String msg, T data) {
        return new AjaxResult(code, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @param code 状态码
     * @param msg  返回内容
     * @return 警告消息
     */
    public static AjaxResult<Object> error(int code, String msg) {
        return new AjaxResult(code, msg, null);
    }

    public static <T> AjaxResult<T> errorMsg(int code, String msg) {
        return new AjaxResult(code, msg, null);
    }

    public boolean isSuccess() {
        return Objects.equals(HttpStatus.HTTP_OK, get(CODE_TAG));
    }

    /**
     * 尚书台接口
     */
    public boolean isDolphinSuccess() {
        return Objects.equals(0, get(CODE_TAG));
    }
}
