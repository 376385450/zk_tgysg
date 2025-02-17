package com.sinohealth.common.enums;


import lombok.Getter;

public enum SortType {
    DEFAULT("DEFAULT"),
    LATEST("LATEST"),
    APPLIED_TIMES("APPLIED_TIMES"),
    VIEW_TIMES("VIEW_TIMES");

    @Getter
    private String type;

    SortType(String type) {
        this.type = type;
    }
}
