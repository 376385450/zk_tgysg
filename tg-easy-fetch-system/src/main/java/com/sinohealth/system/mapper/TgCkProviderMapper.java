package com.sinohealth.system.mapper;

import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.application.entity.TableUpdateTimeEntity;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.table_manage.DataRangeQueryDto;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@Mapper
@Repository
@DataSource(DataSourceType.SLAVE)
public interface TgCkProviderMapper {

    /**
     * 真实表字段查询
     *
     * @see TgCkProvider#selectDataRangeFromCk
     */
    @SelectProvider(type = TgCkProvider.class, method = "selectDataRangeFromCk")
    List<String> selectDataRangeFromCk(String colName, String tableName, DataRangeQueryDto dataRangeQueryDto);

    /**
     * 真实表字段查询 计数
     *
     * @see TgCkProvider#selectDataRangeCountFromCk
     */
    @SelectProvider(type = TgCkProvider.class, method = "selectDataRangeCountFromCk")
    int countDataRangeFromCk(String colName, String tableName, DataRangeQueryDto dataRangeQueryDto);

    /**
     * 字段库 查询
     *
     * @see TgCkProvider#selectFieldDataRangeFromCk
     */
    @SelectProvider(type = TgCkProvider.class, method = "selectFieldDataRangeFromCk")
    List<String> selectFieldDataRangeFromCk(String colName, String tableName, DataRangeQueryDto dataRangeQueryDto);

    /**
     * 字段库 计数
     *
     * @see TgCkProvider#countFieldDataRangeFromCk
     */
    @SelectProvider(type = TgCkProvider.class, method = "countFieldDataRangeFromCk")
    int countFieldDataRangeFromCk(String colName, String tableName, DataRangeQueryDto dataRangeQueryDto);

    /**
     * @see TgCkProvider#runSql
     */
    @SelectProvider(type = TgCkProvider.class, method = "runSql")
    List<String> selectCascadeDataRangeFromCk(String sql);

    /**
     * @see TgCkProvider#selectApplicationDataFromCk
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @SelectProvider(type = TgCkProvider.class, method = "selectApplicationDataFromCk")
    List<LinkedHashMap<String, Object>> selectApplicationDataFromCk(String sql, String whereSql, GetDataInfoRequestDTO requestDTO);

    @SelectProvider(type = TgCkProvider.class, method = "runSql")
    List<LinkedHashMap<String, Object>> selectAllDataFromCk(String sql);

    /**
     * 关掉事务 避免数据源持续使用MySQL
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @SelectProvider(type = TgCkProvider.class, method = "runSql")
    Long countAllDataFromCk(String sql);

    /**
     * @param whereSql 不能为null mybatis 参数匹配绑定会报NPE
     * @see TgCkProvider#selectCountApplicationDataFromCk
     */
    @Deprecated
    @SelectProvider(type = TgCkProvider.class, method = "selectCountApplicationDataFromCk")
    Long selectCountApplicationDataFromCk(String sql, String whereSql);

    /**
     * 开新事务 隔离 避免数据源持续使用MySQL
     *
     * @see TgCkProvider#countFromCk
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @SelectProvider(type = TgCkProvider.class, method = "countFromCk")
    Long countFromCk(String sql);

    /**
     * @see com.sinohealth.system.biz.ck.adapter.CKClusterAdapter#executeAll
     */
    @UpdateProvider(type = TgCkProvider.class, method = "runSql")
    void createTableAccordingApplication(String sql);

    /**
     * @see com.sinohealth.system.biz.ck.adapter.CKClusterAdapter#executeAll
     */
    @InsertProvider(type = TgCkProvider.class, method = "runSql")
    void insert(String sql);

    @SelectProvider(type = TgCkProvider.class, method = "runSql")
    String showCreateTable(String sql);

    @UpdateProvider(type = TgCkProvider.class, method = "runSql")
    void syncToRemoteTable(String sql);


    @UpdateProvider(type = TgCkProvider.class, method = "runSql")
    void selectCkDataInsertPg(String sql);

    @SelectProvider(type = TgCkProvider.class, method = "selectTablesThatNeed2Update")
    List<String> selectTablesThatNeed2Update();

    @InsertProvider(type = TgCkProvider.class, method = "removeNeed2UpdateMessage")
    void removeNeed2UpdateMessage(String tableName);

    @InsertProvider(type = TgCkProvider.class, method = "insertUpdateSuccessMessage")
    void insertUpdateSuccessMessage(String tableName);

    @UpdateProvider(type = TgCkProvider.class, method = "runSql")
    void dropSyncTable(String sql);

    @UpdateProvider(type = TgCkProvider.class, method = "runSql")
    void updateTable(String sql);

    /**
     * @see TgCkProvider#countTable(String)
     */
    @SelectProvider(type = TgCkProvider.class, method = "countTable")
    Long countTable(String tableName);

    /**
     * @see TgCkProvider#selectLastUpdateTime(String)
     */
    @SelectProvider(type = TgCkProvider.class, method = "selectLastUpdateTime")
    Date selectLastSuccessTime(String tableName);

    /**
     * @see TgCkProvider#selectMaxTime
     */
    @SelectProvider(type = TgCkProvider.class, method = "selectMaxTime")
    List<TableUpdateTimeEntity> selectMaxTime(List<String> tables);
}
