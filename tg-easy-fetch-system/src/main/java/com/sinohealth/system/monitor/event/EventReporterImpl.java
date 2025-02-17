package com.sinohealth.system.monitor.event;

import com.sinohealth.common.core.domain.entity.SysRole;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.monitor.OperateTypeEnum;
import com.sinohealth.common.enums.monitor.SecondSubjectTypeEnum;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.domain.EventLog;
import com.sinohealth.system.filter.ThreadContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 3:27 下午
 */
@Component
@RequiredArgsConstructor
public class EventReporterImpl implements EventReporter {

    private final EventLogDAO eventLogDAO;

    private final List<String> role_keys = Arrays.asList("data_manager", "admin");

    /**
     * 埋点数据过滤
     * 不记录管理员、数据管理员的操作事件
     */
    private boolean filter() {
        SysUser loginUser = ThreadContextHolder.getSysUser();
        if (StringUtils.isNull(loginUser) || CollectionUtils.isEmpty(loginUser.getRoles())) {
            return false;
        }
        for (SysRole sysRole : loginUser.getRoles()) {
            String roleKey = sysRole.getRoleKey();
            if (role_keys.contains(roleKey)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void logEvent(EventLog eventLog) {
        if (filter()) {
            eventLogDAO.save(eventLog);
        }
    }

    @Override
    public void operateLogEvent4View(String subjectId, String subjectName, SecondSubjectTypeEnum secondSubjectTypeEnum, List<LogDetail> logDetailList) {
        EventLog eventLog = EventLogBuilderFactory.log4Operate()
                .subjectId(subjectId)
                .subjectName(subjectName)
                .subjectType(secondSubjectTypeEnum.getSubjectType())
                .secondSubjectType(secondSubjectTypeEnum.getType())
                .operateType(OperateTypeEnum.QUERY)
                .userId(ThreadContextHolder.getSysUser().getUserId())
                .build();
        logEvent(eventLog);
    }

}
