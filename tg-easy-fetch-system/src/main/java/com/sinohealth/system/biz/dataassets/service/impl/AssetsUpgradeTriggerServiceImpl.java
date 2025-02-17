package com.sinohealth.system.biz.dataassets.service.impl;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sinohealth.common.annotation.RegisterCronMethod;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.IdTable;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsCompareTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsExpireEnum;
import com.sinohealth.common.enums.dataassets.AssetsSnapshotTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.enums.process.FlowProcessStateEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.system.biz.alert.dto.AssetsAlertMsg;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.service.ApplicationFormService;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.ck.constant.CkTableSuffixTable;
import com.sinohealth.system.biz.common.RedisLock;
import com.sinohealth.system.biz.common.RedisSemaphore;
import com.sinohealth.system.biz.dataassets.dao.AssetsCompareDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowBatchDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowBatchDetailDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsWideUpgradeTriggerDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsCompare;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatch;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatchDetail;
import com.sinohealth.system.biz.dataassets.domain.AssetsWideUpgradeTrigger;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;
import com.sinohealth.system.biz.dataassets.dto.UpsertAssetsBO;
import com.sinohealth.system.biz.dataassets.dto.bo.ExecFlowParam;
import com.sinohealth.system.biz.dataassets.helper.UserDataAssetsUploadFtpHelper;
import com.sinohealth.system.biz.dataassets.service.AssetsQcService;
import com.sinohealth.system.biz.dataassets.service.AssetsUpgradeTriggerService;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.biz.process.dao.TgFlowProcessManagementDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessManagement;
import com.sinohealth.system.biz.process.facade.TgFlowProcessAlertFacade;
import com.sinohealth.system.biz.project.dao.ProjectDAO;
import com.sinohealth.system.biz.table.constants.TablePushStatusEnum;
import com.sinohealth.system.biz.table.dao.TableInfoSnapshotDAO;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import com.sinohealth.system.biz.table.facade.TableInfoSnapshotCompareFacade;
import com.sinohealth.system.biz.table.service.impl.TableInfoSnapshotServiceImpl;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.service.IntergrateProcessDefService;
import com.sinohealth.system.service.SyncHelper;
import com.sinohealth.system.util.ApplicationSqlUtil;
import com.sinohealth.system.util.FtpClient;
import com.sinohealth.system.util.FtpClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-11 13:42
 */
@Slf4j
@Service("assetsUpgradeTriggerService")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AssetsUpgradeTriggerServiceImpl implements AssetsUpgradeTriggerService {

    private static final int maxCon = 4;
    private final ApplicationDAO applicationDAO;
    private final TemplateInfoDAO templateInfoDAO;
    private final TgCkProviderMapper tgCkProviderMapper;
    private final ProjectDAO projectDAO;
    private final AssetsWideUpgradeTriggerDAO assetsWideUpgradeTriggerDAO;
    private final UserDataAssetsDAO userDataAssetsDAO;
    private final UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;
    private final AssetsCompareDAO assetsCompareDAO;
    private final TableInfoSnapshotDAO tableInfoSnapshotDAO;
    private final AssetsFlowBatchDAO assetsFlowBatchDAO;
    private final AssetsFlowBatchDetailDAO assetsFlowBatchDetailDAO;
    private final TgFlowProcessManagementDAO flowProcessManagementDAO;


    private final ApplicationFormService applicationFormService;
    private final IntergrateProcessDefService intergrateProcessDefService;
    private final UserDataAssetsService userDataAssetsService;
    private final AlertService alertService;
    private final AssetsQcService assetsQcService;
    private final TgFlowProcessAlertFacade tgFlowProcessAlertFacade;
    private final AppProperties appProperties;
    private final RedisTemplate redisTemplate;
    private final RedisLock redisLock;
    private final RedisSemaphore redisSemaphore;
    private final SyncHelper syncHelper;
    private final UserDataAssetsUploadFtpHelper userDataAssetsUploadFtpHelper;
    private final CKClusterAdapter ckClusterAdapter;
    @Resource
    @Qualifier(ThreadPoolType.ENHANCED_TTL)
    private Executor pool;
    //    @Resource
//    @Qualifier(ThreadPoolType.POST_MSG)
//    private ThreadPoolTaskExecutor pool;
    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER)
    private ScheduledExecutorService scheduler;

    /**
     * 标记MySQL中过期的数据
     *
     * @param month 近几个月
     * @see DateUtils#convertExpire(LocalDateTime)
     */
    public void markDeleteAssets(Integer month) {
        this.markAssets(month);
        this.markAssetsSnapshot(month);
    }

    private void markAssets(Integer month) {
        LocalDateTime flagTime = LocalDateTime.now().minusMonths(Optional.ofNullable(month).filter(v -> v > 0).orElse(3));
        List<UserDataAssets> expireAssets = userDataAssetsDAO.lambdaQuery()
                .select(UserDataAssets::getId, UserDataAssets::getAssetTableName)
                .and(v -> v.notIn(UserDataAssets::getExpireType, AssetsExpireEnum.DELETE_TAGS)
                        .or().isNull(UserDataAssets::getExpireType))
                .lt(UserDataAssets::getDataExpire, flagTime).list();

        if (CollectionUtils.isEmpty(expireAssets)) {
            return;
        }
        List<Long> assetsIds = Lambda.buildList(expireAssets, UserDataAssets::getId);
        userDataAssetsDAO.lambdaUpdate()
                .in(UserDataAssets::getId, assetsIds)
                .set(UserDataAssets::getExpireType, AssetsExpireEnum.delete.name())
                .update();
        log.info("mark delete: ids={}", assetsIds);
    }

    private void markAssetsSnapshot(Integer month) {
        LocalDateTime flagTime = LocalDateTime.now().minusMonths(Optional.ofNullable(month).filter(v -> v > 0).orElse(3));
        List<UserDataAssetsSnapshot> expireAssetsSnapShots = userDataAssetsSnapshotDAO.lambdaQuery()
                .select(UserDataAssetsSnapshot::getId, UserDataAssetsSnapshot::getAssetTableName)
                .and(v -> v.notIn(UserDataAssetsSnapshot::getExpireType, AssetsExpireEnum.DELETE_TAGS)
                        .or().isNull(UserDataAssetsSnapshot::getExpireType))
                .lt(UserDataAssetsSnapshot::getDataExpire, flagTime)
                .list();

        if (CollectionUtils.isEmpty(expireAssetsSnapShots)) {
            return;
        }
        List<Long> snapshotIds = Lambda.buildList(expireAssetsSnapShots, UserDataAssetsSnapshot::getId);
        userDataAssetsSnapshotDAO.lambdaUpdate()
                .in(UserDataAssetsSnapshot::getId, snapshotIds)
                .set(UserDataAssetsSnapshot::getExpireType, AssetsExpireEnum.delete.name())
                .update();
        log.info("mark snapshot delete: ids={}", snapshotIds);
    }

    /**
     * 定时执行
     */
    @RegisterCronMethod
    public void scheduleCleanCkTable() {
        this.deleteCkTable();
    }

    /**
     * 删除被标记为delete状态的资产表
     */
    @Override
    public void deleteCkTable() {
        // 检查并标记 需要删除的资产
        this.markDeleteAssets(3);

        Map<String, Integer> tableTypeMap = new HashMap<>();
        Map<String, Long> tableIdMap = new HashMap<>();
        try {
            Set<String> shardTables = new HashSet<>();
            Set<String> ftpFiles = new HashSet<>();

            // assets
            List<UserDataAssets> expireAssets = userDataAssetsDAO.lambdaQuery()
                    .select(UserDataAssets::getId, UserDataAssets::getAssetTableName, UserDataAssets::getFtpPath)
                    .eq(UserDataAssets::getExpireType, AssetsExpireEnum.delete.name())
                    .list();
            List<String> tableList = Lambda.buildListPost(expireAssets, UserDataAssets::getAssetTableName, StringUtils::isNoneBlank);
            shardTables.addAll(tableList);
            expireAssets.forEach(v -> tableIdMap.put(v.getAssetTableName(), v.getId()));

            List<String> files = Lambda.buildListPost(expireAssets, UserDataAssets::getFtpPath, StringUtils::isNotBlank);
            ftpFiles.addAll(files);
            tableList.forEach(v -> tableTypeMap.put(v, 1));

            // snapshot
            List<UserDataAssetsSnapshot> snapshots = userDataAssetsSnapshotDAO.getBaseMapper()
                    .selectList(new QueryWrapper<UserDataAssetsSnapshot>().lambda()
                            .eq(UserDataAssetsSnapshot::getExpireType, AssetsExpireEnum.delete.name()));
            snapshots.forEach(v -> tableIdMap.put(v.getAssetTableName(), v.getId()));
            List<String> tables = Lambda.buildListPost(snapshots, UserDataAssetsSnapshot::getAssetTableName, StringUtils::isNoneBlank);
            shardTables.addAll(tables);

            List<String> snapFiles = Lambda.buildListPost(snapshots, UserDataAssetsSnapshot::getFtpPath, StringUtils::isNotBlank);
            ftpFiles.addAll(snapFiles);
            tables.forEach(v -> tableTypeMap.put(v, 2));


            if (CollectionUtils.isNotEmpty(ftpFiles)) {
                try (FtpClient ftpClient = FtpClientFactory.getInstance()) {
                    ftpClient.open();
                    for (String file : ftpFiles) {
                        ftpClient.delete(file);
                    }
                } catch (Exception e) {
                    log.error("删除FTP文件失败:", e);
                }
            }

            if (CollectionUtils.isNotEmpty(shardTables)) {
                for (String tableName : shardTables) {
                    ckClusterAdapter.deleteAssetsTable(tableName);

                    Integer type = tableTypeMap.get(tableName);
                    Long bizId = tableIdMap.get(tableName);
                    if (Objects.equals(type, 1)) {
                        userDataAssetsDAO.update(null, new UpdateWrapper<UserDataAssets>().lambda()
                                .eq(UserDataAssets::getId, bizId)
                                .set(UserDataAssets::getExpireType, AssetsExpireEnum.delete_data.name())
                        );
                    } else {
                        userDataAssetsSnapshotDAO.update(null, new UpdateWrapper<UserDataAssetsSnapshot>().lambda()
                                .eq(UserDataAssetsSnapshot::getId, bizId)
                                .set(UserDataAssetsSnapshot::getExpireType, AssetsExpireEnum.delete_data.name())
                        );
                    }

                    TimeUnit.MILLISECONDS.sleep(new Random(System.currentTimeMillis()).nextInt(300) + 300);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 2. 消费任务 宽表资产自动升级
     *
     * @see TableInfoSnapshotServiceImpl#pushTable 创建任务
     */
    // Debug
//    @Scheduled(cron = "0 0/1 * * * ?")
    @RegisterCronMethod
    @Transactional(rollbackFor = Exception.class)
    public void scheduleWideTable() {
        // 确定有没有锁
        synchronized (TableInfoSnapshotCompareFacade.class) {
            if (TableInfoSnapshotCompareFacade.run) {
                log.warn("ignore trigger via table compare");
                return;
            }
        }

        redisLock.wrapperLock(RedisKeys.Assets.UPGRADE_WIDE_LOCK, () -> {
            // 限制并发数量
            Object con = redisTemplate.opsForValue().get(RedisKeys.Assets.UPGRADE_WIDE_TASK_CON);
            Integer finalMaxCon = Optional.ofNullable(con).map(Object::toString).map(Integer::parseInt).orElse(maxCon);

            Executor inner = TtlExecutors.unwrap(pool);
            ThreadPoolTaskExecutor ttlPool = (ThreadPoolTaskExecutor) inner;

            // 值为7 集群数为2 调度为每分钟时 每分钟处理 5*2*7 一小时处理
            int batch = finalMaxCon * 10;
            int queueSize = ttlPool.getThreadPoolExecutor().getQueue().size();
            if (queueSize >= batch) {
                log.warn("not over count: sumTask={} queueSize={} con={} batch={}",
                        ttlPool.getThreadPoolExecutor().getTaskCount(), queueSize, finalMaxCon, batch);
                int waitCount = assetsWideUpgradeTriggerDAO.count(new QueryWrapper<AssetsWideUpgradeTrigger>().lambda()
                        .eq(AssetsWideUpgradeTrigger::getState, AssetsUpgradeStateEnum.wait.name())
                );
                if (waitCount > 300) {
                    alertService.sendDevNormalMsg("【宽表定时更新】 等待任务数：" + waitCount);
                }
                return;
            }

            // 查询需要执行的任务
            List<AssetsWideUpgradeTrigger> list = assetsWideUpgradeTriggerDAO.getBaseMapper()
                    .selectList(new QueryWrapper<AssetsWideUpgradeTrigger>().lambda()
                            .eq(AssetsWideUpgradeTrigger::getState, AssetsUpgradeStateEnum.wait.name())
                            .orderByAsc(AssetsWideUpgradeTrigger::getCreateTime)
                            .last(" limit " + batch)
                    );
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            // 注意该断言： 3min内能处理完一个批次的宽表任务
            redisTemplate.opsForValue().set(RedisKeys.Assets.UPGRADE_WIDE_TASK_FLAG, 1, Duration.ofMinutes(3));

            Set<Long> ids = Lambda.buildSet(list);
            log.info("id list:{}", ids);
            assetsWideUpgradeTriggerDAO.update(null, new UpdateWrapper<AssetsWideUpgradeTrigger>().lambda()
                    .in(AssetsWideUpgradeTrigger::getId, ids)
                    .set(AssetsWideUpgradeTrigger::getState, AssetsUpgradeStateEnum.running.name())
                    .set(AssetsWideUpgradeTrigger::getStartTime, LocalDateTime.now())
            );

            Set<Long> applyIds = Lambda.buildSet(list, AssetsWideUpgradeTrigger::getApplyId, Objects::nonNull);

            List<TgApplicationInfo> appList = applicationDAO.lambdaQuery()
                    .select(TgApplicationInfo::getId, TgApplicationInfo::getApplicationNo)
                    .in(TgApplicationInfo::getId, applyIds)
                    .list();

            for (TgApplicationInfo info : appList) {
                applicationFormService.runApplication(info.getId(), info.getApplicationNo());
            }

            List<Long> assetsIds = Lambda.buildList(list, AssetsWideUpgradeTrigger::getAssetsId);
            Map<Long, Long> idMap = userDataAssetsDAO.queryAssetsApply(assetsIds);
            Map<Long, Integer> versionMap = userDataAssetsDAO.queryVersion(assetsIds);

            // 数据对比开启的数据
            List<UserDataAssets> tempList = userDataAssetsDAO.lambdaQuery()
                    .select(UserDataAssets::getId, UserDataAssets::getTemplateId)
                    .in(UserDataAssets::getId, assetsIds)
                    .list();
            Map<Long, Long> assetsTempMap = Lambda.buildMap(tempList, UserDataAssets::getId, UserDataAssets::getTemplateId);
            List<TgTemplateInfo> needList = templateInfoDAO.lambdaQuery()
                    .select(TgTemplateInfo::getId)
                    .in(TgTemplateInfo::getId, assetsTempMap.values())
                    .eq(TgTemplateInfo::getAssetsCompare, true)
                    .list();
            Set<Long> needIds = Lambda.buildSet(needList);

            for (AssetsWideUpgradeTrigger trigger : list) {
                pool.execute(() -> {
                    Long applyId = idMap.get(trigger.getAssetsId());
                    if (Objects.isNull(applyId)) {
                        log.error("no apply: assetsId={}", trigger.getAssetsId());
                    }
                    String tableName = ApplicationSqlUtil.buildSnapshotWideTableName(trigger.getAssetsId());

//                    boolean createResult = syncHelper.syncCreateSnapshotTable(applyId, tableName);
                    boolean createResult = syncHelper.createLocalSnapshotTable(applyId, tableName);
                    if (createResult) {
                        assetsWideUpgradeTriggerDAO.update(null, new UpdateWrapper<AssetsWideUpgradeTrigger>().lambda()
                                .eq(AssetsWideUpgradeTrigger::getId, trigger.getId())
                                .set(AssetsWideUpgradeTrigger::getState, AssetsUpgradeStateEnum.success.name())
                                .set(AssetsWideUpgradeTrigger::getFinishTime, LocalDateTime.now())
                        );

                        Optional<TgApplicationInfo> applyOpt = applicationDAO.lambdaQuery()
                                .select(TgApplicationInfo::getApplicationNo)
                                .eq(TgApplicationInfo::getId, applyId).oneOpt();
                        String no = applyOpt.map(TgApplicationInfo::getApplicationNo).orElse(null);
                        applicationFormService.updateRunState(no, ApplyRunStateEnum.wait_accept);

                        String snapshotType;
                        String markApplyKey = RedisKeys.Assets.getMarkApplyKey(applyId);
                        boolean applyTrigger = BooleanUtils.isTrue(redisTemplate.hasKey(markApplyKey));
                        log.info("applyId={} mark={}", applyId, applyTrigger);
                        if (applyTrigger) {
                            snapshotType = AssetsSnapshotTypeEnum.apply_deliver.name();
                            redisTemplate.delete(markApplyKey);
                        } else {
                            snapshotType = AssetsSnapshotTypeEnum.sync_deliver.name();
                        }

                        UpsertAssetsBO bo = UpsertAssetsBO.builder()
                                .tableName(tableName)
                                .snapshotType(snapshotType)
                                .build();

                        userDataAssetsService.replaceAssets(bo, trigger.getAssetsId(), null);

                        // 资产未更新时的数据 要加一
                        Integer lastVersion = Optional.ofNullable(versionMap.get(trigger.getAssetsId())).map(v -> v + 1).orElse(1);

                        if (needIds.contains(assetsTempMap.get(trigger.getAssetsId()))) {
                            // 创建资产对比任务
                            this.createAssetsCompare(trigger, lastVersion);
                        }

                        // 同步数据资产数据到ftp服务器
//                        userDataAssetsUploadFtpHelper.uploadFtp(trigger.getAssetsId());
                        userDataAssetsUploadFtpHelper.addFtpTask(trigger.getAssetsId());
                    } else {
                        log.warn("failed: id={}", trigger.getId());
                        assetsWideUpgradeTriggerDAO.update(null, new UpdateWrapper<AssetsWideUpgradeTrigger>().lambda()
                                .eq(AssetsWideUpgradeTrigger::getId, trigger.getId())
                                .set(AssetsWideUpgradeTrigger::getState, AssetsUpgradeStateEnum.failed.name())
                                .set(AssetsWideUpgradeTrigger::getFinishTime, LocalDateTime.now())
                        );

                        AssetsAlertMsg msg = AssetsAlertMsg.builder()
                                .assetsId(trigger.getAssetsId()).tableVersion(trigger.getActVersion()).build();
                        alertService.sendAssetsAlert(msg);
                    }

                    this.updateTableSnapshotState(trigger);
                    if (this.allWideTriggerFinish(trigger.getQcBatchId())) {
                        assetsQcService.finishWideUpgrade(trigger.getQcBatchId());
                    }
                });
            }
        });
    }

    /**
     * 申请人手动触发 资产更新
     * <p>
     * 宽表 基于SQL重新出数
     *
     * @see com.sinohealth.web.controller.common.OpenApiController#syncCallback 工作流 重新跑
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> manualUpgrade(Long assetsId) {
        Optional<UserDataAssets> assetsOpt = Lambda.queryOneIfExist(assetsId, userDataAssetsDAO::getById);
        if (!assetsOpt.isPresent()) {
            return AjaxResult.error("资产不存在");
        }

        UserDataAssets assets = assetsOpt.get();
        LocalDateTime now = LocalDateTime.now();
        if (Objects.equals(assets.getTemplateType(), TemplateTypeEnum.wide_table.name())) {
            TableInfoSnapshot table = tableInfoSnapshotDAO.getLatest(assets.getBaseTableId());
            if (Objects.equals(table.getVersion(), assets.getBaseVersion())) {
                return AjaxResult.error("当前数据已是最新版本，无需更新");
            }

            Integer existHandle = assetsWideUpgradeTriggerDAO.lambdaQuery()
                    .eq(AssetsWideUpgradeTrigger::getAssetsId, assetsId)
                    .eq(AssetsWideUpgradeTrigger::getActVersion, table.getVersion())
                    .count();
            if (Objects.nonNull(existHandle) && existHandle > 0) {
                return AjaxResult.error("正在更新数据，请勿重复操作");
            }

            redisTemplate.opsForValue().setIfAbsent(RedisKeys.Assets.getMarkApplyKey(assets.getSrcApplicationId()),
                    0, Duration.ofHours(4));

            AssetsWideUpgradeTrigger trigger = new AssetsWideUpgradeTrigger();
            trigger.setAssetsId(assets.getId());
            trigger.setApplyId(assets.getSrcApplicationId());
            trigger.setActVersion(table.getVersion());
            trigger.setTableId(assets.getBaseTableId());
            trigger.setState(AssetsUpgradeStateEnum.wait.name());
            trigger.setCreateTime(now);
            trigger.setUpdateTime(now);
            assetsWideUpgradeTriggerDAO.save(trigger);

            // 立即触发调度
            this.scheduleWideTable();
        } else {
            final Long applyId = assets.getSrcApplicationId();
            Boolean inRun = applicationDAO.lambdaQuery()
                    .select(TgApplicationInfo::getDataState)
                    .eq(TgApplicationInfo::getId, applyId)
                    .oneOpt().map(v -> Objects.equals(v.getDataState(), ApplyDataStateEnum.run.name())).orElse(false);
            if (inRun) {
                return AjaxResult.error("正在更新数据，请勿重复操作");
            }

            redisTemplate.opsForValue().setIfAbsent(RedisKeys.Assets.getMarkApplyKey(assets.getSrcApplicationId()),
                    0, Duration.ofHours(4));

            // 立即执行工作流
            ExecFlowParam param = ExecFlowParam.builder().applicationId(applyId)
                    .triggerId(assets.getFlowDetailId())
                    .workGroup(appProperties.getFlowWorkGroup())
                    .taskName("手动更新")
                    .build();
            userDataAssetsService.executeWorkFlow(param);
        }

        return AjaxResult.succeed();
    }


    private boolean allFlowTriggerFinish(Long qcBatchId) {
        if (Objects.isNull(qcBatchId)) {
            return false;
        }

        Integer actCount = assetsFlowBatchDAO.lambdaQuery()
                .eq(AssetsFlowBatch::getQcBatchId, qcBatchId)
                .eq(AssetsFlowBatch::getNeedQc, true)
                .eq(AssetsFlowBatch::getState, AssetsUpgradeStateEnum.running.name())
                .count();
        boolean running = Objects.nonNull(actCount) && actCount > 0;
        if (running) {
            return false;
        }
//        Integer runCnt = assetsUpgradeTriggerDAO.countNeedTrigger(qcBatchId);
//        if (Objects.nonNull(runCnt) && runCnt > 1) {
//            return false;
//        }

        return true;
    }

    private boolean allWideTriggerFinish(Long batchId) {
        if (Objects.isNull(batchId)) {
            return false;
        }
        Integer actCount = assetsWideUpgradeTriggerDAO.lambdaQuery()
                .in(AssetsWideUpgradeTrigger::getState, AssetsUpgradeStateEnum.actions)
                .eq(AssetsWideUpgradeTrigger::getQcBatchId, batchId)
                .count();
        return Objects.nonNull(actCount) && actCount == 0;
    }

    private void updateTableSnapshotState(AssetsWideUpgradeTrigger trigger) {
        TableInfoSnapshot table = tableInfoSnapshotDAO.lambdaQuery()
                .eq(TableInfoSnapshot::getTableId, trigger.getTableId())
                .eq(TableInfoSnapshot::getVersion, trigger.getActVersion())
                .eq(TableInfoSnapshot::getPushStatus, TablePushStatusEnum.run.name())
                .last(" limit 1 ")
                .one();
        if (Objects.isNull(table)) {
            return;
        }

//        log.info("running {}", trigger.getTableId());
        Integer actCount = assetsWideUpgradeTriggerDAO.lambdaQuery()
                .eq(AssetsWideUpgradeTrigger::getTableId, table.getTableId())
                .eq(AssetsWideUpgradeTrigger::getActVersion, table.getVersion())
                .in(AssetsWideUpgradeTrigger::getState, AssetsUpgradeStateEnum.actions)
                .count();
        log.info("tableId={} actCount={}", trigger.getTableId(), actCount);
        if (actCount == 0) {
            Integer failCount = assetsWideUpgradeTriggerDAO.lambdaQuery()
                    .eq(AssetsWideUpgradeTrigger::getTableId, table.getTableId())
                    .eq(AssetsWideUpgradeTrigger::getActVersion, table.getVersion())
                    .eq(AssetsWideUpgradeTrigger::getState, AssetsUpgradeStateEnum.failed.name())
                    .count();
            if (failCount > 0) {
                table.setPushStatus(TablePushStatusEnum.failed.name());
            } else {
                table.setPushStatus(TablePushStatusEnum.success.name());
            }

            log.info("tableId={} failCount={}", trigger.getTableId(), failCount);

            // 加条件尽量规避重复更新情况
            boolean update = tableInfoSnapshotDAO.lambdaUpdate()
                    .eq(TableInfoSnapshot::getId, table.getId())
                    .set(TableInfoSnapshot::getPushStatus, table.getPushStatus())
                    .eq(TableInfoSnapshot::getPushStatus, TablePushStatusEnum.run.name())
                    .update();

            if (update) {
                // 发送告警
                tgFlowProcessAlertFacade.sendAssetsUpGradeAlert(tableInfoSnapshotDAO.getById(table.getId()));
            }
        }
    }

    /**
     * @param trigger
     * @param lastVersion 资产最新版本
     */
    public void createAssetsCompare(AssetsWideUpgradeTrigger trigger, Integer lastVersion) {
        Integer preVersion = trigger.getPreVersion();
        if (Objects.isNull(preVersion)) {
            return;
        }
        if (BooleanUtils.isNotTrue(trigger.getNeedCompare())) {
            return;
        }

        List<TableInfoSnapshot> snapTablePair = tableInfoSnapshotDAO.getBaseMapper()
                .selectList(new QueryWrapper<TableInfoSnapshot>().lambda()
                        .eq(TableInfoSnapshot::getTableId, trigger.getTableId())
                        .in(TableInfoSnapshot::getVersion, Arrays.asList(trigger.getActVersion(), preVersion))
                );
        Map<Integer, TableInfoSnapshot> verMap = Lambda.buildMap(snapTablePair, TableInfoSnapshot::getVersion);
        TableInfoSnapshot preInfo = verMap.get(preVersion);
        // 历史版本未推送数据，不创建比对, 兼容部分资产失败的情况
        if (!TablePushStatusEnum.END.contains(preInfo.getPushStatus())) {
            return;
        }

        Long assetsId = trigger.getAssetsId();
        UserDataAssetsSnapshot preSnapshot = userDataAssetsSnapshotDAO.lambdaQuery()
                .select(UserDataAssetsSnapshot::getId, UserDataAssetsSnapshot::getVersion)
                .eq(UserDataAssetsSnapshot::getAssetsId, assetsId)
                .eq(UserDataAssetsSnapshot::getBaseVersion, preVersion)
                .orderByDesc(UserDataAssetsSnapshot::getVersion)
                .last(" limit 1")
                .one();
        if (Objects.isNull(preSnapshot)) {
            log.warn("no {} version assets: id={}", preVersion, assetsId);
            return;
        }

        if (CollectionUtils.isNotEmpty(snapTablePair)) {
            snapTablePair.stream().filter(v -> Objects.isNull(v.getVersionPeriod())).forEach(v -> v.setVersionPeriod(""));
        }
        Map<Integer, String> periodMap = Lambda.buildMap(snapTablePair, TableInfoSnapshot::getVersion, TableInfoSnapshot::getVersionPeriod);


        AssetsCompare compare = new AssetsCompare()
                .setBizId(trigger.getBizId())
                .setBaseTableId(trigger.getTableId())
                .setAssetsId(assetsId)
                .setCurVersion(lastVersion)
                .setPreVersion(preSnapshot.getVersion())
                .setCreateType(AssetsCompareTypeEnum.auto.name())
                .setState(AssetsUpgradeStateEnum.wait.name())
                .setCreator(0L)
                .setUpdater(0L);

        compare.setCurVersionPeriod(AssetsCompare.buildVersionPeriod(lastVersion, periodMap.get(trigger.getActVersion())));
        compare.setPreVersionPeriod(AssetsCompare.buildVersionPeriod(preSnapshot.getVersion(), periodMap.get(preVersion)));

        // 只保留4个历史对比，加上当前版本，一共5份
        List<AssetsCompare> keepIds = assetsCompareDAO.lambdaQuery()
                .select(AssetsCompare::getId)
                .eq(AssetsCompare::getAssetsId, assetsId)
                .orderByDesc(AssetsCompare::getId)
                .last(" limit 4")
                .list();
        Set<Long> ids = Lambda.buildSet(keepIds, AssetsCompare::getId);
        if (CollectionUtils.isNotEmpty(ids)) {
            assetsCompareDAO.lambdaUpdate().set(AssetsCompare::getDeleted, true)
                    .eq(AssetsCompare::getAssetsId, assetsId)
                    .notIn(AssetsCompare::getId, ids)
                    .update();
        }

        assetsCompareDAO.save(compare);
    }

    /**
     * 工作流资产自动升级
     * <p>
     * 定时检查配置表，触发工作流执行
     *
     * @see UserDataAssetsServiceImpl#dolphinCallBack(String, Integer)
     */
    @RegisterCronMethod
    @Scheduled(cron = "0 * * * * ?")
    @Override
    public void schedulerRunFlow() {
        redisLock.wrapperLock(RedisKeys.Workflow.UPGRADE_FLOW_LOCK, Duration.ofHours(2), this::runFlow);
    }

    private void runFlow() {
        List<AssetsFlowBatch> batchList = assetsFlowBatchDAO.lambdaQuery()
                .le(AssetsFlowBatch::getExpectTime, LocalDateTime.now())
                .in(AssetsFlowBatch::getState, AssetsUpgradeStateEnum.actions)
                .list();
        if (CollectionUtils.isEmpty(batchList)) {
            return;
        }

        Set<Long> batchIds = Lambda.buildSet(batchList);
        log.info("RUN batchIds={}", batchIds);
        Set<Long> updateIds = batchList.stream()
                .filter(v -> Objects.equals(v.getState(), AssetsUpgradeStateEnum.wait.name()))
                .map(IdTable::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(updateIds)) {
            assetsFlowBatchDAO.updateState(updateIds, AssetsUpgradeStateEnum.running);

            // 更新 全流程状态
            Set<Long> bizIds = batchList.stream()
                    .filter(v -> Objects.equals(v.getState(), AssetsUpgradeStateEnum.wait.name()))
                    .map(AssetsFlowBatch::getBizId).filter(Objects::nonNull).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(bizIds)) {
                flowProcessManagementDAO.lambdaUpdate()
                        .set(TgFlowProcessManagement::getWorkFlowState, FlowProcessStateEnum.RUNNING.getCode())
                        .in(TgFlowProcessManagement::getId, bizIds)
                        .eq(TgFlowProcessManagement::getWorkFlowState, FlowProcessStateEnum.WAIT.getCode())
                        .update();
            }
        }

        // 修正批次的状态
        Integer batchSize = Optional.ofNullable(redisTemplate.opsForValue()
                        .get(RedisKeys.Workflow.UPGRADE_FLOW_BATCH))
                .map(Object::toString).map(Integer::parseInt).orElse(20);
        List<AssetsFlowBatchDetail> waitDetails = assetsFlowBatchDetailDAO.lambdaQuery()
                .in(AssetsFlowBatchDetail::getBatchId, batchIds)
                .eq(AssetsFlowBatchDetail::getState, AssetsUpgradeStateEnum.wait.name())
                .last(" limit " + batchSize)
                .list();
        if (CollectionUtils.isEmpty(waitDetails)) {
            for (AssetsFlowBatch batch : batchList) {
                this.postHandleWhenFinishDetail(batch.getId(), batch.getQcBatchId(), batch.getName());
            }
            return;
        }

        Set<Long> ids = Lambda.buildSet(waitDetails);
        assetsFlowBatchDetailDAO.updateState(ids, AssetsUpgradeStateEnum.running);

        Integer conc = Optional.ofNullable(redisTemplate.opsForValue().get(RedisKeys.Workflow.UPGRADE_FLOW_CON))
                .map(Object::toString).map(Integer::parseInt).orElse(5);
        AtomicInteger count = new AtomicInteger();
        log.info("handle count={}", waitDetails.size());
        for (AssetsFlowBatchDetail detail : waitDetails) {
            try {
                redisSemaphore.acquireBlock(RedisKeys.Workflow.CREATE_ASSETS, conc);
                final Long applyId = detail.getApplicationId();

                // 执行前发现作废 直接标记失败
                Boolean deprecated = applicationDAO.lambdaQuery()
                        .select(TgApplicationInfo::getId, TgApplicationInfo::getCurrentAuditProcessStatus)
                        .eq(TgApplicationInfo::getId, applyId)
                        .oneOpt().map(TgApplicationInfo::getCurrentAuditProcessStatus)
                        .map(v -> Objects.equals(v, ApplicationConst.AuditStatus.INVALID_APPLICATION))
                        .orElse(false);
                if (deprecated) {
                    log.warn("deprecated apply: applyId={} detailId={}", applyId, detail.getId());
                    assetsFlowBatchDetailDAO.updateState(detail.getId(), AssetsUpgradeStateEnum.failed);
                    this.postHandleWhenFinishDetail(detail.getBatchId(), detail.getQcBatchId(), "作废");
                    continue;
                }

                // 启动出数工作流
                ExecFlowParam param = ExecFlowParam.builder().applicationId(applyId)
                        .triggerId(detail.getId())
                        .workGroup(appProperties.getFlowWorkGroup())
                        .taskName("定时出数")
                        .build();
                userDataAssetsService.executeWorkFlow(param);

                int idx = count.incrementAndGet();
                // 轮询等结束
                scheduler.scheduleAtFixedRate(() -> {
                    Optional<TgApplicationInfo> applyOpt = applicationDAO.lambdaQuery()
                            .select(TgApplicationInfo::getDataState)
                            .eq(TgApplicationInfo::getId, applyId).oneOpt();
                    if (!applyOpt.isPresent()) {
                        return;
                    }

                    TgApplicationInfo apply = applyOpt.get();
                    int sec = LocalDateTime.now().getSecond();
                    if (sec > 30 && sec < 40) {
                        log.info("RUN: applyId={}", applyId);
                    }
                    if (!ApplyDataStateEnum.isEnd(apply.getDataState())) {
                        return;
                    }

                    // 更新触发记录状态，只能是从进行中改为当前状态，因为同时在异步创建资产 可能覆盖资产的状态
                    AssetsUpgradeStateEnum state = ApplyDataStateEnum.isSuccess(apply.getDataState())
                            ? AssetsUpgradeStateEnum.success : AssetsUpgradeStateEnum.failed;
                    assetsFlowBatchDetailDAO.updateState(detail.getId(), state);

                    redisSemaphore.release(RedisKeys.Workflow.CREATE_ASSETS);
                    if (redisSemaphore.runCount(RedisKeys.Workflow.CREATE_ASSETS) == 0) {
                        log.info("工作流全部完成 {}", idx);
                    } else {
                        log.info("工作流完成 {}", idx);
                    }

                    this.postHandleWhenFinishDetail(detail.getBatchId(), detail.getQcBatchId(), "完成");
                    throw new CustomException("工作流完成 " + idx);
                }, 60, 5, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

    /**
     * @param batchId   工作流出数批次id
     * @param qcBatchId 原本是动态基于这个id去关联，开启资产qc，后续改动不依赖动态资产，而是全量进行QC，所以逻辑不会进入
     */
    private void postHandleWhenFinishDetail(Long batchId, Long qcBatchId, String name) {
//        Long batchId = detail.getBatchId();
        Integer runCnt = assetsFlowBatchDetailDAO.lambdaQuery()
                .eq(AssetsFlowBatchDetail::getBatchId, batchId)
                .in(AssetsFlowBatchDetail::getState, AssetsUpgradeStateEnum.actions)
                .count();
        if (runCnt == 0) {
            log.warn("Force Finish Batch: id={} name={}", batchId, name);

            Integer failCnt = assetsFlowBatchDetailDAO.lambdaQuery()
                    .eq(AssetsFlowBatchDetail::getBatchId, batchId)
                    .eq(AssetsFlowBatchDetail::getState, AssetsUpgradeStateEnum.failed.name())
                    .count();

            AssetsUpgradeStateEnum finalState = failCnt > 0 ? AssetsUpgradeStateEnum.failed : AssetsUpgradeStateEnum.success;
            boolean updateSuccess = assetsFlowBatchDAO.updateState(batchId, finalState);

            if (updateSuccess) {
                // 发出告警
                tgFlowProcessAlertFacade.sendAssetsFlowAlert(assetsFlowBatchDAO.getById(batchId));
            }
        }

//        Long qcBatchId = detail.getQcBatchId();
        if (Objects.nonNull(qcBatchId)) {
            pool.execute(() -> {
                if (this.allFlowTriggerFinish(qcBatchId)) {
                    log.info("release lock by finish");
                    assetsQcService.finishFlowUpgrade(qcBatchId);
                }
            });
        }
    }

    private String extractName(String name) {
        try {
            String[] rows = name.split("-");
            return rows[1];
        } catch (Exception e) {
            log.error("", e);
            return name;
        }
    }

    @RegisterCronMethod
    public void autoConvertSnapTable() {
        convertToSnapshotTable(1000, null);
    }

    /**
     * 将复制表转换成单表
     *
     * @param max 单次转换的表数量
     */
    @Override
    public void convertToSnapshotTable(Integer max, Long userId) {
        LambdaQueryWrapper<UserDataAssetsSnapshot> query = new QueryWrapper<UserDataAssetsSnapshot>().lambda()
                .select(UserDataAssetsSnapshot::getId, UserDataAssetsSnapshot::getAssetTableName, UserDataAssetsSnapshot::getAssetsSql)
                .eq(Objects.nonNull(userId), UserDataAssetsSnapshot::getApplicantId, userId)
                .likeLeft(UserDataAssetsSnapshot::getAssetTableName, "_shard")
                .and(v -> v.eq(UserDataAssetsSnapshot::getExpireType, AssetsExpireEnum.normal.name()).or().isNull(UserDataAssetsSnapshot::getExpireType))
                .last(" limit " + max);
        List<UserDataAssetsSnapshot> snapshots = userDataAssetsSnapshotDAO.getBaseMapper().selectList(query);
        if (CollectionUtils.isNotEmpty(snapshots)) {
            for (UserDataAssetsSnapshot asset : snapshots) {
                try {
                    String old = asset.getAssetTableName();
                    String snap = asset.getAssetTableName().replace(CkTableSuffixTable.SHARD, CkTableSuffixTable.SNAP);
                    String snapSQL = asset.getAssetsSql().replace(old, snap);

                    ckClusterAdapter.convertTable(asset.getId(), old, snap);
                    userDataAssetsSnapshotDAO.update(null, new UpdateWrapper<UserDataAssetsSnapshot>().lambda()
                            .set(UserDataAssetsSnapshot::getAssetTableName, snap)
                            .set(UserDataAssetsSnapshot::getAssetsSql, snapSQL)
                            .set(UserDataAssetsSnapshot::getUpdateTime, Optional.ofNullable(asset.getUpdateTime()).orElse(LocalDateTime.now()))
                            .eq(UserDataAssetsSnapshot::getId, asset.getId()));
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }
        max -= snapshots.size();
        if (max > 0) {
            LambdaQueryWrapper<UserDataAssets> user = new QueryWrapper<UserDataAssets>().lambda()
                    .select(UserDataAssets::getId, UserDataAssets::getAssetTableName, UserDataAssets::getAssetsSql, UserDataAssets::getUpdateTime)
                    .eq(Objects.nonNull(userId), UserDataAssets::getApplicantId, userId)
                    .likeLeft(UserDataAssets::getAssetTableName, "_shard")
                    .and(v -> v.eq(UserDataAssets::getExpireType, AssetsExpireEnum.normal.name()).or().isNull(UserDataAssets::getExpireType))
                    .last(" limit " + max);
            List<UserDataAssets> assets = userDataAssetsDAO.getBaseMapper().selectList(user);

            if (CollectionUtils.isNotEmpty(assets)) {
                for (UserDataAssets asset : assets) {
                    try {
                        String old = asset.getAssetTableName();
                        String snap = asset.getAssetTableName().replace(CkTableSuffixTable.SHARD, CkTableSuffixTable.SNAP);
                        String snapSQL = asset.getAssetsSql().replace(old, snap);
                        ckClusterAdapter.convertTable(asset.getId(), old, snap);
                        userDataAssetsDAO.update(null, new UpdateWrapper<UserDataAssets>().lambda()
                                .set(UserDataAssets::getAssetTableName, snap)
                                .set(UserDataAssets::getAssetsSql, snapSQL)
                                .set(UserDataAssets::getUpdateTime, Optional.ofNullable(asset.getUpdateTime()).orElse(LocalDateTime.now()))
                                .eq(UserDataAssets::getId, asset.getId()));
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            }
        }
    }
}
