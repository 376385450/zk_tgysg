package com.sinohealth.system.biz.dict.util;

import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.utils.StringUtils;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-21 15:59
 */
public class BizTypeUtil {

    public static String buildBizTypeWhere(String bizType) {
        if (StringUtils.isBlank(bizType)) {
            return "1=1";
        }
        String[] bizTypes = bizType.split(",");
        String condition = Stream.of(bizTypes).map(v -> "  find_in_set('" + v + "', biz_type)").collect(Collectors.joining(" OR "));
        return condition + "  OR biz_type='" + BizTypeEnum.ALL + "'";
    }
}
