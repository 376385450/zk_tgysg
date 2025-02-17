package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Author shallwetalk
 * @Date 2023/11/2
 */
@Data
@TableName(value = "tg_data_sync_application", autoResultMap = true)
public class TgDataSyncApplication {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Long applicationId;

    /**
     * 工作流id
     */
    private Integer flowId;

    /**
     * 同步配置id
     */
    private Integer syncTaskId;

    private String syncTaskName;

    private String tenantId;

    private String syncTaskCron;

    private String ckCluster;

    private String ckEngine;

    private String ckSortKey;

    private String targetDbType;

    private Integer targetDataSourceId;

    private String targetDataSourceDatabase;

    private String targetDataSourceSchema;

    private String targetTableName;

    private Boolean createTarget;

    private Integer syncType;

    private String filterSql;


}
