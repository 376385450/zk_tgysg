package com.sinohealth.system.biz.process.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.process.dao.TgFlowProcessSettingBaseDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessSettingBase;
import com.sinohealth.system.biz.process.mapper.TgFlowProcessSettingBaseMapper;
import org.springframework.stereotype.Repository;

@Repository
public class TgFlowProcessSettingBaseDAOImpl extends ServiceImpl<TgFlowProcessSettingBaseMapper, TgFlowProcessSettingBase>
        implements TgFlowProcessSettingBaseDAO {
}
