package com.sinohealth.system.biz.table.exception;

public class UpdateNoticeException extends RuntimeException {

    public int code = 1002;

    public UpdateNoticeException(String message) {
        super(message);
    }
}
