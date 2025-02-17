package com.sinohealth.system.biz.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.system.biz.dict.service.UniqueDao;
import com.sinohealth.system.biz.dict.service.UniqueDomain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 指标，字段库，字典 唯一校验
 * <p>
 * name field_name biz_type 做业务唯一主键
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-07-04 09:40
 */
@Slf4j
@Service
public class DictUniqueAdapter {

    private static class DiffContext {
        Map<String, String> allNames = new HashMap<>();
        Map<String, String> otherIds = new HashMap<>();
        Map<String, String> otherNames = new HashMap<>();

        public Map<String, String> getAllNames() {
            return allNames;
        }

        public Map<String, String> getOtherIds() {
            return otherIds;
        }

        public Map<String, String> getOtherNames() {
            return otherNames;
        }
    }

    public <T extends UniqueDomain<T>> List<T> checkRepeat(UniqueDao<T> dao, List<T> change) {
        if (CollectionUtils.isEmpty(change)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<T> query = new QueryWrapper<T>().lambda();
        for (T t : change) {
            t.appendQuery(query);
        }

        Set<Long> updateIds = change.stream().map(UniqueDomain::getId).collect(Collectors.toSet());

        // 查出库中有关联的数据
        List<T> exist = dao.queryAllForUnique(query);

        Map<Boolean, List<T>> typeMap = exist.stream()
                .filter(v -> !updateIds.contains(v.getId()))
                .collect(Collectors.groupingBy(v ->
                        Objects.equals(v.getBizType(), BizTypeEnum.ALL)));
        DiffContext dbContext = buildDbContext(typeMap);

        List<T> repeatResult = new ArrayList<>();
        // 重复性校验
        for (T data : change) {
            if (Objects.equals(data.getBizType(), BizTypeEnum.ALL)) {
                boolean repeat = dbContext.getAllNames().containsKey(data.getBizName()) || dbContext.getOtherNames().containsKey(data.getBizName());
                if (repeat) {
                    repeatResult.add(data);
                }
            } else {
                List<String> types = BizTypeEnum.splitType(data.getBizType());
                boolean otherRepeat = types.stream().map(v -> String.format("%s-%s", v, data.getBizName())).anyMatch(dbContext.getOtherIds()::containsKey);
                boolean allRepeat = dbContext.getAllNames().containsKey(data.getBizName());
                if (otherRepeat || allRepeat) {
                    repeatResult.add(data);
                }
            }
        }

        // 自身提交的数据 重复性校验
        DiffContext subContext = buildSubDiff(change);
        for (int i = 0; i < change.size(); i++) {
            T data = change.get(i);
            String index = String.valueOf(i);
            if (Objects.equals(data.getBizType(), BizTypeEnum.ALL)) {
                String exIdx = subContext.getAllNames().get(data.getBizName());
                String othIdx = subContext.getOtherNames().get(data.getBizName());

                boolean repeat = (Objects.nonNull(exIdx) && !Objects.equals(exIdx, index))
                        || (Objects.nonNull(othIdx) && !Objects.equals(othIdx, index));
                if (repeat) {
                    repeatResult.add(data);
                }
            } else {
                List<String> types = BizTypeEnum.splitType(data.getBizType());
                boolean otherRepeat = types.stream()
                        .map(v -> String.format("%s-%s", v, data.getBizName()))
                        .anyMatch(v -> {
                            String idx = subContext.getOtherIds().get(v);
                            return Objects.nonNull(idx) && !Objects.equals(idx, index);
                        });

                String exIdx = subContext.getAllNames().get(data.getBizName());
                if (otherRepeat || (Objects.nonNull(exIdx) && !Objects.equals(exIdx, index))) {
                    repeatResult.add(data);
                }
            }
        }

        return repeatResult;
    }

    private <T extends UniqueDomain<T>> DiffContext buildSubDiff(List<T> change) {
        DiffContext diffContext = new DiffContext();
        for (int i = 0; i < change.size(); i++) {
            T data = change.get(i);
            String index = String.valueOf(i);
            if (Objects.equals(data.getBizType(), BizTypeEnum.ALL)) {
                diffContext.getAllNames().put(data.getBizName(), index);
            } else {
                List<String> types = BizTypeEnum.splitType(data.getBizType());
                diffContext.getOtherNames().put(data.getBizName(), index);
                types.stream().map(v -> String.format("%s-%s", v, data.getBizName())).forEach(v -> diffContext.getOtherIds().put(v, index));
            }
        }
        return diffContext;
    }

    private <T extends UniqueDomain<T>> DiffContext buildDbContext(Map<Boolean, List<T>> submitMap) {
        DiffContext diffContext = new DiffContext();

        List<T> subAllTypeData = submitMap.get(true);
        if (CollectionUtils.isNotEmpty(subAllTypeData)) {
            subAllTypeData.stream().map(UniqueDomain::getBizName).forEach(v -> diffContext.getAllNames().put(v, ""));
        }
        List<T> subOtherData = submitMap.get(false);
        if (CollectionUtils.isNotEmpty(subOtherData)) {
            for (T data : subOtherData) {
                List<String> types = BizTypeEnum.splitType(data.getBizType());
                diffContext.getOtherNames().put(data.getBizName(), "");
                types.stream().map(v -> String.format("%s-%s", v, data.getBizName())).forEach(v -> diffContext.getOtherIds().put(v, ""));
            }
        }
        return diffContext;
    }
}
