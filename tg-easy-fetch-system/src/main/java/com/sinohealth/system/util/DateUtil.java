package com.sinohealth.system.util;


import com.sinohealth.common.exception.CustomException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author zhangyanping
 * @date 2023/11/20 18:09
 */
public class DateUtil {

    /**
     * 获取今日开始时间和当前时间
     */
    public static Pair<Date, Date> getToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        ZonedDateTime zonedDateTime = startOfDay.atZone(ZoneId.systemDefault());
        Date startTime = Date.from(zonedDateTime.toInstant());
        return Pair.of(startTime, new Date());
    }

    /**
     * 获取今日开始时间和当前时间
     */
    public static Pair<Date, Date> getYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterday.atStartOfDay().plusDays(1);

        Date startTime = Date.from(yesterdayStart.atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(yesterdayEnd.atZone(ZoneId.systemDefault()).toInstant());
        return Pair.of(startTime, endTime);
    }

    public static String localDateToMonthStr(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        return date.format(formatter);
    }

    public static String localDateToDayStr(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    public static List<String> generateContinuousDays(Date startTime, Date endTime) {
        LocalDate startDate;
        LocalDate endDate;
        if (startTime == null) {
            startDate = LocalDate.now().minusDays(365);
        } else {
            ZoneId defaultZoneId = ZoneId.systemDefault();
            ZonedDateTime zonedDateTime = startTime.toInstant().atZone(defaultZoneId);
            startDate = zonedDateTime.toLocalDate();
        }

        if (endTime == null) {
            endDate = LocalDate.now();
        } else {
            ZoneId defaultZoneId = ZoneId.systemDefault();
            ZonedDateTime zonedDateTime = endTime.toInstant().atZone(defaultZoneId);
            endDate = zonedDateTime.toLocalDate();
        }

        List<String> days = new ArrayList<>();
        LocalDate currentDate = startDate;
        // 循环生成连续的天数，直到当前日期等于或大于当前日期为止
        while (currentDate.isBefore(endDate)) {
            days.add(localDateToDayStr(currentDate));
            currentDate = currentDate.plusDays(1);
        }

        if (!days.contains(localDateToDayStr(endDate))) {
            days.add(localDateToDayStr(endDate));
        }

        return days;
    }


    public static List<String> generateContinuousMonths(Date startTime, Date endTime) {
        LocalDate startDate;
        LocalDate endDate;
        if (startTime == null) {
            startDate = LocalDate.now().minusDays(365);
        } else {
            ZoneId defaultZoneId = ZoneId.systemDefault();
            ZonedDateTime zonedDateTime = startTime.toInstant().atZone(defaultZoneId);
            startDate = zonedDateTime.toLocalDate();
        }

        if (endTime == null) {
            endDate = LocalDate.now();
        } else {
            ZoneId defaultZoneId = ZoneId.systemDefault();
            ZonedDateTime zonedDateTime = endTime.toInstant().atZone(defaultZoneId);
            endDate = zonedDateTime.toLocalDate();
        }


        List<String> months = new ArrayList<>();
        LocalDate currentDate = startDate;
        // 循环生成连续的天数，直到当前日期等于或大于当前日期为止
        while (currentDate.isBefore(endDate)) {
            months.add(localDateToMonthStr(currentDate));
            currentDate = currentDate.plusMonths(1);
        }

        if (!months.contains(localDateToMonthStr(endDate))) {
            months.add(localDateToMonthStr(endDate));
        }

        return months;
    }


    public static Pair<Date, Date> parseDateStr(Date start, Date end, String countByDayOrMonth) {
        try {
            if (start == null || end == null) {
                ZonedDateTime zone1 = LocalDateTime.now().minusDays(365).atZone(ZoneId.systemDefault());
                return Pair.of(Date.from(zone1.toInstant()), new Date());
            }
            if ("byMonth".equals(countByDayOrMonth)) {
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(start);
                startCalendar.set(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), 1, 0, 0, 0);

                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(end);
                endCalendar.set(endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.getMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);

                return Pair.of(startCalendar.getTime(), endCalendar.getTime());
            } else {
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(start);
                startCalendar.set(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(end);
                endCalendar.set(endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);

                return Pair.of(startCalendar.getTime(), endCalendar.getTime());
            }
        } catch (DateTimeParseException e) {
            throw new CustomException("时间参数格式错误");
        }
    }


    public static Pair<Date, Date> parseDateStr(String start, String end, String countByDayOrMonth) {
        try {
            if (StringUtils.isEmpty(start) || StringUtils.isEmpty(end)) {
                ZonedDateTime zone1 = LocalDateTime.now().minusDays(365).atZone(ZoneId.systemDefault());
                return Pair.of(Date.from(zone1.toInstant()), new Date());
            }

            DateTimeFormatter formatter;
            LocalDate startDate;
            LocalDate endDate;
            if ("byMonth".equals(countByDayOrMonth)) {
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");

                YearMonth yearMonth = YearMonth.parse(start, formatter);
                startDate = LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1);

                YearMonth yearMonth2 = YearMonth.parse(end, formatter);
                endDate = LocalDate.of(yearMonth2.getYear(), yearMonth2.getMonth(), yearMonth2.atEndOfMonth().getDayOfMonth());

                // 获取当前月份的最后一天
            } else {
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                startDate = LocalDate.parse(start, formatter);
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                endDate = LocalDate.parse(end, formatter);
            }
            // 获取当日的起始时间（凌晨零点）
            LocalDateTime startDateTime = startDate.atTime(0, 0, 0);
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            ZonedDateTime zone1 = startDateTime.atZone(ZoneId.systemDefault());
            ZonedDateTime zone2 = endDateTime.atZone(ZoneId.systemDefault());
            return Pair.of(Date.from(zone1.toInstant()), Date.from(zone2.toInstant()));
        } catch (DateTimeParseException e) {
            throw new CustomException("时间参数格式错误");
        }
    }

    /**
     * 计算两时间差值
     *
     * @param startTime  开始时间
     * @param finishTime 完成时间
     * @return 两时间差值
     */
    public static String caluLocalDateTimeDiff(LocalDateTime startTime, LocalDateTime finishTime) {
        if (Objects.nonNull(startTime) && Objects.nonNull(finishTime)) {
            // 计算耗时
            Duration duration = Duration.between(startTime, finishTime);
            long hours = duration.toHours();
            duration = duration.minusHours(hours);
            long minutes = duration.toMinutes();
            duration = duration.minusMinutes(minutes);
            long seconds = duration.getSeconds();
            StringBuilder sb = new StringBuilder();
            if (hours > 0) {
                sb.append(hours).append("h");
            }
            if (minutes > 0) {
                sb.append(minutes).append("m");
            }
            if (seconds > 0) {
                sb.append(seconds).append("s");
            }
            // 耗时【时分秒】
            return sb.toString();
        }
        return null;
    }

}
