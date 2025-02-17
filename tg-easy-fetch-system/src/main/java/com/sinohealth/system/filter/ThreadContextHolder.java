package com.sinohealth.system.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.sinohealth.common.core.domain.entity.SysUser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * @Author Rudolph
 * @Date 2022-04-29 9:57
 * @Desc
 */

@Component
@Scope(value = "request")
public class ThreadContextHolder {

    private static TransmittableThreadLocal<ThreadContext> threadContext = new TransmittableThreadLocal<ThreadContext>() {
        @Override
        public ThreadContext copy(ThreadContext parentValue) {
            // 修改默认拷贝行为，默认为引用传递
            // 使用序列化方式进行拷贝
            if (Objects.nonNull(parentValue)) {
                return JSON.parseObject(JSON.toJSONString(parentValue), parentValue.getClass());
            }
            return null;
        }

        @Override
        protected ThreadContext childValue(ThreadContext parentValue) {
            if (Objects.nonNull(parentValue)) {
                return JSON.parseObject(JSON.toJSONString(parentValue), parentValue.getClass());
            }
            return null;
        }
    };

    public static void setThreadContext(ThreadContext context) {
        threadContext.set(context);
    }

    public static ThreadContext getThreadContext() {
        if (null == threadContext.get()) {
            threadContext.set(new ThreadContextImpl());
        }
        return threadContext.get();
    }

    /**
     * context 内容
     */
    
    public static void setParams(Map<String, Object> params) {
        getThreadContext().setParams(params);
    }

    public static Map getParams() {

        return getThreadContext().getParams();
    }

    public static void setSysUser(SysUser user) {
        getThreadContext().setSysUser(user);
    }

    public static SysUser getSysUser() {
        return getThreadContext().getSysUser();
    }
}
