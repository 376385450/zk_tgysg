package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.common.enums.dataassets.AcceptanceStateEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-17 14:39
 */
@Data
public class AcceptRequest {

    @NotNull(message = "参数缺失")
    private Long assetsId;

    /**
     * @see AcceptanceStateEnum
     */
    @NotBlank(message = "状态缺失")
    private String state;

    @ApiModelProperty("验收说明")
    private String remark;

    private Long applicantId;

    private Boolean forceRetry;
}
