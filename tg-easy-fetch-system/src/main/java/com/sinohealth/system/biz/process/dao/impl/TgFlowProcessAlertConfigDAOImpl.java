package com.sinohealth.system.biz.process.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.process.dao.TgFlowProcessAlertConfigDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessAlertConfig;
import com.sinohealth.system.biz.process.mapper.TgFlowProcessAlertConfigMapper;

@Repository
public class TgFlowProcessAlertConfigDAOImpl extends
    ServiceImpl<TgFlowProcessAlertConfigMapper, TgFlowProcessAlertConfig> implements TgFlowProcessAlertConfigDAO {
    @Override
    public TgFlowProcessAlertConfig query(String category, String code) {
        return this.lambdaQuery().eq(TgFlowProcessAlertConfig::getCategory, category)
            .eq(TgFlowProcessAlertConfig::getCode, code).one();
    }

    @Override
    public List<TgFlowProcessAlertConfig> list(String category) {
        return this.lambdaQuery().eq(TgFlowProcessAlertConfig::getCategory, category).list();
    }
}
