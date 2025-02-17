package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 15:20
 */
@Data
@TableName(value = "tg_deliver_email_template", autoResultMap = true)
public class TgDeliverEmailTemplate {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField(value = "receive_mails", typeHandler = JacksonTypeHandler.class)
    private List<String> receiveMails;

    private String title;

    private String content;

    private String identifyId;

    private String identifyContent;

    private Long createBy;

    private Long updateBy;

    private Date updateTime;

    private Date createTime;
}
