package com.sinohealth.system.biz.scheduler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.DsConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.DbType;
import com.sinohealth.common.enums.ReleaseState;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.data.intelligence.api.Result;
import com.sinohealth.data.intelligence.api.metadata.dto.MetaTableDTO;
import com.sinohealth.data.intelligence.datasource.param.BaseDataSourceParam;
import com.sinohealth.data.intelligence.datasource.util.DataSourceUtils;
import com.sinohealth.data.intelligence.datasource.util.PasswordUtils;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.scheduler.dto.DataSyncTaskClickhouseExt;
import com.sinohealth.system.biz.scheduler.dto.ProcessDefVO;
import com.sinohealth.system.biz.scheduler.dto.UpsertSchedulerTaskBO;
import com.sinohealth.system.biz.scheduler.dto.UpsertTaskVO;
import com.sinohealth.system.biz.scheduler.dto.request.CronParam;
import com.sinohealth.system.biz.scheduler.dto.request.DataSyncTaskFieldConfig;
import com.sinohealth.system.biz.scheduler.dto.request.QueryDataSyncTaskParam;
import com.sinohealth.system.biz.scheduler.dto.request.UpsertDataSyncTaskParam;
import com.sinohealth.system.biz.scheduler.service.IntegrateSyncProcessDefService;
import com.sinohealth.system.biz.scheduler.service.IntegrateSyncTaskService;
import com.sinohealth.system.biz.scheduler.util.GenerateProcessUtil;
import com.sinohealth.system.client.DataSourceApiClient;
import com.sinohealth.system.client.MetadataClient;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.dao.TgDataSyncFieldConfigDAO;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.dto.BaseDataSourceParamDto;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgCogradientInfoMapper;
import com.sinohealth.system.mapper.TgDataSyncApplicationMapper;
import com.sinohealth.system.mapper.TgMetadataInfoMapper;
import com.sinohealth.system.service.IAssetService;
import com.sinohealth.system.service.IntegrateDataSourceService;
import com.sinohealth.system.service.IntergrateProcessDefService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据交换 申请 同步配置任务 有关尚书台部分
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-11-02 16:43
 */
@Slf4j
@Service("integrateSyncTaskService")
public class IntegrateSyncTaskServiceImpl implements IntegrateSyncTaskService {

    @Value("${dsApi.uri}")
    private String uri;
    @Resource
    private RestTemplate restTemplate;
    @Autowired
    private AppProperties appProperties;
    @Resource
    @Qualifier(ThreadPoolType.ASYNC_TASK)
    private ThreadPoolTaskExecutor pool;

    @Resource
    private TgCogradientInfoMapper tgCogradientInfoMapper;
    @Autowired
    private TgDataSyncApplicationMapper syncApplicationMapper;
    @Autowired
    private TgApplicationInfoMapper applicationInfoMapper;
    @Autowired
    private TgMetadataInfoMapper metadataInfoMapper;
    @Autowired
    private TgDataSyncFieldConfigDAO dataSyncFieldConfigDAO;

    @Autowired
    private IntegrateSyncProcessDefService syncProcessDefService;
    @Autowired
    private IntergrateProcessDefService intergrateProcessDefService;
    @Autowired
    private IntegrateDataSourceService integrateDataSourceService;
    @Autowired
    private IAssetService assetService;

    @Autowired
    private MetadataClient metadataClient;
    @Autowired
    private DataSourceApiClient dataSourceApiClient;


    @Override
    public AjaxResult<UpsertTaskVO> upsertTask(UpsertSchedulerTaskBO bo) {
        UpsertDataSyncTaskParam taskParam = new UpsertDataSyncTaskParam();
        TgDataSyncApplication param = bo.getSyncApplication();
        BeanUtils.copyProperties(param, taskParam);
        taskParam.setClickhouseExt(bo.getCkExt());
        taskParam.setFieldsConfigs(bo.getFieldsConfigs());

        taskParam.setId(bo.getSyncTaskId());
        taskParam.setName(param.getSyncTaskName());

        BaseDataSourceParamDto source = bo.getSource();
        taskParam.setSourceDbtype(source.getType());
        taskParam.setSourceDataSourceId(source.getId());
        taskParam.setSourceDataSourceName(source.getName());
        taskParam.setSourceTableName(bo.getSourceTableName());

        BaseDataSourceParamDto target = bo.getTarget();
        taskParam.setTargetDbType(target.getType());
        taskParam.setTargetDataSourceId(target.getId());
        taskParam.setTargetDataSourceName(target.getName());
        taskParam.setTargetTableName(param.getTargetTableName());

        String url = Objects.nonNull(taskParam.getId()) ? DsConstants.SyncTask.UPDATE : DsConstants.SyncTask.CREATE;
        url = uri + url;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(JsonUtils.format(taskParam), headers);

        AjaxResult upsertResult = restTemplate.postForObject(url, request, AjaxResult.class);
        log.info("upsertResult={}", upsertResult);
        if (Objects.isNull(upsertResult) || upsertResult.getCode() != 0) {
            return AjaxResult.error(Optional.ofNullable(upsertResult).map(AjaxResult::getMsg).orElse("创建尚书台同步配置失败"));
        }

        Integer syncTaskId = (Integer) upsertResult.getData();
        QueryDataSyncTaskParam query = QueryDataSyncTaskParam.builder().id(syncTaskId).build();
        request = new HttpEntity<>(JsonUtils.format(query), headers);
        AjaxResult queryRes = restTemplate.postForObject(uri + DsConstants.SyncTask.QUERY_LIST, request, AjaxResult.class);
        if (Objects.isNull(queryRes) || queryRes.getCode() != 0) {
            return AjaxResult.error(Optional.ofNullable(queryRes).map(AjaxResult::getMsg).orElse("创建尚书台同步配置失败。"));
        }
        ArrayList data = (ArrayList) queryRes.getData();
        if (CollectionUtils.isEmpty(data)) {
            return AjaxResult.error("创建尚书台数据同步配置失败。");
        }
        LinkedHashMap first = (LinkedHashMap) data.get(0);
        Object taskName = first.get("name");

        // 尚书台限制，工作流上线状态才能设置Cron表达式，所以要补一个下线
        Integer newFlowId = this.createProcess(bo.getFlowId(), syncTaskId, taskName.toString(),
                bo.getApplyReason(), param.getSyncTaskCron());
        syncProcessDefService.releaseByProcessId(newFlowId, ReleaseState.OFFLINE.getCode());

        syncApplicationMapper.update(null, new UpdateWrapper<TgDataSyncApplication>()
                .lambda().set(TgDataSyncApplication::getSyncTaskId, syncTaskId)
                .set(TgDataSyncApplication::getFlowId, newFlowId)
                .eq(TgDataSyncApplication::getId, param.getId())
        );
        return AjaxResult.success(UpsertTaskVO.builder().taskId(syncTaskId).flowId(newFlowId).build());
    }

    /**
     * 引用配置， 创建工作流， 并且设置Cron
     */
    public Integer createProcess(Integer flowId, Integer syncTaskId, String syncTaskName, String desc, String cron) {
        Long processId = Optional.ofNullable(flowId).map(Integer::longValue).orElse(null);
        ProcessDefVO vo = GenerateProcessUtil.buildProcessDefForScript(syncTaskId, syncTaskName, desc);

        Long cId = null;
        if (Objects.nonNull(flowId)) {
            List<TgCogradientInfo> infos = tgCogradientInfoMapper.selectList(new QueryWrapper<TgCogradientInfo>().lambda().eq(TgCogradientInfo::getProcessId, processId));
            if (CollectionUtils.isNotEmpty(infos)) {
                cId = Optional.ofNullable(infos.get(0).getId()).map(Integer::longValue).orElse(null);
            }
            syncProcessDefService.releaseByProcessId(flowId, ReleaseState.OFFLINE.getCode());
        }


        // 新增或编辑 编辑返回结构不一样
        final AjaxResult result = syncProcessDefService.createProcessDefinition(cId
                , null, vo.getName(), vo.getProcessDefinitionJson(), CronParam.newCron(cron), vo.getLocations(),
                "[]", vo.getReleaseState(), id -> {
                });

        if (result.getCode() == 0) {
            return Objects.nonNull(flowId) ? flowId : (Integer) result.getData();
        } else {
            throw new CustomException("创建工作流异常");
        }
    }

    @Override
    public AjaxResult executeWorkFlow(Long applicationId) {
        log.info("executeWorkFlow applicationId={}", applicationId);
        List<TgDataSyncApplication> syncApplyList = syncApplicationMapper.selectList(new QueryWrapper<TgDataSyncApplication>()
                .lambda().select(TgDataSyncApplication::getFlowId)
                .eq(TgDataSyncApplication::getApplicationId, applicationId)
        );
        if (CollectionUtils.isEmpty(syncApplyList)) {
            return AjaxResult.error("未找到对应数据交换配置");
        }

        TgDataSyncApplication syncApply = syncApplyList.get(0);
        if (Objects.isNull(syncApply.getFlowId())) {
            return AjaxResult.error("工作流未创建成功");
        }

        AjaxResult result = syncProcessDefService.queryProcessInstanceStatus(syncApply.getFlowId(), 1, 10, "RUNNING_EXECUTION");
        //查询正在运行中的实例
        if (result.getCode() == 0) {
            LinkedHashMap<String, Object> map = (LinkedHashMap) result.getData();
            if ((Integer) map.get("total") > 0) {
                return AjaxResult.error("该流程已经在运行中！");
            }
        }

        AjaxResult<Object> onlineResult = syncProcessDefService.releaseByProcessId(syncApply.getFlowId(), ReleaseState.ONLINE.getCode());
        if (!onlineResult.isSuccess()) {
            log.error("onlineResult={}", onlineResult);
            return AjaxResult.error("上线工作流失败");
        }

        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("processDefinitionId", syncApply.getFlowId());
        postParameters.add("failureStrategy", "CONTINUE");
        postParameters.add("taskDependType", null);
        postParameters.add("execType", "");
        postParameters.add("warningType", appProperties.getFinalWarningType());
        postParameters.add("warningGroupId", appProperties.getFinalSwapWarnGroupId());
        postParameters.add("runMode", "RUN_MODE_SERIAL");
        postParameters.add("processInstancePriority", "MEDIUM");
        postParameters.add("workerGroup", "default");
        postParameters.add("timeout", DsConstants.MAX_TASK_TIMEOUT);
        //任务串行化执行
        postParameters.add("inSerialMode", true);

        // 工作流启动参数
        AjaxResult ajaxResult = syncProcessDefService.execProcessInstance(postParameters);
        if (!ajaxResult.isSuccess() && Objects.equals(ajaxResult.getCode(), 50004)) {
            AjaxResult processResult = syncProcessDefService.queryProcessById(syncApply.getFlowId());
            if (processResult.isDolphinSuccess()) {
                Map<String, Object> resultMap = (Map<String, Object>) processResult.getData();
                String flowName = Optional.ofNullable(resultMap.get("name")).map(Object::toString).orElse(null);
                return AjaxResult.error("当前工作流【" + flowName + "】已下线无法执行，请到尚书台上线后执行");
            }
            return AjaxResult.error("当前工作流【 】已下线无法执行，请到尚书台上线后执行");
        }

        return ajaxResult;
    }

    @Override
    public void asyncOfflineWorkFlow(Long assetId) {
        List<TgApplicationInfo> applyList = applicationInfoMapper.selectList(new QueryWrapper<TgApplicationInfo>()
                .lambda().eq(TgApplicationInfo::getNewAssetId, assetId));
        List<Long> applyIds = Lambda.buildList(applyList, TgApplicationInfo::getId);
        if (CollectionUtils.isEmpty(applyIds)) {
            return;
        }

        List<TgDataSyncApplication> syncApplyList = syncApplicationMapper.selectList(new QueryWrapper<TgDataSyncApplication>()
                .lambda().select(TgDataSyncApplication::getFlowId)
                .in(TgDataSyncApplication::getApplicationId, applyIds));
        List<Integer> flowIds = syncApplyList.stream().filter(Objects::nonNull)
                .map(TgDataSyncApplication::getFlowId)
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(flowIds)) {
            return;
        }

        String username = SecurityUtils.getUsername();
        pool.submit(() -> {
            for (Integer flowId : flowIds) {
                try {
                    intergrateProcessDefService.releaseByProcessId(flowId, ReleaseState.OFFLINE.getCode(), username);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
    }

    @Override
    public AjaxResult<Void> offlineExpireFlow() {
        List<TgCogradientInfo> infos = tgCogradientInfoMapper.selectList(new QueryWrapper<TgCogradientInfo>().lambda()
                .select(TgCogradientInfo::getProcessId)
                .eq(TgCogradientInfo::getStatus, ReleaseState.ONLINE.getCode()));
        List<Integer> processIds = Lambda.buildListPost(infos, TgCogradientInfo::getProcessId, Objects::nonNull);
        if (CollectionUtils.isEmpty(processIds)) {
            return AjaxResult.succeed();
        }

        List<TgDataSyncApplication> applyList = syncApplicationMapper.selectList(new QueryWrapper<TgDataSyncApplication>().lambda()
                .select(TgDataSyncApplication::getApplicationId, TgDataSyncApplication::getFlowId)
                .in(TgDataSyncApplication::getFlowId, processIds));
        List<Long> applyIds = Lambda.buildList(applyList, TgDataSyncApplication::getApplicationId);
        if (CollectionUtils.isEmpty(applyIds)) {
            return AjaxResult.succeed();
        }

        List<TgApplicationInfo> expireApply = applicationInfoMapper.selectList(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getId)
                .in(TgApplicationInfo::getId, applyIds)
                .gt(TgApplicationInfo::getDataExpir, new Date()));

        Set<Long> expireIds = Lambda.buildSet(expireApply, TgApplicationInfo::getId);
        if (CollectionUtils.isEmpty(expireIds)) {
            return AjaxResult.succeed();
        }

        List<Integer> flowIds = applyList.stream().filter(v -> expireIds.contains(v.getApplicationId())).map(TgDataSyncApplication::getFlowId).collect(Collectors.toList());
        log.info("expireIds={} flowIds={}", expireIds, flowIds);

        pool.submit(() -> {
            for (Integer flowId : flowIds) {
                try {
                    intergrateProcessDefService.releaseByProcessId(flowId, ReleaseState.OFFLINE.getCode(), "过期");
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });

        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> upsertTaskConfigAndProcess(Long applyId) {
        log.info("executeWorkFlow applicationId={}", applyId);

        List<TgDataSyncApplication> syncApplyList = syncApplicationMapper.selectList(new QueryWrapper<TgDataSyncApplication>()
                .lambda().eq(TgDataSyncApplication::getApplicationId, applyId)
        );
        if (CollectionUtils.isEmpty(syncApplyList)) {
            return AjaxResult.error("未找到对应数据交换配置");
        }
        TgDataSyncApplication syncApply = syncApplyList.get(0);

        TgApplicationInfo info = applicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getNewAssetId, TgApplicationInfo::getApplyReason)
                .eq(TgApplicationInfo::getId, applyId));
        if (Objects.isNull(info)) {
            return AjaxResult.error("关联资产异常");
        }

        TgAssetInfo tgAssetInfo = assetService.queryOne(info.getNewAssetId());
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

        MetaTableDTO metaTable = metaTableRes.getResult();
        // 元数据 数据源id
        Integer sDsId = metaTable.getDataSourceId();
        Integer tDsId = syncApply.getTargetDataSourceId();

        BaseDataSourceParamDto sDs = this.upsertDs(sDsId, metaTable.getDbSchema(), metaTable.getDatabase());
        String ds;
        if (StringUtils.isNotBlank(syncApply.getTargetDataSourceDatabase())) {
            ds = syncApply.getTargetDataSourceDatabase();
        } else {
            com.sinohealth.data.intelligence.datasource.entity.DataSource dsRes = dataSourceApiClient.findById(syncApply.getTargetDataSourceId());
            com.sinohealth.data.intelligence.enums.DataSourceType dsType = com.sinohealth.data.intelligence.enums
                    .DataSourceType.ofName(dsRes.getType());
            BaseDataSourceParam sourceParam = DataSourceUtils.buildDatasourceParam(dsType, dsRes.getDatasourceParams());
            ds = sourceParam.getDatabase();
        }
        BaseDataSourceParamDto tDs = this.upsertDs(tDsId, syncApply.getTargetDataSourceSchema(), ds);

        // 创建 同步配置 & 工作流
        List<TgDataSyncFieldConfig> fieldConfigs = dataSyncFieldConfigDAO.queryByApplyId(syncApply.getId());
        List<DataSyncTaskFieldConfig> fields = fieldConfigs.stream().map(v -> {
            DataSyncTaskFieldConfig config = new DataSyncTaskFieldConfig();
            BeanUtils.copyProperties(v, config);
            return config;
        }).collect(Collectors.toList());

        UpsertSchedulerTaskBO context = UpsertSchedulerTaskBO.builder()
                .syncApplication(syncApply)
                .flowId(syncApply.getFlowId())
                .fieldsConfigs(fields)
                .sourceTableName(metaTable.getRealName())
                .source(sDs)
                .target(tDs)
                .applyReason(info.getApplyReason())
                .build();
        if (Objects.equals(tDs.getType(), DbType.CLICKHOUSE.name())) {
            DataSyncTaskClickhouseExt ext = new DataSyncTaskClickhouseExt();
            ext.setOrderByList(Arrays.asList(Optional.ofNullable(syncApply.getCkSortKey()).orElse("").split(",")));
            ext.setCluster(syncApply.getCkCluster());
            context.setCkExt(ext);
        }
        TgApplicationInfo curApply = applicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getOldApplicationId).eq(TgApplicationInfo::getId, applyId));
        if (Objects.nonNull(curApply)) {
            Long old = curApply.getOldApplicationId();
            if (Objects.nonNull(old)) {
                syncApplyList = syncApplicationMapper.selectList(new QueryWrapper<TgDataSyncApplication>()
                        .lambda().eq(TgDataSyncApplication::getApplicationId, old)
                );
                if (CollectionUtils.isEmpty(syncApplyList)) {
                    return AjaxResult.error("未找到旧申请对应数据交换配置");
                }

                TgDataSyncApplication s = syncApplyList.get(0);
                context.setSyncTaskId(s.getSyncTaskId());
                context.setFlowId(s.getFlowId());
            }
        }

        AjaxResult<UpsertTaskVO> upsertResult = this.upsertTask(context);
        if (!upsertResult.isSuccess()) {
            return AjaxResult.error("保存配置失败:" + upsertResult.getMsg());
        }
        return AjaxResult.succeed();
    }

    public BaseDataSourceParamDto upsertDs(Integer dsId, String schema, String database) {
        if (Objects.isNull(dsId)) {
            throw new CustomException("资产或表未绑定数据源");
        }
        BaseDataSourceParamDto dto = new BaseDataSourceParamDto();

        com.sinohealth.data.intelligence.datasource.entity.DataSource dsRes = dataSourceApiClient.findById(dsId);
        dto.setName(this.buildName(dsId, schema));

        com.sinohealth.data.intelligence.enums.DataSourceType dsType = com.sinohealth.data.intelligence.enums
                .DataSourceType.ofName(dsRes.getType());
        dto.setType(this.convertType(dsType).name());

        BaseDataSourceParam sourceParam = DataSourceUtils.buildDatasourceParam(dsType, dsRes.getDatasourceParams());
        dto.setHost(sourceParam.getHost());
        dto.setPort(sourceParam.getPort());
        dto.setDatabase(database);
        dto.setUserName(sourceParam.getUser());
        dto.setPassword(PasswordUtils.decodePassword(sourceParam.getPassword()));

        Map<String, String> other = sourceParam.getOther();
        if (MapUtils.isNotEmpty(other)) {
            other = other.entrySet().stream()
                    .filter(v -> StringUtils.isNotBlank(v.getKey()) && StringUtils.isNotBlank(v.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (front, current) -> current));
        }
        dto.setOther(other);
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
            this.upsertDs(dto);
        }

        return dto;
    }

    private Integer upsertDs(BaseDataSourceParamDto dto) {
        AjaxResult createRes = integrateDataSourceService.createDataSource(dto);
        if (createRes.getCode() != 0) {
            log.error("CREATE dto={} rsp={}", dto, createRes);
            throw new CustomException("创建数据源失败");
        }
        return (Integer) createRes.get("data");
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

    private String buildName(Integer dsId, String schema) {
        if (StringUtils.isNotBlank(schema)) {
            return "TG_" + dsId + "_" + schema;
        }
        return "TG_" + dsId;
    }
}
