package com.sinohealth.system.biz.scheduler.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-05 18:05
 */
@Data
@Builder
public class UpsertTaskVO {

    private Integer taskId;

    private Integer flowId;
}
