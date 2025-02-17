package com.sinohealth.system.task;/**
 * @author linshiye
 */

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.module.file.dto.HuaweiPath;
import com.sinohealth.system.biz.ws.service.WsMsgService;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.AsyncTask;
import com.sinohealth.system.domain.constant.AsyncTaskConst;
import com.sinohealth.system.dto.application.DeliveryTableTaskParamVO;
import com.sinohealth.system.dto.application.deliver.request.ApplicationDeliverCsvRequest;
import com.sinohealth.system.dto.application.deliver.request.ApplicationDeliverExcelRequest;
import com.sinohealth.system.dto.assets.AssetsChartDownloadReqDTO;
import com.sinohealth.system.dto.assets.AssetsFormDownloadReqDTO;
import com.sinohealth.system.dto.system.AsyncTaskDto;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.service.CustomerAssetsV2Service;
import com.sinohealth.system.service.DataDeliveryService;
import com.sinohealth.system.service.IAsyncTaskService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.impl.AsyncTaskServiceImpl;
import com.sinohealth.system.service.impl.DefaultSyncHelper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lsy
 * @version 1.0
 * @date 2023-03-10 1:15 上午
 */
@Service
@Slf4j
public class AsyncTaskHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTaskHandler.class);

    @Autowired
    private DataDeliveryService dataDeliveryService;

    @Autowired
    private CustomerAssetsV2Service customerAssetsV2Service;

    @Autowired
    private IAsyncTaskService asyncTaskService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private DefaultSyncHelper defaultSyncHelper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Resource
    @Qualifier(ThreadPoolType.ASYNC_TASK)
    private ThreadPoolTaskExecutor pool;

    @Autowired
    private WsMsgService msgService;

    //    private static final ThreadPool threadPool = ExtensionLoader.getExtensionLoader(ThreadPool.class)
//            .getExtension(Constants.THREADPOOL_FIXED);
//    private final ExecutorService executorService = threadPool.getExecutor(ThreadPoolProperties.getCacheThreadPoolProperties("async_fixed_threadPool",
//            10, 10, 50000, 10));
    @Autowired
    private AppProperties appProperties;


    private static final AtomicLong errorCount = new AtomicLong(0);

    public AsyncTaskHandler() {
    }

    /**
     * 异步任务执行
     *
     * @see AsyncTaskServiceImpl#addAsyncTask(AsyncTaskDto)
     */
    @Scheduled(cron = "${dataplatform.asyncScanCron}")
    public void asyncTaskHandler() {
        // todo 改成调度中心定时

        Long size = redisTemplate.opsForList().size(RedisKeys.TASK_QUEUE);
        if (Objects.isNull(size) || size < 1) {
            return;
        }

        // TODO 容错设计
        try {
            for (int i = 0; i < appProperties.getMaxTaskConCount(); i++) {
                pool.submit(() -> {
                    Object idVal = null;
                    try {
                        Long cur = redisTemplate.opsForValue().increment(RedisKeys.TASK_COUNT);
                        if (cur > appProperties.getMaxTaskConCount()) {
//                            this.failover(cur);
                            return;
                        }

                        idVal = redisTemplate.opsForList().leftPop(RedisKeys.TASK_QUEUE);
                        if (Objects.isNull(idVal)) {
                            // empty queue
                            this.failover(cur);
                            return;
                        }
                        redisTemplate.opsForList().rightPush(RedisKeys.TASK_EXECUTING, idVal);

                        log.info("submit task {}", idVal);
                        AsyncTask asyncTask = asyncTaskService.getById(Long.parseLong(idVal.toString()));
                        if (Objects.isNull(asyncTask)) {
                            log.warn("not exist: id={}", idVal);
                            return;
                        }
                        if (Objects.equals(AsyncTaskConst.Status.SUCCEED, asyncTask.getStatus())) {
                            log.warn("already handle: id={}", idVal);
                            return;
                        }

                        log.info("handle task id={}", idVal);
                        this.handleOneTask(sysUserService.selectUserById(asyncTask.getUserId()), asyncTask);
                    } catch (Exception e) {
                        log.error("", e);
                        if (Objects.nonNull(idVal)) {
                            redisTemplate.opsForList().rightPush(RedisKeys.TASK_FAILED, 1, idVal);
                        }
                    } finally {
                        if (Objects.nonNull(idVal)) {
                            redisTemplate.opsForList().remove(RedisKeys.TASK_EXECUTING, 1, idVal);
                        }
                        redisTemplate.opsForValue().decrement(RedisKeys.TASK_COUNT);
                    }
                });
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 服务器故障导致try和finally未成对执行时，游标超过阈值，修复逻辑
     */
    private void failover(Long cur) {
        long l = errorCount.incrementAndGet();
        if (l % 13 == 0) {
            errorCount.set(0L);
            Long actualExec = redisTemplate.opsForList().size(RedisKeys.TASK_EXECUTING);
            Optional.ofNullable(actualExec).filter(c -> c < cur).ifPresent(v -> {
                log.warn("failover for task count: cur={} actual={}", cur, v);
                // finally -1
                redisTemplate.opsForValue().set(RedisKeys.TASK_COUNT, v.intValue() + 1);
            });
        }
    }

    private void handleOneTask(SysUser sysUser, AsyncTask asyncTask) {
        ThreadContextHolder.setSysUser(sysUser);
        LOGGER.info("异步任务开始执行：{}", JSON.toJSONString(asyncTask));
        HuaweiPath huaweiPath = null;
        try {
            switch (asyncTask.getBusinessType()) {
                case AsyncTaskConst.BUSINESS_TYPE.DELIVERY_CSV:
                    ApplicationDeliverCsvRequest applicationDeliverCsvRequest =
                            JSON.parseObject(asyncTask.getParamJson(),
                                    ApplicationDeliverCsvRequest.class);
                    huaweiPath = dataDeliveryService.deliverCsv(applicationDeliverCsvRequest);
                    break;
                case AsyncTaskConst.BUSINESS_TYPE.DELIVERY_EXCEL:
                    ApplicationDeliverExcelRequest applicationDeliverExcelRequest =
                            JSON.parseObject(asyncTask.getParamJson(),
                                    ApplicationDeliverExcelRequest.class);
                    applicationDeliverExcelRequest.setUserId(asyncTask.getUserId());
                    huaweiPath = dataDeliveryService.deliverExcel(applicationDeliverExcelRequest);
                    break;
                case AsyncTaskConst.BUSINESS_TYPE.FORM:
                    AssetsFormDownloadReqDTO assetsFormDownloadReqDTO =
                            JSON.parseObject(asyncTask.getParamJson(),
                                    AssetsFormDownloadReqDTO.class);
                    huaweiPath = customerAssetsV2Service.downloadApply(assetsFormDownloadReqDTO);
                    break;
                case AsyncTaskConst.BUSINESS_TYPE.CHART:
                    AssetsChartDownloadReqDTO assetsChartDownloadReqDTO =
                            JSON.parseObject(asyncTask.getParamJson(),
                                    AssetsChartDownloadReqDTO.class);
                    huaweiPath = customerAssetsV2Service.downloadChart(assetsChartDownloadReqDTO);
                    break;
                case AsyncTaskConst.BUSINESS_TYPE.DELIVERY_TABLE:
                    DeliveryTableTaskParamVO param = JSON.parseObject(asyncTask.getParamJson(), DeliveryTableTaskParamVO.class);
                    defaultSyncHelper.syncApplicationTableToCustomerDatasource(param.getAssetsId(), param.getVersion(), param.getUserId());
                    break;
                default:
                    log.warn("not support type: {} ", asyncTask.getBusinessType());
                    break;
            }
            // todo 改为异步这里需要在业务层通过接口调用
            if (huaweiPath != null) {
                asyncTask.setUrl(huaweiPath.getUrl());
                asyncTask.setFilePath(huaweiPath.getSourcePath());
                asyncTask.setFileName(huaweiPath.getFileName());
            }
            asyncTask.setStatus(AsyncTaskConst.Status.SUCCEED);
            asyncTask.setFlowStatus(AsyncTaskConst.FLOW_STATUS.EXECUTED);
            asyncTask.setRemark(null);
            asyncTask.setReadFlag(AsyncTaskConst.ReadFlag.NO_READ);
            asyncTask.setUpdateTime(new Date());
            asyncTaskService.updateById(asyncTask);
        } catch (Throwable e) {
            asyncTask.setRemark(e.getMessage());
            asyncTask.setStatus(AsyncTaskConst.Status.FAILED);
            asyncTask.setReadFlag(AsyncTaskConst.ReadFlag.NO_READ);
            asyncTask.setFlowStatus(AsyncTaskConst.FLOW_STATUS.EXECUTED);
            asyncTask.setUpdateTime(new Date());
            asyncTaskService.updateById(asyncTask);
            LOGGER.error("异步任务执行异常,任务id{}", asyncTask.getId(), e);
        } finally {
//            msgService.pushDownloadMsg(asyncTask.getUserId());
        }
        LOGGER.info("异步任务执行成功：{}", JSON.toJSONString(asyncTask));
    }

    /**
     * 异步任务执行
     */
    @Scheduled(cron = "${dataplatform.asyncFailedCron}")
    public void deleteFailedTask() {
        List<AsyncTask> asyncTasks = asyncTaskService.selectList(AsyncTaskConst.Status.FAILED, null, null);
        if (CollectionUtil.isNotEmpty(asyncTasks)) {
            for (AsyncTask asyncTask : asyncTasks) {
                asyncTask.setDelFlag(AsyncTaskConst.DEL_FLAG.DELETED);
                asyncTask.setUpdateTime(new Date());
                asyncTaskService.updateById(asyncTask);
            }
        }
    }
}
