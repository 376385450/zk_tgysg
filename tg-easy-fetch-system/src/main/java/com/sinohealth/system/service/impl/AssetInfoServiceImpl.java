package com.sinohealth.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.sinohealth.common.constant.AssetConstants;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.*;
import com.sinohealth.common.enums.dataassets.OwningBusinessLineEnum;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.ipaas.model.ResMaindatamainDepartmentselectbyidsItemDataItem;
import com.sinohealth.system.dao.AssetsCataloguePermissionDAO;
import com.sinohealth.system.dao.TgAssetAuthWhiltlistInfoDAO;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.domain.catalogue.AssetsCatalogue;
import com.sinohealth.system.domain.catalogue.AssetsCataloguePermission;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.label.TgLabelInfo;
import com.sinohealth.system.dto.assets.AssetDetail;
import com.sinohealth.system.dto.assets.AssetStatistics;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.*;
import com.sinohealth.system.service.AssetInfoService;
import com.sinohealth.system.service.IAssetService;
import com.sinohealth.system.service.ILabelService;
import com.sinohealth.system.service.ITableInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author shallwetalk
 * @Date 2023/8/26
 */
@Service
@Slf4j
public class AssetInfoServiceImpl implements AssetInfoService {


    @Autowired
    private TgAssetInfoMapper tgAssetInfoMapper;

    @Autowired
    private AssetsCatalogueMapper assetsCatalogueMapper;

    @Autowired
    private AssetUserRelationMapper assetUserRelationMapper;

    @Autowired
    private TgApplicationInfoMapper tgApplicationInfoMapper;

    @Autowired
    private ILabelService labelService;

    @Autowired
    private AssetLinkMapper assetLinkMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private TgLabelInfoMapper tgLabelInfoMapper;

    @Autowired
    private TgMetadataInfoMapper tgMetadataInfoMapper;

    @Autowired
    private AssetsCataloguePermissionDAO assetsCataloguePermissionDAO;

    @Autowired
    private ITableInfoService tableInfoService;

    @Autowired
    private IAssetService assetService;

    @Autowired
    TgAssetAuthWhiltlistInfoDAO tgAssetAuthWhiltlistInfoDAO;


    @Override
    public AjaxResult<AssetDetail> detail(Long id) {

        final AssetDetail assetDetail = new AssetDetail();

        if (id == null) {
            return AjaxResult.error(AssetConstants.INVALID_ID);
        }

        final TgAssetInfo tgAssetInfo = tgAssetInfoMapper.selectById(id);

        if (id == null) {
            return AjaxResult.error(AssetConstants.NOT_EXIST_ASSET);
        }

        if (Objects.isNull(tgAssetInfo)) {
            return AjaxResult.error(AssetConstants.ASSET_DELETED);
        }

        if (tgAssetInfo.getShelfState().equals("未上架")) {
            return AjaxResult.error(AssetConstants.ASSET_OFF);
        }

        // 获取当前用户的 DEPT 和 USER 信息
        final Long userId = SecurityUtils.getUserId();
        final SinoPassUserDTO o = (SinoPassUserDTO) ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO);
        String deptId = o.getMainOrganizationId();
        // 获取拥有权限的所有目录
        final List<AssetsCataloguePermission> allPermission = assetsCataloguePermissionDAO.findByUserIdAndDeptId(userId, deptId);


        // 当前资产挂接的目录
        final AssetsCatalogue assetsCatalogue = assetsCatalogueMapper.selectById(tgAssetInfo.getAssetMenuId());

        if (tgAssetInfo.getIsFollowAssetMenuReadableRange().equals(AuthItemEnum.FOLLOW_DIR_AUTH)) {
            // 当前资产是跟随目录类型
            // 查看是否有阅读权限
            final List<Integer> readableCatalogue = allPermission.stream().filter(a -> a.getReadable().equals(1))
                    .map(AssetsCataloguePermission::getCatalogueId).collect(Collectors.toList());

            if (!readableCatalogue.stream().anyMatch(a -> assetsCatalogue.getPath().contains("/" + a + "/"))) {
                // 不存在阅读
                assetDetail.setReadable(false);
                return AjaxResult.success(assetDetail);
            }
        } else {
            // 当前资产是自定义阅读权限
            final List<TgAssetWhitelistInfo> infos = tgAssetAuthWhiltlistInfoDAO.findWhiteListInfoByUserIdAndDeptId(userId, o.getMainOrganizationId())
                    .stream().filter(info -> info.getServiceType().equals(WhitlistServiceType.READABLE))
                    .collect(Collectors.toList());
            if (!infos.stream().map(TgAssetWhitelistInfo::getAssetId).anyMatch(a -> a.equals(id))) {
                // 不存在阅读
                assetDetail.setReadable(false);
                return AjaxResult.success(assetDetail);
            }
        }

        assetDetail.setReadable(true);


        BeanUtils.copyProperties(tgAssetInfo, assetDetail);

        assetDetail.setAssetMenu(assetsCatalogue.getName());

        final List<ResMaindatamainDepartmentselectbyidsItemDataItem> result = SinoipaasUtils.mainDepartmentSelectbyids(Lists.newArrayList(tgAssetInfo.getAssetProvider()));
        if (CollUtil.isNotEmpty(result)) {
            assetDetail.setAssetProvider(result.get(0).getDepartName());
        } else {
            assetDetail.setAssetProvider("");
        }

        // 前端说id要使用字符串
        final String assetManagerJson = tgAssetInfo.getAssetManagerJson();
        if (StringUtils.isNotEmpty(assetManagerJson)) {
            final List<String> assetManager = JSONArray.parseArray(assetManagerJson, String.class);
            List<Long> transformAssetManager = Lists.transform(assetManager, Long::parseLong);
            if (CollUtil.isNotEmpty(transformAssetManager)) {
                final List<SysUser> users = sysUserMapper.selectUserByIds(transformAssetManager);
                assetDetail.setAssetManagerName(users.stream().map(SysUser::getRealName).collect(Collectors.joining(",")));
                assetDetail.setCurrentUserManageAsset(assetManager.contains(SecurityUtils.getUserId()));
            } else {
                assetDetail.setAssetManagerName("");
                assetDetail.setCurrentUserManageAsset(false);
            }
        } else {
            assetDetail.setAssetManagerName("");
            assetDetail.setCurrentUserManageAsset(false);
        }

        final Map<Long, List<TgLabelInfo>> fullLabels = labelService.getFullLabels(Lists.newArrayList(id));
        final List<TgLabelInfo> tgLabelInfos1 = fullLabels.get(id);
        if (CollUtil.isNotEmpty(tgLabelInfos1)) {
            assetDetail.setAssetLabel(tgLabelInfos1.stream().map(TgLabelInfo::getName).collect(Collectors.toList()));
        } else {
            assetDetail.setAssetLabel(Collections.emptyList());
        }


        // 资源信息
        final AssetType type = tgAssetInfo.getType();
        assetDetail.setAssetType(type.getName());

        if (type.equals(AssetType.TABLE)) {
            ResourceType resourceType = tgAssetInfo.getResourceType();
            if (resourceType != null && tgAssetInfo.getResourceType().equals(ResourceType.METADATA_MANAGEMENT)) {
                TgMetadataInfo tgMeataDataInfo = TgMetadataInfo.newInstance().selectOne(new QueryWrapper<TgMetadataInfo>() {{
                    eq("asset_id", id);
                }});
                if (tgMeataDataInfo != null) {
                    assetDetail.setDbType(tgMeataDataInfo.getDatabaseType());
                    assetDetail.setAssetHang(tgMeataDataInfo.getMetaDataDatabase());
                    assetDetail.setAssetTable(tgMeataDataInfo.getMetaDataTable());
                    assetDetail.setMetadataType(true);
                    assetDetail.setMetadataId(tgMeataDataInfo.getMetaDataId());
                }
            }
            if (resourceType != null && resourceType.equals(ResourceType.TABLE_MANAGEMENT)) {
                TableInfo tableInfo = tableInfoService.getById(tgAssetInfo.getRelatedId());
                assetDetail.setAssetTable(tableInfo.getTableName());
                assetDetail.setTableId(tableInfo.getId());
                assetDetail.setMetadataType(false);
            }

        } else if (type.equals(AssetType.MODEL)) {
            TgTemplateInfo tgTemplateInfo = TgTemplateInfo.newInstance().selectById(tgAssetInfo.getRelatedId());
            assetDetail.setModuleName(tgTemplateInfo.getName());
            assetDetail.setBaseTable(tgTemplateInfo.getBaseTableName());
            assetDetail.setTableId(tgTemplateInfo.getBaseTableId());
            final String typeName = OwningBusinessLineEnum.getNameByValue(tgTemplateInfo.getBizType());
            assetDetail.setOwningBusinessLine(typeName);
        } else if (type.equals(AssetType.FILE)) {
            TgDocInfo tgDocInfo = TgDocInfo.newInstance().selectById(tgAssetInfo.getRelatedId());
            assetDetail.setAssetHang(tgDocInfo.getName());
            assetDetail.setAssetFileType(tgDocInfo.getType());
        }


        // 资产使用情况
        final LambdaQueryWrapper<TgAssetUserRelation> wq = Wrappers.<TgAssetUserRelation>lambdaQuery()
                .eq(TgAssetUserRelation::getAssetId, id);
        final List<TgAssetUserRelation> tgAssetUserRelations = assetUserRelationMapper.selectList(wq);

        // 浏览次数
        assetDetail.setViewCount(tgAssetUserRelations.stream().mapToInt(TgAssetUserRelation::getViewNum).sum());

        // 收藏量
        assetDetail.setCollectionCount((int) tgAssetUserRelations.stream().filter(t -> t.getIsCollect().equals(1)).count());

        // 转发量
        assetDetail.setShareCount(tgAssetUserRelations.stream().mapToInt(TgAssetUserRelation::getForwardNum).sum());

        // 服务申请次数
        final LambdaQueryWrapper<TgApplicationInfo> applicationWrapper = Wrappers.<TgApplicationInfo>lambdaQuery().eq(TgApplicationInfo::getNewAssetId, id);
        assetDetail.setServiceCount(tgApplicationInfoMapper.selectCount(applicationWrapper));


        // 是否拥有资产管理权限
        final String path = assetsCatalogue.getPath();
        final List<Integer> permission = allPermission.stream().filter(asset -> asset.getAssetsManager().equals(1)).map(AssetsCataloguePermission::getCatalogueId).collect(Collectors.toList());
        assetDetail.setCurrentUserAssetManagerPermissions(permission.stream().anyMatch(a -> path.contains("/" + a + "/")));

        assetDetail.setPermission(assetService.computePermissions(tgAssetInfo, userId, deptId, true));

        // 获取用户关联关系
        assetDetail.setTgAssetRelate(assetService.buildRelate(id));

        //设置是否申请标识
        Integer currentUserApplyCount = assetService.computeCurrentUserApplyCount(id, userId);
        assetDetail.setHasApplied(currentUserApplyCount > 0);

        // 设置资产关联链接
        final LambdaQueryWrapper<AssetLink> eq = Wrappers.<AssetLink>lambdaQuery()
                .eq(AssetLink::getAssetId, id)
                .eq(AssetLink::getLinkType, AssetLinkTypeEnum.ASSET_LINK.getType());
        final List<AssetLink> assetLinks = assetLinkMapper.selectList(eq);
        assetDetail.setAssetLinks(assetLinks);

        return AjaxResult.success(assetDetail);
    }

    @Override
    public AjaxResult<AssetStatistics> statistics(Long id) {
        final LambdaQueryWrapper<TgAssetUserRelation> wq = Wrappers.<TgAssetUserRelation>lambdaQuery()
                .eq(TgAssetUserRelation::getAssetId, id);
        final List<TgAssetUserRelation> tgAssetUserRelations = assetUserRelationMapper.selectList(wq);
        final AssetStatistics assetStatistics = new AssetStatistics();

        assetStatistics.setCurrentUserIsCollection(tgAssetUserRelations.stream().anyMatch(
                a -> a.getUserId().equals(SecurityUtils.getUserId()) && a.getIsCollect().equals(1)));

        assetStatistics.setCollectionCount(tgAssetUserRelations.stream()
                .filter(a -> a.getIsCollect().equals(1))
                .collect(Collectors.toList()).size());

        assetStatistics.setViewNum(tgAssetUserRelations.stream().mapToInt(TgAssetUserRelation::getViewNum).sum());

        assetStatistics.setShareCount(tgAssetUserRelations.stream().mapToInt(TgAssetUserRelation::getForwardNum).sum());

        return AjaxResult.success(assetStatistics);
    }

    @Override
    public List<AssetStatistics> statistics(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }

        final LambdaQueryWrapper<TgAssetUserRelation> wq = Wrappers.<TgAssetUserRelation>lambdaQuery()
                .in(TgAssetUserRelation::getAssetId, ids);
        final Map<Long, List<TgAssetUserRelation>> tgAssetUserRelations = assetUserRelationMapper.selectList(wq)
                .stream().collect(Collectors.groupingBy(TgAssetUserRelation::getAssetId));

        final LambdaQueryWrapper<TgApplicationInfo> applicationWrapper = Wrappers.<TgApplicationInfo>lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getNewAssetId, TgApplicationInfo::getCurrentAuditProcessStatus)
                .in(TgApplicationInfo::getNewAssetId, ids);

        final Map<Long, List<TgApplicationInfo>> applicationInfoMap = tgApplicationInfoMapper.selectList(applicationWrapper)
                .stream().collect(Collectors.groupingBy(TgApplicationInfo::getNewAssetId));

        List<AssetStatistics> list = new ArrayList<>();
        for (Long id : ids) {
            final AssetStatistics assetStatistics = new AssetStatistics();

            assetStatistics.setAssetId(id);

            // 服务申请次数
            final List<TgApplicationInfo> tgApplicationInfos = applicationInfoMap.get(id);
            assetStatistics.setServiceCount(CollUtil.isNotEmpty(tgApplicationInfos) ? tgApplicationInfos.size() : 0);

            // 申请成功次数
            if (CollUtil.isNotEmpty(tgApplicationInfos)) {
                final List<TgApplicationInfo> collect = tgApplicationInfos.stream()
                        .filter(a -> a.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.AUDIT_PASS))
                        .collect(Collectors.toList());
                assetStatistics.setServiceSuccessCount(CollUtil.isNotEmpty(collect) ? collect.size() : 0);
            } else {
                assetStatistics.setServiceSuccessCount(0);
            }


            final List<TgAssetUserRelation> tgAssetUserRela = tgAssetUserRelations.get(id);
            if (CollUtil.isNotEmpty(tgAssetUserRela)) {
                assetStatistics.setCurrentUserIsCollection(tgAssetUserRela.stream().anyMatch(
                        a -> a.getUserId().equals(SecurityUtils.getUserId()) && a.getIsCollect().equals(1)));

                assetStatistics.setCollectionCount((int) tgAssetUserRela.stream()
                        .filter(a -> a.getIsCollect().equals(1)).count());

                assetStatistics.setViewNum(tgAssetUserRela.stream().mapToInt(TgAssetUserRelation::getViewNum).sum());
                assetStatistics.setShareCount(tgAssetUserRela.stream().mapToInt(TgAssetUserRelation::getForwardNum).sum());
            } else {
                assetStatistics.setCurrentUserIsCollection(false);
                assetStatistics.setCollectionCount(0);
                assetStatistics.setViewNum(0);
                assetStatistics.setShareCount(0);
            }

            list.add(assetStatistics);
        }

        return list;
    }

    @Override
    public TgAssetInfo getById(Long id) {
        return tgAssetInfoMapper.selectById(id);
    }
}
