package com.sinohealth.common.enums.dict;

import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 15:53
 */
public enum MetricsTypeEnum {
    /**
     * 常规指标
     */
    normal("常规指标"),
    /**
     * 计算指标
     */
    formula("计算指标"),
    /**
     * 预设指标
     */
    preset("预设指标");

    private final String desc;

    MetricsTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static String getDesc(String type) {
        for (MetricsTypeEnum value : values()) {
            if (Objects.equals(value.name(), type)) {
                return value.desc;
            }
        }
        return "";
    }
}
