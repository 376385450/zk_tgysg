package com.sinohealth.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ApiInvokeTodayDto<T> implements Serializable {

    private String date;

    private T data;

    private Integer sort;

    public ApiInvokeTodayDto(){}
}
