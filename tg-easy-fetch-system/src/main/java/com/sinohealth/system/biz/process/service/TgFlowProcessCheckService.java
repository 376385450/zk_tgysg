package com.sinohealth.system.biz.process.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.process.dto.CreateAutoProcessRequest;
import com.sinohealth.system.biz.process.vo.DqcQcLogVO;
import com.sinohealth.system.dto.common.PageRequest;

import java.util.Optional;

/**
 * @author Kuangcp
 * 2024-08-08 19:20
 */
public interface TgFlowProcessCheckService {

    AjaxResult<String> queryCurPeriod(Long templateId);

//    AjaxResult<DqcLatestStateVO> queryCurPeriod();

    AjaxResult<IPage<DqcQcLogVO>> pageQuery(PageRequest pageRequest);

    Optional<CreateAutoProcessRequest> buildReqByCheck();

    // 全流程排期
//    AjaxResult<IPage<TgFlowProcessPlanPageVO>> pageQueryPlan(PageRequest pageRequest);
//    AjaxResult<Void> upsertPlan(@RequestBody @Valid TgFlowProcessPlanUpsertRequest request);

}
