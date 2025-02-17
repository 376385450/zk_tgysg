package com.sinohealth.system.dto.api.cataloguemanageapi;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/21
 */
@Data
@ApiModel("资产门户首页")
@AllArgsConstructor
@NoArgsConstructor
public class CatalogueDataReadTree implements Serializable {

    @ApiModelProperty("资产总数")
    private Integer assetsCount;

    @ApiModelProperty("目录数")
    private Integer catalogueCount;


    @ApiModelProperty("目录树")
    private List<CatalogueReadAbleDTO> tree;

}
