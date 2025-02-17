package com.sinohealth.system.dto.api.cataloguemanageapi;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author shallwetalk
 * @Date 2023/8/18
 */
@Data
@ApiModel("资产目录数据")
public class CatalogueDataInfoDTO implements Serializable {

    @ApiModelProperty("数据映射")
    private Map<Integer, Integer> count;

    @ApiModelProperty("资产目录数")
    private Integer catalogueCount;

    @ApiModelProperty("资产资源数")
    private Integer assetsCount;

}
