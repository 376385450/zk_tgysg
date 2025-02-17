package com.sinohealth.system.biz.process.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 全流程告警配置设置信息
 */
@Setter
@Getter
@ToString
public class FlowProcessAlertConfigVO {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("分类")
    private String category;

    @ApiModelProperty("编码")
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
