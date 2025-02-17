package com.sinohealth.system.dto.assets;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/25 16:13
 */
@Data
@ApiModel("收藏资产请求参数")
public class CollectAssetRequest {

    @NotNull(message = "资产ID不能为空")
    @ApiModelProperty("资产ID")
    private Long assetId;

    @NotNull(message = "是否收藏参数不能为空")
    @ApiModelProperty("是否收藏，0：取消收藏，1：收藏")
    private Integer isCollect;
}
