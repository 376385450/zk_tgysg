package com.sinohealth.system.biz.application.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Kuangcp
 * 2024-10-17 19:41
 */
@Data
@Accessors(chain = true)
public class ProjectInfoEntity {
    /**
     * 关联客户
     */
    private String tagClient;
    /**
     * 关联应用表名
     */
    private String tagTableName;
    /**
     * 关联标签id
     */
    private String tagIds;

    /**
     * 展示 级联指标开关
     */
    private Boolean cascade;
    private String cascadeField;

}
