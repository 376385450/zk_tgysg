package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
@ApiModel("数据源实体")
public class BaseDataSourceParamDto {

    private Integer id;
    @ApiModelProperty("数据源名称")
    private String name;
    @ApiModelProperty("数据源描述")
    private String note;
    @ApiModelProperty("IP主机名")
    private String host;
    @ApiModelProperty("数据源端口")
    private Integer port;
    @ApiModelProperty("数据库名")
    private String database;
    @ApiModelProperty("用户名")
    private String userName;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("jdbc连接参数")
    private Map<String, String> other;
    /**
     * @see com.sinohealth.common.enums.DbType
     */
    @ApiModelProperty("数据源类型,可用值:MYSQL,POSTGRESQL,HIVE,SPARK,CLICKHOUSE,ORACLE,SQLSERVER,DB2")
    private String type;
}
