package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Jingjun
 * @since 2021/4/24
 */
@ApiModel("TableFieldUpdateDto")
@Data
public class TableFieldUpdateDto {

    @ApiModelProperty
    private List<TableFieldInfoDto> list;
}
