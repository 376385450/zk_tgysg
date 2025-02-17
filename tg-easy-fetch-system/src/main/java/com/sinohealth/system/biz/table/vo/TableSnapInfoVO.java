package com.sinohealth.system.biz.table.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-23 10:17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableSnapInfoVO {

    private Boolean create;
    private Boolean manager;

    private String version;
    private LocalDateTime syncTime;

    private String versionPeriod;
    private String flowProcessType;

    private TablePushAssetsPlanVO plan;
}
