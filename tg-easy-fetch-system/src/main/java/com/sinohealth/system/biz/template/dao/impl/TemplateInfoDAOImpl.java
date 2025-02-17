package com.sinohealth.system.biz.template.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 11:28
 */
@Repository
public class TemplateInfoDAOImpl
        extends ServiceImpl<TgTemplateInfoMapper, TgTemplateInfo>
        implements TemplateInfoDAO {

    public Map<Long, String> queryNameMap(Collection<Long> tempIds) {
        return Lambda.queryMapIfExist(tempIds, v -> lambdaQuery()
                .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName)
                .in(TgTemplateInfo::getId, v)
                .list(), TgTemplateInfo::getId, TgTemplateInfo::getTemplateName);
    }
}
