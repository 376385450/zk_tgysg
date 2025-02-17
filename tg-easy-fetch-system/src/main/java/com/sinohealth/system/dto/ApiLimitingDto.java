package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;
import java.util.Set;

@ApiModel
@Data
public class ApiLimitingDto {
    private static final long serialVersionUID = 1L;


    /**
     *
     */
    private Long id;

    /**
     * 规则状态(0禁用，1启用)
     */
    @ApiModelProperty(value = "规则状态(0禁用，1启用)")
    private Integer status;

    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    @NotBlank(message = "规则名称")
    private String ruleName;

    /**
     * 限流类型(1接口限流,2ip限流)
     */
    @ApiModelProperty(value = "限流类型(1接口限流,2ip限流)")
    @NotBlank(message = "限流类型不允许为空")
    private Integer ruleType;

    /**
     * 规则说明
     */
    @ApiModelProperty(value = "规则说明")
    private String ruleDescription;

    @ApiModelProperty(value = "规则详情")
    private String ruleDetails;


    /**
     * 限制规则单位(0不限制,1年,2月,3周,4日,5时,6分,7秒)
     */
    @ApiModelProperty(value = "限制规则单位(0不限制,1年,2月,3周,4日,5时,6分,7秒)")
    private Integer limitRule = 0;

    /**
     * 限制规则/次(类型为0时不限制次数)
     */
    @ApiModelProperty(value = "限制规则/次(类型为0时不限制次数)")
    private Integer limitFrequency = 0;

    /**
     * IP限流
     */
    @ApiModelProperty(value = "IP限流")
    private String limitIpRule;


    /**
     * 创建人id
     */
    @ApiModelProperty(value = "创建人id")
    private Long createId;

    /**
     * 创建人名称
     */
    @ApiModelProperty(value = "创建人名称")
    private String createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "API数量")
    private Integer apiNum;

    @ApiModelProperty(value = "API_IDS")
    @NotBlank(message = "api不允许为空")
    private Set<Long> apiIds;

    @ApiModelProperty(value = "apiInfos")
    private Set<LimitingApiDto> apiInfos;

    /**
     * apiVersionId
     */
    private Long apiVersionId;

}
