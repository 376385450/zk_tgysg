package com.sinohealth.common.core.mail;

import cn.hutool.core.io.FileUtil;
import com.sinohealth.common.utils.SpringContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-06-10 14:09
 * @Desc
 */
@Slf4j
public class EmailDefaultHandler implements EmailHandle {
    static String emailFrom;

    public static void setEmailFrom(String emailFrom) {
        EmailDefaultHandler.emailFrom = emailFrom;
    }

    @Override
    public void SendMsg(String es_receiver, String es_title, String es_content) {
        JavaMailSender mailSender = (JavaMailSender) SpringContextUtils.getBean("mailSender");
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);
            // 设置发送方邮箱地址
            helper.setFrom(emailFrom);
            helper.setTo(es_receiver);
            helper.setSubject(es_title);
            helper.setText(es_content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("", e);
        }
    }

    @Override
    public void send(List<String> receivers, String title, String content, List<File> files) {
        JavaMailSender mailSender = (JavaMailSender) SpringContextUtils.getBean("mailSender");
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);
            // 设置发送方邮箱地址
            helper.setFrom(emailFrom);
            helper.setTo(receivers.toArray(new String[]{}));
            helper.setSubject(title);
            helper.setText(content, true);
            if (CollectionUtils.isNotEmpty(files)) {
                for (File file : files) {
                    helper.addAttachment(FileUtil.getName(file), new FileSystemResource(file));
                }
            }
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("", e);
        }
    }
}
