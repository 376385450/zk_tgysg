package com.sinohealth.system.monitor.event;

import com.sinohealth.common.enums.monitor.EventTypeEnum;
import com.sinohealth.common.enums.monitor.SubjectTypeEnum;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 3:31 下午
 */
public class OperateStrategyBuilder extends EventLogBuilder<OperateStrategyBuilder>{


    @Override
    protected OperateStrategyBuilder self() {
        return this;
    }

    @Override
    protected EventTypeEnum eventType() {
        return EventTypeEnum.OPERATE;
    }


    public final OperateStrategyBuilder subjectId(String subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public final OperateStrategyBuilder subjectName(String subjectName) {
        this.subjectName = subjectName;
        return this;
    }

    public final OperateStrategyBuilder subjectType(SubjectTypeEnum subjectType) {
        this.subjectType = subjectType;
        return this;
    }

    public final OperateStrategyBuilder secondSubjectType(String secondSubjectType) {
        this.secondSubjectType = secondSubjectType;
        return this;
    }

}
