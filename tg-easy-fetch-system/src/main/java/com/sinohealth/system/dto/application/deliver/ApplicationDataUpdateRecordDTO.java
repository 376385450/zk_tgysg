package com.sinohealth.system.dto.application.deliver;

import java.io.Serializable;
import java.util.Date;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-12-08 17:51
 */
public class ApplicationDataUpdateRecordDTO implements Serializable {

    private Long id;

    /**
     * 关联申请数据id
     */
    private Long applicationId;

    /**
     * 开始更新时间
     */
    private Date startTime;

    /**
     * 完成时间
     */
    private Date finishTime;
    /**
     * 更新状态 待更新 更新中 成功 失败
     */
    private Integer updateState;

    /**
     * 更新数据量
     */
    private Integer updateCount;

    /**
     * 更新人 id
     */
    private Long updaterId;

    /**
     * 触发类型 手动触发（归类到统一执行），自动触发（因为使用BI而需要的数据准备）
     */
    private Integer triggerType;

}
