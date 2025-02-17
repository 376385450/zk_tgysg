package com.sinohealth.common.exception;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-04-02 10:01
 */
public class FTPException extends CustomException {

    public FTPException(String message) {
        super(message);
    }

    public FTPException(String message, Integer code) {
        super(message, code);
    }
}
