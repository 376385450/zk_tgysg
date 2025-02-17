package com.sinohealth.system.dto.assets;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/28 16:09
 */
@Data
@ApiModel("判断是否能查看")
public class JudgeViewableRequest {

    @NotNull(message = "资产ID不能为空")
    @ApiModelProperty("资产ID")
    private Long assetId;
}
