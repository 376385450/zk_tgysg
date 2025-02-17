package com.sinohealth.system.biz.dict.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.biz.dict.service.UniqueDao;

import java.util.Collection;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 09:53
 */
public interface FieldDictDAO extends IService<FieldDict>, UniqueDao<FieldDict> {

    List<FieldDict> queryByDictId(Collection<Long> id);

    Integer queryMaxSort();
}
