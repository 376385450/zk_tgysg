package com.sinohealth.system.biz.rangepreset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sinohealth.bi.data.Filter;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.preset.RangePresetTypeEnum;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.rangepreset.dao.RangePresetDAO;
import com.sinohealth.system.biz.rangepreset.domain.RangePreset;
import com.sinohealth.system.biz.rangepreset.domain.base.CommonPreset;
import com.sinohealth.system.biz.rangepreset.dto.RangePresetDTO;
import com.sinohealth.system.biz.rangepreset.dto.request.RangePresetPageRequest;
import com.sinohealth.system.biz.rangepreset.dto.request.RangePresetUpsertRequest;
import com.sinohealth.system.biz.rangepreset.service.RangePresetService;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.util.ApplicationSqlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-24 14:39
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RangePresetServiceImpl implements RangePresetService {

    private final RangePresetDAO rangePresetDAO;
    private final TgTemplateInfoMapper templateInfoMapper;


    private final ISysUserService userService;

    public Pair<List<TgTemplateInfo>, TgTemplateInfo> queryRelationTemp(Long templateId) {
        if (Objects.isNull(templateId)) {
            return null;
        }

        TgTemplateInfo base = templateInfoMapper.selectOne(new QueryWrapper<TgTemplateInfo>().lambda()
                .select(TgTemplateInfo::getBizType, TgTemplateInfo::getBaseTableName,
                        TgTemplateInfo::getTemplateName, TgTemplateInfo::getTemplateType, TgTemplateInfo::getBaseTableId)
                .eq(TgTemplateInfo::getId, templateId));
        if (Objects.equals(base.getTemplateType(), TemplateTypeEnum.wide_table.name())) {
            List<TgTemplateInfo> infos = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                    .select(TgTemplateInfo::getId, TgTemplateInfo::getBizType, TgTemplateInfo::getTemplateName, TgTemplateInfo::getBaseTableName)
                    .eq(TgTemplateInfo::getBaseTableId, base.getBaseTableId()));
            return Pair.create(infos, null);
        } else {
            return Pair.create(null, base);
        }
    }

    @Override
    public AjaxResult<String> queryByTempId(Long templateId) {
        Pair<List<TgTemplateInfo>, TgTemplateInfo> condition = this.queryRelationTemp(templateId);
        if (Objects.isNull(condition)) {
            return AjaxResult.error("模板不存在");
        }

        List<TgTemplateInfo> infos = condition.getKey();
        if (CollectionUtils.isEmpty(infos)) {
            TgTemplateInfo base = condition.getValue();
            infos = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                    .select(TgTemplateInfo::getBizType, TgTemplateInfo::getTemplateName, TgTemplateInfo::getBaseTableName)
                    .eq(TgTemplateInfo::getBizType, base.getBizType())
                    .in(TgTemplateInfo::getTemplateType, TemplateTypeEnum.SHARD_PRESET)
            );
        }
        return AjaxResult.success(null, infos.stream()
//                .filter(v -> !Objects.equals(v.getId(), templateId))
                .map(TgTemplateInfo::getTemplateName)
                .collect(Collectors.joining("、")));
    }

    @Override
    public AjaxResult<IPage<RangePresetDTO>> pageQuery(RangePresetPageRequest request) {
        LambdaQueryWrapper<RangePreset> wrapper = new QueryWrapper<RangePreset>().lambda();

        IPage<RangePreset> page = rangePresetDAO.getBaseMapper().selectPage(request.buildPage(), wrapper
                .and(StringUtils.isNotBlank(request.getBizType()), v -> v.apply(request.buildBizType()))
                .eq(StringUtils.isNotBlank(request.getGranularity()), RangePreset::getGranularity, request.getGranularity())
                .like(StringUtils.isNotBlank(request.getSearchContent()), RangePreset::getName, request.getSearchContent())
                .eq(Objects.nonNull(request.getTemplateId()), RangePreset::getTemplateId, request.getTemplateId())
                .eq(StringUtils.isNotBlank(request.getRangeType()), RangePreset::getRangeType, request.getRangeType())
                .and(v -> v.eq(RangePreset::getRangeType, RangePresetTypeEnum.common)
                        .or().eq(RangePreset::getCreator, SecurityUtils.getUserId()))
                .orderByDesc(RangePreset::getUpdateTime)
        );

        Set<Long> userIds = page.getRecords().stream()
                .map(RangePreset::getCreator)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nameMap = userService.selectUserNameMapByIds(userIds);

        List<RangePreset> records = page.getRecords();
        Map<Long, String> tempNameMap;
        if (CollectionUtils.isNotEmpty(records)) {
            // 处理模板名称
            Set<Long> templateIds = Lambda.buildSet(records, RangePreset::getTemplateId);
            List<TgTemplateInfo> infos = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                    .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName, TgTemplateInfo::getBaseTableId)
                    .in(TgTemplateInfo::getId, templateIds));
            tempNameMap = Lambda.buildMap(infos, TgTemplateInfo::getId, TgTemplateInfo::getTemplateName);
        } else {
            tempNameMap = Collections.emptyMap();
        }

        return AjaxResult.success(PageUtil.convertMap(page, v -> {
            RangePresetDTO dto = new RangePresetDTO();
            BeanUtils.copyProperties(v, dto);
            dto.setUpdater(nameMap.get(v.getUpdater()));
            dto.setCreator(nameMap.get(v.getCreator()));
            dto.setTemplate(tempNameMap.get(v.getTemplateId()));
            return dto;
        }));
    }

    /**
     * 工作流类型的模板 查询关联模板
     */
    public void handleScheduler(Map<Long, String> tempUseMap, List<? extends CommonPreset> scheduler) {
        if (CollectionUtils.isNotEmpty(scheduler)) {
            Set<String> bizType = scheduler.stream().map(CommonPreset::getBizType).collect(Collectors.toSet());

            List<TgTemplateInfo> allTemp = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                    .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName, TgTemplateInfo::getBizType)
                    .in(TgTemplateInfo::getBizType, bizType)
                    .ne(TgTemplateInfo::getTemplateType, TemplateTypeEnum.wide_table.name())
            );
            Map<Long, String> allMap = Lambda.buildMap(allTemp, TgTemplateInfo::getId, TgTemplateInfo::getBizType);

            Map<String, List<TgTemplateInfo>> bizMap = allTemp.stream().collect(Collectors.groupingBy(TgTemplateInfo::getBizType));

            for (CommonPreset rangePreset : scheduler) {
                String biz = allMap.get(rangePreset.getTemplateId());
                List<TgTemplateInfo> use = bizMap.get(biz);

                String names = use.stream()
//                        .filter(x -> !Objects.equals(x.getId(), rangePreset.getTemplateId()))
                        .map(TgTemplateInfo::getTemplateName)
                        .collect(Collectors.joining("、"));
                tempUseMap.put(rangePreset.getTemplateId(), names);
            }
        }
    }

    /**
     * 查询宽表模式关联的模板
     */
    public void handleWideTemplate(Map<Long, String> tempUseMap, Map<Long, TgTemplateInfo> templateMap, List<? extends CommonPreset> wide) {
        if (CollectionUtils.isNotEmpty(wide)) {
            Set<Long> tableIds = wide.stream().map(v -> templateMap.get(v.getTemplateId())).filter(Objects::nonNull)
                    .map(TgTemplateInfo::getBaseTableId).collect(Collectors.toSet());
            List<TgTemplateInfo> tables = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                    .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName, TgTemplateInfo::getBaseTableId)
                    .in(TgTemplateInfo::getBaseTableId, tableIds));

            Map<Long, TgTemplateInfo> tempMap = Lambda.buildMap(tables, TgTemplateInfo::getId);
            Map<Long, List<TgTemplateInfo>> tableMap = tables.stream().collect(Collectors.groupingBy(TgTemplateInfo::getBaseTableId));

            for (CommonPreset rangePreset : wide) {
                Optional.ofNullable(tempMap.get(rangePreset.getTemplateId()))
                        .map(TgTemplateInfo::getBaseTableId)
                        .map(tableMap::get)
                        .ifPresent(v -> {
                            String names = v.stream()
//                                    .filter(x -> !Objects.equals(x.getId(), rangePreset.getTemplateId()))
                                    .map(TgTemplateInfo::getTemplateName)
                                    .collect(Collectors.joining("、"));
                            tempUseMap.put(rangePreset.getTemplateId(), names);
                        });
            }
        }
    }

    @Override
    public AjaxResult<Void> upsert(RangePresetUpsertRequest request) {
        String filters = request.getFilters();

        FilterDTO filterDTO = JsonUtils.parse(filters, new TypeReference<FilterDTO>() {
        });
        Filter targetFilter = new Filter();
        ApplicationSqlUtil.convertToFilter(filterDTO, targetFilter);
        ApplicationSqlUtil.FilterContext context = new ApplicationSqlUtil.FilterContext();
        boolean hasEmptyNode = ApplicationSqlUtil.hasEmptyNode(targetFilter, context);
        if (hasEmptyNode || !context.isHasItem()) {
            return AjaxResult.error("请补全筛选字段或筛选条件");
        }

//        TgTemplateInfo template = templateInfoMapper.selectById(request.getTemplateId());
//        if (Objects.isNull(template)) {
//            return AjaxResult.error("模板不存在");
//        }

        Long userId = SecurityUtils.getUserId();
        RangePreset entity = new RangePreset();
        BeanUtils.copyProperties(request, entity);
        if (Objects.isNull(request.getId())) {
            entity.setCreator(userId);
        }
        entity.setUpdater(userId);
//        entity.setTemplateType(template.getTemplateType());
        rangePresetDAO.saveOrUpdate(entity);

        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> deleteById(Long id) {
        if (Objects.isNull(id)) {
            return AjaxResult.error("参数缺失");
        }
        rangePresetDAO.removeById(id);
        return AjaxResult.succeed();
    }
}
