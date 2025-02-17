package com.sinohealth.system.biz.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.dto.DataPlanBizDTO;
import com.sinohealth.system.biz.project.dto.DataPlanDetailPageDTO;
import com.sinohealth.system.biz.project.dto.request.BizTypePlanVo;
import com.sinohealth.system.biz.project.dto.request.DataPlanDetailPageRequest;
import com.sinohealth.system.biz.project.dto.request.DataPlanDetailUpdateRequest;
import com.sinohealth.system.biz.project.dto.request.DataPlanPageRequest;
import com.sinohealth.system.biz.project.dto.request.DataPlanYearRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Kuangcp
 * 2024-12-13 14:26
 */
public interface DataPlanService {

    /**
     * 业务线 的 当前期数
     */
    AjaxResult<CurrentDataPlanDTO> curPeriod(String bizType);

    /**
     * 注意一定有对象返回，主数据返回空对象，新加业务线时需同步考虑修改
     *
     * @see com.sinohealth.common.enums.dict.BizTypeEnum
     */
    CurrentDataPlanDTO currentPeriod(String bizType);

    /**
     * 查询当前周期对应的需要出数资产类型
     */
    List<String> queryDeliverTimeType(String bizType);

    List<String> queryNextDeliverTimeType(String bizType);

    /**
     * 排期详情 业务线 排期 月粒度 聚合展示
     */
    AjaxResult<IPage<DataPlanBizDTO>> monthSummary(DataPlanPageRequest request);

    Map<String, DataPlanBizDTO> queryNextDeliverDate();

    AjaxResult<List<String>> listPeriod();

    /**
     * 天的粒度查看所有排期
     */
    AjaxResult<IPage<DataPlanDetailPageDTO>> pageDetail(DataPlanDetailPageRequest request);

    AjaxResult<Void> updateDetail(DataPlanDetailUpdateRequest request);

    /**
     * 年度计划 周期设置
     */
    AjaxResult<Void> rePlanYear(DataPlanYearRequest request);

    AjaxResult<List<BizTypePlanVo>> loadPlan();
}
