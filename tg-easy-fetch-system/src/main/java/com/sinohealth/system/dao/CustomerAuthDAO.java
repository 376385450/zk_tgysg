package com.sinohealth.system.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.TgCustomerApplyAuth;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-09 17:50
 */
public interface CustomerAuthDAO extends IService<TgCustomerApplyAuth> {

    int updateStatus(Long assetsId, Integer status);

    TgCustomerApplyAuth getSubByParentAuthId(Long userId, Long parentAuthId);

    TgCustomerApplyAuth getDataAssets(Long userId, Long assetsId);
}
