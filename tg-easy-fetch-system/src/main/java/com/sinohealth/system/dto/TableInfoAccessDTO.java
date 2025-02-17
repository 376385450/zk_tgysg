package com.sinohealth.system.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户关联的表权限
 *
 * @author linkaiwei
 * @date 2021/7/26 16:53
 * @since 1.1
 */
@Data
public class TableInfoAccessDTO implements Serializable {

    /**
     * 表ID
     */
    private String id;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表中文名
     */
    private String tableAlias;

    /**
     * 数据目录ID
     */
    private Long dirId;

    /**
     * 状态，0 删除 1 可用 2 停用
     */
    private Integer status;

    /**
     * 数据源ID
     */
    private Long datasourceId;

    /**
     * 数据源名称
     */
    private String sourceName;

    /**
     * 权限，1只读;2可读可导出;3内容编辑;4元数据编辑;5是否管理
     */
    private Integer accessType;

}
