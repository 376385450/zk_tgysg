package com.sinohealth.system.biz.alert.service;

import com.sinohealth.system.biz.alert.dto.AssetsAlertMsg;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-16 17:08
 */
public interface AlertService {

    /**
     * 资产创建失败告警
     */
    void sendAssetsAlert(AssetsAlertMsg msg);

    /**
     * 内部业务告警
     */
    void sendDevNormalMsg(String content);

    /**
     * 异常告警
     */
    void sendExceptionAlertMsg(String content);

    /**
     * 发送全流程告警信息
     * 
     * @param webhook webhook
     * @param members 成员
     * @param title 标题
     * @param content 内容
     */
    void sendFlowProcessAlert(String webhook, String members, String title, String content);
}
