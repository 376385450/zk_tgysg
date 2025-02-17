package com.sinohealth.system.job.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.annotation.RegisterCronMethod;
import com.sinohealth.sca.base.basic.util.DateUtils;
import com.sinohealth.system.biz.ck.constant.CkClusterType;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.service.impl.AssetsUpgradeTriggerServiceImpl;
import com.sinohealth.system.domain.ApplicationDataUpdateRecord;
import com.sinohealth.system.domain.constant.SyncTargetType;
import com.sinohealth.system.domain.constant.UpdateRecordStateType;
import com.sinohealth.system.dto.TgCogradientDetailDto;
import com.sinohealth.system.mapper.ApplicationDataUpdateRecordMapper;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.service.IntergrateProcessDefService;
import com.sinohealth.system.service.SyncHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-12-20 16:00
 */
@Slf4j
@Component
public class SnapshotTableSyncJob {

    @Autowired
    private TgApplicationInfoMapper tgApplicationInfoMapper;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private ApplicationDataUpdateRecordMapper applicationDataUpdateRecordMapper;
    @Autowired
    private TgCkProviderMapper tgCkProviderMapper;
    @Autowired
    private IntergrateProcessDefService intergrateProcessDefService;
    @Autowired
    private SyncHelper syncHelper;

    private ThreadPoolExecutor pool;

    /**
     * 检查并重建 晚于底表数据的快照表
     * 1. 查询最近更新的底表
     * 2. 依据底表查询出所有关联的提数申请
     * 3. 重建提数申请对应的快照表（设计限流策略）
     *
     * @see AssetsUpgradeTriggerServiceImpl#scheduleCreateWideTableTask() 新逻辑
     * @see AssetsUpgradeTriggerServiceImpl#scheduleWideTable()
     */
    @Deprecated
    @RegisterCronMethod
    public void syncOutDateTableData() throws InterruptedException {
        this.syncOutDateTableData(1);
    }

    public void cancelSync() {
        pool.shutdownNow();
    }

    public void syncOutDateTableData(int count) throws InterruptedException {
        pool = new ThreadPoolExecutor(count, count, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
        Semaphore semaphore = new Semaphore(count);

        // 最后一次同步成功的数据
        IPage<TgCogradientDetailDto> syncDetails = intergrateProcessDefService
                .querySyncDetail(null, null, 7, 1, 100);
        List<TgCogradientDetailDto> records = syncDetails.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return;
        }

        Date now = new Date();
        Date passDay = com.sinohealth.common.utils.DateUtils.addDays(now, -3);
        Set<Long> tableIds = records.stream().filter(v -> {
            Date date = com.sinohealth.common.utils.DateUtils.parseTimezoneDate(v.getEndTime());
            return Optional.ofNullable(date).map(passDay::before).orElse(false);
        }).map(TgCogradientDetailDto::getTableId).collect(Collectors.toSet());

        List<UserDataAssets> infoList = userDataAssetsDAO.getBaseMapper().selectList(new QueryWrapper<UserDataAssets>()
                .lambda().in(UserDataAssets::getBaseTableId, tableIds));

        log.info("sync: count={} tableIds={}", count, tableIds);

        CountDownLatch latch = new CountDownLatch(infoList.size());
        for (UserDataAssets assets : infoList) {
            semaphore.acquire();
            pool.submit(() -> {
                try {
                    log.info("start: id={} name={}", assets.getId(), assets.getProjectName());
                    boolean syncResult = syncHelper.pushAssetsTableForBI(assets.getId(), assets.getVersion(), 0L);
                    log.info("end: id={} name={} syncResult={}", assets.getId(), assets.getProjectName(), syncResult);
                } catch (Exception e) {
                    log.error("exception: id={} name={}", assets.getId(), assets.getProjectName(), e);
                } finally {
                    semaphore.release();
                }
            });
        }
        latch.await();
        pool.shutdown();
        log.info("sync: count={} Finished", count);
    }

//    public void syncOutDateTableDataBench2(int count, int total) throws InterruptedException {
//        pool = new ThreadPoolExecutor(count, count, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
//        Semaphore semaphore = new Semaphore(count);
//
//
//        log.info("sync: count={} total={}", count, total);
//
//        CountDownLatch latch = new CountDownLatch(total);
//        for (int i = 0; i < total; i++) {
//            semaphore.acquire();
//            int finalI = i;
//            pool.submit(() -> {
//                long start = System.currentTimeMillis();
//                try {
//                    boolean syncResult = syncHelper.syncApplicationTableToSelfDatasourceBench(finalI);
//                    log.info("syncOutDateTable end: syncResult={} {}ms", syncResult, System.currentTimeMillis() - start);
//                } catch (Exception e) {
//                    log.error("syncOutDateTable exception: {}ms", System.currentTimeMillis() - start, e);
//                } finally {
//                    latch.countDown();
//                    semaphore.release();
//                }
//            });
//        }
//        latch.await();
//        pool.shutdown();
//        log.info("sync: count={} Finished", count);
//    }

//    public void syncOutDateTableDataBench(Long applyId, int count, int total) throws InterruptedException {
//        pool = new ThreadPoolExecutor(count, count, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
//        Semaphore semaphore = new Semaphore(count);
//
//        TgApplicationInfo info = tgApplicationInfoMapper.selectById(applyId);
//
//        log.info("sync: applyId={} count={} total={}", applyId, count, total);
//
//        CountDownLatch latch = new CountDownLatch(total);
//        for (int i = 0; i < total; i++) {
//            semaphore.acquire();
//            pool.submit(() -> {
//                long start = System.currentTimeMillis();
//                try {
//                    log.info("syncOutDateTable: id={} name={}", info.getId(), info.getProjectName());
//                    boolean syncResult = syncHelper.syncApplicationTableToSelfDatasourceBench(info.getId(), 0L);
//                    log.info("syncOutDateTable end: id={} name={} syncResult={} {}ms", info.getId(), info.getProjectName(), syncResult, System.currentTimeMillis() - start);
//                } catch (Exception e) {
//                    log.error("syncOutDateTable exception: id={} name={} {}ms", info.getId(), info.getProjectName(), System.currentTimeMillis() - start, e);
//                } finally {
//                    latch.countDown();
//                    semaphore.release();
//                }
//            });
//        }
//        latch.await();
//        pool.shutdown();
//        log.info("sync: count={} Finished", count);
//    }

    /**
     * 删除过期的快照表
     * <p>
     * 注意：处理 当前时间往前一周过期的提数申请
     *
     * DDL容易阻塞
     */
    @Deprecated
    public void deleteExpireTable() {
        Date passDate = DateUtils.addDay(new Date(), -7);

        List<UserDataAssets> infoList = userDataAssetsDAO.getBaseMapper().selectList(new QueryWrapper<UserDataAssets>().lambda()
                .le(UserDataAssets::getDataExpire, passDate));
        List<Long> assetIds = infoList.stream().map(UserDataAssets::getId).collect(Collectors.toList());

        List<ApplicationDataUpdateRecord> snapshotRecords = applicationDataUpdateRecordMapper.selectList(
                new QueryWrapper<ApplicationDataUpdateRecord>().lambda()
                        .in(ApplicationDataUpdateRecord::getAssetsId, assetIds)
                        .eq(ApplicationDataUpdateRecord::getUpdateState, UpdateRecordStateType.SUCCESS)
                        .eq(ApplicationDataUpdateRecord::getSyncTarget, SyncTargetType.SELF_DS)
        );

        Map<Long, String> tableMap = snapshotRecords.stream().collect(Collectors
                .toMap(ApplicationDataUpdateRecord::getAssetsId, ApplicationDataUpdateRecord::getDataTableName,
                        (front, current) -> current));
        for (String tableName : tableMap.values()) {
            log.warn("delete expire: tableName={}", tableName);
            String dropSQL = "DROP TABLE IF EXISTS " + tableName + " ON CLUSTER " + CkClusterType.DEFAULT;
            tgCkProviderMapper.createTableAccordingApplication(dropSQL);
        }
    }

}
