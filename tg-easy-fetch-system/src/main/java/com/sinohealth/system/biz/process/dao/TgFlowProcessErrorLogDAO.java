package com.sinohealth.system.biz.process.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.process.domain.TgFlowProcessErrorLog;

public interface TgFlowProcessErrorLogDAO extends IService<TgFlowProcessErrorLog> {
	/**
	 * 保存异常信息
	 * 
	 * @param bizId    业务编号
	 * @param category 类型
	 * @param msg      异常信息
	 */
	void save(Long bizId, String category, String msg);
}
