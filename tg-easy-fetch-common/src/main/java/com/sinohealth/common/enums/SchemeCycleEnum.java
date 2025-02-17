package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Huangzk
 * @date 2021/7/06 15:26
 */
@Getter
@AllArgsConstructor
public enum SchemeCycleEnum {

    SCHEME_CURRENT(1, "实时更新"),
    SCHEME_HOUR(2, "每小时更新"),
    SCHEME_DAY(3, "每日更新"),
    SCHEME_NOT(4, "不更新"),
    SCHEME_WEEK(5, "每周更新"),
    SCHEME_MONTH(6, "每月更新"),
    SCHEME_SEASON(7, "每季度更新"),
    SCHEME_YEAR(8, "每年更新"),
    ;

    Integer code;
    String name;

    public static SchemeCycleEnum valueOfCode(Integer code) {
        if (code == null) return null;
        for (SchemeCycleEnum value : values()) {
            if (value.getCode().equals(code)) return value;
        }
        return null;
    }


}
