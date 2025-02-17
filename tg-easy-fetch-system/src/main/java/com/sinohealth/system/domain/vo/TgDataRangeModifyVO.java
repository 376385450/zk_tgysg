package com.sinohealth.system.domain.vo;

import lombok.Data;

@Data
public class TgDataRangeModifyVO {
    /**
     * 保存记录id
     */
    private Long rangeTemplateId;

    /**
     * 异常信息
     */
    private String message;
}
