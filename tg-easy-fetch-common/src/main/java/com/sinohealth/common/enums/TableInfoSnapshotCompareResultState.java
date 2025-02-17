package com.sinohealth.common.enums;

import lombok.Getter;

/**
 * @author monster
 * @Date 2024-07-11 13:52
 */
public enum TableInfoSnapshotCompareResultState {
    /**
     * 正常
     */
    NORMAL("normal", "正常"),

    /**
     * 已删除
     */
    DELETED("deleted", "已删除"),

    /**
     * 初始状态，未有结果表
     */
    INIT("init", "初始状态，未有结果表");

    @Getter
    private String type;

    @Getter
    private String describe;

    TableInfoSnapshotCompareResultState(String type, String describe) {
        this.type = type;
        this.describe = describe;
    }
}
