package com.sinohealth.system.vo;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@ApiModel
public class ApprovalQueryVo implements Serializable {

    //申请名称
    @ApiModelProperty("申请名称")
    private String applyName;
    //申请类型
    @ApiModelProperty("申请类型")
    private Integer applyType;
    //申请开始时间
    @ApiModelProperty("申请开始时间")
    private String startDate;
    //申请开始时间
    @ApiModelProperty("申请开始时间")
    private String endDate;
    //申请人
    @ApiModelProperty("申请人")
    private String applicant;

    //状态
    @ApiModelProperty("审批状态")
    private Integer status;

    @ApiModelProperty("当前用户id")
    private Long userId;

    @ApiModelProperty("审批状态（1待审批默认、0其它状态）")
    private int waitingAduit = 1;

    private List<OrderItem> orderItemList;

}
