package com.sinohealth.system.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.utils.dto.Node;
import com.sinohealth.system.biz.dir.dto.DirItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-06-16 11:11
 * @Desc
 */
@ApiModel(description = "地图目录-表单DTO")
@Data
public class TableDataDirItemDto implements Node<TableDataDirItemDto>, DirItem {

    @ApiModelProperty("自增ID")
    private Long id;

    @ApiModelProperty("表单ID")
    private Long tableId;

    @ApiModelProperty("备注")
    private String comment;

    @ApiModelProperty("目录ID")
    private Long dirId;

    @ApiModelProperty("表所属人")
    private String leaderName;

    @ApiModelProperty("表所属人组织")
    private String leaderNameOri;

    @ApiModelProperty("表别名")
    private String tableAlias;

    @ApiModelProperty("表名")
    private String tableName;

    @ApiModelProperty("模板信息")
    private String templateAuditInfo;

    @ApiModelProperty("可见人")
    private String viewUser;

    @ApiModelProperty("资产类型")
    private String assetType = "表";

    @ApiModelProperty("目录id")
    private Long parentId;

    @ApiModelProperty("序号")
    private Integer sort;

    @ApiModelProperty("子节点")
    @JsonIgnore
    private List<TableDataDirItemDto> children = new ArrayList<>();

    @ApiModelProperty("icon")
    private String icon = "table";

    @ApiModelProperty("状态")
    private Integer status;

    @NotEmpty(message = InfoConstants.DIRNAME_REQUIREMENT)
    private String dirName;

    private Integer target;

    @TableField(exist = false)
    private String nodeViewName;

    @TableField(exist = false)
    private Date lastUpdate;

    @TableField(exist = false)
    private String clientNames;

    @TableField(exist = false)
    private Integer requireTimeType;

    @TableField(exist = false)
    private Integer requireAttr;

    private Integer moved;
}
