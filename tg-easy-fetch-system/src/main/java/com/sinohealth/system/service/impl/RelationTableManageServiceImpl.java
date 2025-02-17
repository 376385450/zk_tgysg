package com.sinohealth.system.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.sinohealth.bi.enums.DatabaseEnum;
import com.sinohealth.common.config.DataConnection;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.LogType;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.poi.easyexcel.DateSetExcelUtil;
import com.sinohealth.common.utils.poi.easyexcel.SqlTypeContext;
import com.sinohealth.system.biz.dict.service.BizDataDictService;
import com.sinohealth.system.biz.table.exception.UpdateFailedException;
import com.sinohealth.system.biz.table.exception.UpdateNoticeException;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TableInfoDiy;
import com.sinohealth.system.domain.TableLog;
import com.sinohealth.system.domain.constant.TableConst;
import com.sinohealth.system.domain.vo.DiyTableUpdateResult;
import com.sinohealth.system.dto.TableCreateDto;
import com.sinohealth.system.dto.TableFieldInfoDto;
import com.sinohealth.system.mapper.RelationTableManageMapper;
import com.sinohealth.system.service.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RelationTableManageServiceImpl
        extends ServiceImpl<RelationTableManageMapper, TableInfoDiy>
        implements RelationTableManageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelationTableManageServiceImpl.class);

    @Autowired
    private ITableInfoService tableInfoService;
    @Autowired
    private ITableFieldInfoService fieldInfoService;
    @Autowired
    private ITableLogService tableLogService;
    @Autowired
    private BizDataDictService bizDataDictService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Resource(name = "slaveDataSource")
    private DataSource slaveDataSource;
    @Autowired
    private IApplicationService applicationService;

    public static final String F_VARCHAR = "String";
    public static final String F_DOUBLE = "Int32";
    public static final String F_TIMESTAMP = "Date";
    public static final String PRE_TABLE_NAME = "dim_";
    public static final String LOCAL_SUFFIX = "_local";
    public static final String SHARD_SUFFIX = "_shard";

    private String[] numTypes = {"Int8", "Int16", "Int32", "Int64", "Decimal", "Decimal32", "Decimal64", "Decimal128"};

    private String[] dateTypes = {"Date", "DateTime", "DateTime64"};


    @Override
    public List<TableInfoDiy> page(Integer pageNum, Integer pageSize, String name) {
        List<TableInfoDiy> result;
        PageHelper.startPage(pageNum, pageSize);
        QueryWrapper queryWrapper;
        if (StringUtils.isNotEmpty(name)) {
            queryWrapper = Wrappers.<TableInfoDiy>query().eq("status", 1);
        } else {
            queryWrapper = Wrappers.<TableInfoDiy>query().eq("status", 1);
        }
        if (!SecurityUtils.getLoginUser().isAdmin()) {
            queryWrapper.eq("create_by", SecurityUtils.getUserId());
            queryWrapper.orderByDesc("update_time");
        }
        result = this.list(queryWrapper);
        result.forEach(tableInfoDiy -> {
            tableInfoDiy.setTableInfo(tableInfoService.getById(tableInfoDiy.getTableId()));
        });
        if (StringUtils.isNotBlank(name)) {
            result = result.stream().filter(l -> l.getTableInfo().getTableName().contains(name)).collect(Collectors.toList());
        }

        return result;
    }


    @SneakyThrows
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<DiyTableUpdateResult> importData(MultipartFile file, String tableName,
                                                       Long tableId, Boolean ignoreNotice) {
        if (StringUtils.isBlank(tableName) && Objects.isNull(tableId)) {
            throw new CustomException("参数缺失");
        }

        TableInfoDiy tableInfoDiy;
        if (!DateSetExcelUtil.getFileExtension(file)) {
            throw new CustomException("文件名不合法!");
        }
        if (StringUtils.isNotBlank(tableName) && NumberUtil.isNumber(tableName)) {
            throw new CustomException("英文名称格式不能为纯数字!");
        }

        //解析
        List<Map> maps = DateSetExcelUtil.importExcel(Map.class, file.getInputStream(), "yyyy-MM-dd HH:mm:ss");
        if (CollectionUtils.isEmpty(maps)) {
            throw new CustomException("excel解析失败!");
        }

        List<Map> headFieldInfoList = maps.subList(0, 1);
        //数据
        // List<Map> tailFieldInfoList = maps.subList(2, maps.size());
        List<Map> tailFieldInfoList = maps.subList(1, maps.size());
        if (CollectionUtils.isEmpty(tailFieldInfoList)) {
            throw new CustomException("关联表Excel内无数据");
        }

        String originalFilename = file.getOriginalFilename();
        String lockKey = null;
        Boolean tryLock = null;
        try {
            if (StringUtils.isNotNull(tableId)) {
                final TableInfo info = tableInfoService.getById(tableId);
                if (Objects.isNull(info)) {
                    return AjaxResult.error("关联表不存在");
                }
                lockKey = RedisKeys.getModifyTableLock(info.getTableName());
                tryLock = redisTemplate.opsForValue().setIfAbsent(lockKey, Duration.ofHours(1));
                if (BooleanUtils.isNotTrue(tryLock)) {
                    return AjaxResult.error("勿重复或多人一起操作");
                }
                tableInfoDiy = this.updateTable(originalFilename, info, headFieldInfoList, tailFieldInfoList, ignoreNotice);
            } else {
                lockKey = RedisKeys.getModifyTableLock(tableName);
                tryLock = redisTemplate.opsForValue().setIfAbsent(lockKey, Duration.ofHours(1));
                if (BooleanUtils.isNotTrue(tryLock)) {
                    return AjaxResult.error("勿重复或多人一起操作");
                }
                tableInfoDiy = this.createTable(originalFilename, tableName, headFieldInfoList, tailFieldInfoList);
            }
            return AjaxResult.success(new DiyTableUpdateResult(tableInfoDiy));
        } catch (UpdateFailedException ue) {
            return AjaxResult.success(new DiyTableUpdateResult(ue.code, ue.getMessage()));
        } catch (UpdateNoticeException ne) {
            return AjaxResult.success(new DiyTableUpdateResult(ne.code, ne.getMessage()));
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error(e.getMessage());
        } finally {
            if (BooleanUtils.isTrue(tryLock)) {
                redisTemplate.delete(lockKey);
            }
        }
    }


    public void check(TableCreateDto tableCreateDto,
                      Long tableId,
                      String tableName, Boolean ignoreNotice) {
        boolean inUsed = applicationService.hasTableUsedInApplication(tableName);
        final List<TableFieldInfo> existFields = fieldInfoService.getFieldsByTableId(tableId);
        final Map<String, String> existTypeMap = existFields.stream().collect(Collectors.toMap(TableFieldInfo::getFieldName,
                TableFieldInfo::getFieldType, (front, current) -> current));

        final Map<String, String> expectTypeMap = tableCreateDto.getFields().stream().collect(Collectors.toMap(TableFieldInfoDto::getFieldName,
                TableFieldInfoDto::getFieldType, (front, current) -> current));
        List<String> loseFields = new ArrayList<>();
        List<String> diffFields = new ArrayList<>();
        for (Map.Entry<String, String> entry : existTypeMap.entrySet()) {
            final String value = entry.getValue();
            final String type = expectTypeMap.get(entry.getKey());
            if (Objects.isNull(type)) {
                loseFields.add(entry.getKey());
            } else if (!Objects.equals(type, value)) {
                diffFields.add(entry.getKey());
            }
        }

        if (CollectionUtils.isNotEmpty(loseFields) || CollectionUtils.isNotEmpty(diffFields)) {
            List<String> errorMsg = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(loseFields)) {
                errorMsg.add(String.format("当前表较于旧表缺失的字段：" + String.join("、", loseFields)) + "。");
            }
            if (CollectionUtils.isNotEmpty(diffFields)) {
                errorMsg.add(String.format("当前表较于旧表类型改变的字段：" + String.join("、", diffFields)) + "。");
            }
            StringBuilder msg = new StringBuilder();
            for (int i = 0; i < errorMsg.size(); i++) {
                msg.append((i + 1)).append(".").append(errorMsg.get(i)).append("<br/>");
            }

            if (inUsed) {
                throw new UpdateFailedException("当前表单被引用，更新操作只允许增加表头字段。<br/>" + msg);
            } else if (BooleanUtils.isNotTrue(ignoreNotice)) {
                throw new UpdateNoticeException(msg.toString());
            }
        }
    }

    private String extractOriginName(String localName) {
        final String substring = localName.substring(4);
        return substring.substring(0, substring.length() - 6);
    }

    @Transactional(rollbackFor = Exception.class)
    public TableInfoDiy updateTable(String originalFilename,
                                    TableInfo info,
                                    List<Map> headFieldInfoList,
                                    List<Map> tailFieldInfoList,
                                    Boolean ignoreNotice) throws SQLException {
        TableInfoDiy tableInfoDiy;
        try {
            final String tableName = this.extractOriginName(info.getTableName());
            TableCreateDto tableCreateDto = this.buildFieldInfoData(tableName, headFieldInfoList);

            this.check(tableCreateDto, info.getId(), info.getTableName(), ignoreNotice);

            this.createTableMetaData(tableCreateDto, true);
            //插入数据
            tableInfoDiy = updateTableInfo(tableName, tailFieldInfoList, tableCreateDto, originalFilename);
        } catch (Exception e) {
            LOGGER.error("create table error", e);
            if (e instanceof CustomException || e instanceof UpdateFailedException || e instanceof UpdateNoticeException) {
                throw e;
            }
            if (e.getMessage().contains("TABLE_ALREADY_EXISTS")) {
                throw new CustomException("数据表名已存在, 请确认并重命名");
            }
            throw new CustomException("操作数据库失败!");
        } finally {
            SqlTypeContext.clear();
        }
        return tableInfoDiy;
    }

    private TableInfoDiy updateTableInfo(String tableName, List<Map> tailFieldInfoList,
                                         TableCreateDto tableCreateDto, String originalFilename) throws SQLException {
        int row = insertAll(PRE_TABLE_NAME + tableName + LOCAL_SUFFIX, tailFieldInfoList);
        if (row < 0) {
            throw new CustomException("插入数据行失败!");
        }

        //处理关联表管理
        TableInfoDiy tableInfoDiy = this.getOne(Wrappers.<TableInfoDiy>query().eq("table_id", tableCreateDto.getTableId()));
        if (tableInfoDiy == null) {
            tableInfoDiy = new TableInfoDiy();
            tableInfoDiy.setCreateBy(SecurityUtils.getUserId());
            tableInfoDiy.setCreateTime(new Date());
        } else {
            tableInfoDiy.setUpdateBy(SecurityUtils.getUserId());
            tableInfoDiy.setUpdateTime(new Date());
        }
        tableInfoDiy.setName(originalFilename);
        tableInfoDiy.setStatus(1);
        tableInfoDiy.setTableId(tableCreateDto.getTableId());
        this.saveOrUpdate(tableInfoDiy);
        return tableInfoDiy;
    }

    @Transactional(rollbackFor = Exception.class)
    public TableInfoDiy createTable(String originalFilename,
                                    String tableName,
                                    List<Map> headFieldInfoList,
                                    List<Map> tailFieldInfoList) throws SQLException {
        TableInfoDiy tableInfoDiy;
        try {
            //建表
            TableCreateDto tableCreateDto = this.buildFieldInfoData(tableName, headFieldInfoList);
            this.createTableMetaData(tableCreateDto, false);
            //插入数据
            int row = insertAll(PRE_TABLE_NAME + tableName + LOCAL_SUFFIX, tailFieldInfoList);
            if (row < 0) {
                throw new CustomException("插入数据行失败!");
            }

            tableInfoDiy = updateTableInfo(tableName, tailFieldInfoList, tableCreateDto, originalFilename);
        } catch (Exception e) {
            LOGGER.error("create table error", e);
            if (e instanceof CustomException) {
                throw e;
            }
            if (e.getMessage().contains("TABLE_ALREADY_EXISTS")) {
                throw new CustomException("数据表名已存在, 请确认并重命名");
            }
            throw new CustomException("操作数据库失败!");
        } finally {
            SqlTypeContext.clear();
        }
        return tableInfoDiy;
    }


    private TableCreateDto buildFieldInfoData(String tableName, List<Map> fieldInfoList) {
        List<TableFieldInfoDto> tableFieldInfoDtoList = new ArrayList<>(fieldInfoList.size());

        Map<String, Object> fieldInfoMap = fieldInfoList.get(0);
        for (Map.Entry<String, Object> entry : fieldInfoMap.entrySet()) {
            boolean primaryStatus = false;
            boolean emptyStatus = true;
            String key = entry.getKey();
            String value = (String) entry.getValue();
            TableFieldInfoDto tableFieldInfoDto = new TableFieldInfoDto();
            //数字长度：32位
            //字符串长度:500
            //小数点位数：保留后4位
            String dataType = (String) SqlTypeContext.get(key);

            int dataLength = 0;
            if (DateSetExcelUtil.F_VARCHAR.equals(dataType)) {
                //dataLength = 500;
                dataType = F_VARCHAR;
            } else if (DateSetExcelUtil.F_DOUBLE.equals(dataType) || DateSetExcelUtil.F_INT.equals(dataType)) {
                //dataLength = 32;
                //tableFieldInfoDto.setScale(4);
                dataType = F_DOUBLE;
            } else {
                dataType = F_TIMESTAMP;
            }
            tableFieldInfoDto.setFieldName(key);
            tableFieldInfoDto.setFieldAlias(value);
            tableFieldInfoDto.setDataType(dataType);
            tableFieldInfoDto.setComment(value);
            tableFieldInfoDto.setEmpty(emptyStatus);
            tableFieldInfoDto.setLength(dataLength);
            tableFieldInfoDto.setPrimaryKey(primaryStatus);
            //数值型的就是 指标;其他的就是纬度
            if (Arrays.asList(numTypes).contains(dataType)) {
                tableFieldInfoDto.setFieldType("数值");
                tableFieldInfoDto.setDimIndex(TableConst.DimIndexType.METRIC);
            } else if (Arrays.asList(dateTypes).contains(dataType)) {
                tableFieldInfoDto.setFieldType("日期");
                tableFieldInfoDto.setDimIndex(TableConst.DimIndexType.DIMENSIONS);
            } else {
                tableFieldInfoDto.setFieldType("文本");
                tableFieldInfoDto.setDimIndex(TableConst.DimIndexType.DIMENSIONS);
            }
            tableFieldInfoDtoList.add(tableFieldInfoDto);
        }

        TableCreateDto dto = new TableCreateDto();
        dto.setTableAlias(tableName);
        dto.setTableName(tableName);
        dto.setComment(tableName);
        dto.setSafeLevel(1);
        dto.setIsDdl(false);
        dto.setFields(tableFieldInfoDtoList);
        return dto;
    }


    /**
     * 创建表 字段 元信息
     */
    public void createTableMetaData(TableCreateDto dto, boolean override) {
        TableInfo tableInfo = tableInfoService.getOne(Wrappers.<TableInfo>query().eq("status", 1)
                .eq("table_name", PRE_TABLE_NAME + dto.getTableName() + LOCAL_SUFFIX));
        if (Objects.nonNull(tableInfo) && !override) {
            throw new CustomException("表名已存在请选择更新操作");
        }
        if (Objects.isNull(tableInfo)) {
            tableInfo = new TableInfo();
        }

        // 查询数据库连接
        DataConnection dataConnection;
        try {
            dataConnection = new DataConnection(slaveDataSource.getConnection().getSchema(), slaveDataSource,
                    new JdbcTemplate(slaveDataSource), DatabaseEnum.CLICKHOUSE.getFeature());
        } catch (SQLException e) {
            log.error("", e);
            throw new CustomException("初始化CK数据库连接失败");
        }
        dto.setSchema(dataConnection.getSchema());
        dto.setDatabaseType(DatabaseEnum.CLICKHOUSE.getFeature());

        // 构建并验证SQL语句
        String sql = this.createTableSqlVerify(dto, dataConnection);

        //构建分布式表sql语句
        String distributedSql = createTableSql4Distributed(dto);

        // 执行SQL语句
        dataConnection.getJdbcOperations().execute(sql);

        dataConnection.getJdbcOperations().execute(distributedSql);

        BeanUtils.copyProperties(dto, tableInfo);
        tableInfo.setTableName(tableInfo.getTableName() + LOCAL_SUFFIX);
        tableInfo.setComment(null);
        tableInfo.setDirId(0L);
        if (org.apache.commons.lang3.StringUtils.isEmpty(tableInfo.getTableAlias())) {
            tableInfo.setTableAlias(tableInfo.getTableName());
        }
        tableInfo.setCreateUserId(SecurityUtils.getUserId());
        tableInfo.setCreateTime(new Date());
        tableInfo.setStatus(1);
        tableInfo.setLeaderName(SecurityUtils.getUsername());
        tableInfo.setViewTotal(10);
        tableInfo.setIsDiy(1);
        tableInfoService.saveOrUpdate(tableInfo);

        Long tableInfoId = tableInfo.getId();
        Long dirId = tableInfo.getDirId();
        dto.setTableId(tableInfoId);
        List<TableFieldInfo> fieldInfoList = new ArrayList<>();

        boolean inUsed = applicationService.hasTableUsedInApplication(tableInfo.getTableName());
        if (!inUsed) {
            log.info("delete old field={}", tableInfoId);
            fieldInfoService.deleteByTableId(tableInfoId);
        }

        dto.getFields().forEach(f -> {
            if (fieldInfoService.getCountByTableId(tableInfoId, f.getFieldName()) == 0) {
                TableFieldInfo fieldInfo = new TableFieldInfo();
                BeanUtils.copyProperties(f, fieldInfo);
                fieldInfo.setStatus(true);
                fieldInfo.setCreateTime(new Date());
                fieldInfo.setCreateUserId(SecurityUtils.getUserId());
                fieldInfo.setTableId(tableInfoId);
                fieldInfo.setDirId(dirId);
                fieldInfoList.add(fieldInfo);
            }
        });

        this.fieldInfoService.saveBatch(fieldInfoList);
        SqlTypeContext.put(SqlTypeContext.TABLE_ID, tableInfo.getId());

        TableLog tableLog = new TableLog();
        tableLog.setTableId(tableInfoId);
        tableLog.setDirId(dirId);
        tableLog.setTableAlias(tableInfo.getTableAlias());
        tableLog.setTableName(tableInfo.getTableName());
        tableLog.setLogType(LogType.table_create.getVal());
        tableLog.setCreateTime(new Date());
        tableLog.setUpdateCount(1);
        tableLog.setDataCount(0);
        tableLog.setOperatorId(SecurityUtils.getUserId());
        tableLog.setOperator(SecurityUtils.getUsername());
        tableLog.setContent("创建表" + tableInfo.getTableName());
        tableLogService.save(tableLog);
    }

    protected int insertAll(String tableName, List<Map> datas) throws SQLException {
        /*影响的行数*/
        int affectRowCount = -1;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            /*从数据库连接池中获取数据库连接*/
            connection = new DataConnection(tableName, slaveDataSource, new JdbcTemplate(slaveDataSource), DatabaseEnum.CLICKHOUSE.getFeature()).getDataSource().getConnection();

            Map<String, Object> valueMap = datas.get(0);
            /*获取数据库插入的Map的键值对的值*/
            Set<String> keySet = valueMap.keySet();
            Iterator<String> iterator = keySet.iterator();
            /*要插入的字段sql，其实就是用key拼起来的*/
            StringBuilder columnSql = new StringBuilder();
            /*要插入的字段值，其实就是？*/
            StringBuilder unknownMarkSql = new StringBuilder();
            Object[] keys = new Object[valueMap.size()];
            int i = 0;
            while (iterator.hasNext()) {
                String key = iterator.next();
                keys[i] = key;
                columnSql.append(i == 0 ? "" : ",");
                columnSql.append(key);

                unknownMarkSql.append(i == 0 ? "" : ",");
                unknownMarkSql.append("?");
                i++;
            }
            /*开始拼插入的sql语句*/
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ");
            sql.append(tableName);
            sql.append(" (");
            sql.append(columnSql);
            sql.append(" )  VALUES (");
            sql.append(unknownMarkSql);
            sql.append(" )");

            /*执行SQL预编译*/
            preparedStatement = connection.prepareStatement(sql.toString());
            /*设置不自动提交，以便于在出现异常的时候数据库回滚*/
            connection.setAutoCommit(false);
//            System.out.println(sql);
            log.info("sql={}", sql);
            for (int j = 0; j < datas.size(); j++) {
                for (int k = 0; k < keys.length; k++) {
                    preparedStatement.setObject(k + 1, datas.get(j).get(keys[k]));
                }
                preparedStatement.addBatch();
            }
            int[] arr = preparedStatement.executeBatch();
            connection.commit();
            affectRowCount = arr.length;
            LOGGER.info("成功了插入了:{}行", affectRowCount);
        } catch (Exception e) {
            if (connection != null) {
                connection.rollback();
            }
            LOGGER.error("插入数据失败!", e);
            //throw new CustomException(String.format("第{%d}行数据插入失败", affectRowCount));
            throw new CustomException(String.format("数据插入失败:{%s}", e.getMessage()));
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return affectRowCount;
    }

    public String createTableSqlVerify(TableCreateDto dto, DataConnection dataConnection) {
        dto.setTableName(PRE_TABLE_NAME + dto.getTableName());
        TableInfo table = tableInfoService.getOne(Wrappers.<TableInfo>query().eq("status", 1).eq("table_name", dto.getTableName() + LOCAL_SUFFIX));
        if (table != null) {
            String exeSql1 = "DROP table if exists " + dto.getTableName() + LOCAL_SUFFIX + " on cluster default_cluster";
            String exeSql2 = "DROP table if exists " + dto.getTableName() + SHARD_SUFFIX + " on cluster default_cluster";
            dataConnection.getJdbcOperations().execute(exeSql1);
            dataConnection.getJdbcOperations().execute(exeSql2);
        }

        String sql = dto.getIsDdl() ? dto.getDdl() : this.generateCreateTableSql(dto);
        if (org.apache.commons.lang3.StringUtils.isEmpty(sql)) {
            throw new CustomException("sql为空");
        }
        if (!sql.trim().toLowerCase().startsWith("create")) {
            throw new CustomException("仅支持建表sql");
        }
        LOGGER.info(sql);

        return sql;
    }

    private String generateCreateTableSql(TableCreateDto dto) {

        StringBuffer createSql = new StringBuffer();
        createSql.append("create table ");
        createSql.append(dto.getTableName() + LOCAL_SUFFIX);
        createSql.append(" on cluster default_cluster");
        createSql.append(" (");

        String primaryKey = dto.getFields().get(0).getFieldName();
        int len = dto.getFields().size();
        for (int i = 0; i < len; i++) {
            TableFieldInfoDto field = dto.getFields().get(i);
            createSql.append(String.format(" `%s` ", field.getFieldName()));
            createSql.append(field.getDataType());

            if (!org.apache.commons.lang3.StringUtils.isEmpty(field.getComment())) {
                createSql.append(String.format(" COMMENT '%s' ", field.getComment()));
            }

            if (i < len - 1) {
                createSql.append(",");
            }

        }
        createSql.append(" ) ENGINE = ReplicatedMergeTree('/clickhouse/table/{shard}/" + dto.getSchema() + "/" + dto.getTableName() + LOCAL_SUFFIX + "',\n" +
                " '{replica}') order by ").append(primaryKey);

        return createSql.toString();
    }

    private String createTableSql4Distributed(TableCreateDto dto) {

        String db = dto.getSchema();
        String tableName = dto.getTableName() + SHARD_SUFFIX;
        StringBuffer createSql = new StringBuffer();
        createSql.append("create table ");
        createSql.append(db).append(".");
        createSql.append(tableName);
        createSql.append(" on cluster default_cluster as ");
        createSql.append(db).append(".");
        createSql.append(dto.getTableName() + LOCAL_SUFFIX);
        createSql.append(" ENGINE=Distributed(");
        createSql.append(String.format(" '%s',  '%s', '%s', %s ", "default_cluster", dto.getSchema(), dto.getTableName() + LOCAL_SUFFIX, "rand()"));
        createSql.append("); ");
        dto.setTableNameDistributed(tableName);

        return createSql.toString();
    }


}
