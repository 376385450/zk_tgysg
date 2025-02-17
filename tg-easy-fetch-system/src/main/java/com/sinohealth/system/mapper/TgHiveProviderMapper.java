package com.sinohealth.system.mapper;

import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.common.utils.HiveUtils;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;

@Mapper
@Repository
@DataSource(DataSourceType.HIVE)
public interface TgHiveProviderMapper {
    /**
     * 建表
     * 可以配合HiveUtils使用
     *
     * @param sql 建表语句
     * @see HiveUtils
     */
    @UpdateProvider(type = TgHiveProvider.class, method = "runSql")
    void createTableAccordingApplication(String sql);

    @SelectProvider(type = TgHiveProvider.class, method = "runSql")
    List<String> showCreateTable(String sql);

    /**
     * @see TgHiveProvider#countTable(String)
     */
    @SelectProvider(type = TgHiveProvider.class, method = "countTable")
    Long countTable(String tableName);

    /**
     * 统计
     *
     * @param sql 统计sql
     * @return 统计数量
     */
    @SelectProvider(type = TgHiveProvider.class, method = "runSql")
    Long count(String sql);

    /**
     * @see TgHiveProvider#selectApplicationDataFromHive
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @SelectProvider(type = TgHiveProvider.class, method = "selectApplicationDataFromHive")
    List<LinkedHashMap<String, Object>> selectApplicationDataFromHive(String sql, String whereSql, GetDataInfoRequestDTO requestDTO);

    /**
     * @see TgHiveProvider#selectAllDataFromHive
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @SelectProvider(type = TgHiveProvider.class, method = "selectAllDataFromHive")
    List<LinkedHashMap<String, Object>> selectAllDataFromHive(String tableName);

}
