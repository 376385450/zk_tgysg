package com.sinohealth.system.dto.application.deliver;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 11:17
 */
@Data
@ApiModel("交付数据支持的方式")
public class ApplicationDeliverModeDTO implements Serializable {

    @ApiModelProperty("支持的交付方式, csv,pdf,email,excel")
    private List<String> modes;
}
