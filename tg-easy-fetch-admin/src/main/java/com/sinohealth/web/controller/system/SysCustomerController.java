package com.sinohealth.web.controller.system;

import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.ipaas.model.ResMaindatamasterCompanyselectbypageItemDataItem;
import com.sinohealth.system.service.ISysCustomerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 客户管理
 * 
 * 
 */
@RestController
@RequestMapping("/system/customer")
@Api(tags = "客户管理")
public class SysCustomerController extends BaseController {

    @Autowired
    private ISysCustomerService sysCustomerService;

    @GetMapping("/list")
     @ApiOperation(value = "客户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "manageUserId", value = "管理人员", required = false, type = "int"),
            @ApiImplicitParam(name = "status", value = "账号状态", required = false, type = "int"),
            @ApiImplicitParam(name = "searchVal", value = "搜索字段", required = false, type = "string"),
    })
    public TableDataInfo list( @RequestParam(value = "manageUserId",required = false)Integer manageUserId,
                               @RequestParam(value = "status",required = false)Integer status,
                               @RequestParam(value = "searchVal",required = false)String  searchVal)
    {
        startPage();
        List<SysUser> list = sysCustomerService.selectList(manageUserId,status,searchVal);
        return getDataTable(list);
    }


    @GetMapping("/subList")
    @ApiOperation(value = "子账号列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "账号状态", required = false, type = "int"),
            @ApiImplicitParam(name = "searchVal", value = "搜索字段", required = false, type = "string"),
            @ApiImplicitParam(name = "parentAccountId", value = "父账号id", required = true, type = "int")
    })
    public TableDataInfo subList(  @RequestParam(value = "status",required = false)Integer status,
                               @RequestParam(value = "searchVal",required = false)String  searchVal,
                               @RequestParam(value = "parentAccountId",required = true)Integer  parentAccountId  )
    {
        startPage();
        List<SysUser> list = sysCustomerService.selectSubList(status,searchVal,parentAccountId);
        return getDataTable(list);
    }

    @PostMapping("/batchUpdateManage")
    @ApiOperation("批量分配管理人")
    public AjaxResult batchUpdateManage(@RequestBody Map<String,Object> map) {
        List<Long> ids = (List<Long>) map.get("ids");
        Integer manageId = (Integer) map.get("manageId");
        sysCustomerService.updateByBatchIds(ids,manageId);
        return AjaxResult.success();
    }

    @GetMapping("/manageList")
    @ApiOperation(value = "管理人员列表")
    public TableDataInfo manageList()
    {
        List<SysUser> list = sysCustomerService.selectManageUser();
        return getDataTable(list);
    }

    @PostMapping("/getUserCount")
    @ApiOperation("获取不同类型用户总数")
    public AjaxResult getUserCount() {
        return AjaxResult.success(sysCustomerService.getUserCount());
    }

    /**
     * 获主数据企业信息列表
     *
     */
    @GetMapping("/masterCompanyList")
    @ApiOperation("主数据企业信息列表")
    public TableDataInfo masterCompanyList(@RequestParam(required = false) String companyName,
                                                    @RequestParam(required = false) Integer pageNum,
                                                    @RequestParam(required = false) Integer pageSize) {
        startPage();
        List<ResMaindatamasterCompanyselectbypageItemDataItem> list = SinoipaasUtils.masterCompanySelectbypage(companyName,pageNum,pageSize);
        return getDataTable(list);
    }

}
