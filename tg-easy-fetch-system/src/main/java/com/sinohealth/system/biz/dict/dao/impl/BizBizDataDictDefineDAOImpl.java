package com.sinohealth.system.biz.dict.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.biz.dict.dao.BizDataDictDefineDAO;
import com.sinohealth.system.biz.dict.domain.BizDataDictDefine;
import com.sinohealth.system.biz.dict.mapper.BizDataDictDefineMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:52
 */
@Repository
public class BizBizDataDictDefineDAOImpl extends ServiceImpl<BizDataDictDefineMapper, BizDataDictDefine> implements BizDataDictDefineDAO {
    @Override
    public List<BizDataDictDefine> queryAllForUnique(LambdaQueryWrapper<BizDataDictDefine> query) {
        return this.baseMapper.selectList(query.select(BizDataDictDefine::getId, BizDataDictDefine::getName, BizDataDictDefine::getBizType));
    }
}
