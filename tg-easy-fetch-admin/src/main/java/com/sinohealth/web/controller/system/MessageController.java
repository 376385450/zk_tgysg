package com.sinohealth.web.controller.system;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.TgMessageRecordDimMapper;
import com.sinohealth.web.ws.PushMsgService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author Rudolph
 * @Date 2022-06-02 10:00
 * @Desc
 */
@RestController
@RequestMapping("/table_management/message")
public class MessageController {

    @Autowired
    PushMsgService pushMsgService;

    @Autowired
    TgMessageRecordDimMapper mapper;

    @GetMapping("/view")
    public Object viewMessage(@Param("applicationId") Long applicationId) {
        SysUser sysUser = ThreadContextHolder.getSysUser();
        mapper.updateMessageCountByApplicationIdAndAdviceWho(sysUser.getUserId(), applicationId);
        return AjaxResult.success("ok");
    }

    @GetMapping("/push/{uid}")
    public void pushMsg2User(@PathVariable String uid) {
        pushMsgService.pushMsgToUser(uid, "test");
    }
}
