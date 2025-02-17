package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.mapper.ArkbiAnalysisMapper;
import com.sinohealth.system.service.ArkbiAnalysisService;
import com.sinohealth.system.service.IApplicationService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArkbiAnalysisServiceImpl extends ServiceImpl<ArkbiAnalysisMapper, ArkbiAnalysis> implements ArkbiAnalysisService {

    @Autowired
    private IApplicationService applicationService;

    @Override
    public ArkbiAnalysis getByParentId(Long parentAnalysisId) {
        return lambdaQuery()
                .eq(ArkbiAnalysis::getParentId, parentAnalysisId)
                .eq(ArkbiAnalysis::getStatus, 1)
                .last("limit 1")
                .orderByDesc(ArkbiAnalysis::getId)
                .one();
    }

    @Override
    public Set<String> queryExist(Collection<String> ids) {
        List<ArkbiAnalysis> exist = this.baseMapper.selectList(new QueryWrapper<ArkbiAnalysis>().lambda()
                .select(ArkbiAnalysis::getAnalysisId)
                .in(ArkbiAnalysis::getAnalysisId, ids)
        );
        return Lambda.buildSet(exist, ArkbiAnalysis::getAnalysisId);
    }

    @Override
    public Map<String, Integer> countAssetsVersion(Collection<String> assetsVersions) {
        if (CollectionUtils.isEmpty(assetsVersions)) {
            return Collections.emptyMap();
        }
        // 有效的，图表类型
        List<ArkbiAnalysis> exist = this.baseMapper.selectList(new QueryWrapper<ArkbiAnalysis>().lambda()
                .select(ArkbiAnalysis::getAssetsId)
                .in(ArkbiAnalysis::getAssetsId, assetsVersions)
                .eq(ArkbiAnalysis::getStatus, 1)
        );

        Map<String, List<ArkbiAnalysis>> arkMap = exist.stream().collect(Collectors.groupingBy(ArkbiAnalysis::getAssetsId));
        return arkMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> CollectionUtils.size(v.getValue()), (front, current) -> current));
    }

    @Override
    public List<ArkbiAnalysis> listNormalBiData(List<Long> assetIds) {
        Set<String> assetIdSet = applicationService.listAllNormalDataApplications(assetIds)
                .stream().filter(a -> a.getAssetsId() != null).map(a -> a.getAssetsId().toString()).collect(Collectors.toSet());
        List<ArkbiAnalysis> arkList = list(new QueryWrapper<ArkbiAnalysis>() {{
            eq("status", 1);
        }});
        List<ArkbiAnalysis> result = arkList.stream()
                .filter(a -> StringUtils.isNotBlank(a.getAssetsId()))
                .filter(a -> Arrays.stream(a.getAssetsId().split(",")).anyMatch(assetIdSet::contains))
                .collect(Collectors.toList());
        result.forEach(ark -> {
            // 经确认, 仪表板只能通过单一申请人数据生成, 所以只需要拿到任意申请就可以获取对应申请人id
            // 设置一个 0 值来区分异常数据而生成的仪表板
//            ark.setApplicantId(dataIdUserIdRecordMap.getOrDefault(Long.valueOf(ark.getAssetsId().split(",")[0]), 0L));
            ark.setApplicantId(ark.getCreateBy());
        });
        return result;
    }
}
