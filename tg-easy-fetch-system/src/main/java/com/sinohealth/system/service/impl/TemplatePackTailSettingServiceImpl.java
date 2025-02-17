package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.project.dao.ProjectHelperDAO;
import com.sinohealth.system.biz.project.domain.ProjectDataAssetsRelate;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplatePackTailSetting;
import com.sinohealth.system.mapper.ProjectDataAssetsRelateMapper;
import com.sinohealth.system.mapper.TgTemplatePackTailSettingMapper;
import com.sinohealth.system.service.ITemplatePackTailSettingService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
@AllArgsConstructor
public class TemplatePackTailSettingServiceImpl implements ITemplatePackTailSettingService {
    private final TgTemplatePackTailSettingMapper tgTemplatePackTailSettingMapper;
    private final ApplicationDAO applicationDAO;
    private final ProjectHelperDAO projectHelperDAO;
    private final ProjectDataAssetsRelateMapper projectDataAssetsRelateMapper;
    private final UserDataAssetsDAO userDataAssetsDAO;

    public static final String NONE = "不打包";

    private static final List<String> non = Collections.singletonList(NONE);

    @Override
    public List<TgTemplatePackTailSetting> findByTemplateId(Long templateId) {
        return tgTemplatePackTailSettingMapper.selectList(new LambdaQueryWrapper<TgTemplatePackTailSetting>()
                .eq(TgTemplatePackTailSetting::getTemplateId, templateId).eq(TgTemplatePackTailSetting::getDeleted, 0));
    }

    @Override
    public void deleteByTemplateId(Long templateId) {
        tgTemplatePackTailSettingMapper.delete(new LambdaQueryWrapper<TgTemplatePackTailSetting>().eq(TgTemplatePackTailSetting::getTemplateId, templateId));
    }

    @Override
    public void batchSave(List<TgTemplatePackTailSetting> newSettings) {
        for (TgTemplatePackTailSetting newSetting : newSettings) {
            save(newSetting);
        }
    }

    @Override
    public void save(TgTemplatePackTailSetting newSetting) {
        if (Objects.nonNull(newSetting.getId())) {
            tgTemplatePackTailSettingMapper.updateById(newSetting);
        } else {
            tgTemplatePackTailSettingMapper.insert(newSetting);
        }

    }

    @Override
    public void deleteByIds(List<Long> ids) {
        tgTemplatePackTailSettingMapper.deleteBatchIds(ids);
    }

    @Override
    public TgTemplatePackTailSetting findById(Long id) {
        return tgTemplatePackTailSettingMapper.selectById(id);
    }

    @Override
    public List<String> distinctNameList() {
        Long userId = SecurityUtils.getUserId();

        Set<Long> projectIds = projectHelperDAO.queryProjects(userId);
        if (CollectionUtils.isEmpty(projectIds)) {
            return non;
        }
        List<ProjectDataAssetsRelate> relates = projectDataAssetsRelateMapper
                .selectList(new QueryWrapper<ProjectDataAssetsRelate>().lambda()
                        .in(ProjectDataAssetsRelate::getProjectId, projectIds)
                        .isNotNull(ProjectDataAssetsRelate::getUserAssetId)
                );
        Map<Long, List<ProjectDataAssetsRelate>> assetsProjectMap = relates.stream()
                .collect(Collectors.groupingBy(ProjectDataAssetsRelate::getUserAssetId));
        if (MapUtils.isEmpty(assetsProjectMap)) {
            return non;
        }
        List<UserDataAssets> assets = userDataAssetsDAO.lambdaQuery()
                .select(UserDataAssets::getSrcApplicationId)
                .in(UserDataAssets::getId, assetsProjectMap.keySet())
                .list();
        if (CollectionUtils.isEmpty(assets)) {
            return non;
        }

        List<Long> applyIds = Lambda.buildList(assets, UserDataAssets::getSrcApplicationId);
        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getPackTailId)
                .in(TgApplicationInfo::getId, applyIds)
                .isNotNull(TgApplicationInfo::getPackTailId)
                .list();
        List<Long> tailIds = Lambda.buildList(applyList, TgApplicationInfo::getPackTailId);
        if (CollectionUtils.isEmpty(tailIds)) {
            return non;
        }

        List<TgTemplatePackTailSetting> settings = tgTemplatePackTailSettingMapper
                .selectList(new QueryWrapper<TgTemplatePackTailSetting>()
                        .select(" DISTINCT name").in("id", tailIds));
        List<String> list = Lambda.buildList(settings, TgTemplatePackTailSetting::getName);
        list.add(0, NONE);
        return list;
    }
}
