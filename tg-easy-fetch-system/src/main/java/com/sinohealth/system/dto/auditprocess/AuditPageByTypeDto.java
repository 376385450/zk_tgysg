package com.sinohealth.system.dto.auditprocess;

import com.baomidou.mybatisplus.annotation.TableField;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.common.enums.dataassets.AcceptanceStateEnum;
import com.sinohealth.system.biz.dataassets.dto.FileAssetsUploadDTO;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.domain.constant.ApplicationConst;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @Author: 张子锋
 * @Date: 2023/8/27 21:43
 */
@Data
public class AuditPageByTypeDto {

    private Long applicationId;
    private String applicationNo;
    private Long assetsId;
    private Long customerId;
    private String customerShortName;
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
    private String readableUsers;
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
    /**
     * @see ApplicationConfigTypeConstant
     */
    @ApiModelProperty("0：SQL模式，1：工作流模式 2文件")
    private Integer configType;

    private FileAssetsUploadDTO assetsAttach;

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

    @ApiModelProperty("资产ID")
    private Long assetId;

    @ApiModelProperty("资产名称")
    private String assetName;

    @ApiModelProperty("资产类型")
    private AssetType assetType;

    @ApiModelProperty("交换任务名称")
    private String syncTaskName;

    @ApiModelProperty("服务类型")
    private String serviceType;

    /**
     * @see AcceptanceStateEnum
     */
    private String state;

    @ApiModelProperty("申请原因")
    private String applyReason;

    /**
     * 出数状态
     *
     * @see ApplyDataStateEnum
     */
    private String dataState;

    private Boolean relateDict;

    private String productGra;

    /**
     * 需求个数
     */
    private Integer dataAmount;
    /**
     * 需求成本，单位p
     */
    private BigDecimal dataCost;

    /**
     * 更多
     *
     * @see ApplicationConst.AuditAction
     */
    private List<Integer> actionList;
}
