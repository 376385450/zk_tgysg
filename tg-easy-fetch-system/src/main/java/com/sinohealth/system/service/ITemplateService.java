package com.sinohealth.system.service;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dict.dto.FieldDictDTO;
import com.sinohealth.system.biz.template.dto.PowerPushBiTemplateVO;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.dto.template.TemplateAuditProcessEasyDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ITemplateService {
    AjaxResult<TgTemplateInfo> upsertTemplate(TgTemplateInfo templateInfo, Boolean confirmUpgrade);

    Object query(Map<String, Object> params);

    List<TgTemplateInfo> queryNameByIds(Collection<Long> ids);

    Object delete(Map<String, Object> params);

    void updateProcessInfoById(Long templateId, Long processId, Integer sortIndex);

    /**
     * 模板为主体
     */
    List<TemplateAuditProcessEasyDto> queryByBaseTableId(Long baseTableId);

    /**
     * 流程为主体
     */
    List<TemplateAuditProcessEasyDto> queryProcessesByBaseTableId(Long baseTableId);

    List<TemplateAuditProcessEasyDto> queryProcessesByBaseTableIds(List<Long> baseTableIds);

    Object getTemplateFieldMeta(String page, List<Long> ids, Long templateId);

    Object updateStatus(Long templateId);

    List<TgTemplateInfo> queryByModelIds(List<Long> modelIds);

    List<TgTemplateInfo> getUnLinkedData(List<Long> modelAssetIds);

    List<PowerPushBiTemplateVO> queryForPushBi(String bizType);

    List<PowerPushBiTemplateVO> queryNameList(String bizType);

    List<TgTemplateInfo> listAllAssetsTable(String bizType);

    /**
     * 工作流编号
     *
     * @param flowId 工作流
     * @return 模板信息
     */
    List<TgTemplateInfo> listByFlowId(Long flowId);

    /**
     * 根据编号集合获取模板信息
     *
     * @param ids 主键信息
     * @return 模板信息
     */
    List<TgTemplateInfo> listByIds(List<Long> ids);

    /**
     * 获取分布信息字段
     *
     * @return 分布信息字段
     */
    List<FieldDictDTO> distributedFieldList();
    
    AjaxResult<String> queryBizType(Long templateId);
}
