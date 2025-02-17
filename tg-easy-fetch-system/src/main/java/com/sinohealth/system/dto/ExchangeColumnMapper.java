package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/11/7
 */
@ApiModel("交换数据字段映射")
@Data
public class ExchangeColumnMapper implements Serializable {

    @ApiModelProperty("源字段")
    private List<ExchangeColumnDTO> sourceColumn;

    @ApiModelProperty("目标字段")
    private List<ExchangeColumnDTO> targetColumn;

}
