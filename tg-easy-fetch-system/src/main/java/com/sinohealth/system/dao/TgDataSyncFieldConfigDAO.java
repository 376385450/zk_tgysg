package com.sinohealth.system.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.TgDataSyncFieldConfig;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/11/6
 */
public interface TgDataSyncFieldConfigDAO extends IService<TgDataSyncFieldConfig> {

    void deleteBySyncApplicationId(Integer syncApplicationId);

    List<TgDataSyncFieldConfig> queryByApplyId(Integer syncApplicationId);

}
