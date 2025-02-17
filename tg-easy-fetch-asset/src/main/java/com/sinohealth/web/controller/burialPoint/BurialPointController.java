package com.sinohealth.web.controller.burialPoint;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.service.IAssetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author shallwetalk
 * @Date 2023/8/26
 */
@Api(tags = {"数据分析-分析项目"})
@RestController
@RequestMapping({"/api/burialPoint"})
public class BurialPointController {

    @Autowired
    IAssetService iAssetService;

    @ApiOperation("资产埋点")
    @GetMapping("/assets")
    public AjaxResult<Object> assets(@RequestParam("assetId") String assetId) {
        if (assetId == null) {
            throw new RuntimeException("资产id不可为空");
        }
        return iAssetService.viewAsset(Long.valueOf(assetId));
    }


}
