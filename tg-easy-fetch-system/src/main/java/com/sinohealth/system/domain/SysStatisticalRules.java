package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.sinohealth.common.enums.StatisticsType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.sinohealth.common.annotation.Excel;

/**
 * 统计规则对象 sys_statistical_rules
 *
 * @author dataplatform
 * @date 2021-07-30
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("sys_statistical_rules")
public class SysStatisticalRules implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     *
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务组名
     */
    private String jobGroup;

    /**
     * 调用目标字符串
     */
    private String jobInvokeTarget;

    /**
     * cron执行表达式
     */
    private String jobCron;

    /**
     * 计划执行错误策略（1立即执行 2执行一次 3放弃执行）
     */
    private String jobMisfirePolicy;

    /**
     * 是否并发执行（0允许 1禁止）
     */
    private String jobConcurrent;


    /**
     * 状态（0正常 1暂停）
     */
    private String status;

    /**
     * 任务执行周期类型
     * {@link com.sinohealth.common.enums.StatisticsPeriodType}
     */
    private String statisticsPeriodType;

    /**
     * 任务执行时间
     */
    private String statisticsTime;

    /**
     * 统计类型
     * {@link StatisticsType}
     */
    private String statisticsType;

    /**
     * 统计描述
     */
    private String statisticsDescribe;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     *
     */
    private Long createUserId;

    /**
     *
     */
    private Long updateUserId;

}
