package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.system.domain.constant.ApplicationConst;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Kuangcp
 * 2024-11-01 14:07
 */
@Data
public class FlowAssetsAutoPageRequest {

    private Long autoBatchId;
    /**
     * 已创建批次查询：出数任务批次 id
     */
    private Long batchId;

    /**
     * 未创建批次时的筛选：模板id
     */
    private List<Long> templateIds;

    // 以上参数三选一，以下参数共用

    /**
     * 需求名 项目名
     */
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

    private List<Long> applyIds;

    /**
     * batchId 不为空，查运行批次的情况才使用
     */
    private List<String> state;
}
