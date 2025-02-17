package com.sinohealth.system.domain.vo;

import com.sinohealth.common.constant.CommonConstants;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-01-30 19:39
 */
@Data
public class CustomFieldTemplateVO {
    /**
     * @see CommonConstants.ComputeWay
     */
    @ApiModelProperty("计算方式")
    private Integer computeWay;

    @ApiModelProperty("基础表ID")
    @NotNull
    private Long baseTableId;

    private String showName;
    private String comment;
}
