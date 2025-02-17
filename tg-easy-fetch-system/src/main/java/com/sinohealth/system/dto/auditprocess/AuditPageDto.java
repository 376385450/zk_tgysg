package com.sinohealth.system.dto.auditprocess;

import com.baomidou.mybatisplus.annotation.TableField;
import com.sinohealth.common.enums.dataassets.AcceptanceStateEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-05-23 16:06
 * @Desc
 */
@Data
public class AuditPageDto {
    private Long applicationId;
    private Long newApplicationId;
    private Long applicantId;
    private String baseTableName;
    private String tableAlias;
    /**
     * 需求名
     */
    private String projectName;
    /**
     * 项目名
     */
    private String newProjectName;
    private String templateName;
    private Integer requireAttr;
    private Integer requireTimeType;
    private Date dataExpir;
    @ApiModelProperty("模板类型")
    private String templateType;

    private String applicantName;
    private String applicantDepartment;
    private String createTime;
    private String applyType;
    private Integer currentAuditProcessStatus;

    private Integer currentIndex;

    private String currentHandlers;

    /**
     * 当前节点审批人
     */
    private String currentHandlerNames;

    /**
     * 最后审批人
     */
    private String lastHandlerName;

    /**
     * 申请原因
     */
    private String applyComment;
    /**
     * 文档名称
     */
    private String docName;

    @TableField(exist = false)
    private Integer currentAuditNodeStatus;
    @TableField(exist = false)
    private Integer handleStatus;
    @ApiModelProperty("审核原因")
    private String handleReason;
    @ApiModelProperty("文档权限")
    private List<Integer> docAuthorization;
    @ApiModelProperty("0：SQL模式，1：工作流模式")
    private Integer configType;


    @ApiModelProperty("配置类型 0:SQL,1:工作流(默认0)")
    private Integer type;

    @ApiModelProperty("SQL文本")
    private String sql;

    @ApiModelProperty("工作流ID")
    private Integer workflowId;

    @ApiModelProperty("启动定时任务")
    private Boolean enableScheduledTask;

    @ApiModelProperty("cron表达式")
    private String cron;

    /**
     * @see AcceptanceStateEnum
     */
    private String state;
}
