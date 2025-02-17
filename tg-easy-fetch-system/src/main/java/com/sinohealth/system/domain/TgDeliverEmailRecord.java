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
 * @date 2022-12-05 13:53
 */
@Data
@TableName(value = "tg_deliver_email_record", autoResultMap = true)
public class TgDeliverEmailRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Long assetsId;

    private Long tableId;

    private String tableName;

    /**
     * 1打包交付，0非打包交付
     */
    private Integer allocateType;

    private String projectName;

    private String packName;

    /**
     * parentRecordId null 表示根节点
     */
    private Integer parentRecordId;

    private Long nodeId;

    private String icon;

    @TableField(value = "receiver", typeHandler = JacksonTypeHandler.class)
    private List<String> receiver;

    private String title;

    private String content;

    private Date sendTime;

    private Long createBy;

    private Date createTime;

}
