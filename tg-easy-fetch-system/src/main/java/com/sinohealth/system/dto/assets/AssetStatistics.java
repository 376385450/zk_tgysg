package com.sinohealth.system.dto.assets;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/8/26
 */
@ApiModel(value = "资产收藏与转发统计")
@Data
public class AssetStatistics implements Serializable {

    private Long assetId;

    @ApiModelProperty
    private boolean currentUserIsCollection;

    @ApiModelProperty("收藏量")
    private Integer collectionCount;

    @ApiModelProperty("转发量")
    private Integer shareCount;

    @ApiModelProperty("浏览数")
    private Integer viewNum;

    @ApiModelProperty("服务申请次数")
    private Integer serviceCount;

    @ApiModelProperty("服务申请成功次数")
    private Integer serviceSuccessCount;

}
