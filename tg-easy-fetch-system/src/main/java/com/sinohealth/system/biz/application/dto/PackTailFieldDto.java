package com.sinohealth.system.biz.application.dto;

import com.sinohealth.system.biz.application.constants.FieldType;
import lombok.Data;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-02-26 15:39
 */
@Data
public class PackTailFieldDto {
    /**
     * @see FieldType
     */
    private Integer fieldType;
    private Long fieldId;
    private Boolean markNull;
    private String val;
}
