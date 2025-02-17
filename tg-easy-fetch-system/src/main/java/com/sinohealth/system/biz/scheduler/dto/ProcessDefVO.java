package com.sinohealth.system.biz.scheduler.dto;

import io.swagger.models.auth.In;
import lombok.Builder;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-05 18:20
 */
@Data
@Builder
public class ProcessDefVO {
    private String id;
    private String taskId;
    private String name;
    private String processDefinitionJson;
    private String locations;
    private String description;
    private Integer releaseState;
}
