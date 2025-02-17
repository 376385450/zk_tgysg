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
 * 统计数据库中间对象 sys_statistical_table
 *
 * @author dataplatform
 * @date 2021-08-02
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("sys_statistical_table")
public class SysStatisticalTable implements Serializable {

private static final long serialVersionUID=1L;

    /**
     *
     */
    @TableId(value = "id")
    private Long id;


    /** 统计任务id */
    private Long statisticalId;

    /** 表id */
    private Long tableId;

}
