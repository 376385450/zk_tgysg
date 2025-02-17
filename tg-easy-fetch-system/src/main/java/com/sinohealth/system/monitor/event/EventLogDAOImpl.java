package com.sinohealth.system.monitor.event;

import com.sinohealth.system.domain.EventLog;
import com.sinohealth.system.mapper.EventLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * fixme 用mq异步保存埋点数据
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 4:08 下午
 */
@Component
@RequiredArgsConstructor
public class EventLogDAOImpl implements EventLogDAO {

    private final EventLogMapper eventLogMapper;

    @Async
    @Override
    public void save(EventLog eventLog) {
        eventLogMapper.insert(eventLog);
    }

}
