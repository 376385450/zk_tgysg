package com.sinohealth.system.dto.api.cataloguemanageapi;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/14
 */
@Data
@ApiModel("资产目录树")
public class CatalogueReadAbleDTO implements Serializable {

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("资产数量")
    private Integer assetCount;

    @ApiModelProperty("目录说明")
    private String description;

    @ApiModelProperty("父id")
    private Integer parentId;

    @ApiModelProperty("子级目录")
    private List<CatalogueReadAbleDTO> children;

}
