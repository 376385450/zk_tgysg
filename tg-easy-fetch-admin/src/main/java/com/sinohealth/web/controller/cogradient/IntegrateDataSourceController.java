package com.sinohealth.web.controller.cogradient;

import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.DbConnectType;
import com.sinohealth.common.enums.DbType;
import com.sinohealth.system.dto.BaseDataSourceParamDto;
import com.sinohealth.system.service.IntegrateDataSourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


/**
 *
 *
 * @author penghaiqiu
 * @date 2021/11/24
 * @since 1.6.2
 */
@Api(tags = {"数据源管理"})
@RestController
@RequestMapping("/integrate/datasource")
public class IntegrateDataSourceController extends BaseController {


    @Autowired
    private IntegrateDataSourceService integrateDataSourceService;

    @ApiOperation(value = "创建数据源")
    @PostMapping(value = "/create")
    @ResponseStatus(HttpStatus.CREATED)
    public AjaxResult createDataSource( @RequestBody BaseDataSourceParamDto dataSourceParam) {
        return integrateDataSourceService.createDataSource(dataSourceParam);
    }


    @ApiOperation(value = "查询数据源通过ID")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "数据源ID", required = true, dataType = "Int", example = "100")
    })
    @PostMapping(value = "/update-ui")
    @ResponseStatus(HttpStatus.OK)
    public AjaxResult queryDataSource(
            @RequestParam("id") int id) {
        return integrateDataSourceService.queryDataSource(id);
    }


    @ApiOperation(value = "分页查询数据源列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchVal", value = "搜索值", dataType = "String"),
            @ApiImplicitParam(name = "pageNo", value = "页码号", dataType = "Int", example = "1"),
            @ApiImplicitParam(name = "pageSize", value = "页大小", dataType = "Int", example = "20")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    public AjaxResult queryDataSourceListPaging(
            @RequestParam(value = "searchVal", required = false) String searchVal,
            @RequestParam("pageNo") Integer pageNo,
            @RequestParam("pageSize") Integer pageSize) {
        return integrateDataSourceService.queryDataSourceListPaging(searchVal,pageNo,pageSize);
    }


    @ApiOperation(value = "连接数据源")
    @PostMapping(value = "/connect")
    @ResponseStatus(HttpStatus.OK)
    public AjaxResult connectDataSource(@RequestBody BaseDataSourceParamDto dataSourceParam) {
        return integrateDataSourceService.checkConnection(dataSourceParam);
    }


    @ApiOperation(value = "连接数据源测试")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "数据源ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/connect-by-id")
    @ResponseStatus(HttpStatus.OK)
    public AjaxResult connectionTest(
            @RequestParam("id") int id) {
        return integrateDataSourceService.connectionTest(id);
    }


    @ApiOperation(value = "删除数据源")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "数据源ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    public AjaxResult delete(
            @RequestParam("id") int id) {
        return integrateDataSourceService.delete(id);
    }


    @ApiOperation(value = "验证数据源")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "数据源名称", required = true, dataType = "String")
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    public AjaxResult verifyDataSourceName(
            @RequestParam(value = "name") String name
    ) {
        return integrateDataSourceService.verifyDataSourceName(name);
    }

    @ApiOperation(value = "查询数据源列表通过数据源类型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "DB_TYPE", required = true, dataType = "DbType")
    })
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    public AjaxResult queryDataSourceList(   @RequestParam("type") DbType type) {
        return  integrateDataSourceService.queryDataSourceList(type);
    }

}
