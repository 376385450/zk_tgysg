package com.sinohealth.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.system.biz.application.bo.FieldMetaBO;
import com.sinohealth.system.biz.application.dto.request.ColumnSetting;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.dao.ApplicationColumnSettingDAO;
import com.sinohealth.system.dao.DataDescriptionDAO;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.dto.DataDescDocDTO;
import com.sinohealth.system.dto.DataDescDocUpdateReqDTO;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.service.DataDescriptionService;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-07 14:20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataDescriptionServiceImpl implements DataDescriptionService {

    private final DataDescriptionDAO dataDescriptionDAO;

    private final IApplicationService applicationService;

    private final TgApplicationInfoMapper applicationInfoMapper;
    private final UserDataAssetsDAO userDataAssetsDAO;
    private final ApplicationColumnSettingDAO applicationColumnSettingDAO;

    private final ISysDictTypeService dictTypeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(DataDescDocUpdateReqDTO reqDTO) {
        TgDataDescription entity = dataDescriptionDAO.getByAssetsId(reqDTO.getAssetsId());
        if (entity == null) {
            entity = new TgDataDescription();
            BeanUtil.copyProperties(reqDTO, entity);
            entity.setCreateBy(ThreadContextHolder.getSysUser().getUserId());
            dataDescriptionDAO.save(entity);
        } else {
            TgDataDescription updateEntity = new TgDataDescription();
            BeanUtil.copyProperties(reqDTO, updateEntity);
            updateEntity.setId(entity.getId());
            dataDescriptionDAO.updateById(updateEntity);
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataDescDocDTO getDetail(Long assetsId) {
        TgDataDescription entity = dataDescriptionDAO.getByAssetsId(assetsId);
        if (entity == null) {
            entity = new TgDataDescription();
            // 初始化
            UserDataAssets assets = userDataAssetsDAO.getBaseMapper().selectById(assetsId);
            TgDataDescriptionItem baseTarget = this.buildBaseTarget(assets.getSrcApplicationId());
            TgDataDescriptionItem dataDesc = this.buildDataDesc();
            String dataQuota = baseTarget.getList().stream().map(TgDataDescriptionQuota::getKey).collect(Collectors.joining("、"));

            entity.setDocName(assets.getProjectName());
            entity.setAssetsId(assetsId);
            entity.setDataQuota(dataQuota);
            entity.setDataDesc(dataDesc);
            entity.setBaseTarget(baseTarget);
        }
        DataDescDocDTO dto = new DataDescDocDTO();
        BeanUtil.copyProperties(entity, dto);
        this.handleNull(dto);
        return dto;
    }

    /**
     * 处理历史数据字段空值
     */
    private void handleNull(DataDescDocDTO dto) {
        if (Objects.isNull(dto)) {
            return;
        }
        DataDescDocDTO.DataDescDocDetailItemDTO item = dto.getBaseTarget();
        if (Objects.isNull(item)) {
            return;
        }

        List<DataDescDocDTO.DataDescDocDetailItemDTO.DataDescTargetDTO> list = item.getList();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        for (DataDescDocDTO.DataDescDocDetailItemDTO.DataDescTargetDTO target : list) {
            String dis = target.getDisplayName();
            if (Objects.isNull(dis)) {
                target.setDisplayName(target.getKey());
            }
        }
    }

    @Override
    public TgDataDescription getByAssetsId(Long assetsId) {
        TgDataDescription entity = dataDescriptionDAO.getByAssetsId(assetsId);
        if (Objects.isNull(entity)) {
            return null;
        }

        TgDataDescription dto = new TgDataDescription();
        BeanUtil.copyProperties(entity, dto);
        return dto;
    }

    @Override
    public Map<Long, Integer> queryByAssetsIds(Collection<Long> assetsIds) {
        if (CollectionUtils.isEmpty(assetsIds)) {
            return Collections.emptyMap();
        }

        List<TgDataDescription> descList = dataDescriptionDAO.getBaseMapper()
                .selectList(new QueryWrapper<TgDataDescription>().lambda()
                        .select(TgDataDescription::getAssetsId, TgDataDescription::getId)
                        .in(TgDataDescription::getAssetsId, assetsIds));

        return descList.stream().collect(Collectors.toMap(TgDataDescription::getAssetsId, TgDataDescription::getId, (front, current) -> current));
    }

//    @Override
//    public TgDataDescription getDefaultByApplicationId(Long assetsId) {
//        TgDataDescription entity = dataDescriptionDAO.getByAssetsId(assetsId);
//        if (entity == null) {
//            entity = new TgDataDescription();
//            // 初始化
//            TgDataDescriptionItem baseTarget = this.buildBaseTarget(assetsId);
//            TgDataDescriptionItem dataDesc = this.buildDataDesc();
//            String dataQuota = baseTarget.getList().stream().map(TgDataDescriptionQuota::getKey).collect(Collectors.joining("、"));
//            TgApplicationInfo tgApplicationInfo = applicationInfoMapper.selectById(assetsId);
//            entity.setDocName(tgApplicationInfo.getProjectName());
//            entity.setAssetsId(assetsId);
//            entity.setDataQuota(dataQuota);
//            entity.setDataDesc(dataDesc);
//            entity.setBaseTarget(baseTarget);
//
//            this.fillDefaultVal(baseTarget);
//        }
//        TgDataDescription dto = new TgDataDescription();
//        BeanUtil.copyProperties(entity, dto);
//        return dto;
//    }

//    private void fillDefaultVal(TgDataDescriptionItem baseTarget) {
//        List<SysDictData> data = dictTypeService.selectDictDataByType("data_description", "");
//        Map<String, String> dictMap = data.stream().collect(Collectors.toMap(SysDictData::getDictLabel, SysDictData::getDictValue, (front, current) -> current));
//        for (TgDataDescriptionQuota tgDataDescriptionQuota : baseTarget.getList()) {
//            String key = tgDataDescriptionQuota.getKey();
//            String s = dictMap.get(key);
//            if (StringUtils.isNoneBlank(s)) {
//                tgDataDescriptionQuota.setValue(s);
//            }
//        }
//    }

    /**
     * 构建数据说明
     * 默认要有【品类范围、市场范围、数据时间】
     */
    private TgDataDescriptionItem buildDataDesc() {
        TgDataDescriptionQuota quota1 = new TgDataDescriptionQuota().setKey("品类范围");
        TgDataDescriptionQuota quota2 = new TgDataDescriptionQuota().setKey("市场范围");
        TgDataDescriptionQuota quota3 = new TgDataDescriptionQuota().setKey("数据时间");
        List<TgDataDescriptionQuota> list = Lists.newArrayList(quota1, quota2, quota3);
        return new TgDataDescriptionItem()
                .setList(list);
    }

    /**
     * 构建基础指标，从表中读取字段作为基础指标
     * 只提取数值类型，并排除id
     */
    private TgDataDescriptionItem buildBaseTarget(Long applicationId) {
        // 获取提数申请
        FieldMetaBO fieldMeta = applicationService.getApplicationFieldMeta(applicationId);
        if (fieldMeta.isAllEmpty()) {
            return new TgDataDescriptionItem().setList(new ArrayList<>());
        }

        TgTemplateInfo template = TgTemplateInfo.newInstance().selectById(fieldMeta.getApplyInfo().getTemplateId());

        List<TgDataDescriptionQuota> quotaList = new ArrayList<>();
//        List<TableFieldInfo> tableFields = fieldMeta.getTableFields();
//        tableFields.forEach(field -> {
//            if (Objects.equals(field.getDimIndex(), TableConst.DimIndexType.METRIC)) {
//                String fieldName = firstNotEmpty(field.getRealName(), field.getFieldAlias(), field.getComment(), field.getFieldName());
//                quotaList.add(new TgDataDescriptionQuota().setKey(fieldName));
//            }
//        });
        final List<ColumnSetting> columnSettings = applicationColumnSettingDAO.getByApplicationId(applicationId);
        Map<String, ColumnSetting> colSetting;
        if (CollUtil.isNotEmpty(columnSettings)) {
            colSetting = columnSettings.stream()
                    .collect(Collectors.toMap(ColumnSetting::getFiledName, v -> v));
        } else {
            colSetting = Collections.emptyMap();
        }

        Map<String, ColumnSetting> colAliasSetting;
        Boolean wide = Optional.ofNullable(template).map(v -> Objects.equals(v.getTemplateType(), TemplateTypeEnum.wide_table.name()))
                .orElse(false);
        if (!wide) {
            colAliasSetting = columnSettings.stream()
                    .collect(Collectors.toMap(ColumnSetting::getFiledAlias, v -> v));
        } else {
            colAliasSetting = Collections.emptyMap();
        }

        List<CustomFieldInfo> customFields = fieldMeta.getCustomExcludeDelete();
        customFields.forEach(field -> {
            String fieldName = StrUtil.firstNotBlankStr(field.getRealName(), field.getFieldAlias(),
                    field.getComment(), field.getFieldName());
            String displayName;
            if (wide) {
                displayName = Optional.ofNullable(colSetting.get(fieldName))
                        .map(v -> StrUtil.firstNotBlankStr(v.getCustomName(), fieldName)).orElse(fieldName);
            }else{
                displayName = Optional.ofNullable(colAliasSetting.get(fieldName))
                        .map(v -> StrUtil.firstNotBlankStr(v.getCustomName(), fieldName)).orElse(fieldName);
            }

            quotaList.add(new TgDataDescriptionQuota()
                    .setKey(fieldName)
                    .setDisplayName(displayName)
                    .setFieldSource(field.getFieldSource()));
        });
        return new TgDataDescriptionItem().setList(quotaList);
    }
}
