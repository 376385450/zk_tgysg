package com.sinohealth.system.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.framework.common.utils.StringUtils;
import com.google.common.collect.Lists;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.AuthItemEnum;
import com.sinohealth.common.enums.CataloguePermissionEnum;
import com.sinohealth.common.enums.WhitlistServiceType;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.ipaas.model.ResMaindataMainDepartmentSelectAllItemDataItem;
import com.sinohealth.ipaas.model.ResMaindataMainDepartmentSelectUserWithDeptItemDataItem;
import com.sinohealth.ipaas.model.ResMaindatamainDepartmentselectbyidsItemDataItem;
import com.sinohealth.system.dao.AssetsCatalogueDAO;
import com.sinohealth.system.dao.AssetsCataloguePermissionDAO;
import com.sinohealth.system.dao.TgAssetAuthWhiltlistInfoDAO;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgAssetWhitelistInfo;
import com.sinohealth.system.domain.catalogue.AssetsCatalogue;
import com.sinohealth.system.domain.catalogue.AssetsCataloguePermission;
import com.sinohealth.system.dto.api.cataloguemanageapi.*;
import com.sinohealth.system.dto.assets.MenuNameDto;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.AssetsCatalogueMapper;
import com.sinohealth.system.mapper.SysUserMapper;
import com.sinohealth.system.mapper.TgAssetInfoMapper;
import com.sinohealth.system.service.DataAssetsCatalogueService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author shallwetalk
 * @Date 2023/8/10
 */
@Service
public class DataAssetsCatalogueServiceImpl implements DataAssetsCatalogueService {

    @Autowired
    AssetsCatalogueMapper assetsCatalogueMapper;

    @Autowired
    AssetsCatalogueDAO assetsCatalogueDAO;

    @Autowired
    AssetsCataloguePermissionDAO assetsCataloguePermissionDAO;

    @Autowired
    SysUserMapper sysUserMapper;

    @Autowired
    TgAssetInfoMapper tgAssetInfoMapper;

    @Autowired
    TgAssetAuthWhiltlistInfoDAO tgAssetAuthWhiltlistInfoDAO;

    @Override
    public CatalogueDetailDTO getCatalogueBaseInfo(Integer id) {
        final AssetsCatalogue assetsCatalogue = assetsCatalogueMapper.selectById(id);
        final CatalogueDetailDTO catalogueDetailDTO = new CatalogueDetailDTO();
        BeanUtils.copyProperties(assetsCatalogue, catalogueDetailDTO);

        final SysUser sysUser = sysUserMapper.selectUserById(assetsCatalogue.getCreatedBy());
        catalogueDetailDTO.setCreatedBy(sysUser.getRealName());
        final SysUser sysUser1 = sysUserMapper.selectUserById(assetsCatalogue.getUpdatedBy());
        catalogueDetailDTO.setUpdatedBy(sysUser1.getRealName());
        if (Objects.nonNull(assetsCatalogue.getParentId())) {
            final AssetsCatalogue parent = assetsCatalogueMapper.selectById(assetsCatalogue.getParentId());
            catalogueDetailDTO.setParentName(parent.getName());
        }

        // 权限集合
        final List<AssetsCataloguePermission> permissionsByCatalogueId = assetsCataloguePermissionDAO.getAllPermissionsByCatalogueId(id);

        if (CollUtil.isNotEmpty(permissionsByCatalogueId)) {

            final List<Long> userIds = permissionsByCatalogueId.stream()
                    .filter(a -> a.getType().equals(CataloguePermissionEnum.USER.getType()))
                    .map(AssetsCataloguePermission::getUserId).collect(Collectors.toList());

            final Map<Long, SysUser> userMap = new HashMap<Long, SysUser>();
            if (CollUtil.isNotEmpty(userIds)) {
                userMap.putAll(sysUserMapper.selectUserByIds(userIds).stream().collect(Collectors.toMap(k -> k.getUserId(), v -> v)));
            }

            final List<String> deptIds = permissionsByCatalogueId.stream()
                    .filter(a -> a.getType().equals(CataloguePermissionEnum.DEPT.getType()))
                    .map(AssetsCataloguePermission::getDeptId).collect(Collectors.toList());

            final Map<String, ResMaindatamainDepartmentselectbyidsItemDataItem> deptMap = new HashMap<String, ResMaindatamainDepartmentselectbyidsItemDataItem>();
            if (CollUtil.isNotEmpty(deptIds)) {
                deptMap.putAll(SinoipaasUtils.mainDepartmentSelectbyids(deptIds).stream().collect(Collectors.toMap(k -> k.getId(), v -> v)));
            }

            final List<UserRightsDTO> rights = permissionsByCatalogueId.stream().map(permission -> {
                final UserRightsDTO userRightsDTO = new UserRightsDTO();
                BeanUtils.copyProperties(permission, userRightsDTO);
                if (permission.getType().equals(CataloguePermissionEnum.USER.getType())) {
                    // 填充用户
                    userRightsDTO.setUserName(userMap.get(permission.getUserId()).getRealName());
                } else {
                    // 填充部门
                    userRightsDTO.setDeptName(deptMap.get(permission.getDeptId()).getDepartName());
                }
                return userRightsDTO;
            }).collect(Collectors.toList());

            catalogueDetailDTO.setUserRightsDTOS(rights);
        }

        return catalogueDetailDTO;
    }

    @Override
    public List<CatalogueAllDTO> getCatalogueWithoutPermission() {
        final LambdaQueryWrapper<AssetsCatalogue> wq = Wrappers.<AssetsCatalogue>lambdaQuery()
                .eq(AssetsCatalogue::getDeleted, 0);

        final List<AssetsCatalogue> assetsCatalogues = assetsCatalogueMapper.selectList(wq);

        // 只返回三层
        Integer level = 3;

        return buildAllCatalogue(null, assetsCatalogues, level);
    }

    @Override
    public List<UserPermissionDTO> getInheritedPermissions(Integer parentId) {
        if (Objects.isNull(parentId)) {
            return new ArrayList<UserPermissionDTO>();
        }

        final AssetsCatalogue assetsCatalogue = assetsCatalogueMapper.selectById(parentId);
        // 获取所有父节点id
        final String path = assetsCatalogue.getPath();
        final List<Integer> ids = Arrays.stream(path.split("/"))
                .filter(StringUtils::isNotEmpty).map(Integer::parseInt).collect(Collectors.toList());

        // 获取所有权限，拼接userName和deptName
        final List<AssetsCataloguePermission> allPermissionsByCatalogue = assetsCataloguePermissionDAO.getAllPermissionsByCatalogueIds(ids);
        if (CollUtil.isEmpty(allPermissionsByCatalogue)) {
            return null;
        }
        final Map<Integer, List<AssetsCataloguePermission>> mapByType = allPermissionsByCatalogue.stream().collect(Collectors.groupingBy(AssetsCataloguePermission::getType));
        final List<Long> userIds = Optional.ofNullable(mapByType.get(CataloguePermissionEnum.USER.getType())).orElse(new ArrayList<AssetsCataloguePermission>()).stream().map(AssetsCataloguePermission::getUserId).collect(Collectors.toList());
        final List<SysUser> sysUsers = sysUserMapper.selectUserByIds(userIds);
        final Map<Long, SysUser> userIdMap = sysUsers.stream().collect(Collectors.toMap(k -> k.getUserId(), v -> v));

        final List<String> deptIds = Optional.ofNullable(mapByType.get(CataloguePermissionEnum.DEPT.getType())).orElse(new ArrayList<AssetsCataloguePermission>()).stream().map(AssetsCataloguePermission::getDeptId).collect(Collectors.toList());
        List<ResMaindatamainDepartmentselectbyidsItemDataItem> mainDepartment = Lists.newArrayList();
        if (CollUtil.isNotEmpty(deptIds)) {
            mainDepartment = SinoipaasUtils.mainDepartmentSelectbyids(deptIds);
        }
        final Map<String, ResMaindatamainDepartmentselectbyidsItemDataItem> deptIdMap = mainDepartment.stream().collect(Collectors.toMap(k -> k.getId(), v -> v));

        // 拼凑权限合集并返回
        return allPermissionsByCatalogue.stream()
            .collect(Collectors.groupingBy(a -> {
                if (a.getType().equals(CataloguePermissionEnum.USER.getType())) {
                    return a.getUserId();
                }else {
                    return a.getDeptId();
                }
            }))
            .entrySet().stream()
            .map(entry -> entry.getValue().stream()
                    .reduce((u1, u2) -> {
                        u2.setReadable(u1.getReadable() | u2.getReadable());
                        u2.setAssetsManager(u1.getAssetsManager() | u2.getAssetsManager());
                        u2.setCatalogueManager(u1.getCatalogueManager() | u2.getCatalogueManager());
                        return u2;
                    }).orElse(null)).filter(Objects::nonNull)
            .collect(Collectors.toList())
            .stream()
            .map(catalogue -> {
                final UserPermissionDTO userPermissionDTO = new UserPermissionDTO();
                BeanUtils.copyProperties(catalogue, userPermissionDTO);
                if (userPermissionDTO.getType().equals(CataloguePermissionEnum.USER.getType())) {
                    // 填充人员名称
                    userPermissionDTO.setRealName(userIdMap.get(catalogue.getUserId()).getRealName());
                } else {
                    // 填充部门名称
                    userPermissionDTO.setDeptName(deptIdMap.get(catalogue.getDeptId()).getDepartName());
                }
                return userPermissionDTO;
            }).collect(Collectors.toList());
    }


    @Override
    public List<CatalogueQueryDTO> getCatalogueTree() {

        // 获取拥有权限的所有目录
        final Long userId = SecurityUtils.getUserId();

        final SinoPassUserDTO o = (SinoPassUserDTO)ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO);
        final List<AssetsCataloguePermission> allPermission = assetsCataloguePermissionDAO.findByUserIdAndDeptId(userId, o.getMainOrganizationId());

        // 筛选出有目录管理权限的目录
        final List<AssetsCataloguePermission> catalogueManagerList = allPermission.stream()
                .filter(permission -> permission.getCatalogueManager().equals(1))
                .collect(Collectors.toList());


        if (catalogueManagerList.isEmpty()) {
            // 不存在有权限的目录
            return null;
        }

        // 获取有管理权限下目录路径上所有的目录
        final List<Integer> catalogueIds = catalogueManagerList.stream()
                .map(permission -> permission.getCatalogueId())
                .collect(Collectors.toList());

        final List<AssetsCatalogue> catalogues = assetsCatalogueDAO.selectListInIds(catalogueIds);

        final List<Integer> parentIds = getAllParentIds(catalogues);


        // 获取所有有权限下的子节点
        final List<AssetsCatalogue> assetsCatalogues = assetsCatalogueDAO.selectListInPath(catalogueIds);

        // 补充父级节点
        final List<Integer> ids = parentIds.stream()
                .filter(id -> !catalogueIds.contains(id))
                .collect(Collectors.toSet()).stream().collect(Collectors.toList());

        if (!ids.isEmpty()) {
            assetsCatalogues.addAll(assetsCatalogueDAO.selectListInIds(ids));
        }

        final List<AssetsCatalogue> collect = assetsCatalogues.stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(AssetsCatalogue::getId))), ArrayList::new))
                .stream()
                .sorted(Comparator.comparing(AssetsCatalogue::getSortOrder)
                        .thenComparing(AssetsCatalogue::getCreatedAt))
                .collect(Collectors.toList());

        List<CatalogueQueryDTO> result = buildCataloguesTree(null, collect, catalogueIds);

        return result;
    }


    @Override
    public CatalogueDataReadTree getReadAbleCatalogue() {
        // 获取拥有权限的所有目录
        final Long userId = SecurityUtils.getUserId();
        final SinoPassUserDTO o = (SinoPassUserDTO)ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO);
        final List<AssetsCataloguePermission> allPermission = assetsCataloguePermissionDAO.findByUserIdAndDeptId(userId, o.getMainOrganizationId());

        final List<Integer> readCatalogueId = allPermission.stream()
                .filter(permission -> permission.getReadable().equals(1))
                .map(AssetsCataloguePermission::getCatalogueId)
                .collect(Collectors.toSet())
                .stream()
                .collect(Collectors.toList());

        final CatalogueDataReadTree catalogueDataReadTree = new CatalogueDataReadTree();
        /*if (CollUtil.isEmpty(readCatalogueId)) {
            return catalogueDataReadTree;
        }*/

        // 拥有可阅读权限的所有子级目录
        List<AssetsCatalogue> assetsCatalogues = assetsCatalogueDAO.selectListInPath(readCatalogueId);
        if (CollUtil.isEmpty(readCatalogueId)) {
            assetsCatalogues = new ArrayList<>();
        }

        //复制一份
        //List<AssetsCatalogue> permissionCatalogues = Lists.newCopyOnWriteArrayList(assetsCatalogues);

        // 补充父级节点与有可阅读权限的资产存在的目录

        // 已挂接可阅读的资产目录
        // 获取所有可阅读权限的资产
        final List<TgAssetWhitelistInfo> infos = tgAssetAuthWhiltlistInfoDAO.findWhiteListInfoByUserIdAndDeptId(SecurityUtils.getUserId(), o.getMainOrganizationId())
                .stream().filter(info -> info.getServiceType().equals(WhitlistServiceType.READABLE))
                .collect(Collectors.toList());

        if (!infos.isEmpty()) {
            final List<TgAssetInfo> info = tgAssetInfoMapper.findByTgAssetAuthWhiltlistInfo(infos);
            final List<Integer> assetCatalogueIds = info.stream()
                    .map(TgAssetInfo::getAssetMenuId)
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(assetCatalogueIds)) {
                assetsCatalogues.addAll(assetsCatalogueDAO.selectListInIds(assetCatalogueIds));
            }
        }

        // 父级目录
        final List<Integer> parentIds = getAllParentIds(assetsCatalogues);


        // 过滤已存在的
        final List<Integer> ids = parentIds.stream()
                .filter(id -> !readCatalogueId.contains(id))
                .collect(Collectors.toSet()).stream().collect(Collectors.toList());

        if (!ids.isEmpty()) {
            assetsCatalogues.addAll(assetsCatalogueDAO.selectListInIds(ids));
        }

        // 获取统计计数
        /*final List<TgAssetInfo> allAsset = tgAssetInfoMapper.findAllByAssetMenuId(permissionCatalogues.stream().map(AssetsCatalogue::getId).collect(Collectors.toList()),
                assetsCatalogues.stream().map(AssetsCatalogue::getId).collect(Collectors.toList()),
                userId, o.getMainOrganizationId());

        // 获取
        final Map<Integer, List<TgAssetInfo>> assetMenuIdMap = allAsset.stream().collect(Collectors.groupingBy(TgAssetInfo::getAssetMenuId));
*/
        final List<AssetsCatalogue> collect = assetsCatalogues.stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(AssetsCatalogue::getId))), ArrayList::new))
                .stream()
                .sorted(Comparator.comparing(AssetsCatalogue::getSortOrder)
                        .thenComparing(AssetsCatalogue::getCreatedAt))
                .collect(Collectors.toList());

        List<CatalogueReadAbleDTO> result = buildCataloguesReadableTree(null, collect, readCatalogueId);

        catalogueDataReadTree.setTree(result);
//        catalogueDataReadTree.setCatalogueCount(assetsCatalogues.size());
//        catalogueDataReadTree.setAssetsCount(result.stream().collect(Collectors.summingInt(CatalogueReadAbleDTO::getAssetCount)));

        return catalogueDataReadTree;
    }



    @Override
    public List<CatalogueAssetManageAbleDTO> getAssetsManageAbleCatalogue() {

        // 获取拥有权限的所有目录
        final Long userId = SecurityUtils.getUserId();
        final SinoPassUserDTO o = (SinoPassUserDTO)ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO);
        final List<AssetsCataloguePermission> allPermission = assetsCataloguePermissionDAO.findByUserIdAndDeptId(userId, o.getMainOrganizationId());

        // 筛选出有目录管理权限的目录
        final List<AssetsCataloguePermission> catalogueManagerList = allPermission.stream()
                .filter(permission -> permission.getAssetsManager().equals(1))
                .collect(Collectors.toList());


        if (catalogueManagerList.isEmpty()) {
            // 不存在有权限的目录
            return new ArrayList<>();
        }

        // 获取有管理权限下目录路径上所有的目录
        final List<Integer> catalogueIds = catalogueManagerList.stream()
                .map(permission -> permission.getCatalogueId())
                .collect(Collectors.toList());

        final List<AssetsCatalogue> catalogues = assetsCatalogueDAO.selectListInIds(catalogueIds);

        final List<Integer> parentIds = getAllParentIds(catalogues);

        // 获取所有有权限下的子节点
        final List<AssetsCatalogue> assetsCatalogues = assetsCatalogueDAO.selectListInPath(catalogueIds);

        // 补充父级节点
        final List<Integer> ids = parentIds.stream()
                .filter(id -> !catalogueIds.contains(id))
                .collect(Collectors.toSet()).stream().collect(Collectors.toList());

        if (!ids.isEmpty()) {
            assetsCatalogues.addAll(assetsCatalogueDAO.selectListInIds(ids));
        }

        final List<AssetsCatalogue> collect = assetsCatalogues.stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(AssetsCatalogue::getId))), ArrayList::new))
                .stream()
                .sorted(Comparator.comparing(AssetsCatalogue::getSortOrder)
                        .thenComparing(AssetsCatalogue::getCreatedAt))
                .collect(Collectors.toList());

        final Map<Integer, AssetsCatalogue> catalogueIdNameMap = assetsCatalogueMapper.selectList(Wrappers.<AssetsCatalogue>lambdaQuery().eq(AssetsCatalogue::getDeleted, 0))
                .stream().collect(Collectors.toMap(k -> k.getId(), v -> v));

        List<CatalogueAssetManageAbleDTO> result = buildCataloguesAssetsManageTree(null, collect, catalogueIds, catalogueIdNameMap);

        return result;
    }

    @Override
    public Page<UserDTO> getSelectedUser(Integer pageNum, Integer pageSize, String name) {
        final LambdaQueryWrapper<SysUser> eq = Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getStatus, 0)
                .like(StringUtils.isNotEmpty(name), SysUser::getRealName, name)
                .eq(SysUser::getDelFlag, 0)
                .eq(SysUser::getUserInfoType, 2);
        final IPage<SysUser> page = new Page<>(pageNum, pageSize);
        final IPage<SysUser> sysUserIPage = sysUserMapper.selectPage(page, eq);

        // 获取所有id
        final List<String> userIds = sysUserIPage.getRecords().stream().map(SysUser::getOrgUserId).collect(Collectors.toList());
        final List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> resMaindataMainDepartmentSelectUserWithDeptItemDataItems = SinoipaasUtils.employeeWithDept(userIds);
        final Map<String, ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> collect = resMaindataMainDepartmentSelectUserWithDeptItemDataItems.stream().collect(Collectors.toMap(k -> k.getId(), v -> v));

        Page<UserDTO> page1 = new Page<>(pageNum, pageSize);
        page1.setRecords(sysUserIPage.getRecords().stream().map(user -> {
            final UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getUserId());
            userDTO.setName(user.getRealName());
            final ResMaindataMainDepartmentSelectUserWithDeptItemDataItem item = collect.get(user.getOrgUserId());
            if (Objects.nonNull(item)) {
                userDTO.setDeptFullPath(item.getOrgAdminTreePathText());
                String deptName = "";
                final String orgAdminTreePathText = item.getOrgAdminTreePathText();
                final String[] split = orgAdminTreePathText.split("/");
                if (split.length > 2) {
                    deptName = split[split.length - 2] + "/" + split[split.length - 1];
                } else {
                    deptName = split[split.length - 1];
                }
                userDTO.setDeptInfo(deptName);
            }
            return userDTO;
        }).collect(Collectors.toList()));
        return page1;
    }

    @Override
    public List<DeptDTO> getSelectedDept() {

        final List<ResMaindataMainDepartmentSelectAllItemDataItem> res = SinoipaasUtils.mainDepartmentAll();

        // 部门树
        /*String defaultTopDept = "DEP1483358";
        final DeptDTO topDept = res.stream().filter(result -> result.getId().equals(defaultTopDept))
                .map(r -> {
                    final DeptDTO deptDTO = new DeptDTO();
                    deptDTO.setId(r.getId());
                    deptDTO.setName(r.getDepartName());
                    return deptDTO;
                }).findFirst().get();

        topDept.setChildren(buildDeptTree(defaultTopDept, res));
        List<DeptDTO> deptTree = Lists.newArrayList(topDept);*/

        final List<DeptDTO> collect = res.stream().map(item -> {
            final DeptDTO deptDTO = new DeptDTO();
            deptDTO.setId(item.getId());
            String deptName = "";
            final String orgAdminTreePathText = item.getOrgAdminTreePathText();
            final String[] split = orgAdminTreePathText.split("/");
            if (split.length > 2) {
                deptName = split[split.length - 2] + "/" + split[split.length - 1];
            } else {
                deptName = split[split.length - 1];
            }
            deptDTO.setFullPath(orgAdminTreePathText);
            deptDTO.setName(deptName);
            return deptDTO;
        }).collect(Collectors.toList());
        return collect;
    }


    // 获取父节点并集
    private List<Integer> getAllParentIds(List<AssetsCatalogue> catalogues) {
        final List<Integer> ids = catalogues.stream().map(AssetsCatalogue::getId).collect(Collectors.toList());
        return catalogues.stream().map(c -> {
                return Arrays.stream(c.getPath().split("/"))
                        .filter(StringUtils::isNotEmpty)
                        .map(Integer::parseInt)
                        .filter(id -> !ids.contains(id))
                        .collect(Collectors.toList());
            }).flatMap(c -> c.stream())
            .collect(Collectors.toSet())
            .stream()
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer saveOrUpdate(CatalogueDTO catalogueDTO) {
        AssetsCatalogue assetsCatalogue = new AssetsCatalogue();
        BeanUtils.copyProperties(catalogueDTO, assetsCatalogue);

        if (Objects.isNull(catalogueDTO.getId())) {
            // 新增
            assetsCatalogue.setCreatedBy(SecurityUtils.getUserId());
            assetsCatalogue.setCreatedAt(new Date());
            assetsCatalogue.setUpdatedBy(SecurityUtils.getUserId());
            assetsCatalogue.setUpdatedAt(new Date());
            assetsCatalogueMapper.insert(assetsCatalogue);

            // 设置path
            createPath(assetsCatalogue);
            assetsCatalogue.setLevel(assetsCatalogue.getPath().split("/").length - 1);

            // 设置层级
            assetsCatalogueMapper.updateById(assetsCatalogue);

        } else {
            // 编辑
            final Integer id = assetsCatalogue.getId();


            assetsCatalogue.setUpdatedBy(SecurityUtils.getUserId());
            assetsCatalogue.setUpdatedAt(new Date());

            createPath(assetsCatalogue);
            assetsCatalogue.setLevel(assetsCatalogue.getPath().split("/").length - 1);
            assetsCatalogueMapper.updateById(assetsCatalogue);

            // 处理子集目录的的所有路径迁移
            moveChildPath(assetsCatalogue);

            // 删除原有用户权限
            assetsCataloguePermissionDAO.deleteByCatalogueId(id);
        }

        // 保存用户权限
        final List<UserRightsDTO> userRightsDTOS = catalogueDTO.getUserRightsDTOS();
        if (CollUtil.isNotEmpty(userRightsDTOS)) {

            final List<AssetsCataloguePermission> permissions = userRightsDTOS.stream().map(dto -> {
                final AssetsCataloguePermission assetsCataloguePermission = new AssetsCataloguePermission();
                BeanUtils.copyProperties(dto, assetsCataloguePermission);
                assetsCataloguePermission.setCatalogueId(assetsCatalogue.getId());
                return assetsCataloguePermission;
            }).collect(Collectors.toList());

            assetsCataloguePermissionDAO.saveAll(permissions);

        }

        // 更新全局排序
        changeGlobalSort();

        return assetsCatalogue.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void changeGlobalSort() {
        final List<AssetsCatalogue> assetsCatalogues = assetsCatalogueMapper.selectList(Wrappers.<AssetsCatalogue>lambdaQuery().eq(AssetsCatalogue::getDeleted, 0).orderByAsc(AssetsCatalogue::getSortOrder,AssetsCatalogue::getCreatedAt));
        final List<CatalogueQueryDTO> catalogueQueryDTOS = buildCataloguesTree(null, assetsCatalogues, new ArrayList<>());

        final List<CatalogueQueryDTO> sortList = buildSortList(catalogueQueryDTOS);

        for (int i = 0; i < sortList.size(); i++) {
            assetsCatalogueMapper.update(null, new UpdateWrapper<AssetsCatalogue>().set("global_sort", i+1).eq("id", sortList.get(i).getId()));
        }

    }


    private List<CatalogueQueryDTO> buildSortList(List<CatalogueQueryDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<CatalogueQueryDTO> result = new ArrayList<>();
        for (CatalogueQueryDTO node : list) {
            result.add(node);
            result.addAll(buildSortList(node.getChildren()));
        }
        return result;
    }


    private void createPath(AssetsCatalogue assetsCatalogue) {
        // 设置path
        if (Objects.isNull(assetsCatalogue.getParentId())) {
            assetsCatalogue.setPath("/" + assetsCatalogue.getId() + "/");
        } else {
            final AssetsCatalogue parent = assetsCatalogueMapper.selectById(assetsCatalogue.getParentId());
            assetsCatalogue.setPath(parent.getPath() + assetsCatalogue.getId() + "/");
        }
    }

    private void moveChildPath(AssetsCatalogue assetsCatalogue) {
        final Integer id = assetsCatalogue.getId();
        // 新路径
        final String path = assetsCatalogue.getPath();
        // 获取所有需要转移的文件夹
        final String currentPath = "/" + id + "/";
        final LambdaQueryWrapper<AssetsCatalogue> wrapper = Wrappers.<AssetsCatalogue>lambdaQuery()
                .like(AssetsCatalogue::getPath, currentPath);
        final List<AssetsCatalogue> catalogues = assetsCatalogueMapper.selectList(wrapper);
        final List<AssetsCatalogue> collect = catalogues.stream()
                .filter(catalog -> !catalog.getId().equals(id))
                .map(catalog -> {
                    final String catalogPath = catalog.getPath();
                    final int index = catalogPath.indexOf(currentPath);
                    final String basePath = catalogPath.substring(index + currentPath.length());
                    catalog.setPath(path + basePath);
                    catalog.setLevel(catalog.getPath().split("/").length - 1);
                    return catalog;
                })
                .collect(Collectors.toList());

        assetsCatalogueDAO.updateAll(collect);
    }


    @Override
    public boolean assetReadAble(String type, Long relatedId) {
        final SinoPassUserDTO o = (SinoPassUserDTO)ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO);

        // 查看资产自定义权限中是否存在
        final List<TgAssetWhitelistInfo> info = tgAssetAuthWhiltlistInfoDAO
                .findWhiteListInfoByUserIdAndDeptIdAndTypeAndRelatedId(SecurityUtils.getUserId(), o.getMainOrganizationId(), type, relatedId);

        if (CollUtil.isNotEmpty(info) && info.stream().anyMatch( t -> t.getServiceType().equals(WhitlistServiceType.READABLE))) {
            return true;
        }

        // 查看目录权限是否存在可阅读的情况
        final LambdaQueryWrapper<TgAssetInfo> wq = Wrappers.<TgAssetInfo>lambdaQuery()
                .eq(TgAssetInfo::getRelatedId, relatedId)
                .eq(TgAssetInfo::getType, type)
                .eq(TgAssetInfo::getDeleted, 0);
        final TgAssetInfo tgAssetInfo = tgAssetInfoMapper.selectOne(wq);

        if (tgAssetInfo.getIsFollowAssetMenuReadableRange().equals(AuthItemEnum.FOLLOW_DIR_AUTH)) {
            final Integer assetMenuId = tgAssetInfo.getAssetMenuId();

            final AssetsCatalogue assetsCatalogue = assetsCatalogueMapper.selectById(assetMenuId);
            final String path = assetsCatalogue.getPath();

            final List<Integer> fullPathIds = Arrays.stream(path.split("/"))
                    .filter(StringUtils::isNotEmpty)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            final List<AssetsCataloguePermission> permissions = assetsCataloguePermissionDAO.getAllPermissionsByCatalogueIdsAndUserIdAndDept(fullPathIds, SecurityUtils.getUserId(), o.getMainOrganizationId());

            return permissions.stream().anyMatch(p -> p.getReadable().equals(1));

        }

        return false;
    }

    @Override
    public List<Integer> getManageableAssetMenuIds(Integer dirId) {

        // 获取拥有权限的所有目录
        final Long userId = SecurityUtils.getUserId();
        final SinoPassUserDTO o = (SinoPassUserDTO)ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO);
        final List<AssetsCataloguePermission> allPermission = assetsCataloguePermissionDAO.findByUserIdAndDeptId(userId, o.getMainOrganizationId());

        // 筛选出有目录管理权限的目录
        final List<AssetsCataloguePermission> catalogueManagerList = allPermission.stream()
                .filter(permission -> permission.getAssetsManager().equals(CommonConstants.ASSET_MANAGER))
                .collect(Collectors.toList());

        List<AssetsCatalogue> assetsCatalogues = assetsCatalogueMapper.selectListInPath(catalogueManagerList
                .stream().map(AssetsCataloguePermission::getCatalogueId).collect(Collectors.toList()));

        if (assetsCatalogues.isEmpty()) {
            return Lists.newArrayList();
        }

        if (Objects.nonNull(dirId)) {
            assetsCatalogues = assetsCatalogues.stream().filter(c -> c.getPath().contains("/" + dirId + "/")).collect(Collectors.toList());
        }

        return assetsCatalogues.stream().map(AssetsCatalogue::getId).collect(Collectors.toList());
    }

    @Override
    public List<AssetsCatalogue> getMenuIdsByMenuRootIds(List<Integer> menuIds) {
        return assetsCatalogueMapper.selectListInPath(menuIds);
    }

    @Override
    public AssetsCatalogue getInitialCatalogue() {
        return assetsCatalogueMapper.selectOne(new QueryWrapper<AssetsCatalogue>() {{
            eq("name", "初始化");
        }});
    }

    @Override
    public AssetsCatalogue getCatalogueByDirName(String dirName) {
        return assetsCatalogueMapper.selectOne(new QueryWrapper<AssetsCatalogue>() {{
            eq("name", dirName);
        }});
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCatalogue(Integer id) {


        final List<AssetsCatalogue> assetsCatalogues = assetsCatalogueDAO.selectChild(id);
        if (CollUtil.isNotEmpty(assetsCatalogues)) {
            throw new CustomException("当前目录存在子目录，请删除子目录后再进行删除");
        }

        final LambdaQueryWrapper<TgAssetInfo> wq = Wrappers.<TgAssetInfo>lambdaQuery()
                .eq(TgAssetInfo::getAssetMenuId, id)
                .eq(TgAssetInfo::getDeleted, 0);
        final List<TgAssetInfo> tgAssetInfos = tgAssetInfoMapper.selectList(wq);
        if (CollUtil.isNotEmpty(tgAssetInfos)) {
            throw new CustomException("当前目录存在资产，请转移资产后再进行删除");
        }

        // 删除目录，删除权限
        assetsCatalogueMapper.deleteById(id);
        assetsCataloguePermissionDAO.deleteByCatalogueId(id);

    }


    @Override
    public boolean isSameTopCatalog(Integer catalogId, Integer catalogId2) {

        final List<AssetsCatalogue> catalogues = assetsCatalogueMapper.selectBatchIds(Lists.newArrayList(catalogId, catalogId2));

        return catalogues.stream()
                .collect(Collectors.groupingBy(catalog -> {
                    return Arrays.stream(catalog.getPath().split("/")).filter(StringUtils::isNotEmpty).findFirst().get();
                }, Collectors.counting()))
                .entrySet()
                .stream().anyMatch(v->{return !(v.getValue()>1);});
    }

    /**
     * 资产目录中文转换
     *
     * @param path
     * @return
     */
    @Override
    public String getCataloguePathCn(String path) {
        if (StringUtils.isBlank(path)) {
            return null;
        }

        List<Integer> catalogueIdList = Arrays.stream(path.split("/")).filter(StringUtils::isNotEmpty).map(Integer::parseInt).collect(Collectors.toList());
        List<AssetsCatalogue> catalogueList = assetsCatalogueMapper.selectBatchIds(catalogueIdList);
        Map<Integer, String> cataLogueMap = catalogueList.stream().collect(Collectors.toMap(AssetsCatalogue::getId, AssetsCatalogue::getName));

        String cataloguePathCn = catalogueIdList.stream().map(item -> cataLogueMap.get(item)).collect(Collectors.joining("/"));
        return cataloguePathCn;
    }

    @Override
    public Integer getLevel1CatalogueId(Integer catalogueId) {
        return assetsCatalogueMapper.getLevel1CatalogueId(catalogueId);
    }

    /**
     * 获取一二级目录路径
     * @return
     */
    @Override
    public Map<Integer, String> getLevel12MenuNames() {
        return assetsCatalogueMapper.getLevel12MenuNames()
                .stream().collect(Collectors.toMap(MenuNameDto::getId, MenuNameDto::getMenuName));
    }

    /**
     * 获取全路径
     * @return
     */
    @Override
    public Map<Integer, String> getFullMenuNames() {
        final List<AssetsCatalogue> assetsCatalogues = assetsCatalogueMapper.selectList(Wrappers.emptyWrapper());
        final Map<Integer, AssetsCatalogue> catalogueMap = assetsCatalogues.stream()
                .collect(Collectors.toMap(AssetsCatalogue::getId, v -> v));
        Map<Integer, String> fullPathMap = new HashMap<Integer, String>();
        for (AssetsCatalogue assetsCatalogue : assetsCatalogues) {
            fullPathMap.put(assetsCatalogue.getId(),this.buildCatalogFullPath(assetsCatalogue.getPath(), catalogueMap));
        }
        return fullPathMap;
    }

    private List<CatalogueAllDTO> buildAllCatalogue(Integer parentId, List<AssetsCatalogue> assetsCatalogues, Integer limitLevel) {
        return assetsCatalogues.stream()
                .filter(assetsCatalogue -> {
                    if (Objects.isNull(parentId)) {
                        return Objects.isNull(assetsCatalogue.getParentId());
                    } else {
                        return parentId.equals(assetsCatalogue.getParentId());
                    }
                })
                .sorted(Comparator.comparing(a -> a.getPath().length()))
                .sorted(Comparator.comparing(AssetsCatalogue::getSortOrder))
                .map(assetsCatalogue -> {
                    if (assetsCatalogue.getLevel()>limitLevel) {
                        return null;
                    }
                    final CatalogueAllDTO catalog = new CatalogueAllDTO();
                    catalog.setId(assetsCatalogue.getId());
                    catalog.setName(assetsCatalogue.getName());
                    catalog.setParentId(assetsCatalogue.getParentId());
                    catalog.setDesc(assetsCatalogue.getDescription());
                    catalog.setChild(buildAllCatalogue(assetsCatalogue.getId(), assetsCatalogues, limitLevel));
                    return catalog;
                }).collect(Collectors.toList());


    }

    private List<CatalogueQueryDTO> buildCataloguesTree (Integer parentId, List<AssetsCatalogue> assetsCatalogues, List<Integer> managerAble) {
        return  assetsCatalogues.stream()
            .filter(assetsCatalogue -> {
                if (Objects.isNull(parentId)) {
                    return Objects.isNull(assetsCatalogue.getParentId());
                } else {
                    return parentId.equals(assetsCatalogue.getParentId());
                }
            })
            .sorted(Comparator.comparing(a -> a.getPath().length()))
            .sorted(Comparator.comparing(AssetsCatalogue::getSortOrder))
            .map(assetsCatalogue -> {
                final CatalogueQueryDTO catalogueQueryDTO = new CatalogueQueryDTO();
                BeanUtils.copyProperties(assetsCatalogue, catalogueQueryDTO);
                if (managerAble.contains(assetsCatalogue.getId()) || managerAble.contains(assetsCatalogue.getParentId())) {
                    // 存在可删，可增下级
                    catalogueQueryDTO.setDeletedAble(true);
                    catalogueQueryDTO.setBuildNextLevel(true);
                    if (managerAble.contains(assetsCatalogue.getParentId())) {
                        // 父级可修改，可创建同级别目录
                        catalogueQueryDTO.setBuildSameLevel(true);
                    }
                    managerAble.add(assetsCatalogue.getId());
                }
                catalogueQueryDTO.setChildren(buildCataloguesTree(assetsCatalogue.getId(), assetsCatalogues, managerAble));
                return catalogueQueryDTO;
            }).collect(Collectors.toList());
    }

    private List<CatalogueReadAbleDTO> buildCataloguesReadableTree (Integer parentId, List<AssetsCatalogue> assetsCatalogues, List<Integer> managerAble) {
        return  assetsCatalogues.stream()
            .filter(assetsCatalogue -> {
                if (Objects.isNull(parentId)) {
                    return Objects.isNull(assetsCatalogue.getParentId());
                } else {
                    return parentId.equals(assetsCatalogue.getParentId());
                }
            })
            .sorted(Comparator.comparing(a -> a.getPath().length()))
            .sorted(Comparator.comparing(AssetsCatalogue::getSortOrder))
            .map(assetsCatalogue -> {
                final CatalogueReadAbleDTO catalogueQueryDTO = new CatalogueReadAbleDTO();
                BeanUtils.copyProperties(assetsCatalogue, catalogueQueryDTO);
                if (managerAble.contains(assetsCatalogue.getId()) || managerAble.contains(assetsCatalogue.getParentId())) {
                    //catalogueQueryDTO.setDataAssetsCount(100);
                    managerAble.add(assetsCatalogue.getId());
                }
                catalogueQueryDTO.setParentId(parentId);
                final List<CatalogueReadAbleDTO> children = buildCataloguesReadableTree(assetsCatalogue.getId(), assetsCatalogues, managerAble);
                /*if (children.isEmpty()) {
                    final List<TgAssetInfo> tgAssetInfos = assetMenuIdMap.get(assetsCatalogue.getId());
                    catalogueQueryDTO.setAssetCount(CollUtil.isEmpty(tgAssetInfos)?0:tgAssetInfos.size());
                } else {
                    final Integer count = children.stream().collect(Collectors.summingInt(CatalogueReadAbleDTO::getAssetCount));
                    final List<TgAssetInfo> tgAssetInfos = assetMenuIdMap.get(assetsCatalogue.getId());
                    catalogueQueryDTO.setAssetCount(CollUtil.isEmpty(tgAssetInfos)?count:tgAssetInfos.size() + count);
                }*/
                catalogueQueryDTO.setDescription(assetsCatalogue.getDescription());
                catalogueQueryDTO.setChildren(children);

                return catalogueQueryDTO;
            }).collect(Collectors.toList());
    }

    private List<CatalogueAssetManageAbleDTO> buildCataloguesAssetsManageTree (Integer parentId, List<AssetsCatalogue> assetsCatalogues, List<Integer> assetsManageAble, Map<Integer, AssetsCatalogue> catalogueIdNameMap) {
        return  assetsCatalogues.stream()
            .filter(assetsCatalogue -> {
                if (Objects.isNull(parentId)) {
                    return Objects.isNull(assetsCatalogue.getParentId());
                } else {
                    return parentId.equals(assetsCatalogue.getParentId());
                }
            })
            .sorted(Comparator.comparing(a -> a.getPath().length()))
            .sorted(Comparator.comparing(AssetsCatalogue::getSortOrder))
            .map(assetsCatalogue -> {
                final CatalogueAssetManageAbleDTO catalogueAssetManageDTO = new CatalogueAssetManageAbleDTO();
                BeanUtils.copyProperties(assetsCatalogue, catalogueAssetManageDTO);
                if (assetsManageAble.contains(assetsCatalogue.getId()) || assetsManageAble.contains(assetsCatalogue.getParentId())) {
                    catalogueAssetManageDTO.setChooseAble(true);
                    assetsManageAble.add(assetsCatalogue.getId());
                } else {
                    catalogueAssetManageDTO.setChooseAble(false);
                }
                catalogueAssetManageDTO.setFullPath(buildCatalogFullPath(assetsCatalogue.getPath(), catalogueIdNameMap));
                catalogueAssetManageDTO.setChildren(buildCataloguesAssetsManageTree(assetsCatalogue.getId(), assetsCatalogues, assetsManageAble, catalogueIdNameMap));
                return catalogueAssetManageDTO;
            }).collect(Collectors.toList());
    }

    /**
     * 构建全路径
     * @param path
     * @param catalogueIdNameMap
     * @return
     */
    @Override
    public String buildCatalogFullPath(String path, Map<Integer, AssetsCatalogue> catalogueIdNameMap) {
        return Arrays.stream(path.split("/"))
                .filter(StringUtils::isNotEmpty)
                .map(p -> catalogueIdNameMap.get(Integer.valueOf(p)).getName())
                .collect(Collectors.joining("/"));
    }

    private List<DeptDTO> buildDeptTree(String parentId , List<ResMaindataMainDepartmentSelectAllItemDataItem> res) {
        return res.stream()
                .filter(dept -> dept.getOrgAdminId().equals(parentId))
                .map(dept -> {
                    final DeptDTO dto = new DeptDTO();
                    dto.setId(dept.getId());
                    dto.setName(dept.getDepartName());
                    //dto.setChildren(buildDeptTree(dept.getId(), res));
                    return dto;
                }).collect(Collectors.toList());
    }


}
