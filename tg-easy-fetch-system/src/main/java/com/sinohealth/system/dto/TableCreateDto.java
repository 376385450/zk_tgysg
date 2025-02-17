package com.sinohealth.system.dto;

import com.sinohealth.bi.enums.DatabaseEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Jingjun
 * @since 2021/4/29
 */
@Data
@ApiModel("TableCreateDto")
public class TableCreateDto {

    @ApiModelProperty("表Id")
    private Long tableId;

    private Long dirId;

    private String schema;

    /**
     * 数据库类型（默认mysql）
     * 详情见 {@link com.sinohealth.bi.enums.DatabaseEnum}
     */
    @ApiModelProperty("数据库类型（默认mysql），mysql；hive2；")
    private String databaseType = DatabaseEnum.MYSQL.getFeature();

    @ApiModelProperty("表中文名")
    private String tableAlias;

    @ApiModelProperty("表名")
    @NotNull
    private String tableName;

    @NotNull
    @ApiModelProperty("分布式表名")
    private  String tableNameDistributed;

    @ApiModelProperty("安全等级")
    private Integer safeLevel;

    @ApiModelProperty("备注")
    private String comment;

    @ApiModelProperty("字段配置")
    private List<TableFieldInfoDto> fields;

    @ApiModelProperty("是否DDL")
    @NotNull
    private Boolean isDdl;

    @ApiModelProperty("DDL")
    private String ddl;

}
