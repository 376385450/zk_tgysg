package com.sinohealth.system.biz.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.system.biz.application.dto.ApplicationGranularityDto;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dict.dao.BizDataDictDefineDAO;
import com.sinohealth.system.biz.dict.dao.FieldDictDAO;
import com.sinohealth.system.biz.dict.dao.MetricsDictDAO;
import com.sinohealth.system.biz.dict.domain.BizDataDictDefine;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.biz.dict.domain.MetricsDict;
import com.sinohealth.system.biz.dict.dto.FieldDictDTO;
import com.sinohealth.system.biz.dict.dto.request.FieldDictBatchSaveRequest;
import com.sinohealth.system.biz.dict.dto.request.FieldDictPageRequest;
import com.sinohealth.system.biz.dict.dto.request.FieldListRequest;
import com.sinohealth.system.biz.dict.service.FieldDictService;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.mapper.TableFieldInfoMapper;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import com.sinohealth.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 15:41
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FieldDictServiceImpl implements FieldDictService {

    private final BizDataDictDefineDAO bizDataDictDefineDAO;
    private final FieldDictDAO fieldDictDAO;
    private final MetricsDictDAO metricsDictDAO;
    private final ISysUserService userService;
    private final TableFieldInfoMapper tableFieldInfoMapper;
    private final TgTemplateInfoMapper templateInfoMapper;
    private final DictUniqueAdapter dictUniqueAdapter;
    private final AppProperties appProperties;

    @Override
    public AjaxResult<IPage<FieldDictDTO>> pageQuery(FieldDictPageRequest request) {
        IPage<FieldDict> page = fieldDictDAO.getBaseMapper().selectPage(request.buildPage(),
                new QueryWrapper<FieldDict>().lambda()
                        .and(StringUtils.isNotBlank(request.getSearchContent()),
                                v -> v.like(FieldDict::getFieldName, request.getSearchContent())
                                        .or().like(FieldDict::getName, request.getSearchContent())
                        )
                        .eq(StringUtils.isNotBlank(request.getGranularity()), FieldDict::getGranularity, request.getGranularity())
                        .eq(StringUtils.isNotBlank(request.getUseWay()), FieldDict::getUseWay, request.getUseWay())
                        .and(org.apache.commons.lang3.StringUtils.isNotBlank(request.getBizType()),
                                v -> v.apply(request.buildBizType()))
                        .orderByAsc(FieldDict::getSort, FieldDict::getId)
        );

        List<Long> dictIds = page.getRecords().stream().map(FieldDict::getDictId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> dictNameMap;
        if (CollectionUtils.isNotEmpty(dictIds)) {
            List<BizDataDictDefine> defines = bizDataDictDefineDAO.getBaseMapper()
                    .selectList(new QueryWrapper<BizDataDictDefine>().lambda().in(BizDataDictDefine::getId, dictIds));
            dictNameMap = defines.stream().collect(Collectors.toMap(BizDataDictDefine::getId,
                    BizDataDictDefine::getName, (front, current) -> current));
        } else {
            dictNameMap = Collections.emptyMap();
        }


        Set<Long> userIds = page.getRecords().stream()
                .flatMap(v -> Stream.of(v.getCreator(), v.getUpdater()))
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nameMap = userService.selectUserNameMapByIds(userIds);

        return AjaxResult.success(PageUtil.convertMap(page, v -> {
            FieldDictDTO dto = new FieldDictDTO();
            BeanUtils.copyProperties(v, dto);
            dto.setUpdater(nameMap.get(v.getUpdater()));
            dto.setCreator(nameMap.get(v.getCreator()));
            dto.setDictName(dictNameMap.get(v.getDictId()));
            return dto;
        }));
    }

    @Override
    public AjaxResult<List<FieldDictDTO>> listQuery(FieldListRequest request) {
        if (CollectionUtils.isEmpty(request.getIds())) {
            return AjaxResult.error("参数为空");
        }

        List<FieldDict> dicts = fieldDictDAO.list(new QueryWrapper<FieldDict>().lambda()
                .in(FieldDict::getId, request.getIds()));

        Set<Long> userIds = dicts.stream()
                .flatMap(v -> Stream.of(v.getCreator(), v.getUpdater()))
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nameMap = userService.selectUserNameMapByIds(userIds);

        return AjaxResult.success(dicts.stream().map(v -> {
            FieldDictDTO dto = this.buildDto(v);
            dto.setCreator(nameMap.get(v.getCreator()));
            dto.setUpdater(nameMap.get(v.getUpdater()));
            return dto;
        }).collect(Collectors.toList()));
    }

    @Override
    public List<FieldDictDTO> listAll() {
        List<FieldDict> dicts = fieldDictDAO.list(new QueryWrapper<FieldDict>().lambda().select(
                        FieldDict::getId, FieldDict::getFieldName, FieldDict::getName, FieldDict::getDescription,
                        FieldDict::getGranularity, FieldDict::getBizType, FieldDict::getUseWay, FieldDict::getCreator)
                .orderByAsc(FieldDict::getSort));
        Set<Long> userIds = dicts.stream()
                .flatMap(v -> Stream.of(v.getCreator()))
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nameMap = userService.selectUserNameMapByIds(userIds);

        return dicts.stream().map(v -> {
            FieldDictDTO dto = this.buildDto(v);
            dto.setCreator(nameMap.get(v.getCreator()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Integer countAll() {
        return fieldDictDAO.count();
    }

    @Override
    public List<FieldDict> selectAllIds() {
        return fieldDictDAO.list(new QueryWrapper<FieldDict>().lambda().select(FieldDict::getId, FieldDict::getDictId));
    }

    @Override
    public void fillSort(List<FieldDict> fields) {
        fieldDictDAO.updateBatchById(fields);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> edit(FieldDictDTO dictDTO) {
        FieldDict dict = this.buildDomain(dictDTO);
        List<FieldDict> repeat = dictUniqueAdapter.checkRepeat(fieldDictDAO, Collections.singletonList(dict));
        if (CollectionUtils.isNotEmpty(repeat)) {
            String msg = repeat.stream().map(v -> String.format("【%s(%s)】", v.getFieldName(), v.getName()))
                    .collect(Collectors.joining("、"));
            return AjaxResult.error("字段 " + msg + " 存在重复值，请进行确认，谢谢。");
        }
        fieldDictDAO.saveOrUpdate(dict);
        return AjaxResult.succeed();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> batchSave(FieldDictBatchSaveRequest saveRequest) {
        List<FieldDictDTO> fields = saveRequest.getFields();

        List<FieldDict> entities = fields.stream().map(this::buildDomain).collect(Collectors.toList());
        List<FieldDict> repeat = dictUniqueAdapter.checkRepeat(fieldDictDAO, entities);
        if (CollectionUtils.isNotEmpty(repeat)) {
            String msg = repeat.stream().map(v -> String.format("【%s(%s)】", v.getFieldName(), v.getName()))
                    .collect(Collectors.joining("、"));
            return AjaxResult.error("字段 " + msg + " 存在重复值，请进行确认，谢谢。");
        }
        Integer maxSort = fieldDictDAO.queryMaxSort();
        for (FieldDict entity : entities) {
            entity.setSort(++maxSort);
        }
        fieldDictDAO.saveBatch(entities);
        return AjaxResult.succeed();
    }

    private FieldDict buildDomain(FieldDictDTO dto) {
        Long userId = SecurityUtils.getUserId();
        FieldDict dict = new FieldDict();
        BeanUtils.copyProperties(dto, dict);
        if (Objects.isNull(dict.getId())) {
            dict.setCreator(userId);
        }

        dict.setUpdater(userId);
        return dict;
    }

    private FieldDictDTO buildDto(FieldDict dto) {
        FieldDictDTO dict = new FieldDictDTO();
        BeanUtils.copyProperties(dto, dict);
        return dict;
    }

    @Override
    public AjaxResult<Void> deleteById(Long id) {
        Integer count = tableFieldInfoMapper.selectCount(new QueryWrapper<TableFieldInfo>().lambda()
                .eq(TableFieldInfo::getRelationColId, id));
        if (Objects.nonNull(count) && count > 0) {
            return AjaxResult.error("当前字段被表单管理引用，请取消引用后再删除");
        }

        Integer existMetrics = metricsDictDAO.getBaseMapper().selectCount(new QueryWrapper<MetricsDict>().lambda()
                .eq(MetricsDict::getColName, id));
        if (Objects.nonNull(existMetrics) && existMetrics > 0) {
            return AjaxResult.error("当前字段被指标库引用，请取消引用后再删除");
        }
        if (Objects.equals(appProperties.getDistributedTypeFieldId(), id)
                || Objects.equals(appProperties.getDistributedQjFieldId(), id)) {
            return AjaxResult.error("当前字段用作分布式列，不允许删除");
        }

        List<TgTemplateInfo> infos = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                .select(TgTemplateInfo::getTemplateName, TgTemplateInfo::getGranularityJson)
                .isNotNull(TgTemplateInfo::getGranularityJson));
        List<String> names = new ArrayList<>();
        for (TgTemplateInfo info : infos) {
            JsonBeanConverter.convert2Obj(info);
            boolean hasDep = info.getGranularity().stream().filter(v -> CollectionUtils.isNotEmpty(v.getDetails()))
                    .flatMap(v -> v.getDetails().stream())
                    .anyMatch(v -> (CollectionUtils.isNotEmpty(v.getOptions()) && v.getOptions().contains(id))
                            || (CollectionUtils.isNotEmpty(v.getRequired()) && v.getRequired().contains(id)));
            if (hasDep) {
                names.add(info.getTemplateName());
            }
        }
        if (CollectionUtils.isNotEmpty(names)) {
            return AjaxResult.error("当前字段被" + String.join("、", names) + "模板引用，不允许删除");
        }
        fieldDictDAO.removeById(id);

        return AjaxResult.succeed();
    }


    /**
     * 查询字段库 字段，填入名字
     */
    @Override
    public void fillFieldNameForFilter(ApplicationGranularityDto... arr) {
        if (Objects.isNull(arr)) {
            return;
        }

        Set<Long> fields = new HashSet<>();
        for (ApplicationGranularityDto dto : arr) {
            if (Objects.isNull(dto) || Objects.isNull(dto.getFilter())) {
                continue;
            }
            this.buildFieldList(fields, dto.getFilter());
        }

        Map<Long, String> result = Lambda.queryMapIfExist(fields, fieldDictDAO::listByIds,
                FieldDict::getId, FieldDict::getFieldName);
        if (MapUtils.isEmpty(result)) {
            return;
        }

        for (ApplicationGranularityDto dto : arr) {
            if (Objects.isNull(dto) || Objects.isNull(dto.getFilter())) {
                continue;
            }

            this.fillFieldNameForFilter(result, dto.getFilter());
        }
    }

    @Override
    public void fillFieldNameForFilter(List<FilterDTO> filters) {
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(filters)) {
            return;
        }

        Set<Long> fields = new HashSet<>();
        for (FilterDTO filter : filters) {
            this.buildFieldList(fields, filter);
        }

        Map<Long, String> result = Lambda.queryMapIfExist(fields, fieldDictDAO::listByIds,
                FieldDict::getId, FieldDict::getFieldName);
        if (MapUtils.isEmpty(result)) {
            return;
        }

        for (FilterDTO filter : filters) {
            this.fillFieldNameForFilter(result, filter);
        }
    }

    private void buildFieldList(Set<Long> fields, FilterDTO filter) {
        List<FilterDTO> filters = filter.getFilters();
        if (CollectionUtils.isNotEmpty(filters)) {
            for (FilterDTO filterDTO : filters) {
                this.buildFieldList(fields, filterDTO);
            }
        }

        FilterDTO.FilterItemDTO filterItem = filter.getFilterItem();
        if (Objects.isNull(filterItem)) {
            return;
        }
        Optional.ofNullable(filterItem.getFilters())
                .filter(CollectionUtils::isNotEmpty)
                .map(v -> v.get(0))
                .map(FilterDTO::getFilters).ifPresent(list -> {
                    for (FilterDTO filterDTO : list) {
                        this.buildFieldList(fields, filterDTO);
                    }
                });

        Long fieldId = filterItem.getFieldId();
        fields.add(fieldId);
    }

    private void fillFieldNameForFilter(Map<Long, String> fieldNameMap, FilterDTO filter) {
        List<FilterDTO> filters = filter.getFilters();
        if (CollectionUtils.isNotEmpty(filters)) {
            for (FilterDTO filterDTO : filters) {
                this.fillFieldNameForFilter(fieldNameMap, filterDTO);
            }
        }

        FilterDTO.FilterItemDTO filterItem = filter.getFilterItem();
        if (Objects.isNull(filterItem)) {
            return;
        }
        Optional.ofNullable(filterItem.getFilters())
                .filter(org.apache.commons.collections4.CollectionUtils::isNotEmpty)
                .map(v -> v.get(0))
                .map(FilterDTO::getFilters).ifPresent(list -> {
                    for (FilterDTO filterDTO : list) {
                        this.fillFieldNameForFilter(fieldNameMap, filterDTO);
                    }
                });

        String fieldName = fieldNameMap.get(filterItem.getFieldId());
        filterItem.setFieldName(fieldName);
    }

}
