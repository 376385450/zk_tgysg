package com.sinohealth.system.monitor.event;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 3:30 下午
 */
public class EventLogBuilderFactory {

    public EventLogBuilderFactory() {
    }

    public static OperateStrategyBuilder log4Operate() {
        return new OperateStrategyBuilder();
    }

}
