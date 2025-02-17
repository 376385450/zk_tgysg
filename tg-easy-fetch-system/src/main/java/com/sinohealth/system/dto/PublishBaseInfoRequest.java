package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * @Author manleo
 * @Date 2021/7/6 13:50
 * @Version 1.0
 */
@Data
@ApiModel("发布API服务 请求体")
@Accessors(chain = true)
public class PublishBaseInfoRequest {
    @ApiModelProperty(value = "api接口服务草稿ID")
    @NotBlank
    private String apiBaseInfoId;

    @ApiModelProperty("发布类型  1-新增 2-更新")
    @NotBlank
    private Integer publishType;

    @ApiModelProperty("发布分类")
    @NotBlank
    private String groupId;

    @ApiModelProperty("发布后的接口服务名称")
    @NotBlank
    private String apiName;

    @ApiModelProperty("如果是更新，需要传更新哪个版本的接口ID")
    private String apiVersionId;
}
