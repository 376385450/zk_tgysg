package com.sinohealth.common.enums.application;

import com.sinohealth.common.enums.ExecutionStatus;

import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-12-26 18:19
 */
public enum ApplyDataStateEnum {

    /**
     * 未执行
     */
    none("未执行"),
    /**
     * 待确认
     */
    wait_confirm("待确认"),
    /**
     * 执行中
     */
    run("执行中"),
    /**
     * 执行成功
     */
    success("执行成功"),
    /**
     * 执行失败
     */
    fail("执行失败");

    private final String desc;

    ApplyDataStateEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static ApplyDataStateEnum ofFlowState(Integer state) {
        if (ExecutionStatus.SUCCEED.contains(state)) {
            return ApplyDataStateEnum.success;
        } else {
            return ApplyDataStateEnum.fail;
        }
    }

    public static boolean isEnd(String state) {
        return Objects.equals(state, success.name()) || Objects.equals(state, fail.name());
    }

    public static boolean isSuccess(String state) {
        return Objects.equals(state, success.name());
    }

    public static String getDesc(String type){
        for (ApplyDataStateEnum value : values()) {
            if (Objects.equals(value.name(), type)) {
                return value.getDesc();
            }
        }
        return "";
    }

}
