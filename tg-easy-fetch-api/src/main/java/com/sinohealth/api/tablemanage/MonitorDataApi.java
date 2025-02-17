package com.sinohealth.api.tablemanage;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.GetTableMonitorDataRequestDTO;
import com.sinohealth.system.dto.TableMonitorDataDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/monitor")
public interface MonitorDataApi {
    @PostMapping("/getData")
    AjaxResult<TableMonitorDataDTO> getData(@RequestBody @Validated GetTableMonitorDataRequestDTO requestDTO);
}
