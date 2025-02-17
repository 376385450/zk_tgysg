package com.sinohealth.system.biz.process.dto;

import javax.validation.constraints.NotEmpty;

import com.sinohealth.common.enums.process.FlowProcessAlertCategory;
import com.sinohealth.common.enums.process.FlowProcessTaskEnum;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CreateOrUpdateFlowProcessAlertConfigDetailRequest {
    @ApiModelProperty("主键")
    private Long id;

    /**
     * @see FlowProcessAlertCategory
     */
    @ApiModelProperty("分类")
    @NotEmpty(message = "分类不可为空")
    private String category;

    /**
     * @see FlowProcessTaskEnum
     * @see FlowProcessTypeEnum
     */
    @ApiModelProperty("编码")
    @NotEmpty(message = "编码不可为空")
    private String code;

    @ApiModelProperty("配置名称")
    private String name;

    @ApiModelProperty("成功告警开关")
    private Boolean successAlertSwitch;

    @ApiModelProperty("成功告警webhook")
    private String successWebHook;

    @ApiModelProperty("成功告警群成员手机号")
    private String successMemberNumbers;

    @ApiModelProperty("成功告警标题")
    private String successAlertTitle;

    @ApiModelProperty("成功告警内容")
    private String successAlertContent;

    @ApiModelProperty("失败告警开光")
    private Boolean failAlertSwitch;

    @ApiModelProperty("失败告警webhook")
    private String failWebHook;

    @ApiModelProperty("失败告警群成员手机号")
    private String failMemberNumbers;

    @ApiModelProperty("失败告警标题")
    private String failAlertTitle;

    @ApiModelProperty("失败告警内容")
    private String failAlertContent;
}
