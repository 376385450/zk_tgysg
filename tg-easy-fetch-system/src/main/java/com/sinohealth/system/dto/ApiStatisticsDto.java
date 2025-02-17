package com.sinohealth.system.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiStatisticsDto implements Serializable {

    private String name;

    private String code;

    private long totalNum;

    private long todayNum;

}
