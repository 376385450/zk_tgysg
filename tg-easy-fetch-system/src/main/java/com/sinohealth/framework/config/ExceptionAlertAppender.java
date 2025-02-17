package com.sinohealth.framework.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.LogbackException;
import cn.hutool.extra.spring.SpringUtil;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.framework.web.exception.GlobalTgExceptionHandler;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.config.ThreadPoolType;
import org.apache.commons.math3.util.Pair;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Kuangcp
 * 2024-04-01 09:42
 * @see GlobalTgExceptionHandler MVC 全局异常捕获入口
 */
public class ExceptionAlertAppender extends AppenderBase<ILoggingEvent> {
    @Override
    protected void append(ILoggingEvent event) {
        try {
            Level level = event.getLevel();
            if (level.isGreaterOrEqual(Level.ERROR)) {
                logError(event);
            }
        } catch (Exception ex) {
            throw new LogbackException(event.getFormattedMessage(), ex);
        }
    }

    /**
     * 当 调用 log.error(e); 且传入异常对象时，就会走入告警流程
     */
    private void logError(ILoggingEvent event) {
        ThrowableProxy info = (ThrowableProxy) event.getThrowableProxy();
        if (Objects.isNull(info)) {
            return;
        }

        Optional<Pair<Long, String>> userOpt = SecurityUtils.getUserPairIgnoreError();

        ThreadPoolTaskExecutor pool = SpringUtil.getBean(ThreadPoolType.ASYNC_TASK);
        pool.execute(() -> {
            Throwable exception = info.getThrowable();
            if (Objects.isNull(exception) || exception instanceof CustomException) {
                return;
            }

            String message = event.getFormattedMessage();
            String loggerName = event.getLoggerName();
            if (Objects.equals(loggerName, ThreadPoolConfig.class.getName())) {
                loggerName = "-";
            }

            Optional<Throwable> exOp = Optional.of(exception);
            String name = exOp.map(Throwable::getClass).map(Class::getName).orElse("");
            String msg = StringUtils.isNotBlank(message) ? message : exOp.map(Throwable::getMessage).orElse("");
            String userInfo = userOpt.map(v -> "[" + v.getKey() + "-" + v.getValue() + "] ").orElse("");

            String finalMsg = userInfo + loggerName + ":" + name + " " + msg;
            if (finalMsg.contains("nacos")) {
                return;
            }

            AlertService alert = SpringUtil.getBean(AlertService.class);

            alert.sendExceptionAlertMsg(finalMsg);
        });
    }

    private void logTrace(ILoggingEvent event) {
        String type = "Logback";
        String name = event.getLevel().toString();
        Object message = event.getFormattedMessage();
        String data;
        if (message instanceof Throwable) {
            data = buildExceptionStack((Throwable) message);
        } else {
            data = event.getFormattedMessage().toString();
        }

        ThrowableProxy info = (ThrowableProxy) event.getThrowableProxy();
        if (info != null) {
            data = data + '\n' + buildExceptionStack(info.getThrowable());
        }

//        Cat.logTrace(type, name, "0", data);
    }

    private String buildExceptionStack(Throwable exception) {
        if (exception != null) {
            StringWriter writer = new StringWriter(2048);
            exception.printStackTrace(new PrintWriter(writer));
            return writer.toString();
        } else {
            return "";
        }
    }
}
