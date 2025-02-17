package com.sinohealth.system.mapper;

import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 替换为操作CK
 */
//@Mapper
//@Repository
//@DataSource(DataSourceType.POSTGRESQL)
//@Deprecated
public interface TgPgProviderMapper {
    @Deprecated
    @SelectProvider(type = TgPgProvider.class, method = "selectTableApplicationMapping")
    List<Map<String, Object>> selectTableApplicationMapping(Long applyId);

    @Deprecated
    @UpdateProvider(type = TgPgProvider.class, method = "runSql")
    void createTableAccordingApplication(String sql);

    @Deprecated
    @UpdateProvider(type = TgPgProvider.class, method = "runSql")
    void updateTableApplicationMapping(String sql);

    @Deprecated
    @UpdateProvider(type = TgPgProvider.class, method = "createCustomerApplyAuthTableIfNotExist")
    void createCustomerApplyAuthTableIfNotExist();

    @UpdateProvider(type = TgPgProvider.class, method = "updateCustomerApplyAuthStatus")
    boolean updateCustomerApplyAuthStatus(Long id, Integer status);

    @SelectProvider(type = TgPgProvider.class, method = "selectCustomerApplyAuthStatus")
    int selectCustomerApplyAuthStatus(Long id);

    @Deprecated
    @SelectProvider(type = TgPgProvider.class, method = "queryReportForm4Customer")
    List<Map<String, Object>> queryReportForm4Customer(Long userId);

    @Deprecated
    @SelectProvider(type = TgPgProvider.class, method = "queryTableName")
    String queryTableName(Long userId, Long applyId);

    @Deprecated
    @SelectProvider(type = TgPgProvider.class, method = "queryTableSize")
    Integer queryTableSize(String tableName);

    @Deprecated
    @SelectProvider(type = TgPgProvider.class, method = "queryTableHeaders")
    List<Object> queryTableHeaders(String tableName);

    @Deprecated
    @SelectProvider(type = TgPgProvider.class, method = "queryTableHeadersAndType")
    List<Map<String, Object>> queryTableHeadersAndType(String tableName);

    /**
     * 数据全量下载
     */
    @Deprecated
    @SelectProvider(type = TgPgProvider.class, method = "queryTableData")
    List<LinkedHashMap<String, Object>> queryTableData(String tableName, Integer querySize, Integer offset);

    /**
     * 筛选数据预览和下载
     */
    @Deprecated
    @SelectProvider(type = TgPgProvider.class, method = "queryTableData4Checking")
    List<LinkedHashMap<String, Object>> queryTableData4Checking(String tableName, String whereSql, Integer querySize, Integer offset, String sortBy, String sortField);

    @Deprecated
    @SelectProvider(type = TgPgProvider.class, method = "queryTableSize4Checking")
    Integer queryTableSize4Checking(String tableName, String whereSql);

    @Deprecated
    @UpdateProvider(type = TgPgProvider.class, method = "runSql")
    void dropUselessTable(String sql);
}
