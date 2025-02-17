package com.sinohealth.api.monitor;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.system.domain.SysOperLog;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/monitor/operlog")
public interface SysOperlogApi {

    @GetMapping("/list")
    TableDataInfo list(SysOperLog operLog);

    @GetMapping("/export")
    AjaxResult export(SysOperLog operLog);

    @DeleteMapping("/{operIds}")
    AjaxResult remove(@PathVariable Long[] operIds);

    @DeleteMapping("/clean")
    AjaxResult clean();
}
