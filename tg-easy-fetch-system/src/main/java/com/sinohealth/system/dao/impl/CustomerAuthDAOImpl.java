package com.sinohealth.system.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.system.dao.CustomerAuthDAO;
import com.sinohealth.system.domain.TgCustomerApplyAuth;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.SysCustomerAuthMapper;
import org.springframework.stereotype.Repository;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-09 17:50
 */
@Repository
public class CustomerAuthDAOImpl extends ServiceImpl<SysCustomerAuthMapper, TgCustomerApplyAuth> implements CustomerAuthDAO {

    @Override
    public int updateStatus(Long assetsId, Integer status) {
        Wrapper<TgCustomerApplyAuth> wrapper = Wrappers.lambdaQuery(TgCustomerApplyAuth.class)
                .eq(TgCustomerApplyAuth::getAssetsId, assetsId);
        TgCustomerApplyAuth updateEntity = new TgCustomerApplyAuth();
        updateEntity.setUpdateBy(ThreadContextHolder.getSysUser().getUserId());
        updateEntity.setStatus(status);
        return this.baseMapper.update(updateEntity, wrapper);
    }

    @Override
    public TgCustomerApplyAuth getSubByParentAuthId(Long userId, Long parentAuthId) {
        Wrapper wrapper = Wrappers.<TgCustomerApplyAuth>lambdaQuery()
                .eq(TgCustomerApplyAuth::getUserId, userId)
                .eq(TgCustomerApplyAuth::getParentCustomerAuthId, parentAuthId);
        return getOne(wrapper);
    }

    @Override
    public TgCustomerApplyAuth getDataAssets(Long userId, Long assetsId) {
        Wrapper wrapper = Wrappers.<TgCustomerApplyAuth>lambdaQuery()
                .eq(TgCustomerApplyAuth::getUserId, userId)
                .eq(TgCustomerApplyAuth::getAssetsId, assetsId)
                .eq(TgCustomerApplyAuth::getIcon, CommonConstants.ICON_DATA_ASSETS);
        return getOne(wrapper);
    }

}
