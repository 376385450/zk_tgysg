package com.sinohealth.common.exception;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-09 17:20
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException() {
        super();
    }

    public RateLimitException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
