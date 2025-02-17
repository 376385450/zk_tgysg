package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;



/**
 * 统计结果添加对象 sys_statistical_result
 *
 * @author dataplatform
 * @date 2021-08-02
 */
@Data
@ApiModel("统计结果添加对象")
public class SysStatisticalResuDto {

    /** 统计任务id */
    @ApiModelProperty("统计任务id")
    private Long statisticalId;
    /** 表id */
    @ApiModelProperty("表id")
    private Long tableId;
    /** 任务名称 */
    @ApiModelProperty("任务名称")
    private String jobName;
    /** 任务组名 */
    @ApiModelProperty("任务组名")
    private String jobGroup;
    /** 调用目标字符串 */
    @ApiModelProperty("调用目标字符串")
    private String invokeTarget;
    /** 日志信息 */
    @ApiModelProperty("日志信息")
    private String jobMessage;
    /** 执行状态（0失败,1成功,2进行中） */
    @ApiModelProperty("执行状态（0失败,1成功,2进行中）")
    private String status;
    /** 异常信息 */
    @ApiModelProperty("异常信息")
    private String exceptionInfo;
    /**  */
    @ApiModelProperty("")
    private String tableName;
    /**  */
    @ApiModelProperty("")
    private String tableAlias;
    /** 体量 */
    @ApiModelProperty("体量")
    private long tableMake;
    /** 执行类型（1自动，2手动） */
    @ApiModelProperty("执行类型（1自动，2手动）")
    private String statisticalType;
    /** 创建时间 */
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /** 创建者 */
    @ApiModelProperty("创建者")
    private String createBy;
    /**  */
    @ApiModelProperty("")
    private Long createUserId;
}
