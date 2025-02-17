package com.sinohealth.system.service;

import com.sinohealth.system.dto.CustomerApplyAuthReqV2DTO;
import com.sinohealth.system.dto.CustomerApplyAuthRequestContext;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-09 15:02
 */
public interface DataDeliverCustomerRecordService {

    /**
     * 保存交付客户记录
     * @param authReqDTO
     * @param authRequestContext
     */
    void saveApplyCustomerRecords(CustomerApplyAuthReqV2DTO authReqDTO, CustomerApplyAuthRequestContext authRequestContext);
}
