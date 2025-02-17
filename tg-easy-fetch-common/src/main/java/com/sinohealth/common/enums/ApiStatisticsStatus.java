package com.sinohealth.common.enums;

import java.util.Objects;

public enum ApiStatisticsStatus {
    API_INVOKE(1, "API调用总次数","API_INVOKE"),
    API_RELEASE(2, "API服务总个数","API_RELEASE"),
    API_SUBSCRIBE(3, "API订阅总人数","API_SUBSCRIBE"),
    API_INVOKE_ERROR(4, "API调用失败次数","API_INVOKE_ERROR"),
    API_INVOKE_ERROR_RATE(5, "API调用失败率","API_INVOKE_ERROR_RATE"),

    ;


    private int id;
    private String name;
    private String code;

    private ApiStatisticsStatus(int id, String name,String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static String getStatusName(Integer val) {
        for (ApiStatisticsStatus a : ApiStatisticsStatus.values()) {
            if (a.id == val) {
                return a.name;
            }
        }
        return null;
    }
    public static String getStatusCode(Integer val) {
        for (ApiStatisticsStatus a : ApiStatisticsStatus.values()) {
            if (a.id == val) {
                return a.code;
            }
        }
        return null;
    }

    public static Integer getStatus(Integer val) {
        if(Objects.isNull(val)){
            return null;
        }
        for (ApiStatisticsStatus a : ApiStatisticsStatus.values()) {
            if (a.id == val) {
                return a.id;
            }
        }
        return null;
    }
}
