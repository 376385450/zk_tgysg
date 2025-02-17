package com.sinohealth.web.controller.dict;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.api.dict.BizDataDictApi;
import com.sinohealth.common.annotation.IgnoreBodyLog;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dict.dto.BizDataDictPageDTO;
import com.sinohealth.system.biz.dict.dto.BizDataDictValDTO;
import com.sinohealth.system.biz.dict.dto.request.BizDataDictUpsertRequest;
import com.sinohealth.system.biz.dict.dto.request.DataDictPageRequest;
import com.sinohealth.system.biz.dict.dto.request.TryRunSQLRequest;
import com.sinohealth.system.biz.dict.service.BizDataDictService;
import com.sinohealth.system.biz.dict.service.impl.QueryDictAdapter;
import com.sinohealth.system.util.EasyExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:30
 */
@Slf4j
@Api(tags = {"字典管理"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/api/dict/data")
public class BizDataDictApiController implements BizDataDictApi {

    private final BizDataDictService bizDataDictService;
    private final QueryDictAdapter queryDictAdapter;
    private final AppProperties appProperties;

    @Override
    @PostMapping("/pageQuery")
    @ApiOperation(value = "分页查询字典")
    public AjaxResult<IPage<BizDataDictPageDTO>> pageQuery(@RequestBody DataDictPageRequest request) {
        return bizDataDictService.pageQuery(request);
    }

    @Override
    @ApiOperation(value = "查询字典项")
    @GetMapping("/listDictVal")
    public AjaxResult<List<BizDataDictValDTO>> listDictVal(@RequestParam("dictId") Long dictId) {

        return bizDataDictService.listDictVal(dictId);
    }

    @Override
    @ApiOperation(value = "查询字典项")
    @PostMapping("/readExcel")
    public AjaxResult<List<BizDataDictValDTO>> readExcel(@RequestParam("file") MultipartFile file) {
        return bizDataDictService.readExcel(file);
    }

    @Override
    @PostMapping("/upsert")
    @ApiOperation(value = "新增/编辑")
    public AjaxResult<Void> upsert(@RequestBody @Validated BizDataDictUpsertRequest request) {
        return bizDataDictService.upsert(request);
    }

    @Override
    @GetMapping("/deleteById")
    @ApiOperation(value = "删除")
    public AjaxResult<Void> deleteById(@RequestParam("id") Long id) {
        return bizDataDictService.deleteById(id);
    }

    @Override
    @PostMapping("/tryRun")
    @ApiOperation(value = "试运行")
    public AjaxResult<List<BizDataDictValDTO>> tryRun(@RequestBody TryRunSQLRequest request) {
        return queryDictAdapter.tryRun(request);
    }

    @Override
    @ApiOperation(value = "查询客户字典项")
    @GetMapping("/listCustomDictVal")
    @IgnoreBodyLog
    public AjaxResult<List<BizDataDictValDTO>> listCustomDictVal() {
        return bizDataDictService.listDictVal(appProperties.getCustomerId());
    }

    private List<List<String>> head(String[] headMap) {
        List<List<String>> list = new ArrayList<>();

        for (String head : headMap) {
            List<String> headList = new ArrayList<>();
            headList.add(head);
            list.add(headList);
        }
        return list;
    }

    @Override
    @GetMapping("/downloadTemplate")
    public void exportForSort(HttpServletResponse response) throws Exception {
        ExcelWriter excelWriter = null;
        try {
            ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(response.getOutputStream());
            excelWriter = EasyExcelUtil.appendConfig(excelWriterBuilder);

            String[] headers = new String[]{"字典值"};
            WriteSheet sheet = EasyExcel.writerSheet(0, "字典").head(head(headers)).build();
            List<List<Object>> rows = new ArrayList<>();
            excelWriter.write(rows, sheet);
        } catch (Exception e) {
            log.error("异常", e);
            throw e;
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }
}
