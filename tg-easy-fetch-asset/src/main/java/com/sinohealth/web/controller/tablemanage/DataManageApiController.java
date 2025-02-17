package com.sinohealth.web.controller.tablemanage;

import com.clickhouse.client.ClickHouseException;
import com.sinohealth.api.tablemanage.DataManageApi;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.TableDataDto;
import com.sinohealth.system.service.DataManageService;
import com.sinohealth.system.service.IDataDirService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 【数据管理接口】Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/dataManage")
@Api(tags = {"数据管理接口"})
public class DataManageApiController extends BaseController implements DataManageApi {

    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private IDataDirService dataDirService;

    /**
     * 地图目录列表
     */
    @Override
    @GetMapping("/tree")
    @ApiOperation("地图目录列表")
    public AjaxResult tree(@RequestParam(value = "name", required = false, defaultValue = "") String name,
                           @RequestParam(value = "menu", required = false, defaultValue = "2") Integer menu) {
        return AjaxResult.success(dataManageService.getTree(name, menu));
    }

    @Override
    @GetMapping("/myTree")
    @ApiOperation("数据地图-树状结构")
    public AjaxResult myTree(@RequestParam(value = "tableShow", required = false) String tableShow,
                             @RequestParam(value = "isFilter", required = false) boolean isFilter) {
        boolean loadTable = true;
        if (StringUtils.isEmpty(tableShow) || "0".equals(tableShow)) {
            loadTable = false;
        }
        List<Long> ids = dataManageService.getDirList();
        return AjaxResult.success(dataDirService.getGroupTreeByDirIds(ids, loadTable, null, isFilter));
    }

    @Override
    @PostMapping(value = "/{tableId}/dataInfo")
    @ApiOperation(value = "底表-数据预览（复合筛选）", response = TableDataDto.class)
    public AjaxResult<Object> tableDataInfoApi(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId,
                                               @Validated @RequestBody GetDataInfoRequestDTO requestDTO) {
        if (tableId == null) {
            return AjaxResult.error("请先上传表, 再进行操作");
        }
        return AjaxResult.success(dataManageService.getTableData(tableId, requestDTO));
    }

    /**
     * 填完范围后 直接预览查询到的数据
     */
    @PostMapping(value = "/cmhDataInfo")
    @ApiOperation(value = "CMH产品表-数据预览（复合筛选）", response = TableDataDto.class)
    public AjaxResult<Object> cmhTableDataInfoApi(@Validated @RequestBody GetDataInfoRequestDTO requestDTO) throws Exception {
        try {
            return AjaxResult.success(dataManageService.getCmhTableData(requestDTO));
        } catch (Exception e) {
            if (e.getCause() instanceof ClickHouseException) {
                return AjaxResult.error("查询异常，无法筛选数据，请检查筛选条件是否填写有误");
            }
            throw e;
        }
    }

    @Override
    @GetMapping("/syncTableInfo")
    @ApiOperation(value = "同步表结构")
    public AjaxResult syncTableInfo() {
        dataManageService.syncTableInfo();
        return AjaxResult.success();
    }

    @Override
    @GetMapping("/allTree")
    @ApiOperation("地图目录树")
    public AjaxResult allTree() {
        return AjaxResult.success(dataManageService.getAllTree());
    }


}
