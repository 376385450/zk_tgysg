package com.sinohealth.common.enums.dict;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-08 16:58
 */
public enum DataDictDataTypeEnum {
    Decimal, String, DateTime;

    public static boolean isDecimal(String type) {
        return Decimal.name().equalsIgnoreCase(type);
    }
}
