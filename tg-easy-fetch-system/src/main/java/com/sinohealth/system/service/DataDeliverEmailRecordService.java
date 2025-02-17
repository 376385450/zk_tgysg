package com.sinohealth.system.service;

import com.sinohealth.system.domain.value.deliver.DeliverRequestContextHolder;
import com.sinohealth.system.dto.application.deliver.request.ApplicationDeliverEmailRequest;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-28 10:16
 */
public interface DataDeliverEmailRecordService {

    void saveSendEmailRecords(ApplicationDeliverEmailRequest emailRequest, DeliverRequestContextHolder requestContextHolder);
}
