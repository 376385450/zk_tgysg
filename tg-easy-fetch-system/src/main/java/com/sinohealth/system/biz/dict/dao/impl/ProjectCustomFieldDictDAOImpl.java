package com.sinohealth.system.biz.dict.dao.impl;

import com.alibaba.excel.util.BooleanUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.biz.application.dto.ApplicationGranularityDto;
import com.sinohealth.system.biz.application.dto.TemplateGranularityDto;
import com.sinohealth.system.biz.dict.dao.ProjectCustomFieldDictDAO;
import com.sinohealth.system.biz.dict.domain.ProjectCustomFieldDict;
import com.sinohealth.system.biz.dict.mapper.ProjectCustomFieldDictMapper;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import com.sinohealth.system.service.DataRangeTemplateService;
import com.sinohealth.system.service.impl.DataRangeTemplateServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-18 19:42
 */
@Repository
public class ProjectCustomFieldDictDAOImpl
        extends ServiceImpl<ProjectCustomFieldDictMapper, ProjectCustomFieldDict>
        implements ProjectCustomFieldDictDAO {

    @Autowired
    private DataRangeTemplateService dataRangeTemplateService;
    @Autowired
    private TgTemplateInfoMapper templateInfoMapper;

    /**
     * @see DataRangeTemplateServiceImpl#queryFieldIdsByIds(Long, String)
     */
    @Override
    public void saveMapping(TgApplicationInfo applyInfo, String bizType) {
        if (CollectionUtils.isEmpty(applyInfo.getGranularity())) {
            return;
        }

        Long userId = SecurityUtils.getUserId();
        List<Long> templateIds = applyInfo.getGranularity().stream()
                .map(ApplicationGranularityDto::getRangeTemplateId)
                .filter(Objects::nonNull).collect(Collectors.toList());

        TgTemplateInfo template = TgTemplateInfo.newInstance().selectById(applyInfo.getTemplateId());
        JsonBeanConverter.convert2Obj(template);

        List<TemplateGranularityDto> granularity = template.getGranularity();
        boolean enableRange;
        if (CollectionUtils.isNotEmpty(granularity)) {
            enableRange = granularity.stream().anyMatch(v -> BooleanUtils.isTrue(v.getEnableRangeTemplate()));
        } else {
            enableRange = false;
        }
        // 未开启自定列的模板产生的申请无需存储
        if (!enableRange) {
            return;
        }

        Set<Long> fieldIds = dataRangeTemplateService.queryFieldIdsByIds(templateIds);
        if (CollectionUtils.isEmpty(fieldIds)) {
            // 开启了，但是没有配置时的空占位，用于查询时的跳过
            ProjectCustomFieldDict dict = new ProjectCustomFieldDict();
            dict.setFieldDictId(ApplicationConst.RangeTemplate.EMPTY_USE);
            dict.setCreator(userId);
            dict.setBizType(bizType);
            dict.setApplicationId(applyInfo.getId());
            dict.setProjectId(applyInfo.getProjectId());
            this.save(dict);
            return;
        }
//        List<ProjectCustomFieldDict> existMappings = baseMapper.selectList(
//                new QueryWrapper<ProjectCustomFieldDict>().lambda()
//                        .select(ProjectCustomFieldDict::getProjectId, ProjectCustomFieldDict::getFieldDictId)
//                        .eq(ProjectCustomFieldDict::getProjectId, applyInfo.getProjectId())
//                        .in(ProjectCustomFieldDict::getFieldDictId, fieldIds)
//        );
//        Set<Long> existIds = Lambda.buildSet(existMappings, ProjectCustomFieldDict::getFieldDictId);

        List<ProjectCustomFieldDict> dicts = fieldIds.stream()
//                .filter(v -> !existIds.contains(v))
                .map(v -> {
                    ProjectCustomFieldDict dict = new ProjectCustomFieldDict();
                    dict.setFieldDictId(v);
                    dict.setCreator(userId);
                    dict.setBizType(bizType);
                    dict.setApplicationId(applyInfo.getId());
                    dict.setProjectId(applyInfo.getProjectId());
                    return dict;
                }).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(dicts)) {
            this.saveBatch(dicts);
        }
    }
}
