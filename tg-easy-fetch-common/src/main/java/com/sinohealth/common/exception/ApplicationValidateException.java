package com.sinohealth.common.exception;

import lombok.Data;

/**
 * @Author Rudolph
 * @Date 2022-08-18 9:41
 * @Desc
 */
@Data
public class ApplicationValidateException extends Throwable {
    private static final long serialVersionUID = 1L;

    private Integer code;

    private String message;
}
