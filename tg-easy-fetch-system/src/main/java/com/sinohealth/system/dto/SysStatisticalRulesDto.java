package com.sinohealth.system.dto;

import com.sinohealth.common.module.file.constant.FileConst;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;


/**
 * 统计规则添加对象 sys_statistical_rules
 *
 * @author dataplatform
 * @date 2021-07-30
 */
@Data
@ApiModel("统计规则添加对象")
public class SysStatisticalRulesDto {

    private Long id;
    /**
     * 任务名称
     */
    @ApiModelProperty("任务名称")
    private String jobName;


    /**
     * 任务执行周期类型
     * {@link com.sinohealth.common.enums.StatisticsPeriodType}
     */
    @ApiModelProperty("任务执行周期类型")
    private String statisticsPeriodType;
    /**
     * 任务执行时间
     */
    @ApiModelProperty("任务执行时间")
    @JsonFormat(pattern = "HH:mm:ss")
    private String statisticsTime;
    /**
     * 统计类型
     */
    @ApiModelProperty("统计类型")
    private String statisticsType;
    /**
     * 统计描述
     */
    @ApiModelProperty("统计描述")
    private String statisticsDescribe;

    @ApiModelProperty("统计表id")
    private List<Long> tableIds;
}
