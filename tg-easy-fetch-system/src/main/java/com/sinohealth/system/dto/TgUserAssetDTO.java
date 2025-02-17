package com.sinohealth.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sinohealth.common.annotation.Excel;
import com.sinohealth.common.enums.AssetType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author zhangyanping
 * @date 2023/11/23 14:17
 */
@ApiModel(description = "出数趋势")
@Data
@EqualsAndHashCode(callSuper = false)
public class TgUserAssetDTO {

    @Excel(name = "出数时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private String createTime;

    @Excel(name = "申请用户")
    @ApiModelProperty("申请人姓名")
    private String applicantName;

    @Excel(name = "所属部门")
    @ApiModelProperty("组织名称")
    private String orgName;

    @Excel(name = "三级部门")
    @ApiModelProperty("申请人所属部门")
    private String applicantDepartment;

    @Excel(name = "资产目录")
    @ApiModelProperty("一二级类目名称")
    private String menuName;

    @Excel(name = "申请资产名称")
    @ApiModelProperty("资产名称")
    private String assetName;

    @Excel(name = "申请需求名称")
    @ApiModelProperty("资产名称")
    private String projectName;

    @Excel(name = "需求类型")
    @ApiModelProperty("1：一次性需求、2：持续性需求")
    private String requireTimeType;

    @Excel(name = "出数类型")
    @ApiModelProperty("出数类型")
    private String serviceType;

    private String orgUserId;

}


