package com.sinohealth.system.biz.transfer.listener;

import com.sinohealth.system.biz.transfer.dto.CrApplyVO;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;

/**
 * 常规工作流
 * 通用工作流
 *
 * @author kuangchengping@sinohealth.cn
 * 2024-05-07 19:55
 */
@FunctionalInterface
public interface ExcelFlowApplyHandler {

    void handleRowDetail(CrApplyVO vo, TgTemplateInfo template, TgApplicationInfo info);
}
