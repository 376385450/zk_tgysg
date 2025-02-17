package com.sinohealth.framework.config;

import com.sinohealth.common.constant.LogConstant;
import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.common.utils.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-10 17:50
 */
public class ContextCopyingTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        String traceId = MDC.get(LogConstant.TRACE_ID);
        if (StringUtils.isBlank(traceId)) {
            traceId = StrUtil.randomAlpha(14) + " ";
        }
        String finalTraceId = traceId;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return () -> {
            try {
                SecurityContextHolder.getContext().setAuthentication(auth);
                MDC.put(LogConstant.TRACE_ID, finalTraceId);
                runnable.run();
            } finally {
                MDC.remove(LogConstant.TRACE_ID);
            }
        };
    }
}
