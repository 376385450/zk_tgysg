package com.sinohealth.common.enums.customer;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author shallwetalk
 * @Date 2024/1/10
 */
@AllArgsConstructor
@Getter
public enum CustomerTypeEnum {

    KA(1,"KA"),
    TOP_INDUSTRIES(2,"百强工业"),
    NOT_TOP_INDUSTRIES(3,"非百强工业"),
    CHAIN(4,"连锁"),
    OTHER(5,"其他"),
    SINOHEALTH(6,"中康");

    private Integer type;

    private String name;

}
