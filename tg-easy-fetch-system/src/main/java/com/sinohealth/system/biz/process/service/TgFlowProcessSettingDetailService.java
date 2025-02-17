package com.sinohealth.system.biz.process.service;

import com.sinohealth.system.biz.process.domain.TgFlowProcessSettingDetail;

import java.util.List;

public interface TgFlowProcessSettingDetailService {
    /**
     * 根据基础id删除详细信息
     *
     * @param baseId 配置基础id
     */
    void removeByBaseId(Long baseId);

    /**
     * 批量保存
     *
     * @param details 详细信息
     */
    void saveBatch(List<TgFlowProcessSettingDetail> details);

    /**
     * 根据基础设置编号，获取详细信息
     *
     * @param baseId 基础设置编号
     * @return 详细信息
     */
    List<TgFlowProcessSettingDetail> getByBaseId(Long baseId);
}
