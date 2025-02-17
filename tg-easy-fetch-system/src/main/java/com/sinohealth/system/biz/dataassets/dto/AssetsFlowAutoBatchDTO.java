package com.sinohealth.system.biz.dataassets.dto;

import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.domain.constant.ApplicationConst;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Kuangcp
 * 2024-08-12 10:33
 */
@Data
public class AssetsFlowAutoBatchDTO {

    private Long id;

    private String name;

    private String cron;


    private String bizType;

    /**
     * 多值 ,
     */
    private String templateIds;

    private String applyIds;

    /**
     * @see com.sinohealth.system.biz.dataassets.constant.AutoFlowTypeEnum
     */
    private String autoType;

    /**
     * @see FlowProcessTypeEnum
     */
    private String flowProcessType;


    private String projectName;

    /**
     * @see ApplicationConst.RequireTimeType
     */
    @ApiModelProperty("需求类型 1：一次性需求、2：持续性需求")
    private List<Integer> requireTimeType;

    /**
     * 交付周期
     *
     * @see DeliverTimeTypeEnum
     */
    @ApiModelProperty("时间类型")
    private List<String> deliverTimeType;

    @ApiModelProperty("工作流名称")
    private List<String> flowName;
}
