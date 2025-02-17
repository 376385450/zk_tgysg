package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.domain.ArkbiAnalysisDTO;
import com.sinohealth.system.domain.ArkbiAnalysisQuery;

import java.util.List;

public interface ArkbiAnalysisMapper extends BaseMapper<ArkbiAnalysis> {
    int deleteByPrimaryKey(Long id);

    int insert(ArkbiAnalysis record);

    int insertSelective(ArkbiAnalysis record);

    ArkbiAnalysis selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ArkbiAnalysis record);

    int updateByPrimaryKey(ArkbiAnalysis record);

    List<ArkbiAnalysisDTO> list(ArkbiAnalysisQuery query);
}