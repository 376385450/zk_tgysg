package com.sinohealth.web.controller.dataasset;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.assets.AssetDetail;
import com.sinohealth.system.dto.assets.AssetStatistics;
import com.sinohealth.system.service.AssetInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author shallwetalk
 * @Date 2023/8/26
 */
@Api(tags = "资产详情")
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/data_asset/info"})
public class AssetInfoController {

    @Autowired
    private AssetInfoService assetInfoService;

    @GetMapping("/detail")
    @ApiOperation(value = "资产详情", notes = "", httpMethod = "GET")
    public AjaxResult<AssetDetail> detail (@RequestParam("id") Long id) {
        return assetInfoService.detail(id);
    }

    @GetMapping("/statistics")
    @ApiOperation(value = "收藏与转发量统计")
    public AjaxResult<AssetStatistics> statistics(@RequestParam("id") Long id) {
        return assetInfoService.statistics(id);
    }


}
