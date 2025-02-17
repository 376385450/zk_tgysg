package com.sinohealth.system.biz.dataassets.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.annotation.RegisterCronMethod;
import com.sinohealth.common.config.PowerBiPushProperties;
import com.sinohealth.common.constant.LogConstant;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.enums.process.FlowProcessCategory;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.framework.config.ContextCopyingTaskDecorator;
import com.sinohealth.system.biz.application.constants.FieldType;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.dto.PushMappingField;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.dataassets.dao.PowerBiPushBatchDAO;
import com.sinohealth.system.biz.dataassets.dao.PowerBiPushDetailDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.PowerBiPushBatch;
import com.sinohealth.system.biz.dataassets.domain.PowerBiPushDetail;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;
import com.sinohealth.system.biz.dataassets.dto.FlowAssetsPageDTO;
import com.sinohealth.system.biz.dataassets.dto.PowerBiPushBatchPageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.DataPreviewRequest;
import com.sinohealth.system.biz.dataassets.dto.request.FlowAssetsPageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.PowerBiPushCreateRequest;
import com.sinohealth.system.biz.dataassets.dto.request.PowerBiPushPageRequest;
import com.sinohealth.system.biz.dataassets.mapper.PowerBiPushBatchMapper;
import com.sinohealth.system.biz.dataassets.service.PowerBiPushService;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.biz.dict.dao.MetricsDictDAO;
import com.sinohealth.system.biz.dict.domain.MetricsDict;
import com.sinohealth.system.biz.process.dao.TgFlowProcessManagementDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessManagement;
import com.sinohealth.system.biz.process.facade.TgFlowProcessAlertFacade;
import com.sinohealth.system.biz.table.facade.TableInfoSnapshotCompareFacade;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.dto.ApplicationDataDto;
import com.sinohealth.system.mapper.PowerBIMapper;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ITableFieldInfoService;
import com.sinohealth.system.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 10:03
 */
@Slf4j
@Service
public class PowerBiPushServiceImpl implements PowerBiPushService {


    @Autowired
    private PowerBiPushProperties pushProp;

    @Autowired
    private TemplateInfoDAO templateInfoDAO;
    @Autowired
    private ApplicationDAO applicationDAO;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;
    @Resource
    private PowerBiPushBatchDAO batchDao;
    @Resource
    private PowerBiPushBatchMapper batchMapper;
    @Resource
    private PowerBiPushDetailDAO detailDao;
    @Resource
    private PowerBIMapper powerBIMapper;
    @Resource
    private MetricsDictDAO metricsDictDAO;
    @Autowired
    private TgFlowProcessManagementDAO tgFlowProcessManagementDAO;

    @Resource
    private CKClusterAdapter ckClusterAdapter;

    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER)
    private ScheduledExecutorService scheduler;
    @Resource
    @Qualifier(ThreadPoolType.ENHANCED_TTL)
    private Executor ttl;

    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ITableFieldInfoService tableFieldInfoService;
    @Autowired
    private UserDataAssetsService userDataAssetsService;
    @Autowired
    private TgFlowProcessAlertFacade tgFlowProcessAlertFacade;
    @Autowired
    private RedisTemplate redisTemplate;

    @RegisterCronMethod
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void runningScheduled() {
        // 确定有没有锁
        synchronized (TableInfoSnapshotCompareFacade.class) {
            if (TableInfoSnapshotCompareFacade.run) {
                log.warn("ignore push powerbi via table compare");
                return;
            }
        }


        Boolean lock = redisTemplate.opsForValue().setIfAbsent(RedisKeys.PowerBi.STATE_LOCK_KEY, 1, 1,
                TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(lock)) {
            refreshPushState();
        }
    }


    @Override
    public AjaxResult<List<FlowAssetsPageDTO>> pageQueryAssets(FlowAssetsPageRequest request) {
        if (CollectionUtils.isEmpty(request.getTemplateIds()) && Objects.isNull(request.getBatchId())) {
            return AjaxResult.error("参数缺失");
        }

        if (CollectionUtils.isNotEmpty(request.getTemplateIds())) {
            return AjaxResult.success(userDataAssetsService.listForCreate(request));
        } else {
            // 历史批次 下 明细展示
            List<PowerBiPushDetail> details = detailDao.lambdaQuery()
                    .select(PowerBiPushDetail::getId, PowerBiPushDetail::getApplicationId)
                    .eq(PowerBiPushDetail::getBatchId, request.getBatchId()).list();
            List<Long> applyIds = Lambda.buildList(details, PowerBiPushDetail::getApplicationId);
            return applicationService.pageQueryRelateApply(request, applyIds);
        }
    }

    @Override
    public AjaxResult<IPage<PowerBiPushBatchPageDTO>> pageQuery(PowerBiPushPageRequest request) {
        IPage<PowerBiPushBatch> batchPage = batchMapper.pageQuery(request.buildPage(), request);

        List<PowerBiPushBatch> records = batchPage.getRecords();
        Set<Long> userIds = Lambda.buildSet(records, PowerBiPushBatch::getCreator);
        Map<Long, String> userNameMap = Lambda.queryMapIfExist(userIds, sysUserService::selectUserNameMapByIds);

//        this.refreshPushState(records);

        Set<Long> batchIds = Lambda.buildSet(records, PowerBiPushBatch::getId);
        Map<Long, List<PowerBiPushDetail>> detailMap = detailDao.queryStateDetailByBatch(batchIds);

        List<Long> bizIds = Lambda.buildNonNullList(records, PowerBiPushBatch::getBizId);
        Map<Long, TgFlowProcessManagement> flowMap = tgFlowProcessManagementDAO.queryForPageList(bizIds);

        return AjaxResult.success(PageUtil.convertMap(batchPage, x -> {
            PowerBiPushBatchPageDTO dto = new PowerBiPushBatchPageDTO();
            BeanUtils.copyProperties(x, dto);

            List<PowerBiPushDetail> details = detailMap.getOrDefault(x.getId(), Collections.emptyList());
            Map<String, Long> stateMap = details.stream().collect(Collectors.groupingBy(PowerBiPushDetail::getState, Collectors.counting()));
            dto.setRunCnt(Optional.ofNullable(stateMap.get(AssetsUpgradeStateEnum.running.name())).map(Long::intValue).orElse(0));
            dto.setWaitCnt(Optional.ofNullable(stateMap.get(AssetsUpgradeStateEnum.wait.name())).map(Long::intValue).orElse(0));
            dto.setSuccessCnt(Optional.ofNullable(stateMap.get(AssetsUpgradeStateEnum.success.name())).map(Long::intValue).orElse(0));
            dto.setFailedCnt(Optional.ofNullable(stateMap.get(AssetsUpgradeStateEnum.failed.name())).map(Long::intValue).orElse(0));
            dto.setDetailCnt(details.size());

            LocalDateTime firstStart = details.stream().filter(Objects::nonNull).map(PowerBiPushDetail::getStartTime).filter(Objects::nonNull)
                    .min(LocalDateTime::compareTo).orElse(null);
            dto.setStartTime(firstStart);

            LocalDateTime lastEnd = details.stream().filter(Objects::nonNull).map(PowerBiPushDetail::getFinishTime).filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo).orElse(null);
            dto.setFinishTime(lastEnd);

            Optional<TgFlowProcessManagement> flowOpt = Optional.ofNullable(flowMap.get(x.getBizId()));
            dto.setFlowProcessCategory(flowOpt.map(e -> FlowProcessCategory.AUTO.getCode()).orElse(FlowProcessCategory.MANUAL_OPERATION.getCode()));
            dto.setFlowProcessName(flowOpt.map(TgFlowProcessManagement::getName).orElse(""));

            dto.setCreatorName(Optional.ofNullable(userNameMap.get(x.getCreator())).orElse("系统"));
            dto.setCostTime(DateUtil.caluLocalDateTimeDiff(dto.getStartTime(), dto.getFinishTime()));
            return dto;
        }));
    }

    private void refreshPushState() {
        List<PowerBiPushBatch> runList = batchDao.lambdaQuery().eq(PowerBiPushBatch::getState,
                AssetsUpgradeStateEnum.running.name()).list();
        if (CollectionUtils.isEmpty(runList)) {
            return;
        }
        Set<Long> batchIds = Lambda.buildSet(runList, PowerBiPushBatch::getId);
        List<PowerBiPushDetail> details = detailDao.lambdaQuery()
                .select(PowerBiPushDetail::getId, PowerBiPushDetail::getBatchId, PowerBiPushDetail::getState, PowerBiPushDetail::getFinishTime)
                .in(PowerBiPushDetail::getBatchId, batchIds)
                .list();
        Map<Long, List<PowerBiPushDetail>> batchMap = details.stream()
                .collect(Collectors.groupingBy(PowerBiPushDetail::getBatchId));
        List<PowerBiPushBatch> success = new ArrayList<>();
        List<PowerBiPushBatch> fail = new ArrayList<>();

        for (PowerBiPushBatch batch : runList) {
            List<PowerBiPushDetail> detailList = batchMap.get(batch.getId());
            if (CollectionUtils.isEmpty(detailList)) {
                continue;
            }

            Map<String, Long> stateMap = detailList.stream()
                    .collect(Collectors.groupingBy(PowerBiPushDetail::getState, Collectors.counting()));
            Long waitCnt = stateMap.get(AssetsUpgradeStateEnum.wait.name());
            if (Objects.nonNull(waitCnt) && waitCnt > 0) {
                continue;
            }
            Long runCnt = stateMap.get(AssetsUpgradeStateEnum.running.name());
            if (Objects.nonNull(runCnt) && runCnt > 0) {
                continue;
            }

            detailList.stream().map(PowerBiPushDetail::getFinishTime)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .ifPresent(batch::setFinishTime);

            Long failedCnt = stateMap.get(AssetsUpgradeStateEnum.failed.name());
            if (Objects.nonNull(failedCnt) && failedCnt > 0) {
                batch.setState(AssetsUpgradeStateEnum.failed.name());
                fail.add(batch);
            } else {
                batch.setState(AssetsUpgradeStateEnum.success.name());
                success.add(batch);
            }
        }

        scheduler.schedule(() -> {
            for (PowerBiPushBatch batch : success) {
                boolean updateSuccess = batchDao.updateState(batch.getId(), batch.getFinishTime(),
                        AssetsUpgradeStateEnum.success);
                if (updateSuccess) {
                    tgFlowProcessAlertFacade.sendPowerBiAlert(batchDao.getById(batch.getId()));
                }
            }
            for (PowerBiPushBatch batch : fail) {
                boolean updateSuccess = batchDao.updateState(batch.getId(), batch.getFinishTime(), AssetsUpgradeStateEnum.failed);
                if (updateSuccess) {
                    tgFlowProcessAlertFacade.sendPowerBiAlert(batchDao.getById(batch.getId()));
                }
            }
        }, 3, TimeUnit.SECONDS);
    }

    /**
     * 保存到数据库后，提交到线程池即刻执行
     */
    @Override
    public AjaxResult<Void> createPush(PowerBiPushCreateRequest request) {
        List<UserDataAssets> assetsList = userDataAssetsDAO.queryRelateAssetsById(request.getAssetsIds());
        if (CollectionUtils.isEmpty(assetsList)) {
            log.info("no any assets");
            return AjaxResult.error("没有对应的资产可推送");
        }

        List<Long> templateIds = assetsList.stream().map(UserDataAssets::getTemplateId).distinct().collect(Collectors.toList());
        Integer nameCnt = batchDao.lambdaQuery().eq(PowerBiPushBatch::getName, request.getName()).count();
        if (nameCnt > 0) {
            return AjaxResult.error("推送任务重名");
        }

        List<TgTemplateInfo> tempList = templateInfoDAO.lambdaQuery()
                .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName, TgTemplateInfo::getPushPowerBi,
                        TgTemplateInfo::getTemplateType, TgTemplateInfo::getPushTableName, TgTemplateInfo::getPushFieldsJson)
                .in(TgTemplateInfo::getId, templateIds)
                .eq(TgTemplateInfo::getPushPowerBi, true)
                .list();
        if (CollectionUtils.isEmpty(tempList)) {
            log.info("no template");
            return AjaxResult.error("没有对应的模板可操作");
        }

        Map<Long, TgTemplateInfo> tempMap = new HashMap<>();
        for (TgTemplateInfo temp : tempList) {
            temp.setPushFields(Optional.ofNullable(JsonUtils.parseArray(temp.getPushFieldsJson(),
                    PushMappingField.class)).orElse(new ArrayList<>()));
            tempMap.put(temp.getId(), temp);
        }

        Set<Long> applyIds = Lambda.buildSet(assetsList, UserDataAssets::getSrcApplicationId);
        List<TgApplicationInfo> needPushApply = Lambda.queryListIfExist(applyIds, v -> applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getPushProjectName)
                .in(TgApplicationInfo::getId, v)
                .eq(TgApplicationInfo::getPushPowerBi, true)
                .list());
        if (CollectionUtils.isEmpty(needPushApply)) {
            log.info("not push by apply");
            return AjaxResult.error("没有符合条件的资产可推送.");
        }

        Map<Long, String> applyProMap = Lambda.buildMap(needPushApply, TgApplicationInfo::getId,
                TgApplicationInfo::getPushProjectName);
        List<UserDataAssets> pushAssets = assetsList.stream()
                .filter(v -> applyProMap.containsKey(v.getSrcApplicationId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(pushAssets)) {
            return AjaxResult.error("没有资产可推送");
        }

        Long userId = SecurityUtils.getUserIdOrLocal();
        String idStr = templateIds.stream().map(v -> v + "").collect(Collectors.joining(","));
        String nameStr = tempList.stream().map(TgTemplateInfo::getTemplateName).collect(Collectors.joining("、"));
        PowerBiPushBatch batch = new PowerBiPushBatch();
        batch.setName(request.getName()).setTemplateId(idStr).setTemplateName(nameStr)
                .setBizId(request.getBizId())
                .setState(AssetsUpgradeStateEnum.wait.name())
                .setDeleted(false)
                .setCreator(userId);
        batchDao.save(batch);
        Long batchId = batch.getId();

        scheduler.execute(() -> {
            log.info("异步创建任务: request={}", request);

            List<PowerBiPushDetail> details = new ArrayList<>();
            for (UserDataAssets assets : pushAssets) {
                TgTemplateInfo template = tempMap.get(assets.getTemplateId());
                if (Objects.isNull(template)) {
                    // 模板未开启开关，或者模板被删
                    log.warn("no valid template: {} assetsId:{}", assets.getTemplateId(), assets.getId());
                    continue;
                }

                List<String> pres = new ArrayList<>();
                List<String> inserts = new ArrayList<>();
                Pair<String, String> tablePair = parseSchema(template.getPushTableName());
                String finalProjectName = applyProMap.get(assets.getSrcApplicationId());

                String assetsSql = this.buildSyncAssets(assets.getId(), assets.getVersion(), assets.getAssetTableName(),
                        template, finalProjectName, tablePair.getKey(), tablePair.getValue());
                String projectSql = this.buildSyncProject(finalProjectName);
                inserts.add(projectSql);
                inserts.add(assetsSql);

                String delProject = this.buildProjectDelSql(finalProjectName);
                String delAssets = this.buildDelAssets(finalProjectName, tablePair.getKey(), tablePair.getValue());
                pres.add(delProject);
                pres.add(delAssets);

                PowerBiPushDetail detail = new PowerBiPushDetail()
                        .setBizId(request.getBizId())
                        .setAssetsId(assets.getId())
                        .setApplicationId(assets.getSrcApplicationId())
                        .setAssetsVer(assets.getVersion())
                        .setTableName(assets.getAssetTableName())
                        .setPushTableName(template.getPushTableName())
                        .setState(AssetsUpgradeStateEnum.wait.name())
                        .setPreSql(PowerBiPushDetail.mergeList(pres))
                        .setInsertSql(PowerBiPushDetail.mergeList(inserts));
                details.add(detail);
            }

            details.forEach(v -> v.setBatchId(batchId));
            detailDao.saveBatch(details);

            AjaxResult<Void> pushResult = this.doPush(batchId);
            log.info("push result: pushResult={}", pushResult);
        });
        return AjaxResult.succeed();
    }

    /**
     * @return INSERT INTO TABLE FUNCTION postgresql(...) (a, b,c) select 2, 'B', 4;
     */
    public String buildSyncAssets(Long assetsId, Integer version, String assetsTable,
                                  TgTemplateInfo temp, String finalProjectName,
                                  String schema, String tableName) {

        List<String> fieldAlias = new ArrayList<>();
        List<PushMappingField> fields = temp.getPushFields();
        if (Objects.equals(temp.getTemplateType(), TemplateTypeEnum.wide_table.name())) {
            this.fillWideField(assetsId, version, fields, fieldAlias);
        } else {
            for (PushMappingField field : fields) {
                fieldAlias.add("`" + field.getSrcName() + "` as " + field.getAliasName());
            }
        }

        String select = String.join(", ", fieldAlias);
        String fieldStr = fields.stream().map(PushMappingField::getAliasName).collect(Collectors.joining(","));
        fieldStr = "project_name,etl_date," + fieldStr;
        return "INSERT INTO TABLE FUNCTION " + buildPgFuncSql(schema, tableName) + " (" + fieldStr + ")" +
                " select '" + finalProjectName + "' as project_name, '" + DateUtils.getDate() + "' as etl_date, " + select
                + " FROM " + assetsTable;
    }

    /**
     * 处理宽表字段映射
     */
    private void fillWideField(Long assetsId, Integer version, List<PushMappingField> fields, List<String> fieldAlias) {
        DataPreviewRequest req = new DataPreviewRequest();
        req.setVersion(version);
        req.setPageSize(0);
        req.setAssetId(assetsId);
        AjaxResult<ApplicationDataDto> dataResult = applicationService.queryAssetsDataFromCk(assetsId, req);
        if (!dataResult.isSuccess()) {
            throw new CustomException("资产数据获取异常");
        }

        Set<Long> meIds = fields.stream().filter(v -> Objects.equals(v.getFieldType(), FieldType.METRIC_STR))
                .map(PushMappingField::getFieldId).collect(Collectors.toSet());
        Map<Long, MetricsDict> meMap = Lambda.queryMapIfExist(meIds, v -> metricsDictDAO.getBaseMapper().selectBatchIds(v), MetricsDict::getId);
        List<ApplicationDataDto.Header> header = dataResult.getData().getHeader();
        Optional<Long> tableIdOpt = header.stream().map(ApplicationDataDto.Header::getTableId).filter(Objects::nonNull)
                .filter(v -> v > 0).findAny();
        if (!tableIdOpt.isPresent()) {
            throw new CustomException("资产关联底表获取异常");
        }
        Map<Long, ApplicationDataDto.Header> headerMap = Lambda.buildMap(header, ApplicationDataDto.Header::getId);
        List<TableFieldInfo> tableFields = tableFieldInfoService.getFieldsByTableId(tableIdOpt.get());
        Map<Long, TableFieldInfo> fieldMap = Lambda.buildMap(tableFields, TableFieldInfo::getRelationColId);
        for (PushMappingField field : fields) {
            StringBuilder select = new StringBuilder();
            // 自定义 特殊字段
            if (Objects.isNull(field.getFieldId())) {
                select.append("`").append(field.getSrcName()).append("` as ").append(field.getAliasName());
                fieldAlias.add(select.toString());
                continue;
            }

            String srcName;
            if (Objects.equals(field.getFieldId(), ApplicationConst.PeriodField.PERIOD_TYPE_ID)) {
                srcName = ApplicationConst.PeriodField.PERIOD_TYPE;
            } else if (Objects.equals(field.getFieldType(), FieldType.METRIC_STR)) {
                MetricsDict me = meMap.get(field.getFieldId());
                if (Objects.isNull(me)) {
                    throw new CustomException("引用指标不存在: " + field.getFieldId());
                }
                srcName = me.getName();
            } else {
                srcName = Optional.ofNullable(fieldMap.get(field.getFieldId())).map(TableFieldInfo::getId)
                        .map(headerMap::get).map(ApplicationDataDto.Header::getFiledName)
                        .orElseGet(() -> {
                            // 字段库id 没有找到真实表字段信息
                            log.error("NO MAP: fieldId={}", field.getFieldId());
                            return field.getSrcName();
                        });

            }
            select.append("`").append(srcName).append("` as ").append(field.getAliasName());
            fieldAlias.add(select.toString());
        }
    }

    public Pair<String, String> parseSchema(String origin) {
        // 解析出schema
        String schema = "public";
        String tableName = origin;
        if (tableName.contains(".")) {
            String[] pair = tableName.split("\\.");
            if (pair.length > 1) {
                schema = pair[0];
                tableName = pair[1];
            }
        }

        return Pair.of(schema, tableName);
    }

    public String buildDelAssets(String projectName, String schema, String tableName) {
        return "delete from " + schema + "." + tableName + " where project_name='" + projectName + "'";
    }

    public String buildSyncProject(String finalProjectName) {
        return "INSERT INTO TABLE FUNCTION " + buildPgFuncSql(pushProp.getProjectTableSchema(), pushProp.getProjectTable())
                + "(project_name, etl_date) values('" + finalProjectName + "','" + DateUtils.getDate() + "')";
    }

    public String buildProjectDelSql(String finalProjectName) {
        return "delete from " + pushProp.getProjectTableSchema() + "." + pushProp.getProjectTable()
                + " where project_name = '" + finalProjectName + "'";
    }

    /**
     * https://clickhouse.com/docs/zh/engines/table-engines/integrations/postgresql
     * <p>
     * 只支持 Insert Select操作
     * select * from postgresql(...) where project_name = 'xx'
     */
    public String buildPgFuncSql(String schema, String table) {
        return String.format(" postgresql('%s', '%s', '%s', '%s', '%s', '%s') ",
                pushProp.getHost(), pushProp.getDb(), table, pushProp.getUser(), pushProp.getPwd(), schema);
    }

    @Override
    public AjaxResult<Void> retryPush(Long pushId) {
        if (Objects.isNull(pushId)) {
            return AjaxResult.error("推送任务不存在");
        }
        PowerBiPushBatch batch = batchDao.getBaseMapper().selectById(pushId);
        if (Objects.isNull(batch)) {
            return AjaxResult.error("推送任务不存在");
        }
        if (BooleanUtils.isTrue(batch.getDeleted())) {
            return AjaxResult.error("推送任务已删除");
        }
        if (!Objects.equals(batch.getState(), AssetsUpgradeStateEnum.failed.name())) {
            return AjaxResult.error("仅支持失败的推送任务重试");
        }

        List<PowerBiPushDetail> details = detailDao.lambdaQuery()
                .eq(PowerBiPushDetail::getBatchId, pushId)
                .eq(PowerBiPushDetail::getState, AssetsUpgradeStateEnum.failed.name())
                .list();
        if (CollectionUtils.isEmpty(details)) {
            return AjaxResult.error("无失败资产可重试");
        }

        Set<Long> detailIds = Lambda.buildSet(details, PowerBiPushDetail::getId);
        batchDao.updateState(pushId, AssetsUpgradeStateEnum.running);
        detailDao.lambdaUpdate()
                .set(PowerBiPushDetail::getState, AssetsUpgradeStateEnum.running.name())
                .in(PowerBiPushDetail::getId, detailIds)
                .update();

        // 延迟执行，并加入线程池 限流执行
        scheduler.schedule(() -> {
            Set<Long> assetIds = Lambda.buildSet(details, PowerBiPushDetail::getAssetsId);
            Map<Long, String> assetsNameMap = userDataAssetsDAO.queryAssetsName(assetIds);
            Map<Long, String> failMap = this.executePreSQL(details);
            for (PowerBiPushDetail detail : details) {
                ttl.execute(() -> this.executeInsertSQL(detail, assetsNameMap, failMap));
            }

            log.info("finish retry");
        }, 10, TimeUnit.SECONDS);

        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> replayPush(Long pushId) {
        if (Objects.isNull(pushId)) {
            return AjaxResult.error("推送任务不存在");
        }

        return this.doPush(pushId);
    }

    private AjaxResult<Void> doPush(Long pushId) {
        log.info("start push: pushId={}", pushId);
        PowerBiPushBatch batch = batchDao.getById(pushId);
        if (Objects.isNull(batch)) {
            return AjaxResult.error("推送任务不存在 " + pushId);
        }
        if (BooleanUtils.isTrue(batch.getDeleted())) {
            return AjaxResult.error("推送任务已删除 " + pushId);
        }

        batchDao.updateState(pushId, AssetsUpgradeStateEnum.running);

        List<PowerBiPushDetail> details = detailDao.lambdaQuery()
                .eq(PowerBiPushDetail::getBatchId, pushId)
                .eq(PowerBiPushDetail::getState, AssetsUpgradeStateEnum.wait.name())
                .list();
        if (CollectionUtils.isEmpty(details)) {
            return AjaxResult.error("无资产可推送");
        }

        Set<Long> assetIds = Lambda.buildSet(details, PowerBiPushDetail::getAssetsId);
        Map<Long, String> assetsNameMap = userDataAssetsDAO.queryAssetsName(assetIds);

        Map<Long, String> failMap = this.executePreSQL(details);

        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(3);
        pool.setMaxPoolSize(3);
        pool.setKeepAliveSeconds(60);
        pool.setTaskDecorator(new ContextCopyingTaskDecorator());
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.setThreadFactory(new BasicThreadFactory.Builder().namingPattern("pb" + pushId + "-%d").build());
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        pool.setQueueCapacity(details.size());
        pool.initialize();

        ttl.execute(() -> {
            try {
                List<CompletableFuture<Void>> all = new ArrayList<>();
                // 加入线程池 限流执行
                for (PowerBiPushDetail detail : details) {
                    CompletableFuture<Void> one = CompletableFuture.runAsync(() -> this.executeInsertSQL(detail, assetsNameMap, failMap), pool);
                    all.add(one);
//                pool.execute(() -> this.executeInsertSQL(detail, assetsNameMap, failMap));
                }
                CompletableFuture[] fuList = all.stream().toArray(CompletableFuture[]::new);
                CompletableFuture.allOf(fuList).join();
            } catch (Exception e) {
                log.error("", e);
            } finally {
                pool.shutdown();
            }
        });

        return AjaxResult.succeed();
    }

    private Map<Long, String> executePreSQL(List<PowerBiPushDetail> details) {
        Map<Long, String> failedId = new HashMap<>();
        for (PowerBiPushDetail detail : details) {
            detailDao.updateStartState(detail.getId());

            if (StringUtils.isNotBlank(detail.getPreSql())) {
                try {
                    List<String> list = PowerBiPushDetail.parseList(detail.getPreSql());
                    for (String sin : list) {
                        log.info("pre sql={}", sin);
                        powerBIMapper.deleteData(sin);
                    }
                } catch (Exception e) {
                    log.error("", e);
                    failedId.put(detail.getId(), e.getMessage());
                }
            }
        }
        return failedId;
    }

    private void executeInsertSQL(PowerBiPushDetail detail, Map<Long, String> assetsNameMap, Map<Long, String> failMap) {
        String logPrefix = DateUtils.getTime() + " " + MDC.get(LogConstant.TRACE_ID) + " 资产ID："
                + detail.getAssetsId() + " 资产名：" + assetsNameMap.get(detail.getAssetsId());
        Long id = detail.getId();
        boolean preFail = failMap.containsKey(id);
        if (preFail) {
            detailDao.updateFinishState(id, AssetsUpgradeStateEnum.failed,
                    logPrefix + " error:" + failMap.get(id));
            return;
        }

        try {
            if (StringUtils.isBlank(detail.getInsertSql())) {
                throw new RuntimeException("数据同步SQL构造失败");
            }

            // 工作流类型的资产 表变更的场景
            String table;
            UserDataAssets latest = userDataAssetsDAO.lambdaQuery().select(
                            UserDataAssets::getTemplateType, UserDataAssets::getVersion, UserDataAssets::getAssetTableName)
                    .eq(UserDataAssets::getId, detail.getAssetsId()).one();
            if (!Objects.equals(latest.getTemplateType(), TemplateTypeEnum.wide_table.name())) {
                if (Objects.equals(latest.getVersion(), detail.getAssetsVer())) {
                    table = latest.getAssetTableName();
                } else {
                    UserDataAssetsSnapshot snap = userDataAssetsSnapshotDAO.queryByAssetsId(detail.getAssetsId(), detail.getAssetsVer());
                    table = snap.getAssetTableName();
                }
                detailDao.lambdaUpdate().eq(PowerBiPushDetail::getId, id).set(PowerBiPushDetail::getTableName, table).update();
                detail.setTableName(table);
            }

            List<String> list = PowerBiPushDetail.parseList(detail.getInsertSql());
            for (String sin : list) {
                log.info("sync sql={}", sin);
                ckClusterAdapter.execute(detail.getTableName(), sin);
            }

            detailDao.updateFinishState(id, AssetsUpgradeStateEnum.success,
                    logPrefix + " finish");
        } catch (Exception e) {
            log.error("", e);
            detailDao.updateFinishState(id, AssetsUpgradeStateEnum.failed,
                    logPrefix + " error:" + e.getMessage());
        }
    }

    /**
     *
     */
    @Override
    public AjaxResult<Void> delete(Long pushId) {
        if (Objects.isNull(pushId)) {
            return AjaxResult.error("推送任务不存在");
        }
        PowerBiPushBatch batch = batchDao.getBaseMapper().selectById(pushId);
        if (Objects.isNull(batch)) {
            return AjaxResult.error("推送任务不存在");
        }
        if (BooleanUtils.isTrue(batch.getDeleted())) {
            return AjaxResult.error("推送任务已删除");
        }
        if (!Objects.equals(batch.getState(), AssetsUpgradeStateEnum.wait.name())) {
            return AjaxResult.error("推送任务已运行");
        }
        batchDao.logicDelete(pushId);
        return AjaxResult.succeed();
    }

    /**
     * 按序合并明细的日志
     */
    @Override
    public AjaxResult<String> queryLog(Long pushId) {
        if (Objects.isNull(pushId)) {
            return AjaxResult.error("推送任务不存在");
        }
        PowerBiPushBatch batch = batchDao.getBaseMapper().selectById(pushId);
        if (Objects.isNull(batch)) {
            return AjaxResult.error("推送任务不存在");
        }
        List<PowerBiPushDetail> details = detailDao.lambdaQuery()
                .select(PowerBiPushDetail::getRunLog)
                .eq(PowerBiPushDetail::getBatchId, pushId).list();

        String allLog = details.stream().filter(Objects::nonNull).map(PowerBiPushDetail::getRunLog).filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
        return AjaxResult.success(null, allLog);
    }

    @Override
    public List<PowerBiPushBatch> queryByBizIds(List<Long> bizIds) {
        return batchDao.lambdaQuery().in(PowerBiPushBatch::getBizId, bizIds).list();
    }
}
