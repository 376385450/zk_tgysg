package com.sinohealth.common.enums;

import lombok.Getter;

@Getter
public enum PersonalServiceStatusEnum {

    VALID(1, "可使用"),
    EXPIRED(0, "已过期"),
    INVALID(-1, "不可用");


    private Integer code;

    private String comment;

    PersonalServiceStatusEnum(Integer code, String comment) {
        this.code = code;
        this.comment = comment;
    }
}
