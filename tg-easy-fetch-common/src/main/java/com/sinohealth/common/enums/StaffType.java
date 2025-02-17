package com.sinohealth.common.enums;

import lombok.Getter;

/**
 * @Author Rudolph
 * @Date 2023-08-19 16:20
 * @Desc
 */
@Getter
public enum StaffType {

    DEPT(1,"DEPT", "部门"),
    USER(2,"USER", "人员");

    private Integer id;
    private String type;
    private String describe;

    StaffType(Integer id, String type, String describe) {
        this.id = id;
        this.type = type;
        this.describe = describe;
    }
}
