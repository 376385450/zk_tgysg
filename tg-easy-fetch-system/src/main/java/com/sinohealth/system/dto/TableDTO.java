package com.sinohealth.system.dto;

import com.sinohealth.data.intelligence.enums.DataSourceType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/8/21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("表")
public class TableDTO implements Serializable {

    @ApiModelProperty("表id")
    private Integer tableId;

    @ApiModelProperty("表名")
    private String tableName;

    @ApiModelProperty("表中文名")
    private String cnName;

    @ApiModelProperty("数据源类型")
    private String dataSourceType;

    @ApiModelProperty("数据库")
    private String database;

    @ApiModelProperty("schema")
    private String dbSchema;

    @ApiModelProperty("表所有者")
    private String ownerName;

    @ApiModelProperty("ip")
    private String host;

    @ApiModelProperty("端口")
    private Integer port;

    @ApiModelProperty("是否可选")
    private boolean selectable;

}

