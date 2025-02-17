package com.sinohealth.system.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import com.sinohealth.common.enums.StatisticsPeriodType;
import com.sinohealth.common.enums.StatisticsType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @program:
 * @description:
 * @author: ChenJiaRong
 * @date: 2021/8/2
 **/
@Data
@ApiModel("统计规则显示实体")
public class StatisticalInfoDto {
    /**
     *
     */
    @TableId(value = "id")
    private Long id;
    /**
     * 统计描述
     */
    @ApiModelProperty("统计描述")
    private String statisticsDescribe;
    /**
     * 任务名称
     */
    @ApiModelProperty("任务名称")
    private String jobName;
    /**
     * 统计类型
     * {@link StatisticsType}
     */
    @ApiModelProperty("统计类型")
    private String statisticsType;

    @ApiModelProperty("任务执行周期")
    private String statisticsPeriodName;

    public String getStatisticsPeriodName() {
        return StatisticsPeriodType.findType(statisticsPeriodType).getDescribe();
    }

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

    @ApiModelProperty("统计的表单数量")
    private Integer statisticalTableCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private Date createTime;
    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("最后一次统计时间")
    private Date finallyStatisticalData;


    @ApiModelProperty("统计状态")
    private String statisticalStatus;

    /**
     * 状态（0正常 1暂停）
     */
    @ApiModelProperty("状态（0正常 1暂停）")
    private String status;

    @ApiModelProperty("统计表id")
    private List<Long> tableIds;


}
