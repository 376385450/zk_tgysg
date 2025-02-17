package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(value = "接口统计")
public class ApiInvokeStatisticsDto implements Serializable {

    @ApiModelProperty(value = "apiid")
    private Long apiId;

    @ApiModelProperty(value = "服务中文名")
    private String apiName;

    @ApiModelProperty(value = "服务英文名")
    private String apiNameEn;

    @ApiModelProperty(value = "服务状态(0、驳回，1、通过，2、待审，3、撤销)")
    private Integer apiStatus;

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

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "调用总次数")
    private long invokeNum;

    @ApiModelProperty(value = "调用成功次数")
    private long invokeSuccessNum;

    @ApiModelProperty(value = "调用成功率")
    private BigDecimal invokeSuccessRate;

    @ApiModelProperty(value = "订阅量")
    private long subscribeNum;

}
