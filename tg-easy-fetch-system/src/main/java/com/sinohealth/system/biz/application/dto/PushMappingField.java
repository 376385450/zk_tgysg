package com.sinohealth.system.biz.application.dto;

import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-20 15:11
 */
@Data
public class PushMappingField {

    /**
     * 字段库id
     */
    private Long fieldId;

    /**
     * 原始字段名 不在字段库里定义的情况
     */
    private String srcName;

    /**
     * 目标字段名
     */
    private String aliasName;

    /**
     * 字段类型
     *
     * @see com.sinohealth.system.biz.application.constants.FieldType#METRIC_STR
     */
    private String fieldType;
}
