package com.sinohealth.common.enums.dict;

import com.sinohealth.common.exception.CustomException;
import lombok.Getter;

import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-20 18:45
 */
public enum DeliverTimeTypeEnum {

    month("月度"),
    quarter("季度"),
    halfAYear("半年度"),
    year("年度")
    ;

    @Getter
    final String desc;

    DeliverTimeTypeEnum(String desc) {
        this.desc = desc;
    }

    public static String getTypeDesc(String type) {
        for (DeliverTimeTypeEnum value : values()) {
            if (Objects.equals(value.name(), type)) {
                return value.desc;
            }
        }
        return null;
    }

    public static String getTypeByTransferDesc(String desc) {
        if (desc.contains("月")) {
            return month.name();
        }
        if (desc.contains("季")) {
            return quarter.name();
        }
        if (desc.contains("半年")) {
            return halfAYear.name();
        }

        throw new CustomException("无法匹配时间粒度:" + desc);
    }
}
