package com.sinohealth.system.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * api服务池接口服务响应数据
 *
 * @author che
 * @date 2021-05-13
 */
@Data
@ApiModel("api服务池接口服务响应数据")
public class ApiPoolQueryResponse implements Serializable {

    /**  */
    @TableId(value = "id")
    private Long id;

    @ApiModelProperty("api服务名称")
    private String apiName;

    @ApiModelProperty("接口英文名称")
    private String apiNameEn;

    @ApiModelProperty(" api版本号(内部维护字段)")
    private Long apiVersion;

    @ApiModelProperty(" 分组名称")
    private String groupName;

    @ApiModelProperty(" 接口访问路径")
    private String requestPath;

    @ApiModelProperty(" 0-草稿 1-发布（审核中） 2-已上线（审核通过）3-已驳回（审核不通过）")
    private String apiStatus;

    @ApiModelProperty(" 更新人")
    private String updateBy;

    @ApiModelProperty(" 更新时间")
    private Date updateTime;

    @ApiModelProperty(" 创建时间")
    private Date createTime;

    @ApiModelProperty(" 创建人")
    private String createBy;


    @ApiModelProperty(" 请求方式")
    private String requestMethod;

    @ApiModelProperty("订阅人数")
    private Integer subscribeCount;

    @ApiModelProperty(" 备注")
    private String remark;

    @ApiModelProperty(" 接口关联组ID")
    private String groupId;

    @ApiModelProperty(" 创建人ID")
    private Long createId;


    @ApiModelProperty(" 当前用户是否订阅，0未订阅，1订阅")
    private Integer currentUserSubscribed = 0;

    @ApiModelProperty("接口限制规则")
    private String rule;

    @ApiModelProperty("限流类型(1接口限流,2ip限流)")
    private Integer limitRuleType;

    @ApiModelProperty("限制规则单位(0不限制,1年,2月,3周,4日,5时,6分,7秒)")
    private Integer limitRule;

    @ApiModelProperty("限制规则/次(类型为0时不限制次数)")
    private Integer limitFrequency;

    @ApiModelProperty("IP限流")
    private String limitIpRule;

}
