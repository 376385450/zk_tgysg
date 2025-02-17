package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author Rudolph
 * @Date 2022-05-30 9:42
 * @Desc
 */

@ApiModel(description = "")
@Data
public class TgMessageRecordDim extends Model<TgMessageRecordDim> {
    @ApiModelProperty("主键ID,自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @ApiModelProperty("流程ID")
    private Long processId;
    @ApiModelProperty("流程版本")
    private Integer processVersion;
    @ApiModelProperty("申请ID")
    private Long applicationId;
    @ApiModelProperty("申请人ID")
    private Long applicantId;
    @ApiModelProperty("申请人名称")
    private String applicantName;
    /**
     * 1.代办 2.申请
     */
    @ApiModelProperty("通知类型")
    private Integer noticeType;
    @ApiModelProperty("申请类型：文档、提数")
    private String applicationType;
    @ApiModelProperty("申请时间")
    private String applyTime;
    @ApiModelProperty("处理时间")
    private String handleTime;
    @ApiModelProperty("被通知人")
    private Long adviceWho;
    @ApiModelProperty("资源链接")
    private String resourceLink;
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("内容")
    private String content;
    @ApiModelProperty("状态")
    private Integer status;
    @ApiModelProperty("是否已推送 1 未推送 2 已推送")
    private Integer pushed;
    @ApiModelProperty("是否已读 1 未读 2 已读")
    private Integer viewed;
    @ApiModelProperty("消息类型  1 需要审核 2 申请通过")
    private Integer type;
    /**
     * 出数状态
     * @see ApplyDataStateEnum
     */
    private String dataState;
}
