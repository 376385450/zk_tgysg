package com.sinohealth.system.dto.customer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author shallwetalk
 * @Date 2024/1/10
 */
@Data
@ApiModel("保存客户")
public class SaveCustomerReq {

    private Long id;

    @NotBlank
    private String shortName;

    private String fullName;

    @ApiModelProperty(" 1：KA、2：百强工业、3：非百强工业、4：连锁、5：其他、6：中康")
    private Integer customerType;

    @ApiModelProperty("1 启用 0 禁用")
    private Integer customerStatus;

}
