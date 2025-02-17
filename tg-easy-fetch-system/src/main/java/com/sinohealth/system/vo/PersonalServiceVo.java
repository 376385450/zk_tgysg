package com.sinohealth.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/17 11:35
 */
@Data
@ApiModel("我的服务列表展示实体类")
@Accessors(chain = true)
public class PersonalServiceVo {

    @ApiModelProperty("申请id")
    private Long applyId;

    @ApiModelProperty("资产id")
    private Long assetId;

    @ApiModelProperty("资产名称")
    private String assetName;

    @ApiModelProperty("资产目录中文名称")
    private String cataloguePathCn;

    @ApiModelProperty("交换任务名称")
    private String taskName;

    @ApiModelProperty("工作流id")
    private Integer flowId;

    @ApiModelProperty("任务状态")
    private Integer taskStatus;

    @ApiModelProperty("最后运行状态")
    private Integer lastRunningState;

    @ApiModelProperty("服务类型")
    private String openService;

    @ApiModelProperty("服务状态，1：可使用，0：已过期")
    private Integer serviceStatus;

    @ApiModelProperty("服务有效期")
    private String expireDate;

    @ApiModelProperty("申请时间")
    private String applyDate;

    @ApiModelProperty("关联ID")
    private Long relatedId;

    @ApiModelProperty("文档类型")
    private String docType;

    @ApiModelProperty("审批流程ID")
    private Long processId;

    @ApiModelProperty("资产绑定数据的名称,即资产实体名")
    private String assetBindingDataName;

    @ApiModelProperty("权限")
    private List<String> permissionList;

    @ApiModelProperty("资产上下架状态")
    private String assetShelfState;
}
