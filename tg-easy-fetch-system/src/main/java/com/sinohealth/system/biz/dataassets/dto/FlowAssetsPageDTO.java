package com.sinohealth.system.biz.dataassets.dto;

import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kuangcp
 * 2024-07-15 16:10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowAssetsPageDTO {

    private Long templateId;
    /**
     * 申请id
     */
    private Long applyId;

    private Long assetsId;

    @ApiModelProperty("需求ID")
    private String applicationNo;

    /**
     * 需求名
     */
    private String projectName;

    /**
     * 项目名
     */
    private String newProjectName;

    private String templateName;

    private String applicant;

    private String applyTime;

    private String flowName;
    private String prodCode;

    /**
     * 一次性 持续性
     */
    private Integer requireTimeType;

    /**
     * @see DeliverTimeTypeEnum
     */
    @ApiModelProperty("时间类型")
    private String deliverTimeType;

    /**
     * 前端不用，中间值字段
     */
    private String cronCN;

    /**
     * @see com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum
     */
    private String state;

}
