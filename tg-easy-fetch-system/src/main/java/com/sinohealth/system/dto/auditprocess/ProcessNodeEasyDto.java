package com.sinohealth.system.dto.auditprocess;

import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.system.domain.constant.ApplicationConst;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @Author Rudolph
 * @Date 2022-05-24 10:41
 * @Desc
 */

@ApiModel(description = "流程审核节点")
@Data
public class ProcessNodeEasyDto {

    @ApiModelProperty("申请ID")
    private Long applicationId;
    @ApiModelProperty("流程节点索引,正在审核第 index 个节点")
    private Integer index;
    @ApiModelProperty("节点名称")
    private String nodeName;
    @ApiModelProperty("审核人名称")
    private String handlerName;
    @ApiModelProperty("审核时间")
    private String handleTime;

    @ApiModelProperty("驳回原因 审批说明")
    @Length(max = 200, message = "审批说明/驳回原因 超长")
    private String handleReason;

    @ApiModelProperty("交付时间 T+n")
    private Integer deliverDay;

    /**
     * @see ApplicationConst.AuditStatus
     */
    @ApiModelProperty("状态")
    private Integer status;
    /**
     * @see CommonConstants#HANDLED
     */
    @ApiModelProperty("该节点是否已经审核 1 未审核， 2 已审核")
    private Integer handleStatus;
}
