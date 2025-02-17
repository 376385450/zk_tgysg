package com.sinohealth.system.dto.application;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-05-16 14:15
 * @Desc
 */

@ApiModel(description = "")
@Data
public class Notice {

    /**
     * 消息推送方式 1 局内消息 2 邮件消息
     */
    @ApiModelProperty("消息推送方式 1 局内消息 2 邮件消息")
    private String way;

    /**
     * 通知用户id列表
     */
    @ApiModelProperty("通知用户id列表")
    @TableField(exist = false)
    private List<String> names;

    /**
     * 好像暂时没有用
     */
    @ApiModelProperty("")
    private String email;

    /**
     * 通知邮箱列表
     */
    @ApiModelProperty("通知邮箱列表")
    private List<String> emails;

    /**
     * 邮件标题
     */
    @ApiModelProperty("邮件标题")
    private String title;

    /**
     * 邮件内容
     */
    @ApiModelProperty("邮件内容")
    private String content;
    @ApiModelProperty("")
    private String link;
}

