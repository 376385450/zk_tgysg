package com.sinohealth.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/25 9:49
 */
@Data
@ApiModel("审批管理的文件列表响应")
public class ApplicationManageFileListVo {

    @ApiModelProperty("申请ID")
    private Long applyId;

    @ApiModelProperty("资产ID")
    private Long assetId;

    @ApiModelProperty("资产名称")
    private String assetName;

    @ApiModelProperty("申请类型")
    private String serviceType;

    @ApiModelProperty("申请原因")
    private String applyReason;

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

    private Long currentIndex;

    private String handleNodeJson;

    private String docAuthorizationJson;
}
