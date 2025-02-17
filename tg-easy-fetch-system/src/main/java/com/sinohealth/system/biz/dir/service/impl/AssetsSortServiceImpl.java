package com.sinohealth.system.biz.dir.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dir.dto.AssetsSortEditRequest;
import com.sinohealth.system.biz.dir.entity.DisplaySort;
import com.sinohealth.system.biz.dir.service.AssetsSortService;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TgDocInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.mapper.TableInfoMapper;
import com.sinohealth.system.mapper.TgDocInfoMapper;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-18 15:54
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AssetsSortServiceImpl implements AssetsSortService {

    private final TableInfoMapper tableInfoMapper;
    private final TgDocInfoMapper docInfoMapper;
    private final TgTemplateInfoMapper templateInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Boolean> editSort(AssetsSortEditRequest request) {
        switch (request.getModule()) {
            case ApplicationConst.DirItemType.TEMPLATE_APPLICATION:
                return editSort(request, TgTemplateInfo.class, templateInfoMapper);
            case ApplicationConst.DirItemType.DATA_APPLICATION:
                return editSort(request, TableInfo.class, tableInfoMapper);
            case ApplicationConst.DirItemType.DOC_APPLICATION:
                return editSort(request, TgDocInfo.class, docInfoMapper);
            default:
                return AjaxResult.error("不支持的模板类型");
        }
    }

    @Override
    public void fillDefaultDisSort(DisplaySort entity) {
        if (entity instanceof TgTemplateInfo) {
            fillDisSort(entity, templateInfoMapper);
        } else if (entity instanceof TgDocInfo) {
            fillDisSort(entity, docInfoMapper);
        } else if (entity instanceof TableInfo) {
            fillDisSort(entity, tableInfoMapper);
        } else {
            log.error("不支持的类型: entity={}", entity);
        }
    }

    private <T extends DisplaySort> void fillDisSort(DisplaySort entity, BaseMapper<T> mapper) {
        boolean tableInfo = entity instanceof TableInfo;
        QueryWrapper<T> maxQuery = new QueryWrapper<T>().select(" MAX(" + DisplaySort.SORT_FIELD + ") as " + DisplaySort.SORT_FIELD);
        if (tableInfo) {
            maxQuery.eq("is_diy", 0);
        }
        List<T> exist = mapper.selectList(maxQuery);
        if (CollectionUtils.isEmpty(exist)) {
            entity.fillDisSort(1);
            return;
        }

        T max = exist.get(0);
        entity.fillDisSort(max.getDisSort() + 1);
    }

    private <T extends DisplaySort> AjaxResult<Boolean> editSort(AssetsSortEditRequest request,
                                                                 Class<T> target,
                                                                 BaseMapper<T> mapper) {
        boolean tableInfo = target == TableInfo.class;
        QueryWrapper<T> listQuery = new QueryWrapper<>();
        listQuery.eq(DisplaySort.SORT_FIELD, request.getSort());
        if (tableInfo) {
            listQuery.eq("is_diy", 0);
        }
        List<T> same = mapper.selectList(listQuery);

        if (BooleanUtils.isNotTrue(request.getConfirm())) {
            if (CollectionUtils.isNotEmpty(same)) {
                DisplaySort sameSort = same.get(0);
                return AjaxResult.success("当前排序为" + request.getSort() + "的数据为【" + sameSort.getName() + "】，是否确认顶替？", true);
            }
        }

        List<T> max = mapper.selectList(new QueryWrapper<T>()
                .select("MAX(" + DisplaySort.SORT_FIELD + ") " + DisplaySort.SORT_FIELD)
        );
        Optional<T> maxOpt = max.stream().max(Comparator.comparing(DisplaySort::getDisSort));
        Boolean overflow = maxOpt.map(DisplaySort::getDisSort).map(v -> v < request.getSort()).orElse(false);
        if (overflow) {
            return AjaxResult.error("超出最大排序值: " + maxOpt.map(DisplaySort::getDisSort).orElse(0));
        }

        // 无冲突排序值，直接更新
        if (CollectionUtils.isEmpty(same)) {
            mapper.update(null, new UpdateWrapper<T>().set(DisplaySort.SORT_FIELD,
                    request.getSort()).eq(DisplaySort.ID_FIELD, request.getBizId()));
            return AjaxResult.success(false);
        }

        QueryWrapper<T> affectQuery = new QueryWrapper<T>()
                .select(DisplaySort.ID_FIELD, DisplaySort.SORT_FIELD)
                .ge(DisplaySort.SORT_FIELD, request.getSort());
        if (tableInfo) {
            affectQuery.eq("is_diy", 0);
        }
        List<T> affects = mapper.selectList(affectQuery);

        Map<Long, Integer> sortMap = Lambda.buildMap(affects, DisplaySort::getId, DisplaySort::getDisSort);
        Map<Long, Integer> exceptMap = new HashMap<>(sortMap.size());
        exceptMap.put(request.getBizId(), request.getSort());
        sortMap.remove(request.getBizId());

        AtomicInteger counter = new AtomicInteger(request.getSort());
        affects.stream().sorted(Comparator.comparing(DisplaySort::getDisSort).thenComparing(DisplaySort::getId)).forEach(v -> {
            if (Objects.equals(v.getId(), request.getBizId())) {
                return;
            }

            exceptMap.put(v.getId(), counter.incrementAndGet());
        });

        for (Map.Entry<Long, Integer> entry : exceptMap.entrySet()) {
            mapper.update(null, new UpdateWrapper<T>().set(DisplaySort.SORT_FIELD,
                    entry.getValue()).eq(DisplaySort.ID_FIELD, entry.getKey()));
        }

        return AjaxResult.success(true);
    }
}
