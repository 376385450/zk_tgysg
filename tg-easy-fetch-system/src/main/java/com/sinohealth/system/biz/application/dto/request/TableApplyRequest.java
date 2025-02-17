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
public class TableApplyRequest {

    private Long applicantId;

    private Long processId;

    @ApiModelProperty("申请人所属部门")
    private String applicantDepartment;

    @NotNull(message = "表单ID必填")
    private Long tableId;

    private Date expireDate;

    private String applyReason;

    @NotNull(message = "资产ID必填")
    private Long assetId;

    @NotEmpty(message = "资产权限信息必选")
    private List<AssetPermissionType> permission;
}
