package com.sinohealth.system.biz.application.dto.request;

import com.sinohealth.common.enums.AssetPermissionType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-05 09:48
 */
@Data
public class DocApplyRequest {

    @ApiModelProperty("申请id")
    private Integer applicationId;

    @NotNull(message = "文件ID必填")
    private Long docId;

    private Long processId;
    private Long applicantId;

    @NotNull(message = "资产ID必填")
    private Long assetId;

    @ApiModelProperty("申请人所属部门")
    private String applicantDepartment;

    @ApiModelProperty("申请原因")
    private String applyReason;
    @ApiModelProperty("文档权限")
    private List<Integer> docAuthorization;

    @NotEmpty(message = "资产权限信息必选")
    private List<AssetPermissionType> permission;

    private Date expireDate;

}
