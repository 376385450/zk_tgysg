package com.sinohealth.api.deliver;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.system.dto.application.deliver.event.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/table_management/application/deliverManager")
public interface DeliverCustomApi {

    @PostMapping("/record/customer/list")
    AjaxResult<TableDataInfo<DataDeliverCustomerEventVO>> listApplyCustomerRecords(@RequestBody DataDeliverCustomerEventRequest request);

    @PostMapping("/record/email/list")
    AjaxResult<TableDataInfo<DataDeliverEmailEventVO>> listEmailRecords(@RequestBody DataDeliverEmailEventRequest request);

    @PostMapping("/record/export/list")
    AjaxResult<TableDataInfo<DataDeliverExportEventVO>> listExportRecords(@RequestBody DataDeliverExportEventRequest request);
}
