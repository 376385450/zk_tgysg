package com.sinohealth.system.mapper;

import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-05-27 10:39
 */
@Mapper
@DataSource(DataSourceType.POWER_BI_PG)
public interface PowerBIMapper {

    @Delete("${sql}")
    void deleteData(@Param("sql") String sql);
}
