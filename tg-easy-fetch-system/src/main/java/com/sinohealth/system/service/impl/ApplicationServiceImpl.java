package com.sinohealth.system.service.impl;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sinohealth.bi.data.ClickHouse;
import com.sinohealth.bi.data.Filter;
import com.sinohealth.bi.data.Table;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.config.TransferProperties;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysCustomer;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.common.enums.DbType;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsExpireEnum;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.enums.dataassets.LatestVersionEnum;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.enums.dict.FieldUseWayEnum;
import com.sinohealth.common.enums.monitor.SecondSubjectTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.*;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.common.utils.sign.Md5Utils;
import com.sinohealth.data.intelligence.api.Result;
import com.sinohealth.data.intelligence.api.metadata.dto.MetaTableDTO;
import com.sinohealth.data.intelligence.datasource.param.BaseDataSourceParam;
import com.sinohealth.data.intelligence.datasource.util.DataSourceUtils;
import com.sinohealth.data.intelligence.datasource.util.PasswordUtils;
import com.sinohealth.framework.config.BizScheduledThreadPoolExecutor;
import com.sinohealth.quartz.util.CronUtils;
import com.sinohealth.system.biz.application.bo.FieldMetaBO;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.dao.ApplicationFormDAO;
import com.sinohealth.system.biz.application.dao.ApplicationTaskConfigDAO;
import com.sinohealth.system.biz.application.domain.ApplicationForm;
import com.sinohealth.system.biz.application.domain.ApplicationTaskConfig;
import com.sinohealth.system.biz.application.dto.*;
import com.sinohealth.system.biz.application.dto.request.*;
import com.sinohealth.system.biz.application.entity.HistoryApplyQuoteEntity;
import com.sinohealth.system.biz.application.service.ApplicationTaskConfigService;
import com.sinohealth.system.biz.application.util.ApplyUtil;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.application.util.WideTableSqlBuilder;
import com.sinohealth.system.biz.audit.dto.AuditRequest;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowBatchDetailDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsWideUpgradeTriggerDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatchDetail;
import com.sinohealth.system.biz.dataassets.domain.AssetsWideUpgradeTrigger;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;
import com.sinohealth.system.biz.dataassets.dto.FlowAssetsPageDTO;
import com.sinohealth.system.biz.dataassets.dto.compare.CompareResultVO;
import com.sinohealth.system.biz.dataassets.dto.request.DataPreviewRequest;
import com.sinohealth.system.biz.dataassets.dto.request.FlowAssetsAutoPageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.FlowAssetsPageRequest;
import com.sinohealth.system.biz.dataassets.service.AssetsAdapter;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.biz.dict.dao.FieldDictDAO;
import com.sinohealth.system.biz.dict.dao.MetricsDictDAO;
import com.sinohealth.system.biz.dict.dao.ProjectCustomFieldDictDAO;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.biz.dict.domain.MetricsDict;
import com.sinohealth.system.biz.dict.util.BizTypeUtil;
import com.sinohealth.system.biz.project.dao.ProjectDAO;
import com.sinohealth.system.biz.project.dao.ProjectHelperDAO;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.biz.project.domain.ProjectDataAssetsRelate;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.mapper.ProjectMapper;
import com.sinohealth.system.biz.project.service.DataPlanService;
import com.sinohealth.system.biz.project.service.ProjectService;
import com.sinohealth.system.biz.scheduler.dto.request.DataSyncTaskFieldConfig;
import com.sinohealth.system.biz.scheduler.service.impl.IntegrateSyncTaskServiceImpl;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.biz.transfer.dto.TemplateCtx;
import com.sinohealth.system.biz.ws.service.WsMsgService;
import com.sinohealth.system.client.DataSourceApiClient;
import com.sinohealth.system.client.MetadataClient;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.config.TaskConfigProperties;
import com.sinohealth.system.config.TaskCreateConfig;
import com.sinohealth.system.dao.ApplicationColumnSettingDAO;
import com.sinohealth.system.dao.ApplicationDataUpdateRecordDAO;
import com.sinohealth.system.dao.CustomFieldInfoDAO;
import com.sinohealth.system.dao.CustomerAuthDAO;
import com.sinohealth.system.dao.TgDataSyncFieldConfigDAO;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.domain.ckpg.CkPgJavaDataType;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.AsyncTaskConst;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.domain.constant.RequireAttrType;
import com.sinohealth.system.domain.constant.UpdateRecordStateType;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.domain.customer.Customer;
import com.sinohealth.system.domain.vo.TableInfoSearchVO;
import com.sinohealth.system.dto.ApplicationDataDto;
import com.sinohealth.system.dto.BaseDataSourceParamDto;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.application.*;
import com.sinohealth.system.dto.application.deliver.request.HistoryQueryRequest;
import com.sinohealth.system.dto.assets.AuthTableFieldDTO;
import com.sinohealth.system.dto.auditprocess.ProcessNodeDetailDto;
import com.sinohealth.system.dto.auditprocess.ProcessNodeEasyDto;
import com.sinohealth.system.dto.table_manage.DataRangePageDto;
import com.sinohealth.system.dto.table_manage.DataRangeQueryDto;
import com.sinohealth.system.dto.table_manage.MetaDataFieldInfo;
import com.sinohealth.system.dto.table_manage.TableInfoManageDto;
import com.sinohealth.system.event.EventPublisher;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.*;
import com.sinohealth.system.monitor.event.EventReporterUtil;
import com.sinohealth.system.service.*;
import com.sinohealth.system.util.ApplicationSqlUtil;
import com.sinohealth.system.util.HistoryApplyUtil;
import com.sinohealth.system.util.QuerySqlUtil;
import com.sinohealth.system.vo.ApplicationManageFileListVo;
import com.sinohealth.system.vo.ApplicationManageModelListVo;
import com.sinohealth.system.vo.ApplicationManageTableListVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sinohealth.common.core.redis.RedisKeys.Apply.TRANS_APPLY_MAP;

/**
 * @Author Rudolph
 * @Date 2022-05-13 14:10
 * @Desc 主要负责提数信息解析并生成 SQL
 */
@Service
@Slf4j
public class ApplicationServiceImpl implements IApplicationService {

    // 配置
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private TaskConfigProperties taskConfigProperties;
    @Autowired
    private TransferProperties transferProperties;

    // 基础组件
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ApplicationServiceAspect aspect;
    @Autowired
    private EventPublisher eventPublisher;
    @Autowired
    private WsMsgService msgService;

    // DAO
    @Autowired
    TgApplicationInfoMapper mapper;
    @Autowired
    UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private ProjectHelperDAO projectHelperDAO;
    @Autowired
    private ApplicationTaskConfigDAO applicationTaskConfigDAO;
    @Autowired
    FieldDictDAO fieldDictDAO;
    @Autowired
    private MetricsDictDAO metricsDictDAO;
    @Autowired
    private ProjectCustomFieldDictDAO projectCustomFieldDictDAO;
    @Autowired
    private CustomFieldInfoMapper customFieldInfoMapper;
    @Autowired
    private CustomFieldInfoDAO customFieldInfoDAO;
    @Autowired
    TgCkProviderMapper ckProviderMapper;
    @Autowired
    TgCkCustomerProviderMapper customerProviderMapper;
    @Autowired
    TgNodeMappingMapper tgApplicationDirMappingMapper;
    @Autowired
    private TgApplicationInfoMapper applicationInfoMapper;
    @Autowired
    private ApplicationDAO applicationDAO;
    @Autowired
    private CustomerAuthDAO customerAuthDAO;
    @Autowired
    private ApplicationDataUpdateRecordDAO dataUpdateRecordDAO;
    @Autowired
    private UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;
    @Autowired
    private AssetsWideUpgradeTriggerDAO assetsWideUpgradeTriggerDAO;
    @Autowired
    private AssetsFlowBatchDetailDAO assetsFlowBatchDetailDAO;
    @Autowired
    private TableFieldInfoMapper tableFieldInfoMapper;
    @Autowired
    private TgTemplatePackTailSettingMapper templatePackTailSettingMapper;
    @Autowired
    private ApplicationColumnSettingDAO applicationColumnSettingDAO;
    @Autowired
    private ProjectDataAssetsRelateMapper projectDataAssetsRelateMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private TgTemplateInfoMapper templateInfoMapper;
    @Autowired
    private TemplateInfoDAO templateInfoDAO;
    @Autowired
    private TgDataSyncApplicationMapper tgDataSyncApplicationMapper;
    @Autowired
    private TgDataSyncFieldConfigDAO tgDataSyncFieldConfigDAO;
    @Autowired
    private TgMetadataInfoMapper metadataInfoMapper;
    @Autowired
    private ProjectDataAssetsRelateMapper projectAssetsMapper;
    @Autowired
    private ProjectDAO projectDAO;
    @Autowired
    private ApplicationFormDAO applicationFormDAO;

    @Autowired
    private AssetsAdapter assetsAdapter;

    // 服务
    @Autowired
    IAuditProcessService auditProcessService;
    @Autowired
    ITableFieldInfoService tableFieldInfoService;
    @Autowired
    ITableInfoService tableInfoService;
    @Autowired
    ISysUserService userService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private ISysCustomerService sysCustomerService;
    @Autowired
    private DataRangeTemplateService dataRangeTemplateService;
    @Autowired
    private IDocService docService;
    @Autowired
    private ApplicationTaskConfigService applicationTaskConfigService;
    @Autowired
    private IntergrateAutoProcessDefService intergrateAutoProcessDefService;
    @Autowired
    private IntegrateDataSourceService integrateDataSourceService;
    @Autowired
    private ApplicationColumnSettingService applicationColumnSettingService;
    @Autowired
    private IAssetService assetService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private MetadataClient metadataClient;
    @Autowired
    private DataSourceApiClient dataSourceApiClient;
    @Autowired
    private CKClusterAdapter ckClusterAdapter;

    @Autowired
    private UserDataAssetsService userDataAssetsService;
    @Autowired
    private DataPlanService dataPlanService;

    @Autowired
    private ITemplatePackTailSettingService templatePackTailSettingService;


    /**
     * 字段属性 缓存
     * key: 真实字段id
     */
    private Map<Long, MetaDataFieldInfo> fieldMap = new ConcurrentHashMap<>();
    private static final TimedCache<String, Object> userCache = CacheUtil.newTimedCache(Duration.ofDays(2).toMillis());

    private static final TimedCache<Long, Map<String, Long>> fieldsCache = CacheUtil
            .newTimedCache(Duration.ofMinutes(30).toMillis());
    /**
     * 字段库缓存
     */
    private static final TimedCache<String, Map<String, Long>> fieldDictCache = CacheUtil
            .newTimedCache(Duration.ofMinutes(30).toMillis());


    private static final TimedCache<String, TemplateCtx> templateCache = CacheUtil
            .newTimedCache(Duration.ofMinutes(30).toMillis());
    private static final TimedCache<Long, TgAssetInfo> assetsCache = CacheUtil
            .newTimedCache(Duration.ofMinutes(30).toMillis());
    @Autowired
    private ProjectHelperMapper projectHelperMapper;

    private void normalizeApplicationInfo(TgApplicationInfo info) {
        info.setCreateTime(DateUtils.getTime());
        info.setUpdateTime(DateUtils.getTime());

        // 复制出新需求时，清空旧需求历史值
        info.setCopyFromId(null);
        info.setDataTotal(null);

        info.setConfigSqlWorkflowId(null);
        info.setConfigSql(null);
        // 数据迁移时不清除，正常逻辑下要清除
        if (Objects.isNull(MDC.get(CommonConstants.TRANSFER))) {
            info.setWorkflowId(null);
            info.setConfigType(null);
            info.setAssetsAttachJson(null);
        }
        info.setEvaluationResult("");
        info.setExpectDeliveryTime(null);
        info.setAssetsCreateTime(null);

        info.setNewApplicationId(null);
        info.setFlowInstanceId(null);
        info.setTailSql(null);
        info.setAsql(null);
        if (StringUtils.isBlank(info.getRemarkFiles())) {
            info.setRemarkFiles(null);
        }
        info.setDataState(ApplyDataStateEnum.none.name());
        info.setDataCost(null);
        info.setDataCostMin(null);
        info.setDataAmount(null);
        info.setTimeGra(null);
        info.setProductGra(null);


        if (ApplicationConst.ApplicationType.DOC_APPLICATION.equals(info.getApplicationType())) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(CommonConstants.ID, info.getDocId());
            AjaxResult<?> doc = docService.query(map);
            if (doc.isSuccess()) {
                TgDocInfo docInfo = (TgDocInfo) doc.getData();
                info.setDocName(docInfo.getName());
            }
        } else if (ApplicationConst.ApplicationType.DATA_APPLICATION.equals(info.getApplicationType())) {
            if (StringUtils.isBlank(info.getProjectName())) {
                throw new CustomException("项目名未填写");
            }
            info.setProjectName(info.getProjectName().trim());
            if (Objects.isNull(info.getAssetsId()) && Objects.nonNull(info.getId())) {
                Optional<TgApplicationInfo> oldOpt = applicationDAO.lambdaQuery()
                        .select(TgApplicationInfo::getAssetsId)
                        .eq(TgApplicationInfo::getId, info.getId())
                        .oneOpt();
                oldOpt.map(TgApplicationInfo::getAssetsId).ifPresent(info::setAssetsId);
            }

            if (Objects.equals(info.getRequireTimeType(), ApplicationConst.RequireTimeType.ONCE)) {
                info.setDeliverTimeType("");
            }

            info.setJoinInfo(Collections.emptyList());
            info.setJoinJson("[]");

            // 移除被作为指标的原始维度字段, 以及被选择时间聚合指标的字段
            List<MetricsInfoDto> metricsInfo = info.getMetricsInfo();
            Set<Long> idSet = new HashSet<>();
            if (CollectionUtils.isNotEmpty(metricsInfo)) {
                idSet.addAll(metricsInfo.stream().map(MetricsInfoDto::getColName).collect(Collectors.toSet()));
            }
            String periodField = info.getPeriodField();
            if (StringUtils.isNoneBlank(periodField)) {
                idSet.add(Long.parseLong(periodField));
            }

            // 默认给字段别名赋值
            List<ColsInfoDto> colsInfo = info.getColsInfo();
            Optional.ofNullable(colsInfo).ifPresent(v -> v.forEach(cols -> {
                cols.getSelect().removeAll(idSet);
                List<RealName> realNames = cols.getRealName();
                Optional.ofNullable(realNames).ifPresent(r -> r.forEach(realName -> {
                    if (StringUtils.isBlank(realName.getRealName())) {
                        realName.setRealName(StringUtils.isNotBlank(realName.getFieldAlias())
                                ? realName.getFieldAlias() : realName.getFieldName());
                    }
                }));
            }));
            if (CollectionUtils.isNotEmpty(info.getGranularity())) {
                for (ApplicationGranularityDto dto : info.getGranularity()) {
                    this.trim(dto.getFilter());
                }
            }
        } else if (ApplicationConst.ApplicationType.TABLE_APPLICATION.equals(info.getApplicationType())) {

        }
    }


    private void trim(FilterDTO filter) {
        if (Objects.isNull(filter)) {
            return;
        }
        if (CollectionUtils.isNotEmpty(filter.getFilters())) {
            for (FilterDTO filterFilter : filter.getFilters()) {
                trim(filterFilter);
            }
        }
        FilterDTO.FilterItemDTO item = filter.getFilterItem();
        if (Objects.isNull(item)) {
            return;
        }

        List<FilterDTO> filters = item.getFilters();
        if (CollectionUtils.isNotEmpty(filters)) {
            for (FilterDTO filterFilter : filters) {
                trim(filterFilter);
            }
        }

        Object val = item.getValue();
        if (Objects.isNull(val) || !(val instanceof String)) {
            return;
        }
        if (StringUtils.isBlank(((String) val))) {
            return;
        }
        String trim = ((String) val).trim();
        item.setValue(trim);
    }

    @Override
    public AjaxResult<TgApplicationInfo> addDocApplication(DocApplyRequest docApplyRequest) {
        if (Objects.nonNull(docApplyRequest.getApplicationId())) {
            final TgApplicationInfo tgApplicationInfo = applicationInfoMapper.selectById(docApplyRequest.getApplicationId());
            if (tgApplicationInfo.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.INVALID_APPLICATION)) {
                return AjaxResult.error("已作废的申请不能发起重新申请");
            }
        }
        if (Objects.isNull(docApplyRequest.getDocId())) {
            return AjaxResult.error("请选择文档");
        }
        TgApplicationInfo applicationInfo = new TgApplicationInfo();
        // 资产id
        applicationInfo.setNewAssetId(docApplyRequest.getAssetId());
        // 添加资产权限信息
        applicationInfo.setPermission(docApplyRequest.getPermission());
        applicationInfo.setApplicantId(docApplyRequest.getApplicantId());
        Long processId = docApplyRequest.getProcessId();
        if (Objects.isNull(processId) || Objects.isNull(TgAuditProcessInfo.newInstance().selectById(processId))) {
            return AjaxResult.error(InfoConstants.INVALID_PROCESS);
        }
        applicationInfo.setDataExpir(DateUtils.getEndOfDay(docApplyRequest.getExpireDate()));
        applicationInfo.setProcessId(docApplyRequest.getProcessId());
        applicationInfo.setApplicantDepartment(docApplyRequest.getApplicantDepartment());
        applicationInfo.setApplyReason(docApplyRequest.getApplyReason());
        applicationInfo.setDocAuthorization(docApplyRequest.getDocAuthorization());
        applicationInfo.setApplicationType(ApplicationConst.ApplicationType.DOC_APPLICATION);
        applicationInfo.setDocId(docApplyRequest.getDocId());

        this.normalizeApplicationInfo(applicationInfo);

        // 序列化转换
        TgApplicationInfo tgApplicationInfo = this.convert2Obj(applicationInfo);
        JsonBeanConverter.convert2Json(tgApplicationInfo);

        // 设置组织架构用户信息
        this.setOrgUserInfo(applicationInfo);

        // 申请信息初始化
        this.setInitInfo(tgApplicationInfo);

        TgDocInfo data = getTgDocInfo(applicationInfo);
        // 需不需要审核, 白名单里的权限是否直接够用？
        if (data.getNeed2Audit() && !hasWhitelistAuthorization(applicationInfo, data)) {
            // 埋点: 文档申请数  表: tg_doc_info
            eventPublisher.registerDocEvent(data.getId(), CommonConstants.APPLY_TIMES, InfoConstants.APPLY_DOC);

            // 设置流程处理信息
            this.buildHandleChain(tgApplicationInfo);
        }

        return insertOrUpdateApplication(applicationInfo, tgApplicationInfo);
    }

    @Override
    public AjaxResult<TgApplicationInfo> addTableApplication(TableApplyRequest tableApplyRequest) {
        TgApplicationInfo applicationInfo = new TgApplicationInfo();
        applicationInfo.setApplicantId(tableApplyRequest.getApplicantId());
        // 查询出关联的流程id
        TableInfo info = tableInfoService.getBaseMapper().selectById(tableApplyRequest.getTableId());
        if (Objects.isNull(info.getProcessId())) {
            return AjaxResult.error("请绑定审批流");
        }
        applicationInfo.setProcessId(info.getProcessId());
        applicationInfo.setDataExpir(DateUtils.getEndOfDay(tableApplyRequest.getExpireDate()));
        applicationInfo.setApplicantDepartment(tableApplyRequest.getApplicantDepartment());
        applicationInfo.setApplyReason(tableApplyRequest.getApplyReason());
        applicationInfo.setBaseTableId(tableApplyRequest.getTableId());
        applicationInfo.setApplicationType(ApplicationConst.ApplicationType.TABLE_APPLICATION);
        applicationInfo.setBaseTableName(info.getTableName());

        this.normalizeApplicationInfo(applicationInfo);

        // 序列化转换
        TgApplicationInfo tgApplicationInfo = this.convert2Obj(applicationInfo);
        JsonBeanConverter.convert2Json(tgApplicationInfo);

        // 设置组织架构用户信息
        this.setOrgUserInfo(applicationInfo);

        // 申请信息初始化
        this.setInitInfo(tgApplicationInfo);

        // 设置流程处理信息
        this.buildHandleChain(tgApplicationInfo);

        return insertOrUpdateApplication(applicationInfo, tgApplicationInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult editColumnSetting(ApplicationColumnSettingRequest request) {
        final TgApplicationInfo info = applicationInfoMapper.selectById(request.getApplicationId());
        info.setExportProjectName(request.getExportProjectName());
        applicationInfoMapper.updateById(info);
        applicationColumnSettingService.saveColumnSetting(request.getColumnSettings(), request.getApplicationId());
        return AjaxResult.success();
    }

    @Override
    public AjaxResult<TgApplicationInfo> createTableApplication(TableApplyRequest tableApplyRequest) {
        TgApplicationInfo applicationInfo = new TgApplicationInfo();

        // 资产id
        if (tableApplyRequest.getAssetId() != null) {
            applicationInfo.setNewAssetId(tableApplyRequest.getAssetId());
        }
        // 添加资产权限信息
        if (tableApplyRequest.getPermission() != null && !tableApplyRequest.getPermission().isEmpty()) {
            applicationInfo.setPermission(tableApplyRequest.getPermission());
        }

        applicationInfo.setApplicantId(tableApplyRequest.getApplicantId());
        // 查询出关联的流程id
        TableInfo info = tableInfoService.getBaseMapper().selectById(tableApplyRequest.getTableId());
//        if (Objects.isNull(info.getProcessId())) {
//            return AjaxResult.error("请绑定审批流");
//        }
//        applicationInfo.setProcessId(info.getProcessId());
        Long processId = tableApplyRequest.getProcessId();
        if (Objects.isNull(processId) || Objects.isNull(TgAuditProcessInfo.newInstance().selectById(processId))) {
            return AjaxResult.error(InfoConstants.INVALID_PROCESS);
        }
        applicationInfo.setProcessId(tableApplyRequest.getProcessId());
        applicationInfo.setDataExpir(DateUtils.getEndOfDay(tableApplyRequest.getExpireDate()));
        applicationInfo.setApplicantDepartment(tableApplyRequest.getApplicantDepartment());
        applicationInfo.setApplyReason(tableApplyRequest.getApplyReason());
        applicationInfo.setBaseTableId(tableApplyRequest.getTableId());
        applicationInfo.setApplicationType(ApplicationConst.ApplicationType.TABLE_APPLICATION);
        applicationInfo.setBaseTableName(info.getTableName());

        this.normalizeApplicationInfo(applicationInfo);

        // 序列化转换
        TgApplicationInfo tgApplicationInfo = this.convert2Obj(applicationInfo);
        JsonBeanConverter.convert2Json(tgApplicationInfo);

        // 设置组织架构用户信息
        this.setOrgUserInfo(applicationInfo);

        // 申请信息初始化
        this.setInitInfo(tgApplicationInfo);

        // 设置流程处理信息
        this.buildHandleChain(tgApplicationInfo);

        // 如果是资产负责人或者是白名单用户且申请的权限匹配则无需审核, 表单数据申请直接通过
//        List<AssetPermissionType> applyPermissions = tgApplicationInfo.getPermission().stream().map(p -> {
//            if (p.equals(AssetPermissionType.DATA_EXCHANGE_REQUEST)) {
//                return AssetPermissionType.DATA_EXCHANGE;
//            }
//            if (p.equals(AssetPermissionType.DATA_QUERY_REQUEST)) {
//                return AssetPermissionType.DATA_QUERY;
//            }
//            return p;
//        }).collect(Collectors.toList());
//        autoAudit(tgApplicationInfo, applyPermissions);

        return insertOrUpdateApplication(applicationInfo, tgApplicationInfo);
    }

    /**
     * 新增 修改 同步任务
     *
     * @see IntegrateSyncTaskServiceImpl#executeWorkFlow(Long) 审核入口
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult addSyncApplication(SyncApplicationSaveRequest param) {
        if (Objects.nonNull(param.getId())) {
            final TgApplicationInfo info = applicationInfoMapper.selectById(param.getId());
            if (info.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.INVALID_APPLICATION)) {
                return AjaxResult.error("已作废的申请不能发起重新申请");
            }
        }

        if (StringUtils.isNotBlank(param.getSyncTaskCron()) && !CronUtils.isValidTask(param.getSyncTaskCron())) {
            return AjaxResult.error("数据交换定时任务不支持设置秒，请重新设置");
        }

        final List<TgDataSyncApplication> applications = tgDataSyncApplicationMapper.selectList(new QueryWrapper<TgDataSyncApplication>().lambda()
                //.select(TgDataSyncApplication::getSyncTaskName)
                .ne(Objects.nonNull(param.getId()), TgDataSyncApplication::getApplicationId, param.getId())
                .eq(TgDataSyncApplication::getSyncTaskName, param.getSyncTaskName())
        );

        if (CollUtil.isNotEmpty(applications)) {
            final Integer existCount = applicationInfoMapper.selectCount(Wrappers.<TgApplicationInfo>lambdaQuery()
                    .in(TgApplicationInfo::getId, applications.stream().map(TgDataSyncApplication::getApplicationId).collect(Collectors.toList()))
                    .ne(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.INVALID_APPLICATION)
                    .ne(TgApplicationInfo::getOldApplicationId, param.getId())
            );

            if (Objects.nonNull(existCount) && existCount > 0) {
                return AjaxResult.error("当前交换任务名称已存在，请重新命名。");
            }
        }

        TgAssetInfo tgAssetInfo = assetService.queryOne(param.getAssetId());
        if (Objects.isNull(tgAssetInfo)
                || !Objects.equals(tgAssetInfo.getType(), AssetType.TABLE)
                || Objects.isNull(tgAssetInfo.getMetaId())) {
            return AjaxResult.error("请申请表单资产");
        }

        final TgMetadataInfo tgMetadataInfo = metadataInfoMapper.selectById(tgAssetInfo.getMetaId().intValue());
        Result<MetaTableDTO> metaTableRes = metadataClient.getMetaTableDTO(tgMetadataInfo.getMetaDataId());
        if (metaTableRes.isSuccess()) {
            if (Objects.isNull(metaTableRes.getResult())) {
                return AjaxResult.error("资产不存在");
            }
        }

        Integer flowId = null;
        Optional<TgDataSyncApplication> existOpt = Optional.empty();
        if (Objects.nonNull(param.getId())) {
            List<TgDataSyncApplication> syncList = tgDataSyncApplicationMapper.selectList(new QueryWrapper<TgDataSyncApplication>().lambda()
                    .eq(TgDataSyncApplication::getApplicationId, param.getId()));
            if (CollectionUtils.isNotEmpty(syncList)) {
                existOpt = Optional.ofNullable(syncList.get(0));
            }
            existOpt.ifPresent(v -> param.setSyncTaskId(v.getSyncTaskId()));
            flowId = existOpt.map(TgDataSyncApplication::getFlowId).orElse(null);
        }

        final TgDataSyncApplication application = new TgDataSyncApplication();
        BeanUtils.copyProperties(param, application);

        application.setId(null);
        application.setFlowId(flowId);

        // 保存审批记录
        TgApplicationInfo applicationInfo = new TgApplicationInfo();
        // 资产id
        applicationInfo.setNewAssetId(param.getAssetId());
        // 添加资产权限信息
        final ArrayList<AssetPermissionType> permission = Lists.newArrayList();
        permission.add(AssetPermissionType.DATA_EXCHANGE_REQUEST);
        applicationInfo.setPermission(permission);
        applicationInfo.setApplicantId(SecurityUtils.getUserId());
        if (Objects.nonNull(param.getId())) {
            applicationInfo.setOldApplicationId(Long.parseLong(Integer.toString(param.getId())));
        }
        Long processId = param.getProcessId();
        if (Objects.isNull(processId) || Objects.isNull(TgAuditProcessInfo.newInstance().selectById(processId))) {
            return AjaxResult.error(InfoConstants.INVALID_PROCESS);
        }
        applicationInfo.setDataExpir(DateUtils.getEndOfDay(param.getExpiredDate()));
        applicationInfo.setProcessId(param.getProcessId());
        applicationInfo.setApplicantDepartment(param.getApplicantDepartment());
        applicationInfo.setApplyReason(param.getSyncTaskReason());
        applicationInfo.setApplicationType(ApplicationConst.ApplicationType.DATA_SYNC_APPLICATION);

        this.normalizeApplicationInfo(applicationInfo);

        // 序列化转换
        TgApplicationInfo tgApplicationInfo = this.convert2Obj(applicationInfo);
        JsonBeanConverter.convert2Json(tgApplicationInfo);
        // 设置组织架构用户信息
        this.setOrgUserInfo(applicationInfo);
        // 申请信息初始化
        this.setInitInfo(tgApplicationInfo);
        // 审批流
        this.buildHandleChain(tgApplicationInfo);
        insertOrUpdateApplication(applicationInfo, tgApplicationInfo);

        application.setSyncTaskName(param.getSyncTaskName());
        application.setApplicationId(tgApplicationInfo.getId());

        // 编辑逻辑
//        if (Objects.nonNull(application.getId())) {
//            tgDataSyncApplicationMapper.updateById(application);
//            // 删除原有字段
//            tgDataSyncFieldConfigDAO.deleteBySyncApplicationId(application.getId());
//        } else {
//            tgDataSyncApplicationMapper.insert(application);
//        }
        tgDataSyncApplicationMapper.insert(application);

        // 保存字段映射
        List<DataSyncTaskFieldConfig> fieldConfigs = param.getFieldsConfigs();
        if (CollectionUtils.isNotEmpty(fieldConfigs)) {
            List<TgDataSyncFieldConfig> tgFieldConfigs = fieldConfigs.stream().map(c -> {
                TgDataSyncFieldConfig tgFieldConfig = new TgDataSyncFieldConfig();
                tgFieldConfig.setSyncApplicationId(application.getId());
                BeanUtils.copyProperties(c, tgFieldConfig);
                return tgFieldConfig;
            }).collect(Collectors.toList());
            tgDataSyncFieldConfigDAO.saveBatch(tgFieldConfigs);
        }

        return AjaxResult.success();
    }


    @Override
    public AjaxResult previewApply(TgApplicationInfo applicationInfo, GetDataInfoRequestDTO requestDTO) {
        TgTemplateInfo template = TgTemplateInfo.newInstance().selectById(applicationInfo.getTemplateId());
        if (!Objects.equals(template.getTemplateType(), TemplateTypeEnum.wide_table.name())) {
            // 常规通用 直接取字段库数据
            return AjaxResult.succeed();
        }
        final TgApplicationInfo application = this.beforePreviewHandler(applicationInfo);
        if (application == null) {
            return AjaxResult.error(InfoConstants.REFRESH_PAGE);
        }

        boolean copy = Optional.of(applicationInfo).map(v -> Objects.nonNull(v.getCopyFromId())).orElse(false);
        String originName = Optional.ofNullable(applicationInfo.getCopyFromId()).map(userDataAssetsDAO::queryAssetsName).orElse(null);
        String sql = application.getAsql();
        String whereSql = this.handleWhereSql(requestDTO);
        log.info("sql={} whereSql={}", sql, whereSql);
        if (StringUtils.isBlank(sql)) {
            return AjaxResult.error("提交异常");
        }

        String cacheKey = RedisKeys.Apply.previewSqlCacheKey(Md5Utils.hash(SecurityUtils.getUserId() + " " + sql + whereSql + JsonUtils.format(requestDTO)));
        Object cacheStr = redisTemplate.opsForValue().get(cacheKey);
        List<LinkedHashMap<String, Object>> dataMap;
        if (Objects.nonNull(cacheStr)) {
            dataMap = JsonUtils.parse(cacheStr.toString(), new TypeReference<List<LinkedHashMap<String, Object>>>() {
            });
        } else {
            try {
                dataMap = ckProviderMapper.selectApplicationDataFromCk(sql, whereSql, requestDTO);
                if (CollectionUtils.isNotEmpty(dataMap)) {
                    redisTemplate.opsForValue().set(cacheKey, JsonUtils.format(dataMap), Duration.ofMinutes(2));
                }
            } catch (Exception e) {
                log.error("", e);
                throw new CustomException(ApplicationConst.ErrorMsg.buildCkMsg(e, copy, originName));
            }
        }

        List<ApplicationDataDto.Header> headers = this.previewBuildHeaders(application);
        ApplicationDataDto result = new ApplicationDataDto();
//        Long count = this.countApplicationDataFromCk(sql, whereSql);
//        result.setTotal(count);
        result.setList(dataMap);
        result.setHeader(headers);

        // 列设置
        final List<ColumnSetting> settings = applicationInfo.getColumnSettings();
        if (CollUtil.isEmpty(settings)) {
            result.setHeader(headers.stream()
                    .peek(v -> {
                        if (Objects.isNull(v.getSort())) {
                            v.setSort(ApplicationSqlUtil.nullSort);
                        }
                    })
                    .sorted(Comparator.comparing(ApplicationDataDto.Header::getSort))
                    .collect(Collectors.toList()));
            result.getHeader().forEach(header -> header.setDefaultShow("y"));
        } else {
            final Map<String, ColumnSetting> map = settings.stream().collect(Collectors.toMap(ColumnSetting::getFiledName, v -> v));
            result.setHeader(headers.stream()
                    .peek(v -> {
                        final ColumnSetting columnSetting = map.get(v.getFiledName());
                        v.setSort(columnSetting.getSort());
                        v.setDefaultShow(columnSetting.getDefaultShow());
                        v.setCustomName(columnSetting.getCustomName());
                    })
                    .sorted(Comparator.comparing(ApplicationDataDto.Header::getSort))
                    .collect(Collectors.toList()));
        }
        result.setSql(sql + (StringUtils.isBlank(whereSql) ? "" : " WHERE " + whereSql));
        result.setTailSql(application.getTailSql());

        return AjaxResult.success(result);
    }

    private TgApplicationInfo beforePreviewHandler(TgApplicationInfo applicationInfo) throws CustomException {
        // 规避同表多次join引起别名及字段映射错误
        boolean checkedRepeat = ApplicationSqlUtil.checkRepeatJoin(applicationInfo.getJoinInfo());
        if (checkedRepeat) {
            throw new CustomException("暂不支持同名表多次关联");
        }
        Long processId = applicationInfo.getProcessId();
        if (Objects.isNull(processId) || Objects.isNull(TgAuditProcessInfo.newInstance().selectById(processId))) {
            throw new CustomException(InfoConstants.INVALID_PROCESS);
        }

        this.normalizeApplicationInfo(applicationInfo);

        // 序列化转换
        TgApplicationInfo tgApplicationInfo = this.convert2Obj(applicationInfo);
        JsonBeanConverter.convert2Json(tgApplicationInfo);

        TgTemplateInfo templateInfo = new TgTemplateInfo().selectById(applicationInfo.getTemplateId());
        if (Objects.isNull(templateInfo)) {
            throw new CustomException("请选择模板");
        }

        JsonBeanConverter.convert2Obj(templateInfo);
        // 取最新审批流数据
        // 资产门户的审批流参数绑定在资产TgAssetInfo上，不再去模板获取
//        tgApplicationInfo.setProcessId(templateInfo.getProcessId());
        TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance().selectById(applicationInfo.getNewAssetId());
        AjaxResult<?> ajaxResult = assetService.fillProcessId4FollowMenuDirItem(tgAssetInfo);
        if (!ajaxResult.isSuccess()) {
            throw new CustomException(ajaxResult.getMsg());
        }
        applicationInfo.setProcessId(tgAssetInfo.getProcessId());
        AjaxResult<Object> checkResult = this.validateDataApply(applicationInfo, templateInfo);
        if (checkResult != null) {
            throw new CustomException(checkResult.getMsg());
        }

        if (Objects.isNull(applicationInfo.getId())) {
            Integer count = new TgApplicationInfo().selectCount(new QueryWrapper<TgApplicationInfo>().lambda()
                    .eq(TgApplicationInfo::getProjectName, applicationInfo.getProjectName()));
            if (count > 0) {
                throw new CustomException("需求名重复");
            }
        }

        // 处理过期时间
        applicationInfo.setDataExpir(DateUtils.getEndOfDay(applicationInfo.getDataExpir()));

        // 设置组织架构用户信息
        this.setOrgUserInfo(applicationInfo);

        // 申请信息初始化
        this.setInitInfo(tgApplicationInfo);

        Optional<TemplateTypeEnum> typeOpt = TemplateTypeEnum.of(templateInfo.getTemplateType());
        if (!typeOpt.isPresent()) {
            throw new CustomException("模板配置错误");
        }

        boolean taskType = typeOpt.get().isSchedulerTaskType();
        if (this.isDataApplication(applicationInfo) && !taskType) {
            AjaxResult<TgApplicationInfo> applyResult = this.extractSql(applicationInfo);
            if (!applyResult.isSuccess()) {
                throw new CustomException(applyResult.getMsg());
            }
            tgApplicationInfo = applyResult.getData();
        }

        // 设置流程处理信息
        this.buildHandleChain(tgApplicationInfo);
        return tgApplicationInfo;
    }

    /**
     * 新增或修改尚书台数据源
     *
     * @param dsId     元数据数据源id
     * @param schema   pg gp 使用到
     * @param database 数据库
     * @return 尚书台对象
     */
    @Override
    public BaseDataSourceParamDto upsertDs(Integer dsId, String schema, String database) {
        if (Objects.isNull(dsId)) {
            throw new CustomException("资产或表未绑定数据源");
        }
        BaseDataSourceParamDto dto = new BaseDataSourceParamDto();

        com.sinohealth.data.intelligence.datasource.entity.DataSource dsRes = dataSourceApiClient.findById(dsId);
        dto.setName(this.buildName(dsId, schema));

        com.sinohealth.data.intelligence.enums.DataSourceType dsType = com.sinohealth.data.intelligence.enums
                .DataSourceType.ofName(dsRes.getType());
        dto.setType(convertType(dsType).name());

        BaseDataSourceParam sourceParam = DataSourceUtils.buildDatasourceParam(dsType, dsRes.getDatasourceParams());
        dto.setHost(sourceParam.getHost());
        dto.setPort(sourceParam.getPort());
        dto.setDatabase(database);
        dto.setUserName(sourceParam.getUser());
        dto.setPassword(PasswordUtils.decodePassword(sourceParam.getPassword()));
        dto.setOther(sourceParam.getOther());
        if (StringUtils.isNotBlank(schema)) {
            if (MapUtils.isEmpty(dto.getOther())) {
                dto.setOther(new HashMap<>());
            }
            dto.getOther().put("currentSchema", schema);
        }

        AjaxResult dsResult = integrateDataSourceService.queryDataSourceListPaging(dto.getName(), 1, 2);
        Object data = dsResult.get("data");
        if (Objects.isNull(data)) {
            throw new CustomException("数据源查询异常");
        }
        LinkedHashMap dataMap = (LinkedHashMap) data;
        ArrayList totalList = (ArrayList) dataMap.get("totalList");
        if (CollectionUtils.isEmpty(totalList)) {
            Integer id = this.upsertDs(dto);
            dto.setId(id);
        } else {
            Map first = (LinkedHashMap) totalList.get(0);
            Integer id = (Integer) first.get("id");
            dto.setId(id);
            upsertDs(dto);
        }

        return dto;
    }

    private DbType convertType(com.sinohealth.data.intelligence.enums.DataSourceType dsType) {
        switch (dsType) {
            case MySQL:
                return DbType.MYSQL;
            case Greenplum:
            case PostgreSQL:
                return DbType.POSTGRESQL;
            case Hive:
                return DbType.HIVE;
            case ClickHouse:
                return DbType.CLICKHOUSE;
            case SQLServer:
                return DbType.SQLSERVER;
            case Oracle:
                return DbType.ORACLE;
        }
        throw new CustomException("不支持的数据源类型");
    }

    private Integer upsertDs(BaseDataSourceParamDto dto) {
        AjaxResult createRes = integrateDataSourceService.createDataSource(dto);
        if (createRes.getCode() != 0) {
            log.error("rsp={}", createRes);
            throw new CustomException("创建数据源失败");
        }
        return (Integer) createRes.get("data");
    }

    private String buildName(Integer dsId, String schema) {
        if (StringUtils.isNotBlank(schema)) {
            return "TG_" + dsId + "_" + schema;
        }
        return "TG_" + dsId;
    }

    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<TgApplicationInfo> addTemplateApplication(TgApplicationInfo apply) {
        // 规避同表多次join引起别名及字段映射错误
//        boolean checkedRepeat = ApplicationSqlUtil.checkRepeatJoin(apply.getJoinInfo());
//        if (checkedRepeat) {
//            return AjaxResult.error("暂不支持同名表多次关联");
//        }
        Long processId = apply.getProcessId();
        if (Objects.isNull(processId) || Objects.isNull(TgAuditProcessInfo.newInstance().selectById(processId))) {
            return AjaxResult.error(InfoConstants.INVALID_PROCESS);
        }

        this.normalizeApplicationInfo(apply);

        // 序列化转换
        TgApplicationInfo tgApplicationInfo = this.convert2Obj(apply);
        JsonBeanConverter.convert2Json(tgApplicationInfo);
        tgApplicationInfo.fillGraInfo();

        TgTemplateInfo templateInfo = new TgTemplateInfo().selectById(apply.getTemplateId());
        if (Objects.isNull(templateInfo)) {
            return AjaxResult.error("请选择模板");
        }

        JsonBeanConverter.convert2Obj(templateInfo);
        // 取最新审批流数据
        // 资产门户的审批流参数绑定在资产TgAssetInfo上，不再去模板获取
//        tgApplicationInfo.setProcessId(templateInfo.getProcessId());
        TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance().selectById(apply.getNewAssetId());
        AjaxResult<?> ajaxResult = assetService.fillProcessId4FollowMenuDirItem(tgAssetInfo);
        if (!ajaxResult.isSuccess()) {
            return AjaxResult.error(ajaxResult.getMsg());
        }

        apply.setProcessId(tgAssetInfo.getProcessId());
        AjaxResult<Object> checkResult = this.validateDataApply(apply, templateInfo);
        if (checkResult != null) {
            return AjaxResult.error(checkResult.getMsg());
        }

        if (Objects.isNull(apply.getId())) {
            Integer count = new TgApplicationInfo().selectCount(new QueryWrapper<TgApplicationInfo>().lambda()
                    .eq(TgApplicationInfo::getProjectName, apply.getProjectName()));
            if (count > 0) {
                return AjaxResult.error("需求名重复");
            }
        }

        // 处理过期时间
        apply.setDataExpir(DateUtils.getEndOfDay(apply.getDataExpir()));

        // 设置组织架构用户信息
        this.setOrgUserInfo(apply);

        // 申请信息初始化
        this.setInitInfo(tgApplicationInfo);

        Optional<TemplateTypeEnum> typeOpt = TemplateTypeEnum.of(templateInfo.getTemplateType());
        if (!typeOpt.isPresent()) {
            return AjaxResult.error("模板配置错误");
        }

        boolean taskType = typeOpt.get().isSchedulerTaskType();
        if (this.isDataApplication(apply) && !taskType) {
            AjaxResult<TgApplicationInfo> applyResult = this.extractSql(apply);
            if (StringUtils.isNotBlank(transferProperties.getBaseTableReplace())) {
                Map<String, String> kv = transferProperties.parseTableReplace();
                for (Map.Entry<String, String> en : kv.entrySet()) {
                    if (StringUtils.isNotBlank(apply.getTailSql())) {
                        apply.setTailSql(apply.getTailSql().replace(en.getKey(), en.getValue()));
                    }
                    apply.setAsql(apply.getAsql().replace(en.getKey(), en.getValue()));
                }
            }
            if (!applyResult.isSuccess()) {
                return applyResult;
            }
            tgApplicationInfo = applyResult.getData();
        }

        // 设置流程处理信息
        this.buildHandleChain(tgApplicationInfo);

        // 补偿【防止申请单打包名称为空】
        if (Objects.nonNull(apply.getPackTailId()) && StringUtils.isBlank(apply.getPackTailName())) {
            TgTemplatePackTailSetting setting = templatePackTailSettingService.findById(apply.getPackTailId());
            if (Objects.nonNull(setting)) {
                apply.setPackTailName(setting.getName());
            }
        }

//        产品说这期不做自动审批功能,白做
//         如果是资产负责人或者是白名单用户且申请的权限匹配则无需审核, 模型数据申请直接通过
//        List<AssetPermissionType> applyPermissions = tgApplicationInfo.getPermission().stream().map(p -> {
//            if (p.equals(AssetPermissionType.TEMPLATE_APPLY_REQUEST)) {
//                return AssetPermissionType.TEMPLATE_APPLY;
//            }
//            return p;
//        }).collect(Collectors.toList());
//        autoAudit(tgApplicationInfo, applyPermissions);

        AjaxResult<TgApplicationInfo> saveResult = this.insertOrUpdateApplication(apply, tgApplicationInfo);

        // 草稿状态
        if (apply.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.DRAFT)) {
            return saveResult;
        }

        if (saveResult.isSuccess()) {
            TgApplicationInfo data = saveResult.getData();
            // 更新需求状态
            CurrentDataPlanDTO plan = dataPlanService.currentPeriod(templateInfo.getBizType());
            applicationFormDAO.submitApply(data.getId(), apply.getApplicationNo(), plan.getPeriod(), templateInfo.getBizType());

            applicationTaskConfigService.saveApplicationTaskConfig(data);
            if (taskType) {
                projectCustomFieldDictDAO.saveMapping(tgApplicationInfo, templateInfo.getBizType());
            }
        }

        final List<ColumnSetting> columnSettings = apply.getColumnSettings();
        if (CollUtil.isNotEmpty(columnSettings) && saveResult.isSuccess()) {
            applicationColumnSettingService.saveColumnSetting(columnSettings, saveResult.getData().getId());
        }

        msgService.noticeAudit(tgApplicationInfo.getId());
        msgService.pushUnReadMsg(tgApplicationInfo.getApplicantId());

        return saveResult;
    }

    @Override
    public AjaxResult tryApplication(TgApplicationInfo applicationInfo) {
        this.normalizeApplicationInfo(applicationInfo);

        // 序列化转换
        TgApplicationInfo tgApplicationInfo = this.convert2Obj(applicationInfo);
        JsonBeanConverter.convert2Json(tgApplicationInfo);

        TgTemplateInfo templateInfo = new TgTemplateInfo().selectById(applicationInfo.getTemplateId());
        if (Objects.isNull(templateInfo)) {
            return AjaxResult.error("请选择模板");
        }

        JsonBeanConverter.convert2Obj(templateInfo);
        applicationInfo.setProcessId(templateInfo.getProcessId());
        AjaxResult<Object> checkResult = this.validateDataApply(applicationInfo, templateInfo);
        if (checkResult != null) {
            return checkResult;
        }

        // 设置组织架构用户信息
        this.setOrgUserInfo(applicationInfo);

        // 申请信息初始化
        this.setInitInfo(tgApplicationInfo);

        Optional<TemplateTypeEnum> typeOpt = TemplateTypeEnum.of(templateInfo.getTemplateType());

        // 处理数据申请
        if (this.isDataApplication(applicationInfo) && typeOpt.isPresent() && !typeOpt.get().isSchedulerTaskType()) {
            AjaxResult<TgApplicationInfo> applyResult = this.extractAndTryRunSql(applicationInfo);
            if (!applyResult.isSuccess()) {
                return applyResult;
            }
            tgApplicationInfo = applyResult.getData();
            return AjaxResult.success(tgApplicationInfo.getAsql());
        }

        return AjaxResult.success();
    }


    public AjaxResult<TgApplicationInfo> extractSql(TgApplicationInfo applicationInfo) {
        Object val = redisTemplate.opsForValue().get(RedisKeys.Apply.CLEAN_TEMP_USELESS_KEY);
        boolean fillResult = new WideTableSqlBuilder(val).fillApplication(applicationInfo);
        if (!fillResult) {
            return AjaxResult.error("申请失败，请反馈技术人员处理");
        }
        JsonBeanConverter.convert2Obj(applicationInfo);
        return AjaxResult.success(applicationInfo);
    }

    /**
     * 提取SQL并尝试执行 提前检查错误
     */
    public AjaxResult<TgApplicationInfo> extractAndTryRunSql(TgApplicationInfo applicationInfo) {
        boolean fillResult = new WideTableSqlBuilder().fillApplication(applicationInfo);
        if (!fillResult) {
            return AjaxResult.error("申请失败，请反馈技术人员处理");
        }

//        TgApplicationInfo result = new ApplicationSqlBuilder().buildApplication(applicationInfo);
//        if (Objects.isNull(result)) {
//            return AjaxResult.error("申请失败，请反馈技术人员处理");
//        }
        JsonBeanConverter.convert2Obj(applicationInfo);

        if (StringUtils.isNotBlank(transferProperties.getBaseTableReplace())) {
            Map<String, String> kv = transferProperties.parseTableReplace();
            for (Map.Entry<String, String> en : kv.entrySet()) {
                if (StringUtils.isNotBlank(applicationInfo.getTailSql())) {
                    applicationInfo.setTailSql(applicationInfo.getTailSql().replace(en.getKey(), en.getValue()));
                }
                applicationInfo.setAsql(applicationInfo.getAsql().replace(en.getKey(), en.getValue()));
            }
        }
        try {
            log.info("尝试运行SQL applyId={} userId={} name={}", applicationInfo.getId(),
                    applicationInfo.getApplicantId(), applicationInfo.getProjectName());
//            GetDataInfoRequestDTO request = new GetDataInfoRequestDTO();
//            request.setPageNum(1);
//            request.setPageSize(1);
//            ckProviderMapper.selectApplicationDataFromCk(applicationInfo.getAsql(), "", request);

            if (StringUtils.isNotBlank(applicationInfo.getTailSql())) {
                Long count = ckProviderMapper.countFromCk(applicationInfo.getAsql());
                Long tailCount = ckProviderMapper.countFromCk(applicationInfo.getTailSql());
                log.info("运行SQL applyId={} count={} tail={} total={}", applicationInfo.getId(), count, tailCount, count + tailCount);
            } else {
                Long count = ckProviderMapper.countFromCk(applicationInfo.getAsql());
                log.info("运行SQL applyId={} count={}", applicationInfo.getId(), count);
            }
        } catch (Exception e) {
            log.error("尝试运行SQL 失败 {} ", applicationInfo.getId(), e);
            return AjaxResult.error(ApplicationConst.ErrorMsg.buildCkMsg(e));
        }

        return AjaxResult.success(applicationInfo);
    }

    private AjaxResult<Object> validateDataApply(TgApplicationInfo applicationInfo, TgTemplateInfo templateInfo) {
        boolean dataApply = applicationInfo.getApplicationType().equals(ApplicationConst.ApplicationType.DATA_APPLICATION);
        boolean draft = Objects.equals(applicationInfo.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.DRAFT);
        if (!dataApply || draft) {
            return null;
        }

        String desc = applicationInfo.getApplyDesc();
        if (StringUtils.length(desc) > 200) {
            return AjaxResult.error("背景描述长度超出限制");
        }

        // 数据有效期检测
        if (Objects.nonNull(applicationInfo.getDataExpir())
                && new Date().after(applicationInfo.getDataExpir())) {
            return AjaxResult.error("数据有效期必须设定在当前日期之后");
        }
        // 指标字段检测
        if (checkMetricsAliasNull(applicationInfo)) {
            return AjaxResult.error("自定义指标必须填写指标名称");
        }

        // 指标字段检测
        if (checkMetricsComputeWay(applicationInfo)) {
            return AjaxResult.error("自定义指标必须选择聚合方式");
        }

        // 字段别名 模板别名 申请别名 出现重复
        Map<Long, String> selectFieldMap = applicationInfo.getGranularity().stream()
                .flatMap(v -> CollectionUtils.isEmpty(v.getFields()) ? Stream.empty() : v.getFields().stream())
                .collect(Collectors.toMap(SelectFieldDto::getFieldId, SelectFieldDto::getAlias, (front, current) -> current));
//        List<String> alias = applicationInfo.getGranularity().stream()
//                .flatMap(v -> CollectionUtils.isEmpty(v.getFields()) ? Stream.empty() : v.getFields().stream())
//                .map(SelectFieldDto::getAlias).collect(Collectors.toList());

        Set<String> alias = new HashSet<>(selectFieldMap.values());
        boolean existEmptyAlias = alias.stream().anyMatch(StringUtils::isBlank);
        if (existEmptyAlias) {
            return AjaxResult.error("存在字段未填写别名");
        }
        if (Objects.equals(templateInfo.getTemplateType(), TemplateTypeEnum.wide_table.name())
                && CollectionUtils.isEmpty(applicationInfo.getColsInfo())) {
            return AjaxResult.error("宽表模式必须选择字段");
        }

        // 需要跳过模板中的字段信息 模板的字段都是会从粒度来， realName 是输入的值
        List<String> colsAlias = applicationInfo.getColsInfo().stream()
                .filter(v -> Objects.equals(v.getIsItself(), CommonConstants.APPLICATION))
                .flatMap(v -> CollectionUtils.isEmpty(v.getRealName()) ? Stream.empty() : v.getRealName().stream())
                .map(RealName::getRealName).collect(Collectors.toList());
        alias.addAll(colsAlias);
        Map<String, List<String>> aliasMap = alias.stream().collect(Collectors.groupingBy(v -> v));
        String errorMsg = aliasMap.entrySet().stream().filter(v -> v.getValue().size() > 1).map(Map.Entry::getKey)
                .map(v -> "“" + v + "”")
                .collect(Collectors.joining("、"));
        if (StringUtils.isNoneBlank(errorMsg)) {
            return AjaxResult.error(" 当前模板下存在重复别名：" + errorMsg + "，请重新命名");
        }

        List<ApplicationGranularityDto> clean = new ArrayList<>();
        Map<String, TemplateGranularityDto> tempGraMap = Lambda.buildMap(templateInfo.getGranularity(), TemplateGranularityDto::getGranularity);
        for (ApplicationGranularityDto dto : applicationInfo.getGranularity()) {
            // 粒度的空检查都是为了规避前端空组件值问题
            if (StringUtils.isBlank(dto.getGranularity())) {
                clean.add(dto);
                continue;
            }
            TemplateGranularityDto temp = tempGraMap.get(dto.getGranularity());
            if (Objects.isNull(temp)) {
                clean.add(dto);
                continue;
            }
//            if (Objects.isNull(temp)) {
//                return AjaxResult.error("请配置 提数模板中 【" + FieldGranularityEnum.getDesc(dto.getGranularity()) + "信息】下的粒度");
//            }
            if (BooleanUtils.isTrue(temp.getGranularityRequired())
                    && CollectionUtils.isNotEmpty(temp.getDetails())
                    && CollectionUtils.isEmpty(dto.getSelectGranularity())) {
                return AjaxResult.error("请选择 【" + FieldGranularityEnum.getDesc(dto.getGranularity()) + "信息】下的粒度");
            }

            FilterDTO filter = dto.getFilter();
            if (CollectionUtils.isNotEmpty(dto.getSelectGranularity())
                    && BooleanUtils.isTrue(temp.getEnableFilter())
                    && BooleanUtils.isTrue(temp.getFilterRequired())) {
                if (Objects.isNull(filter)) {
                    return AjaxResult.error("请填写【" + FieldGranularityEnum.getDesc(dto.getGranularity()) + "信息】下的数据范围");
                }
            }
            if (Objects.nonNull(filter)) {
                Filter targetFilter = new Filter();
                ApplicationSqlUtil.convertToFilter(filter, targetFilter);
                ApplicationSqlUtil.FilterContext context = new ApplicationSqlUtil.FilterContext();
                boolean hasEmptyNode = ApplicationSqlUtil.hasEmptyNode(targetFilter, context);
                if (hasEmptyNode || !context.isHasItem()) {
                    return AjaxResult.error("请补全【" + FieldGranularityEnum.getDesc(dto.getGranularity()) + "信息】下的数据范围（筛选字段，筛选条件）");
                }
            }
        }

        applicationInfo.getGranularity().removeIf(clean::contains);

        // 校验join
        if (CollectionUtils.isNotEmpty(applicationInfo.getJoinInfo())) {
            for (JoinInfoDto joinInfoDto : applicationInfo.getJoinInfo()) {
                if (!ApplicationConst.JoinType.TYPE_MAP.containsKey(joinInfoDto.getJoinType())) {
                    return AjaxResult.error("请选择有效的关联方式");
                }
            }
        }

        return null;
    }

    private TgDocInfo getTgDocInfo(TgApplicationInfo applicationInfo) {
        // 这里返回的查询结果只会是 TgDocInfo的包装, 强转是没有问题的,所以消除检查
        @SuppressWarnings("unchecked")
        AjaxResult<TgDocInfo> docInfoAjaxResult = (AjaxResult<TgDocInfo>) docService.query(new HashMap<String, Object>() {{
            put("id", applicationInfo.getDocId());
        }});
        return docInfoAjaxResult.getData();
    }

    private boolean hasWhitelistAuthorization(TgApplicationInfo applicationInfo, TgDocInfo data) {
        return data.getWhitelistUsers().stream().anyMatch(u -> u.getUserId().equals(applicationInfo.getApplicantId())
                && u.getAuthorization().containsAll(applicationInfo.getDocAuthorization()));
    }

    private boolean isDataApplication(TgApplicationInfo applicationInfo) {
        return ApplicationConst.ApplicationType.DATA_APPLICATION.equals(applicationInfo.getApplicationType());
    }

    private boolean isDocApplication(TgApplicationInfo applicationInfo) {
        return ApplicationConst.ApplicationType.DOC_APPLICATION.equals(applicationInfo.getApplicationType());
    }

    /**
     * 生成 No
     *
     * @param reqApply    前端提交进来的对象
     * @param handleApply 加工过的
     * @return handleApply
     */
    private AjaxResult<TgApplicationInfo> insertOrUpdateApplication(TgApplicationInfo reqApply,
                                                                    TgApplicationInfo handleApply) {
        // 根据条件新增或修改
        AjaxResult<TgApplicationInfo> result = insertOrUpdate(reqApply, handleApply);
        if (!result.isSuccess()) {
            return result;
        }

        // 因为要新增之后才能提取到完整的对象, 所以设置一个空的方法调用, 实际是为了切点能获取完整对象
        if (reqApply.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.DRAFT)) {
            aspect.getApplication4Insert(handleApply);
        }
        final TgApplicationInfo data = result.getData();

        // 发送通知
        msgService.noticeAudit(data.getId());

        // 填充需求id
        final Long oldApplicationId = data.getOldApplicationId();
        if (Objects.nonNull(oldApplicationId)) {
            // 重新申请沿用旧id
            final TgApplicationInfo oldTgApplication = applicationInfoMapper.selectById(oldApplicationId);
            data.setApplicationNo(oldTgApplication.getApplicationNo());
            data.updateById();
        } else {
            if (Objects.nonNull(reqApply.getTemplateId())) {
                TgTemplateInfo templateInfo = new TgTemplateInfo().selectById(reqApply.getTemplateId());

                boolean isPersistence = Objects.equals(data.getRequireTimeType(), ApplicationConst.RequireTimeType.PERSISTENCE);
                String perPrefix = isPersistence ? "dur_" : "";
                String bizPrefix = BizTypeEnum.valueOf(templateInfo.getBizType()).getShortId();

                final String noStr = perPrefix + bizPrefix + "_" + data.getId() + DateUtil.format(new Date(), "MMdd");
                data.setApplicationNo(noStr);
                data.updateById();
            }
        }

        // TODO 需求单

        return result;
    }

    private boolean checkMetricsAliasNull(TgApplicationInfo tgApplicationInfo) {
        if (CollectionUtils.isEmpty(tgApplicationInfo.getMetricsInfo())) {
            return false;
        }
        AtomicBoolean checkAliasNull = new AtomicBoolean(false);
        tgApplicationInfo.getMetricsInfo().forEach((m) -> {
            if (StringUtils.isBlank(m.getAliasName())) {
                checkAliasNull.set(true);
            }
        });
        return checkAliasNull.get();
    }

    private boolean checkMetricsComputeWay(TgApplicationInfo tgApplicationInfo) {
        if (CollectionUtils.isEmpty(tgApplicationInfo.getMetricsInfo())) {
            return false;
        }

        return tgApplicationInfo.getMetricsInfo().stream().anyMatch(v -> Objects.isNull(v.getComputeWay()));
    }

    @Override
    public Object query(Map<String, Object> params) {

        TgApplicationInfo tgApplicationInfo = new TgApplicationInfo();

        // One 查询
        if (ObjectUtils.isNotNull(params.get(CommonConstants.ID))) {
            return tgApplicationInfo.selectById((Serializable) params.get(CommonConstants.ID));
        }

        // Page 查询
        int pagenum = Integer.parseInt(ThreadContextHolder.getParams().get(CommonConstants.PAGENUM).toString());
        int pagesize = Integer.parseInt(ThreadContextHolder.getParams().get(CommonConstants.PAGESIZE).toString());
        if (ObjectUtils.isNotNull(pagenum, pagesize)) {
            PageHelper.startPage(pagenum, pagesize);
            PageInfo<TgApplicationInfo> pageInfo = new PageInfo(tgApplicationInfo.selectAll().stream()
                    .sorted(Comparator.comparing(TgApplicationInfo::getUpdateTime))
                    .collect(Collectors.toList()));
            pageInfo.getList().stream().forEach(JsonBeanConverter::convert2Obj);
            return pageInfo;
        }

        return "error usage";
    }


    @Override
    public Object delete(Map<String, Object> params) {

        TgApplicationInfo tgApplicationInfo = new TgApplicationInfo();
        tgApplicationInfo.setId(Long.valueOf(String.valueOf(params.get(CommonConstants.ID))));
        tgApplicationInfo.deleteById();
        return "ok";
    }

    /**
     * 业务宽表 级联
     *
     * @see ApplicationServiceImpl#queryCascadeDataRange 产品标准表级联
     */
    @Override
    public AjaxResult queryDataRange(String colName,
                                     String tableName,
                                     String assetsTable,
                                     DataRangeQueryDto dataRange,
                                     DataSourceType sourceType) {
        if (Objects.equals(dataRange.getIsSelected(), "1")
                && CollectionUtils.isEmpty(dataRange.getData())
                && StringUtils.isNoneBlank(dataRange.getSearchContent())) {
            DataRangePageDto dataRangePageDto = new DataRangePageDto(0, 1, 10, Collections.emptyList());
            return AjaxResult.success(dataRangePageDto);
        }

        boolean isCN = ReUtil.isMatch(ReUtil.RE_CHINESES, colName);
        if (isCN) {
            colName = "`" + colName + "`";
        }
        Integer pageNum = dataRange.getPageNum();
        Integer pageSize = dataRange.getPageSize();

        List<String> results = null;
        if (Objects.isNull(dataRange.getTargetTable())) {
            dataRange.setTargetTable(tableName);
        }

        String querySQL = TgCkProvider.selectDataRangeFromCk(colName, tableName, dataRange);
        try {
            if (Objects.equals(sourceType, DataSourceType.CUSTOMER_CK)) {
                results = customerProviderMapper.selectDataRangeFromCk(colName, tableName, dataRange);
            } else {
                results = ckClusterAdapter.mixQueryRange(assetsTable, tableName, colName, dataRange);
            }
        } catch (Exception sqlException) {
            if (sqlException.getMessage().contains("ILLEGAL_TYPE_OF_ARGUMENT")) {
                return AjaxResult.error("目前只支持检索字符串类型");
            }
            log.error("", sqlException);
        }
        if (Objects.isNull(results)) {
            DataRangePageDto dataRangePageDto = new DataRangePageDto(0, pageNum, pageSize, Collections.emptyList());
            return AjaxResult.success(dataRangePageDto);
        }

        int total;
        if (Objects.equals(sourceType, DataSourceType.CUSTOMER_CK)) {
            total = customerProviderMapper.selectDataRangeCountFromCk(colName, tableName, dataRange);
        } else {
            Long count = ckClusterAdapter.mixCountRange(assetsTable, tableName, colName, dataRange);
            total = Optional.ofNullable(count).map(Long::intValue).orElse(0);
        }

        total = "".equals(dataRange.getIsSelected()) ? total
                : "1".equals(dataRange.getIsSelected()) ? dataRange.getData().size()
                : total - dataRange.getData().size();

        int originSize = results.size();
        results.removeIf(StringUtils::isBlank);
        if (originSize != results.size()) {
            results.add(0, ApplicationSqlUtil.NULL_FLAG);
        }
        DataRangePageDto dataRangePageDto = new DataRangePageDto(total, pageNum, pageSize, results, querySQL);
        return AjaxResult.success(dataRangePageDto);
    }

    @Override
    public AjaxResult<IPage<ApplicationFormPageDto>> pageQueryApplicationByNo(ApplicationFormPageRequest request) {
        IPage<TgApplicationInfo> pageRes = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getApplicationNo, TgApplicationInfo::getId, TgApplicationInfo::getCreateTime,
                        TgApplicationInfo::getCurrentAuditProcessStatus,
                        TgApplicationInfo::getCurrentHandlers, TgApplicationInfo::getHandleNodeJson, TgApplicationInfo::getCurrentIndex,
                        TgApplicationInfo::getAssetsId, TgApplicationInfo::getProjectName)
                .eq(TgApplicationInfo::getApplicationNo, request.getApplicationNo())
                .notIn(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.notShow)
                .orderByDesc(TgApplicationInfo::getId)
                .page(request.buildPage());
        List<TgApplicationInfo> records = pageRes.getRecords();
        Map<Long, Integer> applyVerMap;
        if (CollectionUtils.isNotEmpty(records)) {
            Optional<Long> aidOpt = records.stream().map(TgApplicationInfo::getAssetsId).filter(Objects::nonNull).findAny();

            applyVerMap = aidOpt.map(v -> {
                List<UserDataAssets> aList = userDataAssetsDAO.lambdaQuery()
                        .select(UserDataAssets::getId, UserDataAssets::getVersion, UserDataAssets::getSrcApplicationId)
                        .eq(UserDataAssets::getId, v)
                        .list();
                return aList.stream().collect(Collectors.toMap(UserDataAssets::getSrcApplicationId, UserDataAssets::getVersion,
                        (front, current) -> front > current ? front : current));
            }).orElse(Collections.emptyMap());
        } else {
            applyVerMap = Collections.emptyMap();
        }

        List<Long> userIds = records.stream()
                .flatMap(v -> Arrays.stream(v.getCurrentHandlers().split(",")))
                .filter(StringUtils::isNotBlank)
                .map(Long::valueOf)
                .distinct().collect(Collectors.toList());
        List<SysUser> userList = Lambda.queryListIfExist(userIds, userService::selectUserByIds);
        Map<Long, SysUser> userMap = userList.stream().collect(Collectors.toMap(SysUser::getUserId, v -> v, (front, current) -> current));

        AtomicInteger cnt = new AtomicInteger(request.buildOffset());
        return AjaxResult.success(PageUtil.convertMap(pageRes, v -> {
            JsonBeanConverter.convert2Obj(v);
            ApplicationFormPageDto dto = new ApplicationFormPageDto();
            dto.setNo(cnt.incrementAndGet());
            dto.setApplicationId(v.getId());
            dto.setCreateTime(v.getCreateTime());
            dto.setAuditState(v.getCurrentAuditProcessStatus());

            fillAuditInfo(v, userMap, dto);
            dto.setAssetsVersion(applyVerMap.get(v.getId()));
            dto.setProjectName(v.getProjectName());
            return dto;
        }));
    }

    private static void fillAuditInfo(TgApplicationInfo v, Map<Long, SysUser> userMap,
                                      ApplicationFormPageDto dto) {
        Optional<ProcessNodeEasyDto> nodeOpt = ApplyUtil.lastNode(v);
        if (Objects.equals(v.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDITING)) {
            String currentHandlerNames = "";
            if (!"".equals(v.getCurrentHandlers())) {
                currentHandlerNames = StringUtils.join(Arrays.stream(v.getCurrentHandlers().split(","))
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
            dto.setHandleUser(currentHandlerNames);
        } else {
            nodeOpt.map(ProcessNodeEasyDto::getHandlerName).ifPresent(dto::setHandleUser);
        }
        nodeOpt.map(ProcessNodeEasyDto::getHandleTime).ifPresent(dto::setAuditTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deleteApplication(TgApplicationInfo apply) {
        final Long id = apply.getId();
        apply = applicationInfoMapper.selectById(id);
        if (apply.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.DRAFT)) {
            apply.deleteById();
            return AjaxResult.success();
        } else {
            return AjaxResult.error("不能删除非草稿状态的申请单");
        }
    }

    /**
     * 找出有关联的条件，并设置字段名
     */
    private List<FilterDTO.FilterItemDTO> matchCascade(List<FilterDTO.FilterItemDTO> items) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        Map<String, FilterDTO.FilterItemDTO> itemMap = items.stream().collect(Collectors
                .toMap(FilterDTO.FilterItemDTO::getFieldName, v -> v, (front, current) -> current));
        List<List<String>> cascadeFields = appProperties.buildCascadeFieldsList();

        Set<FilterDTO.FilterItemDTO> result = new HashSet<>();
        // 历史使用物理表id， 当前使用字段库id
//        List<TableFieldInfo> fields = tableFieldInfoService.findListByFieldIds(new ArrayList<>(itemMap.keySet()));
//
//        Map<Long, String> nameMap = fields.stream()
//                .collect(Collectors.toMap(TableFieldInfo::getId, TableFieldInfo::getFieldName, (front, current) -> current));
//        for (FilterDTO.FilterItemDTO item : items) {
//            item.setFieldName(nameMap.get(item.getFieldId()));
//        }

        for (List<String> cascadeField : cascadeFields) {
            Collection<String> matches = CollectionUtils.intersection(itemMap.keySet(), cascadeField);
            if (CollectionUtils.isEmpty(matches)) {
                continue;
            }
            // 因为条件排除了自身，所以非空就看作有交集了
            for (String matchedField : matches) {
                for (FilterDTO.FilterItemDTO item : items) {
                    if (Objects.equals(item.getFieldName(), matchedField)) {
                        result.add(item);
                    }
                }
            }
        }
        return new ArrayList<>(result);
    }


    /**
     * 产品标准表级联
     */
    @Override
    public AjaxResult<DataRangePageDto> queryCascadeDataRange(MetaDataFieldInfo fieldInfo, DataRangeQueryDto queryDTO) {
        if (Objects.equals(queryDTO.getIsSelected(), "1")
                && CollectionUtils.isEmpty(queryDTO.getData())
                && StringUtils.isNoneBlank(queryDTO.getSearchContent())) {
            DataRangePageDto dataRangePageDto = new DataRangePageDto(0, 1, 10, Collections.emptyList());
            return AjaxResult.success(dataRangePageDto);
        }
        List<FilterDTO.FilterItemDTO> items = queryDTO.getFilterItems();
        List<FilterDTO.FilterItemDTO> needItems = this.matchCascade(items);
//        String whereSQL = " status not in ('回收站','禁用') AND ";
        String whereSQL = QuerySqlUtil.buildWhere(appProperties.getCascadeTable(), needItems);

        String order = queryDTO.getSearchOrder() == 1 ? "ASC" : "DESC";

        String fieldName = fieldInfo.getColName();
        String sqlInPattern = " %s %s (%s) ";
        String isSelected = queryDTO.getIsSelected();
        List<String> data = queryDTO.getData();
        if (StringUtils.isBlank(isSelected)) {
            // 全部标签
        } else if (isSelected.equals("1")) {
            // 已勾选
            if (data.size() > 0) {
                whereSQL += " AND " + String.format(sqlInPattern, fieldName, " IN ",
                        "'" + StringUtils.join(queryDTO.getData(), "','") + "'");
            }
        } else {
            // 未勾选
            if (data.size() > 0) {
                whereSQL += " AND " + String.format(sqlInPattern, fieldName, " NOT IN ",
                        "'" + StringUtils.join(queryDTO.getData(), "','") + "'");
            }
        }

        String searchCondition = QuerySqlUtil.buildCondition(fieldName, queryDTO);
        if (StringUtils.isNotBlank(searchCondition)) {
            whereSQL += " AND " + searchCondition;
        }

        Integer pageNum = queryDTO.getPageNum();
        Integer pageSize = queryDTO.getPageSize();

        String fmt = "SELECT DISTINCT %s FROM %s WHERE %s GROUP BY %s ORDER BY %s %s";
        String querySQL = String.format(fmt, fieldName, appProperties.getCascadeTable(), whereSQL, fieldName, fieldName, order);

        log.info("querySQL={}", querySQL);
        String limitFmt = " LIMIT %s offset %s";
        List<String> results = ckProviderMapper.selectCascadeDataRangeFromCk(querySQL +
                String.format(limitFmt, pageSize, (pageNum - 1) * pageSize));

        if (CollectionUtils.isEmpty(results)) {
            DataRangePageDto dataRangePageDto = new DataRangePageDto(0, pageNum, pageSize, Collections.emptyList(), querySQL);
            return AjaxResult.success(dataRangePageDto);
        }

        Long total = ckProviderMapper.selectCountApplicationDataFromCk(querySQL, "");
        total = "".equals(queryDTO.getIsSelected()) ? total
                : "1".equals(queryDTO.getIsSelected()) ? queryDTO.getData().size()
                : Optional.ofNullable(total).map(Long::intValue).orElse(0) - queryDTO.getData().size();

        int originSize = results.size();
        results.removeIf(StringUtils::isBlank);
        if (originSize != results.size()) {
            results.add(0, ApplicationSqlUtil.NULL_FLAG);
        }

        DataRangePageDto dataRangePageDto = new DataRangePageDto(total.intValue(), pageNum, pageSize, results, querySQL);
        return AjaxResult.success(dataRangePageDto);
    }

    @Override
    public AjaxResult<ApplicationDataDto> getApplicationDataFromCk(Long applyId, GetDataInfoRequestDTO requestDTO, UserDataAssets assets) {
        TgApplicationInfo application = aspect.getApplication(applyId);
        if (application == null) {
            return AjaxResult.error(InfoConstants.REFRESH_PAGE);
        }

        Optional<UserDataAssets> assetsOpt = Optional.ofNullable(assets);
        boolean copy = assetsOpt.map(v -> Objects.nonNull(v.getCopyFromId())).orElse(false);
        String originName = assetsOpt.map(UserDataAssets::getCopyFromId).map(userDataAssetsDAO::queryAssetsName).orElse(null);

        String asSql = assetsOpt.map(UserDataAssets::getAssetsSql).orElse(null);
        String sql = StringUtils.isNoneBlank(asSql) ? asSql : application.getAsql();
        String whereSql = this.handleWhereSql(requestDTO);
        if (StringUtils.isBlank(sql)) {
            return AjaxResult.error("数据同步中");
        }
        if (copy) {
            sql = "SELECT * FROM (" + sql + ") " + StrUtil.randomAlpha(3);
        }
        log.info("sql={} whereSql={}", sql, whereSql);
        List<LinkedHashMap<String, Object>> dataMap;

        List<ApplicationDataDto.Header> headers = this.buildHeaders(application);
        try {
            if (Objects.isNull(assets)) {
                dataMap = ckProviderMapper.selectApplicationDataFromCk(sql, whereSql, requestDTO);
            } else {
                dataMap = ckClusterAdapter.mixQueryData(assets.getAssetTableName(), sql, whereSql, requestDTO);
            }
        } catch (Exception e) {
            log.error("", e);
            throw new CustomException(ApplicationConst.ErrorMsg.buildCkMsg(e, copy, originName));
        }

        Long count;
        if (Objects.isNull(assets)) {
            count = ckProviderMapper.selectCountApplicationDataFromCk(sql, Optional.ofNullable(whereSql).orElse(""));
        } else {
            count = ckClusterAdapter.mixCount(assets.getAssetTableName(), sql, whereSql);
        }

        ApplicationDataDto result = new ApplicationDataDto();
        result.setTotal(count);
        result.setList(dataMap);
        result.setHeader(headers);

        result.setHeader(headers.stream()
                .peek(v -> {
                    if (Objects.isNull(v.getSort())) {
                        v.setSort(ApplicationSqlUtil.nullSort);
                    }
                })
                .sorted(Comparator.comparing(ApplicationDataDto.Header::getSort))
                .collect(Collectors.toList()));
        result.getHeader().forEach(header -> header.setDefaultShow("y"));
        result.setSql(sql + (StringUtils.isBlank(whereSql) ? "" : " WHERE " + whereSql));
        result.setRelateDict(application.getRelateDict());

        return AjaxResult.success(result);
    }

    /**
     * 不知道为什么现在这个接口在【客户-我的资产-预览】和【我的数据-预览】两个地方都调用。
     * 需要统计【客户-我的资产-预览】次数
     *
     * @see ApplicationServiceImpl#buildHeaders(UserDataAssets)
     */
    @DataSource(DataSourceType.SLAVE)
    @Override
    public AjaxResult<ApplicationDataDto> queryAssetsDataFromCk(Long assetsId, DataPreviewRequest requestDTO) {
        UserDataAssets assets = aspect.getDataAssets(assetsId);
        if (assets == null) {
            return AjaxResult.error(InfoConstants.REFRESH_PAGE);
        }

        boolean isSnapshot = false;
        // 查询快照版本
        Integer version = requestDTO.getVersion();
        if (Objects.nonNull(version)) {
            UserDataAssetsSnapshot snapshot = userDataAssetsSnapshotDAO.queryByAssetsId(assetsId, version);
            if (Objects.nonNull(snapshot)) {
                log.info("use snapshot: version={}", version);
                assets = snapshot;
                isSnapshot = true;
            }
        }
        AssetsExpireEnum expire = DateUtils.convertExpire(assets.getDataExpire());
        if (Objects.equals(AssetsExpireEnum.delete, expire)) {
            ApplicationDataDto dto = new ApplicationDataDto();
            dto.setExpireType(expire.name());
            return AjaxResult.success(dto);
        }

        final Long applicationId = assets.getSrcApplicationId();

        final List<ColumnSetting> columnSettings = applicationColumnSettingDAO.getByApplicationId(applicationId);
        Map<String, ColumnSetting> colSetting;
        if (CollUtil.isNotEmpty(columnSettings)) {
            colSetting = columnSettings.stream()
                    .collect(Collectors.toMap(ColumnSetting::getFiledName, v -> v));
        } else {
            colSetting = new HashMap<>();
        }

        LatestVersionEnum latestVersionEnum = this.buildVersionEnum(assets, isSnapshot);
        if (Objects.equals(assets.getTemplateType(), TemplateTypeEnum.wide_table.name())) {
            return this.getDataForWideTable(requestDTO, assets, colSetting, expire, latestVersionEnum);
        }

        return this.getDataForFlowTable(requestDTO, assets, colSetting, expire, latestVersionEnum);
    }

    /**
     * 宽表模式 数据获取
     */
    private AjaxResult<ApplicationDataDto> getDataForWideTable(DataPreviewRequest requestDTO,
                                                               UserDataAssets assets,
                                                               Map<String, ColumnSetting> colSetting,
                                                               AssetsExpireEnum expire,
                                                               LatestVersionEnum latestVersionEnum) {
        final Long applicationId = assets.getSrcApplicationId();
        AjaxResult<ApplicationDataDto> dataInfo = this.getApplicationDataFromCk(applicationId, requestDTO, assets);
        if (!dataInfo.isSuccess()) {
            throw new IllegalArgumentException(dataInfo.getMsg());
        }
        String sql = assets.getAssetsSql();
        ApplicationDataDto data = dataInfo.getData();
        // 另存项目 字段特殊处理
        if (dataInfo.isSuccess() && Objects.nonNull(assets.getCopyFromId())) {
            List<ApplicationDataDto.Header> headers = data.getHeader();

            // com.sinohealth.system.biz.dataassets.service.impl.UserDataAssetsServiceImpl.saveAs 中另存中会在Select后拼接\n

            String[] lines = sql.split("\n");
            String selectFields = lines[0];
            if (!selectFields.contains("*")) {
                selectFields = selectFields.replace("SELECT", "");
                String[] fields = selectFields.split(",");
                Set<String> finalFields = Stream.of(fields).map(v -> v.replace("`", "")).map(String::trim).collect(Collectors.toSet());
                headers.removeIf(v -> !finalFields.contains(v.getFiledName()));
            }

        }
        if (MapUtils.isNotEmpty(colSetting) && BooleanUtils.isNotFalse(data.getRelateDict())) {
            data.setHeader(data.getHeader().stream()
                    .peek(v -> {
                        if (MapUtils.isNotEmpty(colSetting)) {
                            final ColumnSetting setting = colSetting.get(v.getFiledName());
                            if (Objects.isNull(setting)) {
                                return;
                            }
                            v.setSort(setting.getSort());
                            v.setDefaultShow(setting.getDefaultShow());
                            v.setCustomName(setting.getCustomName());
                        }
                    })
                    .sorted(Comparator.comparing(ApplicationDataDto.Header::getSort))
                    .collect(Collectors.toList()));
        } else {
            // 特殊说明-原易数阁需求：针对period_new字段默认不勾选，且不会作为导出字段
            data.setHeader(data.getHeader().stream()
                    .peek(v -> {
                        if (v.getFiledName().equals(ApplicationConst.PeriodField.PERIOD_NEW)) {
                            v.setDefaultShow("n");
                        }
                        v.setCustomName(v.getFiledAlias());
                    })
                    .sorted(Comparator.comparing(ApplicationDataDto.Header::getSort))
                    .collect(Collectors.toList()));
        }

        Optional.of(data).ifPresent(v -> {
            v.setExpireType(expire.name());
            v.setLatestVersion(latestVersionEnum.getCode());
        });
        return dataInfo;
    }

    /**
     * 常规通用 数据获取
     */
    private AjaxResult<ApplicationDataDto> getDataForFlowTable(DataPreviewRequest requestDTO,
                                                               UserDataAssets assets,
                                                               Map<String, ColumnSetting> colSetting,
                                                               AssetsExpireEnum expire,
                                                               LatestVersionEnum latestVersionEnum) {
        Long assetsId = assets.getId();
        final SysUser sysUser = ThreadContextHolder.getSysUser();
        String sql = assets.getAssetsSql();
        if (sysUser != null && Objects.equals(sysUser.getUserInfoType(), CommonConstants.CUSTOMER_UESR)) {
            // 数据埋点
            TgCustomerApplyAuth applyAuth = customerAuthDAO.getDataAssets(sysUser.getUserId(), assetsId);
            // 判断当前用户是否是资产所有者
            if (applyAuth != null) {
                // 客户查表次数(资产粒度)-埋点 (get(0)是因为一个资产只能分配给一个客户)
                EventReporterUtil.operateLogEvent4View(applyAuth.getId().toString(),
                        assets.getProjectName(), SecondSubjectTypeEnum.CUSTOMER_APPLY_AUTH_VIEW, null);
                // 客户查表次数(表单粒度)-埋点
                EventReporterUtil.operateLogEvent4View(assets.getBaseTableId().toString(),
                        assets.getBaseTableName(), SecondSubjectTypeEnum.CUSTOMER_APPLY_TABLE_VIEW, null);
            }
        }

        String whereSql = this.handleWhereSql(requestDTO);
        if (StringUtils.isBlank(sql)) {
            return AjaxResult.error("数据同步中");
        }

        boolean copy = Objects.nonNull(assets.getCopyFromId());
        String originName = Optional.ofNullable(assets.getCopyFromId()).map(userDataAssetsDAO::queryAssetsName).orElse(null);

        log.info("sql={} whereSql=[{}]", sql, whereSql);
        List<LinkedHashMap<String, Object>> dataMap;
        try {
            dataMap = ckClusterAdapter.mixQueryData(assets.getAssetTableName(), sql, whereSql, requestDTO);
        } catch (Exception e) {
            log.error("", e);
            throw new CustomException(ApplicationConst.ErrorMsg.buildCkMsg(e, copy, originName));
        }


        // 查询数据库连接 获取查询元数据
        TgApplicationInfo apply = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getRelateDict)
                .eq(TgApplicationInfo::getId, assets.getSrcApplicationId())
                .one();
        String finalSQL = TgCkProvider.selectApplicationDataFromCk(sql, whereSql, requestDTO);
        Set<String> names = ckClusterAdapter.mixColumnNames(assets.getAssetTableName(), finalSQL);
        log.info("HEAD: header={}", names);

        List<ApplicationDataDto.Header> headers;
        boolean originSort = Objects.nonNull(apply) && BooleanUtils.isFalse(apply.getRelateDict());
        if (originSort) {
            AtomicLong cnt = new AtomicLong();
            headers = names.stream().map(v -> {
                long id = cnt.incrementAndGet();
                ApplicationDataDto.Header h = new ApplicationDataDto.Header(id, v, v, CkPgJavaDataType.String.toString());
                h.setSort((int) id);
                h.setCustomName(v);
                h.setDefaultShow("y");
                return h;
            }).collect(Collectors.toList());

        } else {
            headers = this.buildHeaders(assets);
            headers.removeIf(v -> !names.contains(v.getFiledName()));
        }

//        Long count = this.countApplicationDataFromCk(sql, whereSql);
        Long count = ckClusterAdapter.mixCount(assets.getAssetTableName(), sql, whereSql);
        ApplicationDataDto result = new ApplicationDataDto();
        result.setTotal(count);
        result.setList(dataMap);
        if (!originSort) {
            result.setHeader(headers.stream()
                    .peek(v -> {
                        if (MapUtils.isEmpty(colSetting)) {
                            if (Objects.isNull(v.getSort())) {
                                v.setSort(ApplicationSqlUtil.nullSort);
                            }
                            // 特殊说明-原易数阁需求：针对period_new字段默认不勾选，且不会作为导出字段
                            if (v.getFiledName().equals(ApplicationConst.PeriodField.PERIOD_NEW)) {
                                v.setDefaultShow("n");
                            } else {
                                v.setDefaultShow("y");
                            }
                            v.setCustomName(v.getFiledAlias());
                        } else {
                            final ColumnSetting setting = colSetting.get(v.getFiledName());
                            v.setSort(setting.getSort());
                            v.setDefaultShow(setting.getDefaultShow());
                            v.setCustomName(setting.getCustomName());
                        }
                    })
                    .sorted(Comparator.comparing(ApplicationDataDto.Header::getSort))
                    .collect(Collectors.toList()));
        } else {
            result.setHeader(headers);
        }
        result.setSql(finalSQL);
        result.setExpireType(expire.name());
        result.setLatestVersion(latestVersionEnum.getCode());
        return AjaxResult.success(result);
    }

    public LatestVersionEnum buildVersionEnum(UserDataAssets assets, boolean useSnapshot) {
        if (useSnapshot) {
            return LatestVersionEnum.success_read;
        }

        boolean noRead = Objects.equals(assets.getReadFlag(), AsyncTaskConst.ReadFlag.NO_READ);

        if (Objects.equals(assets.getTemplateType(), TemplateTypeEnum.wide_table.name())) {
            List<AssetsWideUpgradeTrigger> triggers = assetsWideUpgradeTriggerDAO.getBaseMapper().selectList(new QueryWrapper<AssetsWideUpgradeTrigger>().lambda()
                    .select(AssetsWideUpgradeTrigger::getState)
                    .eq(AssetsWideUpgradeTrigger::getAssetsId, assets.getId())
                    .orderByDesc(AssetsWideUpgradeTrigger::getCreateTime)
                    .last(" limit 1")
            );

            if (CollectionUtils.isNotEmpty(triggers)) {
                AssetsWideUpgradeTrigger trigger = triggers.get(0);
                if (Objects.equals(trigger.getState(), AssetsUpgradeStateEnum.failed.name())) {
                    return LatestVersionEnum.failed;
                }
            }

            return noRead ? LatestVersionEnum.success_not_read : LatestVersionEnum.success_read;
        }

        Boolean failed = assetsFlowBatchDetailDAO.lambdaQuery()
                .eq(AssetsFlowBatchDetail::getApplicationId, assets.getSrcApplicationId())
                .in(AssetsFlowBatchDetail::getState, AssetsUpgradeStateEnum.end)
                .orderByDesc(AssetsFlowBatchDetail::getFinishTime)
                .last(" limit 1")
                .oneOpt().map(v -> Objects.equals(v.getState(), AssetsUpgradeStateEnum.failed.name()))
                .orElse(false);
        if (failed) {
            return LatestVersionEnum.failed;
        }

        return noRead ? LatestVersionEnum.success_not_read : LatestVersionEnum.success_read;
    }

    /**
     * @see ApplicationServiceImpl#buildHeaders(TgApplicationInfo)
     */
    @Override
    public List<ApplicationDataDto.Header> buildHeaders(UserDataAssets dataAssets) {
        List<ApplicationDataDto.Header> header = new ArrayList<>();

        FieldMetaBO fieldMeta;
        TgApplicationInfo info = TgApplicationInfo.newInstance().selectById(dataAssets.getSrcApplicationId());
        JsonBeanConverter.convert2Obj(info);

        List<Long> templateIds = info.getGranularity().stream().map(ApplicationGranularityDto::getRangeTemplateId)
                .filter(Objects::nonNull).collect(Collectors.toList());
        Set<Long> fieldIds = dataRangeTemplateService.queryFieldIdsByIds(templateIds);

        if (Objects.equals(dataAssets.getTemplateType(), TemplateTypeEnum.wide_table.name())) {
            return this.buildHeaders(info);
        } else {
            fieldMeta = this.queryAssetsFieldMeta(dataAssets, fieldIds);
        }

        List<TableFieldInfo> fieldList = fieldMeta.getTableFields();

        for (TableFieldInfo field : fieldList) {
            ApplicationDataDto.Header newHeader = new ApplicationDataDto.Header(field.getId(), field.getTableId(),
                    field.getFieldName(),
//                    StringUtils.isNotBlank(field.getRealName()) ? field.getRealName() : field.getFieldAlias(),
                    this.firstNotBlankStr(field.getRealName(), field.getFieldAlias()),
                    field.getDataType(), field.isPrimaryKey(), field.getDefaultShow());

            newHeader.setSort(field.getSort());
            header.add(newHeader);
        }


        return header;
    }

    private String firstNotBlankStr(String... names) {
        return Stream.of(names).filter(StringUtils::isNotBlank).findFirst().orElse("");
    }

    private List<ApplicationDataDto.Header> previewBuildHeaders(TgApplicationInfo applyInfo) {
        List<ApplicationDataDto.Header> header = new ArrayList<>();

        FieldMetaBO fieldMeta = this.getApplicationFieldMeta(applyInfo);

        List<TableFieldInfo> fieldList = fieldMeta.getTableFields();
        List<CustomFieldInfo> customFields = fieldMeta.getCustomFields();


        TgTemplateInfo template = JsonBeanConverter.convert2Obj(new TgTemplateInfo().selectById(applyInfo.getTemplateId()));
        // 日期聚合
        if (CollectionUtils.isNotEmpty(template.getApplicationPeriodField())) {
            // period_str 特殊处理 禁用了等于，防止通过id去查列数据（真实列才可行）
            if (CollectionUtils.isNotEmpty(applyInfo.getColsInfo())) {
                List<FieldDict> periodList = fieldDictDAO.getBaseMapper().selectList(new QueryWrapper<FieldDict>().lambda()
                        .in(FieldDict::getFieldName, ApplicationConst.PeriodField.idToNameMap.values()));
                Map<String, Integer> periodMap = Lambda.buildMap(periodList, FieldDict::getFieldName, FieldDict::getSort);

                List<Long> selectPeriod = applyInfo.getColsInfo().stream().flatMap(v -> v.getSelect().stream())
                        .filter(ApplicationConst.PeriodField.idToNameMap::containsKey).collect(Collectors.toList());

                if (!selectPeriod.isEmpty() && selectPeriod.size() != ApplicationConst.PeriodField.idToNameMap.size()) {
                    for (Long id : selectPeriod) {
                        ApplicationConst.PeriodFieldEnum periodFieldEnum = ApplicationConst.PeriodFieldEnum.of(id);
                        ApplicationDataDto.Header newHeader = new ApplicationDataDto.Header(periodFieldEnum.getId(), periodFieldEnum.getName(),
                                periodFieldEnum.getAlias(), periodFieldEnum.getViewType());
                        Integer sort = periodMap.getOrDefault(periodFieldEnum.getName(), id.intValue());
                        newHeader.setSort(sort);
                        header.add(newHeader);
                    }
                } else {
                    ApplicationConst.PeriodField.idToNameMap.forEach((id, v) -> {
                                ApplicationConst.PeriodFieldEnum periodFieldEnum = ApplicationConst.PeriodFieldEnum.of(id);
                                ApplicationDataDto.Header newHeader = new ApplicationDataDto.Header(periodFieldEnum.getId(),
                                        periodFieldEnum.getName(), periodFieldEnum.getAlias(), periodFieldEnum.getViewType());
                                Integer sort = periodMap.getOrDefault(periodFieldEnum.getName(), id.intValue());
                                newHeader.setSort(sort);
                                header.add(newHeader);
                            }
                    );
                }
            }
        }

        Map<Long, TableFieldInfo> fieldMap = Lambda.buildMap(fieldList, TableFieldInfo::getRelationColId);

        Map<Long, FieldDict> fieldDictMap = Lambda.queryMapIfExist(fieldMap.keySet(), fieldDictDAO::listByIds, FieldDict::getId);
        int joinSort = ApplicationSqlUtil.joinSort;
        for (TableFieldInfo field : fieldList) {
            ApplicationDataDto.Header newHeader = new ApplicationDataDto.Header(field.getId(), field.getTableId(),
                    field.getFieldName(), StringUtils.isNotBlank(field.getRealName()) ? field.getRealName() : field.getFieldAlias(),
                    field.getDataType(), field.isPrimaryKey(), field.getDefaultShow());
            newHeader.setSort(Optional.ofNullable(fieldDictMap.get(field.getRelationColId())).map(FieldDict::getSort).orElse(joinSort++));
            header.add(newHeader);
        }

        Map<Long, String> nameMap = Lambda.buildMap(applyInfo.getCustomMetrics(), CustomMetricsLabelDto::getMetricsId,
                CustomMetricsLabelDto::getAlias);
        List<MetricsDict> metricsDicts = Lambda.queryListIfExist(nameMap.keySet(), metricsDictDAO.getBaseMapper()::selectBatchIds);

        Map<Long, Integer> meSortMap = Lambda.buildMap(metricsDicts, MetricsDict::getId, MetricsDict::getSort);
        int metricsSort = ApplicationSqlUtil.metricsSort;
        for (CustomFieldInfo field : customFields) {
            ApplicationDataDto.Header newHeader = new ApplicationDataDto.Header(field.getId(), field.getTableId(),
                    Optional.ofNullable(nameMap.get(field.getId())).orElse(field.getFieldName()),
                    Optional.ofNullable(nameMap.get(field.getId())).orElse(field.getFieldName()),
                    CkPgJavaDataType.Int32.name(), field.isPrimaryKey(), "y");
            newHeader.setSort(metricsSort + meSortMap.getOrDefault(field.getId(), ApplicationSqlUtil.metricsSort));
            header.add(newHeader);
        }

        return header;
    }

    /**
     * fieldName 是 select 中的字段名
     * alias 是 select 的 alias
     * realName 是申请时输入的内容
     *
     * @see ApplicationServiceImpl#buildHeaders(UserDataAssets)
     */
    private List<ApplicationDataDto.Header> buildHeaders(TgApplicationInfo applyInfo) {
        List<ApplicationDataDto.Header> allHeader = new ArrayList<>();

        FieldMetaBO fieldMeta = this.getApplicationFieldMeta(applyInfo.getId());

        List<TableFieldInfo> fieldList = fieldMeta.getTableFields();
        List<CustomFieldInfo> customFields = fieldMeta.getCustomFields();


        TgTemplateInfo template = JsonBeanConverter.convert2Obj(new TgTemplateInfo().selectById(applyInfo.getTemplateId()));
        // 日期聚合
        if (CollectionUtils.isNotEmpty(template.getApplicationPeriodField())) {
            // period_str 特殊处理 禁用了等于，防止通过id去查列数据（真实列才可行）
            if (CollectionUtils.isNotEmpty(applyInfo.getColsInfo())) {
                List<FieldDict> periodList = fieldDictDAO.getBaseMapper().selectList(new QueryWrapper<FieldDict>().lambda()
                        .in(FieldDict::getFieldName, ApplicationConst.PeriodField.idToNameMap.values()));
                Map<String, Integer> periodMap = Lambda.buildMap(periodList, FieldDict::getFieldName, FieldDict::getSort);

                List<Long> selectPeriod = applyInfo.getColsInfo().stream().flatMap(v -> v.getSelect().stream())
                        .filter(ApplicationConst.PeriodField.idToNameMap::containsKey).collect(Collectors.toList());

                if (!selectPeriod.isEmpty() && selectPeriod.size() != ApplicationConst.PeriodField.idToNameMap.size()) {
                    for (Long id : selectPeriod) {
                        ApplicationConst.PeriodFieldEnum periodFieldEnum = ApplicationConst.PeriodFieldEnum.of(id);
                        ApplicationDataDto.Header newHeader = new ApplicationDataDto.Header(periodFieldEnum.getId(), periodFieldEnum.getName(),
                                periodFieldEnum.getAlias(), periodFieldEnum.getViewType());
                        Integer sort = periodMap.getOrDefault(periodFieldEnum.getName(), id.intValue());
                        newHeader.setSort(sort);
                        allHeader.add(newHeader);
                    }
                } else {
                    ApplicationConst.PeriodField.idToNameMap.forEach((id, v) -> {
                                ApplicationConst.PeriodFieldEnum periodFieldEnum = ApplicationConst.PeriodFieldEnum.of(id);
                                ApplicationDataDto.Header newHeader = new ApplicationDataDto.Header(periodFieldEnum.getId(),
                                        periodFieldEnum.getName(), periodFieldEnum.getAlias(), periodFieldEnum.getViewType());
                                Integer sort = periodMap.getOrDefault(periodFieldEnum.getName(), id.intValue());
                                newHeader.setSort(sort);
                                allHeader.add(newHeader);
                            }
                    );
                }
            }
        }

        Set<Long> fieldIds = Lambda.buildSet(fieldList, TableFieldInfo::getRelationColId);
        Map<Long, FieldDict> fieldDictMap = Lambda.queryMapIfExist(fieldIds, fieldDictDAO::listByIds, FieldDict::getId);
        int joinSort = ApplicationSqlUtil.joinSort;
        for (TableFieldInfo field : fieldList) {
            ApplicationDataDto.Header newHeader = new ApplicationDataDto.Header(field.getId(), field.getTableId(),
                    field.getFieldName(), StringUtils.isNotBlank(field.getRealName()) ? field.getRealName() : field.getFieldAlias(),
                    field.getDataType(), field.isPrimaryKey(), field.getDefaultShow());
            newHeader.setSort(Optional.ofNullable(fieldDictMap.get(field.getRelationColId())).map(FieldDict::getSort).orElse(joinSort++));
            allHeader.add(newHeader);
        }

        Map<Long, String> nameMap = Lambda.buildMap(applyInfo.getCustomMetrics(), CustomMetricsLabelDto::getMetricsId,
                CustomMetricsLabelDto::getAlias);
        List<MetricsDict> metricsDicts = Lambda.queryListIfExist(nameMap.keySet(), metricsDictDAO.getBaseMapper()::selectBatchIds);

        Map<Long, Integer> meSortMap = Lambda.buildMap(metricsDicts, MetricsDict::getId, MetricsDict::getSort);
        int metricsSort = ApplicationSqlUtil.metricsSort;
        for (CustomFieldInfo field : customFields) {
            ApplicationDataDto.Header newHeader = new ApplicationDataDto.Header(field.getId(), field.getTableId(),
                    Optional.ofNullable(nameMap.get(field.getId())).orElse(field.getFieldName()),
                    Optional.ofNullable(nameMap.get(field.getId())).orElse(field.getFieldName()),
                    CkPgJavaDataType.Int32.name(), field.isPrimaryKey(), "y");
            newHeader.setSort(metricsSort + meSortMap.getOrDefault(field.getId(), ApplicationSqlUtil.metricsSort));
            allHeader.add(newHeader);
        }

        List<ColumnSetting> settings = applicationColumnSettingDAO.getByApplicationId(applyInfo.getId());
        if (CollectionUtils.isNotEmpty(settings)) {
            Map<String, ColumnSetting> setMap = Lambda.buildMap(settings, ColumnSetting::getFiledName, v -> v);
            for (ApplicationDataDto.Header header : allHeader) {
                ColumnSetting set = setMap.get(header.getFiledName());
                if (Objects.isNull(set)) {
                    log.warn("NOT FOUND: alias={}", header.getFiledName());
                    continue;
                }
                header.setSort(set.getSort());
                header.setCustomName(set.getCustomName());
                header.setDefaultShow(set.getDefaultShow());
            }
        }

        return allHeader;
    }

    @Override
    public String handleWhereSql(GetDataInfoRequestDTO requestDTO) {
        if (requestDTO == null || requestDTO.getFilter() == null) {
            return "";
        }

        final FilterDTO filter = requestDTO.getFilter();
        // 构建 where 语句

        Table table = new Table();
        table.setUniqueId(1L);
        table.setFactTable(true);
        Filter targetFilter = new Filter();
        ApplicationSqlUtil.convertToFilter(filter, targetFilter);

        final ClickHouse clickHouse = new ClickHouse(Collections.singletonList(table), targetFilter);
        String whereSql = clickHouse.getWhereSql();
        whereSql = whereSql.replace("t_1.", "").replace("WHERE", "");

        return whereSql;
    }

    @Override
    public List<TgApplicationInfo> queryByIds(List<Long> applicationIds) {
        if (CollectionUtils.isEmpty(applicationIds)) {
            return Lists.newArrayList();
        }
        return mapper.selectBatchIds(applicationIds);
    }

    private String[] rebuildSelect(TgApplicationInfo originApplication, TgApplicationInfo copyApplicationInfo,
                                   GetDataInfoRequestDTO queryCriteria) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(queryCriteria.getFieldIds())) {
            List<Long> fieldIds = StringUtils.toList(queryCriteria.getFieldIds(), Long::valueOf);

            this.filterSelectedField(copyApplicationInfo, fieldIds);

            FieldMetaBO fieldMeta = getApplicationFieldMeta(originApplication.getId());
            List<TableFieldInfo> tableFieldInfos = fieldMeta.getTableFields();
            Map<Long, TableFieldInfo> tableFieldInfoMap = tableFieldInfos.stream()
                    .collect(Collectors.toMap(TableFieldInfo::getId, Function.identity()));
            List<CustomFieldInfo> customFieldInfos = fieldMeta.getCustomFields();
            Map<Long, CustomFieldInfo> customFieldInfoMap = customFieldInfos.stream()
                    .collect(Collectors.toMap(CustomFieldInfo::getId, Function.identity()));

            Map<Long, String> applyNameMap = new HashMap<>();
            if (Objects.nonNull(copyApplicationInfo.getTemplateMetrics())
                    && CollectionUtils.isNotEmpty(copyApplicationInfo.getTemplateMetrics().getRealName())) {
                for (RealName realName : copyApplicationInfo.getTemplateMetrics().getRealName()) {
                    applyNameMap.put(realName.getId(), realName.getRealName());
                }
            }
            String[] fields = fieldIds.stream().map(fieldId -> {
                if (tableFieldInfoMap.containsKey(fieldId)) {
                    return tableFieldInfoMap.get(fieldId).getFieldName();
                }
                if (customFieldInfoMap.containsKey(fieldId)) {
                    String fieldAlias = applyNameMap.get(fieldId);
//                        String fieldAlias = customFieldInfoMap.get(fieldId).getFieldAlias();
                    fieldAlias = ApplicationSqlUtil.trimMetricSuffix(fieldAlias);
                    return "`" + fieldAlias + "`";
                }
                String name = ApplicationConst.PeriodField.idToNameMap.get(fieldId);
                if (StringUtils.isNoneBlank(name)) {
                    return name;
                }
                return null;
            }).filter(Objects::nonNull).toArray(String[]::new);

            if (fields.length != 0) {
                return fields;
            }
        }
        return null;
    }

    /**
     * 过滤出选择的原始字段 自定义字段
     */
    private void filterSelectedField(TgApplicationInfo copyApplicationInfo, List<Long> fieldIds) {
        List<ColsInfoDto> colsInfo = copyApplicationInfo.getColsInfo();
        if (CollectionUtils.isNotEmpty(colsInfo)) {
            colsInfo.forEach(v -> v.getSelect().retainAll(fieldIds));

            for (Long fieldId : fieldIds) {
                if (ApplicationConst.PeriodField.idToNameMap.containsKey(fieldId)) {
                    colsInfo.get(0).getSelect().add(fieldId);
                }
            }
        }
        if (Objects.nonNull(copyApplicationInfo.getTemplateMetrics())
                && CollectionUtils.isNotEmpty(copyApplicationInfo.getTemplateMetrics().getSelect())) {
            copyApplicationInfo.getTemplateMetrics().getSelect().retainAll(fieldIds);
        }

        copyApplicationInfo.setColsInfo(colsInfo);

        List<CustomFieldInfo> cus = customFieldInfoMapper.selectList(new QueryWrapper<CustomFieldInfo>().lambda()
                .in(CustomFieldInfo::getId, fieldIds));
        Set<String> alias = cus.stream().map(CustomFieldInfo::getFieldAlias).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(copyApplicationInfo.getMetricsInfo())) {
            copyApplicationInfo.getMetricsInfo().removeIf(v -> !alias.contains(v.getAliasName()));
        }
    }

    @Override
    public FieldMetaBO queryAssetsFieldMeta(UserDataAssets dataAssets, Set<Long> additionFields) {
        List<AuthTableFieldDTO> fields = ckClusterAdapter.mixMetaFields(dataAssets.getAssetTableName());
//        List<AuthTableFieldDTO> fields = dataClickhouseMapper.getFields(selfCKProperties.getDatabase(),
//                dataAssets.getAssetTableName());
        for (AuthTableFieldDTO field : fields) {
            field.setDataType(ApplicationSqlUtil.trimLengthAndType(field.getDataType()));
        }

        Long srcApplicationId = dataAssets.getSrcApplicationId();
        TgApplicationInfo apply = TgApplicationInfo.newInstance().selectById(srcApplicationId);
        Set<Long> fieldIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(additionFields)) {
            fieldIds.addAll(additionFields);
        }
        JsonBeanConverter.convert2Obj(apply);
        List<ApplicationGranularityDto> granularity = apply.getGranularity();
        granularity.stream()
                .filter(v -> CollectionUtils.isNotEmpty(v.getFields()))
                .flatMap(v -> v.getFields().stream()).map(SelectFieldDto::getFieldId).forEach(fieldIds::add);

        Map<String, Integer> fieldSortMap;
        Map<String, String> fieldNameMap;
        Map<String, Long> fieldIdMap;
        if (CollectionUtils.isNotEmpty(fieldIds)) {
            List<FieldDict> fieldList = fieldDictDAO.listByIds(fieldIds);
            fieldNameMap = Lambda.buildMap(fieldList, FieldDict::getFieldName, FieldDict::getName);
            fieldIdMap = Lambda.buildMap(fieldList, FieldDict::getFieldName, FieldDict::getId);
            fieldSortMap = Lambda.buildMap(fieldList, FieldDict::getFieldName, FieldDict::getSort);
        } else {
            fieldNameMap = new HashMap<>();
            fieldIdMap = new HashMap<>();
            fieldSortMap = new HashMap<>();
        }

        int metricsSort = ApplicationSqlUtil.metricsSort;
        List<CustomMetricsLabelDto> metrics = apply.getCustomMetrics();
        if (CollectionUtils.isNotEmpty(metrics)) {
            Map<Long, String> metricNameMap = metrics.stream()
                    .filter(v -> BooleanUtils.isTrue(v.getSelect()))
                    .collect(Collectors.toMap(CustomMetricsLabelDto::getMetricsId,
                            CustomMetricsLabelDto::getAlias, (front, current) -> current));

            if (MapUtils.isNotEmpty(metricNameMap)) {
                List<MetricsDict> metricsDicts = metricsDictDAO.listByIds(metricNameMap.keySet());
                Map<Long, MetricsDict> originMap = metricsDicts.stream().collect(Collectors.toMap(MetricsDict::getId,
                        v -> v, (front, current) -> current));
                for (Map.Entry<Long, String> entry : metricNameMap.entrySet()) {
                    Long id = entry.getKey();
                    originMap.get(id);
                    MetricsDict dict = originMap.get(id);
                    fieldNameMap.put(dict.getFieldName(), firstNotBlankStr(entry.getValue(), dict.getName()));
                    fieldIdMap.put(dict.getFieldName(), dict.getId());
                    fieldSortMap.put(dict.getFieldName(), metricsSort + dict.getSort());
                }
            }
        }
        AtomicInteger joinSort = new AtomicInteger(ApplicationSqlUtil.joinSort);
        List<TableFieldInfo> fieldInfos = fields.stream().map(v -> {
            TableFieldInfo field = new TableFieldInfo();
            String finalName = firstNotBlankStr(fieldNameMap.get(v.getFieldName()), v.getFieldAlias());
            field.setFieldAlias(finalName);
            field.setRealName(finalName);
            field.setComment(finalName);
            field.setFieldName(v.getFieldName());
            field.setDataType(v.getDataType());
            field.setId(fieldIdMap.get(v.getFieldName()));
            field.setSort(fieldSortMap.getOrDefault(v.getFieldName(), joinSort.incrementAndGet()));
            return field;
        }).collect(Collectors.toList());
        return new FieldMetaBO(fieldInfos, Collections.emptyList());
    }

    public FieldMetaBO getApplicationFieldMeta(TgApplicationInfo appli) {
        TgApplicationInfo application = JsonBeanConverter.convert2Obj(appli);
        List<ColsInfoDto> colsInfo = application.getColsInfo();
        List<Long> ids = new ArrayList<>(64);
        List<RealName> realNames = new ArrayList<>(64);

        // 添加字段别名信息
        colsInfo.forEach((x) -> {
            ids.addAll(Optional.ofNullable(x.getSelect()).orElse(Collections.emptyList()));
            realNames.addAll(Optional.ofNullable(x.getRealName()).orElse(Collections.emptyList()));
        });

        List<SelectFieldDto> fields = application.getGranularity().stream()
                .filter(v -> CollectionUtils.isNotEmpty(v.getFields()))
                .flatMap(v -> v.getFields().stream())
                .collect(Collectors.toList());
        TgTemplateInfo template = JsonBeanConverter.convert2Obj(new TgTemplateInfo().selectById(application.getTemplateId()));
        //移除原始的日期字段
        Long periodId = CollectionUtils.isNotEmpty(template.getApplicationPeriodField()) ? template.getApplicationPeriodField().get(0) : null;

        List<Long> fieldDicts = fields.stream().map(SelectFieldDto::getFieldId)
                .filter(v -> !Objects.equals(periodId, v))
                .collect(Collectors.toList());
        Map<Long, String> aliasMap = Lambda.buildMap(fields, SelectFieldDto::getFieldId, SelectFieldDto::getAlias);
        Set<Long> depTableIds = new HashSet<>();
        depTableIds.addAll(application.calcTableIds());
        depTableIds.addAll(template.calcTableIds());

        if (CollectionUtils.isEmpty(fieldDicts)) {
            throw new CustomException("至少选择一个维度列");
        }
        List<TableFieldInfo> fieldInfos = tableFieldInfoService.getBaseMapper().selectList(
                new QueryWrapper<TableFieldInfo>().lambda()
                        .in(TableFieldInfo::getTableId, depTableIds)
                        .in(TableFieldInfo::getRelationColId, fieldDicts));
        Map<Long, TableFieldInfo> selectFieldMap = fieldInfos.stream()
                .collect(Collectors.toMap(TableFieldInfo::getRelationColId, v -> v, (front, current) -> current));

        Map<Long, RealName> realNameMap = realNames.stream()
                .collect(Collectors.toMap(RealName::getId, v -> v, (front, current) -> current));

        Map<Long, TableInfo> tableMap = new HashMap<>();
        // 添加表名信息
        selectFieldMap.values().forEach(x -> {
            // 申请中字段JSON的别名 覆盖原表字段名, 缺省使用别名
            x.setRealName(StrUtil.firstNotBlankStr(aliasMap.get(x.getRelationColId()),
                    Optional.ofNullable(realNameMap.get(x.getId())).map(RealName::getRealName).orElse(x.getComment()))
            );
            Long tableId = x.getTableId();
            TableInfo table = tableMap.computeIfAbsent(tableId, tableInfoService::getById);
            x.setTableName(table.getTableName());
            String alias = application.getTableAliasMapping().get(table.getTableNameDistributed());
            if (alias != null) {
                x.setFieldName(alias + "_" + x.getFieldName());
            }
        });

        AtomicInteger joinSort = new AtomicInteger(ApplicationSqlUtil.joinSort);
        List<TableFieldInfo> joinFields = new ArrayList<>();
        // 申请时添加的关联表字段
        for (ColsInfoDto colsInfoDto : application.getColsInfo()) {
            if (Objects.equals(colsInfoDto.getIsItself(), CommonConstants.APPLICATION)) {

                List<TableFieldInfo> tmpFields = colsInfoDto.getRealName().stream().map(v -> {
                    TableFieldInfo info = new TableFieldInfo();
                    info.setId(v.getId());
                    info.setFieldName(v.getFieldName());
                    info.setDataType(v.getDataType());
                    info.setTableId(v.getTableId());

                    // 申请中字段JSON的别名 覆盖原表字段名, 缺省使用别名
                    info.setRealName(StrUtil.firstNotBlankStr(aliasMap.get(v.getRelationColId()),
                            Optional.ofNullable(realNameMap.get(v.getId())).map(RealName::getRealName).orElse(v.getComment()))
                    );
                    TableInfoManageDto table = tableInfoService.getDetail(v.getTableId());
                    info.setTableName(table.getTableName());

                    String alias = application.getTableAliasMapping().get(table.getTableNameDistributed());
                    if (alias != null) {
                        info.setFieldName(alias + "_" + info.getFieldName());
                    }
                    info.setSort(joinSort.incrementAndGet());
                    return info;
                }).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(tmpFields)) {
                    joinFields.addAll(tmpFields);
                }
            }
        }

        // 申请时添加的指标信息
        List<CustomFieldInfo> customFields = new ArrayList<>();
        long applicationUuid = getUuid(CommonConstants.APPLICATION + application.getCreateTime());
        List<CustomFieldInfo> customApplyFields = customFieldInfoMapper.selectCustomFields(applicationUuid, CommonConstants.APPLICATION);
        if (CollectionUtils.isNotEmpty(customApplyFields)) {
            for (CustomFieldInfo customApplyField : customApplyFields) {
                customApplyField.setFieldName(customApplyField.getFieldAlias());
                if (ids.contains(customApplyField.getId())) {
                    customFields.add(customApplyField);
                } else if (CollectionUtils.isNotEmpty(application.getMetricsInfo())) {
                    boolean applyMetric = application.getMetricsInfo().stream()
                            .anyMatch(v -> Objects.equals(v.getAliasName(), customApplyField.getFieldAlias()));
                    if (applyMetric) {
                        // 因为默认名字为自动计算的名字
                        customApplyField.setFieldName(customApplyField.getFieldName());
                        customFields.add(customApplyField);
                    }
                }
            }
        }

        // 申请中选用的模板指标
        List<CustomMetricsLabelDto> metrics = application.getCustomMetrics();
        if (CollectionUtils.isNotEmpty(metrics)) {
            Set<Long> metricsIds = metrics.stream().map(CustomMetricsLabelDto::getMetricsId).collect(Collectors.toSet());
            List<MetricsDict> dicts = metricsDictDAO.listByIds(metricsIds);
            Map<Long, String> dictMap = Lambda.buildMap(dicts, MetricsDict::getId, MetricsDict::getName);

            for (CustomMetricsLabelDto metric : metrics) {
                if (BooleanUtils.isNotTrue(metric.getSelect())) {
                    continue;
                }
                CustomFieldInfo field = new CustomFieldInfo();
                field.setId(metric.getMetricsId());
//                field.setFieldName(dictMap.get(metric.getMetricsId()));
                field.setFieldName(StringUtils.isBlank(metric.getAlias()) ? dictMap.get(metric.getMetricsId()) : metric.getAlias());
                field.setFieldAlias(metric.getAlias());
                field.setRealName(metric.getAlias());
                field.setComment(metric.getAlias());
                field.setFieldSource(ApplicationConst.FieldSource.METRICS);

                field.setDataType(CkPgJavaDataType.Decimal.toString());
                customFields.add(field);
            }
        }


        ArrayList<TableFieldInfo> tableFields = new ArrayList<>(selectFieldMap.values());
        if (CollectionUtils.isNotEmpty(joinFields)) {
            tableFields.addAll(joinFields);
        }
        return new FieldMetaBO(application, tableFields, customFields);
    }

    @Override
    public FieldMetaBO getApplicationFieldMeta(Long applyId) {
        TgApplicationInfo application = JsonBeanConverter.convert2Obj(new TgApplicationInfo().selectById(applyId));
        List<ColsInfoDto> colsInfo = application.getColsInfo();
        List<Long> ids = new ArrayList<>(64);
        List<RealName> realNames = new ArrayList<>(64);

        // 添加字段别名信息
        colsInfo.forEach((x) -> {
            ids.addAll(Optional.ofNullable(x.getSelect()).orElse(Collections.emptyList()));
            realNames.addAll(Optional.ofNullable(x.getRealName()).orElse(Collections.emptyList()));
        });

        List<SelectFieldDto> fields = application.getGranularity().stream()
                .filter(v -> CollectionUtils.isNotEmpty(v.getFields()))
                .flatMap(v -> v.getFields().stream())
                .collect(Collectors.toList());
        TgTemplateInfo template = JsonBeanConverter.convert2Obj(new TgTemplateInfo().selectById(application.getTemplateId()));
        //移除原始的日期字段
        Long periodId = CollectionUtils.isNotEmpty(template.getApplicationPeriodField()) ? template.getApplicationPeriodField().get(0) : null;

        List<Long> fieldDicts = fields.stream().map(SelectFieldDto::getFieldId)
                .filter(v -> !Objects.equals(periodId, v))
                .collect(Collectors.toList());
        Map<Long, String> aliasMap = Lambda.buildMap(fields, SelectFieldDto::getFieldId, SelectFieldDto::getAlias);
        Set<Long> depTableIds = new HashSet<>();
        depTableIds.addAll(application.calcTableIds());
        depTableIds.addAll(template.calcTableIds());
        List<TableFieldInfo> fieldInfos;
        if (CollectionUtils.isNotEmpty(depTableIds) && CollectionUtils.isNotEmpty(fieldDicts)) {
//            tableFieldInfoService.lambdaQuery()
//
//                    .list();
            fieldInfos = tableFieldInfoService.getBaseMapper().selectList(new QueryWrapper<TableFieldInfo>().lambda()
                    .in(TableFieldInfo::getTableId, depTableIds)
                    .in(TableFieldInfo::getRelationColId, fieldDicts));
        } else {
            fieldInfos = Collections.emptyList();
        }
        Map<Long, TableFieldInfo> selectFieldMap = fieldInfos.stream()
                .collect(Collectors.toMap(TableFieldInfo::getRelationColId, v -> v, (front, current) -> current));

        Map<Long, RealName> realNameMap = realNames.stream()
                .collect(Collectors.toMap(RealName::getId, v -> v, (front, current) -> current));

        Map<Long, TableInfo> tableMap = new HashMap<>();
        // 添加表名信息
        selectFieldMap.values().forEach(x -> {
            // 申请中字段JSON的别名 覆盖原表字段名, 缺省使用别名
            x.setRealName(StrUtil.firstNotBlankStr(aliasMap.get(x.getRelationColId()),
                    Optional.ofNullable(realNameMap.get(x.getId())).map(RealName::getRealName).orElse(x.getComment()))
            );
            Long tableId = x.getTableId();
            TableInfo table = tableMap.computeIfAbsent(tableId, tableInfoService::getById);
            x.setTableName(table.getTableName());
            String alias = application.getTableAliasMapping().get(table.getTableNameDistributed());
            if (alias != null) {
                x.setFieldName(alias + "_" + x.getFieldName());
            }
        });

        AtomicInteger joinSort = new AtomicInteger(ApplicationSqlUtil.joinSort);
        List<TableFieldInfo> joinFields = new ArrayList<>();
        // 申请时添加的关联表字段
        for (ColsInfoDto colsInfoDto : application.getColsInfo()) {
            if (Objects.equals(colsInfoDto.getIsItself(), CommonConstants.APPLICATION)) {

                List<TableFieldInfo> tmpFields = colsInfoDto.getRealName().stream().map(v -> {
                    TableFieldInfo info = new TableFieldInfo();
                    info.setId(v.getId());
                    info.setFieldName(v.getFieldName());
                    info.setDataType(v.getDataType());
                    info.setTableId(v.getTableId());

                    // 申请中字段JSON的别名 覆盖原表字段名, 缺省使用别名
                    info.setRealName(StrUtil.firstNotBlankStr(aliasMap.get(v.getRelationColId()),
                            Optional.ofNullable(realNameMap.get(v.getId())).map(RealName::getRealName).orElse(v.getComment()))
                    );
                    TableInfoManageDto table = tableInfoService.getDetail(v.getTableId());
                    info.setTableName(table.getTableName());

                    String alias = application.getTableAliasMapping().get(table.getTableNameDistributed());
                    if (alias != null) {
                        info.setFieldName(alias + "_" + info.getFieldName());
                    }
                    info.setSort(joinSort.incrementAndGet());
                    return info;
                }).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(tmpFields)) {
                    joinFields.addAll(tmpFields);
                }
            }
        }

        // 申请时添加的指标信息
        List<CustomFieldInfo> customFields = new ArrayList<>();
        long applicationUuid = getUuid(CommonConstants.APPLICATION + application.getCreateTime());
        List<CustomFieldInfo> customApplyFields = customFieldInfoMapper.selectCustomFields(applicationUuid, CommonConstants.APPLICATION);
        if (CollectionUtils.isNotEmpty(customApplyFields)) {
            for (CustomFieldInfo customApplyField : customApplyFields) {
                customApplyField.setFieldName(customApplyField.getFieldAlias());
                if (ids.contains(customApplyField.getId())) {
                    customFields.add(customApplyField);
                } else if (CollectionUtils.isNotEmpty(application.getMetricsInfo())) {
                    boolean applyMetric = application.getMetricsInfo().stream()
                            .anyMatch(v -> Objects.equals(v.getAliasName(), customApplyField.getFieldAlias()));
                    if (applyMetric) {
                        // 因为默认名字为自动计算的名字
                        customApplyField.setFieldName(customApplyField.getFieldName());
                        customFields.add(customApplyField);
                    }
                }
            }
        }

        // 申请中选用的模板指标
        List<CustomMetricsLabelDto> metrics = application.getCustomMetrics();
        if (CollectionUtils.isNotEmpty(metrics)) {
            Set<Long> metricsIds = metrics.stream().map(CustomMetricsLabelDto::getMetricsId).collect(Collectors.toSet());
            List<MetricsDict> dicts = metricsDictDAO.listByIds(metricsIds);
            Map<Long, String> dictMap = Lambda.buildMap(dicts, MetricsDict::getId, MetricsDict::getName);

            for (CustomMetricsLabelDto metric : metrics) {
                if (BooleanUtils.isNotTrue(metric.getSelect())) {
                    continue;
                }
                CustomFieldInfo field = new CustomFieldInfo();
                field.setId(metric.getMetricsId());
                field.setFieldName(StringUtils.isBlank(metric.getAlias()) ? dictMap.get(metric.getMetricsId()) : metric.getAlias());
                field.setFieldAlias(metric.getAlias());
                field.setRealName(metric.getAlias());
                field.setComment(metric.getAlias());
                field.setFieldSource(ApplicationConst.FieldSource.METRICS);

                field.setDataType(CkPgJavaDataType.Decimal.toString());
                customFields.add(field);
            }
        }


        ArrayList<TableFieldInfo> tableFields = new ArrayList<>(selectFieldMap.values());
        if (CollectionUtils.isNotEmpty(joinFields)) {
            tableFields.addAll(joinFields);
        }
        return new FieldMetaBO(application, tableFields, customFields);
    }

    @Override
    public AjaxResult<Map<String, Object>> getAssetsBasicInfo(Long assetsId, Integer version) {
        UserDataAssets assets = userDataAssetsDAO.getById(assetsId);
        if (Objects.isNull(assets)) {
            return AjaxResult.error(InfoConstants.REFRESH_PAGE);
        }

        // 使用快照版本
        if (Objects.nonNull(version)) {
            UserDataAssetsSnapshot snapshot = userDataAssetsSnapshotDAO.queryByAssetsId(assetsId, version);
            if (Objects.nonNull(snapshot)) {
                log.info("use snapshot: version={}", version);
                assets = snapshot;
            }
        }

        List<Integer> actions = assetsAdapter.buildActions(assets);
        TgApplicationInfo apply = TgApplicationInfo.newInstance().selectById(assets.getSrcApplicationId());
        JsonBeanConverter.convert2Obj(apply);

        FieldMetaBO meta = this.queryAssetsFieldMeta(assets, null);
        TableInfo info = tableInfoService.getById(apply.getBaseTableId());
        // 主项目
        TgTemplateInfo template = templateInfoMapper.selectById(apply.getTemplateId());

        Map<String, ApplicationDataUpdateRecord> latestMap = dataUpdateRecordDAO.queryLatestByAssetVersions(Collections
                .singletonList(assets.getAssetsVersion()));

        final List<ColumnSetting> columnSettings = applicationColumnSettingDAO.getByApplicationId(apply.getId());

        List<ProjectDataAssetsRelate> projectList = projectDataAssetsRelateMapper.selectList(new QueryWrapper<ProjectDataAssetsRelate>().lambda()
                .eq(ProjectDataAssetsRelate::getUserAssetId, assetsId));
        List<Long> proIds = Lambda.buildNonNullList(projectList, ProjectDataAssetsRelate::getProjectId);
        List<Project> proList = Lambda.queryListIfExist(proIds, projectMapper::selectBatchIds);
        List<Long> customIds = Lambda.buildNonNullList(proList, Project::getCustomerId);
        List<Customer> customers = Lambda.queryListIfExist(customIds, customerMapper::selectBatchIds);

        UserDataAssets finalAssets = assets;
        return AjaxResult.success(new HashMap<String, Object>() {{
            put("applicationNo", apply.getApplicationNo());
            put("applicantName", apply.getApplicantName());
            put("projectName", finalAssets.getProjectName());
            put("newProjectName", proList.stream().map(Project::getName).collect(Collectors.joining("、")));
            put("customerName", customers.stream().map(Customer::getShortName).collect(Collectors.joining("、")));
            put("bizType", BizTypeEnum.getDesc(template.getBizType()));
            put("applyDesc", apply.getApplyDesc());
            put("exportProjectName", apply.getExportProjectName());
            put("requireTimeType", ApplicationConst.RequireTimeTypeEnum.DESC_MAP.get(apply.getRequireTimeType()) + "需求");
            put("requireAttr", Optional.ofNullable(RequireAttrType.DESC_MAP.get(apply.getRequireAttr())).orElse(""));
            put("dataExpir", apply.getDataExpir());
            if (CollUtil.isNotEmpty(columnSettings)) {
                put("columnSetting", columnSettings.stream().sorted(Comparator.comparing(ColumnSetting::getSort)).collect(Collectors.toList()));
            }
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            put("applyPassedTime", Optional.ofNullable(apply.getApplyPassedTime()).orElse("-"));
            put("applyLastUpdateTime", Optional.ofNullable(finalAssets.getUpdateTime())
                    .map(v -> Date.from(v.atZone(ZoneId.systemDefault()).toInstant()))
                    .map(fmt::format).orElse("-"));
            put("dataCount", finalAssets.getDataTotal());
            put("prodCode", finalAssets.getProdCode());

            put("fieldsCount", meta.mergeFieldsWithPeriod().size());
            put("tableAlias", Optional.ofNullable(info).map(TableInfo::getTableAlias).orElse(""));
            put("applicantId", apply.getApplicantId());
            Long templateId = apply.getTemplateId();
            String templateName = Optional.ofNullable(templateId).map(v -> new TgTemplateInfo().selectById(v))
                    .map(TgTemplateInfo::getTemplateName).orElse("");
            put("templateName", templateName);
            // 数据同步到本地BI数据库的动作状态
            put("updateState", Optional.ofNullable(latestMap.get(finalAssets.getAssetsVersion()))
                    .map(ApplicationDataUpdateRecord::getUpdateState).orElse(UpdateRecordStateType.NONE));
            put("copy", Objects.nonNull(finalAssets.getCopyFromId()));
            put("status", finalAssets.getStatus());
            put("ftpStatus", finalAssets.getFtpStatus());
            put("actions", actions);
            put("configType", apply.getConfigType());


            // 资产门户所需字段
            if (apply.getNewAssetId() != null) {
                TgAssetInfo tgAssetInfo = TgAssetInfo.newInstance().selectById(apply.getNewAssetId());
                put("assetId", tgAssetInfo.getId());
                put("processId", apply.getProcessId());
                put("relatedId", tgAssetInfo.getRelatedId());
            }
        }});
    }

    @Override
    public List<Map<String, Object>> getTopResources(Map<String, Object> parameterMap) {
        return mapper.getTopResources(parameterMap);
    }

    @Override
    public List<TgApplicationInfo> listAllNormalDataApplications(List<Long> assetIds) {
        return mapper.listAllNormalDataApplications(assetIds);
    }

    @Override
    public Object updateStatus(Long assetsId) {
        UserDataAssets assets = new UserDataAssets().selectById(assetsId);

        int status = assets.getStatus().equals(ApplicationConst.ApplyStatus.ENABLE) ?
                ApplicationConst.ApplyStatus.DISABLE : ApplicationConst.ApplyStatus.ENABLE;
        assets.setStatus(status);
        // 联动禁用授权的客户资产
        if (this.isUpdateSuccess(assets)) {
            return AjaxResult.success(assets);
        }

        return AjaxResult.error("状态变更失败");
    }

    private boolean isUpdateSuccess(UserDataAssets assets) {
        customerAuthDAO.updateStatus(assets.getId(), assets.getStatus());
        return assets.updateById();
    }

    @Override
    public Object getSearchTableSource() {
        return mapper.querySearchTableSource(ThreadContextHolder.getSysUser().getUserId());
    }

    @Override
    public List<TableInfoSearchVO> getSearchTableAlias() {
//        List<Long> tableIds = mapper.queryDistinctTableIdByApplicantId(ThreadContextHolder.getSysUser().getUserId(),
//                ApplicationConst.ApplicationType.DATA_APPLICATION);
//        if (CollectionUtils.isEmpty(tableIds)) {
//            return Collections.emptyList();
//        }

        List<TableInfo> infos = tableInfoService.getBaseMapper().selectList(new QueryWrapper<TableInfo>()
                .lambda().eq(TableInfo::getStatus, 1));

        return infos.stream().map(v -> {
            TableInfoSearchVO vo = new TableInfoSearchVO();
            BeanUtils.copyProperties(v, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public String getReadableUsers(String readableUserIds) {
        if (StringUtils.isBlank(readableUserIds)) {
            return "";
        }

        String cacheKey = "readable_user_names:" + readableUserIds;
        Object cache = userCache.get(cacheKey);
        if (Objects.isNull(cache)) {
//        }
//        if (Boolean.FALSE.equals(redisTemplate.hasKey(cacheKey))) {
            StringBuilder sb4User = new StringBuilder();
            StringBuilder sb4Customer = new StringBuilder();
            List<Long> userIds = Arrays.stream(readableUserIds.split(","))
                    .map(Long::parseLong).distinct().collect(Collectors.toList());
            List<SysUser> users = userService.selectUserByIds(userIds);

            for (SysUser user : users) {
                Integer type = user.getUserInfoType();
                if (Objects.equals(type, CommonConstants.INNER_UESR)) {
                    Optional<SinoPassUserDTO> orgUserInfo = Optional.ofNullable(SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId()));
                    orgUserInfo.ifPresent(u -> {
                        sb4User.append(orgUserInfo.get().getViewName());
                        sb4User.append("\n");
                    });
                }

                if (Objects.equals(type, CommonConstants.CUSTOMER_UESR)) {
                    Optional<SysCustomer> sysCustomerOpt = Optional.ofNullable(sysCustomerService.getByUserId(user.getUserId()));
                    sysCustomerOpt.ifPresent(c -> sb4Customer.append("客户 - ").append(sysCustomerOpt.get().getFullName()).append("\n"));
                }
            }

            String val = "".contentEquals(sb4Customer) ? sb4User.toString() : sb4User + "\n" + sb4Customer;
//            redisTemplate.opsForValue().set(cacheKey, val, 5, TimeUnit.DAYS);
            userCache.put(cacheKey, val);
            return val;
        } else {
//            return (String) redisTemplate.opsForValue().get(cacheKey);
            return (String) cache;
        }
    }

    @Override
    public MetaDataFieldInfo getMetaDataFieldInfo(String id) {
        if (ObjectUtils.isNull(fieldMap.get(Long.valueOf(id)))) {
            rebuildFieldCacheMap();
        }
        return fieldMap.get(Long.valueOf(id));
    }

    /**
     * 撤销提数申请
     */
    @Override
    public void withdrawApplication(Long userId, Long applicationId) {
        mapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                .eq(TgApplicationInfo::getId, applicationId)
                .eq(TgApplicationInfo::getApplicantId, userId)
                .set(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.WITHDRAW_APPLICATION)
                .set(TgApplicationInfo::getStatus, ApplicationConst.ApplyStatus.DISABLE)
        );

        // 作废配置表
        applicationTaskConfigDAO.lambdaUpdate()
                .set(ApplicationTaskConfig::getActive, false)
                .eq(ApplicationTaskConfig::getApplicationId, applicationId)
                .update();

        msgService.noticeAudit(applicationId);

        Optional<ApplicationForm> formOpt = applicationFormDAO.lambdaQuery()
                .eq(ApplicationForm::getApplicationId, applicationId).oneOpt();
        if (formOpt.isPresent()) {
            // 删除 待审核的情况，如果是有申请通过的做重新申请，则无需处理
            applicationFormDAO.lambdaUpdate()
                    .eq(ApplicationForm::getId, formOpt.get().getId())
                    .remove();
        }

    }

    @Override
    public List<TgApplicationInfo> queryApplicationByUserId(String applicationType) {
        Long userId = ThreadContextHolder.getSysUser().getUserId();
        return mapper.queryApplicationByApplicantId(userId, applicationType);
    }

    @Override
    public Boolean existApplicationByName(String applicationType, String projectName, Integer auditStatus) {
        Long userId = ThreadContextHolder.getSysUser().getUserId();
        List<TgApplicationInfo> infos = mapper.queryApplicationByApplicantIdAndName(userId, applicationType, projectName, auditStatus);
        return CollectionUtils.isNotEmpty(infos);
    }

    private AjaxResult<TgApplicationInfo> insertOrUpdate(TgApplicationInfo reqApply, TgApplicationInfo result) {
        // 同一用户项目名相同视为更改, 更换项目名视为新增, 项目名唯一
        TgApplicationInfo lastApply = mapper.selectById(reqApply.getId());

        result.setApplyType(StringUtils.isBlank(result.getApplyType()) ? CommonConstants.INSERT : CommonConstants.UPDATE);

        boolean finishState = Objects.nonNull(lastApply)
                && (Objects.equals(ApplicationConst.AuditStatus.AUDIT_PASS, lastApply.getCurrentAuditProcessStatus())
                || Objects.equals(ApplicationConst.AuditStatus.INVALID_APPLICATION, lastApply.getCurrentAuditProcessStatus()));

        // 未完成审批，重新申请 更新自身
        if (!finishState && Objects.nonNull(lastApply)) {
            if (!Objects.equals(result.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.DRAFT)) {
                result.setCurrentAuditProcessStatus(ApplicationConst.AuditStatus.AUDITING);
            }
            result.updateById();
            return AjaxResult.success(result);
        }

        if (this.isDataApplication(reqApply) && finishState) {
            if (sameNameSameUser(result, lastApply)) {
                if (lastApply.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.AUDITING)) {
                    return AjaxResult.error("该项目当前正在审核中, 提交更改前请先撤销原申请");
                }
                // 互绑新旧申请, 新申请通过前, 只显示旧申请的数据
                result.setId(null);
                result.setOldApplicationId(lastApply.getId());
                result.insert();
                lastApply.setNewApplicationId(result.getId());
                lastApply.updateById();
            } else if (diffNameSameUser(result, lastApply)) {
                result.insert();
            } else if (sameNameDiffUser(result, lastApply)) {
                return AjaxResult.error("该项目名已被其他用户占用, 请修改后提交 " + lastApply.getApplicantId() + "->" + result.getApplicantId());
            }
        } else {
            result.insert();
        }

        return AjaxResult.success(result);
    }

    private boolean sameNameDiffUser(TgApplicationInfo result, TgApplicationInfo tgApplicationInfo) {
        return tgApplicationInfo.getProjectName().equals(result.getProjectName())
                && !tgApplicationInfo.getApplicantId().equals(result.getApplicantId());
    }

    private boolean diffNameSameUser(TgApplicationInfo newOne, TgApplicationInfo oldOne) {
        boolean sameUser = oldOne.getApplicantId().equals(newOne.getApplicantId()) || Objects.equals(oldOne.getApplicantId(), 198L);
        return !oldOne.getProjectName().equals(newOne.getProjectName()) && sameUser;
    }

    private boolean sameNameSameUser(TgApplicationInfo newOne, TgApplicationInfo oldOne) {
        boolean sameUser = oldOne.getApplicantId().equals(newOne.getApplicantId()) || Objects.equals(oldOne.getApplicantId(), 198L);
        return oldOne.getProjectName().equals(newOne.getProjectName()) && sameUser;
    }

    private void setInitInfo(TgApplicationInfo result) {
        TgAuditProcessInfo auditProcess = JsonBeanConverter.convert2Obj(auditProcessService.queryCurrentProcess(result.getProcessId()));
        result.setProcessVersion(auditProcess.getProcessVersion());
        result.setCurrentIndex(CommonConstants.INIT_INDEX);

//        if (StringUtils.isNotBlank(result.getReadableUsers())) {
//            result.setReadableUsers(StringUtils.join(Arrays.stream(result.getReadableUsers().split(","))
//                    .distinct().filter(r -> !r.equals(result.getApplicantId().toString()))
//                    .collect(Collectors.toList()), ","));
//        }
        result.setCurrentAuditNodeStatus(ApplicationConst.AuditStatus.AUDITING);
        if (Objects.isNull(result.getCurrentAuditProcessStatus())) {
            result.setCurrentAuditProcessStatus(ApplicationConst.AuditStatus.AUDITING);
        }
        if (CollectionUtils.isNotEmpty(result.getJoinInfo())) {
            String names = result.getJoinInfo().stream()
                    .flatMap(v -> Stream.of(v.getTableName1(), v.getTableName2())).distinct()
                    .collect(Collectors.joining(","));
            result.setAllTableNames(names);
        } else {
            result.setAllTableNames(result.calcTableInfo());
        }

        result.setUpdateTime(DateUtils.getTime());
        if (ObjectUtils.isNull(result.getId())) {
            result.setCreateTime(DateUtils.getTime());
        }
    }

    private TgApplicationInfo convert2Obj(TgApplicationInfo applicationInfo) {
        TgApplicationInfo result = JsonBeanConverter.convert2Json(applicationInfo);
        JsonBeanConverter.convert2Obj(result);
        return result;
    }

    private void setOrgUserInfo(TgApplicationInfo applicationInfo) {
        SysUser sysuser = ThreadContextHolder.getSysUser();
        SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(sysuser.getOrgUserId());
        applicationInfo.setApplicantId(sysuser.getUserId());
        applicationInfo.setApplicantName(sinoPassUserDTO.getViewName());
    }

    private void rebuildFieldCacheMap() {
        Map<Long, MetaDataFieldInfo> newFieldMap = new HashMap<>();

        List<CustomFieldInfo> customFieldInfos = customFieldInfoDAO.getBaseMapper()
                .selectList(new QueryWrapper<CustomFieldInfo>().lambda());
        for (CustomFieldInfo info : customFieldInfos) {
            MetaDataFieldInfo metaDataFieldInfo = new MetaDataFieldInfo(info.getTableId() + "", info.getId(), info.getFieldName());
            newFieldMap.put(info.getId(), metaDataFieldInfo);
        }

        List<TableFieldInfo> list = tableFieldInfoService.findListByIds(null);
        for (TableFieldInfo l : list) {
            MetaDataFieldInfo metaDataFieldInfo = new MetaDataFieldInfo(l.getTableName(), l.getId(), l.getFieldName());
            newFieldMap.put(l.getId(), metaDataFieldInfo);
        }
        fieldMap = newFieldMap;
    }

    private String getFieldName(String id) {
        if (ObjectUtils.isNull(fieldMap.get(Long.valueOf(id)))) {
            rebuildFieldCacheMap();
        }
        return fieldMap.get(Long.valueOf(id)).getColName();
    }

    private String getTableName(String id) {
        if (ObjectUtils.isNull(fieldMap.get(Long.valueOf(id)))) {
            rebuildFieldCacheMap();
        }
        return fieldMap.get(Long.valueOf(id)).getTableName();
    }

    private void buildHandleChain(TgApplicationInfo tgApplicationInfo) {

        // 1. 对应的流程链信息
        try {
            TgAuditProcessInfo auditProcess = JsonBeanConverter.convert2Obj(auditProcessService.queryCurrentProcess(tgApplicationInfo.getProcessId()));
            if (auditProcess.getProcessChainDetailInfo().size() > 0) {
                final int[] index = {0};
                List<Integer> statusChain = new ArrayList<>();
                List<Long> allHandlers = new ArrayList<>();
                tgApplicationInfo.getHandleNode().clear();
                for (ProcessNodeDetailDto p : auditProcess.getProcessChainDetailInfo()) {
                    // 如果存在处理详情链节点, 则将处理链节点预填到处理简链
                    if (index[0] == 0) {
                        tgApplicationInfo.setCurrentHandlers(StringUtils.join(p.getHandlers(), ","));
                    }

                    tgApplicationInfo.getHandleNode().add(new ProcessNodeEasyDto() {{
                        setNodeName(p.getName());
                        setStatus(ApplicationConst.AuditStatus.AUDITING);
                        setIndex(index[0]);
                        setHandleStatus(CommonConstants.UNHANDLE);

                    }});

                    // 增加处理详情链节点映射信息
                    p.getHandlers().stream().forEach((h) -> {
                        Map<String, Integer> handleNodeIndexMap = tgApplicationInfo.getHandlerIndexMapping().getOrDefault(h, new HashMap<>(8));
                        handleNodeIndexMap.put(String.valueOf(index[0]), ApplicationConst.AuditStatus.AUDITING);
                        tgApplicationInfo.getHandlerIndexMapping().put(h, handleNodeIndexMap);
                        // 采集所有处理人
                        allHandlers.add(h);
                    });

                    // 补足状态链
                    statusChain.add(ApplicationConst.AuditStatus.AUDITING);

                    index[0]++;
                }

                tgApplicationInfo.setStatusChain(StringUtils.join(statusChain, ","));
                tgApplicationInfo.setAllHandlers(StringUtils.join(allHandlers, ","));

                JsonBeanConverter.convert2Json(tgApplicationInfo);
            }
        } catch (NullPointerException e) {
            log.error("异常", e);
        }
    }

    /**
     * 要注意普通分布式表和单表的使用区别
     */
    @Override
    public Long countApplicationDataFromCk(String mainSql, String whereSql) {
        try {
            return Optional.ofNullable(ckProviderMapper.selectCountApplicationDataFromCk(mainSql, whereSql)).orElse(0L);
        } catch (Exception e) {
            return 0L;
        }
    }

    public long getUuid(String... fromString) {
        String join = StringUtils.join(fromString, ",");
        long result = java.util.UUID.nameUUIDFromBytes(join.getBytes(StandardCharsets.UTF_8)).getMostSignificantBits() / 10000;
        return result > 0 ? result : result * (-1);
    }

    @Override
    public boolean hasTableUsedInApplication(String tableName) {
        Integer count = mapper.selectCount(new QueryWrapper<TgApplicationInfo>().lambda()
                .like(TgApplicationInfo::getAllTableNames, "%" + tableName + "%").last(" limit 2"));
        return count > 0;
    }

    @Override
    public AjaxResult<TgApplicationInfo> bindWorkflowOrSQL(ApplicationConfigRequest request) {
        TgApplicationInfo info = mapper.selectById(request.getId());
//        info.setConfigType(request.getType());
        LambdaUpdateChainWrapper<TgApplicationInfo> update = applicationDAO.lambdaUpdate()
                .eq(TgApplicationInfo::getId, info.getId())
                .set(TgApplicationInfo::getConfigType, request.getType());

        if (ApplicationConfigTypeConstant.WORK_FLOW_TYPE.equals(request.getType())) {
            log.info("更新SQL配置：{}", info);
//            info.setWorkflowId(request.getWorkflowId());
//            info.setConfigSqlWorkflowId(null);
//            info.setConfigSql(null);
//            info.setRelateDict(request.getRelateDict());
//
//            info.setDataAmount(request.getDataAmount());
//            info.setDataCostMin(request.getDataCostMin());
//            info.setDataCost(request.getDataCost());

            update
                    .set(TgApplicationInfo::getWorkflowId, request.getWorkflowId())
                    .set(TgApplicationInfo::getConfigSqlWorkflowId, null)
                    .set(TgApplicationInfo::getConfigSql, null)
                    .set(TgApplicationInfo::getRelateDict, request.getRelateDict())
                    .set(TgApplicationInfo::getDataAmount, request.getDataAmount())
                    .set(TgApplicationInfo::getDataCostMin, request.getDataCostMin())
                    .set(TgApplicationInfo::getDataCost, request.getDataCost())
            ;

//            boolean success = mapper.updateById(info) > 0;
            boolean success = update.update();
            if (success) {
                return AjaxResult.success(info);
            } else {
                return AjaxResult.error("");
            }
        }

        //如果SQL模式未创建流程则执行新建操作，
        String tgTableName = ApplicationSqlUtil.buildAssetsTableName(request.getId());
        TaskCreateConfig config = new TaskCreateConfig(taskConfigProperties.getTableId(), request.getId().toString(), tgTableName,
                taskConfigProperties.getTenantId(), taskConfigProperties.getSourceDbType(),
                taskConfigProperties.getSourceDb(), taskConfigProperties.getTargetDbType(),
                taskConfigProperties.getTargetDb(), request.getSql(), redisTemplate);
        int releaseState = 1;
        log.info("创建工作流：{}", config);
        //将创建后的工作流ID设置进来
        final AjaxResult result = intergrateAutoProcessDefService.createProcessDefinition(null, config.getTableId(),
                config.getName(), config.buildTaskDefineJson(), null, config.buildLocation(),
                config.buildConnects(), releaseState, info::setConfigSqlWorkflowId);
        if (result.getCode() == 0) {
//            info.setConfigSql(request.getSql());
//            info.setWorkflowId(null);
//            info.setRelateDict(request.getRelateDict());
//
//            info.setDataAmount(request.getDataAmount());
//            info.setDataCostMin(request.getDataCostMin());
//            info.setDataCost(request.getDataCost());

            update
                    .set(TgApplicationInfo::getWorkflowId, null)
                    .set(TgApplicationInfo::getConfigSqlWorkflowId, info.getConfigSqlWorkflowId())
                    .set(TgApplicationInfo::getConfigSql, request.getSql())
                    .set(TgApplicationInfo::getRelateDict, request.getRelateDict())
                    .set(TgApplicationInfo::getDataAmount, request.getDataAmount())
                    .set(TgApplicationInfo::getDataCostMin, request.getDataCostMin())
                    .set(TgApplicationInfo::getDataCost, request.getDataCost())
            ;

            log.info("更新SQL配置：{}", info);
//            boolean success = mapper.updateById(info) > 0;
            boolean success = update.update();
            if (success) {
                return AjaxResult.success(info);
            } else {
                return AjaxResult.error("");
            }
        }
        return AjaxResult.error("");
    }

    /**
     * @see AuditProcessServiceImpl#queryAuditProcessAuditListByType 查询
     */
    @Override
    public AjaxResult<Boolean> bindFile(ApplicationConfigRequest request) {
        try {
            String format = JsonUtils.format(request.getAssetsAttach());

            boolean update = applicationDAO.lambdaUpdate()
                    .set(TgApplicationInfo::getAssetsAttachJson, format)
                    .set(TgApplicationInfo::getConfigType, ApplicationConfigTypeConstant.FILE_TYPE)
                    .set(TgApplicationInfo::getDataAmount, request.getDataAmount())
                    .set(TgApplicationInfo::getDataCost, request.getDataCost())
                    .set(TgApplicationInfo::getDataCostMin, request.getDataCostMin())
                    .eq(TgApplicationInfo::getId, request.getId())
                    .update();
            return AjaxResult.success(update);
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    @Override
    public AjaxResult<List<LatestProjectDto>> queryLatestProject(Long templateId) {
        return AjaxResult.success(applicationInfoMapper.queryLastProjectName(SecurityUtils.getUserId(), templateId));
    }

    @Override
    public AjaxResult<List<HistoryApplyQuoteDto>> queryHistoryQuote(Long templateId, String name, Integer state) {
        List<TgApplicationInfo> infos = applicationInfoMapper.selectList(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getProjectName, TgApplicationInfo::getApplicationNo,
                        TgApplicationInfo::getCurrentAuditProcessStatus, TgApplicationInfo::getCreateTime)
                .eq(TgApplicationInfo::getApplicantId, SecurityUtils.getUserId())
                .eq(Objects.nonNull(templateId), TgApplicationInfo::getTemplateId, templateId)
                .eq(Objects.nonNull(state), TgApplicationInfo::getCurrentAuditProcessStatus, state)
                .like(StringUtils.isNotBlank(name), TgApplicationInfo::getProjectName, name)
                .orderByDesc(TgApplicationInfo::getCreateTime)
        );

        return AjaxResult.success(infos.stream().map(v -> HistoryApplyQuoteDto.builder()
                        .id(v.getId())
                        .applicationNo(v.getApplicationNo())
                        .projectName(v.getProjectName())
                        .createTime(v.getCreateTime())
                        .currentAuditProcessStatus(v.getCurrentAuditProcessStatus())
                        .build())
                .collect(Collectors.toList()));
    }

    /**
     *
     */
    @Override
    public AjaxResult<IPage<HistoryApplyQuoteEntity>> queryHistoryQuote(HistoryQueryRequest request) {
        Set<Long> projectIds = projectHelperDAO.queryProjects(request.getUserId());
        if (CollectionUtils.isEmpty(projectIds)) {
            return AjaxResult.success(new Page<>());
        }
        request.setProjectIds(new ArrayList<>(projectIds));
        if (StringUtils.isNotBlank(request.getProjectName())) {
            List<Project> list = projectDAO.lambdaQuery()
                    .in(Project::getId, projectIds)
                    .like(Project::getName, request.getProjectName())
                    .list();
            if (CollectionUtils.isEmpty(list)) {
                return AjaxResult.success(new Page<>());
            }
            List<Long> ids = Lambda.buildList(list);
            request.setProjectIds(ids);
        } else {
            request.setProjectName(null);
        }

        if (Objects.nonNull(request.getProjectId()) && CollectionUtils.isNotEmpty(request.getProjectIds())) {
            request.getProjectIds().remove(request.getProjectId());
        }
        // 剩余项目空时，特殊值排除项目
        if (CollectionUtils.isEmpty(request.getProjectIds())) {
            request.setProjectIds(Collections.singletonList(-999999L));
        }

        IPage<HistoryApplyQuoteEntity> pageResult = applicationInfoMapper.pageHistoryQuote(request.buildPage(), request);
        List<HistoryApplyQuoteEntity> records = pageResult.getRecords();
        for (HistoryApplyQuoteEntity record : records) {
            String user = record.getApplicant();
            if (StringUtils.isNotBlank(user) && user.contains("-")) {
                String[] split = user.split("-");
                if (split.length > 1) {
                    record.setApplicant(split[1]);
                }
            }
        }
        return AjaxResult.success(pageResult);
    }

    @Override
    public Integer countByTemplateId(String templateId) {
        return applicationInfoMapper.selectCount(new QueryWrapper<TgApplicationInfo>().lambda()
                .eq(TgApplicationInfo::getTemplateId, Long.valueOf(templateId)));
    }

    @Override
    public AjaxResult<List<ApplyMetricsDto>> queryMetricsByApply(Long applicationId) {
        TgApplicationInfo applicationInfo = TgApplicationInfo.newInstance().selectById(applicationId);
        if (Objects.isNull(applicationInfo)) {
            return AjaxResult.success(Collections.emptyList());
        }
        JsonBeanConverter.convert2Obj(applicationInfo);
        List<CustomMetricsLabelDto> customMetrics = applicationInfo.getCustomMetrics();
        List<Long> ids = customMetrics.stream().map(CustomMetricsLabelDto::getMetricsId).collect(Collectors.toList());
        List<MetricsDict> metricsDicts = Lambda.queryListIfExist(ids, metricsDictDAO.getBaseMapper()::selectBatchIds);

        return AjaxResult.success(metricsDicts.stream().map(v -> {
            ApplyMetricsDto dto = new ApplyMetricsDto();
            BeanUtils.copyProperties(v, dto);
            return dto;
        }).collect(Collectors.toList()));
    }

    @Override
    public AjaxResult<List<OneItem>> batchParseSql(ParseSqlBatchRequest t) {
        List<OneItem> applyList = t.getApplyList();
        for (OneItem oneItem : applyList) {
            try {
                if (StringUtils.isNotBlank(oneItem.getName())) {
                    redisTemplate.opsForHash().put(RedisKeys.Apply.TRANS_PRO_MAP, Long.valueOf(oneItem.getId()), oneItem.getName());
                }

                String productSQL = oneItem.getSql();
                if (Objects.isNull(productSQL)) {
                    log.error("NULL {}", oneItem);
                    continue;
                }
                if (productSQL.contains("select")) {
                    log.error("ignore select 嵌套子查询");
                    continue;
                }

                String id = oneItem.getId();
                Object val = redisTemplate.opsForHash().get(RedisKeys.Apply.VALIDATE_RESULT_MAP, Long.parseLong(id));
                if (Objects.isNull(val)) {
                    oneItem.setCompare(CompareResultVO.builder().build());
                } else {
                    CompareResultVO parse = JsonUtils.parse(val.toString(), CompareResultVO.class);
                    oneItem.setCompare(parse);
                }

                try {
                    // 品牌模板：模糊转换 废弃
//                    productSQL = HistoryApplyUtil.convertFuzzyQuerySql(productSQL, transferProperties.getFuzzyQueryExcludeField());
                    // TODO 加否定SQL后，原逻辑乱了
                    if (StringUtils.isNotBlank(oneItem.getExcludeSql()) && !oneItem.getExcludeSql().contains("1=0")) {
//                        String tmpSql = HistoryApplyUtil.convertReverseSql(oneItem.getExcludeSql());
//                        productSQL = "(" + productSQL + ") AND (" + HistoryApplyUtil.convertFuzzyQuerySql(tmpSql, transferProperties.getFuzzyQueryExcludeField()) + ")";
                        String tmpSql = HistoryApplyUtil.convertReverseSql(oneItem.getExcludeSql());
                        productSQL = "(" + productSQL + ") AND (" + tmpSql + ")";
                    }

//                    log.info("id:{} , sql : {}", oneItem.getId(), productSQL);
                    FilterDTO productFilter = HistoryApplyUtil.parseSql(productSQL);
                    this.fillTableId(productFilter);
                    this.fillFieldIdForFilter(transferProperties.getBaseTableId(), productFilter);
                    oneItem.setFilter(this.handleForSingle(productFilter));
                } catch (Exception e) {
                    log.error("错误{},", oneItem.getId(), e);
                }

//                String areaSQL = String.format(" zone_name = '%s'", oneItem.getAreaGra());
//                if (StringUtils.isNotBlank(oneItem.getAreaSql()) && !oneItem.getAreaSql().contains("1=1")) {
//                    areaSQL = "(" + areaSQL + ") AND " + oneItem.getAreaSql().replace("a.", "");
//                }
//                FilterDTO areaFilter = HistoryApplyUtil.parseSql(areaSQL);
//                this.fillFieldIdForFilter(transferProperties.getBaseTableId(), areaFilter);
//                oneItem.setAreaFilter(this.handleForSingle(areaFilter));

                // 市场范围特殊处理
                String areaSQL;
                if (StringUtils.isBlank(oneItem.getAreaSql())
                        || oneItem.getAreaSql().contains("1=1")
                        || oneItem.getAreaSql().contains("1 = 1")) {
                    String area = Stream.of(oneItem.parseAreaGra())
                            .map(v -> "'" + v + "'").collect(Collectors.joining(","));
                    areaSQL = String.format(" zone_name in (%s)", area);
                } else {
                    areaSQL = oneItem.getAreaSql().replace("a.", "");
                    areaSQL = areaSQL.replace("province", "city_co_name");
                }
                FilterDTO areaFilter = HistoryApplyUtil.parseSql(areaSQL);
                this.fillFieldIdForFilter(transferProperties.getBaseTableId(), areaFilter);
                this.fillTableId(areaFilter);
                oneItem.setAreaFilter(this.handleForSingle(areaFilter));

                // 时间范围
                FilterDTO timeFilter = oneItem.parseTimeFilter();
                this.fillFieldIdForFilter(transferProperties.getBaseTableId(), timeFilter);
                oneItem.setTimeFilter(timeFilter);
            } catch (NullPointerException e) {
                log.error("NPE 无法解析SQL", e);
            } catch (Exception e) {
                log.error("", e);
                return AjaxResult.error(e.getMessage());
            }
        }

        return AjaxResult.success(applyList);
    }

    /**
     * 单个条件值下 结构需要转换成数组
     */
    private FilterDTO handleForSingle(FilterDTO filter) {
        List<FilterDTO> filters = filter.getFilters();
        if (CollectionUtils.isNotEmpty(filters)) {
            return filter;
        }

        FilterDTO result = new FilterDTO();
        ArrayList<FilterDTO> list = new ArrayList<>();
        list.add(filter);
        result.setFilters(list);
        result.setLogicalOperator(filter.getLogicalOperator());
        return result;
    }

    /**
     * 填充字段库id
     */
    @Override
    public void fillFieldIdForFilter(Long tableId, FilterDTO filter) {
        Map<String, Long> fieldNameMap = fieldsCache.get(tableId, () -> {
            List<TableFieldInfo> fields = tableFieldInfoMapper.selectList(new QueryWrapper<TableFieldInfo>().lambda()
                    .select(TableFieldInfo::getId, TableFieldInfo::getFieldName, TableFieldInfo::getRelationColId)
                    .eq(TableFieldInfo::getTableId, tableId));
            // TODO 区别：实际表字段名 和 字段库名字
            return Lambda.buildMap(fields, TableFieldInfo::getFieldName, TableFieldInfo::getRelationColId,
                    f -> Objects.nonNull(f.getRelationColId()));
//            List<Long> fieldDictIds = Lambda.buildList(fields, TableFieldInfo::getRelationColId);
//            List<FieldDict> dicts = fieldDictDAO.getBaseMapper().selectBatchIds(fieldDictIds);
//            return Lambda.buildMap(dicts, FieldDict::getFieldName, FieldDict::getId);
        });

        List<FilterDTO> filters = filter.getFilters();
        if (CollectionUtils.isNotEmpty(filters)) {
            for (FilterDTO filterDTO : filters) {
                this.fillFieldIdForFilter(tableId, filterDTO);
            }
        }

        FilterDTO.FilterItemDTO filterItem = filter.getFilterItem();
        if (Objects.isNull(filterItem)) {
            return;
        }
        Optional.ofNullable(filterItem.getFilters())
                .filter(org.apache.commons.collections4.CollectionUtils::isNotEmpty)
                .map(v -> v.get(0))
                .map(FilterDTO::getFilters).ifPresent(list -> {
                    for (FilterDTO filterDTO : list) {
                        this.fillFieldIdForFilter(tableId, filterDTO);
                    }
                });

        Long fieldId = fieldNameMap.get(filterItem.getFieldName());
        filterItem.setFieldId(fieldId);
    }

    /**
     * 依据模板的 业务线，字段分类，填充字段id
     */
    @Override
    public void fillFieldIdForFilter(TgTemplateInfo template, FieldGranularityEnum type, FilterDTO filter) {
        String key = type.name() + template.getBizType();
        Map<String, Long> fieldNameMap = fieldDictCache.get(key, () -> {
            List<FieldDict> fieldList = fieldDictDAO.getBaseMapper().selectList(new QueryWrapper<FieldDict>().lambda()
                    .select(FieldDict::getId, FieldDict::getFieldName)
                    .eq(FieldDict::getGranularity, type)
                    .eq(FieldDict::getUseWay, FieldUseWayEnum.normal.name())
                    .and(org.apache.commons.lang3.StringUtils.isNotBlank(template.getBizType()),
                            v -> v.apply(BizTypeUtil.buildBizTypeWhere(template.getBizType())))
            );

            return Lambda.buildMap(fieldList, FieldDict::getFieldName, FieldDict::getId);
        });


        List<FilterDTO> filters = filter.getFilters();
        if (CollectionUtils.isNotEmpty(filters)) {
            for (FilterDTO filterDTO : filters) {
                this.fillFieldIdForFilter(template, type, filterDTO);
            }
        }

        FilterDTO.FilterItemDTO filterItem = filter.getFilterItem();
        if (Objects.isNull(filterItem)) {
            return;
        }
        Optional.ofNullable(filterItem.getFilters())
                .filter(org.apache.commons.collections4.CollectionUtils::isNotEmpty)
                .map(v -> v.get(0))
                .map(FilterDTO::getFilters).ifPresent(list -> {
                    for (FilterDTO filterDTO : list) {
                        this.fillFieldIdForFilter(template, type, filterDTO);
                    }
                });

        Long fieldId = fieldNameMap.get(filterItem.getFieldName());
        filterItem.setFieldId(fieldId);
    }

    /**
     * @param projectId Excel id
     */
    @Override
    public AjaxResult<String> applyDetail(Long projectId) {
        Object applyId = redisTemplate.opsForHash().get(TRANS_APPLY_MAP, projectId);
        if (Objects.isNull(applyId)) {
            return AjaxResult.error("未申请");
        }
        TgApplicationInfo info = applicationInfoMapper.selectById((Long) applyId);
        log.warn("Detail: {} {}", info.getId(), info.getAssetsId());
        return AjaxResult.success("", info.getAsql());
    }

    /**
     *
     */
    @Override
    public String fixNullTable(String ids) {
        if (StringUtils.isBlank(ids)) {
            return "Empty";
        }
        List<Long> idList = Arrays.stream(ids.split(",")).map(Long::parseLong).collect(Collectors.toList());
        List<TgApplicationInfo> infos = applicationDAO.listByIds(idList);
        if (CollectionUtils.isEmpty(infos)) {
            return "Empty";
        }

        for (TgApplicationInfo info : infos) {
            JsonBeanConverter.convert2Obj(info);

            for (ApplicationGranularityDto dto : info.getGranularity()) {
                FilterDTO filter = dto.getFilter();
                fillTableId(filter);
            }
            JsonBeanConverter.convert2Json(info);
            applicationDAO.lambdaUpdate().set(TgApplicationInfo::getGranularityJson, info.getGranularityJson())
                    .eq(TgApplicationInfo::getId, info.getId()).update();
        }

        return "";
    }

    private void fillTableId(FilterDTO filter) {
        if (Objects.isNull(filter)) {
            return;
        }

        FilterDTO.FilterItemDTO item = filter.getFilterItem();
        if (Objects.nonNull(item)) {
            item.setTableId(transferProperties.getBaseTableId());
            List<FilterDTO> filters = item.getFilters();
            if (CollectionUtils.isNotEmpty(filters)) {
                filters.forEach(this::fillTableId);
            }
        }
        List<FilterDTO> filters = filter.getFilters();
        if (CollectionUtils.isNotEmpty(filters)) {
            filters.forEach(this::fillTableId);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> transferApply(HttpServletRequest req, TransHistoryApplyRequest request) {
        List<FmtHistoryApply> applyList = request.getApplyList();
        if (CollectionUtils.isEmpty(applyList)) {
            return AjaxResult.error("参数为空");
        }

        applyList.removeIf(v -> CollectionUtils.isNotEmpty(request.getIgnoreIds()) && request.getIgnoreIds().contains(v.getId()));

        TgTemplateInfo template = new TgTemplateInfo().selectById(transferProperties.getTemplateId());
        if (Objects.isNull(template)) {
            return AjaxResult.error("常规模板不存在");
        }
        TgAssetInfo asset = assetService.queryOne(template.getId(), AssetType.MODEL);
        if (Objects.isNull(asset)) {
            return AjaxResult.error("常规模板未上架");
        }

        List<String> proNames = applyList.stream().map(FmtHistoryApply::getNewProjectName).distinct().collect(Collectors.toList());
        List<Project> proList = projectMapper.selectList(new QueryWrapper<Project>().lambda().in(Project::getName, proNames));

        Map<String, Long> proMap = Lambda.buildMap(proList, Project::getName, Project::getId);
        if (proList.size() != proNames.size()) {
            String no = proNames.stream().filter(v -> !proMap.containsKey(v)).collect(Collectors.joining(","));
            log.info(" 项目不存在 {}", no);
            return AjaxResult.error("项目不存在 " + no);
        }

        Set<String> realNames = Lambda.buildSet(applyList, FmtHistoryApply::getApplyUser);
        Map<String, Long> userMap = userService.selectUserByRealNames(realNames);

        List<TableFieldInfo> fields = tableFieldInfoService.getFieldsByTableId(template.getBaseTableId());
        List<Long> fieldDictIds = Lambda.buildList(fields, TableFieldInfo::getRelationColId);
        List<FieldDict> fieldDictList = Lambda.queryListIfExist(fieldDictIds, fieldDictDAO.getBaseMapper()::selectBatchIds);
        Map<Long, String> fieldDictNameMap = Lambda.buildMap(fieldDictList, FieldDict::getId, FieldDict::getName);
        fieldDictNameMap.putAll(ApplicationConst.PeriodField.idToNameMap);

        JsonBeanConverter.convert2Obj(template);

        List<Long> metricsIds = Lambda.buildList(template.getCustomMetrics(), CustomMetricsLabelDto::getMetricsId);
        List<MetricsDict> metrics = metricsDictDAO.getBaseMapper().selectList(new QueryWrapper<MetricsDict>()
                .lambda().in(MetricsDict::getId, metricsIds));
        Map<Long, String> metricsMap = Lambda.buildMap(metrics, MetricsDict::getId, MetricsDict::getName);

        Map<Long, Set<Long>> proUsers = new HashMap<>();
        for (FmtHistoryApply apply : applyList) {
            if (CollectionUtils.isNotEmpty(request.getIgnoreIds()) && request.getIgnoreIds().contains(apply.getId())) {
                log.warn("IGNORE: id={} {}", apply.getId(), apply.getProjectName());
                continue;
            }
            Boolean exist = redisTemplate.opsForHash().hasKey(TRANS_APPLY_MAP, apply.getId());
            if (BooleanUtils.isTrue(exist)
                    && BooleanUtils.isNotTrue(request.getDebug())
                    && BooleanUtils.isNotTrue(request.getRetry())
            ) {
//                log.warn("ignore handled row: id={} {}", apply.getId(), apply.getProjectName());
                continue;
            }
            log.warn("start handle row: id={} {}", apply.getId(), apply.getProjectName());

            redisTemplate.<Long, String>opsForHash().put(RedisKeys.Apply.TRANS_USER_MAP, apply.getId(), apply.getApplyUser());

            TgApplicationInfo info = TgApplicationInfo.newInstance();
            if (BooleanUtils.isTrue(exist) && BooleanUtils.isTrue(request.getRetry())) {
                Object lastApply = redisTemplate.opsForHash().get(TRANS_APPLY_MAP, apply.getId());
                info.setId((Long) lastApply);
                TgApplicationInfo last = applicationDAO.lambdaQuery()
                        .select(TgApplicationInfo::getId, TgApplicationInfo::getAssetsId)
                        .eq(TgApplicationInfo::getId, info.getId())
                        .oneOpt().orElseThrow(() -> new RuntimeException("旧申请不存在 " + lastApply));
                log.info("retry apply: lastApply={}", lastApply);
                info.setAssetsId(last.getAssetsId());
            }

            info.setTemplateId(template.getId());
            info.setNewAssetId(asset.getId());
            info.setRequireTimeType(ApplicationConst.RequireTimeType.PERSISTENCE);
            info.setProjectName(apply.getProjectName());
            info.setProjectId(proMap.get(apply.getNewProjectName()));
            info.setProcessId(template.getProcessId());
//            info.setTemplateVersion(template.getVersion());
            info.setExportProjectName(true);

            Long applyId = BooleanUtils.isTrue(request.getMockSameUser()) ? transferProperties.getApplicantId() : userMap.get(apply.getApplyUser());
            if (!Objects.equals(applyId, SecurityUtils.getUserId())) {
                fillUserAuthById(req, applyId);
            }
            info.setApplicantId(applyId);
            TgCollectionUtils.appendSetVal(proUsers, info.getProjectId(), info.getApplicantId());

            info.setDeliverTimeType(DeliverTimeTypeEnum.getTypeByTransferDesc(apply.getDeliverTimeType()));
            info.setColsInfo(template.getColsInfo());
            info.setBaseTableId(template.getBaseTableId());
            info.setBaseTableName(template.getBaseTableName());

            info.setDataExpir(HistoryApplyUtil.parseExpire(apply.getEndTime()));

            // 粒度和字段处理
            List<TemplateGranularityDto> granularityList = template.getGranularity();
            List<ApplicationGranularityDto> applyGras = new ArrayList<>();
            for (TemplateGranularityDto tempGra : granularityList) {
                if (Objects.equals(tempGra.getGranularity(), FieldGranularityEnum.time.name())) {
                    Optional<TemplateGranularityDetailDto> row = tempGra.getDetails().stream()
                            .filter(v -> Objects.equals(v.getName(), apply.getTimeGra()))
                            .findAny();
                    if (!row.isPresent()) {
                        throw new CustomException("时间信息 异常，粒度未配置: " + apply.getTimeGra());
                    }

                    // 全选是因为固定维度。
                    List<SelectFieldDto> selectFieldDtos = Stream.of(row.get().getRequired(), row.get().getOptions())
                            .flatMap(v -> CollectionUtils.isEmpty(v) ? Stream.empty() : v.stream())
                            .map(v -> {
                                // 特殊处理 时间列
                                String alias = Optional.ofNullable(fieldDictNameMap.get(v))
                                        .orElse(v == -1L ? ApplicationConst.PeriodField.PERIOD_TYPE_ALIAS : null);
                                if (StringUtils.isBlank(alias)) {
                                    throw new CustomException("存在未填写别名的字段");
                                }
                                return SelectFieldDto.builder()
                                        .fieldId(v)
                                        .alias(alias)
                                        .build();
                            }).collect(Collectors.toList());
                    ApplicationGranularityDto tmp = ApplicationGranularityDto.builder()
                            .granularity(tempGra.getGranularity())
                            .selectGranularity(Collections.singletonList(row.get().getName()))
                            .fields(selectFieldDtos)
                            .filter(apply.getTimeFilter())
                            .build();
//                    apply.handleSeasonFilter();
                    applyGras.add(tmp);
                } else if (Objects.equals(tempGra.getGranularity(), FieldGranularityEnum.product.name())) {
                    if (CollectionUtils.isEmpty(tempGra.getDetails())) {
                        throw new CustomException("产品未配置粒度");
                    }

                    // 特殊处理，只会有一个粒度
                    TemplateGranularityDetailDto first = tempGra.getDetails().get(0);

                    List<SelectFieldDto> selectFieldDtos = Stream.of(first.getRequired(), first.getOptions())
                            .flatMap(v -> CollectionUtils.isEmpty(v) ? Stream.empty() : v.stream())
                            .map(v -> SelectFieldDto.builder()
                                    .fieldId(v)
                                    .alias(fieldDictNameMap.get(v))
                                    .build()).collect(Collectors.toList());

                    ApplicationGranularityDto tmp = ApplicationGranularityDto.builder()
                            .granularity(tempGra.getGranularity())
                            .selectGranularity(Collections.singletonList(first.getName()))
                            .fields(selectFieldDtos)
                            .filter(apply.getNewFilter())
                            .build();
                    applyGras.add(tmp);
                } else if (Objects.equals(tempGra.getGranularity(), FieldGranularityEnum.area.name())) {
                    String[] parts = apply.parseAreaGra();
                    List<TemplateGranularityDetailDto> selectList = Stream.of(parts)
                            .map(v -> tempGra.getDetails().stream().filter(d -> Objects.equals(d.getName(), v)).findAny())
                            .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
                    List<String> names = Lambda.buildList(selectList, TemplateGranularityDetailDto::getName);
                    if (selectList.size() != parts.length) {
                        String missing = Stream.of(parts).filter(v -> !names.contains(v)).collect(Collectors.joining(","));
                        throw new CustomException("市场信息 异常，粒度未配置: " + missing);
                    }

                    List<SelectFieldDto> selectFieldDtos =
                            selectList.stream()
                                    .flatMap(v -> Stream.of(v.getRequired(), v.getOptions()))
                                    .flatMap(v -> CollectionUtils.isEmpty(v) ? Stream.empty() : v.stream())
                                    .map(v -> SelectFieldDto.builder()
                                            .fieldId(v)
                                            .alias(fieldDictNameMap.get(v))
                                            .build()).collect(Collectors.toList());

                    ApplicationGranularityDto tmp = ApplicationGranularityDto.builder()
                            .granularity(tempGra.getGranularity())
                            .selectGranularity(names)
                            .fields(selectFieldDtos)
                            .filter(apply.getAreaFilter())
                            .build();
                    applyGras.add(tmp);
                } else {
                    if (CollectionUtils.isNotEmpty(tempGra.getDetails())) {
                        log.error("未填写粒度 {} 粒度数：{}", tempGra.getGranularity(), tempGra.getDetails().size());
                    }
                }
            }
            info.setGranularity(applyGras);

            // 默认勾选全部指标
            List<CustomMetricsLabelDto> labels = metricsIds.stream().map(v -> {
                CustomMetricsLabelDto dto = new CustomMetricsLabelDto();
                dto.setMetricsId(v);
                dto.setAlias(metricsMap.get(v));
                dto.setSelect(true);
                return dto;
            }).collect(Collectors.toList());
            info.setCustomMetrics(labels);


//            List<CustomMetricsLabelDto> labels = Stream.of(apply.getMetrics())
//                    .filter(StringUtils::isNotBlank)
//                    .flatMap(v -> Arrays.stream(v.split("，")))
//                    .map(v -> {
//                        Long id = metricsMap.get(v);
//                        if (Objects.isNull(id)) {
//                            throw new RuntimeException("存在模板没配置的指标 id:" + apply.getId() + " name:" + v);
//                        }
//                        CustomMetricsLabelDto dto = new CustomMetricsLabelDto();
//                        dto.setMetricsId(id);
//                        dto.setAlias(v);
//                        dto.setSelect(true);
//                        return dto;
//                    }).collect(Collectors.toList());
//            info.setCustomMetrics(labels);

            info.setRequireAttr(RequireAttrType.getByTransferDesc(apply.getRequireAttr()));
            info.setContractNo(Optional.ofNullable(apply.getContractNo()).filter(v -> StringUtils.isNotBlank(v) && !v.contains("NULL")).orElse(null));

            if (BooleanUtils.isTrue(transferProperties.getCloseRound())) {
                MDC.put(CommonConstants.REMOVE_ROUND, "RRRRRemove");
            }
            AjaxResult tmp;
            if (BooleanUtils.isTrue(request.getDebug())) {
                tmp = this.tryApplication(info);
            } else {
                tmp = this.addTemplateApplication(info);
            }

            log.info("create: tmp={}", tmp);

            if (tmp.isSuccess()) {
                Optional.ofNullable(tmp).map(AjaxResult::getData).map(v -> {
                    if (v instanceof TgApplicationInfo) {
                        return (TgApplicationInfo) v;
                    }
                    log.warn("type not match: v={}", v);
                    return null;
                }).map(TgApplicationInfo::getId).ifPresent(v -> redisTemplate.<Long, Long>opsForHash().put(TRANS_APPLY_MAP, apply.getId(), v)
                );
            } else {
                log.error("tmp={}", tmp);
            }
        }

        projectService.patchUserProjectRelation(proUsers);
        return AjaxResult.succeed();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> transferTailApply(HttpServletRequest req, TransHistoryApplyRequest request) {
        if (CollectionUtils.isEmpty(request.getApplyList())) {
            return AjaxResult.error("参数为空");
        }

        List<FmtHistoryApply> applyList = request.getApplyList();
        applyList.removeIf(v -> CollectionUtils.isNotEmpty(request.getIgnoreIds()) && request.getIgnoreIds().contains(v.getId()));

        List<String> proNames = applyList.stream().map(FmtHistoryApply::getNewProjectName).distinct().collect(Collectors.toList());
        List<Project> proList = projectMapper.selectList(new QueryWrapper<Project>().lambda().in(Project::getName, proNames));

        Map<String, Long> proMap = Lambda.buildMap(proList, Project::getName, Project::getId);
        if (proList.size() != proNames.size()) {
            String no = proNames.stream().filter(v -> !proMap.containsKey(v)).collect(Collectors.joining(","));
            return AjaxResult.error("项目不存在 " + no);
        }

        Set<String> realNames = Lambda.buildSet(request.getApplyList(), FmtHistoryApply::getApplyUser);
        Map<String, Long> userMap = userService.selectUserByRealNames(realNames);


        Date now = new Date();
        for (FmtHistoryApply apply : applyList) {
            if (StringUtils.isBlank(apply.getTailTemplateName())) {
                log.warn("IGNORE EMPTY TEMP: id={} {}", apply.getId(), apply.getProjectName());
                continue;
            }
            Boolean exist = redisTemplate.opsForHash().hasKey(RedisKeys.Apply.TRANS_TAIL_APPLY_MAP, apply.getId());
            if (BooleanUtils.isTrue(exist)
                    && BooleanUtils.isNotTrue(request.getDebug())
                    && BooleanUtils.isNotTrue(request.getRetry())
            ) {
//                log.warn("ignore handled row: id={} {}", apply.getId(), apply.getProjectName());
                continue;
            }
            log.warn("start handle row: id={} {}", apply.getId(), apply.getProjectName());

            redisTemplate.<Long, String>opsForHash().put(RedisKeys.Apply.TRANS_USER_MAP, apply.getId(), apply.getApplyUser());

            TgApplicationInfo info = TgApplicationInfo.newInstance();
            if (BooleanUtils.isTrue(exist) && BooleanUtils.isTrue(request.getRetry())) {
                Object lastApply = redisTemplate.opsForHash().get(RedisKeys.Apply.TRANS_TAIL_APPLY_MAP, apply.getId());
                info.setId((Long) lastApply);
                TgApplicationInfo last = applicationDAO.lambdaQuery()
                        .select(TgApplicationInfo::getId, TgApplicationInfo::getAssetsId)
                        .eq(TgApplicationInfo::getId, info.getId())
                        .oneOpt().orElseThrow(() -> new RuntimeException("旧申请不存在 " + lastApply));
                log.info("retry tail apply: lastApply={}", lastApply);
                info.setAssetsId(last.getAssetsId());
            }

            TemplateCtx ctx = this.queryTemplate(apply.getTailTemplateName());
            TgTemplateInfo template = ctx.getTemplateInfo();
            TgAssetInfo asset = ctx.getAsset();
            Map<Long, String> fieldDictNameMap = ctx.getFieldDictNameMap();
            Map<Long, String> metricsMap = ctx.getMetricsMap();
            List<Long> metricsIds = ctx.getMetricsIds();


            info.setTemplateId(template.getId());
            info.setNewAssetId(asset.getId());
            info.setProjectId(proMap.get(apply.getNewProjectName()));
            info.setProcessId(template.getProcessId());

//            info.setTemplateVersion(template.getVersion());
            info.setRequireTimeType(ApplicationConst.RequireTimeType.PERSISTENCE);
            info.setProjectName(apply.getProjectName() + "-长尾");
            Long applyId = BooleanUtils.isTrue(request.getMockSameUser()) ? transferProperties.getApplicantId() : userMap.get(apply.getApplyUser());
            if (!Objects.equals(applyId, SecurityUtils.getUserId())) {
                fillUserAuthById(req, applyId);
            }
            info.setApplicantId(applyId);
//            info.setPm(apply.getLeaderName());
            info.setDeliverTimeType(DeliverTimeTypeEnum.getTypeByTransferDesc(apply.getDeliverTimeType()));
//            info.setClientNames(apply.getCustomer());
            info.setColsInfo(template.getColsInfo());
            info.setBaseTableId(template.getBaseTableId());
            info.setBaseTableName(template.getBaseTableName());
            info.setExportProjectName(true);
            info.setDataExpir(HistoryApplyUtil.parseExpire(apply.getEndTime()));

            // 硬编码 特殊处理 打包方式，最好其实是读取excel，但是要改前端
            String packName = "打包到分类四";
            TgTemplatePackTailSetting tailSetting = templatePackTailSettingMapper.selectOne(new QueryWrapper<TgTemplatePackTailSetting>().lambda()
                    .eq(TgTemplatePackTailSetting::getTemplateId, template.getId())
                    .eq(TgTemplatePackTailSetting::getName, packName)
                    .eq(TgTemplatePackTailSetting::getDeleted, 0L)
            );
            if (Objects.isNull(tailSetting)) {
                throw new RuntimeException("打包方式未配置: " + template.getName() + " " + packName);
            }
            info.setPackTailId(tailSetting.getId());
            info.setPackTailName(packName);
            info.setPackTailSwitch(true);

            // 粒度和字段处理
            List<TemplateGranularityDto> granularityList = template.getGranularity();
            List<ApplicationGranularityDto> applyGras = new ArrayList<>();
            for (TemplateGranularityDto tempGra : granularityList) {
                if (Objects.equals(tempGra.getGranularity(), FieldGranularityEnum.time.name())) {
                    Optional<TemplateGranularityDetailDto> row = tempGra.getDetails().stream()
                            .filter(v -> Objects.equals(v.getName(), apply.getTimeGra()))
                            .findAny();
                    if (!row.isPresent()) {
                        throw new CustomException("时间信息 异常，粒度未配置: " + apply.getTimeGra());
                    }

                    // 全选是因为固定维度。
                    List<SelectFieldDto> selectFieldDtos = Stream.of(row.get().getRequired(), row.get().getOptions())
                            .flatMap(v -> CollectionUtils.isEmpty(v) ? Stream.empty() : v.stream())
                            .map(v -> {
                                // 特殊处理 时间列
                                String alias = Optional.ofNullable(fieldDictNameMap.get(v))
                                        .orElse(v == -1L ? ApplicationConst.PeriodField.PERIOD_TYPE_ALIAS : null);
                                if (StringUtils.isBlank(alias)) {
                                    throw new CustomException("存在未填写别名的字段");
                                }
                                return SelectFieldDto.builder()
                                        .fieldId(v)
                                        .alias(alias)
                                        .build();
                            }).collect(Collectors.toList());
                    ApplicationGranularityDto tmp = ApplicationGranularityDto.builder()
                            .granularity(tempGra.getGranularity())
                            .selectGranularity(Collections.singletonList(row.get().getName()))
                            .fields(selectFieldDtos)
                            .filter(apply.getTimeFilter())
                            .build();

//                    apply.handleSeasonFilter();
                    applyGras.add(tmp);
                } else if (Objects.equals(tempGra.getGranularity(), FieldGranularityEnum.product.name())) {
                    if (CollectionUtils.isEmpty(tempGra.getDetails())) {
                        throw new CustomException("产品未配置粒度");
                    }

                    // 特殊处理，只会有一个粒度
                    TemplateGranularityDetailDto first = tempGra.getDetails().get(0);

                    List<SelectFieldDto> selectFieldDtos = Stream.of(first.getRequired(), first.getOptions())
                            .flatMap(v -> CollectionUtils.isEmpty(v) ? Stream.empty() : v.stream())
                            .map(v -> SelectFieldDto.builder()
                                    .fieldId(v)
                                    .alias(fieldDictNameMap.get(v))
                                    .build()).collect(Collectors.toList());

                    ApplicationGranularityDto tmp = ApplicationGranularityDto.builder()
                            .granularity(tempGra.getGranularity())
                            .selectGranularity(Collections.singletonList(first.getName()))
                            .fields(selectFieldDtos)
                            .filter(apply.getNewFilter())
                            .build();
                    applyGras.add(tmp);
                } else if (Objects.equals(tempGra.getGranularity(), FieldGranularityEnum.area.name())) {
                    String[] parts = apply.parseAreaGra();
                    List<TemplateGranularityDetailDto> selectList = Stream.of(parts)
                            .map(v -> tempGra.getDetails().stream().filter(d -> Objects.equals(d.getName(), v)).findAny())
                            .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
                    List<String> names = Lambda.buildList(selectList, TemplateGranularityDetailDto::getName);
                    if (selectList.size() != parts.length) {
                        String missing = Stream.of(parts).filter(v -> !names.contains(v)).collect(Collectors.joining(","));
                        throw new CustomException("市场信息 异常，粒度未配置: " + missing);
                    }

                    List<SelectFieldDto> selectFieldDtos =
                            selectList.stream()
                                    .flatMap(v -> Stream.of(v.getRequired(), v.getOptions()))
                                    .flatMap(v -> CollectionUtils.isEmpty(v) ? Stream.empty() : v.stream())
                                    .map(v -> SelectFieldDto.builder()
                                            .fieldId(v)
                                            .alias(fieldDictNameMap.get(v))
                                            .build()).collect(Collectors.toList());

                    ApplicationGranularityDto tmp = ApplicationGranularityDto.builder()
                            .granularity(tempGra.getGranularity())
                            .selectGranularity(names)
                            .fields(selectFieldDtos)
                            .filter(apply.getAreaFilter())
                            .build();
                    applyGras.add(tmp);
                } else {
                    if (CollectionUtils.isNotEmpty(tempGra.getDetails())) {
                        log.error("未填写粒度 {} 粒度数：{}", tempGra.getGranularity(), tempGra.getDetails().size());
                    }
                }
            }
            info.setGranularity(applyGras);

            // 默认勾选全部指标
            List<CustomMetricsLabelDto> labels = metricsIds.stream().map(v -> {
                CustomMetricsLabelDto dto = new CustomMetricsLabelDto();
                dto.setMetricsId(v);
                dto.setAlias(metricsMap.get(v));
                dto.setSelect(true);
                return dto;
            }).collect(Collectors.toList());
            info.setCustomMetrics(labels);

            info.setRequireAttr(RequireAttrType.getByTransferDesc(apply.getRequireAttr()));
            info.setContractNo(Optional.ofNullable(apply.getContractNo()).filter(v -> StringUtils.isNotBlank(v) && !v.contains("NULL")).orElse(null));

            if (BooleanUtils.isTrue(transferProperties.getCloseRound())) {
                MDC.put(CommonConstants.REMOVE_ROUND, "RRRRRemove");
            }
            AjaxResult tmp;
            if (BooleanUtils.isTrue(request.getDebug())) {
                tmp = this.tryApplication(info);
            } else {
                tmp = this.addTemplateApplication(info);
            }

            log.info("create: tmp={}", tmp);

            if (tmp.isSuccess()) {
                Optional.ofNullable(tmp).map(AjaxResult::getData).map(v -> {
                    if (v instanceof TgApplicationInfo) {
                        return (TgApplicationInfo) v;
                    }
                    log.warn("type not match: v={}", v);
                    return null;
                }).map(TgApplicationInfo::getId).ifPresent(v -> redisTemplate.<Long, Long>opsForHash().put(RedisKeys.Apply.TRANS_TAIL_APPLY_MAP, apply.getId(), v)
                );
            } else {
                log.error("tmp={}", tmp);
            }
        }

        return AjaxResult.succeed();
    }


    private TemplateCtx queryTemplate(String name) {
        TemplateCtx cache = templateCache.get(name);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        List<TgTemplateInfo> list = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                .eq(TgTemplateInfo::getTemplateName, name));
        if (CollectionUtils.size(list) == 1) {
            TgTemplateInfo template = list.get(0);

            TgAssetInfo asset = assetService.queryOne(template.getId(), AssetType.MODEL);
            if (Objects.isNull(asset)) {
                throw new CustomException("长尾模板未上架");
            }

            List<TableFieldInfo> fields = tableFieldInfoService.getFieldsByTableId(template.getBaseTableId());
            List<Long> fieldDictIds = Lambda.buildList(fields, TableFieldInfo::getRelationColId);
            List<FieldDict> fieldDictList = Lambda.queryListIfExist(fieldDictIds, fieldDictDAO.getBaseMapper()::selectBatchIds);
            Map<Long, String> fieldDictNameMap = Lambda.buildMap(fieldDictList, FieldDict::getId, FieldDict::getName);

            fieldDictNameMap.putAll(ApplicationConst.PeriodField.idToNameMap);

            JsonBeanConverter.convert2Obj(template);

            List<Long> metricsIds = Lambda.buildList(template.getCustomMetrics(), CustomMetricsLabelDto::getMetricsId);
            List<MetricsDict> metrics = metricsDictDAO.getBaseMapper().selectList(new QueryWrapper<MetricsDict>()
                    .lambda().in(MetricsDict::getId, metricsIds));
            Map<Long, String> metricsMap = Lambda.buildMap(metrics, MetricsDict::getId, MetricsDict::getName);


            TemplateCtx ctx = TemplateCtx.builder()
                    .templateInfo(template)
                    .asset(asset)
                    .fieldDictNameMap(fieldDictNameMap)
                    .metricsIds(metricsIds)
                    .metricsMap(metricsMap)
                    .build();
            templateCache.put(name, ctx);
            return ctx;
        }
        log.info("list={}", list);
        throw new CustomException("提数模板错误 " + name);
    }

    private TgAssetInfo queryAssets(Long tempId) {
        TgAssetInfo cache = assetsCache.get(tempId);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        TgAssetInfo asset = assetService.queryOne(tempId, AssetType.MODEL);
        if (Objects.isNull(asset)) {
            throw new CustomException("常规模板未上架");
        }
        assetsCache.put(tempId, asset);
        return asset;
    }


    /**
     * 模拟Token认证
     */
    private void fillUserAuthById(HttpServletRequest request, Long userId) {
        SysUser sysUser = userService.selectUserById(userId);
        ThreadContextHolder.setSysUser(sysUser);
        UserDetails userDetails = userDetailsService.loadUserByUsername(sysUser.getUserName());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        SinoPassUserDTO orgUserInfo;
        try {
            if (ObjectUtils.isNotNull(sysUser.getOrgUserId())) {
                orgUserInfo = Optional.ofNullable(SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId())).orElse(createEmptyOrgUser());
            } else {
                orgUserInfo = createEmptyOrgUser();
            }
        } catch (NullPointerException e) {
            log.error("异常捕获", e);
            orgUserInfo = createEmptyOrgUser();
        }

        ThreadContextHolder.getParams().put(CommonConstants.ORG_USER_INFO, orgUserInfo);
    }

    private SinoPassUserDTO createEmptyOrgUser() {
        return new SinoPassUserDTO();
    }

    /**
     *
     */
    @Override
    public AjaxResult<Void> auditAllSku(Integer batch, Long no) {
        return this.auditAll(batch, no, RedisKeys.Apply.TRANS_APPLY_MAP, false);
    }

    @Override
    public AjaxResult<Void> auditAllTail(Integer batch, Long no) {
        return this.auditAll(batch, no, RedisKeys.Apply.TRANS_TAIL_APPLY_MAP, false);
    }

    private BizScheduledThreadPoolExecutor pool = null;

    @Override
    public AjaxResult<Void> stopFlowSchedulerPool() {
        if (Objects.isNull(pool)) {
            return AjaxResult.error("线程池未启动");
        }
        List<Runnable> wait = pool.shutdownNow();
        pool = null;
        if (CollectionUtils.isNotEmpty(wait)) {
            log.error("Shutdown: wait={}", wait.size());
            return AjaxResult.error("wait " + wait.size());
        }
        return AjaxResult.success("stop success", null);
    }

    /**
     * 限制并发数的前提下，等工作流跑完一个就补一个，直到跑完指定的batch数
     */
    public AjaxResult<Void> auditFlowSchedule(Integer batch, Long no, Integer con) {
        return auditFlow(batch, no, con, RedisKeys.FlowApply.TRANS_APPLY_MAP);
    }

    public AjaxResult<Void> auditRangeFlowSchedule(Integer batch, Long no, Integer con) {
        return auditFlow(batch, no, con, RedisKeys.RangeApply.TRANS_APPLY_MAP);
    }

    public AjaxResult<Void> auditInsFlowSchedule(Integer batch, Long no, Integer con) {
        return auditFlow(batch, no, con, RedisKeys.InCompleteCustomApply.TRANS_APPLY_MAP);
    }

    @Override
    public AjaxResult<Void> auditCusFlowSchedule(Integer batch, Long no, Integer con) {
        return auditFlow(batch, no, con, RedisKeys.NormalCustomApply.TRANS_APPLY_MAP);
    }

    private AjaxResult<Void> auditFlow(Integer batch, Long no, Integer con, String applyKey) {
        int core = Optional.ofNullable(con).orElse(3);
        if (Objects.isNull(pool)) {
            pool = new BizScheduledThreadPoolExecutor(core, "flow-%d");
        }

        Map all = redisTemplate.opsForHash().entries(applyKey);
        if (MapUtils.isEmpty(all)) {
            return AjaxResult.error("无待审核的数据");
        }
        if (Objects.nonNull(no)) {
            Object applyId = redisTemplate.opsForHash().get(applyKey, no.intValue());
            if (Objects.nonNull(applyId)) {
                auditOneCache((Long) applyId, no);
                return AjaxResult.succeed();
            } else {
                return AjaxResult.error("no 找不到对应的申请");
            }
        }
        if (Objects.isNull(batch)) {
            return AjaxResult.error("batch 参数缺失");
        }

        AtomicInteger count = new AtomicInteger();
        Semaphore semaphore = new Semaphore(core);

        // TODO 硬编码处理的数据
        List<Integer> care = Arrays.asList(97, 98, 99, 100, 101, 102, 103, 104, 105, 106);

        for (Object o : all.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            // excel id
            Object k = entry.getKey();
            // 申请id
            Object v = entry.getValue();
            if (!care.contains(k)) {
                continue;
            }

            try {
                Boolean member = redisTemplate.opsForSet().isMember(RedisKeys.Apply.AUDIT_APPLY_SET, v);
                if (BooleanUtils.isTrue(member)) {
                    continue;
                }
                int idx = count.incrementAndGet();
                if (idx > batch) {
                    return AjaxResult.error("over limit");
                }
                semaphore.acquire();
                Long applyId = (Long) v;
                String result = auditOneCache(applyId, k);
                if (Objects.isNull(result)) {
                    semaphore.release();
                    continue;
                }

                pool.scheduleAtFixedRate(() -> {
                    TgApplicationInfo apply = applicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
                            .select(TgApplicationInfo::getDataState)
                            .eq(TgApplicationInfo::getId, applyId));

                    int sec = LocalDateTime.now().getSecond();
                    if (sec > 30 && sec < 40) {
                        log.info("RUN: idx={} applyId={}", idx, applyId);
                    }
                    if (Objects.nonNull(apply)
                            && (Objects.equals(apply.getDataState(), ApplyDataStateEnum.success.name()) || Objects.equals(apply.getDataState(), ApplyDataStateEnum.fail.name()))) {
                        semaphore.release();
                        if (semaphore.getQueueLength() == 0) {
                            throw new RuntimeException("工作流全部完成 " + idx);
                        }
                        throw new RuntimeException("工作流完成 " + idx);
                    }
                }, 30, 5, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("", e);
            }
        }

        return AjaxResult.success("所有工作流已完成审核", null);
    }

    private AjaxResult<Void> auditAll(Integer batch, Long no, String applyKey, boolean brand) {
        Map all = redisTemplate.opsForHash().entries(applyKey);
        if (MapUtils.isEmpty(all)) {
            return AjaxResult.error("无待审核的数据");
        }

        //TODO 硬过滤
        List<Long> care = Arrays.asList(302L, 303L, 304L, 305L, 306L, 307L, 308L, 309L, 310L, 311L, 312L, 313L, 314L, 315L, 316L, 317L, 318L, 319L, 320L, 321L);
        if (Objects.nonNull(no)) {
            Object applyId = redisTemplate.opsForHash().get(applyKey, no);
            if (Objects.nonNull(applyId)) {
                auditOneCache((Long) applyId, no);
                return AjaxResult.succeed();
            } else {
                return AjaxResult.error("no 找不到对应的申请");
            }
        }
        if (Objects.isNull(batch)) {
            return AjaxResult.error("batch 参数缺失");
        }

        AtomicInteger count = new AtomicInteger();
        for (Object o : all.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            // excel id
            Object k = entry.getKey();
            // 申请id
            Object v = entry.getValue();
            if (!care.contains(k)) {
                continue;
            }
            try {
                Boolean member = redisTemplate.opsForSet().isMember(RedisKeys.Apply.AUDIT_APPLY_SET, v);
                if (BooleanUtils.isTrue(member)) {
                    continue;
                }
                if (count.incrementAndGet() > batch) {
                    return AjaxResult.error("over limit");
                }
                auditOneCache((Long) v, k);
            } catch (Exception e) {
                log.error("", e);
            }
        }

        return AjaxResult.success("所有数据已完成审核", null);
    }

    public String auditOneCache(Long applyId, Object no) {
        AuditRequest dto = new AuditRequest();
        dto.setApplicationId(applyId);
        dto.setIndex(0);
        dto.setConfirmData(true);
        dto.setStatus(ApplicationConst.AuditStatus.AUDIT_PASS);

        log.info("AUDIT START: id={} applyId={}", no, applyId);
        Object result = auditProcessService.auditProcess(dto);
        log.warn("AUDIT PASS: id={} applyId={} result={}", no, applyId, result);

        redisTemplate.opsForSet().add(RedisKeys.Apply.AUDIT_APPLY_SET, applyId);

        // 申请单不存在 提交申请的时候事务回滚了 但是redis有数据
        if (Objects.isNull(result)) {
            return null;
        }

        Optional<TgApplicationInfo> infoOpt = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getTemplateId)
                .eq(TgApplicationInfo::getId, applyId)
                .oneOpt();

        Boolean common = infoOpt.map(TgApplicationInfo::getTemplateId)
                .map(v -> templateInfoDAO.getById(v))
                .map(TgTemplateInfo::getTemplateType)
                .map(v -> Objects.equals(TemplateTypeEnum.customized.name(), v))
                .orElse(false);
        if (common) {
            AjaxResult exeResult = userDataAssetsService.executeWorkFlow(applyId);
            log.info("exeResult={}", exeResult);
        }

        // 线上不进行验收, 测试环境做自动比对
//        if (BooleanUtils.isNotTrue(transferProperties.getProd())) {
//            scheduler.schedule(() -> {
//                compareAdapter.compareAssetsForSku(Long.parseLong(no.toString()), false, null);
//            }, 10, TimeUnit.SECONDS);
//        }
        return "OK";
    }

    private void validateFilter(FilterDTO filter) {
        FilterDTO.FilterItemDTO item = filter.getFilterItem();
        if (Objects.nonNull(item)) {
            if (Objects.isNull(item.getFieldId())) {
                throw new CustomException("id为空: " + item.getFieldName());
            }

            if (CollectionUtils.isNotEmpty(item.getFilters())) {
                for (FilterDTO itemFilter : item.getFilters()) {
                    validateFilter(itemFilter);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(filter.getFilters())) {
            for (FilterDTO son : filter.getFilters()) {
                validateFilter(son);
            }
        }
    }

    /**
     * 分页查询库表类型的申请
     */
    @Override
    public AjaxResult<PageInfo<ApplicationManageTableListVo>> pageQueryTableApplication(PageQueryTableApplicationRequest pageRequest) {
        Page<ApplicationManageTableListVo> page = mapper.pageQueryTableApplication(new Page(pageRequest.getPageNum(), pageRequest.getPageSize()),
                pageRequest, SecurityUtils.getUserId());
        List<ApplicationManageTableListVo> records = page.getRecords();
        records.forEach(item -> {
            item.setCurrentHandlersCn(convertHandlesCn(item.getCurrentHandlers()));
            item.setFinalHandler(buildFinalHandles(item.getCurrentIndex(), item.getHandleNodeJson()));
        });
        return AjaxResult.success(PageUtil.convert(page));
    }

    /**
     * 分页查询文件类型的申请
     */
    @Override
    public AjaxResult<PageInfo<ApplicationManageFileListVo>> pageQueryFileApplication(PageQueryFileApplicationRequest pageRequest) {
        Page<ApplicationManageFileListVo> page = mapper.pageQueryFileApplication(new Page(pageRequest.getPageNum(), pageRequest.getPageSize()),
                pageRequest, SecurityUtils.getUserId());
        List<ApplicationManageFileListVo> records = page.getRecords();
        records.stream().forEach(item -> {
            item.setCurrentHandlersCn(convertHandlesCn(item.getCurrentHandlers()));
            item.setFinalHandler(buildFinalHandles(item.getCurrentIndex(), item.getHandleNodeJson()));
            item.setServiceType(buildFileServiceType(item.getDocAuthorizationJson()));
        });
        return AjaxResult.success(PageUtil.convert(page));
    }

    /**
     * 分页查询模型的申请
     */
    @Override
    public AjaxResult<PageInfo<ApplicationManageModelListVo>> pageQueryModelApplication(PageQueryModelApplicationRequest pageRequest) {
        Page<ApplicationManageModelListVo> page = mapper.pageQueryModelApplication(new Page(pageRequest.getPageNum(), pageRequest.getPageSize()),
                pageRequest, SecurityUtils.getUserId());
        List<ApplicationManageModelListVo> records = page.getRecords();
        records.stream().forEach(item -> {
            item.setCurrentHandlersCn(convertHandlesCn(item.getCurrentHandlers()));
            item.setFinalHandler(buildFinalHandles(item.getCurrentIndex(), item.getHandleNodeJson()));
        });
        return AjaxResult.success(PageUtil.convert(page));
    }

    @Override
    public List<TgApplicationInfo> getValidApplicationByAssetIdAndUserId(Long assetId, Long userId) {
        String currentTime = DateUtils.getTime();
        return mapper.getValidApplicationByAssetIdAndUserId(assetId, userId, currentTime);
    }

    @Override
    public List<TgApplicationInfo> queryApplicationByApplicantIdAndName(List<Long> assetIds, Long userId) {
        String currentTime = DateUtils.getTime();
        return mapper.getValidApplicationByAssetIdsAndUserId(assetIds, userId, currentTime);
    }

    @Override
    public List<TgApplicationInfo> queryOldApplicationWithoutNewAssetId() {
        return TgApplicationInfo.newInstance().selectList(new QueryWrapper<TgApplicationInfo>() {{
            isNull("new_asset_id");
        }});
    }

    /**
     * 转换文件申请类型
     */
    private String buildFileServiceType(String docAuthorizationJson) {
        if (StringUtils.isBlank(docAuthorizationJson)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        JSONArray jsonArray = JSONObject.parseArray(docAuthorizationJson);
        if (jsonArray != null && jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                Integer auth = jsonArray.getInteger(i);
                if (auth.equals(DataDirConst.DocPermission.CAN_VIEW_PDF)) {
                    sb.append("文件阅读/");
                } else if (auth.equals(DataDirConst.DocPermission.CAN_DOWNLOAD_PDF)) {
                    sb.append("文件PDF下载/");
                } else if (auth.equals(DataDirConst.DocPermission.CAN_DOWNLOAD_SRC)) {
                    sb.append("源文件下载");
                }
            }
            return StringUtils.removeEnd(sb.toString(), "/");
        }
        return null;
    }

    /**
     * 获取最后审批人
     */
    private String buildFinalHandles(Long currentIndex, String handleNodeJson) {
        if (currentIndex == null || StringUtils.isBlank(handleNodeJson)) {
            return null;
        }
        JSONArray jsonArray = JSONObject.parseArray(handleNodeJson);
        if (jsonArray != null && !jsonArray.isEmpty()) {
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null) {
                    if (currentIndex.equals(jsonObject.getLong("index"))
                            && StringUtils.isNotBlank(jsonObject.getString("handlerName"))) {
                        return org.apache.commons.lang3.StringUtils
                                .substringAfter(jsonObject.getString("handlerName"), "-");
                    }
                }
            }
        }
        return null;
    }

    /**
     * 当前处理人 转换为 中文名称
     */
    private String convertHandlesCn(String userIdStr) {
        if (StringUtils.isBlank(userIdStr)) {
            return null;
        }

        List<Long> userIds = Arrays.stream(userIdStr.split(",")).map(Long::valueOf).distinct().collect(Collectors.toList());
        Map<Long, String> nameMap = Lambda.queryMapIfExist(userIds, userService::selectUserNameMapByIds);
        return userIds.stream().map(nameMap::get).collect(Collectors.joining(","));
    }

    @Override
    public AjaxResult<List<FlowAssetsPageDTO>> pageQueryRelateApply(FlowAssetsPageRequest request, List<Long> applyIds) {
        if (CollectionUtils.isEmpty(applyIds)) {
            return AjaxResult.success(Collections.emptyList());
        }
        LambdaQueryChainWrapper<TgApplicationInfo> wrapper = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getTemplateId, TgApplicationInfo::getProjectName,
                        TgApplicationInfo::getApplicationNo, TgApplicationInfo::getApplicantId,
                        TgApplicationInfo::getApplicantName, TgApplicationInfo::getCreateTime,
                        TgApplicationInfo::getRequireTimeType, TgApplicationInfo::getAssetsId, TgApplicationInfo::getDeliverTimeType)
//                .like(StringUtils.isNotBlank(request.getProjectName()), TgApplicationInfo::getProjectName, request.getProjectName())
                .eq(Objects.nonNull(request.getRequireTimeType()), TgApplicationInfo::getRequireTimeType, request.getRequireTimeType())
                .eq(StringUtils.isNotBlank(request.getDeliverTimeType()), TgApplicationInfo::getDeliverTimeType, request.getDeliverTimeType())
                .in(TgApplicationInfo::getId, applyIds);

        wrapper.orderByDesc(TgApplicationInfo::getCreateTime);
        List<TgApplicationInfo> records = wrapper.list();

        Set<Long> assetsIds = Lambda.buildSet(records, TgApplicationInfo::getAssetsId);
        Map<Long, UserDataAssets> assetsMap = Lambda.queryMapIfExist(assetsIds, v -> userDataAssetsDAO.lambdaQuery()
                .select(UserDataAssets::getId, UserDataAssets::getProdCode)
                .in(UserDataAssets::getId, v)
                .list(), UserDataAssets::getId);

        Map<Long, Long> projectMap = Lambda.queryMapIfExist(assetsIds, v ->
                        projectAssetsMapper.selectList(new QueryWrapper<ProjectDataAssetsRelate>().lambda()
                                .in(ProjectDataAssetsRelate::getUserAssetId, v)),
                ProjectDataAssetsRelate::getUserAssetId, ProjectDataAssetsRelate::getProjectId);
        Map<Long, String> proNameMap = Lambda.queryMapIfExist(projectMap.values(), projectDAO::listByIds,
                Project::getId, Project::getName);

        Set<Long> tempIds = Lambda.buildSet(records, TgApplicationInfo::getTemplateId);
        Map<Long, String> tempNameMap = templateInfoDAO.queryNameMap(tempIds);

        return AjaxResult.success(records.stream().map(v -> {
                    String projectName = Optional.ofNullable(v.getAssetsId()).map(projectMap::get)
                            .map(proNameMap::get).orElse(null);
                    String prodCode = Optional.ofNullable(v.getAssetsId()).map(assetsMap::get)
                            .map(UserDataAssets::getProdCode).orElse("");
                    return FlowAssetsPageDTO.builder()
                            .applyId(v.getId())
                            .templateId(v.getTemplateId())
                            .assetsId(v.getAssetsId())
                            .applicationNo(v.getApplicationNo())
                            .projectName(v.getProjectName())
                            .newProjectName(projectName)
                            .templateName(tempNameMap.get(v.getTemplateId()))
                            .prodCode(prodCode)
                            .applicant(v.getApplicantName())
                            .applyTime(v.getCreateTime())
                            .requireTimeType(v.getRequireTimeType())
                            .deliverTimeType(v.getDeliverTimeType())
                            .build();
                }).filter(v -> {
                    if (StringUtils.isBlank(request.getProjectName())) {
                        return true;
                    }

                    if (v.getProjectName().contains(request.getProjectName())
                            || v.getNewProjectName().contains(request.getProjectName())) {
                        return true;
                    }

                    return false;
                })
                .collect(Collectors.toList()));
    }

    @Override
    public AjaxResult<List<FlowAssetsPageDTO>> pageQueryRelateApply(FlowAssetsAutoPageRequest request, List<Long> applyIds) {
        if (CollectionUtils.isEmpty(applyIds)) {
            return AjaxResult.success(Collections.emptyList());
        }
        LambdaQueryChainWrapper<TgApplicationInfo> wrapper = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getTemplateId, TgApplicationInfo::getProjectName,
                        TgApplicationInfo::getApplicationNo, TgApplicationInfo::getApplicantId,
                        TgApplicationInfo::getApplicantName, TgApplicationInfo::getCreateTime,
                        TgApplicationInfo::getRequireTimeType, TgApplicationInfo::getAssetsId, TgApplicationInfo::getDeliverTimeType)
//                .like(StringUtils.isNotBlank(request.getProjectName()), TgApplicationInfo::getProjectName, request.getProjectName())
                .in(CollectionUtils.isNotEmpty(request.getRequireTimeType()), TgApplicationInfo::getRequireTimeType, request.getRequireTimeType())
                .in(CollectionUtils.isNotEmpty(request.getDeliverTimeType()), TgApplicationInfo::getDeliverTimeType, request.getDeliverTimeType())
                .in(TgApplicationInfo::getId, applyIds);

        wrapper.orderByDesc(TgApplicationInfo::getCreateTime);
        List<TgApplicationInfo> records = wrapper.list();

        Set<Long> assetsIds = Lambda.buildSet(records, TgApplicationInfo::getAssetsId);
        Map<Long, UserDataAssets> assetsMap = Lambda.queryMapIfExist(assetsIds, v -> userDataAssetsDAO.lambdaQuery()
                        .select(UserDataAssets::getId, UserDataAssets::getProdCode)
                        .in(UserDataAssets::getId, v)
                        .list(),
                UserDataAssets::getId);

        Map<Long, Long> projectMap = Lambda.queryMapIfExist(assetsIds, v ->
                        projectAssetsMapper.selectList(new QueryWrapper<ProjectDataAssetsRelate>().lambda()
                                .in(ProjectDataAssetsRelate::getUserAssetId, v)),
                ProjectDataAssetsRelate::getUserAssetId, ProjectDataAssetsRelate::getProjectId);
        Map<Long, String> proNameMap = Lambda.queryMapIfExist(projectMap.values(), projectDAO::listByIds,
                Project::getId, Project::getName);

        Set<Long> tempIds = Lambda.buildSet(records, TgApplicationInfo::getTemplateId);
        Map<Long, String> tempNameMap = templateInfoDAO.queryNameMap(tempIds);

        return AjaxResult.success(records.stream().map(v -> {
                    String projectName = Optional.ofNullable(v.getAssetsId()).map(projectMap::get)
                            .map(proNameMap::get).orElse(null);
                    String prodCode = Optional.ofNullable(v.getAssetsId()).map(assetsMap::get)
                            .map(UserDataAssets::getProdCode).orElse("");
                    return FlowAssetsPageDTO.builder()
                            .applyId(v.getId())
                            .templateId(v.getTemplateId())
                            .assetsId(v.getAssetsId())
                            .applicationNo(v.getApplicationNo())
                            .projectName(v.getProjectName())
                            .newProjectName(projectName)
                            .templateName(tempNameMap.get(v.getTemplateId()))
                            .prodCode(prodCode)
                            .applicant(v.getApplicantName())
                            .applyTime(v.getCreateTime())
                            .requireTimeType(v.getRequireTimeType())
                            .deliverTimeType(v.getDeliverTimeType())
                            .build();
                }).filter(v -> {
                    String projectName = request.getProjectName();
                    if (StringUtils.isBlank(projectName)) {
                        return true;
                    }

                    return v.getProjectName().contains(projectName)
                            || v.getNewProjectName().contains(projectName);
                })
                .collect(Collectors.toList()));
    }
}
