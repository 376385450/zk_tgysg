package com.sinohealth.system.dto;

import com.sinohealth.common.config.DataSourceFactory;
import com.sinohealth.common.utils.DirCache;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 字段映射信息
 *
 * @author linkaiwei
 * @date 2021/11/4 10:21
 * @since 1.6.1.0
 */
@Data
@ApiModel("字段映射信息")
@Accessors(chain = true)
public class TableMappingDTO implements Serializable {

    @ApiModelProperty(value = "字段映射信息ID", position = 1)
    private Long id;

    @ApiModelProperty(value = "源表ID", position = 2)
    private Long tableId;

    @NotNull(message = "源字段ID不能为空")
    @ApiModelProperty(value = "源字段ID（新增必传）", position = 3)
    private Long fieldId;

    @ApiModelProperty(value = "源表字段", position = 4)
    private String fieldName;

    @ApiModelProperty(value = "源表字段名", position = 5)
    private String fieldAlias;

    @NotNull(message = "关联表数据目录ID不能为空")
    @ApiModelProperty(value = "关联表数据目录ID（新增必传）", position = 6)
    private Long relationDirId;

    @NotNull(message = "关联表ID不能为空")
    @ApiModelProperty(value = "关联表ID（新增必传）", position = 7)
    private Long relationTableId;

    @ApiModelProperty(value = "关联表名称", position = 8)
    private String relationTableName;

    @ApiModelProperty(value = "关联表中文名称", position = 9)
    private String relationTableAlias;

    @NotNull(message = "关联表字段ID不能为空")
    @ApiModelProperty(value = "关联表字段ID（新增必传）", position = 10)
    private Long relationFieldId;

    @ApiModelProperty(value = "关联表字段名称", position = 11)
    private String relationFieldName;

    @ApiModelProperty(value = "关联表字段中文名称", position = 12)
    private String relationFieldAlias;

    @NotNull(message = "映射字段ID不能为空")
    @ApiModelProperty(value = "映射字段ID（新增必传）", position = 13)
    private Long mappingFieldId;

    @ApiModelProperty(value = "映射字段名称", position = 14)
    private String mappingFieldName;

    @ApiModelProperty(value = "映射字段中文名称", position = 15)
    private String mappingFieldAlias;


    @ApiModelProperty(value = "数据库名称", position = 16)
    public String getRelationSourceName() {
        return  "";
    }

    @ApiModelProperty(value = "映射字段类型", position = 99)
    private String dataType;

    @ApiModelProperty(value = "映射字段是否是主键", position = 99)
    private boolean primaryKey;

    @ApiModelProperty(value = "目录名称")
    private String dirName;

}
