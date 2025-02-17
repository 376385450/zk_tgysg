package com.sinohealth.common.enums.monitor;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 2:00 下午
 */
public enum SubjectTypeEnum {

    MAP("map", "地图目录"),

    CUSTOMER("customer","客户资产")

    ;

    private String type;

    private String msg;

    SubjectTypeEnum(String type, String msg) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
