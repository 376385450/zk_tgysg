package com.sinohealth.system.biz.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinohealth.system.biz.process.dao.TgFlowProcessSettingBaseDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessSettingBase;
import com.sinohealth.system.biz.process.service.TgFlowProcessSettingBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TgFlowProcessSettingBaseServiceImpl implements TgFlowProcessSettingBaseService {
    private final TgFlowProcessSettingBaseDAO tgFlowProcessSettingBaseDAO;

    @Override
    public List<TgFlowProcessSettingBase> findByCategory(String category) {
        LambdaQueryWrapper<TgFlowProcessSettingBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgFlowProcessSettingBase::getCategory, category);
        return tgFlowProcessSettingBaseDAO.list(wrapper);
    }

    @Override
    public List<TgFlowProcessSettingBase> findByName(String name) {
        LambdaQueryWrapper<TgFlowProcessSettingBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgFlowProcessSettingBase::getName, name);
        return tgFlowProcessSettingBaseDAO.list(wrapper);
    }

    @Override
    public void saveOrUpdate(TgFlowProcessSettingBase entity) {
        tgFlowProcessSettingBaseDAO.saveOrUpdate(entity);
    }

    @Override
    public TgFlowProcessSettingBase detail(Long id) {
        return tgFlowProcessSettingBaseDAO.getById(id);
    }
}
