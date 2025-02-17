package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author Kuangcp
 * 2024-08-09 15:19
 */
@Data
public class AssetsFlowBatchEditRequest {

    private Long id;

    @Length(max = 64, message = "期数 超长")
    private String period;

    /**
     * @see FlowProcessTypeEnum
     */
    private String flowProcessType;

    private String remark;
}
