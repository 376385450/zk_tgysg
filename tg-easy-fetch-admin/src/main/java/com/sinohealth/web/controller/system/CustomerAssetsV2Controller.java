package com.sinohealth.web.controller.system;

import com.alibaba.fastjson.JSON;
import com.sinohealth.arkbi.param.DownloadFileType;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.domain.TgCustomerApplyAuth;
import com.sinohealth.system.domain.constant.AsyncTaskConst;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.assets.*;
import com.sinohealth.system.dto.system.AsyncTaskDto;
import com.sinohealth.system.service.CustomerAssetsV2Service;
import com.sinohealth.system.service.IAsyncTaskService;
import com.sinohealth.system.service.ISysCustomerAuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;

/**
 * 客户资产v2
 *
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-01 16:24
 */
@Slf4j
@Api("客户资产v2")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/system/customer/auth")
public class CustomerAssetsV2Controller extends BaseController {

    private final CustomerAssetsV2Service customerAssetsV2Service;

    private final ISysCustomerAuthService sysCustomerAuthService;

    @Autowired
    private IAsyncTaskService asyncTaskService;

    @ApiOperation("客户资产目录树")
    @PostMapping("/assetsTree")
    public AjaxResult getTree(@Valid @RequestBody AssetsDirTreeQuery query) {
        List tree = customerAssetsV2Service.getTree(query);
        return AjaxResult.success(tree);
    }

    @ApiOperation("客户报表")
    @PostMapping("/customerAssetsTree")
    public AjaxResult getTree(@Valid @RequestBody CustomerAuthTreeQuery query) {
        List tree = customerAssetsV2Service.getCustomerTree(query);
        return AjaxResult.success(tree);
    }

    @ApiOperation("子账号客户报表")
    @PostMapping("/subCustomerAssetsTree")
    public AjaxResult querySubCustomerAuthList(@Valid @RequestBody SubCustomerAuthTreeQuery query) {
        List tree = customerAssetsV2Service.getSubCustomerTree(query);
        return AjaxResult.success(tree);
    }


    @GetMapping("/outer/queryReportFormHeaders")
    @ApiOperation(value = "客户资产-查询表头")
    public AjaxResult queryReportFormHeaders(@RequestParam Long assetsId) {
        List<AuthTableFieldDTO> authTableFields = customerAssetsV2Service.getAuthTableFields(assetsId);
        return AjaxResult.success(authTableFields);
    }

    @ApiOperation(value = "客户资产-查询表头以及分页数据")
    @PostMapping("/outer/{assetsId}/queryReportFormHeaders")
    public AjaxResult queryReportFormHeadersApi(@PathVariable("assetsId") Long assetsId,
                                                @Valid @RequestBody GetDataInfoRequestDTO requestDTO) {
        Object result = customerAssetsV2Service.getAuthTableData(assetsId, requestDTO);
        return AjaxResult.success(result);
    }

    @ApiOperation(value = "客户资产-数据表单下载 Excel Csv")
    @PostMapping("/form/download")
    public void downloadApply(@Valid @RequestBody AssetsFormDownloadReqDTO reqDTO, HttpServletResponse response) throws Exception {
        AjaxResult<Object> ajx = sysCustomerAuthService.checkParam(reqDTO.getAssetsId(), CommonConstants.CUSTOMER_DOWNLOAD, response);
        if (ajx != null) {
            return;
        }
        TgCustomerApplyAuth tgCustomerApplyAuth = customerAssetsV2Service.getApplyAuthNode(reqDTO.getAssetsId());
        if (tgCustomerApplyAuth == null) {
            return;
        }

        if (Objects.equals(reqDTO.getDownloadType(), DownloadFileType.EXCEL.name())) {
            Long dataVolume = customerAssetsV2Service.getDataVolume(reqDTO.getAssetsId(), "");
            if (dataVolume > CommonConstants.EXCEL_EXPORT_SIZE) {
                response.setStatus(265);
                this.writeErrorMessage(response, response.getOutputStream(), InfoConstants.EXCEL_LIMIT);
                return;
            }
        }

        //  做到异步任务中心
        AsyncTaskDto asyncTaskDto = new AsyncTaskDto();
        asyncTaskDto.setProjectName(tgCustomerApplyAuth.getNodeName());
        asyncTaskDto.setBusinessType(AsyncTaskConst.BUSINESS_TYPE.FORM);
        asyncTaskDto.setParamJson(JSON.toJSONString(reqDTO));
        asyncTaskDto.setUserId(SecurityUtils.getUserId());
        if (reqDTO.getDownloadType().equalsIgnoreCase("excel")) {
            asyncTaskDto.setType(AsyncTaskConst.Type.EXCEL);
        } else if (reqDTO.getDownloadType().equalsIgnoreCase("csv")) {
            asyncTaskDto.setType(AsyncTaskConst.Type.CSV);
        }
        asyncTaskDto.setUserId(SecurityUtils.getUserId());
        asyncTaskService.addAsyncTask(asyncTaskDto);
//        customerAssetsV2Service.downloadApply(reqDTO);
    }


    private void writeErrorMessage(HttpServletResponse response, ServletOutputStream outputStream, String s) {
        log.error("{}", s);
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = new PrintWriter(outputStream);
        out.println(s);
        out.flush();
    }

    @ApiOperation(value = "客户资产-图表分析下载")
    @PostMapping("/chart/download")
    public void downloadChart(@Valid @RequestBody AssetsChartDownloadReqDTO reqDTO) {
        TgCustomerApplyAuth tgCustomerApplyAuth = customerAssetsV2Service.getArkbiAuthNode(reqDTO.getArkId());
        if (tgCustomerApplyAuth == null) {
            return;
        }
        //  做到异步任务中心
        AsyncTaskDto asyncTaskDto = new AsyncTaskDto();
        asyncTaskDto.setProjectName(tgCustomerApplyAuth.getNodeName());
        asyncTaskDto.setBusinessType(AsyncTaskConst.BUSINESS_TYPE.CHART);
        asyncTaskDto.setParamJson(JSON.toJSONString(reqDTO));
        asyncTaskDto.setUserId(SecurityUtils.getUserId());
        if (reqDTO.getDownloadType().equalsIgnoreCase("excel")) {
            asyncTaskDto.setType(AsyncTaskConst.Type.EXCEL);
        } else if (reqDTO.getDownloadType().equalsIgnoreCase("csv")) {
            asyncTaskDto.setType(AsyncTaskConst.Type.CSV);
        }
        asyncTaskDto.setUserId(SecurityUtils.getUserId());
        asyncTaskService.addAsyncTask(asyncTaskDto);
//        customerAssetsV2Service.downloadChart(reqDTO);
    }

    @ApiOperation(value = "客户资产-仪表板下载")
    @PostMapping("/dashboard/download")
    public void downloadDashboard(@Valid @RequestBody AssetsDashboardDownloadReqDTO reqDTO) throws Exception {
        customerAssetsV2Service.downloadDashboard(reqDTO);
    }

    @ApiOperation("客户资产-禁用、启用")
    @PostMapping("/status/update")
    public AjaxResult updateAuthStatus(@Valid @RequestBody AssetsAuthStatusUpdateReqDTO reqDTO) {
        customerAssetsV2Service.updateAuthStatus(reqDTO);
        return AjaxResult.success();
    }

}
