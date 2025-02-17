package com.sinohealth.system.biz.application.dto.request;

import com.sinohealth.system.biz.scheduler.dto.request.DataSyncTaskFieldConfig;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-02 15:29
 */
@Data
public class SyncApplicationSaveRequest {

    @ApiModelProperty("id")
    private Integer id;

    @ApiModelProperty("资产id")
    @NotNull(message = "资产id不能为空")
    private Long assetId;

    @ApiModelProperty("任务有效期")
    private Date expiredDate;

    @ApiModelProperty("任务名称")
    @NotBlank(message = "name不能为空")
    private String syncTaskName;

    @ApiModelProperty("申请原因")
    @Length(max = 200, message = "申请原因超过{max}")
    private String syncTaskReason;

    @ApiModelProperty("租户")
    private String tenantId;

    @ApiModelProperty("流程id")
    private Long processId;

    @ApiModelProperty("申请人部门")
    private String applicantDepartment;

    @ApiModelProperty("cron表达式")
    private String syncTaskCron;

    @ApiModelProperty("CK集群")
    private String ckCluster;

    @ApiModelProperty("CK引擎")
    private String ckEngine;

    @ApiModelProperty("CK排序键")
    private String ckSortKey;

    @ApiModelProperty("目标类型")
    @NotBlank(message = "targetDbType不能为空")
    private String targetDbType;

    @ApiModelProperty("目标ID")
    @NotNull(message = "targetDataSourceId不能为空")
    private Integer targetDataSourceId;

    @ApiModelProperty("目标表名")
    @NotBlank(message = "targetTableName不能为空")
    private String targetTableName;

    @ApiModelProperty("目标表数据库")
    private String targetDataSourceDatabase;

    @ApiModelProperty("目标表schema")
    private String targetDataSourceSchema;

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

    // 中间值 字段
    /**
     * 同步配置id
     */
    private Integer syncTaskId;
}
