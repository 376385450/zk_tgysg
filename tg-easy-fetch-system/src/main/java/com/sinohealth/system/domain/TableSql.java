package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;
import com.sinohealth.common.annotation.Excel;

/**
 * 【请填写功能名称】对象 table_sql
 * 
 * @author dataplatform
 * @date 2021-05-07
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("table_sql")
public class TableSql implements Serializable {

private static final long serialVersionUID=1L;


    /**  */
    @TableId(value = "table_id")
    private Long tableId;

    /**  */
    private String generateSql;

    /**  */
    private String tableName;

}
