package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Huangzk
 * @date 2021/7/05 15:59
 */
@Getter
@AllArgsConstructor
public enum SortingFieldEnum {

    //删除状态
    ORDER_ASC("asc"),
    //正常状态
    ORDER_DESC("desc");

    String status;



    public static SortingFieldEnum valueOfCode(String value) {
        if (value == null) return null;
        for (SortingFieldEnum statusEnum : SortingFieldEnum.values()) {
            if (statusEnum.getStatus().equals(value)) {
                return statusEnum;
            }
        }
        return null;
    }
}
