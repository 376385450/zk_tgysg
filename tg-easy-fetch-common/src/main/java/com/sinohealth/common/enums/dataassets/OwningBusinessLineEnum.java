package com.sinohealth.common.enums.dataassets;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author shallwetalk
 * @Date 2023/9/5
 */
@Getter
@AllArgsConstructor
public enum OwningBusinessLineEnum {

    small_ticket("小票"),
    main_data("主数据"),
    hospital("医院"),
    other("其他"),
    B2C("B2C"),
    O2O("O2O"),
    cmh("CMH");


    String typeName;

    public static String getNameByValue(String value) {
        for (OwningBusinessLineEnum owningBusinessLineEnum : OwningBusinessLineEnum.values()) {
            if (owningBusinessLineEnum.name().equals(value)) {
                return owningBusinessLineEnum.getTypeName();
            }
        }
        return null;
    }

}
