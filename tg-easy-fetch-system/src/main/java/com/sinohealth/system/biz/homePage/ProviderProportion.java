package com.sinohealth.system.biz.homePage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/8/24
 */
@ApiModel("资产占比")
@Data
public class ProviderProportion implements Serializable {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("提供方id")
    private String providerId;

    @ApiModelProperty("文件id")
    private Integer catalogueId;

    @ApiModelProperty("资产数量")
    private Integer assetCount;

    @ApiModelProperty("资产占比")
    private double assetProportion;

    @ApiModelProperty("资产应用占比")
    private double assetApplyProportion;

}
