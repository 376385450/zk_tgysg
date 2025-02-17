package com.sinohealth.system.biz.project.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * @author Kuangcp
 * 2024-12-11 11:31
 */
@Data
public class DataPlanBizDTO {

    private String period;
    private LocalDate qcDate;
    private LocalDate sopDate;
    private LocalDate deliverDate;
}
