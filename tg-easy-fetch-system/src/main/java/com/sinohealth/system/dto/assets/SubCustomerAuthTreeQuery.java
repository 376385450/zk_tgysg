package com.sinohealth.system.dto.assets;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2023-03-10 10:36
 */
@Data
@ApiModel("子账号已分配资产目录树查询")
public class SubCustomerAuthTreeQuery implements Serializable {

    @NotNull(message = "父账号id不能为空")
    private Long parentUserId;

    @NotNull(message = "子账号id不能为空")
    private Long subUserId;

}
