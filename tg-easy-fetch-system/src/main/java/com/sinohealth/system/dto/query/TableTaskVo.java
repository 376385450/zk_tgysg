package com.sinohealth.system.dto.query;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TableTaskVo implements Serializable {

    @ApiModelProperty("任务类型（1导出，2复制）")
    private Integer taskType;

    @ApiModelProperty("进度（0失败,1成功,2进行中）")
    private Integer speedOfProgress;

    @ApiModelProperty("创建用户")
    private String operator;

    @ApiModelProperty("开始时间")
    private Date startTime;

    @ApiModelProperty("结束时间")
    private Date endTime;
}
