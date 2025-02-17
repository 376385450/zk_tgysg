package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 表任务对象 table_task
 *
 * @author liruifa
 * @date 2021-06-28
 */
@Data
public class TableTaskDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @ApiModelProperty("任务类型（1导出，2复制）")
    private String taskType;

    @ApiModelProperty("进度（0失败,1成功,2进行中）")
    private String speedOfProgress;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("完成时间")
    private Date completeTime;

    @ApiModelProperty("耗时")
    private String useTime;

    @ApiModelProperty("创建用户")
    private String operator;

    @ApiModelProperty("备注")
    private String remarks;

    @ApiModelProperty("任务内容")
    private String content;

    @ApiModelProperty("任务批号")
    private String batchNumber;

    @ApiModelProperty("任务结果")
    private String result;
}
