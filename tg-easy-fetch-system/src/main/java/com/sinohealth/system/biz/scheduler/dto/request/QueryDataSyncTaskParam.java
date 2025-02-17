package com.sinohealth.system.biz.scheduler.dto.request;

import lombok.Builder;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-06 15:58
 */
@Data
@Builder
public class QueryDataSyncTaskParam {

    private Integer id;

    private String name;
}
