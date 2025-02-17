package com.sinohealth.system.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.system.dao.TgDataSyncFieldConfigDAO;
import com.sinohealth.system.domain.TgDataSyncFieldConfig;
import com.sinohealth.system.mapper.TgDataSyncFieldConfigMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/11/6
 */
@Repository
public class TgDataSyncFieldConfigDAOImpl extends ServiceImpl<TgDataSyncFieldConfigMapper, TgDataSyncFieldConfig> implements TgDataSyncFieldConfigDAO {


    @Override
    public void deleteBySyncApplicationId(Integer syncApplicationId) {
        final LambdaQueryWrapper<TgDataSyncFieldConfig> wq = Wrappers.<TgDataSyncFieldConfig>lambdaQuery()
                .eq(TgDataSyncFieldConfig::getSyncApplicationId, syncApplicationId);
        baseMapper.delete(wq);
    }

    @Override
    public List<TgDataSyncFieldConfig> queryByApplyId(Integer syncApplicationId) {
        final LambdaQueryWrapper<TgDataSyncFieldConfig> wq = Wrappers.<TgDataSyncFieldConfig>lambdaQuery()
                .eq(TgDataSyncFieldConfig::getSyncApplicationId, syncApplicationId);

        return baseMapper.selectList(wq);
    }
}
