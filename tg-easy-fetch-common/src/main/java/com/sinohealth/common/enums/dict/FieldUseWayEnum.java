package com.sinohealth.common.enums.dict;

import java.util.Objects;

/**
 * 字段 使用途径
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 09:45
 */
public enum FieldUseWayEnum {
    normal("标准"),
    custom("自定义");

    private final String desc;

    FieldUseWayEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static String getDesc(String type){
        if (Objects.isNull(type)) {
            return "";
        }
        for (FieldUseWayEnum value : values()) {
            if (Objects.equals(value.name(), type)) {
                return value.desc;
            }
        }
        return "";
    }
}