package com.sinohealth.web.controller.system;

import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.system.biz.assets.dto.TableAssetsListVO;
import com.sinohealth.system.biz.dataassets.dto.AssetValidateDTO;
import com.sinohealth.system.domain.asset.TgAssetRelateResp;
import com.sinohealth.system.dto.assets.AssetApplicationPageQuery;
import com.sinohealth.system.dto.assets.AssetBackendPageQuery;
import com.sinohealth.system.dto.assets.AssetFrontendPageQuery;
import com.sinohealth.system.dto.assets.AssetIndicatorQuery;
import com.sinohealth.system.dto.assets.ChangeSortDTO;
import com.sinohealth.system.dto.assets.CollectAssetRequest;
import com.sinohealth.system.dto.assets.CollectListRequest;
import com.sinohealth.system.dto.assets.ForwardAssetRequest;
import com.sinohealth.system.dto.assets.JudgeViewableRequest;
import com.sinohealth.system.dto.assets.LastAssetQuery;
import com.sinohealth.system.service.IAssetService;
import com.sinohealth.system.vo.CollectListVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * @Author Rudolph
 * @Date 2023-08-23 17:49
 * @Desc
 */
@Slf4j
@Api(value = "/api/asset", tags = {"资产接口"})
@RestController
@RequestMapping({"/api/asset"})
public class AssetApiController extends BaseController {

    @Autowired
    private IAssetService assetService;

    @Autowired
    private RedisTemplate redisTemplate;


    @ApiOperation(value = "获取可关联的资产")
    @GetMapping("/relatableAsset")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "assetId", required = false, value = "当前资产id"),
            @ApiImplicitParam(name = "catalogId", required = true, value = "选择的目录id")
    })
    public AjaxResult<TgAssetRelateResp> relatableAsset(@RequestParam(value = "assetId", required = false) Integer assetId,
                                                        @RequestParam(value = "catalogId") Integer catalogId,
                                                        @RequestParam(value = "lastCatalogId", required = false) Integer lastCatalogId) {
        return assetService.relatableAsset(assetId, catalogId, lastCatalogId);
    }

    @ApiOperation(value = "检验排序值是否重复")
    @GetMapping("/validateSort")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "assetId", required = true, value = "资产id"),
            @ApiImplicitParam(name = "sortNum", required = true, value = "输入的排序值")
    })
    public AjaxResult<AssetValidateDTO> validateSort(@RequestParam("assetId") Long assetId, @RequestParam("sortNum") Integer sortNum) {
        return assetService.validateSort(assetId, sortNum);
    }

    @ApiOperation(value = "修改排序")
    @PostMapping("/changeSort")
    public AjaxResult<Boolean> changeSort(@RequestBody ChangeSortDTO changeSortDTO) {
        Boolean lock = null;
        try {
            lock = redisTemplate.opsForValue().setIfAbsent(RedisKeys.SORT_MODIFY_LOCK, 0, Duration.ofSeconds(10));
            if (BooleanUtils.isTrue(lock)) {
                return assetService.changeSort(changeSortDTO);
            } else {
                return AjaxResult.error("请勿频繁操作");
            }
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error(e.getMessage());
        } finally {
            if (Objects.nonNull(lock)) {
                redisTemplate.delete(RedisKeys.SORT_MODIFY_LOCK);
            }
        }
    }


    @ApiOperation(value = "后台目录资产查询", notes = "", httpMethod = "GET")
    @GetMapping("/query")
    public AjaxResult<?> query(@ModelAttribute AssetBackendPageQuery queryParam) {
        return assetService.query(queryParam);
    }

    @GetMapping("/queryFlowTable")
    public AjaxResult<List<TableAssetsListVO>> queryFlowTable() {
        return assetService.queryFlowTable();
    }

    @ApiOperation(value = "资产最近申请查询", notes = "", httpMethod = "GET")
    @GetMapping("/last_asset_query")
    public AjaxResult<?> lastAssetQuery(@Valid @ModelAttribute LastAssetQuery queryParam) {
        return assetService.lastAssetQuery(queryParam);
    }

    @ApiOperation(value = "资产指标查询")
    @GetMapping("/asset_indicator_query")
    public AjaxResult<?> assetIndicatorQuery(@Valid @ModelAttribute AssetIndicatorQuery queryParam) {
        return assetService.assetIndicatorQuery(queryParam);
    }

    @ApiOperation(value = "前台目录资产查询", notes = "", httpMethod = "GET")
    @GetMapping("/front_tree_query")
//    @Cacheable(value = RedisKeys.Cache.ASSET_TREE)
    public AjaxResult<?> frontTreeQuery(@ModelAttribute AssetFrontendPageQuery queryParam) {
        return assetService.frontTreeQuery(queryParam);
    }

    @ApiOperation(value = "获取有阅读权限的资产列表")
    @GetMapping("/getAllAsset")
    public AjaxResult getAllAsset() {
        return assetService.getAllAsset();
    }


    @ApiOperation(value = "后台我的申请查询", notes = "", httpMethod = "GET")
    @GetMapping("/my_application_query")
    public AjaxResult<?> myApplicationQuery(@ModelAttribute AssetApplicationPageQuery queryParam) {
        if (Objects.isNull(queryParam)) {
            return AjaxResult.error("参数缺失");
        }
        return assetService.myApplicationQuery(queryParam);
    }


    @ApiOperation(value = "后台我的申请分类数量查询", notes = "", httpMethod = "GET")
    @GetMapping("/my_application_count")
    public AjaxResult<?> myApplicationCount() {
        return assetService.myApplicationCount();
    }


    @ApiOperation(value = "逻辑删除资产", notes = "", httpMethod = "DELETE")
    @DeleteMapping("/delete")
    public AjaxResult<?> delete(@RequestParam Long id) {
        return assetService.delete(id);
    }


    @ApiOperation(value = "收藏资产/取消收藏资产")
    @PostMapping("/collect")
    public AjaxResult<Object> collectAsset(@Validated @RequestBody CollectAssetRequest collectAssetRequest) {
        return assetService.collectAsset(collectAssetRequest);
    }


    @ApiOperation(value = "转发资产")
    @PostMapping("/forward")
    public AjaxResult<Object> forwardAsset(@Validated @RequestBody ForwardAssetRequest forwardAssetRequest) {
        return assetService.forwardAsset(forwardAssetRequest);
    }


    @ApiOperation(value = "我的收藏列表")
    @PostMapping("/collect/list")
    public AjaxResult<PageInfo<CollectListVo>> collectList(@Validated @RequestBody CollectListRequest collectListRequest) {
        return assetService.collectList(collectListRequest);
    }


    @ApiOperation(value = "判断是否有查看权限")
    @PostMapping("/collect/judgeViewable")
    public AjaxResult<Object> judgeViewable(@Validated @RequestBody JudgeViewableRequest judgeViewableRequest) {
        return assetService.judgeViewable(judgeViewableRequest);
    }


}
