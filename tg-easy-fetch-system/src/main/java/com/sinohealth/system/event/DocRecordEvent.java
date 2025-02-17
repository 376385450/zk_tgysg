package com.sinohealth.system.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * @Author Rudolph
 * @Date 2022-12-01 14:19
 * @Desc
 */
@Getter
@Setter
public class DocRecordEvent extends ApplicationEvent {

    private Integer eventCode;
    private String eventComment;
    private Long docId;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public DocRecordEvent(Object source) {
        super(source);
    }

    public DocRecordEvent(Object source, Long docId, Integer eventCode, String eventComment) {
        this(source);
        this.docId = docId;
        this.eventCode = eventCode;
        this.eventComment = eventComment;
    }
}
