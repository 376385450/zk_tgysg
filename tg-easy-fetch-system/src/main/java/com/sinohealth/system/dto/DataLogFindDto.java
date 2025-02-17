package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @program:
 * @description:
 * @author: ChenJiaRong
 * @date: 2021/8/3
 **/
@Data
@ApiModel("")
public class DataLogFindDto {

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;
}
