package com.sinohealth.system.biz.table.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * 底表比对计划新增或更新参数
 *
 * @author monster
 * @Date 2024-07-18 16:26
 */
@Setter
@Getter
@ToString
public class TableDiffPlanCreateOrUpdateRequest {
    /**
     * 计划编号
     */
    private Long id;

    /**
     * 表编号
     */
    @NotNull(message = "表编号不可为空")
    private Long tableId;

    /**
     * 旧版本编号
     */
    @NotNull(message = "旧版本编号不可为空")
    private Long oldVersionId;
}
