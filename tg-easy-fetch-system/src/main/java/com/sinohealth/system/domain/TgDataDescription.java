package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Date;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-07 14:07
 */
@Data
@TableName(value = "tg_data_description", autoResultMap = true)
public class TgDataDescription {

    private Integer id;

    /**
     * 资产id
     */
    private Long assetsId;

    /**
     * 文档名称
     */
    private String docName;

    /**
     * 数据指标
     */
    private String dataQuota;

    /**
     * 数据说明
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private TgDataDescriptionItem dataDesc;

    /**
     * 基础指标
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private TgDataDescriptionItem baseTarget;

    private Date createTime;

    private Date updateTime;

    private Long createBy;

}
