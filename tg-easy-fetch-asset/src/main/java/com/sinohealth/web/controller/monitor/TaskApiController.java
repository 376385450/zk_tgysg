//package com.sinohealth.web.controller.monitor;
//
//import com.sinohealth.api.monitor.TaskApi;
//import com.sinohealth.common.core.domain.AjaxResult;
//import com.sinohealth.common.core.redis.RedisKeys;
//import com.sinohealth.system.domain.AsyncTask;
//import com.sinohealth.system.domain.constant.AsyncTaskConst;
//import com.sinohealth.system.job.application.SnapshotTableSyncJob;
//import com.sinohealth.system.service.IAsyncTaskService;
//import com.sinohealth.system.service.ITaskService;
//import com.sinohealth.system.service.impl.DefaultSyncHelper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Date;
//import java.util.List;
//
///**
// * 任务执行入口
// * 后门调试接口 对接代理门户
// */
//@Slf4j
//@RestController
//@RequestMapping("/api/task")
//public class TaskApiController implements TaskApi {
//    @Autowired
//    private ITaskService taskService;
//    @Autowired
//    private SnapshotTableSyncJob snapshotTableSyncJob;
//    @Autowired
//    private IAsyncTaskService asyncTaskService;
//    @Autowired
//    private DefaultSyncHelper syncHelper;
//    @Autowired
//    private RedisTemplate redisTemplate;
//
//    @Override
//    @GetMapping("/countTableRow")
//    public AjaxResult countTableRowByUpdateTime() {
//        taskService.countTableRowByUpdateTime(new Date());
//        return AjaxResult.success();
//    }
//
//    @Override
//    @GetMapping("/useStatics")
//    public AjaxResult updateUseStatics() {
//        taskService.updateUseStatics(new Date());
//        return AjaxResult.success();
//    }
//
//    @Override
//    @GetMapping("/syncTableInfo")
//    public AjaxResult syncTableInfo() {
//        taskService.syncTableInfo();
//
//        return AjaxResult.success();
//    }
//
//    /**
//     * 更新表热度信息
//     */
//    @Override
//    @GetMapping("/updateTableHeat")
//    public AjaxResult updateTableHeat() {
//        taskService.updateTableHeat();
//        return AjaxResult.success();
//    }
//
//    @Override
//    @GetMapping("/deleteExpireTable")
//    public AjaxResult deleteExpireTable() {
//        snapshotTableSyncJob.deleteExpireTable();
//        return AjaxResult.success();
//    }
//
//    @Override
//    @GetMapping("/syncOutDateTableData")
//    public AjaxResult syncOutDateTableData() throws InterruptedException {
//        snapshotTableSyncJob.syncOutDateTableData();
//        return AjaxResult.success();
//    }
//
//    @Override
//    @GetMapping("/benchmarkSyncShutDown")
//    public String benchmarkSyncShutDown() {
//        snapshotTableSyncJob.cancelSync();
//        return "OK";
//    }
////
////    @GetMapping("/benchmarkSync/{applyId}/{count}/{total}")
////    public String benchmarkSync(@PathVariable Integer count, @PathVariable Integer total, @PathVariable Long applyId) throws InterruptedException {
////        if (Objects.isNull(applyId)) {
////            return "ERROR";
////        }
////        snapshotTableSyncJob.syncOutDateTableDataBench(
////                applyId,
////                Optional.ofNullable(count).orElse(1),
////                Optional.ofNullable(total).orElse(1));
////        return "OK";
////    }
////
////    @GetMapping("/benchmarkSync2/{count}/{total}")
////    public String benchmarkSync(@PathVariable Integer count, @PathVariable Integer total) throws InterruptedException {
////        snapshotTableSyncJob.syncOutDateTableDataBench2(
////                Optional.ofNullable(count).orElse(1),
////                Optional.ofNullable(total).orElse(1));
////        return "OK";
////    }
//
//    @Override
//    @GetMapping("/rePushToCustomer")
//    public Boolean rePushToCustomer(@RequestParam("applyId") Long applyId) {
//        boolean finish = syncHelper.syncApplicationTableToCustomerDatasource(applyId, null, 0L);
//        log.info("finish={}", finish);
//        return finish;
//    }
//
//    @Override
//    @GetMapping("/retryDownload")
//    public String retryDownload(@RequestParam("id") String id) {
//        redisTemplate.opsForList().remove(RedisKeys.TASK_EXECUTING, 1, id);
//        redisTemplate.opsForList().rightPush(RedisKeys.TASK_QUEUE, id);
//        return "OK";
//    }
//
//    // TODO 清空堆积任务
//
//    // TODO 重推失效任务
//    @Override
//    @GetMapping("/retryAllDownload")
//    public String retryAllDownload() {
//        List<AsyncTask> asyncTasks = asyncTaskService.selectList(AsyncTaskConst.Status.HANGING, null, null);
//        for (AsyncTask asyncTask : asyncTasks) {
//            redisTemplate.opsForList().remove(RedisKeys.TASK_EXECUTING, 1, asyncTask.getId());
//            redisTemplate.opsForList().rightPush(RedisKeys.TASK_QUEUE, asyncTask.getId());
//        }
//        return "OK";
//    }
//
//    @Override
//    @GetMapping("/ping")
//    public String ping() {
//        return "pong";
//    }
//
//}
