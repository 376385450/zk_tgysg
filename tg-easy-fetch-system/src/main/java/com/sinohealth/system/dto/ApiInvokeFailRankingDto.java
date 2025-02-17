package com.sinohealth.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ApiInvokeFailRankingDto implements Serializable {

    private String groupName;

    private String apiName;

    private Long apiId;

    private long totalNum;

    private long errorNum;

    private String failRateStr;

    private BigDecimal failRate;
}
