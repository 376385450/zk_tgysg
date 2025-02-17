package com.sinohealth.system.biz.dict.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.dict.domain.BizDataDictDefine;
import com.sinohealth.system.biz.dict.service.UniqueDao;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:51
 */
public interface BizDataDictDefineDAO extends IService<BizDataDictDefine>, UniqueDao<BizDataDictDefine> {
}
