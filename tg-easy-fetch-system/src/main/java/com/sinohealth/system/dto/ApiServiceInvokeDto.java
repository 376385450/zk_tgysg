package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel(value = "服务记录")
public class ApiServiceInvokeDto implements Serializable {

    @ApiModelProperty(value = "apiid")
    private Long apiId;

    @ApiModelProperty(value = "服务中文名")
    private String apiName;

    @ApiModelProperty(value = "服务英文名")
    private String apiNameEn;

    @ApiModelProperty(value = "接口访问路径")
    private String requestPath;

    @ApiModelProperty(value = "api版本号（由操作用户定义）")
    private String apiVersionOut;

    @ApiModelProperty(value = "api版本号(内部维护字段)")
    private String apiVersion;

    @ApiModelProperty(value = "接口所属自定义组",hidden = true)
    private Long groupId;

    @ApiModelProperty(value = "接口所属自定义组")
    private String groupName;

    @ApiModelProperty(value = "调用人Id",hidden = true)
    private Long userId;

    @ApiModelProperty(value = "调用人所在组")
    private String userGroupName;

    @ApiModelProperty(value = "调用人")
    private String createBy;

    @ApiModelProperty(value = "调用时间")
    private Date createTime;

    @ApiModelProperty(value = "原因")
    private String invokeMessage;

    @ApiModelProperty(value = "本次调用响应时间")
    private String executeTime;

    @ApiModelProperty(value = "状态")
    private Integer invokeStatus;



}
