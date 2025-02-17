package com.sinohealth.system.vo;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@ApiModel
@Data
public class ApiLimitingQueryVo {

    private static final long serialVersionUID = 1L;


    /**
     * 规则名称
     */
    @ApiModelProperty(value = "规则名称")
    private String ruleName;


    /**
     * 创建人名称
     */
    @ApiModelProperty(value = "创建人名称")
    private String createBy;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "排序{key,value}key为字段名称，value为排序类型（1为升序，2为降序）")
    private List<OrderItem> orderFieldList;

}
