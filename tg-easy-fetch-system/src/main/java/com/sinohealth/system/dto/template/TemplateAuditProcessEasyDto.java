package com.sinohealth.system.dto.template;

import lombok.Data;

/**
 * @Author Rudolph
 * @Date 2022-06-15 15:34
 * @Desc
 */
@Data
public class TemplateAuditProcessEasyDto {
    private Long templateId;
    private Long processId;
    private Long baseTableId;
    private String templateName;
    private String processName;

    /**
     * 排序
     */
    private Integer sortIndex;
}
