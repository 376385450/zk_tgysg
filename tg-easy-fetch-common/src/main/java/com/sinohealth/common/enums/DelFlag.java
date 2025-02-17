package com.sinohealth.common.enums;

import lombok.Getter;

@Getter
public enum DelFlag {

    NOT_DEL(0, "未删除"),
    DEL(1, "已删除");

    private int code;

    private String comment;

    DelFlag(int code, String comment) {
        this.code = code;
        this.comment = comment;
    }
}
