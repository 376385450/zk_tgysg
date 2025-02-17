package com.sinohealth.system.biz.project.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Kuangcp
 * 2024-12-11 13:43
 */
@Data
public class DataPlanDetailPageDTO {
    private Long id;
    private LocalDate day;
    private String bizType;
    private Integer duration;
    private String period;
    private Boolean holiday;
    private String flowProcessType;
    private String updater;
    private LocalDateTime updateTime;
    private Boolean editable;

}
