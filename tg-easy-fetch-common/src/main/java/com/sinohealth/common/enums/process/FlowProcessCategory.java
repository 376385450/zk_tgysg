package com.sinohealth.common.enums.process;

import lombok.Getter;

@Getter
public enum FlowProcessCategory {
    /**
     * 自动
     */
    AUTO("auto", "自动"),

    /**
     * 手动
     */
    MANUAL_OPERATION("manualOperation", "手动");

    private final String code;

    private final String msg;

    FlowProcessCategory(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
