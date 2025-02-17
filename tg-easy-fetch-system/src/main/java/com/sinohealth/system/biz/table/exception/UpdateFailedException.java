package com.sinohealth.system.biz.table.exception;

public class UpdateFailedException extends RuntimeException{

    public int code = 1001;

    public UpdateFailedException(String message) {
        super(message);
    }

}
