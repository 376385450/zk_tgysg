package com.sinohealth.system.domain.constant;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-01-04 17:29
 */
public interface RequireAttrType {
    // 新增枚举请注意顺序，运营分析会按顺序进行统计
    int CONTRACT = 1;
    int SALE_BEFORE = 2;
    int MEETING = 3;

    /**
     * @see RequireAttrType#REPORT 替换值
     */
    @Deprecated
    int PAID_REPORT = 4;
    int REPORT = 5;
    int PRODUCT = 6;
    int INTERNAL = 7;

    Map<Integer, String> DESC_MAP = new HashMap<Integer, String>() {{
        put(CONTRACT, "合同");
        put(SALE_BEFORE, "销前");
        put(MEETING, "三会");
        put(PAID_REPORT, "付费报告");
        put(REPORT, "报告");
        put(PRODUCT, "产品");
        put(INTERNAL, "内部使用");
    }};

    static Integer getByTransferDesc(String desc){
        return DESC_MAP.entrySet().stream().filter(v -> Objects.equals(v.getValue(), desc))
                .map(Map.Entry::getKey).findAny().orElse(null);
    }
}
