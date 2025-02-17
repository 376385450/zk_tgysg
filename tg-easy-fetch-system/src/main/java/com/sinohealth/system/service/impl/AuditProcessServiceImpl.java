package com.sinohealth.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.util.BooleanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.DataAssetsConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AuditTypeEnum;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsSnapshotTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.data.intelligence.api.Result;
import com.sinohealth.data.intelligence.api.datasource.dto.DataSourceDTO;
import com.sinohealth.system.anno.MessagePointCut;
import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.dao.ApplicationFormDAO;
import com.sinohealth.system.biz.application.dao.ApplicationTaskConfigDAO;
import com.sinohealth.system.biz.application.domain.ApplicationForm;
import com.sinohealth.system.biz.application.domain.ApplicationTaskConfig;
import com.sinohealth.system.biz.application.dto.DataApplicationPageDto;
import com.sinohealth.system.biz.application.dto.DocApplicationPageDto;
import com.sinohealth.system.biz.application.dto.SyncApplyDetailVO;
import com.sinohealth.system.biz.application.dto.TableApplicationPageDto;
import com.sinohealth.system.biz.application.entity.ProjectInfoEntity;
import com.sinohealth.system.biz.application.service.ApplicationFormService;
import com.sinohealth.system.biz.application.service.CustomTagService;
import com.sinohealth.system.biz.application.util.ApplyUtil;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.audit.dto.AuditRequest;
import com.sinohealth.system.biz.dataassets.dao.AcceptanceRecordDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.AcceptanceRecord;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.UpsertAssetsBO;
import com.sinohealth.system.biz.dataassets.dto.bo.ExecFlowParam;
import com.sinohealth.system.biz.dataassets.helper.UserDataAssetsUploadFtpHelper;
import com.sinohealth.system.biz.dataassets.service.AssetsUpgradeTriggerService;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.biz.msg.dao.MessageRecordDimDAO;
import com.sinohealth.system.biz.project.dao.ProjectDAO;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.mapper.ProjectMapper;
import com.sinohealth.system.biz.project.service.DataPlanService;
import com.sinohealth.system.biz.scheduler.dto.request.DataSyncTaskFieldConfig;
import com.sinohealth.system.biz.scheduler.service.IntegrateSyncTaskService;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.biz.ws.service.WsMsgService;
import com.sinohealth.system.client.DatasourceClient;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.domain.customer.Customer;
import com.sinohealth.system.dto.auditprocess.AuditApplicationSearchDto;
import com.sinohealth.system.dto.auditprocess.AuditPageByTypeDto;
import com.sinohealth.system.dto.auditprocess.AuditPageDto;
import com.sinohealth.system.dto.auditprocess.ProcessNodeDetailDto;
import com.sinohealth.system.dto.auditprocess.ProcessNodeEasyDto;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.*;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.IAuditProcessService;
import com.sinohealth.system.service.ISysCustomerService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.SyncHelper;
import com.sinohealth.system.util.ApplicationSqlUtil;
import com.sinohealth.system.util.ListUtil;
import com.sinohealth.system.vo.TgApplicationInfoDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @Author Rudolph
 * @Date 2022-05-16 13:57
 * @Desc
 */
@Slf4j
@Service
public class AuditProcessServiceImpl implements IAuditProcessService {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    TgAuditProcessInfoMapper mapper;
    @Autowired
    TgTemplateInfoMapper templateInfoMapper;
    @Autowired
    private TemplateInfoDAO templateInfoDAO;
    @Autowired
    TgMessageRecordDimMapper messageMapper;
    @Autowired
    private ProjectDAO projectDAO;

    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;

    @Autowired
    DatasourceClient datasourceClient;
    @Autowired
    private ApplicationTaskConfigDAO applicationTaskConfigDAO;
    @Autowired
    TgApplicationInfoMapper applicationInfoMapper;
    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private AcceptanceRecordDAO acceptanceRecordDAO;
    @Autowired
    private MessageRecordDimDAO messageRecordDimDAO;
    @Autowired
    private ApplicationFormDAO applicationFormDAO;

    @Autowired
    ISysUserService userService;
    @Autowired
    ISysCustomerService sysCustomerService;
    @Autowired
    IApplicationService applicationService;
    @Autowired
    ITableInfoService tableInfoService;
    @Autowired
    private UserDataAssetsService userDataAssetsService;
    @Autowired
    private AssetsUpgradeTriggerService assetsUpgradeTriggerService;
    @Autowired
    private IntegrateSyncTaskService syncTaskService;
    @Autowired
    private CustomTagService customTagService;
    @Autowired
    private ApplicationFormService applicationFormService;
    @Autowired
    private UserDataAssetsUploadFtpHelper userDataAssetsUploadFtpHelper;
    @Autowired
    private DataPlanService dataPlanService;

    @Autowired
    private TgAssetInfoMapper tgAssetInfoMapper;
    @Autowired
    private TgDataSyncApplicationMapper tgDataSyncApplicationMapper;

    @Autowired
    private TgDataSyncFieldConfigMapper tgDataSyncFieldConfigMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private SyncHelper syncHelper;

    @Autowired
    private TgNoticeReadMapper tgNoticeReadMapper;

    @Resource
    @Qualifier(ThreadPoolType.SYNC_CK)
    private ThreadPoolTaskExecutor pool;

    @Resource
    @Qualifier(ThreadPoolType.ENHANCED_TTL)
    private Executor ttl;

    @Autowired
    private WsMsgService wsMsgService;

    @Override
    public Object add(TgAuditProcessInfo auditProcessInfo) {
        TgAuditProcessInfo result = JsonBeanConverter.convert2Json(auditProcessInfo);

        SinoPassUserDTO orgUserInfo = (SinoPassUserDTO) ThreadContextHolder.getParams()
                .get(CommonConstants.ORG_USER_INFO);

        result.setCurrentAuditStatus(ApplicationConst.AuditStatus.AUDITING);

        if (ObjectUtils.isNull(result.getProcessId())) {
            // 创建流程逻辑
            result.setCreateTime(DateUtils.getTime());
            result.setUpdateTime(DateUtils.getTime());
            result.insert();
            result.setProcessId(result.getId());
            result.setProcessVersion(CommonConstants.INIT_VERSION);
            result.setCreator(orgUserInfo.getViewName());
            result.setUpdater(orgUserInfo.getViewName());
            result.insertOrUpdate();
        } else {
            // 更改流程逻辑
            result.setUpdater(orgUserInfo.getViewName());
            result.setProcessVersion(result.getProcessVersion() + 1);
            result.setUpdateTime(DateUtils.getTime());
            result.insert();
        }
        return result;
    }

    @Override
    public Object query(Map<String, Object> params) {
        TgAuditProcessInfo tgAuditProcessInfo = new TgAuditProcessInfo();

        // One 查询
        if (ObjectUtils.isNotNull(params.get(CommonConstants.ID))) {
            return JsonBeanConverter.convert2Obj(tgAuditProcessInfo.selectById((Serializable) params.get(CommonConstants.ID)));
        }

        // Page 查询
        Integer pagenum = Integer.valueOf(ThreadContextHolder.getParams().get(CommonConstants.PAGENUM).toString());
        Integer pagesize = Integer.valueOf(ThreadContextHolder.getParams().get(CommonConstants.PAGESIZE).toString());
        if (ObjectUtils.isNotNull(pagenum, pagesize)) {
            PageHelper.startPage(pagenum, pagesize);
            PageInfo<TgAuditProcessInfo> pageInfo = new PageInfo<>(mapper.queryAuditProcessPageByProcessIdAndMaxVersion(params));
            pageInfo.getList().forEach(JsonBeanConverter::convert2Obj);
            return pageInfo;
        }

        return "error usage";
    }

    @Override
    public AjaxResult delete(Map<String, Object> params) {
        TgAuditProcessInfo tgAuditProcessInfo = new TgAuditProcessInfo();
        TgAuditProcessInfo info = tgAuditProcessInfo.selectById(Long.valueOf(String.valueOf(params.get(CommonConstants.ID))));

        Integer count = templateInfoMapper.selectCount(new QueryWrapper<TgTemplateInfo>().lambda().eq(TgTemplateInfo::getProcessId, info.getProcessId()));
        if (count > 0) {
            return AjaxResult.error("已有模板关联，不允许删除");
        }

        mapper.deleteByMap(new HashMap<String, Object>() {{
            put(CommonConstants.PROCESS_ID, info.getProcessId());
        }});
        return AjaxResult.success();
    }

    // 查询当前用户的申请列表
    @Override
    public PageInfo<?> queryAuditProcessApplicationList(Map<String, Object> params) {
        SysUser user = ThreadContextHolder.getSysUser();
        Integer pagenum = Integer.valueOf(ThreadContextHolder.getParams().get(CommonConstants.PAGENUM).toString());
        Integer pagesize = Integer.valueOf(ThreadContextHolder.getParams().get(CommonConstants.PAGESIZE).toString());
        AuditApplicationSearchDto searchDto = BeanUtil.mapToBean(params, AuditApplicationSearchDto.class, false, null);
        if (Objects.isNull(searchDto.getSearchContent())) {
            searchDto.setSearchContent(searchDto.getSearchProjectName());
        }
        PageHelper.startPage(pagenum, pagesize);
        Object applicationType = params.get("applicationType");

        if (Objects.equals(applicationType, ApplicationConst.ApplicationType.DOC_APPLICATION)) {
            // 如果检索的是文档申请, 返回文档申请分页
            List<DocApplicationPageDto> data = mapper.queryDocApplicationPageDtoByApplicationId(String.valueOf(user.getUserId()), searchDto);
            if (CollectionUtils.isNotEmpty(data)) {
                for (DocApplicationPageDto datum : data) {
                    if (StringUtils.isBlank(datum.getDocName())) {
                        datum.setDocName(datum.getDocNameBak() + "（该文档已被删除）");
                    }
                }
            }
            return new PageInfo<>(data);
        } else if (Objects.equals(applicationType, ApplicationConst.ApplicationType.DATA_APPLICATION)) {
            searchDto.setApplicationType(ApplicationConst.ApplicationType.DATA_APPLICATION);
            // 默认返回 模板 申请分页
            List<DataApplicationPageDto> pageData = mapper.queryDataApplicationPageDtoByApplicantId(String.valueOf(user.getUserId()), searchDto);
            if (CollectionUtils.isNotEmpty(pageData)) {
                List<Long> tableIds = pageData.stream().map(DataApplicationPageDto::getBaseTableId).distinct().collect(Collectors.toList());
                List<TableInfo> tableInfos = tableInfoService.getBaseMapper().selectBatchIds(tableIds);

                List<Long> appyIds = Lambda.buildList(pageData, DataApplicationPageDto::getApplicationId);
                Map<Long, Long> applyMap = userDataAssetsDAO.queryApplyAssets(appyIds);
                Map<Long, String> aliasMap = tableInfos.stream().collect(Collectors.toMap(TableInfo::getId,
                        TableInfo::getTableAlias, (front, current) -> current));

                Map<Long, AcceptanceRecord> recordMap = acceptanceRecordDAO.queryLatestStateByApplyId(appyIds);

                for (DataApplicationPageDto page : pageData) {
                    page.setTableAlias(aliasMap.get(page.getBaseTableId()));
                    page.setRelateAssets(Objects.nonNull(applyMap.get(page.getApplicationId())));
                    page.setState(Optional.ofNullable(recordMap.get(page.getApplicationId()))
                            .map(AcceptanceRecord::getState).orElse(null));
                }

            }
            return new PageInfo<>(pageData);
        } else if (Objects.equals(applicationType, ApplicationConst.ApplicationType.TABLE_APPLICATION)) {
            // 默认返回数据申请分页
            searchDto.setApplicationType(ApplicationConst.ApplicationType.TABLE_APPLICATION);
            // table alias
            List<DataApplicationPageDto> pageData = mapper.queryDataApplicationPageDtoByApplicantId(String.valueOf(user.getUserId()), searchDto);
            if (CollectionUtils.isNotEmpty(pageData)) {
                List<Long> tableIds = pageData.stream().map(DataApplicationPageDto::getBaseTableId).distinct().collect(Collectors.toList());
                List<TableInfo> tableInfos = tableInfoService.getBaseMapper().selectBatchIds(tableIds);
                Map<Long, String> aliasMap = tableInfos.stream().collect(Collectors.toMap(TableInfo::getId,
                        TableInfo::getTableAlias, (front, current) -> current));
                List<TableApplicationPageDto> result = new ArrayList<>();
                for (DataApplicationPageDto page : pageData) {
                    TableApplicationPageDto table = new TableApplicationPageDto();
                    BeanUtils.copyProperties(page, table);
                    table.setName(aliasMap.get(page.getBaseTableId()));
                    table.setExpireDate(page.getDataExpir());
                    result.add(table);
                }
                return new PageInfo<>(result);
            }
        }

        return new PageInfo<>();
    }

    @Override
    public Integer qeuryAuditProcessApplicationListCount(Map<String, Object> params) {
        SysUser user = ThreadContextHolder.getSysUser();
        AuditApplicationSearchDto searchDto = BeanUtil.mapToBean(params, AuditApplicationSearchDto.class, false, null);
        return mapper.queryDataApplicationPageDtoByApplicantId(String.valueOf(user.getUserId()), searchDto).size();
    }

    @Override
    public TgApplicationInfoDetailVO queryAuditProcessApplicationDetail(Long applicationId) {
        if (Objects.isNull(applicationId)) {
            throw new CustomException("参数缺失");
        }
        TgApplicationInfo result = JsonBeanConverter.convert2Obj(mapper.queryApplicationById(applicationId));

        // 请求消息查看
//        this.viewMessage(result.getId());

        TgApplicationInfoDetailVO vo = new TgApplicationInfoDetailVO();
        BeanUtil.copyProperties(result, vo);
        vo.setApplySQL(result.getAsql());

        if (Objects.nonNull(result.getAssetsId())) {
            UserDataAssets userDataAssets = UserDataAssets.newInstance()
                    .selectOne(new QueryWrapper<UserDataAssets>().lambda()
                            .select(UserDataAssets::getAssetsSql)
                            .eq(UserDataAssets::getId, result.getAssetsId()));
            if (userDataAssets != null) {
                vo.setAssetsSQL(userDataAssets.getAssetsSql());
            }
        }

        if (Objects.nonNull(result.getNewAssetId())) {
            TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance().selectById(result.getNewAssetId());
            if (Objects.nonNull(tgAssetInfo)) {
                vo.setAssetId(tgAssetInfo.getId());
                vo.setAssetName(tgAssetInfo.getAssetName());
            }
        }

        TgTemplateInfo tgTemplateInfo = templateInfoMapper.selectById(result.getTemplateId());
        if (Objects.nonNull(tgTemplateInfo)) {
            TgTemplateInfo template = JsonBeanConverter.convert2Obj(tgTemplateInfo);
            vo.setAllowApplicationPeriod(tgTemplateInfo.getAllowApplicationPeriod());
            vo.setColAttr(tgTemplateInfo.getColAttr());
            vo.setApplicationPeriodField(template.getApplicationPeriodField());
            vo.setMustSelectFields(template.getMustSelectFields());
            vo.setTemplateName(template.getTemplateName());
            vo.setJoinTableAttr(template.getJoinTableAttr());
            vo.setTemplateType(template.getTemplateType());
            vo.setBizType(template.getBizType());

            // TODO 模板数据渲染
        }

//        final SysUser user = userService.selectUserById(vo.getApplicantId());
//        final List<ResMaindataMainDepartmentSelectUserWithDeptItemDataItem> items = SinoipaasUtils.employeeWithDept(Lists.newArrayList(user.getOrgUserId()));
//        if (CollUtil.isNotEmpty(items)) {
//            final String deptName = DeptUtil.showDeptName(items.get(0).getOrgAdminTreePathText());
//            // 补充用户
//            vo.setApplicantDepartment(deptName);
//            vo.setApplicantName(deptName + "-" + user.getRealName());
//        }

        List<ProcessNodeEasyDto> handleNode = vo.getHandleNode();
        Map<Long, Map<String, Integer>> indexMap = vo.getHandlerIndexMapping();
        Map<Long, String> nameMap = indexMap.keySet().stream()
                .collect(Collectors.toMap(v -> v, v -> userService.getUserOnlyName(v), (front, current) -> current));
        if (CollectionUtils.isNotEmpty(handleNode)) {
            for (ProcessNodeEasyDto node : handleNode) {
                if (!Objects.equals(node.getStatus(), ApplicationConst.AuditStatus.AUDITING)) {
                    continue;
                }
                String index = node.getIndex() + "";
                String users = indexMap.entrySet().stream()
                        .filter(v -> v.getValue().containsKey(index)).map(Map.Entry::getKey)
                        .map(nameMap::get)
                        .collect(Collectors.joining("、"));
                node.setHandlerName("待审批人：" + users);
            }
        }

        Optional.ofNullable(result.getBaseTableId())
                .map(tableInfoService::getById)
                .map(TableInfo::getTableAlias)
                .ifPresent(vo::setTableAlias);
        // 补丁
        if (!Objects.equals(vo.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDIT_PASS)) {
            vo.setAssetsCreateTime(null);
            vo.setExpectDeliveryTime(null);
        }
        if (StringUtils.isNotBlank(vo.getTagProjectName()) && StringUtils.isNotBlank(vo.getTagTags())) {
            ProjectInfoEntity entity = customTagService.listRelateInfos(StrUtil.split(vo.getTagProjectName()), StrUtil.split(vo.getTagTags()));
            vo.setCascade(entity.getCascade());
            vo.setCascadeField(entity.getCascadeField());
        }

        return vo;
    }

    @Override
    public SyncApplyDetailVO querySyncDetail(Long applicationId) {
        TgApplicationInfo result = JsonBeanConverter.convert2Obj(mapper.queryApplicationById(applicationId));
        // 请求消息查看
        this.viewMessage(result.getId());

        // 申请id查询详情
        List<TgDataSyncApplication> applys = tgDataSyncApplicationMapper.selectList(
                new QueryWrapper<TgDataSyncApplication>().lambda()
                        .eq(TgDataSyncApplication::getApplicationId, applicationId));
        if (CollectionUtils.isEmpty(applys)) {
            return null;
        }

        TgDataSyncApplication apply = applys.get(0);
        SyncApplyDetailVO vo = new SyncApplyDetailVO();
        BeanUtils.copyProperties(apply, vo);
        BeanUtil.copyProperties(result, vo);

        final LambdaQueryWrapper<TgApplicationInfo> wq = Wrappers.<TgApplicationInfo>lambdaQuery()
                .eq(TgApplicationInfo::getId, apply.getApplicationId());
        final TgApplicationInfo tgApplicationInfo = applicationInfoMapper.selectOne(wq);
        vo.setSyncTaskReason(tgApplicationInfo.getApplyReason());
        vo.setExpiredDate(tgApplicationInfo.getDataExpir());

        final Integer targetDataSourceId = vo.getTargetDataSourceId();
        final Result<DataSourceDTO> detail = datasourceClient.detail(targetDataSourceId);
        if (detail.isSuccess()) {
            vo.setTargetDataSourceName(detail.getResult().getName());
        }

        final Integer id = apply.getId();

        final LambdaQueryWrapper<TgDataSyncFieldConfig> wq2 = Wrappers.<TgDataSyncFieldConfig>lambdaQuery()
                .eq(TgDataSyncFieldConfig::getSyncApplicationId, id);

        final List<TgDataSyncFieldConfig> tgDataSyncFieldConfigs = tgDataSyncFieldConfigMapper.selectList(wq2);
        final List<DataSyncTaskFieldConfig> collect = tgDataSyncFieldConfigs.stream()
                .map(config -> {
                    final DataSyncTaskFieldConfig dataSyncTaskFieldConfig = new DataSyncTaskFieldConfig();
                    BeanUtils.copyProperties(config, dataSyncTaskFieldConfig);
                    return dataSyncTaskFieldConfig;
                }).collect(Collectors.toList());
        vo.setFieldsConfigs(collect);

        return vo;
    }

    /**
     * 查询当前用户的审核列表，
     * 与申请列表不同， 审核列表需要判定当前用户是否处在审核链的当前节点，
     * 如果不是当前节点， 不显示信息，审核信息
     */
    @Override
    public List<AuditPageDto> queryAuditProcessAuditList(SysUser user, Map<String, Object> params) {
        Optional<SysUser> userOpt = Optional.ofNullable(user);
        String applicationType = (String) Optional.ofNullable(params.get("applicationType")).orElse(ApplicationConst.ApplicationType.DATA_APPLICATION);

        // 1. 查询用户当前需要审核的申请
        AuditApplicationSearchDto searchDto = BeanUtil.mapToBean(params, AuditApplicationSearchDto.class, false, null);

        List<TgApplicationInfo> list = Optional.ofNullable(mapper.queryApplicationByHandler(userOpt.map(SysUser::getUserId)
                .map(Object::toString).orElse(null), searchDto)).orElse(new ArrayList<>());
        List<AuditPageDto> result = new ArrayList<>();

        List<Long> templateIds = list.stream().map(TgApplicationInfo::getTemplateId).distinct().collect(Collectors.toList());
        Map<Long, TgTemplateInfo> templateMap;
        if (CollectionUtils.isNotEmpty(templateIds)) {
            List<TgTemplateInfo> tgTemplateInfos = templateInfoMapper.selectBatchIds(templateIds);
            templateMap = tgTemplateInfos.stream().collect(Collectors
                    .toMap(TgTemplateInfo::getId, v -> v, (front, current) -> current));
        } else {
            templateMap = Collections.emptyMap();
        }

        List<Long> userIds = list.stream()
                .flatMap(v -> Arrays.stream(v.getCurrentHandlers().split(",")))
                .filter(StringUtils::isNotBlank)
                .map(Long::valueOf)
                .distinct().collect(Collectors.toList());
        List<SysUser> userList = Lambda.queryListIfExist(userIds, userService::selectUserByIds);
        Map<Long, SysUser> userMap = userList.stream().collect(Collectors.toMap(SysUser::getUserId, v -> v, (front, current) -> current));

        List<Long> tableIds = list.stream().map(TgApplicationInfo::getBaseTableId).distinct().collect(Collectors.toList());

        Map<Long, String> tableAliasMap = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(tableIds)) {
            List<TableInfo> tableInfos = tableInfoService.getBaseMapper().selectBatchIds(tableIds);
            tableAliasMap = tableInfos.stream().collect(Collectors.toMap(TableInfo::getId, TableInfo::getTableAlias, (front, current) -> current));
        }

        Map<Long, Project> projectMap = Lambda.queryMapIfExist(Lambda.buildNonNullList(list, TgApplicationInfo::getProjectId),
                projectDAO::listByIds, Project::getId);

        List<Long> assetsIds = list.stream().map(TgApplicationInfo::getAssetsId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());

        List<UserDataAssets> assets = Lambda.queryListIfExist(assetsIds, v -> userDataAssetsDAO.getBaseMapper()
                .selectList(new QueryWrapper<UserDataAssets>().lambda()
                        .select(UserDataAssets::getId, UserDataAssets::getSrcApplicationId).in(UserDataAssets::getId, v)));
        Map<Long, Long> assetsApplyMap = Lambda.buildMap(assets, UserDataAssets::getId, UserDataAssets::getSrcApplicationId);

        for (TgApplicationInfo application : list) {
            if (Objects.equals(ApplicationConst.ApplicationType.DATA_APPLICATION, applicationType)) {
                if (!(Objects.equals(application.getApplicationType(), ApplicationConst.ApplicationType.DATA_APPLICATION)
                        || Objects.equals(application.getApplicationType(), ApplicationConst.ApplicationType.TABLE_APPLICATION))) {
                    continue;
                }
            } else {
                if (!applicationType.equals(application.getApplicationType())) {
                    continue;
                }
            }
            JsonBeanConverter.convert2Obj(application);
            AuditPageDto pageDTO = new AuditPageDto();
            BeanUtil.copyProperties(application, pageDTO, true);
            Optional<TgTemplateInfo> tempOpt = Optional.ofNullable(templateMap.get(application.getTemplateId()));
            pageDTO.setTemplateName(tempOpt.map(TgTemplateInfo::getTemplateName).orElse(null));
            pageDTO.setTemplateType(tempOpt.map(TgTemplateInfo::getTemplateType).orElse(null));
            pageDTO.setNewProjectName(Optional.ofNullable(projectMap.get(application.getProjectId())).map(Project::getName).orElse(""));
            pageDTO.setTableAlias(tableAliasMap.get(application.getBaseTableId()));
            pageDTO.setType(application.getConfigType());
            pageDTO.setSql(application.getConfigSql());
            Long latestApply = assetsApplyMap.get(application.getAssetsId());
            // TODO 多个重新申请先后执行时 new 维护异常
            pageDTO.setNewApplicationId(Objects.isNull(latestApply) || Objects.equals(latestApply, application.getId()) ? null : 1L);

            String currentHandlerNames = "";
            if (!"".equals(pageDTO.getCurrentHandlers())) {
                currentHandlerNames = StringUtils.join(Arrays.stream(pageDTO.getCurrentHandlers().split(","))
                        .map((id) -> {
                            SysUser sysUser = userMap.get(Long.valueOf(id));
                            SinoPassUserDTO temp = null;
                            try {
                                temp = SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId());
                            } catch (NullPointerException e) {
                                log.error("主数据人员组织编码为 NULL：{}", sysUser.getOrgUserId());
                            }

                            return temp != null ? temp.getViewName() : "";
                        }).collect(Collectors.toList()), ",");
            }

            pageDTO.setCurrentHandlerNames(currentHandlerNames);

            if (Objects.equals(searchDto.getSearchStatus(), CommonConstants.UNHANDLE)) {
                // 搜索未处理节点链
                if (application.getCurrentIndex() < application.getHandleNode().size()
                        && (null != pageDTO.getCurrentAuditProcessStatus()
                        && pageDTO.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.AUDITING))
                ) {
                    ProcessNodeEasyDto p = application.getHandleNode().get(application.getCurrentIndex());
                    if (p.getHandleStatus().equals(CommonConstants.UNHANDLE)) {
                        pageDTO.setCurrentAuditNodeStatus(p.getStatus());
                        pageDTO.setApplicationId(application.getId());
                        pageDTO.setHandleStatus(p.getHandleStatus());
                        pageDTO.setHandleReason(p.getHandleReason());
                        pageDTO.setCurrentHandlerNames(currentHandlerNames);
                        this.fillLastName(application, pageDTO);

                        pageDTO.setDocAuthorization(application.getDocAuthorization());
                        result.add(pageDTO);
                    }
                }
            } else {
                // 文档审核记录报表 用户为空
                if (Objects.isNull(user)) {
                    for (ProcessNodeEasyDto p : application.getHandleNode()) {
                        if (Objects.equals(p.getHandleStatus(), CommonConstants.UNHANDLE)) {
                            continue;
                        }

                        AuditPageDto tempPage = new AuditPageDto();
                        BeanUtils.copyProperties(pageDTO, tempPage);
                        tempPage.setCurrentAuditNodeStatus(p.getStatus());
                        tempPage.setApplicationId(application.getId());
                        tempPage.setHandleStatus(p.getHandleStatus());
                        tempPage.setHandleReason(p.getHandleReason());
                        tempPage.setCurrentHandlerNames(p.getHandlerName());
                        tempPage.setDocAuthorization(application.getDocAuthorization());
                        result.add(tempPage);
                    }
                } else {
                    // 搜索已处理节点链
                    Map<String, Integer> handlerIndexMap = application.getHandlerIndexMapping().get(user.getUserId());
                    appendResult(result, application, pageDTO, handlerIndexMap);

                    this.fillLastName(application, pageDTO);
                }
            }
        }

        // 过滤审核状态
        if (ObjectUtils.isNotNull(searchDto.getSearchProcessStatus())) {
            result = result.stream().filter(n -> n.getCurrentAuditProcessStatus() == (Integer.parseInt(searchDto.getSearchProcessStatus())))
                    .collect(Collectors.toList());
        }

        // 当前审核人只关心自己未审核的数据
        if (Objects.equals(searchDto.getSearchStatus(), CommonConstants.UNHANDLE)) {
            searchDto.setSearchHandlerName(SinoipaasUtils.mainEmployeeSelectbyid(ThreadContextHolder.getSysUser().getOrgUserId()).getViewName());
        }
        // 过滤审核人
        if (ObjectUtils.isNotNull(searchDto.getSearchHandlerName())) {
            result = result.stream().filter((n) -> n.getCurrentHandlerNames().contains(searchDto.getSearchHandlerName()))
                    .collect(Collectors.toList());
        }

        List<Long> applyIds = Lambda.buildList(result, AuditPageDto::getApplicationId);
        Map<Long, AcceptanceRecord> recordMap = acceptanceRecordDAO.queryLatestStateByApplyId(applyIds);
        for (AuditPageDto dto : result) {
            dto.setState(Optional.ofNullable(recordMap.get(dto.getApplicationId())).map(AcceptanceRecord::getState).orElse(null));
        }

        // 3. 分页返回当前数据
        return result.stream().sorted(Comparator.comparing(AuditPageDto::getCreateTime).reversed()).collect(Collectors.toList());
    }

    private void fillLastName(TgApplicationInfo application, AuditPageDto pageDTO) {
        ApplyUtil.lastNode(application).map(ProcessNodeEasyDto::getHandlerName)
                .ifPresent(pageDTO::setLastHandlerName);
    }

    private void fillLastName(TgApplicationInfo application, AuditPageByTypeDto pageDTO) {
        ApplyUtil.lastNode(application).map(ProcessNodeEasyDto::getHandlerName)
                .ifPresent(pageDTO::setLastHandlerName);
    }

    private static void appendResult(List<AuditPageDto> result, TgApplicationInfo application, AuditPageDto pageDTO, Map<String, Integer> handlerIndexMap) {
        final boolean[] judge = {false};
        final int[] index = {0};
        if (ObjectUtils.isNotNull(handlerIndexMap)) {
            handlerIndexMap.forEach((k, v) -> {
                if (!v.equals(ApplicationConst.AuditStatus.AUDITING)) {
                    judge[0] = true;
                }
                if (!v.equals(ApplicationConst.AuditStatus.AUDITING) && index[0] < Integer.parseInt(k)) {
                    index[0] = Integer.parseInt(k);
                }
            });
            if (judge[0]) {
                ProcessNodeEasyDto p = application.getHandleNode().get(index[0]);
                pageDTO.setCurrentAuditNodeStatus(p.getStatus());
                pageDTO.setApplicationId(application.getId());
                pageDTO.setHandleStatus(p.getHandleStatus());
                pageDTO.setHandleReason(p.getHandleReason());
                pageDTO.setDocAuthorization(application.getDocAuthorization());
                result.add(pageDTO);
            }
        }
    }

    private static void appendResult(List<AuditPageByTypeDto> result, TgApplicationInfo application, AuditPageByTypeDto pageDTO, Map<String, Integer> handlerIndexMap) {
        final boolean[] judge = {false};
        final int[] index = {0};
        if (ObjectUtils.isNotNull(handlerIndexMap)) {
            handlerIndexMap.forEach((k, v) -> {
                if (!v.equals(ApplicationConst.AuditStatus.AUDITING)) {
                    judge[0] = true;
                }
                if (!v.equals(ApplicationConst.AuditStatus.AUDITING) && index[0] < Integer.parseInt(k)) {
                    index[0] = Integer.parseInt(k);
                }
            });
            if (judge[0]) {
                ProcessNodeEasyDto p = application.getHandleNode().get(index[0]);
                pageDTO.setCurrentAuditNodeStatus(p.getStatus());
                pageDTO.setApplicationId(application.getId());
                pageDTO.setHandleStatus(p.getHandleStatus());
                pageDTO.setHandleReason(p.getHandleReason());
                pageDTO.setDocAuthorization(application.getDocAuthorization());
                result.add(pageDTO);
            }
        }
    }

    // 查找需要被审核的申请详情
    @Override
    public Object qeuryAuditProcessAuditDetail(Long applicationId) {

        TgApplicationInfo result = JsonBeanConverter.convert2Obj(mapper.queryApplicationById(applicationId));

        // 请求消息查看
        viewMessage(result.getId());

        return result;
    }

    @Override
    public Pair<Boolean, Long> overLimit(Long applicationId) {
        TgApplicationInfo apply = TgApplicationInfo.newInstance().selectById(applicationId);
        return this.overLimit(apply);
    }

    private Pair<Boolean, Long> overLimit(TgApplicationInfo tgApplicationInfo) {
        if (!Objects.equals(tgApplicationInfo.getApplicationType(), ApplicationConst.ApplicationType.DATA_APPLICATION)) {
            return Pair.of(false, 0L);
        }

        TgTemplateInfo template = new TgTemplateInfo().selectById(tgApplicationInfo.getTemplateId());
        Optional<TemplateTypeEnum> typeOpt = TemplateTypeEnum.of(template.getTemplateType());
        if (!typeOpt.isPresent()) {
            return Pair.of(false, 0L);
        }

        TemplateTypeEnum type = typeOpt.get();
        if (!type.isWideTableType()) {
            return Pair.of(false, -1L);
        }
        Long dataTotal = applicationService.countApplicationDataFromCk(tgApplicationInfo.getAsql(), "");
        // 追加长尾数据量
        if (StringUtils.isNotBlank(tgApplicationInfo.getTailSql())) {
            dataTotal += applicationService.countApplicationDataFromCk(tgApplicationInfo.getTailSql(), "");
        }
        return Pair.of(Objects.nonNull(dataTotal) && dataTotal > appProperties.getAuditWarnCount(), dataTotal);
    }

    @MessagePointCut
    @Override
    public Object auditProcess(AuditRequest node) {
        SysUser sysUser = ThreadContextHolder.getSysUser();
        SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId());
        TgApplicationInfo apply = new TgApplicationInfo().selectById(node.getApplicationId());
        if (Objects.isNull(apply)) {
            throw new CustomException("申请不存在 " + node.getApplicationId());
        }
        if (Objects.equals(apply.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDIT_PASS)) {
            throw new CustomException("该申请已完成全部审核，请勿重复操作");
        }

        JsonBeanConverter.convert2Obj(apply);
        ProcessNodeEasyDto result = apply.getHandleNode().get(node.getIndex());
        result.setStatus(node.getStatus());
        result.setHandleTime(DateUtils.getTime());
        result.setHandlerName(sinoPassUserDTO.getUserName());
        result.setHandleReason(node.getHandleReason());
        result.setDeliverDay(node.getDeliverDay());
        // 进入审核时，重置状态为none，为批量审核功能打补丁
        apply.setDataState(ApplyDataStateEnum.none.name());
        apply.setEvaluationResult(node.getEvaluationResult());

        // 流程状态判定
        final boolean auditPassFinish = node.getStatus().equals(ApplicationConst.AuditStatus.AUDIT_PASS)
                && apply.getCurrentIndex() >= apply.getHandleNode().size() - 1;
        final boolean auditPassIn = node.getStatus().equals(ApplicationConst.AuditStatus.AUDIT_PASS)
                && !(apply.getCurrentIndex() >= apply.getHandleNode().size() - 1);
        final boolean auditFail = node.getStatus().equals(ApplicationConst.AuditStatus.AUDIT_FAIL);
        if (auditPassFinish) {
            Optional<ApplicationForm> applicationForm = applicationFormDAO.queryByNo(apply.getApplicationNo());
            Boolean running = applicationForm.map(v -> Objects.equals(v.getApplyRunState(), ApplyRunStateEnum.running.name())).orElse(false);
            if (running) {
                throw new RuntimeException(apply.getApplicationNo() + " 正在执行中，当次审核通过无效，需稍后再审核");
            }

            // 全节点链通过
            apply.setApplyPassedTime(DateUtils.getTime());
            apply.setStatus(ApplicationConst.ApplyStatus.ENABLE);
            apply.setCurrentAuditProcessStatus(ApplicationConst.AuditStatus.AUDIT_PASS);

            if (BooleanUtils.isNotTrue(node.getConfirmData())) {
                Pair<Boolean, Long> limitRes = this.overLimit(apply);
                if (limitRes.getKey()) {
                    log.warn("over limit: id={} name={}", apply.getId(), apply.getProjectName());
                    applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                            .set(TgApplicationInfo::getDataState, ApplyDataStateEnum.wait_confirm.name())
                            .eq(TgApplicationInfo::getId, apply.getId())
                    );

                    List<Long> userIds = apply.getHandlerIndexMapping().entrySet().stream()
                            .filter(v -> v.getValue().containsKey(node.getIndex() + "")).map(Map.Entry::getKey)
                            .collect(Collectors.toList());
                    for (Long userId : userIds) {
                        messageRecordDimDAO.createByRun(apply, userId, ApplyDataStateEnum.wait_confirm);
                    }
                    // 特殊化处理，规避AOP流程
                    return null;
                }

                if (limitRes.getValue() >= 0) {
                    apply.setDataState(ApplyDataStateEnum.run.name());
                }
            } else {
                if (Objects.equals(apply.getApplicationType(), ApplicationConst.ApplicationType.DATA_APPLICATION)) {
                    TgTemplateInfo template = new TgTemplateInfo().selectById(apply.getTemplateId());
                    Optional<TemplateTypeEnum> typeOpt = TemplateTypeEnum.of(template.getTemplateType());
                    if (typeOpt.isPresent()) {
                        TemplateTypeEnum type = typeOpt.get();
                        if (type.isWideTableType()) {
                            apply.setDataState(ApplyDataStateEnum.run.name());
                        }
                    }
                }
            }

            // 最终审核通过
            final TgNoticeRead tgNoticeRead2 = new TgNoticeRead();
            tgNoticeRead2.setUserId(apply.getApplicantId());
            tgNoticeRead2.setApplicationId(apply.getId());
            tgNoticeRead2.setHasRead(0);
            tgNoticeRead2.setAuditUserId(sysUser.getUserId());
            tgNoticeRead2.setAuditType(1);
            tgNoticeRead2.setBizType(2);
            tgNoticeRead2.setCreateTime(new Date());
            tgNoticeReadMapper.insert(tgNoticeRead2);

            ttl.execute(() -> this.postHandleForPass(apply, result));
            // ws推送通知
            wsMsgService.noticeAudit(apply.getId());
            wsMsgService.pushNoticeMsg(Lists.newArrayList(apply.getApplicantId()), tgNoticeRead2.getId());
        } else if (auditPassIn) {
            // 还在走节点链
            apply.setCurrentAuditProcessStatus(ApplicationConst.AuditStatus.AUDITING);
            final Integer index = node.getIndex();
            apply.setCurrentIndex(index + 1);
            TgAuditProcessInfo process = JsonBeanConverter.convert2Obj(queryCurrentProcess(apply.getProcessId()));
            apply.setCurrentHandlers(StringUtils.join(process.getProcessChainDetailInfo()
                    .get(apply.getCurrentIndex()).getHandlers(), ","));

            // 记录中间审核人
            final TgNoticeRead tgNoticeRead2 = new TgNoticeRead();
            tgNoticeRead2.setUserId(apply.getApplicantId());
            tgNoticeRead2.setApplicationId(apply.getId());
            tgNoticeRead2.setHasRead(0);
            tgNoticeRead2.setAuditUserId(sysUser.getUserId());
            tgNoticeRead2.setAuditType(4);
            tgNoticeRead2.setBizType(2);
            tgNoticeRead2.setCreateTime(new Date());
            tgNoticeReadMapper.insert(tgNoticeRead2);

            // ws推送代办
            wsMsgService.noticeAudit(apply.getId());

        } else if (auditFail) {
            // 节点拒绝
            apply.setCurrentAuditProcessStatus(ApplicationConst.AuditStatus.AUDIT_FAIL);
            // 审核驳回
            final TgNoticeRead tgNoticeRead = new TgNoticeRead();
            tgNoticeRead.setUserId(apply.getApplicantId());
            tgNoticeRead.setApplicationId(apply.getId());
            tgNoticeRead.setHasRead(0);
            tgNoticeRead.setAuditUserId(sysUser.getUserId());
            tgNoticeRead.setAuditType(2);
            tgNoticeRead.setBizType(2);
            tgNoticeRead.setCreateTime(new Date());
            tgNoticeReadMapper.insert(tgNoticeRead);

            applicationFormDAO.auditApplyReject(apply.getId(), apply.getApplicationNo());

            // ws推送通知
            wsMsgService.noticeAudit(apply.getId());
            wsMsgService.pushNoticeMsg(Lists.newArrayList(apply.getApplicantId()), tgNoticeRead.getId());
        }


        result.setHandleStatus(CommonConstants.HANDLED);

        // 修改处理详情链节点映射信息
        Map<String, Integer> handleNodeMap = apply.getHandlerIndexMapping().get(sysUser.getUserId());
        handleNodeMap.put(node.getIndex().toString(), node.getStatus());

        // 修改状态链
        List<Integer> statusChain = Arrays.stream(apply.getStatusChain().split(","))
                .mapToInt(Integer::valueOf).boxed().collect(Collectors.toList());
        statusChain.set(node.getIndex(), node.getStatus());
        apply.setStatusChain(StringUtils.join(statusChain, ","));
        JsonBeanConverter.convert2Json(apply);
        apply.insertOrUpdate();
        boolean active = Objects.equals(ApplicationConst.AuditStatus.AUDIT_PASS, apply.getCurrentAuditProcessStatus());
        applicationTaskConfigDAO.lambdaUpdate()
                .eq(ApplicationTaskConfig::getApplicationId, apply.getId())
                .set(ApplicationTaskConfig::getActive, active)
                .update();

        // 中间节点异常处理
        if (auditPassIn) {
            this.handleException(apply);
        }
        return result;
    }

    /**
     * 异常处理
     * 1、 判断流程是否结束
     * 2、 判断当前流程节点的处理人是否都已离职
     *
     * @param tgApplicationInfo 申请
     */
    public void handleException(TgApplicationInfo tgApplicationInfo) {
        // 当前处理的流程节点下标
        final Integer currentIndex = tgApplicationInfo.getCurrentIndex();
        // 当前节点的处理人userId
        List<Long> handlerIdList = ListUtil.toList(tgApplicationInfo.getCurrentHandlers(), Long::valueOf);
        List<SysUser> handlerList = userService.listByIds(handlerIdList);
        handlerList.forEach(sysUser -> {
            if (StringUtils.isNotEmpty(sysUser.getOrgUserId())) {
                sysUser.setSinoPassUserDTO(SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId()));
            }
        });
        List<SysUser> normalHandlerList = handlerList.stream()
                .filter(sysUser -> {
                    String statusText = Optional.ofNullable(sysUser.getSinoPassUserDTO()).map(SinoPassUserDTO::getEmployeeStatusText).orElse(null);
                    return statusText == null || !StringUtils.equals(sysUser.getSinoPassUserDTO().getEmployeeStatusText(), "离职");
                })
                .collect(Collectors.toList());
        // 全部都已离职，则走异常处理
        if (CollectionUtils.isEmpty(normalHandlerList)) {
            TgAuditProcessInfo process = JsonBeanConverter.convert2Obj(queryCurrentProcess(tgApplicationInfo.getProcessId()));
            ProcessNodeDetailDto processNode = process.getProcessChainDetailInfo().get(currentIndex);
            ProcessNodeEasyDto result = tgApplicationInfo.getHandleNode().get(currentIndex);
            // 延续
            boolean circulation = false;
            Long abnormal = Long.valueOf(processNode.getAbnormalHandle());
            if (abnormal == 1L) {
                final int nodeStatus = ApplicationConst.AuditStatus.AUDIT_PASS;
                // 判断是不是走到最后一个节点
                final boolean finish = currentIndex >= tgApplicationInfo.getHandleNode().size() - 1;
                if (finish) {
                    // 全节点链通过
                    tgApplicationInfo.setApplyPassedTime(DateUtils.getTime());
                    tgApplicationInfo.setStatus(ApplicationConst.ApplyStatus.ENABLE);
                    tgApplicationInfo.setCurrentAuditProcessStatus(ApplicationConst.AuditStatus.AUDIT_PASS);

                    ttl.execute(() -> this.postHandleForPass(tgApplicationInfo, result));
                } else {
                    circulation = true;
                    // 还在走流程
                    tgApplicationInfo.setCurrentAuditProcessStatus(ApplicationConst.AuditStatus.AUDITING);
                    // 流转到下一个节点
                    tgApplicationInfo.setCurrentIndex(currentIndex + 1);
                    // 下一个节点的处理人
                    tgApplicationInfo.setCurrentHandlers(StringUtils.join(process.getProcessChainDetailInfo()
                            .get(tgApplicationInfo.getCurrentIndex()).getHandlers(), ","));
                }
                // 当前节点自动同意
                result.setStatus(nodeStatus);
                result.setHandleStatus(CommonConstants.HANDLED);
                result.setHandleTime(DateUtils.getTime());
                result.setHandleReason("节点审批人员离职自动同意");
                // 修改处理详情链节点映射信息,这里应该不需要处理
                Map<String, Integer> handleNodeMap = tgApplicationInfo.getHandlerIndexMapping().get(handlerList.get(0).getUserId());
                handleNodeMap.put(currentIndex.toString(), nodeStatus);
                // 修改状态链
                List<Integer> statusChain = Arrays.stream(tgApplicationInfo.getStatusChain().split(","))
                        .mapToInt(Integer::valueOf).boxed().collect(Collectors.toList());
                statusChain.set(currentIndex, nodeStatus);
                tgApplicationInfo.setStatusChain(StringUtils.join(statusChain, ","));
            } else {
                // 自动驳回
                // 流程驳回
                tgApplicationInfo.setCurrentAuditProcessStatus(ApplicationConst.AuditStatus.AUDIT_FAIL);
                result.setStatus(ApplicationConst.AuditStatus.AUDIT_FAIL);
                result.setHandleStatus(CommonConstants.HANDLED);
                result.setHandleTime(DateUtils.getTime());
                result.setHandleReason("节点审批人员离职自动驳回");
            }
            //
            tgApplicationInfo.insertOrUpdate();

            if (circulation) {
                this.handleException(tgApplicationInfo);
            }
        }
    }


    /**
     * 审批通过后 后置业务处理
     */
    private void postHandleForPass(TgApplicationInfo apply, ProcessNodeEasyDto result) {
        this.createDataAssetsAfterAudit(apply, result);
        this.invokeSyncAfterAudit(apply);
        this.deprecatedOldApply(apply);
    }

    /**
     * 数据交换工作流调度执行
     */
    private void invokeSyncAfterAudit(TgApplicationInfo apply) {
        if (!Objects.equals(apply.getApplicationType(), ApplicationConst.ApplicationType.DATA_SYNC_APPLICATION)) {
            return;
        }

        AjaxResult<Void> createRes = syncTaskService.upsertTaskConfigAndProcess(apply.getId());
        if (!createRes.isSuccess()) {
            log.error("执行异常: createRes={}", createRes);
            throw new CustomException("创建尚书台同步配置失败");
        }

        AjaxResult execResult = syncTaskService.executeWorkFlow(apply.getId());
        if (!execResult.isDolphinSuccess()) {
            log.error("执行异常: execResult={}", execResult);
            throw new CustomException("执行工作流异常");
        }
    }

    private void deprecatedOldApply(TgApplicationInfo apply) {
//        this.deprecatedDocApply(apply);
        this.deprecatedDataApply(apply);
        this.deprecatedTableApply(apply);
    }

    private void deprecatedTableApply(TgApplicationInfo apply) {
        if (Objects.isNull(apply)) {
            return;
        }
        if (!Objects.equals(apply.getApplicationType(), ApplicationConst.ApplicationType.TABLE_APPLICATION)) {
            return;
        }
        List<TgApplicationInfo> brother = applicationInfoMapper.selectList(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getId)
                .eq(TgApplicationInfo::getBaseTableId, apply.getBaseTableId())
                .eq(TgApplicationInfo::getApplicationType, ApplicationConst.ApplicationType.TABLE_APPLICATION)
                .eq(TgApplicationInfo::getApplicantId, apply.getApplicantId())
                .ne(TgApplicationInfo::getId, apply.getId())
        );

        Set<Long> ids = brother.stream().map(TgApplicationInfo::getId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        log.info("作废申请：ids={}", ids);
        applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                .set(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.INVALID_APPLICATION)
                .in(TgApplicationInfo::getId, ids)
        );
    }

    private void deprecatedDocApply(TgApplicationInfo apply) {
        if (Objects.isNull(apply)) {
            return;
        }
        if (!Objects.equals(apply.getApplicationType(), ApplicationConst.ApplicationType.DOC_APPLICATION)) {
            return;
        }

        List<TgApplicationInfo> brother = applicationInfoMapper.selectList(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getId)
                .eq(TgApplicationInfo::getDocId, apply.getDocId())
                .eq(TgApplicationInfo::getApplicationType, ApplicationConst.ApplicationType.DOC_APPLICATION)
                .eq(TgApplicationInfo::getApplicantId, apply.getApplicantId())
                .ne(TgApplicationInfo::getId, apply.getId())
        );
        Set<Long> ids = brother.stream().map(TgApplicationInfo::getId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        log.info("作废申请：ids={}", ids);
        applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                .set(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.INVALID_APPLICATION)
                .in(TgApplicationInfo::getId, ids)
        );
    }

    /**
     * 当前申请单审批通过时，作废原申请，原申请的派生申请
     */
    private void deprecatedDataApply(TgApplicationInfo apply) {
        if (Objects.isNull(apply)) {
            return;
        }
        if (!Objects.equals(apply.getApplicationType(), ApplicationConst.ApplicationType.DATA_APPLICATION)
                && !Objects.equals(apply.getApplicationType(), ApplicationConst.ApplicationType.DATA_SYNC_APPLICATION)) {
            return;
        }

        Long oldApplicationId = apply.getOldApplicationId();
        if (Objects.isNull(oldApplicationId)) {
            return;
        }

        Set<Long> ids = new HashSet<>();
        TgApplicationInfo origin = applicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getId)
                .eq(TgApplicationInfo::getId, apply.getOldApplicationId())
        );
        ids.add(origin.getId());

        List<TgApplicationInfo> brother = applicationInfoMapper.selectList(new QueryWrapper<TgApplicationInfo>().lambda()
                        .select(TgApplicationInfo::getId)
                        .eq(TgApplicationInfo::getOldApplicationId, apply.getOldApplicationId())
                        .ne(TgApplicationInfo::getId, apply.getId())
//                .in(TgApplicationInfo::getCurrentAuditProcessStatus, CommonConstants.NEED_INVALID)
        );
        for (TgApplicationInfo info : brother) {
            ids.add(info.getId());
        }
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        log.info("作废申请：ids={}", ids);
        applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                .set(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.INVALID_APPLICATION)
                .in(TgApplicationInfo::getId, ids)
        );
        applicationTaskConfigDAO.lambdaUpdate()
                .set(ApplicationTaskConfig::getActive, false)
                .in(ApplicationTaskConfig::getApplicationId, ids)
                .update();
    }

    /**
     * 数据类的申请 审核通过后创建资产
     */
    private void createDataAssetsAfterAudit(TgApplicationInfo apply, ProcessNodeEasyDto result) {
        if (!Objects.equals(apply.getApplicationType(), ApplicationConst.ApplicationType.DATA_APPLICATION)) {
            return;
        }

        Optional<TgTemplateInfo> tempOpt = templateInfoDAO.lambdaQuery()
                .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateType, TgTemplateInfo::getBizType)
                .eq(TgTemplateInfo::getId, apply.getTemplateId())
                .oneOpt();
        Optional<TemplateTypeEnum> typeOpt = tempOpt.flatMap(v -> TemplateTypeEnum.of(v.getTemplateType()));
        if (!typeOpt.isPresent()) {
            log.error("not support type: {}", apply.getTemplateId());
            return;
        }

        CurrentDataPlanDTO plan = dataPlanService.currentPeriod(tempOpt.get().getBizType());
        applicationFormDAO.auditApplyPass(apply.getId(), apply.getApplicationNo(), plan.getPeriod());
        LocalDateTime expectDeliveryTime = DateUtils.addDaysSkippingWeekends(LocalDateTime.now(), result.getDeliverDay());
        if (Objects.nonNull(expectDeliveryTime)) {
            try {
                applicationDAO.lambdaUpdate()
                        .set(TgApplicationInfo::getExpectDeliveryTime, expectDeliveryTime)
                        .eq(TgApplicationInfo::getId, apply.getId()).update();
            } catch (Exception e) {
                log.error("", e);
            }
        }
        apply.setExpectDeliveryTime(expectDeliveryTime);

        applicationTaskConfigDAO.getBaseMapper().update(null,
                new UpdateWrapper<ApplicationTaskConfig>().lambda()
                        .eq(ApplicationTaskConfig::getApplicationId, apply.getId())
                        .set(ApplicationTaskConfig::getApproveTime, LocalDateTime.now())
        );

        // 通用模式 不创建资产，审核通过后 在审核页面配置好，通过执行动作触发工作流，同样是回调后创建资产
        TemplateTypeEnum type = typeOpt.get();
        if (type.isManualTaskType()) {
            return;
        }

        if (type.isWideTableType()) {
            // 宽表模式 直接创建资产
            this.handleDataAssetsForWide(apply);
        } else if (type.isNormalType()) {
            // 常规模式 不创建资产，回调后创建, 此处启动工作流
            this.handleAssetsForNormal(apply);
        }
    }

    private void handleAssetsForNormal(TgApplicationInfo apply) {
        AjaxResult exeResult;
        if (Objects.nonNull(apply.getOldApplicationId())) {
            ExecFlowParam param = ExecFlowParam.builder().applicationId(apply.getId())
                    .triggerId(DataAssetsConstants.RE_APPLY_TRIGGER_ID)
                    .taskName("重新申请")
                    .build();
            exeResult = userDataAssetsService.executeWorkFlow(param);
        } else {
            exeResult = userDataAssetsService.executeWorkFlow(apply.getId());
        }
        if (!exeResult.isSuccess()) {
            log.error("执行失败: exeResult={}", exeResult);
        } else {
            applicationFormService.runApplication(apply.getId(), apply.getApplicationNo());
        }

//        final LambdaQueryWrapper<TgNoticeRead> wq = Wrappers.<TgNoticeRead>lambdaQuery()
//                .eq(TgNoticeRead::getApplicationId, apply.getId());
//
//        final List<TgNoticeRead> tgNoticeReads = tgNoticeReadMapper.selectList(wq);
//        final UserDataAssets userDataAssets = userDataAssetsDAO.getBaseMapper().selectById(apply.getAssetsId());
//        for (TgNoticeRead tgNoticeRead : tgNoticeReads) {
//            tgNoticeRead.setVersion(userDataAssets.getVersion());
//            tgNoticeReadMapper.updateById(tgNoticeRead);
//        }
    }

    private void handleDataAssetsForWide(TgApplicationInfo apply) {
        applicationFormService.runApplication(apply.getId(), apply.getApplicationNo());

        String tableName = ApplicationSqlUtil.buildTableName(apply.getId());
        boolean createResult = syncHelper.createLocalSnapshotTable(apply.getId(), tableName);
        if (!createResult) {
            applicationFormDAO.updateRunState(apply.getApplicationNo(), ApplyRunStateEnum.run_failed);
            throw new CustomException("创建CK资产表失败");
        }

        // 断言 宽表模式不会失败
        applicationFormDAO.updateRunState(apply.getApplicationNo(), ApplyRunStateEnum.wait_accept);

        // 考虑：异步创建资产，预留错误补偿接口
        UpsertAssetsBO bo = UpsertAssetsBO.builder()
                .info(apply)
                .tableName(tableName)
                .snapshotType(AssetsSnapshotTypeEnum.re_apply.name())
                .build();
        userDataAssetsService.upsertDataAssets(bo);


        // 导出数据资产数据到ftp服务器
        userDataAssetsUploadFtpHelper.addFtpTask(apply.getAssetsId());

        applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                .set(TgApplicationInfo::getDataState, ApplyDataStateEnum.success.name())
                .eq(TgApplicationInfo::getId, apply.getId())
        );

        // 修改通知
        final Long id = apply.getId();
        final LambdaQueryWrapper<TgNoticeRead> wq = Wrappers.<TgNoticeRead>lambdaQuery()
                .eq(TgNoticeRead::getApplicationId, id)
                .in(TgNoticeRead::getAuditType, com.google.common.collect.Lists.newArrayList(1, 4));
        final List<TgNoticeRead> tgNoticeReads = tgNoticeReadMapper.selectList(wq);
        final UserDataAssets userDataAssets = userDataAssetsDAO.getBaseMapper().selectById(apply.getAssetsId());
        userDataAssetsService.copyNewNotice(tgNoticeReads,
                Optional.ofNullable(userDataAssets).map(UserDataAssets::getVersion).orElse(1),
                AuditTypeEnum.SUCCESS);

        messageRecordDimDAO.createByRun(apply, apply.getApplicantId(), ApplyDataStateEnum.success);
        messageRecordDimDAO.createByRun(apply, SecurityUtils.getUserId(), ApplyDataStateEnum.success);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateGeneric(TgAuditProcessInfo auditProcessInfo) {
        List<TgAuditProcessInfo> all = auditProcessInfo.selectAll();
        all.forEach((a) -> {
            a.setIsGeneric(CommonConstants.NONGENERIC.toString());
            a.updateById();
        });
        auditProcessInfo = auditProcessInfo.selectById();
        auditProcessInfo.setIsGeneric(CommonConstants.GENERIC.toString());
        auditProcessInfo.updateById();
    }

    @Override
    public TgAuditProcessInfo queryProcessByIdAndVersion(Long processId, Integer processVersion) {
        return mapper.queryProcessByIdAndVersion(processId, processVersion);
    }

    @Override
    public TgAuditProcessInfo queryGenericProcess() {
        return mapper.queryProcessByGenericAndMaxVersion();
    }

    @Override
    public TgAuditProcessInfo queryCurrentProcess(Long processId) {
        return mapper.queryProcessByIdAndMaxVersion(processId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean extendsAuditProcess(Long oldUserId, Long newUserId) {
        List<TgAuditProcessInfo> tgAuditProcessInfos = mapper.queryProcessNeedToUpdate(oldUserId);
        for (TgAuditProcessInfo tgAuditProcessInfo : tgAuditProcessInfos) {
            JsonBeanConverter.convert2Obj(tgAuditProcessInfo);
            List<ProcessNodeDetailDto> processChainDetailInfo = tgAuditProcessInfo.getProcessChainDetailInfo();
            processChainDetailInfo.forEach(p -> {
                List<Long> newHandlers = p.getHandlers().stream().map(h -> {
                    if (h.longValue() == oldUserId.longValue()) {
                        return newUserId;
                    }
                    return h;
                }).collect(Collectors.toList());
                p.setHandlers(newHandlers);
                p.getNoticesInfo().forEach(n -> {
                    List<String> newNames = n.getNames().stream().map(s -> {
                        if (s.equals(oldUserId.toString())) {
                            return newUserId.toString();
                        }
                        return s;
                    }).collect(Collectors.toList());
                    n.setNames(newNames);
                });
            });

            JsonBeanConverter.convert2Json(tgAuditProcessInfo).updateById();
        }

        return true;
    }

    @Override
    public List<AuditPageByTypeDto> queryAuditProcessAuditListByUser(SysUser user) {
        Optional<SysUser> userOpt = Optional.ofNullable(user);

        // 1. 查询用户当前需要审核的申请
        Map<String, Object> params = new HashMap<>();
        params.put("searchStatus", 1);
        AuditApplicationSearchDto searchDto = BeanUtil.mapToBean(params, AuditApplicationSearchDto.class, false, null);
        /*if (searchDto.getApplicationType().equals(ApplicationConst.ApplicationType.TABLE_APPLICATION)) {
            final List<String> list = Lists.newArrayList(searchDto.getApplicationType(), ApplicationConst.ApplicationType.DATA_SYNC_APPLICATION);
            searchDto.setExtraApplicationType(list);
        }*/

        // 复杂查询
        List<TgApplicationInfo> list = Optional.ofNullable(mapper.queryApplyByHandlerAndType(userOpt.map(SysUser::getUserId)
                .map(Object::toString).orElse(null), searchDto)).orElse(new ArrayList<>());
        List<AuditPageByTypeDto> result = new ArrayList<>();

        List<Long> templateIds = list.stream().map(TgApplicationInfo::getTemplateId).distinct().collect(Collectors.toList());
        Map<Long, TgTemplateInfo> templateMap;
        if (CollectionUtils.isNotEmpty(templateIds)) {
            List<TgTemplateInfo> tgTemplateInfos = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                    .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateType, TgTemplateInfo::getTemplateName)
                    .in(TgTemplateInfo::getId, templateIds)
            );
//            List<TgTemplateInfo> tgTemplateInfos = templateInfoMapper.selectBatchIds(templateIds);
            templateMap = tgTemplateInfos.stream().collect(Collectors
                    .toMap(TgTemplateInfo::getId, v -> v, (front, current) -> current));
        } else {
            templateMap = Collections.emptyMap();
        }


        List<Long> userIds = list.stream()
                .flatMap(v -> Arrays.stream(v.getCurrentHandlers().split(",")))
                .filter(StringUtils::isNotBlank)
                .map(Long::valueOf)
                .distinct().collect(Collectors.toList());
        List<SysUser> userList = userService.selectUserByIds(userIds);
        Map<Long, SysUser> userMap = userList.stream().collect(Collectors.toMap(SysUser::getUserId, v -> v, (front, current) -> current));

        List<Long> tableIds = list.stream().map(TgApplicationInfo::getBaseTableId).distinct().collect(Collectors.toList());

        Map<Long, String> tableAliasMap = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(tableIds)) {
            List<TableInfo> tableInfos = tableInfoService.getBaseMapper().selectBatchIds(tableIds);
            tableAliasMap = tableInfos.stream().collect(Collectors.toMap(TableInfo::getId, TableInfo::getTableAlias, (front, current) -> current));
        }

        Map<Long, Project> projectMap = Lambda.queryMapIfExist(Lambda.buildNonNullList(list, TgApplicationInfo::getProjectId),
                projectDAO::listByIds, Project::getId);


        // 客户
        final List<Long> projectIds = list.stream()
                .map(TgApplicationInfo::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, Customer> customerMap = new HashMap<>();
        if (CollUtil.isNotEmpty(projectIds)) {
            final List<Project> projects = projectMapper.selectBatchIds(projectIds);
            customerMap.putAll(customerMapper.selectBatchIds(projects.stream()
                            .map(Project::getCustomerId).collect(Collectors.toList()))
                    .stream().collect(Collectors.toMap(Customer::getId, v -> v)));
        }

        // 新资产ID
        List<Long> newAssetsIds = list.stream().map(TgApplicationInfo::getNewAssetId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<TgAssetInfo> newAssets = Lambda.queryListIfExist(newAssetsIds, v -> tgAssetInfoMapper
                .selectList(new QueryWrapper<TgAssetInfo>().lambda()
                        .select(TgAssetInfo::getId, TgAssetInfo::getAssetName, TgAssetInfo::getType).in(TgAssetInfo::getId, v)));
        Map<Long, TgAssetInfo> newAssetsApplyMap = Lambda.buildMap(newAssets, TgAssetInfo::getId, v -> v);

        // newApplicationId对应的申请集合
        List<Long> newApplyIdList = list.stream()
                .map(TgApplicationInfo::getNewApplicationId)
                .filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());
        List<TgApplicationInfo> newApplyList = CollectionUtils.isEmpty(newApplyIdList) ? new ArrayList<>()
                : applicationInfoMapper.selectList(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getCurrentAuditProcessStatus)
                .in(TgApplicationInfo::getId, newApplyIdList));

        Map<Long, Integer> newApplyMap = newApplyList.stream()
                .collect(Collectors.toMap(TgApplicationInfo::getId, TgApplicationInfo::getCurrentAuditProcessStatus, (front, current) -> current));

        for (TgApplicationInfo apply : list) {
            JsonBeanConverter.convert2Obj(apply);
            AuditPageByTypeDto pageDTO = new AuditPageByTypeDto();
//            copier.copy(apply, pageDTO, null);

//            BeanUtil.copyProperties(apply, pageDTO, true);
            pageDTO.setNewApplicationId(apply.getNewApplicationId());
            // 需求id
            pageDTO.setApplicationNo(apply.getApplicationNo());

            final Project project = projectMap.get(apply.getProjectId());
            if (Objects.nonNull(project)) {
                final Customer customer = customerMap.get(project.getCustomerId());
                pageDTO.setCustomerId(customer.getId());
                pageDTO.setCustomerShortName(customer.getShortName());
            }

            pageDTO.setApplicantId(apply.getApplicantId());
            pageDTO.setBaseTableName(apply.getBaseTableName());
            pageDTO.setProjectName(apply.getProjectName());
            pageDTO.setRequireAttr(apply.getRequireAttr());
            pageDTO.setRequireTimeType(apply.getRequireTimeType());
            pageDTO.setDataExpir(apply.getDataExpir());
            pageDTO.setApplicantName(apply.getApplicantName());
            pageDTO.setApplicantDepartment(apply.getApplicantDepartment());
            pageDTO.setCreateTime(apply.getCreateTime());
            pageDTO.setApplyType(apply.getApplyType());
            pageDTO.setCurrentAuditProcessStatus(apply.getCurrentAuditProcessStatus());
            pageDTO.setCurrentIndex(apply.getCurrentIndex());
            pageDTO.setCurrentHandlers(apply.getCurrentHandlers());
            pageDTO.setApplyComment(apply.getApplyComment());
            pageDTO.setDocName(apply.getDocName());
            pageDTO.setCurrentAuditNodeStatus(apply.getCurrentAuditNodeStatus());
            pageDTO.setDocAuthorization(apply.getDocAuthorization());
            pageDTO.setConfigType(apply.getConfigType());
            pageDTO.setWorkflowId(apply.getWorkflowId());
            pageDTO.setApplyReason(apply.getApplyReason());
            pageDTO.setDataState(apply.getDataState());


            Optional<TgTemplateInfo> tempOpt = Optional.ofNullable(templateMap.get(apply.getTemplateId()));
            pageDTO.setTemplateName(tempOpt.map(TgTemplateInfo::getTemplateName).orElse(null));
            pageDTO.setTemplateType(tempOpt.map(TgTemplateInfo::getTemplateType).orElse(null));
            pageDTO.setNewProjectName(Optional.ofNullable(projectMap.get(apply.getProjectId())).map(Project::getName).orElse(""));
            pageDTO.setTableAlias(tableAliasMap.get(apply.getBaseTableId()));
            pageDTO.setType(apply.getConfigType());
            pageDTO.setSql(apply.getConfigSql());
            final TgAssetInfo tgAssetInfo = newAssetsApplyMap.get(apply.getNewAssetId());
            if (Objects.nonNull(tgAssetInfo)) {
                pageDTO.setAssetId(apply.getNewAssetId());
                pageDTO.setAssetName(tgAssetInfo.getAssetName());
                pageDTO.setAssetType(tgAssetInfo.getType());
            } else {
                continue;
            }
            pageDTO.setServiceType(apply.getPermission().stream()
                    .map(item -> item.getTypeName().replace("申请", "")).collect(Collectors.joining("/")));
//            Long latestApply = assetsApplyMap.get(apply.getAssetsId());
            if (apply.getPermission().contains(AssetPermissionType.DATA_EXCHANGE_REQUEST)) {
                // 数据交换申请, 设置数据交换名称
                TgDataSyncApplication tgDataSyncApplication = tgDataSyncApplicationMapper.selectOne(
                        new QueryWrapper<TgDataSyncApplication>().lambda()
                                .select(TgDataSyncApplication::getSyncTaskName)
                                .eq(TgDataSyncApplication::getApplicationId, apply.getId()));
                pageDTO.setSyncTaskName(tgDataSyncApplication != null ? tgDataSyncApplication.getSyncTaskName() : "");
            }

            if (ApplicationConst.AuditStatus.AUDIT_PASS == apply.getCurrentAuditProcessStatus()
                    && (newApplyMap.get(apply.getNewApplicationId()) == null || ApplicationConst.AuditStatus.AUDIT_PASS != newApplyMap.get(apply.getNewApplicationId()))) {
                pageDTO.setNewApplicationId(null);
            } else {
                pageDTO.setNewApplicationId(1L);
            }
            // TODO 多个重新申请先后执行时 new 维护异常
//            pageDTO.setNewApplicationId(Objects.isNull(latestApply) || Objects.equals(latestApply, apply.getId()) ? null : 1L);

            String currentHandlerNames = "";
            if (!"".equals(pageDTO.getCurrentHandlers())) {
                currentHandlerNames = StringUtils.join(Arrays.stream(pageDTO.getCurrentHandlers().split(","))
                        .map((id) -> {
                            SysUser sysUser = userMap.get(Long.valueOf(id));
                            SinoPassUserDTO temp = null;
                            try {
                                temp = SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId());
                            } catch (NullPointerException e) {
                                log.error("主数据人员组织编码为 NULL：{}", sysUser == null ? null : sysUser.getOrgUserId());
                            }

                            return temp != null ? temp.getViewName() : "";
                        }).collect(Collectors.toList()), ",");
            }

            pageDTO.setCurrentHandlerNames(currentHandlerNames);

            if (Objects.equals(searchDto.getSearchStatus(), CommonConstants.UNHANDLE)) {
                // 搜索未处理节点链
                if (apply.getCurrentIndex() < apply.getHandleNode().size()
                        && (null != pageDTO.getCurrentAuditProcessStatus()
                        && pageDTO.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.AUDITING))
                ) {
                    ProcessNodeEasyDto p = apply.getHandleNode().get(apply.getCurrentIndex());
                    if (p.getHandleStatus().equals(CommonConstants.UNHANDLE)) {
                        pageDTO.setCurrentAuditNodeStatus(p.getStatus());
                        pageDTO.setApplicationId(apply.getId());
                        pageDTO.setHandleStatus(p.getHandleStatus());
                        pageDTO.setHandleReason(p.getHandleReason());
                        pageDTO.setCurrentHandlerNames(currentHandlerNames);
                        this.fillLastName(apply, pageDTO);

                        pageDTO.setDocAuthorization(apply.getDocAuthorization());
                        result.add(pageDTO);
                    }
                }
            } else {
                // 文档审核记录报表 用户为空
                if (Objects.isNull(user)) {
                    for (ProcessNodeEasyDto p : apply.getHandleNode()) {
                        if (Objects.equals(p.getHandleStatus(), CommonConstants.UNHANDLE)) {
                            continue;
                        }

                        AuditPageByTypeDto tempPage = new AuditPageByTypeDto();
                        BeanUtils.copyProperties(pageDTO, tempPage);
                        tempPage.setCurrentAuditNodeStatus(p.getStatus());
                        tempPage.setApplicationId(apply.getId());
                        tempPage.setHandleStatus(p.getHandleStatus());
                        tempPage.setHandleReason(p.getHandleReason());
                        tempPage.setCurrentHandlerNames(p.getHandlerName());
                        tempPage.setDocAuthorization(apply.getDocAuthorization());
                        result.add(tempPage);
                    }
                } else {
                    // 搜索已处理节点链
                    Map<String, Integer> handlerIndexMap = apply.getHandlerIndexMapping().get(user.getUserId());
                    appendResult(result, apply, pageDTO, handlerIndexMap);

                    this.fillLastName(apply, pageDTO);
                }
            }
        }

        // 过滤资产名称
        if (StringUtils.isNotBlank(searchDto.getAssetName())) {
            result = result.stream()
                    .filter(n -> StringUtils.isNotBlank(n.getAssetName()) && n.getAssetName().contains(searchDto.getAssetName()))
                    .collect(Collectors.toList());
        }

        // 过滤审核状态
        if (ObjectUtils.isNotNull(searchDto.getSearchProcessStatus())) {
            result = result.stream()
                    .filter(n -> n.getCurrentAuditProcessStatus() == (Integer.parseInt(searchDto.getSearchProcessStatus())))
                    .collect(Collectors.toList());
        }

        // 当前审核人只关心自己未审核的数据
        if (Objects.equals(searchDto.getSearchStatus(), CommonConstants.UNHANDLE)) {
            userOpt.ifPresent(v -> searchDto.setSearchHandlerName(SinoipaasUtils.mainEmployeeSelectbyid(v.getOrgUserId()).getViewName()));
        }
        // 过滤审核人
        if (ObjectUtils.isNotNull(searchDto.getSearchHandlerName())) {
            result = result.stream().filter((n) -> n.getCurrentHandlerNames().contains(searchDto.getSearchHandlerName()))
                    .collect(Collectors.toList());
        }

        List<Long> applyIds = Lambda.buildList(result, AuditPageByTypeDto::getApplicationId);
        Map<Long, AcceptanceRecord> recordMap = acceptanceRecordDAO.queryLatestStateByApplyId(applyIds);
        for (AuditPageByTypeDto dto : result) {
            dto.setState(Optional.ofNullable(recordMap.get(dto.getApplicationId())).map(AcceptanceRecord::getState).orElse(null));
        }

        // 3. 返回当前数据，由前端分页
        return result;
    }

    /**
     * @see AuditProcessServiceImpl#buildActions
     */
    @Override
    public AjaxResult<List<AuditPageByTypeDto>> queryAuditProcessAuditListByType(SysUser user, Map<String, Object> params) {
        Optional<SysUser> userOpt = Optional.ofNullable(user);
        String applicationType = (String) params.get("applicationType");
        if (StringUtils.isBlank(applicationType)) {
            return AjaxResult.error("申请类型不能为空");
        }

        // 1. 查询用户当前需要审核的申请
        AuditApplicationSearchDto searchDto = BeanUtil.mapToBean(params, AuditApplicationSearchDto.class, false, null);
        if (searchDto.getApplicationType().equals(ApplicationConst.ApplicationType.TABLE_APPLICATION)) {
            final List<String> list = Lists.newArrayList(searchDto.getApplicationType(), ApplicationConst.ApplicationType.DATA_SYNC_APPLICATION);
            searchDto.setExtraApplicationType(list);
        }

        // 注意增加返回字段时要在这里改对应的SQL增加查询
        // 复杂查询
        String userIdStr = userOpt.map(SysUser::getUserId).map(Object::toString).orElse(null);
        log.info("search={}", JsonUtils.format(searchDto));
        List<TgApplicationInfo> list = mapper.queryApplyByHandlerAndType(userIdStr, searchDto);

        Set<Long> assetsIds = Lambda.buildSet(list, TgApplicationInfo::getAssetsId);
        Map<Long, UserDataAssets> assetsMap = Lambda.queryMapIfExist(assetsIds, v -> userDataAssetsDAO.lambdaQuery()
                .select(UserDataAssets::getId, UserDataAssets::getFtpStatus, UserDataAssets::getFtpPath)
                .in(UserDataAssets::getId, v)
                .list(), UserDataAssets::getId);
        Set<String> noList = list.stream().map(TgApplicationInfo::getApplicationNo).filter(StringUtils::isNoneBlank).collect(Collectors.toSet());

        Set<String> pauseNoSet = applicationFormDAO.queryPause(noList);

        List<Long> templateIds = list.stream().map(TgApplicationInfo::getTemplateId).distinct().collect(Collectors.toList());
        Map<Long, TgTemplateInfo> templateMap;
        if (CollectionUtils.isNotEmpty(templateIds)) {
            List<TgTemplateInfo> tgTemplateInfos = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                    .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateType, TgTemplateInfo::getTemplateName)
                    .in(TgTemplateInfo::getId, templateIds)
            );
//            List<TgTemplateInfo> tgTemplateInfos = templateInfoMapper.selectBatchIds(templateIds);
            templateMap = tgTemplateInfos.stream().collect(Collectors
                    .toMap(TgTemplateInfo::getId, v -> v, (front, current) -> current));
        } else {
            templateMap = Collections.emptyMap();
        }

        List<Long> userIds = list.stream()
                .flatMap(v -> Arrays.stream(v.getCurrentHandlers().split(",")))
                .filter(StringUtils::isNotBlank)
                .map(Long::valueOf)
                .distinct().collect(Collectors.toList());
        List<SysUser> userList = userService.selectUserByIds(userIds);
        Map<Long, SysUser> userMap = userList.stream().collect(Collectors.toMap(SysUser::getUserId, v -> v, (front, current) -> current));

        List<Long> tableIds = list.stream().map(TgApplicationInfo::getBaseTableId).distinct().collect(Collectors.toList());
        Map<Long, String> tableAliasMap;
        if (CollectionUtils.isNotEmpty(tableIds)) {
            List<TableInfo> tableInfos = tableInfoService.getBaseMapper().selectBatchIds(tableIds);
            tableAliasMap = tableInfos.stream().collect(Collectors.toMap(TableInfo::getId, TableInfo::getTableAlias, (front, current) -> current));
        } else {
            tableAliasMap = Collections.emptyMap();
        }

        Map<Long, Project> projectMap = Lambda.queryMapIfExist(Lambda.buildNonNullList(list, TgApplicationInfo::getProjectId),
                projectDAO::listByIds, Project::getId);


        // 客户
        final List<Long> projectIds = list.stream()
                .map(TgApplicationInfo::getProjectId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, Customer> customerMap = new HashMap<>();
        if (CollUtil.isNotEmpty(projectIds)) {
            final List<Project> projects = projectMapper.selectBatchIds(projectIds);
            customerMap.putAll(customerMapper.selectBatchIds(projects.stream()
                            .map(Project::getCustomerId).collect(Collectors.toList()))
                    .stream().collect(Collectors.toMap(Customer::getId, v -> v)));
        }

        // 新资产ID
        List<Long> newAssetsIds = list.stream().map(TgApplicationInfo::getNewAssetId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<TgAssetInfo> newAssets = Lambda.queryListIfExist(newAssetsIds, v -> tgAssetInfoMapper
                .selectList(new QueryWrapper<TgAssetInfo>().lambda()
                        .select(TgAssetInfo::getId, TgAssetInfo::getAssetName).in(TgAssetInfo::getId, v)));
        Map<Long, String> newAssetsApplyMap = Lambda.buildMap(newAssets, TgAssetInfo::getId, TgAssetInfo::getAssetName);

        // newApplicationId对应的申请集合
        List<Long> newApplyIdList = list.stream()
                .map(TgApplicationInfo::getNewApplicationId)
                .filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());
        List<TgApplicationInfo> newApplyList = CollectionUtils.isEmpty(newApplyIdList) ? new ArrayList<>()
                : applicationInfoMapper.selectList(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getCurrentAuditProcessStatus)
                .in(TgApplicationInfo::getId, newApplyIdList));
        Map<Long, Integer> newApplyMap = newApplyList.stream()
                .collect(Collectors.toMap(TgApplicationInfo::getId, TgApplicationInfo::getCurrentAuditProcessStatus, (front, current) -> current));

        List<AuditPageByTypeDto> result = new ArrayList<>();
        for (TgApplicationInfo apply : list) {
            if (!applicationType.equals(apply.getApplicationType())
                    && !(CollUtil.isNotEmpty(searchDto.getExtraApplicationType()) && searchDto.getExtraApplicationType().contains(apply.getApplicationType()))) {
                continue;
            }
            JsonBeanConverter.convert2Obj(apply);
            AuditPageByTypeDto pageDTO = new AuditPageByTypeDto();
//            copier.copy(apply, pageDTO, null);

//            BeanUtil.copyProperties(apply, pageDTO, true);
            pageDTO.setNewApplicationId(apply.getNewApplicationId());
            pageDTO.setApplicationNo(apply.getApplicationNo());
            pageDTO.setAssetsId(apply.getAssetsId());

            final Project project = projectMap.get(apply.getProjectId());
            if (Objects.nonNull(project)) {
                final Customer customer = customerMap.get(project.getCustomerId());
                pageDTO.setCustomerId(customer.getId());
                pageDTO.setCustomerShortName(customer.getShortName());
            }

            pageDTO.setApplicantId(apply.getApplicantId());
            pageDTO.setAssetsAttach(apply.getAssetsAttach());
            pageDTO.setBaseTableName(apply.getBaseTableName());
            pageDTO.setProjectName(apply.getProjectName());
//            pageDTO.setReadableUsers(apply.getClientNames());
            pageDTO.setRequireAttr(apply.getRequireAttr());
            pageDTO.setRequireTimeType(apply.getRequireTimeType());
            pageDTO.setDataExpir(apply.getDataExpir());
            pageDTO.setApplicantName(apply.getApplicantName());
            pageDTO.setApplicantDepartment(apply.getApplicantDepartment());
            pageDTO.setCreateTime(apply.getCreateTime());
            pageDTO.setApplyType(apply.getApplyType());
            pageDTO.setCurrentAuditProcessStatus(apply.getCurrentAuditProcessStatus());
            pageDTO.setCurrentIndex(apply.getCurrentIndex());
            pageDTO.setCurrentHandlers(apply.getCurrentHandlers());
            pageDTO.setApplyComment(apply.getApplyComment());
            pageDTO.setDocName(apply.getDocName());
            pageDTO.setCurrentAuditNodeStatus(apply.getCurrentAuditNodeStatus());
            pageDTO.setDocAuthorization(apply.getDocAuthorization());
            pageDTO.setConfigType(apply.getConfigType());
            pageDTO.setWorkflowId(apply.getWorkflowId());
            pageDTO.setApplyReason(apply.getApplyReason());
            pageDTO.setDataState(apply.getDataState());
            pageDTO.setRelateDict(apply.getRelateDict());
            pageDTO.setDataAmount(apply.getDataAmount());
            pageDTO.setDataCost(apply.getDataCost());


            // 注意这里权衡性能(原始字段字符串很大)和改动量 做了hardcode 固定顺序取明确值。
            String productGra = apply.getPushProjectName();
            if (StringUtils.isNotBlank(productGra)) {
//                Optional.ofNullable(JsonUtils.parseArray(apply.getGranularityJson(),
//                                ApplicationGranularityDto.class)).orElse(Collections.emptyList())
//                        .stream().filter(v -> Objects.equals(FieldGranularityEnum.product.name(), v.getGranularity()))
//                        .map(v -> {
//                            if (CollectionUtils.isEmpty(v.getSelectGranularity())) {
//                                return "";
//                            }
//                            return String.join(",", v.getSelectGranularity());
//                        }).findAny().ifPresent(pageDTO::setProductGra);
                productGra = productGra.replace("\"", "");
                productGra = productGra.replace("[", "");
                productGra = productGra.replace("]", "");

                pageDTO.setProductGra(productGra);
            }


            Optional<TgTemplateInfo> tempOpt = Optional.ofNullable(templateMap.get(apply.getTemplateId()));
            pageDTO.setTemplateName(tempOpt.map(TgTemplateInfo::getTemplateName).orElse(null));
            pageDTO.setTemplateType(tempOpt.map(TgTemplateInfo::getTemplateType).orElse(null));
            pageDTO.setNewProjectName(Optional.ofNullable(projectMap.get(apply.getProjectId())).map(Project::getName).orElse(""));
            pageDTO.setTableAlias(tableAliasMap.get(apply.getBaseTableId()));
            pageDTO.setType(apply.getConfigType());
            pageDTO.setSql(apply.getConfigSql());
            pageDTO.setAssetName(newAssetsApplyMap.get(apply.getNewAssetId()));
            pageDTO.setAssetId(apply.getNewAssetId());
            pageDTO.setServiceType(apply.getPermission().stream()
                    .map(item -> item.getTypeName().replace("申请", "")).collect(Collectors.joining("/")));
//            Long latestApply = assetsApplyMap.get(apply.getAssetsId());
            if (apply.getPermission().contains(AssetPermissionType.DATA_EXCHANGE_REQUEST)) {
                // 数据交换申请, 设置数据交换名称
                TgDataSyncApplication tgDataSyncApplication = tgDataSyncApplicationMapper.selectOne(
                        new QueryWrapper<TgDataSyncApplication>().lambda()
                                .select(TgDataSyncApplication::getSyncTaskName)
                                .eq(TgDataSyncApplication::getApplicationId, apply.getId()));
                pageDTO.setSyncTaskName(tgDataSyncApplication != null ? tgDataSyncApplication.getSyncTaskName() : "");
            }

            if (ApplicationConst.AuditStatus.AUDIT_PASS == apply.getCurrentAuditProcessStatus()
                    && (newApplyMap.get(apply.getNewApplicationId()) == null || ApplicationConst.AuditStatus.AUDIT_PASS != newApplyMap.get(apply.getNewApplicationId()))) {
                pageDTO.setNewApplicationId(null);
            } else {
                pageDTO.setNewApplicationId(1L);
            }
            // TODO 多个重新申请先后执行时 new 维护异常
//            pageDTO.setNewApplicationId(Objects.isNull(latestApply) || Objects.equals(latestApply, apply.getId()) ? null : 1L);

            String currentHandlerNames = "";
            if (!"".equals(pageDTO.getCurrentHandlers())) {
                currentHandlerNames = StringUtils.join(Arrays.stream(pageDTO.getCurrentHandlers().split(","))
                        .map((id) -> {
                            SysUser sysUser = userMap.get(Long.valueOf(id));
                            SinoPassUserDTO temp = null;
                            try {
                                temp = SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId());
                            } catch (NullPointerException e) {
                                log.error("主数据人员组织编码为 NULL：{}", sysUser == null ? null : sysUser.getOrgUserId());
                            }

                            return temp != null ? temp.getViewName() : "";
                        }).collect(Collectors.toList()), ",");
            }

            pageDTO.setCurrentHandlerNames(currentHandlerNames);

            if (Objects.equals(searchDto.getSearchStatus(), CommonConstants.UNHANDLE)) {
                // 搜索未处理节点链
                if (apply.getCurrentIndex() < apply.getHandleNode().size()
                        && (null != pageDTO.getCurrentAuditProcessStatus()
                        && pageDTO.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.AUDITING))
                ) {
                    ProcessNodeEasyDto p = apply.getHandleNode().get(apply.getCurrentIndex());
                    if (p.getHandleStatus().equals(CommonConstants.UNHANDLE)) {
                        pageDTO.setCurrentAuditNodeStatus(p.getStatus());
                        pageDTO.setApplicationId(apply.getId());
                        pageDTO.setHandleStatus(p.getHandleStatus());
                        pageDTO.setHandleReason(p.getHandleReason());
                        pageDTO.setCurrentHandlerNames(currentHandlerNames);
                        this.fillLastName(apply, pageDTO);

                        pageDTO.setDocAuthorization(apply.getDocAuthorization());
                        result.add(pageDTO);
                    }
                }
            } else {
                // 文档审核记录报表 用户为空
                if (Objects.isNull(user)) {
                    for (ProcessNodeEasyDto p : apply.getHandleNode()) {
                        if (Objects.equals(p.getHandleStatus(), CommonConstants.UNHANDLE)) {
                            continue;
                        }

                        AuditPageByTypeDto tempPage = new AuditPageByTypeDto();
                        BeanUtils.copyProperties(pageDTO, tempPage);
                        tempPage.setCurrentAuditNodeStatus(p.getStatus());
                        tempPage.setApplicationId(apply.getId());
                        tempPage.setHandleStatus(p.getHandleStatus());
                        tempPage.setHandleReason(p.getHandleReason());
                        tempPage.setCurrentHandlerNames(p.getHandlerName());
                        tempPage.setDocAuthorization(apply.getDocAuthorization());
                        result.add(tempPage);
                    }
                } else {
                    // 搜索已处理节点链
                    Map<String, Integer> handlerIndexMap = apply.getHandlerIndexMapping().get(user.getUserId());
                    appendResult(result, apply, pageDTO, handlerIndexMap);

                    this.fillLastName(apply, pageDTO);
                }
            }
        }

        // 过滤资产名称
        if (StringUtils.isNotBlank(searchDto.getAssetName())) {
            String name = searchDto.getAssetName().trim();
//            result = result.stream()
//                    .filter(n -> {
//                        boolean assets = StringUtils.isNotBlank(n.getAssetName()) && n.getAssetName().contains(name);
//                        boolean project = StringUtils.isNotBlank(n.getProjectName()) && n.getProjectName().contains(name);
//                        return assets || project;
//                    })
//                    .collect(Collectors.toList());

            result.removeIf(n -> {
                boolean assets = StringUtils.isNotBlank(n.getAssetName()) && n.getAssetName().contains(name);
                boolean project = StringUtils.isNotBlank(n.getProjectName()) && n.getProjectName().contains(name);
                return !assets && !project;
            });
        }

        for (AuditPageByTypeDto dto : result) {
            dto.setActionList(buildActions(dto, assetsMap, pauseNoSet));
        }

        // 过滤审核状态
//        if (ObjectUtils.isNotNull(searchDto.getSearchProcessStatus())) {
//            result = result.stream().filter(n -> n.getCurrentAuditProcessStatus() == (Integer.parseInt(searchDto.getSearchProcessStatus())))
//                    .collect(Collectors.toList());
//        }

        // 当前审核人只关心自己未审核的数据
        if (Objects.equals(searchDto.getSearchStatus(), CommonConstants.UNHANDLE)) {
            searchDto.setSearchHandlerName(SinoipaasUtils.mainEmployeeSelectbyid(ThreadContextHolder.getSysUser().getOrgUserId()).getViewName());
        }
        // 过滤审核人
        if (ObjectUtils.isNotNull(searchDto.getSearchHandlerName())) {
//            result = result.stream().filter((n) -> n.getCurrentHandlerNames().contains(searchDto.getSearchHandlerName()))
//                    .collect(Collectors.toList());
            result.removeIf(v -> !v.getCurrentHandlerNames().contains(searchDto.getSearchHandlerName()));
        }

        if (Objects.equals(searchDto.getSearchStatus(), CommonConstants.HANDLED)
                && Objects.equals(searchDto.getApplicationType(), ApplicationConst.ApplicationType.DATA_APPLICATION)) {
            List<Long> applyIds = Lambda.buildList(result, AuditPageByTypeDto::getApplicationId);
            Map<Long, AcceptanceRecord> recordMap = acceptanceRecordDAO.queryLatestStateByApplyId(applyIds);
            for (AuditPageByTypeDto dto : result) {
                dto.setState(Optional.ofNullable(recordMap.get(dto.getApplicationId())).map(AcceptanceRecord::getState).orElse(null));
            }
        }

        // 3. 返回当前数据，由前端分页
        return AjaxResult.success(result);
    }

    /**
     * 审核页的按钮
     */
    private static List<Integer> buildActions(AuditPageByTypeDto dto,
                                              Map<Long, UserDataAssets> assetsMap,
                                              Set<String> pauseNoSet) {
        Long aid = dto.getAssetsId();
        List<Integer> act = new ArrayList<>();
        act.add(ApplicationConst.AuditAction.DETAIL);

        boolean taskType = TemplateTypeEnum.isSchedulerTaskType(dto.getTemplateType());
        boolean customizedType = TemplateTypeEnum.isCustomizedType(dto.getTemplateType());

        // 暂停需求不可执行
        if (!pauseNoSet.contains(dto.getApplicationNo()) && taskType) {
            act.add(ApplicationConst.AuditAction.RUN);
        }
        if (customizedType) {
            act.add(ApplicationConst.AuditAction.CONFIG);
        }

        Integer auditProcess = dto.getCurrentAuditProcessStatus();
        if (Objects.equals(auditProcess, ApplicationConst.AuditStatus.AUDIT_PASS)) {
            if (Objects.equals(dto.getDataState(), ApplyDataStateEnum.success.name())) {
                if (!Objects.equals(dto.getConfigType(), ApplicationConfigTypeConstant.FILE_TYPE)) {
                    act.add(ApplicationConst.AuditAction.PREVIEW);
                }

                Optional.ofNullable(assetsMap.get(aid))
                        .map(UserDataAssets::getFtpPath).filter(StringUtils::isNoneBlank)
                        .ifPresent(v -> act.add(ApplicationConst.AuditAction.DOWNLOAD));
            }
            act.add(ApplicationConst.AuditAction.ACCEPTANCE_RECORD);
            act.add(ApplicationConst.AuditAction.DEPRECATED);
        }
        return act;
    }

    private void viewMessage(Long applicationId) {
        SysUser sysUser = ThreadContextHolder.getSysUser();
        messageMapper.updateMessageCountByApplicationIdAndAdviceWho(sysUser.getUserId(), applicationId);
    }
}
