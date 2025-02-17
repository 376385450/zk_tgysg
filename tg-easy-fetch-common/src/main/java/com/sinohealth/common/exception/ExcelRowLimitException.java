package com.sinohealth.common.exception;

import lombok.Getter;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2024-01-22 17:10
 */
public class ExcelRowLimitException extends RuntimeException {

    @Getter
    private String message;

    public ExcelRowLimitException(String message) {
        this.message = message;
    }
}
