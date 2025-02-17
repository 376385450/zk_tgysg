package com.sinohealth.common.enums;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2024-01-23 14:23
 */
public enum FtpStatus {

    SUCCESS,

    FAILURE,

    UPLOADING,

    /**
     * 批量更新时设置的队列
     */
    WAIT,
    ;

}
