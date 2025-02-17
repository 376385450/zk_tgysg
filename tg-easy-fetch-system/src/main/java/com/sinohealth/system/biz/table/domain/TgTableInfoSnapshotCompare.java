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
 * @Date 2024-07-11 10:43
 */
@Data
@TableName("tg_table_info_snapshot_compare")
@ApiModel("库表快照比对表")
@Accessors(chain = true)
public class TgTableInfoSnapshotCompare implements Serializable {
    @TableId
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty(value = "表id")
    private Long tableId;

    @ApiModelProperty(value = "业务编号")
    private Long bizId;

    @ApiModelProperty(value = "计划编号")
    private Long planId;

    @ApiModelProperty(value = "新版id")
    private Long newVersionId;

    @ApiModelProperty(value = "新版本号")
    private Integer newVersion;

    @ApiModelProperty(value = "新周期")
    private String newPeriod;

    @ApiModelProperty(value = "新版期数名称")
    private String newVersionPeriod;

    @ApiModelProperty(value = "旧版id")
    private Long oldVersionId;

    @ApiModelProperty(value = "旧版本号")
    private Integer oldVersion;

    @ApiModelProperty(value = "旧周期")
    private String oldPeriod;

    @ApiModelProperty(value = "旧版期数名称")
    private String oldVersionPeriod;

    @ApiModelProperty(value = "状态")
    private String state;

    @ApiModelProperty(value = "结果表状态")
    private String resultState;

    @ApiModelProperty(value = "失败原因")
    private String failReason;

    @ApiModelProperty(value = "回调地址")
    private String callbackUrl;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "预计执行时间")
    private Date planExecuteTime;

    @ApiModelProperty(value = "完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "完成时间")
    private Long creator;

    @ApiModelProperty(value = "创建人")
    private Date updateTime;
}
