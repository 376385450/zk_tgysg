package com.sinohealth.system.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @Author Rudolph
 * @Date 2022-12-01 14:21
 * @Desc
 */
@Component
@Slf4j
public class EventPublisher {
    @Autowired
    private ApplicationEventPublisher context;

    public void registerDocEvent(Long docId, Integer eventCode, String eventComment) {
        log.info("事件注册：{}", eventComment);
        context.publishEvent(new DocRecordEvent(this, docId, eventCode, eventComment));
    }
}
