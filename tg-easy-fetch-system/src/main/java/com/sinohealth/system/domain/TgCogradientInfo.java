package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sinohealth.common.utils.SecurityUtils;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tg_cogradient_info")
public class TgCogradientInfo {

    @TableId(value = "id")
    private Integer id;

    /**
     * 表ID
     */
    private Long tableId;

    /**
     * 工作流id
     */
    private Integer processId;

    /**
     * cron配置 对应的调度id
     */
    private Integer scheduleId;

    private String processDefinitionJson;

    private String name;


    private String crontab;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /**
     * 状态：0上线，1下线
     */
    private Integer status;

    public TgCogradientInfo() {
    }

    public TgCogradientInfo(Long tableId, String processDefinitionJson, String name) {
        this.tableId = tableId;
        this.processDefinitionJson = processDefinitionJson;
        this.name = name;
        this.createBy = SecurityUtils.getUsername();
        this.createTime = new Date();
    }
}
