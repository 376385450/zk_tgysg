package com.sinohealth.system.service;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.DbType;
import com.sinohealth.system.dto.BaseDataSourceParamDto;

public interface IntegrateDataSourceService {

    AjaxResult createDataSource(BaseDataSourceParamDto baseDataSourceParamDto);

    AjaxResult queryDataSource(int id);

    AjaxResult queryDataSourceListPaging(String searchVal, Integer pageNo, Integer pageSize);


    AjaxResult verifyDataSourceName(String name);

    AjaxResult checkConnection(BaseDataSourceParamDto baseDataSourceParamDto);

    AjaxResult connectionTest(int id);

    AjaxResult delete(int datasourceId);

    AjaxResult queryDataSourceList(DbType type);

}

