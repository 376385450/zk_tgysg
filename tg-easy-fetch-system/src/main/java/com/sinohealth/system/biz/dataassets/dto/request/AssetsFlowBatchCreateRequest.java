package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Kuangcp
 * 2024-07-15 17:03
 */
@Data
public class AssetsFlowBatchCreateRequest {

    @NotBlank(message = "任务名为空")
    @Length(max = 64, message = "任务名超长")
    private String name;

    @Length(max = 200, message = "版本说明超长")
    private String remark;

    @NotBlank(message = "业务线为空")
    private String bizType;

    @NotEmpty(message = "模板不能为空")
    private List<Long> templateIds;

    @NotEmpty(message = "需求不能为空")
    private List<Long> applyIds;

    @NotNull(message = "期望时间为空")
    private LocalDateTime expectTime;

    private Boolean needQc;

    /**
     * 临时跑数版本 手动时才有
     *
     * @see FlowProcessTypeEnum
     */
    private Boolean tempType;

    @Length(max = 64, message = "期数 超长")
    private String period;
//    /**
//     * 版本类型
//     *
//     * @see FlowProcessTypeEnum
//     */
//    private String flowProcessType;

    /**
     * 业务关联id
     */
    private Long bizId;

    private Long autoId;

    /**
     * 申请单时间类型
     */
    private List<String> deliverTimeTypes;

    /**
     * 是否限制一次性需求
     */
    private Boolean filterOne = true;
}
