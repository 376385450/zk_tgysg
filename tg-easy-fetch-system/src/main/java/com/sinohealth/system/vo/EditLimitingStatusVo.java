package com.sinohealth.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@ApiModel
@Data
public class EditLimitingStatusVo implements Serializable {

    @ApiModelProperty(value = "限流id,[1,2,...]")
    private Set<Long> limitingIds;

    @ApiModelProperty(value = "修改状态(0禁用，1启用)")
    private Integer status;
}
