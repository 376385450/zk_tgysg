package com.sinohealth.system.dto.personalservice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @Author Zhangzifeng
 * @Date 2023/9/12 15:08
 */
@ApiModel("判断是否有操作权限")
@Data
public class JudgePermissionRequest {


    @ApiModelProperty("资产ID")
    @NotNull
    private Long assetId;

    @ApiModelProperty("权限")
    @NotBlank
    private String permission;
}
