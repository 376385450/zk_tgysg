package com.sinohealth.common.enums;

import lombok.Getter;

/**
 * @Author Rudolph
 * @Date 2023-08-08 10:19
 * @Desc
 */
@Getter
public enum ShowUnit {
    HOUR("H", "小时"),
    MINUTE("M", "分钟"),
    SECOND("S", "秒");

    private String type;

    private String alias;

    ShowUnit(String type, String alias) {
        this.type = type;
        this.alias = alias;
    }

}
