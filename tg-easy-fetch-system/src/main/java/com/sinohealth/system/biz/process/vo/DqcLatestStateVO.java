package com.sinohealth.system.biz.process.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author Kuangcp
 * 2024-08-14 15:47
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DqcLatestStateVO {

    private String period;

    private String flowProcessType;

    private String nextPeriod;

    /**
     * 允许新建 排期计划
     */
    private Boolean create;
}
