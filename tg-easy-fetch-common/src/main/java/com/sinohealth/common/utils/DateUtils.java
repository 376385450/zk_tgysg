package com.sinohealth.common.utils;

import com.sinohealth.common.enums.TimeType;
import com.sinohealth.common.enums.dataassets.AssetsExpireEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 时间工具类
 */
@Slf4j
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {
    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    private static final DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDate() {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    public static String getDate(int day) {
        if (day == 0) {
            return new SimpleDateFormat(YYYY_MM_DD).format(new Date());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, day);
            return new SimpleDateFormat(YYYY_MM_DD).format(calendar.getTime());
        }
    }

    public static final String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static final String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static final String dateTimeNow(final String format) {
        return parseDateToStr(format, new Date());
    }

    public static final String dateTime(final Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static final String parseDateToStr(final String format, final Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(format).format(date);
    }

    public static final String parseDateToStr(final String format, final LocalDateTime date) {
        if (date == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return formatter.format(date);
    }

    public static final String getStartTime(final Date date, int addDay) {
        if (addDay != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, addDay);
            return new SimpleDateFormat("yyyy-MM-dd 23:59:59").format(calendar.getTime());
        }

        return new SimpleDateFormat("yyyy-MM-dd 23:59:59").format(date);

    }

    public static final String getEndTime(final Date date, int addDay) {
        if (addDay != 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE, addDay);
            return new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(calendar.getTime());
        }
        return new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(date);

    }

    public static final Date dateTime(final String format, final String ts) {
        try {
            return new SimpleDateFormat(format).parse(ts);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static String compactDate() {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate() {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate, TimeType... timeType) {
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        if (timeType.length >= 2) {
            StringBuffer str = new StringBuffer();
            List<TimeType> list = Arrays.stream(timeType).sorted(Comparator.comparing(TimeType::getId)).collect(Collectors.toList());
            for (TimeType item : list) {
                str.append(diff / item.getKd() + item.getName());
                diff = diff % item.getKd();
            }
            return str.toString();
        } else if (timeType.length == 1) {
            return diff / timeType[0].getKd() + timeType[0].getName();
        } else {
            return getDatePoor(endDate, nowDate);
        }
    }

    /**
     * 当天日期，不包含时分秒
     *
     * @return 当天日期，不包含时分秒
     * @author linkaiwei
     * @date 2021-12-02 17:29:01
     * @since 1.6.3
     */
    public static Date getTodayDate() {
        return parseDate(getDate());
    }

    /**
     * 计算两个时间之间间隔的天数
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 天数
     * @author linkaiwei
     * @date 2022-02-10 16:39:19
     * @since 1.6.4.0
     */
    public static int differentDays(Date startDate, Date endDate) {
        Calendar startCalender = Calendar.getInstance();
        startCalender.setTime(startDate);
        Calendar endCalender = Calendar.getInstance();
        endCalender.setTime(endDate);
        int startDay = startCalender.get(Calendar.DAY_OF_YEAR);
        int endDay = endCalender.get(Calendar.DAY_OF_YEAR);
        int startYear = startCalender.get(Calendar.YEAR);
        int endYear = endCalender.get(Calendar.YEAR);
        if (startYear != endYear) {
            int timeDistance = 0;
            for (int i = startYear; i < endYear; i++) {
                if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0) {
                    timeDistance += 366;

                } else {
                    timeDistance += 365;
                }
            }
            return timeDistance + (endDay - startDay);

        } else {
            return endDay - startDay;
        }
    }

    public static boolean hasDataExpired(Date dataExpire) {
        if (Objects.isNull(dataExpire)) {
            return false;
        }
        return DateUtils.truncatedCompareTo(DateUtils.getNowDate(), dataExpire, Calendar.SECOND) > 0;
    }

    public static boolean hasDataExpired(LocalDateTime dataExpire) {
        if (Objects.isNull(dataExpire)) {
            return false;
        }
        return dataExpire.isBefore(LocalDateTime.now());
    }

    /**
     * 判断是否过期，忽略时分秒
     *
     * @param dataExpire
     */
    public static boolean hasDataExpiredOnlyDays(Date dataExpire) {
        if (Objects.isNull(dataExpire)) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(YYYY_MM_DD);
        return sdf.format(new Date()).compareTo(sdf.format(dataExpire)) > 0;
    }

    /**
     * 超过过期时间3个月后标记为删除态
     */
    public static AssetsExpireEnum convertExpire(LocalDateTime dataExpire) {
        if (Objects.isNull(dataExpire)) {
            return AssetsExpireEnum.normal;
        }
        boolean delete = dataExpire.isBefore(LocalDateTime.now().minusMonths(3));
        if (delete) {
            return AssetsExpireEnum.delete;
        }

        if (dataExpire.isBefore(LocalDateTime.now())) {
            return AssetsExpireEnum.expire;
        }
        return AssetsExpireEnum.normal;
    }

    public static Date parseTimezoneDate(String timeStr) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try {
            return fmt.parse(timeStr);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    public static Date parseDateTime(String timeStr) {
        SimpleDateFormat fmt = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        try {
            return fmt.parse(timeStr);
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    public static LocalDateTime addDaysSkippingWeekends(LocalDateTime date, Integer days) {
        if (Objects.isNull(date) || Objects.isNull(days)) {
            return null;
        }
        LocalDateTime result = date;
        int addedDays = 0;
        while (addedDays < days) {
            result = result.plusDays(1);
            if (!(result.getDayOfWeek() == DayOfWeek.SATURDAY || result.getDayOfWeek() == DayOfWeek.SUNDAY)) {
                ++addedDays;
            }
        }
        return result;
    }

    /**
     * 获取日期里最后的时分秒
     *
     * @param date
     * @return
     */
    public static Date getEndOfDay(final Date date) {
        if (date == null) {
            return null;
        }
        String dateStr = new SimpleDateFormat("yyyy-MM-dd 23:59:59").format(date);
        try {
            return new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS).parse(dateStr);
        } catch (ParseException e) {
            log.error("日期转换出错：{}", dateStr, e);
            return null;
        }
    }

    public static String getDateYM() {
        return formatYM(LocalDate.now());
    }

    public static String formatYM(LocalDate date) {
        return date.format(YYYYMM);
    }
}
