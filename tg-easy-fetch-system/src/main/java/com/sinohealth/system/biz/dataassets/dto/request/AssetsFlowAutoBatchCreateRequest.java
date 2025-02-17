package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.domain.constant.ApplicationConst;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author Kuangcp
 * 2024-08-12 09:10
 */
@Data
public class AssetsFlowAutoBatchCreateRequest {

    private Long id;

    @NotBlank(message = "任务名为空")
    @Length(max = 64, message = "任务名超长")
    private String name;

    private String cron;

    @NotBlank(message = "业务线为空")
    private String bizType;

    @NotEmpty(message = "模板不能为空")
    private List<Long> templateIds;

    private List<Long> applyIds;

    /**
     * @see com.sinohealth.system.biz.dataassets.constant.AutoFlowTypeEnum
     */
    private String autoType;

    /**
     * 版本类型
     *
     * @see FlowProcessTypeEnum
     */
    private String flowProcessType;

    // 以下为筛选参数 保存用于后续过滤

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
