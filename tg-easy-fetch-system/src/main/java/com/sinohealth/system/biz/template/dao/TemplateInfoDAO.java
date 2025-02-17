package com.sinohealth.system.biz.template.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.TgTemplateInfo;

import java.util.Collection;
import java.util.Map;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 11:27
 */
public interface TemplateInfoDAO extends IService<TgTemplateInfo> {

    Map<Long, String> queryNameMap(Collection<Long> allTemp);
}
