package com.sinohealth.common.enums.process;

import lombok.Getter;
import org.apache.commons.lang3.BooleanUtils;

@Getter
public enum FlowProcessStateEnum {
    /**
     * 无需执行
     */
    NOT_EXECUTION_REQUIRED("notExecutionRequired", "无需执行"),

    /**
     * 待执行
     */
    WAIT("wait", "待执行"),

    /**
     * 执行中
     */
    RUNNING("running", "执行中"),

    /**
     * 执行失败
     */
    FAILED("failed", "执行失败"),

    /**
     * 执行成功
     */
    SUCCESS("success", "执行成功");

    private final String code;

    private final String msg;

    FlowProcessStateEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public static String getCode(Boolean open) {
        return BooleanUtils.isTrue(open) ? WAIT.getCode() : NOT_EXECUTION_REQUIRED.getCode();
    }
}
