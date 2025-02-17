package com.sinohealth.api.dict;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dict.dto.BizDataDictPageDTO;
import com.sinohealth.system.biz.dict.dto.BizDataDictValDTO;
import com.sinohealth.system.biz.dict.dto.request.BizDataDictUpsertRequest;
import com.sinohealth.system.biz.dict.dto.request.DataDictPageRequest;
import com.sinohealth.system.biz.dict.dto.request.TryRunSQLRequest;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("/api/dict/data")
public interface BizDataDictApi {

    @PostMapping("/pageQuery")
    @ApiOperation(value = "分页查询字典")
    AjaxResult<IPage<BizDataDictPageDTO>> pageQuery(@RequestBody DataDictPageRequest request);

    @ApiOperation(value = "查询字典项")
    @GetMapping("/listDictVal")
    AjaxResult<List<BizDataDictValDTO>> listDictVal(@RequestParam("dictId") Long dictId);

    @ApiOperation(value = "查询字典项")
    @PostMapping("/readExcel")
    AjaxResult<List<BizDataDictValDTO>> readExcel(@RequestParam("file") MultipartFile file);

    @PostMapping("/upsert")
    @ApiOperation(value = "新增/编辑")
    AjaxResult<Void> upsert(@RequestBody @Validated BizDataDictUpsertRequest request);

    @GetMapping("/deleteById")
    @ApiOperation(value = "删除")
    AjaxResult<Void> deleteById(@RequestParam("id") Long id);

    @PostMapping("/tryRun")
    @ApiOperation(value = "试运行")
    AjaxResult<List<BizDataDictValDTO>> tryRun(@RequestBody TryRunSQLRequest request);

    @ApiOperation(value = "查询客户字典项")
    @GetMapping("/listCustomDictVal")
    AjaxResult<List<BizDataDictValDTO>> listCustomDictVal();

    @GetMapping("/downloadTemplate")
    void exportForSort(HttpServletResponse response) throws Exception;
}
