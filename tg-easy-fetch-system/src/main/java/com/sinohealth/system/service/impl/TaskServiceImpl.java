package com.sinohealth.system.service.impl;

import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.util.concurrent.AtomicDouble;
import com.sinohealth.bi.enums.DatabaseEnum;
import com.sinohealth.common.config.DataConnection;
import com.sinohealth.common.config.DataSourceFactory;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.enums.HeatEnum;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.DirCache;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.dto.TableBaseInfoDto;
import com.sinohealth.system.mapper.DataDirMapper;
import com.sinohealth.system.mapper.TableInfoMapper;
import com.sinohealth.system.mapper.TableLogMapper;
import com.sinohealth.system.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Jingjun
 * @since 2021/5/20
 */
@Service("taskService")
@Slf4j
public class TaskServiceImpl implements ITaskService {

    @Autowired
    private ITableInfoService tableInfoService;

    @Autowired
    private ITableStatisticsService tableStatisticsService;

    @Autowired
    private ISysUserTableService userTableService;

    @Autowired
    private ITableFieldInfoService fieldInfoService;

    @Resource
    private DataDirMapper dataDirMapper;

    @Resource
    private TableInfoMapper tableInfoMapper;

    @Resource
    private TableLogMapper tableLogMapper;


    /**
     * 统计表行数，每两小时更新一次
     *
     * @param now
     */
    @Transactional
    public void countTableRowByUpdateTime(Date now) {
        log.info("-----------------------------------表行数统计开始-----------------------------------");

//        Calendar c = Calendar.getInstance();
//        c.setTime(now);
//        c.add(Calendar.HOUR, -2);
//        c.add(Calendar.MINUTE, -10);
//        countBySchema(c.getTime(), 5L, "sino%");


        LambdaQueryWrapper<DataDir> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.ne(DataDir::getDatasourceId, 0);
        final List<DataDir> dataDirList = dataDirMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(dataDirList)) {
            return;
        }
        dataDirList.forEach(dataDir -> {
            final DataConnection dataConnection = DataSourceFactory.getDataConnection(dataDir.getId());
            if (DatabaseEnum.MYSQL.getFeature().equals(dataConnection.getDatabaseType())) {
                countRowByMySQL(dataConnection, dataDir.getId());

            } else if (DatabaseEnum.HIVE2.getFeature().equals(dataConnection.getDatabaseType())) {
                // 预留

            } else if (DatabaseEnum.IMPALA.getFeature().equals(dataConnection.getDatabaseType())) {
                // 预留

            }
        });


        log.info("-----------------------------------表行数统计结束-----------------------------------");
    }

    private void countRowByMySQL(DataConnection dataConnection, Long dirId) {
        JdbcOperations jdbcOperations = dataConnection.getJdbcOperations();

        // 查询表信息
        LambdaQueryWrapper<TableInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(TableInfo::getDirId, dirId);
        final List<TableInfo> tableInfoList = tableInfoMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(tableInfoList)) {
            return;
        }

        StringBuffer analyzeSql = new StringBuffer("ANALYZE TABLE ");
        Set<Object> tableNameSet = new HashSet<>();
        tableInfoList.forEach(tableInfo -> {
            if (!tableNameSet.contains(tableInfo.getTableName())) {
                tableNameSet.add(tableInfo.getTableName());
                analyzeSql.append(tableInfo.getTableName()).append(",");
            }
        });
        analyzeSql.deleteCharAt(analyzeSql.length() - 1);
        try {
            // 更新 information_schema 表的表信息行数
            jdbcOperations.queryForList(analyzeSql.toString());

        } catch (DataAccessException e) {
            log.error("", e);
        }


        // 查询表的行数信息
        String tableRowSql = "SELECT table_name, table_rows FROM information_schema.tables WHERE table_schema = '"
                + dataConnection.getSchema() + "' ORDER BY table_rows desc";
        final List<Map<String, Object>> rowList = jdbcOperations.queryForList(tableRowSql);
        if (CollectionUtils.isNotEmpty(rowList)) {
            tableInfoList.forEach(tableInfo -> rowList.forEach(map -> {
                final Object tableRows = map.get("table_rows");
                if (tableInfo.getTableName().equals(map.get("table_name")) && tableRows != null
                        && ((BigInteger) tableRows).longValue() != tableInfo.getTotalRow()) {
                    tableInfo.setTotalRow(((BigInteger) tableRows).longValue());
                    tableInfoMapper.updateById(tableInfo);
                }
            }));
        }
    }

    @Deprecated
    private void countBySchema(Date now, Long dirId, String schema) {
        JdbcOperations jdbcOperations = DataSourceFactory.getDataConnection(dirId).getJdbcOperations();
        List<Map<String, Object>> updateList = jdbcOperations.queryForList("select  CONCAT(\"select count(*)  total_row,'\",t.TABLE_NAME,\"' table_name from \",t.TABLE_SCHEMA,\".\",t.TABLE_NAME) countSql from information_schema.`TABLES` t where t.TABLE_SCHEMA like '" + schema + "' and t.UPDATE_TIME >'" + DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, now) + "'");

        if (!ObjectUtils.isEmpty(updateList)) {

            StringBuffer sql = new StringBuffer();

            int len = updateList.size();

            int pages = PageUtil.totalPage(len, 100);

            for (int page = 0; page < pages; page++) {
                int start = PageUtil.getStart(page, 100);
                int end = (page + 1) == pages ? len : (page + 1) * 100;
                sql.append(updateList.get(start).get("countSql"));
                for (int i = start + 1; i < end; i++) {
                    sql.append(" union all ");
                    sql.append(updateList.get(i).get("countSql"));
                }

                List<Map<String, Object>> countList = jdbcOperations.queryForList(sql.toString());

                countList.forEach(map -> {
                    tableInfoService.update(Wrappers.<TableInfo>update().eq("table_name", map.get("table_name")).set("total_row", map.get("total_row")));
                });
                countList.clear();
            }
        }
    }

    /**
     * 统计每天的使用监控
     *
     * @param now
     */
    @Transactional
    public void updateUseStatics(Date now) {
        log.info("-----------------------------------使用监控统计开始-----------------------------------");

        List<TableStatistics> list = tableStatisticsService.ininTableStatistics();
        List<SysUserTable> userTableList = userTableService.groupByUserTable();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.DAY_OF_WEEK, -1);
        list.forEach(t -> {
            userTableList.stream().filter(u -> u.getTableId().equals(t.getTableId())).forEach(u -> {
                if (u.isConcern()) {
                    t.setTotalConcern(u.getAccessType());
                }
                t.setTotalAccount(t.getTotalAccount() + u.getAccessType());

            });
            t.setCreateTime(cal.getTime());
            t.setTotalQueryTimes(t.getTotalQueryTimes() + t.getQueryTimes());
            tableInfoService.updateQueryTime(t.getTableId(), -t.getQueryTimes(), t.getQueryTimes());
        });

        tableStatisticsService.saveBatch(list);

        log.info("-----------------------------------使用监控统计结束-----------------------------------");
    }

    /**
     * 定时任务-同步全部表结构
     *
     * @author linkaiwei
     * @date 2021-08-12 12:00:54
     * @since 1.3.3.0
     */
    @Override
    public void syncTableInfo() {
        log.info("-----------------------------------同步表基础信息开始-----------------------------------");

        String tableSqlWithSchema = "select t.TABLE_NAME,t.TABLE_ROWS as totalRows,t.DATA_LENGTH as storeSize,t.CREATE_TIME,t.UPDATE_TIME,t.TABLE_COMMENT as comment from information_schema.`TABLES` t where t.TABLE_SCHEMA = '%s'";
        String tableSqlWithSchemaAndName = "select t.TABLE_NAME,t.TABLE_ROWS as totalRows,t.DATA_LENGTH as storeSize,t.CREATE_TIME,t.UPDATE_TIME,t.TABLE_COMMENT as comment from information_schema.`TABLES` t where t.TABLE_SCHEMA = '%s' and t.TABLE_NAME like '%s'";
        String fieldSqlWithSchema = "select t.TABLE_NAME,t.COLUMN_COMMENT as comment,t.COLUMN_NAME as fieldName, CASE t.IS_NULLABLE WHEN 'NO' THEN 0 ELSE 1 END as empty, t.COLUMN_TYPE as dataType, CASE t.COLUMN_KEY WHEN 'PRI' THEN 1 ELSE 0 END as primary_key from information_schema.`COLUMNS` t where t.TABLE_SCHEMA='%s'";
        String fieldSqlWithSchemaAndName = "select t.TABLE_NAME,t.COLUMN_COMMENT as comment,t.COLUMN_NAME as fieldName, CASE t.IS_NULLABLE WHEN 'NO' THEN 0 ELSE 1 END as empty, t.COLUMN_TYPE as dataType, CASE t.COLUMN_KEY WHEN 'PRI' THEN 1 ELSE 0 END as primary_key from information_schema.`COLUMNS` t where t.TABLE_SCHEMA='%s' and t.TABLE_NAME like '%s'";

        // 查询出全部的数据目录信息
        List<DataDir> dataDirList = DirCache.getList().stream().filter(d -> d.getDatasourceId() > 0).collect(Collectors.toList());
        Date now = new Date();
        dataDirList.forEach(dataDir -> {
            // 获取数据目录对应的数据源连接信息
            DataConnection dataConnection = DataSourceFactory.getDataConnection(dataDir.getId());

            if (DatabaseEnum.HIVE2.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
                syncHive(dataConnection, dataDir, now);
                return;
            }

            if (DatabaseEnum.IMPALA.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
                syncImpala(dataConnection, dataDir, now);
                return;
            }

            String tableSql = StringUtils.isEmpty(dataDir.getPrefix())
                    ? String.format(tableSqlWithSchema, dataConnection.getSchema())
                    : String.format(tableSqlWithSchemaAndName, dataConnection.getSchema(), dataDir.getPrefix() + "%");

            List<TableBaseInfoDto> tables = dataConnection.getJdbcOperations()
                    .query(tableSql, new BeanPropertyRowMapper<>(TableBaseInfoDto.class));

            List<TableInfo> tableInfoList = new ArrayList<>(tables.size());

            // 已存在的表
            List<TableInfo> existTableInfoList = tableInfoService.list(Wrappers.<TableInfo>query().eq("dir_id", dataDir.getId()));
            tables.stream().filter(t -> !existTableInfoList.stream().anyMatch(e -> e.getTableName().equals(t.getTableName()))).forEach(t -> {
                TableInfo tableInfo = new TableInfo();
                tableInfo.setTableName(t.getTableName());
                tableInfo.setTotalRow(t.getTotalRows());
                tableInfo.setTableAlias(StringUtils.isEmpty(t.getComment()) ? t.getTableName() : t.getComment());
                tableInfo.setComment(t.getComment());
                tableInfo.setStatus(1);
                tableInfo.setDirId(dataDir.getId());
                tableInfo.setCreateTime(now);
                tableInfo.setDataLength(t.getStoreSize() == null ? 0 : t.getStoreSize());
                tableInfoList.add(tableInfo);
            });


            existTableInfoList.forEach(t -> {

                Optional<TableBaseInfoDto> opt = tables.stream().filter(n -> n.getTableName().equals(t.getTableName())).findFirst();
                if (opt.isPresent()) {
                    if (t.getStatus() == 0) {
                        // 将已删除的表，恢复正常。
                        tableInfoService.update(Wrappers.<TableInfo>update().set("status", 1).eq("id", t.getId()));

                    } else {
                        // 更新表信息
                        final UpdateWrapper<TableInfo> update = Wrappers.update();
                        update.set("update_time", now);
                        update.set("data_length", opt.get().getStoreSize() == null ? 0 : opt.get().getStoreSize());

                        String oldComment = StringUtils.isEmpty(t.getComment()) ? "" : t.getComment();
                        String newComment = StringUtils.isEmpty(opt.get().getComment()) ? "" : opt.get().getComment();
                        if (!oldComment.equals(newComment)) {
                            update.set("comment", newComment);
                        }

                        update.eq("id", t.getId());
                        tableInfoService.update(update);
                    }
                } else {
                    // 将已删除的表，标志为删除。
                    tableInfoService.update(Wrappers.<TableInfo>update().set("status", 0).eq("id", t.getId()));
                    userTableService.remove(Wrappers.<SysUserTable>query().eq("table_id", t.getId()));
                }
            });

            if (!tableInfoList.isEmpty()) {
                tableInfoService.saveBatch(tableInfoList);
            }

            // 表字段信息
            String fieldSql = StringUtils.isEmpty(dataDir.getPrefix())
                    ? String.format(fieldSqlWithSchema, dataConnection.getSchema())
                    : String.format(fieldSqlWithSchemaAndName, dataConnection.getSchema(), dataDir.getPrefix() + "%");
            List<TableFieldInfo> fieldInfoList = dataConnection.getJdbcOperations()
                    .query(fieldSql, new BeanPropertyRowMapper<>(TableFieldInfo.class));

            if (!ObjectUtils.isEmpty(fieldInfoList)) {
                List<TableFieldInfo> fieldList = fieldInfoService.list(Wrappers.<TableFieldInfo>query().eq("dir_id", dataDir.getId()));

                List<TableFieldInfo> addFields = new ArrayList<>();
                List<TableFieldInfo> updateFields = new ArrayList<>();

                fieldInfoList.forEach(field -> {
                    field.setFieldAlias(StringUtils.isEmpty(field.getComment()) ? field.getFieldName() : field.getComment());
                    field.setCreateTime(now);
                    field.setDirId(dataDir.getId());
                    field.setStatus(true);
                    int index = field.getDataType().indexOf("(");
                    if (index > 0) {
                        int index1 = field.getDataType().indexOf(",");
                        if (index1 > 0) {
                            String length = field.getDataType().substring(index + 1, index1);
                            field.setLength(Integer.parseInt(length));
                            String scale = field.getDataType().substring(index1 + 1, field.getDataType().indexOf(")"));
                            field.setScale(Integer.parseInt(scale));
                        } else {
                            String length = field.getDataType().substring(index + 1, field.getDataType().indexOf(")"));
                            field.setLength(Integer.parseInt(length));
                        }
                        field.setDataType(field.getDataType().substring(0, index));
                    }
                    Optional<TableInfo> opt = tableInfoList.stream().filter(t -> t.getTableName().equals(field.getTableName())).findFirst();
                    if (opt.isPresent()) {
                        // 新增表的字段
                        field.setTableId(opt.get().getId());
                        addFields.add(field);

                    } else {
                        // 已存在的表的字段
                        Optional<TableInfo> ext = existTableInfoList.stream()
                                .filter(t -> t.getTableName().equals(field.getTableName()))
                                .findFirst();
                        if (ext.isPresent()) {
                            field.setTableId(ext.get().getId());
                            Optional<TableFieldInfo> extField = fieldList.stream()
                                    .filter(t -> t.getTableId().equals(field.getTableId())
                                            && t.getFieldName().equals(field.getFieldName()))
                                    .findFirst();
                            if (extField.isPresent()) {
                                TableFieldInfo info = extField.get();
                                if (!info.getDataType().equals(field.getDataType())
                                        || info.getLength() != field.getLength()
                                        || info.getScale() != field.getScale()
                                        || info.isPrimaryKey() != field.isPrimaryKey()
                                        || !info.getComment().equals(field.getComment())) {
                                    info.setDataType(field.getDataType());
                                    info.setLength(field.getLength());
                                    info.setScale(field.getScale());
                                    info.setPrimaryKey(field.isPrimaryKey());
                                    info.setComment(field.getComment());
                                    // 产品确认，每次同步都用注释覆盖中文字段（注释不为空）
                                    if (StringUtils.isNotBlank(field.getComment())) {
                                        info.setFieldAlias(field.getComment());
                                    }
                                    updateFields.add(info);
                                }
                            } else {
                                addFields.add(field);
                            }
                        }
                    }
                });

                if (!addFields.isEmpty()) {
                    fieldInfoService.saveBatch(addFields);
                }
                if (!updateFields.isEmpty()) {
                    fieldInfoService.updateBatchById(updateFields);
                }
                if (!ObjectUtils.isEmpty(fieldInfoList) && !ObjectUtils.isEmpty(fieldList)) {
                    List<Long> deleteFields = fieldList.stream().filter(t -> !fieldInfoList.stream().anyMatch(a -> t.getTableId().equals(a.getTableId()) && a.getFieldName().equals(t.getFieldName()))).map(t -> t.getId()).collect(Collectors.toList());
                    if (!ObjectUtils.isEmpty(deleteFields)) {
                        fieldInfoService.removeByIds(deleteFields);
                    }
                }
            }
        });

        log.info("-----------------------------------同步表基础信息结束-----------------------------------");
    }


    private void syncHive(DataConnection dataConnection, DataDir dataDir, Date now) {
        String hiveTableInfoSql = "SHOW TABLES";
        String hiveFieldInfoSql = "DESC %s";

        // 查询表信息
        final List<Map<String, Object>> tables = dataConnection.getJdbcOperations().queryForList(hiveTableInfoSql);
        if (CollectionUtils.isEmpty(tables)) {
            return;
        }
        // 解决 SHOW TABLES 返回字段名称不一样的问题
        String key = "tab_name";

        // 新增的表信息
        List<TableInfo> newTableInfoList = new ArrayList<>(tables.size());

        // 已存在的表
        List<TableInfo> existTableInfoList = tableInfoService.list(
                Wrappers.lambdaQuery(TableInfo.class).eq(TableInfo::getDirId, dataDir.getId()));
        tables.stream().filter(table -> existTableInfoList.stream()
                .noneMatch(existTable -> existTable.getTableName().equals(table.get(key))))
                .forEach(table -> {
                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setTableName((String) table.get(key));
                    tableInfo.setTableAlias(tableInfo.getTableName());
                    tableInfo.setStatus(1);
                    tableInfo.setDirId(dataDir.getId());
                    tableInfo.setCreateTime(now);
                    newTableInfoList.add(tableInfo);
                });

        existTableInfoList.forEach(t -> {
            Optional<Map<String, Object>> opt = tables.stream()
                    .filter(n -> t.getTableName().equals(n.get(key)))
                    .findFirst();

            if (opt.isPresent()) {
                if (t.getStatus() == 0) {
                    // 将已删除的表，恢复正常。
                    tableInfoService.update(Wrappers.<TableInfo>update().set("status", 1).eq("id", t.getId()));
                }
            } else {
                // 将已删除的表，标志为删除。
                tableInfoService.update(Wrappers.<TableInfo>update().set("status", 0).eq("id", t.getId()));
                userTableService.remove(Wrappers.<SysUserTable>query().eq("table_id", t.getId()));
            }
        });
        // 批量保存新增的表信息
        if (CollectionUtils.isNotEmpty(newTableInfoList)) {
            tableInfoService.saveBatch(newTableInfoList);
        }


        // 表字段信息
        List<TableFieldInfo> existFieldList = fieldInfoService.list(Wrappers.<TableFieldInfo>query()
                .eq("dir_id", dataDir.getId()));


        tables.forEach(table -> {
            final String tabName = (String) table.get(key);
            String fieldSql = String.format(hiveFieldInfoSql, dataConnection.getSchema() + "." + tabName);
            final List<Map<String, Object>> fieldInfoList = dataConnection.getJdbcOperations().queryForList(fieldSql);
            if (CollectionUtils.isNotEmpty(fieldInfoList)) {

                List<TableFieldInfo> newFieldList = new ArrayList<>();
                List<TableFieldInfo> addFields = new ArrayList<>();
                List<TableFieldInfo> updateFields = new ArrayList<>();

                AtomicBoolean isField = new AtomicBoolean(true);
                fieldInfoList.forEach(field -> {
                    final String colName = (String) field.get("col_name");
                    final String dataType = (String) field.get("data_type");
                    final String comment = (String) field.get("comment");

                    // 判断是否是字段（有可能是描述信息）
                    if (StringUtils.isBlank(colName) || !isField.get()) {
                        isField.set(false);
                        return;
                    }

                    // 字段信息
                    TableFieldInfo tableFieldInfo = new TableFieldInfo();
                    tableFieldInfo.setFieldName(colName);
                    tableFieldInfo.setFieldAlias(StringUtils.isEmpty(comment) ? tableFieldInfo.getFieldName() : comment);
                    tableFieldInfo.setDataType(dataType);
                    tableFieldInfo.setLength(0);
                    tableFieldInfo.setScale(0);
                    tableFieldInfo.setPrimaryKey(false);
                    tableFieldInfo.setEmpty(false);
                    tableFieldInfo.setComment(comment);
                    tableFieldInfo.setTableId(0L);
                    tableFieldInfo.setTableName(tabName);
                    tableFieldInfo.setDirId(dataDir.getId());
                    tableFieldInfo.setCreateTime(now);
                    tableFieldInfo.setCreateUserId(0L);
                    tableFieldInfo.setUpdateTime(now);
                    tableFieldInfo.setUpdateUserId(0L);
                    tableFieldInfo.setStatus(true);

                    Optional<TableInfo> opt = newTableInfoList.stream()
                            .filter(t -> t.getTableName().equals(tableFieldInfo.getTableName()))
                            .findFirst();
                    if (opt.isPresent()) {
                        // 新增表的字段
                        tableFieldInfo.setTableId(opt.get().getId());
                        addFields.add(tableFieldInfo);

                    } else {
                        // 已存在表的字段
                        Optional<TableInfo> ext = existTableInfoList.stream()
                                .filter(t -> t.getTableName().equals(tableFieldInfo.getTableName()))
                                .findFirst();
                        if (ext.isPresent()) {
                            tableFieldInfo.setTableId(ext.get().getId());
                            Optional<TableFieldInfo> extField = existFieldList.stream()
                                    .filter(t -> t.getTableId().equals(tableFieldInfo.getTableId())
                                            && t.getFieldName().equals(tableFieldInfo.getFieldName()))
                                    .findFirst();
                            if (extField.isPresent()) {
                                TableFieldInfo info = extField.get();
                                if (!info.getDataType().equals(tableFieldInfo.getDataType())
                                        || info.getLength() != tableFieldInfo.getLength()
                                        || info.getScale() != tableFieldInfo.getScale()) {
                                    info.setDataType(tableFieldInfo.getDataType());
                                    info.setLength(tableFieldInfo.getLength());
                                    info.setScale(tableFieldInfo.getScale());
                                    updateFields.add(info);
                                }
                            } else {
                                addFields.add(tableFieldInfo);
                            }
                        }
                    }

                    newFieldList.add(tableFieldInfo);
                });


                if (!addFields.isEmpty()) {
                    fieldInfoService.saveBatch(addFields);
                }
                if (!updateFields.isEmpty()) {
                    fieldInfoService.updateBatchById(updateFields);
                }
                if (!ObjectUtils.isEmpty(newTableInfoList) && !ObjectUtils.isEmpty(existFieldList)) {
                    List<Long> deleteFields = existFieldList.stream()
                            .filter(t -> newFieldList.stream()
                                    .noneMatch(a -> t.getTableId().equals(a.getTableId())
                                            && a.getFieldName().equals(t.getFieldName())))
                            .map(TableFieldInfo::getId)
                            .collect(Collectors.toList());
                    if (!ObjectUtils.isEmpty(deleteFields)) {
                        fieldInfoService.removeByIds(deleteFields);
                    }
                }
            }
        });
    }


    private void syncImpala(DataConnection dataConnection, DataDir dataDir, Date now) {
        String impalaTableInfoSql = "SHOW TABLES";
        String impalaFieldInfoSql = "DESCRIBE %s";

        // 查询表信息
        final List<Map<String, Object>> tables = dataConnection.getJdbcOperations().queryForList(impalaTableInfoSql);
        if (CollectionUtils.isEmpty(tables)) {
            return;
        }
        // 解决 SHOW TABLES 返回字段名称不一样的问题
        String key = "name";

        // 新增的表信息
        List<TableInfo> newTableInfoList = new ArrayList<>(tables.size());

        // 已存在的表
        List<TableInfo> existTableInfoList = tableInfoService.list(
                Wrappers.lambdaQuery(TableInfo.class).eq(TableInfo::getDirId, dataDir.getId()));
        tables.stream().filter(table -> existTableInfoList.stream()
                .noneMatch(existTable -> existTable.getTableName().equals(table.get(key))))
                .forEach(table -> {
                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setTableName((String) table.get(key));
                    tableInfo.setTableAlias(tableInfo.getTableName());
                    tableInfo.setStatus(1);
                    tableInfo.setDirId(dataDir.getId());
                    tableInfo.setCreateTime(now);
                    newTableInfoList.add(tableInfo);
                });

        existTableInfoList.forEach(t -> {
            Optional<Map<String, Object>> opt = tables.stream()
                    .filter(n -> t.getTableName().equals(n.get(key)))
                    .findFirst();

            if (opt.isPresent()) {
                if (t.getStatus() == 0) {
                    // 将已删除的表，恢复正常。
                    tableInfoService.update(Wrappers.<TableInfo>update().set("status", 1).eq("id", t.getId()));
                }
            } else {
                // 将已删除的表，标志为删除。
                tableInfoService.update(Wrappers.<TableInfo>update().set("status", 0).eq("id", t.getId()));
                userTableService.remove(Wrappers.<SysUserTable>query().eq("table_id", t.getId()));
            }
        });
        // 批量保存新增的表信息
        if (CollectionUtils.isNotEmpty(newTableInfoList)) {
            tableInfoService.saveBatch(newTableInfoList);
        }


        // 表字段信息
        List<TableFieldInfo> existFieldList = fieldInfoService.list(Wrappers.<TableFieldInfo>query()
                .eq("dir_id", dataDir.getId()));


        tables.forEach(table -> {
            final String tabName = (String) table.get(key);
            String fieldSql = String.format(impalaFieldInfoSql, dataConnection.getSchema() + "." + tabName);
            final List<Map<String, Object>> fieldInfoList = dataConnection.getJdbcOperations().queryForList(fieldSql);
            if (CollectionUtils.isNotEmpty(fieldInfoList)) {

                List<TableFieldInfo> newFieldList = new ArrayList<>();
                List<TableFieldInfo> addFields = new ArrayList<>();
                List<TableFieldInfo> updateFields = new ArrayList<>();

                AtomicBoolean isField = new AtomicBoolean(true);
                fieldInfoList.forEach(field -> {
                    final String colName = (String) field.get("name");
                    final String dataType = (String) field.get("type");
                    final String comment = (String) field.get("comment");

                    // 判断是否是字段（有可能是描述信息）
                    if (StringUtils.isBlank(colName) || !isField.get()) {
                        isField.set(false);
                        return;
                    }

                    // 字段信息
                    TableFieldInfo tableFieldInfo = new TableFieldInfo();
                    tableFieldInfo.setFieldName(colName);
                    tableFieldInfo.setFieldAlias(StringUtils.isEmpty(comment) ? tableFieldInfo.getFieldName() : comment);
                    tableFieldInfo.setDataType(dataType);
                    tableFieldInfo.setLength(0);
                    tableFieldInfo.setScale(0);
                    tableFieldInfo.setPrimaryKey(false);
                    tableFieldInfo.setEmpty(false);
                    tableFieldInfo.setComment(comment);
                    tableFieldInfo.setTableId(0L);
                    tableFieldInfo.setTableName(tabName);
                    tableFieldInfo.setDirId(dataDir.getId());
                    tableFieldInfo.setCreateTime(now);
                    tableFieldInfo.setCreateUserId(0L);
                    tableFieldInfo.setUpdateTime(now);
                    tableFieldInfo.setUpdateUserId(0L);
                    tableFieldInfo.setStatus(true);

                    Optional<TableInfo> opt = newTableInfoList.stream()
                            .filter(t -> t.getTableName().equals(tableFieldInfo.getTableName()))
                            .findFirst();
                    if (opt.isPresent()) {
                        // 新增表的字段
                        tableFieldInfo.setTableId(opt.get().getId());
                        addFields.add(tableFieldInfo);

                    } else {
                        // 已存在表的字段
                        Optional<TableInfo> ext = existTableInfoList.stream()
                                .filter(t -> t.getTableName().equals(tableFieldInfo.getTableName()))
                                .findFirst();
                        if (ext.isPresent()) {
                            tableFieldInfo.setTableId(ext.get().getId());
                            Optional<TableFieldInfo> extField = existFieldList.stream()
                                    .filter(t -> t.getTableId().equals(tableFieldInfo.getTableId())
                                            && t.getFieldName().equals(tableFieldInfo.getFieldName()))
                                    .findFirst();
                            if (extField.isPresent()) {
                                TableFieldInfo info = extField.get();
                                if (!info.getDataType().equals(tableFieldInfo.getDataType())
                                        || info.getLength() != tableFieldInfo.getLength()
                                        || info.getScale() != tableFieldInfo.getScale()) {
                                    info.setDataType(tableFieldInfo.getDataType());
                                    info.setLength(tableFieldInfo.getLength());
                                    info.setScale(tableFieldInfo.getScale());
                                    updateFields.add(info);
                                }
                            } else {
                                addFields.add(tableFieldInfo);
                            }
                        }
                    }

                    newFieldList.add(tableFieldInfo);
                });


                if (!addFields.isEmpty()) {
                    fieldInfoService.saveBatch(addFields);
                }
                if (!updateFields.isEmpty()) {
                    fieldInfoService.updateBatchById(updateFields);
                }
                if (!ObjectUtils.isEmpty(newTableInfoList) && !ObjectUtils.isEmpty(existFieldList)) {
                    List<Long> deleteFields = existFieldList.stream()
                            .filter(t -> newFieldList.stream()
                                    .noneMatch(a -> t.getTableId().equals(a.getTableId())
                                            && a.getFieldName().equals(t.getFieldName())))
                            .map(TableFieldInfo::getId)
                            .collect(Collectors.toList());
                    if (!ObjectUtils.isEmpty(deleteFields)) {
                        fieldInfoService.removeByIds(deleteFields);
                    }
                }
            }
        });
    }

    /**
     * 设置百分比的数据
     *
     * @param list      需要计算百分比的数据列表
     * @param totalData 总数
     * @author linkaiwei
     * @date 2021-12-06 10:06:17
     * @since 1.6.3
     */
    private void getPercent(List<Map<String, Object>> list, Double totalData) {
        if (totalData != 0) {
            AtomicDouble total = new AtomicDouble();
            AtomicInteger index = new AtomicInteger();
            final int size = list.size();

            list.forEach(sourceDataSizeMap -> {
                final double percentage = new BigDecimal(sourceDataSizeMap.get("value").toString())
                        .divide(BigDecimal.valueOf(totalData), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100))
                        .doubleValue();
                index.getAndIncrement();

                if (index.get() != size) {
                    total.set(total.get() + percentage);
                    sourceDataSizeMap.put("percentage", percentage + "%");

                } else {
                    sourceDataSizeMap.put("percentage", BigDecimal.valueOf(100 - total.get())
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue() + "%");
                }
            });

        } else {
            list.forEach(sourceDataSizeMap -> sourceDataSizeMap.put("percentage", "0%"));
        }
    }


    /**
     * 更新表热度信息
     *
     * @author linkaiwei
     * @date 2022-02-10 11:08:47
     * @since 1.6.4.0
     */
    @Override
    public void updateTableHeat() {
        // 查询每个表的最新变更日志记录时间
        QueryWrapper<TableLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("table_id tableId, max(create_time) createTime");
        queryWrapper.lambda().groupBy(TableLog::getTableId);
        final List<TableLog> tableLogList = tableLogMapper.selectList(queryWrapper);

        if (CollectionUtils.isNotEmpty(tableLogList)) {
            Set<Long> tableIdSet = new HashSet<>();
            Map<Long, Integer> heatMap = new HashMap<>();
            Date now = new Date();
            tableLogList.forEach(tableLog -> {
                final HeatEnum heatEnum = HeatEnum.getHeatEnum(DateUtils.differentDays(tableLog.getCreateTime(), now));
                if (heatEnum != null) {
                    heatMap.put(tableLog.getTableId(), heatEnum.getCode());
                }

                tableIdSet.add(tableLog.getTableId());
            });


            // 更新表的热度信息
            LambdaQueryWrapper<TableInfo> lambdaQueryWrapper = Wrappers.lambdaQuery();
            lambdaQueryWrapper.in(TableInfo::getId, tableIdSet);
            final List<TableInfo> tableInfoList = tableInfoMapper.selectList(lambdaQueryWrapper);
            if (CollectionUtils.isNotEmpty(tableInfoList)) {
                tableInfoList.forEach(tableInfo -> {
                    final Integer heat = heatMap.get(tableInfo.getId());
                    if (heat != null) {
                        tableInfo.setHeat(heat);
                        tableInfo.setUpdateTime(now);
                        tableInfoMapper.updateById(tableInfo);
                    }
                });
            }
        }
    }
}
