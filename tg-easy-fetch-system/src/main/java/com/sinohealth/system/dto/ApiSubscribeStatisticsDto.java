package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(value = "接口统计")
public class ApiSubscribeStatisticsDto implements Serializable {

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "用户名称")
    private String userName;

    @ApiModelProperty(value = "用户所属组")
    private String userGroupName;

    @ApiModelProperty(value = "用户订阅数")
    private long subscribeNum;

    @ApiModelProperty(value = "订阅接口调用次数")
    private long invokeNum;


}
