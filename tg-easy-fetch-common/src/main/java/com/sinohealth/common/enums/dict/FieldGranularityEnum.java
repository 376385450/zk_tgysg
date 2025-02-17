package com.sinohealth.common.enums.dict;

import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 17:24
 */
public enum FieldGranularityEnum {
    time("时间"),
    area("市场"),
    product("产品"),
    member("会员"),
    other("其他"),
    metrics("指标"),
    ;

    private final String desc;

    FieldGranularityEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }


    public static String getDesc(String type) {
        if (Objects.isNull(type)) {
            return "";
        }
        for (FieldGranularityEnum value : values()) {
            if (Objects.equals(value.name(), type)) {
                return value.desc;
            }
        }
        return "";
    }
}
