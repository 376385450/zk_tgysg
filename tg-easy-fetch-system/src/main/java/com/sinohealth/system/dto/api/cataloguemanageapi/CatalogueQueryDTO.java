package com.sinohealth.system.dto.api.cataloguemanageapi;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/10
 */
@ApiModel("目录树结构")
@Data
public class CatalogueQueryDTO implements Serializable {

    @ApiModelProperty("目录结构id")
    private Integer id;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("是否可创建同级")
    private boolean buildSameLevel;

    @ApiModelProperty("是否可创建子级")
    private boolean buildNextLevel;

    @ApiModelProperty("是否可删除")
    private boolean deletedAble;

    @ApiModelProperty("排序")
    private Integer sortOrder;

    @ApiModelProperty("子节点")
    private List<CatalogueQueryDTO> children;

}
