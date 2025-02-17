package com.sinohealth.system.biz.dataassets.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.ShelfState;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dataassets.FileTypeEnum;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.ipaas.model.ResMaindataMainDepartmentSelectUserWithDeptItemDataItem;
import com.sinohealth.ipaas.model.ResMaindatamainDepartmentselectbyidsItemDataItem;
import com.sinohealth.system.biz.dataassets.service.HomePageService;
import com.sinohealth.system.biz.homePage.*;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.catalogue.AssetsCatalogue;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.dto.assets.TgAssetFrontTreeQueryResult;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.*;
import com.sinohealth.system.service.IAssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author shallwetalk
 * @Date 2023/8/24
 */
@Service
@Slf4j
public class HomePageServiceImpl implements HomePageService {


    @Autowired
    private TgAssetInfoMapper tgAssetInfoMapper;

    @Autowired
    private TgApplicationInfoMapper tgApplicationInfoMapper;

    @Autowired
    private AssetsCatalogueMapper assetsCatalogueMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private AssetUserRelationMapper assetUserRelationMapper;

    @Autowired
    private IAssetService assetService;


    @Override
    public AjaxResult<List<HotAssetsDTO>> hotAssets(Integer type, Integer pageSize, Integer catalogId, Integer source) {
        Page page = new Page(1, pageSize);
        List<HotAssetsDTO> list;
        // 获取半年前的时间
        final Date date = DateUtils.addMonths(new Date(), -6);
        final List<TgAssetFrontTreeQueryResult> tgAssetFrontTreeQueryResults = assetService.allReadableAsset();
        final Map<Long, TgAssetFrontTreeQueryResult> readableMap = tgAssetFrontTreeQueryResults.stream().collect(Collectors.toMap(k -> k.getId(), v -> v));
        List<Integer> catalogueIds = getAllChild(catalogId);
        if (type == 1) {
            // 按申请次数
            final IPage<HotAssetsDTO> applicantCount = tgApplicationInfoMapper.getApplicantCount(page, null, date, catalogueIds, source);
            list = applicantCount.getRecords();
        } else {
            // 按浏览次数
            final IPage<HotAssetsDTO> hotAssetsDTOIPage = assetUserRelationMapper.hotViewAssets(page, null, date, catalogueIds, source);
            list = hotAssetsDTOIPage.getRecords();
        }
        final Long userId = SecurityUtils.getUserId();
        final SinoPassUserDTO o = (SinoPassUserDTO) ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO);
        String deptId = o.getMainOrganizationId();
        list.forEach(a -> {
            TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance().selectById(a.getId());
            a.setType(tgAssetInfo.getType());
            a.setPermission(assetService.computePermissions(tgAssetInfo, userId, deptId, true));
            // 显示当前用户是否申请过该资产, 以及申请次数
            Integer currentUserApplyCount = assetService.computeCurrentUserApplyCount(a.getId(), userId);
            a.setCurrentUserApplyCount(currentUserApplyCount);
            a.setHasApplied(currentUserApplyCount > 0);
            a.setHasReadPermission(Objects.nonNull(readableMap.get(a.getId())));
            // 填充流程信息
            a.setProcessId(tgAssetInfo.getProcessId());
            a.setRelatedId(tgAssetInfo.getRelatedId());
            a.setAssetBindingDataName(tgAssetInfo.getAssetBindingDataName());
        });
        return AjaxResult.success(list);
    }

    @Override
    public AjaxResult<DataStatistics> getAssetStatistics(Integer catalogId) {
        List<Integer> catalogueIds = getAllChild(catalogId);
        final LambdaQueryWrapper<TgAssetInfo> wq = Wrappers.<TgAssetInfo>lambdaQuery()
                .eq(TgAssetInfo::getShelfState, ShelfState.LISTING.getStatus())
                .in(CollUtil.isNotEmpty(catalogueIds), TgAssetInfo::getAssetMenuId, catalogueIds)
                .eq(TgAssetInfo::getDeleted, 0);
        final List<TgAssetInfo> tgAssetInfos = tgAssetInfoMapper.selectList(wq).stream().filter(i -> Objects.nonNull(i.getType())).collect(Collectors.toList());
        final Map<AssetType, List<TgAssetInfo>> typeMap = tgAssetInfos.stream()
                .collect(Collectors.groupingBy(TgAssetInfo::getType));

        final DataStatistics dataStatistics = new DataStatistics();

        dataStatistics.setAssetsAllCount(tgAssetInfos.size());
        final List<TgAssetInfo> moduleList = typeMap.get(AssetType.MODEL);
        dataStatistics.setModuleAllCount(CollUtil.isNotEmpty(moduleList) ? moduleList.size() : 0);
        final List<TgAssetInfo> tableList = typeMap.get(AssetType.TABLE);
        dataStatistics.setTableAllCount(CollUtil.isNotEmpty(tableList) ? tableList.size() : 0);
        final List<TgAssetInfo> fileList = typeMap.get(AssetType.FILE);
        dataStatistics.setFileAllCount(CollUtil.isNotEmpty(fileList) ? fileList.size() : 0);
        return AjaxResult.success(dataStatistics);
    }

    @Override
    public AjaxResult<AssetDistribution> assetDistribution(Integer catalogId) {
        List<Integer> catalogueIds = getAllChild(catalogId);
        // 获取所有已上架的资产
        final LambdaQueryWrapper<TgAssetInfo> wq = Wrappers.<TgAssetInfo>lambdaQuery()
                .eq(TgAssetInfo::getShelfState, ShelfState.LISTING.getStatus())
                .in(CollUtil.isNotEmpty(catalogueIds), TgAssetInfo::getAssetMenuId, catalogueIds)
                .eq(TgAssetInfo::getDeleted, 0);
        final List<TgAssetInfo> tgAssetInfos = tgAssetInfoMapper.selectList(wq).stream()
                .filter(i -> Objects.nonNull(i.getType())).collect(Collectors.toList());

        final String otherShowName = "其他";

        // 获取资产提供方三级部门映射关系
        final Map<String, String> providerMap = new HashMap<String, String>();
        final List<String> providers = tgAssetInfos.stream().map(TgAssetInfo::getAssetProvider).collect(Collectors.toList());
        final List<ResMaindatamainDepartmentselectbyidsItemDataItem> dataItems = SinoipaasUtils.mainDepartmentSelectbyids(providers);
        for (ResMaindatamainDepartmentselectbyidsItemDataItem item : dataItems) {
            final String orgAdminTreePathText = item.getOrgAdminTreePathText();
            final List<String> fullPath = Arrays.asList(orgAdminTreePathText.split("/"));
            if (fullPath.size() > 4) {
                final String path = fullPath.get(3) + "/" + fullPath.get(4);
                providerMap.put(item.getId(), path);
            } else {
                providerMap.put(item.getId(), otherShowName);
            }
        }


        // 根据资产提供者分组
        final Map<String, List<TgAssetInfo>> collect = tgAssetInfos.stream()
                .collect(Collectors.groupingBy(TgAssetInfo::getAssetProvider));

        // 根据资产提供者提供的资产数量进行排序
        final List<List<TgAssetInfo>> sortList = collect.entrySet().stream().map(a -> a.getValue())
                .sorted(Collections.reverseOrder(Comparator.comparingInt(List::size)))
                .collect(Collectors.toList());


        // 按照三级部门进行统计
        // 求资产占比与资产数
        Map<String, List<TgAssetInfo>> map = new HashMap<>();
        final Iterator<List<TgAssetInfo>> iterator = sortList.iterator();
        while (iterator.hasNext()) {
            final List<TgAssetInfo> next = iterator.next();
            final String assetProvider = next.stream().findAny().get().getAssetProvider();
            final String showProvider = providerMap.get(assetProvider);
            final List<TgAssetInfo> assetInfos = map.get(showProvider);
            if (CollUtil.isNotEmpty(assetInfos)) {
                assetInfos.addAll(next);
                map.put(showProvider, assetInfos);
            } else {
                map.put(showProvider, next);
            }
            iterator.remove();
        }

        // 获取资产应用占比
        Map<String, Double> applyProportion = new HashMap<String, Double>();
        // 申请总量
        final Integer allCount = tgApplicationInfoMapper.selectCount(Wrappers.<TgApplicationInfo>lambdaQuery().isNotNull(TgApplicationInfo::getNewAssetId).in(TgApplicationInfo::getNewAssetId, tgAssetInfos.stream().map(TgAssetInfo::getId).collect(Collectors.toSet())));
        map.entrySet().stream()
                .forEach(entry -> {
                    final List<TgAssetInfo> value = entry.getValue();
                    final List<Long> assetIds = value.stream().map(TgAssetInfo::getId).collect(Collectors.toList());
                    Integer count = 0;
                    if (CollUtil.isNotEmpty(assetIds)) {
                        final LambdaQueryWrapper<TgApplicationInfo> lwq = Wrappers.<TgApplicationInfo>lambdaQuery()
                                .in(TgApplicationInfo::getNewAssetId, assetIds);
                        count = tgApplicationInfoMapper.selectCount(lwq);
                    }
                    BigDecimal b = new BigDecimal((float) count / allCount);
                    Double result = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
                    applyProportion.put(entry.getKey(), result);
                });


        final AssetDistribution assetDistribution = new AssetDistribution();


        //资产分布饼图
        List<ProviderProportion> proportions = new ArrayList<>();
        map.forEach((k, v) -> {
            final ProviderProportion providerProportion = new ProviderProportion();
            final String name = providerMap.get(k);
            providerProportion.setName(Objects.isNull(name) ? k : name);
            Integer assetCount = CollUtil.isEmpty(v) ? 0 : v.size();
            providerProportion.setAssetCount(assetCount);
            providerProportion.setProviderId(k);
            // 资产占比
            BigDecimal b = new BigDecimal((float) assetCount / tgAssetInfos.size());
            Double result = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
            providerProportion.setAssetProportion(result);
            // 应用占比
            providerProportion.setAssetApplyProportion(applyProportion.get(k));
            proportions.add(providerProportion);
        });

        assetDistribution.setProportions(CollUtil.isEmpty(proportions) ? proportions : proportions.stream().sorted(Comparator.comparing(ProviderProportion::getAssetCount).reversed()).collect(Collectors.toList()));

        // 资产提供方总数
        assetDistribution.setProviderCount(proportions.size());

        return AjaxResult.success(assetDistribution);
    }


    @Override
    public AjaxResult<AssetDistribution> assetDistributions(Integer catalogId, Integer type) {
        AssetDistribution assetDistribution = new AssetDistribution();
        // 获取需要统计的目录-所有
        final List<AssetsCatalogue> assetsCatalogues = assetsCatalogueMapper.selectListInPath(Lists.newArrayList(catalogId));
        // 获取所有二级目录，根据二级目录，对所有需要统计的目录进行分组
        final LambdaQueryWrapper<AssetsCatalogue> wq = Wrappers.<AssetsCatalogue>lambdaQuery()
                .eq(AssetsCatalogue::getParentId, catalogId)
                .eq(AssetsCatalogue::getDeleted, 0);
        final List<AssetsCatalogue> parentList = assetsCatalogueMapper.selectList(wq);
        Map<AssetsCatalogue, List<AssetsCatalogue>> map = new HashMap<AssetsCatalogue, List<AssetsCatalogue>>();
        parentList.forEach(asset -> {
            final List<AssetsCatalogue> catalogues = assetsCatalogues.stream()
                    .filter(cata -> cata.getPath().contains(asset.getPath()))
                    .collect(Collectors.toList());

            map.put(asset, catalogues);
        });

        // 获取资产总数
        final List<Integer> catalogueIds = assetsCatalogues.stream().map(AssetsCatalogue::getId).collect(Collectors.toList());
        final LambdaQueryWrapper<TgAssetInfo> eq = Wrappers.<TgAssetInfo>lambdaQuery()
                .in(TgAssetInfo::getAssetMenuId, catalogueIds)
                .eq(TgAssetInfo::getShelfState, "已上架")
                .eq(TgAssetInfo::getDeleted, 0);
        final List<TgAssetInfo> tgAssetInfos = tgAssetInfoMapper.selectList(eq);

        // 资产总数
        final int assetCount = tgAssetInfos.size();
        assetDistribution.setAssetCount(assetCount);

        if (type.equals(1)) {
            // 主业务 - 根据二级目录进行统计
            final List<ProviderProportion> proportions = map.entrySet().stream()
                    .map(entry -> {
                        final ProviderProportion providerProportion = new ProviderProportion();
                        // 二级目录名称
                        providerProportion.setName(entry.getKey().getName());
                        final List<Integer> ids = entry.getValue()
                                .stream().map(AssetsCatalogue::getId)
                                .collect(Collectors.toList());
                        // 当前目录下需要统计的资产
                        final List<TgAssetInfo> assetInfos = tgAssetInfos.stream()
                                .filter(asset -> ids.contains(asset.getAssetMenuId()))
                                .collect(Collectors.toList());

                        // 计算分布
                        if (CollUtil.isEmpty(assetInfos)) {
                            providerProportion.setAssetCount(0);
                            providerProportion.setAssetProportion(0);
                        } else {
                            providerProportion.setAssetCount(assetInfos.size());
                            BigDecimal b = new BigDecimal((float) assetInfos.size() / assetCount);
                            Double result = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();

                            providerProportion.setAssetProportion(result);
                        }

                        return providerProportion;
                    }).collect(Collectors.toList());
            assetDistribution.setProportions(CollUtil.isEmpty(proportions) ? proportions : proportions.stream().sorted(Comparator.comparing(ProviderProportion::getAssetCount).reversed()).collect(Collectors.toList()));
        } else {
            // 资产类型 - 根据【模型】【库表】【文件】为维度进行数据统计
            final List<ProviderProportion> proportions = tgAssetInfos.stream()
                    .collect(Collectors.groupingBy(TgAssetInfo::getType))
                    .entrySet()
                    .stream()
                    .map(entry -> {
                        final ProviderProportion providerProportion = new ProviderProportion();
                        // 维度
                        providerProportion.setName(entry.getKey().getName());

                        // 获取占比
                        final List<TgAssetInfo> assetInfos = entry.getValue();

                        if (CollUtil.isEmpty(assetInfos)) {
                            providerProportion.setAssetProportion(0);
                            providerProportion.setAssetCount(0);
                        } else {
                            providerProportion.setAssetCount(assetInfos.size());
                            BigDecimal b = new BigDecimal((float) assetInfos.size() / assetCount);
                            Double result = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();

                            providerProportion.setAssetProportion(result);
                        }

                        return providerProportion;
                    }).collect(Collectors.toList());
            assetDistribution.setProportions(CollUtil.isEmpty(proportions) ? proportions : proportions.stream().sorted(Comparator.comparing(ProviderProportion::getAssetCount).reversed()).collect(Collectors.toList()));
        }

        return AjaxResult.success(assetDistribution);
    }

    @Override
    public AjaxResult<List<LatestAsset>> latestAsset(Integer pageSize, Integer catalogId) {
        // 获取当前用户的 DEPT 和 USER 信息
        final Long userId = SecurityUtils.getUserId();
        final SinoPassUserDTO o = (SinoPassUserDTO) ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO);
        String deptId = o.getMainOrganizationId();
        final List<TgAssetFrontTreeQueryResult> tgAssetFrontTreeQueryResults = assetService.allReadableAsset();
        final Map<Long, TgAssetFrontTreeQueryResult> readableMap = tgAssetFrontTreeQueryResults.stream()
                .collect(Collectors.toMap(TgAssetFrontTreeQueryResult::getId, v -> v));
        List<Integer> catalogueIds = getAllChild(catalogId);
        final LambdaQueryWrapper<TgAssetInfo> wq = Wrappers.<TgAssetInfo>lambdaQuery()
                .eq(TgAssetInfo::getShelfState, ShelfState.LISTING.getStatus())
                .eq(TgAssetInfo::getDeleted, 0)
                .in(CollUtil.isNotEmpty(catalogueIds), TgAssetInfo::getAssetMenuId, catalogueIds)
                .orderByDesc(TgAssetInfo::getUpdateTime)
                .last("limit " + pageSize);
        final List<TgAssetInfo> infos = tgAssetInfoMapper.selectList(wq);
        SimpleDateFormat fullParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat parser = new SimpleDateFormat("MM-dd");
        return AjaxResult.success(infos.stream()
                .map(i -> {
                    try {
                        final String time = Objects.nonNull(i.getUpdateTime()) ? parser.format(fullParser.parse(i.getUpdateTime())) : "";
                        TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance().selectById(i.getId());
                        // 显示当前用户是否申请过该资产, 以及申请次数
                        Integer currentUserApplyCount = assetService.computeCurrentUserApplyCount(i.getId(), userId);
                        return new LatestAsset(i.getId(),
                                i.getAssetName(),
                                time,
                                assetService.computePermissions(tgAssetInfo, userId, deptId, true),
                                tgAssetInfo.getType(),
                                tgAssetInfo.getProcessId(),
                                tgAssetInfo.getRelatedId(),
                                tgAssetInfo.getAssetBindingDataName(),
                                currentUserApplyCount > 0,
                                Objects.nonNull(readableMap.get(i.getId())),
                                currentUserApplyCount
                        );
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList()));
    }

    @Override
    public AjaxResult<List<AssetApply>> assetApply(Integer catalogId) {
        List<Integer> catalogueIds = getAllChild(catalogId);

        final List<TgApplicationInfo> list = tgApplicationInfoMapper.queryUsefulApplicationInfo(catalogueIds);
        if (list.isEmpty()) {
            return AjaxResult.success(new ArrayList<>());
        }

        final List<Long> userIds = list.stream().map(TgApplicationInfo::getApplicantId).distinct().collect(Collectors.toList());

        final List<SysUser> users = sysUserMapper.selectUserByIds(userIds);

        // 防止用户被物理删除
        final List<Long> existUserId = users.stream().map(SysUser::getUserId).collect(Collectors.toList());

        // 获取userId的deptId
        final Map<String, Long> orgIdUserIdMap = users.stream().collect(Collectors.toMap(SysUser::getOrgUserId, SysUser::getUserId));

        final List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> result
                = SinoipaasUtils.employeeWithDept(users.stream().map(SysUser::getOrgUserId).collect(Collectors.toList()));

        final String otherShowName = "其他";

        // 获取资产提供方三级部门映射关系
        final Map<String, String> providerMap = new HashMap<String, String>();
        for (ResMaindataMainDepartmentSelectUserWithDeptItemDataItem item : result) {
            final String orgAdminTreePathText = item.getOrgAdminTreePathText();
            final List<String> fullPath = Arrays.asList(orgAdminTreePathText.split("/"));
            if (fullPath.size() > 4) {
                final String path = fullPath.get(3) + "/" + fullPath.get(4);
                providerMap.put(item.getId(), path);
            } else {
                providerMap.put(item.getId(), otherShowName);
            }
        }

        Map<Long, ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> map = new HashMap<>();
        result.forEach(item -> {
            final Long userId = orgIdUserIdMap.get(item.getId());
            map.put(userId, item);
        });

        // 根据部门分组-排序-统计
        final List<AssetApply> applyList = list.stream()
                .filter(a -> existUserId.contains(a.getApplicantId())).collect(Collectors.groupingBy(a -> {
                    final ResMaindataMainDepartmentSelectUserWithDeptItemDataItem item = map.get(a.getApplicantId());
                    return providerMap.get(item.getId());
                })).values().stream()
                .sorted(Collections.reverseOrder(Comparator.comparingInt(List::size)))
                .map(a -> {
                    final AssetApply assetApply = new AssetApply();

                    final String departName = providerMap.get(map.get(a.stream().findAny().get().getApplicantId()).getId());
                    assetApply.setDeptName(departName);
                    final Map<String, List<TgApplicationInfo>> typeGroup = a.stream()
                            .collect(Collectors.groupingBy(TgApplicationInfo::getApplicationType));

                    final List<TgApplicationInfo> moduleList = typeGroup.get(ApplicationConst.ApplicationType.DATA_APPLICATION);
                    assetApply.setModuleCount(CollUtil.isNotEmpty(moduleList) ? moduleList.size() : 0);

                    final List<TgApplicationInfo> tableList = new ArrayList<>();
                    final List<TgApplicationInfo> table = typeGroup.get(ApplicationConst.ApplicationType.TABLE_APPLICATION);
                    if (CollUtil.isNotEmpty(table)) {
                        tableList.addAll(table);
                    }
                    final List<TgApplicationInfo> sync = typeGroup.get(ApplicationConst.ApplicationType.DATA_SYNC_APPLICATION);
                    if (CollUtil.isNotEmpty(sync)) {
                        tableList.addAll(sync);
                    }
                    assetApply.setTableCount(CollUtil.isNotEmpty(tableList) ? tableList.size() : 0);

                    final List<TgApplicationInfo> fileList = typeGroup.get(ApplicationConst.ApplicationType.DOC_APPLICATION);
                    assetApply.setFileCount(CollUtil.isNotEmpty(fileList) ? fileList.size() : 0);

                    return assetApply;
                })
                .collect(Collectors.toList());


        return AjaxResult.success(applyList);
    }


    @Override
    public AjaxResult<AssetTypeStatistics> assetType(Integer catalogId, Integer type) {
        AssetTypeStatistics assetTypeStatistics = new AssetTypeStatistics();
        // 获取需要统计的目录-所有
        final List<AssetsCatalogue> assetsCatalogues = assetsCatalogueMapper.selectListInPath(Lists.newArrayList(catalogId));
        // 获取所有二级目录，根据二级目录，对所有需要统计的目录进行分组
        final LambdaQueryWrapper<AssetsCatalogue> wq = Wrappers.<AssetsCatalogue>lambdaQuery()
                .eq(AssetsCatalogue::getParentId, catalogId)
                .eq(AssetsCatalogue::getDeleted, 0);
        final List<AssetsCatalogue> parentList = assetsCatalogueMapper.selectList(wq);
        Map<Integer, List<AssetsCatalogue>> map = new HashMap<Integer, List<AssetsCatalogue>>();
        parentList.forEach(asset -> {
            final List<AssetsCatalogue> catalogues = assetsCatalogues.stream()
                    .filter(cata -> cata.getPath().contains(asset.getPath()))
                    .collect(Collectors.toList());

            map.put(asset.getId(), catalogues);
        });

        AssetType filterType = null;
        if (type.equals(2)) {
            filterType = AssetType.MODEL;
        } else if (type.equals(3)) {
            filterType = AssetType.TABLE;
        } else if (type.equals(4)) {
            filterType = AssetType.FILE;
        }


        // 获取资产总数
        final List<Integer> catalogueIds = assetsCatalogues.stream().map(AssetsCatalogue::getId).collect(Collectors.toList());
        final LambdaQueryWrapper<TgAssetInfo> eq = Wrappers.<TgAssetInfo>lambdaQuery()
                .in(TgAssetInfo::getAssetMenuId, catalogueIds)
                .eq(TgAssetInfo::getShelfState, "已上架")
                .eq(Objects.nonNull(filterType), TgAssetInfo::getType, filterType)
                .eq(TgAssetInfo::getDeleted, 0);
        final List<TgAssetInfo> tgAssetInfos = tgAssetInfoMapper.selectList(eq);

        // 资产总量
        assetTypeStatistics.setAssetCount(tgAssetInfos.size());

        // 二级目录排序
        final List<AssetsCatalogue> catalogues = parentList.stream()
                .sorted(Comparator.comparing(AssetsCatalogue::getSortOrder))
                .collect(Collectors.toList());

        // 设置横坐标坐标轴
        final List<String> labels = catalogues.stream().map(AssetsCatalogue::getName).collect(Collectors.toList());
        assetTypeStatistics.setLabels(labels);

        // 设置纵坐标内容
        Map<String, List<Integer>> dataResult = new HashMap<String, List<Integer>>();
        catalogues.stream()
                .forEach(assetsCatalogue -> {
                    // 获取所有当前二级目录相关的子目录(含自己)
                    final List<Integer> allDir = map.get(assetsCatalogue.getId())
                            .stream().map(AssetsCatalogue::getId).collect(Collectors.toList());

                    if (type.equals(1)) {
                        // 全部 - 统计【模型】【库表】【文件】

                        // 获取当前循环需要统计的资产
                        final Map<AssetType, List<TgAssetInfo>> typeMap = tgAssetInfos.stream()
                                .filter(asset -> allDir.contains(asset.getAssetMenuId()))
                                .collect(Collectors.groupingBy(TgAssetInfo::getType));

                        // 统计并填充
                        Arrays.stream(AssetType.values()).forEach(assetType -> {
                            final List<TgAssetInfo> assetInfos = typeMap.get(assetType);
                            Integer count = 0;
                            if (CollUtil.isNotEmpty(assetInfos)) {
                                count = assetInfos.size();
                            }
                            List<Integer> list = dataResult.get(assetType.getType());
                            if (CollUtil.isEmpty(list)) {
                                list = new ArrayList<Integer>();
                            }
                            list.add(count);
                            dataResult.put(assetType.getType(), list);
                        });

                    } else if (type.equals(2)) {
                        // 模型 - 统计 【宽表】【常规】【通用】 TemplateTypeEnum

                        // 获取当前循环需要统计的资产
                        final Map<String, List<TgAssetInfo>> typeMap = tgAssetInfos.stream()
                                .filter(asset -> allDir.contains(asset.getAssetMenuId()))
                                .filter(asset -> asset.getType().equals(AssetType.MODEL))
                                .collect(Collectors.groupingBy(TgAssetInfo::getAssetBindingDataType));

                        Arrays.stream(TemplateTypeEnum.values()).forEach(assetType -> {
                            final List<TgAssetInfo> assetInfos = typeMap.get(assetType.name());
                            Integer count = 0;
                            if (CollUtil.isNotEmpty(assetInfos)) {
                                count = assetInfos.size();
                            }
                            List<Integer> list = dataResult.get(assetType.getDesc());
                            if (CollUtil.isEmpty(list)) {
                                list = new ArrayList<Integer>();
                            }
                            list.add(count);
                            dataResult.put(assetType.getDesc(), list);
                        });

                    } else if (type.equals(3)) {
                        // 库表 - 统计 【表】【元数据】

                        // 获取当前循环需要统计的资产
                        final Map<String, List<TgAssetInfo>> typeMap = tgAssetInfos.stream()
                                .filter(asset -> allDir.contains(asset.getAssetMenuId()))
                                .filter(asset -> asset.getType().equals(AssetType.TABLE))
                                .collect(Collectors.groupingBy(TgAssetInfo::getAssetBindingDataType));

                        // 统计并填充
                        // 表
                        final List<TgAssetInfo> assetInfos = typeMap.get(CommonConstants.TABLE_TYPE);
                        Integer tableCount = 0;
                        if (CollUtil.isNotEmpty(assetInfos)) {
                            tableCount = assetInfos.size();
                        }

                        List<Integer> tableList = dataResult.get(CommonConstants.TABLE_TYPE);
                        if (CollUtil.isEmpty(tableList)) {
                            tableList = new ArrayList<Integer>();
                        }
                        tableList.add(tableCount);

                        dataResult.put(CommonConstants.TABLE_TYPE, tableList);

                        // 元数据
                        final List<TgAssetInfo> assetInfos1 = typeMap.get(CommonConstants.META_TYPE);

                        Integer metaCount = 0;
                        if (CollUtil.isNotEmpty(assetInfos1)) {
                            metaCount = assetInfos1.size();
                        }

                        List<Integer> metaList = dataResult.get(CommonConstants.META_TYPE);
                        if (CollUtil.isEmpty(metaList)) {
                            metaList = new ArrayList<Integer>();
                        }
                        metaList.add(metaCount);

                        dataResult.put(CommonConstants.META_TYPE, metaList);

                    } else {
                        // 文档 - 统计 【pdf】【execl】【word】.... FileTypeEnum

                        // 获取当前循环需要统计的资产
                        final Map<String, List<TgAssetInfo>> typeMap = tgAssetInfos.stream()
                                .filter(asset -> allDir.contains(asset.getAssetMenuId()))
                                .filter(asset -> asset.getType().equals(AssetType.FILE))
                                .collect(Collectors.groupingBy(a -> FileTypeEnum.getAssetType(a.getAssetBindingDataType())));

                        Arrays.stream(FileTypeEnum.values()).forEach(fileType -> {
                            final List<TgAssetInfo> assetInfos = typeMap.get(fileType.getDesc());
                            Integer count = 0;
                            if (CollUtil.isNotEmpty(assetInfos)) {
                                count = assetInfos.size();
                            }
                            List<Integer> list = dataResult.get(fileType.getDesc());
                            if (CollUtil.isEmpty(list)) {
                                list = new ArrayList<Integer>();
                            }
                            list.add(count);
                            dataResult.put(fileType.getDesc(), list);
                        });

                    }
                });

        final List<AssetTypeData> typeData = dataResult.entrySet()
                .stream()
                .map(res -> {
                    final AssetTypeData assetTypeData = new AssetTypeData();
                    assetTypeData.setName(res.getKey());
                    assetTypeData.setData(res.getValue());
                    return assetTypeData;
                }).collect(Collectors.toList());

        assetTypeStatistics.setTypeData(typeData);

        return AjaxResult.success(assetTypeStatistics);
    }


    private List<Integer> getAllChild(Integer catalogId) {
        List<Integer> catalogueIds = new ArrayList<Integer>();
        if (Objects.nonNull(catalogId)) {
            final LambdaQueryWrapper<AssetsCatalogue> wq = Wrappers.<AssetsCatalogue>lambdaQuery()
                    .like(AssetsCatalogue::getPath, "/" + catalogId + "/");
            catalogueIds = assetsCatalogueMapper.selectList(wq)
                    .stream()
                    .map(AssetsCatalogue::getId)
                    .collect(Collectors.toList());
        }
        return catalogueIds;
    }

}
