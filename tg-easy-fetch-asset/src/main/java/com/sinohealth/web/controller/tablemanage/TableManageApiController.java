package com.sinohealth.web.controller.tablemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.ResourceType;
import com.sinohealth.common.enums.ShelfState;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.biz.scheduler.service.IntegrateSyncTaskService;
import com.sinohealth.system.biz.table.vo.TableListVO;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgAssetTableInfo;
import com.sinohealth.system.domain.TgMetadataInfo;
import com.sinohealth.system.dto.TableInfoDto;
import com.sinohealth.system.dto.TableMappingDTO;
import com.sinohealth.system.dto.TableRelationDto;
import com.sinohealth.system.dto.analysis.SaveTableMappingRequestDTO;
import com.sinohealth.system.dto.table_manage.TableInfoManageDto;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.service.DataManageService;
import com.sinohealth.system.service.IAssetService;
import com.sinohealth.system.service.ITableFieldInfoService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.ITableMappingService;
import com.sinohealth.system.service.ITableRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author Rudolph
 * @Date 2023-08-09 10:20
 * @Desc
 */
@RestController
@RequestMapping("/api/tableManage")
@Api(tags = {"资产门户表单管理接口"})
public class TableManageApiController extends BaseController {

    @Autowired
    private IAssetService assetService;
    @Autowired
    private ITableInfoService tableInfoService;
    @Autowired
    private ITableMappingService tableMappingService;
    @Autowired
    private ITableRelationService tableRelationService;
    @Autowired
    private DataManageService dataManageService;
    @Autowired
    private ITableFieldInfoService tableFieldInfoService;
    @Autowired
    private IntegrateSyncTaskService integrateSyncTaskService;

    @Autowired
    private AppProperties appProperties;


    @GetMapping("/list")
    @ApiOperation(value = "表单管理列表", response = TableInfo.class)
    public TableDataInfo<TableInfo> findbyTablename(@ApiParam("表名") @RequestParam(value = "tableName", required = false) String tableName,
                                                    @RequestParam(value = "pageNum", required = false) Integer pageNum,
                                                    @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        List<TableInfo> list = tableInfoService.findPage(tableName, pageNum, pageSize);
        return getDataTable(list);
    }

    @GetMapping("/{dirId}/allTable")
    @ApiOperation(value = "查询目录所有表", response = TableInfo.class)
    public AjaxResult list(@ApiParam(hidden = true) @PathVariable("dirId") Long dirId) {
        return AjaxResult.success(tableInfoService.list(Wrappers.<TableInfo>query().eq("dir_id", dirId).eq("status", 1)));
    }

    @GetMapping(value = "/{tableId}/metadata/detail")
    @ApiOperation(value = "表单详情", response = TableInfoManageDto.class)
    public AjaxResult metaDataDetailApi(@ApiParam(hidden = true) @PathVariable("tableId") Long id) {
        return AjaxResult.success(tableInfoService.getDetail(id));
    }


    @PostMapping(value = {"/{tableId}/metadata/update"})
    @ApiOperation(value = "表管理-表单更新")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult metaDataUpdateApi(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId,
                                        @Validated @RequestBody TgAssetTableInfo tgAssetTableInfo) {

        TgAssetInfo tgAssetInfo = tgAssetTableInfo.getTgAssetInfo();

        if (tableId == null && tgAssetInfo.getResourceType().equals(ResourceType.TABLE_MANAGEMENT)) {
            return AjaxResult.error(InfoConstants.NEED_TABLE_ID);
        }
        if (assetService.checkSameAssetName(tgAssetInfo.getId(), tgAssetInfo.getAssetName()) > 0) {
            return AjaxResult.error(InfoConstants.DUPLICATED_ASSET_NAME);
        }
        AjaxResult<?> ajaxResult = assetService.fillProcessId4FollowMenuDirItem(tgAssetInfo);
        if (!ajaxResult.isSuccess()) {
            return ajaxResult;
        }

        if (tgAssetInfo.getResourceType().equals(ResourceType.TABLE_MANAGEMENT)) {
            TableInfoManageDto tgTableInfo = tgAssetTableInfo.getBindingData();
            tgTableInfo.setId(tableId);
            tgTableInfo.setProcessId(tgAssetInfo.getProcessId());
            tgTableInfo.setViewTotal(tgAssetInfo.getQueryLimit());
            tgAssetInfo.setAssetBindingDataName(tgTableInfo.getTableName());
            tgAssetInfo.setAssetBindingDataType(CommonConstants.TABLE_TYPE);
            tgAssetInfo.setRelatedId(tableId);
            tgAssetInfo.setType(AssetType.TABLE);
            assetService.addAsset(tgAssetInfo);
            tableInfoService.updateTableInfo(tgTableInfo);
            return AjaxResult.success();
        }
        if (tgAssetInfo.getResourceType().equals(ResourceType.METADATA_MANAGEMENT)) {
            TgMetadataInfo tgMetadataInfo = tgAssetTableInfo.getTgMetadataInfo();
            if (tgAssetInfo.getId() != null) {
                tgMetadataInfo.delete(new QueryWrapper<TgMetadataInfo>() {{
                    eq("asset_id", tgAssetInfo.getId());
                }});
            }
            tgAssetInfo.setAssetBindingDataType(CommonConstants.META_TYPE);
            tgAssetInfo.setAssetBindingDataName(tgMetadataInfo.getMetaDataTable());
            tgMetadataInfo.setUpdateTime(DateUtils.getTime());
            tgMetadataInfo.setUpdater(ThreadContextHolder.getSysUser().getRealName());
            tgAssetInfo.setType(AssetType.TABLE);
            assetService.addAsset(tgAssetInfo);
            tgMetadataInfo.setAssetId(tgAssetInfo.getId());
            tgMetadataInfo.insertOrUpdate();
            tgAssetInfo.setMetaId(tgMetadataInfo.getId());
            tgAssetInfo.updateById();

            // 下架资产，下线关联的数据交换工作流
            if (Objects.equals(tgAssetInfo.getShelfState(), ShelfState.UNLIST.getStatus())) {
                integrateSyncTaskService.asyncOfflineWorkFlow(tgAssetInfo.getId());
            }
            return AjaxResult.success();
        }

        return AjaxResult.error("错误的资源类型");

    }


    @GetMapping(value = "/{tableId}/metadata/fields")
    @ApiOperation(value = "表单详情-元数据信息-基础信息", response = TableFieldInfo.class)
    public AjaxResult getFieldInfo(@ApiParam(hidden = true) @PathVariable("tableId") Long id) {
        return AjaxResult.success(tableInfoService.getTableFieldMeta(id));
    }


    @GetMapping(value = "/{tableId}/metadata/mapping")
    @ApiOperation(value = "表单详情-元数据信息-字段映射列表", response = TableMappingDTO.class)
    public AjaxResult<List<TableMappingDTO>> listMapping(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId,
                                                         @ApiParam(value = "字段ID") @RequestParam(value = "fieldId", required = false) Long fieldId) {
        return AjaxResult.success(tableMappingService.listMapping(tableId, fieldId));
    }

    @GetMapping(value = "/{tableId}/metadata/relation")
    @ApiOperation(value = "表单详情-元数据信息-关联关系", response = TableRelationDto.class)
    public AjaxResult getRelationInfo(@ApiParam(hidden = true) @PathVariable("tableId") Long id) {
        return AjaxResult.success(tableRelationService.getList(id, null));
    }

    @DeleteMapping(value = "/{dirId}/metadata/relation/delete")
    @ApiOperation(value = "删除关联表数据", response = TableRelationDto.class)
    public AjaxResult deleteRelationInfo(@ApiParam(value = "关联id") @RequestParam("relationId") Long relationId, @PathVariable(required = false) @ApiParam(value = "库目录id") String dirId) {
        if (tableRelationService.delete(relationId, dirId)) {

            return AjaxResult.success();
        }
        return AjaxResult.error();
    }

    @PutMapping(value = "/{tableId}/metadata/mapping")
    @ApiOperation(value = "表单详情-元数据信息-保存字段映射")
    public AjaxResult<Boolean> saveMapping(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId,
                                           @Validated @RequestBody SaveTableMappingRequestDTO requestDTO) {
        requestDTO.setTableId(tableId);
        tableMappingService.saveMapping(requestDTO);
        return AjaxResult.success(true);
    }


    @DeleteMapping(value = "/{tableId}/metadata/mapping/{mappingId}")
    @ApiOperation(value = "表单详情-元数据信息-删除字段映射")
    public AjaxResult<Boolean> deleteMapping(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId,
                                             @ApiParam(hidden = true) @PathVariable("mappingId") Long mappingId) {
        tableMappingService.deleteMapping(mappingId);
        return AjaxResult.success(true);
    }

    /**
     * 获取【请填写功能名称】详细信息
     */
    @GetMapping(value = "/{tableId}/metadata")
    @ApiOperation(value = "表单详情-表基础信息", response = TableInfoDto.class)
    public AjaxResult getInfo(@ApiParam(hidden = true) @PathVariable("tableId") Long id) {
        return AjaxResult.success(dataManageService.getTableBaseInfo(id));
    }

    @PostMapping(value = "/metadata/fields/getByIds")
    @ApiOperation(value = "表单详情-批量元数据信息", response = TableFieldInfo.class)
    public AjaxResult getFieldInfo(@RequestBody List<Long> ids) {
        return AjaxResult.success(tableFieldInfoService.findListByIds(ids));
    }

    @GetMapping("/listAll")
    @ApiOperation(value = "全部数据表 + 模板使用的关联表", response = TableInfo.class)
    public TableDataInfo<TableInfo> listAll() {
        List<TableInfo> list = tableInfoService.findAllNotDiyAndSelfDiy();
        return getDataTable(list);
    }

    /**
     * 自身创建的关联表
     */
    @GetMapping("/listAllDiy")
    @ApiOperation(value = "全部数据表 + 模板使用的关联表", response = TableInfo.class)
    public TableDataInfo<TableInfo> listAllDiy() {
        List<TableInfo> list = tableInfoService.findAllDiy(SecurityUtils.getUserId());
        return getDataTable(list);
    }

    /**
     * 所有用户创建的关联表
     */
    @GetMapping("/listAllDiyTable")
    @ApiOperation(value = "全部数据表 + 模板使用的关联表", response = TableInfo.class)
    public TableDataInfo<TableInfo> listAllDiyTable() {
        List<TableInfo> list = tableInfoService.findAllDiy(null);
        return getDataTable(list);
    }

    @GetMapping("/listAllAssetsTable")
    @ApiOperation(value = "全部数据表")
    public TableDataInfo<TableListVO> listAllAssetsTable(@RequestParam("bizType") String bizType) {
        List<TableInfo> list = tableInfoService.listAllAssetsTable(bizType);
        List<TableListVO> result = list.stream().map(v -> {
            TableListVO vo = new TableListVO();
            BeanUtils.copyProperties(v, vo);
            vo.setDefaultFlowProcess(Objects.equals(v.getId(), appProperties.getDefaultCmhTableId()));
            return vo;
        }).collect(Collectors.toList());
        return getDataTable(result);
    }

    @GetMapping("/listAllTables")
    @ApiOperation(value = "获取所有表单", response = TableInfo.class)
    public TableDataInfo<TableInfo> listAllTables() {
        List<TableInfo> list = tableInfoService.list();
        return getDataTable(list);
    }

    @PostMapping(value = "/metadata/create")
    @ApiOperation(value = "新增表单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tableName", value = "表名称", required = true, type = "String"),
            @ApiImplicitParam(name = "localSql", value = "本地创建表sql", required = true, type = "localSql"),
            @ApiImplicitParam(name = "dropTableBeforeCreate", value = "分布式表创建表sql", type = "dropTableBeforeCreate"),
    })
    public AjaxResult createApi(@RequestParam(value = "localSql") String localSql,
                                @RequestParam(value = "tableName") String tableName,
                                @RequestParam(value = "dropTableBeforeCreate", required = false, defaultValue = "false") boolean dropTableBeforeCreate) {
        return dataManageService.createTable(localSql, tableName, dropTableBeforeCreate);
    }

    @GetMapping(value = "/metadata/drop")
    @ApiOperation(value = "删除表单")
    public AjaxResult drop(@RequestParam(value = "tableName", required = true) String tableName) {
        try {
            return dataManageService.dropTable(tableName);
        } catch (SQLException e) {
            return AjaxResult.error("删除失败");
        }
    }

    @GetMapping(value = "/metadata/updateStatus")
    @ApiOperation(value = "更改表单状态")
    public AjaxResult updateStatus(@RequestParam(value = "tableName", required = true) String tableName) {
        dataManageService.updateStatus(tableName);
        return AjaxResult.success();

    }

}
