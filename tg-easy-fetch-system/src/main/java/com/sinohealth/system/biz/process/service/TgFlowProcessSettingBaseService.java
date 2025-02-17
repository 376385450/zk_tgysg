package com.sinohealth.system.biz.process.service;

import com.sinohealth.system.biz.process.domain.TgFlowProcessSettingBase;

import java.util.List;

/**
 * 全流程管理基础设置信息
 */
public interface TgFlowProcessSettingBaseService {
    /**
     * 根据配置类型获取数据
     *
     * @param category 配置类型 @see com.sinohealth.common.enums.process.FlowProcessCategory
     * @return 配置信息
     */
    List<TgFlowProcessSettingBase> findByCategory(String category);

    /**
     * 根据名称获取基础配置
     *
     * @param name 名称
     * @return 配置信息
     */
    List<TgFlowProcessSettingBase> findByName(String name);

    /**
     * 保存或更新
     *
     * @param entity 实体信息
     */
    void saveOrUpdate(TgFlowProcessSettingBase entity);

    /**
     * 详情
     *
     * @param id 主键
     * @return 基础设置信息
     */
    TgFlowProcessSettingBase detail(Long id);
}
