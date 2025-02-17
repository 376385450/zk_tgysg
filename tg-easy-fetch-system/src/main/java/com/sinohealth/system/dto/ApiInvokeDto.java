package com.sinohealth.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ApiInvokeDto implements Serializable {

    private String date;

    private long totalNum;

    private long errorNum;

    private Integer invokeType;

    private String failRateStr;

    private BigDecimal failRate;

    public ApiInvokeDto(){}

}
