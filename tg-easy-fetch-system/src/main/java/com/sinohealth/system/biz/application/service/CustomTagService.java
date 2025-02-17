package com.sinohealth.system.biz.application.service;

import com.sinohealth.common.config.AppProperties;
import com.sinohealth.system.biz.application.entity.ProjectInfoEntity;

import java.util.List;

/**
 * V1.9.8 自定义标签
 *
 * @author Kuangcp
 * 2024-10-17 17:05
 * @see AppProperties#tagTable 核心业务定义表
 */
public interface CustomTagService {

    List<String> listProjectName(String key);

    List<String> listTag(List<String> projectNames, String key);

    ProjectInfoEntity listRelateInfos(List<String> projectNames, List<String> tags);

    /**
     * 列出需要展示级联开关的字段
     */
    List<String> listCascadeField();
}
