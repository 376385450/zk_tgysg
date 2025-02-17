package com.sinohealth.web.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.common.RedisLock;
import com.sinohealth.system.biz.process.dto.CreateOrUpdateFlowProcessAlertConfigRequest;
import com.sinohealth.system.biz.process.dto.FlowProcessPageRequest;
import com.sinohealth.system.biz.process.dto.FlowProcessSaveSettingRequest;
import com.sinohealth.system.biz.process.facade.TgFlowProcessFacade;
import com.sinohealth.system.biz.process.service.TgFlowProcessCheckService;
import com.sinohealth.system.biz.process.vo.DqcQcLogVO;
import com.sinohealth.system.biz.process.vo.FlowProcessAlertConfigVO;
import com.sinohealth.system.biz.process.vo.FlowProcessSettingBaseVO;
import com.sinohealth.system.biz.process.vo.FlowProcessVO;
import com.sinohealth.system.dto.common.PageRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author zengjun
 * @Date 2024-08-05 16:22
 */

@Api(value = "/api/flow_process", tags = {"全流程管理接口"})
@Slf4j
@RestController
@RequestMapping("/api/flow_process")
@RequiredArgsConstructor
public class FlowProcessController {

    private final TgFlowProcessFacade tgFlowProcessFacade;
    private final TgFlowProcessCheckService flowProcessCheckService;
    private final RedisLock redisLock;

    /**
     * 保存全流程配置
     *
     * @param request 参数
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/saveSetting")
    @ApiOperation(value = "保存全流程配置")
    public AjaxResult<Void> saveSetting(@RequestBody @Valid FlowProcessSaveSettingRequest request) {
        return tgFlowProcessFacade.saveSetting(request);
    }

    /**
     * 全流程管理-分页
     *
     * @param request 参数
     * @return 全流程记录
     */
    @GetMapping("/page")
    @ApiOperation(value = "全流程管理-分页")
    public AjaxResult<IPage<FlowProcessVO>> page(@ModelAttribute FlowProcessPageRequest request) {
        return tgFlowProcessFacade.page(request);
    }

    /**
     * 删除管理记录
     *
     * @param id 记录编号
     * @return 是否成功
     */
    @ApiOperation(value = "删除管理记录")
    @DeleteMapping("delete/{id}")
    public AjaxResult<Void> delete(@PathVariable(value = "id") Long id) {
        return tgFlowProcessFacade.delete(id);
    }

    /**
     * 分页查询 QC日志
     */
    @ApiModelProperty(value = "qc日志")
    @PostMapping("/pageQcLog")
    public AjaxResult<IPage<DqcQcLogVO>> pageQcLog(@RequestBody PageRequest pageRequest) {
        return flowProcessCheckService.pageQuery(pageRequest);
    }

    /**
     * 分页查询 全流程排期
     */
//    @ApiModelProperty(value = "全流程排期")
//    @PostMapping("/pagePlan")
//    public AjaxResult<IPage<TgFlowProcessPlanPageVO>> pagePlan(@RequestBody PageRequest pageRequest) {
//        return flowProcessCheckService.pageQueryPlan(pageRequest);
//    }

    /**
     * 新建/编辑 全流程排期
     */
//    @PostMapping("/upsertPlan")
//    @ApiOperation(value = "新建/编辑 全流程排期")
//    public AjaxResult<Void> upsertPlan(@RequestBody @Validated TgFlowProcessPlanUpsertRequest request) {
//        return redisLock.wrapperLock(RedisKeys.FlowProcess.PLAN_LOCK_KEY,
//                        () -> flowProcessCheckService.upsertPlan(request))
//                .orElse(AjaxResult.error("请勿重复操作"));
//    }

    /**
     * 查询当前期数
     */
//    @Deprecated
//    @GetMapping("/dqcState")
//    public AjaxResult<DqcLatestStateVO> dqcState() {
//        return flowProcessCheckService.queryCurPeriod();
//    }

    /**
     * 任务完成回调
     *
     * @param bizId    全流程任务主键
     * @param category 全流程任务类别
     * @return 是否回调成功
     */
    @GetMapping("/callback")
    public String callback(@RequestParam(value = "bizId", required = false) Long bizId,
                           @RequestParam(value = "category", required = false) String category,
                           @RequestParam(value = "state", required = false) Integer state,
                           @RequestParam(value = "instanceUid", required = false) String uid) {
        tgFlowProcessFacade.callback(bizId, category, state, uid);
        return "OK";
    }

    /**
     * 详情
     *
     * @param category 类型
     * @return 详情信息
     */
    @ApiOperation("详情")
    @GetMapping("/detail")
    public AjaxResult<FlowProcessSettingBaseVO> detail(@RequestParam(value = "category") String category) {
        return tgFlowProcessFacade.detail(null, category);
    }

    /**
     * 保存告警配置
     *
     * @param request 参数
     * @return 是否成功
     */
    @ApiOperation("保存告警配置")
    @PostMapping("/saveAlertConfig")
    public AjaxResult<Void> saveAlertConfig(@RequestBody @Valid CreateOrUpdateFlowProcessAlertConfigRequest request) {
        return tgFlowProcessFacade.saveAlertConfig(request);
    }

    /**
     * 查询告警配置
     *
     * @param category 类型
     * @return 告警配置
     */
    @ApiOperation("查询告警配置")
    @GetMapping("/queryAlertConfig")
    public AjaxResult<List<FlowProcessAlertConfigVO>>
    queryAlertConfig(@RequestParam(value = "category", required = true) String category) {
        return tgFlowProcessFacade.queryAlertConfig(category);
    }
}
