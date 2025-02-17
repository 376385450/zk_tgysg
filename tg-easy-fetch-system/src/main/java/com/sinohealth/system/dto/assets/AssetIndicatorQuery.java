package com.sinohealth.system.dto.assets;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author Rudolph
 * @Date 2023-10-30 15:04
 * @Desc
 */
@Data
public class AssetIndicatorQuery {
    @ApiModelProperty("资产ID, 非空")
    @NotNull(message = "资产ID不能为NULL")
    private Long assetId;

    @ApiModelProperty("根据传入时间决定周期长短, 默认七天")
    private Integer lastDuration = 7;
}

