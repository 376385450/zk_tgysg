package com.sinohealth.api.dict;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dict.dto.FieldDictDTO;
import com.sinohealth.system.biz.dict.dto.request.FieldDictBatchSaveRequest;
import com.sinohealth.system.biz.dict.dto.request.FieldDictPageRequest;
import com.sinohealth.system.biz.dict.dto.request.FieldListRequest;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("/api/dict/field")
public interface FieldDictApi {

    @PostMapping("/listQuery")
    @ApiOperation(value = "ID查询字段")
    AjaxResult<List<FieldDictDTO>> listQuery(@RequestBody FieldListRequest request);
    
    @PostMapping("/pageQuery")
    @ApiOperation(value = "分页查询字段")
    AjaxResult<IPage<FieldDictDTO>> pageQuery(@RequestBody FieldDictPageRequest request);

    @PostMapping("/edit")
    @ApiOperation(value = "编辑")
    AjaxResult<Void> edit(@RequestBody FieldDictDTO dictDTO);

    @PostMapping("/batchSave")
    @ApiOperation(value = "批量保存")
    AjaxResult<Void> batchSave(@RequestBody @Validated FieldDictBatchSaveRequest saveRequest);

    @GetMapping("/deleteById")
    @ApiOperation(value = "删除")
    AjaxResult<Void> deleteById(@RequestParam("id") Long id);
    

    @GetMapping("/exportAllFields")
    void exportForSort(HttpServletResponse response) throws Exception;

    @PostMapping(value = {"/uploadSortExcel"})
    AjaxResult<Void> uploadSortExcel(@RequestParam("file") MultipartFile file);
}
