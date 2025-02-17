package com.sinohealth.web.controller.message;

import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.biz.message.dto.MessageDTO;
import com.sinohealth.system.biz.message.service.MessageService;
import com.sinohealth.system.biz.ws.msg.UnReadMsg;
import com.sinohealth.system.biz.ws.service.WsMsgService;
import com.sinohealth.system.dto.notice.NoticeReadDTO;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;

/**
 * @Author shallwetalk
 * @Date 2024/2/26
 */
@Api(tags = {"消息接口"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/api/messageCenter")
public class MessageCenterApiController {

    private final MessageService messageService;

    @GetMapping("/getMessageList")
    public AjaxResult<PageInfo<MessageDTO>> getMessageList(@RequestParam("pageNum") Long pageNum,
                                                           @RequestParam("pageSize") Long pageSize,
                                                           @RequestParam("type") Integer type,
                                                           @RequestParam(value = "queryTime", required = false) Date queryTime) {
        return AjaxResult.success(messageService.getMessageList(pageNum, pageSize, type, queryTime, SecurityUtils.getUserId(), null));
    }

    @PostMapping("/readMessage")
    public AjaxResult<Void> readMessage(@RequestBody NoticeReadDTO noticeReadDTO) {
        return AjaxResult.success(messageService.readMessage(noticeReadDTO));
    }

    @PostMapping("/markAllRead")
    public AjaxResult<Void> markAllRead(@RequestBody NoticeReadDTO noticeReadDTO) {
        return AjaxResult.success(messageService.markAllRead(noticeReadDTO));
    }

    @GetMapping("/getUnRead")
    public AjaxResult<UnReadMsg> getUnread() {
        return AjaxResult.success(messageService.unReadMeg(SecurityUtils.getUserId()));
    }

    @Autowired
    private WsMsgService msgService;

    @GetMapping("/unread")
    public AjaxResult<UnReadMsg> unreadMsg(@RequestParam(value = "userId", required = false) Long userId, HttpServletRequest request) {
        final UnReadMsg unReadMsg = messageService.unReadMeg(userId);
        return AjaxResult.success(unReadMsg);
    }

}
