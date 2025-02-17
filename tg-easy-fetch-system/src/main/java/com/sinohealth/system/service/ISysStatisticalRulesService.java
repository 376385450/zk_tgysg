package com.sinohealth.system.service;

import com.sinohealth.system.domain.SysStatisticalRules;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.dto.StatisticalInfoDto;
import com.sinohealth.system.dto.SysStatisticalRulesDto;
import com.sinohealth.system.dto.SysStatisticalTableDto;
import com.sinohealth.system.dto.TableInfoDto;
import com.sinohealth.system.vo.SysStatisticalResultVo;

import java.util.List;
import java.util.Map;

/**
 * 统计规则Service接口
 *
 * @author dataplatform
 * @date 2021-07-30
 */
public interface ISysStatisticalRulesService extends IService<SysStatisticalRules> {

    boolean addOrUpdate(SysStatisticalRulesDto bo);


    void addCronTask(SysStatisticalRules sysStatisticalRules);

    void addCronTaskAll();

    boolean statusWithValidByIdsJurisdiction(Long asList, String status);

    boolean statusWithValidByIds(Long asList, String status);

    boolean statisticsTask(Long id, String statisticalType);

    Map<String, String> getStatisticsPeriodType();

    Map<String, String> getStatisticsType();

    boolean addTable(SysStatisticalTableDto bo);

    boolean deleteTable(SysStatisticalTableDto bo);

    List<TableInfoDto> getTableList(List<Long> ids);

    List<StatisticalInfoDto> getStatisticalList();

    List<SysStatisticalResultVo> queryList(Long id);

    StatisticalInfoDto getStatistical(Long id);
}
