package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("TgCogradientDetailDto")
public class TgCogradientDetailDto {

    @ApiModelProperty("任务ID")
    private Long id;

    @ApiModelProperty("ds任务实例ID")
    private Integer taskId;

    @ApiModelProperty("表id")
    private Long tableId;

    @ApiModelProperty("表名称")
    private String tableName;

    @ApiModelProperty("ds流程id")
   private Integer processId;

    @ApiModelProperty("任务名称")
    private String name;

    @ApiModelProperty("同步状态")
    private Integer state;

    @ApiModelProperty("数据总量")
    private Long totalCnt;

    @ApiModelProperty("变更数量")
    private  Long  changeCnt;

    @ApiModelProperty("调度时间")
    private String submitTime;

    @ApiModelProperty("结束时间")
    private String endTime;



}
