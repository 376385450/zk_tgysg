package com.sinohealth.system.biz.dataassets.constant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.constant.ApplicationConst;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-06-12 17:15
 */
@Getter
public enum MergeTimeTypeEnum {

    per_month(ApplicationConst.RequireTimeType.PERSISTENCE, DeliverTimeTypeEnum.month.name()),
    per_quarter(ApplicationConst.RequireTimeType.PERSISTENCE, DeliverTimeTypeEnum.quarter.name()),
    per_halfAYear(ApplicationConst.RequireTimeType.PERSISTENCE, DeliverTimeTypeEnum.halfAYear.name()),
    per_year(ApplicationConst.RequireTimeType.PERSISTENCE, DeliverTimeTypeEnum.year.name()),
    once(ApplicationConst.RequireTimeType.ONCE, null);

    /**
     * @see com.sinohealth.system.domain.constant.ApplicationConst.RequireTimeType
     */
    private final int requireTimeType;

    /**
     * @see DeliverTimeTypeEnum
     */
    private final String deliverTimeType;

    MergeTimeTypeEnum(int requireTimeType, String deliverTimeType) {
        this.requireTimeType = requireTimeType;
        this.deliverTimeType = deliverTimeType;
    }

    public static Optional<MergeTimeTypeEnum> of(String deliverTimeType) {
        for (MergeTimeTypeEnum value : values()) {
            if (Objects.equals(value.name(), deliverTimeType)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    public static List<MergeTimeTypeEnum> of(List<String> types) {
        return types.stream().map(MergeTimeTypeEnum::of).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
    }


    public static Consumer<LambdaQueryWrapper<TgApplicationInfo>> buildCondition(List<MergeTimeTypeEnum> types) {

        Map<Integer, List<MergeTimeTypeEnum>> requireTypeMap = types.stream()
                .collect(Collectors.groupingBy(MergeTimeTypeEnum::getRequireTimeType));

        List<Consumer<LambdaQueryWrapper<TgApplicationInfo>>> conds = new ArrayList<>();
        if (requireTypeMap.containsKey(ApplicationConst.RequireTimeType.PERSISTENCE)) {
            List<MergeTimeTypeEnum> tmp = requireTypeMap.get(ApplicationConst.RequireTimeType.PERSISTENCE);

            List<String> deliverTypes = tmp.stream().map(MergeTimeTypeEnum::getDeliverTimeType).collect(Collectors.toList());
            Consumer<LambdaQueryWrapper<TgApplicationInfo>> w = v -> v.and(vv ->
                    vv.eq(TgApplicationInfo::getRequireTimeType, ApplicationConst.RequireTimeType.PERSISTENCE)
                            .in(TgApplicationInfo::getDeliverTimeType, deliverTypes));
            conds.add(w);
        }

        if (requireTypeMap.containsKey(ApplicationConst.RequireTimeType.ONCE)) {
            Consumer<LambdaQueryWrapper<TgApplicationInfo>> w = v ->
                    v.eq(TgApplicationInfo::getRequireTimeType, ApplicationConst.RequireTimeType.ONCE);
            conds.add(w);
        }
        if (conds.size() > 1) {
            Consumer<LambdaQueryWrapper<TgApplicationInfo>> a = conds.get(0);
            Consumer<LambdaQueryWrapper<TgApplicationInfo>> b = conds.get(1);

            return w -> {
                a.accept(w);
                w.or();
                b.accept(w);
            };
        }

        return w -> conds.get(0).accept(w);
    }
}
