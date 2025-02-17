package com.sinohealth.common.enums;

import lombok.Getter;

/**
 * @author monster
 * @Date 2024-07-11 13:52
 */
public enum TableInfoSnapshotCompareState {
    /**
     * 待执行
     */
    WAITING("waiting", "待执行"),
    
    /**
     * 执行中
     */
    RUNNING("running", "执行中"),

    /**
     * 执行成功
     */
    COMPLETED("completed", "执行成功"),

    /**
     * 执行失败
     */
    FAIL("fail", "执行失败");

    @Getter
    private String type;

    @Getter
    private String describe;

    TableInfoSnapshotCompareState(String type, String describe) {
        this.type = type;
        this.describe = describe;
    }
}
