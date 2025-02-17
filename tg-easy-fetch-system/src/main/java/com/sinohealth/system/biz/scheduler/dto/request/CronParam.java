package com.sinohealth.system.biz.scheduler.dto.request;

import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.StringUtils;
import lombok.Builder;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-11-06 19:40
 * @see org.apache.dolphinscheduler.api.dto.ScheduleParam 尚书台项目中对应的类
 * <p>
 * {"startTime":"2023-06-20 14:04:46","endTime":"2050-11-04 14:04:46","crontab":"0 0 * * * ? *","timezoneId":"Asia/Shanghai"}
 */
@Data
@Builder
public class CronParam {
    private String startTime;
    private String endTime;
    private String crontab;
    private String timezoneId;

    public static String newCron(String cron) {
        if (StringUtils.isBlank(cron)) {
            return "";
        }
        Date date = new Date();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        CronParam param = CronParam.builder()
                .startTime(fmt.format(date))
                .endTime(fmt.format(DateUtils.addYears(date, 20)))
                .crontab(cron)
                .timezoneId("Asia/Shanghai")
                .build();
        return JsonUtils.format(param);
    }
}
