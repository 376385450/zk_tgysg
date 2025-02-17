package com.sinohealth.web.controller.dataasset.homePage;

import com.alibaba.fastjson.JSON;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.biz.dataassets.service.HomePageService;
import com.sinohealth.system.biz.homePage.AssetApply;
import com.sinohealth.system.biz.homePage.AssetDistribution;
import com.sinohealth.system.biz.homePage.AssetTypeStatistics;
import com.sinohealth.system.biz.homePage.DataStatistics;
import com.sinohealth.system.biz.homePage.HotAssetsDTO;
import com.sinohealth.system.biz.homePage.LatestAsset;
import com.sinohealth.system.domain.statistic.VerticalModel;
import com.sinohealth.system.service.ICustomStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author shallwetalk
 * @Date 2023/8/24
 */
@Api(tags = "资产门户首页接口")
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/data_asset/homePage"})
public class AssetsHomePageController {

    @Autowired
    HomePageService service;

    @Autowired
    private final ICustomStatisticsService customStaticsService;

    private final RedisTemplate redisTemplate;

    @ApiOperation("热门资产")
    @GetMapping("/hotAssets")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "类型 1按申请次数 2按浏览次数", value = "type", required = true),
            @ApiImplicitParam(name = "条数", value = "pageSize", required = true),
            @ApiImplicitParam(name = "目录id", value = "catalogId", required = false),
            @ApiImplicitParam(name = "source 1热门 2冷门")

    })
    @Cacheable(value = RedisKeys.Cache.STATISTICS)
    public AjaxResult<List<HotAssetsDTO>> hotAssets(@RequestParam("type") Integer type,
                                                    @RequestParam("pageSize") Integer pageSize,
                                                    @RequestParam(value = "catalogId", required = false) Integer catalogId,
                                                    @RequestParam(value = "source") Integer source) {
        return service.hotAssets(type, pageSize, catalogId, source);
    }

    @ApiOperation("页头资产统计")
    @GetMapping("/assetStatistics")
    @Cacheable(value = RedisKeys.Cache.STATISTICS)
    public AjaxResult<DataStatistics> getAssetStatistics(@RequestParam(value = "catalogId", required = false) Integer catalogId) {
        return service.getAssetStatistics(catalogId);
    }

    @ApiOperation("资产分布统计")
    @GetMapping("/assetDistribution")
    @Cacheable(value = RedisKeys.Cache.STATISTICS)
    public AjaxResult<AssetDistribution> assetDistribution(@RequestParam(value = "catalogId", required = false) Integer catalogId) {
        return service.assetDistribution(catalogId);
    }

    @ApiOperation("资产分布2.0--二级目录统计")
    @GetMapping("/assetDistributions")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "顶级目录id", value = "catalogId", required = true),
            @ApiImplicitParam(name = "类型 1 主业务 2 资产类型", value = "type", required = true)

    })
    @Cacheable(value = RedisKeys.Cache.STATISTICS)
    public AjaxResult<AssetDistribution> assetDistributions(@RequestParam(value = "catalogId") Integer catalogId,
                                                            @RequestParam(value = "type") Integer type) {
        return service.assetDistributions(catalogId, type);
    }

    @ApiOperation("最新资产")
    @GetMapping("/latestAsset")
    public AjaxResult<List<LatestAsset>> latestAsset(@RequestParam("pageSize") Integer pageSize,
                                                     @RequestParam(value = "catalogId", required = false) Integer catalogId) {
        return service.latestAsset(pageSize, catalogId);
    }

    @ApiOperation("资产应用情况")
    @GetMapping("/assetApply")
    @Cacheable(value = RedisKeys.Cache.STATISTICS)
    public AjaxResult<List<AssetApply>> assetApply(@RequestParam(value = "catalogId", required = false) Integer catalogId) {
        return service.assetApply(catalogId);
    }

    @ApiOperation("资产类型")
    @GetMapping("/assetType")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "顶级目录id", value = "catalogId", required = true),
            @ApiImplicitParam(name = "类型 1 全部 2 模型 3 库表 4 文档", value = "type", required = true)

    })
    public AjaxResult<AssetTypeStatistics> assetType(@RequestParam(value = "catalogId") Integer catalogId,
                                                     @RequestParam(value = "type") Integer type) {
        return service.assetType(catalogId, type);
    }

    @ApiOperation("用户资产类型")
    @GetMapping("/userTypeModel")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Integer 1 - 分析师/ 2 - 客户", value = "type", required = true)

    })
    public AjaxResult<?> userTypeModel(@RequestParam(value = "catalogId", required = false) Integer catalogId,
                                       @RequestParam(value = "type", defaultValue = "1") Integer type) {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("option", type == 1 ? "分析师" : "客户");
        parameterMap.put("businessType", "userTypeModel");
        parameterMap.put("uid", SecurityUtils.getUserId());
        if (catalogId != null) {
            parameterMap.put("catalogId", catalogId);
        }
        String key = "userTypeModel" + parameterMap.hashCode();
        Object cache = redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(cache)) {
            VerticalModel val = JSON.parseObject(cache.toString(), VerticalModel.class);
            return AjaxResult.success(val);
        }

        VerticalModel val = customStaticsService.getUserTypeModel(parameterMap);
        redisTemplate.opsForValue().set(key, JSON.toJSONString(val), 3, TimeUnit.MINUTES);
        return AjaxResult.success(val);
    }


}
