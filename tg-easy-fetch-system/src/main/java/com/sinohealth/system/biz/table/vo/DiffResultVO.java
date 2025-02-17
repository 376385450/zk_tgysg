package com.sinohealth.system.biz.table.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DiffResultVO {
    /**
     * 类型
     */
    private String dimIndex;

    /**
     * 详细表名
     */
    private String detailTableName;

    /**
     * 额外信息表名
     */
    private String extraTableName;
}
