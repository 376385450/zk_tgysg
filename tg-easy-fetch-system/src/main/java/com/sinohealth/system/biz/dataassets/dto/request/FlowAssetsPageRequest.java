package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.system.domain.constant.ApplicationConst;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Kuangcp
 * 2024-07-15 16:51
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FlowAssetsPageRequest {

    /**
     * 已创建批次查询：出数任务批次 id
     */
    private Long batchId;

    /**
     * 创建批次时的筛选：模板id
     */
    private List<Long> templateIds;

    // 以上参数二选一，以下参数共用

    private String projectName;

    /**
     * @see ApplicationConst.RequireTimeType
     */
    @ApiModelProperty("需求类型 1：一次性需求、2：持续性需求")
    private Integer requireTimeType;

    /**
     * 交付周期
     *
     * @see DeliverTimeTypeEnum
     */
    @ApiModelProperty("时间类型")
    private String deliverTimeType;

    @ApiModelProperty("工作流名称")
    private String flowName;
}
