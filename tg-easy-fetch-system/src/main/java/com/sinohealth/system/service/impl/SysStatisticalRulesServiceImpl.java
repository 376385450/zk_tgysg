package com.sinohealth.system.service.impl;

import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.common.constant.ScheduleConstants;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.enums.StatisticsPeriodType;
import com.sinohealth.common.enums.StatisticsType;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.BeanUtil;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.task.CronTaskRegistrar;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.dto.*;
import com.sinohealth.system.service.*;
import com.sinohealth.system.vo.SysStatisticalResultVo;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.mapper.SysStatisticalRulesMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计规则Service业务层处理
 *
 * @author dataplatform
 * @date 2021-07-30
 */
@Service("sysStatisticalRulesService")
public class SysStatisticalRulesServiceImpl extends ServiceImpl<SysStatisticalRulesMapper, SysStatisticalRules> implements ISysStatisticalRulesService {


    @Autowired
    CronTaskRegistrar cronTaskRegistrar;

    @Autowired
    ISysStatisticalTableService sysStatisticalTableService;

    @Autowired
    ISysStatisticalResultService sysStatisticalResultService;

    @Autowired
    private ITableInfoService tableService;

    @Autowired
    private IGroupDataDirService groupDataDirService;

    @Override
    @Transactional
    public boolean addOrUpdate(SysStatisticalRulesDto bo) {
        SysStatisticalRules sysStatisticalRules = (SysStatisticalRules) BeanUtil.copyProperties(bo, SysStatisticalRules::new);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (sysStatisticalRules.getId() == null) {
            sysStatisticalRules.setCreateBy(loginUser.getUsername());
            sysStatisticalRules.setCreateUserId(loginUser.getUserId());
        } else {
            sysStatisticalRules.setUpdateBy(loginUser.getUsername());
            sysStatisticalRules.setUpdateUserId(loginUser.getUserId());

        }
        sysStatisticalRules.setStatus("0");
        //处理周期类型
        StatisticsPeriodType statisticsPeriodType = StatisticsPeriodType.findType(sysStatisticalRules.getStatisticsPeriodType());
        if (statisticsPeriodType == null) {
            throw new CustomException("statisticsPeriodType 类型错误 ！");
        }
        //处理统计类型
        StatisticsType statisticsType = StatisticsType.findType(sysStatisticalRules.getStatisticsType());
        if (statisticsType == null) {
            throw new CustomException("statisticsType 类型错误 ！");
        }

        //处理cron表达式
        sysStatisticalRules.setJobCron(statisticsPeriodType.getCron(sysStatisticalRules.getStatisticsTime()));
        sysStatisticalRules.setJobInvokeTarget(statisticsType.getMethod());

        sysStatisticalRules.setJobGroup(ScheduleConstants.TASK_CLASS_NAME_STATISTICS);
        boolean save = saveOrUpdate(sysStatisticalRules);
//        boolean save = false;
//        if (ObjectUtils.isEmpty(sysStatisticalRules.getId())) {
//
//            save = save(sysStatisticalRules);
//        } else {
//            updateById(sysStatisticalRules);
//            save = true;
//        }
//        if (!save) {
//            return true;
//        }

        //处理统计的表
        List<TableInfo> tableInfoList = tableService.listByIds(bo.getTableIds());
        LambdaQueryWrapper<SysStatisticalTable> queryWrapper = Wrappers.<SysStatisticalTable>lambdaQuery();
        queryWrapper.eq(SysStatisticalTable::getStatisticalId, sysStatisticalRules.getId());
        //原来有的中间表数据
        List<SysStatisticalTable> sysStatisticalTableList = sysStatisticalTableService.list(queryWrapper);
        if (!ObjectUtils.isEmpty(tableInfoList)) {
            //新的统计的表id
            List<Long> newTableId = tableInfoList.stream().map(TableInfo::getId).collect(Collectors.toList());
            //原来有的统计的表id
            List<Long> formerTableId = sysStatisticalTableList.stream().map(SysStatisticalTable::getTableId).collect(Collectors.toList());
            //添加统计的表
            newTableId.stream().filter(t -> !formerTableId.contains(t)).forEach(id -> {
                SysStatisticalTable data = new SysStatisticalTable();
                data.setStatisticalId(sysStatisticalRules.getId());
                data.setTableId(id);
                sysStatisticalTableService.save(data);
            });

            //删除统计的表
            List<Long> collect = sysStatisticalTableList.stream().filter(m -> !newTableId.contains(m.getTableId())).map(SysStatisticalTable::getId).collect(Collectors.toList());
            if (!collect.isEmpty()) {
                sysStatisticalTableService.removeByIds(collect);
            }
        } else if (!ObjectUtils.isEmpty(sysStatisticalTableList)) {
            sysStatisticalTableService.removeByIds(sysStatisticalTableList.stream().map(SysStatisticalTable::getId).collect(Collectors.toList()));
        }

        addCronTask(sysStatisticalRules);

        return true;
    }


    @Override
    public void addCronTask(SysStatisticalRules sysStatisticalRules) {
        StatisticsType statisticsType = StatisticsType.findType(sysStatisticalRules.getStatisticsType());
        String[] split = statisticsType.getMethod().split("\\.");
        //处理计时间任务
        cronTaskRegistrar.addCronTask(sysStatisticalRules.getJobGroup() + "_" + sysStatisticalRules.getId(),
                sysStatisticalRules.getJobCron(),
                split[0], split[1]
                , sysStatisticalRules.getId(), "1");
    }

    /**
     * 加载定时任务
     */
    @Override
    public void addCronTaskAll() {
        LambdaQueryWrapper<SysStatisticalRules> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysStatisticalRules::getStatus, "0");
        List<SysStatisticalRules> list = list(queryWrapper);
        list.forEach(sysStatisticalRules -> {
            addCronTask(sysStatisticalRules);
        });

    }

    @Override
    public boolean statusWithValidByIdsJurisdiction(Long asList, String status) {
        //判断权限只有创建人可以修改
        SysStatisticalRules byId = getById(asList);
        Long userId = SecurityUtils.getUserId();
        if (!userId.equals(byId.getCreateUserId())) {
            throw new CustomException("该统计只能由创建人 【" + byId.getCreateBy() + "】修改");
        }

        return statusWithValidByIds(asList, status);
    }

    @Override
    public boolean statusWithValidByIds(Long asList, String status) {

//        Collection<SysStatisticalRules> entityList = asList.stream().map(e -> {
//            SysStatisticalRules sysStatisticalRules = new SysStatisticalRules();
//            sysStatisticalRules.setId(e);
//            sysStatisticalRules.setStatus(status);
//            return sysStatisticalRules;
//        }).collect(Collectors.toList());
//
//        return updateBatchById(entityList);

        SysStatisticalRules sysStatisticalRules = new SysStatisticalRules();
        sysStatisticalRules.setId(asList);
        sysStatisticalRules.setStatus(status);
        return updateById(sysStatisticalRules);
    }


    /**
     * 统计定时任务处理方法
     *
     * @param id
     * @param statisticalType 执行类型（1自动，2手动）
     */
    @Override
    @Transactional
    public boolean statisticsTask(Long id, String statisticalType) {
        SysStatisticalRules sysStatisticalRules = getById(id);
        if (sysStatisticalRules == null) {
            return false;
        }
        SysStatisticalResult sysStatisticalResult = new SysStatisticalResult();
        sysStatisticalResult.setStatisticalId(sysStatisticalRules.getId());
        sysStatisticalResult.setJobName(sysStatisticalRules.getJobName());
        sysStatisticalResult.setJobGroup(sysStatisticalRules.getJobGroup());
        sysStatisticalResult.setInvokeTarget(sysStatisticalRules.getJobInvokeTarget());
        sysStatisticalResult.setStatisticalType(statisticalType);
        if ("2".equals(statisticalType)) {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            sysStatisticalResult.setCreateBy(loginUser.getUsername());
            sysStatisticalResult.setCreateUserId(loginUser.getUserId());
        } else {
            sysStatisticalResult.setCreateBy("自动");
        }
        sysStatisticalResult.setStatus("1");

        LambdaQueryWrapper<SysStatisticalTable> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysStatisticalTable::getStatisticalId, sysStatisticalRules.getId());
        //查找统计的表
        List<SysStatisticalTable> sysStatisticalTableList = sysStatisticalTableService.list(queryWrapper);
        if (ObjectUtils.isEmpty(sysStatisticalTableList)) {
            return true;
        }
        sysStatisticalTableList.forEach(sysStatisticalTable -> {
            //统计
            TableInfoDto tableBaseInfo = tableService.getTableBaseInfo(sysStatisticalTable.getTableId());
            sysStatisticalResult.setTableId(sysStatisticalTable.getTableId());
            sysStatisticalResult.setTableName(tableBaseInfo.getTableName());
            sysStatisticalResult.setTableAlias(tableBaseInfo.getTableAlias());
            sysStatisticalResult.setTableMake(tableBaseInfo.getStoreSize());
            sysStatisticalResult.setTotalRows(tableBaseInfo.getTotalRows());
//            sysStatisticalResult.setExceptionInfo("");
//            sysStatisticalResult.setJobMessage("");

            sysStatisticalResultService.save(sysStatisticalResult);
        });


        return true;
    }

    @Override
    public Map<String, String> getStatisticsPeriodType() {
        return Arrays.stream(StatisticsPeriodType.values()).collect(Collectors.toMap(StatisticsPeriodType::getType, StatisticsPeriodType::getDescribe));
    }

    @Override
    public Map<String, String> getStatisticsType() {
        return Arrays.stream(StatisticsType.values()).collect(Collectors.toMap(StatisticsType::getType, StatisticsType::getDescribe));
    }

    @Override
    public boolean addTable(SysStatisticalTableDto bo) {
        TableInfo tableInfo = tableService.getById(bo.getTableId());
        if (tableInfo == null) {
            throw new CustomException("查询不到数据库表");
        }

        LambdaQueryWrapper<SysStatisticalTable> queryWrapper = Wrappers.<SysStatisticalTable>lambdaQuery();
        queryWrapper.eq(SysStatisticalTable::getStatisticalId, bo.getStatisticalId());
        queryWrapper.eq(SysStatisticalTable::getTableId, bo.getTableId());

        SysStatisticalTable one = sysStatisticalTableService.getOne(queryWrapper);
        if (one != null) {
            throw new CustomException("该数据库表已经关联：：" + tableInfo.getTableName());
        }
        //添加统计的表
        SysStatisticalTable data = (SysStatisticalTable) BeanUtil.copyProperties(bo, SysStatisticalTable::new);
        return sysStatisticalTableService.save(data);

    }

    @Override
    public boolean deleteTable(SysStatisticalTableDto bo) {
        LambdaQueryWrapper<SysStatisticalTable> queryWrapper = Wrappers.<SysStatisticalTable>lambdaQuery();
        queryWrapper.eq(SysStatisticalTable::getStatisticalId, bo.getStatisticalId());
        queryWrapper.eq(SysStatisticalTable::getTableId, bo.getTableId());
        return sysStatisticalTableService.remove(queryWrapper);
    }

    @Override
    public List<TableInfoDto> getTableList(List<Long> ids) {
        LambdaQueryWrapper<TableInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(TableInfo::getId, ids);
        List<TableInfo> lists = tableService.list(queryWrapper);

        List<TableInfoDto> collect = lists.stream().map(tableInfo -> {
            TableInfoDto dto = (TableInfoDto) BeanUtil.copyProperties(tableInfo, TableInfoDto::new);

//            dto.setTableAlias(tableInfo.getTableAlias());
//            dto.setDirId(tableInfo.getDirId());
//            dto.setId(tableInfo.getId());
//            dto.setComment(tableInfo.getComment());

            List<GroupLeaderDto> leaderDtos = groupDataDirService.queryGroupLeader(tableInfo.getDirId());
            if (!ObjectUtils.isEmpty(leaderDtos)) {
                StringBuffer groupName = new StringBuffer();
                StringBuffer leaderName = new StringBuffer();
                leaderDtos.forEach(s -> {
                    groupName.append(s.getGroupName());
                    groupName.append("、");
                    leaderName.append(s.getUserName());
                    leaderName.append("、");
                });
                groupName.deleteCharAt(groupName.length() - 1);
                leaderName.deleteCharAt(leaderName.length() - 1);
                dto.setDeptName(groupName.toString());
                dto.setManagerName(leaderName.toString());
            }
            return dto;
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public List<StatisticalInfoDto> getStatisticalList() {

        List<StatisticalInfoDto> list = (List<StatisticalInfoDto>) BeanUtil.copyProperties(list(), StatisticalInfoDto::new);

        list.forEach(l -> {
            LambdaQueryWrapper<SysStatisticalTable> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(SysStatisticalTable::getStatisticalId, l.getId());
            queryWrapper.eq(SysStatisticalTable::getStatisticalId, l.getId());
            //查找统计的表
            List<SysStatisticalTable> sysStatisticalTableList = sysStatisticalTableService.list(queryWrapper);

            LambdaQueryWrapper<SysStatisticalResult> queryWrapperResult = Wrappers.lambdaQuery();
            queryWrapperResult.eq(SysStatisticalResult::getStatisticalId, l.getId());
//            queryWrapperResult.eq(SysStatisticalResult::getStatus, "1");
            //查找统计的历史
            PageHelper.startPage(1, 1, "create_time desc");
            List<SysStatisticalResult> list1 = sysStatisticalResultService.list(queryWrapperResult);
            if (!list1.isEmpty()) {
                l.setFinallyStatisticalData(list1.get(0).getCreateTime());
                l.setStatisticalStatus(list1.get(0).getStatus());

            }
            l.setStatisticalTableCount(sysStatisticalTableList.size());
            l.setTableIds(sysStatisticalTableList.stream().map(SysStatisticalTable::getTableId).collect(Collectors.toList()));
        });

        return list;
    }

    @Override
    public StatisticalInfoDto getStatistical(Long id) {
        StatisticalInfoDto l = (StatisticalInfoDto) BeanUtil.copyProperties(getById(id), StatisticalInfoDto::new);
        LambdaQueryWrapper<SysStatisticalTable> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysStatisticalTable::getStatisticalId, l.getId());
        //查找统计的表
        List<SysStatisticalTable> sysStatisticalTableList = sysStatisticalTableService.list(queryWrapper);

        LambdaQueryWrapper<SysStatisticalResult> queryWrapperResult = Wrappers.<SysStatisticalResult>lambdaQuery();
        queryWrapperResult.eq(SysStatisticalResult::getStatisticalId, l.getId())
                .orderByDesc(SysStatisticalResult::getCreateTime).last("limit 1");
//            queryWrapperResult.eq(SysStatisticalResult::getStatus, "1");
        //查找统计的历史
//        PageHelper.startPage(1, 1, "create_time DESC");
        List<SysStatisticalResult> list1 = sysStatisticalResultService.list(queryWrapperResult);
        if (!list1.isEmpty()) {
            l.setFinallyStatisticalData(list1.get(0).getCreateTime());
            l.setStatisticalStatus(list1.get(0).getStatus());

        }
        l.setStatisticalTableCount(sysStatisticalTableList.size());
        l.setTableIds(sysStatisticalTableList.stream().map(SysStatisticalTable::getTableId).collect(Collectors.toList()));

        return l;
    }

    @Override
    public List<SysStatisticalResultVo> queryList(Long id) {
        SysStatisticalRules byId = getById(id);
        LambdaQueryWrapper<SysStatisticalTable> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(SysStatisticalTable::getStatisticalId, byId.getId());
        //查找统计的表
        List<SysStatisticalTable> sysStatisticalTableList = sysStatisticalTableService.list(queryWrapper);

        LambdaQueryWrapper<SysStatisticalResult> queryWrapperResult = Wrappers.<SysStatisticalResult>lambdaQuery();
        queryWrapperResult.eq(SysStatisticalResult::getStatisticalId, byId.getId())
                .in(SysStatisticalResult::getTableId, sysStatisticalTableList.stream().map(SysStatisticalTable::getTableId).collect(Collectors.toList()))
                .orderByDesc(SysStatisticalResult::getCreateTime).last("limit " + sysStatisticalTableList.size() * 2);
        //查找统计的历史
//        PageHelper.startPage(1, 1, "create_time DESC");
        List<SysStatisticalResult> sysStatisticalResultList = sysStatisticalResultService.list(queryWrapperResult);
        List<SysStatisticalResultVo> collect = sysStatisticalTableList.stream().map(m -> {
            SysStatisticalResultVo sysStatisticalResultVo = new SysStatisticalResultVo();

            TableInfo tableInfo = tableService.getById(m.getTableId());

            sysStatisticalResultVo.setTableAlias(tableInfo.getTableAlias());
            sysStatisticalResultVo.setTableName(tableInfo.getTableName());
            List<SysStatisticalResult> sysStatisticalResult = sysStatisticalResultList.stream().filter(f -> f.getTableId().equals(m.getTableId())).collect(Collectors.toList());
            if
            (sysStatisticalResult.size() > 0) {
                sysStatisticalResultVo.setTotalRowsFormer(0L);
                sysStatisticalResultVo.setTableMakeFormer(0L);
                sysStatisticalResultVo.setNewLime(sysStatisticalResult.get(0).getCreateTime());
                sysStatisticalResultVo.setTotalRowsNew(sysStatisticalResult.get(0).getTotalRows());
                sysStatisticalResultVo.setTableMakeNew(sysStatisticalResult.get(0).getTableMake());
                if (sysStatisticalResult.size() == 2) {
                    sysStatisticalResultVo.setFormerLime(sysStatisticalResult.get(1).getCreateTime());
                    sysStatisticalResultVo.setTotalRowsFormer(sysStatisticalResult.get(1).getTotalRows());
                    sysStatisticalResultVo.setTableMakeFormer(sysStatisticalResult.get(1).getTableMake());

                }
                sysStatisticalResultVo.setTableMakeVariation(sysStatisticalResultVo.getTableMakeNew() - sysStatisticalResultVo.getTableMakeFormer());
                sysStatisticalResultVo.setTotalRowsVariation(sysStatisticalResultVo.getTotalRowsNew() - sysStatisticalResultVo.getTotalRowsFormer());

            }



            return sysStatisticalResultVo;
        }).collect(Collectors.toList());
        return collect;
    }


}
