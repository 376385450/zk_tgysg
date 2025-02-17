package com.sinohealth.system.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplicationSelectListVo {
    @ApiModelProperty("申请Id")
    private Long id;

    @ApiModelProperty("项目名称")
    private String projectName;

    @ApiModelProperty("数据有效截止时间")
    private LocalDateTime dataExpire;

    /**
     * @see com.sinohealth.system.domain.constant.UpdateRecordStateType
     */
    @ApiModelProperty("数据同步状态")
    private Integer updateState;
}
