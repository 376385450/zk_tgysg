package com.sinohealth.api.monitor;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.system.domain.SysLogininfor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/monitor/logininfor")
public interface SysLoginInfoApi {

    @GetMapping("/list")
    TableDataInfo list(SysLogininfor logininfor);

    @GetMapping("/export")
    AjaxResult export(SysLogininfor logininfor);

    @DeleteMapping("/{infoIds}")
    AjaxResult remove(@PathVariable Long[] infoIds);

    @DeleteMapping("/clean")
    AjaxResult clean();
}
