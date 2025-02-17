package com.sinohealth.common.constant;

import com.sinohealth.common.utils.StrUtil;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-10 17:47
 */
public class LogConstant {

    public static final String TRACE_ID = "tid";
//    public static final String DEBUG = "debug";

    public static String genTraceId() {
        return StrUtil.randomAlpha(8) + " ";
    }

    /**
     * 长请求记录日志的阈值
     */
    public static final int LONG_RT_WARN_MS = 400;

    public static final int MAX_RESPONSE_STR_LENGTH = 4096;
}
