package com.sinohealth.system.biz.msg.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.system.biz.msg.dao.MessageRecordDimDAO;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgMessageRecordDim;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.TgMessageRecordDimMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2023-12-28 14:53
 */
@Repository
public class MessageRecordDimDAOImpl
        extends ServiceImpl<TgMessageRecordDimMapper, TgMessageRecordDim>
        implements MessageRecordDimDAO {

    @Override
    public void createByRun(TgApplicationInfo tgApplicationInfo, Long adviceWho, ApplyDataStateEnum state) {
        SysUser sysUser = ThreadContextHolder.getSysUser();

        TgMessageRecordDim message = new TgMessageRecordDim();
        message.setApplicantId(sysUser.getUserId());
        message.setApplicantName(sysUser.getRealName());
        message.setApplicationType(tgApplicationInfo.getApplicationType());
        message.setApplicationId(tgApplicationInfo.getId());
        message.setProcessId(tgApplicationInfo.getProcessId());
        message.setApplyTime(tgApplicationInfo.getCreateTime());
        message.setProcessVersion(tgApplicationInfo.getProcessVersion());
        message.setType(tgApplicationInfo.getCurrentAuditProcessStatus());
        message.setAdviceWho(adviceWho);
        message.setDataState(state.name());

        message.insert();
    }
}
