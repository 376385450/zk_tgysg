package com.sinohealth.system.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.dao.TgDeliverEmailTemplateDAO;
import com.sinohealth.system.domain.TgDeliverEmailTemplate;
import com.sinohealth.system.mapper.TgDeliverEmailTemplateMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 15:24
 */
@Slf4j
@Repository
public class TgDeliverEmailTemplateDAOImpl
        extends ServiceImpl<TgDeliverEmailTemplateMapper, TgDeliverEmailTemplate>
        implements TgDeliverEmailTemplateDAO {
    @Override
    public TgDeliverEmailTemplate getByIdentify(String identify) {
        Wrapper<TgDeliverEmailTemplate> wrapper = Wrappers.<TgDeliverEmailTemplate>lambdaQuery()
                .eq(TgDeliverEmailTemplate::getIdentifyId, identify);
        return getOne(wrapper);
    }
}
