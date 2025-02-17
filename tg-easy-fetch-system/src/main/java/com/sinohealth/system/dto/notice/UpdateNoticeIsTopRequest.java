package com.sinohealth.system.dto.notice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/23 15:26
 */
@Data
@ApiModel
public class UpdateNoticeIsTopRequest {

    @NotNull(message = "主键ID不能为空")
    @ApiModelProperty("主键ID")
    private Long id;

    @NotNull(message = "是否置顶参数不能为空")
    @ApiModelProperty("是否置顶，0：不置顶，1：置顶")
    private Integer isTop;
}
