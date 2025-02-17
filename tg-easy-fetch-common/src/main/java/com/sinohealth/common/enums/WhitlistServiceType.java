package com.sinohealth.common.enums;

import lombok.Getter;

/**
 * @Author shallwetalk
 * @Date 2023/8/15
 */
@Getter
public enum WhitlistServiceType {


    READABLE("READABLE_AUTH", "资产阅读权限"),

    SERVICE_AUTH("SERVICE_AUTH", "服务白名单");


    private String type;
    private String describe;

    WhitlistServiceType(String type, String describe) {
        this.type = type;
        this.describe = describe;
    }

}
