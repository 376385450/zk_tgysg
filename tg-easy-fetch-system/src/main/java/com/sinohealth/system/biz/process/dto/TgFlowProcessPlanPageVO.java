package com.sinohealth.system.biz.process.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Kuangcp
 * 2024-09-05 14:08
 */
@Data
public class TgFlowProcessPlanPageVO {

    private Long id;

    private String period;
    private LocalDate qcDate;
    private LocalDate sopDate;
    private LocalDate deliverDate;

    /**
     * 是否可编辑
     */
    private Boolean edit;

    @ApiModelProperty("更新人")
    private String updater;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
