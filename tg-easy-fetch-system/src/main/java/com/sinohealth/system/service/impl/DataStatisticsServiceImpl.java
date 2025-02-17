package com.sinohealth.system.service.impl;

import cn.hutool.core.lang.Validator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.sinohealth.common.core.page.PageDomain;
import com.sinohealth.common.core.page.TableSupport;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.dataassets.AssetsSnapshotTypeEnum;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.sql.SqlUtil;
import com.sinohealth.ipaas.model.ResMaindataMainDepartmentSelectUserWithDeptItemDataItem;
import com.sinohealth.system.biz.project.mapper.ProjectMapper;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.RequireAttrType;
import com.sinohealth.system.dto.DataStatisticsDTO;
import com.sinohealth.system.dto.TgApplicationInfoDTOV2;
import com.sinohealth.system.dto.TgLoginInfoDTO;
import com.sinohealth.system.dto.TgUserAssetDTO;
import com.sinohealth.system.dto.api.cataloguemanageapi.CatalogueAllDTO;
import com.sinohealth.system.mapper.SysLogininforMapper;
import com.sinohealth.system.mapper.SysUserMapper;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgAssetInfoMapper;
import com.sinohealth.system.service.DataAssetsCatalogueService;
import com.sinohealth.system.service.DataStatisticsService;
import com.sinohealth.system.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sinohealth.system.domain.constant.RequireAttrType.PAID_REPORT;
import static com.sinohealth.system.domain.constant.RequireAttrType.REPORT;

/**
 * @author zhangyanping
 * @date 2023/11/20 17:28
 */
@Slf4j
@Service
public class DataStatisticsServiceImpl implements DataStatisticsService {

    @Resource
    private SysLogininforMapper sysLogininforMapper;
    @Resource
    private TgApplicationInfoMapper tgApplicationInfoMapper;
    @Resource
    private TgAssetInfoMapper tgAssetInfoMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private DataAssetsCatalogueService dataAssetsCatalogueService;

    @Resource
    private ProjectMapper projectMapper;

    public static final String TOTAL = "总量";

    /**
     * 日活统计
     */
    @Override
    public Integer duaCount(Date startTime, Date endTime) {
        return sysLogininforMapper.countDua(startTime, endTime);
    }


    /**
     * 部门日活统计
     */
    @Override
    public Integer depCount(Date startTime, Date endTime) {

        //查询org用户对应的活跃数
        Map<String, Map<String, Integer>> map = sysLogininforMapper.countByOrgUserId(startTime, endTime);
        Set<String> orgUserIds = map.keySet();

        if (orgUserIds.size() == 0) {
            return 0;
        }
        //查询用户所在的部门
        final List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> result
                = SinoipaasUtils.employeeWithDept(new ArrayList<>(orgUserIds));

        if (result == null) {
            return 0;
        }

        return computeDepId(result);

    }

    @Override
    public Integer dailyApplicationCount(Date startTime, Date endTime) {
        LambdaQueryWrapper<TgApplicationInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ne(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.DRAFT);
        if (startTime != null && endTime != null) {
            queryWrapper.between(TgApplicationInfo::getCreateTime, startTime, endTime);
        }
        queryWrapper.isNotNull(TgApplicationInfo::getNewAssetId);
        return tgApplicationInfoMapper.selectCount(queryWrapper);
    }

    @Override
    public Integer dailyPassedApplicationCount(Date startTime, Date endTime) {
        LambdaQueryWrapper<TgApplicationInfo> queryWrapper = new QueryWrapper<TgApplicationInfo>().lambda();
        queryWrapper.eq(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.AUDIT_PASS);
        if (startTime != null && endTime != null) {
            queryWrapper.between(TgApplicationInfo::getApplyPassedTime, startTime, endTime);
        }
        queryWrapper.isNotNull(TgApplicationInfo::getNewAssetId);
        return tgApplicationInfoMapper.selectCount(queryWrapper);
    }


    @Override
    public Map<String, Integer> countByRequireAttr(Date startTime, Date endTime, Boolean isPassed, Integer assetMenuId) {

        Map<Integer, Map<String, Integer>> map = tgApplicationInfoMapper.countByRequireAttr(startTime, endTime, isPassed, assetMenuId);

        Map<String, Integer> result = new HashMap<>(map.size());
        for (String value : RequireAttrType.DESC_MAP.values()) {
            result.put(value, 0);
        }

        for (Map.Entry<Integer, Map<String, Integer>> e : map.entrySet()) {
            String name = RequireAttrType.DESC_MAP.get(e.getKey());
            if (name != null) {
                result.put(name, Integer.parseInt(e.getValue().get("count") + ""));
            }
        }

        final Integer integer = result.get(RequireAttrType.DESC_MAP.get(PAID_REPORT));
        final Integer count = result.get(RequireAttrType.DESC_MAP.get(REPORT));
        result.put(RequireAttrType.DESC_MAP.get(REPORT), count + integer);
        result.remove(RequireAttrType.DESC_MAP.get(PAID_REPORT));

        return result;
    }

    @Override
    public Map<String, Integer> countByRequireTimeType(Date startTime, Date endTime, Boolean isPassed, Integer assetMenuId) {

        Map<String, Map<String, Integer>> map = tgApplicationInfoMapper.countByRequireTimeType(startTime, endTime, isPassed, assetMenuId);
        Map<String, Integer> result = new HashMap<>(map.size());
        for (String value : ApplicationConst.RequireTimeTypeEnum.DESC_MAP.values()) {
            result.put(value, 0);
        }
        for (Map.Entry<String, Map<String, Integer>> e : map.entrySet()) {
            String name = ApplicationConst.RequireTimeTypeEnum.DESC_MAP.get(Integer.parseInt(e.getKey()));
            result.put(name, e.getValue().get("count"));
        }
        return result;
    }

    @Override
    public List<DataStatisticsDTO> countByDayOrMonth(String dateFormat, Date startTime, Date endTime) {
        return sysLogininforMapper.countByDayOrMonth(dateFormat, startTime, endTime);
    }

    @Override
    public Map<String, List<DataStatisticsDTO>> countDuaByConditions(String countByDayOrMonth, Date startTime, Date endTime) {
        String dateFormat = "%Y-%m-%d";

        List<String> dateList;
        if ("byMonth".equals(countByDayOrMonth)) {
            dateFormat = "%Y-%m";
            dateList = DateUtil.generateContinuousMonths(startTime, endTime);
        } else {
            dateList = DateUtil.generateContinuousDays(startTime, endTime);
        }

        Map<String, DataStatisticsDTO> map1 = this.countByDayOrMonth(dateFormat, startTime, endTime).stream().collect(Collectors.toMap(DataStatisticsDTO::getDate, x -> x));
        Map<String, DataStatisticsDTO> map2 = this.countDepByDayOrMonth(dateFormat, startTime, endTime).stream().collect(Collectors.toMap(DataStatisticsDTO::getDate, x -> x));

        List<DataStatisticsDTO> res1 = new ArrayList<>(dateList.size());
        List<DataStatisticsDTO> res2 = new ArrayList<>(dateList.size());
        for (String date : dateList) {
            putOrCreateObj(map1, date, res1);
            putOrCreateObj(map2, date, res2);
        }

        Map<String, List<DataStatisticsDTO>> result = new HashMap<>();
        result.put("duaCount", res1);
        result.put("depCount", res2);
        return result;
    }

    @Override
    public List<DataStatisticsDTO> countDepByDayOrMonth(String dateFormat, Date startTime, Date endTime) {
        List<DataStatisticsDTO> list = sysLogininforMapper.countDepByDayOrMonth(dateFormat, startTime, endTime);

        Set<String> orgUserIds = new HashSet<>(64);
        for (DataStatisticsDTO e : list) {
            if (e.getOrgUserIds() != null) {
                orgUserIds.addAll(Arrays.asList(e.getOrgUserIds().split(",")));
            }
        }
        if (orgUserIds.size() == 0) {
            return Collections.emptyList();
        }

        //查询用户所在的部门
        final List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> result
                = SinoipaasUtils.employeeWithDept(new ArrayList<>(orgUserIds));
        if (result == null) {
            return Collections.emptyList();
        }

        //计算用户共计多少个部门
        for (DataStatisticsDTO e : list) {
            if (e.getOrgUserIds() == null) {
                e.setCount(0);
                continue;
            }
            Set<String> set = Stream.of(e.getOrgUserIds().split(",")).collect(Collectors.toSet());
            List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> collect = result.stream().filter(x -> set.contains(x.getId())).collect(Collectors.toList());
            e.setCount(computeDepId(collect));
            e.setOrgUserIds(null);
        }
        return list;
    }

    @Override
    public Map<String, List<DataStatisticsDTO>> countByAssetType(String countByDayOrMonth, Date startTime, Date endTime, Integer assetMenuId) {
        String dateFormat = "%Y-%m-%d";
        List<String> dateList;
        if ("byMonth".equals(countByDayOrMonth)) {
            dateFormat = "%Y-%m";
            dateList = DateUtil.generateContinuousMonths(startTime, endTime);
        } else {
            dateList = DateUtil.generateContinuousDays(startTime, endTime);
        }

        //统计模版类型
        Map<String, DataStatisticsDTO> map1 = tgAssetInfoMapper.countByAssetType(dateFormat, startTime, endTime, AssetType.MODEL.getType(), assetMenuId).stream().collect(Collectors.toMap(DataStatisticsDTO::getDate, x -> x));
        //统计库表类型
        Map<String, DataStatisticsDTO> map2 = tgAssetInfoMapper.countByAssetType(dateFormat, startTime, endTime, AssetType.TABLE.getType(), assetMenuId).stream().collect(Collectors.toMap(DataStatisticsDTO::getDate, x -> x));
        //统计文件类型
        Map<String, DataStatisticsDTO> map3 = tgAssetInfoMapper.countByAssetType(dateFormat, startTime, endTime, AssetType.FILE.getType(), assetMenuId).stream().collect(Collectors.toMap(DataStatisticsDTO::getDate, x -> x));
        //统计全部类型
        Map<String, DataStatisticsDTO> map4 = tgAssetInfoMapper.countByAssetType(dateFormat, startTime, endTime, null, assetMenuId).stream().collect(Collectors.toMap(DataStatisticsDTO::getDate, x -> x));

        List<DataStatisticsDTO> res1 = new ArrayList<>(dateList.size());
        List<DataStatisticsDTO> res2 = new ArrayList<>(dateList.size());
        List<DataStatisticsDTO> res3 = new ArrayList<>(dateList.size());
        List<DataStatisticsDTO> res4 = new ArrayList<>(dateList.size());
        for (String date : dateList) {
            putOrCreateObj(map1, date, res1);
            putOrCreateObj(map2, date, res2);
            putOrCreateObj(map3, date, res3);
            putOrCreateObj(map4, date, res4);
        }

        Map<String, List<DataStatisticsDTO>> result = new HashMap<>();
        result.put(AssetType.MODEL.getType(), res1);
        result.put(AssetType.TABLE.getType(), res2);
        result.put(AssetType.FILE.getType(), res3);
        result.put("ALL", res4);
        return result;

    }

    @Override
    public Map<String, List<DataStatisticsDTO>> countDepAssetByConditions(String countByDayOrMonth, Date startTime, Date endTime, Integer assetMenuId) {
        List<DataStatisticsDTO> list = tgApplicationInfoMapper.countPassedApplicationByConditions(startTime, endTime, assetMenuId, null, null, null);

        //查询用户所在的部门
        Set<String> orgUserIds = list.stream().map(DataStatisticsDTO::getOrgUserIds).collect(Collectors.toSet());

        if (orgUserIds.size() == 0) {
            return Collections.emptyMap();
        }

        final List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> deptList = SinoipaasUtils.employeeWithDept(new ArrayList<>(orgUserIds));
        if (deptList == null) {
            return Collections.emptyMap();
        }

        //部门和用户的映射
        Map<String, Set<String>> depUserMap = computeDepUserByPath(deptList);

        //类型和用户的映射
        Map<String, List<DataStatisticsDTO>> typeCountMap = list.stream().collect(Collectors.groupingBy(DataStatisticsDTO::getType));

        //部门和类型的映射，通过用户关联
        Map<String, List<DataStatisticsDTO>> result = new HashMap<>(depUserMap.size());

        for (Map.Entry<String, Set<String>> entry : depUserMap.entrySet()) {
            Set<String> userIdSet = entry.getValue();
            DataStatisticsDTO fileCount = count(typeCountMap, AssetType.FILE.getType(), userIdSet);
            DataStatisticsDTO modelCount = count(typeCountMap, AssetType.MODEL.getType(), userIdSet);
            DataStatisticsDTO tableCount = count(typeCountMap, AssetType.TABLE.getType(), userIdSet);

            result.put(entry.getKey(), Arrays.asList(fileCount, modelCount, tableCount));
        }

        return result;
    }

    @Override
    public List<TgLoginInfoDTO> queryLoginInfoByPage(Date startTime, Date endTime, String searchKey, Integer pageSize) {
        Set<Long> userIds = loadAllUserByDepName(searchKey);
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        if (Validator.isNotNull(pageNum) && Validator.isNotNull(pageSize)) {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.startPage(pageNum, pageSize == null ? pageDomain.getPageNum() : pageSize, orderBy);
        }
        List<TgLoginInfoDTO> list = sysLogininforMapper.queryLoginInfoByPage(startTime, endTime, searchKey, userIds);
        Set<String> orgUserIds = list.stream().map(TgLoginInfoDTO::getOrgUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> map = queryOrgUserInfo(orgUserIds);

        for (TgLoginInfoDTO dto : list) {
            ResMaindataMainDepartmentSelectUserWithDeptItemDataItem dep = map.get(dto.getOrgUserId());
            if (dep != null) {
                final String orgAdminTreePathText = dep.getOrgAdminTreePathText();
                final List<String> fullPath = Arrays.asList(orgAdminTreePathText.split("/"));
                if (fullPath.size() > 4) {
                    dto.setOrgName(fullPath.get(fullPath.size() - 2) + "/" + fullPath.get(fullPath.size() - 1));
                    dto.setApplicantDepartment(fullPath.get(4));
                } else if (fullPath.size() >= 3) {
                    dto.setOrgName(fullPath.get(fullPath.size() - 2) + "/" + fullPath.get(fullPath.size() - 1));
                    dto.setApplicantDepartment("其他");
                } else {
                    dto.setOrgName(orgAdminTreePathText);
                    dto.setApplicantDepartment("其他");
                }
                dto.setApplicantName(dep.getUserName());
            }

        }
        return list;
    }


    @Override
    public List<TgApplicationInfoDTOV2> queryApplicationInfoByPage(Date startTime, Date endTime, String searchKey, String assetName, String assetType, Integer assetMenuId, Integer pageSize, String assetMenuName) {
        Set<Long> userIds = loadAllUserByDepName(searchKey);

        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        if (Validator.isNotNull(pageNum) && Validator.isNotNull(pageSize)) {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.startPage(pageNum, pageSize == null ? pageDomain.getPageNum() : pageSize, orderBy);
        }

        List<TgApplicationInfoDTOV2> list = tgApplicationInfoMapper.queryApplicationInfoByPage(startTime, endTime, searchKey, assetName, assetType, assetMenuId, userIds, assetMenuName);

        Set<String> orgUserIds = list.stream().map(TgApplicationInfoDTOV2::getOrgUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> map = queryOrgUserInfo(orgUserIds);

        for (TgApplicationInfoDTOV2 dto : list) {
            ResMaindataMainDepartmentSelectUserWithDeptItemDataItem dep = map.get(dto.getOrgUserId());
            //设置部门
            if (dep != null) {
                final String orgAdminTreePathText = dep.getOrgAdminTreePathText();
                final List<String> fullPath = Arrays.asList(orgAdminTreePathText.split("/"));
                if (fullPath.size() > 4) {
                    dto.setOrgName(fullPath.get(fullPath.size() - 2) + "/" + fullPath.get(fullPath.size() - 1));
                    dto.setApplicantDepartment(fullPath.get(4));
                } else if (fullPath.size() >= 3) {
                    dto.setOrgName(fullPath.get(fullPath.size() - 2) + "/" + fullPath.get(fullPath.size() - 1));
                    dto.setApplicantDepartment("其他");
                } else {
                    dto.setOrgName(orgAdminTreePathText);
                    dto.setApplicantDepartment("其他");
                }
                dto.setApplicantName(dep.getUserName());
            }

            AssetType e = AssetType.valueOf(dto.getAssetType());
            dto.setAssetType(e.getName());

            if (StringUtils.isNotEmpty(dto.getServiceType())) {
                List<AssetPermissionType> types = JsonUtils.parseArray(dto.getServiceType(), AssetPermissionType.class);
                if (types != null && types.size() > 0) {
                    AssetPermissionType assetPermissionType = types.get(0);
                    dto.setServiceType(assetPermissionType.getShowName());
                } else {
                    dto.setServiceType(null);
                }
            }
        }
        return list;
    }


    @Override
    public List<DataStatisticsDTO> countUserDataAsset(String countByDayOrMonth, Date startTime, Date endTime, Integer assetMenuId) {
        String dateFormat = "%Y-%m-%d";
        List<String> dateList;
        if ("byMonth".equals(countByDayOrMonth)) {
            dateFormat = "%Y-%m";
            dateList = DateUtil.generateContinuousMonths(startTime, endTime);
        } else {
            dateList = DateUtil.generateContinuousDays(startTime, endTime);
        }

        Map<String, DataStatisticsDTO> map = tgApplicationInfoMapper.countUserDataAsset(dateFormat, startTime, endTime, AssetType.MODEL.getType(), assetMenuId).stream().collect(Collectors.toMap(DataStatisticsDTO::getDate, x -> x));
        List<DataStatisticsDTO> res1 = new ArrayList<>(dateList.size());
        for (String date : dateList) {
            putOrCreateObj(map, date, res1);
        }
        return res1;
    }

    @Override
    public List<TgUserAssetDTO> queryUserAssetInfoByPage(Date startTime, Date endTime, String searchKey, String assetName, String serviceType, Integer assetMenuId, Integer pageSize, String assetMenuName) {
        Set<Long> userIds = loadAllUserByDepName(searchKey);
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        if (Validator.isNotNull(pageNum) && Validator.isNotNull(pageSize)) {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.startPage(pageNum, pageSize == null ? pageDomain.getPageNum() : pageSize, orderBy);
        }
        List<TgUserAssetDTO> list = tgAssetInfoMapper.queryUserAssetInfoByPage(startTime, endTime, null, assetName, serviceType, assetMenuId, userIds, assetMenuName);
        Set<String> orgUserIds = list.stream().map(TgUserAssetDTO::getOrgUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> map = queryOrgUserInfo(orgUserIds);

        for (TgUserAssetDTO dto : list) {
            ResMaindataMainDepartmentSelectUserWithDeptItemDataItem dep = map.get(dto.getOrgUserId());
            //设置部门
            if (dep != null) {
                final String orgAdminTreePathText = dep.getOrgAdminTreePathText();
                final List<String> fullPath = Arrays.asList(orgAdminTreePathText.split("/"));
                if (fullPath.size() > 4) {
                    dto.setOrgName(fullPath.get(fullPath.size() - 2) + "/" + fullPath.get(fullPath.size() - 1));
                    dto.setApplicantDepartment(fullPath.get(4));
                } else if (fullPath.size() >= 3) {
                    dto.setOrgName(fullPath.get(fullPath.size() - 2) + "/" + fullPath.get(fullPath.size() - 1));
                    dto.setApplicantDepartment("其他");
                } else {
                    dto.setOrgName(orgAdminTreePathText);
                    dto.setApplicantDepartment("其他");
                }
                dto.setApplicantName(dep.getUserName());
            }

            if (StringUtils.isNotEmpty(dto.getRequireTimeType())) {
                dto.setRequireTimeType(ApplicationConst.RequireTimeTypeEnum.DESC_MAP.get(Integer.parseInt(dto.getRequireTimeType())));
            }

            if (StringUtils.isNotEmpty(dto.getServiceType())) {
                dto.setServiceType(AssetsSnapshotTypeEnum.snapshotMap.get(dto.getServiceType()));
            }

        }
        return list;
    }


    /**
     * 按目录统计申请趋势
     */
    @Override
    public Map<String, List<DataStatisticsDTO>> countByCatalogId(String countByDayOrMonth, Date startTime, Date endTime, Integer assetMenuId, Integer countType) {

        String dateFormat = "%Y-%m-%d";
        List<String> dateList;
        if ("byMonth".equals(countByDayOrMonth)) {
            dateFormat = "%Y-%m";
            dateList = DateUtil.generateContinuousMonths(startTime, endTime);
        } else {
            dateList = DateUtil.generateContinuousDays(startTime, endTime);
        }

        List<CatalogueAllDTO> catalogList = dataAssetsCatalogueService.getCatalogueWithoutPermission();
        CatalogueAllDTO totalCountDTO = new CatalogueAllDTO();
        totalCountDTO.setName(TOTAL);
        totalCountDTO.setId(assetMenuId);
        boolean isRootCatalogue = assetMenuId == null || assetMenuId == 0;
        if (!isRootCatalogue) {
            for (CatalogueAllDTO dto : catalogList) {
                if (dto.getId().equals(assetMenuId)) {
                    //统计二级目录和父级的总量
                    catalogList = dto.getChild();
                    break;
                }
            }
        }
        if (catalogList == null) {
            catalogList = new ArrayList<>();
        }

        catalogList.add(0, totalCountDTO);

        Map<String, List<DataStatisticsDTO>> result = new LinkedHashMap<>();

        //逐个统计
        for (CatalogueAllDTO dto : catalogList) {
            //如果是二级目录，总量不需要统计一级目录
            Map<String, DataStatisticsDTO> map;
            if (countType == null || countType == 1) {
                //分目录统计申请趋势
                map = tgAssetInfoMapper.countByAssetType(dateFormat, startTime, endTime, AssetType.MODEL.getType(), dto.getId()).stream().collect(Collectors.toMap(DataStatisticsDTO::getDate, x -> x));
            } else {
                //分目录统计出数趋势
                map = tgApplicationInfoMapper.countUserDataAsset(dateFormat, startTime, endTime, AssetType.MODEL.getType(), dto.getId()).stream().collect(Collectors.toMap(DataStatisticsDTO::getDate, x -> x));
            }
            List<DataStatisticsDTO> list = new ArrayList<>(dateList.size());
            for (String date : dateList) {
                putOrCreateObj(map, date, list);
            }
            result.put(dto.getName(), list);
        }
        return result;
    }

    @Override
    public Map<String, Map<String, Integer>> countDepApplicationByCatalogId(Date startTime, Date endTime, Integer assetMenuId, Integer countType) {
        List<DataStatisticsDTO> list;
        if (countType == null || countType == 1) {
            list = tgApplicationInfoMapper.countPassedApplicationByConditions(startTime, endTime, assetMenuId, null, null, AssetType.MODEL.getType());
        } else {
            list = tgApplicationInfoMapper.countUserDataAssetByUserId(startTime, endTime, AssetType.MODEL.getType(), assetMenuId, null);
        }

        //查询用户所在的部门
        Set<String> orgUserIds = list.stream().map(DataStatisticsDTO::getOrgUserIds).collect(Collectors.toSet());
        if (orgUserIds.size() == 0) {
            return Collections.emptyMap();
        }

        final List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> deptList = SinoipaasUtils.employeeWithDept(new ArrayList<>(orgUserIds));
        if (deptList == null) {
            return Collections.emptyMap();
        }

        //部门和用户的映射
        Map<String, Set<String>> depUserMap = computeDepUserByPath(deptList);

        //查询分类
        List<CatalogueAllDTO> catalogList = dataAssetsCatalogueService.getCatalogueWithoutPermission();
        boolean isRootCatalogue = assetMenuId == null || assetMenuId == 0;
        if (!isRootCatalogue) {
            for (CatalogueAllDTO dto : catalogList) {
                if (dto.getId().equals(assetMenuId)) {
                    catalogList = dto.getChild();
                    break;
                }
            }
        }
        if (catalogList == null) {
            catalogList = new ArrayList<>();
        }

        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : depUserMap.entrySet()) {
            if (catalogList.isEmpty()) {
                continue;
            }
            Map<String, Integer> itemMap = new LinkedHashMap<>();
            for (CatalogueAllDTO dto : catalogList) {
                List<DataStatisticsDTO> dataList;
                if (countType == null || countType == 1) {
                    //分目录统计申请趋势
                    dataList = tgApplicationInfoMapper.countPassedApplicationByConditions(startTime, endTime, dto.getId(), null, entry.getValue(), AssetType.MODEL.getType());
                } else {
                    dataList = tgApplicationInfoMapper.countUserDataAssetByUserId(startTime, endTime, AssetType.MODEL.getType(), dto.getId(), entry.getValue());
                }

                int count = 0;
                for (DataStatisticsDTO statisticsDTO : dataList) {
                    count += statisticsDTO.getCount();
                }
                itemMap.put(dto.getName(), count);
            }
            result.put(entry.getKey(), itemMap);
        }
        return result;
    }


    /**
     * 根据名称模糊检索
     */
    private Set<Long> loadAllUserByDepName(String depName) {
        if (StringUtils.isEmpty(depName)) {
            return Collections.emptySet();
        }
        final List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> deptList = SinoipaasUtils.employeeWithDept(new ArrayList<>());
        if (deptList == null) {
            return Collections.emptySet();
        }

        Set<String> orgUserIds = deptList.stream().filter(x -> {
            final List<String> fullPath = Arrays.asList(x.getOrgAdminTreePathText().split("/"));
            String dep;
            if (fullPath.size() > 4) {
                dep = fullPath.get(fullPath.size() - 2) + "/" + fullPath.get(fullPath.size() - 1) + "/" + fullPath.get(4);
            } else if (fullPath.size() >= 3) {
                dep = fullPath.get(fullPath.size() - 2) + "/" + fullPath.get(fullPath.size() - 1) + "/其他";
            } else {
                dep = x.getOrgAdminTreePathText() + "/其他";
            }
            return dep.contains(depName) || x.getUserName().contains(depName);
        }).map(ResMaindataMainDepartmentSelectUserWithDeptItemDataItem::getId).collect(Collectors.toSet());
        if (orgUserIds.isEmpty()) {
            return Collections.singleton(-1L);
        }
        return sysUserMapper.findIdByOrgIds(orgUserIds);
    }

    private Map<String, ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> queryOrgUserInfo(Set<String> orgUserIds) {
        if (CollectionUtils.isEmpty(orgUserIds)) {
            return Collections.emptyMap();
        }
        final List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> deptList = SinoipaasUtils.employeeWithDept(new ArrayList<>(orgUserIds));
        if (deptList == null) {
            return Collections.emptyMap();
        }
        return deptList.stream().collect(Collectors.toMap(ResMaindataMainDepartmentSelectUserWithDeptItemDataItem::getId, x -> x));
    }

    private DataStatisticsDTO count(Map<String, List<DataStatisticsDTO>> typeCountMap, String type, Set<String> userIdSet) {
        List<DataStatisticsDTO> temp = typeCountMap.get(type);
        if (CollectionUtils.isEmpty(temp)) {
            return new DataStatisticsDTO(type, 0);
        }
        int cnt = 0;
        for (DataStatisticsDTO e : temp) {
            if (userIdSet.contains(e.getOrgUserIds())) {
                cnt += e.getCount();
            }
        }
        return new DataStatisticsDTO(type, cnt);
    }

    private Integer computeDepId(List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> result) {
        //汇总部门统计数据
        Set<String> set = new HashSet<>();
        String other = "其他";
        for (ResMaindataMainDepartmentSelectUserWithDeptItemDataItem item : result) {
            //组织机构/广州中康数字科技有限公司/中台组织/中康大数据研究院/数据中心/商用数据部/数据运营组
            final String orgAdminTreePathText = item.getOrgAdminTreePathText();
            final List<String> fullPath = Arrays.asList(orgAdminTreePathText.split("/"));
            if (fullPath.size() > 4) {
                String path = fullPath.get(3) + "/" + fullPath.get(4);
                set.add(path);
            } else {
                set.add(other);
            }
        }
        return set.size();
    }


    /**
     * 根据三级部门进行分组
     */
    private Map<String, Set<String>> computeDepUserByPath(List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> result) {
        //按照部门将用户分组
        String other = "其他";
        Map<String, Set<String>> depUserMap = new HashMap<>(result.size());

        for (ResMaindataMainDepartmentSelectUserWithDeptItemDataItem item : result) {
            //组织机构/广州中康数字科技有限公司/中台组织/中康大数据研究院/数据中心/商用数据部/数据运营组
            final String orgAdminTreePathText = item.getOrgAdminTreePathText();
            final List<String> fullPath = Arrays.asList(orgAdminTreePathText.split("/"));
            if (fullPath.size() > 4) {
                String path = fullPath.get(3) + "/" + fullPath.get(4);
                depUserMap.computeIfAbsent(path, v -> new HashSet<>()).add(item.getId());
            } else {
                depUserMap.computeIfAbsent(other, v -> new HashSet<>()).add(item.getId());
            }
        }
        return depUserMap;
    }


    private void putOrCreateObj(Map<String, DataStatisticsDTO> map, String date, List<DataStatisticsDTO> res) {
        if (map.containsKey(date)) {
            res.add(map.get(date));
        } else {
            res.add(new DataStatisticsDTO(date));
        }
    }
}
