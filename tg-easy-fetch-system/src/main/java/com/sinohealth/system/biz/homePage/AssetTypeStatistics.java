package com.sinohealth.system.biz.homePage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/9/18
 */
@Data
@ApiModel("资产类型")
public class AssetTypeStatistics implements Serializable {

    @ApiModelProperty("资产数量")
    private Integer assetCount;

    @ApiModelProperty("标签")
    private List<String> labels;

    @ApiModelProperty("数据")
    private List<AssetTypeData> typeData;

}
