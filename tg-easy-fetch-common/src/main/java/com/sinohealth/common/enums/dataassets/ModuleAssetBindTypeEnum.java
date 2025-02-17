package com.sinohealth.common.enums.dataassets;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author shallwetalk
 * @Date 2023/9/5
 */
@AllArgsConstructor
@Getter
public enum ModuleAssetBindTypeEnum {

    wide_table("宽表模板"),
    normal("常规模板"),
    customized("通用模板");


    String typeName;

    public static String getNameByValue(String value) {
        for (ModuleAssetBindTypeEnum moduleAssetBindTypeEnum : ModuleAssetBindTypeEnum.values()) {
            if (moduleAssetBindTypeEnum.name().equals(value)) {
                return moduleAssetBindTypeEnum.getTypeName();
            }
        }
        return null;
    }

}
