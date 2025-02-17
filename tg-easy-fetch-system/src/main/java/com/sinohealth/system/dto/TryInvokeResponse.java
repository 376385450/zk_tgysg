package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;


/**
 * 行业概念添加对象 data_business
 *
 * @author dataplatform
 * @date 2021-05-13
 */
@Data
@ApiModel("行业概念添加对象")
public class TryInvokeResponse implements Serializable {

    @ApiModelProperty("userId")
    private Long userId;

    @ApiModelProperty("服务中文名称")
    private String apiVersionId;

    @ApiModelProperty("服务中文名称")
    private String apiName;

    @ApiModelProperty("服务英文名称")
    private String apiNameEn;

    @ApiModelProperty("接口访问路径")
    private String requestPath;

    @ApiModelProperty(value = "请求方式 例：GET/PUT/POST/DELETE")
    private String requestMethod;

    @ApiModelProperty(" 0-草稿 1-发布（审核中） 2-已上线（审核通过）3-已驳回（审核不通过）")
    private String apiStatus;


}
