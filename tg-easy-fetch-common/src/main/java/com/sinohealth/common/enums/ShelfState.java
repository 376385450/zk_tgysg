package com.sinohealth.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author shallwetalk
 * @Date 2023/8/24
 */
@Getter
@AllArgsConstructor
public enum ShelfState {

    LISTING("已上架"),
    DELIST("已下架"),
    UNLIST("未上架");

    private String status;

}
