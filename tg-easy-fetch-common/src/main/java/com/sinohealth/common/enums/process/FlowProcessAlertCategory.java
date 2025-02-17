package com.sinohealth.common.enums.process;

import lombok.Getter;

@Getter
public enum FlowProcessAlertCategory {
    /**
     * 全流程
     */
    FLOW_PROCESS("flowProcess", "全流程"),

    /**
     * 子流程
     */
    SUB_PROCESS("subProcess", "子流程");

    private final String code;

    private final String msg;

    FlowProcessAlertCategory(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
