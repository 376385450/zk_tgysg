package com.sinohealth.system.biz.table.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sinohealth.common.alert.AlertTemplate;
import com.sinohealth.common.alert.plugin.wechatrobot.message.TextMessage;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.TableInfoSnapshotCompareDetailCategory;
import com.sinohealth.common.enums.TableInfoSnapshotCompareResultState;
import com.sinohealth.common.enums.TableInfoSnapshotCompareState;
import com.sinohealth.common.enums.dataassets.AssetsExpireEnum;
import com.sinohealth.common.enums.dataassets.AssetsSnapshotTypeEnum;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.enums.dataassets.TablePushPlanStateEnum;
import com.sinohealth.common.enums.process.FlowProcessCategory;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.dao.ApplicationFormDAO;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.ck.constant.CkTableSuffixTable;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.biz.dataassets.dao.AssetsCompareDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsWideUpgradeTriggerDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsWideUpgradeTrigger;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;
import com.sinohealth.system.biz.dataassets.dto.FlowAssetsPageDTO;
import com.sinohealth.system.biz.dataassets.helper.UserDataAssetsUploadFtpHelper;
import com.sinohealth.system.biz.dataassets.service.AssetsQcService;
import com.sinohealth.system.biz.dataassets.service.impl.AssetsUpgradeTriggerServiceImpl;
import com.sinohealth.system.biz.process.dao.TgFlowProcessManagementDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessManagement;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.service.DataPlanService;
import com.sinohealth.system.biz.table.constants.DeleteStatusEnum;
import com.sinohealth.system.biz.table.constants.TablePushStatusEnum;
import com.sinohealth.system.biz.table.dao.TableInfoSnapshotDAO;
import com.sinohealth.system.biz.table.dao.TgTableInfoSnapshotCompareDAO;
import com.sinohealth.system.biz.table.dao.TgTableInfoSnapshotCompareDetailDAO;
import com.sinohealth.system.biz.table.dao.TgTableInfoSnapshotComparePlanDAO;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import com.sinohealth.system.biz.table.domain.TablePushAssetsPlan;
import com.sinohealth.system.biz.table.domain.TgTableInfoSnapshotCompare;
import com.sinohealth.system.biz.table.domain.TgTableInfoSnapshotCompareDetail;
import com.sinohealth.system.biz.table.domain.TgTableInfoSnapshotComparePlan;
import com.sinohealth.system.biz.table.dto.*;
import com.sinohealth.system.biz.table.facade.TableInfoSnapshotCompareFacade;
import com.sinohealth.system.biz.table.mapper.TablePushAssetsPlanMapper;
import com.sinohealth.system.biz.table.service.TableInfoSnapshotService;
import com.sinohealth.system.biz.table.vo.TableComparePlanVO;
import com.sinohealth.system.biz.table.vo.TableInfoCompareTaskVO;
import com.sinohealth.system.biz.table.vo.TableInfoSnapshotPageVO;
import com.sinohealth.system.biz.table.vo.TablePushAssetsPlanVO;
import com.sinohealth.system.biz.table.vo.TableSnapInfoVO;
import com.sinohealth.system.config.AlertBizType;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.ckpg.SelfCKProperties;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.mapper.TableInfoMapper;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.mapper.TgHiveProviderMapper;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-04-18 14:24
 */
@Slf4j
@Service
public class TableInfoSnapshotServiceImpl implements TableInfoSnapshotService {

    @Resource
    private TableInfoMapper tableInfoMapper;
    @Resource
    private TableInfoSnapshotDAO tableInfoSnapshotDAO;
    @Resource
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;
    @Resource
    private AssetsWideUpgradeTriggerDAO assetsWideUpgradeTriggerDAO;
    @Autowired
    private TablePushAssetsPlanMapper tablePushAssetsPlanMapper;
    @Autowired
    private AssetsCompareDAO assetsCompareDAO;
    @Autowired
    private ApplicationFormDAO applicationFormDAO;


    @Autowired
    private AssetsQcService assetsQcService;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private ISysUserService sysUserService;

    @Resource(name = "slaveDataSource")
    private DataSource slaveDataSource;
    @Resource
    private SelfCKProperties selfCKProperties;

    @Resource
    @Qualifier(ThreadPoolType.SYNC_CK)
    private ThreadPoolTaskExecutor pool;
    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER)
    private ScheduledExecutorService scheduler;
    @Autowired
    private TgCkProviderMapper ckProviderMapper;
    @Autowired
    private CKClusterAdapter ckClusterAdapter;
    @Qualifier(AlertBizType.BIZ)
    @Autowired
    private AlertTemplate bizAlertTemplate;
    @Autowired
    private AppProperties appProperties;

    @Autowired
    private TgHiveProviderMapper hiveProviderMapper;

    @Autowired
    private TableInfoSnapshotCompareFacade tableInfoSnapshotCompareFacade;
    @Autowired
    private TgFlowProcessManagementDAO tgFlowProcessManagementDAO;

    @Autowired
    private TgTableInfoSnapshotCompareDAO tgTableInfoSnapshotCompareDAO;

    @Autowired
    private TgTableInfoSnapshotCompareDetailDAO tgTableInfoSnapshotCompareDetailDAO;

    @Autowired
    private TgTableInfoSnapshotComparePlanDAO tgTableInfoSnapshotComparePlanDAO;
    @Autowired
    private UserDataAssetsUploadFtpHelper userDataAssetsUploadFtpHelper;

    @Autowired
    private ApplicationDAO applicationDAO;
    @Autowired
    private DataPlanService dataPlanService;


    private AjaxResult<Void> rollingTableCommon(String shardTable, boolean partly) {
        List<TableInfo> existTables = tableInfoMapper.selectList(new QueryWrapper<TableInfo>()
                .lambda().select(TableInfo::getId, TableInfo::getVersion, TableInfo::getBizType)
                .eq(TableInfo::getTableNameDistributed, shardTable));
        if (CollectionUtils.isEmpty(existTables)) {
            bizAlertTemplate.send(TextMessage.build("底表不存在：" + shardTable,
                    null, null));
            log.warn("NOT EXIST: shardTable={}", shardTable);
            return AjaxResult.error("not exist");
        }
        if (existTables.size() != 1) {
            bizAlertTemplate.send(TextMessage.build("底表重名：" + shardTable,
                    null, null));
            return AjaxResult.error("exist repeat " + shardTable);
        }
        TableInfo table = existTables.get(0);
        Long tableId = table.getId();

        TableInfoSnapshot last = tableInfoSnapshotDAO.getLatest(tableId);

        if (Objects.equals(last.getPushStatus(), TablePushStatusEnum.run.name())) {
            bizAlertTemplate.send(TextMessage.build("底表在推送资产升级中，新版本不允许替换，需要推送完后手动重试：" + shardTable, null, null));
            return AjaxResult.error("最新版本推送中，忽略该版本替换");
        }

        pool.execute(() -> this.rollingTableSync(shardTable, table, partly));
        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> rollingTable(String shardTable) {
        return this.rollingTableCommon(shardTable, false);
    }

    @Override
    public AjaxResult<Void> rollingTablePartly(String shardTable) {
        return this.rollingTableCommon(shardTable, true);
    }

    /**
     * @param shardTable 原始表名，不是swap表
     * @param table      表信息
     */
    private void rollingTableSync(String shardTable, TableInfo table, boolean partly) {
        Long tableId = table.getId();
        String db;
        try {
            LocalDateTime syncTime = LocalDateTime.now();
            db = selfCKProperties.getDatabase();

            Integer curVer = Optional.ofNullable(table.getVersion()).orElse(1);
            int newVersion = curVer + 1;

//            this.deleteOldVersion(tableId, ope, db);
            String local = CkTableSuffixTable.getLocal(shardTable);

            String versionLocal = null;
            String versionShard = null;
            // 全量更新时，置换临时表和当前表
            if (!partly) {
                String origin = CkTableSuffixTable.getOrigin(shardTable);
                String swapShard = CkTableSuffixTable.getSwapShard(shardTable);
                String swapLocal = CkTableSuffixTable.getLocal(swapShard);

                try {
                    ckClusterAdapter.execute("show create table " + db + "." + swapShard);
                } catch (Exception e) {
                    log.warn("NO Swap table {}", e.getMessage());
                    return;
                }
                // 创建历史版本表
                versionLocal = origin + "_v" + curVer + CkTableSuffixTable.LOCAL;
                versionShard = CkTableSuffixTable.getShard(versionLocal);

                log.info("UPGRADE: {} TO {} {}", shardTable, curVer, versionShard);
                // 删除 swap_shard
                ckClusterAdapter.execute("DROP TABLE IF EXISTS " + db + "." + swapShard + " on cluster default_cluster");

                ckClusterAdapter.execute("rename table " + local + " to " + versionLocal + " on cluster default_cluster");
                ckClusterAdapter.execute("rename table " + swapLocal + " to " + local + " on cluster default_cluster");

                String versionShardDDL = "    CREATE TABLE IF NOT EXISTS " + db + "." + versionShard + " ON cluster default_cluster " + "    as " + db + "." + versionLocal + "    ENGINE = Distributed(default_cluster, " + db + ", " + versionLocal + ", rand()) ;";
                ckClusterAdapter.execute(versionShardDDL);
            }

            tableInfoMapper.update(null, new UpdateWrapper<TableInfo>().lambda()
                    .eq(TableInfo::getId, tableId)
                    .set(TableInfo::getVersion, newVersion)
                    .set(TableInfo::getSyncTime, syncTime));
            // 更新上一次记录
            tableInfoSnapshotDAO.update(null, new UpdateWrapper<TableInfoSnapshot>().lambda()
                    .eq(TableInfoSnapshot::getTableId, tableId)
                    .eq(TableInfoSnapshot::getVersion, curVer)
                    .set(Objects.nonNull(versionLocal), TableInfoSnapshot::getTableName, versionLocal)
                    .set(Objects.nonNull(versionShard), TableInfoSnapshot::getTableNameDistributed, versionShard)
                    .set(TableInfoSnapshot::getLatest, false));

            if (!partly) {
                tableInfoSnapshotDAO.update(null, new UpdateWrapper<TableInfoSnapshot>().lambda()
                        .eq(TableInfoSnapshot::getTableName, local)
                        .eq(TableInfoSnapshot::getLatest, false)
                        .set(TableInfoSnapshot::getTableName, versionLocal)
                        .set(TableInfoSnapshot::getTableNameDistributed, versionShard));
            }

            CurrentDataPlanDTO plan = dataPlanService.currentPeriod(table.getBizType());
            // 创建新版本记录
            Long count = ckProviderMapper.countTable(shardTable);
            TableInfoSnapshot latest = new TableInfoSnapshot()
                    .setLatest(true)
                    .setTableId(tableId)
                    .setVersion(newVersion)
                    .setVersionPeriod(this.queryMaxVersionPeriod(shardTable))
                    .setTableName(local)
                    .setTableNameDistributed(shardTable)
                    .setStatus(DeleteStatusEnum.normal.name())
                    .setPushStatus(TablePushStatusEnum.none.name())
                    .setVersionPeriod(plan.getPeriod())
                    .setFlowProcessType(plan.getFlowProcessType())
                    .setTotalRow(count)
                    .setSyncTime(syncTime);

            if (StringUtils.isNotBlank(plan.getPeriod())) {
                latest.setRemark(plan.getPeriod() + "-" + FlowProcessTypeEnum.getDescByName(plan.getFlowProcessType()));
            }
            tableInfoSnapshotDAO.save(latest);

            // 依据推送计划 创建一批推送任务
//            TablePushAssetsPlan plan = tablePushAssetsPlanMapper.selectOne(new QueryWrapper<TablePushAssetsPlan>().lambda().eq(TablePushAssetsPlan::getTableId, tableId).eq(TablePushAssetsPlan::getState, TablePushPlanStateEnum.wait.name()).last(" limit 1"));
//            if (Objects.nonNull(plan)) {
//                tablePushAssetsPlanMapper.update(null, new UpdateWrapper<TablePushAssetsPlan>().lambda().eq(TablePushAssetsPlan::getId, plan.getId())
//                        .set(TablePushAssetsPlan::getState, TablePushPlanStateEnum.execute.name()));
//                TableSnapshotPushRequest req = new TableSnapshotPushRequest();
//                req.setTableId(tableId);
//                req.setPreVersion(plan.getPreVersion());
//                req.setNeedQc(true);
//                this.pushTable(req);
//            }

            // 触发底表比对计划[不抛出异常/不影响主流程]
            triggerDiffPlan(tableId);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 删除最老版本的CK表 限制共存的表数量
     *
     * @see TableInfoSnapshotServiceImpl#asyncDeleteQcVersionTable(Long)
     */
    @Deprecated
    private void deleteOldVersion(Long tableId, JdbcOperations ope, String db) {
        int historyCnt = appProperties.getMaxVersionedTabCnt() - 1; // 减1是为了给最新版本
        List<TableInfoSnapshot> needDelete = tableInfoSnapshotDAO.lambdaQuery()
                .select(TableInfoSnapshot::getId, TableInfoSnapshot::getTableName, TableInfoSnapshot::getTableNameDistributed)
                .eq(TableInfoSnapshot::getStatus, DeleteStatusEnum.normal.name()).eq(TableInfoSnapshot::getTableId, tableId)
                .orderByDesc(TableInfoSnapshot::getVersion).last(" limit " + historyCnt + ", 1000 ").list();
        if (CollectionUtils.isEmpty(needDelete)) {
            return;
        }

        for (TableInfoSnapshot tab : needDelete) {
            log.warn("delete table: id={} table={}", tableId, tab.getTableName());
            ope.execute("DROP TABLE IF EXISTS " + db + "." + tab.getTableNameDistributed() + " on cluster default_cluster");
            ope.execute("DROP TABLE IF EXISTS " + db + "." + tab.getTableName() + " on cluster default_cluster");
        }

        Set<Long> ids = Lambda.buildSet(needDelete, TableInfoSnapshot::getId);
        tableInfoSnapshotDAO.lambdaUpdate().set(TableInfoSnapshot::getStatus, DeleteStatusEnum.delete.name()).in(TableInfoSnapshot::getId, ids).update();
    }

    public void asyncDeleteQcVersionTable(Long tableId) {
        // 保留最新3期 交付+临时 版本
        List<TableInfoSnapshot> deliverTables = tableInfoSnapshotDAO.lambdaQuery()
                .eq(TableInfoSnapshot::getTableId, tableId)
                .in(TableInfoSnapshot::getFlowProcessType, FlowProcessTypeEnum.rollDelete)
                .eq(TableInfoSnapshot::getStatus, DeleteStatusEnum.normal.name())
                .orderByDesc(TableInfoSnapshot::getVersion)
                .last(" limit 3,10000")
                .list();

        // 非最新SOP版本
        List<TableInfoSnapshot> normalTables = tableInfoSnapshotDAO.lambdaQuery()
                .eq(TableInfoSnapshot::getTableId, tableId)
                .eq(TableInfoSnapshot::getFlowProcessType, FlowProcessTypeEnum.sop.name())
                .eq(TableInfoSnapshot::getStatus, DeleteStatusEnum.normal.name())
                .orderByDesc(TableInfoSnapshot::getVersion)
                .last(" limit 1,10000")
                .list();

        // QC 版本
        List<TableInfoSnapshot> qcTables = tableInfoSnapshotDAO.lambdaQuery()
                .eq(TableInfoSnapshot::getTableId, tableId)
                .eq(TableInfoSnapshot::getFlowProcessType, FlowProcessTypeEnum.qc.name())
                .eq(TableInfoSnapshot::getStatus, DeleteStatusEnum.normal.name())
                .list();

        List<TableInfoSnapshot> all = new ArrayList<>();
        all.addAll(qcTables);
        all.addAll(normalTables);
        all.addAll(deliverTables);
        if (CollectionUtils.isEmpty(all)) {
            return;
        }

        // 删除底表 以及 对应的资产表
        pool.execute(() -> {
            // 删除底表
            for (TableInfoSnapshot qcTable : all) {
                AjaxResult<Void> deleteResult = this.deleteSnapshot(qcTable.getId());
                if (!deleteResult.isSuccess()) {
                    log.warn("delete failed: {} {}", qcTable.getTableId(), qcTable.getVersion());
                }
            }

            // 删除资产表
            if (CollectionUtils.isEmpty(qcTables)) {
                return;
            }
            List<Integer> versionList = Lambda.buildList(qcTables, TableInfoSnapshot::getVersion);
            List<UserDataAssetsSnapshot> history = userDataAssetsSnapshotDAO.lambdaQuery()
                    .select(UserDataAssetsSnapshot::getId, UserDataAssetsSnapshot::getAssetTableName, UserDataAssetsSnapshot::getFtpPath)
                    .eq(UserDataAssetsSnapshot::getBaseTableId, tableId)
                    .in(UserDataAssetsSnapshot::getBaseVersion, versionList)
                    .in(UserDataAssetsSnapshot::getSnapshotType, AssetsSnapshotTypeEnum.AUTO_DELETE_SCOPE)
                    .in(UserDataAssetsSnapshot::getExpireType, AssetsExpireEnum.TABLE_EXIST)
                    .list();
            if (CollectionUtils.isEmpty(history)) {
                return;
            }
            for (UserDataAssetsSnapshot snap : history) {
                try {
                    ckClusterAdapter.deleteAssetsTable(snap.getAssetTableName());
                    userDataAssetsSnapshotDAO.update(null, new UpdateWrapper<UserDataAssetsSnapshot>().lambda()
                            .eq(UserDataAssetsSnapshot::getId, snap.getId())
                            .set(UserDataAssetsSnapshot::getExpireType, AssetsExpireEnum.delete_data.name())
                    );
                } catch (Exception e) {
                    log.error("", e);
                }
            }

            // 删除FTP文件
            List<String> paths = history.stream().map(UserDataAssets::getFtpPath)
                    .filter(StringUtils::isNoneBlank).collect(Collectors.toList());
            userDataAssetsUploadFtpHelper.deleteFtp(paths);
        });

    }

    private String queryMaxVersionPeriod(String shardTable) {
        try {
            return ckProviderMapper.showCreateTable("select max(period) from " + shardTable);
        } catch (Exception ignore) {
            log.warn("not match field {}", shardTable);
            return null;
        }
    }

    @Scheduled(cron = "0 * * * * ? ")
    public void refreshStatus() {
        List<TableInfoSnapshot> run = tableInfoSnapshotDAO.lambdaQuery()
                .select(TableInfoSnapshot::getId, TableInfoSnapshot::getTableId, TableInfoSnapshot::getVersion)
                .eq(TableInfoSnapshot::getPushStatus, TablePushStatusEnum.run.name())
                .list();
        if (CollectionUtils.isEmpty(run)) {
            return;
        }
        this.refreshPushStatus(run);
    }

    @Override
    public AjaxResult<IPage<TableInfoSnapshotPageVO>> pageQuery(TableSnapshotPageRequest request) {
        IPage<TableInfoSnapshot> data = tableInfoSnapshotDAO.lambdaQuery()
                .eq(TableInfoSnapshot::getTableId, request.getTableId())
                .orderByDesc(TableInfoSnapshot::getVersion)
                .eq(StringUtils.isNotBlank(request.getPushStatus()), TableInfoSnapshot::getPushStatus, request.getPushStatus())
                .eq(StringUtils.isNotBlank(request.getFlowProcessType()), TableInfoSnapshot::getFlowProcessType, request.getFlowProcessType())
                .like(StringUtils.isNotBlank(request.getPeriod()), TableInfoSnapshot::getVersionPeriod, request.getPeriod())
                .page(request.buildPage());
        List<TableInfoSnapshot> records = data.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return AjaxResult.success(PageUtil.empty());
        }

        this.refreshPushStatus(data);

        List<Long> bizIds = Lambda.buildNonNullList(records, TableInfoSnapshot::getBizId);
        Map<Long, TgFlowProcessManagement> flowMap = tgFlowProcessManagementDAO.queryForPageList(bizIds);

        List<Long> userIds = flowMap.values().stream().map(TgFlowProcessManagement::getCreator).collect(Collectors.toList());
        List<Long> partUserIds = Lambda.buildNonNullList(records, TableInfoSnapshot::getCreateBy);
        userIds.addAll(partUserIds);
        Map<Long, String> nameMap = sysUserService.selectUserNameMapByIds(userIds);

        Map<Integer, List<AssetsWideUpgradeTrigger>> assVerMap;
        Set<Integer> versionSet = Lambda.buildSet(records, TableInfoSnapshot::getVersion);
        if (CollectionUtils.isNotEmpty(versionSet)) {
            List<AssetsWideUpgradeTrigger> allTriggers = assetsWideUpgradeTriggerDAO.lambdaQuery()
                    .select(AssetsWideUpgradeTrigger::getActVersion, AssetsWideUpgradeTrigger::getState,
                            AssetsWideUpgradeTrigger::getStartTime, AssetsWideUpgradeTrigger::getFinishTime)
                    .eq(AssetsWideUpgradeTrigger::getTableId, request.getTableId())
                    .in(AssetsWideUpgradeTrigger::getActVersion, versionSet)
                    .list();
            assVerMap = Lambda.buildGroupMap(allTriggers, AssetsWideUpgradeTrigger::getActVersion);
        } else {
            assVerMap = Collections.emptyMap();
        }

        return AjaxResult.success(PageUtil.convertMap(data, v -> {
            TableInfoSnapshotPageVO vo = new TableInfoSnapshotPageVO();
            BeanUtils.copyProperties(v, vo);
            Optional<TgFlowProcessManagement> flowOpt = Optional.ofNullable(flowMap.get(v.getBizId()));
            vo.setFlowProcessCategory(flowOpt.map(x -> FlowProcessCategory.AUTO.getCode()).orElse(FlowProcessCategory.MANUAL_OPERATION.getCode()));
            vo.setFlowProcessName(flowOpt.map(TgFlowProcessManagement::getName).orElse(""));

            List<AssetsWideUpgradeTrigger> triggers = assVerMap.getOrDefault(v.getVersion(), Collections.emptyList());

            // 隐藏开始时间 未执行时
            if (!TablePushStatusEnum.none.name().equals(vo.getPushStatus())) {
                LocalDateTime first = triggers.stream().filter(Objects::nonNull).map(AssetsWideUpgradeTrigger::getStartTime)
                        .filter(Objects::nonNull).min(LocalDateTime::compareTo).orElse(null);
                vo.setStartTime(first);
            }

            // 隐藏结束时间 直到终止状态
            if (TablePushStatusEnum.END.contains(vo.getPushStatus())) {
                LocalDateTime last = triggers.stream().filter(Objects::nonNull).map(AssetsWideUpgradeTrigger::getFinishTime)
                        .filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);
                vo.setFinishTime(last);
            }

            vo.setCreator(flowOpt.map(u -> "系统").orElse(Optional.ofNullable(v.getCreateBy())
                    .filter(u -> !Objects.equals(u, 0L)).map(nameMap::get).orElse("系统")));

            Map<String, Long> stateMap = triggers.stream()
                    .collect(Collectors.groupingBy(AssetsWideUpgradeTrigger::getState, Collectors.counting()));
            vo.setDetailCnt(triggers.size());
            vo.setFinishCnt(Optional.ofNullable(stateMap.get(AssetsUpgradeStateEnum.success.name()))
                    .map(Long::intValue).orElse(0));
            vo.setCostTime(DateUtil.caluLocalDateTimeDiff(vo.getStartTime(), vo.getFinishTime()));
            return vo;
        }));
    }

    private void refreshPushStatus(IPage<TableInfoSnapshot> data) {
        List<TableInfoSnapshot> actList = data.getRecords().stream()
                .filter(v -> TablePushStatusEnum.run.name().equals(v.getPushStatus())).collect(Collectors.toList());
        refreshPushStatus(actList);
    }

    private void refreshPushStatus(List<TableInfoSnapshot> actList) {
        if (CollectionUtils.isEmpty(actList)) {
            return;
        }

        Set<Long> successIds = new HashSet<>();
        Set<Long> failIds = new HashSet<>();
        for (TableInfoSnapshot table : actList) {
            Integer actCount = assetsWideUpgradeTriggerDAO.lambdaQuery()
                    .eq(AssetsWideUpgradeTrigger::getTableId, table.getTableId())
                    .eq(AssetsWideUpgradeTrigger::getActVersion, table.getVersion())
                    .in(AssetsWideUpgradeTrigger::getState, AssetsUpgradeStateEnum.actions)
                    .count();
            if (actCount == 0) {
                Integer failCount = assetsWideUpgradeTriggerDAO.lambdaQuery()
                        .eq(AssetsWideUpgradeTrigger::getTableId, table.getTableId())
                        .eq(AssetsWideUpgradeTrigger::getActVersion, table.getVersion())
                        .eq(AssetsWideUpgradeTrigger::getState, AssetsUpgradeStateEnum.failed.name())
                        .count();
                if (failCount > 0) {
                    table.setPushStatus(TablePushStatusEnum.failed.name());
                    failIds.add(table.getTableId());
                } else {
                    table.setPushStatus(TablePushStatusEnum.success.name());
                    successIds.add(table.getTableId());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(successIds)) {
            tableInfoSnapshotDAO.lambdaUpdate().in(TableInfoSnapshot::getTableId, successIds)
                    .eq(TableInfoSnapshot::getLatest, true)
                    .set(TableInfoSnapshot::getPushStatus, AssetsUpgradeStateEnum.success.name()).update();
        }
        if (CollectionUtils.isNotEmpty(failIds)) {
            tableInfoSnapshotDAO.lambdaUpdate().in(TableInfoSnapshot::getTableId, failIds)
                    .eq(TableInfoSnapshot::getLatest, true)
                    .set(TableInfoSnapshot::getPushStatus, AssetsUpgradeStateEnum.failed.name()).update();
        }
    }

    /**
     * 手动触发底表推数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> manualPushTable(TableSnapshotPushRequest request) {
        if (Objects.nonNull(request.getCompareType())) {
            request.setNeedCompare(true);

            Optional<TableInfoSnapshot> lastOpt = tableInfoSnapshotDAO.lambdaQuery()
                    .select(TableInfoSnapshot::getId, TableInfoSnapshot::getVersion)
                    .eq(TableInfoSnapshot::getFlowProcessType, request.getCompareType())
                    .eq(TableInfoSnapshot::getPushStatus, TablePushStatusEnum.success.name())
                    .orderByDesc(TableInfoSnapshot::getVersion)
                    .last(" limit 1")
                    .oneOpt();
            if (lastOpt.isPresent()) {
                request.setPreVersion(lastOpt.get().getVersion());
            } else {
                return AjaxResult.error("需求数据对比所选版本不存在");
            }
        }
        request.setNeedQc(true);
//        if (StringUtils.isBlank(request.getRemark())) {
//            request.setRemark(request.getVersionPeriod() + "-" + FlowProcessTypeEnum.getDescByName(request.getFlowProcessType()));
//        }

        request.setCreateBy(SecurityUtils.getUserId());
        return this.pushTable(request);
    }

    /**
     * TODO 支持立即推动更新， 实现思路为写入trigger记录后，当前实例启动临时线程池 消费掉刚刚创建的任务
     * <p>
     * 创建 底表推动资产更新 任务
     *
     * @see AssetsUpgradeTriggerServiceImpl#scheduleWideTable 消费这里创建的任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> pushTable(TableSnapshotPushRequest request) {
        Long tableId = request.getTableId();
        Integer preVersion = request.getPreVersion();

        TableInfoSnapshot last = tableInfoSnapshotDAO.getLatest(tableId);
        Integer version = last.getVersion();
        if (Objects.isNull(request.getSkipAssertsBaseVersionFilter()) || !request.getSkipAssertsBaseVersionFilter()) {
            if (Objects.equals(version, preVersion)) {
                return AjaxResult.error("对比版本不能是当前版本");
            }
        }
        String prodCodes = null;
        if (CollectionUtils.isNotEmpty(request.getProdCodes())) {
            prodCodes = String.join(",", request.getProdCodes());
        }
//        boolean needUpdateVersion = StringUtils.isNotBlank(request.getVersionPeriod());
        tableInfoSnapshotDAO.update(null, new UpdateWrapper<TableInfoSnapshot>().lambda()
                        .eq(TableInfoSnapshot::getId, last.getId())
                        .set(TableInfoSnapshot::getPushStatus, TablePushStatusEnum.run.name())
                        .set(TableInfoSnapshot::getPreVersion, preVersion)
                        .set(TableInfoSnapshot::getRemark, request.getRemark())
//                        .set(TableInfoSnapshot::getFlowProcessType, request.getFlowProcessType())
                        .set(CollectionUtils.isNotEmpty(request.getProdCodes()), TableInfoSnapshot::getProdCodes, prodCodes)
                        .set(Objects.nonNull(request.getCreateBy()), TableInfoSnapshot::getCreateBy, request.getCreateBy())
//                .set(needUpdateVersion, TableInfoSnapshot::getVersionPeriod, request.getVersionPeriod())
        )
        ;

//        this.updateAssetsVersionPeriod(tableId, version, request.getVersionPeriod());

        // 创建资产更新任务
//        LambdaQueryChainWrapper<UserDataAssets> wrapper = userDataAssetsDAO.lambdaQuery()
//                .select(UserDataAssets::getId, UserDataAssets::getBaseTableId, UserDataAssets::getVersion)
//                .eq(UserDataAssets::getTemplateType, TemplateTypeEnum.wide_table.name())
//                .eq(UserDataAssets::getBaseTableId, tableId)
//                // 一次性需求不自动升级版本
//                .eq(UserDataAssets::getRequireTimeType, ApplicationConst.RequireTimeType.PERSISTENCE)
//                .ne(UserDataAssets::getBaseVersion, version);
//        userDataAssetsDAO.fillValid(wrapper);
//        List<UserDataAssets> relateAssets = wrapper.list();

        List<UserDataAssets> relateAssets = userDataAssetsDAO.queryRelateAssets(tableId
                , version, request.getSkipAssertsBaseVersionFilter(), request.getProdCodes());
        if (CollectionUtils.isEmpty(relateAssets)) {
            log.info("no assets need trigger {}", tableId);
            tableInfoSnapshotDAO.updatePushStatus(last.getId(), TablePushStatusEnum.success);
            return AjaxResult.succeed();
        }

        if (CollectionUtils.isNotEmpty(request.getDeliverTimeTypes())) {
            // 限制资产时间类型
            Set<Long> applicationIds = relateAssets.stream().map(UserDataAssets::getSrcApplicationId).collect(Collectors.toSet());
            List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
                    .select(TgApplicationInfo::getId)
                    .in(TgApplicationInfo::getId, applicationIds)
                    .ne(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.INVALID_APPLICATION)
                    // 限制一次性需求
                    .eq(TgApplicationInfo::getRequireTimeType, ApplicationConst.RequireTimeType.PERSISTENCE)
                    // 限制对应的时间类型【月度、季度、半年度、年度】
                    .in(TgApplicationInfo::getDeliverTimeType, request.getDeliverTimeTypes())
                    .gt(TgApplicationInfo::getDataExpir, new Date())
                    .list();
            Set<Long> filterApplicationIds = Lambda.buildSet(applyList, TgApplicationInfo::getId);

            relateAssets = relateAssets.stream()
                    .filter(i -> filterApplicationIds.contains(i.getSrcApplicationId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(relateAssets)) {
                log.info("no assets need trigger {}", tableId);
                tableInfoSnapshotDAO.updatePushStatus(last.getId(), TablePushStatusEnum.success);
                return AjaxResult.succeed();
            }
        }

        // 过滤掉暂停的需求
        Set<Long> applicationIds = relateAssets.stream().map(UserDataAssets::getSrcApplicationId).collect(Collectors.toSet());
        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getApplicationNo)
                .in(TgApplicationInfo::getId, applicationIds)
                .list();
        Set<String> noSet = Lambda.buildSet(applyList, TgApplicationInfo::getApplicationNo);
        Set<String> pauseNos = applicationFormDAO.queryPause(noSet);
        Set<Long> pauseIds = applyList.stream().filter(v -> pauseNos.contains(v.getApplicationNo()))
                .map(TgApplicationInfo::getId).collect(Collectors.toSet());
        relateAssets = relateAssets.stream()
                .filter(i -> !pauseIds.contains(i.getSrcApplicationId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(relateAssets)) {
            log.info("no assets need trigger {}", tableId);
            tableInfoSnapshotDAO.updatePushStatus(last.getId(), TablePushStatusEnum.success);
            return AjaxResult.succeed();
        }

        List<Long> assetsIds = Lambda.buildList(relateAssets, UserDataAssets::getId);
        LocalDateTime now = LocalDateTime.now();
        List<AssetsWideUpgradeTrigger> triggerList = relateAssets.stream().map(v -> {
            AssetsWideUpgradeTrigger trigger = new AssetsWideUpgradeTrigger();
            trigger.setAssetsId(v.getId());
            trigger.setApplyId(v.getSrcApplicationId());
            trigger.setBizId(request.getBizId());
            trigger.setPreVersion(preVersion);
            trigger.setActVersion(version);
            trigger.setTableId(v.getBaseTableId());
            trigger.setNeedQc(request.getNeedQc());
            trigger.setNeedCompare(request.getNeedCompare());
            trigger.setState(AssetsUpgradeStateEnum.wait.name());
            trigger.setCreateTime(now);
            trigger.setUpdateTime(now);
            return trigger;
        }).collect(Collectors.toList());

        log.info("save wide trigger: ids={}", assetsIds);

        assetsWideUpgradeTriggerDAO.saveBatch(triggerList);
        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<TableSnapInfoVO> queryTablePlanInfo(Long tableId) {
        TablePushAssetsPlan plan = tablePushAssetsPlanMapper.selectOne(new QueryWrapper<TablePushAssetsPlan>().lambda()
                .eq(TablePushAssetsPlan::getTableId, tableId)
                .eq(TablePushAssetsPlan::getState, TablePushPlanStateEnum.wait.name()));
        TableSnapInfoVO.TableSnapInfoVOBuilder builder = TableSnapInfoVO.builder();

        if (Objects.nonNull(plan)) {
            String period = Optional.ofNullable(plan.getPreVersion())
                    .map(v -> tableInfoSnapshotDAO.getVersion(tableId, v))
                    .map(TableInfoSnapshot::getVersionPeriod).orElse(null);
            TablePushAssetsPlanVO nextPlan = TablePushAssetsPlanVO.builder().id(plan.getId())
                    .preVersion(plan.getPreVersion()).preVersionPeriod(period).build();
            builder.plan(nextPlan);
        }
        Integer runCount = tableInfoSnapshotDAO.lambdaQuery()
                .eq(TableInfoSnapshot::getTableId, tableId)
                .eq(TableInfoSnapshot::getPushStatus, TablePushStatusEnum.run.name()).count();
        Integer latestCantRun = tableInfoSnapshotDAO.lambdaQuery()
                .eq(TableInfoSnapshot::getTableId, tableId)
                .eq(TableInfoSnapshot::getLatest, true)
                .in(TableInfoSnapshot::getPushStatus, TablePushStatusEnum.CANT_REPEAT_RUN).count();

        Long userId = SecurityUtils.getUserId();

        List<TgAssetInfo> infos = TgAssetInfo.newInstance().selectList(new QueryWrapper<TgAssetInfo>().lambda()
                .select(TgAssetInfo::getAssetManagerJson).eq(TgAssetInfo::getRelatedId, tableId)
                .eq(TgAssetInfo::getType, AssetType.TABLE.name()));
        boolean manager = infos.stream().filter(Objects::nonNull)
                .map(TgAssetInfo::getAssetManagerJson).filter(Objects::nonNull)
                .map(v -> Optional.ofNullable(JsonUtils.parse(v, new TypeReference<List<String>>() {
                })).orElse(new ArrayList<>()))
                .anyMatch(v -> v.contains(userId + ""));
        builder.create(runCount == 0 && latestCantRun == 0).manager(manager);

        Optional<TableInfoSnapshot> lastOpt = tableInfoSnapshotDAO.lambdaQuery()
                .eq(TableInfoSnapshot::getTableId, tableId)
                .eq(TableInfoSnapshot::getLatest, true).last(" limit 1").oneOpt();

        lastOpt.ifPresent(v -> builder
                .version("V" + v.getVersion()).syncTime(v.getSyncTime())
                .versionPeriod(v.getVersionPeriod())
                .flowProcessType(v.getFlowProcessType()));

        return AjaxResult.success(builder.build());
    }

    @Override
    public AjaxResult<Void> planPush(TableSnapshotPushRequest request) {
        // 取消已有数据
        tablePushAssetsPlanMapper.update(null, new UpdateWrapper<TablePushAssetsPlan>().lambda().set(TablePushAssetsPlan::getState, TablePushPlanStateEnum.cancel.name()).eq(TablePushAssetsPlan::getTableId, request.getTableId()).eq(TablePushAssetsPlan::getState, TablePushPlanStateEnum.wait.name()));

        TableInfoSnapshot latest = tableInfoSnapshotDAO.getLatest(request.getTableId());
        TablePushAssetsPlan plan = new TablePushAssetsPlan();
        plan.setTableId(request.getTableId());
        plan.setPreVersion(request.getPreVersion());
        plan.setNextVersion(latest.getVersion() + 1);
        plan.setState(TablePushPlanStateEnum.wait.name());
        plan.setCreator(SecurityUtils.getUserId());
        tablePushAssetsPlanMapper.insert(plan);
        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> cancelPlanPush(Long planId) {
        tablePushAssetsPlanMapper.update(null, new UpdateWrapper<TablePushAssetsPlan>().lambda().set(TablePushAssetsPlan::getState, TablePushPlanStateEnum.cancel.name()).eq(TablePushAssetsPlan::getId, planId).eq(TablePushAssetsPlan::getState, TablePushPlanStateEnum.wait.name()));
        return AjaxResult.succeed();
    }

    /**
     * 计算底表差异 保存到表
     */
    @Override
    public AjaxResult<Void> calculateDiff(TableDiffRequest request) {
        tableInfoSnapshotCompareFacade.handle(request);
        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> deleteSnapshot(Long id) {
        TableInfoSnapshot snap = tableInfoSnapshotDAO.getById(id);
        if (Objects.isNull(snap)) {
            return AjaxResult.error("不存在");
        }
        if (BooleanUtil.isTrue(snap.getLatest())) {
            return AjaxResult.error("不允许删除最新版本");
        }
        if (Objects.equals(snap.getPushStatus(), TablePushStatusEnum.run.name())) {
            return AjaxResult.error("数据推送中，不允许删除");
        }
        String local = snap.getTableName();
        String shard = snap.getTableNameDistributed();

        log.info("delete: id:{} version:{}", snap.getTableId(), snap.getVersion());

        // 历史版本和最新版本用同一个CK表时，不允许删除历史版本
        TableInfoSnapshot latest = tableInfoSnapshotDAO.getLatest(snap.getTableId());
        String tableName = latest.getTableName();
        if (Objects.equals(tableName, snap.getTableName())) {
            return AjaxResult.error("不允许删除当前版本，特殊版本");
        }

        ckProviderMapper.dropSyncTable("drop table if exists " + selfCKProperties.getDatabase() + "." + local + " on cluster default_cluster");
        ckProviderMapper.dropSyncTable("drop table if exists " + selfCKProperties.getDatabase() + "." + shard + " on cluster default_cluster");

        // 这种情况是因为QC增量同步导致的，需要特殊处理
        List<TableInfoSnapshot> sameRelate = tableInfoSnapshotDAO.lambdaQuery()
                .select(TableInfoSnapshot::getId)
                .eq(TableInfoSnapshot::getTableName, tableName)
                .eq(TableInfoSnapshot::getTableId, snap.getTableId())
                .eq(TableInfoSnapshot::getLatest, false)
                .list();
        if (CollectionUtils.isNotEmpty(sameRelate)) {
            List<Long> snapIds = Lambda.buildList(sameRelate);
            tableInfoSnapshotDAO.lambdaUpdate()
                    .in(TableInfoSnapshot::getId, snapIds)
                    .set(TableInfoSnapshot::getStatus, DeleteStatusEnum.delete.name())
                    .update();
        }
        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> edit(TableSnapshotUpdateRequest request) {
        TableInfoSnapshot snap = tableInfoSnapshotDAO.getById(request.getId());
        if (Objects.isNull(snap)) {
            return AjaxResult.error("不存在");
        }
        boolean needUpdateVersion = StringUtils.isNotBlank(request.getVersionPeriod());
        tableInfoSnapshotDAO.update(null, new UpdateWrapper<TableInfoSnapshot>()
                .lambda().eq(TableInfoSnapshot::getId, request.getId())
                .set(TableInfoSnapshot::getRemark, request.getRemark())
                .set(TableInfoSnapshot::getFlowProcessType, request.getFlowProcessType())
                .set(needUpdateVersion, TableInfoSnapshot::getVersionPeriod, request.getVersionPeriod()));
//        this.updateAssetsVersionPeriod(snap.getTableId(), snap.getVersion(), request.getVersionPeriod());

        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<IPage<TableInfoCompareTaskVO>> diffPage(TableDiffPageRequest request) {
        IPage<TgTableInfoSnapshotCompare> data = tgTableInfoSnapshotCompareDAO.page(request.buildPage(), new QueryWrapper<TgTableInfoSnapshotCompare>().lambda().eq(TgTableInfoSnapshotCompare::getTableId, request.getTableId()).orderByDesc(TgTableInfoSnapshotCompare::getCreateTime));
        Map<Long, List<TgTableInfoSnapshotCompareDetail>> taskMap;
        List<Long> taskIds = Optional.ofNullable(data.getRecords()).orElse(Collections.emptyList()).stream().filter(i -> Objects.equals(TableInfoSnapshotCompareResultState.NORMAL.getType(), i.getResultState())).map(TgTableInfoSnapshotCompare::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(taskIds)) {
            List<TgTableInfoSnapshotCompareDetail> list = queryCompareDetails(taskIds);
            taskMap = Optional.ofNullable(list).orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(TgTableInfoSnapshotCompareDetail::getCompareId));
        } else {
            taskMap = Collections.emptyMap();
        }

        TgTableInfoSnapshotCompare last = tgTableInfoSnapshotCompareDAO.getOne(new LambdaQueryWrapper<TgTableInfoSnapshotCompare>()
                .eq(TgTableInfoSnapshotCompare::getResultState, TableInfoSnapshotCompareResultState.NORMAL.getType())
                .eq(TgTableInfoSnapshotCompare::getTableId, request.getTableId())
                .orderByDesc(TgTableInfoSnapshotCompare::getCreateTime).last("limit 1"));
        IPage<TableInfoCompareTaskVO> convert = data.convert(i -> buildCompareVo(taskMap, i, last));
        return AjaxResult.success(convert);
    }

    @Override
    public AjaxResult<List<TableInfoCompareTaskVO>> diffList(TableDiffListRequest request) {
        List<TgTableInfoSnapshotCompare> list = tgTableInfoSnapshotCompareDAO.list(new LambdaQueryWrapper<TgTableInfoSnapshotCompare>().in(CollectionUtils.isNotEmpty(request.getIds()), TgTableInfoSnapshotCompare::getId, request.getIds()).in(CollectionUtils.isNotEmpty(request.getBizIds()), TgTableInfoSnapshotCompare::getBizId, request.getBizIds()));

        List<TableInfoCompareTaskVO> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            Map<Long, List<TgTableInfoSnapshotCompareDetail>> taskMap;
            List<Long> taskIds = Optional.of(list).orElse(Collections.emptyList()).stream().filter(i -> Objects.equals(TableInfoSnapshotCompareResultState.NORMAL.getType(), i.getResultState())).map(TgTableInfoSnapshotCompare::getId).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(taskIds)) {
                taskMap = Optional.ofNullable(queryCompareDetails(taskIds)).orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(TgTableInfoSnapshotCompareDetail::getCompareId));
            } else {
                taskMap = Collections.emptyMap();
            }
            list.forEach(i -> result.add(buildCompareVo(taskMap, i, null)));
        }
        return AjaxResult.success(result);
    }

    @Override
    public AjaxResult<Void> deleteDiff(Long taskId) {
        TgTableInfoSnapshotCompare compare = tgTableInfoSnapshotCompareDAO.getById(taskId);
        if (Objects.isNull(compare)) {
            throw new CustomException("比对任务不存在");
        }
        if (!Objects.equals(TableInfoSnapshotCompareResultState.NORMAL.getType(), compare.getResultState())) {
            throw new CustomException("该任务不可删除");
        }
        compare.setResultState(TableInfoSnapshotCompareResultState.DELETED.getType());
        tgTableInfoSnapshotCompareDAO.updateById(compare);

        tableInfoSnapshotCompareFacade.dropTaskTable(taskId, true);
        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> failDiff(Long taskId) {
        TgTableInfoSnapshotCompare compare = tgTableInfoSnapshotCompareDAO.getById(taskId);
        if (Objects.isNull(compare)) {
            throw new CustomException("比对任务不存在");
        }
        compare.setState(TableInfoSnapshotCompareState.FAIL.getType());
        compare.setFailReason("人工作废");
        tgTableInfoSnapshotCompareDAO.updateById(compare);

        tableInfoSnapshotCompareFacade.dropTaskTable(taskId, true);
        return AjaxResult.succeed();
    }

    private void updateAssetsVersionPeriod(Long tableId, Integer version, String versionPeriod) {
        if (StringUtils.isBlank(versionPeriod)) {
            return;
        }
        log.info("update all assets: tableId={} version={} versionStr={}", tableId, version, versionPeriod);
        userDataAssetsDAO.lambdaUpdate()
                .eq(UserDataAssets::getBaseTableId, tableId)
                .eq(UserDataAssets::getBaseVersion, version)
                .set(UserDataAssets::getBaseVersionPeriod, versionPeriod)
                .update();
    }

    @Override
    public AjaxResult<Void> compareCreateOrUpdate(TableDiffPlanCreateOrUpdateRequest request) {
        TgTableInfoSnapshotComparePlan one;
        if (Objects.nonNull(request.getId())) {
            one = tgTableInfoSnapshotComparePlanDAO.getById(request.getId());
            if (Objects.isNull(one)) {
                throw new CustomException("无对应计划");
            }
            if (!Objects.equals(one.getTableId(), request.getTableId())) {
                throw new CustomException("非相同表比对设置");
            }
        } else {
            // 二次判断
            LambdaQueryWrapper<TgTableInfoSnapshotComparePlan> lam = new LambdaQueryWrapper<>();
            lam.eq(TgTableInfoSnapshotComparePlan::getTableId, request.getTableId());
            one = tgTableInfoSnapshotComparePlanDAO.getOne(lam);
            if (Objects.isNull(one)) {
                one = new TgTableInfoSnapshotComparePlan();
                one.setCreateTime(new Date());
            }
        }
        one.setTableId(request.getTableId());
        one.setOldVersionId(request.getOldVersionId());
        one.setCreator(SecurityUtils.getUserId());
        one.setUpdateTime(new Date());
        tgTableInfoSnapshotComparePlanDAO.saveOrUpdate(one);
        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> deleteComparePlan(Long planId) {
        tgTableInfoSnapshotComparePlanDAO.removeById(planId);
        return AjaxResult.succeed();
    }

    /**
     *
     */
    @Override
    public AjaxResult<List<FlowAssetsPageDTO>> pageQueryAssets(TablePushDetailPageRequest request) {
        if (Objects.isNull(request.getTableId()) || Objects.isNull(request.getVersion())) {
            return AjaxResult.error("参数缺失");
        }
        List<AssetsWideUpgradeTrigger> triggers = assetsWideUpgradeTriggerDAO.lambdaQuery()
                .select(AssetsWideUpgradeTrigger::getAssetsId, AssetsWideUpgradeTrigger::getApplyId)
                .eq(AssetsWideUpgradeTrigger::getTableId, request.getTableId())
                .eq(AssetsWideUpgradeTrigger::getActVersion, request.getVersion())
                .list();

        List<Long> applyIds = Lambda.buildList(triggers, AssetsWideUpgradeTrigger::getApplyId);
//        Map<Long, AssetsWideUpgradeTrigger> assetsIds = Lambda.buildMap(triggers, AssetsWideUpgradeTrigger::getAssetsId);
//        List<UserDataAssets> lastList = userDataAssetsDAO.lambdaQuery()
//                .in(UserDataAssets::getId, assetsIds.keySet())
//                .and(v -> v.apply("concat(base_table_id,'#', base_version) in ('" + request.getTableId() + "#" + request.getVersion() + "')"))
//                .list();
//        List<UserDataAssetsSnapshot> snapList = userDataAssetsSnapshotDAO.lambdaQuery()
//                .in(UserDataAssetsSnapshot::getAssetsId, assetsIds.keySet())
//                .and(v -> v.apply("concat(base_table_id,'#', base_version) in ('" + request.getTableId() + "#" + request.getVersion() + "')"))
//                .list();
//        List<Long> applyIds = Stream.of(lastList, snapList).filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream)
//                .map(UserDataAssets::getSrcApplicationId).collect(Collectors.toList());
        return applicationService.pageQueryRelateApply(request, applyIds);
    }

    @Override
    public AjaxResult<TableComparePlanVO> comparePlanDetail(Long tableId) {
        LambdaQueryWrapper<TgTableInfoSnapshotComparePlan> lam = new LambdaQueryWrapper<>();
        lam.eq(TgTableInfoSnapshotComparePlan::getTableId, tableId);
        TgTableInfoSnapshotComparePlan one = tgTableInfoSnapshotComparePlanDAO.getOne(lam);
        if (Objects.isNull(one)) {
            return AjaxResult.success(new TableComparePlanVO());
        }
        TableComparePlanVO r = new TableComparePlanVO();
        BeanUtils.copyProperties(one, r);
        return AjaxResult.success(r);
    }

    @Override
    public List<AssetsWideUpgradeTrigger> queryByBizIds(List<Long> bizIds) {
        if (CollectionUtils.isEmpty(bizIds)) {
            return Collections.emptyList();
        }
        return assetsWideUpgradeTriggerDAO.lambdaQuery().in(AssetsWideUpgradeTrigger::getBizId
                , bizIds).list();
    }

    /**
     * 触发底表比对计划[不抛出异常/不影响主流程]
     *
     * @param tableId 表id
     */
    private void triggerDiffPlan(Long tableId) {
        try {
            AjaxResult<TableComparePlanVO> re = comparePlanDetail(tableId);
            if (re.isSuccess() && Objects.nonNull(re.getData())) {
                TableComparePlanVO data = re.getData();

                TableDiffRequest request = new TableDiffRequest();
                request.setTableId(tableId);
                request.setOldVersionId(data.getOldVersionId());
                request.setPlanId(data.getId());
                calculateDiff(request);
            }
        } catch (Exception e) {
            log.info("触发任务失败:{}", e.getMessage());
        }
    }

    /**
     * 构建结果
     *
     * @param taskMap 详细信息集合
     * @param i       每个元素
     * @param lastOne 最后一个
     * @return 比对结果信息
     */
    private TableInfoCompareTaskVO buildCompareVo(Map<Long, List<TgTableInfoSnapshotCompareDetail>> taskMap, TgTableInfoSnapshotCompare i, TgTableInfoSnapshotCompare lastOne) {
        TableInfoCompareTaskVO e = new TableInfoCompareTaskVO();
        e.setId(i.getId());
        e.setTableId(i.getTableId());
        e.setNewVersionId(i.getNewVersionId());
        e.setNewVersion(i.getNewVersion());
        e.setNewPeriod(i.getNewPeriod());
        e.setNewVersionPeriod(i.getNewVersionPeriod());
        e.setOldVersionId(i.getOldVersionId());
        e.setOldPeriod(i.getOldPeriod());
        e.setOldVersion(i.getOldVersion());
        e.setOldVersionPeriod(i.getOldVersionPeriod());
        e.setState(i.getState());
        e.setResultState(i.getResultState());
        e.setFailReason(i.getFailReason());
        e.setCreateTime(i.getCreateTime());
        e.setFinishTime(i.getFinishTime());
        List<TgTableInfoSnapshotCompareDetail> details = taskMap.get(i.getId());
        if (CollectionUtils.isNotEmpty(details)) {
            e.setResultTableNames(details.stream().map(TgTableInfoSnapshotCompareDetail::getTableName).collect(Collectors.toSet()));
        }
        e.setCreator(i.getCreator());
        e.setUpdateTime(i.getUpdateTime());
        if (Objects.nonNull(lastOne)) {
            e.setLatest(Objects.equals(i.getId(), lastOne.getId()));

        }
        return e;
    }

    /**
     * 查询比对详细结果
     *
     * @param taskIds 任务编号
     * @return 比对详细结果
     */
    private List<TgTableInfoSnapshotCompareDetail> queryCompareDetails(List<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<TgTableInfoSnapshotCompareDetail> lam = new LambdaQueryWrapper<>();
        lam.in(TgTableInfoSnapshotCompareDetail::getCompareId, taskIds);
        lam.in(TgTableInfoSnapshotCompareDetail::getCategory, Arrays.asList(TableInfoSnapshotCompareDetailCategory.DIFF_WIDE.getType(), TableInfoSnapshotCompareDetailCategory.DIFF_DETAIL.getType()));
        return tgTableInfoSnapshotCompareDetailDAO.list(lam);
    }
}
