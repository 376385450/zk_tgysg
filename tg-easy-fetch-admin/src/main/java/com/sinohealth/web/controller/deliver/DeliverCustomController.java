//package com.sinohealth.web.controller.deliver;
//
//import cn.hutool.http.HttpStatus;
//import com.github.pagehelper.PageInfo;
//import com.sinohealth.common.core.domain.AjaxResult;
//import com.sinohealth.common.core.page.TableDataInfo;
//import com.sinohealth.system.dto.application.deliver.event.*;
//import com.sinohealth.system.service.DataDeliverRecordService;
//import io.swagger.annotations.Api;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * @author kuangchengping@sinohealth.cn
// * 2022-12-21 10:13
// */
//@Api("数据交付-分配客户")
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/table_management/application/deliverManager")
//public class DeliverCustomController {
//    private final DataDeliverRecordService dataDeliverRecordService;
//
//    /**
//     * 分配客户列表
//     */
//    @PostMapping("/record/customer/list")
//    public AjaxResult<TableDataInfo<DataDeliverCustomerEventVO>> listApplyCustomerRecords(@RequestBody DataDeliverCustomerEventRequest request) {
//        PageInfo<DataDeliverCustomerEventVO> page = dataDeliverRecordService.listApplyCustomerRecordsForManager(request);
//        TableDataInfo<DataDeliverCustomerEventVO> rspData = new TableDataInfo<>();
//        rspData.setCode(HttpStatus.HTTP_OK);
//        rspData.setRows(page.getList());
//        rspData.setTotal(page.getTotal());
//        return AjaxResult.success(rspData);
//    }
//
//    /**
//     * 邮件
//     */
//    @PostMapping("/record/email/list")
//    public AjaxResult<TableDataInfo<DataDeliverEmailEventVO>> listEmailRecords(@RequestBody DataDeliverEmailEventRequest request) {
//        PageInfo<DataDeliverEmailEventVO> page = dataDeliverRecordService.listSendEmailRecords(request);
//        TableDataInfo<DataDeliverEmailEventVO> rspData = new TableDataInfo<>();
//        rspData.setCode(HttpStatus.HTTP_OK);
//        rspData.setRows(page.getList());
//        rspData.setTotal(page.getTotal());
//        return AjaxResult.success(rspData);
//    }
//
//    /**
//     * 下载导出
//     */
//    @PostMapping("/record/export/list")
//    public AjaxResult<TableDataInfo<DataDeliverExportEventVO>> listExportRecords(@RequestBody DataDeliverExportEventRequest request) {
//        PageInfo<DataDeliverExportEventVO> page = dataDeliverRecordService.listDownloadRecords(request);
//        TableDataInfo<DataDeliverExportEventVO> rspData = new TableDataInfo();
//        rspData.setCode(HttpStatus.HTTP_OK);
//        rspData.setRows(page.getList());
//        rspData.setTotal(page.getTotal());
//        return AjaxResult.success(rspData);
//    }
//
//}
