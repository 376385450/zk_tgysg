package com.sinohealth.system.biz.process.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Kuangcp
 * 2024-08-08 16:36
 */
@Data
@Builder
public class DqcQcLogVO {

    private String period;
    private String prodcode;
    private String remark;
    private String qcState;
    private LocalDate qcTime;
    private String sopState;
    private String sopTime;
    private LocalDateTime updateTime;
}
