package com.sinohealth.system.biz.project.util;

import java.time.LocalDate;

/**
 * @author Kuangcp
 * 2024-12-25 11:14
 */
public class DataPlanIdx {
    LocalDate date;
    String period;
    int duration;
    String type;

    public DataPlanIdx(LocalDate date, String period, int duration, String type) {
        this.date = date;
        this.period = period;
        this.duration = duration;
        this.type = type;
    }
}
