package com.sinohealth.system.dto.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author Rudolph
 * @Date 2022-06-08 14:43
 * @Desc
 */
@ApiModel(description = "对外名称信息")
@Data
public class RealName implements Serializable {
    @ApiModelProperty("ID")
    private Long id;
    @ApiModelProperty("字段名称")
    private String fieldName;
    @ApiModelProperty("字段别名")
    private String fieldAlias;
    @ApiModelProperty("数据类型")
    private String dataType;
    @ApiModelProperty("长度")
    private Integer length;
    @ApiModelProperty("主键与否")
    private Boolean primaryKey;
    @ApiModelProperty("空值与否")
    private Boolean empty;
    @ApiModelProperty("注释")
    private String comment;
    @ApiModelProperty("表ID")
    private Long tableId;
    @ApiModelProperty("表名")
    private String tableName;
    @ApiModelProperty("目录ID")
    private Long dirId;
    @ApiModelProperty("创建时间")
    private Date createTime;
    @ApiModelProperty("创建者ID")
    private Long createUserId;
    @ApiModelProperty("更新时间")
    private Date updateTime;
    @ApiModelProperty("更新者ID")
    private Long updateUserId;
    @ApiModelProperty("状态")
    private Boolean status;
    @ApiModelProperty("排序")
    private Integer sort;
    @ApiModelProperty("映射名称")
    private String mappingName;
    @ApiModelProperty("重点字段")
    private Boolean majorField;
    @ApiModelProperty("对外名称")
    private String realName;
    @ApiModelProperty("拷贝字段")
    private String copyFields;
    @ApiModelProperty("字段类型")
    private String fieldType;
    @ApiModelProperty("维度/指标")
    private String dimIndex;

    @ApiModelProperty("关联字段库id")
    private Long relationColId;

    @ApiModelProperty("是否必选")
    private Boolean required;

}
