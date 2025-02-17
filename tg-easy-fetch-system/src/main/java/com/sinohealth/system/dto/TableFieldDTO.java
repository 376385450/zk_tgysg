package com.sinohealth.system.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 表关联的表字段信息
 *
 * @author linkaiwei
 * @date 2021/7/26 16:53
 * @since 1.1
 */
@Data
public class TableFieldDTO implements Serializable {

    /**
     * 表ID
     */
    private String id;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 状态，0 删除 1 可用 2 停用
     */
    private Integer status;

    /**
     * 数据源名称
     */
    private String sourceName;

    /**
     * 字段ID
     */
    private String fieldId;

    /**
     * 字段名称
     */
    private String fieldName;

}
