package com.sinohealth.system.biz.project.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Kuangcp
 * 2024-12-11 13:53
 */
@Data
public class DataPlanDetailUpdateRequest {
    @NotNull(message = "id缺失")
    private Long id;
//    private String period;
    private Boolean holiday;
//    private String flowProcessType;
}
