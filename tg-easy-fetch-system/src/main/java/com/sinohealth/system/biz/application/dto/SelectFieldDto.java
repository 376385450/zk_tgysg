package com.sinohealth.system.biz.application.dto;

import com.sinohealth.system.biz.dict.domain.FieldDict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-22 15:06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectFieldDto {
    /**
     * 字段库id
     *
     * @see FieldDict#id
     */
    private Long fieldId;

    private String alias;
}
