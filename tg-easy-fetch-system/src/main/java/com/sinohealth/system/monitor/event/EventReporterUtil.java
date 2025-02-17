package com.sinohealth.system.monitor.event;

import com.sinohealth.common.enums.monitor.SecondSubjectTypeEnum;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.spring.SpringUtils;
import com.sinohealth.system.domain.EventLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 事件上报
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 3:10 下午
 */
public class EventReporterUtil {

    private static final Logger log = LoggerFactory.getLogger(EventReporterUtil.class);

    private EventReporterUtil() {
    }

    public static void logEvent(EventLog eventLog) {
        try {
            EventReporter eventReporter = SpringUtils.getBean(EventReporter.class);
            eventReporter.logEvent(eventLog);
        } catch (Exception var2) {
            log.error("[logEvent]上报日志失败。eventLog:{}", eventLog, var2);
        }
    }

    public static void operateLogEvent4View(String subjectId, String subjectName, SecondSubjectTypeEnum secondSubjectEnum, List<LogDetail> logDetailList) {
        try {
            EventReporter eventReporter = SpringUtils.getBean(EventReporter.class);
            eventReporter.operateLogEvent4View(subjectId, subjectName, secondSubjectEnum, logDetailList);
        } catch (Exception exception) {
            log.error("[operateLogEvent4View]上报日志失败。subjectId:{}, subjectName:{}, secondSubjectEnum:{}, logDetailList:{}", subjectId, subjectName, secondSubjectEnum, JsonUtils.format(logDetailList), exception);
        }
    }

}
