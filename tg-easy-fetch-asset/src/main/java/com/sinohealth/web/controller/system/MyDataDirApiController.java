package com.sinohealth.web.controller.system;

import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.dataassets.dto.request.DataDirRequest;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.domain.constant.ErrorCode;
import com.sinohealth.system.domain.vo.TableInfoSearchVO;
import com.sinohealth.system.dto.DataDirDto;
import com.sinohealth.system.dto.DataDirUpdateReqDTO;
import com.sinohealth.system.dto.GetDashboardEditParam;
import com.sinohealth.system.dto.SaveArkbiParam;
import com.sinohealth.system.dto.table_manage.DataManageFormDto;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.IMyDataDirService;
import com.sinohealth.system.vo.ApplicationSelectListVo;
import com.sinohealth.system.vo.ArkBIEditVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

/**
 * 数据目录Controller
 *
 * @author linweiwu
 * @date 2022-04-16
 */
@Slf4j
@Api(value = "/api/system/my_dir", tags = {"我的数据-数据目录管理"})
@RestController
@RequestMapping("/api/system/my_dir")
public class MyDataDirApiController extends BaseController {

    @Resource
    private IMyDataDirService myDataDirService;

    @Autowired
    private IApplicationService applicationService;

    @Autowired
    private UserDataAssetsService userDataAssetsService;


    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "DataDir", name = "dataDir", value = "")
    })
    @ApiOperation(value = "添加一个新的地图目录", notes = "我的数据-数据目录管理-添加目录", httpMethod = "POST")
    @PostMapping("new")
    public AjaxResult<Object> newDir(@RequestBody @Valid DataDir dataDir) {
        try {
            myDataDirService.newDir(dataDir);
            Long id = dataDir.getId();
            return AjaxResult.success(InfoConstants.REQUEST_OK, id);
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD, ex);
        }
    }


    /**
     * 我的数据 目录和数据（数据，BI）
     *
     * @see MyDataDirApiController#dataAssetsApi
     */
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "long", name = "dirId", value = "")
    })
    @ApiOperation(value = "查看目录节点", notes = "我的数据-数据目录管理-查看目录树", httpMethod = "GET")
    @GetMapping("dir_tree")
    @Deprecated
    public AjaxResult<Object> dirTreeApi(@RequestParam(defaultValue = "0", required = false) Long dirId,
                                         @RequestParam(required = false) Integer searchStatus,
                                         @RequestParam(required = false) String searchBaseTable,
                                         @RequestParam(required = false) Long baseTableId,
                                         @RequestParam(required = false) String searchProjectName,
                                         @RequestParam(defaultValue = "file,data", required = false) String type,
                                         @RequestParam(required = false) String expireType,
                                         @RequestParam(required = false) String clientNames,
                                         @RequestParam(required = false) Integer requireTimeType,
                                         @RequestParam(required = false) Integer requireAttr) {
        try {
//            List<DataDirDto> list = myDataDirService.getDirTreeGroup(dirId, searchStatus, searchProjectName, searchBaseTable,
//                    baseTableId, expireType, clientNames, requireTimeType, requireAttr);
            return AjaxResult.success(InfoConstants.REQUEST_OK, Collections.emptyList());
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD);
        }
    }

    /**
     * 我的数据 目录和数据（数据，BI）
     */

    @ApiOperation(value = "我的数据 列表")
    @PostMapping("dataAssets")
    public AjaxResult<Object> dataAssetsApi(@RequestBody DataDirRequest request) {
        return AjaxResult.success(InfoConstants.REQUEST_OK, Collections.emptyList());
    }

    /**
     * 另存为 选择目录 时使用
     */

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "long", name = "dirId", value = "")
    })
    @ApiOperation(value = "只查看目录节点", notes = "我的数据-数据目录管理-查看目录树", httpMethod = "GET")
    @GetMapping("all_dir_tree")
    public AjaxResult<Object> myDataDirTreeApi() {
        try {
            List<DataDirDto> list = myDataDirService.getMyDataDirTreeGroup();
            return AjaxResult.success(InfoConstants.REQUEST_OK, list);
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD, ex);
        }
    }

    /**
     * 获取提数申请的父级目录
     *
     * @param applyId
     * @return
     */

    @GetMapping("/dir_tree/trunk")
    public AjaxResult<Object> getDirTree(@RequestParam("applyId") Long applyId) {
        List<DataDirDto> dirTree = myDataDirService.getDirTree(applyId);
        return AjaxResult.success(InfoConstants.REQUEST_OK, dirTree);
    }


    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "long", name = "dirId", value = "")
    })
    @ApiOperation(value = "查看文件节点", notes = "我的数据-数据目录管理-查看目录下的文件", httpMethod = "GET")
    @GetMapping("tables")
    public AjaxResult<Object> getTableList(@RequestParam(defaultValue = "0", required = false) Long dirId) {
        try {
            List<DataManageFormDto> list = myDataDirService.listTablesByDirId(dirId);
            return AjaxResult.success(InfoConstants.REQUEST_OK, list);
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD, ex);
        }
    }


    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "DataDir", name = "dataDir", value = "")
    })
    @ApiOperation(value = "更新目录节点", notes = "我的数据-数据目录管理-修改目录树", httpMethod = "PUT")
    @Transactional(rollbackFor = Exception.class)
    @PutMapping("update")
    public AjaxResult<Object> getDirTree(@RequestBody DataDir dataDir) {
        try {
            int successNums = myDataDirService.update(dataDir);
            // 重排目录底下的目录节点
            List<DataDir> list = myDataDirService.selectSonOfParentDir(dataDir.getParentId());
            for (int idx = 0, sort = 2; idx < list.size(); idx++, sort += 2) {
                list.get(idx).setSort(sort);
                myDataDirService.update(list.get(idx));
            }
            return AjaxResult.success(InfoConstants.REQUEST_OK, successNums);
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD, ex);
        }
    }


    @ApiOperation(value = "更新目录节点", notes = "我的数据-数据目录管理-修改目录树2")
    @PostMapping("/update2")
    public AjaxResult updateDirTree(@Valid @RequestBody DataDirUpdateReqDTO reqDTO) {
        myDataDirService.updateV2(reqDTO);
        return AjaxResult.success();
    }


    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "long", name = "dirId", value = "")
    })
    @ApiOperation(value = "删除目录节点", notes = "我的数据-数据目录管理-删除目录树", httpMethod = "DELETE")
    @DeleteMapping("delete")
    public AjaxResult<Object> deleteDirTree(@RequestParam Long dirId) {
        Integer deletedNums = myDataDirService.delete(dirId);
        return AjaxResult.success(deletedNums);
    }


    @GetMapping("/search/table_source")
    public Object searchTableSource() {
        return AjaxResult.success(applicationService.getSearchTableSource());
    }


    @GetMapping("/search/table_alias")
    public AjaxResult<List<TableInfoSearchVO>> searchTableSourceAlias() {
        return AjaxResult.success(applicationService.getSearchTableAlias());
    }


    @ApiOperation(value = "获取新建BI图表URL", notes = "我的数据-数据目录管理-获取新建BI图表URL", httpMethod = "GET")
    @GetMapping("/bi/chart/edit")
    public AjaxResult<Object> createBIChart(@Validated @RequestParam("assetsId") Long assetsId,
                                            @RequestParam("version") Integer version) {
        try {
            ArkBIEditVo vo = myDataDirService.createBIChart(assetsId, version, SecurityUtils.getUserId());
            return AjaxResult.success(InfoConstants.REQUEST_OK, vo);
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(ErrorCode.APPLY_DATA_SYNC_FAILED, ex.getMessage());
        }
    }


    @ApiOperation(value = "获取新建BI图表URL", notes = "我的数据-数据目录管理-获取新建BI图表URL", httpMethod = "GET")
    @GetMapping("/bi/chart/syncData")
    public AjaxResult syncData(@Validated @RequestParam("assetId") Long assetId) {
        try {
            return myDataDirService.syncData(assetId);
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(ErrorCode.APPLY_DATA_SYNC_FAILED, ex.getMessage());
        }
    }

    @ApiOperation(value = "保存BI图表", notes = "我的数据-数据目录管理-保存BI图表", httpMethod = "POST")
    @PostMapping("/bi/chart/save")
    public AjaxResult<Void> saveBIChart(@Validated @RequestBody SaveArkbiParam param) throws Exception {
        try {
            myDataDirService.updateBIChart(param);
            return AjaxResult.succeed();
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD);
        }
    }

    /**
     * @see MyDataDirApiController#createEmptyBIDashboard 替代实现
     */
    @PostMapping("/bi/dashboard/edit")
    @Deprecated
    public AjaxResult<Object> createBIDashboard(@Validated @RequestBody GetDashboardEditParam param) {
        try {
            ArkBIEditVo vo = myDataDirService.createBIDashboard(param, SecurityUtils.getUserId());
            return AjaxResult.success(InfoConstants.REQUEST_OK, vo);
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(ErrorCode.APPLY_DATA_SYNC_FAILED, "数据开始同步，尚未同步完成");
        }
    }

    @ApiOperation(value = "新建空白BI仪表板URL", notes = "我的数据-数据目录管理-新建BI仪表板URL", httpMethod = "POST")
    @PostMapping("/bi/dashboard/new")
    public AjaxResult<Object> createEmptyBIDashboard() {
        try {
            ArkBIEditVo vo = myDataDirService.createEmptyBIDashboard(SecurityUtils.getUserId());
            return AjaxResult.success(InfoConstants.REQUEST_OK, vo);
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(ErrorCode.APPLY_DATA_SYNC_FAILED, "跳转异常");
        }
    }


    @ApiOperation(value = "保存BI仪表板", notes = "我的数据-数据目录管理-保存BI仪表板", httpMethod = "POST")
    @PostMapping("/bi/dashboard/save")
    public AjaxResult<Void> updateBIDashboard(@Validated @RequestBody SaveArkbiParam param) {
        try {
            myDataDirService.updateBIDashboard(param);
            return AjaxResult.succeed();
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD);
        }
    }

    @GetMapping("/bi/chart/depDashboard")
    public AjaxResult<List<String>> depDashboard(@RequestParam("extAnalysisId") String extAnalysisId) {
        return myDataDirService.queryDepDashboard(extAnalysisId);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "long", name = "extAnalysisId", value = "分析ID")
    })
    @ApiOperation(value = "获取BI图表/仪表板编辑URL", notes = "我的数据-数据目录管理-获取BI图表/仪表板编辑URL", httpMethod = "GET")
    @GetMapping("/bi/chart/modify")
    public AjaxResult<Object> getBIModify(@Validated @RequestParam("extAnalysisId") String extAnalysisId) {
        if (StringUtils.isBlank(extAnalysisId)) {
            return AjaxResult.error("参数为空");
        }
        try {
            ArkBIEditVo vo = myDataDirService.getBIModify(extAnalysisId, SecurityUtils.getUserId());
            return AjaxResult.success(InfoConstants.REQUEST_OK, vo);
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD);
        }
    }


    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "long", name = "extAnalysisId", value = "分析ID")
    })
    @ApiOperation(value = "复制图表/仪表板获取编辑URL", notes = "我的数据-数据目录管理-复制图表/仪表板获取编辑URL", httpMethod = "GET")
    @GetMapping("/bi/viz/copy")
    public AjaxResult<Object> getBICopy(@Validated @RequestParam("extAnalysisId") String extAnalysisId) {
        try {
            ArkBIEditVo vo = myDataDirService.crateBICopy(extAnalysisId);
            return AjaxResult.success(InfoConstants.REQUEST_OK, vo);
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD, ex);
        }
    }


    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "long", name = "extAnalysisId", value = "分析ID")
    })
    @ApiOperation(value = "复制图表/仪表板获取编辑URL", notes = "我的数据-数据目录管理-复制图表/仪表板获取编辑URL", httpMethod = "GET")
    @GetMapping("/bi/viz/copy/to/user")
    public AjaxResult<Object> getBICopy(@Validated @RequestParam("extAnalysisId") String extAnalysisId,
                                        @RequestParam("customerId") Long customerId) {
        try {
            ArkbiAnalysis vo = myDataDirService.createBICopyForCustomer(extAnalysisId, customerId);
            return AjaxResult.success(InfoConstants.REQUEST_OK, vo);
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD, ex);
        }
    }


    /**
     * 用于建仪表板
     */

    @ApiOperation(value = "返回申请项目列表", notes = "我的数据-数据目录管理-返回申请项目列表", httpMethod = "GET")
    @GetMapping("application/list")
    @Deprecated
    public AjaxResult<Object> getApplicationList() {
        try {
            List<ApplicationSelectListVo> list = myDataDirService.getApplicationList();
            return AjaxResult.success(InfoConstants.REQUEST_OK, list);
        } catch (Exception ex) {
            log.error("", ex);
            return AjaxResult.error(InfoConstants.REQUEST_BAD, ex);
        }
    }
}
