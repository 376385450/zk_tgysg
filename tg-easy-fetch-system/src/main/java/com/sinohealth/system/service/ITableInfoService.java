package com.sinohealth.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.data.common.response.PageInfo;
import com.sinohealth.data.intelligence.api.metadataRegister.dto.ColumnDTO;
import com.sinohealth.system.biz.dir.vo.TablePageVO;
import com.sinohealth.system.biz.table.dto.TablePageQueryRequest;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TgMetadataInfo;
import com.sinohealth.system.dto.BigTableDto;
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
import com.sinohealth.system.dto.TableInfoDto;
import com.sinohealth.system.dto.TableMapDto;
import com.sinohealth.system.dto.TableStatisticDto;
import com.sinohealth.system.dto.UpdateReturnDataRequestDTO;
import com.sinohealth.system.dto.table_manage.DataManageFormDto;
import com.sinohealth.system.dto.table_manage.TableInfoManageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 【请填写功能名称】Service接口
 *
 * @author jingjun
 * @date 2021-04-20
 */
public interface ITableInfoService extends IService<TableInfo> {

    void refreshTableCount(Long dirId, Long tableId, String tableName, String cachekey);

    IPage<TableInfo> getList(Long dirId, String tableName, String fieldName, Integer accessType, boolean isFilter, Integer pageNum, Integer pageSize);

    TableInfoManageDto getDetail(Long tableId);

    List<DataManageFormDto> getListByDirId(Long dirid);

    List<DataManageFormDto> getListByDirId(Long dirid, Integer menu);

    List<String> getOriginTables(Integer sourceId, String dataBase, String schema);

    PageInfo<TableDTO> getMetaTableAsset(Integer pageNum, Integer pageSize, Long tenantId, String realName, String cnName, String dataSourceType);

    List<TableInfo> findPage(String tableName, Integer pageNum, Integer pageSize);

    List<TableInfo> findByTableName(String tableName);

    List<TableInfo> getMyTableList(Long dirId, String tableName, Integer safeLevel, Boolean concern, Set<Long> dirIdSet, Set<Long> extDirIdSet);

    TableInfoDto getTableBaseInfo(Long tableId);

    List<ColumnDTO> getMetaColumns(Integer metaTableId);

    List<String> getEnableDataSourceType();

    ExchangeColumnMapper getColumns(Integer metaTableId, String targetSourceType, Boolean createTarget, Integer sourceId, String database, String schema, String table);

    List<ColumnDTO> getWriteColumns(Integer metaTableId, Integer writerTableId);

    List<DataSourceDTO> getDatasource(Long tenantId, String dataSourceType);

    List<String> getDatabase(Integer sourceId);

    List<String> getCluster(Integer datasourceId);

    List<String> getSchema(Integer sourceId);

    List<TableDTO> getTables(String databaseOrSchema, String ip, Integer port, Long tenantId, String tableName);

    List<TableFieldInfo> getTableFieldMeta(Long tableId);

    List<TableMapDto> getTableMap(Long dirId, Long tableId, String tableName, String fieldName, Integer accessType);

    List<TableMapDto> getTableLineage(Long tableId, int level, String fieldName);

    TableDataDto getTableData(Long tableId, String fieldName, String condition, String leftVal, String rightVal, String fieldIds, String sortBy, String sortingField);

    TableDataDto getTableData(Long tableId, GetDataInfoRequestDTO requestDTO);

    String exportTableData(Long tableId, String fieldName, String condition, String leftVal, String rightVal, String sortBy, String sortingField);


    void createTable(TableCreateDto dto);

    String createTableSqlVerify(TableCreateDto dto);

    String copyTable(TableCopyDto dto);

    void updateTableInfo(TableInfoDto dto);

    void addData(Long tableId, Map<String, String> body);

    void deleteData(Long tableId, Map<String, String> body);


    void updateQueryTime(Long tableId, int addTimes, int addTotalTimes);

    List<BigTableDto> getBigTableTop20(List<Long> dirids);

    void updateData(Long tableId, LinkedHashMap<String, String> body);

    TableStatisticDto getTableStatisticDto(Long dirid);

    String editTreeStatus(DataTreeDto tree);

    DataTreeDto getDataTreeList();

    DataTreeDto getDataTreeByTableId(String tableId);

    String editDataFieldStatus(DataTreeDto tree);

    List<TableInfo> findByFieldName(String fieldName);

    List<TableInfo> findByFieldNames(String fieldName);


    List<TableDataDto.Header> getTableDataSaturability(Long tableId);

    List<TableInfoDto> getTableBaseInfoIds(List<Long> ids);

    void copyTable(TableCopyInfoDto i);

    Boolean uploadReturnData(Long tableId, MultipartFile file);

    PageListReturnDataDTO pageListReturnData(Integer pageNum, Integer pageSize, Integer type, String content);

    Boolean deleteReturnData(String increasCode);

    Boolean updateReturnData(UpdateReturnDataRequestDTO requestDTO);

    void updateTableInfo(TableInfoManageDto dto);


    void updateDirIdOfTableInfo(TableInfoDto tableInfoDto);

    List<TableInfo> findAllNotDiyAndSelfDiy();

    List<TableInfo> findAllDiy(Long userId);

    List<TableInfo> findAllNotDiy();

    TableInfo selectTableInfoByTableName(String tn);

    List<DataManageFormDto> getTablesWithoutDir(Integer menu);

    Object updateStatus(Long tableId);

    List<TableInfo> listAllAssetsTable(String bizType);

    List<TableInfo> queryByTableIds(List<Long> tableIds);

    List<TgMetadataInfo> queryMetaDataByAssetIds(List<Long> assetIds);

    AjaxResult<IPage<TablePageVO>> pageQueryTable(TablePageQueryRequest request);

    List<TableInfo> getUnLinkedData(List<Long> tableAssetIds);
}
