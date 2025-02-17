package com.sinohealth.system.biz.dict.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.dict.domain.MetricsDict;
import com.sinohealth.system.biz.dict.service.UniqueDao;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:05
 */
public interface MetricsDictDAO extends IService<MetricsDict>, UniqueDao<MetricsDict> {

    Integer queryMaxSort();
}
