package com.sinohealth.system.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.TgDeliverEmailTemplate;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 15:23
 */
public interface TgDeliverEmailTemplateDAO extends IService<TgDeliverEmailTemplate> {

    TgDeliverEmailTemplate getByIdentify(String identify);
}
