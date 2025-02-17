package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.ArkbiAnalysis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ArkbiAnalysisService extends IService<ArkbiAnalysis> {
    ArkbiAnalysis getByParentId(Long parentAnalysisId);

    Set<String> queryExist(Collection<String> ids);

    /**
     * @return assetsVersion -> bi count
     */
    Map<String, Integer> countAssetsVersion(Collection<String> assetsVersions);

    List<ArkbiAnalysis> listNormalBiData(List<Long> assetIds);
}
