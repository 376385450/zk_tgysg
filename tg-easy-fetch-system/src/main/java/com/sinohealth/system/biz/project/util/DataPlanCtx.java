package com.sinohealth.system.biz.project.util;

import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.biz.project.domain.DataPlan;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * @author Kuangcp
 * 2024-12-25 10:40
 */
@Slf4j
@Data
public class DataPlanCtx {

    public static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyyMM");

    int qc;
    int sop;
    int deliver;

    int year;
    boolean useQc;
    boolean useSop;

    Deque<DataPlanIdx> timeline = new ArrayDeque<>();

    public DataPlanCtx(int qc, int sop, int deliver, int year) {
        this.qc = qc;
        this.sop = sop;
        this.deliver = deliver;
        this.year = year;

        this.useQc = qc > 0;
        this.useSop = sop > 0;


        LocalDate now = LocalDate.now();
        // 时间段跨年 覆盖边界情况
        LocalDate pp = LocalDate.of(now.getYear() - 1, 12, 1);

        int months = 12 * year + 4;
        for (int i = 0; i < months; i++) {
            LocalDate lastEnd = pp.plusMonths(i).minusDays(1);
            if (useQc) {
                timeline.add(new DataPlanIdx(lastEnd.plusDays(qc), lastEnd.format(YM), qc, FlowProcessTypeEnum.qc.name()));
            }
            if (useSop) {
                timeline.add(new DataPlanIdx(lastEnd.plusDays(sop), lastEnd.format(YM), sop, FlowProcessTypeEnum.sop.name()));
            }
            timeline.add(new DataPlanIdx(lastEnd.plusDays(deliver), lastEnd.format(YM), deliver, FlowProcessTypeEnum.deliver.name()));
        }
    }

    public DataPlanIdx peek(LocalDate now) {
        DataPlanIdx tmp = null;
        while (true) {
            if (Objects.isNull(timeline.peek())) {
                throw new RuntimeException("数据排期异常");
            }

            if (!(timeline.peek().date.isBefore(now) || timeline.peek().date.isEqual(now))) {
                break;
            }
            tmp = timeline.pop();
        }
        if (Objects.nonNull(tmp)) {
            timeline.addFirst(tmp);
        }
        return timeline.peek();
    }

    public void fill(LocalDate now, DataPlan plan) {
        DataPlanIdx peek = peek(now);
        plan.setPeriod(peek.period);
        plan.setDuration(peek.duration);
        plan.setFlowProcessType(peek.type);
    }
}
