package com.sinohealth.system.biz.ws.msg;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-02-28 18:44
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UnReadMsg extends AbstractMsg implements Serializable {

    private Long userId;
    private long applyCount; // 申请通知
    private long updateCount; // 资产更新
    private long upgradeCount; // 系统升级
    private long todoCount; // 待办

    @Override
    public String getType() {
        return MsgType.UNREAD;
    }
}
