package com.sinohealth.system.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-20 10:36
 */
@Data
@JsonNaming
@Accessors(chain = true)
public class CustomerApplyAuthList implements Serializable {

    @ApiModelProperty("客户id")
    private Long userId;

    @ApiModelProperty("客户名称")
    private String customer;

    @ApiModelProperty("授权")
    private String authType;

    @ApiModelProperty("已分配目录id列表")
    private List<Long> ids;

    private List<CustomerApplyDTO> list;

    private List<TgCustomerApplyAuthDto> authList;
}
