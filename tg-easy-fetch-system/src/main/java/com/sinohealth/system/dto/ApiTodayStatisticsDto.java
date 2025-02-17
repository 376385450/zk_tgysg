package com.sinohealth.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ApiTodayStatisticsDto<T> implements Serializable {

    private String name;

    private String code;

    private List<T> data;

}
