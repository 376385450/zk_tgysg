package com.sinohealth.system.biz.homePage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/24
 */
@Data
@ApiModel("资产分布")
public class AssetDistribution implements Serializable {

    @ApiModelProperty("资产提供方总数")
    private Integer providerCount;

    @ApiModelProperty("资产总数")
    private Integer assetCount;

    @ApiModelProperty("资产分布饼图")
    private List<ProviderProportion> proportions;

}
