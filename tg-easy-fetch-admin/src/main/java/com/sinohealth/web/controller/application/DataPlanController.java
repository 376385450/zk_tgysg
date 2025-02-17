package com.sinohealth.web.controller.application;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.dto.DataPlanBizDTO;
import com.sinohealth.system.biz.project.dto.DataPlanDetailPageDTO;
import com.sinohealth.system.biz.project.dto.request.BizTypePlanVo;
import com.sinohealth.system.biz.project.dto.request.DataPlanDetailPageRequest;
import com.sinohealth.system.biz.project.dto.request.DataPlanDetailUpdateRequest;
import com.sinohealth.system.biz.project.dto.request.DataPlanPageRequest;
import com.sinohealth.system.biz.project.dto.request.DataPlanYearRequest;
import com.sinohealth.system.biz.project.service.DataPlanService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Kuangcp
 * 2024-12-11 10:57
 */
@Api(tags = {"排期管理"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/api/dataPlan")
public class DataPlanController {

    @Autowired
    private DataPlanService dataPlanService;

    /**
     * 业务线 的 当前期数
     */
    @GetMapping("/curPeriod/{bizType}")
    public AjaxResult<CurrentDataPlanDTO> curPeriod(@PathVariable("bizType") String bizType) {
        return dataPlanService.curPeriod(bizType);
    }

    /**
     * 排期详情 业务线 排期 月粒度 聚合展示
     */
    @PostMapping("/monthSummary")
    public AjaxResult<IPage<DataPlanBizDTO>> monthSummary(@RequestBody DataPlanPageRequest request) {
        if (StringUtils.isBlank(request.getBizType())) {
            return AjaxResult.error("业务线未选择");
        }
        return dataPlanService.monthSummary(request);
    }

    @GetMapping("/listPeriod")
    public AjaxResult<List<String>> listPeriod() {
        return dataPlanService.listPeriod();
    }

    /**
     * 天的粒度查看所有排期
     */
    @PostMapping("/pageDetail")
    public AjaxResult<IPage<DataPlanDetailPageDTO>> pageDetail(@RequestBody DataPlanDetailPageRequest request) {
        return dataPlanService.pageDetail(request);
    }

    @PostMapping("/updateDetail")
    public AjaxResult<Void> updateDetail(@RequestBody @Validated DataPlanDetailUpdateRequest request) {
        return dataPlanService.updateDetail(request);
    }

    /**
     * 年度计划 周期设置
     */
    @PostMapping("/planYear")
    public AjaxResult<Void> rePlanYear(@RequestBody DataPlanYearRequest request) {
        return dataPlanService.rePlanYear(request);
    }

    @GetMapping("/loadPlan")
    public AjaxResult<List<BizTypePlanVo>> loadPlan() {
        return dataPlanService.loadPlan();
    }

}
