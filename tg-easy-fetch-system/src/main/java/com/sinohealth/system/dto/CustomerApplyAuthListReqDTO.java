package com.sinohealth.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-19 14:06
 */
@Data
@JsonNaming
public class CustomerApplyAuthListReqDTO implements Serializable {

    @ApiModelProperty("资产id")
    private Long assetsId;

    @ApiModelProperty("数据目录id列表")
    private List<Long> ids;

}
