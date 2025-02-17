package com.sinohealth.web.controller.system;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.common.annotation.RepeatSubmit;
import com.sinohealth.common.config.DataSourceFactory;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableDataInfo;
import com.sinohealth.common.enums.LogType;
import com.sinohealth.common.enums.SpeedOfProgressType;
import com.sinohealth.common.enums.TaskType;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.uuid.IdUtils;
import com.sinohealth.data.common.response.PageInfo;
import com.sinohealth.data.intelligence.api.metadataRegister.dto.ColumnDTO;
import com.sinohealth.system.biz.dataassets.dto.FlowAssetsPageDTO;
import com.sinohealth.system.biz.dir.vo.TablePageVO;
import com.sinohealth.system.biz.table.dto.TableDiffPageRequest;
import com.sinohealth.system.biz.table.dto.TableDiffPlanCreateOrUpdateRequest;
import com.sinohealth.system.biz.table.dto.TableDiffRequest;
import com.sinohealth.system.biz.table.dto.TablePageQueryRequest;
import com.sinohealth.system.biz.table.dto.TablePushDetailPageRequest;
import com.sinohealth.system.biz.table.dto.TableSnapshotPageRequest;
import com.sinohealth.system.biz.table.dto.TableSnapshotPushRequest;
import com.sinohealth.system.biz.table.dto.TableSnapshotUpdateRequest;
import com.sinohealth.system.biz.table.service.TableInfoSnapshotService;
import com.sinohealth.system.biz.table.vo.TableComparePlanVO;
import com.sinohealth.system.biz.table.vo.TableInfoCompareTaskVO;
import com.sinohealth.system.biz.table.vo.TableInfoSnapshotPageVO;
import com.sinohealth.system.biz.table.vo.TableInfoVO;
import com.sinohealth.system.biz.table.vo.TableSnapInfoVO;
import com.sinohealth.system.domain.SysUserTable;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TableLog;
import com.sinohealth.system.domain.TableTask;
import com.sinohealth.system.dto.DataSourceDTO;
import com.sinohealth.system.dto.DataTreeDto;
import com.sinohealth.system.dto.ExchangeColumnMapper;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.PageListReturnDataDTO;
import com.sinohealth.system.dto.TableCopyDto;
import com.sinohealth.system.dto.TableCopyInfoDto;
import com.sinohealth.system.dto.TableCreateDto;
import com.sinohealth.system.dto.TableDTO;
import com.sinohealth.system.dto.TableDataDto;
import com.sinohealth.system.dto.TableFieldInfoDto;
import com.sinohealth.system.dto.TableFieldSortDTO;
import com.sinohealth.system.dto.TableInfoDto;
import com.sinohealth.system.dto.TableMappingDTO;
import com.sinohealth.system.dto.TableRelationUpdateDto;
import com.sinohealth.system.dto.UpdateReturnDataRequestDTO;
import com.sinohealth.system.dto.analysis.SaveTableMappingRequestDTO;
import com.sinohealth.system.service.IAssetService;
import com.sinohealth.system.service.ISysUserTableService;
import com.sinohealth.system.service.ITableFieldInfoService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.ITableLogService;
import com.sinohealth.system.service.ITableMappingService;
import com.sinohealth.system.service.ITableRelationService;
import com.sinohealth.system.service.ITableTaskService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
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
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 【请填写功能名称】Controller
 *
 * @author jingjun
 * @date 2021-04-16
 */
@RestController
@RequestMapping("/api/system/table")
//@Api(tags = {"资产目录数据地图表单接口"})
public class TableInfoApiController extends BaseController {
    @Autowired
    private ITableInfoService tableInfoService;
    @Autowired
    private ITableRelationService tableRelationService;
    @Autowired
    private ITableFieldInfoService tableFieldInfoService;
    @Autowired
    private ISysUserTableService userTableService;
    @Autowired
    private ITableTaskService iTableTaskService;
    @Autowired
    private ITableLogService tableLogService;
    @Resource
    private ITableMappingService tableMappingService;

    @Autowired
    private IAssetService assetService;


    private final static String copyTableSql = " create table %s  like %s.%s ";
    private final static String copyDataSql = " create table %s select * from %s.%s ";
    @Autowired
    private TableInfoSnapshotService tableInfoSnapshotService;


    @GetMapping("/{dirId}/list")
    //@ApiOperation(value = "表列表查询", response = TableInfo.class)
    public TableDataInfo list(@PathVariable("dirId") Long dirId,
                              @RequestParam(value = "tableName", required = false) String tableName,
                              @RequestParam(value = "fieldName", required = false) String fieldName,
                              @RequestParam(value = "accessType", required = false) Integer accessType,
                              @RequestParam(value = "isFilter", required = false) boolean isFilter,
                              @RequestParam(value = "pageNum", required = false) Integer pageNum,
                              @RequestParam(value = "pageSize", required = false) Integer pageSize) {

        final IPage<TableInfo> page = tableInfoService.getList(dirId, tableName, fieldName, accessType, isFilter,
                pageNum, pageSize);
        final TableDataInfo<TableInfo> tableDataInfo = getDataTable(page.getRecords());
        tableDataInfo.setTotal(page.getTotal());
        return tableDataInfo;
    }


    @GetMapping("/list")
    //@ApiOperation(value = "表名搜索", response = TableInfo.class)
    public AjaxResult findbyTablename(@ApiParam("表名") @RequestParam(value = "tableName", required = false) String tableName, @ApiParam("字段名") @RequestParam(value = "fieldName", required = false) String fieldName) {


        if (!StringUtils.isEmpty(tableName) || (StringUtils.isEmpty(tableName) && StringUtils.isEmpty(fieldName))) {

            return AjaxResult.success(tableInfoService.findByTableName(tableName));
        }
        return AjaxResult.success(tableInfoService.findByFieldName(fieldName));
    }


    @PostMapping("/queryByIds")
    public AjaxResult<List<TableInfoVO>> queryByIds(@RequestBody List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return AjaxResult.error("参数为空");
        }
        List<TableInfo> infos = tableInfoService.getBaseMapper().selectBatchIds(ids);
        List<TableInfoVO> volist = infos.stream().map(v -> {
            TableInfoVO vo = new TableInfoVO();
            BeanUtils.copyProperties(v, vo);
            return vo;
        }).collect(Collectors.toList());
        return AjaxResult.success(volist);
    }


    @GetMapping("/field/list")
    //@ApiOperation(value = "字段名搜索", response = TableInfo.class)
    public AjaxResult findByFieldName(@RequestParam(value = "fieldName") String fieldName) {

        return AjaxResult.success(tableInfoService.findByFieldNames(fieldName));
    }


    @GetMapping("/{dirId}/allTable")
    //@ApiOperation(value = "查询库里所有表", response = TableInfo.class)
    public AjaxResult list(@PathVariable("dirId") Long dirId) {

        return AjaxResult.success(tableInfoService.list(Wrappers.<TableInfo>query().eq("dir_id", dirId).eq("status", 1)));
    }


    @PostMapping(value = "/metadataId")
    //@ApiOperation(value = "表单详情-表基础信息-id传输", response = TableInfoDto.class)
    public AjaxResult getInfoId(@RequestBody List<Long> ids) {
        return AjaxResult.success(tableInfoService.getTableBaseInfoIds(ids));
    }

    /**
     * 获取【请填写功能名称】详细信息
     */

    @GetMapping(value = "/{tableId}/metadata")
    //@ApiOperation(value = "表单详情-表基础信息", response = TableInfoDto.class)
    public AjaxResult getInfo(@ApiParam(hidden = true) @PathVariable("tableId") Long id) {
        return AjaxResult.success(tableInfoService.getTableBaseInfo(id));
    }


    @PostMapping(value = "/{tableId}/metadata")
    //@ApiOperation(value = "表单详情-表基础信息")
    public AjaxResult getInfo(@ApiParam(hidden = true) @PathVariable("tableId") Long id, @RequestBody TableInfoDto dto) {
        dto.setId(id);
        tableInfoService.updateTableInfo(dto);
        return AjaxResult.success();
    }

    /**
     * 获取可用的数据源类型
     *
     * @return
     */
    @GetMapping("/enableDatasourceType")
    public AjaxResult<List<String>> getEnableDataSourceType() {
        final List<String> enableDataSourceType = tableInfoService.getEnableDataSourceType();
        return AjaxResult.success(enableDataSourceType);
    }

    //@GetMapping("/")


    @GetMapping("/getDatasource")
    public AjaxResult<List<DataSourceDTO>> getDatasource(@RequestParam(value = "tenantId") Long tenantId,
                                                         @RequestParam(value = "dataSourceType") String dataSourceType) {
        final List<DataSourceDTO> datasource = tableInfoService.getDatasource(tenantId, dataSourceType);
        return AjaxResult.success(datasource);
    }


    @ApiOperation("挂接元数据")
    @GetMapping("/getMetaTableAsset")
    public AjaxResult<PageInfo<TableDTO>> getMetaTableAsset(@RequestParam(value = "pageNum") Integer pageNum,
                                                            @RequestParam(value = "pageSize") Integer pageSize,
                                                            @RequestParam(value = "tenantId") Long tenantId,
                                                            @RequestParam(value = "realName", required = false) String realName,
                                                            @RequestParam(value = "cnName", required = false) String cnName,
                                                            @RequestParam(value = "dataSourceType", required = false) String dataSourceType) {
        PageInfo<TableDTO> tableDTOIPage = tableInfoService.getMetaTableAsset(pageNum, pageSize, tenantId, realName, cnName, dataSourceType);
        return AjaxResult.success(tableDTOIPage);
    }

    @GetMapping("/getMetaColumns")
    public AjaxResult<List<ColumnDTO>> getMetaColumns(@RequestParam(value = "metadataId") Integer metaTableId) {
        List<ColumnDTO> columns = tableInfoService.getMetaColumns(metaTableId);
        return AjaxResult.success(columns);
    }

    @ApiOperation("获取字段")
    @GetMapping("/getColumns")
    public AjaxResult<ExchangeColumnMapper> getColumns(@RequestParam(value = "metadataId") Integer metadataId,
                                                       @RequestParam(value = "targetSourceType") String targetSourceType,
                                                       @RequestParam(value = "createTarget") Boolean createTarget,
                                                       @RequestParam(value = "sourceId", required = false) Integer sourceId,
                                                       @RequestParam(value = "database", required = false) String database,
                                                       @RequestParam(value = "schema", required = false) String schema,
                                                       @RequestParam(value = "table", required = false) String table) {
        ExchangeColumnMapper columns = tableInfoService.getColumns(metadataId, targetSourceType, createTarget, sourceId, database, schema, table);
        return AjaxResult.success(columns);
    }

    @ApiOperation("获取ck集群")
    @GetMapping("/getCluster")
    public AjaxResult<List<String>> getCluster(@RequestParam("datasourceId") Integer datasourceId) {
        List<String> list = tableInfoService.getCluster(datasourceId);
        return AjaxResult.success(list);
    }

    /**
     * 只用于 数据同步 构造写字段
     */
    @GetMapping("/getWriteColumns")
    public AjaxResult<List<ColumnDTO>> getWriteColumns(@RequestParam(value = "metadataId") Integer metadataId,
                                                       @RequestParam(value = "dataSourceId", required = false) Integer dataSourceId) {
        return AjaxResult.success(tableInfoService.getWriteColumns(metadataId, dataSourceId));
    }

    @GetMapping("/getDatabase")
    public AjaxResult<List<String>> getDatabase(@RequestParam("sourceId") Integer sourceId) {
        List<String> list = tableInfoService.getDatabase(sourceId);
        return AjaxResult.success(list);
    }

    @GetMapping("/getSchema")
    public AjaxResult<List<String>> getSchema(@RequestParam("sourceId") Integer sourceId) {
        List<String> list = tableInfoService.getSchema(sourceId);
        return AjaxResult.success(list);
    }

    @GetMapping("/getTables")
    public AjaxResult<List<TableDTO>> getTables(@RequestParam("ip") String ip,
                                                @RequestParam("port") Integer port,
                                                @RequestParam(value = "databaseOrSchema") String databaseOrSchema,
                                                @RequestParam("tenantId") Long tenantId,
                                                @RequestParam(value = "tableName", required = false) String tableName) {
        List<TableDTO> list = tableInfoService.getTables(databaseOrSchema, ip, port, tenantId, tableName);
        return AjaxResult.success(list);
    }

    @GetMapping("/getOriginTables")
    public AjaxResult<List<String>> getOriginTables(@RequestParam(value = "sourceId") Integer sourceId,
                                                    @RequestParam(value = "dataBase", required = false) String dataBase,
                                                    @RequestParam(value = "schema", required = false) String schema) {
        List<String> tables = tableInfoService.getOriginTables(sourceId, dataBase, schema);
        return AjaxResult.success(tables);
    }


    // @PreAuthorize("@ss.hasPermi('system:table:create')")
    @PostMapping(value = "/{dirId}/ddl/create")
    //@ApiOperation(value = "创建表")
    public AjaxResult createTable(@PathVariable("dirId") Long dirId, @Validated @RequestBody TableCreateDto dto) {
        dto.setDirId(dirId);
        tableInfoService.createTable(dto);
        return AjaxResult.success();
    }


    // @PreAuthorize("@ss.hasPermi('system:table:create')")
    @PostMapping(value = "/{dirId}/ddl/create/sqlVerify")
    //@ApiOperation(value = "创建表验证sql")
    public AjaxResult sqlVerify(@PathVariable("dirId") Long dirId, @Validated @RequestBody TableCreateDto dto) {
        dto.setDirId(dirId);
        tableInfoService.createTableSqlVerify(dto);
        return AjaxResult.success();
    }


    // @PreAuthorize("@ss.hasPermi('system:table:copy')")
    @PostMapping(value = "/{dirId}/ddl/copy")
    //@ApiOperation(value = "复制表")
    public AjaxResult copyTable(@PathVariable("dirId") Long dirId, @Validated @RequestBody TableCopyDto dto) {
        dto.setDirId(dirId);
        String batchNumber = IdUtils.generateBusinessSerialNo();
        String username = SecurityUtils.getUsername();
        Long userId = SecurityUtils.getUserId();
        List<TableCopyInfoDto> tableCopyInfoDtoList = dto.getList().parallelStream().map(i -> {
            TableCopyInfoDto tableCopyInfoDto = new TableCopyInfoDto();
            tableCopyInfoDto.setDirId(dto.getDirId());
            tableCopyInfoDto.setFromTableId(i.getFromTableId());
            tableCopyInfoDto.setCopyData(i.isCopyData());
            tableCopyInfoDto.setToTableAlias(i.getToTableAlias());
            tableCopyInfoDto.setToTableName(i.getToTableName());
            tableCopyInfoDto.setToDirId(i.getToDirId());
            tableCopyInfoDto.setUserId(userId);
            tableCopyInfoDto.setUsername(username);
            return tableCopyInfoDto;
        }).collect(Collectors.toList());

        //库来源
        StringBuffer fromSchema = new StringBuffer();
        try {
            if (Objects.nonNull(dto.getDirId())) {
                fromSchema.append(DataSourceFactory.getDataConnection(dto.getDirId()).getSchema());
            }
            if (StringUtils.isEmpty(fromSchema.toString())) {
                fromSchema.append("未知来源库");
            }
        } catch (Exception e) {
            logger.error("未知来源库", e.getMessage());
            if (StringUtils.isEmpty(fromSchema.toString())) {
                fromSchema.append("未知来源库");
            }
        }
        //查看库是否有操作权限
        Set<Long> tableIds = tableCopyInfoDtoList.stream().map(TableCopyInfoDto::getFromTableId).collect(Collectors.toSet());
        LambdaQueryWrapper<SysUserTable> sutLqw = new LambdaQueryWrapper<>();
        sutLqw.eq(SysUserTable::getUserId, userId);
        sutLqw.in(SysUserTable::getTableId, tableIds);
        List<SysUserTable> sysUserTableSet = userTableService.list(sutLqw);
        Set<Long> haveTableIds = sysUserTableSet.stream().filter(i -> i.getAccessType() > 1).map(SysUserTable::getTableId).collect(Collectors.toSet());

        tableCopyInfoDtoList.stream().forEach(i -> {
            TableTask tableTask = new TableTask();
            TableLog tableLog = new TableLog();
            //查看是否存在相同表名
            List<TableInfo> cuTableInfos = tableInfoService.list(Wrappers.<TableInfo>query().eq("dir_id", i.getToDirId()).eq("table_name", i.getToTableName()));
            //获取需要拷贝的表
            TableInfo fromTableInfo = tableInfoService.getOne(Wrappers.<TableInfo>query().eq("id", i.getFromTableId()).eq("dir_id", i.getDirId()).eq("status", 1));

            //目标库
            StringBuffer toSchema = new StringBuffer();
            try {
                if (Objects.nonNull(i.getToDirId())) {
                    toSchema.append(DataSourceFactory.getDataConnection(i.getToDirId()).getSchema());
                }
                if (StringUtils.isEmpty(toSchema.toString())) {
                    toSchema.append("未知目标库");
                }
            } catch (Exception e) {
                logger.error("未知目标库", e.getMessage());
                if (StringUtils.isEmpty(toSchema.toString())) {
                    toSchema.append("未知目标库");
                }
            }
            if (!CollectionUtils.isEmpty(cuTableInfos)) {
                i.setSuccess(false);
                i.setMessage(String.format("表名%s已被使用|", i.getToTableName()));
            } else if (Objects.isNull(fromTableInfo)) {
                i.setSuccess(false);
                i.setMessage((StringUtils.isEmpty(i.getMessage()) ? "" : i.getMessage()) + "源表已被禁用/不存在|");
            } else if (Objects.equals("未知来源库", fromSchema.toString())) {
                i.setSuccess(false);
                i.setMessage((StringUtils.isEmpty(i.getMessage()) ? "" : i.getMessage()) + "未知来源库|");
            } else if (Objects.equals("未知目标库", toSchema.toString())) {
                i.setSuccess(false);
                i.setMessage((StringUtils.isEmpty(i.getMessage()) ? "" : i.getMessage()) + "目标库不存在|");
            } else if (CollectionUtils.isEmpty(haveTableIds) || !haveTableIds.contains(i.getFromTableId())) {
                i.setSuccess(false);
                i.setMessage((StringUtils.isEmpty(i.getMessage()) ? "" : i.getMessage()) + "无权限复制表[" + (Objects.isNull(fromTableInfo) ? "未知" : fromTableInfo.getTableName()) + "]权限|");
            } else {
                i.setSuccess(true);
                tableTask.setSpeedOfProgress(SpeedOfProgressType.RUNNING.getId());
            }
            if (!i.isSuccess()) {
                tableTask.setSpeedOfProgress(SpeedOfProgressType.ERROR.getId());
                tableTask.setRemarks("复制失败：" + i.getMessage());
            }
            tableTask.setContent("从 " + fromSchema.toString() + "库复制表" + (Objects.isNull(fromTableInfo) ? "未知" : fromTableInfo.getTableName()) + " 到 " + toSchema.toString() + "库" + i.getToTableName() + "表");
            tableTask.setTaskType(TaskType.COPY.getId());
            tableTask.setParams(JSON.toJSONString(i));
            tableTask.setDirId(i.getDirId());
            tableTask.setTableId(i.getFromTableId());
            tableTask.setOperator(username);
            tableTask.setOperatorId(userId);
            tableTask.setCreateTime(DateUtils.getNowDate());
            tableTask.setCompleteTime(DateUtils.getNowDate());
            tableTask.setBatchNumber(batchNumber);
            i.setTableTask(tableTask);

            String sql = String.format(i.isCopyData() ? copyDataSql : copyTableSql, i.getToTableName(), fromSchema, (Objects.isNull(fromTableInfo) ? "未知" : fromTableInfo.getTableName()));
            i.setSql(sql);
            tableLog.setDirId(i.getToDirId());
            tableLog.setTableName(i.getToTableName());
            tableLog.setTableAlias(StringUtils.isEmpty(i.getToTableAlias()) ? i.getToTableName() : i.getToTableAlias());
            tableLog.setTableId(0L);
            tableLog.setLogType(LogType.table_copy.getVal());
            tableLog.setCreateTime(new Date());
            tableLog.setUpdateCount(1);
            tableLog.setDataCount(0);
            tableLog.setOperatorId(userId);
            tableLog.setOperator(username);
            tableLog.setContent(sql);
            tableLog.setComment("从 " + fromSchema.toString() + "库复制表" + (Objects.isNull(fromTableInfo) ? "未知" : fromTableInfo.getTableName()) + " 到 " + toSchema.toString() + "库" + i.getToTableName() + "表");
            i.setTableLog(tableLog);

            if (Objects.nonNull(fromTableInfo)) {
                fromTableInfo.setId(null);
                fromTableInfo.setStatus(1);
                fromTableInfo.setDirId(i.getToDirId());
                fromTableInfo.setTableName(i.getToTableName());
                fromTableInfo.setTableAlias(StringUtils.isEmpty(i.getToTableAlias()) ? i.getToTableName() : i.getToTableAlias());
                fromTableInfo.setUpdateUserId(0L);
                fromTableInfo.setCreateUserId(userId);
                fromTableInfo.setCreateTime(new Date());
                fromTableInfo.setCopySql(sql);
                i.setTableInfo(fromTableInfo);
            }
        });

        tableCopyInfoDtoList.stream().forEach(i -> {
            iTableTaskService.save(i.getTableTask());
            if (i.isSuccess()) {
                try {
                    tableInfoService.copyTable(i);
                    LambdaUpdateWrapper<TableTask> luw = new LambdaUpdateWrapper<>();
                    luw.eq(TableTask::getId, i.getTableTask().getId());
                    luw.set(TableTask::getRemarks, "复制成功");
                    luw.set(TableTask::getCompleteTime, DateUtils.getNowDate());
                    luw.set(TableTask::getSpeedOfProgress, SpeedOfProgressType.SUCCESS.getId());
                    iTableTaskService.update(luw);
                } catch (Exception e) {
                    LambdaUpdateWrapper<TableTask> luw = new LambdaUpdateWrapper<>();
                    luw.eq(TableTask::getId, i.getTableTask().getId());
                    luw.set(TableTask::getRemarks, "复制失败:" + e.getMessage());
                    luw.set(TableTask::getCompleteTime, DateUtils.getNowDate());
                    luw.set(TableTask::getSpeedOfProgress, SpeedOfProgressType.ERROR.getId());
                    iTableTaskService.update(luw);
                } finally {
                    tableLogService.save(i.getTableLog());
                }
            } else {
                tableLogService.save(i.getTableLog());
            }
        });
        return AjaxResult.success(String.format("复制总数：%s 个，成功数量：%s 个", tableCopyInfoDtoList.size(), tableCopyInfoDtoList.stream().filter(i -> i.isSuccess() == true).count()));
    }


    @GetMapping(value = "/{tableId}/metadata/fields")
    //@ApiOperation(value = "表单详情-元数据信息-基础信息", response = TableFieldInfo.class)
    public AjaxResult getFieldInfo(@ApiParam(hidden = true) @PathVariable("tableId") Long id) {
        return AjaxResult.success(tableInfoService.getTableFieldMeta(id));
    }


    @PostMapping(value = "/{tableId}/metadata/fields")
    //@ApiOperation(value = "表单详情-新增表字段元数据信息")
    public AjaxResult addFieldInfo(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId, @Validated @RequestBody TableFieldInfoDto dto) {
        dto.setTableId(tableId);
        tableFieldInfoService.updateField(dto);
        return AjaxResult.success();
    }


    @PutMapping(value = "/{tableId}/metadata/fields")
    //@ApiOperation(value = "表单详情-修改表字段元数据信息")
    public AjaxResult updateFieldInfo(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId, @Validated @RequestBody TableFieldInfoDto dto) {
        dto.setTableId(tableId);
        tableFieldInfoService.updateField(dto);
        return AjaxResult.success();
    }


    @PutMapping(value = "/{tableId}/metadata/majorField")
    //@ApiOperation(value = "表单详情-设置表字段的重点字段")
    public AjaxResult updateMajorField(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId,
                                       @Validated @RequestBody TableFieldInfoDto dto) {
        dto.setTableId(tableId);
        tableFieldInfoService.updateMajorField(dto);
        return AjaxResult.success();
    }


    @GetMapping(value = "/{tableId}/metadata/relation")
    //@ApiOperation(value = "表单详情-元数据信息-关联关系", response = TableRelationDto.class)
    public AjaxResult getRelationInfo(@ApiParam(hidden = true) @PathVariable("tableId") Long id) {
        return AjaxResult.success(tableRelationService.getList(id, null));
    }


    @GetMapping(value = "/{dirId}/tableMap")
    //@ApiOperation(value = "地图目录-地图模式", response = TableMapDto.class)
    public AjaxResult getAllTableMap(@ApiParam(hidden = true) @PathVariable("dirId") Long dirId, @RequestParam(required = false) Long tableId, @RequestParam(value = "tableName", required = false) String tableName, @RequestParam(value = "fieldName", required = false) String fieldName, @RequestParam(value = "accessType", required = false) Integer accessType) {
        return AjaxResult.success(tableInfoService.getTableMap(dirId, tableId, tableName, fieldName, accessType));
    }


    @GetMapping(value = "/{tableId}/lineage")
    //@ApiOperation(value = "血缘关系", response = TableMapDto.class)
    public AjaxResult getTableLineage(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId, @ApiParam(value = "第几层") @RequestParam int level, @RequestParam(required = false) String fieldName) {
        return AjaxResult.success(tableInfoService.getTableLineage(tableId, level, fieldName));
    }


    @PutMapping(value = "/{tableId}/metadata/relation")
    //@ApiOperation(value = "表单详情-修改元数据信息-关联关系")
    public AjaxResult updateRelationInfo(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId, @Validated @RequestBody TableRelationUpdateDto dto) {

        dto.setTableId(tableId);
        tableRelationService.updateRelation(dto);
        return AjaxResult.success();
    }


    @DeleteMapping(value = "/{dirId}/metadata/relation/delete")
    //@ApiOperation(value = "删除关联表数据", response = TableRelationDto.class)
    public AjaxResult deleteRelationInfo(@ApiParam(value = "关联id") @RequestParam("relationId") Long relationId, @PathVariable(required = false) @ApiParam(value = "库目录id") String dirId) {
        if (tableRelationService.delete(relationId, dirId)) {

            return AjaxResult.success();
        }
        return AjaxResult.error();
    }


    @GetMapping("/{tableId}/export")
    //@ApiOperation(value = "导出表")
    public AjaxResult exportData(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId, @RequestParam(required = false) @ApiParam(value = "字段名") String fieldName, @RequestParam(required = false) @ApiParam(value = "条件") String condition, @RequestParam(required = false) @ApiParam(value = "逗号左边值") String leftVal, @RequestParam(required = false) @ApiParam(value = "逗号右边值") String rightVal
            , @RequestParam(required = false) @ApiParam(value = "排序类型,asc或者desc") String sortBy, @RequestParam(required = false) @ApiParam(value = "排序字段") String sortingField) {

        return AjaxResult.success(tableInfoService.exportTableData(tableId, fieldName, condition, leftVal, rightVal, sortBy, sortingField));
    }


    @GetMapping(value = "/{tableId}/data")
    //@ApiOperation(value = "表单详情-数据预览", response = TableDataDto.class)
    public AjaxResult getDataInfo(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId, @RequestParam(required = false) @ApiParam(value = "字段名") String fieldName, @RequestParam(required = false) @ApiParam(value = "条件") String condition, @RequestParam(required = false) @ApiParam(value = "逗号左边值") String leftVal, @RequestParam(required = false) @ApiParam(value = "逗号右边值") String rightVal, @RequestParam(required = false) @ApiParam(value = "选择显示字段") String fieldIds
            , @RequestParam(required = false) @ApiParam(value = "排序类型,asc或者desc") String sortBy, @RequestParam(required = false) @ApiParam(value = "排序字段") String sortingField) {

        return AjaxResult.success(tableInfoService.getTableData(tableId, fieldName, condition, leftVal, rightVal, fieldIds, sortBy, sortingField));
    }


    @PostMapping(value = "/{tableId}/dataInfo")
    //@ApiOperation(value = "表单详情-数据预览（复合筛选）", response = TableDataDto.class)
    public AjaxResult<TableDataDto> getDataInfo(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId,
                                                @Validated @RequestBody GetDataInfoRequestDTO requestDTO) {

        return AjaxResult.success(tableInfoService.getTableData(tableId, requestDTO));
    }


    @GetMapping(value = "/{tableId}/dataDaturability")
    //@ApiOperation(value = "表单详情饱和度", response = TableDataDto.class)
    public AjaxResult getDataSaturability(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId) {

        return AjaxResult.success(tableInfoService.getTableDataSaturability(tableId));
    }


    @DeleteMapping(value = "/{tableId}/data")
    //@ApiOperation(value = "表单详情-数据预览-删除数据")
    public AjaxResult deleteData(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId, @RequestBody Map<String, String> body) {
        tableInfoService.deleteData(tableId, body);
        return AjaxResult.success();
    }


    @PostMapping(value = "/{tableId}/data")
    //@ApiOperation(value = "表单详情-数据预览-新增数据")
    public AjaxResult addData(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId, @RequestBody Map<String, String> body) {
        tableInfoService.addData(tableId, body);
        return AjaxResult.success();
    }


    @PutMapping(value = "/{tableId}/data")
    //@ApiOperation(value = "表单详情-数据预览-修改数据")
    public AjaxResult updateData(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId, @RequestBody LinkedHashMap<String, String> body) {
        tableInfoService.updateData(tableId, body);
        return AjaxResult.success();
    }


    @PostMapping("/editDataTreeStatus")
    //@ApiOperation("修改数据目录状态")
    public AjaxResult editDataTreeStatus(@ApiParam(value = "tree") @RequestBody DataTreeDto tree) {
        return AjaxResult.success(tableInfoService.editTreeStatus(tree));
    }


    @GetMapping("/getDataTreeList")
    //@ApiOperation("获取数据目录列表")
    public AjaxResult getDataTreeList() {
        return AjaxResult.success(tableInfoService.getDataTreeList());
    }


    @GetMapping("/getDataTreeByTableId")
    //@ApiOperation("根据tableId获取表字段信息")
    public AjaxResult getDataTreeByTableId(@ApiParam(value = "表id(tableId)") String tableId) {
        return AjaxResult.success(tableInfoService.getDataTreeByTableId(tableId));
    }


    @PostMapping("/editDataFieldStatus")
    //@ApiOperation("修改数据目录表字段状态")
    public AjaxResult editDataFieldStatus(@ApiParam(value = "tree") @RequestBody DataTreeDto tree) {
        return AjaxResult.success(tableInfoService.editDataFieldStatus(tree));
    }


    /**
     * 字段映射列表
     *
     * @param tableId 表ID
     * @return 结果
     * @author linkaiwei
     * @date 2021-11-03 17:06:33
     * @since 1.6.1.0
     */

    @GetMapping(value = "/{tableId}/metadata/mapping")
    //@ApiOperation(value = "表单详情-元数据信息-字段映射列表", response = TableMappingDTO.class)
    public AjaxResult<List<TableMappingDTO>> listMapping(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId,
                                                         @ApiParam(value = "字段ID") @RequestParam(value = "fieldId", required = false) Long fieldId) {
        return AjaxResult.success(tableMappingService.listMapping(tableId, fieldId));
    }

    /**
     * 保存字段映射
     *
     * @param tableId    表ID
     * @param requestDTO 详情见 {@link SaveTableMappingRequestDTO}
     * @return 结果
     * @author linkaiwei
     * @date 2021-11-03 17:06:33
     * @since 1.6.1.0
     */

    @PutMapping(value = "/{tableId}/metadata/mapping")
    //@ApiOperation(value = "表单详情-元数据信息-保存字段映射")
    public AjaxResult<Boolean> saveMapping(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId,
                                           @Validated @RequestBody SaveTableMappingRequestDTO requestDTO) {
        requestDTO.setTableId(tableId);
        tableMappingService.saveMapping(requestDTO);
        return AjaxResult.success(true);
    }

    /**
     * 删除字段映射
     *
     * @param tableId   表ID
     * @param mappingId 映射字段信息ID
     * @return 结果
     * @author linkaiwei
     * @date 2021-11-03 17:06:33
     * @since 1.6.1.0
     */

    @DeleteMapping(value = "/{tableId}/metadata/mapping/{mappingId}")
    //@ApiOperation(value = "表单详情-元数据信息-删除字段映射")
    public AjaxResult<Boolean> deleteMapping(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId,
                                             @ApiParam(hidden = true) @PathVariable("mappingId") Long mappingId) {
        tableMappingService.deleteMapping(mappingId);
        return AjaxResult.success(true);
    }

    /**
     * 更新表字段排序
     *
     * @param tableId 表ID
     * @param sortDTO 字段排序列表
     * @return 结果
     * @author linkaiwei
     * @date 2021-11-09 10:27:54
     * @since 1.6.1.0
     */

    @PostMapping(value = "/{tableId}/data/updateFieldSort")
    //@ApiOperation(value = "更新表字段排序")
    public AjaxResult<Boolean> updateFieldSort(@ApiParam(hidden = true) @PathVariable("tableId") Long tableId,
                                               @Validated @RequestBody TableFieldSortDTO sortDTO) {
        return AjaxResult.success(tableFieldInfoService.updateFieldSort(tableId, sortDTO));
    }

    /**
     * 上传表的回传数据
     *
     * @param tableId 表ID
     * @param file    表的回传数据
     * @author linkaiwei
     * @date 2022-02-14 10:13:39
     * @since 1.6.4.0
     */
    //@ApiOperation(value = "上传表的回传数据")
    @PostMapping("/{tableId}/uploadReturnData")
    public AjaxResult<Boolean> uploadReturnData(@ApiParam("表ID") @PathVariable("tableId") Long tableId,
                                                @ApiParam("表的回传数据") @RequestParam("file") MultipartFile file) {
        return AjaxResult.success(tableInfoService.uploadReturnData(tableId, file));
    }

    /**
     * 分页查询回传数据
     *
     * @param tableId  表ID
     * @param pageNum  分页，页码
     * @param pageSize 分页，每页数量
     * @param type     搜索类型，null/0全部，1药监名称，2产品规格，3生产企业，4是否已经处理
     * @param content  搜索内容
     * @author linkaiwei
     * @date 2022-02-14 10:13:39
     * @since 1.6.4.0
     */
    //@ApiOperation(value = "分页查询回传数据")
    @GetMapping("/{tableId}/pageListReturnData")
    public AjaxResult<PageListReturnDataDTO> pageListReturnData(@ApiParam("表ID") @PathVariable("tableId") Long tableId,
                                                                @ApiParam("页码，默认1") @RequestParam(value = "pageNum", required = false) Integer pageNum,
                                                                @ApiParam("每页数量，默认10") @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                                                @ApiParam("搜索类型，null/0全部，1药监名称，2产品规格，3生产企业，4是否已经处理") @RequestParam(value = "type", required = false) Integer type,
                                                                @ApiParam("搜索内容") @RequestParam(value = "content", required = false) String content) {
        return AjaxResult.success(tableInfoService.pageListReturnData(pageNum, pageSize, type, content));
    }

    /**
     * 删除回传数据
     *
     * @param tableId     表ID
     * @param increasCode 自增code
     * @author linkaiwei
     * @date 2022-02-14 10:13:39
     * @since 1.6.4.0
     */
    //@ApiOperation(value = "删除回传数据")
    @DeleteMapping("/{tableId}/deleteReturnData")
    public AjaxResult<Boolean> deleteReturnData(@ApiParam("表ID") @PathVariable("tableId") Long tableId,
                                                @ApiParam("自增code") @RequestParam("increasCode") String increasCode) {
        return AjaxResult.success(tableInfoService.deleteReturnData(increasCode));
    }

    /**
     * 更新回传数据
     *
     * @param tableId    表ID
     * @param requestDTO 更新回传数据信息
     * @author linkaiwei
     * @date 2022-02-14 10:13:39
     * @since 1.6.4.0
     */
    //@ApiOperation(value = "更新回传数据")
    @PutMapping("/{tableId}/updateReturnData")
    public AjaxResult<Boolean> updateReturnData(@ApiParam("表ID") @PathVariable("tableId") Long tableId,
                                                @ApiParam("更新回传数据信息") @RequestBody UpdateReturnDataRequestDTO requestDTO) {
        return AjaxResult.success(tableInfoService.updateReturnData(requestDTO));
    }


    @ApiOperation(value = "数据库表状态启用禁用", notes = "数据库表", httpMethod = "PUT")
    @PutMapping("/status/{tableId}/update")
    public Object updateStatus(@PathVariable("tableId") Long tableId) {
        return tableInfoService.updateStatus(tableId);
    }

    /**
     * 表单管理 分页
     */
    @PostMapping("/pageQueryTable")
    public AjaxResult<IPage<TablePageVO>> pageQueryTable(@RequestBody TablePageQueryRequest request) {
        return tableInfoService.pageQueryTable(request);
    }

    /**
     * 底表出数管理
     */
    @PostMapping("/snapshot/pageQuery")
    public AjaxResult<IPage<TableInfoSnapshotPageVO>> pageQuery(@RequestBody TableSnapshotPageRequest request) {
        return tableInfoSnapshotService.pageQuery(request);
    }

    @PostMapping("/snapshot/pageAssets")
    public AjaxResult<List<FlowAssetsPageDTO>> pageQuery(@RequestBody TablePushDetailPageRequest request) {
        return tableInfoSnapshotService.pageQueryAssets(request);
    }

    @GetMapping("/snapshot/delete")
    public AjaxResult<Void> deleteSnapshot(@RequestParam("id") Long id) {
        return tableInfoSnapshotService.deleteSnapshot(id);
    }

    @PostMapping("/snapshot/edit")
    public AjaxResult<Void> editSnapshot(@RequestBody @Validated TableSnapshotUpdateRequest request) {
        return tableInfoSnapshotService.edit(request);
    }

    /**
     * 推送 底表 出数
     */
    @PostMapping("/snapshot/push")
    public AjaxResult<Void> pushTable(@RequestBody @Validated TableSnapshotPushRequest request) {
        return tableInfoSnapshotService.manualPushTable(request);
    }

    @GetMapping("/snapshot/queryInfo")
    public AjaxResult<TableSnapInfoVO> queryTablePlanInfo(@RequestParam("tableId") Long tableId) {
        return tableInfoSnapshotService.queryTablePlanInfo(tableId);
    }

    @RepeatSubmit
    @PostMapping("/snapshot/planPush")
    public AjaxResult<Void> planPush(@RequestBody @Validated TableSnapshotPushRequest request) {
        return tableInfoSnapshotService.planPush(request);
    }

    @RepeatSubmit
    @GetMapping("/snapshot/cancelPlanPush")
    AjaxResult<Void> cancelPlanPush(@RequestParam("planId") Long planId) {
        return tableInfoSnapshotService.cancelPlanPush(planId);
    }

    /**
     * 底表比对
     *
     * @param request 表单信息
     */
    @ApiOperation(value = "发起底表比对")
    @PostMapping("/snapshot/calculateDiff")
    public AjaxResult<Void> calculateDiff(@RequestBody @Validated TableDiffRequest request) {
        return tableInfoSnapshotService.calculateDiff(request);
    }

    /**
     * 底表比对分页结果
     *
     * @param request 参数信息
     * @return 库表比对信息
     */
    @ApiOperation(value = "底表比对分页结果")
    @PostMapping("/snapshot/diffPage")
    public AjaxResult<IPage<TableInfoCompareTaskVO>> diffPage(@RequestBody @Validated TableDiffPageRequest request) {
        return tableInfoSnapshotService.diffPage(request);
    }

    /**
     * 删除底表比对结果
     *
     * @param taskId 任务编号
     * @return 是否成功
     */
    @ApiOperation(value = "删除底表比对结果")
    @DeleteMapping("/snapshot/deleteDiff/{taskId}")
    public AjaxResult<Void> deleteDiff(@PathVariable(value = "taskId") Long taskId) {
        return tableInfoSnapshotService.deleteDiff(taskId);
    }

    /**
     * 作废底表比对结果
     *
     * @param taskId 任务编号
     * @return 是否成功
     */
    @ApiOperation(value = "作废底表比对结果")
    @GetMapping("/snapshot/failDiff")
    public AjaxResult<Void> failDiff(@RequestParam(value = "taskId") Long taskId) {
        return tableInfoSnapshotService.failDiff(taskId);
    }

    /**
     * 底表比对计划新增或更新
     *
     * @param request 请求参数
     * @return 是否成功
     */
    @ApiOperation(value = "底表比对计划新增或更新")
    @PostMapping("/snapshot/comparePlan")
    public AjaxResult<Void> compareCreateOrUpdate(@RequestBody @Validated TableDiffPlanCreateOrUpdateRequest request) {
        return tableInfoSnapshotService.compareCreateOrUpdate(request);
    }

    /**
     * 删除底表比对计划结果
     *
     * @param planId 任务编号
     * @return 是否成功
     */
    @ApiOperation(value = "删除底表比对计划")
    @DeleteMapping("/snapshot/comparePlan/{planId}")
    public AjaxResult<Void> deleteComparePlan(@PathVariable(value = "planId") Long planId) {
        return tableInfoSnapshotService.deleteComparePlan(planId);
    }

    /**
     * 底表比对计划详情
     *
     * @param tableId 表编号
     * @return 底表比对计划详情
     */
    @ApiOperation(value = "底表比对计划详情")
    @GetMapping("/snapshot/comparePlan/{tableId}")
    public AjaxResult<TableComparePlanVO> comparePlanDetail(@PathVariable(value = "tableId") Long tableId) {
        return tableInfoSnapshotService.comparePlanDetail(tableId);
    }
}
