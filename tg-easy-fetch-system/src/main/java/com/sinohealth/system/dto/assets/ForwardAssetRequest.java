package com.sinohealth.system.dto.assets;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/25 16:38
 */
@Data
@ApiModel("转发资产的请求参数")
public class ForwardAssetRequest {

    @NotNull(message = "资产ID不能为空")
    @ApiModelProperty("资产ID")
    private Long assetId;
}
