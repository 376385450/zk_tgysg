package com.sinohealth.web.controller.dataasset;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dataassets.dto.FlowAssetsPageDTO;
import com.sinohealth.system.biz.dataassets.dto.PowerBiPushBatchPageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.FlowAssetsPageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.PowerBiPushCreateRequest;
import com.sinohealth.system.biz.dataassets.dto.request.PowerBiPushPageRequest;
import com.sinohealth.system.biz.dataassets.service.PowerBiPushService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-25 10:05
 */
@Api(tags = "PowerBi数据推送")
@RestController
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping({"/api/data_asset/pb"})
public class PowerBiPushController {

    private final PowerBiPushService powerBiPushService;

    @PostMapping("/create")
    public AjaxResult<Void> createPush(@RequestBody @Validated PowerBiPushCreateRequest request) {
        return powerBiPushService.createPush(request);
    }

    @PostMapping("/page")
    public AjaxResult<IPage<PowerBiPushBatchPageDTO>> pageQuery(@RequestBody PowerBiPushPageRequest request) {
        return powerBiPushService.pageQuery(request);
    }

    @PostMapping("/pageAssets")
    public AjaxResult<List<FlowAssetsPageDTO>> pageQuery(@RequestBody FlowAssetsPageRequest request) {
        return powerBiPushService.pageQueryAssets(request);
    }


    @GetMapping("/retry")
    AjaxResult<Void> retryPush(@RequestParam("pushId") Long pushId) {
        return powerBiPushService.retryPush(pushId);
    }

    @GetMapping("/delete")
    public AjaxResult<Void> delete(@RequestParam("pushId") Long pushId) {
        return powerBiPushService.delete(pushId);
    }


    @GetMapping("/queryLog")
    public AjaxResult<String> queryLog(@RequestParam("pushId") Long pushId) {
        return powerBiPushService.queryLog(pushId);
    }
}
