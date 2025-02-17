package com.sinohealth.web.controller.dataasset;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dataassets.dto.AssetsFlowAutoBatchDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsFlowAutoBatchPageDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsFlowBatchInfoDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsFlowBatchPageDTO;
import com.sinohealth.system.biz.dataassets.dto.FlowAssetsPageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsFlowAutoBatchCreateRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsFlowBatchCreateRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsFlowBatchEditRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsFlowBatchPageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AutoFlowBatchPageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.FlowAssetsAutoPageRequest;
import com.sinohealth.system.biz.dataassets.service.AssetsFlowService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Kuangcp
 * 2024-07-15 15:10
 */
@Api(tags = "工作流出数管理")
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/data_asset/flow"})
public class AssetsFlowController {

    private final AssetsFlowService flowService;

    @PostMapping("/page")
    public AjaxResult<IPage<AssetsFlowBatchPageDTO>> pageQuery(
            @RequestBody @Validated AssetsFlowBatchPageRequest request) {
        return flowService.pageQueryBatch(request);
    }

    @PostMapping("/pageAssets")
    public AjaxResult<List<FlowAssetsPageDTO>> pageQuery(@RequestBody FlowAssetsAutoPageRequest request) {
        return flowService.listAssets(request);
    }

    @PostMapping("/create")
    public AjaxResult<Void> createBatch(@RequestBody AssetsFlowBatchCreateRequest request) {
        request.setFilterOne(false);
        return flowService.createBatch(request);
    }

    @GetMapping("/detail")
    public AjaxResult<AssetsFlowBatchInfoDTO> detail(@RequestParam("id") Long id) {
        return flowService.batchDetail(id);
    }

    @PostMapping("/edit")
    public AjaxResult<Void> editBatch(@RequestBody AssetsFlowBatchEditRequest request) {
        return flowService.editBatch(request);
    }

    @GetMapping("/delete")
    public AjaxResult<Void> deleteBatch(@RequestParam("id") Long id) {
        return flowService.deleteBatch(id);
    }

    /**
     * 自动出数配置 主列表
     */
    @PostMapping("/auto/list")
    public AjaxResult<List<AssetsFlowAutoBatchPageDTO>> listAutoBatch(@RequestBody AutoFlowBatchPageRequest request) {
        return AjaxResult.success(flowService.listAutoBatch(request));
    }

    @PostMapping("/auto/listScheduler")
    public AjaxResult<List<AssetsFlowAutoBatchPageDTO>> listAutoBatchByScheduler(@RequestBody AutoFlowBatchPageRequest request) {
        return flowService.listAutoBatchByScheduler(request);
    }

    /**
     * 自动出数配置 查明细内容
     */
    @GetMapping("/auto/detail")
    public AjaxResult<AssetsFlowAutoBatchDTO> queryAutoBatch(@RequestParam Long id) {
        return flowService.queryAutoBatch(id);
    }

    /**
     * 创建自动出数任务
     */
    @PostMapping("/auto/upsert")
    public AjaxResult<Void> upsertAutoBatch(@RequestBody @Validated AssetsFlowAutoBatchCreateRequest request) {
        return flowService.upsertAutoBatch(request);
    }

    @GetMapping("/auto/delete")
    public AjaxResult<Void> deleteAutoBatch(@RequestParam("id") Long id) {
        return flowService.deleteAutoBatch(id);
    }

}
