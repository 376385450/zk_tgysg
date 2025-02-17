package com.sinohealth.system.biz.table.dto;

import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import com.sinohealth.system.domain.TableFieldInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 创建与生成sku对应hive外部表参数
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CreateAndWriteCkSkuRequest {
    /**
     * 版本信息
     */
    private TableInfoSnapshot version;

    /**
     * 唯一键
     */
    private String uniqueKey;

    /**
     * 字段信息
     */
    private List<TableFieldInfo> fields;

    /**
     * 新标签表名称
     */
    private String newTagTableName;

    /**
     * 新指标表名称
     */
    private String newIndexTableName;

    /**
     * 对比任务信息编号
     */
    private Long taskId;

    /**
     * ck host节点
     */
    private String hostName;

    /**
     * 限制条件
     */
    private String condition;

    /**
     * 产品编码
     */
    private List<String> prodCodes;
}
