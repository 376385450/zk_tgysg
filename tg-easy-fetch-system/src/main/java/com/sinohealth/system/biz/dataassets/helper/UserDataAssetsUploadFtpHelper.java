package com.sinohealth.system.biz.dataassets.helper;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.FtpStatus;
import com.sinohealth.common.exception.ExcelRowLimitException;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.common.FileAdapter;
import com.sinohealth.system.biz.dataassets.dao.AssetsCompareDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsCompare;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;
import com.sinohealth.system.biz.dataassets.dto.request.DataPreviewRequest;
import com.sinohealth.system.biz.table.facade.TableInfoSnapshotCompareFacade;
import com.sinohealth.system.config.FileProperties;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.value.ResourceDeliverFailover;
import com.sinohealth.system.domain.value.deliver.DeliverResourceType;
import com.sinohealth.system.domain.value.deliver.DeliverStrategyFactory;
import com.sinohealth.system.domain.value.deliver.ResourceDeliverStrategy;
import com.sinohealth.system.domain.value.deliver.datasource.ApplicationDataSource;
import com.sinohealth.system.domain.value.deliver.sink.FtpResourceSink;
import com.sinohealth.system.util.FtpClient;
import com.sinohealth.system.util.FtpClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2024-01-24 14:47
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataAssetsUploadFtpHelper {

    private final AssetsCompareDAO assetsCompareDAO;
    private final UserDataAssetsDAO userDataAssetsDAO;
    private final UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;

    private final DeliverStrategyFactory deliverStrategyFactory;
    private final AssetsCompareInvoker assetsCompareInvoker;

    private final FileAdapter fileAdapter;

    private final Environment environment;
    private final FileProperties fileProperties;
    private final RedisTemplate redisTemplate;

    @Resource
    @Qualifier(ThreadPoolType.FTP_TASK)
    private ThreadPoolTaskExecutor pool;

    private final AlertService alertService;


    public static final int FTP_MAX_CON = 4;
    private AtomicReference<Semaphore> semaphore = new AtomicReference<>(new Semaphore(FTP_MAX_CON, false));

    /**
     * 只用于批量操作时 入队列
     * <p>
     * 要注意 消费端和创建端 事务隔离问题
     *
     * @see UserDataAssetsUploadFtpHelper#scheduleUploadQueue() 消费
     * @see UserDataAssetsUploadFtpHelper#uploadFtp(Long) 处理
     */
    public void addFtpTask(Long assetsId) {
        if (Objects.isNull(assetsId)) {
            alertService.sendDevNormalMsg("提交了空资产任务导出Excel");
            return;
        }
        redisTemplate.opsForList().rightPush(RedisKeys.Ftp.FTP_QUEUE, assetsId);
        userDataAssetsDAO.lambdaUpdate()
                .set(UserDataAssets::getFtpStatus, FtpStatus.WAIT)
                .eq(UserDataAssets::getId, assetsId)
                .update();
    }

    public String retryAllWaitFtp() {
        List<UserDataAssets> waitList = userDataAssetsDAO.getBaseMapper().selectList(new QueryWrapper<UserDataAssets>().lambda()
                .select(UserDataAssets::getId)
                .eq(UserDataAssets::getFtpStatus, FtpStatus.WAIT));
        if (CollectionUtils.isEmpty(waitList)) {
            return "EMPTY wait";
        }

        for (UserDataAssets userDataAssets : waitList) {
            this.addFtpTask(userDataAssets.getId());
        }
        return "OK";
    }

    public void deleteFtp(List<String> paths) {
        if (CollectionUtils.isEmpty(paths)) {
            return;
        }

        try (FtpClient ftpClient = FtpClientFactory.getInstance()) {
            ftpClient.open();
            for (String file : paths) {
                ftpClient.delete(file);
            }
        } catch (Exception e) {
            log.error("删除FTP文件失败:", e);
        }
    }

    public String retryFtp(Long assetsId) {
        UserDataAssets waitList = userDataAssetsDAO.getBaseMapper().selectOne(new QueryWrapper<UserDataAssets>().lambda()
                .select(UserDataAssets::getId)
                .eq(UserDataAssets::getFtpStatus, FtpStatus.WAIT).eq(UserDataAssets::getId, assetsId)
                .last(" limit 1"));
        if (Objects.isNull(waitList)) {
            return "invalid " + assetsId;
        }

        this.addFtpTask(assetsId);
        return "OK";
    }

    public String retryFtpForce(Long assetsId) {
        boolean update = userDataAssetsDAO.lambdaUpdate()
                .set(UserDataAssets::getFtpStatus, FtpStatus.WAIT)
                .eq(UserDataAssets::getId, assetsId).update();
        if (!update) {
            return "No data: " + assetsId;
        }

        this.addFtpTask(assetsId);
        return "OK";
    }

    public String setFtpConcurrency(Integer con) {
        Semaphore cache = semaphore.get();

        if (cache.getQueueLength() > 0 || pool.getActiveCount() > 0) {
            return "Still Run " + semaphore.get().availablePermits();
        }
        Semaphore next = new Semaphore(con, true);
        semaphore.set(next);
        log.info("cache={} next={}", cache, next);
        return "Set " + con;
    }

    @Scheduled(cron = "0/20 * * * * ?")
    public void scheduleUploadQueue() {
        try {
            // 确定有没有锁
            synchronized (TableInfoSnapshotCompareFacade.class) {
                if (TableInfoSnapshotCompareFacade.run) {
                    log.warn("ignore export via table compare");
                    return;
                }
            }

            // 生产环境 导出Excel和快照表创建任务错开
            String activeProfile = environment.getActiveProfiles()[0];
            if (Objects.equals(activeProfile, "prd")) {
                Boolean triggerRun = redisTemplate.hasKey(RedisKeys.Assets.UPGRADE_WIDE_TASK_FLAG);
                if (BooleanUtils.isTrue(triggerRun)) {
                    return;
                }
            }

            // 提高小任务的调度执行机会, 进而提升整体执行效率
            int aval = semaphore.get().availablePermits() + 2;
//            log.info("ftp check available:{}", aval);
            for (int i = 0; i < aval; i++) {
                Object assetsId = redisTemplate.opsForList().leftPop(RedisKeys.Ftp.FTP_QUEUE);
                if (Objects.isNull(assetsId)) {
                    break;
                }

                semaphore.get().acquire();
                pool.execute(() -> {
                    try {
                        this.uploadFtp((Long) assetsId);
                    } catch (Exception e) {
                        log.error("", e);
                    } finally {
                        semaphore.get().release();
                    }
                });
            }

        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 同步执行
     *
     * @see UserDataAssetsUploadFtpHelper#addFtpTask(Long) 异步提交
     */
    public void uploadFtp(Long assetsId) {
        log.info("upload FTP assetsId={}", assetsId);
        if (Objects.isNull(assetsId)) {
            log.error("UPLOAD invalid: null assetsId");
            return;
        }

        UserDataAssets userDataAssets = userDataAssetsDAO.getById(assetsId);
        if (userDataAssets == null) {
            log.error("UPLOAD invalid: assetsId: {}", assetsId);
            return;
        }
        this.uploadFtpForAssets(userDataAssets);
    }

//    public void uploadFtp(TgApplicationInfo apply) {
//        if (!ApplicationConst.ApplicationType.DATA_APPLICATION.equals(apply.getApplicationType())) {
//            return;
//        }
//        if (apply.getAssetsId() == null) {
//            log.error("上传数据资产到ftp校验assetsId非空失败!, applyId: {}", apply.getId());
//            return;
//        }
//        UserDataAssets userDataAssets = userDataAssetsDAO.getById(apply.getAssetsId());
//        if (userDataAssets == null) {
//            log.error("上传数据资产到ftp校验数据资产非空失败!, assetsId: {}", apply.getAssetsId());
//            return;
//        }
//        this.uploadFtpForAssets(userDataAssets);
//    }

    /**
     * 上传数据资产
     */
    public void uploadFtpForAssets(UserDataAssets userDataAssets) {
        final Long assetsId = userDataAssets.getId();
        try {
            Object banFlag = redisTemplate.opsForValue().get(RedisKeys.Ftp.BAN_FTP_UPLOAD);
            if (Objects.nonNull(banFlag) && banFlag instanceof Integer && (Integer) banFlag == 1) {
                userDataAssetsDAO.lambdaUpdate()
                        .set(UserDataAssets::getFtpStatus, FtpStatus.WAIT)
                        .eq(UserDataAssets::getId, userDataAssets.getId())
                        .update();
                return;
            }

            // 修改状态为上传中
            userDataAssetsDAO.lambdaUpdate()
                    .set(UserDataAssets::getFtpStatus, FtpStatus.UPLOADING)
                    .eq(UserDataAssets::getId, userDataAssets.getId())
                    .update();

            log.info("数据资产上传ftp, assetsId: {} {}", userDataAssets.getId(), userDataAssets.getProjectName());
            String ftpUrl = this.doUploadFtp(assetsId, null);

            // 更新数据资产
            userDataAssetsDAO.lambdaUpdate()
                    .set(UserDataAssets::getFtpPath, ftpUrl)
                    .set(UserDataAssets::getFtpStatus, FtpStatus.SUCCESS)
                    .eq(UserDataAssets::getId, userDataAssets.getId())
                    .update();

            // 触发对比
            List<AssetsCompare> compareList = assetsCompareDAO.lambdaQuery()
                    .eq(AssetsCompare::getAssetsId, assetsId)
                    .eq(AssetsCompare::getCurVersion, userDataAssets.getVersion())
                    .list();
            assetsCompareInvoker.invokeCompare(compareList);
        } catch (Exception e) {
            log.error("数据资产上传ftp失败 assetsId={} applyId={} {} : {} \n", assetsId,
                    userDataAssets.getSrcApplicationId(), userDataAssets.getProjectName(), e.getMessage(), e);
            userDataAssetsDAO.lambdaUpdate()
                    .set(UserDataAssets::getFtpStatus, FtpStatus.FAILURE)
                    .set(UserDataAssets::getFtpErrorMessage, ExceptionUtils.getStackTrace(e))
                    .eq(UserDataAssets::getId, userDataAssets.getId())
                    .update();
        }
    }

    /**
     * 上传数据资产快照
     */
    public void uploadFtpForSnapshot(UserDataAssetsSnapshot userDataAssets) {
        final Long assetsId = userDataAssets.getAssetsId();
        final Integer version = userDataAssets.getVersion();
        try {
            // 修改状态为上传中
            userDataAssetsSnapshotDAO.lambdaUpdate()
                    .set(UserDataAssets::getFtpStatus, FtpStatus.UPLOADING)
                    .eq(UserDataAssets::getId, userDataAssets.getId())
                    .update();
            log.info("数据资产【快照】上传ftp开始, assetsId: {}, version: {}", assetsId, version);

            String ftpUrl = this.doUploadFtp(assetsId, version);

            // 更新数据资产
            userDataAssetsSnapshotDAO.lambdaUpdate()
                    .set(UserDataAssets::getFtpPath, ftpUrl)
                    .set(UserDataAssets::getFtpStatus, FtpStatus.SUCCESS)
                    .eq(UserDataAssets::getId, userDataAssets.getId())
                    .update();
        } catch (Exception e) {
            log.error("数据资产【快照】上传ftp失败 assetsId: {}, version: {}\n", assetsId, version, e);
            userDataAssetsSnapshotDAO.lambdaUpdate()
                    .set(UserDataAssets::getFtpStatus, FtpStatus.FAILURE)
                    .set(UserDataAssets::getFtpErrorMessage, ExceptionUtils.getStackTrace(e))
                    .eq(UserDataAssets::getId, userDataAssets.getId())
                    .update();
        }
    }

    private String doUploadFtp(Long assetsId, Integer version) throws Exception {
        ApplicationDataSource applicationDataSource = new ApplicationDataSource(assetsId)
                .setRequestDTO(new DataPreviewRequest().setVersion(version));

        ResourceDeliverStrategy strategy = deliverStrategyFactory.getStrategy(applicationDataSource, DeliverResourceType.EXCEL);
        ResourceDeliverStrategy failoverStrategy = deliverStrategyFactory.getStrategy(applicationDataSource, DeliverResourceType.CSV);
        com.sinohealth.system.domain.value.deliver.Resource resource =
                ResourceDeliverFailover.deliver(strategy, failoverStrategy, applicationDataSource,
                        e -> e instanceof ExcelRowLimitException);

        String suffix = FileUtil.getSuffix(resource.getName());
        String remote = fileAdapter.buildAssetsPath(assetsId, suffix);
        FtpResourceSink ftpResourceSink = new FtpResourceSink().setResource(resource).setRemote(remote);
        return ftpResourceSink.process();
    }

}
