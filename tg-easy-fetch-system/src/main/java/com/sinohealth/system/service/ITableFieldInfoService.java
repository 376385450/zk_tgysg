package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.dto.TableFieldInfoDto;
import com.sinohealth.system.dto.TableFieldSortDTO;

import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author dataplatform
 * @date 2021-04-24
 */
public interface ITableFieldInfoService extends IService<TableFieldInfo> {

    public void updateField(TableFieldInfoDto dto);

    Boolean updateFieldSort(Long tableId, TableFieldSortDTO sortDTO);

    void updateMajorField(TableFieldInfoDto dto);

    List<TableFieldInfo> findListByIds(List<Long> ids);

    int getCountByTableId(Long tableId, String fieldName);

    List<TableFieldInfo> findListByFieldIds(List<Long> ids);

    List<String> getFieldsByTableName(String tableName);
    List<TableFieldInfo> getFieldsByTableId(Long tableId);

    void deleteByTableId(Long tableId);
}
