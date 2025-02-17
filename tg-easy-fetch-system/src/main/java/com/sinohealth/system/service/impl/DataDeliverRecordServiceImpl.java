package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.project.constants.ProjectRelateEnum;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.biz.project.domain.ProjectDataAssetsRelate;
import com.sinohealth.system.biz.project.mapper.ProjectMapper;
import com.sinohealth.system.converter.DataDeliverCustomerEventConverter;
import com.sinohealth.system.dao.ApplicationDataUpdateRecordDAO;
import com.sinohealth.system.dao.TgDeliverCustomerRecordDAO;
import com.sinohealth.system.dao.TgDeliverDownloadRecordDAO;
import com.sinohealth.system.dao.TgDeliverEmailRecordDAO;
import com.sinohealth.system.domain.ApplicationDataUpdateRecord;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.domain.TgDeliverDownloadRecord;
import com.sinohealth.system.domain.TgDeliverEmailRecord;
import com.sinohealth.system.domain.value.deliver.DeliverRequestContextHolder;
import com.sinohealth.system.dto.CustomerApplyAuthReqV2DTO;
import com.sinohealth.system.dto.CustomerApplyAuthRequestContext;
import com.sinohealth.system.dto.TgDeliverCustomerRecordDTO;
import com.sinohealth.system.dto.application.deliver.event.*;
import com.sinohealth.system.dto.application.deliver.request.ApplicationDeliverEmailRequest;
import com.sinohealth.system.mapper.ProjectDataAssetsRelateMapper;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.monitor.mapper.MonitorDataMapper;
import com.sinohealth.system.service.*;
import com.sinohealth.system.util.IconHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 14:04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataDeliverRecordServiceImpl implements DataDeliverRecordService {

    private final TgDeliverEmailRecordDAO deliverEmailRecordDAO;

    private final TgDeliverDownloadRecordDAO deliverDownloadRecordDAO;

    private final TgDeliverCustomerRecordDAO tgDeliverCustomerRecordDAO;

    private final ApplicationDataUpdateRecordDAO applicationDataUpdateRecordDAO;

    private final TgApplicationInfoMapper applicationInfoMapper;

    private final ISysUserService sysUserService;

    private final ArkbiAnalysisService arkbiAnalysisService;

    private final DataDeliverCustomerRecordService deliverCustomerRecordService;

    private final DataDeliverDownloadRecordService deliverDownloadRecordService;

    private final DataDeliverEmailRecordService deliverEmailRecordService;

    private final ISysUserService iSysUserService;

    private final MonitorDataMapper monitorDataMapper;
    private final UserDataAssetsDAO userDataAssetsDAO;

    private final ProjectMapper projectMapper;
    private final ProjectDataAssetsRelateMapper projectDataAssetsRelateMapper;

    /**
     * 只关注表单的数据，不需要关心bi的数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDownloadRecords(DeliverRequestContextHolder requestContextHolder) {
        deliverDownloadRecordService.saveDownloadRecords(requestContextHolder);
    }

    @Override
    public void saveSendEmailRecords(ApplicationDeliverEmailRequest emailRequest, DeliverRequestContextHolder requestContextHolder) {
        deliverEmailRecordService.saveSendEmailRecords(emailRequest, requestContextHolder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveApplyCustomerRecords(CustomerApplyAuthReqV2DTO authReqDTO, CustomerApplyAuthRequestContext authRequestContext) {
        deliverCustomerRecordService.saveApplyCustomerRecords(authReqDTO, authRequestContext);
    }

    @Override
    public PageInfo<DataDeliverExportEventVO> listDownloadRecords(DataDeliverExportEventRequest request) {
        // 有分页参数为空，则返回所有数据
        if (request.getPage() == null || request.getSize() == null) {
            request.setPage(1);
            request.setSize(Integer.MAX_VALUE);
        }
        PageHelper.startPage(request.getPage(), request.getSize());
        Wrapper<TgDeliverDownloadRecord> wrapper = Wrappers.<TgDeliverDownloadRecord>lambdaQuery()
                .eq(request.getAssetsId() != null, TgDeliverDownloadRecord::getAssetsId, request.getAssetsId())
                .like(StringUtils.isNoneBlank(request.getSearchKey()), TgDeliverDownloadRecord::getProjectName, request.getSearchKey())
                .eq(Objects.nonNull(request.getAllocateType()), TgDeliverDownloadRecord::getAllocateType, request.getAllocateType())
                .eq(Objects.nonNull(request.getTableId()), TgDeliverDownloadRecord::getTableId, request.getTableId())
                .isNull(TgDeliverDownloadRecord::getParentRecordId)
                .orderByDesc(TgDeliverDownloadRecord::getDownloadTime);
        List<TgDeliverDownloadRecord> list = deliverDownloadRecordDAO.list(wrapper);
        int count = deliverDownloadRecordDAO.count(wrapper);

        List<Long> assetsIds = list.stream().map(TgDeliverDownloadRecord::getAssetsId).distinct().collect(Collectors.toList());
        Map<Long, String> projectMap;
        Map<Long, String> proNameMap;
        Map<Long, Long> assetsProMap;
        if (CollectionUtils.isNotEmpty(assetsIds)) {
            List<UserDataAssets> infos = userDataAssetsDAO.getBaseMapper().selectBatchIds(assetsIds);

            List<ProjectDataAssetsRelate> relateList = projectDataAssetsRelateMapper.selectList(new QueryWrapper<ProjectDataAssetsRelate>().lambda()
                    .in(ProjectDataAssetsRelate::getUserAssetId, assetsIds)
                    .eq(ProjectDataAssetsRelate::getProType, ProjectRelateEnum.master.name())
            );
            assetsProMap = Lambda.buildMap(relateList, ProjectDataAssetsRelate::getUserAssetId,
                    ProjectDataAssetsRelate::getProjectId);
            List<Long> proIds = Lambda.buildNonNullList(relateList, ProjectDataAssetsRelate::getProjectId);
            List<Project> proList = Lambda.queryListIfExist(proIds, v -> projectMapper.selectList(new QueryWrapper<Project>().lambda().in(Project::getId, v)));
            proNameMap = Lambda.buildMap(proList, Project::getId, Project::getName);

            projectMap = infos.stream().collect(Collectors.toMap(UserDataAssets::getId,
                    UserDataAssets::getProjectName, (front, current) -> current));
        } else {
            projectMap = Collections.emptyMap();
            proNameMap = Collections.emptyMap();
            assetsProMap = Collections.emptyMap();
        }


        List<DataDeliverExportEventVO> collect = list.stream().map(it -> {
            DataDeliverExportEventVO eventVO = new DataDeliverExportEventVO();
            eventVO.setId(Long.valueOf(it.getId()));
            eventVO.setTableId(it.getTableId());
            eventVO.setTableName(it.getTableName());
            eventVO.setAllocateType(Objects.equals(it.getAllocateType(), 1) ? "打包" : "单份");
            eventVO.setProjectName(projectMap.get(it.getAssetsId()));
            eventVO.setNewProjectName(Optional.ofNullable(assetsProMap.get(it.getAssetsId())).map(proNameMap::get).orElse(""));
            eventVO.setDownloadTime(DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", it.getDownloadTime()));
            eventVO.setExportType(it.getDownloadType());
            SysUser user = iSysUserService.getById(it.getCreateBy());
            eventVO.setOperator(Optional.ofNullable(user).map(SysUser::getUserName).orElse(""));
            eventVO.setOperatorId(it.getCreateBy());
            return eventVO;
        }).collect(Collectors.toList());
        PageInfo<DataDeliverExportEventVO> pageInfo = new PageInfo<>();
        pageInfo.setList(collect);
        pageInfo.setTotal(count);
        return pageInfo;
    }

    @Override
    public PageInfo<DataDeliverEmailEventVO> listSendEmailRecords(DataDeliverEmailEventRequest request) {
        // 有分页参数为空，则返回所有数据
        if (request.getPage() == null || request.getSize() == null) {
            request.setPage(1);
            request.setSize(Integer.MAX_VALUE);
        }
        PageHelper.startPage(request.getPage(), request.getSize());
        Wrapper<TgDeliverEmailRecord> wrapper = Wrappers.<TgDeliverEmailRecord>lambdaQuery()
                .eq(Objects.nonNull(request.getAssetsId()), TgDeliverEmailRecord::getAssetsId, request.getAssetsId())
                .eq(Objects.nonNull(request.getAllocateType()), TgDeliverEmailRecord::getAllocateType, request.getAllocateType())
                .eq(Objects.nonNull(request.getTableId()), TgDeliverEmailRecord::getTableId, request.getTableId())
                .and(StringUtils.isNoneBlank(request.getSearchKey()),
                        wp -> wp.like(TgDeliverEmailRecord::getProjectName, request.getSearchKey())
                                .or().like(TgDeliverEmailRecord::getReceiver, request.getSearchKey()))
                .isNull(TgDeliverEmailRecord::getParentRecordId)
                .orderByDesc(TgDeliverEmailRecord::getCreateTime);
        List<TgDeliverEmailRecord> list = deliverEmailRecordDAO.list(wrapper);

        List<Long> assetsIds = list.stream().map(TgDeliverEmailRecord::getAssetsId).distinct().collect(Collectors.toList());
        Map<Long, String> projectMap;
        if (CollectionUtils.isNotEmpty(assetsIds)) {
            List<UserDataAssets> infos = userDataAssetsDAO.getBaseMapper().selectBatchIds(assetsIds);
            projectMap = infos.stream().collect(Collectors.toMap(UserDataAssets::getId,
                    UserDataAssets::getProjectName, (front, current) -> current));
        } else {
            projectMap = Collections.emptyMap();
        }

        List<DataDeliverEmailEventVO> collect = list.stream().map(it -> {
            DataDeliverEmailEventVO eventDTO = new DataDeliverEmailEventVO();
            eventDTO.setId(Long.valueOf(it.getId()));
            eventDTO.setAllocateType(Objects.equals(it.getAllocateType(), 1) ? "打包" : "单份");
            eventDTO.setProjectName(projectMap.get(it.getAssetsId()));
            eventDTO.setOutName(it.getPackName());
            eventDTO.setTableId(it.getTableId());
            eventDTO.setTableName(it.getTableName());
            eventDTO.setReceiverEmails(it.getReceiver());
            eventDTO.setSendTime(DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", it.getSendTime()));
            SysUser user = iSysUserService.getById(it.getCreateBy());
            eventDTO.setOperator(Optional.ofNullable(user).map(SysUser::getUserName).orElse(""));
            eventDTO.setOperatorId(it.getCreateBy());
            return eventDTO;
        }).collect(Collectors.toList());
        PageInfo pageInfo = new PageInfo<>(list);
        pageInfo.setList(collect);
        return pageInfo;
    }

    @Override
    public PageInfo<DataDeliverCustomerEventVO> listApplyCustomerRecords(DataDeliverCustomerEventRequest request) {
        this.normalizePageParam(request);
        // 先查询出父节点，再查询出子节点，组装
        List<TgDeliverCustomerRecordDTO> records = tgDeliverCustomerRecordDAO.queryParentList(request);
        if (CollectionUtils.isEmpty(records)) {
            return new PageInfo<>();
        }

        // 转换DTO
        List<DataDeliverCustomerEventVO> result = new ArrayList<>();
        for (TgDeliverCustomerRecordDTO record : records) {
            DataDeliverCustomerEventVO convert = DataDeliverCustomerEventConverter.convert(record);
            result.add(convert);
        }
        PageHelper.clearPage();
        fillDataUpdateInfo(result);
        fillArkbiInfo(result);
        buildTree(result);
        PageInfo pageInfo = new PageInfo<>(records);
        pageInfo.setList(result);
        return pageInfo;
    }

    @Override
    public PageInfo<DataDeliverCustomerEventVO> listApplyCustomerRecordsForManager(DataDeliverCustomerEventRequest request) {
        this.normalizePageParam(request);
        // 先查询出父节点，再查询出子节点，组装
        List<TgDeliverCustomerRecordDTO> records = tgDeliverCustomerRecordDAO.queryParentList(request);
        if (CollectionUtils.isEmpty(records)) {
            return new PageInfo<>();
        }
        // 转换DTO
        List<DataDeliverCustomerEventVO> result = new ArrayList<>();
        for (TgDeliverCustomerRecordDTO record : records) {
            DataDeliverCustomerEventVO convert = DataDeliverCustomerEventConverter.convert(record);
            result.add(convert);
        }
        PageHelper.clearPage();
        fillDataUpdateInfo(result);
        fillArkbiInfo(result);
        buildTree(result);
        PageInfo pageInfo = new PageInfo<>(records);
        pageInfo.setList(result);
        return pageInfo;
    }

    private static void normalizePageParam(DataDeliverCustomerEventRequest request) {
        // 若分页参数为空，则返回所有数据
        if (request.getPage() == null || request.getSize() == null) {
            request.setPage(1);
            request.setSize(Integer.MAX_VALUE);
        }
        PageHelper.startPage(request.getPage(), request.getSize());
    }

    private void buildTree(List<DataDeliverCustomerEventVO> parent) {
        if (CollectionUtils.isEmpty(parent)) {
            return;
        }
        List<Integer> parentRecordIds = parent.stream().map(DataDeliverCustomerEventVO::getId).collect(Collectors.toList());
        List<TgDeliverCustomerRecordDTO> childList = tgDeliverCustomerRecordDAO.listChilds(parentRecordIds);
        Map<Integer, List<TgDeliverCustomerRecordDTO>> childMap = childList.stream()
                .collect(Collectors.groupingBy(TgDeliverCustomerRecordDTO::getParentRecordId));
        List<DataDeliverCustomerEventVO> childVOList = new ArrayList();
        for (DataDeliverCustomerEventVO eventVO : parent) {
            if (!childMap.containsKey(eventVO.getId())) {
                continue;
            }
            List<TgDeliverCustomerRecordDTO> childRecords = childMap.get(eventVO.getId());
            List<DataDeliverCustomerEventVO> childNodes = childRecords.stream().map(DataDeliverCustomerEventConverter::convert).collect(Collectors.toList());
            eventVO.setItemList(childNodes);
            childVOList.addAll(childNodes);
        }
        fillDataUpdateInfo(childVOList);
        fillArkbiInfo(childVOList);
        buildTree(childVOList);
    }

    /**
     * 填充数据更新信息
     * 1. 数据的更新日期
     * 2. 数据条数
     *
     * @param eventList
     */
    private void fillDataUpdateInfo(List<DataDeliverCustomerEventVO> eventList) {
        List<Long> assetsIds = eventList.stream()
                .filter(event -> IconHelper.isAssetsNode(event.getIcon()))
                .map(DataDeliverCustomerEventVO::getNodeId)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(assetsIds)) {
            return;
        }

        Map<Long, ApplicationDataUpdateRecord> updateRecordMap = applicationDataUpdateRecordDAO.queryLatestByAssetIds(assetsIds);
        List<Long> dataUpdateUserIds = updateRecordMap.values().stream().map(ApplicationDataUpdateRecord::getUpdaterId).distinct().collect(Collectors.toList());
        Map<Long, SysUser> dataUpdateUserMap = CollectionUtils.isEmpty(dataUpdateUserIds) ? Collections.emptyMap() :
                sysUserService.listByIds(dataUpdateUserIds).stream().collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
        Map<Long, Long> applyDownloadMap = monitorDataMapper.groupByApplyDownloadView(assetsIds)
                .stream().collect(Collectors.toMap(it -> Long.valueOf(it.get("applyId").toString()), it -> Long.valueOf(it.get("total").toString())));
        eventList.stream()
                .filter(event -> IconHelper.isAssetsNode(event.getIcon()))
                .filter(event -> updateRecordMap.containsKey(event.getNodeId()))
                .forEach(event -> {
                    final Long applyId = event.getNodeId();
                    ApplicationDataUpdateRecord updateRecord = updateRecordMap.get(applyId);
                    SysUser user = dataUpdateUserMap.get(updateRecord.getUpdaterId());
                    event.setDataUpdater(Optional.ofNullable(user).map(SysUser::getRealName).orElse(""));
                    event.setDataUpdaterId(updateRecord.getUpdaterId());
                    event.setDataUpdateTime(DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, updateRecord.getFinishTime()));
                    event.setDataTotal(updateRecord.getUpdateCount());
                    event.setExportTotal(Optional.ofNullable(applyDownloadMap.get(applyId)).orElse(0L));
                    event.setItemList(Lists.newArrayList());
                });
    }


    /**
     * 填充bi信息
     *
     * @param eventList
     */
    private void fillArkbiInfo(List<DataDeliverCustomerEventVO> eventList) {
        List<Long> arkbiIds = eventList.stream()
                .filter(event -> IconHelper.isArkbiNode(event.getIcon()))
                .map(DataDeliverCustomerEventVO::getNodeId)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(arkbiIds)) {
            return;
        }
        Map<Long, ArkbiAnalysis> analysisMap = arkbiAnalysisService.listByIds(arkbiIds).stream()
                .collect(Collectors.toMap(ArkbiAnalysis::getId, Function.identity()));
        eventList.stream()
                .filter(event -> IconHelper.isArkbiNode(event.getIcon()))
                .filter(event -> analysisMap.containsKey(event.getNodeId()))
                .forEach(event -> {
                    ArkbiAnalysis arkbiAnalysis = analysisMap.get(event.getNodeId());
                    event.setApplicationIds(com.sinohealth.common.utils.StringUtils.toList(arkbiAnalysis.getAssetsId(), Long::valueOf));
                    event.setExtAnalysisId(arkbiAnalysis.getAnalysisId());
                });

    }

}
