package com.sinohealth.system.dto.application;

import com.sinohealth.common.constant.CommonConstants;
import lombok.Data;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-01-31 16:27
 */
@Data
public class CustomFieldDepFieldVO {

    /**
     * 只能使用此类函数字符串
     *
     * @see CommonConstants.ComputeWay#MAX_STR
     */
    private String operator;

    private String field;

    private String alias;

}
