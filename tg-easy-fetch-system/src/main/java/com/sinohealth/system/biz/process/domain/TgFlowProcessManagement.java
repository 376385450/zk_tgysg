package com.sinohealth.system.biz.process.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 全流程管理
 *
 * @author zegnjun
 * 2024-08-05 15:17
 * @see TgFlowProcessErrorLog 异常日志
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("tg_flow_process_management")
public class TgFlowProcessManagement implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    @ApiModelProperty("业务类型")
    private String bizType;

    @ApiModelProperty("设置id")
    private Long settingId;

    @ApiModelProperty("流程名称")
    private String name;

    @ApiModelProperty("期数")
    private String period;

    /**
     * @see FlowProcessTypeEnum
     */
    @ApiModelProperty("版本类型【QC/SOP/交付版本/临时改数版本")
    private String versionCategory;

    @ApiModelProperty("底表资产编号")
    private Long tableAssetId;

    @ApiModelProperty("表当前快照id")
    private Long tableSnapshotId;

    @ApiModelProperty("表当前快照版本")
    private Integer tableSnapshotVersion;

    @ApiModelProperty("底表资产表名")
    private String tableAssetName;

    @ApiModelProperty("模板资产ids")
    private String templateIds;

    @ApiModelProperty("模板资产名称")
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

    @ApiModelProperty("宽表工作流id")
    private String syncId;

    @ApiModelProperty("工作流执行状态")
    private String workFlowState;

    @ApiModelProperty("资产升级状态")
    private String assetsUpdateState;

    @ApiModelProperty("库表对比执行情况")
    private String tableDataCompareState;

    @ApiModelProperty("库表对比对应版本记录")
    private Long tableDataCompareBizId;

    @ApiModelProperty("项目qc执行情况")
    private String qcState;

    @ApiModelProperty("数据对比执行情况")
    private String planCompareState;

    @ApiModelProperty("数据对比对应版本记录")
    private Long planCompareBizId;

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

    @TableLogic
    @ApiModelProperty("是否已删除")
    private Integer deleted;
}
