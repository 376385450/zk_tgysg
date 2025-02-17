package com.sinohealth.system.biz.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinohealth.system.biz.process.dao.TgFlowProcessSettingDetailDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessSettingDetail;
import com.sinohealth.system.biz.process.service.TgFlowProcessSettingDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TgFlowProcessSettingDetailServiceImpl implements TgFlowProcessSettingDetailService {
    private final TgFlowProcessSettingDetailDAO tgFlowProcessSettingDetailDAO;

    @Override
    public void removeByBaseId(Long baseId) {
        tgFlowProcessSettingDetailDAO.remove(new LambdaQueryWrapper<TgFlowProcessSettingDetail>().eq(TgFlowProcessSettingDetail::getBaseId, baseId));
    }

    @Override
    public void saveBatch(List<TgFlowProcessSettingDetail> details) {
        tgFlowProcessSettingDetailDAO.saveOrUpdateBatch(details);
    }

    @Override
    public List<TgFlowProcessSettingDetail> getByBaseId(Long baseId) {
        return tgFlowProcessSettingDetailDAO.list(new LambdaQueryWrapper<TgFlowProcessSettingDetail>().eq(TgFlowProcessSettingDetail::getBaseId, baseId));
    }
}
