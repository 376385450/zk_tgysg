package com.sinohealth.system.monitor.event;

import com.sinohealth.system.domain.EventLog;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 4:08 下午
 */
public interface EventLogDAO {

    void save(EventLog eventLog);
}
