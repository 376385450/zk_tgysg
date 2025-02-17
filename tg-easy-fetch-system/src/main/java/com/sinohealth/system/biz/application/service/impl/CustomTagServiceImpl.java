package com.sinohealth.system.biz.application.service.impl;

import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.application.entity.ProjectInfoEntity;
import com.sinohealth.system.biz.application.service.CustomTagService;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dict.dto.BizDataDictValDTO;
import com.sinohealth.system.biz.dict.service.BizDataDictService;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Kuangcp
 * 2024-10-17 19:42
 */
@Slf4j
@Service
public class CustomTagServiceImpl implements CustomTagService {

    @Autowired
    private TgCkProviderMapper ckProviderMapper;

    @Autowired
    private BizDataDictService bizDataDictService;
    @Autowired
    private AppProperties appProperties;

    @Override
    public List<String> listProjectName(String key) {
        String tabPart = appProperties.getTagTable() + " WHERE status = '1' ";
        if (StringUtils.isNotBlank(key)) {
            tabPart += " AND project_name like '%" + key + "%'";
        }
        List<LinkedHashMap<String, Object>> data = ckProviderMapper.selectAllDataFromCk(
                "SELECT DISTINCT project_name FROM " + tabPart);

        return data.stream()
                .map(v -> v.get("project_name")).filter(Objects::nonNull).map(Object::toString)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listTag(List<String> projectNames, String key) {
        String tabPart = appProperties.getTagTable() + " WHERE status = '1' ";
        if (CollectionUtils.isNotEmpty(projectNames)) {
            String in = projectNames.stream().map(v -> "'" + v + "'").collect(Collectors.joining(","));
            tabPart += " AND project_name IN (" + in + ")";
        }
        if (StringUtils.isNotBlank(key)) {
            tabPart += " AND zdy_cname like '%" + key + "%'";
        }
        List<LinkedHashMap<String, Object>> data = ckProviderMapper.selectAllDataFromCk(
                "SELECT DISTINCT zdy_cname FROM " + tabPart);

        return data.stream()
                .map(v -> v.get("zdy_cname")).filter(Objects::nonNull).map(Object::toString)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectInfoEntity listRelateInfos(List<String> projectNames, List<String> tags) {
        String tabPart = appProperties.getTagTable() + " WHERE status = '1' ";
        if (CollectionUtils.isNotEmpty(projectNames)) {
            String in = projectNames.stream().map(v -> "'" + v + "'").collect(Collectors.joining(","));
            tabPart += " AND project_name IN (" + in + ")";
        }
        if (CollectionUtils.isNotEmpty(tags)) {
            String in = tags.stream().map(v -> "'" + v + "'").collect(Collectors.joining(","));
            tabPart += " AND zdy_cname IN (" + in + ")";
        }

        List<LinkedHashMap<String, Object>> data = ckProviderMapper.selectAllDataFromCk(
                "SELECT customer_name,yy_table,bq_id FROM " + tabPart);

        List<String> clients = new ArrayList<>();
        List<String> tables = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        data.forEach(v -> {
            Optional.ofNullable(v.get("customer_name")).map(Object::toString).ifPresent(clients::add);
            Optional.ofNullable(v.get("yy_table")).map(Object::toString).ifPresent(tables::add);
            Optional.ofNullable(v.get("bq_id")).map(Object::toString).ifPresent(ids::add);
        });

        List<String> cascade = listCascadeField();
        boolean matchCascade = tags.stream().anyMatch(cascade::contains);
        return new ProjectInfoEntity()
                .setTagClient(clients.stream().distinct().collect(Collectors.joining(",")))
                .setTagTableName(tables.stream().distinct().collect(Collectors.joining(",")))
                .setTagIds(ids.stream().distinct().collect(Collectors.joining(",")))
                .setCascade(matchCascade)
                .setCascadeField("仅限【" + String.join("、", cascade) + "】指标进行级联");
    }

    @Override
    public List<String> listCascadeField() {
        AjaxResult<List<BizDataDictValDTO>> listResult = bizDataDictService.listDictVal(appProperties.getTagCascadeDictId());
        if (!listResult.isSuccess()) {
            return Collections.emptyList();
        }
        List<BizDataDictValDTO> list = listResult.getData();
        return Lambda.buildList(list, BizDataDictValDTO::getVal);
    }
}
