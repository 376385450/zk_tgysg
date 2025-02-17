package com.sinohealth.system.biz.dict.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.dict.dao.MetricsDictDAO;
import com.sinohealth.system.biz.dict.domain.MetricsDict;
import com.sinohealth.system.biz.dict.mapper.MetricsDictMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:15
 */
@Repository
public class MetricsDictDAOImpl extends ServiceImpl<MetricsDictMapper, MetricsDict> implements MetricsDictDAO {

    @Override
    public List<MetricsDict> queryAllForUnique(LambdaQueryWrapper<MetricsDict> query) {
        return this.baseMapper.selectList(query.select(MetricsDict::getId, MetricsDict::getName, MetricsDict::getFieldName, MetricsDict::getBizType));
    }

    @Override
    public Integer queryMaxSort() {
        return this.baseMapper.queryMaxSort();
    }
}
