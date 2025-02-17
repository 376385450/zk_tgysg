package com.sinohealth.system.biz.application.dto;

import com.sinohealth.system.biz.scheduler.dto.request.DataSyncTaskFieldConfig;
import com.sinohealth.system.domain.TgDataSyncApplication;
import com.sinohealth.system.dto.auditprocess.ProcessNodeEasyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-06 17:15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncApplyDetailVO extends TgDataSyncApplication {

    // TODO 补充审批所需字段
    private String x;

    private String syncTaskReason;

    private Date expiredDate;

    private String targetDataSourceName;

    private Long applicantId;

    private String applicantName;

    private String createTime;

    private List<ProcessNodeEasyDto> handleNode;

    private String currentHandlers;

    private Integer currentIndex;

    private Integer currentAuditProcessStatus;

    private Integer currentAuditNodeStatus;

    private List<DataSyncTaskFieldConfig> fieldsConfigs;

}
