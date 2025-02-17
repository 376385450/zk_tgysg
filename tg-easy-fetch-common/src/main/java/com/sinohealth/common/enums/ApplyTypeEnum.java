package com.sinohealth.common.enums;

import java.util.Objects;

public enum ApplyTypeEnum {
    IS_TABLE(1, "数据表权限"),
    IS_API_SUBSCRIBE(2, "接口服务--订阅"),
    IS_API_RELEASE(3, "接口服务--发布"),
    ;



    private int id;
    private String name;

    private ApplyTypeEnum(int id, String name) {
        this.id = id;
        this.name = name;
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

    public static String getStatusName(Integer val) {
        for (ApplyTypeEnum a : ApplyTypeEnum.values()) {
            if (a.id == val) {
                return a.name;
            }
        }
        return null;
    }

    public static Integer getStatus(Integer val) {
        if(Objects.isNull(val)){
            return null;
        }
        for (ApplyTypeEnum a : ApplyTypeEnum.values()) {
            if (a.id == val) {
                return a.id;
            }
        }
        return null;
    }
}
