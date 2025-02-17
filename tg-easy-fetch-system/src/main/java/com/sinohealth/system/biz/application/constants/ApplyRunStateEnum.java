package com.sinohealth.system.biz.application.constants;

/**
 * 需求单 流程执行状态
 *
 * @author Kuangcp
 * 2024-12-10 13:41
 * @see ApplyStateEnum
 */
public enum ApplyRunStateEnum {

    /**
     * 待审核
     */
    wait_audit("待审核"),
    /**
     * 已驳回
     */
    audit_reject("已驳回"),
    /**
     * 待处理 审核通过
     * 特殊处理：取最新期数
     */
    wait_run("待处理"),
    /**
     * 处理中
     */
    running("处理中"),
    /**
     * 处理失败
     */
    run_failed("处理失败"),
    /**
     * 待验收 处理成功
     */
    wait_accept("待验收"),
    /**
     * 完成
     */
    finish("完成");


    private final String desc;

    ApplyRunStateEnum(String desc) {
        this.desc = desc;
    }


    public static String getDesc(String type) {
        for (ApplyRunStateEnum value : values()) {
            if (value.name().equals(type)) {
                return value.desc;
            }
        }
        return "";
    }
}
