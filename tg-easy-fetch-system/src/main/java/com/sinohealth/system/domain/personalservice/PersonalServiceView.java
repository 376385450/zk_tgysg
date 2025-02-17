package com.sinohealth.system.domain.personalservice;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 个人服务视图
 *
 * @Author Zhangzifeng
 * @Date 2023/8/17 13:46
 */
@Data
public class PersonalServiceView {

    @ApiModelProperty("申请id")
    private Long applyId;

    @ApiModelProperty("资产id")
    private Long assetId;

    @ApiModelProperty("资产名称")
    private String assetName;

    @ApiModelProperty("资产目录")
    private String cataloguePath;

    @ApiModelProperty("资产目录中文名称")
    private String cataloguePathCn;

    @ApiModelProperty("申请了的服务类型Json")
    private String openServiceJson;

    @ApiModelProperty("服务有效期")
    private Date expireDate;

    @ApiModelProperty("交换任务id")
    private Integer syncTaskId;

    @ApiModelProperty("交换任务名称")
    private String taskName;

    @ApiModelProperty("任务状态")
    private Integer taskStatus;

    @ApiModelProperty("最后运行状态")
    private Integer lastRunningState;

    @ApiModelProperty("申请时间")
    private Date applyDate;

    @ApiModelProperty("关联ID")
    private Long relatedId;

    @ApiModelProperty("文档类型")
    private String docType;

    @ApiModelProperty("审批流程ID")
    private Long processId;

    @ApiModelProperty("资产绑定数据的名称,即资产实体名")
    private String assetBindingDataName;

    @ApiModelProperty("资产服务类型Json")
    private String allOpenServiceJson;

    @ApiModelProperty("资产上下架状态")
    private String assetShelfState;

    private Integer flowId;
}
