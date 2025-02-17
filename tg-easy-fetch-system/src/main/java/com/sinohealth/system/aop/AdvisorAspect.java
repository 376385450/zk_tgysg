package com.sinohealth.system.aop;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.mail.EmailDefaultHandler;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.audit.dto.AuditRequest;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgAuditProcessInfo;
import com.sinohealth.system.domain.TgMessageRecordDim;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.dto.application.Notice;
import com.sinohealth.system.dto.auditprocess.ProcessNodeEasyDto;
import com.sinohealth.system.event.EventPublisher;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.TgAuditProcessInfoMapper;
import com.sinohealth.system.service.ISysUserService;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @Author Rudolph
 * @Date 2022-05-27 15:24
 * @Desc
 */
@Log
@Aspect
@Component
public class AdvisorAspect {

    @Autowired
    TgAuditProcessInfoMapper processInfoMapper;

    @Autowired
    ISysUserService sysUserService;

    @Autowired
    EventPublisher eventPublisher;

    @Resource
    @Qualifier(ThreadPoolType.POST_MSG)
    private ThreadPoolTaskExecutor pool;

    @Pointcut(value = "@within(com.sinohealth.system.anno.MessagePointCut) || @annotation(com.sinohealth.system.anno.MessagePointCut)")
    public void pointcut() {
    }

    @Value("${spring.mail.username:tech@sinohealth.cn}")
    private String emailFrom;

    @Around("pointcut()")
    public Object messagePushingRecord(ProceedingJoinPoint pjp) throws Throwable {
//        log.info(">>>>>>>>>>>>>>>>>>>> 进入消息记录 | ");
        Object proceed = pjp.proceed();
        SysUser sysUser = ThreadContextHolder.getSysUser();

        pool.execute(() -> {
            TgApplicationInfo tgApplicationInfo = new TgApplicationInfo();

            if ("void com.sinohealth.system.service.impl.ApplicationServiceAspect.getApplication4Insert(TgApplicationInfo)".equals(pjp.getSignature().toString())) {
                // 处理添加申请逻辑
                Optional<Object> optional = Arrays.stream(pjp.getArgs()).filter(a -> a instanceof TgApplicationInfo).findFirst();
                if (optional.isPresent()) {
                    TgApplicationInfo applicationInfo = (TgApplicationInfo) optional.get();
                    // 当前处于开始节点并且不是审核失败
                    boolean isFail = Objects.equals(applicationInfo.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDIT_FAIL);
                    if (applicationInfo.getCurrentIndex() == 0 && !isFail) {
                        handleAddApplication(applicationInfo, sysUser);
                    } else {
                        // 处理审核申请逻辑
                        tgApplicationInfo = handleAuditApplication(applicationInfo.getId(), sysUser, tgApplicationInfo);
                        // 根据流程通知相应人员
                        handleProcessNoticeNode(sysUser, tgApplicationInfo);
                    }
                }
            } else if ("Object com.sinohealth.system.service.impl.AuditProcessServiceImpl.auditProcess(AuditRequest)".equals(pjp.getSignature().toString())) {
                // 审核入参
                Optional<Object> optional = Arrays.stream(pjp.getArgs()).filter(a -> a instanceof AuditRequest).findFirst();
                if (Objects.isNull(proceed)) {
                    return;
                }
                if (optional.isPresent()) {
                    AuditRequest processNodeEasyDto = (AuditRequest) optional.get();
                    // 处理审核申请逻辑
                    tgApplicationInfo = handleAuditApplication(processNodeEasyDto.getApplicationId(), sysUser, tgApplicationInfo);
                    // 根据流程通知相应人员
                    handleProcessNoticeNode(sysUser, tgApplicationInfo);
                }
            }
        });

        return proceed;
    }

    private void handleAddApplication(TgApplicationInfo applicationInfo, SysUser sysUser) {
        log.info(">>>>>>>>>>>>>>>>>>>> 当前提数申请新增/修改逻辑 | ");
        adviceAllHandlersWithInnerMessage(sysUser, applicationInfo);
    }

    private TgApplicationInfo handleAuditApplication(Long applicationId, SysUser sysUser, TgApplicationInfo tgApplicationInfo) {
//        log.info(">>>>>>>>>>>>>>>>>>>> 当前提数流程审核逻辑 | ");
        // 获取提数申请
        tgApplicationInfo = JsonBeanConverter.convert2Obj(tgApplicationInfo.selectById(applicationId));
        // 当前审批节点是最后一个则表示整个审批结束
        boolean currentSuccess = Objects.equals(tgApplicationInfo.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDIT_PASS);
        boolean isLastNode = tgApplicationInfo.getCurrentIndex() >= tgApplicationInfo.getHandleNode().size() - 1;
        boolean isAuthProcessSuccessFinish = isLastNode && currentSuccess;
        if (isAuthProcessSuccessFinish) {
//            // 站内信通知提数申请人通过或者驳回
//            // 1. 这里是通知提数申请人
//            messageRecord.setAdviceWho(tgApplicationInfo.getApplicantId());
//            buildMessage(messageRecord, sysUser, tgApplicationInfo);
//            // 埋点：文档成功申请数据 表: tg_doc_info
            registerDocEvent(tgApplicationInfo);
//            messageRecord.insert();
        } else if (currentSuccess) {
            // 成功通知下一个节点审核人
            adviceAllHandlersWithInnerMessage(sysUser, tgApplicationInfo);
        }
        return tgApplicationInfo;
    }

    private void registerDocEvent(TgApplicationInfo tgApplicationInfo) {
        if (tgApplicationInfo.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.AUDIT_PASS)
                && tgApplicationInfo.getApplicationType().equals(ApplicationConst.ApplicationType.DOC_APPLICATION)) {
            eventPublisher.registerDocEvent(tgApplicationInfo.getDocId(), CommonConstants.SUCCESSFUL_APPLY_TIMES, InfoConstants.SUCCESSFUL_APPLY_TIMES);
        }
    }

    /**
     * 全部审核通过
     *
     * @param tgApplicationInfo
     */
    private void noticeSuccess(SysUser sysUser, TgApplicationInfo tgApplicationInfo) {
        TgAuditProcessInfo tgAuditProcessInfo = JsonBeanConverter.convert2Obj(processInfoMapper.queryProcessByIdAndVersion(tgApplicationInfo.getProcessId(), tgApplicationInfo.getProcessVersion()));
        tgAuditProcessInfo.getSucessNode().stream().filter(it -> "1".equals(it.getIsNotices())).flatMap(it -> it.getNoticesInfo().stream()).forEach(notice -> {
            if (notice.getWay().equals(CommonConstants.EMAIL)) {
                noticeWithEmail(notice);
            } else {
                // 站内信通知
                // 这里存的是用户id，模板中的"发起者"是提数申请的申请人
                notice.getNames().stream()
                        .map(name -> {
                            if ("发起者".equals(name)) {
                                return tgApplicationInfo.getApplicantId();
                            } else {
                                return Long.valueOf(name);
                            }
                        }).forEach(userId -> buildMessage(sysUser, tgApplicationInfo, userId, notice).insert());
            }
        });
    }

    /**
     * 审核驳回通知
     *
     * @param tgApplicationInfo
     */
    private void noticeFail(SysUser sysUser, TgApplicationInfo tgApplicationInfo) {
        TgAuditProcessInfo tgAuditProcessInfo = JsonBeanConverter.convert2Obj(processInfoMapper.queryProcessByIdAndVersion(tgApplicationInfo.getProcessId(), tgApplicationInfo.getProcessVersion()));
        tgAuditProcessInfo.getRejectNode().stream().filter(it -> "1".equals(it.getIsNotices())).flatMap(it -> it.getNoticesInfo().stream()).forEach(notice -> {
            if (notice.getWay().equals(CommonConstants.EMAIL)) {
                noticeWithEmail(notice);
            } else {
                // 站内信通知
                // 这里存的是用户id，模板中的"发起者"是提数申请的申请人
                notice.getNames().stream()
                        .map(name -> {
                            if ("发起者".equals(name)) {
                                return tgApplicationInfo.getApplicantId();
                            } else {
                                return Long.valueOf(name);
                            }
                        }).forEach(userId -> buildMessage(sysUser, tgApplicationInfo, userId, notice).insert());
            }
        });
    }


    private void handleProcessNoticeNode(SysUser sysUser, TgApplicationInfo applyInfo) {
        // 节点通过除默认通知相关处理人和申请人外, 还需要通知流程节点的设定的每一个人员
        // 获取通知对象列表
        int handleNodeIndex = applyInfo.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.AUDIT_PASS) ?
                applyInfo.getCurrentIndex() : applyInfo.getCurrentIndex() - 1;
        handleNodeIndex = Math.max(handleNodeIndex, 0);
        TgAuditProcessInfo auditProcessInfo = processInfoMapper.queryProcessByIdAndVersion(applyInfo.getProcessId(), applyInfo.getProcessVersion());
        TgAuditProcessInfo tgAuditProcessInfo = JsonBeanConverter.convert2Obj(auditProcessInfo);
        // 审批结束相关的通知
        ProcessNodeEasyDto lastHandleNode = applyInfo.getHandleNode().get(handleNodeIndex);
        boolean isFail = lastHandleNode.getStatus().equals(ApplicationConst.AuditStatus.AUDIT_FAIL);
        boolean isSuccess = lastHandleNode.getStatus().equals(ApplicationConst.AuditStatus.AUDIT_PASS);
        boolean isAuthProcessFinish = applyInfo.getCurrentIndex() > applyInfo.getHandleNode().size() - 1 || isFail;

        if (isAuthProcessFinish) {
            // 驳回、结束通知
            // 根据模板通知人员
            if (applyInfo.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.AUDIT_PASS)) {
                noticeSuccess(sysUser, applyInfo);
            } else if (applyInfo.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.AUDIT_FAIL)) {
                noticeFail(sysUser, applyInfo);
            }
        } else {
            // 中间节点成功才通知
            if (isSuccess) {
                List<Notice> noticesInfo = tgAuditProcessInfo.getProcessChainDetailInfo().get(handleNodeIndex).getNoticesInfo();
                // 中间节点通知
                for (Notice notice : noticesInfo) {
                    if (notice.getWay().equals(CommonConstants.EMAIL)) {
                        // 邮件消息通知
                        noticeWithEmail(notice);
                    } else {
                        // 局内消息通知
                        notice.getNames().stream().map(Long::valueOf).forEach(adviceWho -> {
                            buildMessage(sysUser, applyInfo, adviceWho, notice).insert();
                        });
                    }
                }
            }
        }
    }

    @SneakyThrows
    private void noticeWithEmail(Notice n) {
        EmailDefaultHandler.setEmailFrom(new InternetAddress(emailFrom, "天宫易数阁平台", "UTF-8").toString());
        EmailDefaultHandler emailDefaultHandler = new EmailDefaultHandler();
        for (String x : n.getNames()) {
            try {
                SysUser noticedMan = sysUserService.selectUserById(Long.valueOf(x));
                String email = noticedMan.getEmail();
                emailDefaultHandler.SendMsg(email, n.getTitle(), n.getContent());
            } catch (Exception e) {
                log.warning(">>>>>>>>>>>>>>>>>>>> 邮件发送失败, 检查邮箱是否正确 | ");
            }
        }
        if (CollectionUtils.isNotEmpty(n.getEmails())) {
            for (String email : n.getEmails()) {
                try {
                    emailDefaultHandler.SendMsg(email, n.getTitle(), n.getContent());
                } catch (Exception e) {
                    log.warning(">>>>>>>>>>>>>>>>>>>> 邮件发送失败, 检查邮箱" + email + "是否正确 | ");
                }
            }
        }
    }

    private void adviceAllHandlersWithInnerMessage(SysUser sysUser, TgApplicationInfo finalTgApplicationInfo) {
        if (ObjectUtils.isNotNull(finalTgApplicationInfo.selectById()) && StringUtils.isNotBlank(finalTgApplicationInfo.getCurrentHandlers())) {
            List<String> collect = Arrays.stream(finalTgApplicationInfo.getCurrentHandlers().split(",")).collect(Collectors.toList());

            if (collect.size() > 0) {
                collect.stream().mapToLong(Long::valueOf).forEach((h) -> {
                    TgMessageRecordDim messageRecord = buildMessage(sysUser, finalTgApplicationInfo, h);
                    messageRecord.insert();
                });
            }
        }
    }

    private TgMessageRecordDim buildMessage(SysUser sysUser, TgApplicationInfo tgApplicationInfo, Long adviceWho) {
        TgMessageRecordDim message = new TgMessageRecordDim();
        message.setApplicantId(sysUser.getUserId());
        message.setApplicantName(sysUser.getRealName());
        message.setApplicationType(tgApplicationInfo.getApplicationType());
        message.setApplicationId(tgApplicationInfo.getId());
        message.setProcessId(tgApplicationInfo.getProcessId());
        message.setApplyTime(tgApplicationInfo.getCreateTime());
        message.setProcessVersion(tgApplicationInfo.getProcessVersion());
        message.setType(tgApplicationInfo.getCurrentAuditProcessStatus());
        message.setAdviceWho(adviceWho);
        return message;
    }

    private TgMessageRecordDim buildMessage(SysUser sysUser, TgApplicationInfo tgApplicationInfo, Long adviceWho, Notice notice) {
        TgMessageRecordDim message = new TgMessageRecordDim();
        message.setApplicantId(sysUser.getUserId());
        message.setApplicantName(sysUser.getRealName());
        message.setApplicationType(tgApplicationInfo.getApplicationType());
        message.setApplicationId(tgApplicationInfo.getId());
        message.setProcessId(tgApplicationInfo.getProcessId());
        message.setApplyTime(tgApplicationInfo.getCreateTime());
        message.setProcessVersion(tgApplicationInfo.getProcessVersion());
        message.setType(tgApplicationInfo.getCurrentAuditProcessStatus());
        message.setAdviceWho(adviceWho);
        message.setTitle(notice.getTitle());
        message.setContent(notice.getContent());
        return message;
    }


}
