package com.sinohealth.system.biz.dataassets.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.system.biz.dataassets.service.impl.AssetsUpgradeTriggerServiceImpl;
import com.sinohealth.system.biz.table.service.impl.TableInfoSnapshotServiceImpl;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 资产更新：日志型触发 一条配置触发一次
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-08-23 17:19
 * @see TableInfoSnapshotServiceImpl#pushTable 创建任务
 * @see AssetsUpgradeTriggerServiceImpl#scheduleWideTable 消费任务
 * @see AssetsFlowBatchDetail 工作流触发
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_assets_wide_upgrade_trigger")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AssetsWideUpgradeTrigger extends Model<AssetsWideUpgradeTrigger> implements IdTable {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("业务关联id")
    private Long bizId;

    private Long assetsId;
    private Long applyId;

    private Long tableId;

    /**
     * 触发更新的 底表版本
     */
    private Integer actVersion;
    /**
     * 用于对比的底表历史版本
     * <p>
     * 资产做对比时需要取该底表版本的最新资产版本来做参照 null 时不执行资产比对逻辑
     */
    private Integer preVersion;

    /**
     * @see AssetsQcBatch#id
     */
    private Long qcBatchId;

    /**
     * 是否需要qc
     */
    @ApiModelProperty("是否需要qc")
    private Boolean needQc;

    /**
     * 是否需要数据对比
     */
    @ApiModelProperty("是否需要数据对比")
    private Boolean needCompare;

    /**
     * @see AssetsUpgradeStateEnum
     */
    private String state;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
    /**
     * 启动时间
     */
    private LocalDateTime startTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;
}
