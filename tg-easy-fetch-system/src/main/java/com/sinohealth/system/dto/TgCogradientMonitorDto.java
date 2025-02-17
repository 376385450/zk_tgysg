package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("TgCogradientMonitorDto")
public class TgCogradientMonitorDto {

    @ApiModelProperty("表id")
    private Long tableId;

    @ApiModelProperty("表名称")
    private String tableName;

    @ApiModelProperty("ds流程定义ids")
    private String processIds;

    @ApiModelProperty("进行中个数")
    private  int runningCnt;

    @ApiModelProperty("失败个数")
    private  int failCnt;

    @ApiModelProperty("成功个数")
    private  int successCnt;
}
