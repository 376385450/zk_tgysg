package com.sinohealth.common.enums;

import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: ChenJiaRong
 * Date:   2021/7/27
 * Explain: 数据标准枚举
 */

public enum DataStandardType {
    UNKNOWN(-1),DICTIONARIES(1),CATALOGUE(2),CONCEPT(3);

    DataStandardType(Integer type) {
        this.type = type;
    }

    private Integer type;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public static DataStandardType getDataStandardType(Integer type) {
        List<DataStandardType> collect = Arrays.stream(values()).filter(v -> v.getType().equals(type)).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(collect)) {
            return UNKNOWN;
        }
        return collect.get(0);
    }
}
