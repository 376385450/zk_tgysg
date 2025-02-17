package com.sinohealth.system.domain;

import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author Rudolph
 * @Date 2023-10-30 18:34
 * @Desc
 */
@Data
@ApiModel(description = "最近资产申请结果类")
public class TgLastApplicationInfo {
    @ApiModelProperty("申请人名")
    private String applicant = "****";
    @ApiModelProperty("上级部门/当前部门")
    private String department;
    @ApiModelProperty("部门全路径")
    private String fullDepartment;
    @ApiModelProperty("申请时间")
    private String applyTime;
    @ApiModelProperty("资产类型")
    private AssetType type;
    @ApiModelProperty("申请类型")
    private String applyType;
    @ApiModelProperty("申请权限")
    private List<AssetPermissionType> permission;
    @ApiModelProperty("申请说明")
    private String applyComment;
    @ApiModelProperty("需求类型,模板时展示")
    private String requireAttr;
    @ApiModelProperty("需求名称,模板时展示")
    private String projectName;
}
