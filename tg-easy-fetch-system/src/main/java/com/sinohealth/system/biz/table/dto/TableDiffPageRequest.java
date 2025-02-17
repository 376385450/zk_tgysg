package com.sinohealth.system.biz.table.dto;

import com.sinohealth.system.dto.common.PageRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Setter
@Getter
@ToString
public class TableDiffPageRequest extends PageRequest {
    /**
     * 表编号
     */
    @NotNull(message = "表编号不可为空")
    private Long tableId;
}
