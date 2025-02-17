package com.sinohealth.system.biz.table.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.Set;

@Setter
@Getter
@ToString
public class TableInfoCompareTaskVO {
    @TableId
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty(value = "表id")
    private Long tableId;

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

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "结果表名称")
    private Set<String> resultTableNames;

    @ApiModelProperty(value = "完成时间")
    private Long creator;

    @ApiModelProperty(value = "创建人")
    private Date updateTime;

    @ApiModelProperty(value = "是否是最新记录")
    private Boolean latest;
}
