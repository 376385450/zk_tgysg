package com.sinohealth.system.service;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.TableDataDto;
import com.sinohealth.system.dto.TableInfoDto;
import com.sinohealth.system.dto.table_manage.DataManageDto;

import java.sql.SQLException;
import java.util.List;

/**
 * 数据管理Service接口
 */
public interface DataManageService {

    DataManageDto getTree(String name, Integer menu);

    List<Long> getDirList();

    TableDataDto getTableData(Long tableId, GetDataInfoRequestDTO requestDTO);

    TableDataDto getCmhTableData(GetDataInfoRequestDTO requestDTO);

    TableInfoDto getTableBaseInfo(Long tableId);

    void syncTableInfo();

    /**
     * 创建CK底表
     * <p>
     * TODO 优化，简化使用门槛，支持从别的库快速转为CK的DDL语句
     */
    AjaxResult createTable(String localSql, String tableName, Boolean dropTableBeforeCreate);

    DataManageDto getAllTree();

    AjaxResult dropTable(String tableName) throws SQLException;

    void updateStatus(String tableName);
}
