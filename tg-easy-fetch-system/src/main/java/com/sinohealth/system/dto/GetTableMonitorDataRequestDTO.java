package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 10:38 上午
 */
@Data
@ApiModel("监控查看，请求参数")
@Accessors(chain = true)
public class GetTableMonitorDataRequestDTO implements Serializable {

    @NotNull(message = "tableId不能为空")
    @ApiModelProperty("表单id")
    private Long tableId;
}
