package com.sinohealth.system.vo;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.util.*;

@Data
@ApiModel(value = "服务监控查询对象")
public class ApiServiceQueryVo implements Serializable {

    @ApiModelProperty(value = "服务中文名称")
    private String apiName;

    @ApiModelProperty(value = "服务英文名称")
    private String apiNameEn;

    @ApiModelProperty(value = "自定义组")
    private Long groupId;

    @ApiModelProperty(value = "接口调用路径")
    private String url;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "调用人名称")
    private String userName;

    @ApiModelProperty(value = "调用人所属组")
    private Integer userGroup;

    @ApiModelProperty(value = "调用人所属组下用户", hidden = true)
    private Set<Long> userIds;

    @ApiModelProperty(value = "当前用户id", hidden = true)
    private Long userId;

    @ApiModelProperty(value = "允许查看api", hidden = true)
    private Collection<Long> apiIds;

    @ApiModelProperty(value = "api状态(1-发布（审核中） 2-已上线（审核通过）3-已驳回（审核不通过）)", hidden = true)
    private String apiStatus = "2";

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "排序{key,value}key为字段名称，value为排序类型（1为升序，2为降序）")
    private List<OrderItem> orderFieldList;

}
