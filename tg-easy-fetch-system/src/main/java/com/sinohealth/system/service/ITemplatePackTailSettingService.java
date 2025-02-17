package com.sinohealth.system.service;

import com.sinohealth.system.domain.TgTemplatePackTailSetting;

import java.util.List;

public interface ITemplatePackTailSettingService {
    /**
     * 查询模板打包配置表
     *
     * @param templateId 模板比那吗
     * @return 模板打包配置
     */
    List<TgTemplatePackTailSetting> findByTemplateId(Long templateId);

    /**
     * 根据模板id删除模板打包配置信息
     *
     * @param templateId 模板id
     */
    void deleteByTemplateId(Long templateId);

    /**
     * 批量保存
     *
     * @param newSettings 新配置
     */
    void batchSave(List<TgTemplatePackTailSetting> newSettings);

    /**
     * 批量保存
     *
     * @param newSetting 新配置
     */
    void save(TgTemplatePackTailSetting newSetting);

    /**
     * 批量删除
     *
     * @param ids 主键集合
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据id获取打包配置
     *
     * @param id 主键
     * @return 打包配置
     */
    TgTemplatePackTailSetting findById(Long id);

    List<String> distinctNameList();
}
