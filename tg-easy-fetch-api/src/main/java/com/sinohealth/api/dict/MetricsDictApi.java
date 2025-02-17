package com.sinohealth.api.dict;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dict.dto.MetricsDictDTO;
import com.sinohealth.system.biz.dict.dto.request.DictCommonPageRequest;
import com.sinohealth.system.biz.dict.dto.request.TableMetricsQueryRequest;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/dict/metrics")
public interface MetricsDictApi {
    @PostMapping("/pageQuery")
    @ApiOperation(value = "分页查询字段")
    AjaxResult<IPage<MetricsDictDTO>> pageQuery(@RequestBody DictCommonPageRequest request);

    @PostMapping("/upsert")
    @ApiOperation(value = "新增/编辑")
    AjaxResult<Void> upsert(@RequestBody MetricsDictDTO request);

    @PostMapping("/queryByTableId")
    @ApiOperation(value = "表查关联指标")
    AjaxResult<List<MetricsDictDTO>> queryByTableId(@RequestBody @Validated TableMetricsQueryRequest request);

    @GetMapping("/deleteById")
    @ApiOperation(value = "删除")
    AjaxResult<Void> deleteById(@RequestParam("id") Long id);

}
