package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;


/**
 * 统计数据库中间添加对象 sys_statistical_table
 *
 * @author dataplatform
 * @date 2021-08-02
 */
@Data
@ApiModel("统计数据库中间添加对象")
public class SysStatisticalTableDto {

    /**
     * 统计任务id
     */
    @ApiModelProperty("统计任务id")
    private Long statisticalId;
    /**
     * 表id
     */
    @ApiModelProperty("表id")
    private Long tableId;
}
