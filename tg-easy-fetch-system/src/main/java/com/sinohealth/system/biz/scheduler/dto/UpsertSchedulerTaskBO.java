package com.sinohealth.system.biz.scheduler.dto;

import com.sinohealth.system.biz.application.dto.request.SyncApplicationSaveRequest;
import com.sinohealth.system.biz.scheduler.dto.request.DataSyncTaskFieldConfig;
import com.sinohealth.system.domain.TgDataSyncApplication;
import com.sinohealth.system.dto.BaseDataSourceParamDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-08 15:59
 */
@Data
@Builder
public class UpsertSchedulerTaskBO {

    private SyncApplicationSaveRequest param;

    private TgDataSyncApplication syncApplication;

    private List<DataSyncTaskFieldConfig> fieldsConfigs;

    private String applyReason;

    private Integer flowId;
    private Integer syncTaskId;

    private String sourceTableName;
    private BaseDataSourceParamDto source;
    private BaseDataSourceParamDto target;
    private DataSyncTaskClickhouseExt ckExt;
}
