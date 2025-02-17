package com.sinohealth.system.biz.process.dto;

import com.sinohealth.system.dto.common.PageRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FlowProcessPageRequest extends PageRequest {
    @ApiModelProperty("流程名称")
    private String name;

    @ApiModelProperty("流程状态")
    private String state;

    @ApiModelProperty("版本期数")
    private String period;

    @ApiModelProperty("版本类型")
    private String category;

    @ApiModelProperty("关联模板")
    private String modelAssertNames;
}
