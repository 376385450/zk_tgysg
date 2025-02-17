package com.sinohealth.system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.ibatis.annotations.ConstructorArgs;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class ApiTodayDto implements Serializable {

    private String date;

    private Object totalNum;

    private Integer sort;

    public ApiTodayDto(){}

}
