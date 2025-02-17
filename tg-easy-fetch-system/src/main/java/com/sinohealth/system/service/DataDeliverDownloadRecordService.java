package com.sinohealth.system.service;

import com.sinohealth.system.domain.value.deliver.DeliverRequestContextHolder;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-28 10:13
 */
public interface DataDeliverDownloadRecordService {

    void saveDownloadRecords(DeliverRequestContextHolder requestContextHolder);
}
