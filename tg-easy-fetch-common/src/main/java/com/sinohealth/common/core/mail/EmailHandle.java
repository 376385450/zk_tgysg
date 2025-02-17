package com.sinohealth.common.core.mail;

import java.io.File;
import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-06-10 14:07
 * @Desc
 */

public interface EmailHandle{

    void SendMsg(String es_receiver, String es_title, String es_content);

    /**
     * 发送带附件的邮件
     * @param receivers
     * @param title
     * @param content
     * @param files
     */
    void send(List<String> receivers, String title, String content, List<File> files);

}
