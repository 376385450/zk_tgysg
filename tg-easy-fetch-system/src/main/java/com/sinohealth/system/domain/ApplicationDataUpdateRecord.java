package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.sinohealth.system.domain.constant.SyncTargetType;
import com.sinohealth.system.domain.constant.SyncTriggerType;
import com.sinohealth.system.domain.constant.UpdateRecordStateType;
import com.sinohealth.system.domain.vo.TgTableApplicationMappingInfo;
import lombok.Data;

import java.util.Date;

/**
 * 内网到内网 & 内网到外网 的 同步记录
 *
 * @author kuangchengping@sinohealth.cn
 * 2022-12-08 17:56
 */
@Data
@TableName("tg_application_data_update_record")
public class ApplicationDataUpdateRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资产id
     */
    private Long assetsId;

    /**
     * 资产版本
     */
    private Integer version;

    /**
     * 虚拟字段
     */
    @TableField(updateStrategy = FieldStrategy.NEVER, insertStrategy = FieldStrategy.NEVER)
    private String assetsVersion;

    /**
     * 开始更新时间
     */
    private Date startTime;

    /**
     * 完成时间
     */
    private Date finishTime;
    /**
     * 更新状态  1 待更新 2 更新中 3 成功 4 失败
     *
     * @see UpdateRecordStateType
     */
    private Integer updateState;

    /**
     * 更新数据量
     */
    private Long updateCount;

    /**
     * 更新人 id
     */
    private Long updaterId;

    /**
     * 触发同步的类型 手动触发（归类到统一执行），自动触发（因为使用BI而需要的数据准备）
     *
     * @see SyncTriggerType
     */
    private Integer triggerType;

    /**
     * 同步目标: 1 易数阁内网CK 2 客户外网CK
     *
     * @see SyncTargetType
     */
    private Integer syncTarget;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 数据表名
     *
     * @see TgTableApplicationMappingInfo#dataTableName
     */
    private String dataTableName;

    /**
     * 异常信息
     */
    private String cause;

}
