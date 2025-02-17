package com.sinohealth.web.controller.deliver;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.sinohealth.api.deliver.DataDeliverApi;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dir.dto.node.AssetsNode;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.AsyncTaskConst;
import com.sinohealth.system.domain.value.deliver.DeliverHelper;
import com.sinohealth.system.domain.value.deliver.DeliverRequestContextHolder;
import com.sinohealth.system.domain.value.deliver.DeliverResourceType;
import com.sinohealth.system.dto.DeliverEmailTemplateDTO;
import com.sinohealth.system.dto.application.deliver.ApplicationDataDescDocVerifyDTO;
import com.sinohealth.system.dto.application.deliver.ApplicationDeliverModeDTO;
import com.sinohealth.system.dto.application.deliver.event.*;
import com.sinohealth.system.dto.application.deliver.request.*;
import com.sinohealth.system.dto.system.AsyncTaskDto;
import com.sinohealth.system.service.*;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 10:21
 */
@Api("数据交付")
@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/table_management/application/deliver")
public class DataDeliverApiController extends BaseController implements DataDeliverApi {

    private final DataDeliveryService dataDeliveryService;

    private final DataDeliverRecordService dataDeliverRecordService;

    private final DeliverEmailTemplateService deliverEmailTemplateService;

    private final IApplicationService applicationService;
    private final CKClusterAdapter ckClusterAdapter;

    @Autowired
    private IAsyncTaskService asyncTaskService;

    @Override
    @PostMapping("/mode/list")
    public AjaxResult<ApplicationDeliverModeDTO> queryMode(@Valid @RequestBody ApplicationDeliverModeQuery query) {
        DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.build(query, null);
        List<DeliverResourceType> resourceTypeList = DeliverHelper.getSupportResourceType(requestContextHolder);
        List<String> modeList = resourceTypeList.stream().map(DeliverResourceType::name).collect(Collectors.toList());

        modeList.remove(DeliverResourceType.CUSTOMER.name());
        modeList.remove(DeliverResourceType.EMAIL.name());
        // 打包类型 屏蔽 PDF 和 图片
        if (BooleanUtil.isTrue(query.getPack())) {
            modeList.remove(DeliverResourceType.PDF.name());
            modeList.remove(DeliverResourceType.IMAGE.name());
        }

        ApplicationDeliverModeDTO result = new ApplicationDeliverModeDTO();
        result.setModes(modeList);
        return AjaxResult.success(result);
    }

    @Override
    @PostMapping("/descDocVerify")
    public AjaxResult<ApplicationDataDescDocVerifyDTO> verifyDataDescDocument(@Valid @RequestBody ApplicationDataDescDocVerifyRequest request) {
        ApplicationDataDescDocVerifyDTO result = dataDeliveryService.verifyDataDescription(request);
        return AjaxResult.success(result);
    }


    @Override
    @PostMapping("/pdf")
    public void deliverPdf(@Valid @RequestBody ApplicationDeliverPdfRequest request) throws Exception {
        Assert.isTrue(request.getAssetsId() != null || CollectionUtils.isNotEmpty(request.getNodeIds()), "nodeIds和assetsId不能都为空");
        dataDeliveryService.deliverPdf(request);
    }

    @Override
    @PostMapping("/csv")
    public void deliverCsv(@Valid @RequestBody ApplicationDeliverCsvRequest request) throws Exception {
        Assert.isTrue(request.getAssetsId() != null || CollectionUtils.isNotEmpty(request.getNodeIds()), "nodeIds和assetsId不能都为空");
        AsyncTaskDto asyncTaskDto = new AsyncTaskDto();
        asyncTaskDto.setProjectName(request.getPackName());
        asyncTaskDto.setBusinessType(AsyncTaskConst.BUSINESS_TYPE.DELIVERY_CSV);
        asyncTaskDto.setParamJson(JSON.toJSONString(request));
        if (request.getPack()) {
            asyncTaskDto.setType(AsyncTaskConst.Type.ZIP);
        } else {
            asyncTaskDto.setType(AsyncTaskConst.Type.CSV);
        }
        asyncTaskDto.setUserId(SecurityUtils.getUserId());
        asyncTaskService.addAsyncTask(asyncTaskDto);
    }

    @Override
    @PostMapping("/image")
    public void deliverImage(@Valid @RequestBody ApplicationDeliverImageRequest request) throws Exception {
        Assert.isTrue(request.getAssetsId() != null || CollectionUtils.isNotEmpty(request.getNodeIds()), "nodeIds和assetsId不能都为空");
        dataDeliveryService.deliverImage(request);
    }

    @Override
    @PostMapping("/excel")
    public AjaxResult deliverExcel(@Valid @RequestBody ApplicationDeliverExcelRequest request, HttpServletResponse response) throws Exception {
        Assert.isTrue(request.getAssetsId() != null || CollectionUtils.isNotEmpty(request.getNodeIds()), "nodeIds和assetsId不能都为空");
        AjaxResult ajaxResult = checkDataColumn(request);
        if (ajaxResult != null) {
            response.setStatus(265);
            writeErrorMessage(response, response.getOutputStream(), InfoConstants.EXCEL_LIMIT);
        } else {
            AsyncTaskDto asyncTaskDto = new AsyncTaskDto();
            asyncTaskDto.setProjectName(request.getPackName());
            asyncTaskDto.setBusinessType(AsyncTaskConst.BUSINESS_TYPE.DELIVERY_EXCEL);
            asyncTaskDto.setParamJson(JSON.toJSONString(request));
            if (request.getPack()) {
                asyncTaskDto.setType(AsyncTaskConst.Type.ZIP);
            } else {
                asyncTaskDto.setType(AsyncTaskConst.Type.EXCEL);
            }
            asyncTaskDto.setUserId(SecurityUtils.getUserId());
            asyncTaskService.addAsyncTask(asyncTaskDto);
        }
        return AjaxResult.success();
    }

    private AjaxResult checkDataColumn(ApplicationDeliverExcelRequest request) {
        if (Objects.nonNull(request.getAssetsId())) {
            AjaxResult<Object> result = this.validateExportCount(request.getAssetsId());
            if (result != null) {
                return result;
            }
        }
        if (CollectionUtils.isNotEmpty(request.getNodeIds())) {
            for (String id : request.getNodeIds()) {
                Pair<Long, String> idPair = AssetsNode.parseId(id);
                if (Objects.isNull(idPair) || !Objects.equals(idPair.getValue(), ApplicationConst.AssetsIcon.DATA)) {
                    log.warn("选择了非数据资产: id={}", id);
                    continue;
                }
                AjaxResult<Object> result = this.validateExportCount(idPair.getKey());
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }


    private AjaxResult<Object> validateExportCount(Long assetsId) {
        UserDataAssets info = UserDataAssets.newInstance().selectById(assetsId);
        if (Objects.isNull(info)) {
            return null;
        }
        String sql = info.getAssetsSql();
        Long count = ckClusterAdapter.mixCount(info.getAssetTableName(), sql);
        if (count > CommonConstants.EXCEL_EXPORT_SIZE) {
            return AjaxResult.error("数据量已超出Excel数据量限制（" + CommonConstants.EXCEL_EXPORT_SIZE + "），建议缩小数据筛选范围");
        }
        return null;
    }

    @Override
    @PostMapping("/emailTemplate")
    public AjaxResult<DeliverEmailTemplateDTO> getEmailTemplate(@Valid @RequestBody DeliverEmailTemplateQuery query) {
        DeliverEmailTemplateDTO template = deliverEmailTemplateService.getTemplate(query);
        return AjaxResult.success(template);
    }

    @Override
    @PostMapping("/email")
    public AjaxResult deliverEmail(@Valid @RequestBody ApplicationDeliverEmailRequest request) throws Exception {
        Assert.isTrue(request.getAssetsId() != null
                || CollectionUtils.isNotEmpty(request.getNodeIds()), "ids和applicationId不能都为空");
        dataDeliveryService.deliverEmail(request);
        return AjaxResult.success();
    }

    @Override
    @PostMapping("/record/customer/list")
    public AjaxResult<TableDataInfo<DataDeliverCustomerEventVO>> listApplyCustomerRecords(@Valid @RequestBody DataDeliverCustomerEventRequest request) {
        PageInfo<DataDeliverCustomerEventVO> page = dataDeliverRecordService.listApplyCustomerRecords(request);
        TableDataInfo<DataDeliverCustomerEventVO> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.setRows(page.getList());
        rspData.setTotal(page.getTotal());
        return AjaxResult.success(rspData);
    }

    @Override
    @PostMapping("/record/email/list")
    public AjaxResult<TableDataInfo<DataDeliverEmailEventVO>> listEmailRecords(@Valid @RequestBody DataDeliverEmailEventRequest request) {
        PageInfo<DataDeliverEmailEventVO> page = dataDeliverRecordService.listSendEmailRecords(request);
        TableDataInfo<DataDeliverEmailEventVO> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.setRows(page.getList());
        rspData.setTotal(page.getTotal());
        return AjaxResult.success(rspData);
    }

    @Override
    @PostMapping("/record/export/list")
    public AjaxResult<TableDataInfo<DataDeliverExportEventVO>> listExportRecords(@Valid @RequestBody DataDeliverExportEventRequest request) {
        PageInfo<DataDeliverExportEventVO> page = dataDeliverRecordService.listDownloadRecords(request);
        TableDataInfo<DataDeliverExportEventVO> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.setRows(page.getList());
        rspData.setTotal(page.getTotal());
        return AjaxResult.success(rspData);
    }

    private void writeErrorMessage(HttpServletResponse response, ServletOutputStream outputStream, String s) {
        log.error("{}", s);
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = new PrintWriter(outputStream);
        out.println(s);
        out.flush();
    }

}
