package com.sinohealth.system.biz.application.dto.request;

import com.sinohealth.system.biz.application.dto.FmtHistoryApply;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-09-20 15:09
 */
@Data
public class TransHistoryApplyRequest {

    private List<FmtHistoryApply> applyList;

    /**
     * 调试，不创建申请
     */
    private Boolean debug;

    /**
     * 已有申请时重新申请，而不是忽略
     */
    private Boolean retry;
    /**
     * true 统一申请人 false 转移到对应用户
     */
    private Boolean mockSameUser;

    private Set<Long> ignoreIds;
}
