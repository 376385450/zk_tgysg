package com.sinohealth.system.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.dao.DataDescriptionDAO;
import com.sinohealth.system.domain.TgDataDescription;
import com.sinohealth.system.mapper.TgDataDescriptionMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-07 14:19
 */
@Repository
public class DataDescriptionDAOImpl extends ServiceImpl<TgDataDescriptionMapper, TgDataDescription> implements DataDescriptionDAO {

    @Override
    public TgDataDescription getByAssetsId(Long assetsId) {
        Wrapper<TgDataDescription> wrapper = Wrappers.lambdaQuery(TgDataDescription.class)
                .eq(TgDataDescription::getAssetsId, assetsId);
        return this.baseMapper.selectOne(wrapper);
    }

    @Override
    public List<TgDataDescription> listByAssetsIds(List<Long> assetsIds) {
        if (CollectionUtils.isEmpty(assetsIds)) {
            return Collections.emptyList();
        }
        Wrapper<TgDataDescription> wrapper = Wrappers.lambdaQuery(TgDataDescription.class)
                .in(TgDataDescription::getAssetsId, assetsIds);
        return this.baseMapper.selectList(wrapper);
    }

}
