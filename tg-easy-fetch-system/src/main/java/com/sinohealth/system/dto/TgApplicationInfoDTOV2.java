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
@ApiModel(description = "申请明细")
@Data
@EqualsAndHashCode(callSuper = false)
public class TgApplicationInfoDTOV2 {
    private Long id;

    @Excel(name = "申请通过时间")
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

    @Excel(name = "资产类型")
    @ApiModelProperty("模版(MODEL)/库表(TABLE)/文件(FILE)")
    private String assetType;

    @Excel(name = "服务类型")
    @ApiModelProperty("服务类型")
    private String serviceType;

    private String projectName;

    private String orgUserId;


}


