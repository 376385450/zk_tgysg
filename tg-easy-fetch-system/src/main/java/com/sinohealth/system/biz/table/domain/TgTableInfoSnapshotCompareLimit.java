package com.sinohealth.system.biz.table.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author monster
 * @Date 2024-07-11 10:43
 */
@Data
@TableName("tg_table_info_snapshot_compare_limit")
@ApiModel("库表快照比对条件表")
@Accessors(chain = true)
public class TgTableInfoSnapshotCompareLimit implements Serializable {
    @TableId
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty(value = "表id")
    private Long tableId;

    @ApiModelProperty(value = "条件语句")
    private String conditionSql;
}
