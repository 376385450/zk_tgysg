package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.domain.TgDataRangeTemplate;
import com.sinohealth.system.domain.vo.TgDataRangeGroupVO;
import com.sinohealth.system.domain.vo.TgDataRangeTemplateVO;
import com.sinohealth.system.domain.vo.TgDataRangeVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangyanping
 * @date 2023/5/15 16:58
 */
public interface DataRangeTemplateService extends IService<TgDataRangeTemplate> {

    List<TgDataRangeVO> queryByIds(Collection<Long> templateIds);

    Set<Long> queryFieldIdsByIds(Collection<Long> templateIds);

    Set<Long> queryFieldIdsByIds(Long projectId, String bizType);

    /**
     * K：模版ID
     * V：sql1;sql2;sql3
     * 批量查询模版且返回对应CaseWhen SQL
     */
    Map<Long, String> buildTargetSqlMap(Collection<Long> templateIds, String applicationNo);

    String getCreateTablePre(String applicationNo, String caseWhenSql);

    String buildCreateTableSql(String applicationNo, TgDataRangeTemplateVO vo, Map<Long, FieldDict> dictMap);

    /**
     * 单元测试方法用
     */
    String buildSqlBydDataRangeGroup(TgDataRangeGroupVO groupData);

}
