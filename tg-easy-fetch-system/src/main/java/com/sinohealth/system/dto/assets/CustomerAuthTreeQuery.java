package com.sinohealth.system.dto.assets;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2023-02-13 20:04
 */
@Data
@ApiModel("客户已分配资产目录树查询")
public class CustomerAuthTreeQuery implements Serializable {

    @NotNull(message = "用户id")
    private Long userId;
}
