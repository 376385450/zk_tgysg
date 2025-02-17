package com.sinohealth.common.enums;

import java.util.Objects;

public enum StatusTypeEnum {
    IS_DELETE(0, "删除"),
    IS_ENABLE(1, "启用"),
    IS_DISABLE(2, "禁用"),
    ;

    private int id;
    private String name;

    private StatusTypeEnum(int id, String name) {
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
        for (StatusTypeEnum a : StatusTypeEnum.values()) {
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
        for (StatusTypeEnum a : StatusTypeEnum.values()) {
            if (a.id == val) {
                return a.id;
            }
        }
        return null;
    }
}
