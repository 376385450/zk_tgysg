package com.sinohealth.system.dto.notice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/23 15:33
 */
@Data
@ApiModel("删除公告请求参数")
public class DeleteNoticeRequest {

    @NotNull(message = "主键id不能为空")
    @ApiModelProperty("主键ID")
    private Long id;
}
