package com.sinohealth.system.biz.message.service;

import com.github.pagehelper.PageInfo;
import com.sinohealth.system.biz.message.dto.MessageDTO;
import com.sinohealth.system.biz.message.dto.MessageDetailDTO;
import com.sinohealth.system.biz.ws.msg.UnReadMsg;
import com.sinohealth.system.dto.notice.NoticeReadDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2024/2/26
 */
public interface MessageService {

    PageInfo<MessageDTO> getMessageList(Long pageNum , Long pageSize , Integer type , Date queryTime, Long userId, Boolean read);

    UnReadMsg unReadMeg(Long userId);

    Void readMessage(NoticeReadDTO noticeReadDTO);

    Void markAllRead(NoticeReadDTO noticeReadDTO);

}
