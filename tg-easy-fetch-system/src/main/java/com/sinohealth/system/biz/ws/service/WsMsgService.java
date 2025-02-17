package com.sinohealth.system.biz.ws.service;

import java.util.Collection;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-02-29 10:35
 */
public interface WsMsgService {

    void pushUnReadMsg();

    void pushUnReadMsg(Collection<Long> userIds);

    void pushUnReadMsg(Long userId);

    /**
     * 通知
     * @param userId
     */
    void pushNoticeMsg(Long userId);

    void pushNoticeMsg(Collection<Long> userIds, Long id);

    /**
     * 系统升级
     */
    void pushAnnouncementMsg(Long id);

    void pushTodoMsg(Long userId);

    void pushDownloadMsg(Long userId);

    /**
     * 代办
     * @param userId
     */
    void pushTodoMsg(Collection<Long> userId, Long applyId);

    void noticeAudit(Long applyId);

    /**
     * 资产更新
     */
    void pushAssetsMsg(Long assetsId);

    boolean hasWsOnline(Long userId);
}
