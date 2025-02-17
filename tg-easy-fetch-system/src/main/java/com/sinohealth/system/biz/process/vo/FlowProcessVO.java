package com.sinohealth.system.biz.process.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@ToString
public class FlowProcessVO implements Serializable {
    private Long id;

    @ApiModelProperty("设置id")
    private Long settingId;

    @ApiModelProperty("流程名称")
    private String name;

    @ApiModelProperty("期数")
    private String period;

    @ApiModelProperty("版本类型【QC/SOP/交付版本/临时改数版本")
    private String versionCategory;

    @ApiModelProperty("底表资产编号")
    private Long tableAssetId;

    @ApiModelProperty("底表资产表名")
    private String tableAssetName;

    @ApiModelProperty("模板资产ids")
    private String templateIds;

    @ApiModelProperty("模板资产名称s")
    private String templateNames;

    @ApiModelProperty("计划执行时间")
    private Date planExecutionTime;

    @ApiModelProperty("执行开始时间")
    private Date executionBeginTime;

    @ApiModelProperty("执行完成时间")
    private Date executionFinishTime;

    @ApiModelProperty("附加信息")
    private String attach;

    @ApiModelProperty("状态")
    private String state;

    @ApiModelProperty("宽表执行情况")
    private String syncState;

    @ApiModelProperty("工作流执行状态")
    private String workFlowState;

    @ApiModelProperty("库表对比执行情况")
    private String tableDataCompareState;

    @ApiModelProperty("项目qc执行情况")
    private String qcState;

    @ApiModelProperty("数据对比执行情况")
    private String planCompareState;

    @ApiModelProperty("powerBI执行情况")
    private String pushPowerBiState;

    @ApiModelProperty("创建类型【自动、手动】")
    private String createCategory;

    @ApiModelProperty("创建人")
    private Long creator;

    @ApiModelProperty("创建人名称")
    private String creatorName;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("执行时长")
    private String costTime;

    @ApiModelProperty("交付周期")
    private String deliveryCycle;
}
