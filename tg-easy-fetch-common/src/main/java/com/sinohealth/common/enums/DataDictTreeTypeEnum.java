package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Huangzk
 * @date 2021/5/20 15:26
 */
@Getter
@AllArgsConstructor
public enum DataDictTreeTypeEnum {

    DD(1, "数据字典"),
    CR(2, "编码目录"),
    BC(3, "行业概念");

    Integer code;
    String name;

    public static DataDictTreeTypeEnum valueOfCode(Integer code) {
        if (code == null) return null;
        for (DataDictTreeTypeEnum value : values()) {
            if (value.getCode().equals(code)) return value;
        }
        return null;
    }




}
