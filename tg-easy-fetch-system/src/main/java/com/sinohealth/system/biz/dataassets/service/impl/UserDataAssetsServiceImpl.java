package com.sinohealth.system.biz.dataassets.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.DataAssetsConstants;
import com.sinohealth.common.constant.DsConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.AuditTypeEnum;
import com.sinohealth.common.enums.ExecutionStatus;
import com.sinohealth.common.enums.FtpStatus;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dataassets.AcceptanceStateEnum;
import com.sinohealth.common.enums.dataassets.AssetsExpireEnum;
import com.sinohealth.common.enums.dataassets.AssetsSnapshotTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StopWatch;
import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.data.common.response.PageInfo;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.dao.ApplicationFormDAO;
import com.sinohealth.system.biz.application.dao.ApplicationTaskConfigDAO;
import com.sinohealth.system.biz.application.domain.ApplicationTaskConfig;
import com.sinohealth.system.biz.application.dto.request.ApplicationSaveAsRequest;
import com.sinohealth.system.biz.application.service.ApplicationFormService;
import com.sinohealth.system.biz.application.util.ApplyUtil;
import com.sinohealth.system.biz.application.util.CostTimeUtil;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.arkbi.service.ArkBiService;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.biz.dataassets.dao.AcceptanceRecordDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowBatchDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowBatchDetailDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsWideUpgradeTriggerDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.*;
import com.sinohealth.system.biz.dataassets.domain.entity.UsableDataAssetsEntity;
import com.sinohealth.system.biz.dataassets.dto.*;
import com.sinohealth.system.biz.dataassets.dto.bo.ApplyFlowContext;
import com.sinohealth.system.biz.dataassets.dto.bo.ExecFlowParam;
import com.sinohealth.system.biz.dataassets.dto.request.*;
import com.sinohealth.system.biz.dataassets.helper.UserDataAssetsUploadFtpHelper;
import com.sinohealth.system.biz.dataassets.mapper.UserDataAssetsMapper;
import com.sinohealth.system.biz.dataassets.mapper.UserDataAssetsSnapshotMapper;
import com.sinohealth.system.biz.dataassets.service.AssetsAdapter;
import com.sinohealth.system.biz.dataassets.service.AssetsCompareService;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.biz.dataassets.service.UserFileAssetsService;
import com.sinohealth.system.biz.dataassets.util.DataAssetsUtil;
import com.sinohealth.system.biz.dir.dto.node.ArkBiNode;
import com.sinohealth.system.biz.dir.dto.node.AssetsNode;
import com.sinohealth.system.biz.dir.dto.node.CustomerNode;
import com.sinohealth.system.biz.dir.dto.node.FileAssetsNode;
import com.sinohealth.system.biz.dir.dto.node.ProjectNode;
import com.sinohealth.system.biz.dir.dto.node.UserDataAssetsNode;
import com.sinohealth.system.biz.dir.util.AssetsTreeUtil;
import com.sinohealth.system.biz.process.service.TgFlowProcessCheckService;
import com.sinohealth.system.biz.project.constants.ProjectRelateEnum;
import com.sinohealth.system.biz.project.dao.DataPlanDAO;
import com.sinohealth.system.biz.project.dao.ProjectDAO;
import com.sinohealth.system.biz.project.dao.ProjectHelperDAO;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.biz.project.domain.ProjectDataAssetsRelate;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.mapper.ProjectMapper;
import com.sinohealth.system.biz.project.service.DataPlanService;
import com.sinohealth.system.biz.table.dao.TableInfoSnapshotDAO;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.biz.ws.service.WsMsgService;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.dao.ApplicationDataUpdateRecordDAO;
import com.sinohealth.system.domain.ApplicationDataUpdateRecord;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.domain.ProjectHelper;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgNoticeRead;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.domain.constant.UpdateRecordStateType;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.domain.customer.Customer;
import com.sinohealth.system.mapper.*;
import com.sinohealth.system.service.ArkbiAnalysisService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.IntergrateAutoProcessDefService;
import com.sinohealth.system.service.IntergrateProcessDefService;
import com.sinohealth.system.service.impl.MyDataDirServiceImpl;
import com.sinohealth.system.service.impl.TemplatePackTailSettingServiceImpl;
import com.sinohealth.system.util.ApplicationSqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 1. 回溯 我的资产树API前版本实现 7b8ff56d2
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-05-10 10:53
 */
@Slf4j
@Service
public class UserDataAssetsServiceImpl implements UserDataAssetsService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private ApplicationTaskConfigDAO applicationTaskConfigDAO;
    @Autowired
    private TgCkProviderMapper ckProviderMapper;
    @Autowired
    private ProjectHelperMapper projectHelperMapper;
    @Autowired
    private ProjectHelperDAO projectHelperDAO;
    @Autowired
    private TemplateInfoDAO templateInfoDAO;
    @Autowired
    private UserDataAssetsMapper userDataAssetsMapper;
    @Autowired
    private ProjectDataAssetsRelateMapper projectDataAssetsRelateMapper;
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private TgTemplateInfoMapper tgTemplateInfoMapper;
    @Autowired
    private TgApplicationInfoMapper applicationInfoMapper;
    @Autowired
    private ApplicationDAO applicationDAO;
    @Autowired
    private ApplicationDataUpdateRecordDAO dataUpdateRecordDAO;
    @Autowired
    private ProjectDAO projectDAO;
    @Autowired
    private UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;
    @Autowired
    private UserDataAssetsSnapshotMapper userDataAssetsSnapshotMapper;
    @Autowired
    private AcceptanceRecordDAO acceptanceRecordDAO;
    @Autowired
    private AssetsWideUpgradeTriggerDAO assetsWideUpgradeTriggerDAO;
    @Autowired
    private TgNoticeReadMapper tgNoticeReadMapper;
    @Autowired
    private TableInfoSnapshotDAO tableInfoSnapshotDAO;
    @Autowired
    private AssetsFlowBatchDAO flowBatchDAO;
    @Autowired
    private AssetsFlowBatchDetailDAO assetsFlowBatchDetailDAO;
    @Autowired
    private DataPlanDAO dataPlanDAO;
    @Autowired
    private ApplicationFormDAO applicationFormDAO;

    // Service
    @Autowired
    private IntergrateProcessDefService intergrateProcessDefService;
    @Autowired
    private IntergrateAutoProcessDefService intergrateAutoProcessDefService;
    @Autowired
    private ArkbiAnalysisService arkbiAnalysisService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private UserFileAssetsService userFileAssetsService;
    @Autowired
    private AssetsAdapter assetsAdapter;
    @Autowired
    private ArkBiService arkBiService;
    @Autowired
    private UserDataAssetsUploadFtpHelper userDataAssetsUploadFtpHelper;
    @Autowired
    private WsMsgService wsMsgService;
    @Autowired
    private AssetsCompareService assetsCompareService;
    @Autowired
    private AlertService alertService;
    @Autowired
    private TgFlowProcessCheckService processCheckService;
    @Autowired
    private DataPlanService dataPlanService;
    @Autowired
    private ApplicationFormService applicationFormService;


    // 技术组件 非业务
    @Autowired
    private CKClusterAdapter ckClusterAdapter;
    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER_CK)
    private ScheduledExecutorService scheduler;
    @Resource
    @Qualifier(ThreadPoolType.ENHANCED_TTL)
    private Executor ttl;

    @Autowired
    private AppProperties appProperties;

    @Override
    public AjaxResult<IPage<UserDataAssetsVersionPageDTO>> pageQueryAssetsSnapshot(UserDataAssetsVersionPageRequest request) {
        if (Objects.isNull(request.getAssetsId())) {
            return AjaxResult.error("参数为空");
        }

        IPage<UserDataAssetsSnapshot> page = userDataAssetsSnapshotMapper.pageWithMain(request.buildPage(), request.getAssetsId());

        List<Long> deleteIds = new ArrayList<>();
        for (UserDataAssetsSnapshot record : page.getRecords()) {
            AssetsExpireEnum state = DateUtils.convertExpire(record.getDataExpire());
            if (Objects.isNull(record.getExpireType())) {
                record.setExpireType(state.name());
            }
            if (Objects.equals(state, AssetsExpireEnum.delete) && !Objects.equals(record.getExpireType(), AssetsExpireEnum.delete.name())) {
                if (!Objects.equals(record.getExpireType(), AssetsExpireEnum.delete_data.name())) {
                    deleteIds.add(record.getId());
                }
                record.setExpireType(AssetsExpireEnum.delete.name());
            }
            if (Objects.equals(record.getExpireType(), AssetsExpireEnum.delete_data.name())) {
                record.setExpireType(AssetsExpireEnum.delete.name());
            }
        }
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            userDataAssetsSnapshotDAO.update(null, new UpdateWrapper<UserDataAssetsSnapshot>().lambda()
                    .in(UserDataAssetsSnapshot::getId, deleteIds)
                    .set(UserDataAssetsSnapshot::getExpireType, AssetsExpireEnum.delete.name())
            );
        }

        Integer latestVersion = userDataAssetsDAO.queryVersion(request.getAssetsId());

        Set<String> versionList = Lambda.buildSet(page.getRecords(), UserDataAssets::buildTableVersion);
        List<TableInfoSnapshot> tableList = tableInfoSnapshotDAO.queryByVersion(versionList);
        Map<String, TableInfoSnapshot> tableVerMap = Lambda.buildMap(tableList, TableInfoSnapshot::buildTableVersion);

        Map<String, AcceptanceRecord> recordMap = acceptanceRecordDAO.queryByAssetsIdAndVersion(page.getRecords());

        AtomicInteger countIdx = new AtomicInteger((request.getPage() - 1) * request.getSize());
        return AjaxResult.success(PageUtil.convertMap(page, v -> {
            UserDataAssetsVersionPageDTO dto = new UserDataAssetsVersionPageDTO();
//            dto.setId(v.getId());
            dto.setExpireType(v.getExpireType());
            dto.setSort(countIdx.incrementAndGet());
            dto.setVersion(v.getVersion());
            dto.setLatest(Objects.equals(v.getVersion(), latestVersion));
            String state = Optional.ofNullable(recordMap.get(v.getAssetsVersion()))
                    .map(AcceptanceRecord::getState).orElse(AcceptanceStateEnum.wait.name());
            dto.setAcceptState(state);
            dto.setCreateTime(v.getCreateTime());
            dto.setSnapshotType(v.getSnapshotType());
            dto.setAssetsId(v.getAssetsId());
            dto.setSrcApplicationId(v.getSrcApplicationId());
            dto.setTemplateType(v.getTemplateType());

            // 区分处理 版本信息： 来自底表，来自工作流管理
            Boolean scheduler = TemplateTypeEnum.of(v.getTemplateType()).map(TemplateTypeEnum::isSchedulerTaskType).orElse(false);
            if (scheduler) {
                Optional<AssetsFlowBatch> batchOpt = Optional.ofNullable(v.getFlowDetailId())
                        .flatMap(assetsFlowBatchDetailDAO::queryBatchId)
                        .map(flowBatchDAO::getById);
                String remark = batchOpt.map(AssetsFlowBatch::getRemark).orElse("");
                dto.setTableRemark(remark);
                dto.setPeriod(batchOpt.map(AssetsFlowBatch::getPeriod).orElse(""));
                dto.setFlowProcessType(v.getFlowProcessType());
                dto.setAssetsRemark(FlowProcessTypeEnum.getDescByName(dto.getFlowProcessType()));
            } else {
                Optional<TableInfoSnapshot> snapOpt = Optional.ofNullable(tableVerMap.get(v.buildTableVersion()));
                dto.setTableVersion("V" + snapOpt.map(TableInfoSnapshot::getVersion).orElse(1) +
                        snapOpt.map(TableInfoSnapshot::getVersionPeriod).map(t -> "(" + t + ")").orElse(""));
                dto.setTableRemark(snapOpt.map(TableInfoSnapshot::getRemark).orElse(""));
                dto.setPeriod(snapOpt.map(TableInfoSnapshot::getVersionPeriod).orElse(""));
                dto.setFlowProcessType(v.getFlowProcessType());
                dto.setAssetsRemark(FlowProcessTypeEnum.getDescByName(dto.getFlowProcessType()));
            }

            return dto;
        }));
    }

    @Override
    public AjaxResult<Void> editAssetsInfo(UserDataAssetsVersionEditRequest request) {
        if (!FlowProcessTypeEnum.all.contains(request.getFlowProcessType())) {
            return AjaxResult.error("请选择有效的版本类型");
        }

        Integer latestVersion = userDataAssetsDAO.queryVersion(request.getAssetsId());
        if (Objects.equals(latestVersion, request.getVersion())) {
            boolean success = userDataAssetsDAO.lambdaUpdate()
                    .set(UserDataAssets::getFlowProcessType, request.getFlowProcessType())
                    .eq(UserDataAssets::getId, request.getAssetsId())
                    .update();
            if (!success) {
                log.info("编辑资产失败 {}", request);
                return AjaxResult.error("编辑失败");
            }
        } else {
            boolean success = userDataAssetsSnapshotDAO.lambdaUpdate()
                    .set(UserDataAssetsSnapshot::getFlowProcessType, request.getFlowProcessType())
                    .eq(UserDataAssetsSnapshot::getAssetsId, request.getAssetsId())
                    .eq(UserDataAssetsSnapshot::getVersion, request.getVersion())
                    .update();
            if (!success) {
                log.info("编辑历史资产失败 {}", request);
                return AjaxResult.error("编辑失败.");
            }
        }

        return AjaxResult.succeed();
    }

    private void saveDataAssets(UserDataAssets assets, TgApplicationInfo info) {
        userDataAssetsDAO.save(assets);

        int costMin;
        if (Objects.equals(TemplateTypeEnum.customized.name(), assets.getTemplateType())) {
            costMin = 0;
        } else {
            costMin = CostTimeUtil.calcCostMin(info.getCreateTime());
        }

        applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                .eq(TgApplicationInfo::getId, assets.getSrcApplicationId())
                .set(TgApplicationInfo::getAssetsId, assets.getId())
                .set(TgApplicationInfo::getAssetsCreateTime, LocalDateTime.now())
                .set(costMin > 0, TgApplicationInfo::getDataCostMin, costMin)
        );
    }

    /**
     * @see AssetsUpgradeTriggerServiceImpl#scheduleWideTable()
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertDataAssets(UpsertAssetsBO bo) {
        TgApplicationInfo info = bo.getInfo();
        TgTemplateInfo template = new TgTemplateInfo().selectById(info.getTemplateId());

        // 重新申请后，更新已有资产 不创建资产
        Long assetsId;
        if (Objects.nonNull(info.getOldApplicationId())) {
            TgApplicationInfo oldApply = TgApplicationInfo.newInstance().selectById(info.getOldApplicationId());
            if (Objects.nonNull(oldApply) && Objects.nonNull(oldApply.getAssetsId()) && Objects.isNull(info.getAssetsId())) {
                assetsId = oldApply.getAssetsId();
                info.setAssetsId(assetsId);
            } else {
                assetsId = info.getAssetsId();
            }
        } else {
            assetsId = info.getAssetsId();
        }

        if (Objects.nonNull(assetsId)) {
            info.setAssetsId(assetsId);
            // 更新资产触发点：重新申请审核通过，工作流手动执行
            this.replaceAssets(bo, assetsId, template);
        } else {
            // 创建资产触发点: 宽表模式审核通过，其他模式回调
            this.createNewAssets(bo, template);
        }
    }


    /**
     * 删除旧映射关系，新增新关系
     */
    private void refreshProjectRelate(Long assetsId, Long projectId) {
        if (Objects.isNull(assetsId) || Objects.isNull(projectId)) {
            return;
        }

        // 删除原有主项目关系，删除从项目关系（新主项目是之前的从项目）
        projectDataAssetsRelateMapper.delete(new QueryWrapper<ProjectDataAssetsRelate>().lambda()
                .eq(ProjectDataAssetsRelate::getProType, ProjectRelateEnum.master.name())
                .eq(ProjectDataAssetsRelate::getUserAssetId, assetsId)
        );
        projectDataAssetsRelateMapper.delete(new QueryWrapper<ProjectDataAssetsRelate>().lambda()
                .eq(ProjectDataAssetsRelate::getProjectId, projectId)
                .eq(ProjectDataAssetsRelate::getUserAssetId, assetsId)
        );

        ProjectDataAssetsRelate relate = new ProjectDataAssetsRelate()
                .setProType(ProjectRelateEnum.master.name())
                .setProjectId(projectId)
                .setUserAssetId(assetsId);
        projectDataAssetsRelateMapper.insert(relate);
    }

    private void createNewAssets(UpsertAssetsBO bo, TgTemplateInfo template) {
        String assetsSql = bo.calcAssetsSql();
        LocalDateTime dataExpire = bo.calcDataExpire();
        TgApplicationInfo info = bo.getInfo();
        boolean file = bo.isFile();

        TableInfoSnapshot latest = tableInfoSnapshotDAO.getLatest(info.getBaseTableId());
        Long applyId = info.getId();
        UserDataAssets assets = new UserDataAssets();
        BeanUtils.copyProperties(info, assets);
        Optional<TableInfoSnapshot> tableOpt = Optional.ofNullable(latest);
        Integer baseVersion = tableOpt.map(TableInfoSnapshot::getVersion).orElse(null);
        assets.setSrcApplicationId(applyId)
                .setTemplateType(template.getTemplateType())
                .setAssetTableName(bo.getTableName())
                .setAssetsSql(assetsSql)
                .setCreator(info.getApplicantId())
                .setUpdater(info.getApplicantId())
                .setVersion(1)
                .setBaseVersion(baseVersion)
                .setBaseVersionPeriod(tableOpt.map(TableInfoSnapshot::getVersionPeriod).orElse(null))
                .setSnapshotType(AssetsSnapshotTypeEnum.apply.name())
                .setConfigType(info.getConfigType())
                // 宽表类型取上游类型
                .setFlowProcessType(tableOpt.map(TableInfoSnapshot::getFlowProcessType).orElse(null))
                .setDataExpire(dataExpire)
                .setExpireType(AssetsExpireEnum.normal.name())
                .setDeprecated(false)
        ;
        if (file) {
            FileAssetsUploadDTO assetsAttach = info.getAssetsAttach();
            assets.setFtpStatus(FtpStatus.SUCCESS.name());
            assets.setFtpPath(assetsAttach.getPath());
        }
        // 工作流类型 取排期表
        if (Objects.isNull(assets.getFlowProcessType())
                && TemplateTypeEnum.isSchedulerTaskType(template.getTemplateType())) {
            AjaxResult<CurrentDataPlanDTO> curRes = dataPlanService.curPeriod(template.getBizType());
            assets.setFlowProcessType(curRes.getData().getFlowProcessType());
        }

        this.saveDataAssets(assets, info);
        // 设置assets回去
        info.setAssetsId(assets.getId());

        this.refreshProjectRelate(assets.getId(), info.getProjectId());

        AcceptanceRecord record = new AcceptanceRecord();
        record.setAssetsId(assets.getId());
        record.setUser(assets.getApplicantId());
        record.setApplicationId(assets.getSrcApplicationId());
        record.setVersion(assets.getVersion());
        record.setState(AcceptanceStateEnum.wait.name());
        record.setBizType(template.getBizType());
        acceptanceRecordDAO.save(record);

        if (Objects.equals(template.getTemplateType(), TemplateTypeEnum.wide_table.name())) {
            AssetsWideUpgradeTrigger trigger = new AssetsWideUpgradeTrigger();
            trigger.setAssetsId(assets.getId());
            trigger.setActVersion(baseVersion);
            trigger.setTableId(template.getBaseTableId());
            trigger.setApplyId(info.getId());
            trigger.setState(AssetsUpgradeStateEnum.success.name());
            trigger.setCreateTime(LocalDateTime.now());
            trigger.setUpdateTime(LocalDateTime.now());

            assetsWideUpgradeTriggerDAO.save(trigger);
        }

        this.asyncUpdateCount(assets);
    }

    public void replaceAssets(UpsertAssetsBO bo, Long assetsId, TgTemplateInfo template) {
        log.info("replaceAssets: assetsId={}", assetsId);
        UserDataAssets existAssets = userDataAssetsDAO.getById(assetsId);
        if (Objects.isNull(existAssets)) {
            log.error("资产不存在: assetsId={}", assetsId);
            return;
        }

        if (Objects.isNull(template)) {
            template = new TgTemplateInfo().selectById(existAssets.getTemplateId());
        }
        if (Objects.isNull(bo.getInfo())) {
            TgApplicationInfo info = TgApplicationInfo.newInstance().selectById(existAssets.getSrcApplicationId());
            JsonBeanConverter.convert2Obj(info);
            bo.setInfo(info);
        }


        TgApplicationInfo info = bo.getInfo();
        Long nowApplyId = info.getId();
        Long existApplyId = existAssets.getSrcApplicationId();
        int costMin;
        // 申请id一致时不更新出数耗时
        if (Objects.equals(TemplateTypeEnum.customized.name(), existAssets.getTemplateType())
                || Objects.equals(existApplyId, nowApplyId)) {
            costMin = 0;
        } else {
            costMin = CostTimeUtil.calcCostMin(info.getCreateTime());
        }

        applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                .eq(TgApplicationInfo::getId, nowApplyId)
                .set(TgApplicationInfo::getAssetsCreateTime, LocalDateTime.now())
                .set(costMin > 0, TgApplicationInfo::getDataCostMin, costMin)
        );

        int newVersion = Optional.ofNullable(existAssets.getVersion()).map(v -> v + 1).orElse(1);
        LocalDateTime dataExpire = bo.calcDataExpire();

        // 更新历史资产 过期时间
        userDataAssetsSnapshotDAO.update(null, new UpdateWrapper<UserDataAssetsSnapshot>().lambda()
                .eq(UserDataAssetsSnapshot::getAssetsId, assetsId)
                .set(UserDataAssetsSnapshot::getDataExpire, dataExpire)
        );

        // 保存历史版本
        UserDataAssetsSnapshot snapshot = new UserDataAssetsSnapshot();
        BeanUtils.copyProperties(existAssets, snapshot);
        snapshot.setId(null);
        snapshot.setCreateTime(existAssets.getCreateTime());
        snapshot.setUpdateTime(existAssets.getUpdateTime());
        snapshot.setSnapshotType(existAssets.getSnapshotType());
        snapshot.setExpireType(AssetsExpireEnum.normal.name());
        snapshot.setAssetsId(assetsId);
        snapshot.setVersion(newVersion - 1);
        snapshot.setDataExpire(dataExpire);
        userDataAssetsSnapshotDAO.save(snapshot);

        String assetsSql = bo.calcAssetsSql();
        TableInfoSnapshot latest = tableInfoSnapshotDAO.getLatest(info.getBaseTableId());

        // 更新当前资产
        String tableName = bo.getTableName();
        // 同申请 做升级时，维持原状态
        boolean deprecated = Objects.equals(info.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.INVALID_APPLICATION);
        boolean file = bo.isFile();

        LambdaUpdateChainWrapper<UserDataAssets> wrapper = userDataAssetsDAO.lambdaUpdate()
                .eq(UserDataAssets::getId, assetsId)
                .set(UserDataAssets::getAssetTableName, tableName)
                .set(UserDataAssets::getSrcApplicationId, nowApplyId)
                .set(UserDataAssets::getAssetsSql, assetsSql)
                .set(UserDataAssets::getProcessId, info.getProcessId())
                .set(UserDataAssets::getProcessVersion, info.getProcessVersion())
                .set(UserDataAssets::getRequireTimeType, info.getRequireTimeType())
                .set(UserDataAssets::getContractNo, info.getContractNo())
                .set(UserDataAssets::getVersion, newVersion)
                .set(UserDataAssets::getBaseVersion, Optional.ofNullable(latest).map(TableInfoSnapshot::getVersion).orElse(null))
                .set(UserDataAssets::getBaseVersionPeriod, Optional.ofNullable(latest).map(TableInfoSnapshot::getVersionPeriod).orElse(null))
                .set(UserDataAssets::getFtpStatus, FtpStatus.WAIT)
                .set(UserDataAssets::getFtpPath, "")
                .set(UserDataAssets::getFtpErrorMessage, "")
                .set(UserDataAssets::getSnapshotType, bo.getSnapshotType())
                .set(UserDataAssets::getDataExpire, dataExpire)
                .set(UserDataAssets::getExpireType, AssetsExpireEnum.normal.name())
                .set(UserDataAssets::getFlowDetailId, bo.getTriggerId())
                .set(UserDataAssets::getDeprecated, deprecated)
                .set(UserDataAssets::getCreateTime, LocalDateTime.now())
                .set(UserDataAssets::getFlowProcessType, "")
                .set(UserDataAssets::getConfigType, info.getConfigType());

        if (file) {
            FileAssetsUploadDTO assetsAttach = info.getAssetsAttach();
            wrapper.set(UserDataAssets::getFtpStatus, FtpStatus.SUCCESS.name())
                    .set(UserDataAssets::getFtpPath, assetsAttach.getPath());
        }

        String flowType;
        if (TemplateTypeEnum.isSchedulerTaskType(template.getTemplateType())) {
            flowType = Optional.ofNullable(bo.getTriggerId()).map(assetsFlowBatchDetailDAO::getById)
                    .map(AssetsFlowBatchDetail::getBatchId)
                    .map(flowBatchDAO::getById).map(AssetsFlowBatch::getFlowProcessType).orElse(null);
        } else {
            flowType = Optional.ofNullable(latest).map(TableInfoSnapshot::getFlowProcessType).orElse(null);
        }
        wrapper.set(UserDataAssets::getFlowProcessType, flowType);

        wrapper.update();

        existAssets.setAssetTableName(tableName);
        existAssets.setAssetsSql(assetsSql);
        existAssets.setConfigType(info.getConfigType());
        this.asyncUpdateCount(existAssets);

        this.refreshProjectRelate(assetsId, info.getProjectId());

        acceptanceRecordDAO.getBaseMapper().update(null, new UpdateWrapper<AcceptanceRecord>().lambda()
                .eq(AcceptanceRecord::getAssetsId, assetsId)
                .eq(AcceptanceRecord::getState, AcceptanceStateEnum.wait.name())
                .set(AcceptanceRecord::getState, AcceptanceStateEnum.version_roll.name())
        );

//        Optional<Integer> lastPassVersion = acceptanceRecordDAO.lambdaQuery()
//                .select(AcceptanceRecord::getVersion)
//                .eq(AcceptanceRecord::getAssetsId, assetsId)
//                .eq(AcceptanceRecord::getState, AcceptanceStateEnum.pass.name())
//                .orderByDesc(AcceptanceRecord::getVersion)
//                .last(" limit 1")
//                .oneOpt().map(AcceptanceRecord::getVersion);

        // 前一个 交付版本 的资产版本
        Optional<Integer> lastPassVersion = userDataAssetsSnapshotDAO.lambdaQuery()
                .select(UserDataAssets::getVersion)
                .eq(UserDataAssetsSnapshot::getAssetsId, assetsId)
                .eq(UserDataAssetsSnapshot::getFlowProcessType, FlowProcessTypeEnum.deliver.name())
                .orderByDesc(UserDataAssets::getVersion)
                .last(" limit 1 ")
                .oneOpt().map(UserDataAssets::getVersion);

        AcceptanceRecord record = new AcceptanceRecord();
        record.setAssetsId(existAssets.getId());
        record.setUser(existAssets.getApplicantId());
        record.setApplicationId(nowApplyId);
        record.setVersion(newVersion);
        record.setState(AcceptanceStateEnum.wait.name());
        record.setBizType(template.getBizType());
        acceptanceRecordDAO.save(record);

        // 预设对比 并存在前一次验收通过的数据
        if (BooleanUtils.isTrue(existAssets.getPlanCompare()) && lastPassVersion.isPresent()) {
            Integer lastVersion = lastPassVersion.get();
            AjaxResult<Void> createResult = assetsCompareService.createCompare(assetsId, lastVersion, newVersion, false);
            if (!createResult.isSuccess()) {
                log.warn("planCompare Failed={}", createResult);
            } else {
                log.info("planCompare Success={} {}", lastVersion, newVersion);
            }
        }

//        String existTableName = existAssets.getAssetTableName();
        scheduler.schedule(() -> this.handleReplaceSaveAsAssets(tableName, existAssets.getId(), newVersion),
                10, TimeUnit.SECONDS);
    }

    /**
     * 更新另存资产的SQL
     *
     * @see UserDataAssetsServiceImpl#saveAs
     */
    public void handleReplaceSaveAsAssets(String tableName, Long mainId, Integer version) {
        if (StringUtils.isBlank(tableName)) {
            return;
        }
        List<UserDataAssets> relationSaveAs = userDataAssetsDAO.getBaseMapper()
                .selectList(new QueryWrapper<UserDataAssets>().lambda()
                        .select(UserDataAssets::getId, UserDataAssets::getAssetsSql)
                        .eq(UserDataAssets::getCopyMainId, mainId)
                );
        if (CollectionUtils.isEmpty(relationSaveAs)) {
            return;
        }
        for (UserDataAssets relation : relationSaveAs) {
            Optional<String> replace = ApplicationSqlUtil.replaceRelationTable(relation.getAssetsSql(), tableName);
            if (!replace.isPresent()) {
                log.warn("no relation: mainAssetsId={} assetsId={}", mainId, relation.getId());
                alertService.sendDevNormalMsg("另存资产更新失败，关联关系错误：" + mainId + " <-> " + relation.getId());
                continue;
            }

            log.info("replace relation table: assetsId={}", relation.getId());
            userDataAssetsDAO.getBaseMapper().update(null, new UpdateWrapper<UserDataAssets>().lambda()
                    .eq(UserDataAssets::getId, relation.getId())
                    .set(UserDataAssets::getAssetsSql, replace.get())
                    .set(UserDataAssets::getAssetTableName, tableName)
                    .set(UserDataAssets::getVersion, version)
            );

            userDataAssetsUploadFtpHelper.addFtpTask(relation.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncApplicationNo(Long targetId) {
        final LambdaQueryWrapper<TgApplicationInfo> wq = Wrappers.<TgApplicationInfo>lambdaQuery()
                .eq(TgApplicationInfo::getApplicationType, ApplicationConst.ApplicationType.DATA_APPLICATION)
                .eq(Objects.nonNull(targetId), TgApplicationInfo::getId, targetId);

        final List<TgApplicationInfo> tgApplicationInfos = applicationInfoMapper.selectList(wq);

        final List<TgApplicationInfo> oldApplicationId = tgApplicationInfos.stream()
                .filter(a -> Objects.isNull(a.getOldApplicationId()))
                .collect(Collectors.toList());

        Map<Long, String> idApplicationNoMap = new HashMap<>();

        // 设置所有初版需求id
        for (TgApplicationInfo info : oldApplicationId) {
            if (Objects.nonNull(info.getTemplateId())) {
                TgTemplateInfo templateInfo = new TgTemplateInfo().selectById(info.getTemplateId());
                if (Objects.isNull(templateInfo)) {
                    continue;
                }
                boolean isPersistence = Objects.equals(info.getRequireTimeType(), ApplicationConst.RequireTimeType.PERSISTENCE);
                final Long id = info.getId();
                DateTime dateTime = DateUtil.parseDateTime(info.getCreateTime());
                String s = BizTypeEnum.valueOf(templateInfo.getBizType()).getShortId() + "_" + id + DateUtil.format(dateTime, "MMdd");
                if (isPersistence) {
                    s = "dur_" + s;
                    info.setApplicationNo(s);
                } else {
                    info.setApplicationNo(s);
                }
                idApplicationNoMap.put(info.getId(), s);

                applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                        .eq(TgApplicationInfo::getId, info.getId())
                        .set(TgApplicationInfo::getApplicationNo, info.getApplicationNo()));

                ApplicationTaskConfig config = applicationTaskConfigDAO.queryByApplicationId(info.getId());
                if (Objects.isNull(config)) {
                    continue;
                }
                if (StringUtils.isNoneBlank(config.getZdyProduct())) {
                    String nw = ReUtil.replaceAll(config.getZdyProduct(),
                            "temp\\..*_zdylab", "temp." + info.getApplicationNo() + "_zdylab");
                    config.setZdyProduct(nw);
                }

                applicationTaskConfigDAO.update(null, new UpdateWrapper<ApplicationTaskConfig>().lambda()
                        .eq(ApplicationTaskConfig::getApplicationId, info.getId())
                        .set(ApplicationTaskConfig::getApplicationNo, info.getApplicationNo())
                        .set(StringUtils.isNoneBlank(config.getZdyProduct()), ApplicationTaskConfig::getZdyProduct, config.getZdyProduct())
                );
            }
        }

        final List<TgApplicationInfo> reApplication = tgApplicationInfos.stream()
                .filter(a -> Objects.nonNull(a.getOldApplicationId()))
                .collect(Collectors.toList());

        final Map<Long, TgApplicationInfo> infoMap = reApplication.stream().collect(Collectors.toMap(TgApplicationInfo::getId, v -> v));

        final List<ApplicationTree> tree = oldApplicationId.stream().map(a -> {
            final ApplicationTree applicationTree = new ApplicationTree();
            applicationTree.setApplicationId(a.getId());
            applicationTree.setChildIds(buildChild(reApplication, a.getId()));
            return applicationTree;
        }).collect(Collectors.toList());

        for (ApplicationTree applicationTree : tree) {
            final String no = idApplicationNoMap.get(applicationTree.getApplicationId());
            for (Long id : applicationTree.getChildIds()) {
                final TgApplicationInfo info = infoMap.get(id);
                info.setApplicationNo(no);
                applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                        .eq(TgApplicationInfo::getId, info.getId())
                        .set(TgApplicationInfo::getApplicationNo, info.getApplicationNo()));

                ApplicationTaskConfig config = applicationTaskConfigDAO.queryByApplicationId(info.getId());
                if (Objects.isNull(config)) {
                    continue;
                }
                if (StringUtils.isNoneBlank(config.getZdyProduct())) {
                    String nw = ReUtil.replaceAll(config.getZdyProduct(),
                            "temp\\..*_zdylab", "temp." + info.getApplicationNo() + "_zdylab");
                    config.setZdyProduct(nw);
                }

                applicationTaskConfigDAO.update(null, new UpdateWrapper<ApplicationTaskConfig>().lambda()
                        .eq(ApplicationTaskConfig::getApplicationId, info.getId())
                        .set(ApplicationTaskConfig::getApplicationNo, info.getApplicationNo())
                        .set(StringUtils.isNoneBlank(config.getZdyProduct()), ApplicationTaskConfig::getZdyProduct, config.getZdyProduct())
                );
            }
        }

    }


    private List<Long> buildChild(List<TgApplicationInfo> reApplication, Long parentId) {
        final List<TgApplicationInfo> collect = reApplication.stream().filter(a -> a.getOldApplicationId().equals(parentId))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            final List<Long> child = collect.stream().map(TgApplicationInfo::getId).collect(Collectors.toList());
            final ArrayList<Long> list = Lists.newArrayList(child);
            for (Long aLong : list) {
                child.addAll(buildChild(reApplication, aLong));
            }
            return child;
        }
        return new ArrayList<>();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSaveAs(Long id) {
        UserDataAssets origin = userDataAssetsDAO.getById(id);
        if (!Objects.equals(SecurityUtils.getUserId(), origin.getApplicantId())) {
            return false;
        }
        // 删除项目关联资产
        final LambdaQueryWrapper<ProjectDataAssetsRelate> wq = Wrappers.<ProjectDataAssetsRelate>lambdaQuery()
                .eq(ProjectDataAssetsRelate::getUserAssetId, id);
        projectDataAssetsRelateMapper.delete(wq);
        return userDataAssetsDAO.update(new UpdateWrapper<UserDataAssets>().lambda().eq(UserDataAssets::getId, id)
                .set(UserDataAssets::getStatus, ApplicationConst.ApplyStatus.DISABLE));
    }

    @Override
    public Long saveAs(ApplicationSaveAsRequest request) {
        boolean valid = userDataAssetsDAO.validProjectName(request.getProjectName());
        if (!valid) {
            throw new CustomException("当前另存为名称已存在，请重新命名");
        }
        UserDataAssets origin = userDataAssetsDAO.getById(request.getAssetsId());
        Optional<TemplateTypeEnum> typeOpt = TemplateTypeEnum.of(origin.getTemplateType());
        if (!typeOpt.isPresent()) {
            throw new CustomException("资产对应的模板类型不支持另存");
        }

        UserDataAssets saveAssets = new UserDataAssets();
        BeanUtils.copyProperties(origin, saveAssets);
        saveAssets.setId(null)
                .setVersion(1)
                .setProjectName(request.getProjectName())
                .setCopyFromId(origin.getId())
                .setCopyMainId(Optional.ofNullable(origin.getCopyMainId()).orElse(origin.getId()))
                .setFirstSyncTag(0)
                .setNeedSyncTag(CommonConstants.NOT_UPDATE_TASK)
                .setStatus(ApplicationConst.ApplyStatus.ENABLE)
                .setFtpStatus(null)
                .setFtpPath(null)
                .setFtpErrorMessage(null)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());

        // 1. 重新构造选择的字段 以及 数据范围
        Optional.ofNullable(request.getQuery()).ifPresent(queryCriteria -> {
            // select
            String[] selectFields;
            if (StringUtils.isNotBlank(queryCriteria.getFieldNames())) {
//                List<ApplicationDataDto.Header> headers = applicationService.buildHeaders(origin);
                selectFields = Stream.of(queryCriteria.getFieldNames().split(",")).map(v -> "`" + v + "`")
                        .toArray(String[]::new);
            } else {
                selectFields = new String[]{"*"};
            }

            // where
            String whereSql = queryCriteria.buildWhereSQL();
            String sql = origin.getAssetsSql();
            // 需要将whereSql拼接到原sql中，原则上是用AST解析出原sql的where子句，现在直接用嵌套子查询
            String[] finalSelectFields = selectFields;
            String finalSQL = new SQL() {
                {
                    // 注意此处涉及后续资产升级时 SQL改动的情况，所以格式不能改，要改的话下游逻辑也要一起改
                    this.SELECT(finalSelectFields).FROM("( " + sql + " ) tt");
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(whereSql)) {
                        this.WHERE(whereSql);
                    }
                }
            }.toString();
            saveAssets.setAssetsSql(finalSQL);
        });
        log.info("sql={}", saveAssets.getAssetsSql());
        // 2. 由于引用原本的申请，不需要复制申请对应的自定义指标数据
        userDataAssetsDAO.save(saveAssets);

        ProjectDataAssetsRelate relate = new ProjectDataAssetsRelate()
                .setProjectId(request.getProjectId())
                .setUserAssetId(saveAssets.getId())
                .setProType(ProjectRelateEnum.master.name());
        projectDataAssetsRelateMapper.insert(relate);

        this.asyncUpdateCount(saveAssets);

//        userDataAssetsUploadFtpHelper.uploadFtp(saveAssets.getId());
        scheduler.schedule(() -> userDataAssetsUploadFtpHelper.addFtpTask(saveAssets.getId()), 10, TimeUnit.SECONDS);
        return saveAssets.getId();
    }

    public void updateCount(UserDataAssets assets) {
        String sql = assets.getAssetsSql();
        Long id = assets.getId();
        String assetTableName = assets.getAssetTableName();

        String prodCodeVal = "";
        try {
            prodCodeVal = ckClusterAdapter.mixDistinctConcat(assetTableName, "t_1_prodcode");
        } catch (Exception e) {
            try {
                prodCodeVal = ckClusterAdapter.mixDistinctConcat(assetTableName, "prodcode");
            } catch (Exception ex) {
            }
        }

        try {
            Long count = ckClusterAdapter.mixCount(assetTableName, sql);
            long finalCnt = Optional.ofNullable(count).orElse(0L);
            userDataAssetsDAO.getBaseMapper().update(null, new UpdateWrapper<UserDataAssets>().lambda()
                    .set(UserDataAssets::getDataTotal, finalCnt)
                    .set(UserDataAssets::getProdCode, prodCodeVal)
                    .eq(UserDataAssets::getId, id)
            );
            log.info("id={} sql={} count={}", id, sql, finalCnt);
        } catch (Exception e) {
            log.error("id={} sql={}", id, sql, e);
        }
    }

    @Override
    public void asyncUpdateCount(UserDataAssets assets) {
        if (ApplicationConfigTypeConstant.isFile(assets.getConfigType())) {
            return;
        }
        scheduler.schedule(() -> ttl.execute(() -> updateCount(assets)), 10, TimeUnit.SECONDS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> createDataAssetsByCallback(UserAssetsCallbackRequest request) {
        log.info("request={}", request);
        Long applyId = request.getApplicationId();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(RedisKeys.Apply.createAssetsKey(applyId), 1, Duration.ofSeconds(10));
        if (BooleanUtils.isNotTrue(lock)) {
            log.info("try lock failed");
            return AjaxResult.error("重复请求");
        }
        if (Objects.isNull(applyId) || StringUtils.isBlank(request.getTableName())) {
            return AjaxResult.error("参数缺失");
        }

        TgApplicationInfo info = new TgApplicationInfo().selectById(applyId);
        if (Objects.isNull(info)) {
            return AjaxResult.error("申请不存在");
        }
        TgTemplateInfo temp = new TgTemplateInfo().selectOne(new QueryWrapper<TgTemplateInfo>().lambda()
                .select(TgTemplateInfo::getTemplateType).eq(TgTemplateInfo::getId, info.getTemplateId())
        );
        if (!Objects.equals(temp.getTemplateType(), TemplateTypeEnum.normal.name()) &&
                !Objects.equals(temp.getTemplateType(), TemplateTypeEnum.customized.name())) {
            return AjaxResult.error("仅支持常规和通用模板的申请");
        }

        // 读取 手动更新 的标记key
        String markApplyKey = RedisKeys.Assets.getMarkApplyKey(applyId);
        boolean applyTrigger = BooleanUtils.isTrue(redisTemplate.hasKey(markApplyKey));
        String snapshotType = Optional.ofNullable(request.getTriggerId())
                .map(v -> Objects.equals(DataAssetsConstants.RE_APPLY_TRIGGER_ID, v)
                        ? AssetsSnapshotTypeEnum.re_apply.name()
                        : AssetsSnapshotTypeEnum.schedule_deliver.name())
                .orElse(AssetsSnapshotTypeEnum.manual_deliver.name());
        if (applyTrigger) {
            snapshotType = AssetsSnapshotTypeEnum.apply_deliver.name();
            redisTemplate.delete(markApplyKey);
        }

        UpsertAssetsBO bo = UpsertAssetsBO.builder().triggerId(request.getTriggerId()).info(info)
                .tableName(request.getTableName())
                .snapshotType(snapshotType).build();

        this.upsertDataAssets(bo);
        applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                .set(TgApplicationInfo::getDataState, ApplyDataStateEnum.success.name())
                .eq(TgApplicationInfo::getId, info.getId())
        );

        this.handleBizMsgNotice(info);

        return AjaxResult.succeed();
    }

    private void handleBizMsgNotice(TgApplicationInfo info) {
        try {
            // 修改通知
            final Long id = info.getId();

            final UserDataAssets userDataAssets = userDataAssetsDAO.getBaseMapper().selectById(info.getAssetsId());
            log.info("apply={} assetsId={} {}", info.getId(), info.getAssetsId(), Objects.isNull(userDataAssets));

            final LambdaQueryWrapper<TgNoticeRead> exist = Wrappers.<TgNoticeRead>lambdaQuery()
                    .eq(TgNoticeRead::getApplicationId, id)
                    .in(TgNoticeRead::getAuditType, AuditTypeEnum.SUCCESS.getValue())
                    .orderByDesc(TgNoticeRead::getCreateTime)
                    .last(" limit 1");

            List<TgNoticeRead> existSuccessList = tgNoticeReadMapper.selectList(exist);
            if (CollectionUtils.isEmpty(existSuccessList)) {
                final LambdaQueryWrapper<TgNoticeRead> wq = Wrappers.<TgNoticeRead>lambdaQuery()
                        .eq(TgNoticeRead::getApplicationId, id)
                        .in(TgNoticeRead::getAuditType, Lists.newArrayList(1, 4));
                final List<TgNoticeRead> tgNoticeReads = tgNoticeReadMapper.selectList(wq);

                this.copyNewNotice(tgNoticeReads,
                        Optional.ofNullable(userDataAssets).map(UserDataAssets::getVersion).orElse(1),
                        AuditTypeEnum.SUCCESS);
            } else {
                for (TgNoticeRead tgNoticeRead : existSuccessList) {
                    tgNoticeRead.setVersion(Optional.ofNullable(userDataAssets).map(UserDataAssets::getVersion).orElse(1));
                    tgNoticeReadMapper.updateById(tgNoticeRead);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public List<UserDataAssets> queryAllAssets(String expireType, List<Long> templateId, Collection<Long> assetsIds) {
        if (CollectionUtils.isEmpty(assetsIds)) {
            return Collections.emptyList();
        }

        boolean deprecated = Objects.equals(expireType, "deprecated");
        boolean expire = Objects.equals(expireType, "expire");

        LambdaQueryWrapper<UserDataAssets> wrapper = new QueryWrapper<UserDataAssets>()
                .lambda()
                .eq(UserDataAssets::getStatus, ApplicationConst.ApplyStatus.ENABLE)
                .in(CollectionUtils.isNotEmpty(templateId), UserDataAssets::getTemplateId, templateId);
        wrapper.in(UserDataAssets::getId, assetsIds);
        if (deprecated) {
            wrapper.eq(UserDataAssets::getDeprecated, true);
        } else {
            if (expire) {
                wrapper.lt(UserDataAssets::getDataExpire, LocalDateTime.now())
                        .eq(UserDataAssets::getDeprecated, false);
            } else {
                wrapper.ge(UserDataAssets::getDataExpire, LocalDateTime.now());
            }
        }
        return userDataAssetsDAO.list(wrapper);
    }

    @Override
    public List<UserDataAssets> queryAllValidAssetsByUserId(boolean expire, boolean sync) {
        Long userId = SecurityUtils.getUserId();

        LambdaQueryWrapper<UserDataAssets> wrapper = new QueryWrapper<UserDataAssets>()
                .lambda().eq(UserDataAssets::getStatus, ApplicationConst.ApplyStatus.ENABLE);
        wrapper.eq(UserDataAssets::getApplicantId, userId);
        if (expire) {
            wrapper.lt(UserDataAssets::getDataExpire, LocalDateTime.now());
        } else {
            wrapper.ge(UserDataAssets::getDataExpire, LocalDateTime.now());
        }
        wrapper.and(v -> v.isNull(UserDataAssets::getConfigType).or()
                .ne(UserDataAssets::getConfigType, ApplicationConfigTypeConstant.FILE_TYPE));
        return userDataAssetsDAO.list(wrapper);
    }

    /**
     * @see MyDataDirServiceImpl#getApplicationList()
     */
    @Override
    public AjaxResult<List<UserDataAssetsSyncDTO>> querySyncList() {
        List<UserDataAssets> latestAssets = this.queryAllValidAssetsByUserId(false, true);
        List<Long> assetsIds = Lambda.buildList(latestAssets, UserDataAssets::getId);
        if (CollectionUtils.isEmpty(assetsIds)) {
            return AjaxResult.success("empty", null);
        }
        List<UserDataAssetsSnapshot> snapshots = userDataAssetsSnapshotDAO.queryByAssetsIds(assetsIds);

        List<UserDataAssets> total = new ArrayList<>();
        total.addAll(latestAssets);
        total.addAll(snapshots);

        List<String> versions = new ArrayList<>();
        total.forEach(v -> versions.add(v.getAssetsVersion()));
        Map<String, Integer> countMap = arkbiAnalysisService.countAssetsVersion(versions);

        Map<String, ApplicationDataUpdateRecord> dataUpdateRecordMap = dataUpdateRecordDAO.queryLatestByAssetVersions(versions);

        List<UserDataAssetsSyncDTO> list = total.stream().map(v -> {
            ApplicationDataUpdateRecord record = dataUpdateRecordMap.get(v.getAssetsVersion());
            Integer state = Optional.ofNullable(record).map(ApplicationDataUpdateRecord::getUpdateState)
                    .orElse(UpdateRecordStateType.NONE);
//            Boolean handle = redisTemplate.opsForSet().isMember(RedisKeys.Assets.SYNC_HANDLE_CACHE, v.getAssetsVersion());
            return UserDataAssetsSyncDTO.builder()
                    .createTime(v.getCreateTime())
                    .assetsId(v.getAssetsId())
                    .assetsName(v.getProjectName())
                    .version(v.getVersion())
                    .chartCount(Optional.ofNullable(countMap.get(v.getAssetsVersion())).orElse(0))
                    .state(BooleanUtils.isTrue(false) ? UpdateRecordStateType.UPDATING : state)
                    .dataExpire(v.getDataExpire())
                    .build();
        }).collect(Collectors.toList());

        return AjaxResult.success(list);
    }

    @Override
    public List<AssetsNode> queryUserAssetsTree(AssetsDirRequest request) {
        StopWatch watch = new StopWatch();
        Long userId = SecurityUtils.getUserId();

        watch.start("mapping");

        Set<Long> projectIds = projectHelperDAO.queryProjects(userId);
        if (CollectionUtils.isEmpty(projectIds)) {
            return Collections.emptyList();
        }

        boolean normal = Objects.equals(request.getExpireType(), "normal");
        List<Project> projectList = projectMapper.selectBatchIds(projectIds);
        Map<Long, Project> projectMap = Lambda.buildMap(projectList, Project::getId);
        Set<Long> customerIds = Lambda.buildSet(projectList, Project::getCustomerId);
        List<Customer> customers = customerMapper.selectBatchIds(customerIds);
        Map<Long, Customer> customerMap = Lambda.buildMap(customers, Customer::getId);
        watch.stop();

        List<AssetsNode> list = new ArrayList<>();
        customers.stream().map(v -> CustomerNode.builder()
                .name(v.getShortName())
                .icon(ApplicationConst.AssetsIcon.CUSTOMER)
                .createTime(v.getCreateTime())
                .updateTime(v.getUpdateTime())
                .customerId(v.getId())
                .parentId(AssetsNode.ROOT)
                .creator(v.getCreator())
                .build()).forEach(list::add);
        this.fillProjectAssets(projectList, userId, list);

        // 可使用Tab 追加的数据
        if (normal) {
            this.fillFileAssets(watch, projectMap, customerMap, list);
            this.fillArkBiData(watch, userId, CommonConstants.ICON_DASHBOARD, list);
        }
        this.fillArkBiData(watch, userId, CommonConstants.ICON_CHART, list);

        // 填入数据资产
        List<ProjectDataAssetsRelate> relates = projectDataAssetsRelateMapper
                .selectList(new QueryWrapper<ProjectDataAssetsRelate>().lambda()
                        .in(ProjectDataAssetsRelate::getProjectId, projectIds)
                        .isNotNull(ProjectDataAssetsRelate::getUserAssetId)
                );
        Map<Long, List<ProjectDataAssetsRelate>> assetsProjectMap = relates.stream()
                .collect(Collectors.groupingBy(ProjectDataAssetsRelate::getUserAssetId));

        watch.start("data");
        this.fillDataAssets(request, assetsProjectMap, projectMap, customerMap, list);
        watch.stop();

        this.fillUserInfo(watch, projectIds, list);
        this.fillNId(list);

        watch.start("search");
        List<AssetsNode> result = this.searchAssets(list, request);
        watch.stop();
        long ms = watch.getTotalTimeMillis();
        if (ms > 300) {
            log.warn("TIMEOUT TREE: {}ms {}", ms, watch.prettyPrint());
        }
        return result;
    }

    private void fillNId(List<AssetsNode> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (AssetsNode node : list) {
            node.setNId(StrUtil.randomAlpha(8));
        }
    }

    private void fillProjectAssets(List<Project> projectList, Long userId, List<AssetsNode> list) {
        projectList.stream().map(v -> {
            ProjectNode tmp = ProjectNode.builder()
                    .name(v.getName())
                    .projectId(v.getId())
                    .icon(ApplicationConst.AssetsIcon.PROJECT)
                    .createTime(v.getCreateTime())
                    .updateTime(v.getUpdateTime())
                    .creator(v.getCreator())
                    .build();
            // 需要考虑扩展支持一个项目关联多个客户
            tmp.fillParentId(v.getCustomerId(), ApplicationConst.AssetsIcon.CUSTOMER);
            if (Objects.equals(v.getProjectManager(), userId)) {
                tmp.setActions(DataDirConst.ActionType.PROJECT_ACTIONS);
            } else {
                tmp.setActions(Collections.emptyList());
            }
            return tmp;
        }).forEach(list::add);
    }

    private void fillFileAssets(StopWatch watch, Map<Long, Project> projectMap,
                                Map<Long, Customer> customerMap, List<AssetsNode> list) {
        watch.start("file");
        List<UserFileAssets> userFileAssets = userFileAssetsService.queryAvailableAssets(projectMap.keySet());
        Long userId = SecurityUtils.getUserId();
        userFileAssets.stream().map(v -> {
            Optional<Project> proOpt = Optional.ofNullable(projectMap.get(v.getProjectId()));
            FileAssetsNode tmp = FileAssetsNode.builder()
                    .name(v.getName())
                    .icon(ApplicationConst.AssetsIcon.FILE)
                    .createTime(v.getCreateTime())
                    .assetsId(v.getId())
                    .creator(v.getCreator())
                    .newProjectName(proOpt.map(Project::getName).orElse(""))
                    .customerName(proOpt.map(Project::getCustomerId).map(customerMap::get).map(Customer::getShortName).orElse(""))
                    .updateTime(v.getCreateTime())
                    .build();
            tmp.fillParentId(v.getProjectId(), ApplicationConst.AssetsIcon.PROJECT);

            List<Integer> actions = new ArrayList<>();
            actions.add(DataDirConst.ActionType.FILE_DOWNLOAD);
            if (Objects.equals(tmp.getCreator(), userId)) {
                actions.add(DataDirConst.ActionType.FILE_DELETE);
            }
            if (StringUtils.isNoneBlank(v.getPdfPath())) {
                actions.add(DataDirConst.ActionType.FILE_PREVIEW);
            }
            tmp.setActions(actions);

            return tmp;
        }).forEach(list::add);
        watch.stop();
    }

    // 获取当前申请人的BI图表 V1.9.0不做共享协作
    private void fillArkBiData(StopWatch watch, Long userId, String iconChart, List<AssetsNode> list) {
        watch.start(iconChart);
        List<ArkbiAnalysis> arkbiAnalyses = arkbiAnalysisService
                .lambdaQuery()
                .eq(ArkbiAnalysis::getCreateBy, userId)
                .eq(ArkbiAnalysis::getStatus, 1)
                .eq(ArkbiAnalysis::getType, iconChart)
                .list();
        //拿到所有项目的ID
        List<Long> assetsIds = arkbiAnalyses.stream()
                .flatMap(v -> v.getAssetsIds().stream())
                .collect(Collectors.toList());
        List<UserDataAssets> apps = Lambda.queryListIfExist(assetsIds, userDataAssetsDAO.getBaseMapper()::selectBatchIds);
        Map<Long, UserDataAssets> applicationInfoMap = apps.stream()
                .collect(Collectors.toMap(UserDataAssets::getId, Function.identity()));

        arkbiAnalyses.stream().map(v -> {
            ArkBiNode tmp = ArkBiNode.builder()
                    .name(v.getName())
                    .createTime(v.getCreateTime())
                    .updateTime(v.getUpdateTime())
                    .id(v.getId())
                    .creator(v.getCreateBy())
                    .version(v.getFirstVersion())
                    .build();
            tmp.setProjectNames(new HashSet<>());
            tmp.setAssetsIds(new HashSet<>());
            tmp.setApplicantIds(new HashSet<>());

            List<Long> ids = v.getAssetsIds();
            if (CollectionUtils.isNotEmpty(ids)) {
                List<UserDataAssets> infos = ids.stream()
                        .filter(applicationInfoMap::containsKey)
                        .map(applicationInfoMap::get)
                        .filter(Objects::nonNull).collect(Collectors.toList());
                infos.forEach(o -> {
                    tmp.getProjectNames().add(o.getProjectName());
                    tmp.getAssetsIds().add(o.getId());
                    tmp.getApplicantIds().add(o.getApplicantId());
                });
            }

            tmp.setActions(DataDirConst.ActionType.BI_DEFAULT_ACTIONS);
            tmp.setExtAnalysisId(v.getAnalysisId());
            tmp.setIcon(v.getType());

            if (Objects.equals(v.getType(), CommonConstants.ICON_CHART)) {
                tmp.setIcon(ApplicationConst.AssetsIcon.CHART);
                List<Long> aids = v.getAssetsIds();
                if (CollectionUtils.isNotEmpty(aids)) {
                    tmp.fillParentId(aids.get(0), ApplicationConst.AssetsIcon.DATA);
                } else {
                    log.warn("图表未关联资产: id={} name={}", v.getId(), v.getName());
                    tmp.markRoot();
                }
            } else {
                tmp.markRoot();
                tmp.setIcon(ApplicationConst.AssetsIcon.DASHBOARD);
            }
            return tmp;
        }).forEach(list::add);
        watch.stop();
    }

    private void fillUserInfo(StopWatch watch, Set<Long> projectIds, List<AssetsNode> list) {
        watch.start("user");
        List<ProjectHelper> relateUser = projectHelperMapper.selectList(new QueryWrapper<ProjectHelper>().lambda()
                .in(ProjectHelper::getProjectId, projectIds));
        Map<Long, List<ProjectHelper>> projectUserMap = relateUser.stream()
                .collect(Collectors.groupingBy(ProjectHelper::getProjectId));
        Set<Long> projectUsers = list.stream().filter(AssetsNode::isProject)
                .flatMap(v -> projectUserMap.get(v.getBizId()).stream().map(ProjectHelper::getUserId))
                .collect(Collectors.toSet());

        // 用户名
        Set<Long> userIds = list.stream().map(AssetsNode::getCreator).collect(Collectors.toSet());
        userIds.addAll(projectUsers);
        Map<Long, String> userMap = userService.selectUserNameMapByIds(userIds);
        list.forEach(v -> v.setCreatorName(userMap.get(v.getCreator())));
        list.stream().filter(AssetsNode::isProject).filter(v -> v instanceof ProjectNode).forEach(v -> {
            ProjectNode project = (ProjectNode) v;
            List<ProjectHelper> userList = projectUserMap.get(project.getProjectId());
            String users = userList.stream().map(ProjectHelper::getUserId).map(userMap::get)
                    .collect(Collectors.joining("、"));
            project.setHelper(users);
        });
        watch.stop();
    }

    /**
     * @see UserDataAssetsNode
     */
    private void fillDataAssets(AssetsDirRequest request,
                                Map<Long, List<ProjectDataAssetsRelate>> assetsProjectMap,
                                Map<Long, Project> projectMap, Map<Long, Customer> customerMap, List<AssetsNode> list) {
        String expireType = request.getExpireType();
        List<UserDataAssets> userDataAssets = this.queryAllAssets(expireType, request.getTemplateId(), assetsProjectMap.keySet());
        if (CollectionUtils.isEmpty(userDataAssets)) {
            return;
        }
        Set<String> versionList = Lambda.buildSet(userDataAssets, UserDataAssets::buildTableVersion);
        List<TableInfoSnapshot> tables = tableInfoSnapshotDAO.queryByVersion(versionList);
        Map<String, TableInfoSnapshot> tableVerMap = Lambda.buildMap(tables, TableInfoSnapshot::buildTableVersion);

        List<Long> assetsIds = Lambda.buildList(userDataAssets, UserDataAssets::getId);
        Map<Long, Integer> versionMap = userDataAssetsSnapshotDAO.batchCountSnapshot(assetsIds);

        Map<String, AcceptanceRecord> recordMap = acceptanceRecordDAO.queryByAssetsIdAndVersion(userDataAssets);
        List<Long> recordUsers = Lambda.buildList(recordMap.values(), AcceptanceRecord::getUser);
        Map<Long, String> userNameMap = Lambda.queryMapIfExist(recordUsers, userService::selectUserNameMapByIds);

        List<Long> originIds = Lambda.buildNonNullList(userDataAssets, UserDataAssets::getCopyFromId);
        Map<Long, String> originNameMap = userDataAssetsDAO.queryAssetsName(originIds);

        AssetsItemContext context = AssetsItemContext.builder()
                .userNameMap(userNameMap)
                .versionMap(versionMap)
                .recordMap(recordMap)
                .build();

        // 打包名称 搜索
        List<Long> applyIds = Lambda.buildList(userDataAssets, UserDataAssets::getSrcApplicationId);
        if (CollectionUtils.isNotEmpty(request.getPackTailName())) {
            Set<Long> matchIds = new HashSet<>();
            // 特殊处理不打包的情况
            if (request.getPackTailName().contains(TemplatePackTailSettingServiceImpl.NONE)) {
                request.getPackTailName().remove(TemplatePackTailSettingServiceImpl.NONE);
                List<TgApplicationInfo> matchApply = applicationDAO.lambdaQuery()
                        .select(TgApplicationInfo::getId)
                        .eq(TgApplicationInfo::getPackTailSwitch, false)
                        .in(TgApplicationInfo::getId, applyIds)
                        .list();
                Set<Long> tmp = Lambda.buildSet(matchApply, TgApplicationInfo::getId);
                matchIds.addAll(tmp);
            }
            if (CollectionUtils.isNotEmpty(request.getPackTailName())) {
                List<TgApplicationInfo> matchApply = applicationDAO.lambdaQuery()
                        .select(TgApplicationInfo::getId)
                        .in(TgApplicationInfo::getPackTailName, request.getPackTailName())
                        .in(TgApplicationInfo::getId, applyIds)
                        .list();
                Set<Long> tmp = Lambda.buildSet(matchApply, TgApplicationInfo::getId);
                matchIds.addAll(tmp);
            }
            if (CollectionUtils.isEmpty(matchIds)) {
                return;
            }

            userDataAssets = userDataAssets.stream().filter(v -> matchIds.contains(v.getSrcApplicationId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(userDataAssets)) {
                return;
            }
        }

        List<TgApplicationInfo> graList = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getTimeGra)
                .in(TgApplicationInfo::getId, applyIds)
                .list();

        Map<Long, String> timeGraMap = graList.stream().filter(v -> Objects.nonNull(v.getTimeGra()))
                .collect(Collectors.toMap(TgApplicationInfo::getId, TgApplicationInfo::getTimeGra, (front, current) -> current));
        context.setTimeGraMap(timeGraMap);

        // 时间颗粒度 搜索
        if (CollectionUtils.isNotEmpty(request.getTimeGra())) {
            Set<Long> matchIds = graList.stream()
                    .filter(v -> request.getTimeGra().stream().anyMatch(x -> ApplyUtil.containGra(v.getTimeGra(), x)))
                    .map(TgApplicationInfo::getId).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(matchIds)) {
                return;
            }
            userDataAssets = userDataAssets.stream().filter(v -> matchIds.contains(v.getSrcApplicationId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(userDataAssets)) {
                return;
            }
        }

        Set<Long> tempIds = Lambda.buildSet(userDataAssets, UserDataAssets::getTemplateId);
        Map<Long, String> templateMap = Lambda.queryMapIfExist(tempIds, v ->
                tgTemplateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                        .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName)
                        .in(TgTemplateInfo::getId, v)), TgTemplateInfo::getId, TgTemplateInfo::getTemplateName);
        userDataAssets.stream().flatMap(assets -> {
            context.setAssets(assets);
            String expireStr = Optional.ofNullable(assets.getDataExpire())
                    .map(dateExpire -> DateUtils.parseDateToStr("yyyy-MM-dd", dateExpire)).orElse(null);
            List<ProjectDataAssetsRelate> proList = assetsProjectMap.get(assets.getId());
            if (CollectionUtils.isEmpty(proList)) {
                return Stream.empty();
            }
            return proList.stream().map(ProjectDataAssetsRelate::getProjectId).distinct().map(proId -> {
                Optional<Project> proOpt = Optional.ofNullable(projectMap.get(proId));
                UserDataAssetsNode node = new UserDataAssetsNode();
                BeanUtils.copyProperties(assets, node);
                node.setTemplateId(assets.getTemplateId());
                node.setTemplateName(templateMap.get(assets.getTemplateId()));
                node.setDataExpire(expireStr);
                node.setAssetsId(assets.getId());
                node.setIcon(ApplicationConst.AssetsIcon.DATA);
                node.setRequireTimeType(assets.getRequireTimeType());
                node.setSrcApplicationId(assets.getSrcApplicationId());
                node.fillParentId(proId, ApplicationConst.AssetsIcon.PROJECT);
                node.setNewProjectName(proOpt.map(Project::getName).orElse(""));
                node.setCustomerName(proOpt.map(Project::getCustomerId).map(customerMap::get).map(Customer::getShortName).orElse(""));
                node.setFtpStatus(assets.getFtpStatus());
                node.setFtpErrorMessage(assets.getFtpErrorMessage());
                node.setCopyFromName(originNameMap.get(assets.getCopyFromId()));

                Boolean scheduler = TemplateTypeEnum.of(assets.getTemplateType()).map(TemplateTypeEnum::isSchedulerTaskType).orElse(false);
                if (scheduler) {
                    String remark = Optional.ofNullable(assets.getFlowDetailId())
                            .flatMap(assetsFlowBatchDetailDAO::queryBatchId)
                            .map(flowBatchDAO::getById).map(AssetsFlowBatch::getRemark).orElse("");
                    node.setTableRemark(remark);
                } else {
                    Optional<TableInfoSnapshot> snapOpt = Optional.ofNullable(tableVerMap.get(assets.buildTableVersion()));
                    node.setTableRemark(snapOpt.map(TableInfoSnapshot::getRemark).orElse(""));
                }

//                node.setTableRemark(Optional.ofNullable(tableVerMap.get(assets.buildTableVersion())).map(TableInfoSnapshot::getRemark).orElse(""));

                this.fillDataAssetsFieldVal(context, node);
                return node;
            });
        }).forEach(list::add);
    }

    /**
     * 循环调用，需要顾虑性能影响
     * 填充需要特殊处理的字段, 无外部IO调用, 纯变量处理
     */
    private void fillDataAssetsFieldVal(AssetsItemContext context, UserDataAssetsNode item) {
        if (Objects.isNull(item)) {
            return;
        }

//        item.setTimeGra(context.getTimeGraMap().get(item.getSrcApplicationId()));
        ApplyUtil.cleanGra(context.getTimeGraMap().get(item.getSrcApplicationId())).ifPresent(item::setTimeGra);
        UserDataAssets assets = context.getAssets();
        item.setTotalVersion(Optional.ofNullable(context.getVersionMap().get(assets.getId())).orElse(0));
        Optional<AcceptanceRecord> recordOpt = Optional.ofNullable(context.getRecordMap().get(assets.getAssetsVersion()));
        String state = recordOpt.map(AcceptanceRecord::getState).orElse(null);
        item.setState(state);
        String username = recordOpt.map(v -> {
            if (v.getUser() == 0L) {
                return "系统";
            }
            return context.getUserNameMap().get(v.getUser());
        }).orElse("");
        item.setUser(Objects.equals(state, AcceptanceStateEnum.wait.name()) ? "" : username);

        List<Integer> actions = assetsAdapter.buildActions(assets);
        item.setActions(actions);
    }

    private List<AssetsNode> searchAssets(List<AssetsNode> nodes, AssetsDirRequest request) {
        if (CollectionUtils.isEmpty(nodes)) {
            return nodes;
        }

        // 构建树
        Map<String, List<AssetsNode>> parentMap = nodes.stream().collect(Collectors.groupingBy(AssetsNode::getParentId));
        List<AssetsNode> roots = parentMap.get(AssetsNode.ROOT);
        if (CollectionUtils.isEmpty(roots)) {
            return nodes;
        }
        Set<Long> visited = new HashSet<>();
        for (AssetsNode root : roots) {
            AssetsTreeUtil.fillChild(root, parentMap, visited);
        }

        // 搜索，剪枝
        List<AssetsNode> remove = new ArrayList<>();
//        boolean needClean = request.needClean();
        boolean needClean = true;
        for (AssetsNode root : roots) {
            boolean search = AssetsTreeUtil.search(root, request);
            boolean emptyDir;
            if (needClean) {
                emptyDir = AssetsTreeUtil.cleanEmptyDir(root);
            } else {
                emptyDir = false;
            }
            if (!search || emptyDir) {
                remove.add(root);
            }
        }
        roots.removeAll(remove);
        roots.removeIf(AssetsNode::isHidden);
        return roots;
//        return AssetsTreeUtil.treeToList(roots);
    }

    /**
     * 针对表查询列表 不组装树
     */
    @Override
    public List<AssetsNode> queryUserAssets(AssetsDirRequest request) {
        List<AssetsNode> tree = this.queryUserAssetsTree(request);
        List<AssetsNode> list = AssetsTreeUtil.treeToList(tree);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        return list.stream()
                .filter(v -> Objects.equals(v.getIcon(), request.getIcon()))
                .collect(Collectors.toList());
    }

    @Override
    public AjaxResult<PageInfo<UserDataAssetResp>> listMyAsset(MyAssetRequest request) {
        Page<UserDataAssets> page = new Page<>(request.getPageNum(), request.getPageSize());
        request.setUserId(SecurityUtils.getUserId());
        final IPage<UserDataAssetResp> resp = userDataAssetsDAO.queryUserAsset(page, request);
        final List<UserDataAssetResp> collect = resp.getRecords()
                .stream()
                .peek(a -> a.setHasDataExpired(false))
                .collect(Collectors.toList());
        PageInfo<UserDataAssetResp> pageInfo = new PageInfo<>(collect, resp.getTotal(), Integer.parseInt(Long.toString(resp.getPages())), resp.getCurrent());
        return AjaxResult.success(pageInfo);
    }

    @Override
    public AjaxResult<List<String>> assetsTimeGra(AssetsDirRequest request) {
        AssetsDirRequest req = new AssetsDirRequest();
        req.setIcon(ApplicationConst.AssetsIcon.DATA);
        req.setExpireType(request.getExpireType());
        List<AssetsNode> data = this.queryUserAssets(req);
        List<String> names = data.stream()
                .filter(v -> v instanceof UserDataAssetsNode)
                .map(v -> (UserDataAssetsNode) v).map(UserDataAssetsNode::getTimeGra)
                .filter(StringUtils::isNoneBlank)
                .flatMap(v -> Stream.of(v.split(",")))
                .distinct().collect(Collectors.toList());
        return AjaxResult.success(names);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult mixExecuteForApply(Long applicationId) {
        // 特殊处理 文件类型
        Optional<TgApplicationInfo> infoOpt = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getConfigType, TgApplicationInfo::getApplicationNo)
                .eq(TgApplicationInfo::getId, applicationId)
                .oneOpt();
        if (!infoOpt.isPresent()) {
            return AjaxResult.error("提数申请不存在");
        }

        Integer configType = infoOpt.map(TgApplicationInfo::getConfigType).orElse(null);
        Optional<TgTemplateInfo> tempOpt = templateInfoDAO.lambdaQuery()
                .select(TgTemplateInfo::getTemplateType)
                .eq(TgTemplateInfo::getId, infoOpt.get().getTemplateId())
                .oneOpt();
        Boolean custom = tempOpt.map(TgTemplateInfo::getTemplateType)
                .map(v -> Objects.equals(TemplateTypeEnum.customized.name(), v)).orElse(false);
        if (Objects.isNull(configType) && custom) {
            return AjaxResult.error("请先配置 SQL/工作流/文件 后再执行");
        }

        if (ApplicationConfigTypeConstant.isFile(configType)) {
            return this.deliverApplyByFile(applicationId);
        } else {
            Boolean lock = redisTemplate.opsForValue().setIfAbsent(RedisKeys.Workflow.executeApplyLock(applicationId), 1, Duration.ofMinutes(1));
            if (BooleanUtils.isNotTrue(lock)) {
                return AjaxResult.error("请勿重复执行工作流");
            }

            return this.executeWorkFlow(applicationId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public AjaxResult deliverApplyByFile(Long applyId) {
        TgApplicationInfo info = new TgApplicationInfo().selectById(applyId);
        JsonBeanConverter.convert2Obj(info);
        if (Objects.isNull(info.getAssetsAttach())) {
            return AjaxResult.error("未配置文件");
        }
        applicationFormService.runApplication(applyId, info.getApplicationNo());

        UpsertAssetsBO bo = UpsertAssetsBO.builder().info(info)
                .file(true).snapshotType(AssetsSnapshotTypeEnum.manual_deliver.name()).build();
        this.upsertDataAssets(bo);

        applicationFormDAO.updateRunState(info.getApplicationNo(), ApplyRunStateEnum.wait_accept);

        applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                .set(TgApplicationInfo::getDataState, ApplyDataStateEnum.success.name())
                .eq(TgApplicationInfo::getId, info.getId())
        );

        this.handleBizMsgNotice(info);
        return AjaxResult.success();
    }

    @Override
    public AjaxResult executeWorkFlow(Long applicationId) {
        return this.executeWorkFlow(ExecFlowParam.builder()
                .workGroup(appProperties.getFlowWorkGroup())
                .taskName("手动执行")
                .applicationId(applicationId).build());
    }

    /**
     * 执行出数工作流
     *
     * @see UserDataAssetsService#dolphinCallBack 工作流完成后 更新状态 回调
     * @see com.sinohealth.web.controller.common.OpenApiController#syncCallback 工作流完成后 创建资产 回调
     */
    @Override
    public AjaxResult executeWorkFlow(ExecFlowParam param) {
        Long applicationId = param.getApplicationId();
        Long triggerId = param.getTriggerId();
        log.info("executeWorkFlow applicationId={}", applicationId);
        AjaxResult<ApplyFlowContext> statusRes = this.queryWorkflowStatusWithCtx(applicationId);
        if (!statusRes.isSuccess()) {
            alertService.sendDevNormalMsg(param.getTaskName() + ":" + statusRes.getMsg());
            return statusRes;
        }

        //查询正在运行中的实例
        ApplyFlowContext ctx = statusRes.getData();
        Integer runningCount = ctx.getStatusDTO().getRunningCount();
        if (runningCount != 0) {
            alertService.sendDevNormalMsg(param.getTaskName() + ": 该流程已经在运行中！");
            return AjaxResult.error("该流程已经在运行中！");
        }
        TgTemplateInfo template = ctx.getTemplate();
        TgApplicationInfo apply = ctx.getApply();
        ApplicationTaskConfig config = ctx.getConfig();
        Integer flowId = ctx.getFlowId();

        MultiValueMap<String, Object> postParameters = new LinkedMultiValueMap<>();
        postParameters.add("processDefinitionId", flowId);
        postParameters.add("failureStrategy", "CONTINUE");
        postParameters.add("taskDependType", null);
        postParameters.add("execType", "");

        postParameters.add("warningType", appProperties.getFinalHandleWarningType());
        postParameters.add("warningGroupId", appProperties.getFinalWarnGroupId());

        postParameters.add("runMode", "RUN_MODE_SERIAL");
        postParameters.add("processInstancePriority", "MEDIUM");
        postParameters.add("workerGroup", Optional.ofNullable(param.getWorkGroup()).orElse("default"));
        postParameters.add("timeout", DsConstants.MAX_TASK_TIMEOUT);
        postParameters.add("callbackUrl", Optional.ofNullable(appProperties.getDolphinCallHost())
                .map(v -> v + "/tg-easy-fetch/openapi/dolphin/callback")
                .orElse(""));
        //任务串行化执行
        postParameters.add("inSerialMode", true);

        // 工作流启动参数
        Map<String, Object> params = new HashMap<>();
        params.put("application_id", applicationId);
        params.put("ysg_host", Optional.ofNullable(appProperties.getDolphinCallHost()).orElse(""));
        Optional.ofNullable(triggerId).ifPresent(v -> params.put("trigger_id", v));
        postParameters.add("startParams", JSON.toJSONString(params));

        AjaxResult ajaxResult;
        if (DataAssetsUtil.useAutoProcess(template, apply)) {
            ajaxResult = intergrateAutoProcessDefService.execProcessInstance(postParameters);
        } else {
            ajaxResult = intergrateProcessDefService.execProcessInstance(postParameters);
        }

        if (ajaxResult.isDolphinSuccess()) {
            Object data = ajaxResult.getData();
            if (Objects.isNull(data)) {
                log.warn("未返回实例id: ajaxResult={}", ajaxResult);
            } else {
                String uid = data.toString();
                applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                        .eq(TgApplicationInfo::getId, applicationId)
                        .set(TgApplicationInfo::getFlowInstanceId, uid)
                        .set(TgApplicationInfo::getDataState, ApplyDataStateEnum.run.name())
                );

                applicationFormService.runApplication(applicationId, apply.getApplicationNo());
            }
        } else if (Objects.equals(ajaxResult.getCode(), 50004)) {
            // PROCESS_DEFINE_NOT_RELEASE 工作流下线情况
            AjaxResult processResult;
            if (DataAssetsUtil.useAutoProcess(template, apply)) {
                processResult = intergrateProcessDefService.queryProcessById(flowId);
            } else {
                processResult = intergrateAutoProcessDefService.queryProcessById(flowId);
            }

            if (processResult.isDolphinSuccess()) {
                Map<String, Object> resultMap = (Map<String, Object>) processResult.getData();
                config.setFlowName(Optional.ofNullable(resultMap.get("name")).map(Object::toString).orElse(null));
            }
            alertService.sendDevNormalMsg(param.getTaskName() + "当前工作流【" + config.getFlowName() + "】已下线无法执行，请到尚书台上线后执行");
            return AjaxResult.error("当前工作流【" + config.getFlowName() + "】已下线无法执行，请到尚书台上线后执行");
        }
        if (!ajaxResult.isDolphinSuccess()) {
            log.error("exe result={}", ajaxResult);
            alertService.sendDevNormalMsg(param.getTaskName() + ":" + ajaxResult.getMsg());
            return AjaxResult.error(ajaxResult.getMsg());
        }

        return AjaxResult.succeed();
    }

    /**
     * 更新工作流状态，前提是易数阁启动的实例
     *
     * @see UserDataAssetsServiceImpl#handleBizMsgNotice(TgApplicationInfo)
     */
    @Override
    public void dolphinCallBack(String instanceUid, Integer state) {
        List<TgApplicationInfo> list = applicationInfoMapper.selectList(new QueryWrapper<TgApplicationInfo>().lambda()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getApplicationNo)
                .eq(TgApplicationInfo::getFlowInstanceId, instanceUid)
                .last(" limit 2")
        );
        if (CollectionUtils.isEmpty(list)) {
            log.warn("no mapping: instanceUid={}", instanceUid);
            return;
        }
        if (CollectionUtils.size(list) > 1) {
            log.warn("repeat mapping: {} {}", instanceUid, list.stream().map(v -> v.getId().toString()).collect(Collectors.joining(",")));
        }

        Optional<TgApplicationInfo> applyOpt = Optional.ofNullable(list.get(0));
        Long applyId = applyOpt.map(TgApplicationInfo::getId).orElse(null);
        if (Objects.isNull(applyId)) {
            log.warn("ERROR: list={}", list);
            return;
        }

        TgApplicationInfo info = applyOpt.get();
        ApplyDataStateEnum status = ApplyDataStateEnum.ofFlowState(state);
        if (Objects.equals(status, ApplyDataStateEnum.fail)) {
            applicationFormDAO.updateRunState(info.getApplicationNo(), ApplyRunStateEnum.run_failed);

            final LambdaQueryWrapper<TgNoticeRead> wq = Wrappers.<TgNoticeRead>lambdaQuery()
                    .eq(TgNoticeRead::getApplicationId, applyId)
                    .in(TgNoticeRead::getAuditType, Lists.newArrayList(1, 4));
            final List<TgNoticeRead> tgNoticeReads = tgNoticeReadMapper.selectList(wq);

            Integer version = applyOpt.map(TgApplicationInfo::getAssetsId).map(userDataAssetsDAO::queryVersion).orElse(1);
            this.copyNewNotice(tgNoticeReads, version, AuditTypeEnum.FAILED);
        } else {
            applicationFormDAO.updateRunState(info.getApplicationNo(), ApplyRunStateEnum.wait_accept);
        }

        applicationInfoMapper.update(null, new UpdateWrapper<TgApplicationInfo>()
                .lambda().eq(TgApplicationInfo::getId, applyId)
                .set(TgApplicationInfo::getDataState, status.name())
        );
    }

    @Override
    public void copyNewNotice(List<TgNoticeRead> tgNoticeReads, Integer version, AuditTypeEnum type) {
        Set<Long> auditUser = new HashSet<>();
        int loopCount = 0;
        for (TgNoticeRead tgNoticeRead : tgNoticeReads) {
            // 过滤多个节点同个审核人只发一次推送
            if (auditUser.contains(tgNoticeRead.getAuditUserId())) {
                continue;
            } else {
                auditUser.add(tgNoticeRead.getAuditUserId());
                auditUser.add(tgNoticeRead.getUserId());
            }
            final TgNoticeRead noticeRead = new TgNoticeRead();
            BeanUtils.copyProperties(tgNoticeRead, noticeRead);

            // 同个申请单的申请人只有可能相同
            if (loopCount > 0 && auditUser.contains(tgNoticeRead.getUserId())) {
                noticeRead.setUserId(null);
            }

            noticeRead.setAuditType(type.getValue());
            noticeRead.setHasRead(0);
            noticeRead.setAuditUserHasRead(0);
            noticeRead.setCreateTime(new Date());
            noticeRead.setVersion(version);
            noticeRead.setId(null);
            tgNoticeReadMapper.insert(noticeRead);
            // ws
            wsMsgService.pushNoticeMsg(auditUser, noticeRead.getId());
            loopCount++;
        }
    }

    @Override
    public AjaxResult<ProcessDefStatusDTO> queryWorkflowStatus(Long applicationId) {
        // 特殊处理，规避文件类型
        Optional<TgApplicationInfo> infoOpt = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getConfigType)
                .eq(TgApplicationInfo::getId, applicationId)
                .oneOpt();
        if (!infoOpt.isPresent()) {
            return AjaxResult.error("提数申请不存在");
        }
        Integer configType = infoOpt.map(TgApplicationInfo::getConfigType).orElse(null);
//        if (Objects.isNull(configType)) {
//            return AjaxResult.error("请先配置 SQL/工作流/文件 后再执行");
//        }

        if (ApplicationConfigTypeConstant.isFile(configType)) {
            return AjaxResult.success(ProcessDefStatusDTO.NONE);
        }
        AjaxResult<ApplyFlowContext> result = this.queryWorkflowStatusWithCtx(applicationId);
        if (!result.isSuccess()) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success(result.getData().getStatusDTO());
    }

    private AjaxResult<ApplyFlowContext> queryWorkflowStatusWithCtx(Long applicationId) {
        TgApplicationInfo info = new TgApplicationInfo().selectById(applicationId);
        if (Objects.isNull(info)) {
            return AjaxResult.error("提数申请不存在");
        }
        Long templateId = info.getTemplateId();
        TgTemplateInfo tgTemplateInfo = new TgTemplateInfo().selectById(templateId);
        if (Objects.equals(tgTemplateInfo.getTemplateType(), TemplateTypeEnum.wide_table.name())) {
            return AjaxResult.error("宽表模式不支持该操作");
        }
        Integer id = DataAssetsUtil.getFinalSchedulerId(tgTemplateInfo, info);
        if (Objects.isNull(id)) {
            return AjaxResult.error("请先绑定工作流/SQL，谢谢！");
        }
        ApplicationTaskConfig config = applicationTaskConfigDAO.queryByApplicationId(info.getId());
        if (Objects.isNull(config)) {
            return AjaxResult.error("工作流参数生成失败");
        }

        ApplyFlowContext ctx = ApplyFlowContext.builder().flowId(id).template(tgTemplateInfo).apply(info).config(config).build();
        try {
            if (StringUtils.isBlank(info.getFlowInstanceId())) {
                ctx.setStatusDTO(ProcessDefStatusDTO.NONE);
                return AjaxResult.success(ctx);
            }
            AjaxResult result = intergrateProcessDefService.queryProcessInstanceStatusByUUID(info.getFlowInstanceId());
//            log.info("res={}", result);
            if (result.isDolphinSuccess()) {
                ArrayList instances = (ArrayList) result.getData();
                if (CollectionUtils.isEmpty(instances)) {
                    ctx.setStatusDTO(ProcessDefStatusDTO.NONE);
                    return AjaxResult.success(ctx);
                }

                // state , uid
                LinkedHashMap<String, Object> map = (LinkedHashMap) instances.get(0);

                String state = (String) map.get("state");
                ExecutionStatus status = ExecutionStatus.valueOf(state);
                if (ExecutionStatus.RUNNING.contains(status.getCode())) {
                    ctx.setStatusDTO(ProcessDefStatusDTO.ONE);
                } else {
                    ctx.setStatusDTO(ProcessDefStatusDTO.NONE);
                }
                return AjaxResult.success(ctx);
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return AjaxResult.error("查询流程状态异常！！");
    }

    @Override
    public boolean getNeedUpdate(Long assetId, String tableName) {
        List<ApplicationDataUpdateRecord> updateRecords = dataUpdateRecordDAO.queryCustomerByApplyIds(Collections.singletonList(assetId));
        // 数据为空表示没有同步过，无需做更新
        Map<Long, ApplicationDataUpdateRecord> latestSuccessMap = updateRecords.stream()
                .filter(v -> Objects.equals(v.getUpdateState(), UpdateRecordStateType.SUCCESS))
                .collect(Collectors.toMap(ApplicationDataUpdateRecord::getAssetsId, v -> v, (front, current) -> {
                    if (front.getCreateTime().before(current.getCreateTime())) {
                        return current;
                    }
                    return front;
                }));

        String tab = com.sinohealth.common.utils.StringUtils.replaceLast(tableName, "_local", "_shard");
        try {
            Date lastDate = ckProviderMapper.selectLastSuccessTime(tab);
            if (Objects.isNull(lastDate)) {
                return false;
            } else {
                return Optional.ofNullable(latestSuccessMap.get(assetId)).map(v -> v.getCreateTime()
                        .before(lastDate)).orElse(false);
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public AjaxResult<Void> manualDeprecated(Long assetsId, Long applyId) {
        Optional<UserDataAssets> assetsOpt = userDataAssetsDAO.lambdaQuery()
                .eq(UserDataAssets::getId, assetsId)
                .oneOpt();
        if (!assetsOpt.isPresent()) {
            return AjaxResult.error("资产不存在");
        }
        UserDataAssets assets = assetsOpt.get();
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(assets.getDataExpire())) {
            return AjaxResult.error("过期资产不可作废");
        }


        Set<Long> assetsIds = new HashSet<>();
        assetsIds.add(assetsId);

        Set<Long> cur = Sets.newHashSet(assetsId);
        while (CollectionUtils.isNotEmpty(cur)) {
            List<UserDataAssets> nextLayer = userDataAssetsDAO.lambdaQuery()
                    .select(UserDataAssets::getId)
                    .in(UserDataAssets::getCopyFromId, cur)
                    .list();
            cur = Lambda.buildSet(nextLayer);
            if (CollectionUtils.isNotEmpty(cur)) {
                assetsIds.addAll(cur);
            }
        }

        userDataAssetsDAO.lambdaUpdate()
                .set(UserDataAssets::getDeprecated, true)
                .set(UserDataAssets::getDataExpire, now)
                .in(UserDataAssets::getId, assetsIds)
                .update();
        userDataAssetsSnapshotDAO.lambdaUpdate()
                .set(UserDataAssets::getDeprecated, true)
                .set(UserDataAssets::getDataExpire, now)
                .in(UserDataAssetsSnapshot::getAssetsId, assetsIds)
                .update();

        applicationDAO.lambdaUpdate()
                .set(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.INVALID_APPLICATION)
                .eq(TgApplicationInfo::getId, Optional.ofNullable(applyId).orElse(assets.getSrcApplicationId()))
                .update();

        Optional<TgApplicationInfo> applyOpt = applicationDAO.lambdaQuery().select(TgApplicationInfo::getId, TgApplicationInfo::getApplicationNo)
                .eq(TgApplicationInfo::getId, applyId).oneOpt();
        applyOpt.map(TgApplicationInfo::getApplicationNo)
                .ifPresent(v -> applicationFormDAO.markRunState(v, ApplicationConst.AuditAction.DEPRECATED));

        applicationTaskConfigDAO.lambdaUpdate()
                .set(ApplicationTaskConfig::getActive, false)
                .eq(ApplicationTaskConfig::getApplicationId, Optional.ofNullable(applyId).orElse(assets.getSrcApplicationId()))
                .update();

        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> manualDeprecatedByApply(Long applyId) {
        Optional<TgApplicationInfo> applyOpt = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getAssetsId, TgApplicationInfo::getDataState)
                .eq(TgApplicationInfo::getId, applyId)
                .oneOpt();
        Long assetsId = applyOpt.map(TgApplicationInfo::getAssetsId).orElse(null);
        if (Objects.isNull(assetsId)) {
            return AjaxResult.error("申请无法找到对应资产");
        }
        Boolean inRun = applyOpt.map(v -> Objects.equals(v.getDataState(), ApplyDataStateEnum.run.name()))
                .orElse(false);
        if (inRun) {
            return AjaxResult.error("申请正在出数，无法作废资产");
        }

        return this.manualDeprecated(assetsId, applyId);
    }


    /**
     * 创建批次时的资产筛选
     */
    public List<FlowAssetsPageDTO> listForCreate(FlowAssetsPageRequest request) {
        List<UsableDataAssetsEntity> records = userDataAssetsMapper.pageQueryFlowAssets(request);
        return convertUsableEntity(records);
    }

    @Override
    public List<FlowAssetsPageDTO> listForCreate(FlowAssetsAutoPageRequest request) {
        List<UsableDataAssetsEntity> records = userDataAssetsMapper.pageQueryFlowAssetsForAuto(request);
        return convertUsableEntity(records);
    }

    private List<FlowAssetsPageDTO> convertUsableEntity(List<UsableDataAssetsEntity> records) {
        Set<Long> tempIds = Lambda.buildSet(records, UsableDataAssetsEntity::getTemplateId);
        Set<Long> applyIds = Lambda.buildSet(records, UsableDataAssetsEntity::getSrcApplicationId);
        Map<Long, String> tempNameMap = templateInfoDAO.queryNameMap(tempIds);

        Map<Long, TgApplicationInfo> applyMap = Lambda.queryMapIfExist(applyIds,
                v -> applicationDAO.lambdaQuery().select(TgApplicationInfo::getId, TgApplicationInfo::getTemplateId,
                                TgApplicationInfo::getApplicationNo, TgApplicationInfo::getCreateTime)
                        .in(TgApplicationInfo::getId, applyIds).list(),
                TgApplicationInfo::getId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return records.stream().map(v -> {
            Optional<TgApplicationInfo> applyOpt = Optional.ofNullable(applyMap.get(v.getSrcApplicationId()));
            TgApplicationInfo apply = applyOpt.orElseThrow(() -> new CustomException("资产的申请不存在：" + v.getId()));
            return FlowAssetsPageDTO.builder()
                    .applyId(apply.getId())
                    .templateId(apply.getTemplateId())
                    .assetsId(v.getId())
                    .applicationNo(apply.getApplicationNo())
                    .projectName(v.getProjectName())
                    .newProjectName(v.getNewProjectName())
                    .templateName(tempNameMap.get(v.getTemplateId()))
                    .applicant(v.getApplicantName())
                    .applyTime(formatter.format(v.getCreateTime()))
                    .requireTimeType(v.getRequireTimeType())
                    .deliverTimeType(v.getDeliverTimeType())
                    .build();
        }).collect(Collectors.toList());
    }
}
