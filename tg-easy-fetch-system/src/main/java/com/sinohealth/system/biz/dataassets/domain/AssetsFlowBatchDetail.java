package com.sinohealth.system.biz.dataassets.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.system.biz.dataassets.service.impl.AssetsFlowServiceImpl;
import com.sinohealth.system.biz.dataassets.service.impl.AssetsUpgradeTriggerServiceImpl;
import com.sinohealth.system.biz.dataassets.util.DataAssetsUtil;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author Kuangcp
 * 2024-07-15 11:16
 * @see AssetsFlowServiceImpl#createBatch 创建批次
 * @see AssetsUpgradeTriggerServiceImpl#tryLockForCreateQcBatch 更新QC批次
 * @see AssetsUpgradeTriggerServiceImpl#schedulerRunFlow() 消费任务
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_assets_flow_batch_detail")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AssetsFlowBatchDetail extends Model<AssetsFlowBatchDetail> implements IdTable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * @see AssetsFlowBatch#id
     */
    private Long batchId;

    private Long templateId;

    private Long applicationId;

    private String projectName;

    private Long projectId;

    private Long applicantId;

    /**
     * 申请人
     */
    private String applicantName;
    /**
     * @see AssetsQcBatch#id
     */
    private Long qcBatchId;

    /**
     * @see com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum
     */
    private String state;

    /**
     * @see DataAssetsUtil#getFinalSchedulerId(TgTemplateInfo, TgApplicationInfo)
     */
    @ApiModelProperty("工作流ID")
    private Integer workflowId;

    private String workflowName;

    @ApiModelProperty("数据有效截止时间")
    private Date dataExpire;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
}
