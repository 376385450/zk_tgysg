package com.sinohealth.system.service;

import com.sinohealth.system.dto.DeliverEmailTemplateDTO;
import com.sinohealth.system.dto.application.deliver.request.DeliverEmailTemplateQuery;
import com.sinohealth.system.dto.application.deliver.request.DeliverEmailTemplateUpdateRequestDTO;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 15:08
 */
public interface DeliverEmailTemplateService {

    /**
     * 获取邮件模板
     * @param query
     * @return
     */
    DeliverEmailTemplateDTO getTemplate(DeliverEmailTemplateQuery query);

    /**
     * 更新邮件模板
     * @param requestDTO
     */
    void updateTemplate(DeliverEmailTemplateUpdateRequestDTO requestDTO);
}
