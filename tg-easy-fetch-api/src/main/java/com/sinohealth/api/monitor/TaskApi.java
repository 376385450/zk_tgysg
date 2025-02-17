package com.sinohealth.api.monitor;

import com.sinohealth.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/task")
public interface TaskApi {

    @GetMapping("/countTableRow")
    AjaxResult countTableRowByUpdateTime();

    @GetMapping("/useStatics")
    AjaxResult updateUseStatics();

    @GetMapping("/syncTableInfo")
    AjaxResult syncTableInfo();

    @GetMapping("/updateTableHeat")
    AjaxResult updateTableHeat();

    @GetMapping("/deleteExpireTable")
    AjaxResult deleteExpireTable();

    @GetMapping("/syncOutDateTableData")
    AjaxResult syncOutDateTableData() throws InterruptedException;

    @GetMapping("/benchmarkSyncShutDown")
    String benchmarkSyncShutDown();


    @GetMapping("/rePushToCustomer")
    Boolean rePushToCustomer(@RequestParam("applyId") Long applyId);

    @GetMapping("/retryDownload")
    String retryDownload(@RequestParam("id") String id);
    
    @GetMapping("/retryAllDownload")
    String retryAllDownload();
    
    @GetMapping("/ping")
    String ping();
}
