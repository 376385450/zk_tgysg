package com.sinohealth.system.biz.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.ProjectStatusEnum;
import com.sinohealth.common.enums.StatusTypeEnum;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserFileAssets;
import com.sinohealth.system.biz.dataassets.dto.UserDataAssetResp;
import com.sinohealth.system.biz.dataassets.dto.request.MyAssetRequest;
import com.sinohealth.system.biz.dataassets.mapper.UserDataAssetsMapper;
import com.sinohealth.system.biz.dataassets.mapper.UserFileAssetsMapper;
import com.sinohealth.system.biz.dict.dto.request.DictCommonPageRequest;
import com.sinohealth.system.biz.project.constants.ProjectRelateEnum;
import com.sinohealth.system.biz.project.dao.ProjectDAO;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.biz.project.domain.ProjectDataAssetsRelate;
import com.sinohealth.system.biz.project.dto.ProjectDTO;
import com.sinohealth.system.biz.project.dto.ProjectValDTO;
import com.sinohealth.system.biz.project.dto.request.ProjectUpsertParam;
import com.sinohealth.system.biz.project.service.ProjectService;
import com.sinohealth.system.domain.ProjectHelper;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.customer.Customer;
import com.sinohealth.system.mapper.*;
import com.sinohealth.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-29 11:42
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectDAO projectDAO;

    private final TgApplicationInfoMapper tgApplicationInfoMapper;

    private final ISysUserService userService;

    private final ProjectDataAssetsRelateMapper projectDataAssetsRelateMapper;

    private final ProjectHelperMapper projectHelperMapper;

    private final CustomerMapper customerMapper;

    private final UserDataAssetsMapper userDataAssetsMapper;

    private final TgTemplateInfoMapper tgTemplateInfoMapper;

    private final TgAssetInfoMapper tgAssetInfoMapper;

    private final UserFileAssetsMapper userFileAssetsMapper;

    @Override
    public AjaxResult<IPage<ProjectDTO>> pageQuery(DictCommonPageRequest request) {
        final List<Long> helpProjectId = new ArrayList<>();
        if (request.getType().equals(1)) {
            // 我的项目
            final LambdaQueryWrapper<ProjectHelper> wq = Wrappers.<ProjectHelper>lambdaQuery()
                    .eq(ProjectHelper::getUserId, SecurityUtils.getUserId());
            final List<ProjectHelper> projectHelpers = projectHelperMapper.selectList(wq);
            if (CollUtil.isNotEmpty(projectHelpers)) {
                helpProjectId.addAll(projectHelpers.stream().map(ProjectHelper::getProjectId).collect(Collectors.toList()));
            }
        }
        // 客户id
        final List<Long> customerIds = new ArrayList<>();

        if (StringUtils.isNotEmpty(request.getCustomerName())) {
            final LambdaQueryWrapper<Customer> customerLambdaQueryWrapper = Wrappers.<Customer>lambdaQuery()
                    .like(Customer::getShortName, request.getCustomerName());
            final List<Customer> customers = customerMapper.selectList(customerLambdaQueryWrapper);
            if (CollUtil.isEmpty(customers)) {
                return AjaxResult.success(PageUtil.empty());
            }
            customerIds.addAll(customers
                    .stream().map(Customer::getId).collect(Collectors.toList()));
        }


        final LambdaQueryWrapper<Project> queryWrapper = new QueryWrapper<Project>()
                .lambda().and(StringUtils.isNotBlank(request.getSearchContent()),
                        v -> v.like(Project::getName, request.getSearchContent()))
                .and(request.getType().equals(1), wq -> {
                    wq.eq(Project::getProjectManager, SecurityUtils.getUserId())
                            .or()
                            .in(CollUtil.isNotEmpty(helpProjectId), Project::getId, helpProjectId);
                })
                .in(CollUtil.isNotEmpty(customerIds), Project::getCustomerId, customerIds)
                .eq(Objects.nonNull(request.getProjectStatus()), Project::getStatus, request.getProjectStatus());


        queryWrapper.orderByDesc(Project::getUpdateTime);

        IPage<Project> page = projectDAO.getBaseMapper().selectPage(request.buildPage(), queryWrapper);

        Set<Long> userIds = page.getRecords().stream()
                .flatMap(v -> Stream.of(v.getCreator(), v.getUpdater()))
                .filter(Objects::nonNull).collect(Collectors.toSet());
        // 项目经理
        final Set<Long> projectManager = page.getRecords().stream()
                .map(Project::getProjectManager)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        userIds.addAll(projectManager);
        // 协作用户
        final List<Long> ids = page.getRecords().stream()
                .map(Project::getId).collect(Collectors.toList());

        final Map<Long, List<ProjectHelper>> helpers = new HashMap<>();
        if (CollUtil.isNotEmpty(ids)) {
            final LambdaQueryWrapper<ProjectHelper> in = Wrappers.<ProjectHelper>lambdaQuery()
                    .in(ProjectHelper::getProjectId, ids);
            final List<ProjectHelper> projectHelpers = projectHelperMapper.selectList(in);
            helpers.putAll(projectHelpers.stream()
                    .collect(Collectors.groupingBy(ProjectHelper::getProjectId)));
            final Set<Long> helper = projectHelpers.stream()
                    .map(ProjectHelper::getUserId)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            userIds.addAll(helper);
        }

        // 客户信息
        final Map<Long, Customer> customerMap = new HashMap<>();
        final Set<Long> cids = page.getRecords().stream().map(Project::getCustomerId).collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(cids)) {
            customerMap.putAll(customerMapper.selectBatchIds(cids).stream().collect(Collectors.toMap(Customer::getId, v -> v)));
        }

        Map<Long, String> nameMap = userService.selectUserNameMapByIds(userIds);

        List<Long> projectIds = page.getRecords().stream().map(Project::getId).distinct().collect(Collectors.toList());
        Map<Long, Integer> useMap;
        if (CollectionUtils.isNotEmpty(projectIds)) {
            Map<Long, Map<String, Long>> valMap = tgApplicationInfoMapper.groupByProject(projectIds);
            useMap = valMap.values().stream().collect(Collectors.toMap(v -> v.get("project_id"),
                    v -> v.get("amount").intValue(), (front, current) -> current));
        } else {
            useMap = Collections.emptyMap();
        }

        // 关联资产数
        final LambdaQueryWrapper<ProjectDataAssetsRelate> notNull = Wrappers.<ProjectDataAssetsRelate>lambdaQuery()
                .in(CollUtil.isNotEmpty(projectIds), ProjectDataAssetsRelate::getProjectId, projectIds)
                .isNotNull(ProjectDataAssetsRelate::getUserAssetId);
        final Map<Long, List<ProjectDataAssetsRelate>> assetMap = new HashMap<>();
        assetMap.putAll(projectDataAssetsRelateMapper.selectList(notNull)
                .stream().collect(Collectors.groupingBy(ProjectDataAssetsRelate::getProjectId)));

        return AjaxResult.success(PageUtil.convertMap(page, v -> {
            ProjectDTO dto = new ProjectDTO();
            BeanUtils.copyProperties(v, dto);
            dto.setUpdater(nameMap.get(v.getUpdater()));
            dto.setCreator(nameMap.get(v.getCreator()));
            dto.setProjectManagerName(nameMap.get(v.getProjectManager()));
            final Customer customer = customerMap.getOrDefault(v.getCustomerId(), null);
            if (Objects.nonNull(customer)) {
                dto.setCustomerType(customer.getCustomerType());
                dto.setCustomerShortName(customer.getShortName());
            }
            dto.setIsProjectManager(SecurityUtils.getUserId().equals(v.getProjectManager()));
            final List<String> helperNames = helpers.getOrDefault(v.getId(), Lists.newArrayList()).stream()
                    .map(a -> {
                        return nameMap.get(a.getUserId());
                    }).collect(Collectors.toList());
            dto.setCollaborationUser(helperNames);
            dto.setRelateAssetCount(assetMap.getOrDefault(v.getId(), Lists.newArrayList()).size());
            dto.setRelationApply(useMap.getOrDefault(v.getId(), 0));
            return dto;
        }));
    }

    @Override
    public AjaxResult<ProjectDTO> detail(Long id) {
        final Project project = projectDAO.getById(id);
        final ProjectDTO projectDTO = new ProjectDTO();
        BeanUtils.copyProperties(project, projectDTO);

        // 获取关联用户
        final LambdaQueryWrapper<ProjectHelper> wq = Wrappers.<ProjectHelper>lambdaQuery()
                .eq(ProjectHelper::getProjectId, id);
        final List<ProjectHelper> projectHelpers = projectHelperMapper.selectList(wq);
        projectDTO.setCollaborationUserIds(projectHelpers.stream().map(ProjectHelper::getUserId).map(a -> Long.toString(a)).collect(Collectors.toList()));

        // 获取关联需求
        final LambdaQueryWrapper<ProjectDataAssetsRelate> eq = Wrappers.<ProjectDataAssetsRelate>lambdaQuery()
                .eq(ProjectDataAssetsRelate::getProjectId, id);
        final List<ProjectDataAssetsRelate> list = projectDataAssetsRelateMapper.selectList(eq);
        final List<Long> userAssetIds = list.stream()
                .map(ProjectDataAssetsRelate::getUserAssetId)
                .distinct()
                .collect(Collectors.toList());

        final Map<Long, ProjectDataAssetsRelate> projectDataAssetsRelateMap = list.stream()
                .collect(Collectors.toMap(ProjectDataAssetsRelate::getUserAssetId, v -> v));

        final Long customerId = project.getCustomerId();

        final Customer customer = customerMapper.selectById(customerId);
        projectDTO.setCustomerShortName(customer.getShortName());
        projectDTO.setCustomerType(customer.getCustomerType());
        projectDTO.setIsProjectManager(Objects.equals(SecurityUtils.getUserId(), project.getProjectManager()));

        List<UserDataAssetResp> assets = new ArrayList<>();

        if (CollUtil.isNotEmpty(userAssetIds)) {
            final MyAssetRequest request = new MyAssetRequest();
            request.setUserDataAssetIds(userAssetIds);
            final List<UserDataAssetResp> c = userDataAssetsMapper.listAssets(request);

            final LambdaQueryWrapper<ProjectDataAssetsRelate> in = Wrappers.<ProjectDataAssetsRelate>lambdaQuery()
                    .in(ProjectDataAssetsRelate::getUserAssetId, userAssetIds);
            final Map<Long, List<ProjectDataAssetsRelate>> collect = projectDataAssetsRelateMapper.selectList(in)
                    .stream()
                    .collect(Collectors.groupingBy(ProjectDataAssetsRelate::getUserAssetId));

            final List<UserDataAssetResp> resps = c.stream()
                    .peek(a -> {
                        a.setCurrentUserAsset(SecurityUtils.getUserId().equals(a.getUserId()));
                        a.setHasDataExpired(DateUtils.hasDataExpired(a.getDataExpire()));
                        final List<ProjectDataAssetsRelate> projectDataAssetsRelates = collect.get(a.getAssetId());
                        // 资产关联项目数
                        if (CollUtil.isNotEmpty(projectDataAssetsRelates)) {
                            a.setRelateCount(projectDataAssetsRelates.size());
                        } else {
                            a.setRelateCount(0);
                        }
                        if (Objects.isNull(a.getCopyFromId())) {
                            if (id.equals(a.getAssetProjectId())) {
                                a.setMainAsset(true);
                            } else {
                                a.setMainAsset(false);
                            }
                        } else {
                            a.setMainAsset(false);
                        }
                        final ProjectDataAssetsRelate relate = projectDataAssetsRelateMap.get(a.getAssetId());
                        if (Objects.nonNull(relate)) {
                            a.setProType(relate.getProType());
                        }
                    })
                    .collect(Collectors.toList());
            assets.addAll(resps);
        }

        projectDTO.setAssets(assets);

        // 上传文件
        final LambdaQueryWrapper<UserFileAssets> eq1 = Wrappers.<UserFileAssets>lambdaQuery()
                .eq(UserFileAssets::getProjectId, id);
        final List<UserFileAssets> userFileAssets = userFileAssetsMapper.selectList(eq1);
        if (CollUtil.isNotEmpty(userFileAssets)) {
            projectDTO.setUploadFileUsers(userFileAssets.stream().map(UserFileAssets::getCreator).distinct().collect(Collectors.toList()));
        }

        return AjaxResult.success(projectDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> upsert(ProjectUpsertParam request) {
        Project project = new Project();
        boolean isCreate = Objects.isNull(request.getId());
        if (isCreate) {
            Integer exist = projectDAO.getBaseMapper().selectCount(new QueryWrapper<Project>().lambda()
                    .eq(Project::getName, request.getName()));
            if (Objects.nonNull(exist) && exist > 0) {
                return AjaxResult.error("项目名已存在");
            }
        }

        Long userId = SecurityUtils.getUserId();
        BeanUtils.copyProperties(request, project);
        if (isCreate) {
            project.setCreator(userId);
        } else {
            project.setUpdateTime(LocalDateTime.now());
        }
        project.setUpdater(userId);
        projectDAO.saveOrUpdate(project);

        // 保存关联资产
        // 先删除已保存的关联资产
        final LambdaQueryWrapper<ProjectDataAssetsRelate> wrapper = Wrappers.<ProjectDataAssetsRelate>lambdaQuery()
                .eq(ProjectDataAssetsRelate::getProjectId, project.getId());
        final List<ProjectDataAssetsRelate> projectDataAssetsRelates = projectDataAssetsRelateMapper.selectList(wrapper);
        final Map<Long, ProjectDataAssetsRelate> oldAssets = new HashMap<>();
        if (CollUtil.isNotEmpty(projectDataAssetsRelates)) {
            oldAssets.putAll(projectDataAssetsRelates.stream()
                    .collect(Collectors.toMap(ProjectDataAssetsRelate::getUserAssetId, v -> v)));
        }
        if (!isCreate) {
            projectDataAssetsRelateMapper.delete(wrapper);
        }
        final List<Long> relateAsset = request.getRelateAsset();
        List<Long> relateUser = new ArrayList<>();
        if (CollUtil.isNotEmpty(relateAsset)) {
            for (Long userDataAssetId : relateAsset) {
                final ProjectDataAssetsRelate projectDataAssetsRelate = new ProjectDataAssetsRelate();
                projectDataAssetsRelate.setUserAssetId(userDataAssetId);
                projectDataAssetsRelate.setProjectId(project.getId());
                if (MapUtil.isNotEmpty(oldAssets) && Objects.nonNull(oldAssets.get(userDataAssetId))) {
                    projectDataAssetsRelate.setProType(oldAssets.get(userDataAssetId).getProType());
                } else {
                    projectDataAssetsRelate.setProType(ProjectRelateEnum.slave.name());
                }
                projectDataAssetsRelateMapper.insert(projectDataAssetsRelate);
            }
            final List<Long> applicationIds = userDataAssetsMapper.selectBatchIds(relateAsset)
                    .stream()
                    .map(UserDataAssets::getSrcApplicationId)
                    .distinct()
                    .collect(Collectors.toList());
            relateUser.addAll(tgApplicationInfoMapper.selectBatchIds(applicationIds)
                    .stream()
                    .map(TgApplicationInfo::getApplicantId)
                    .distinct()
                    .collect(Collectors.toList()));
        }

        // 保存关联用户
        final List<Long> helpers = CollUtil.isNotEmpty(request.getCollaborationUser()) ? request.getCollaborationUser() : new ArrayList<>();

        helpers.add(request.getProjectManager());

        if (relateUser.contains(SecurityUtils.getUserId())) {
            helpers.add(SecurityUtils.getUserId());
        }
        helpers.addAll(relateUser);
        final List<ProjectHelper> list = helpers
                .stream()
                .distinct()
                .map(a -> {
                    final ProjectHelper projectHelper = new ProjectHelper();
                    projectHelper.setProjectId(project.getId());
                    projectHelper.setUserId(a);
                    return projectHelper;
                }).collect(Collectors.toList());
        if (!isCreate) {
            final LambdaQueryWrapper<ProjectHelper> eq = Wrappers.<ProjectHelper>lambdaQuery()
                    .eq(ProjectHelper::getProjectId, project.getId());
            projectHelperMapper.delete(eq);
        }
        for (ProjectHelper projectHelper : list) {
            projectHelperMapper.insert(projectHelper);
        }

        return AjaxResult.succeed();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> changeStatus(ProjectUpsertParam request) {
        final Long id = request.getId();
        final Project project = projectDAO.getById(id);
        if (project.getStatus().equals(ProjectStatusEnum.OFFLINE.getCode())) {
            project.setStatus(ProjectStatusEnum.ONLINE.getCode());
        } else {
            project.setStatus(ProjectStatusEnum.OFFLINE.getCode());
        }
        project.setUpdateTime(LocalDateTime.now());
        projectDAO.updateById(project);
        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> deleteById(Long id) {
        if (Objects.isNull(id)) {
            return AjaxResult.error("");
        }

        Integer count = tgApplicationInfoMapper.selectCount(new QueryWrapper<TgApplicationInfo>()
                .lambda().eq(TgApplicationInfo::getProjectId, id));
        if (Objects.nonNull(count) && count > 0) {
            return AjaxResult.error("当前项目关联需求申请，无法删除");
        }

        projectDAO.removeById(id);
        return AjaxResult.succeed();
    }

    /**
     * 如果有资产id就返回资产关联的项目，空则带出有协作权限的项目
     */
    @Override
    public AjaxResult<List<ProjectValDTO>> listAvailableProjects(Long assetsId) {
        List<Long> projects;
        if (Objects.nonNull(assetsId)) {
            List<ProjectDataAssetsRelate> projectList = projectDataAssetsRelateMapper.selectList(
                    new QueryWrapper<ProjectDataAssetsRelate>().lambda().eq(ProjectDataAssetsRelate::getUserAssetId, assetsId)
            );
            if (CollectionUtils.isEmpty(projectList)) {
                return AjaxResult.error("无项目权限");
            }
            projects = Lambda.buildList(projectList, ProjectDataAssetsRelate::getProjectId);
        } else {
            List<ProjectHelper> helpers = projectHelperMapper.selectList(new QueryWrapper<ProjectHelper>().lambda()
                    .eq(ProjectHelper::getUserId, SecurityUtils.getUserId()));
            projects = Lambda.buildList(helpers, ProjectHelper::getProjectId);
        }

        List<Project> list = projectDAO.getBaseMapper().selectList(new QueryWrapper<Project>().lambda().in(Project::getId, projects)
                .eq(Project::getStatus, StatusTypeEnum.IS_ENABLE.getId()));
        return AjaxResult.success(list.stream()
                .map(v -> ProjectValDTO.builder().projectId(v.getId()).projectName(v.getName()).build()).collect(Collectors.toList()));
    }

    @Override
    public void patchUserProjectRelation(Map<Long, Set<Long>> userList) {
        if (MapUtil.isEmpty(userList)) {
            return;
        }

        Set<Long> proIds = userList.keySet();
        List<ProjectHelper> helpers = projectHelperMapper.selectList(new QueryWrapper<ProjectHelper>().lambda()
                .in(ProjectHelper::getProjectId, proIds));

        List<ProjectHelper> needSave = calcNeedSaveRelation(userList, helpers);

        for (ProjectHelper projectHelper : needSave) {
            projectHelperMapper.insert(projectHelper);
        }

    }

    static List<ProjectHelper> calcNeedSaveRelation(Map<Long, Set<Long>> userList, List<ProjectHelper> helpers) {
        List<ProjectHelper> needSave = new ArrayList<>();
        Map<Long, Set<Long>> existPairMap = Lambda.buildGroupMapSet(helpers,
                ProjectHelper::getProjectId, ProjectHelper::getUserId);
        for (Map.Entry<Long, Set<Long>> entry : userList.entrySet()) {
            Set<Long> existList = existPairMap.get(entry.getKey());
            Set<Long> expect = entry.getValue();
            if (CollectionUtils.isNotEmpty(existList)) {
                expect.removeAll(existList);
            }

            expect.stream().map(a -> {
                final ProjectHelper projectHelper = new ProjectHelper();
                projectHelper.setProjectId(entry.getKey());
                projectHelper.setUserId(a);
                return projectHelper;
            }).forEach(needSave::add);
        }
        return needSave;
    }
}
