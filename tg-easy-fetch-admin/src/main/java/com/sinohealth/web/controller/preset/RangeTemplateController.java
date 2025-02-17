package com.sinohealth.web.controller.preset;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.rangepreset.dto.RangeTemplatePresetDTO;
import com.sinohealth.system.biz.rangepreset.dto.request.RangePresetPageRequest;
import com.sinohealth.system.biz.rangepreset.dto.request.RangeTemplateUpsertRequest;
import com.sinohealth.system.biz.rangepreset.service.RangeTemplatePresetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 自定义列 预设保存和使用
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-07-28 16:10
 */
@Slf4j
@Api(tags = {"自定义列预设管理"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping({"/preset/rangeTemplate","/api/preset/rangeTemplate"})
public class RangeTemplateController {

    private final RangeTemplatePresetService templatePresetService;

    @PostMapping("/pageQuery")
    @ApiOperation(value = "分页查询范围预设")
    public AjaxResult<IPage<RangeTemplatePresetDTO>> pageQuery(@RequestBody RangePresetPageRequest request) {
        return templatePresetService.pageQuery(request);
    }

    @PostMapping("/upsert")
    @ApiOperation(value = "新增或编辑 范围预设")
    public AjaxResult<Void> upsert(@RequestBody @Validated RangeTemplateUpsertRequest request) {
        return templatePresetService.upsert(request);
    }

    @GetMapping("/deleteById")
    @ApiOperation(value = "删除")
    public AjaxResult<Void> deleteById(@RequestParam("id") Long id) {
        return templatePresetService.deleteById(id);
    }
}
