package com.sinohealth.common.enums.process;

import lombok.Getter;

@Getter
public enum FlowProcessUpdateType {
    /**
     * 全量
     */
    ALL("all", "全量"),

    /**
     * 按品类
     */
    type("type", "按品类"),

    /**
     * 首次全量，其余按品类
     */
    ALL_OR_TYPE("allOrType", "首次全量，其余按品类"),

    ;


    private final String code;

    private final String desc;

    FlowProcessUpdateType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
