package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jingjun
 * @since 2021/5/25
 */
@Getter
@Setter
@ApiModel("MyViewDto")
public class MyViewDto {
    @ApiModelProperty("表单数量")
    private long tableSize;
    @ApiModelProperty("累计更新数")
    private long totalUpdateRecord;
    @ApiModelProperty("昨日更新数")
    private long updateRecord;
}
