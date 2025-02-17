package com.sinohealth.system.biz.table.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author monster
 * @Date 2024-07-18 10:43
 */
@Data
@TableName("tg_table_info_snapshot_compare_plan")
@ApiModel("库表快照比对计划表")
@Accessors(chain = true)
public class TgTableInfoSnapshotComparePlan implements Serializable {
    @TableId
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty(value = "表id")
    private Long tableId;

    @ApiModelProperty(value = "旧版id")
    private Long oldVersionId;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "完成时间")
    private Long creator;

    @ApiModelProperty(value = "创建人")
    private Date updateTime;
}
