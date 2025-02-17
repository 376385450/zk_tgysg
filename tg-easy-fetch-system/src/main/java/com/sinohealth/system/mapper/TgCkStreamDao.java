package com.sinohealth.system.mapper;

import com.framework.common.utils.ThreadUtil;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.ck.dao.SnapshotTableMappingDAO;
import com.sinohealth.system.biz.ck.domain.SnapshotTableMapping;
import com.sinohealth.system.biz.ck.dto.CkDataSource;
import com.sinohealth.system.biz.dataassets.dto.bo.QueueChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-09-22 14:34
 */
@Slf4j
@Mapper
@Repository
public class TgCkStreamDao {

    @Resource(name = "slaveDataSource")
    private DataSource slaveDataSource;

    @Resource(name = "customerCKDataSource")
    private DataSource customerCKDataSource;

    @Autowired
    private CKClusterAdapter ckClusterAdapter;

    @Autowired
    private SnapshotTableMappingDAO snapshotTableMappingDAO;


    public void fetchBatchQueue(String tableName, String sql, String where, int fetchSize,
                                QueueChannel<LinkedHashMap<String, Object>> channel) {
        SnapshotTableMapping mapping = snapshotTableMappingDAO.selectByTable(tableName);
        if (Objects.isNull(mapping)) {
            fetchBatchWithQueue(slaveDataSource, sql, where, fetchSize, channel);
            return;
        }

        ckClusterAdapter.queryWithFailover(mapping, sql, (ckDs, ignored) -> {
            fetchBatchWithQueue(ckDs.getSource(), sql, where, fetchSize, channel);
            return 0;
        });
    }

    public void fetchBatch(String tableName, String sql, String where, int fetchSize,
                           Consumer<List<LinkedHashMap<String, Object>>> handle) {
        SnapshotTableMapping mapping = snapshotTableMappingDAO.selectByTable(tableName);
        if (Objects.isNull(mapping)) {
            fetchBatchWithDataResource(slaveDataSource, sql, where, fetchSize, handle);
            return;
        }

        ckClusterAdapter.queryWithFailover(mapping, sql, (ckDs, ignored) -> {
            fetchBatchWithDataResource(ckDs.getSource(), sql, where, fetchSize, handle);
            return 0;
        });
    }

    public void fetchCustomBatch(String tableName, String where, int fetchSize, Consumer<List<LinkedHashMap<String, Object>>> handle) {
        fetchBatchWithDataResource(customerCKDataSource, "SELECT * FROM " + tableName, where, fetchSize, handle);
    }

    private void fetchBatchWithDataResource(DataSource ds, String sql, String where, int fetchSize,
                                            Consumer<List<LinkedHashMap<String, Object>>> handle) {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            connection = ds.getConnection();
            String query;
            if (StringUtils.isNotBlank(where)) {
                query = sql + " WHERE " + where;
            } else {
                query = sql;
            }

            log.info("stream export: query={}", query);

            stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setQueryTimeout(36000);
            stmt.setFetchSize(fetchSize);

            rs = stmt.executeQuery(query);
            int counter = 0;

            List<LinkedHashMap<String, Object>> data = new ArrayList<>();
            while (rs.next()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                data.add(row);

                if (data.size() > fetchSize) {
                    handle.accept(data);
                    counter++;
                    data = new ArrayList<>();
                }
            }
            if (!data.isEmpty()) {
                handle.accept(data);
                counter++;
            }

            log.info("stream export: count={} dataSize={} ", counter, (counter - 1) * fetchSize + data.size());
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        } finally {
            close(connection, stmt, rs);
        }
    }

    public void fetchCustomBatchHost(String hostname, String sql, String where, int fetchSize, Function<List<LinkedHashMap<String, Object>>, Future<?>> handle) {
        CkDataSource host = ckClusterAdapter.getHost(hostname);
        fetchBatchWithDataResourceAsyncHandle(host.getSource(), sql, where, fetchSize, handle);
    }

    private void fetchBatchWithDataResourceAsyncHandle(DataSource ds, String sql, String where, int fetchSize,
                                                       Function<List<LinkedHashMap<String, Object>>, Future<?>> handle) {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            connection = ds.getConnection();
            String query;
            if (StringUtils.isNotBlank(where)) {
                query = sql + " WHERE " + where;
            } else {
                query = sql;
            }

            log.info("stream export: query={}", query);

            stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setQueryTimeout(36000);
            stmt.setFetchSize(fetchSize);

            rs = stmt.executeQuery(query);
            int counter = 0;
            List<Future<?>> futures = new ArrayList<>();
            List<LinkedHashMap<String, Object>> data = new ArrayList<>();
            while (rs.next()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }
                data.add(row);

                if (data.size() >= fetchSize) {
                    futures.add(handle.apply(data));
                    counter++;
                    data = new ArrayList<>();
                }
            }
            if (!data.isEmpty()) {
                futures.add(handle.apply(data));
                counter++;
            }

            log.info("stream export: count={} dataSize={} ", counter, (counter - 1) * fetchSize + data.size());
            futures.forEach(i -> {
                try {
                    i.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
            log.info("异步线程处理完成");
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        } finally {
            close(connection, stmt, rs);
            ThreadUtil.sleep(5000);
        }
    }

    private void fetchBatchWithQueue(DataSource ds, String sql, String where, int fetchSize,
                                     QueueChannel<LinkedHashMap<String, Object>> channel) {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            connection = ds.getConnection();
            String query;
            if (StringUtils.isNotBlank(where)) {
                query = sql + " WHERE " + where;
            } else {
                query = sql;
            }

            log.info("stream queue export: query={}", query);

            stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            stmt.setQueryTimeout(3600);
            stmt.setFetchSize(fetchSize);

            rs = stmt.executeQuery(query);
            int counter = 0;

            while (rs.next()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }

                counter++;
//                long start = System.currentTimeMillis();
                try {
                    channel.put(row);
//                    long end = System.currentTimeMillis();
//                    long wait = end - start;
//                    if (wait > 1000) {
//                        log.warn("wait writer: ={}", wait);
//                    }
                } catch (Exception e) {
                    log.error("", e);
                    break;
                }
            }

            log.info("stream queue export: size={} ", counter);
        } catch (Exception e) {
            log.error("", e);
            throw new RuntimeException(e);
        } finally {
            close(connection, stmt, rs);
            channel.stop();
        }
    }

    private void close(AutoCloseable... rs) {
        if (Objects.isNull(rs)) {
            return;
        }
        for (AutoCloseable r : rs) {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }


}
