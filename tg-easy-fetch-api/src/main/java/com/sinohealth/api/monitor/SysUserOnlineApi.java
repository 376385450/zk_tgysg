package com.sinohealth.api.monitor;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/api/monitor/online")
public interface SysUserOnlineApi {

    @GetMapping("/list")
    TableDataInfo list(String ipaddr, String userName);

    @DeleteMapping("/{tokenId}")
    AjaxResult forceLogout(@PathVariable String tokenId);
}
