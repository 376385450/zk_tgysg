package com.sinohealth.system.biz.process.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * @author Kuangcp
 * 2024-09-05 14:13
 */
@Data
public class TgFlowProcessPlanUpsertRequest {

    private Long id;
    @NotNull(message = "QC时间不能为空")
    private LocalDate qcDate;
    @NotNull(message = "SOP时间不能为空")
    private LocalDate sopDate;
    @NotNull(message = "交付时间不能为空")
    private LocalDate deliverDate;
}
