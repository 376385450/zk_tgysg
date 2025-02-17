package com.sinohealth.system.biz.dataassets.dto.request;

import com.sinohealth.common.exception.CustomException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Kuangcp
 * 2025-01-18 16:37
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class AutoFlowBatchPageRequest {
    private String projectName;
    private String autoName;
    private String timeType;

    public enum TimeType {
        three_day, one_week, one_month;

        public static TimeType of(String type) {
            for (TimeType value : values()) {
                if (value.name().equals(type)) {
                    return value;
                }
            }
            return null;
        }

        public Date getEndDate() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime result;
            switch (this) {
                case one_week:
                    result = now.plusWeeks(1);
                    break;
                case one_month:
                    result = now.plusMonths(1);
                    break;
                case three_day:
                    result = now.plusDays(3);
                    break;
                default:
                    throw new CustomException("参数错误");
            }
            return Date.from(result.atZone(ZoneId.systemDefault()).toInstant());
        }
    }
}
