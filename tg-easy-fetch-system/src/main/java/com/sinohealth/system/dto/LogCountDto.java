package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jingjun
 * @since 2021/5/26
 */
@Getter
@Setter
@ApiModel("LogCountDto")
public class LogCountDto {
    @ApiModelProperty("日期")
    private String date;

    @ApiModelProperty("变更类型， 43 查询 44 导出")
    private Integer logType;

    @ApiModelProperty("次数")
    private int times;
}
