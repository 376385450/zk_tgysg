package com.sinohealth.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/25 10:58
 */
@Data
@ApiModel("审批管理的模型列表响应")
public class ApplicationManageModelListVo {

    @ApiModelProperty("申请ID")
    private Long applyId;

    @ApiModelProperty("资产ID")
    private Long assetId;

    @ApiModelProperty("需求名称")
    private String demandName;

    @ApiModelProperty("项目名称")
    private String projectName;

    @ApiModelProperty("资产名称")
    private String assetName;

    @ApiModelProperty("需求性质 1：内部分析、2：交付客户、3：对外宣传")
    private Integer requireAttr;

    @ApiModelProperty("客户名称")
    private String clientNames;

    @ApiModelProperty("需求类型，1：一次性需求、2：持续性需求")
    private String requireTimeType;

    @ApiModelProperty("数据有效期")
    private String expireDate;

    @ApiModelProperty("申请人")
    private String applicantName;

    @ApiModelProperty("申请时间")
    private String applyDate;

    @ApiModelProperty("申请类型")
    private String applyType;

    @ApiModelProperty("当前节点审批人用户ID")
    private String currentHandlers;

    @ApiModelProperty("当前节点审批人用户名称")
    private String currentHandlersCn;

    @ApiModelProperty("流程状态")
    private Integer processStatus;

    @ApiModelProperty("最后审批人")
    private String finalHandler;

    @ApiModelProperty("数据验收状态，wait：待验收，pass：验收通过，reject：验收不通过，version_roll：验收过期")
    private String dataAcceptStatus;

    private Long currentIndex;

    private String handleNodeJson;

}
