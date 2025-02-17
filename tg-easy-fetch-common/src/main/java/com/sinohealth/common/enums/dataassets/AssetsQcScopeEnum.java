package com.sinohealth.common.enums.dataassets;

import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-06-17 17:48
 */
public enum AssetsQcScopeEnum {
    split("项目拆分"), merge("项目合并");

    final String desc;

    AssetsQcScopeEnum(String desc) {
        this.desc = desc;
    }

    public static String getDesc(String key) {
        for (AssetsQcScopeEnum value : values()) {
            if (Objects.equals(value.name(), key)) {
                return value.desc;
            }
        }
        return null;
    }
}
