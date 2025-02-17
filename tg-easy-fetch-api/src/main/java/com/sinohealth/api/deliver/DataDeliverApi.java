package com.sinohealth.api.deliver;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.system.dto.DeliverEmailTemplateDTO;
import com.sinohealth.system.dto.application.deliver.request.DeliverEmailTemplateQuery;
import com.sinohealth.system.dto.application.deliver.*;
import com.sinohealth.system.dto.application.deliver.event.*;
import com.sinohealth.system.dto.application.deliver.request.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequestMapping("/api/table_management/application/deliver")
public interface DataDeliverApi {
    
    @PostMapping("/mode/list")
    AjaxResult<ApplicationDeliverModeDTO> queryMode(@Valid @RequestBody ApplicationDeliverModeQuery query);

    @PostMapping("/descDocVerify")
    AjaxResult<ApplicationDataDescDocVerifyDTO> verifyDataDescDocument(@Valid @RequestBody ApplicationDataDescDocVerifyRequest request);

    @PostMapping("/pdf")
    void deliverPdf(@Valid @RequestBody ApplicationDeliverPdfRequest request) throws Exception;

    @PostMapping("/csv")
    void deliverCsv(@Valid @RequestBody ApplicationDeliverCsvRequest request) throws Exception;

    @PostMapping("/image")
    void deliverImage(@Valid @RequestBody ApplicationDeliverImageRequest request) throws Exception;

    @PostMapping("/excel")
    AjaxResult deliverExcel(@Valid @RequestBody ApplicationDeliverExcelRequest request, HttpServletResponse response) throws Exception;

    @PostMapping("/emailTemplate")
    AjaxResult<DeliverEmailTemplateDTO> getEmailTemplate(@Valid @RequestBody DeliverEmailTemplateQuery query);

    @PostMapping("/email")
    AjaxResult deliverEmail(@Valid @RequestBody ApplicationDeliverEmailRequest request) throws Exception;

    @PostMapping("/record/customer/list")
    AjaxResult<TableDataInfo<DataDeliverCustomerEventVO>> listApplyCustomerRecords(@Valid @RequestBody DataDeliverCustomerEventRequest request);

    @PostMapping("/record/email/list")
    AjaxResult<TableDataInfo<DataDeliverEmailEventVO>> listEmailRecords(@Valid @RequestBody DataDeliverEmailEventRequest request);

    @PostMapping("/record/export/list")
    AjaxResult<TableDataInfo<DataDeliverExportEventVO>> listExportRecords(@Valid @RequestBody DataDeliverExportEventRequest request);
    
    
}
