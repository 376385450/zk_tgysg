package com.sinohealth.system.biz.project.util;

import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.system.biz.project.domain.DataPlan;
import com.sinohealth.system.biz.project.dto.request.BizTypePlanVo;
import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Kuangcp
 * 2024-12-13 16:12
 */
@Slf4j
public class DataPlanUtil {

    public static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyyMM");

    public static List<DataPlan> buildPlans(Long userId, int year, List<BizTypePlanVo> plans) {
        return buildPlans(userId, year, plans, false);
    }

    /**
     * 假定三个节点依次递增，且 qc必须小于月底，sop可能大于月底，交付必须大于月底
     *
     * @param first 第一次设置排期
     */
    public static List<DataPlan> buildPlans(Long userId, int year, List<BizTypePlanVo> plans, boolean first) {
        LocalDate today = LocalDate.now();

        int curYear = today.getYear();
        LocalDate start = LocalDate.of(curYear, 1, 1);
        LocalDate prePeriodEnd = start.plusYears(year).minusDays(1);

        List<DataPlan> allPlans = new ArrayList<>();
        for (BizTypePlanVo plan : plans) {
            String bizType = plan.getBizType();
            Integer qc = plan.getQc();
            Integer sop = plan.getSop();
            Integer deliver = plan.getDeliver();

            log.info("plan: {}", JsonUtils.format(plan));

            LocalDate periodEnd;
            if (qc != 0) {
                periodEnd = prePeriodEnd.plusMonths(1).plusDays(qc - 1);
            } else if (sop != 0) {
                periodEnd = prePeriodEnd.plusMonths(1).plusDays(sop - 1);
            } else {
                periodEnd = prePeriodEnd.plusMonths(1).plusDays(deliver - 1);
            }
            LocalDate idx = today;
            // 首次初始化包含当天的数据
            if (first) {
                idx = idx.minusDays(1);
            }

            DataPlanCtx ctx = new DataPlanCtx(qc, sop, deliver, year);

            while (idx.isBefore(periodEnd)) {
                idx = idx.plusDays(1);
                DayOfWeek dayOfWeek = idx.getDayOfWeek();
                DataPlan data = new DataPlan()
                        .setHoliday(Objects.equals(dayOfWeek, DayOfWeek.SUNDAY) || Objects.equals(dayOfWeek, DayOfWeek.SATURDAY))
                        .setDay(idx)
                        .setBizType(bizType)
                        .setDuration(1)
                        .setUpdater(userId);
                allPlans.add(data);
                ctx.fill(idx, data);

                log.info("{} {} {} {} {}", plan.getBizType(), idx, data.getPeriod(), data.getFlowProcessType(), data.getDuration());
            }
//            log.info("end");
        }
        return allPlans;
    }
}
