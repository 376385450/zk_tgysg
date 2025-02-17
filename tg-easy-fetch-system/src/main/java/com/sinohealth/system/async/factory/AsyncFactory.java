package com.sinohealth.system.async.factory;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sinohealth.common.config.DataSourceFactory;
import com.sinohealth.common.constant.Constants;
import com.sinohealth.common.enums.LogType;
import com.sinohealth.common.enums.SpeedOfProgressType;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.LogUtils;
import com.sinohealth.common.utils.ServletUtils;
import com.sinohealth.common.utils.ip.AddressUtils;
import com.sinohealth.common.utils.ip.IpUtils;
import com.sinohealth.common.utils.spring.SpringUtils;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.service.*;
import eu.bitwalker.useragentutils.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.TimerTask;

/**
 * 异步工厂（产生任务用）
 */
public class AsyncFactory {
    private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status   状态
     * @param message  消息
     * @param args     列表
     * @return 任务task
     */
    public static TimerTask recordLogininfor(final String username, final String status, final String message, final Date time,
                                             final Object... args) {
        final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        final String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
        return new TimerTask() {
            @Override
            public void run() {
                String address = AddressUtils.getRealAddressByIP(ip);
                StringBuilder s = new StringBuilder();
                s.append(LogUtils.getBlock(ip));
                s.append(address);
                s.append(LogUtils.getBlock(username));
                s.append(LogUtils.getBlock(status));
                s.append(LogUtils.getBlock(message));
                // 打印信息到日志
                sys_user_logger.info(s.toString(), args);
                // 获取客户端操作系统
                String os = userAgent.getOperatingSystem().getName();
                // 获取客户端浏览器
                String browser = userAgent.getBrowser().getName();
                // 封装对象
                SysLogininfor logininfor = new SysLogininfor();
                logininfor.setUserName(username);
                logininfor.setIpaddr(ip);
                logininfor.setLoginLocation(address);
                logininfor.setBrowser(browser);
                logininfor.setOs(os);
                logininfor.setLoginTime(time);
                logininfor.setMsg(message);
                // 日志状态
                if (Constants.LOGIN_SUCCESS.equals(status) || Constants.LOGOUT.equals(status)) {
                    logininfor.setStatus(Constants.SUCCESS);
                } else if (Constants.LOGIN_FAIL.equals(status)) {
                    logininfor.setStatus(Constants.FAIL);
                }
                // 插入数据
                SpringUtils.getBean(ISysLogininforService.class).insertLogininfor(logininfor);
            }
        };
    }

    /**
     * 操作日志记录
     *
     * @param operLog 操作日志信息
     * @return 任务task
     */
    public static TimerTask recordOper(final SysOperLog operLog) {
        return new TimerTask() {
            @Override
            public void run() {
                // 远程查询操作地点
                operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
                SpringUtils.getBean(ISysOperLogService.class).insertOperlog(operLog);
            }
        };
    }

    public static TimerTask queryTableTimes(TableInfo tableInfo, Long loginUserId, String loginName) {
        return new TimerTask() {
            @Override
            public void run() {
                TableLog tableLog = new TableLog();
                tableLog.setTableId(tableInfo.getId());
                tableLog.setDirId(tableInfo.getDirId());
                tableLog.setTableAlias(tableInfo.getTableAlias());
                tableLog.setTableName(tableInfo.getTableName());
                tableLog.setLogType(LogType.data_query.getVal());
                tableLog.setCreateTime(new Date());
                tableLog.setUpdateCount(1);
                tableLog.setDataCount(0);
                tableLog.setOperatorId(loginUserId);
                tableLog.setOperator(loginName);
                tableLog.setContent("查询数据");
                SpringUtils.getBean(ITableLogService.class).save(tableLog);

                SpringUtils.getBean(ITableInfoService.class).updateQueryTime(tableInfo.getId(), 1, 0);
            }
        };
    }

    public static TimerTask createTableLog(TableInfo tableInfo, Long loginUserId, String loginName, LogType logType, String content, int updateCount, boolean countTotal, Date now) {

        return createTableLog(tableInfo, loginUserId, loginName, logType, content, updateCount, countTotal, now, null,null);
    }
    public static TimerTask createTableLog(TableInfo tableInfo, Long loginUserId, String loginName, LogType logType, String content, int updateCount, boolean countTotal, Date now, String comment) {

        return createTableLog(tableInfo, loginUserId, loginName, logType, content, updateCount, countTotal, now, comment,null);
    }
    public static TimerTask createTableLog(TableInfo tableInfo, Long loginUserId, String loginName, LogType logType, String content, int updateCount, boolean countTotal, Date now, String comment, String preContent) {
        return new TimerTask() {
            @Override
            public void run() {
                TableLog tableLog = new TableLog();
                tableLog.setTableId(tableInfo.getId());
                tableLog.setDirId(tableInfo.getDirId());
                tableLog.setTableAlias(tableInfo.getTableAlias());
                tableLog.setTableName(tableInfo.getTableName());
                tableLog.setLogType(logType.getVal());
                tableLog.setCreateTime(now);
                tableLog.setUpdateCount(updateCount);
                tableLog.setDataCount(countTotal ? DataSourceFactory.getDataConnection(tableInfo.getDirId()).getJdbcOperations().queryForObject("select count(*) from " + tableInfo.getTableName(), Integer.class) : 0);
                tableLog.setOperatorId(loginUserId);
                tableLog.setOperator(loginName);
                tableLog.setContent(content);
                tableLog.setPreContent(preContent);
                tableLog.setComment(comment);
                SpringUtils.getBean(ITableLogService.class).save(tableLog);

            }
        };
    }

    public static TimerTask updateTableRow(TableInfo tableInfo, String key) {
        return new TimerTask() {
            @Override
            public void run() {
                SpringUtils.getBean(ITableInfoService.class).refreshTableCount(tableInfo.getDirId(), tableInfo.getId(), tableInfo.getTableName(), key);
            }
        };
    }

    public static TimerTask copyTableTask(Long dirId, String sql, TableTask tableTask) {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    SpringUtils.getBean(ITableTaskService.class).save(tableTask);
                    DataSourceFactory.getDataConnection(dirId).getJdbcOperations().execute(sql);
                    LambdaUpdateWrapper<TableTask> luw = new LambdaUpdateWrapper<>();
                    luw.eq(TableTask::getId,tableTask.getId());
                    luw.set(TableTask::getRemarks,"复制成功");
                    luw.set(TableTask::getCompleteTime, DateUtils.getNowDate());
                    luw.set(TableTask::getSpeedOfProgress, SpeedOfProgressType.SUCCESS.getId());
                    SpringUtils.getBean(ITableTaskService.class).update(luw);
                }catch (Exception e){
                    LambdaUpdateWrapper<TableTask> luw = new LambdaUpdateWrapper<>();
                    luw.eq(TableTask::getId,tableTask.getId());
                    luw.set(TableTask::getRemarks,"复制失败:" + e.getMessage());
                    luw.set(TableTask::getCompleteTime,DateUtils.getNowDate());
                    luw.set(TableTask::getSpeedOfProgress,SpeedOfProgressType.ERROR.getId());
                    SpringUtils.getBean(ITableTaskService.class).update(luw);
                }
            }
        };
    }

}
