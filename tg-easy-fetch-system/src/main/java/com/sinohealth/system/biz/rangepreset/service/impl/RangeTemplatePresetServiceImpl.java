package com.sinohealth.system.biz.rangepreset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.preset.RangePresetTypeEnum;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.rangepreset.dao.RangeTemplatePresetDAO;
import com.sinohealth.system.biz.rangepreset.domain.RangeTemplatePreset;
import com.sinohealth.system.biz.rangepreset.dto.RangeTemplatePresetDTO;
import com.sinohealth.system.biz.rangepreset.dto.request.RangePresetPageRequest;
import com.sinohealth.system.biz.rangepreset.dto.request.RangeTemplateUpsertRequest;
import com.sinohealth.system.biz.rangepreset.service.RangeTemplatePresetService;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.vo.TgDataRangeGroupVO;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.util.ApplicationSqlUtil;
import com.sinohealth.system.util.RangeTemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-31 15:24
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RangeTemplatePresetServiceImpl implements RangeTemplatePresetService {

    private final RangeTemplatePresetDAO rangeTemplatePresetDAO;
    private final TgTemplateInfoMapper templateInfoMapper;

    private final ISysUserService userService;

    @Override
    public AjaxResult<IPage<RangeTemplatePresetDTO>> pageQuery(RangePresetPageRequest request) {
        LambdaQueryWrapper<RangeTemplatePreset> wrapper = new QueryWrapper<RangeTemplatePreset>().lambda();

        IPage<RangeTemplatePreset> page = rangeTemplatePresetDAO.getBaseMapper().selectPage(request.buildPage(), wrapper
                .and(StringUtils.isNotBlank(request.getBizType()), v -> v.apply(request.buildBizType()))
                .eq(StringUtils.isNotBlank(request.getGranularity()), RangeTemplatePreset::getGranularity, request.getGranularity())
                .like(StringUtils.isNotBlank(request.getSearchContent()), RangeTemplatePreset::getName, request.getSearchContent())
                .eq(StringUtils.isNotBlank(request.getRangeType()), RangeTemplatePreset::getRangeType, request.getRangeType())
                .eq(Objects.nonNull(request.getTemplateId()), RangeTemplatePreset::getTemplateId, request.getTemplateId())
                .and(v -> v.eq(RangeTemplatePreset::getRangeType, RangePresetTypeEnum.common)
                        .or().eq(RangeTemplatePreset::getCreator, SecurityUtils.getUserId()))
                .orderByDesc(RangeTemplatePreset::getUpdateTime)
        );

        Set<Long> userIds = page.getRecords().stream()
                .map(RangeTemplatePreset::getCreator)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nameMap = userService.selectUserNameMapByIds(userIds);

        List<RangeTemplatePreset> records = page.getRecords();
        Map<Long, String> tempNameMap;
        if (CollectionUtils.isNotEmpty(records)) {
            // 处理模板名称
            Set<Long> templateIds = Lambda.buildSet(records, RangeTemplatePreset::getTemplateId);
            List<TgTemplateInfo> infos = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                    .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName, TgTemplateInfo::getBaseTableId)
                    .in(TgTemplateInfo::getId, templateIds));
            tempNameMap = Lambda.buildMap(infos, TgTemplateInfo::getId, TgTemplateInfo::getTemplateName);
        } else {
            tempNameMap = Collections.emptyMap();
        }

        return AjaxResult.success(PageUtil.convertMap(page, v -> {
            RangeTemplatePresetDTO dto = new RangeTemplatePresetDTO();
            BeanUtils.copyProperties(v, dto);
            dto.setUpdater(nameMap.get(v.getUpdater()));
            dto.setCreator(nameMap.get(v.getCreator()));
            dto.setTemplate(tempNameMap.get(v.getTemplateId()));
            return dto;
        }));
    }

    @Override
    public AjaxResult<Void> upsert(RangeTemplateUpsertRequest request) {
        String groupList = request.getGroupList();
        if (StringUtils.isBlank(groupList)) {
            return AjaxResult.error("自定义列为空");
        }

        List<TgDataRangeGroupVO> groups = JsonUtils.parse(groupList, new TypeReference<List<TgDataRangeGroupVO>>() {
        });

        RangeTemplateUtil.checkGroupParam(groups, false);

        boolean existEmpty = groups.stream().flatMap(v -> v.getData().stream())
                .anyMatch(ApplicationSqlUtil::hasEmptyNode);
        if (existEmpty) {
            return AjaxResult.error("请填写完整自定义列 数据范围");
        }

        TgTemplateInfo template = templateInfoMapper.selectById(request.getTemplateId());
        if (Objects.isNull(template)) {
            return AjaxResult.error("模板不存在");
        }

        Long userId = SecurityUtils.getUserId();
        RangeTemplatePreset entity = new RangeTemplatePreset();
        BeanUtils.copyProperties(request, entity);
        if (Objects.isNull(request.getId())) {
            entity.setCreator(userId);
        }
        entity.setUpdater(userId);
        entity.setTemplateType(template.getTemplateType());
        rangeTemplatePresetDAO.saveOrUpdate(entity);

        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> deleteById(Long id) {
        if (Objects.isNull(id)) {
            return AjaxResult.error("参数缺失");
        }
        rangeTemplatePresetDAO.removeById(id);
        return AjaxResult.succeed();
    }
}
