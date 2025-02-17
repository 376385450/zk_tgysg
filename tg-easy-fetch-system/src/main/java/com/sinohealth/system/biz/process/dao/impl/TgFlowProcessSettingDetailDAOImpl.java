package com.sinohealth.system.biz.process.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.process.dao.TgFlowProcessSettingDetailDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessSettingDetail;
import com.sinohealth.system.biz.process.mapper.TgFlowProcessSettingDetailMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TgFlowProcessSettingDetailDAOImpl extends ServiceImpl<TgFlowProcessSettingDetailMapper, TgFlowProcessSettingDetail>
        implements TgFlowProcessSettingDetailDAO {
}
