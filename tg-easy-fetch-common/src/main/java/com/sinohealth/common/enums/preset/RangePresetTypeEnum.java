package com.sinohealth.common.enums.preset;

import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-24 14:43
 */
public enum RangePresetTypeEnum {

    common("公共"),
    personal("个人");

    private final String desc;

    RangePresetTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }


    public static String getDesc(String type) {
        if (Objects.isNull(type)) {
            return "";
        }
        for (RangePresetTypeEnum value : values()) {
            if (Objects.equals(value.name(), type)) {
                return value.desc;
            }
        }
        return "";
    }
}
