package com.sinohealth.system.biz.dataassets.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @author zhangyanping
 * @date 2023/7/5 13:37
 */
@Data
@ToString
@ApiModel
public class ProcessDefStatusDTO {
    @ApiModelProperty(value = "正在运行中的实例数量")
    private Integer runningCount;


    public static final ProcessDefStatusDTO NONE = new ProcessDefStatusDTO(0);
    public static final ProcessDefStatusDTO ONE = new ProcessDefStatusDTO(1);

    public ProcessDefStatusDTO() {
    }

    public ProcessDefStatusDTO(Integer runningCount) {
        this.runningCount = runningCount;
    }
}
