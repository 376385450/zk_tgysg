package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Huangzk
 * @date 2021/5/12 15:59
 */
@Getter
@AllArgsConstructor
public enum DataDictTreeStatusEnum {

    //删除状态
    DELETE(0),
    //正常状态
    NORMAL(1);

    Integer status;

    public static DataDictTreeStatusEnum valueOfCode(Integer value) {
        if (value == null) return null;
        for (DataDictTreeStatusEnum statusEnum : DataDictTreeStatusEnum.values()) {
            if (statusEnum.getStatus().equals(value)) {
                return statusEnum;
            }
        }
        return null;
    }
}
