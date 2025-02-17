package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Huangzk
 * @date 2021/5/20 15:26
 */
@Getter
@AllArgsConstructor
public enum OperationTypeEnum {

    INSERT(1, "INSERT"),
    UPDATE(2, "UPDATE"),
    DELETE(3, "DELETE");

    Integer code;
    String name;

    public static OperationTypeEnum valueOfCode(Integer code) {
        if (code == null) return null;
        for (OperationTypeEnum value : values()) {
            if (value.getCode().equals(code)) return value;
        }
        return null;
    }


}
