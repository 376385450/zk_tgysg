package com.sinohealth.system.biz.table.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 创建与复制ck数据表结果信息
 */

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CreateAndCopyResultVO {
    /**
     * 新标签表名称
     */
    private String tagTableName;

    /**
     * 新指标表名称
     */
    private String indexTableName;
}
