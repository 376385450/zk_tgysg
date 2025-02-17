/*
package com.sinohealth.web.controller.dict;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.api.dict.MetricsDictApi;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dict.dto.MetricsDictDTO;
import com.sinohealth.system.biz.dict.dto.request.DictCommonPageRequest;
import com.sinohealth.system.biz.dict.dto.request.TableMetricsQueryRequest;
import com.sinohealth.system.biz.dict.service.MetricsDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

*/
/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:31
 *//*

@Slf4j
@Api(tags = {"指标管理"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/api/dict/metrics")
public class MetricsDictApiController implements MetricsDictApi {

    private final MetricsDictService metricsDictService;

    @Override
    @PostMapping("/pageQuery")
    @ApiOperation(value = "分页查询字段")
    public AjaxResult<IPage<MetricsDictDTO>> pageQuery(@RequestBody DictCommonPageRequest request) {
        return metricsDictService.pageQuery(request);
    }

    @Override
    @PostMapping("/upsert")
    @ApiOperation(value = "新增/编辑")
    public AjaxResult<Void> upsert(@RequestBody MetricsDictDTO request) {
        return metricsDictService.upsert(request);
    }

    @Override
    @PostMapping("/queryByTableId")
    @ApiOperation(value = "表查关联指标")
    public AjaxResult<List<MetricsDictDTO>> queryByTableId(@RequestBody @Validated TableMetricsQueryRequest request) {
        return metricsDictService.queryByTableId(request);
    }

    @Override
    @GetMapping("/deleteById")
    @ApiOperation(value = "删除")
    public AjaxResult<Void> deleteById(@RequestParam("id") Long id) {
        return metricsDictService.deleteById(id);
    }

}*/
