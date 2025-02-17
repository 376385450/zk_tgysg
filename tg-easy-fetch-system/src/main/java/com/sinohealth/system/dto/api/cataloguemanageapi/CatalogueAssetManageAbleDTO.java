package com.sinohealth.system.dto.api.cataloguemanageapi;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/16
 */
@ApiModel("可挂接资产的目录")
@Data
public class CatalogueAssetManageAbleDTO implements Serializable {

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("父id")
    private Integer parentId;

    @ApiModelProperty("是否可选")
    private Boolean chooseAble;

    @ApiModelProperty("全路径")
    private String fullPath;

    @ApiModelProperty("子节点")
    private List<CatalogueAssetManageAbleDTO> children;

}
