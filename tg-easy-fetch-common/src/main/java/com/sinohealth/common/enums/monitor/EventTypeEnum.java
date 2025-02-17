package com.sinohealth.common.enums.monitor;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 11:57 上午
 */
public enum EventTypeEnum {

    OPERATE("OPERATE", "操作日志")

    ;

    private String type;
    private String msg;

    EventTypeEnum(String type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public String getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }

}
