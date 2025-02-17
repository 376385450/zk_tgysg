package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author zhangyanping
 * @date 2023/5/15 16:16
 */
@Data
@TableName(value = "tg_data_range_template", autoResultMap = true)
public class TgDataRangeTemplate {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 数据范围预设json
     */
    private String dataRangeConfig;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 修改者
     */
    private String updater;
    /**
     * 创建者
     */
    private String creator;

}
