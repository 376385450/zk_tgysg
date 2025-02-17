package com.sinohealth.api.tablemanage;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.TableDataDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/dataManage")
public interface DataManageApi {
    @GetMapping("/tree")
    @ApiOperation("地图目录列表")
    AjaxResult tree(@RequestParam(value = "name", required = false, defaultValue = "") String name,
                    @RequestParam(value = "menu", required = false, defaultValue = "2") Integer menu);

    @GetMapping("/myTree")
    @ApiOperation("数据地图-树状结构")
    AjaxResult myTree(@RequestParam(value = "tableShow", required = false) String tableShow,
                      @RequestParam(value = "isFilter", required = false) boolean isFilter);

    @PostMapping(value = "/{tableId}/dataInfo")
    @ApiOperation(value = "底表-数据预览（复合筛选）", response = TableDataDto.class)
    AjaxResult<Object> tableDataInfoApi(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId,
                                        @Validated @RequestBody GetDataInfoRequestDTO requestDTO);

    @GetMapping("/syncTableInfo")
    @ApiOperation(value = "同步表结构")
    AjaxResult syncTableInfo();

    @GetMapping("/allTree")
    @ApiOperation("地图目录树")
    AjaxResult allTree();
}
