package com.sinohealth.system.dto.api.cataloguemanageapi;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/11
 */
@Data
@ApiModel("类目详细信息")
public class CatalogueDetailDTO implements Serializable {

    @ApiModelProperty("类目id")
    private Integer id;

    @ApiModelProperty("目录名称")
    private String name;

    @ApiModelProperty("上级目录id")
    private Integer parentId;

    @ApiModelProperty("上级目录名称")
    private String parentName;

    @ApiModelProperty("目录编码")
    private String code;

    @ApiModelProperty("排序")
    private Integer sortOrder;

    @ApiModelProperty("目录描述")
    private String description;

    @ApiModelProperty("地址")
    private String path;

    @ApiModelProperty("目录图标")
    private String icon;

    @ApiModelProperty("资产编目流程")
    private Long catalogueFlowId;

    @ApiModelProperty("申请服务流程")
    private Long serviceFlowId;

    @ApiModelProperty("创建人")
    private String createdBy;

    @ApiModelProperty("创建时间")
    private Date createdAt;

    @ApiModelProperty("更新人")
    private String updatedBy;

    @ApiModelProperty("更新时间")
    private Date updatedAt;

    //用户权限
    @ApiModelProperty("用户权限")
    private List<UserRightsDTO> userRightsDTOS;


}

