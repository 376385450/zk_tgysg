package com.sinohealth.system.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.dao.TgTableApplicationMappingInfoDAO;
import com.sinohealth.system.domain.vo.TgTableApplicationMappingInfo;
import com.sinohealth.system.mapper.TgTableApplicationMappingInfoMapper;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 09:41
 */
@Repository
public class TgTableApplicationMappingInfoDAOImpl
        extends ServiceImpl<TgTableApplicationMappingInfoMapper, TgTableApplicationMappingInfo>
        implements TgTableApplicationMappingInfoDAO {

    @Override
    public List<TgTableApplicationMappingInfo> list(List<Long> assetsIds) {
        if (CollectionUtils.isEmpty(assetsIds)) {
            return Collections.emptyList();
        }
        Wrapper<TgTableApplicationMappingInfo> wrapper = Wrappers.<TgTableApplicationMappingInfo>lambdaQuery()
                .in(TgTableApplicationMappingInfo::getAssetsId, assetsIds);
        return list(wrapper);
    }

    @Override
    public TgTableApplicationMappingInfo getByAssetsId(Long dataAssetsId) {
        Wrapper<TgTableApplicationMappingInfo> wrapper = Wrappers.<TgTableApplicationMappingInfo>lambdaQuery()
                .eq(TgTableApplicationMappingInfo::getAssetsId, dataAssetsId);
        return getOne(wrapper);
    }

    @Override
    public void deleteById(Integer id) {
        this.baseMapper.deleteById(id);
    }
}
