package com.sinohealth.system.monitor.event;

import com.sinohealth.common.enums.monitor.SecondSubjectTypeEnum;
import com.sinohealth.system.domain.EventLog;

import java.util.List;


/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 3:12 下午
 */
public interface EventReporter {

    /**
     * 事件上报
     * @param eventLog
     */
    void logEvent(EventLog eventLog);

    /**
     * 查看事件上报
     * @param subjectId 主体id
     * @param subjectName 主体名称
     * @param secondSubjectTypeEnum 操作
     * @param logDetailList 明细
     */
    void operateLogEvent4View(String subjectId, String subjectName, SecondSubjectTypeEnum secondSubjectTypeEnum, List<LogDetail> logDetailList);

}
