package com.sinohealth.system.biz.application.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @author Kuangcp
 * 2024-10-18 13:41
 */
@Slf4j
public class CostTimeUtil {

    public static int calcCostMin(String startTime, Long endMs) {
        // 解析字符串为 LocalDateTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime applyTime = LocalDateTime.parse(startTime, formatter);
        long applyMs = applyTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        BigDecimal floatVal = BigDecimal.valueOf((endMs - applyMs) / 60_000.0);
        int costMin = floatVal.setScale(0, RoundingMode.HALF_UP).intValue();

        if (costMin < 1) {
            log.warn("invalid cost: {}min {}", costMin, startTime);
            return 1;
        }
        log.info("cost: {}min {}", costMin, startTime);
        return costMin;
    }

    public static int calcCostMin(String startTime) {
        return calcCostMin(startTime, System.currentTimeMillis());
    }

    public static Integer calcCostMin(BigDecimal cost) {
        if (Objects.isNull(cost)) {
            return null;
        }
        return cost.multiply(BigDecimal.valueOf(30)).setScale(0, RoundingMode.HALF_UP).intValue();
    }
}
