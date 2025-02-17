package com.sinohealth.system.dto.table_manage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Rudolph
 * @Date 2022-05-20 11:29
 * @Desc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaDataFieldInfo {

    private String tableName;
    private Long colId;
    private String colName;
}
