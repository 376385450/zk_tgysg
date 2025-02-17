package com.sinohealth.system.biz.scheduler.dto.request;

import com.sinohealth.system.biz.scheduler.dto.DataSyncTaskClickhouseExt;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2023-07-18 10:50
 */
@Data
public class UpsertDataSyncTaskParam implements Serializable {

    private Integer id;

    @ApiModelProperty("任务名称")
    @NotBlank(message = "name不能为空")
    private String name;

    @ApiModelProperty("源类型")
    @NotBlank(message = "sourceDbtype不能为空")
    private String sourceDbtype;

    @ApiModelProperty("源ID")
    @NotNull(message = "sourceDataSourceId不能为空")
    private Integer sourceDataSourceId;

    @ApiModelProperty("源别名")
    @NotBlank(message = "sourceDataSourceName不能为空")
    private String sourceDataSourceName;

    @ApiModelProperty("源表")
    @NotBlank(message = "sourceTableName不能为空")
    private String sourceTableName;

    @ApiModelProperty("目标类型")
    @NotBlank(message = "targetDbType不能为空")
    private String targetDbType;

    @ApiModelProperty("目标ID")
    @NotNull(message = "targetDataSourceId不能为空")
    private Integer targetDataSourceId;

    @ApiModelProperty("目标别名")
    @NotBlank(message = "targetDataSourceName不能为空")
    private String targetDataSourceName;

    @ApiModelProperty("目标表名")
    @NotBlank(message = "targetTableName不能为空")
    private String targetTableName;

    @ApiModelProperty("创建表")
    @NotNull(message = "createTarget不能为空")
    private Boolean createTarget;

    @NotEmpty(message = "fieldsConfigs不能为空")
    private List<DataSyncTaskFieldConfig> fieldsConfigs;

    @ApiModelProperty("同步方式: 1全量,2增量")
    @NotNull(message = "syncType不能为空")
    private Integer syncType;

    @ApiModelProperty("过滤条件")
    private String filterSql;

    @ApiModelProperty("clickhouse额外参数")
    private DataSyncTaskClickhouseExt clickhouseExt;
}
