package com.sinohealth.system.biz.dict.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.dict.dao.FieldDictDAO;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.biz.dict.mapper.FieldDictMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:21
 */
@Repository
public class FieldDictDAOImpl extends ServiceImpl<FieldDictMapper, FieldDict> implements FieldDictDAO {

    @Override
    public List<FieldDict> queryByDictId(Collection<Long> id) {
        return this.baseMapper.selectList(new QueryWrapper<FieldDict>().lambda().in(FieldDict::getDictId, id));
    }

    @Override
    public List<FieldDict> queryAllForUnique(LambdaQueryWrapper<FieldDict> query) {
        return this.baseMapper.selectList(query.select(FieldDict::getId, FieldDict::getName, FieldDict::getFieldName, FieldDict::getBizType));
    }

    public Integer queryMaxSort() {
        return this.baseMapper.queryMaxSort();
    }
}
