package com.sinohealth.common.utils.task;

import java.util.concurrent.ScheduledFuture;

/**
 * @program:
 * @description: 定时任务控制类
 * @author: ChenJiaRong
 * @date: 2021/7/30
 **/
public final class ScheduledTask {

    public volatile ScheduledFuture<?> future;
    /**
     * 取消定时任务
     */
    public void cancel() {
        ScheduledFuture<?> future = this.future;
        if (future != null) {
            future.cancel(true);
        }
    }
}
