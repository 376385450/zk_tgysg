package com.sinohealth.system.mapper;

import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.dto.table_manage.DataRangeQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-10-31 20:04
 */
@Mapper
@Repository
@DataSource(DataSourceType.CUSTOMER_CK)
public interface TgCkCustomerProviderMapper {

    @UpdateProvider(type = TgCkProvider.class, method = "runSql")
    void createTableAccordingApplication(String sql);


    /**
     * @see TgCkProvider#countTable(String)
     */
    @SelectProvider(type = TgCkProvider.class, method = "countTable")
    Integer countTable(String tableName);

    /**
     * @see TgCkProvider#selectDataRangeFromCk
     */
    @SelectProvider(type = TgCkProvider.class, method = "selectDataRangeFromCk")
    List<String> selectDataRangeFromCk(String colName, String tableName, DataRangeQueryDto dataRangeQueryDto);

    /**
     * @see TgCkProvider#selectDataRangeCountFromCk
     */
    @SelectProvider(type = TgCkProvider.class, method = "selectDataRangeCountFromCk")
    int selectDataRangeCountFromCk(String colName, String tableName, DataRangeQueryDto dataRangeQueryDto);

}
