package com.sinohealth.system.biz.dataassets.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatch;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcBatch;
import com.sinohealth.system.biz.dataassets.util.DataAssetsUtil;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

@Setter
@Getter
@ToString
public class AssetsFlowBatchDetailVO {

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
