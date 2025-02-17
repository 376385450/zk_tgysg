package com.sinohealth.system.biz.process.dao.impl;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.process.dao.TgFlowProcessErrorLogDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessErrorLog;
import com.sinohealth.system.biz.process.mapper.TgFlowProcessErrorLogMapper;

@Repository
public class TgFlowProcessErrorLogDAOImpl extends ServiceImpl<TgFlowProcessErrorLogMapper, TgFlowProcessErrorLog>
		implements TgFlowProcessErrorLogDAO {
	@Override
	public void save(Long bizId, String category, String msg) {
		TgFlowProcessErrorLog log = new TgFlowProcessErrorLog();
		log.setBizId(bizId);
		log.setCategory(category);
		log.setErrorMsg(msg);
		this.save(log);
	}
}
