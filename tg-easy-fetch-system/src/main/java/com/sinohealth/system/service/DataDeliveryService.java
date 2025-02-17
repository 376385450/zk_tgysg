package com.sinohealth.system.service;


import com.sinohealth.common.module.file.dto.HuaweiPath;
import com.sinohealth.system.dto.application.deliver.*;
import com.sinohealth.system.dto.application.deliver.request.*;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 21:24
 */
public interface DataDeliveryService {

    HuaweiPath deliverExcel(ApplicationDeliverExcelRequest request) throws Exception;

    HuaweiPath deliverCsv(ApplicationDeliverCsvRequest request) throws Exception;

    void deliverPdf(ApplicationDeliverPdfRequest request) throws Exception;

    void deliverImage(ApplicationDeliverImageRequest request) throws Exception;

    void deliverEmail(ApplicationDeliverEmailRequest request) throws Exception;

    /**
     * 校验数据说明文档是否存在
     * @param request
     */
    ApplicationDataDescDocVerifyDTO verifyDataDescription(ApplicationDataDescDocVerifyRequest request);
}
