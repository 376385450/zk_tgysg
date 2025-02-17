package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.LogType;
import com.sinohealth.common.enums.LogTypeGather;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.domain.SysUserRole;
import com.sinohealth.system.domain.TableLog;
import com.sinohealth.system.dto.*;
import com.sinohealth.system.mapper.SysUserRoleMapper;
import com.sinohealth.system.mapper.SysUserTableMapper;
import com.sinohealth.system.mapper.TableLogMapper;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.ITableLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 变更记录Service业务层处理
 *
 * @author jingjun
 * @date 2021-04-20
 */
@Service
public class TableLogServiceImpl extends ServiceImpl<TableLogMapper, TableLog> implements ITableLogService {

    @Autowired
    private SysUserRoleMapper userRoleMapper;
    @Autowired
    private SysUserTableMapper userTableMapper;
    @Autowired
    private ITableInfoService tableInfoService;
    @Autowired
    private ISysUserService sysUserService;

    @Override
    public List<TableLog> getList(List<Long> dirIds, Long tableId, Integer type, Long userId, String changeType) {
        QueryWrapper<TableLog> query = Wrappers.<TableLog>query();
        if (!ObjectUtils.isEmpty(dirIds)) {
            if (dirIds.size() == 1) {
                query.eq("dir_id", dirIds.get(0));
            } else {
                query.in("dir_id", dirIds);
            }

        }
        if (tableId != null) {
            query.eq("table_id", tableId);
        }

        if (!StringUtils.isEmpty(changeType)) {
            switch (changeType) {
                case "metadata":
                    query.lt("log_type", LogType.data_create.getVal());
                    break;
                case "data":
                    query.gt("log_type", LogType.table_copy.getVal());
                    query.lt("log_type", LogType.data_query.getVal());
                    break;
                default:

                    break;
            }

        } else if (type != null) {
            //增加 （元数据变更、关联变更、数据变更、表单变更）判断
            List<Integer> logTypeList = LogTypeGather.findLogTypeList(type);
            if (logTypeList != null) {
                query.in("log_type", logTypeList);
            } else {
                if ("".equals(LogType.findName(type))) {
                    //TODO 这里是否要验证类型不对不查询
                }
                query.eq("log_type", type);
            }

        } else {
            // 全部查询默认过滤（数据变更-查询）类型
            query.ne("log_type", LogType.data_query.getVal());
        }

        if (userId != null) {
            query.eq("operator_id", userId);
        }
        List<TableLog> list = this.list(query);
        list.forEach(tableLog -> {
            //处理更新人
            if(StringUtils.isNotEmpty(tableLog.getOperator())){
                SysUser user = sysUserService.selectUserByUserName(tableLog.getOperator());
                if(user !=null && StringUtils.isNotEmpty(user.getOrgUserId())){
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                    if(sinoPassUserDTO != null){
                        tableLog.setOperatorOri(sinoPassUserDTO.getViewName());
                    }
                }
            }
        });
        return list;
    }

    @Override
    public List<UserQueryTableLogDto> getQueryAndExportTableLog(Long dirId) {
        PageHelper.startPage(1, 20, " id desc ");
        List<UserQueryTableLogDto> list = this.baseMapper.getQueryAndExportTableLog(dirId);

        if (!ObjectUtils.isEmpty(list)) {
            List<SysUserRole> roles = userRoleMapper.getUserRoleName(list.stream().map(l -> l.getUserId()).distinct().collect(Collectors.toList()));
            StringBuffer name = new StringBuffer();
            list.forEach(u -> {
                roles.stream().filter(r -> r.getUserId().equals(u.getUserId())).forEach(r -> {
                    name.append(r.getRoleName());
                    name.append(",");
                });
                name.deleteCharAt(name.length() - 1);
                u.setRoleName(name.toString());
                name.setLength(0);
            });
        }
        return list;
    }

    public QueryTableCountDto queryTableMap(Long dirId, String tableName, Date startTime, Date endTime) {
        QueryTableCountDto data = new QueryTableCountDto();
        if (startTime == null || endTime == null) {
            Calendar calendar = Calendar.getInstance();
            endTime = calendar.getTime();
            calendar.add(Calendar.DATE, -30);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            startTime = calendar.getTime();
        }

        List<QueryTableCountDto.QueryTableCount> list = new ArrayList<>();
        List<LogCountDto> countDtos = this.baseMapper.groupByQueryAndExport(dirId, tableName, DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, startTime), DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, endTime));

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        end.add(Calendar.DATE, 1);
        end.set(Calendar.HOUR, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        while (end.compareTo(calendar) > 0) {
            String date = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, calendar.getTime());
            QueryTableCountDto.QueryTableCount dto = new QueryTableCountDto.QueryTableCount();

            Map<String, Integer> dateList = Arrays.stream(LogType.values()).collect(Collectors.toMap(l -> l.getVal() + "", l -> 0));

            countDtos.stream().filter(c -> c.getDate().equals(date)).forEach(c -> {
//                if (c.getLogType() == LogType.data_query.getVal()) {
//                    dto.setQueryTimes(c.getTimes());
//                } else {
//                    dto.setExportTimes(c.getTimes());
//                }
                LogType logType = LogType.findLogType(c.getLogType());

                if (logType != null) {
                    dateList.put(logType.getVal() + "", c.getTimes());

                }


            });
            dto.setDate(date);
            dto.setDataList(dateList);
            list.add(dto);
            calendar.add(Calendar.DATE, 1);
        }

        Map<String, String> dateList = Arrays.stream(LogType.values()).collect(Collectors.toMap(l -> l.getVal() + "", l -> l.getName()));
        data.setTypeData(dateList);
        data.setList(list);
        return data;

    }

    public List<TableLog> getOneTableMap(Long tableId, String startDate, String endDate) {

        List<TableLog> list = this.baseMapper.getOneTableMap(tableInfoService.getById(tableId).getDirId(), tableId, startDate, endDate);

        if (!ObjectUtils.isEmpty(list)) {
            List<TableLog> dataCountList = this.baseMapper.selectBatchIds(list.stream().map(t -> t.getId()).collect(Collectors.toList()));
            list.forEach(t -> {
                dataCountList.stream().filter(d -> d.getId().equals(t.getId())).findFirst().ifPresent(d -> {
                    t.setDataCount(d.getDataCount());
                });
            });
            list.sort(Comparator.comparing(TableLog::getId));
        }

        return list;
    }

    public List<TableLog> getMyConcernTableTop20(Long userId) {
        return this.baseMapper.getMyConcernTableTop20(userId);
    }

    public List<QueryTableHistoryDto> getMyQueryTableHistory() {
        List<Long> ids = this.baseMapper.getLogIdMyQueryTop20(SecurityUtils.getUserId());
        if (ObjectUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        List<QueryTableHistoryDto> list = this.baseMapper.getQueryTableHistory(ids);
        list.sort((a, b) -> b.getId().compareTo(a.getId()));
        return list;
    }

    public MyViewDto getMyStatistics(Long userId) {
        MyViewDto dto = new MyViewDto();
        Date now = new Date();
        dto.setTableSize(userTableMapper.getCountTableByUserId(userId));
        Long totalUpate = this.baseMapper.getMyTableTotalUpate(userId, null, null);
        dto.setTotalUpdateRecord(totalUpate != null ? totalUpate : 0L);
        Long upate = this.baseMapper.getMyTableTotalUpate(userId, DateUtils.getStartTime(now, -2), DateUtils.getEndTime(now, 0));
        dto.setUpdateRecord(upate != null ? upate : 0L);
        return dto;

    }

    public List<TableLogMapDto> getLast7DayLogStatistic(Long userId) {
        Date now = new Date();
        List<LogCountDto> logCountDtoList = this.baseMapper.getLogStatisticGroupByDay(userId, DateUtils.getStartTime(now, -8));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DATE, -7);

        List<TableLogMapDto> logMapDtos = new ArrayList<>(7);

        while (now.compareTo(calendar.getTime()) > 0) {
            String date = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, calendar.getTime());
            TableLogMapDto dto = new TableLogMapDto();
            dto.setDate(date);
            int metadataSize = 0;
            int relationSize = 0;
            int tableSize = 0;
            int dataSize = 0;
            for (LogCountDto count : logCountDtoList) {
                if (count.getDate().equals(date)) {
                    if (count.getLogType().intValue() < LogType.relation_create.getVal()) {
                        metadataSize += count.getTimes();
                    } else if (count.getLogType().intValue() < LogType.table_create.getVal()) {
                        relationSize += count.getTimes();
                    } else if (count.getLogType().intValue() < LogType.data_create.getVal()) {
                        tableSize += count.getTimes();
                    } else {
                        dataSize += count.getTimes();
                    }
                }
            }

            dto.getList().add(new TableLogMapDto.CountType(0, metadataSize));
            dto.getList().add(new TableLogMapDto.CountType(1, relationSize));
            dto.getList().add(new TableLogMapDto.CountType(2, tableSize));
            dto.getList().add(new TableLogMapDto.CountType(3, dataSize));

            logMapDtos.add(dto);
            calendar.add(Calendar.DATE, 1);
        }

        return logMapDtos;
    }
}
