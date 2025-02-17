package com.sinohealth.system.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.StatusTypeEnum;
import com.sinohealth.common.enums.monitor.SecondSubjectTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.module.file.dto.HuaweiPath;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.saas.file.api.rest.FileApi;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.system.config.FileProperties;
import com.sinohealth.system.converter.CustomerAuth2AssetsDirConverter;
import com.sinohealth.system.dao.TgTableApplicationMappingInfoDAO;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.domain.TgCustomerApplyAuth;
import com.sinohealth.system.domain.ckpg.CustomerCKProperties;
import com.sinohealth.system.domain.value.deliver.DeliverResourceType;
import com.sinohealth.system.domain.value.deliver.DeliverStrategyFactory;
import com.sinohealth.system.domain.value.deliver.Resource;
import com.sinohealth.system.domain.value.deliver.ResourceDeliverStrategy;
import com.sinohealth.system.domain.value.deliver.datasource.CharAnalysisDataSource;
import com.sinohealth.system.domain.value.deliver.datasource.CustomerApplyDataSource;
import com.sinohealth.system.domain.value.deliver.datasource.PanelDataSource;
import com.sinohealth.system.domain.value.deliver.sink.HttpServletResponseResourceSink;
import com.sinohealth.system.domain.value.deliver.sink.ObsResourceSink;
import com.sinohealth.system.domain.vo.TgTableApplicationMappingInfo;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.TgCustomerApplyAuthDto;
import com.sinohealth.system.dto.assets.*;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.OutsideClickhouseMapper;
import com.sinohealth.system.monitor.event.EventReporterUtil;
import com.sinohealth.system.monitor.mapper.MonitorDataMapper;
import com.sinohealth.system.service.*;
import com.sinohealth.system.util.ApplicationSqlUtil;
import com.sinohealth.system.util.TreeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-01 17:27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerAssetsV2ServiceImpl implements CustomerAssetsV2Service {

    private final DeliverStrategyFactory deliverStrategyFactory;

    private final ISysCustomerAuthService sysCustomerAuthService;

    private final ArkbiAnalysisService arkbiAnalysisService;

    private final TgTableApplicationMappingInfoDAO tableApplicationMappingInfoDAO;

    private final OutsideClickhouseMapper outsideClickhouseMapper;

    private final CustomerCKProperties customerCKProperties;

    private final MonitorDataMapper monitorDataMapper;

    private final ISysUserService sysUserService;

    private final DataDescriptionService dataDescriptionService;


    @Autowired
    private FileApi fileApi;

    @Autowired
    private AppProperties appProperties;
    @Autowired
    private FileProperties fileProperties;

    @Override
    public Long getDataVolume(Long applicationId, String whereSql) {
        TgTableApplicationMappingInfo mappingInfo = tableApplicationMappingInfoDAO.getByAssetsId(applicationId);
        String tableName = mappingInfo.getDataTableName();
        String countSQL = outsideClickhouseMapper.buildCountSQL(tableName, whereSql);
        return outsideClickhouseMapper.getDataCount(countSQL);
    }

    @Override
    public List getTree(AssetsDirTreeQuery query) {
//        Wrapper<TgCustomerApplyAuth> wrapper = Wrappers.lambdaQuery(TgCustomerApplyAuth.class)
//                .eq(TgCustomerApplyAuth::getUserId, ThreadContextHolder.getSysUser().getUserId())
//                .eq(TgCustomerApplyAuth::getStatus, StatusTypeEnum.IS_ENABLE.getId())
//                .like(StringUtils.isNotBlank(query.getSearchKey()), TgCustomerApplyAuth::getNodeName, query.getSearchKey());
//        List<TgCustomerApplyAuth> list = sysCustomerAuthService.list(wrapper);

        // 为了避免数据异步同步，授权记录先于数据出现在列表页，需要关联映射表做过滤，
        // 但是BI类数据无法直接过滤，可能会出现BI图标立即展示，但是数据未同步
        List<TgCustomerApplyAuth> list = sysCustomerAuthService.queryForTree(ThreadContextHolder.getSysUser().getUserId(),
                StatusTypeEnum.IS_ENABLE.getId(), query.getSearchKey());

        return buildAssetsTree(list);
    }

    @Override
    public List getCustomerTree(CustomerAuthTreeQuery query) {
        Wrapper<TgCustomerApplyAuth> wrapper = Wrappers.lambdaQuery(TgCustomerApplyAuth.class).eq(TgCustomerApplyAuth::getUserId, query.getUserId());
        List<TgCustomerApplyAuth> list = sysCustomerAuthService.list(wrapper);
        return buildAssetsTree(list);
    }

    @Override
    public List getSubCustomerTree(SubCustomerAuthTreeQuery query) {
        // 先查询出父账号的授权资产
        Wrapper<TgCustomerApplyAuth> parentAuthWrapper = Wrappers.lambdaQuery(TgCustomerApplyAuth.class).eq(TgCustomerApplyAuth::getStatus, 1).eq(TgCustomerApplyAuth::getUserId, query.getParentUserId());
        List<TgCustomerApplyAuth> parentAuthList = sysCustomerAuthService.list(parentAuthWrapper);
        // 查询出子账号的授权资产
        Wrapper<TgCustomerApplyAuth> subAuthWrapper = Wrappers.lambdaQuery(TgCustomerApplyAuth.class).eq(TgCustomerApplyAuth::getUserId, query.getSubUserId());
        List<TgCustomerApplyAuth> subtAuthList = sysCustomerAuthService.list(subAuthWrapper);
        // 修改资产授权状态
        parentAuthList.forEach(it -> it.setAuthType(""));
        // 启用、禁用状态已父账号为准?
        Function<TgCustomerApplyAuth, Long> keyFunction = it -> it.getParentCustomerAuthId();
        Map<Long, TgCustomerApplyAuth> subAuthMap = subtAuthList.stream().collect(Collectors.toMap(it -> keyFunction.apply(it), Function.identity()));
        // 根据授权资产找到对应的数据目录
        parentAuthList.forEach(parentAuth -> {
            TgCustomerApplyAuth subAuth = subAuthMap.get(parentAuth.getId());
            if (Objects.nonNull(subAuth)) {
                parentAuth.setAuthType(subAuth.getAuthType());
            }
        });
        return buildAssetsTree(parentAuthList);
    }

    private List buildAssetsTree(List<TgCustomerApplyAuth> list) {
        List<Long> authIds = list.stream().map(TgCustomerApplyAuth::getId).collect(Collectors.toList());
        Map<Long, Long> viewMap = CollectionUtils.isEmpty(authIds) ? Collections.EMPTY_MAP :
                monitorDataMapper.groupByCustomerAuthView(authIds, ThreadContextHolder.getSysUser().getUserId())
                        .stream().collect(Collectors.toMap(it -> Long.valueOf(it.get("authId").toString()), it -> (Long) it.get("total")));
        List<AssetsDirDTO> assetsDirDTOList = list.stream().map(CustomerAuth2AssetsDirConverter::convert).collect(Collectors.toList());

        // 1. bi节点额外设置extAnalysisId
        List<Long> arkbiIds = assetsDirDTOList.stream().filter(AssetsDirDTO::isArkbiNode).map(AssetsDirDTO::getNodeId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(arkbiIds)) {
            Map<Long, String> arkbiMap = arkbiAnalysisService.listByIds(arkbiIds).stream().collect(Collectors.toMap(ArkbiAnalysis::getId, ArkbiAnalysis::getAnalysisId));
            assetsDirDTOList.stream().filter(AssetsDirDTO::isArkbiNode).forEach(node -> {
                node.setExtAnalysisId(arkbiMap.get(node.getNodeId()));
            });
        }

        // 2. 设置数据条数、数据更新时间、最新期数
        List<Long> assetsIds = assetsDirDTOList.stream().filter(AssetsDirDTO::isApplicationNode)
                .map(AssetsDirDTO::getNodeId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(assetsIds)) {
            Map<Long, TgTableApplicationMappingInfo> mappingInfoMap = tableApplicationMappingInfoDAO.list(assetsIds)
                    .stream().collect(Collectors.toMap(TgTableApplicationMappingInfo::getAssetsId, Function.identity()));
            assetsDirDTOList.stream().filter(AssetsDirDTO::isApplicationNode).filter(node -> mappingInfoMap.containsKey(node.getNodeId()))
                    .forEach(node -> {
                        TgTableApplicationMappingInfo mappingInfo = mappingInfoMap.get(node.getNodeId());
                        node.setDataCount(mappingInfo.getDataVolume());
                        node.setDataUpdateTime(DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", mappingInfo.getDateUpdateTime()));
                        node.setLatestDate(DateUtils.dateTime(mappingInfo.getDateUpdateTime()));
                    });
        }

        // 3. 设置查看次数
        assetsDirDTOList.forEach(dirDTO -> dirDTO.setViewCount(viewMap.getOrDefault(dirDTO.getId(), 0L)));

        Map<Long, Integer> descMap = dataDescriptionService.queryByAssetsIds(assetsIds);
        assetsDirDTOList.stream().filter(AssetsDirDTO::isApplicationNode).forEach(v -> {
            v.setHasDataDesc(descMap.containsKey(v.getNodeId()));
        });

        // 4. 更新人
        List<Long> updateUserIds = assetsDirDTOList.stream().map(AssetsDirDTO::getUpdateBy).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, SysUser> userMap;
        if (CollectionUtils.isNotEmpty(updateUserIds)) {
            userMap = sysUserService.listByIds(updateUserIds).stream().collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
        } else {
            userMap = Collections.emptyMap();
        }

        assetsDirDTOList.stream().filter(it -> Objects.nonNull(it.getUpdateBy()))
                .forEach(it -> it.setUpdatedByName(Optional.ofNullable(userMap.get(it.getUpdateBy()))
                        .map(SysUser::getUserName).orElse(null)));
        // 转化树形结构
        return TreeUtils.transformTreeGroup(0L, assetsDirDTOList, null);
    }

    @Override
    public List<AuthTableFieldDTO> getAuthTableFields(Long assetsId) {
        // 外网ck授权表名
        TgTableApplicationMappingInfo mappingInfo = tableApplicationMappingInfoDAO.getByAssetsId(assetsId);
        Assert.isTrue(mappingInfo != null, "客户资产不存在： " + assetsId);
        // 读取外网ck system.columns元数据表，获取表字段
        List<AuthTableFieldDTO> fields = outsideClickhouseMapper.getFields(customerCKProperties.getDatabase(), mappingInfo.getDataTableName());
        for (AuthTableFieldDTO field : fields) {
            field.setDataType(ApplicationSqlUtil.trimLengthAndType(field.getDataType()));
        }
        return fields;
    }

    /**
     * 埋点
     * 1、我的数据 -> 交付记录 -> 预览 （这个入口不需要统计）
     * 2、我的资产 -> 详情
     *
     * @param assetsId
     * @param requestDTO
     * @return
     */
    @Override
    public Object getAuthTableData(Long assetsId, GetDataInfoRequestDTO requestDTO) {
        TgCustomerApplyAuthDto customerApplyAuth = sysCustomerAuthService.queryList(assetsId.intValue(), null).get(0);
        // 判断当前用户是否是资产所有者
        if (ThreadContextHolder.getSysUser().getUserId().equals(customerApplyAuth.getUserId())) {
            // 客户查表次数(资产粒度)-埋点 (get(0)是因为一个资产只能分配给一个客户)
            EventReporterUtil.operateLogEvent4View(customerApplyAuth.getId().toString(),
                    customerApplyAuth.getProjectName(), SecondSubjectTypeEnum.CUSTOMER_APPLY_AUTH_VIEW, null);
            // 客户查表次数(表单粒度)-埋点
            UserDataAssets assets = new UserDataAssets().selectById(assetsId);
            if (Objects.isNull(assets)) {
                throw new CustomException("资产数据不存在");
            }

            // 常规模式没有底表id
            if (Objects.nonNull(assets.getBaseTableId())) {
                EventReporterUtil.operateLogEvent4View(assets.getBaseTableId().toString(),
                        assets.getBaseTableName(), SecondSubjectTypeEnum.CUSTOMER_APPLY_TABLE_VIEW, null);
            }
        }

        //
        TgTableApplicationMappingInfo mappingInfo = tableApplicationMappingInfoDAO.getByAssetsId(assetsId);
        Assert.isTrue(mappingInfo != null, "客户资产不存在： " + assetsId);
        String tableName = mappingInfo.getDataTableName();
        String whereSql = requestDTO.buildWhereSQL();
        Integer pageSize = requestDTO.getPageSize();
        Integer pageNum = requestDTO.getPageNum();
        String sortBy = requestDTO.getSortBy();
        String sortField = requestDTO.getSortingField();
        List<AuthTableFieldDTO> tableFieldInfos = getAuthTableFields(assetsId);
        String selectDataSQL = outsideClickhouseMapper.buildSelectDataSQL(tableName, whereSql, pageSize, (pageNum - 1) * pageSize, sortBy, sortField);
        List<LinkedHashMap<String, Object>> list = outsideClickhouseMapper.selectBySQL(selectDataSQL);
        String countSQL = outsideClickhouseMapper.buildCountSQL(tableName, whereSql);
        Long dataVolume = outsideClickhouseMapper.getDataCount(countSQL);
        return new HashMap<String, Object>() {{
            put("header", tableFieldInfos);
            put("list", list);
            put("total", dataVolume);
        }};
    }

    @Override
    public HuaweiPath downloadApply(AssetsFormDownloadReqDTO reqDTO) throws Exception {

        TgCustomerApplyAuth node = getApplyAuthNode(reqDTO.getAssetsId());
        CustomerApplyDataSource customerApplyDataSource = new CustomerApplyDataSource(node.getNodeId());
        DeliverResourceType deliverResourceType = DeliverResourceType.fromName(reqDTO.getDownloadType());
        customerApplyDataSource.setRequestDTO(reqDTO.getFilter());
        ResourceDeliverStrategy strategy = deliverStrategyFactory.getStrategy(customerApplyDataSource, deliverResourceType);
        Resource resource = strategy.deliver(customerApplyDataSource);
//        HttpServletResponseResourceSink sink = new HttpServletResponseResourceSink();
//        sink.setResource(resource);
//        sink.setType(deliverResourceType);
//        sink.process();
        ObsResourceSink sink = new ObsResourceSink(resource, deliverResourceType, fileApi, fileProperties);
        return sink.process();
    }


    @Override
    public HuaweiPath downloadChart(AssetsChartDownloadReqDTO reqDTO) throws Exception {
        TgCustomerApplyAuth node = getArkbiAuthNode(reqDTO.getArkId());
        CharAnalysisDataSource charAnalysisDataSource = new CharAnalysisDataSource(node.getNodeId(), node.getNodeName());
        DeliverResourceType deliverResourceType = DeliverResourceType.fromName(reqDTO.getDownloadType());
        ResourceDeliverStrategy strategy = deliverStrategyFactory.getStrategy(charAnalysisDataSource, deliverResourceType);
        Resource resource = strategy.deliver(charAnalysisDataSource);
        ObsResourceSink sink = new ObsResourceSink(resource, deliverResourceType, fileApi, fileProperties);
        return sink.process();
    }

    @Override
    public void downloadDashboard(AssetsDashboardDownloadReqDTO reqDTO) throws Exception {
        TgCustomerApplyAuth node = getArkbiAuthNode(reqDTO.getArkId());
        PanelDataSource panelDataSource = new PanelDataSource(node.getNodeId(), node.getNodeName());
        DeliverResourceType deliverResourceType = DeliverResourceType.fromName(reqDTO.getDownloadType());
        ResourceDeliverStrategy strategy = deliverStrategyFactory.getStrategy(panelDataSource, deliverResourceType);
        Resource resource = strategy.deliver(panelDataSource);
        HttpServletResponseResourceSink sink = new HttpServletResponseResourceSink();
        sink.setResource(resource);
        sink.setType(deliverResourceType);
        sink.process();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAuthStatus(AssetsAuthStatusUpdateReqDTO reqDTO) {
        // fixme 是否需要权限校验？
        TgCustomerApplyAuth entity = new TgCustomerApplyAuth();
        entity.setId(reqDTO.getAuthId());
        entity.setStatus(reqDTO.getStatus());
        entity.setUpdateBy(ThreadContextHolder.getSysUser().getUserId());
        sysCustomerAuthService.updateById(entity);
    }

    public TgCustomerApplyAuth getArkbiAuthNode(Long arkbiId) {
        Wrapper<TgCustomerApplyAuth> wrapper = Wrappers.<TgCustomerApplyAuth>lambdaQuery()
//                .eq(TgCustomerApplyAuth::getUserId, ThreadContextHolder.getSysUser().getUserId())
                .in(TgCustomerApplyAuth::getIcon, Arrays.asList(CommonConstants.ICON_CHART, CommonConstants.ICON_DASHBOARD)).eq(TgCustomerApplyAuth::getNodeId, arkbiId);
        TgCustomerApplyAuth entity = sysCustomerAuthService.getOne(wrapper);
        Assert.isTrue(entity != null, "bi数据不存在");
        return entity;
    }

    public TgCustomerApplyAuth getApplyAuthNode(Long assetsId) {
        Wrapper<TgCustomerApplyAuth> wrapper = Wrappers.<TgCustomerApplyAuth>lambdaQuery()
//                .eq(TgCustomerApplyAuth::getUserId, ThreadContextHolder.getSysUser().getUserId())
                .eq(TgCustomerApplyAuth::getIcon, CommonConstants.ICON_DATA_ASSETS).eq(TgCustomerApplyAuth::getNodeId, assetsId);
        TgCustomerApplyAuth entity = sysCustomerAuthService.getOne(wrapper, false);
        Assert.isTrue(entity != null, "授权表单不存在");
        return entity;
    }


}
