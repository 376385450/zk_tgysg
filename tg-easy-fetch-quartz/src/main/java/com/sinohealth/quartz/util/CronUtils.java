package com.sinohealth.quartz.util;

import lombok.extern.log4j.Log4j2;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * cron表达式工具类
 */
@Log4j2
public class CronUtils {
    /**
     * 返回一个布尔值代表一个给定的Cron表达式的有效性
     *
     * @param cronExpression Cron表达式
     * @return boolean 表达式是否有效
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * 是否合法的任务，不支持高频率cron
     */
    public static boolean isValidTask(String cronExpression) {
        boolean valid = CronExpression.isValidExpression(cronExpression);
        String[] parts = cronExpression.split(" ");
        return valid && !parts[0].contains("*");
    }

    /**
     * 返回一个字符串值,表示该消息无效Cron表达式给出有效性
     *
     * @param cronExpression Cron表达式
     * @return String 无效时返回表达式错误描述,如果有效返回null
     */
    public static String getInvalidMessage(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return null;
        } catch (ParseException pe) {
            return pe.getMessage();
        }
    }

    /**
     * 返回下一个执行时间根据给定的Cron表达式
     *
     * @param cronExpression Cron表达式
     * @return Date 下次Cron表达式执行时间
     */
    public static Date getNextExecution(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getNextValidTimeAfter(new Date(System.currentTimeMillis()));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static List<Date> getSelfFireDateList(String cronExpression, int fireTimes) {
        Date startTime = new Date();
        try {
            CronExpression cron = new CronExpression(cronExpression);
            List<Date> dateList = new ArrayList<>();
            while (fireTimes > 0) {
                if (startTime == null) {
                    startTime = cron.getNextValidTimeAfter(new Date());
                } else {
                    startTime = cron.getNextValidTimeAfter(startTime);
                }
                dateList.add(startTime);
                fireTimes--;
            }
            return dateList;
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

    }
}
