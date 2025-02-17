package com.sinohealth.web.controller.tablemanage;

import com.sinohealth.api.tablemanage.RelationTableManageApi;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.system.domain.TableInfoDiy;
import com.sinohealth.system.domain.vo.DiyTableUpdateResult;
import com.sinohealth.system.service.RelationTableManageService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 【关联表管理接口】Controller
 */
@RestController
@RequestMapping("/api/relationTableManage")
@Api(tags = {"关联表管理接口"})
public class RelationTableManageApiController extends BaseController implements RelationTableManageApi {


    @Autowired
    private RelationTableManageService relationTableManageService;

    @Override
    @ApiOperation(value = "列表", response = TableInfoDiy.class)
    @GetMapping("/list")
    public TableDataInfo<TableInfoDiy> list(
            @ApiParam("页码，默认1") @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @ApiParam("每页数量，默认10") @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @ApiParam("搜索名称") @RequestParam(value = "name", required = false) String name) {
        List<TableInfoDiy> list = relationTableManageService.page(pageNum, pageSize, name);
        return getDataTable(list);
    }

    @Override
    @ApiOperation(value = "关联表-上传")
    @PostMapping("/importData")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", required = true, value = "文件"),
            @ApiImplicitParam(name = "tableName", required = true, value = "表英文名")
    })
    public AjaxResult<DiyTableUpdateResult> importDataset(@RequestParam("file") MultipartFile file,
                                                          @RequestParam(value = "tableName", required = false) String tableName,
                                                          @RequestParam(value = "tableId", required = false) Long tableId,
                                                          @RequestParam(value = "ignoreNotice", required = false) Boolean ignoreNotice
    ) {
        return relationTableManageService.importData(file, tableName, tableId, ignoreNotice);
    }
}
