package com.sinohealth.web.controller.dict;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.annotation.IgnoreBodyLog;
import com.sinohealth.common.annotation.IgnoreLog;
import com.sinohealth.common.annotation.RateLimit;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dict.domain.MetricsDict;
import com.sinohealth.system.biz.dict.dto.MetricsDictDTO;
import com.sinohealth.system.biz.dict.dto.request.DictCommonPageRequest;
import com.sinohealth.system.biz.dict.dto.request.TableMetricsQueryRequest;
import com.sinohealth.system.biz.dict.service.MetricsDictService;
import com.sinohealth.system.biz.dict.util.ExcelUtil;
import com.sinohealth.system.util.EasyExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:31
 */
@Slf4j
@Api(tags = {"指标管理"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping({"/dict/metrics", "/api/dict/metrics"})
public class MetricsDictController {

    private final MetricsDictService metricsDictService;

    @PostMapping("/pageQuery")
    @ApiOperation(value = "分页查询字段")
    @IgnoreLog
    public AjaxResult<IPage<MetricsDictDTO>> pageQuery(@RequestBody DictCommonPageRequest request) {
        return metricsDictService.pageQuery(request);
    }

    @GetMapping("/queryForDesc")
    @ApiOperation(value = "说明文档 引用指标说明")
    public AjaxResult<List<MetricsDictDTO>> queryForDesc(@RequestParam("assetsId") Long assetsId) {
        return metricsDictService.queryAllForDesc(assetsId);
    }

    @PostMapping("/upsert")
    @ApiOperation(value = "新增/编辑")
    public AjaxResult<Void> upsert(@RequestBody MetricsDictDTO request) {
        return metricsDictService.upsert(request);
    }

    @PostMapping("/queryByTableId")
    @ApiOperation(value = "表查关联指标")
    AjaxResult<List<MetricsDictDTO>> queryByTableId(@RequestBody @Validated TableMetricsQueryRequest request) {
        return metricsDictService.queryByTableId(request);
    }

    @GetMapping("/exportAllFields")
    @IgnoreBodyLog
    @RateLimit
    public void exportForSort(HttpServletResponse response) throws Exception {
        ExcelWriter excelWriter = null;
        try {
            ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(response.getOutputStream());
            excelWriter = EasyExcelUtil.appendConfig(excelWriterBuilder);

            String[] headers = new String[]{"指标ID", "指标英文名", "指标中文名", "指标类型", "业务线", "创建人"};
            WriteSheet sheet = EasyExcel.writerSheet(0, "指标库").head(ExcelUtil.head(headers)).build();

            List<List<Object>> rows = new ArrayList<>();
            List<MetricsDictDTO> dictDTOS = metricsDictService.listAll();

            for (MetricsDictDTO dictDTO : dictDTOS) {
                List<Object> row = new ArrayList<>();
                row.add(dictDTO.getId());
                row.add(dictDTO.getFieldName());
                row.add(dictDTO.getName());
                row.add(dictDTO.getMetricsType());
                row.add(BizTypeEnum.getDescList(dictDTO.getBizType()));
                row.add(dictDTO.getCreator());
                rows.add(row);
            }
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

    @PostMapping(value = {"/uploadSortExcel"})
    @IgnoreBodyLog
    public AjaxResult<Void> uploadSortExcel(@RequestParam("file") MultipartFile file) {
        try {
            Workbook book = WorkbookFactory.create(file.getInputStream());
            Sheet sheetAt = book.getSheetAt(0);
            int rowSize = sheetAt.getPhysicalNumberOfRows();
            List<Long> uploadIds = new ArrayList<>();
            List<MetricsDict> fields = new ArrayList<>();
            // skip first head row
            for (int i = 1; i < rowSize; i++) {
                Cell cell = sheetAt.getRow(i).getCell(0);
                if (Objects.isNull(cell)) {
                    continue;
                }
                long id = (long) cell.getNumericCellValue();
                MetricsDict e = new MetricsDict();
                uploadIds.add(id);
                e.setId(id);
                e.setSort(i);
                fields.add(e);
            }

            List<MetricsDict> fieldDicts = metricsDictService.selectAllIds();
            List<Long> existIds = Lambda.buildList(fieldDicts, MetricsDict::getId);
            existIds.removeAll(uploadIds);
            if (!existIds.isEmpty()) {
                return AjaxResult.error("指标库排序失败，原因：导入的指标ID与指标库ID不匹配，请重试");
            }
            metricsDictService.fillSort(fields);
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error("指标库排序失败，原因：导入的表头格式不正确，请重试");
        }

        return AjaxResult.succeed();
    }

    @GetMapping("/deleteById")
    @ApiOperation(value = "删除")
    public AjaxResult<Void> deleteById(@RequestParam("id") Long id) {
        return metricsDictService.deleteById(id);
    }
}
