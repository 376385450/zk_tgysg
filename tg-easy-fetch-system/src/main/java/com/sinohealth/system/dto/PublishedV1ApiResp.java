package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author manleo
 * @Date 2021/7/6 17:22
 * @Version 1.0
 */
@Data
@ApiModel("获取当前登录用户的API服务或有管理权限的API服务 响应体")
@Accessors(chain = true)
public class PublishedV1ApiResp {
    @ApiModelProperty(value = "apiVersionId")
    private Long apiVersionId;

    @ApiModelProperty(value = "api服务名称")
    private String apiName;
}
