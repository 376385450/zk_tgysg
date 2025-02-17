package com.sinohealth.system.biz.msg.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgMessageRecordDim;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2023-12-28 14:52
 */
public interface MessageRecordDimDAO extends IService<TgMessageRecordDim> {

    void createByRun(TgApplicationInfo tgApplicationInfo, Long adviceWho, ApplyDataStateEnum state);
}
