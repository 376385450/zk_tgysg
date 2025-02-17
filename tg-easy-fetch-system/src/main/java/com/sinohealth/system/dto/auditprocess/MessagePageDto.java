package com.sinohealth.system.dto.auditprocess;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-06-06 17:38
 * @Desc
 */
@ApiModel(description = "")
@Data
public class MessagePageDto {
    @ApiModelProperty("消息记录ID")
    private Long rid;
    /**
     * 浮窗标题
     */
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("内容")
    private String content;
    /**
     * 列表时标题
     */
    @ApiModelProperty("HTML标题")
    private String htmlTitle;
    @ApiModelProperty("申请ID")
    private Long applicationId;
    @ApiModelProperty("项目名称")
    private String projectName;
    @ApiModelProperty("申请人名称")
    private String applicantName;
    @ApiModelProperty("申请类型：文档、提数")
    private String applicationType;
    @ApiModelProperty("表中文别名")
    private String tableAlias;
    @ApiModelProperty("消息类型 1 需要审核 2 申请通过")
    private Integer type;
    @ApiModelProperty("创建时间")
    private String createTime;
    @ApiModelProperty("1 弹窗 2 提数数值 3 列表数据")
    private Integer dataType;
    @ApiModelProperty("未读消息条数")
    private Integer messageCount;
    @ApiModelProperty("消息列表")
    private List<MessagePageDto> messageList;
    @ApiModelProperty("申请模板名称")
    private String templateName;
}
