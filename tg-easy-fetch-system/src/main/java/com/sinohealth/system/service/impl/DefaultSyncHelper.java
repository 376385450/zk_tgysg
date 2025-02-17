package com.sinohealth.system.service.impl;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.uuid.UUID;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.ck.constant.CkClusterType;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;
import com.sinohealth.system.biz.dataassets.dto.request.DataPreviewRequest;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.dao.ApplicationDataUpdateRecordDAO;
import com.sinohealth.system.dao.TgTableApplicationMappingInfoDAO;
import com.sinohealth.system.domain.ApplicationDataUpdateRecord;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.ckpg.CkPgJavaDataType;
import com.sinohealth.system.domain.ckpg.CustomerCKProperties;
import com.sinohealth.system.domain.ckpg.PostgresqlProperties;
import com.sinohealth.system.domain.ckpg.SelfCKProperties;
import com.sinohealth.system.domain.constant.SyncTargetType;
import com.sinohealth.system.domain.constant.SyncTriggerType;
import com.sinohealth.system.domain.constant.UpdateRecordStateType;
import com.sinohealth.system.domain.vo.TgTableApplicationMappingInfo;
import com.sinohealth.system.dto.ApplicationDataDto;
import com.sinohealth.system.dto.TgSyncTask;
import com.sinohealth.system.dto.application.deliver.update.UpdateRecordStatusParam;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgCkCustomerProviderMapper;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.mapper.TgSyncTaskMapper;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.SyncHelper;
import com.sinohealth.system.util.ApplicationSqlUtil;
import com.sinohealth.system.util.TgDataSourceUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Rudolph
 * @Date 2022-07-26 14:17
 * @Desc 用来分发 CK 的数据表到 内网或者外网的CK中
 */
@Service
@Slf4j
public class DefaultSyncHelper implements SyncHelper {

    @Autowired
    TgApplicationInfoMapper tgApplicationInfoMapper;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;

    @Autowired
    TgSyncTaskMapper tgSyncTaskMapper;
    @Resource
    private TgCkCustomerProviderMapper customerProviderMapper;
    @Autowired
    TgCkProviderMapper tgCkProviderMapper;
    @Autowired
    TgTableApplicationMappingInfoDAO tgTableApplicationMappingInfoDAO;
    @Autowired
    private ApplicationDataUpdateRecordDAO dataUpdateRecordDAO;

    @Autowired
    IApplicationService applicationService;

    @Autowired
    PostgresqlProperties postgresqlProperties;
    @Resource
    CustomerCKProperties customerCKProperties;
    @Resource
    SelfCKProperties selfCKProperties;
    @Autowired
    private AppProperties appProperties;

    @Resource
    @Qualifier(ThreadPoolType.SYNC_CK)
    private ThreadPoolTaskExecutor pool;

    @Autowired
    private CKClusterAdapter ckClusterAdapter;

    @Override
    public void updateApplicationWhichNeed2Update() {
        List<String> tablesThatNeed2Update = tgCkProviderMapper.selectTablesThatNeed2Update();
        for (String tn : tablesThatNeed2Update) {
            tgApplicationInfoMapper.updateNeedSyncTagByTableName(tn);
            tgCkProviderMapper.removeNeed2UpdateMessage(tn);
            tgCkProviderMapper.insertUpdateSuccessMessage(tn);
        }
    }

//    @Override
    public void syncDispatchTask() {
        for (; ; ) {
            // 抓取同步任务
            TgSyncTask tgSyncTask = tgSyncTaskMapper.selectTaskNeed2Sync();
            Date syncStartDate = DateUtils.getNowDate();
            UserDataAssets assets = new UserDataAssets();
            try {
                // 如果同步任务全部执行完毕, 退出循环
                if (null == tgSyncTask) {
                    break;
                }

                // 执行同步任务

                assets = assets.selectById(tgSyncTask.getAssetsId());
                ApplicationDataDto applicationDataDto = applicationService.queryAssetsDataFromCk(tgSyncTask.getAssetsId(), new DataPreviewRequest() {{
                    setPageNum(1);
                    setPageSize(1);
                }}).getData();
                boolean isFinished = this.syncApplicationTableToCustomerDatasource(applicationDataDto, assets);
                if (isFinished) {
                    // 执行信息记录
                    Date syncEndDate = DateUtils.getNowDate();
                    tgSyncTask.setActualSyncTime(syncStartDate);
                    tgSyncTask.setActualSyncDoneTime(syncEndDate);
                    tgSyncTask.setSyncState(1);
                    tgSyncTask.updateById();
//                    assets.setApplyLastUpdateTime(DateUtils.getTime());
                    assets.setNeedSyncTag(CommonConstants.NOT_UPDATE_TASK);
                    assets.updateById();
                }
            } catch (Exception e) {
                // 执行失败任务记录, 继续运行下一个任务
                log.error("执行同步失败", e);
                tgSyncTask.setActualSyncTime(syncStartDate);
                tgSyncTask.setSyncComment(e.getMessage());
                tgSyncTask.setSyncState(2);
                tgSyncTask.updateById();
                if (Objects.nonNull(assets.getId())) {
                    assets.setNeedSyncTag(CommonConstants.NOT_UPDATE_TASK);
                    assets.updateById();
                }
            }
        }
        // 更新申请是否需要标志
        updateApplicationWhichNeed2Update();
    }

    @Override
    public boolean syncApplicationTableToCustomerDatasource(Long assetsId, Integer version, Long userId) {
        UserDataAssets dataAssets;
        if (Objects.nonNull(version)) {
            dataAssets = userDataAssetsSnapshotDAO.queryByAssetsId(assetsId, version);
        } else {
            dataAssets = userDataAssetsDAO.getById(assetsId);
        }

        DataPreviewRequest request = DataPreviewRequest.buildForHead();

        AjaxResult<ApplicationDataDto> queryResult = applicationService.queryAssetsDataFromCk(assetsId, request);
        ApplicationDataDto applicationDataDto = queryResult.getData();
        return this.syncApplicationTableToCustomerDatasource(applicationDataDto, dataAssets, userId);
    }

    @Override
    public void asyncApplicationTableToCustomerDatasource(Long assetsId, Integer version) {
        log.info("assetsId={}", assetsId);
        Long userId = SecurityUtils.getUserId();
        pool.execute(() -> this.syncApplicationTableToCustomerDatasource(assetsId, version, userId));
    }

    @Override
    public boolean syncApplicationTableToCustomerDatasource(ApplicationDataDto applicationDataDto, UserDataAssets applicationInfo) {
        Long userId = SecurityUtils.getUserId();
        return this.syncApplicationTableToCustomerDatasource(applicationDataDto, applicationInfo, userId);
    }

    @Override
    public boolean syncApplicationTableToCustomerDatasource(ApplicationDataDto applicationDataDto,
                                                            UserDataAssets assets, Long userId) {
        StopWatch watch = new StopWatch();
        watch.start("已有");
        Long assetsId = assets.getId();
        log.info("开始同步: assetsId={} userId={}", assetsId, userId);
        ApplicationDataUpdateRecord existRecord = dataUpdateRecordDAO.querySyncApplication(UpdateRecordStatusParam.builder()
                .assetId(assetsId)
                .version(assets.getVersion())
                .syncTarget(SyncTargetType.CUSTOMER_DS)
                .updateStates(Arrays.asList(UpdateRecordStateType.UPDATING, UpdateRecordStateType.WAIT_UPDATE))
                .build());
        if (Objects.nonNull(existRecord)) {
            log.warn("已有进行中的同步任务: updateRecord={}", existRecord);
            return false;
        }

        String tableName;
        String ckQuerySql = assets.getAssetsSql();
        TgTableApplicationMappingInfo mappingInfo = tgTableApplicationMappingInfoDAO.getByAssetsId(assetsId);
        watch.stop();

        if (Objects.nonNull(mappingInfo)) {
            watch.start("删除分布式表");
            tableName = mappingInfo.getDataTableName();
            String dropSQL = "DROP TABLE IF EXISTS " + tableName + " ON CLUSTER " + CkClusterType.DEFAULT;
            customerProviderMapper.createTableAccordingApplication(dropSQL);
            log.info("dropSQL={}", dropSQL);
            watch.stop();

            watch.start("删除本地表");
            String dropLocalSQL = "DROP TABLE IF EXISTS " + tableName.replace("_shard", "_local")
                    + " ON CLUSTER " + CkClusterType.DEFAULT;
            customerProviderMapper.createTableAccordingApplication(dropLocalSQL);
            log.info("dropLocalSQL={}", dropLocalSQL);
            watch.stop();

            tgTableApplicationMappingInfoDAO.deleteById(mappingInfo.getId());
        } else {
            // 保持内外数据源的表名一致，所以不隔离内外的同步记录类型
            String tmpTableName = this.queryAlreadySyncTableName(assetsId, assets.getVersion());
            if (StringUtils.isNoneBlank(tmpTableName)) {
                tableName = tmpTableName;
            } else {
                tableName = "tg_" + assetsId + "_" + DateUtils.dateTimeNow() + "_shard";
            }
        }

        ApplicationDataUpdateRecord record = new ApplicationDataUpdateRecord();
        record.setAssetsId(assetsId);
        record.setVersion(assets.getVersion());
        record.setStartTime(new Date());
        record.setUpdateState(UpdateRecordStateType.UPDATING);
        record.setUpdaterId(userId);
        record.setTriggerType(SyncTriggerType.AUTO);
        record.setSyncTarget(SyncTargetType.CUSTOMER_DS);
        record.setCreateTime(new Date());
        record.setDataTableName(tableName);
        dataUpdateRecordDAO.save(record);

        try {
            watch.start("创建CK本地表");
            boolean wideTable = Objects.equals(assets.getTemplateType(), TemplateTypeEnum.wide_table.name());
            String targetDB = customerCKProperties.getDatabase();
            String localSQL = this.buildCkLocalSqlByTmpTable(applicationDataDto, tableName, assets.getAssetTableName(), ckQuerySql, targetDB, true);
            customerProviderMapper.createTableAccordingApplication(localSQL);
            watch.stop();

            watch.start("创建CK分布式表");
            customerProviderMapper.createTableAccordingApplication(this.buildCkShardSql(targetDB, tableName, CkClusterType.DEFAULT));
            watch.stop();

            // remote 方式迁移数据
            Pair<String, String> valPair = TgDataSourceUtil.parseHostAndDb(customerCKProperties.getUrl());
            String syncFmt = "INSERT INTO function remote('%s', '%s', '%s', '%s') %s";
            String syncSql = String.format(syncFmt, valPair.getFirst(), valPair.getSecond() + "." + tableName,
                    customerCKProperties.getUsername(), customerCKProperties.getPassword(), ckQuerySql);
            log.info("sync: sql={}", syncSql);

            watch.start("sync");
            tgCkProviderMapper.syncToRemoteTable(syncSql);
            watch.stop();

            TgTableApplicationMappingInfo mapping = new TgTableApplicationMappingInfo();
            mapping.setAssetsId(assetsId);
            mapping.setTableName(assets.getAllTableNames());
            mapping.setDataTableName(tableName);
            mapping.setDateUpdateTime(new Date());
            Long dataTotal = assets.getDataTotal();
            mapping.setDataVolume(dataTotal);
            tgTableApplicationMappingInfoDAO.save(mapping);
            log.info("save mapping {}", mapping);

            record.setUpdateCount(dataTotal);
            record.setFinishTime(new Date());
            record.setUpdateState(UpdateRecordStateType.SUCCESS);
            dataUpdateRecordDAO.updateById(record);
        } catch (Exception e) {
            log.error("", e);
            record.setUpdateState(UpdateRecordStateType.FAILED);
            record.setCause(e.toString());
            dataUpdateRecordDAO.updateById(record);
        }

        log.info("SYNC CUSTOMER {}", watch.prettyPrint());
        return true;
    }

    /**
     * 不管内网先做快照，还是外网先做快照，用最先成功的那份表的表名
     */
    private String queryAlreadySyncTableName(Long assetsId, Integer version) {
        String tableName = null;
        ApplicationDataUpdateRecord lastSuccessRecord = dataUpdateRecordDAO.querySyncApplication(UpdateRecordStatusParam.builder()
                .assetId(assetsId)
                .version(version)
                .updateState(UpdateRecordStateType.SUCCESS)
                .build());
        if (Objects.nonNull(lastSuccessRecord)) {
            tableName = lastSuccessRecord.getDataTableName();
        }
        return tableName;
    }

    @Override
    public boolean syncApplicationTableToSelfDatasourceBench(int no) {
        int i = new SecureRandom().nextInt();
        if (i < 0) {
            i *= -1;
        }
        i %= 9999999;

        // 压测 每次使用不同的表名
        String time = DateUtils.dateTimeNow();
        String localTable = "tg_101_" + time + "_" + i + "_local";
        String shardTable = "tg_101_" + time + "_" + i + "_shard";
        String createSql = "CREATE TABLE tgysg." + localTable + " ON cluster " + CkClusterType.DEFAULT + " \n" +
                "(\n" +
                "    `t_1_sz_phl_avg` Float64,\n" +
                "    `t_1_jq_phl_avg` Float64,\n" +
                "    `t_1_jx_max` String,\n" +
                "    `t_1_ddu_sum` Nullable(Decimal(38, 2)),\n" +
                "    `t_1_fd_xsl_sum` Decimal(38, 2),\n" +
                "    `t_1_tz_fdxse_sum` Decimal(38, 2),\n" +
                "    `t_1_sample_xse_sum` Decimal(38, 2),\n" +
                "    `t_1_otc_rx_min` String,\n" +
                "    `t_1_zone_name` String,\n" +
                "    `t_1_period` Date,\n" +
                "    `t_1_province` String,\n" +
                "    `t_1_city_co_name` String,\n" +
                "    `t_1_std_id` Int32,\n" +
                "    `t_1_zx` String,\n" +
                "    `t_1_dx` String,\n" +
                "    `t_1_prodcode` String,\n" +
                "    `t_1_sort1` String,\n" +
                "    `t_1_sort2` String,\n" +
                "    `t_1_sort3` String,\n" +
                "    `t_1_sort4` String,\n" +
                "    `t_1_tym` String,\n" +
                "    `t_1_brand` String,\n" +
                "    `t_1_spm` String,\n" +
                "    `t_1_pm_all` String,\n" +
                "    `t_1_pm` String,\n" +
                "    `t_1_cj` String,\n" +
                "    `t_1_gg` String,\n" +
                "    `t_1_company_rights` String,\n" +
                "    `t_1_short_cj` String,\n" +
                "    `t_1_short_brand` String,\n" +
                "    `t_1_tv` Decimal(18, 2),\n" +
                "    `t_1_vpd` Nullable(Decimal(18, 2)),\n" +
                "    `t_1_otherstag` Int32,\n" +
                "    `t_1_sc_old_label` String,\n" +
                "    `t_1_std_cwid` Int32\n" +
                ")\n" +
                "ENGINE = ReplicatedMergeTree('/clickhouse/table/{shard}/tgysg/" + localTable + "', '{replica}')\n" +
                "ORDER BY t_1_period\n" +
                "SETTINGS index_granularity = 8192";
        log.info("createSql={}", createSql);
//        ckHelper.executeDDL(createSql);

        ckClusterAdapter.executeAll(createSql);
        String shardSql = "CREATE TABLE IF NOT EXISTS tgysg." + shardTable + " ON cluster " + CkClusterType.DEFAULT + "\n" +
                "as tgysg." + localTable + "\n" +
                "ENGINE = Distributed(" + CkClusterType.DEFAULT + ", tgysg, " + localTable + ", rand())";
        // tgCkProviderMapper.createTableAccordingApplication(shardSql);
//        ckHelper.executeDDL(shardSql);
        ckClusterAdapter.executeAll(shardSql);
        log.info("shardSql={}", shardSql);

        String cond = "";
        if (no % 2 == 0) {
            cond = "IN ('P026', 'P028')";
        } else {
            cond = "IN ('P024','P025', 'P027')";
        }
        String insertSql = "insert into " + shardTable + " SELECT t_1_sz_phl_avg,\n" +
                "       t_1_jq_phl_avg,\n" +
                "       t_1_jx_max,\n" +
                "       t_1_ddu_sum,\n" +
                "       t_1_fd_xsl_sum,\n" +
                "       t_1_tz_fdxse_sum,\n" +
                "       t_1_sample_xse_sum,\n" +
                "       t_1_otc_rx_min,\n" +
                "       t_1_zone_name,\n" +
                "       t_1_period,\n" +
                "       t_1_province,\n" +
                "       t_1_city_co_name,\n" +
                "       t_1_std_id,\n" +
                "       t_1_zx,\n" +
                "       t_1_dx,\n" +
                "       t_1_prodcode,\n" +
                "       t_1_sort1,\n" +
                "       t_1_sort2,\n" +
                "       t_1_sort3,\n" +
                "       t_1_sort4,\n" +
                "       t_1_tym,\n" +
                "       t_1_brand,\n" +
                "       t_1_spm,\n" +
                "       t_1_pm_all,\n" +
                "       t_1_pm,\n" +
                "       t_1_cj,\n" +
                "       t_1_gg,\n" +
                "       t_1_company_rights,\n" +
                "       t_1_short_cj,\n" +
                "       t_1_short_brand,\n" +
                "       t_1_tv,\n" +
                "       t_1_vpd,\n" +
                "       t_1_otherstag,\n" +
                "       t_1_sc_old_label,\n" +
                "       t_1_std_cwid\n" +
                "FROM (SELECT\n" +
                "             SUM(t_1.sample_xse)               t_1_sample_xse_sum,\n" +
                "             SUM(t_1.tz_fdxse)                 t_1_tz_fdxse_sum,\n" +
                "             SUM(t_1.fd_xsl)                   t_1_fd_xsl_sum,\n" +
                "             AVG(t_1.sz_phl)                   t_1_sz_phl_avg,\n" +
                "             AVG(t_1.jq_phl)                   t_1_jq_phl_avg,\n" +
                "             SUM(t_1.ddu)                      t_1_ddu_sum,\n" +
                "             MAX(t_1.jx)                       t_1_jx_max,\n" +
                "             MIN(t_1.otc_rx)                   t_1_otc_rx_min,\n" +
                "             t_1.zone_name                     t_1_zone_name,\n" +
                "             t_1.period                        t_1_period,\n" +
                "             t_1.province                      t_1_province,\n" +
                "             t_1.city_co_name                  t_1_city_co_name,\n" +
                "             t_1.std_id                        t_1_std_id,\n" +
                "             t_1.zx                            t_1_zx,\n" +
                "             t_1.dx                            t_1_dx,\n" +
                "             t_1.prodcode                      t_1_prodcode,\n" +
                "             t_1.sort1                         t_1_sort1,\n" +
                "             t_1.sort2                         t_1_sort2,\n" +
                "             t_1.sort3                         t_1_sort3,\n" +
                "             t_1.sort4                         t_1_sort4,\n" +
                "             t_1.tym                           t_1_tym,\n" +
                "             t_1.brand                         t_1_brand,\n" +
                "             t_1.spm                           t_1_spm,\n" +
                "             t_1.pm_all                        t_1_pm_all,\n" +
                "             t_1.pm                            t_1_pm,\n" +
                "             t_1.cj                            t_1_cj,\n" +
                "             t_1.gg                            t_1_gg,\n" +
                "             t_1.company_rights                t_1_company_rights,\n" +
                "             t_1.short_cj                      t_1_short_cj,\n" +
                "             t_1.short_brand                   t_1_short_brand,\n" +
                "             t_1.tv                            t_1_tv,\n" +
                "             t_1.vpd                           t_1_vpd,\n" +
                "             t_1.otherstag                     t_1_otherstag,\n" +
                "             t_1.sc_old_label                  t_1_sc_old_label,\n" +
                "             t_1.std_cwid                      t_1_std_cwid\n" +
                "      FROM tgysg.cmh_sku_shard t_1\n" +
                "      WHERE (t_1.zone_name IN ('全国', '城市', '省份') and (t_1.prodcode " + cond + "))\n" +
                "      GROUP BY t_1.period, t_1.brand, t_1.zx, t_1.pm, t_1.tv, t_1.std_id, t_1.zone_name, t_1.spm, t_1.prodcode,\n" +
                "               t_1.pm_all, t_1.otherstag, t_1.vpd, t_1.dx, t_1.short_cj, t_1.sort1, t_1.province, t_1.sort4,\n" +
                "               t_1.city_co_name, t_1.sort2, t_1.sort3, t_1.gg, t_1.cj, t_1.sc_old_label, t_1.std_cwid, t_1.tym,\n" +
                "               t_1.short_brand, t_1.company_rights) template\n" +
                "WHERE template.t_1_prodcode  " + cond;
        log.info("insertSql={}", insertSql);
//        ckHelper.executeDDL(insertSql);
        ckClusterAdapter.executeAll(insertSql);
        return true;
    }

    @Override
    public boolean syncApplicationTableToSelfDatasourceBench(Long assetsId, Long userId) {
        String traceId = UUID.randomUUID().toString();
        log.info("[{}] sync: applyId={} userId={}", traceId, assetsId, userId);

        UserDataAssets assets = new UserDataAssets().selectById(assetsId);
        ApplicationDataDto applicationDataDto = applicationService.queryAssetsDataFromCk(assetsId, DataPreviewRequest.buildForHead()).getData();

        String ckQuerySql = assets.getAssetsSql();
        int i = new SecureRandom().nextInt();
        if (i < 0) {
            i *= -1;
        }
        i %= 9999999;

        // 压测 每次使用不同的表名
        String tableName = "tg_" + assets.getId() + "_" + DateUtils.dateTimeNow() + "_" + i + "_shard";

        ApplicationDataUpdateRecord record = new ApplicationDataUpdateRecord();
        record.setAssetsId(assetsId);
        record.setStartTime(new Date());
        record.setUpdateState(UpdateRecordStateType.UPDATING);
        record.setUpdaterId(userId);
        record.setTriggerType(SyncTriggerType.AUTO);
        record.setSyncTarget(SyncTargetType.SELF_DS);
        record.setCreateTime(new Date());
        record.setDataTableName(tableName);
        record.setVersion(assets.getVersion());
        dataUpdateRecordDAO.save(record);

        try {
            // 创建CK本地表
            boolean wide = Objects.equals(assets.getTemplateType(), TemplateTypeEnum.wide_table.name());
            String targetDB = selfCKProperties.getDatabase();
            String localSQL = this.buildCkLocalSqlByTmpTable(applicationDataDto, tableName, assets.getAssetTableName(), ckQuerySql, targetDB, false);
            log.info("[{}] localSQL={}", traceId, localSQL);
//            ckHelper.executeDDL(localSQL);
            ckClusterAdapter.executeAll(localSQL);

            // 创建CK分布式表
            String shardSql = this.buildCkShardSql(targetDB, tableName, CkClusterType.DEFAULT);
            log.info("[{}] shardSql={}", traceId, shardSql);
//            ckHelper.executeDDL(shardSql);
            ckClusterAdapter.executeAll(shardSql);

            // insert 数据
            String syncFmt = "INSERT INTO %s.%s %s";
            String syncSql = String.format(syncFmt, targetDB, tableName, ckQuerySql);
            log.info("[{}]sync: syncSql={}", traceId, syncSql);
            tgCkProviderMapper.syncToRemoteTable(syncSql);

            Long count = tgCkProviderMapper.countTable(tableName);
            record.setUpdateCount(count);
            record.setUpdateState(UpdateRecordStateType.SUCCESS);
            record.setFinishTime(new Date());
            dataUpdateRecordDAO.updateById(record);
            log.info("[{}]sync: finish sync", traceId);
        } catch (Exception e) {
            log.error("[{}]", traceId, e);
            record.setUpdateState(UpdateRecordStateType.FAILED);
            dataUpdateRecordDAO.updateById(record);
        }

        return true;
    }

    @Override
    public boolean createLocalSnapshotTable(Long applyId, String tableName) {
        String traceId = StrUtil.randomAlpha(5);
        log.info("[{}] sync: applyId={} userId={}", traceId, applyId, tableName);

        try {
            AjaxResult<ApplicationDataDto> dataRes = applicationService.getApplicationDataFromCk(applyId,
                    DataPreviewRequest.buildForHead(), null);
            ApplicationDataDto applicationDataDto = dataRes.getData();
            if (!dataRes.isSuccess() || Objects.isNull(applicationDataDto)) {
                log.error("FAILED: dataRes={}", dataRes);
                return false;
            }

            TgApplicationInfo info = tgApplicationInfoMapper.selectOne(new QueryWrapper<TgApplicationInfo>().lambda()
                    .select(TgApplicationInfo::getAsql, TgApplicationInfo::getTailSql, TgApplicationInfo::getApplicantId)
                    .eq(TgApplicationInfo::getId, applyId)
            );

//            TgApplicationInfo info = TgApplicationInfo.newInstance().selectById(applyId);
            String ckQuerySql = info.getAsql();
            // 创建CK本地表
            String targetDB = selfCKProperties.getDatabase();
            tableName = tableName.replace("_shard", "_snap");
            String localSQL = this.buildCkSnapshotSqlByTmpTable(applicationDataDto, tableName, ckQuerySql);
            log.info("[{}] localSQL={}", traceId, localSQL);

            String syncFmt = "INSERT INTO %s.%s %s";
            String insertSQL = String.format(syncFmt, targetDB, tableName, ckQuerySql);
            // 长尾特殊处理
            if (StringUtils.isNotBlank(info.getTailSql())) {
                String tailInsertSQL = String.format(syncFmt, targetDB, tableName, info.getTailSql());
                log.info("[{}]sync: insertSQL={} tailSql={}", traceId, insertSQL, tailInsertSQL);
                ckClusterAdapter.createTable(info.getApplicantId(), tableName, this.replaceForNull(localSQL),
                        insertSQL, tailInsertSQL);
            } else {
                log.info("[{}]sync: insertSQL={}", traceId, insertSQL);
                ckClusterAdapter.createTable(info.getApplicantId(), tableName, localSQL, insertSQL, null);
            }
            log.info("[{}]sync: finish sync", traceId);
        } catch (Exception e) {
            log.error("[{}]", traceId, e);
            return false;
        }

        return true;
    }

    private String replaceForNull(String ddl) {
        ddl = ddl.replace("`Decimal", "` Decimal");
        while (StringUtils.contains(ddl, " Decimal(")) {
            log.info("replace once");
            ddl = ReUtil.replaceAll(ddl, " Decimal\\((\\d+),\\s(\\d+)\\) ", " Nullable(Decimal($1, $2)) ");
        }
        return ddl;
    }

    @Override
    public boolean pushAssetsTableForBI(Long assetsId, Integer version, Long userId) {
        String traceId = StrUtil.randomAlpha(5);
        log.info("[{}] sync: assetsId={} version={} userId={}", traceId, assetsId, version, userId);
        ApplicationDataUpdateRecord existRecord = dataUpdateRecordDAO.querySyncApplication(UpdateRecordStatusParam.builder()
                .assetId(assetsId)
                .version(version)
                .syncTarget(SyncTargetType.SELF_DS)
                .updateStates(Arrays.asList(UpdateRecordStateType.UPDATING, UpdateRecordStateType.WAIT_UPDATE))
                .build());
        if (Objects.nonNull(existRecord)) {
            log.warn("[{}]已有进行中的同步任务: updateRecord={}", traceId, existRecord);
            return false;
        }

        UserDataAssets assets = new UserDataAssets().selectById(assetsId);
        String assetsSQL;
        String assetsTableName;
        if (Objects.isNull(version) || Objects.equals(assets.getVersion(), version)) {
            assetsSQL = assets.getAssetsSql();
            assetsTableName = assets.getAssetTableName();
        } else {
            UserDataAssetsSnapshot snapshot = userDataAssetsSnapshotDAO.queryByAssetsId(assetsId, version);
            assetsSQL = snapshot.getAssetsSql();
            assetsTableName = snapshot.getAssetTableName();
        }
        DataPreviewRequest requestDTO = DataPreviewRequest.buildForHead();
        requestDTO.setVersion(version);
        ApplicationDataDto applicationDataDto = applicationService.queryAssetsDataFromCk(assetsId, requestDTO).getData();

        // 保持内外数据源的表名一致，所以不隔离内外的同步记录类型
        String tableName = this.queryAlreadySyncTableName(assetsId, assets.getVersion());
        if (Objects.nonNull(tableName)) {
            String dropSQL = "DROP TABLE IF EXISTS " + tableName + " ON CLUSTER " + CkClusterType.BI;
//            ckHelper.executeDDL(dropSQL);
            ckClusterAdapter.executeAll(dropSQL);

            String dropLocalSQL = "DROP TABLE IF EXISTS " + tableName.replace("_shard", "_local") + " ON CLUSTER " + CkClusterType.BI;
//            ckHelper.executeDDL(dropLocalSQL);
            ckClusterAdapter.executeAll(dropLocalSQL);
        } else {
            tableName = "tg_" + assets.getId() + "_" + DateUtils.dateTimeNow() + "_shard";
        }

        ApplicationDataUpdateRecord record = new ApplicationDataUpdateRecord();
        record.setAssetsId(assetsId);
        record.setStartTime(new Date());
        record.setUpdateState(UpdateRecordStateType.UPDATING);
        record.setUpdaterId(userId);
        record.setTriggerType(SyncTriggerType.AUTO);
        record.setSyncTarget(SyncTargetType.SELF_DS);
        record.setCreateTime(new Date());
        record.setDataTableName(tableName);
        record.setVersion(version);
        dataUpdateRecordDAO.save(record);

        try {
            // 创建CK本地表 创建CK分布式表
            String targetDB = selfCKProperties.getDatabase();
//            boolean wide = Objects.equals(assets.getTemplateType(), TemplateTypeEnum.wide_table.name());
            String localSQL = this.buildCkLocalSqlByTmpTable(applicationDataDto, tableName, assetsTableName,
                    assetsSQL, targetDB, false);
            log.info("[{}] localSQL={}", traceId, localSQL);
//            ckHelper.executeDDL(localSQL);
            ckClusterAdapter.executeAll(localSQL);

            String shardSql = this.buildCkShardSql(targetDB, tableName, CkClusterType.BI);
            log.info("[{}] shardSql={}", traceId, shardSql);
//            ckHelper.executeDDL(shardSql);
            ckClusterAdapter.executeAll(shardSql);

            // remote 方式迁移数据
            // 废弃的原因是BI用的是主CK库，没有划分库，只有对客户的才做了库的切分
//            Pair<String, String> valPair = TgDataSourceUtil.parseHostAndDb(selfCKProperties.getUrl());
//            String syncFmt = "INSERT INTO function remote('%s', '%s', '%s', '%s') %s";
//            String syncSql = String.format(syncFmt, valPair.getFirst(), valPair.getSecond() + "." + tableName,
//                    selfCKProperties.getUsername(), selfCKProperties.getPassword(), assetsSQL);
//            log.info("[{}]sync: syncSql={}", traceId, syncSql);
//            tgCkProviderMapper.syncToRemoteTable(syncSql);

            // 同库，所以直接insert
            String syncFmt = "INSERT INTO %s.%s %s";
            String syncSql = String.format(syncFmt, targetDB, tableName, assetsSQL);
            log.info("[{}]sync: syncSql={}", traceId, syncSql);
//            tgCkProviderMapper.syncToRemoteTable(syncSql);

            ckClusterAdapter.execute(assetsTableName, syncSql);

            // TODO 延迟统计
            Long count = tgCkProviderMapper.countTable(tableName);
            record.setUpdateCount(count);
            record.setUpdateState(UpdateRecordStateType.SUCCESS);
            record.setFinishTime(new Date());
            dataUpdateRecordDAO.updateById(record);
            log.info("[{}]sync: finish sync", traceId);
        } catch (Exception e) {
            log.error("[{}]", traceId, e);
            record.setUpdateState(UpdateRecordStateType.FAILED);
            record.setCause(e.toString());
            dataUpdateRecordDAO.updateById(record);
        }

        return true;
    }

    @Override
    public void asyncPushAssetsTableForBI(Long assetsId, Integer version) {
        Long userId = SecurityUtils.getUserId();
        pool.execute(() -> this.pushAssetsTableForBI(assetsId, version, userId));
    }

    private String getLocalTableName(String shardTableName) {
        return shardTableName.replace("_shard", "_local");
    }

    /**
     * 1. 通过提数申请构造出的复杂SQL create table tmp_table as (SQL) 创建一个临时表
     * 2. show create table tmp_table
     * 3. 处理第二步得到的SQL： 移除数据库名，添加集群动作标志, 追加字段注释，修改replica数据库目录
     *
     * @param custom true 客户表 使用复制表 false BI使用使用本地表
     * @see ApplicationServiceImpl#buildHeaders  headers 构造来源
     * @see ApplicationSqlUtil#buildAssetsTableName 表名规则
     */
    public String buildCkLocalSqlByTmpTable(ApplicationDataDto applicationDataDto, String tableName,
                                            String assetsTable, String querySQL, String database, boolean custom) {
        String localTableName = this.getLocalTableName(tableName);
        List<ApplicationDataDto.Header> headers = applicationDataDto.getHeader();

        Map<String, ApplicationDataDto.Header> headerMap = headers.stream()
                .collect(Collectors.toMap(ApplicationDataDto.Header::getFiledName, v -> v, (front, current) -> current));

        // Order
        // 优先选择日期类型的字段，如果没有就使用全字段（元组），需要注意通用表模型下的查询性能 通过二级索引优化
        String orderField = headers.stream()
                .filter(v -> Objects.equals(v.getDataType(), CkPgJavaDataType.Date.name())
                        || Objects.equals(v.getDataType(), CkPgJavaDataType.DateTime.name())).findAny()
                .map(ApplicationDataDto.Header::getFiledName)
                // https://clickhouse.com/docs/en/engines/table-engines/mergetree-family/mergetree
                .orElse(" tuple() ");

        String db = selfCKProperties.getDatabase();
        // 内层查询需要指定库，否则默认是default库 报错
        String result = ApplicationSqlUtil.appendDb(querySQL, db);

        // 追加库名，建表内的查询SQL，CK需要指定库名
        result = ReUtil.replaceAll(result, " JOIN (\\w+) t_", " JOIN " + db + ".$1 t_");

        String now = DateUtils.dateTimeNow();
        String tmpName = localTableName + now + "_tmp";

        String createSnapTmpSql = String.format("CREATE TABLE IF NOT EXISTS %s ENGINE = MergeTree() ORDER BY %s AS (%s LIMIT 0)",
                tmpName, orderField, result);
        String localSql = ckClusterAdapter.tryTemp(assetsTable, tmpName, createSnapTmpSql);

        // 1.8.5 BI不使用复制表,对客户的表才用复制表，降低ZK负担
        if (custom) {
            localSql = localSql.replace("MergeTree", String.format("ReplicatedMergeTree('/clickhouse/table/{shard}/%s/%s','{replica}')", db, tmpName));
        }
        localSql = localSql.replace(db + ".", "");
        // 集群内建local表
        localSql = ReUtil.replaceAll(localSql, "\\{shard\\}/(\\w+)/", "table/{shard}/" + database + "/");
        localSql = localSql.replace(tmpName, localTableName);
        localSql = localSql.replace("/" + localTableName, "/" + localTableName + "-" + now);

        return ApplicationSqlUtil.convertCkSqlByDruid(localSql, headerMap,
                custom ? CkClusterType.DEFAULT : CkClusterType.BI,
                selfCKProperties.getDatabase());
    }


    /**
     * 基于资产SQL创建临时表，从而得到建表语句
     *
     * @see DefaultSyncHelper#buildCkLocalSqlByTmpTable
     */
    public String buildCkSnapshotSqlByTmpTable(ApplicationDataDto applicationDataDto, String tableName, String querySQL) {
        List<ApplicationDataDto.Header> headers = applicationDataDto.getHeader();

        Map<String, ApplicationDataDto.Header> headerMap = headers.stream()
                .collect(Collectors.toMap(ApplicationDataDto.Header::getFiledName, v -> v, (front, current) -> current));

        // Order
        // 优先选择日期类型的字段，如果没有就使用全字段（元组），需要注意通用表模型下的查询性能 通过二级索引优化
        // https://clickhouse.com/docs/en/engines/table-engines/mergetree-family/mergetree
        String orderField = headers.stream()
                .filter(v -> Objects.equals(v.getDataType(), CkPgJavaDataType.Date.name())
                        || Objects.equals(v.getDataType(), CkPgJavaDataType.DateTime.name())).findAny()
                .map(ApplicationDataDto.Header::getFiledName)
                .orElse(" tuple() ");

        String db = selfCKProperties.getDatabase();
        // 内层查询需要指定库，否则默认是default库 报错
        String result = ApplicationSqlUtil.appendDb(querySQL, db);

        // 追加库名，建表内的查询SQL，CK需要指定库名
        result = ReUtil.replaceAll(result, " JOIN (\\w+) t_", " JOIN " + db + ".$1 t_");

        String now = DateUtils.dateTimeNow();
        String tmpName = tableName + now + "_tmp";

        //
        String createTmpSql = String.format("CREATE TABLE %s ENGINE = MergeTree() ORDER BY %s AS (%s LIMIT 0)",
                tmpName, orderField, result);

        String localSql = ckClusterAdapter.tryRandomTemp(tmpName, createTmpSql);

        localSql = localSql.replace(db + ".", "");
        localSql = localSql.replace(tmpName, tableName);
        localSql = localSql.replace("/" + tableName, "/" + tableName + "-" + now);

        return ApplicationSqlUtil.convertCkSqlByDruid(localSql, headerMap, null, selfCKProperties.getDatabase());
    }

//    private String convertCkSqlByDruid(String createSQL, Map<String, ApplicationDataDto.Header> headerMap, String cluster) {
//        int engineIdx = createSQL.indexOf("ENGINE");
//        String table = createSQL.substring(0, engineIdx);
//        String suffix = createSQL.substring(engineIdx);
//
//        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(table, JdbcConstants.CLICKHOUSE);
//        if (CollectionUtils.isEmpty(sqlStatements)) {
//            throw new RuntimeException("SQL解析错误");
//        }
//        SQLCreateTableStatement statement = (SQLCreateTableStatement) sqlStatements.get(0);
//        List<SQLTableElement> fields = statement.getTableElementList();
//        if (Objects.isNull(fields)) {
//            throw new RuntimeException("SELECT查询字段为空");
//        }
//        StringBuilder res = new StringBuilder();
//        for (SQLTableElement field : fields) {
//            SQLColumnDefinition def = (SQLColumnDefinition) field;
//            String columnName = def.getName().getSimpleName();
//            String originName = columnName.replace("`", "");
//
//            ApplicationDataDto.Header alias = headerMap.get(originName);
//            res.append(def.getName()).append(def.getDataType()).append(" COMMENT '")
//                    .append(Optional.ofNullable(alias).map(ApplicationDataDto.Header::getFiledAlias).orElse("")).append("',");
//        }
//        String tableNameF = statement.getTableSource().getName().getSimpleName().replace(selfCKProperties.getDatabase() + ".", "");
//        String field = res.substring(0, res.length() - 1);
//        String clusterStr = Optional.ofNullable(cluster).map(v -> " ON cluster " + v).orElse("");
//        return " CREATE TABLE " + tableNameF + clusterStr + " (" + field + ") " + suffix;
//    }

    /**
     * CCJSqlParserUtil 无法支持 Nullable(Int32)
     */
    @Deprecated
    private String parseSql(String localSql, Map<String, ApplicationDataDto.Header> headerMap) {
        try {
            // 语法解析，补入注释
            int engineIdx = localSql.indexOf("ENGINE");
            String table = localSql.substring(0, engineIdx);
            String suffix = localSql.substring(engineIdx);

            CreateTable parse = (CreateTable) CCJSqlParserUtil.parse(table);

            StringBuilder res = new StringBuilder();
            List<ColumnDefinition> columnDefinitions = parse.getColumnDefinitions();
            for (ColumnDefinition columnDefinition : columnDefinitions) {
                String columnName = columnDefinition.getColumnName();
                String originName = columnName.replace("`", "");
                ApplicationDataDto.Header alias = headerMap.get(originName);
                res.append(columnDefinition.getColumnName()).append(columnDefinition.getColDataType())
                        .append(" COMMENT '").append(Optional.ofNullable(alias).map(ApplicationDataDto.Header::getFiledAlias)
                                .orElse("")).append("',");
            }

            String tableNameF = parse.getTable().getName().replace(selfCKProperties.getDatabase() + ".", "");
            String field = res.substring(0, res.length() - 1);

            return " CREATE TABLE " + tableNameF + " ON cluster " + CkClusterType.DEFAULT + " (" + field + ") " + suffix;
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildCkShardSql(String database, String tableName, String cluster) {
        String localTableName = getLocalTableName(tableName);
        String sql = "CREATE TABLE " + database + "." + tableName
                + " ON CLUSTER " + cluster + " AS " + database + "." + localTableName
                + " ENGINE = Distributed(" + cluster + ", " + database + ", " + localTableName + ", rand())";
//        log.info("create shard customer ck table: sql={}", sql);
        return sql;
    }


    private void generateCkCreateSql(ApplicationDataDto applicationDataDto, StringBuilder ckCreateTableSqlBuilder, String tableName) {
        ckCreateTableSqlBuilder.append("CREATE TABLE ")
                .append(tableName).append(" ON CLUSTER " + CkClusterType.DEFAULT + " ( ");
        applicationDataDto.getHeader().forEach(f -> {
            String dataType = f.getDataType();
            String filedName = f.getFiledName();
            ckCreateTableSqlBuilder.append(filedName).append(" ");
            CkPgJavaDataType ckPgJavaDataType = CkPgJavaDataType.fromTypeString(dataType);
            if ("DECIMAL".equalsIgnoreCase(ckPgJavaDataType.getPgSQLType())) {
                ckCreateTableSqlBuilder.append(dataType)
                        .append("(").append(ckPgJavaDataType.getDefaultPrecision()).append(",").append(ckPgJavaDataType.getDefaultScale()).append(")").append(",");
            } else {
                ckCreateTableSqlBuilder.append(dataType).append(",");
            }
        });
        ckCreateTableSqlBuilder.deleteCharAt(ckCreateTableSqlBuilder.length() - 1);
        ckCreateTableSqlBuilder.append(" ) ");
        String engineFormatInfo = "ENGINE = PostgreSQL('%s', '%s', '%s','%s', '%s');";

        Pair<String, String> valPair = TgDataSourceUtil.parseHostAndDb(postgresqlProperties.getUrl());
        String engineInfo = String.format(engineFormatInfo, valPair.getFirst(), valPair.getSecond(), tableName,
                postgresqlProperties.getUsername(), postgresqlProperties.getPassword());
        ckCreateTableSqlBuilder.append(engineInfo);
    }

    private void generatePgCreateSql(ApplicationDataDto applicationDataDto, StringBuilder pgCreateTableSqlBuilder, String tableName) {
        pgCreateTableSqlBuilder.append("CREATE TABLE ")
                .append(tableName).append(" ( ").append("tg_init_id SERIAL,");
        applicationDataDto.getHeader().stream().forEach(f -> {
            String dataType = f.getDataType();
            String filedName = f.getFiledName();
            pgCreateTableSqlBuilder.append(filedName).append(" ");
            CkPgJavaDataType ckPgJavaDataType = CkPgJavaDataType.fromTypeString(dataType);
            if ("DECIMAL".equalsIgnoreCase(ckPgJavaDataType.getPgSQLType())) {
                pgCreateTableSqlBuilder.append(CkPgJavaDataType.fromTypeString(dataType).getPgSQLType())
                        .append("(").append(ckPgJavaDataType.getDefaultPrecision()).append(",").append(ckPgJavaDataType.getDefaultScale()).append(")").append(",");
            } else {
                pgCreateTableSqlBuilder.append(CkPgJavaDataType.fromTypeString(dataType).getPgSQLType()).append(",");
            }

        });
        pgCreateTableSqlBuilder.append("CONSTRAINT " + tableName + "_pkey PRIMARY KEY (tg_init_id));");
        pgCreateTableSqlBuilder.append("COMMENT ON COLUMN " + tableName + ".tg_init_id IS '自增主键';");
        applicationDataDto.getHeader().stream().forEach(f -> {
            pgCreateTableSqlBuilder.append("COMMENT ON COLUMN " + tableName + "." + f.getFiledName() + " IS '" + f.getFiledAlias() + "';");
        });
    }
}
