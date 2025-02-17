package com.sinohealth.system.dto;

import com.sinohealth.common.utils.DirCache;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Jingjun
 * @since 2021/4/24
 */

@Data
@ApiModel("TableRelationDto")
public class TableRelationDto {

    private Long id;

    private Long tableId;
    @ApiModelProperty("字段ID")
    private Long fieldId;
    @ApiModelProperty("目录id")
    private Long dirId;
    @ApiModelProperty("目录名称")
    private String dirName;
    @ApiModelProperty("关联中间表ID")
    private Long relationId;
    @ApiModelProperty("主表字段")
    private String fieldName;
    @ApiModelProperty("主表字段名")
    private String fieldAlias;
    @ApiModelProperty("关联表ID")
    private Long refTableId;

    private String refTableName;

    private String refTableAlias;
    @ApiModelProperty("关联字段ID")
    private Long refFieldId;

    @ApiModelProperty("关联字段名")
    private String refFieldAlias;

    private String refFieldName;
    @ApiModelProperty("关联DIRID")
    private Long refDirId;

    /*@ApiModelProperty("数据地图中文路径")
    public String getRefDirPath() {
        return refDirId == null ? "" : DirCache.getDir(refDirId).getDirPath();
    }

    @ApiModelProperty("数据地图ID路径 用,隔开")
    public String getRefIdPath() {
        return refDirId == null ? "" : DirCache.getDir(refDirId).getIdPath();
    }*/

}
