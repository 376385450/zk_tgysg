package com.sinohealth.web.controller.table_manage;

import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.GetTableMonitorDataRequestDTO;
import com.sinohealth.system.dto.TableMonitorDataDTO;
import com.sinohealth.system.service.IMonitorDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-11 10:36 上午
 */
@RestController
@RequestMapping("/monitor")
@Api(tags = {"监控管理接口"})
@RequiredArgsConstructor
public class MonitorDataController extends BaseController {

    private final IMonitorDataService monitorDataService;

    @ApiOperation("查看表单监控数据")
    @PostMapping("/getData")
    public AjaxResult<TableMonitorDataDTO> getData(@RequestBody @Validated GetTableMonitorDataRequestDTO requestDTO) {
        TableMonitorDataDTO dataDTO = monitorDataService.getData(requestDTO);
        return AjaxResult.success(dataDTO);
    }

}
