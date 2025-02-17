package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.sinohealth.common.annotation.Excel;

/**
 * 统计结果对象 sys_statistical_result
 *
 * @author dataplatform
 * @date 2021-08-02
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("sys_statistical_result")
public class SysStatisticalResult implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     *
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 统计任务id
     */
    private Long statisticalId;

    /**
     * 表id
     */
    private Long tableId;

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
    private String invokeTarget;

    /**
     * 日志信息
     */
    private String jobMessage;

    /**
     * 执行状态（0失败,1成功,2进行中）
     */
    private String status;

    /**
     * 异常信息
     */
    private String exceptionInfo;

    /**
     *
     */
    private String tableName;

    /**
     *
     */
    private String tableAlias;

    /**
     * 体量
     */
    private long tableMake;
    /**
     * 行数
     */
    private long totalRows;

    /**
     * 执行类型（1自动，2手动）
     */
    private String statisticalType;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     *
     */
    private Long createUserId;

}
