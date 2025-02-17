package com.sinohealth.system.biz.alert.service.impl;

import com.sinohealth.common.alert.AlertTemplate;
import com.sinohealth.common.alert.plugin.wechatrobot.message.MarkdownMessage;
import com.sinohealth.common.alert.plugin.wechatrobot.message.TextMessage;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.LogConstant;
import com.sinohealth.common.spi.alert.AlertChannelType;
import com.sinohealth.common.spi.alert.AlertPluginInstance;
import com.sinohealth.common.spi.alert.AlertPluginManager;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.ip.IpUtils;
import com.sinohealth.system.biz.alert.dto.AssetsAlertMsg;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.application.dao.ApplicationTaskConfigDAO;
import com.sinohealth.system.biz.application.domain.ApplicationTaskConfig;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.config.AlertBizType;
import com.sinohealth.system.config.ThreadPoolType;
import com.sun.management.GarbageCollectionNotificationInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author kuangchengping@sinohealth.cn 2023-08-16 17:09
 */
@Slf4j
@Service
public class AlertServiceImpl implements AlertService {

    private final AtomicLong lastAlert = new AtomicLong();
    private final AtomicBoolean delay = new AtomicBoolean();
    private final List<String> msgQueue = Collections.synchronizedList(new ArrayList<>(32));
    private final Duration frequency = Duration.ofSeconds(10);
    @Autowired
    private AppProperties appProperties;
    @Resource
    @Qualifier(AlertBizType.BIZ)
    private AlertTemplate alertTemplate;
    @Resource
    @Qualifier(AlertBizType.DEV)
    private AlertTemplate devAlertTemplate;
    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER)
    private ScheduledExecutorService scheduler;
    @Autowired
    private ApplicationTaskConfigDAO applicationTaskConfigDAO;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private AlertPluginManager alertPluginManager;

    @Override
    public void sendAssetsAlert(AssetsAlertMsg msg) {
        boolean need = !msg.isSuccess() || Objects.equals(appProperties.getAlertType(), 1);
        if (!need) {
            log.warn("ignore alert: msg={}", msg);
            return;
        }

        try {
            if (Objects.nonNull(msg.getAssetsId())) {
                UserDataAssets assets = userDataAssetsDAO.getBaseMapper().selectById(msg.getAssetsId());

                String content = String.format("底表【%s】 V%d 推送资产版本升级失败，影响需求【%s】,请及时处理。", assets.getBaseTableName(),
                    msg.getTableVersion(), assets.getProjectName());
                log.info("create assets: {}", content);
                MarkdownMessage alert = MarkdownMessage.build(content);
                alertTemplate.send(alert);
                return;
            }

            if (Objects.nonNull(msg.getApplyId())) {
                ApplicationTaskConfig config = applicationTaskConfigDAO.queryByApplicationId(msg.getApplyId());
                Optional<ApplicationTaskConfig> configOpt = Optional.ofNullable(config);
                String content = configOpt.map(
                    v -> String.format("工作流【%s】推送资产版本升级失败，影响需求【%s】,请及时处理。", v.getFlowName(), v.getApplicationName()))
                    .orElse("工作流推送资产版本升级失败-参数错误: " + msg.getApplyId() + " " + msg.getTableName());

                log.info("create assets: {}", content);
                MarkdownMessage alert = MarkdownMessage.build(content);
                safeSend(alert);
                return;
            }
            log.warn("param miss: msg={}", msg);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public void sendDevNormalMsg(String content) {
        if (StringUtils.isBlank(appProperties.getWxDevRobot())) {
            log.warn("ROBOT: content={}", content);
            return;
        }
        try {
            MarkdownMessage alert = MarkdownMessage.build(content);
            safeSend(alert);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public void sendExceptionAlertMsg(String content) {
        long now = System.currentTimeMillis();
        long last = lastAlert.get();
        Duration delta = Duration.ofMillis(now - last);
        String hostIp = IpUtils.getHostIp();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        String nowStr = dateFormat.format(new Date());
        String traceId = MDC.get(LogConstant.TRACE_ID);

        String msg = nowStr + " <font color=\"warning\">" + traceId + "</font> " + hostIp + "\n" + content;
        if (delta.compareTo(frequency) > 0) {
            lastAlert.set(now);
            sendDevNormalMsg(msg);
        } else {
            if (msgQueue.size() > 20) {
                log.warn("EXCEPTION BOOM");
                return;
            }
            msgQueue.add(msg);
            if (delay.get()) {
                return;
            }

            delay.set(true);
            scheduler.schedule(() -> {
                String msgBlock = String.join("\n", msgQueue);
                msgQueue.clear();
                lastAlert.set(System.currentTimeMillis());
                sendDevNormalMsg(msgBlock);
                delay.set(false);
            }, frequency.minus(delta).toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void sendFlowProcessAlert(String webhook, String members, String title, String content) {
        if (StringUtils.isEmpty(webhook) || StringUtils.isEmpty(content)) {
            return;
        }
        try {
            AlertPluginInstance alertPluginInstance = new AlertPluginInstance();
            alertPluginInstance.setAlertChannelType(AlertChannelType.wechatrobot);
            Map<String, String> map = new HashMap<>();
            map.put("webHook", webhook);
            alertPluginInstance.setPluginInstanceParams(map);
            AlertTemplate at = new AlertTemplate(alertPluginManager, alertPluginInstance);
            if (StringUtils.isNotBlank(title)) {
                content = title + " - " + content;
            }

            TextMessage alert = TextMessage.build(content.substring(0, Math.min(2047, content.length())), null, null);
            if (StringUtils.isNotBlank(members)) {
                List<String> phones = Arrays.asList(members.split(";"));
                alert.getText().setMentionedMobileList(phones);
            }
            at.send(alert);
        } catch (Exception e) {
            log.info("发送通知异常：{}", e.getMessage());
        }
    }

    private void safeSend(TextMessage alert) {
        String txt = alert.getText().getContent();
        alert.getText().setContent(txt.substring(0, Math.min(2047, txt.length())));
        devAlertTemplate.send(alert);
    }

    private void safeSend(MarkdownMessage alert) {
        String txt = alert.getMarkdown().getContent();
        alert.getMarkdown().setContent(txt.substring(0, Math.min(2047, txt.length())));
        devAlertTemplate.send(alert);
    }

    @PostConstruct
    public void delayStartGcMonitoring() {
        scheduler.schedule(this::installGCMonitoring, 60, TimeUnit.SECONDS);
    }

    /**
     * http://www.fasterj.com/articles/gcnotifs.shtml
     */

    public void installGCMonitoring() {
        // get all the GarbageCollectorMXBeans - there's one for each heap generation
        // so probably two - the old generation and young generation
        List<GarbageCollectorMXBean> gcbeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
        // Install a notifcation handler for each bean
        for (GarbageCollectorMXBean gcbean : gcbeans) {
            // log.info("{}", gcbean);
            NotificationEmitter emitter = (NotificationEmitter)gcbean;
            // use an anonymously generated listener for this example
            // - proper code should really use a named class
            NotificationListener listener = new NotificationListener() {
                // keep a count of the total time spent in GCs
                long totalGcDuration = 0;

                // implement the notifier callback handler
                @Override
                public void handleNotification(Notification notification, Object handback) {
                    // we only handle GARBAGE_COLLECTION_NOTIFICATION notifications here
                    if (notification.getType()
                        .equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
                        // get the information associated with this notification
                        GarbageCollectionNotificationInfo info =
                            GarbageCollectionNotificationInfo.from((CompositeData)notification.getUserData());
                        // get all the info and pretty print it
                        long duration = info.getGcInfo().getDuration();
                        String gctype = info.getGcAction();
                        if ("end of minor GC".equals(gctype)) {
                            gctype = "Young Gen GC";
                        } else if ("end of major GC".equals(gctype)) {
                            gctype = "Old Gen GC";
                        }
                        String headerMsg = gctype + ": - " + info.getGcInfo().getId() + " " + info.getGcName()
                            + " (from " + info.getGcCause() + ") " + duration + " milliseconds; start-end times "
                            + info.getGcInfo().getStartTime() + "-" + info.getGcInfo().getEndTime();
                        // log.info(headerMsg);
                        if (!Objects.equals(gctype, "Old Gen GC")) {
                            return;
                        }
                        sendExceptionAlertMsg(headerMsg);
                        // log.info("GcInfo CompositeType: " + info.getGcInfo().getCompositeType());
                        // log.info("GcInfo MemoryUsageAfterGc: " + info.getGcInfo().getMemoryUsageAfterGc());
                        // log.info("GcInfo MemoryUsageBeforeGc: " + info.getGcInfo().getMemoryUsageBeforeGc());

                        // Get the information about each memory space, and pretty print it
                        Map<String, MemoryUsage> membefore = info.getGcInfo().getMemoryUsageBeforeGc();
                        Map<String, MemoryUsage> mem = info.getGcInfo().getMemoryUsageAfterGc();
                        for (Map.Entry<String, MemoryUsage> entry : mem.entrySet()) {
                            String name = entry.getKey();
                            MemoryUsage memdetail = entry.getValue();
                            long memInit = memdetail.getInit();
                            long memCommitted = memdetail.getCommitted();
                            long memMax = memdetail.getMax();
                            long memUsed = memdetail.getUsed();
                            MemoryUsage before = membefore.get(name);
                            long beforepercent = memCommitted == 0 ? 0 : ((before.getUsed() * 1000L) / memCommitted);
                            long percent = memCommitted == 0 ? 0 : ((memUsed * 1000L) / memCommitted); // >100% when it
                                                                                                       // gets expanded

                            final String memType = memCommitted == memMax ? "(fully expanded)" : "(still expandable)";
                            log.info("  " + name + memType + "used: " + (beforepercent / 10) + "."
                                + (beforepercent % 10) + "%->" + (percent / 10) + "." + (percent % 10) + "%("
                                + ((memUsed / 1048576) + 1) + "MB) / ");
                        }
                        totalGcDuration += info.getGcInfo().getDuration();
                        long percent = totalGcDuration * 1000L / info.getGcInfo().getEndTime();
                        log.info("GC cumulated overhead " + (percent / 10) + "." + (percent % 10) + "%");
                    }
                }
            };

            // Add the listener
            emitter.addNotificationListener(listener, null, null);
        }
    }
}
