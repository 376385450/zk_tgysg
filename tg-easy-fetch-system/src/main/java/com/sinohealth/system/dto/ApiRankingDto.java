package com.sinohealth.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiRankingDto implements Serializable {

    private String groupName;

    private String apiName;

    private Long apiId;

    private long totalNum;

}
