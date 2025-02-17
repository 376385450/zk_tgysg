package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.TableLog;
import com.sinohealth.system.dto.*;

import java.util.Date;
import java.util.List;

/**
 * 变更记录Service接口
 *
 * @author jingjun
 * @date 2021-04-20
 */
public interface ITableLogService extends IService<TableLog> {

    public List<TableLog> getList(List<Long> dirIds,Long tableId,Integer type,Long userId, String changeType);

    public List<UserQueryTableLogDto> getQueryAndExportTableLog(Long dirId);

    public QueryTableCountDto queryTableMap(Long dirId, String tableName, Date startTime, Date endTime);

    public List<TableLog> getOneTableMap(Long tableId, String startDate,  String endDate);

    public List<TableLog> getMyConcernTableTop20(Long userId);

    public List<QueryTableHistoryDto> getMyQueryTableHistory();

    public MyViewDto getMyStatistics(Long userId);

    public List<TableLogMapDto> getLast7DayLogStatistic(Long userId);
}
