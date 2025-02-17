package com.sinohealth.common.utils;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-17 11:24
 */

public class StopWatch extends org.springframework.util.StopWatch {

    public StopWatch() {
        super();
    }

    public void watchRun(String task, Runnable run) {
        this.start(task);
        run.run();
        this.stop();
    }

    public <T> T watchCall(String task, Call<T> call) {
        this.start(task);
        T result = call.call();
        this.stop();
        return result;
    }
}
