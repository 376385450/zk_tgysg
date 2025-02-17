package com.sinohealth.system.dto.api.cataloguemanageapi;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/4
 */
@ApiModel("新增编辑目录入参")
@Data
public class CatalogueDTO {

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("目录名称")
    private String name;

    @ApiModelProperty("上级目录id")
    private Integer parentId;

    @ApiModelProperty("目录编码")
    private String code;

    @ApiModelProperty("排序")
    private Integer sortOrder;

    @ApiModelProperty("目录描述")
    private String description;

    @ApiModelProperty("目录图标")
    private String icon;

    @ApiModelProperty("资产编目流程")
    private Long catalogueFlowId;

    @ApiModelProperty("申请服务流程")
    private Long serviceFlowId;

    //用户权限
    @ApiModelProperty("用户权限")
    private List<UserRightsDTO> userRightsDTOS;

}
