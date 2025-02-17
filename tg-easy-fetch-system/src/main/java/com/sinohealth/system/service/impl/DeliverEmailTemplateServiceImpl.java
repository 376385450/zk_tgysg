package com.sinohealth.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.sinohealth.system.dao.DataDescriptionDAO;
import com.sinohealth.system.dao.TgDeliverEmailTemplateDAO;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.domain.TgDataDescription;
import com.sinohealth.system.domain.TgDeliverEmailTemplate;
import com.sinohealth.system.domain.value.deliver.DataSource;
import com.sinohealth.system.domain.value.deliver.DeliverDataSourceType;
import com.sinohealth.system.domain.value.deliver.DeliverRequestContextHolder;
import com.sinohealth.system.domain.value.deliver.datasource.ApplicationDataSource;
import com.sinohealth.system.dto.DeliverEmailTemplateDTO;
import com.sinohealth.system.dto.application.deliver.request.DeliverEmailTemplateQuery;
import com.sinohealth.system.dto.application.deliver.request.DeliverEmailTemplateUpdateRequestDTO;
import com.sinohealth.system.dto.application.deliver.request.DeliverPackBaseReq;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.service.ArkbiAnalysisService;
import com.sinohealth.system.service.DeliverEmailTemplateService;
import com.sinohealth.system.util.DataDescriptionHtmlUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 15:08
 */
@Service
@RequiredArgsConstructor
public class DeliverEmailTemplateServiceImpl implements DeliverEmailTemplateService {

    private final TgDeliverEmailTemplateDAO deliverEmailTemplateDAO;

    private final ArkbiAnalysisService arkbiAnalysisService;

    private final DataDescriptionDAO dataDescriptionDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliverEmailTemplateDTO getTemplate(DeliverEmailTemplateQuery query) {
        Pair<String, String> identifyPair = identifyId(query.getNodeIds(), query.getAssetsId());
        TgDeliverEmailTemplate byIdentify = deliverEmailTemplateDAO.getByIdentify(identifyPair.getKey());
        if (byIdentify == null) {
            // 创建空模板
            byIdentify = new TgDeliverEmailTemplate();
            byIdentify.setIdentifyId(identifyPair.getKey());
            byIdentify.setIdentifyContent(identifyPair.getValue());
            // 获取bi的分享链接
            DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.build(new DeliverPackBaseReq(query), null);
            Map<Long, String> arkbiMap = requestContextHolder.getDataSources().stream()
                    .filter(it -> DeliverDataSourceType.PANEL.equals(it.support()) || DeliverDataSourceType.CHART_ANALYSIS.equals(it.support()))
                    .collect(Collectors.toMap(DataSource::getId, DataSource::getName, (k1, k2) -> k1));
            if (MapUtils.isNotEmpty(arkbiMap)) {
                List<Long> arkbiIds = Lists.newArrayList(arkbiMap.keySet());
                List<ArkbiAnalysis> arkbiAnalyses = arkbiAnalysisService.listByIds(arkbiIds);
                StringBuilder sb = new StringBuilder();
                arkbiAnalyses.forEach(arkbi -> sb.append(arkbiMap.get(arkbi.getId())).append("分享地址：").append(arkbi.getShareUrl()));
                byIdentify.setContent(sb.toString());
            }
            // 设置数据说明文档
            byIdentify.setContent(getDataDescriptionHtml(requestContextHolder));
            deliverEmailTemplateDAO.save(byIdentify);
        }
        DeliverEmailTemplateDTO dto = new DeliverEmailTemplateDTO();
        BeanUtil.copyProperties(byIdentify, dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(DeliverEmailTemplateUpdateRequestDTO requestDTO) {
        Pair<String, String> identifyPair = identifyId(requestDTO.getNodeIds(), requestDTO.getAssetsId());
        TgDeliverEmailTemplate entity = deliverEmailTemplateDAO.getByIdentify(identifyPair.getKey());
        if (entity != null) {
            // 可更新为null
            LambdaUpdateWrapper<TgDeliverEmailTemplate> updateWrapper = Wrappers.lambdaUpdate(TgDeliverEmailTemplate.class)
                    .set(TgDeliverEmailTemplate::getTitle, requestDTO.getTitle())
                    .set(TgDeliverEmailTemplate::getReceiveMails, JSON.toJSONString(requestDTO.getReceiveMails()))
                    .set(TgDeliverEmailTemplate::getContent, requestDTO.getContent())
                    .set(TgDeliverEmailTemplate::getUpdateBy, ThreadContextHolder.getSysUser().getUserId())
                    .eq(TgDeliverEmailTemplate::getId, entity.getId());
            deliverEmailTemplateDAO.update(updateWrapper);
        }

    }

    /**
     * 生成唯一标识
     */
    public Pair<String, String> identifyId(List<String> ids, Long applicationId) {
        String first = CollectionUtils.isNotEmpty(ids) ? JSON.toJSONString(ids) : "";
        String second = applicationId != null ? String.valueOf(applicationId) : "";
        String s = first + "_" + second;
        Map<String, Object> map = new HashMap<>();
        map.put("ids", ids);
        map.put("applicationId", applicationId);
        return new Pair<>(Base64.decodeStr(s), JSON.toJSONString(map));
    }

    private String getDataDescriptionHtml(DeliverRequestContextHolder requestContextHolder) {
        List<ApplicationDataSource> applicationDataSources = requestContextHolder.getApplicationDataSources();
        if (CollectionUtils.isNotEmpty(applicationDataSources)) {
            List<Long> collect = applicationDataSources.stream().map(ApplicationDataSource::getAssetsId).distinct().collect(Collectors.toList());
            List<TgDataDescription> descriptions = dataDescriptionDAO.listByAssetsIds(collect);
            return DataDescriptionHtmlUtil.html(descriptions);
        }
        return "";
    }


}
