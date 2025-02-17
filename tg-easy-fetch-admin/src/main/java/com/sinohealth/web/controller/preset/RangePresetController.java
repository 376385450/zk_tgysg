package com.sinohealth.web.controller.preset;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.rangepreset.dto.RangePresetDTO;
import com.sinohealth.system.biz.rangepreset.dto.request.RangePresetPageRequest;
import com.sinohealth.system.biz.rangepreset.dto.request.RangePresetUpsertRequest;
import com.sinohealth.system.biz.rangepreset.service.RangePresetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-07-24 13:48
 */
@Slf4j
@Api(tags = {"范围预设管理"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping({"/preset/range", "/api/preset/range"})
public class RangePresetController {

    private final RangePresetService rangePresetService;

    @PostMapping("/pageQuery")
    @ApiOperation(value = "分页查询范围预设 使用和管理共用这个接口")
    public AjaxResult<IPage<RangePresetDTO>> pageQuery(@RequestBody RangePresetPageRequest request) {
        return rangePresetService.pageQuery(request);
    }

    @PostMapping("/upsert")
    @ApiOperation(value = "新增或编辑 范围预设")
    public AjaxResult<Void> upsert(@RequestBody @Validated RangePresetUpsertRequest request) {
        return rangePresetService.upsert(request);
    }

    @ApiOperation(value = "依据模板id查询可用模板")
    @GetMapping("/queryByTemplateId")
    public AjaxResult<String> queryByTemplateId(@RequestParam("templateId") Long templateId) {
//        return rangePresetService.queryByTempId(templateId);
        return AjaxResult.success("", "");
    }

    @GetMapping("/deleteById")
    @ApiOperation(value = "删除")
    public AjaxResult<Void> deleteById(@RequestParam("id") Long id) {
        return rangePresetService.deleteById(id);
    }
}
