package com.sinohealth.system.biz.application.constants;

import lombok.Getter;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-02-23 13:45
 */
@Getter
public enum TopPeriodTypeEnum {
    fixed(0, "固定"),
    dynamic(1, "动态");

    private final int code;
    private final String desc;

    TopPeriodTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
