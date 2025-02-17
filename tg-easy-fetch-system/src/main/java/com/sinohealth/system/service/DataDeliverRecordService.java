package com.sinohealth.system.service;

import com.github.pagehelper.PageInfo;
import com.sinohealth.system.domain.value.deliver.DeliverRequestContextHolder;
import com.sinohealth.system.dto.CustomerApplyAuthReqV2DTO;
import com.sinohealth.system.dto.CustomerApplyAuthRequestContext;
import com.sinohealth.system.dto.application.deliver.request.ApplicationDeliverEmailRequest;
import com.sinohealth.system.dto.application.deliver.event.DataDeliverCustomerEventVO;
import com.sinohealth.system.dto.application.deliver.event.DataDeliverCustomerEventRequest;
import com.sinohealth.system.dto.application.deliver.event.DataDeliverEmailEventVO;
import com.sinohealth.system.dto.application.deliver.event.DataDeliverEmailEventRequest;
import com.sinohealth.system.dto.application.deliver.event.DataDeliverExportEventVO;
import com.sinohealth.system.dto.application.deliver.event.DataDeliverExportEventRequest;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 14:04
 */
public interface DataDeliverRecordService {

    /**
     * 保存交付记录
     * @param requestContextHolder
     */
    void saveDownloadRecords(DeliverRequestContextHolder requestContextHolder);

    /**
     * 保存交付邮件记录
     * @param emailRequest
     * @param requestContextHolder
     */
    void saveSendEmailRecords(ApplicationDeliverEmailRequest emailRequest, DeliverRequestContextHolder requestContextHolder);

    /**
     * 保存交付客户记录
     * @param authReqDTO
     * @param authRequestContext
     */
    void saveApplyCustomerRecords(CustomerApplyAuthReqV2DTO authReqDTO, CustomerApplyAuthRequestContext authRequestContext);

    /**
     * 查询下载/导出记录
     * @param request
     * @return
     */
    PageInfo<DataDeliverExportEventVO> listDownloadRecords(DataDeliverExportEventRequest request);

    /**
     * 查询邮件发送记录
     * @param request
     * @return
     */
    PageInfo<DataDeliverEmailEventVO> listSendEmailRecords(DataDeliverEmailEventRequest request);

    /**
     * 查询交付客户记录
     * @param request
     * @return
     */
    PageInfo<DataDeliverCustomerEventVO> listApplyCustomerRecords(DataDeliverCustomerEventRequest request);


    /**
     * 客户交付记录 - 管理角色
     */
    PageInfo<DataDeliverCustomerEventVO> listApplyCustomerRecordsForManager(DataDeliverCustomerEventRequest request);
}
