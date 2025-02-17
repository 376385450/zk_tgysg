package com.sinohealth.api.monitor;

import com.sinohealth.common.core.domain.AjaxResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/monitor/server")
public interface ServerApi {

    @GetMapping()
    AjaxResult getInfo() throws Exception;
}
