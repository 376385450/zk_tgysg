package com.sinohealth.system.dto.personalservice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author Zhangzifeng
 * @Date 2023/9/11 19:49
 */
@ApiModel("请求服务权限")
@Data
public class QueryPermissionListRequest {

    @NotNull
    @ApiModelProperty("资产ID")
    private Long assetId;
}
