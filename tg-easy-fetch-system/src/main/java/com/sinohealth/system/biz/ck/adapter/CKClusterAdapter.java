package com.sinohealth.system.biz.ck.adapter;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.alibaba.excel.util.BooleanUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sinohealth.bi.enums.DatabaseEnum;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.config.DataConnection;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.framework.config.properties.DruidProperties;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.ck.constant.CkClusterType;
import com.sinohealth.system.biz.ck.constant.CkErrorMsg;
import com.sinohealth.system.biz.ck.constant.CkTableSuffixTable;
import com.sinohealth.system.biz.ck.constant.SnapshotTableHdfsStateEnum;
import com.sinohealth.system.biz.ck.constant.SnapshotTableStateEnum;
import com.sinohealth.system.biz.ck.dao.SnapshotTableMappingDAO;
import com.sinohealth.system.biz.ck.domain.SnapshotTableMapping;
import com.sinohealth.system.biz.ck.domain.SnapshotTableMappingDetail;
import com.sinohealth.system.biz.ck.dto.CkDataSource;
import com.sinohealth.system.biz.ck.mapper.SnapshotTableMappingDetailMapper;
import com.sinohealth.system.biz.common.RedisLock;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.ckpg.SelfCKProperties;
import com.sinohealth.system.dto.ApplicationDataDto;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.assets.AuthTableFieldDTO;
import com.sinohealth.system.dto.table_manage.DataRangeQueryDto;
import com.sinohealth.system.mapper.DataClickhouseMapper;
import com.sinohealth.system.mapper.OutsideClickhouseMapper;
import com.sinohealth.system.mapper.TgCkProvider;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.util.ApplicationSqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.ResultSetMetaData;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-12-01 14:12
 */
@Slf4j
@Service
public class CKClusterAdapter {

    /**
     * 创建表后 延迟备份的时延
     */
    public static final int BACK_DELAY_MIN = 60;
    public static final String HDFS_PREFIX = "ext_hdfs_";

    /**
     * CK数据节点连接
     */
    private final Map<String, CkDataSource> dsMap = new ConcurrentHashMap<>();

    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER_CK)
    private ScheduledExecutorService scheduler;

//    @Resource
//    @Qualifier(ThreadPoolType.ENHANCED_TTL)
//    private Executor ttl;

    @Resource
    @Qualifier(ThreadPoolType.MINI_CK)
    private ThreadPoolTaskExecutor miniCkPool;
    @Autowired
    private RedisLock redisLock;


    @Resource(name = "slaveDataSource")
    private DruidDataSource slaveDataSource;
    @Resource
    private SelfCKProperties selfCKProperties;
    @Autowired
    private AppProperties appProperties;
    @Autowired
    private DruidProperties druidProperties;


    // DAO
    @Autowired
    private DataClickhouseMapper dataClickhouseMapper;
    @Autowired
    private SnapshotTableMappingDetailMapper snapshotTableMappingDetailMapper;
    @Autowired
    private SnapshotTableMappingDAO snapshotTableMappingDAO;
    @Autowired
    private TgCkProviderMapper ckProviderMapper;

    @Autowired
    private AlertService alertService;


    @PostConstruct
    public void init() {
        log.info("start init ck ds");

        try {
            DataConnection dataConnection = this.getConn();
            String cluster = "select host_name,host_address from system.clusters where cluster = '" + CkClusterType.DEFAULT + "'";
            List<Map<String, Object>> nodes = dataConnection.getJdbcOperations().queryForList(cluster);

            for (Map<String, Object> node : nodes) {
                Object hostName = node.get("host_name");
                Object hostAddress = node.get("host_address");
                DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
                dataSource.setEnable(true);
                dataSource.setMaxActive(10);
                dataSource.setDbType(slaveDataSource.getDbType());
                dataSource.setDriverClassName(slaveDataSource.getDriverClassName());
                // 替换为单节点IP
                dataSource.setUrl(StringUtils.replaceHost(slaveDataSource.getUrl(), hostAddress.toString()));
                dataSource.setUsername(slaveDataSource.getUsername());
                dataSource.setPassword(slaveDataSource.getPassword());
                druidProperties.dataSource(dataSource);
                dataSource.setValidationQuery("SELECT 6");
//                Properties properties = new Properties();
//                properties.put("socket_timeout", 8000000);
//                dataSource.setConnectProperties(properties);


                DataConnection conn = new DataConnection(selfCKProperties.getDatabase(), dataSource, new JdbcTemplate(dataSource),
                        DatabaseEnum.CLICKHOUSE.getFeature());
                dsMap.put(hostName.toString(), new CkDataSource(hostName.toString(), hostAddress.toString(), dataSource, conn));
            }
            log.info("init ck success: {}", nodes);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 选择存储量最小的CK节点
     *
     * @param exclude 排除节点
     */
    public Optional<CkDataSource> determineDataSource(Collection<String> exclude) {
        if (MapUtils.isEmpty(dsMap)) {
            return Optional.empty();
        }
        try {
            DataConnection dataConnection = this.getConn();
            String cluster = "SELECT hostName() as host,sum(data_compressed_bytes) AS usage" +
                    " FROM clusterAllReplicas('" + CkClusterType.DEFAULT + "',`system`.parts)" +
                    " WHERE database='" + selfCKProperties.getDatabase() + "'";
            if (CollectionUtils.isNotEmpty(exclude)) {
                String hosts = exclude.stream().map(v -> "'" + v + "'").collect(Collectors.joining(","));
                cluster += " AND hostName() not in (" + hosts + ")";
            }
            cluster += " GROUP BY host";
            List<Map<String, Object>> nodes = dataConnection.getJdbcOperations().queryForList(cluster);
            if (CollectionUtils.isEmpty(nodes)) {
                return Optional.empty();
            }

            Optional<Pair<String, Long>> minOpt = nodes.stream().map(v -> {
                String host = Optional.ofNullable(v.get("host")).map(Object::toString).orElse("");
                Long useAge = Optional.ofNullable(v.get("usage")).map(Object::toString).map(u -> {
                    try {
                        return Long.valueOf(u);
                    } catch (Exception e) {
                        return 0L;
                    }
                }).orElse(0L);
                return Pair.of(host, useAge);
            }).min(Comparator.comparing(Pair::getSecond));

            return minOpt.map(Pair::getFirst).map(dsMap::get);
        } catch (Exception e) {
            log.error("", e);
        }

        return Optional.empty();
    }

    /**
     * @param sql 分布式SQL
     */
    public void executeAll(String sql) {
        sql = sql.replaceAll("(?i)ON (?i)cluster \\w+", " ");
        for (CkDataSource ds : dsMap.values()) {
            ds.getConn().getJdbcOperations().execute(sql);
        }
    }

    public void execute(String sql) {
        this.execute(dsMap.values().iterator().next(), sql);
    }

    public void executeHost(String host, String sql) {
        CkDataSource ds = dsMap.get(host);
        if (Objects.isNull(ds)) {
            throw new CustomException("数据源异常");
        }

        this.execute(ds, sql);
    }

    public String showCreateTable(String host, String sql) {
        CkDataSource ds = dsMap.get(host);
        if (Objects.isNull(ds)) {
            throw new CustomException("数据源异常");
        }
        return this.singleStrVal(ds, sql);
    }

    /**
     * 指定节点，执行查询语句
     *
     * @param host     host名称【key】
     * @param countSql 统计语句
     * @return 统计结果
     */
    public Long executeCountHost(String host, String countSql) {
        CkDataSource ds = dsMap.get(host);
        if (Objects.isNull(ds)) {
            throw new CustomException("数据源异常");
        }

        return this.count(ds, countSql);
    }

    /**
     * 根据host获取连接信息
     *
     * @param host host
     * @return 连接信息
     */
    public CkDataSource getHost(String host) {
        CkDataSource ds = dsMap.get(host);
        if (Objects.isNull(ds)) {
            throw new CustomException("数据源异常");
        }
        return ds;
    }

    /**
     * 获取所有host节点信息
     *
     * @return host节点信息
     */
    public List<CkDataSource> getAllHost() {
        return new ArrayList<>(dsMap.values());
    }

    public void execute(String tableName, String sql) {
        SnapshotTableMapping mapping = snapshotTableMappingDAO.selectByTable(tableName);
        if (Objects.isNull(mapping)) {
            // TODO：兜底， 断言 不存在映射的表是分布式表，任意节点执行SQL, 考虑是否会有隐患
//            this.execute(dsMap.values().iterator().next(), sql);
            this.execute(sql);
            return;
//            throw new CustomException("表未被记录分片情况");
        }

        String host = mapping.getHost();
        CkDataSource ds = dsMap.get(host);
        if (Objects.isNull(ds)) {
            throw new CustomException("数据源异常");
        }

        this.execute(ds, sql);
    }

    /**
     * @param assetsTable 快照资产表
     * @param tableName   临时表
     * @param createSQL   临时表建表语句
     */
    public String tryTemp(String assetsTable, String tableName, String createSQL) {
        if (assetsTable == null) {
            return tryRandomTemp(tableName, createSQL);
        }
        SnapshotTableMapping mapping = snapshotTableMappingDAO.selectByTable(assetsTable);
        if (Objects.isNull(mapping)) {
            return tryRandomTemp(tableName, createSQL);
        }

        String host;
        if (Objects.equals(mapping.getState(), SnapshotTableStateEnum.failover.name())) {
            host = mapping.getCandidateHost();
        } else {
            host = mapping.getHost();
        }
        return this.tryTempWithDs(tableName, createSQL, dsMap.get(host));
    }

    /**
     * @param tableName 临时表
     * @param createSQL 临时表建表语句
     */
    public String tryRandomTemp(String tableName, String createSQL) {
        CkDataSource ds = randomDs();
        if (Objects.isNull(ds)) {
            throw new CustomException("CK数据源管理异常");
        }
        return this.tryTempWithDs(tableName, createSQL, ds);
    }

    private String tryTempWithDs(String tableName, String createSQL, CkDataSource ds) {
        String dropTmpSQL = "DROP TABLE IF EXISTS " + tableName;
        log.info("host:{} ip:{} createTmpSql={}", ds.getHostName(), ds.getHostIp(), createSQL);
        this.executeWithRetry(ds, createSQL);

        String localSql;
        try {
            DataConnection conn = ds.getConn();
            localSql = conn.getJdbcOperations().queryForObject("SHOW CREATE TABLE " + tableName, String.class);
            log.info("tmpLocalSql={}", localSql);
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }

        // 删除临时表
        this.executeWithRetry(ds, dropTmpSQL);

        return localSql;
    }

    private CkDataSource randomDs() {
        int idx = ThreadLocalRandom.current().nextInt(dsMap.size());
        int i = 0;
        for (Map.Entry<String, CkDataSource> entry : dsMap.entrySet()) {
            if (idx == i) {
                return entry.getValue();
            }
            i++;
        }
        throw new RuntimeException("找不到可用数据源");
    }

    private void executeWithRetry(CkDataSource ds, String sql) {
        try {
            execute(ds, sql);
        } catch (Exception e) {
            log.error("RETRY", e);
            try {
                Thread.sleep(3000);
                execute(ds, sql);
            } catch (Exception se) {
                throw new RuntimeException(se);
            }
        }
    }

    private void execute(CkDataSource ds, String sql) {
        try {
            DataConnection conn = ds.getConn();
            conn.getJdbcOperations().execute(sql);
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询资产快照数据：本地表或者分布式表
     */
    public List<LinkedHashMap<String, Object>> mixQueryData(String tableName, final String sql, final String where,
                                                            final GetDataInfoRequestDTO requestDTO) {
        SnapshotTableMapping mapping = snapshotTableMappingDAO.selectByTable(tableName);
        if (Objects.isNull(mapping)) {
            return ckProviderMapper.selectApplicationDataFromCk(sql, where, requestDTO);
        }

        String finalSQL = TgCkProvider.selectApplicationDataFromCk(sql, where, requestDTO);
        return this.queryWithFailover(mapping, finalSQL, this::query);
    }

    public List<LinkedHashMap<String, Object>> query(String tableName, String sql) {
        SnapshotTableMapping mapping = snapshotTableMappingDAO.selectByTable(tableName);
        if (Objects.isNull(mapping)) {
            return ckProviderMapper.selectAllDataFromCk(sql);
        }

        return this.queryWithFailover(mapping, sql, this::query);
    }

    /**
     * 查询资产快照列：本地表或者分布式表
     */
    public List<String> mixQueryRange(String assetsTable, String tableName, String colName, DataRangeQueryDto range) {
        SnapshotTableMapping mapping = snapshotTableMappingDAO.selectByTable(assetsTable);
        if (Objects.isNull(mapping)) {
            return ckProviderMapper.selectDataRangeFromCk(colName, tableName, range);
        }

        String finalSQL = TgCkProvider.selectDataRangeFromCk(colName, tableName, range);
        return this.queryWithFailover(mapping, finalSQL, this::queryRange);
    }

    public List<AuthTableFieldDTO> mixMetaFields(String tableName) {
        String sql = "SELECT `name` as fieldName,`type` as dataType,if(notEmpty(`comment`), `comment`,`fieldName`) " +
                "as fieldAlias FROM system.columns WHERE database = '" + selfCKProperties.getDatabase()
                + "' AND `table` = '" + tableName + "' ORDER BY position ASC";
        SnapshotTableMapping mapping = snapshotTableMappingDAO.selectByTable(tableName);
        if (Objects.isNull(mapping)) {
            return dataClickhouseMapper.getFields(selfCKProperties.getDatabase(), tableName);
        }

        return this.queryWithFailover(mapping, sql, this::queryFields);
    }

    public Long mixCountRange(String assetsTable, String tableName, String colName, DataRangeQueryDto range) {
        SnapshotTableMapping mapping = snapshotTableMappingDAO.selectByTable(assetsTable);
        if (Objects.isNull(mapping)) {
            return (long) ckProviderMapper.countDataRangeFromCk(colName, tableName, range);
        }

        String finalSQL = TgCkProvider.selectDataRangeCountFromCk(colName, tableName, range);
        return this.queryWithFailover(mapping, finalSQL, this::count);
    }

    public Long mixCount(String tableName, final String sql) {
        return this.mixCount(tableName, sql, "");
    }

    /**
     * 统计资产快照行数：本地表或者分布式表
     */
    public Long mixCount(String tableName, final String sql, final String where) {
        String finalSql;
        if (StringUtils.isNotBlank(where)) {
            finalSql = "SELECT COUNT(*) FROM (" + sql + " WHERE " + where + ")";
        } else {
            finalSql = "SELECT COUNT(*) FROM (" + sql + ")";
        }

        SnapshotTableMapping mapping = snapshotTableMappingDAO.selectByTable(tableName);
        if (Objects.isNull(mapping)) {
            return dataClickhouseMapper.count(finalSql);
//            return ckProviderMapper.selectCountApplicationDataFromCk(sql, Optional.ofNullable(where).orElse(""));
        }

        return this.queryWithFailover(mapping, finalSql, this::count);
    }

    public String mixDistinctConcat(String tableName, final String fieldName) {
        String finalSql = "select arrayStringConcat(groupArray(distinct `" + fieldName + "`), '、') from " + tableName;

        SnapshotTableMapping mapping = snapshotTableMappingDAO.selectByTable(tableName);
        if (Objects.isNull(mapping)) {
            return dataClickhouseMapper.singleStrVal(finalSql);
        }

        return this.queryWithFailover(mapping, finalSql, this::singleStrVal);
    }


    public Set<String> mixColumnNames(String tableName, final String sql) {
        SnapshotTableMapping mapping = snapshotTableMappingDAO.selectByTable(tableName);
        if (Objects.isNull(mapping)) {
            return this.columnNamesByDs(slaveDataSource, sql);
        }

        return this.queryWithFailover(mapping, sql, this::columnNames);
    }


    /**
     * 包含故障转移
     */
    public <T> T queryWithFailover(SnapshotTableMapping mapping, String sql, BiFunction<CkDataSource, String, T> func) {
        boolean failover = Objects.equals(mapping.getState(), SnapshotTableStateEnum.failover.name());

        boolean noBackup = StringUtils.isBlank(mapping.getCandidateHost());

        String host = determineHost(mapping, failover, noBackup);
        CkDataSource ds = dsMap.get(host);
        if (Objects.isNull(ds)) {
            throw new CustomException(host + " 数据源异常");
        }

        try {
            return func.apply(ds, sql);
        } catch (Exception e) {
            if (!CkErrorMsg.needRetry(e.getMessage())) {
                throw e;
            }

            try {
                // 原节点重试： 批量出数时的查询报错，需要对原节点重试，因为此时并未备份
                TimeUnit.SECONDS.sleep(1);
                return func.apply(ds, sql);
            } catch (Exception re) {
                log.error("Retry: ", re);

                // 重试仍出错
                alertService.sendDevNormalMsg("CK触发故障转移,请检查 " + host + " 节点的 " + mapping.getTableName());
                if (!failover) {
                    log.error("first {} host error: ", host, e);
                    snapshotTableMappingDAO.update(new UpdateWrapper<SnapshotTableMapping>().lambda()
                            .eq(SnapshotTableMapping::getId, mapping.getId())
                            .set(SnapshotTableMapping::getState, SnapshotTableStateEnum.failover.name())
                    );

                    if (noBackup) {
                        log.warn("query occur before backup action: mapping={}", mapping);
                    } else {
                        String finalHost = Objects.equals(mapping.getCandidateHost(), host)
                                ? mapping.getHost() : mapping.getCandidateHost();
                        return func.apply(dsMap.get(finalHost), sql);
                    }
                }

                throw e;

            }
        }
    }

    private static String determineHost(SnapshotTableMapping mapping, boolean failover, boolean noBackup) {
        String host;

        if (noBackup) {
            host = mapping.getHost();
        } else {
            if (failover) {
                host = mapping.getCandidateHost();
            } else {
                // 有备份 数据正常时 随机选择节点查询
                boolean first = ThreadLocalRandom.current().nextInt(10) % 2 == 0;
                host = first ? mapping.getHost() : mapping.getCandidateHost();
            }
        }
        return host;
    }

    private Set<String> columnNames(CkDataSource ds, String sql) {
        return this.columnNamesByDs(ds.getSource(), sql);
    }

    private Set<String> columnNamesByDs(DruidDataSource ds, String sql) {
        DataConnection conn = new DataConnection(selfCKProperties.getDatabase(), ds, new JdbcTemplate(ds),
                DatabaseEnum.CLICKHOUSE.getFeature());

        Set<String> names = new LinkedHashSet<>();
        conn.getJdbcOperations().query(sql, c -> {
            ResultSetMetaData metaData = c.getMetaData();
            int count = metaData.getColumnCount();
            for (int i = 1; i <= count; i++) {
                String name = metaData.getColumnName(i);
                names.add(name);
            }
        });

        return names;
    }

    private Long count(CkDataSource ds, String sql) {
//        DruidDataSource v = ds.getSource();
//        DataConnection conn = new DataConnection(selfCKProperties.getDatabase(), v, new JdbcTemplate(v),
//                DatabaseEnum.CLICKHOUSE.getFeature());
        DataConnection conn = ds.getConn();

        // 验证 超时问题
//        try {
//            for (int i = 0; i < 4; i++) {
//                TimeUnit.SECONDS.sleep(3);
//                conn.getJdbcOperations().queryForObject(sql, Long.class);
//            }
//        } catch (Exception e) {
//            log.error("", e);
//        }
        return conn.getJdbcOperations().queryForObject(sql, Long.class);
    }

    private String singleStrVal(CkDataSource ds, String sql) {
        DataConnection conn = ds.getConn();
        return conn.getJdbcOperations().queryForObject(sql, String.class);
    }

    private List<AuthTableFieldDTO> queryFields(CkDataSource ds, String sql) {
//        DruidDataSource v = ds.getSource();
//        DataConnection conn = new DataConnection(selfCKProperties.getDatabase(), v, new JdbcTemplate(v),
//                DatabaseEnum.CLICKHOUSE.getFeature());
        DataConnection conn = ds.getConn();

        List<Map<String, Object>> result = conn.getJdbcOperations().queryForList(sql);
        return result.stream().map(a -> {
            AuthTableFieldDTO dto = new AuthTableFieldDTO();
            dto.setFieldName(Optional.ofNullable(a.get("fieldName")).map(Object::toString).orElse(""));
            dto.setFieldAlias(Optional.ofNullable(a.get("fieldAlias")).map(Object::toString).orElse(""));
            dto.setDataType(Optional.ofNullable(a.get("dataType")).map(Object::toString).orElse(""));
            return dto;
        }).collect(Collectors.toList());
    }

    private List<String> queryRange(CkDataSource ds, String sql) {
//        DruidDataSource v = ds.getSource();
//        DataConnection conn = new DataConnection(selfCKProperties.getDatabase(), v, new JdbcTemplate(v),
//                DatabaseEnum.CLICKHOUSE.getFeature());
        DataConnection conn = ds.getConn();
        return conn.getJdbcOperations().queryForList(sql, String.class);
    }

    private List<LinkedHashMap<String, Object>> query(CkDataSource ds, String sql) {
//        DruidDataSource v = ds.getSource();
//        DataConnection conn = new DataConnection(selfCKProperties.getDatabase(), v, new JdbcTemplate(v),
//                DatabaseEnum.CLICKHOUSE.getFeature());
        DataConnection conn = ds.getConn();
        RowMapper<Map<String, Object>> rowMapper = new ColumnMapRowMapper() {
            protected Map<String, Object> createColumnMap(int columnCount) {
                return new LinkedHashMap<>(columnCount);
            }
        };

        List<Map<String, Object>> result = conn.getJdbcOperations().query(sql, rowMapper);
        List<LinkedHashMap<String, Object>> ct = new ArrayList<>(result.size());
        for (Map<String, Object> map : result) {
            ct.add((LinkedHashMap<String, Object>) map);
        }
        return ct;
    }

    /**
     * @param applyUserId 申请人
     * @param tableName   旧资产表 新表名
     */
    public void convertTable(Long applyUserId, String tableName, String newName) {
        log.info("CONVERT old={} new={}", tableName, newName);
        SnapshotTableMapping exist = snapshotTableMappingDAO.selectByTable(newName);
        if (Objects.nonNull(exist)) {
            log.warn("exist: {}", newName);
            return;
        }

        String syncFmt = "INSERT INTO %s.%s %s";
        String insertSQL = String.format(syncFmt, selfCKProperties.getDatabase(), newName, "SELECT * FROM " + tableName);
        CkDataSource ds = randomDs();
        try {
//            JdbcTemplate temp = new JdbcTemplate(ds.getSource());
//            DataConnection conn = new DataConnection(selfCKProperties.getDatabase(), ds.getSource(), temp,
//                    DatabaseEnum.CLICKHOUSE.getFeature());
            DataConnection conn = ds.getConn();
            String localName = ApplicationSqlUtil.getLocalTableName(tableName);
            String localSql = conn.getJdbcOperations().queryForObject("SHOW CREATE TABLE " + localName, String.class);
            log.info("createSql={}", localSql);
            localSql = localSql.replace(localName, newName);

            // 创建单点表
            this.createTable(applyUserId, newName, localSql, insertSQL, null);

            // 删除原有表
            this.execute(ds, "drop table if exists " + localName + " on cluster default_cluster");
            this.execute(ds, "drop table if exists " + tableName + " on cluster default_cluster");
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建第一个表，并异步创建备份表，存储关联关系
     *
     * @param create    建表SQL
     * @param insert    写数据SQL
     * @param insertSql 第二段写数据SQL
     */
    public void createTable(Long applyUserId, String tableName, String create, String insert, String insertSql) {
        if (StringUtils.isBlank(create) || StringUtils.isBlank(insert)) {
            log.info("create={} insert={}", create, insert);
            throw new RuntimeException("资产DDL SQL构造异常");
        }
        Optional<CkDataSource> ds = this.determineDataSource(null);
        if (!ds.isPresent()) {
            throw new CustomException("无节点可用");
        }
        // 将复制表转换为普通表
        create = create.replaceAll("ReplicatedMergeTree\\(.*\\)", "MergeTree()");
        log.info("main host: {} {}", ds.get().getHostName(), ds.get().getHostIp());
        this.execute(ds.get(), create);
        this.execute(ds.get(), insert);
        if (StringUtils.isNotBlank(insertSql)) {
            this.execute(ds.get(), insertSql);
        }

        LocalDateTime now = LocalDateTime.now();
        SnapshotTableMapping mapping = new SnapshotTableMapping()
                .setTableName(tableName)
                .setState(SnapshotTableStateEnum.create.name())
                .setHdfsState(SnapshotTableHdfsStateEnum.none.name())
                .setCreator(applyUserId)
                .setCreateTime(now)
                .setUpdateTime(now)
                .setHost(ds.get().getHostName());
        snapshotTableMappingDAO.save(mapping);

        Long id = mapping.getId();
        SnapshotTableMappingDetail detail = new SnapshotTableMappingDetail().setMapId(id).setDdl(create);
        snapshotTableMappingDetailMapper.insert(detail);

//        String host = mapping.getHost();
//        if (BooleanUtils.isTrue(appProperties.getNeedBackup())) {
//            // 异步创建备份表
//            String finalCreate = create;
//            scheduler.schedule(() ->
//                            miniCkPool.execute(()
//                                    -> this.backupAssetsTableHandler(tableName, id, host, finalCreate, mapping)
//                            ),
//                    BACK_DELAY_MIN, TimeUnit.MINUTES
//            );
//        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduleToBackup() {
        if (BooleanUtils.isNotTrue(appProperties.getNeedBackup())) {
            return;
        }

        redisLock.wrapperLock(RedisKeys.Assets.TABLE_BACKUP_LOCK, Duration.ofHours(2), () -> {
            log.info("start backup ck table");
            List<SnapshotTableMapping> mapList = snapshotTableMappingDAO.lambdaQuery()
                    .eq(SnapshotTableMapping::getState, SnapshotTableStateEnum.create.name())
                    .list();
            if (CollectionUtils.isEmpty(mapList)) {
                return;
            }
            miniCkPool.execute(() -> {
                List<Long> mapIds = Lambda.buildList(mapList, SnapshotTableMapping::getId);
                List<SnapshotTableMappingDetail> details = snapshotTableMappingDetailMapper.selectList(
                        new QueryWrapper<SnapshotTableMappingDetail>().lambda()
                                .in(SnapshotTableMappingDetail::getMapId, mapIds));
                Map<Long, SnapshotTableMappingDetail> detailMap = Lambda.buildMap(details, SnapshotTableMappingDetail::getMapId);

                for (SnapshotTableMapping mapping : mapList) {
                    try {
                        String create = Optional.ofNullable(detailMap.get(mapping.getId()))
                                .map(SnapshotTableMappingDetail::getDdl)
                                .filter(StringUtils::isNoneBlank)
                                .orElseThrow(() -> new CustomException(mapping.getTableName() + " 表DDL丢失"));

                        this.backupAssetsTableHandler(create, mapping);
//                    log.info("create={}", create);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
            });
        });
    }

    private void backupAssetsTableHandler(String finalCreate, SnapshotTableMapping mapping) {
        String tableName = mapping.getTableName();
        Long id = mapping.getId();
        String host = mapping.getHost();

        snapshotTableMappingDAO.updateState(id, SnapshotTableStateEnum.copying);

        Optional<CkDataSource> backupNode = this.determineDataSource(Collections.singleton(host));
        if (!backupNode.isPresent()) {
            snapshotTableMappingDAO.updateState(id, SnapshotTableStateEnum.copy_fail);
            throw new CustomException("无备份节点可用");
        }

        log.info("backup host: {} {} {}", backupNode.get().getHostName(), backupNode.get().getHostIp(), tableName);
        this.executeWithRetry(backupNode.get(), finalCreate);
        snapshotTableMappingDAO.update(null, new UpdateWrapper<SnapshotTableMapping>().lambda()
                .set(SnapshotTableMapping::getState, SnapshotTableStateEnum.normal.name())
                .set(SnapshotTableMapping::getCandidateHost, backupNode.get().getHostName())
                .eq(SnapshotTableMapping::getId, id));

        // 主节点跑 insert remote 到备份节点
        CkDataSource mainDs = dsMap.get(host);
        String syncFmt = "INSERT INTO function remote('%s', '%s', '%s', '%s') %s";
        String syncSql = String.format(syncFmt, backupNode.get().getHostIp(),
                selfCKProperties.getDatabase() + "." + tableName,
                selfCKProperties.getUsername(), selfCKProperties.getPassword(), "select * from " + tableName);
        this.executeWithRetry(mainDs, syncSql);
        log.info("finish ck backup {}", tableName);

        this.backupToHdfs(mapping);
    }

    public void backupToHdfs(String tableName) {
        SnapshotTableMapping map = snapshotTableMappingDAO.selectByTable(tableName);
        if (Objects.isNull(map)) {
            log.warn("not exist: {}", tableName);
            return;
        }
        if (Objects.equals(map.getHdfsState(), SnapshotTableHdfsStateEnum.normal.name())) {
            log.warn("already backup: tableName={}", tableName);
            return;
        }
        this.backupToHdfs(map);
    }

    public void backupToHdfs(Integer max) {
        List<SnapshotTableMapping> list = snapshotTableMappingDAO.getBaseMapper()
                .selectList(new QueryWrapper<SnapshotTableMapping>().lambda()
                        .notIn(SnapshotTableMapping::getHdfsState, SnapshotTableHdfsStateEnum.notNeedBackup));
        int count = 0;
        for (SnapshotTableMapping map : list) {
            count++;
            if (Objects.nonNull(max) && count > max) {
                return;
            }
            this.backupToHdfs(map);
        }
    }

    /**
     * 基于主节点创建HDFS备份
     */
    public void backupToHdfs(SnapshotTableMapping mapping) {
        if (BooleanUtils.isNotTrue(appProperties.getNeedHdfs())) {
            return;
        }
        if (Objects.equals(mapping.getHdfsState(), SnapshotTableHdfsStateEnum.normal.name())) {
            return;
        }

        // HDFS备份
        Long id = mapping.getId();
        String tableName = mapping.getTableName();
        try {
            snapshotTableMappingDAO.updateHdfsState(id, SnapshotTableHdfsStateEnum.copying);

            this.execute(dsMap.get(mapping.getHost()), this.buildHdfsCreate(tableName));

            String hiveInsert = String.format("INSERT INTO %s SELECT * FROM %s", HDFS_PREFIX + tableName, tableName);
            this.execute(dsMap.get(mapping.getHost()), hiveInsert);
            snapshotTableMappingDAO.updateHdfsState(id, SnapshotTableHdfsStateEnum.normal);
            log.info("finish hdfs backup");
        } catch (Exception e) {
            log.error("", e);
            snapshotTableMappingDAO.updateHdfsState(id, SnapshotTableHdfsStateEnum.copy_fail);
        }
    }

    public void deleteHdfsTable(String tableName) {
        SnapshotTableMapping map = snapshotTableMappingDAO.selectByTable(tableName);
        if (Objects.isNull(map)) {
            log.warn("not exist: {}", tableName);
            return;
        }

        this.deleteHdfsTable(map);
    }

    /**
     * 删除资产表
     */
    public void deleteAssetsTable(String tableName) {
        if (StringUtils.contains(tableName, CkTableSuffixTable.SNAP)) {
            this.deleteTable(tableName);
        } else {
            String shardSQL = "DROP TABLE IF EXISTS " + tableName;
            log.info("shard: SQL={}", shardSQL);
            this.executeAll(shardSQL);

            String localSQL = "DROP TABLE IF EXISTS " + tableName.replace("_shard", "_local");
            log.info("local: SQL={}", localSQL);
            this.executeAll(localSQL);
        }
    }

    /**
     * 异步删除CK表，HDFS表
     *
     * @param tableName 表名
     */
    public void deleteTable(String tableName) {
        scheduler.schedule(() -> miniCkPool.execute(() -> {
            SnapshotTableMapping map = snapshotTableMappingDAO.selectByTable(tableName);
            if (Objects.isNull(map)) {
                log.warn("not exist: {}", tableName);
                return;
            }

            this.deleteHdfsTable(map);
            this.deleteClickhouseTable(map);
        }), 10, TimeUnit.SECONDS);
    }

    public void deleteClickhouseTable(SnapshotTableMapping mapping) {
        String tableName = mapping.getTableName();
        try {
            String drop = "DROP TABLE IF EXISTS " + tableName;
            this.execute(dsMap.get(mapping.getHost()), drop);
            if (StringUtils.isNotBlank(mapping.getCandidateHost())) {
                this.execute(dsMap.get(mapping.getCandidateHost()), drop);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public void deleteHdfsTable(SnapshotTableMapping mapping) {
        if (!Objects.equals(mapping.getHdfsState(), SnapshotTableHdfsStateEnum.normal.name())) {
            log.warn("not need delete: tableName={}", mapping.getTableName());
            return;
        }
        String tableName = mapping.getTableName();
        try {
            this.execute(dsMap.get(mapping.getHost()), this.buildHdfsCreate(tableName));
            this.execute(dsMap.get(mapping.getHost()), String.format("TRUNCATE TABLE %s", HDFS_PREFIX + tableName));
            this.execute(dsMap.get(mapping.getHost()), String.format("DROP TABLE %s", HDFS_PREFIX + tableName));
            snapshotTableMappingDAO.updateHdfsState(mapping.getId(), SnapshotTableHdfsStateEnum.delete);
            log.info("finish delete hdfs table");
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * @see OutsideClickhouseMapper#getFields(String, String)
     */
    public List<ApplicationDataDto.Header> queryTableMeta(String table) {
        return null;
    }

    public void recoverFromHdfsForHost(String host) {
        boolean exist = dsMap.containsKey(host);
        if (!exist) {
            throw new CustomException("不支持的CK节点");
        }

        List<SnapshotTableMapping> mapList = snapshotTableMappingDAO.selectByHost(host);
        if (CollectionUtils.isEmpty(mapList)) {
            return;
        }
        Set<Long> ids = Lambda.buildSet(mapList, SnapshotTableMapping::getId);
        List<SnapshotTableMappingDetail> detail = snapshotTableMappingDetailMapper.selectList(
                new QueryWrapper<SnapshotTableMappingDetail>().lambda()
                        .in(SnapshotTableMappingDetail::getMapId, ids)
        );
        Map<Long, String> ddlMap = Lambda.buildMap(detail, SnapshotTableMappingDetail::getMapId, SnapshotTableMappingDetail::getDdl);

        for (SnapshotTableMapping snap : mapList) {
            try {
                TimeUnit.MILLISECONDS.sleep(10);
                this.recoverFromHdfs(dsMap.get(host), snap.getTableName(), ddlMap.get(snap.getId()));
            } catch (InterruptedException e) {
                log.error("", e);
            }
        }
    }

    /**
     * 调试用
     */
    public void recoverFromHdfs(String tableName) {
        SnapshotTableMapping map = snapshotTableMappingDAO.selectByTable(tableName);
        if (Objects.isNull(map)) {
            log.warn("not exist: {}", tableName);
            return;
        }
        SnapshotTableMappingDetail detail = snapshotTableMappingDetailMapper.selectOne(
                new QueryWrapper<SnapshotTableMappingDetail>().lambda()
                        .eq(SnapshotTableMappingDetail::getMapId, map.getId())
        );

        this.recoverFromHdfs(dsMap.get(map.getHost()), tableName, detail.getDdl());
        this.recoverFromHdfs(dsMap.get(map.getCandidateHost()), tableName, detail.getDdl());
    }

    private void recoverFromHdfs(CkDataSource ds, String table, String ddl) {
        String exist = String.format("select count(*) from system.tables where name = '%s' and database = '%s' ",
                table, selfCKProperties.getDatabase());

        // 当前节点已存在该表，无需恢复
        Integer existCount = ds.getConn().getJdbcOperations().queryForObject(exist, Integer.class);
        if (Objects.nonNull(existCount) && existCount > 0) {
            return;
        }
        this.execute(ds, ddl);
        this.execute(ds, this.buildHdfsCreate(table));
        this.execute(ds, String.format("INSERT INTO %s SELECT * FROM %s", table, HDFS_PREFIX + table));
    }

    private String buildHdfsCreate(String tableName) {
        return String.format("CREATE TABLE IF NOT EXISTS %s as %s ENGINE = HDFS('hdfs://nameservice1/backup/clickhouse/%s/%s/', 'ORC');",
                HDFS_PREFIX + tableName, tableName, selfCKProperties.getDatabase(), tableName);
    }

    private DataConnection getConn() {
        // v.getConnection().getSchema() 需要注意连接资源泄漏问题 数据库名不会变化，可直接使用配置内容
        return new DataConnection(selfCKProperties.getDatabase(), slaveDataSource,
                new JdbcTemplate(slaveDataSource), DatabaseEnum.CLICKHOUSE.getFeature());
    }
}
