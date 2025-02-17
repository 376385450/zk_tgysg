package com.sinohealth.system.biz.ws.msg;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-02-29 10:32
 */
public interface MsgType {
    /**
     * 未读数
     */
    String UNREAD = "unread";
    /**
     * 通知列表 刷新
     */
    String NOTICE_REFRESH = "notice_refresh";
    /**
     * 待办列表 刷新
     */
    String TODO_REFRESH = "todo_refresh";
    /**
     * 系统升级 公告发布
     */
    String ANNOUNCEMENT = "announcement";
    /**
     * 资产更新 公告发布
     */
    String ASSETS_UPDATE = "assets_update";

    /**
     * 下载中心 列表刷新
     */
    String DOWNLOAD_REFRESH = "download_refresh";
}
