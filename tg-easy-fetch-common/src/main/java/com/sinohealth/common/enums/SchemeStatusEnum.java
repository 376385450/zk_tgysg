package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Huangzk
 * @date 2021/7/06 15:26
 */
@Getter
@AllArgsConstructor
public enum SchemeStatusEnum {

    STATIC(0, "静态"),
    DYNAMIC(1, "动态");

    Integer code;
    String name;

    public static SchemeStatusEnum valueOfCode(Integer code) {
        if (code == null) return null;
        for (SchemeStatusEnum value : values()) {
            if (value.getCode().equals(code)) return value;
        }
        return null;
    }




}
