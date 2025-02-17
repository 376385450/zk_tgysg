package com.sinohealth.system.biz.application.bo;

import lombok.Builder;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-03-01 16:39
 */
@Data
@Builder
public class CustomFieldBuilderVO {
    private Long tableId;
    private Long fieldId;
    private Integer type;
    private String expression;
    private String aliasName;
    private String fullNameUnderScore;
    private String func;
    private Boolean createTemplate;
}
