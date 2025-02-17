package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author Jingjun
 * @since 2021/5/13
 */
@Data
@ApiModel("TableLogUpdateDto")
public class TableLogUpdateDto {
    @NotNull
    @ApiModelProperty("备注")
    private String comment;
}
