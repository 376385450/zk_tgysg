package com.sinohealth.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.AssetConstants;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.enums.AssetLinkTypeEnum;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.AuthItemEnum;
import com.sinohealth.common.enums.IsCollectEnum;
import com.sinohealth.common.enums.ResourceType;
import com.sinohealth.common.enums.ShelfState;
import com.sinohealth.common.enums.StaffType;
import com.sinohealth.common.enums.WhitlistServiceType;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dataassets.ModuleAssetBindTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.data.intelligence.api.Result;
import com.sinohealth.data.intelligence.api.metadata.dto.MetaTableDTO;
import com.sinohealth.data.intelligence.api.metadata.param.MetaTableGetParam;
import com.sinohealth.ipaas.model.ResMaindataMainDepartmentSelectUserWithDeptItemDataItem;
import com.sinohealth.ipaas.model.ResMaindatamainDepartmentselectbyidsItemDataItem;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.assets.dto.TableAssetsListVO;
import com.sinohealth.system.biz.dataassets.dto.AssetValidateDTO;
import com.sinohealth.system.biz.dict.constant.KeyDictType;
import com.sinohealth.system.biz.dict.dao.KeyValDictDAO;
import com.sinohealth.system.biz.dict.domain.KeyValDict;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.biz.project.mapper.ProjectMapper;
import com.sinohealth.system.client.MetadataClient;
import com.sinohealth.system.dao.AssetsCataloguePermissionDAO;
import com.sinohealth.system.dao.TgAssetAuthWhiltlistInfoDAO;
import com.sinohealth.system.domain.AssetLink;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgAssetBurialPoint;
import com.sinohealth.system.domain.TgAssetDocBindingInfo;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgAssetInfoSimpleDTO;
import com.sinohealth.system.domain.TgAssetStaffParam;
import com.sinohealth.system.domain.TgAssetTableBindingInfo;
import com.sinohealth.system.domain.TgAssetTemplateBindingInfo;
import com.sinohealth.system.domain.TgAssetUserRelation;
import com.sinohealth.system.domain.TgAssetWhitelistInfo;
import com.sinohealth.system.domain.TgAuditProcessInfo;
import com.sinohealth.system.domain.TgDocInfo;
import com.sinohealth.system.domain.TgLastApplicationInfo;
import com.sinohealth.system.domain.TgMetadataInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.asset.RelateAssetInfo;
import com.sinohealth.system.domain.asset.TgAssetReadableResp;
import com.sinohealth.system.domain.asset.TgAssetRelate;
import com.sinohealth.system.domain.asset.TgAssetRelateParam;
import com.sinohealth.system.domain.asset.TgAssetRelateResp;
import com.sinohealth.system.domain.catalogue.AssetsCatalogue;
import com.sinohealth.system.domain.catalogue.AssetsCataloguePermission;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.RequireAttrType;
import com.sinohealth.system.domain.converter.AssetBeanConverter;
import com.sinohealth.system.domain.converter.AssetUserRelationBeanConverter;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.domain.customer.Customer;
import com.sinohealth.system.domain.label.TgLabelInfo;
import com.sinohealth.system.dto.GuideDTO;
import com.sinohealth.system.dto.api.cataloguemanageapi.CatalogueDetailDTO;
import com.sinohealth.system.dto.api.cataloguemanageapi.UserDTO;
import com.sinohealth.system.dto.assets.AssetApplicationPageQuery;
import com.sinohealth.system.dto.assets.AssetBackendPageQuery;
import com.sinohealth.system.dto.assets.AssetFrontendPageQuery;
import com.sinohealth.system.dto.assets.AssetIndicatorDTO;
import com.sinohealth.system.dto.assets.AssetIndicatorQuery;
import com.sinohealth.system.dto.assets.AssetStatistics;
import com.sinohealth.system.dto.assets.ChangeSortDTO;
import com.sinohealth.system.dto.assets.CollectAssetRequest;
import com.sinohealth.system.dto.assets.CollectListRequest;
import com.sinohealth.system.dto.assets.ForwardAssetRequest;
import com.sinohealth.system.dto.assets.JudgeViewableRequest;
import com.sinohealth.system.dto.assets.LastAssetQuery;
import com.sinohealth.system.dto.assets.TgAssetFrontTreeQueryResult;
import com.sinohealth.system.dto.assets.TgAssetMyApplicationPageResult;
import com.sinohealth.system.dto.auditprocess.ProcessNodeEasyDto;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.AssetLinkMapper;
import com.sinohealth.system.mapper.AssetUserRelationMapper;
import com.sinohealth.system.mapper.AssetsCatalogueMapper;
import com.sinohealth.system.mapper.CustomerMapper;
import com.sinohealth.system.mapper.SysUserMapper;
import com.sinohealth.system.mapper.TableInfoMapper;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgAssetBurialPointMapper;
import com.sinohealth.system.mapper.TgAssetInfoMapper;
import com.sinohealth.system.mapper.TgAssetRelateMapper;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import com.sinohealth.system.service.AssetInfoService;
import com.sinohealth.system.service.DataAssetsCatalogueService;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.IAssetService;
import com.sinohealth.system.service.IDocService;
import com.sinohealth.system.service.ILabelService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.ITemplateService;
import com.sinohealth.system.service.IntergrateProcessDefService;
import com.sinohealth.system.util.DeptUtil;
import com.sinohealth.system.vo.CollectListVo;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @Author Rudolph
 * @Date 2023-08-08 9:54
 * @Desc
 */
@Service
public class AssetServiceImpl implements IAssetService {

    @Autowired
    private IDocService docService;

    @Autowired
    private ITableInfoService tableInfoService;

    @Autowired
    private TgAssetBurialPointMapper tgAssetBurialPointMapper;

    @Autowired
    private ITemplateService templateService;

    @Autowired
    private MetadataClient metadataClient;

    @Autowired
    private TgAssetRelateMapper tgAssetRelateMapper;
    @Autowired
    private TgApplicationInfoMapper tgApplicationInfoMapper;

    @Autowired
    private DataAssetsCatalogueService catalogueService;

    @Autowired
    private AssetsCatalogueMapper assetsCatalogueMapper;
    @Autowired
    private TgAssetInfoMapper tgAssetInfoMapper;
    @Autowired
    private KeyValDictDAO keyValDictDAO;
    @Autowired
    private TableInfoMapper tableInfoMapper;
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private AssetsCataloguePermissionDAO assetsCataloguePermissionDAO;

    @Autowired
    private AssetUserRelationMapper assetUserRelationMapper;

    @Autowired
    private DataAssetsCatalogueService dataAssetsCatalogueService;

    @Autowired
    private AssetInfoService assetInfoService;

    @Autowired
    private TgAssetAuthWhiltlistInfoDAO whiltlistInfoDAO;

    @Autowired
    private IApplicationService applicationService;

    @Resource
    private ILabelService labelService;

    @Autowired
    private TgApplicationInfoMapper applicationInfoMapper;
    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private TgTemplateInfoMapper tgTemplateInfoMapper;

    @Autowired
    private IAssetService assetService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private AssetLinkMapper assetLinkMapper;

    @Autowired
    private IntergrateProcessDefService intergrateProcessDefService;

    @Autowired
    private AppProperties appProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAsset(TgAssetInfo assetInfo) {
        // 判断是否在同一个一级目录下进行
        removeAssetRelateWhenChangePath(assetInfo);
        JsonBeanConverter.convert2Json(assetInfo);
        if (assetInfo.getId() == null) {
            Long selectCount = getMaxValue(assetInfo.getType());
            // 100 步长排序值填充
            assetInfo.setAssetSort(selectCount + 1);
        }
        assetInfo.setUpdater(ThreadContextHolder.getSysUser().getRealName());
        assetInfo.setUpdateTime(DateUtils.getTime());
        assetInfo.insertOrUpdate();
        addAssetAuthInfo(assetInfo);

        // 更新标签关联表
        labelService.updateLabelRelation(assetInfo);

        // 先删除，再新增关联关系
        deleteAllAssetRelated(assetInfo);
        addAssetRelate(assetInfo);

        // 更新关联链接
        final List<AssetLink> assetLinks = assetInfo.getAssetLinks();
        deleteAllAssetLink(assetInfo);
        addAssetLink(assetInfo);


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAssetRelateWhenChangePath(TgAssetInfo assetInfo) {
        if (Objects.nonNull(assetInfo.getId())) {
            // 修改才需要进行判断
            final TgAssetInfo tgAssetInfo = tgAssetInfoMapper.selectById(assetInfo.getId());
            final Integer oldMenu = tgAssetInfo.getAssetMenuId();
            final Integer newMenu = assetInfo.getAssetMenuId();
            //目录id不一致才需要进行判断
            if (!oldMenu.equals(newMenu)) {
                // 判断是否是同一个顶级目录
                if (!catalogueService.isSameTopCatalog(newMenu, oldMenu)) {
                    // 非同一个顶级目录，删除所有关联关系
                    final LambdaQueryWrapper<TgAssetRelate> wq = Wrappers.<TgAssetRelate>lambdaQuery()
                            .eq(TgAssetRelate::getRelateAssetId, assetInfo.getId());
                    tgAssetRelateMapper.delete(wq);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAssetRelate(TgAssetInfo assetInfo) {
        final List<RelateAssetInfo> tgAssetRelate = assetInfo.getAssetInfos();
        if (CollUtil.isNotEmpty(tgAssetRelate)) {
            AtomicInteger count = new AtomicInteger(0);
            final List<TgAssetRelate> relateList = tgAssetRelate.stream()
                    .map(related -> {
                        final TgAssetRelate relate = new TgAssetRelate();
                        relate.setAssetId(assetInfo.getId());
                        relate.setRelateAssetId(related.getAssetId());
                        relate.setRelateSort(count.getAndIncrement());
                        return relate;
                    }).collect(Collectors.toList());
            for (TgAssetRelate assetRelate : relateList) {
                tgAssetRelateMapper.insert(assetRelate);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllAssetRelated(TgAssetInfo assetInfo) {
        final Long id = assetInfo.getId();
        final LambdaQueryWrapper<TgAssetRelate> wq = Wrappers.<TgAssetRelate>lambdaQuery()
                .eq(TgAssetRelate::getAssetId, id);
        tgAssetRelateMapper.delete(wq);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAllAssetLink(TgAssetInfo assetInfo) {
        final Long id = assetInfo.getId();
        final LambdaQueryWrapper<AssetLink> wq = Wrappers.<AssetLink>lambdaQuery()
                .eq(AssetLink::getAssetId, id);
        assetLinkMapper.delete(wq);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addAssetLink(TgAssetInfo assetInfo) {
        final Long id = assetInfo.getId();
        // 资产关联链接
        final List<AssetLink> assetLinks = assetInfo.getAssetLinks();

        if (CollUtil.isNotEmpty(assetLinks)) {
            for (AssetLink assetLink : assetLinks) {
                assetLink.setLinkType(AssetLinkTypeEnum.ASSET_LINK.getType());
                assetLink.setAssetId(id);
                assetLinkMapper.insert(assetLink);
            }
        }

        final List<AssetLink> guideLinks = assetInfo.getGuideLinks();

        if (CollUtil.isNotEmpty(guideLinks)) {
            for (AssetLink guideLink : guideLinks) {
                guideLink.setLinkType(AssetLinkTypeEnum.GUIDE_LINK.getType());
                guideLink.setAssetId(id);
                assetLinkMapper.insert(guideLink);
            }
        }

    }


    @Override
    public AjaxResult getAllAsset() {
        final List<TgAssetFrontTreeQueryResult> tgAssetFrontTreeQueryResults = allReadableAsset();
        final List<TgAssetReadableResp> collect = tgAssetFrontTreeQueryResults.stream()
                .map(a -> new TgAssetReadableResp() {{
                    setAssetName(a.getAssetName());
                    setId(a.getId());
                }}).collect(Collectors.toList());
        return AjaxResult.success(collect);
    }

    @Override
    public AjaxResult<TgAssetRelateResp> relatableAsset(Integer assetId, Integer catalogId, Integer lastCatalogId) {
        final TgAssetRelateResp resp = new TgAssetRelateResp();
        // 判断是否是同一个顶级目录
        if (Objects.nonNull(lastCatalogId)) {
            resp.setIsChangeTopCatalog(catalogueService.isSameTopCatalog(catalogId, lastCatalogId));
        }

        final CatalogueDetailDTO info = catalogueService.getCatalogueBaseInfo(catalogId);
        final Integer catalog = Arrays.stream(info.getPath().split("/")).filter(StringUtils::isNotBlank).findFirst().map(Integer::parseInt).get();
        final List<AssetsCatalogue> menuIdsByMenuRootIds = catalogueService.getMenuIdsByMenuRootIds(Lists.newArrayList(catalog));
        final List<Integer> catalogueIds = menuIdsByMenuRootIds.stream().map(AssetsCatalogue::getId).collect(Collectors.toList());
        final LambdaQueryWrapper<TgAssetInfo> wq = Wrappers.<TgAssetInfo>lambdaQuery()
                .ne(Objects.nonNull(assetId), TgAssetInfo::getId, assetId)
                .in(TgAssetInfo::getAssetMenuId, catalogueIds)
                .eq(TgAssetInfo::getShelfState, "已上架")
                .eq(TgAssetInfo::getDeleted, 0);
        final List<TgAssetInfo> assetInfos = tgAssetInfoMapper.selectList(wq);
        final List<TgAssetRelateParam> list = assetInfos.stream()
                .collect(Collectors.groupingBy(TgAssetInfo::getType))
                .entrySet()
                .stream()
                .map(entry -> {
                    final TgAssetRelateParam tgAssetRelateParam = new TgAssetRelateParam();
                    tgAssetRelateParam.setTypeName(entry.getKey().getName());

                    final List<RelateAssetInfo> infos = entry.getValue().stream()
                            .map(asset -> {
                                final RelateAssetInfo assetInfo = new RelateAssetInfo();
                                assetInfo.setAssetName(asset.getAssetName());
                                assetInfo.setAssetId(asset.getId());
                                return assetInfo;
                            }).collect(Collectors.toList());

                    tgAssetRelateParam.setChildren(infos);

                    return tgAssetRelateParam;
                }).collect(Collectors.toList());
        resp.setList(list);
        return AjaxResult.success(resp);
    }

    @Override
    public AjaxResult<AssetValidateDTO> validateSort(Long assetId, Integer sortNum) {

        final TgAssetInfo tgAssetInfo = tgAssetInfoMapper.selectById(assetId);

        final AssetType type = tgAssetInfo.getType();

        final LambdaQueryWrapper<TgAssetInfo> wq = Wrappers.<TgAssetInfo>lambdaQuery()
                .eq(TgAssetInfo::getType, type)
                .eq(TgAssetInfo::getDeleted, 0)
                .eq(TgAssetInfo::getAssetSort, sortNum)
                .ne(TgAssetInfo::getId, assetId);

        final TgAssetInfo tgAssetInfo1 = tgAssetInfoMapper.selectOne(wq);

        final AssetValidateDTO assetValidateDTO = new AssetValidateDTO();

        if (Objects.nonNull(tgAssetInfo1)) {
            String msg = "当前排序为%s的数据为【%s】，是否确认顶替？";
            final String format = String.format(msg, sortNum, tgAssetInfo1.getAssetName());
            assetValidateDTO.setMsg(format);
            assetValidateDTO.setRepeat(true);
        } else {
            assetValidateDTO.setRepeat(false);
        }

        return AjaxResult.success(assetValidateDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Boolean> changeSort(ChangeSortDTO changeSortDTO) {
        final Long assetId = changeSortDTO.getAssetId();
        final TgAssetInfo tgAssetInfo = tgAssetInfoMapper.selectById(assetId);

        final Long sortNum = changeSortDTO.getSortNum();
        tgAssetInfo.setAssetSort(sortNum);

        final AssetType type = tgAssetInfo.getType();

        Long maxValue = getMaxValue(type);

        if (maxValue < sortNum) {
            throw new CustomException("当前排序最大值不能超过[" + maxValue + "]");
        }

        final LambdaQueryWrapper<TgAssetInfo> wq = Wrappers.<TgAssetInfo>lambdaQuery()
                .eq(TgAssetInfo::getType, type)
                .ge(TgAssetInfo::getAssetSort, changeSortDTO.getSortNum())
                .ne(TgAssetInfo::getId, changeSortDTO.getAssetId())
                .eq(TgAssetInfo::getDeleted, 0)
                .orderByAsc(TgAssetInfo::getAssetSort);

        final List<TgAssetInfo> assetInfos = tgAssetInfoMapper.selectList(wq);

        // 获取需要变更列表的初始值
        AtomicLong sortIndex = new AtomicLong(sortNum + 1);

        final List<TgAssetInfo> updateList = assetInfos.stream()
                .map(asset -> {
                    asset.setAssetSort(sortIndex.getAndIncrement());
                    return asset;
                }).collect(Collectors.toList());

        updateList.add(tgAssetInfo);

        tgAssetInfoMapper.updateBatch(updateList);

        return AjaxResult.success(true);
    }

    @Override
    public Long getMaxValue(AssetType assetType) {
        final LambdaQueryWrapper<TgAssetInfo> wq = Wrappers.<TgAssetInfo>lambdaQuery()
                .eq(TgAssetInfo::getType, assetType)
                .eq(TgAssetInfo::getDeleted, 0);

        final List<TgAssetInfo> assetInfos = tgAssetInfoMapper.selectList(wq);

        final Optional<Long> max = assetInfos.stream()
                .map(TgAssetInfo::getAssetSort)
                .max(Comparator.comparing(Long::longValue));

        return max.isPresent() ? max.get() : 0L;
    }

    @Override
    public void addAssetAuthInfo(TgAssetInfo assetInfo) {

        TgAssetWhitelistInfo.newInstance().delete(new QueryWrapper<TgAssetWhitelistInfo>() {{
            eq("asset_id", assetInfo.getId());
        }});

        List<TgAssetStaffParam> customAssetReadableWhiteList = assetInfo.getCustomAssetReadableWhiteList();
        customAssetReadableWhiteList.forEach(
                i -> insertAssetAuth(assetInfo, i, WhitlistServiceType.READABLE)
        );

        List<TgAssetStaffParam> serviceWhiteList = assetInfo.getServiceWhiteList();
        serviceWhiteList.forEach(
                i -> insertAssetAuth(assetInfo, i, WhitlistServiceType.SERVICE_AUTH)
        );
    }


    @Override
    public GuideDTO guide(Long assetId) {
        final TgAssetInfo tgAssetInfo = tgAssetInfoMapper.selectById(assetId);
        final GuideDTO guideDTO = new GuideDTO();
        guideDTO.setGuide(tgAssetInfo.getGuide());
        guideDTO.setGuideDesc(tgAssetInfo.getGuideDesc());

        final LambdaQueryWrapper<AssetLink> wq = Wrappers.<AssetLink>lambdaQuery()
                .eq(AssetLink::getAssetId, assetId)
                .eq(AssetLink::getLinkType, AssetLinkTypeEnum.GUIDE_LINK.getType());
        guideDTO.setGuideLinks(assetLinkMapper.selectList(wq));

        return guideDTO;
    }

    @Override
    public void insertAssetAuth(TgAssetInfo assetInfo, TgAssetStaffParam i, WhitlistServiceType type) {
        TgAssetWhitelistInfo tgAssetWhitelistInfo = TgAssetWhitelistInfo.newInstance();
        tgAssetWhitelistInfo.setType(assetInfo.getType());
        tgAssetWhitelistInfo.setRelatedId(assetInfo.getRelatedId());
        if (i.getType().equals(StaffType.DEPT.getId())) {
            tgAssetWhitelistInfo.setStaffType(StaffType.DEPT);
        } else {
            tgAssetWhitelistInfo.setStaffType(StaffType.USER);
        }
        tgAssetWhitelistInfo.setAssetId(assetInfo.getId());
        tgAssetWhitelistInfo.setStaffId(i.getId());
        tgAssetWhitelistInfo.setServiceType(type);
        tgAssetWhitelistInfo.setCreateTime(DateUtils.getTime());
        tgAssetWhitelistInfo.setAssetOpenServicesJson(JsonUtils.format(i.getAssetOpenServices()));
        if (i.getExpirationDate() != null) {
            tgAssetWhitelistInfo.setExpirationDate(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, i.getExpirationDate()));
        }
        tgAssetWhitelistInfo.setCreator(ThreadContextHolder.getSysUser().getRealName());
        tgAssetWhitelistInfo.insert();
    }


    @Override
    public List<TgAssetInfo> queryList(List<Long> relatedIds, AssetType type) {
        QueryWrapper<TgAssetInfo> qw = new QueryWrapper<TgAssetInfo>() {{
            eq("type", type);
            in("related_id", relatedIds);
        }};
        List<TgAssetInfo> tgAssetInfos = TgAssetInfo.newInstance().selectList(qw);
        tgAssetInfos.stream().forEach(JsonBeanConverter::convert2Obj);
        return tgAssetInfos;
    }

    @Override
    public Page<TgAssetInfo> queryPage(AssetBackendPageQuery queryParam) {
        Page<TgAssetInfo> page = new Page<>(queryParam.getPageNum(), queryParam.getPageSize());
        List<Integer> manageableAssetMenuIds = catalogueService.getManageableAssetMenuIds(queryParam.getDirId());
        Page<TgAssetInfo> tgAssetInfoPage = tgAssetInfoMapper.backendQuery(page, manageableAssetMenuIds, queryParam);
        Map<Integer, String> fullMenuNames = catalogueService.getFullMenuNames();
        Map<Long, String> userNameMap = catalogueService.getSelectedUser(0, Integer.MAX_VALUE, null).getRecords()
                .stream().collect(Collectors.toMap(UserDTO::getId, UserDTO::getName));
        for (TgAssetInfo tgAssetInfo : tgAssetInfoPage.getRecords()) {
            JsonBeanConverter.convert2Obj(tgAssetInfo);
            tgAssetInfo.setMenuName(fullMenuNames.get(tgAssetInfo.getAssetMenuId()));
            List<String> assetManager = tgAssetInfo.getAssetManager() != null ? tgAssetInfo.getAssetManager() : new ArrayList<>();
            List<Long> transformAssetManager = Lists.transform(assetManager, Long::parseLong);
            tgAssetInfo.setAssetManagerName(StringUtils.joinNames(transformAssetManager, userNameMap, ","));
        }
        return tgAssetInfoPage;
    }

    @Override
    public TgAssetInfo queryOne(Long relatedId, AssetType type) {
        QueryWrapper<TgAssetInfo> qw = new QueryWrapper<TgAssetInfo>() {{
            eq("type", type);
            eq("related_id", relatedId);
        }};
        TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance().selectOne(qw);
        JsonBeanConverter.convert2Obj(tgAssetInfo);
        return tgAssetInfo;
    }

    @Override
    public TgAssetInfo queryOne(Long id) {
        TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance().selectById(id);
        JsonBeanConverter.convert2Obj(tgAssetInfo);
        List<String> labels = labelService.getLabels(id);
        List<Integer> integerLabelList = Lists.transform(labels, Integer::parseInt);
        tgAssetInfo.setAssetLabels(integerLabelList);
        return tgAssetInfo;
    }

    @Override
    public void delete(Long relatedId, AssetType type) {
        QueryWrapper<TgAssetInfo> qw = new QueryWrapper<TgAssetInfo>() {{
            eq("type", type);
            eq("related_id", relatedId);
        }};
        TgAssetInfo.newInstance().delete(qw);
    }

    @Override
    public AjaxResult<?> delete(Long id) {
        TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance().selectById(id);
        AssetType type = tgAssetInfo.getType();
        if (type.equals(AssetType.MODEL)) {
            Integer relatedAssetCount = TgApplicationInfo.newInstance().selectCount(new QueryWrapper<TgApplicationInfo>() {{
                eq("new_asset_id", id);
            }});
            if (relatedAssetCount > 0) {
                return AjaxResult.error(InfoConstants.UNABLE_DELETE_ASSET);
            }
        }
        if (type.equals(AssetType.TABLE)) {
            Long tableId = tgAssetInfo.getRelatedId();
            Integer relatedAssetCount = TgTemplateInfo.newInstance().selectCount(new QueryWrapper<TgTemplateInfo>() {{
                eq("base_table_id", tableId);
            }});
            if (relatedAssetCount > 0) {
                return AjaxResult.error(InfoConstants.UNABLE_DELETE_ASSET);
            }
        }
        TgAssetInfo.newInstance().deleteById(id);
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<?> query(AssetBackendPageQuery queryParam) {

        boolean isQueryOne = queryParam.getId() != null;
        boolean isQueryPage = queryParam.getPageNum() != null && queryParam.getPageSize() != null;

        List<TgAssetInfoSimpleDTO> resutlList = new ArrayList<>();

        if (AssetType.MODEL.equals(queryParam.getType())) {
            if (isQueryOne) {
                TgAssetInfo tgAssetInfo = queryOne(queryParam.getId());
                tgAssetInfo.setAssetInfos(buildRelate(queryParam.getId()));
                final List<AssetLink> assetLinks = buildLinks(queryParam.getId());
                tgAssetInfo.setAssetLinks(assetLinks.stream().filter(a -> a.getLinkType().equals(AssetLinkTypeEnum.ASSET_LINK.getType())).collect(Collectors.toList()));
                tgAssetInfo.setGuideLinks(assetLinks.stream().filter(a -> a.getLinkType().equals(AssetLinkTypeEnum.GUIDE_LINK.getType())).collect(Collectors.toList()));
                TgTemplateInfo data = (TgTemplateInfo) templateService.query(new HashMap<String, Object>() {{
                    put(CommonConstants.ID, tgAssetInfo.getRelatedId());
                }});

                data.setDistributedFields(templateService.distributedFieldList());

                TgAssetTemplateBindingInfo tgAssetTemplateBindingInfo = new TgAssetTemplateBindingInfo();
                tgAssetTemplateBindingInfo.setTgAssetInfo(tgAssetInfo);
                tgAssetTemplateBindingInfo.setBindingData(data);
                return AjaxResult.success(tgAssetTemplateBindingInfo);
            }
            if (isQueryPage) {

                if (StringUtils.isNotBlank(queryParam.getBizType())) {
                    List<TgTemplateInfo> list = templateService.listAllAssetsTable(queryParam.getBizType());
                    if (CollectionUtils.isNotEmpty(list)) {
                        queryParam.setRelatedIds(list.stream().map(TgTemplateInfo::getId).collect(Collectors.toList()));
                    } else {
                        Page<TgAssetInfoSimpleDTO> page = new Page<>();
                        page.setRecords(resutlList);
                        return AjaxResult.success(page);
                    }
                }
                if (Objects.nonNull(queryParam.getFlowId())) {
                    // 关联工作流
                    Set<Long> relatedIds = new HashSet<>();
                    // 1.模板中的工作流
                    List<TgTemplateInfo> tgTemplateInfos = templateService.listByFlowId(queryParam.getFlowId());
                    if (CollectionUtils.isNotEmpty(tgTemplateInfos)) {
                        relatedIds.addAll(tgTemplateInfos.stream().map(TgTemplateInfo::getId).collect(Collectors.toList()));
                    }

                    // 2.申请表中的工作流
                    List<TgApplicationInfo> applicationInfos = applicationDAO.lambdaQuery().eq(TgApplicationInfo::getConfigSqlWorkflowId, queryParam.getFlowId()).or().eq(TgApplicationInfo::getWorkflowId, queryParam.getFlowId()).list();
                    if (CollectionUtils.isNotEmpty(applicationInfos)) {
                        relatedIds.addAll(applicationInfos.stream().map(TgApplicationInfo::getTemplateId).collect(Collectors.toList()));
                    }
                    if (CollectionUtils.isEmpty(relatedIds)) {
                        Page<TgAssetInfoSimpleDTO> page = new Page<>();
                        page.setRecords(resutlList);
                        return AjaxResult.success(page);
                    }
                    queryParam.setFlowRelatedIds(new ArrayList<>(relatedIds));
                }
                Page<TgAssetInfo> tgAssetInfoPage = queryPage(queryParam);
                if (!tgAssetInfoPage.getRecords().isEmpty()) {
                    List<Long> modelIds = tgAssetInfoPage.getRecords().stream().map(TgAssetInfo::getRelatedId).collect(Collectors.toList());
                    Map<Long, TgTemplateInfo> templateInfoMap = templateService.queryByModelIds(modelIds).stream()
                            .collect(Collectors.toMap(TgTemplateInfo::getId, template -> template));

                    final Map<Long, AssetStatistics> collect = assetInfoService.statistics(tgAssetInfoPage.getRecords().stream().map(TgAssetInfo::getId).collect(Collectors.toList()))
                            .stream().collect(Collectors.toMap(AssetStatistics::getAssetId, v -> v));

                    Map<String, String> dataItemMap = this.queryDepMap(tgAssetInfoPage);

                    Map<Integer, String> flowNameMap = queryFlowMap();
                    Map<Long, Integer> relateMap = Collections.emptyMap();
                    Map<Long, List<TgApplicationInfo>> applicationMap = Collections.emptyMap();
                    // 查询对应的工作流信息
                    List<Long> relateIds =
                            tgAssetInfoPage.getRecords().stream().map(TgAssetInfo::getRelatedId).filter(Objects::nonNull
                            ).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(relateIds)) {
                        List<TgTemplateInfo> tgTemplateInfos = templateService.listByIds(relateIds);
                        relateMap = Optional.ofNullable(tgTemplateInfos).orElse(Collections.emptyList())
                                .stream().filter(templateInfo -> Objects.nonNull(templateInfo.getSchedulerId()))
                                .collect(Collectors.toMap(TgTemplateInfo::getId, TgTemplateInfo::getSchedulerId, (v1, v2) -> v2));

                        List<TgApplicationInfo> applicationInfos = applicationDAO.lambdaQuery().in(TgApplicationInfo::getTemplateId,
                                relateIds).list();
                        applicationMap = Optional.ofNullable(applicationInfos).orElse(Collections.emptyList()).stream()
                                .collect(Collectors.groupingBy(TgApplicationInfo::getTemplateId));
                    }

                    // 查询
                    for (TgAssetInfo a : tgAssetInfoPage.getRecords()) {
                        final TgAssetInfoSimpleDTO e = AssetBeanConverter.assetTemplateInfo2AssetSimpleDTO(a, templateInfoMap.get(a.getRelatedId()));
                        e.setApplyTimes(collect.get(a.getId()).getServiceCount());
                        e.setAssetProvider(a.getAssetProvider());
                        e.setAssetProviderName(dataItemMap.get(a.getAssetProvider()));
                        e.setAssetBindingDataType(a.getAssetBindingDataType());
                        e.setDesc(a.getAssetDescription());

                        // 组装flowName信息
                        if (TemplateTypeEnum.normal.name().equals(a.getAssetBindingDataType())
                                || TemplateTypeEnum.customized.name().equals(a.getAssetBindingDataType())) {
                            if (TemplateTypeEnum.normal.name().equals(a.getAssetBindingDataType()) && relateMap.containsKey(a.getRelatedId())) {
                                e.setFlowName(flowNameMap.get(relateMap.get(a.getRelatedId())));
                            } else if (TemplateTypeEnum.customized.name().equals(a.getAssetBindingDataType()) && applicationMap.containsKey(a.getRelatedId())) {
                                List<TgApplicationInfo> applicationInfos = applicationMap.get(a.getRelatedId());
                                if (CollectionUtils.isNotEmpty(applicationInfos)) {
                                    Set<Integer> flowIds = applicationInfos.stream().map(i -> Objects.nonNull(i.getConfigSqlWorkflowId()) ?
                                            i.getConfigSqlWorkflowId() : i.getWorkflowId()).filter(Objects::nonNull).collect(Collectors.toSet());
                                    List<String> flowNames = new ArrayList<>();
                                    flowIds.forEach(i -> {
                                        if (flowNameMap.containsKey(i)) {
                                            flowNames.add(flowNameMap.get(i));
                                        }
                                    });
                                    e.setFlowName(String.join(",", flowNames));
                                }
                            }
                        }
                        resutlList.add(e);
                    }
                }

                Page<TgAssetInfoSimpleDTO> page = new Page<>();
                BeanUtils.copyProperties(tgAssetInfoPage, page);
                page.setRecords(resutlList);
                return AjaxResult.success(page);
            }
        }

        if (AssetType.FILE.equals(queryParam.getType())) {
            if (isQueryOne) {
                TgAssetInfo tgAssetInfo = queryOne(queryParam.getId());
                tgAssetInfo.setAssetInfos(buildRelate(queryParam.getId()));
                TgDocInfo data = (TgDocInfo) docService.query(new HashMap<String, Object>() {{
                    put(CommonConstants.ID, tgAssetInfo.getRelatedId());
                }}).getData();
                TgAssetDocBindingInfo tgAssetDocBindingInfo = new TgAssetDocBindingInfo();
                tgAssetDocBindingInfo.setTgAssetInfo(tgAssetInfo);
                tgAssetDocBindingInfo.setBindingData(data);
                return AjaxResult.success(tgAssetDocBindingInfo);
            }

            if (isQueryPage) {
                Page<TgAssetInfo> tgAssetInfoPage = queryPage(queryParam);
                if (!tgAssetInfoPage.getRecords().isEmpty()) {
                    List<Long> docIds = tgAssetInfoPage.getRecords().stream()
                            .map(TgAssetInfo::getRelatedId).collect(Collectors.toList());
                    Map<Long, TgDocInfo> docInfoMap = docService.queryByDocIds(docIds).stream()
                            .collect(Collectors.toMap(TgDocInfo::getId, doc -> doc));

                    final Map<String, String> dataItemMap = this.queryDepMap(tgAssetInfoPage);

                    tgAssetInfoPage.getRecords().forEach(a -> {
                        final TgAssetInfoSimpleDTO e = AssetBeanConverter.assetDocInfo2AssetSimpleDTO(a, docInfoMap.get(a.getRelatedId()));
                        e.setAssetProvider(a.getAssetProvider());
                        e.setAssetProviderName(dataItemMap.get(a.getAssetProvider()));
                        resutlList.add(e);
                    });
                }
                Page<TgAssetInfoSimpleDTO> page = new Page<>();
                BeanUtils.copyProperties(tgAssetInfoPage, page);
                page.setRecords(resutlList);
                return AjaxResult.success(page);
            }
        }

        if (AssetType.TABLE.equals(queryParam.getType())) {
            if (isQueryOne) {
                TgAssetInfo tgAssetInfo = queryOne(queryParam.getId());
                tgAssetInfo.setAssetInfos(buildRelate(queryParam.getId()));
                TgAssetTableBindingInfo tgAssetTableBindingInfo = new TgAssetTableBindingInfo();
                tgAssetTableBindingInfo.setTgAssetInfo(tgAssetInfo);
                if (tgAssetInfo.getResourceType().equals(ResourceType.TABLE_MANAGEMENT)) {
                    TableInfo data = tableInfoService.getById(tgAssetInfo.getRelatedId());
                    tgAssetTableBindingInfo.setTgAssetInfo(tgAssetInfo);
                    tgAssetTableBindingInfo.setBindingData(data);
                }
                if (tgAssetInfo.getResourceType().equals(ResourceType.METADATA_MANAGEMENT)) {
                    TgMetadataInfo tgMeataDataInfo = TgMetadataInfo.newInstance().selectById(tgAssetInfo.getMetaId());
                    final MetaTableGetParam metaTableGetParam = new MetaTableGetParam();
                    metaTableGetParam.setMetaTableId(tgMeataDataInfo.getMetaDataId());
                    final Result<MetaTableDTO> metaTable = metadataClient.getMetaTable(metaTableGetParam);
                    if (metaTable.isSuccess()) {
                        tgMeataDataInfo.setCnName(metaTable.getResult().getCnName());
                        tgMeataDataInfo.setTenantName(metaTable.getResult().getTenantName());
                    }
                    tgAssetTableBindingInfo.setTgMetadataInfo(tgMeataDataInfo);
                }

                return AjaxResult.success(tgAssetTableBindingInfo);
            }

            if (isQueryPage) {
                if (StringUtils.isNotBlank(queryParam.getBizType())) {
                    List<TableInfo> list = tableInfoService.listAllAssetsTable(queryParam.getBizType());
                    if (CollectionUtils.isNotEmpty(list)) {
                        queryParam.setRelatedIds(list.stream().map(TableInfo::getId).collect(Collectors.toList()));
                    } else {
                        Page<TgAssetInfoSimpleDTO> page = new Page<>();
                        page.setRecords(resutlList);
                        return AjaxResult.success(page);
                    }
                }
                Page<TgAssetInfo> tgAssetInfoPage = queryPage(queryParam);
                if (!tgAssetInfoPage.getRecords().isEmpty()) {
                    List<Long> tableIds = tgAssetInfoPage.getRecords().stream().map(TgAssetInfo::getRelatedId).collect(Collectors.toList());
                    Map<Long, TableInfo> tableInfoMap = tableInfoService.queryByTableIds(tableIds).stream()
                            .collect(Collectors.toMap(TableInfo::getId, table -> table));

                    final Map<String, String> dataItemMap = this.queryDepMap(tgAssetInfoPage);

                    tgAssetInfoPage.getRecords().forEach(a -> {
                        if (a.getResourceType() != null && a.getResourceType().equals(ResourceType.TABLE_MANAGEMENT)) {
                            resutlList.add(AssetBeanConverter.assetTableInfo2AssetSimpleDTO(a,
                                    tableInfoMap.get(a.getRelatedId()), dataItemMap.get(a.getAssetProvider())));
                        }
                        if (a.getResourceType() != null && a.getResourceType().equals(ResourceType.METADATA_MANAGEMENT)) {
                            TgMetadataInfo tgMetadataInfo = TgMetadataInfo.newInstance().selectById(a.getMetaId());
                            resutlList.add(AssetBeanConverter.assetMetaDataInfo2AssetSimpleDTO(a,
                                    tgMetadataInfo, dataItemMap.get(a.getAssetProvider())));
                        }
                    });
                }

                Page<TgAssetInfoSimpleDTO> page = new Page<>();
                BeanUtils.copyProperties(tgAssetInfoPage, page);
                page.setRecords(resutlList);
                return AjaxResult.success(page);
            }
        }

        return AjaxResult.error("missing query");
    }

    private Map<String, String> queryDepMap(Page<TgAssetInfo> tgAssetInfoPage) {
        final List<String> deptIds = tgAssetInfoPage.getRecords().stream().map(TgAssetInfo::getAssetProvider)
                .collect(Collectors.toList());
        return SinoipaasUtils.mainDepartmentSelectbyids(deptIds).stream()
                .collect(Collectors.toMap(ResMaindatamainDepartmentselectbyidsItemDataItem::getId,
                        ResMaindatamainDepartmentselectbyidsItemDataItem::getDepartName, (v1, v2) -> v1));
    }

    @Override
    public List<RelateAssetInfo> buildRelate(Long id) {
        final LambdaQueryWrapper<TgAssetRelate> wq = Wrappers.<TgAssetRelate>lambdaQuery()
                .eq(TgAssetRelate::getAssetId, id);
        final List<TgAssetRelate> tgAssetRelates = tgAssetRelateMapper.selectList(wq);
        if (CollUtil.isEmpty(tgAssetRelates)) {
            return new ArrayList<>();
        }
        final Long userId = SecurityUtils.getUserId();
        final LambdaQueryWrapper<TgAssetInfo> wq1 = Wrappers.<TgAssetInfo>lambdaQuery()
                .in(TgAssetInfo::getId, tgAssetRelates.stream().map(TgAssetRelate::getRelateAssetId).collect(Collectors.toList()))
                .eq(TgAssetInfo::getShelfState, ShelfState.LISTING.getStatus());
        final List<TgAssetInfo> assetInfos = tgAssetInfoMapper.selectList(wq1);
        if (assetInfos.isEmpty()) {
            return new ArrayList<>();
        }
        final SinoPassUserDTO o = (SinoPassUserDTO) ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO);
        String deptId = o.getMainOrganizationId();
        final List<TgAssetRelate> relateSort = tgAssetRelates.stream().sorted(Comparator.comparing(TgAssetRelate::getRelateSort))
                .collect(Collectors.toList());
        List<RelateAssetInfo> sortList = new ArrayList<>();
        final Map<Long, RelateAssetInfo> infos = assetInfos.stream()
                .map(asset -> {
                    final RelateAssetInfo tgAssetRelateParam = new RelateAssetInfo();
                    tgAssetRelateParam.setAssetId(asset.getId());
                    tgAssetRelateParam.setAssetName(asset.getAssetName());
                    tgAssetRelateParam.setAssetType(asset.getType().getType());
                    tgAssetRelateParam.setPermission(computePermissions(asset, userId, deptId, true));
                    tgAssetRelateParam.setProcessId(asset.getProcessId());
                    tgAssetRelateParam.setRelatedId(asset.getRelatedId());
                    tgAssetRelateParam.setAssetBindingDataName(asset.getAssetBindingDataName());
                    // 显示当前用户是否申请过该资产, 以及申请次数
                    Integer currentUserApplyCount = computeCurrentUserApplyCount(asset.getId(), userId);
                    tgAssetRelateParam.setCurrentUserApplyCount(currentUserApplyCount);
                    tgAssetRelateParam.setHasApplied(currentUserApplyCount > 0);
                    return tgAssetRelateParam;
                }).collect(Collectors.toMap(RelateAssetInfo::getAssetId, v -> v));
        // 根据排序输出
        for (TgAssetRelate tgAssetRelate : relateSort) {
            final RelateAssetInfo relateAssetInfo = infos.get(tgAssetRelate.getRelateAssetId());
            if (Objects.nonNull(relateAssetInfo)) {
                sortList.add(relateAssetInfo);
            }
        }
        return sortList;
    }

    public List<AssetLink> buildLinks(Long id) {
        final LambdaQueryWrapper<AssetLink> wq = Wrappers.<AssetLink>lambdaQuery()
                .eq(AssetLink::getAssetId, id);
        return assetLinkMapper.selectList(wq);
    }

    @Override
    public List<TgAssetFrontTreeQueryResult> allReadableAsset() {
        Long userId = SecurityUtils.getUserId();

        LoginUser loginUser = SecurityUtils.getLoginUser();
        String deptId = Optional.ofNullable(loginUser.getUser()).map(SysUser::getOrgUserId)
                .map(SinoipaasUtils::mainEmployeeSelectbyid)
                .map(SinoPassUserDTO::getMainOrganizationId).orElse("");

//        final Object o = ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO);
//        String deptId;
//        if (o instanceof JSONObject) {
//            deptId = ((JSONObject) o).getString("mainOrganizationId");
//        } else {
//            deptId = ((SinoPassUserDTO) o).getMainOrganizationId();
//        }

        List<Integer> allReadableMenuIds = getAllReadableCatalogue(null, userId, deptId);
        Page<TgAssetFrontTreeQueryResult> page = new Page<>(1, Integer.MAX_VALUE);
        Page<TgAssetFrontTreeQueryResult> result = tgAssetInfoMapper.frontTreeQuery(page, allReadableMenuIds, deptId, userId.toString(), new AssetFrontendPageQuery(), null);
        return result.getRecords();
    }

    @Override
    public AjaxResult<?> frontTreeQuery(AssetFrontendPageQuery queryParam) {

        // 获取当前用户的 DEPT 和 USER 信息
        SysUser sysUser = ThreadContextHolder.getSysUser();
        Long userId = sysUser.getUserId();
        String deptId = ((SinoPassUserDTO) ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO)).getMainOrganizationId();

        List<Integer> allReadableMenuIds = getAllReadableCatalogue(queryParam.getDirId(), userId, deptId);

        // 获取处理资产数据结果
        Page<TgAssetFrontTreeQueryResult> page = new Page<>(queryParam.getPageNum(), queryParam.getPageSize());

        final List<Integer> assetIds = labelService.searchLabelRelate(queryParam.getSearchContent());

        // 此查询出来包含有阅读权限的目录下的资产及所有跟随目录的资产
        Page<TgAssetFrontTreeQueryResult> result = tgAssetInfoMapper.frontTreeQuery(page, allReadableMenuIds, deptId, userId.toString(), queryParam, assetIds);

        if (CollUtil.isNotEmpty(result.getRecords())) {
            // 资产提供方值
            final List<String> assetProvider = result.getRecords().stream()
                    .map(TgAssetFrontTreeQueryResult::getAssetProvider)
                    .filter(Objects::nonNull).distinct().collect(Collectors.toList());
            final List<ResMaindatamainDepartmentselectbyidsItemDataItem> items = SinoipaasUtils.mainDepartmentSelectbyids(assetProvider);
            final Map<String, String> deptMap = items.stream()
                    .collect(Collectors.toMap(ResMaindatamainDepartmentselectbyidsItemDataItem::getId, v -> {
                        final String orgAdminTreePathText = v.getOrgAdminTreePathText();
                        final String[] split = orgAdminTreePathText.split("/");
                        if (split.length > 2) {
                            return split[split.length - 2] + "/" + split[split.length - 1];
                        } else {
                            return split[split.length - 1];
                        }
                    }));

            // 获取资产负责人
            final List<Long> userIds = result.getRecords().stream()
                    .map(TgAssetFrontTreeQueryResult::getAssetManagerJson)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
                    .stream().map(a -> JSONArray.parseArray(a, String.class))
                    .flatMap(List::stream)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
                    .stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            final LambdaQueryWrapper<SysUser> wq = Wrappers.<SysUser>lambdaQuery()
                    .in(CollUtil.isNotEmpty(userIds), SysUser::getUserId, userIds);
            final List<SysUser> users = sysUserMapper.selectList(wq);
            final Map<Long, SysUser> userMap = users.stream().collect(Collectors.toMap(SysUser::getUserId, v -> v));

            // 补充资产的详情信息
            final Map<Integer, AssetsCatalogue> catalogueIdNameMap = assetsCatalogueMapper
                    .selectList(Wrappers.<AssetsCatalogue>lambdaQuery().eq(AssetsCatalogue::getDeleted, 0))
                    .stream().collect(Collectors.toMap(AssetsCatalogue::getId, v -> v));

            // 资产统计
            final Map<Long, AssetStatistics> assetStatisticsMap = assetInfoService.statistics(result.getRecords().stream().map(TgAssetFrontTreeQueryResult::getId).collect(Collectors.toList()))
                    .stream().collect(Collectors.toMap(AssetStatistics::getAssetId, v -> v));

            // 资产标签
            final Map<Long, List<TgLabelInfo>> fullLabels = labelService.getFullLabels(result.getRecords().stream().map(TgAssetFrontTreeQueryResult::getId).collect(Collectors.toList()));

            Map<Long, TgAssetInfo> tgAssetIdMap = new HashMap<Long, TgAssetInfo>();
            if (CollUtil.isNotEmpty(result.getRecords())) {
                tgAssetIdMap = tgAssetInfoMapper.selectBatchIds(result.getRecords().stream().map(TgAssetFrontTreeQueryResult::getId).collect(Collectors.toList())).stream().collect(Collectors.toMap(k -> k.getId(), v -> v));
            }

            final Map<TgAssetInfo, List<AssetPermissionType>> permissionMap = computePermissions(new ArrayList<>(tgAssetIdMap.values()), userId, deptId, true);

            // 获取模板类型
            final List<Long> ids = tgAssetIdMap.values()
                    .stream()
                    .filter(a -> a.getType().equals(AssetType.MODEL))
                    .map(TgAssetInfo::getRelatedId).collect(Collectors.toList());
            final Map<Long, TgTemplateInfo> templateInfoMap = Lambda.queryListIfExist(ids, tgTemplateInfoMapper::selectBatchIds)
                    .stream()
                    .collect(Collectors.toMap(TgTemplateInfo::getId, v -> v));

            for (TgAssetFrontTreeQueryResult a : result.getRecords()) {
                // 设置目录全路径
                a.setMenuName(catalogueService.buildCatalogFullPath(catalogueIdNameMap.get(a.getAssetMenuId()).getPath(), catalogueIdNameMap));
                // 设置标签
                final List<String> labels = fullLabels.get(a.getId()).stream().map(TgLabelInfo::getName).collect(Collectors.toList());
                a.setAssetLabels(labels);

                final AssetStatistics assetStatistics = assetStatisticsMap.get(a.getId());
                if (Objects.nonNull(assetStatistics)) {
                    a.setServiceCount(assetStatistics.getServiceCount());
                    a.setViewCount(assetStatistics.getViewNum());
                    a.setCollectionCount(assetStatistics.getCollectionCount());
                    a.setShareCount(assetStatistics.getShareCount());
                } else {
                    a.setServiceCount(0);
                    a.setViewCount(0);
                    a.setCollectionCount(0);
                    a.setShareCount(0);
                }
                // 资产提供方
                if (Objects.nonNull(a.getAssetProvider())) {
                    a.setAssetProviderId(a.getAssetProvider());
                    a.setAssetProvider(deptMap.get(a.getAssetProvider()));
                }
                // 如果是模型类型,添加baseTable
                if (a.getType().equals(AssetType.MODEL)) {
                    TgTemplateInfo tgTemplateInfo = templateInfoMap.get(a.getRelatedId());
                    a.setBaseTable(tgTemplateInfo.getBaseTableName());
                    a.setBaseTableId(tgTemplateInfo.getBaseTableId());
                    //替换模型绑定子类型
                    a.setAssetBindingDataType(ModuleAssetBindTypeEnum.getNameByValue(a.getAssetBindingDataType()));
                }
                final TgAssetInfo tgAssetInfo = tgAssetIdMap.get(a.getId());
                // 设置权限
                a.setPermission(permissionMap.get(tgAssetInfo));

                // 设置资产负责人
                final List<String> managers = JSONArray.parseArray(a.getAssetManagerJson(), String.class);
                if (CollUtil.isNotEmpty(managers)) {
                    a.setAssetManagerName(managers.stream().map(b -> {
                                final SysUser user = userMap.get(Long.parseLong(b));
                                if (user != null) {
                                    return user.getRealName();
                                } else {
                                    return null;
                                }
                            }).filter(StringUtils::isNotEmpty)
                            .collect(Collectors.joining(",")));
                }

                //设置是否申请标识
//                Integer currentUserApplyCount = assetService.computeCurrentUserApplyCount(a.getId(), userId);
//                a.setHasApplied(currentUserApplyCount > 0);
            }

            //设置是否申请标识
            Set<Long> assetsIds = Lambda.buildSet(result.getRecords(), TgAssetFrontTreeQueryResult::getId);
            Map<Long, Integer> cntMap = assetService.computeCurrentUserApplyCount(userId, assetsIds);
            for (TgAssetFrontTreeQueryResult a : result.getRecords()) {
                a.setHasApplied(Optional.ofNullable(cntMap.get(a.getId())).map(v -> v > 0).orElse(false));
            }
        }

        return AjaxResult.success(result);
    }

    /**
     * 计算用户对某一资产拥有的权限
     *
     * @param tgAssetInfo          资产
     * @param userId               用户id
     * @param deptId               部门id
     * @param isIncludeApplication 是否包含申请的数据权限, false 只计算资产负责人和白名单的权限, true 包含申请的权限
     * @return
     */
    @Override
    public List<AssetPermissionType> computePermissions(TgAssetInfo tgAssetInfo, Long userId, String deptId, boolean isIncludeApplication) {
        List<AssetPermissionType> permission = new ArrayList<>();
        if (tgAssetInfo == null) {
            return Lists.newArrayList();
        }
        JsonBeanConverter.convert2Obj(tgAssetInfo);
        AssetType type = tgAssetInfo.getType();

        // 0. 默认显示资产开放列表的按钮
        permission.addAll(tgAssetInfo.getAssetOpenServices());

        // 1. 无需审核的服务直接加上服务
        if (!tgAssetInfo.getNonAuditAssetOpenServices().isEmpty()) {
            permission.addAll(replacePermission(tgAssetInfo.getNonAuditAssetOpenServices()));
        }

        // 2. 资产负责人名单是否包含当前用户, 如果是则加上对应权限
        List<String> assetManager = tgAssetInfo.getAssetManager();
        if (assetManager.contains(userId.toString())) {
            List<AssetPermissionType> assetManagerPermissions = new ArrayList<>();
            if (type.equals(AssetType.FILE)) {
                // 负责人文件权限全显示
                assetManagerPermissions.add(AssetPermissionType.READ_FILE);
                assetManagerPermissions.add(AssetPermissionType.DOWNLOAD_PDF);
                assetManagerPermissions.add(AssetPermissionType.DOWNLOAD_SRC);
            }
            if (type.equals(AssetType.TABLE)) {
                // 负责人表单拥有查询权限
                assetManagerPermissions.add(AssetPermissionType.DATA_QUERY);
            }
            if (type.equals(AssetType.MODEL)) {
                // 负责人表单拥有查询权限
                assetManagerPermissions.add(AssetPermissionType.TEMPLATE_APPLY);
            }
            permission.addAll(assetManagerPermissions);
        }

        // 2. 获取该条资产的白名单中是否包含该用户, 如果是则加上对应权限
        List<TgAssetWhitelistInfo> validServiceWhiteListInfo = whiltlistInfoDAO.findValidServiceWhiteListInfoByAssetIdAndUserIdAndDeptId(tgAssetInfo.getId(), userId, deptId);
        for (TgAssetWhitelistInfo whitelist : validServiceWhiteListInfo) {
            JsonBeanConverter.convert2Obj(whitelist);
            if (whitelist.getType().equals(type)) {
                List<AssetPermissionType> whitelistPermission = whitelist.getAssetOpenServices();
                permission.addAll(replacePermission(whitelistPermission));
            }
        }

        List<TgApplicationInfo> validApplicationList = applicationService.getValidApplicationByAssetIdAndUserId(tgAssetInfo.getId(), userId);
        if (isIncludeApplication) {
            // 3. 获取该条资产的有效申请记录,有效申请记录是否包含当前用户, 如果是则加上对应权限
            for (TgApplicationInfo applicationInfo : validApplicationList) {
                JsonBeanConverter.convert2Obj(applicationInfo);
                List<AssetPermissionType> applicationPermission = replacePermission(applicationInfo.getPermission());
                // 排除第一第二步已经拥有的权限，剩余权限为有效申请记录，需要进行是否服务下线判断
                applicationPermission = applicationPermission.stream()
                        .filter(p -> !permission.contains(p))
                        .collect(Collectors.toList());
                applicationPermission = checkServiceOffLine(tgAssetInfo, applicationPermission);
                permission.addAll(applicationPermission);
            }
        }

        if (type.equals(AssetType.MODEL) && !validApplicationList.isEmpty()) {
            permission.add(AssetPermissionType.CHECK_PERSONAL_DATA_REQUEST);
        }

        List<AssetPermissionType> result = permission.stream().distinct().collect(Collectors.toList());

        removeRequestTypeWhenUnnecessary(result);

        return result;
    }

    @Override
    public Map<TgAssetInfo, List<AssetPermissionType>> computePermissions(List<TgAssetInfo> tgAssetInfoList, Long userId, String deptId, boolean isIncludeApplication) {

        Map<TgAssetInfo, List<AssetPermissionType>> map = new HashMap<>();

        if (CollUtil.isEmpty(tgAssetInfoList)) {
            return map;
        }

        // 批量获取服务白名单
        final List<TgAssetWhitelistInfo> whiteListInfoByUserIdAndDeptIdAndType = whiltlistInfoDAO.findWhiteListInfoByUserIdAndDeptIdAndType(tgAssetInfoList.stream().map(TgAssetInfo::getId).collect(Collectors.toList()), userId, deptId);

        final Map<Long, List<TgAssetWhitelistInfo>> whiteMap = whiteListInfoByUserIdAndDeptIdAndType.stream()
                .collect(Collectors.groupingBy(TgAssetWhitelistInfo::getAssetId));


        // 批量获取申请
        final List<TgApplicationInfo> tgApplicationInfos = applicationService.queryApplicationByApplicantIdAndName(tgAssetInfoList.stream().map(TgAssetInfo::getId).collect(Collectors.toList()), userId);

        final Map<Long, List<TgApplicationInfo>> applicationMap = tgApplicationInfos.stream()
                .collect(Collectors.groupingBy(TgApplicationInfo::getNewAssetId));


        for (TgAssetInfo tgAssetInfo : tgAssetInfoList) {
            List<AssetPermissionType> permission = new ArrayList<>();
            if (tgAssetInfo == null) {
                continue;
            }
            JsonBeanConverter.convert2Obj(tgAssetInfo);
            AssetType type = tgAssetInfo.getType();

            // 0. 默认显示资产开放列表的按钮
            permission.addAll(tgAssetInfo.getAssetOpenServices());

            // 1. 无需审核的服务直接加上服务
            if (!tgAssetInfo.getNonAuditAssetOpenServices().isEmpty()) {
                permission.addAll(replacePermission(tgAssetInfo.getNonAuditAssetOpenServices()));
            }

            // 2. 资产负责人名单是否包含当前用户, 如果是则加上对应权限
            List<String> assetManager = tgAssetInfo.getAssetManager();
            if (assetManager.contains(userId.toString())) {
                List<AssetPermissionType> assetManagerPermissions = new ArrayList<>();
                if (type.equals(AssetType.FILE)) {
                    // 负责人文件权限全显示
                    assetManagerPermissions.add(AssetPermissionType.READ_FILE);
                    assetManagerPermissions.add(AssetPermissionType.DOWNLOAD_PDF);
                    assetManagerPermissions.add(AssetPermissionType.DOWNLOAD_SRC);
                }
                if (type.equals(AssetType.TABLE)) {
                    // 负责人表单拥有查询权限
                    assetManagerPermissions.add(AssetPermissionType.DATA_QUERY);
                }
                if (type.equals(AssetType.MODEL)) {
                    // 负责人表单拥有查询权限
                    assetManagerPermissions.add(AssetPermissionType.TEMPLATE_APPLY);
                }
                permission.addAll(assetManagerPermissions);
            }

            // 2. 获取该条资产的白名单中是否包含该用户, 如果是则加上对应权限
            List<TgAssetWhitelistInfo> validServiceWhiteListInfo = whiteMap.get(tgAssetInfo.getId());
            if (CollUtil.isNotEmpty(validServiceWhiteListInfo)) {
                for (TgAssetWhitelistInfo whitelist : validServiceWhiteListInfo) {
                    JsonBeanConverter.convert2Obj(whitelist);
                    if (whitelist.getType().equals(type)) {
                        List<AssetPermissionType> whitelistPermission = whitelist.getAssetOpenServices();
                        permission.addAll(replacePermission(whitelistPermission));
                    }
                }
            }

            List<TgApplicationInfo> validApplicationList = applicationMap.get(tgAssetInfo.getId());
            if (isIncludeApplication) {
                if (CollUtil.isNotEmpty(validApplicationList)) {
                    // 4. 获取该条资产的有效申请记录,有效申请记录是否包含当前用户, 如果是则加上对应权限
                    for (TgApplicationInfo applicationInfo : validApplicationList) {
                        JsonBeanConverter.convert2Obj(applicationInfo);
                        List<AssetPermissionType> applicationPermission = replacePermission(applicationInfo.getPermission());
                        // 排除第一第二步已经拥有的权限，剩余权限为有效申请记录，需要进行是否服务下线判断
                        applicationPermission = applicationPermission.stream()
                                .filter(p -> !permission.contains(p))
                                .collect(Collectors.toList());
                        applicationPermission = checkServiceOffLine(tgAssetInfo, applicationPermission);
                        permission.addAll(applicationPermission);
                    }
                }
            }

            if (type.equals(AssetType.MODEL) && CollUtil.isNotEmpty(validApplicationList)) {
                permission.add(AssetPermissionType.CHECK_PERSONAL_DATA_REQUEST);
            }

            List<AssetPermissionType> result = permission.stream().distinct().collect(Collectors.toList());

            removeRequestTypeWhenUnnecessary(result);

            map.put(tgAssetInfo, result);
        }
        return map;
    }

    /**
     * 例如：当存在 READ_FILE 则删除 READ_FILE_REQUEST
     */
    private void removeRequestTypeWhenUnnecessary(List<AssetPermissionType> result) {
        List<AssetPermissionType> enumsToRemove = new ArrayList<>();
        for (AssetPermissionType assetPermissionType : result) {
            if (assetPermissionType.getType().endsWith("_REQUEST")) {
                String enumWithoutRequest = assetPermissionType.getType().replace("_REQUEST", "");
                AssetPermissionType correspondingEnum = AssetPermissionType.valueOf(enumWithoutRequest);

                // 如果找到对应的枚举，将其添加到要删除的列表中
                if (result.contains(correspondingEnum)) {
                    enumsToRemove.add(assetPermissionType);
                }
            }
        }
        result.removeAll(enumsToRemove);
    }

    private List<AssetPermissionType> replacePermission(List<AssetPermissionType> whitelistPermission) {
        List<AssetPermissionType> result = whitelistPermission.stream().map(p -> {
            if (p.equals(AssetPermissionType.READ_FILE_REQUEST)) {
                return AssetPermissionType.READ_FILE;
            } else if (p.equals(AssetPermissionType.DOWNLOAD_PDF_REQUEST)) {
                return AssetPermissionType.DOWNLOAD_PDF;
            } else if (p.equals(AssetPermissionType.DOWNLOAD_SRC_REQUEST)) {
                return AssetPermissionType.DOWNLOAD_SRC;
            } else if (p.equals(AssetPermissionType.DATA_QUERY_REQUEST)) {
                return AssetPermissionType.DATA_QUERY;
            } else if (p.equals(AssetPermissionType.DATA_EXCHANGE_REQUEST)) {
                return AssetPermissionType.DATA_EXCHANGE;
            } else if (p.equals(AssetPermissionType.TEMPLATE_APPLY_REQUEST)) {
                return AssetPermissionType.TEMPLATE_APPLY;
            }
            return p;
        }).collect(Collectors.toList());

        return result;
    }

    private List<AssetPermissionType> checkServiceOffLine(TgAssetInfo tgAssetInfo, List<AssetPermissionType> applicationPermission) {
        final List<AssetPermissionType> assetOpenServices = tgAssetInfo.getAssetOpenServices();
        return AssetPermissionType.checkOffLine(assetOpenServices, applicationPermission);
    }


    private List<Integer> getAllReadableCatalogue(Integer parentId, Long userId, String deptId) {
        // 获取目录树下所有目录
        List<AssetsCatalogue> menus = catalogueService.getMenuIdsByMenuRootIds(new ArrayList<Integer>() {{
            if (parentId != null) {
                add(parentId);
            }
        }});
        if (menus.isEmpty()) {
            return Collections.emptyList();
        }

        // 检查传输的目录是否有权限(有它的权限或者它父级的权限)
        List<AssetsCataloguePermission> menusPermission = assetsCataloguePermissionDAO.findByUserIdAndDeptId(userId, deptId);
        // 获取可阅读目录权限且存在于目录树的目录ID
        List<Integer> readableMenuIds = menusPermission.stream()
                .filter(a -> a.getReadable().equals(1))
                .map(AssetsCataloguePermission::getCatalogueId).collect(Collectors.toList());

        final List<Integer> collect = menus.stream()
                .filter(a -> readableMenuIds.stream().anyMatch(b -> a.getPath().contains("/" + b + "/")))
                .map(AssetsCatalogue::getId).distinct().collect(Collectors.toList());

        // 获取所有的Path包含（可阅读目录权限且存在于目录树的目录ID）的目录ID
        List<Integer> allReadableMenuIds = collect.isEmpty() ? Lists.newArrayList()
                : catalogueService.getMenuIdsByMenuRootIds(collect).stream().map(AssetsCatalogue::getId).collect(Collectors.toList());

        return allReadableMenuIds;
    }


    /**
     * 收藏资产
     *
     * @param collectAssetRequest
     * @return
     */
    @Override
    public AjaxResult<Object> collectAsset(CollectAssetRequest collectAssetRequest) {
        List<TgAssetUserRelation> relationList = assetUserRelationMapper.selectList(new QueryWrapper<TgAssetUserRelation>().lambda()
                .eq(TgAssetUserRelation::getAssetId, collectAssetRequest.getAssetId())
                .eq(TgAssetUserRelation::getUserId, SecurityUtils.getUserId()));
        if (CollectionUtils.isNotEmpty(relationList)) {
            TgAssetUserRelation relation = relationList.get(0);
            relation.setIsCollect(collectAssetRequest.getIsCollect())
                    .setCollectTime(IsCollectEnum.YES.getCode().equals(collectAssetRequest.getIsCollect()) ? new Date() : null)
                    .setUpdater(SecurityUtils.getUsername())
                    .setUpdateTime(new Date());
            assetUserRelationMapper.updateById(relation);
        } else {
            assetUserRelationMapper.insert(AssetUserRelationBeanConverter.toEntity(collectAssetRequest));
        }
        return AjaxResult.success();
    }

    /**
     * 转发资产
     *
     * @param forwardAssetRequest
     * @return
     */
    @Override
    public AjaxResult<Object> forwardAsset(ForwardAssetRequest forwardAssetRequest) {
        List<TgAssetUserRelation> relationList = assetUserRelationMapper.selectList(new QueryWrapper<TgAssetUserRelation>().lambda()
                .eq(TgAssetUserRelation::getAssetId, forwardAssetRequest.getAssetId())
                .eq(TgAssetUserRelation::getUserId, SecurityUtils.getUserId()));
        if (CollectionUtils.isNotEmpty(relationList)) {
            TgAssetUserRelation relation = relationList.get(0);
            relation.setForwardNum(relation.getForwardNum() + 1)
                    .setUpdater(SecurityUtils.getUsername())
                    .setUpdateTime(new Date());
            assetUserRelationMapper.updateById(relation);
        } else {
            assetUserRelationMapper.insert(AssetUserRelationBeanConverter.toEntity(forwardAssetRequest));
        }
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<Object> viewAsset(Long assetId) {

        List<TgAssetUserRelation> relationList = assetUserRelationMapper.selectList(new QueryWrapper<TgAssetUserRelation>().lambda()
                .eq(TgAssetUserRelation::getAssetId, assetId)
                .eq(TgAssetUserRelation::getUserId, SecurityUtils.getUserId()));
        if (CollectionUtils.isNotEmpty(relationList)) {
            TgAssetUserRelation relation = relationList.get(0);
            relation.setViewNum(relation.getViewNum() + 1)
                    .setUpdater(SecurityUtils.getUsername())
                    .setUpdateTime(new Date());
            assetUserRelationMapper.updateById(relation);
        } else {
            assetUserRelationMapper.insert(AssetUserRelationBeanConverter.viewAsset(assetId));
        }

        final DateTime beginTime = DateUtil.beginOfDay(new Date());
        final DateTime endTime = DateUtil.endOfDay(new Date());
        final LambdaQueryWrapper<TgAssetBurialPoint> wq = Wrappers.<TgAssetBurialPoint>lambdaQuery()
                .between(TgAssetBurialPoint::getBurialDate, beginTime, endTime)
                .eq(TgAssetBurialPoint::getAssetId, assetId);

        TgAssetBurialPoint tgAssetBurialPoint = tgAssetBurialPointMapper.selectOne(wq);
        if (Objects.nonNull(tgAssetBurialPoint)) {
            tgAssetBurialPoint.setViewNum(tgAssetBurialPoint.getViewNum() + 1);
            tgAssetBurialPointMapper.updateById(tgAssetBurialPoint);
        } else {
            tgAssetBurialPoint = new TgAssetBurialPoint();
            tgAssetBurialPoint.setBurialDate(DateUtil.offsetHour(DateUtil.beginOfDay(new Date()), 12));
            tgAssetBurialPoint.setAssetId(assetId);
            tgAssetBurialPoint.setViewNum(1);
            tgAssetBurialPointMapper.insert(tgAssetBurialPoint);
        }

        return AjaxResult.success();
    }

    /**
     * 收藏列表
     *
     * @param collectListRequest
     * @return
     */
    @Override
    public AjaxResult<PageInfo<CollectListVo>> collectList(CollectListRequest collectListRequest) {
        IPage<CollectListVo> page = assetUserRelationMapper.collectList(
                new Page(collectListRequest.getPageNum(), collectListRequest.getPageSize()), SecurityUtils.getUserId(), collectListRequest);
        page.getRecords().forEach(item -> {
            item.setCataloguePathCn(dataAssetsCatalogueService.getCataloguePathCn(item.getCataloguePath()));
            Optional<ResMaindatamainDepartmentselectbyidsItemDataItem> optionalDepart =
                    Optional.ofNullable(SinoipaasUtils.mainDepartmentSelectbyids(item.getAssetProvider()));
            item.setAssetProviderName(optionalDepart.isPresent() ? optionalDepart.get().getDepartName() : null);
        });
        return AjaxResult.success(PageUtil.convert(page));
    }

    @Override
    public int checkSameAssetName(Long id, String assetName) {
        return TgAssetInfo.newInstance().selectCount(new QueryWrapper<TgAssetInfo>() {{
            if (id != null) {
                ne("id", id);
            }
            eq("asset_name", assetName);
        }});
    }

    /**
     * 判断是否可以查看
     *
     * @param judgeViewableRequest
     * @return
     */
    @Override
    public AjaxResult<Object> judgeViewable(JudgeViewableRequest judgeViewableRequest) {
        String errorMsg = null;
        TgAssetInfo tgAssetInfo = tgAssetInfoMapper.selectById(judgeViewableRequest.getAssetId());
        if (tgAssetInfo == null || !"已上架".equals(tgAssetInfo.getShelfState())) {
            errorMsg = AssetConstants.ASSET_OFF;
        } else if (!dataAssetsCatalogueService.assetReadAble(tgAssetInfo.getType().name(), tgAssetInfo.getRelatedId())) {
            errorMsg = AssetConstants.ASSET_NO_READ;
        }

        if (errorMsg != null) {
            return AjaxResult.error(AssetConstants.PERMISSION_LACK_ERROR_CODE, errorMsg);
        }
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<?> fillProcessId4FollowMenuDirItem(TgAssetInfo tgAssetInfo) {
        if (tgAssetInfo.getIsFollowServiceMenuReadableRange().equals(AuthItemEnum.FOLLOW_DIR_AUTH)) {
            // 设置流程id
            CatalogueDetailDTO catalogueBaseInfo = catalogueService.getCatalogueBaseInfo(tgAssetInfo.getAssetMenuId());
            if (catalogueBaseInfo == null || catalogueBaseInfo.getServiceFlowId() == null) {
                return AjaxResult.error(InfoConstants.NEED_PROCESS_ID);
            }
            if (TgAuditProcessInfo.newInstance().selectById(catalogueBaseInfo.getServiceFlowId()) == null) {
                return AjaxResult.error(InfoConstants.INVALID_PROCESS);
            }
            tgAssetInfo.setProcessId(catalogueBaseInfo.getServiceFlowId());
        }
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<?> myApplicationQuery(AssetApplicationPageQuery queryParam) {
        queryParam.setSearchContent(StrUtil.decode(queryParam.getSearchContent()));
        List<SysUser> sysUsers = sysUserMapper.selectList(new QueryWrapper<>());
        Map<Long, SysUser> userMap = sysUsers.stream().collect(Collectors.toMap(SysUser::getUserId, v -> v, (front, current) -> current));
        SysUser sysUser = ThreadContextHolder.getSysUser();
        Long userId = sysUser.getUserId();
        // 获取处理资产数据结果
        Page<TgAssetMyApplicationPageResult> page = new Page<>(queryParam.getPageNum(), queryParam.getPageSize());
        // 此查询出来包含有阅读权限的目录下的资产及所有跟随目录的资产
        Page<TgAssetMyApplicationPageResult> result = tgAssetInfoMapper.myApplicaionQuery(page, queryParam, userId);

        // 新增项目名称和客户简称
        final List<Long> applicationIds = result.getRecords().stream().map(TgAssetMyApplicationPageResult::getApplicationId).collect(Collectors.toList());
        Map<Long, TgApplicationInfo> applicationInfoMap = new HashMap<>();
        Map<Long, Project> projectMap = new HashMap<>();
        Map<Long, String> customerNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(applicationIds)) {
            final List<TgApplicationInfo> tgApplicationInfos = applicationService.queryByIds(applicationIds);
            applicationInfoMap.putAll(tgApplicationInfos.stream().collect(Collectors.toMap(TgApplicationInfo::getId, v -> v)));
            final List<Long> projectIds = tgApplicationInfos.stream().map(TgApplicationInfo::getProjectId).collect(Collectors.toList());
            final List<Project> projects = projectMapper.selectBatchIds(projectIds);
            final List<Long> customerIds = projects.stream().map(Project::getCustomerId).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(customerIds)) {
                customerNameMap.putAll(customerMapper.selectBatchIds(customerIds).stream().collect(Collectors.toMap(Customer::getId, Customer::getShortName)));
            }
            projectMap.putAll(projects.stream().collect(Collectors.toMap(Project::getId, v -> v)));
        }

        // 判断是否可修改合同编号，可修改的情况包括：1.已审核通过且没重新申请 2.已审核通过且重新申请还没通过
        List<Long> newApplyIdList = result.getRecords().stream()
                .map(TgAssetMyApplicationPageResult::getNewApplicationId)
                .filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());
        List<TgApplicationInfo> newApplyList = org.apache.commons.collections4.CollectionUtils.isEmpty(newApplyIdList) ? new ArrayList<>()
                : applicationInfoMapper.selectList(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getCurrentAuditProcessStatus)
                .in(TgApplicationInfo::getId, newApplyIdList));
        Map<Long, Integer> newApplyMap = newApplyList.stream()
                .collect(Collectors.toMap(TgApplicationInfo::getId, TgApplicationInfo::getCurrentAuditProcessStatus, (front, current) -> current));

        result.getRecords().forEach(r -> {
            JsonBeanConverter.convert2Obj(r, userMap);
            if (ApplicationConst.AuditStatus.AUDIT_PASS == r.getCurrentAuditProcessStatus()
                    && (newApplyMap.get(r.getNewApplicationId()) == null || ApplicationConst.AuditStatus.AUDIT_PASS != newApplyMap.get(r.getNewApplicationId()))) {
                r.setRelateAssets(true);
            } else {
                r.setRelateAssets(false);
            }

            // 填充项目名称与客户简称
            final TgApplicationInfo info = applicationInfoMap.get(r.getApplicationId());
            r.setProjectId(info.getProjectId());
            final Project project = projectMap.get(info.getProjectId());
            if (Objects.nonNull(project)) {
                r.setProjectName(project.getName());
                r.setClientNames(customerNameMap.get(project.getCustomerId()));
            }

            r.setServiceType(r.getAssetOpenServices().stream()
                    .map(item -> item.getTypeName().replace("申请", "")).collect(Collectors.joining("/")));
            boolean newPass = newApplyMap.get(r.getNewApplicationId()) == null || ApplicationConst.AuditStatus.AUDIT_PASS != newApplyMap.get(r.getNewApplicationId());
            r.setRelateAssets(ApplicationConst.AuditStatus.AUDIT_PASS == r.getCurrentAuditProcessStatus() && newPass);
        });

        return AjaxResult.success(result);
    }

    @Override
    public AjaxResult<?> myApplicationCount() {
        SysUser sysUser = ThreadContextHolder.getSysUser();
        Long userId = sysUser.getUserId();
        Map<String, Map<String, Object>> maps = tgAssetInfoMapper.myApplicaionCount(userId);
        Map<String, Object> stringIntegerMap = transformNestedMap(maps);
        return AjaxResult.success(new HashMap<String, Object>() {{
            put("dataCount", stringIntegerMap.getOrDefault("MODEL", 0));
            put("docCount", stringIntegerMap.getOrDefault("FILE", 0));
            put("tableCount", stringIntegerMap.getOrDefault("TABLE", 0));
        }});
    }

    @Override
    public List<TgAssetInfo> queryAll() {
        return TgAssetInfo.newInstance().selectAll();
    }

    @Override
    public Integer computeCurrentUserApplyCount(Long assetId, Long userId) {
        return tgAssetInfoMapper.computeCurrentUserApplyCount(assetId, userId);
    }

    @Override
    public Map<Long, Integer> computeCurrentUserApplyCount(Long userId, Collection<Long> assetIds) {
        if (CollectionUtils.isEmpty(assetIds)) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> result = tgApplicationInfoMapper.selectMaps(new QueryWrapper<TgApplicationInfo>()
                .select("new_asset_id, count(*) as cnt")
                .eq("applicant_id", userId)
                .eq("current_audit_process_status", ApplicationConst.AuditStatus.AUDIT_PASS)
                .ge("data_expir", new Date())
                .in("new_asset_id", assetIds)
                .groupBy("new_asset_id")
        );
        return result.stream().collect(Collectors.toMap(
                v -> Optional.ofNullable(v.get("new_asset_id")).map(Object::toString).map(Long::parseLong).orElse(0L),
                v -> Optional.ofNullable(v.get("cnt")).map(Object::toString).map(Integer::parseInt).orElse(0),
                (front, current) -> current));
    }

    @Override
    public AjaxResult<?> lastAssetQuery(LastAssetQuery queryParam) {
        QueryWrapper qw = new QueryWrapper() {{
            eq("new_asset_id", queryParam.getAssetId());
            eq("current_audit_process_status", ApplicationConst.AuditStatus.AUDIT_PASS);
            orderByDesc("create_time");
        }};
        Page<TgApplicationInfo> page = applicationInfoMapper.selectPage(new Page<>(queryParam.getPageNum(), queryParam.getPageSize()), qw);
        List<TgLastApplicationInfo> result = new ArrayList<>();

        final List<TgApplicationInfo> records = page.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            final List<SysUser> sysUsers = sysUserMapper.selectUserByIds(records.stream().map(TgApplicationInfo::getApplicantId).collect(Collectors.toSet()));
            final List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> sinoPassUserDTOS = SinoipaasUtils.employeeWithDept(sysUsers.stream().map(SysUser::getOrgUserId).collect(Collectors.toList()));
            final Map<Long, String> userIdMap = sysUsers.stream().collect(Collectors.toMap(SysUser::getUserId, SysUser::getOrgUserId));
            final Map<String, ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> sinoPassUserDTOMap = sinoPassUserDTOS.stream().collect(Collectors.toMap(k -> k.getId(), v -> v));

            records.forEach(a -> {
                result.add(new TgLastApplicationInfo() {{
                    JsonBeanConverter.convert2Obj(a);
                    final String orgUserId = userIdMap.get(a.getApplicantId());
                    final ResMaindataMainDepartmentSelectUserWithDeptItemDataItem sinoPassUserDTO = sinoPassUserDTOMap.get(orgUserId);
                    setDepartment(DeptUtil.showDeptName(sinoPassUserDTO.getOrgAdminTreePathText()));
                    setApplyTime(a.getCreateTime());
                    setPermission(a.getPermission());
                    String applyType = a.getPermission().stream().map(AssetPermissionType::getShowName).collect(Collectors.joining(","));
                    setApplyType(applyType);
                    setApplyComment(a.getApplyReason());
                    setRequireAttr(RequireAttrType.DESC_MAP.get(a.getRequireAttr()));
                    setProjectName(a.getProjectName());
                }});
            });
        }

        com.sinohealth.data.common.response.PageInfo<TgLastApplicationInfo> pageInfo = new com.sinohealth.data.common.response.PageInfo<>(result,
                page.getTotal(), Integer.valueOf(Long.toString(page.getPages())), page.getCurrent());
        return AjaxResult.success(pageInfo);
    }

    @Override
    public AjaxResult<?> assetIndicatorQuery(AssetIndicatorQuery queryParam) {
        AssetIndicatorDTO result = new AssetIndicatorDTO();

        // 申请总数量
        Long assetId = queryParam.getAssetId();

        List<TgApplicationInfo> totalApplication = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getApplicantId,
                        TgApplicationInfo::getHandleNodeJson, TgApplicationInfo::getCreateTime,
                        TgApplicationInfo::getCurrentAuditProcessStatus)
                .eq(TgApplicationInfo::getNewAssetId, assetId)
                .ne(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.DRAFT)
                .notLike(TgApplicationInfo::getProjectName, "测试")
                .notLike(TgApplicationInfo::getProjectName, "test")
                .list();

        if (CollectionUtils.isEmpty(totalApplication)) {
            // 如果没有数据, 直接截断后面计算
            result.setApplicationTotalNum(0);
            result.setLastPeriodApplicationTotalNum(0);
            result.setApplicantTotalNum(0);
            result.setAverageApplicationNumPerPerson(0.0);
            return AjaxResult.success(result);
        }

        int totalApplicationCount = totalApplication.size();
        result.setApplicationTotalNum(totalApplicationCount);

        // 近一周期申请数量
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(queryParam.getLastDuration());
        String formatDateTime = sevenDaysAgo.format(formatter);
        Integer lastPeriodCount = applicationInfoMapper.selectCount(new QueryWrapper<TgApplicationInfo>() {{
            eq("new_asset_id", assetId);
            gt("create_time", formatDateTime);
            ne("current_audit_process_status", ApplicationConst.AuditStatus.DRAFT);
        }});
        result.setLastPeriodApplicationTotalNum(lastPeriodCount);

        // 申请总人数
//        Integer totalApplicant = applicationInfoMapper.selectList(new QueryWrapper<TgApplicationInfo>() {{
//            eq("new_asset_id", assetId);
//            ne("current_audit_process_status", ApplicationConst.AuditStatus.DRAFT);
//            groupBy("applicant_id");
//        }}).size();
        Integer totalApplicant = totalApplication.stream().collect(Collectors.groupingBy(TgApplicationInfo::getApplicantId)).size();
        result.setApplicantTotalNum(totalApplicant);

        // 人均申请次数
        Double averageApplicationNumPerPerson = (double) totalApplicationCount / totalApplicant;
        result.setAverageApplicationNumPerPerson(averageApplicationNumPerPerson);

        final List<TgApplicationInfo> passApplyList = totalApplication.stream()
                .filter(v -> Objects.equals(v.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDIT_PASS))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(passApplyList)) {
            return AjaxResult.success(result);
        }

        // 申请通过率
        long passCnt = totalApplication.stream()
                .filter(v -> Objects.equals(v.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDIT_PASS))
                .count();
//        Integer passCount = applicationInfoMapper.selectCount(new QueryWrapper<TgApplicationInfo>() {{
//            eq("new_asset_id", assetId);
//            eq("current_audit_process_status", ApplicationConst.AuditStatus.AUDIT_PASS);
//        }});
        Double passRate = (double) passCnt / totalApplicationCount;
        result.setPassRate(passRate);

        // 处理了第一个节点的申请列表
        List<TgApplicationInfo> handleFirstNodeApplicationList = totalApplication.stream()
                .filter(a -> {
                    JsonBeanConverter.convert2Obj(a);
                    return CollectionUtils.isNotEmpty(a.getHandleNode())
                            && a.getHandleNode().get(0).getHandleStatus().equals(CommonConstants.HANDLED);
                }).collect(Collectors.toList());

        // 最短申请审批时长
        List<Duration> auditDurationList = handleFirstNodeApplicationList.stream().map(a -> {
            List<ProcessNodeEasyDto> handleNodes = a.getHandleNode();
            if (CollectionUtils.isEmpty(handleNodes)) {
                return null;
            }
            ProcessNodeEasyDto processNodeEasyDto = handleNodes.get(0);
            String handleTime = processNodeEasyDto.getHandleTime();
            String createTime = a.getCreateTime();
            // 解析字符串为 LocalDateTime
            LocalDateTime startTime = LocalDateTime.parse(createTime, formatter);
            LocalDateTime endTime = LocalDateTime.parse(handleTime, formatter);
            // 计算时长
            return Duration.between(startTime, endTime);
        }).filter(Objects::nonNull).filter(v -> !v.isNegative()).collect(Collectors.toList());

        Duration minDuration = !auditDurationList.isEmpty() ? Collections.min(auditDurationList) : Duration.ZERO;
        double minDurationHours = minDuration.getSeconds() / 3600.0;
        result.setShortestAuditDuration(minDurationHours);

        // 平均申请审批时长
        Duration totalDuration = Duration.ZERO;
        long itemCount = auditDurationList.size();
        // 遍历 Duration 集合
        for (Duration duration : auditDurationList) {
            totalDuration = totalDuration.plus(duration);
        }
        double averageDurationHours = itemCount > 0 ? (totalDuration.getSeconds() / 3600.0 / itemCount) : 0.0;
        result.setAverageAuditDuration(averageDurationHours);

        return AjaxResult.success(result);
    }

    private Map<String, Object> transformNestedMap(Map<String, Map<String, Object>> nestedMap) {
        // 创建一个新的 Map 用于存储转换后的结果
        Map<String, Object> resultMap = new HashMap<>();

        // 遍历原始的嵌套 Map
        for (Map.Entry<String, Map<String, Object>> entry : nestedMap.entrySet()) {
            String type = entry.getKey();
            Map<String, Object> innerMap = entry.getValue();
            // 从内部 Map 中获取 count，并将其转换为 Integer
            Object countObj = innerMap.get("count");
            resultMap.put(type, countObj);
        }

        return resultMap;
    }

    @Override
    public AjaxResult<List<TableAssetsListVO>> queryFlowTable() {
        List<KeyValDict> tableList = keyValDictDAO.listFlowTable();
        if (CollectionUtils.isEmpty(tableList)) {
            return AjaxResult.success(Collections.emptyList());
        }

        List<String> shardTableList = tableList.stream().map(KeyValDict::getName)
                .map(v -> v.replace(KeyDictType.syncTableKeyPrefix, "")).collect(Collectors.toList());
        List<TableInfo> tableInfos = tableInfoMapper.selectList(new QueryWrapper<TableInfo>().lambda()
                .select(TableInfo::getId)
                .in(TableInfo::getTableNameDistributed, shardTableList));
        List<Long> tableIds = Lambda.buildList(tableInfos);

        List<TgAssetInfo> infoList = tgAssetInfoMapper.selectList(new QueryWrapper<TgAssetInfo>().lambda()
                .select(TgAssetInfo::getAssetName, TgAssetInfo::getId)
                .in(TgAssetInfo::getRelatedId, tableIds)
                .eq(TgAssetInfo::getType, AssetType.TABLE.name())
        );

        return AjaxResult.success(infoList.stream().map(v -> {
            TableAssetsListVO vo = new TableAssetsListVO();
            vo.setId(v.getId());
            vo.setAssetName(v.getAssetName());
            return vo;
        }).collect(Collectors.toList()));
    }

    /**
     * 获取工作流名称映射表
     *
     * @return 工作流映射信息
     */
    private Map<Integer, String> queryFlowMap() {
        AjaxResult ajaxResult = intergrateProcessDefService.queryProcessDefinitionList(1, 1000, null, null);
        if (ajaxResult.getCode() != 0) {
            return Collections.emptyMap();
        }

        LinkedHashMap data = (LinkedHashMap) ajaxResult.get("data");
        ArrayList<LinkedHashMap> list = (ArrayList<LinkedHashMap>) data.get("totalList");
        return list.stream().collect(Collectors.toMap(i -> Integer.valueOf(i.get("id").toString()), i -> i.get("name").toString(),
                (v1, v2) -> v2));
    }
}
