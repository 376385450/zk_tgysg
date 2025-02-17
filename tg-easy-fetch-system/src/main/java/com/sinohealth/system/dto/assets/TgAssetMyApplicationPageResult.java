package com.sinohealth.system.dto.assets;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.system.biz.project.domain.Project;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author Rudolph
 * @Date 2023-09-03 14:17
 * @Desc
 */
@ApiModel(description = "/api/asset/my_application_query 返回的数据类")
@Data
public class TgAssetMyApplicationPageResult {

    @ApiModelProperty("申请ID")
    private Long applicationId;
    @ApiModelProperty("资产ID")
    private Long assetId;
    @ApiModelProperty("关联实体ID(模型/库表/文件)")
    private Long relatedId;
    @ApiModelProperty("审批流程ID")
    private Long processId;
    @ApiModelProperty("资产名称")
    private String assetName;
    @ApiModelProperty("资产绑定数据的类型,即资产子类型")
    private String assetBindingDataType;
    @ApiModelProperty("资产绑定数据的名称,即资产实体名")
    private String assetBindingDataName;
    @ApiModelProperty("模型(MODEL)/库表(TABLE)/文件(FILE)")
    private AssetType assetType;
    @ApiModelProperty("开放申请服务项(申请类型)")
    @TableField(exist = false)
    private List<AssetPermissionType> assetOpenServices;
    @ApiModelProperty("开放申请服务项(申请类型)JSON")
    @JsonIgnore
    private String assetOpenServicesJson;
    @ApiModelProperty("需求性质")
    private Integer requireAttr;
    @ApiModelProperty("需求类型")
    private Integer requireTimeType;
    @ApiModelProperty("申请原因")
    private String applyReason;
    @ApiModelProperty("申请时间")
    private String applyTime;
    @ApiModelProperty("申请人ID")
    private Long applicantId;
    @ApiModelProperty("需求Id")
    private String applicationNo;
    @ApiModelProperty("申请人姓名")
    private String applicantName;
    @ApiModelProperty("流程状态")
    private Integer currentAuditProcessStatus;
    @ApiModelProperty("过期时间")
    private String dataExpir;
    @ApiModelProperty("客户名称")
    private String clientNames;
    @ApiModelProperty("模型名称")
    private String templateName;
    @ApiModelProperty("需求名称")
    private String requireName;
    @ApiModelProperty("项目id")
    private Long projectId;
    @ApiModelProperty("项目名称")
    private String projectName;
    @ApiModelProperty("当前审核人ID")
    private String currentHandlers;
    @ApiModelProperty("当前审核人名")
    private String currentHandlerNames;
    @ApiModelProperty("数据验收状态")
    private String state;
    @ApiModelProperty("布尔类型，是否可修改合同编号")
    private boolean relateAssets;
    @ApiModelProperty("重新申请会有此id,保留新申请的id")
    private Long newApplicationId;
    @ApiModelProperty("服务类型")
    private String serviceType;

    /**
     * 出数状态
     * @see ApplyDataStateEnum
     */
    private String dataState;
}
