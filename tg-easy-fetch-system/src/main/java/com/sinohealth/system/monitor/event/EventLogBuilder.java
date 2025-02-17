package com.sinohealth.system.monitor.event;

import com.sinohealth.common.enums.monitor.EventTypeEnum;
import com.sinohealth.common.enums.monitor.OperateTypeEnum;
import com.sinohealth.common.enums.monitor.SubjectTypeEnum;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.system.domain.EventLog;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.Optional;

/**
 * 由于一些字段是某些操作独有的，所以一些字段的设置下放到子类中
 *
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 3:33 下午
 */
public abstract class EventLogBuilder<T extends EventLogBuilder<?>> {

    /**
     * 用户id
     */
    protected Long userId;

    /**
     * 操作类型 , 更新, 创建, 删除
     */
    protected OperateTypeEnum operateType;

    /**
     * 操作主体id
     */
    protected String subjectId;

    /**
     * 操作主体名称
     */
    protected String subjectName;

    /**
     * 操作主体类型
     */
    protected SubjectTypeEnum subjectType;

    /**
     * 操作主体二级操作名称
     */
    protected String secondSubjectType;

    /**
     * 业务数据字段
     */
    protected String eventLogData;

    /**
     * 方法名称
     */
    protected String methodName;

    /**
     * 事件发生的日期 yyyy-MM-dd
     */
    protected String logDate;

    /**
     * 事件发生的日期时间 yyyy-MM-dd HH:mm:ss
     */
    protected String logTime;

    protected abstract T self();

    protected abstract EventTypeEnum eventType();

    /**
     * 校验扩展点
     */
    protected void validate() {
        Assert.isTrue(Objects.nonNull(userId), "userId为空");
        Assert.isTrue(Objects.nonNull(eventType()), "eventType为空");
    }

    public final T userId(Long userId) {
        this.userId = userId;
        return self();
    }

    public final T operateType(OperateTypeEnum operateType) {
        this.operateType = operateType;
        return self();
    }

    public final T eventLogData(String eventLogData) {
        this.eventLogData = eventLogData;
        return self();
    }

    public EventLog build() {
        validate();
        EventLog eventLog = new EventLog();
        eventLog.setUserId(userId);
        eventLog.setLogTime(DateUtils.dateTimeNow("yyyy/MM/dd HH:mm:ss"));
        eventLog.setLogDate(DateUtils.dateTimeNow("yyyy/MM/dd"));
        eventLog.setEventType(eventType().getType());
        eventLog.setSubjectId(subjectId);
        eventLog.setSubjectName(subjectName);
        eventLog.setSubjectType(Optional.ofNullable(subjectType).map(SubjectTypeEnum::getType).orElse(null));
        eventLog.setSecondSubjectType(secondSubjectType);
        eventLog.setOperateType(Optional.ofNullable(operateType).map(OperateTypeEnum::getType).orElse(null));
        eventLog.setEventLogData(eventLogData);
        return eventLog;
    }

}
