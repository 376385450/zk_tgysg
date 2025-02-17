package com.sinohealth.common.enums;

import lombok.Getter;

@Getter
public enum IsCollectEnum {

    YES(1, "已收藏"),
    NO(0, "未收藏");

    private Integer code;

    private String comment;

    IsCollectEnum(Integer code, String comment) {
        this.code = code;
        this.comment = comment;
    }
}
