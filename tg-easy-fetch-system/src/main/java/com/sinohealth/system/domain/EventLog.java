package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 11:39 上午
 */
@Data
@Accessors(chain = true)
@TableName("event_log")
public class EventLog implements Serializable {

    @TableId(value = "id")
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 日志类型
     */
    private String eventType;

    /**
     * 操作类型 , 更新, 创建, 删除
     */
    private String operateType;

    /**
     * 操作主体id
     */
    private String subjectId;

    /**
     * 操作主体名称
     */
    private String subjectName;

    /**
     * 一级操作类型
     */
    private String subjectType;

    /**
     * 操作主体二级操作名称
     */
    private String secondSubjectType;

    /**
     * 业务数据字段
     */
    private String eventLogData;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 事件发生的日期 yyyy/MM/dd
     */
    private String logDate;

    /**
     * 事件发生的日期时间 yyyy/MM/dd HH:mm:ss
     */
    private String logTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
