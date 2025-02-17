package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 表任务对象 table_task
 *
 * @author liruifa
 * @date 2021-06-28
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("table_task")
@ApiModel("table_task")
public class TableTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    private Long dirId;

    private Long tableId;

    @ApiModelProperty("参数")
    private String params;

    @ApiModelProperty("任务类型（1导出，2复制）")
    private Integer taskType;

    @ApiModelProperty("进度（0失败,1成功,2进行中）")
    private Integer speedOfProgress;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("完成时间")
    private Date completeTime;

    @ApiModelProperty("创建用户ID")
    private Long operatorId;

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
