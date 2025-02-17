package com.sinohealth.system.biz.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.MetricsTypeEnum;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dict.dao.FieldDictDAO;
import com.sinohealth.system.biz.dict.dao.MetricsDictDAO;
import com.sinohealth.system.biz.dict.dao.PresetMetricsDefineDAO;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.biz.dict.domain.MetricsDict;
import com.sinohealth.system.biz.dict.domain.PresetMetricsDefine;
import com.sinohealth.system.biz.dict.dto.MetricsDictDTO;
import com.sinohealth.system.biz.dict.dto.MetricsFormulaVO;
import com.sinohealth.system.biz.dict.dto.request.DictCommonPageRequest;
import com.sinohealth.system.biz.dict.dto.request.TableMetricsQueryRequest;
import com.sinohealth.system.biz.dict.service.MetricsDictService;
import com.sinohealth.system.biz.dict.util.BizTypeUtil;
import com.sinohealth.system.biz.dict.util.Expression;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.mapper.TableFieldInfoMapper;
import com.sinohealth.system.mapper.TableInfoMapper;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import com.sinohealth.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 16:14
 */
@Slf4j
@Service
public class MetricsDictServiceImpl implements MetricsDictService {

    @Autowired
    private MetricsDictDAO metricsDictDAO;
    @Autowired
    private PresetMetricsDefineDAO presetMetricsDefineDAO;
    @Autowired
    private TableFieldInfoMapper tableFieldInfoMapper;
    @Autowired
    private TableInfoMapper tableInfoMapper;
    @Autowired
    private FieldDictDAO fieldDictDAO;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private DictUniqueAdapter dictUniqueAdapter;
    @Autowired
    private TgTemplateInfoMapper templateInfoMapper;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;

    @Override
    public AjaxResult<IPage<MetricsDictDTO>> pageQuery(DictCommonPageRequest request) {
        LambdaQueryWrapper<MetricsDict> wrapper = new QueryWrapper<MetricsDict>().lambda()
                .and(StringUtils.isNotBlank(request.getSearchContent()),
                        v -> v.like(MetricsDict::getFieldName, request.getSearchContent())
                                .or().like(MetricsDict::getName, request.getSearchContent()))
                .and(StringUtils.isNotBlank(request.getBizType()), v -> v.apply(request.buildBizType()))
                .orderByAsc(MetricsDict::getSort, MetricsDict::getUpdateTime);

        IPage<MetricsDict> page = metricsDictDAO.page(request.buildPage(), wrapper);

        Set<Long> userIds = page.getRecords().stream()
                .flatMap(v -> Stream.of(v.getCreator(), v.getUpdater()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        Map<Long, String> nameMap = userService.selectUserNameMapByIds(userIds);
        List<Long> fieldIds = page.getRecords().stream().map(MetricsDict::getColName)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> fieldNameMap;
        if (CollectionUtils.isNotEmpty(fieldIds)) {
            List<FieldDict> fieldDicts = fieldDictDAO.listByIds(fieldIds);
            fieldNameMap = fieldDicts.stream().collect(Collectors.toMap(FieldDict::getId,
                    v -> v.getFieldName() + "(" + v.getName() + ")", (front, current) -> current));
        } else {
            fieldNameMap = Collections.emptyMap();
        }

        List<Long> presetIds = page.getRecords().stream()
                .filter(v -> Objects.equals(v.getMetricsType(), MetricsTypeEnum.preset.name()))
                .map(MetricsDict::getId).collect(Collectors.toList());
        Map<Long, List<Long>> defineIdsMap;
        if (CollectionUtils.isNotEmpty(presetIds)) {
            Map<Long, List<PresetMetricsDefine>> defineMap = presetMetricsDefineDAO.queryByPresetMetricsId(presetIds);
            defineIdsMap = defineMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue()
                    .stream().map(PresetMetricsDefine::getMetricsId).collect(Collectors.toList()), (front, current) -> current));
        } else {
            defineIdsMap = Collections.emptyMap();
        }

        return AjaxResult.success(PageUtil.convertMap(page, v -> {
            MetricsDictDTO dto = new MetricsDictDTO();
            BeanUtils.copyProperties(v, dto);
            if (Objects.equals(v.getMetricsType(), MetricsTypeEnum.normal.name()) && StringUtils.isBlank(dto.getFormulaDisplay())) {
                dto.setFormulaDisplay(dto.getFormula());
            } else if (Objects.equals(v.getMetricsType(), MetricsTypeEnum.formula.name())) {
                Optional<CommonConstants.ComputeWayEnum> enumOpt = CommonConstants.ComputeWayEnum.getById(v.getComputeWay());
                enumOpt.ifPresent(e -> dto.setFormulaDisplay(Optional.ofNullable(fieldNameMap.get(v.getColName()))
                        .orElse("") + " - " + e.getDesc()));
            }

            if (Objects.equals(v.getMetricsType(), MetricsTypeEnum.preset.name())) {
                dto.setFormulaDef(new MetricsFormulaVO(defineIdsMap.get(v.getId())));
            }

            dto.setUpdater(nameMap.get(v.getUpdater()));
            dto.setCreator(nameMap.get(v.getCreator()));
            return dto;
        }));
    }

    @Override
    public AjaxResult<List<MetricsDictDTO>> queryAllForDesc(Long assetsId) {
        log.info("assetsId={}", assetsId);
        if (Objects.isNull(assetsId)) {
            return AjaxResult.error("资产为空");
        }
        UserDataAssets dataAssets = userDataAssetsDAO.getById(assetsId);
        if (Objects.isNull(dataAssets)) {
            return AjaxResult.error("资产不存在");
        }
        TgTemplateInfo template = templateInfoMapper.selectById(dataAssets.getTemplateId());
        if (Objects.isNull(template)) {
            return AjaxResult.error("提数模板不存在");
        }

        LambdaQueryWrapper<MetricsDict> wrapper = new QueryWrapper<MetricsDict>().lambda()
                .select(MetricsDict::getId, MetricsDict::getName, MetricsDict::getMeaning)
                .apply(BizTypeUtil.buildBizTypeWhere(template.getBizType()));

        List<MetricsDict> dicts = metricsDictDAO.getBaseMapper().selectList(wrapper);
        List<MetricsDictDTO> list = dicts.stream().map(v -> {
            MetricsDictDTO dto = new MetricsDictDTO();
            BeanUtils.copyProperties(v, dto);
            if (StringUtils.isBlank(dto.getMeaning())) {
                dto.setMeaning(dto.getName());
            }
            return dto;
        }).collect(Collectors.toList());

        return AjaxResult.success(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> upsert(MetricsDictDTO request) {
        MetricsDict dict = new MetricsDict();

        if (Objects.equals(request.getMetricsType(), MetricsTypeEnum.preset.name())
                && StringUtils.isBlank(request.getFormula())) {
            return AjaxResult.error("请配置完整的预设指标");
        }

        if (Objects.equals(request.getMetricsType(), MetricsTypeEnum.preset.name())) {
            if (StringUtils.isBlank(request.getFormula())) {
                return AjaxResult.error("请配置完整的预设指标");
            }

            // 注意：目前仅支持两个字段间的四则运算。按实际需求再做扩展
            if (StringUtils.isNotBlank(request.getFormula())) {
                Expression expression = new Expression(request.getFormula());
                if (!expression.isRightExpression()) {
                    return AjaxResult.error("请配置正确的预设指标计算表达式");
                }
            }
        }

        if (Objects.equals(request.getMetricsType(), MetricsTypeEnum.formula.name())
                && (Objects.isNull(request.getColName()) || Objects.isNull(request.getComputeWay()))) {
            return AjaxResult.error("请配置完整的计算指标");
        }

        Long userId = SecurityUtils.getUserId();
        BeanUtils.copyProperties(request, dict);
        if (Objects.isNull(request.getId())) {
            dict.setCreator(userId);
        }

        dict.setUpdater(userId);
        List<MetricsDict> repeat = dictUniqueAdapter.checkRepeat(metricsDictDAO, Collections.singletonList(dict));
        if (CollectionUtils.isNotEmpty(repeat)) {
            String msg = repeat.stream().map(v -> String.format("【%s(%s)】", v.getFieldName(), v.getName()))
                    .collect(Collectors.joining("、"));
            return AjaxResult.error("指标 " + msg + " 存在重复值，请进行确认，谢谢。");
        }
        if (Objects.isNull(dict.getId())) {
            dict.setSort(metricsDictDAO.queryMaxSort() + 1);
        } else {
            dict.setUpdateTime(LocalDateTime.now());
        }

        metricsDictDAO.saveOrUpdate(dict);

        if (Objects.nonNull(request.getFormulaDef()) && CollectionUtils.isNotEmpty(request.getFormulaDef().getMetrics())) {
            presetMetricsDefineDAO.remove(new QueryWrapper<PresetMetricsDefine>().lambda()
                    .eq(PresetMetricsDefine::getPresetId, dict.getId()));
            List<PresetMetricsDefine> presets = request.getFormulaDef().getMetrics().stream().map(v -> {
                PresetMetricsDefine presetMetricsDefine = new PresetMetricsDefine();
                presetMetricsDefine.setMetricsId(v);
                presetMetricsDefine.setPresetId(dict.getId());
                return presetMetricsDefine;
            }).collect(Collectors.toList());
            presetMetricsDefineDAO.saveBatch(presets);
        }

        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> deleteById(Long id) {
        List<PresetMetricsDefine> presets = presetMetricsDefineDAO.queryByMetricsId(Collections.singleton(id));
        if (CollectionUtils.isNotEmpty(presets)) {
            return AjaxResult.error("当前内容被引用，请取消引用后再删除");
        }

        List<TgTemplateInfo> infos = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                .select(TgTemplateInfo::getCustomMetricsJson).isNotNull(TgTemplateInfo::getCustomMetricsJson));
        for (TgTemplateInfo info : infos) {
            JsonBeanConverter.convert2Obj(info);
            if (CollectionUtils.isNotEmpty(info.getCustomMetrics())) {
                boolean hasDep = info.getCustomMetrics().stream().anyMatch(v -> Objects.equals(v.getMetricsId(), id));
                if (hasDep) {
                    return AjaxResult.error("当前内容被提数模板引用，请取消引用后再删除");
                }
            }
        }

        metricsDictDAO.removeById(id);
        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<List<MetricsDictDTO>> queryByTableId(TableMetricsQueryRequest request) {
        if (CollectionUtils.isEmpty(request.getTableId())) {
            return AjaxResult.error("请先选择数据表");
        }

        // 查出关联的字段库
        List<TableFieldInfo> fields = tableFieldInfoMapper.selectList(new QueryWrapper<TableFieldInfo>().lambda()
                .select(TableFieldInfo::getRelationColId)
                .in(TableFieldInfo::getTableId, request.getTableId()));
        List<Long> colIds = fields.stream().filter(Objects::nonNull).map(TableFieldInfo::getRelationColId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(colIds)) {
            return AjaxResult.success(Collections.emptyList());
        }

        TableInfo tableInfo = tableInfoMapper.selectById(request.getTableId().get(0));
        // 查出关联的指标
        List<MetricsDict> metricsDicts = metricsDictDAO.getBaseMapper().selectList(new QueryWrapper<MetricsDict>().lambda()
                .in(MetricsDict::getColName, colIds)
                .like(StringUtils.isNotBlank(request.getSearchContent()), MetricsDict::getName, request.getSearchContent())
                .and(StringUtils.isNotBlank(tableInfo.getBizType()), v -> v.apply(buildBizType(tableInfo.getBizType())))
        );

        Set<Long> metricsIds = metricsDicts.stream().map(MetricsDict::getId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(metricsIds)) {
            // 查出指标关联的预设指标
            List<PresetMetricsDefine> presets = presetMetricsDefineDAO.queryByMetricsId(metricsIds);
            Map<Long, List<PresetMetricsDefine>> preSetMap = presets.stream().collect(Collectors.groupingBy(PresetMetricsDefine::getPresetId));
            List<Long> presetIds = preSetMap.entrySet().stream().filter(v -> {
                List<Long> depIds = v.getValue().stream().map(PresetMetricsDefine::getMetricsId).collect(Collectors.toList());
                return metricsIds.containsAll(depIds);
            }).map(Map.Entry::getKey).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(presetIds)) {
                List<MetricsDict> presetMetrics = metricsDictDAO.listByIds(presetIds);
                metricsDicts.addAll(presetMetrics);
            }
        }

        List<MetricsDictDTO> list = metricsDicts.stream().map(v -> {
            MetricsDictDTO dto = new MetricsDictDTO();
            BeanUtils.copyProperties(v, dto);
            return dto;
        }).collect(Collectors.toList());
        return AjaxResult.success(list);
    }

    @Override
    public List<MetricsDictDTO> listAll() {
        List<MetricsDict> dicts = metricsDictDAO.list(new QueryWrapper<MetricsDict>().lambda().select(
                        MetricsDict::getId, MetricsDict::getFieldName, MetricsDict::getName, MetricsDict::getMetricsType,
                        MetricsDict::getBizType, MetricsDict::getCreator)
                .orderByAsc(MetricsDict::getSort));
        Set<Long> userIds = dicts.stream()
                .flatMap(v -> Stream.of(v.getCreator()))
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nameMap = userService.selectUserNameMapByIds(userIds);

        return dicts.stream().map(v -> {
            MetricsDictDTO dto = new MetricsDictDTO();
            BeanUtils.copyProperties(v, dto);
            dto.setMetricsType(MetricsTypeEnum.getDesc(dto.getMetricsType()));
            dto.setCreator(nameMap.get(v.getCreator()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MetricsDict> selectAllIds() {
        return metricsDictDAO.list(new QueryWrapper<MetricsDict>().lambda()
                .select(MetricsDict::getId));
    }

    @Override
    public void fillSort(List<MetricsDict> fields) {
        metricsDictDAO.updateBatchById(fields);
    }

    public String buildBizType(String bizType) {
        if (com.sinohealth.common.utils.StringUtils.isBlank(bizType)) {
            return "1=1";
        }
        return "  find_in_set('" + bizType + "', biz_type)" + "  OR biz_type='" + BizTypeEnum.ALL + "'";
    }
}
