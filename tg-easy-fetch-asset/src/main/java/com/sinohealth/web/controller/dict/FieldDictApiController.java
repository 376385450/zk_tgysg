package com.sinohealth.web.controller.dict;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.api.dict.FieldDictApi;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.enums.dict.FieldUseWayEnum;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.biz.dict.dto.FieldDictDTO;
import com.sinohealth.system.biz.dict.dto.request.FieldDictBatchSaveRequest;
import com.sinohealth.system.biz.dict.dto.request.FieldDictPageRequest;
import com.sinohealth.system.biz.dict.dto.request.FieldListRequest;
import com.sinohealth.system.biz.dict.service.FieldDictService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:31
 */
@Slf4j
@Api(tags = {"字段库管理"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/api/dict/field")
public class FieldDictApiController implements FieldDictApi {

    private final FieldDictService fieldDictService;

    @Override
    @PostMapping("/listQuery")
    @ApiOperation(value = "ID查询字段")
    public AjaxResult<List<FieldDictDTO>> listQuery(@RequestBody FieldListRequest request) {
        return fieldDictService.listQuery(request);
    }

    @Override
    @PostMapping("/pageQuery")
    @ApiOperation(value = "分页查询字段")
    public AjaxResult<IPage<FieldDictDTO>> pageQuery(@RequestBody FieldDictPageRequest request) {
        return fieldDictService.pageQuery(request);
    }

    @Override
    @PostMapping("/edit")
    @ApiOperation(value = "编辑")
    public AjaxResult<Void> edit(@RequestBody FieldDictDTO dictDTO) {
        return fieldDictService.edit(dictDTO);
    }

    @Override
    @PostMapping("/batchSave")
    @ApiOperation(value = "批量保存")
    public AjaxResult<Void> batchSave(@RequestBody @Validated FieldDictBatchSaveRequest saveRequest) {
        return fieldDictService.batchSave(saveRequest);
    }

    @Override
    @GetMapping("/deleteById")
    @ApiOperation(value = "删除")
    public AjaxResult<Void> deleteById(@RequestParam("id") Long id) {
        return fieldDictService.deleteById(id);
    }

    @Override
    @GetMapping("/exportAllFields")
    public void exportForSort(HttpServletResponse response) throws Exception {
        ExcelWriter excelWriter = null;
        try {
            ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(response.getOutputStream());
            excelWriter = EasyExcelUtil.appendConfig(excelWriterBuilder);

            String[] headers = new String[]{"字段ID", "字段英文名", "字段中文名", "字段描述", "字段分类", "业务线", "使用途径", "创建人"};
            WriteSheet sheet = EasyExcel.writerSheet(0, "字段库").head(ExcelUtil.head(headers)).build();

            List<List<Object>> rows = new ArrayList<>();
            List<FieldDictDTO> dictDTOS = fieldDictService.listAll();

            for (FieldDictDTO dictDTO : dictDTOS) {
                List<Object> row = new ArrayList<>();
                row.add(dictDTO.getId());
                row.add(dictDTO.getFieldName());
                row.add(dictDTO.getName());
                row.add(dictDTO.getDescription());
                row.add(FieldGranularityEnum.getDesc(dictDTO.getGranularity()));
                row.add(BizTypeEnum.getDescList(dictDTO.getBizType()));
                row.add(FieldUseWayEnum.getDesc(dictDTO.getUseWay()));
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

    @Override
    @PostMapping(value = {"/uploadSortExcel"})
    public AjaxResult<Void> uploadSortExcel(@RequestParam("file") MultipartFile file) {
        try {
            Workbook book = WorkbookFactory.create(file.getInputStream());
            Sheet sheetAt = book.getSheetAt(0);
            int rowSize = sheetAt.getPhysicalNumberOfRows();
            List<Long> uploadIds = new ArrayList<>();
            List<FieldDict> fields = new ArrayList<>();
            // skip first head row
            for (int i = 1; i < rowSize; i++) {
                Cell cell = sheetAt.getRow(i).getCell(0);
                long id = (long) cell.getNumericCellValue();
                FieldDict e = new FieldDict();
                uploadIds.add(id);
                e.setId(id);
                e.setSort(i);
                fields.add(e);
            }

            List<FieldDict> fieldDicts = fieldDictService.selectAllIds();
            Map<Long, Long> fieldDictMap = fieldDicts.stream().filter(v -> Objects.nonNull(v.getDictId())).collect(Collectors.toMap(v -> v.getId(), v -> v.getDictId(), (front, current) -> current));
            List<Long> existIds = Lambda.buildList(fieldDicts, FieldDict::getId);
            existIds.removeAll(uploadIds);
            if (existIds.size() > 0) {
                return AjaxResult.error("字段库排序失败，原因：导入的字段ID与字段库ID不匹配，请重试");
            }
            for (FieldDict field : fields) {
                field.setDictId(fieldDictMap.get(field.getId()));
            }
            fieldDictService.fillSort(fields);
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error("字段库排序失败，原因：导入的表头格式不正确，请重试");
        }

        return AjaxResult.succeed();
    }
}
