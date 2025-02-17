package com.sinohealth.system.service.impl;

import cn.hutool.core.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.bi.data.ClickHouse;
import com.sinohealth.bi.data.Filter;
import com.sinohealth.bi.data.Table;
import com.sinohealth.bi.enums.DatabaseEnum;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.config.DataConnection;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.AssetPermissionType;
import com.sinohealth.common.enums.SortingFieldEnum;
import com.sinohealth.common.enums.monitor.SecondSubjectTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dir.service.AssetsSortService;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.ckpg.SelfCKProperties;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.domain.constant.TableConst;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.TableBaseInfoDto;
import com.sinohealth.system.dto.TableDataDto;
import com.sinohealth.system.dto.TableInfoDto;
import com.sinohealth.system.dto.TableMappingDTO;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.table_manage.DataManageDto;
import com.sinohealth.system.dto.table_manage.DataManageFormDto;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.DataDirMapper;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import com.sinohealth.system.monitor.event.EventReporterUtil;
import com.sinohealth.system.service.DataManageService;
import com.sinohealth.system.service.IAssetService;
import com.sinohealth.system.service.IDataDirService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ITableFieldInfoService;
import com.sinohealth.system.util.ApplicationSqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service("dataManageService")
public class DataManageServiceImpl implements DataManageService {

    @Autowired
    private DataDirMapper dataDirMapper;
    @Autowired
    private TgApplicationInfoMapper applicationInfoMapper;
    @Autowired
    private TgTemplateInfoMapper templateInfoMapper;

    @Autowired
    private TableInfoServiceImpl tableInfoService;
    @Autowired
    private ITableFieldInfoService fieldInfoService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private IDataDirService dataDirService;
    @Autowired
    private AssetsSortService assetsSortService;
    @Autowired
    private IAssetService assetService;

    @Resource(name = "slaveDataSource")
    private DataSource slaveDataSource;
    @Autowired
    private SelfCKProperties selfCKProperties;
    @Autowired
    private AppProperties appProperties;

    private final String[] numTypes = {"Int8", "Int16", "Int32", "Int64", "Decimal", "Decimal32", "Decimal64", "Decimal128"};
    private final String[] dateTypes = {"Date", "DateTime", "DateTime64"};

    @Override
    public DataManageDto getTree(String name, Integer menu) {
        List<DataDir> list;
        if (StringUtils.isNotEmpty(name)) {
            List<Long> ids = dataDirMapper.getDirIdsByName(name, CommonConstants.DATA_DIR, null);
            if (ids != null && ids.size() > 0) {
                //查找子节点的所有父节点包括自身
                List<Long> allNodeIds = new ArrayList<>();
                for (Long id : ids) {
                    allNodeIds.addAll(dataDirMapper.getAllNodeIds(id));
                }
                list = dataDirMapper.selectBatchIds(allNodeIds);
            } else {
                list = new ArrayList<>();
            }

        } else {
            list = dataDirMapper.getDataDirsByTarget(CommonConstants.DATA_DIR, null);
        }

        return initDataManageDto(list, menu);
    }

    private AtomicInteger counter = new AtomicInteger();
    private Set<Long> existIds = new HashSet<>();

    private DataManageDto initDataManageDto(List<DataDir> list, Integer menu) {
        DataManageDto dto = new DataManageDto();
        dto.setId(0L);
        dto.setDirName("根节点");
        dto.setDataManageFormDtos(new ArrayList<>());


        findChildren(list, dto, null, menu);
        System.out.println(counter);
        counter.set(0);
        existIds.clear();
        // 没有关联目录的表罗列在下面
        List<DataManageFormDto> tablesWithoutDir = tableInfoService.getTablesWithoutDir(menu);
//        if (isTableMenu(menu) && isNotManager()) {
//            List<DataManageFormDto> temp = tablesWithoutDir.stream().filter(d->StringUtils.isNotBlank(d.getLeaderName())
//                    && d.getLeaderName().equals(ThreadContextHolder.getSysUser().getUserName())).collect(Collectors.toList());
//            tablesWithoutDir.clear();
//            tablesWithoutDir.addAll(temp);
//        }
        dto.getChildren().addAll(new ArrayList<DataManageDto>() {{
            add(new DataManageDto() {{
                setId(Long.MAX_VALUE);
                setDirName("未绑定目录");
                setDataManageFormDtos(tablesWithoutDir);
            }});
        }});
        return dto;
    }

    private boolean isTableMenu(Integer menu) {
        return menu == CommonConstants.TABLE_MANAGEMENT;
    }

    private boolean isNotManager() {
        return ThreadContextHolder.getSysUser().getRoleName() == null
                || !ThreadContextHolder.getSysUser().getRoleName().contains("管理员");
    }

    @Override
    public List<Long> getDirList() {
        List<DataDir> list = dataDirMapper.selectList(null);
        return list.stream().map(DataDir::getId).collect(Collectors.toList());
    }

    private void findChildren(List<DataDir> list, DataManageDto dto, List<DataManageDto> endDirList, Integer menu) {
        List<DataManageDto> children = list.stream().filter(d -> d.getParentId().equals(dto.getId())).map(d -> {
                    DataManageDto child = new DataManageDto();
                    BeanUtils.copyProperties(d, child);
                    child.setDataManageFormDtos(tableInfoService.getListByDirId(d.getId(), menu));
                    findChildren(list, child, endDirList, menu);
                    return child;
                }
        ).sorted(Comparator.comparing(DataManageDto::getSort)).collect(Collectors.toList());

        if (endDirList != null && children.isEmpty()) {
            endDirList.add(dto);
        }
        dto.setChildren(children);
    }

    @Override
    public TableDataDto getTableData(Long tableId, GetDataInfoRequestDTO requestDTO) {
        final String fieldIds = requestDTO.getFieldIds();
        final String sortingField = requestDTO.getSortingField();
        final String sortBy = requestDTO.getSortBy();
        final FilterDTO filter = requestDTO.getFilter();

        TableInfo tableInfo = tableInfoService.getById(tableId);
        if (Objects.isNull(tableInfo)) {
            throw new CustomException("表单不存在!");
        }

        QueryWrapper<TableFieldInfo> wrapper = Wrappers.<TableFieldInfo>query()
                .eq("table_id", tableId)
                .eq("status", 1)
                .orderByAsc("sort");
        if (!StringUtils.isEmpty(fieldIds)) {
            Object[] split = fieldIds.split(",");
            wrapper.in("id", split);
        }
        List<TableFieldInfo> fields = fieldInfoService.list(wrapper);

        // 获取表对应的数据库连接
        final DataConnection dataConnection = new DataConnection(tableInfo.getTableName(), slaveDataSource,
                new JdbcTemplate(slaveDataSource), DatabaseEnum.CLICKHOUSE.getFeature());

        // 构建 where 语句
        String whereSql = "";
        if (filter != null) {
            Table table = new Table();
            table.setUniqueId(1L);
            table.setTableId(tableInfo.getId());
            table.setTableName(tableInfo.getTableName());
            table.setFactTable(true);
            table.setSourceName(dataConnection.getSchema());
            Filter targetFilter = new Filter();
            ApplicationSqlUtil.convertToFilter(filter, targetFilter);

            final ClickHouse mySql = new ClickHouse(Collections.singletonList(table), null, null,
                    targetFilter, null, null);
            whereSql = mySql.getWhereSql();

//            StringBuffer sb = new StringBuffer();
//            QuerySqlUtil.findOthersSql(filter.getFilters(), tableInfo, sb);
//            whereSql = " WHERE " + whereSql + sb;
            whereSql = " WHERE " + whereSql;
        }

        List<TableDataDto.Header> headerList = new ArrayList<>(fields.size());


        // 查询表字段映射信息
        /*final List<TableMappingDTO> mappingList = tableMappingMapper.listMapping(tableId, null);*/

        int viewRange = tableInfo.getViewTotal();

        // 判断权限（没有权限只能查看10条）
       /* LambdaQueryWrapper<SysUserTable> sutLqw = new LambdaQueryWrapper<>();
        sutLqw.eq(SysUserTable::getUserId, SecurityUtils.getUserId());
        sutLqw.eq(SysUserTable::getTableId, tableId);
        List<SysUserTable> sysUserTableSet = userTableService.list(sutLqw);*/
        boolean flag = SecurityUtils.getUsername().equals(tableInfo.getLeaderName());
        boolean viewAllFlag = false;
        if (StringUtils.isNotEmpty(tableInfo.getViewUser())) {
            viewAllFlag = Arrays.asList(tableInfo.getViewUser().split(",")).contains(String.valueOf(SecurityUtils.getUserId()));
        }

        Integer applyAll = TgApplicationInfo.newInstance().selectCount(new QueryWrapper<TgApplicationInfo>().lambda()
                .eq(TgApplicationInfo::getBaseTableId, tableInfo.getId())
                .eq(TgApplicationInfo::getApplicationType, ApplicationConst.ApplicationType.TABLE_APPLICATION)
                .eq(TgApplicationInfo::getApplicantId, SecurityUtils.getUserId())
                .eq(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.AUDIT_PASS)
                .gt(TgApplicationInfo::getDataExpir, new Date())
        );

        // 资产门户需求:如果是资产负责人或者白名单人员可以查看表单全量数据
        // 获取当前用户的 DEPT 和 USER 信息
        List<AssetPermissionType> permissionTypes = new ArrayList<>();
        if (requestDTO.getAssetId() != null) {
            SysUser sysUser = ThreadContextHolder.getSysUser();
            Long userId = sysUser.getUserId();
            String deptId = ((SinoPassUserDTO) ThreadContextHolder.getParams().get(CommonConstants.ORG_USER_INFO)).getMainOrganizationId();
            final TgAssetInfo tgAssetInfo = assetService.queryOne(requestDTO.getAssetId());
            permissionTypes = assetService.computePermissions(tgAssetInfo, userId, deptId, true);
        }

        boolean inRange = requestDTO.getPageNum() * requestDTO.getPageSize() < viewRange;
        boolean canViewAll = flag || viewAllFlag || (Objects.nonNull(applyAll) && applyAll > 0)
                || permissionTypes.contains(AssetPermissionType.DATA_QUERY);
        int pageSize;
        int startRow;
        if (canViewAll || inRange) {
            startRow = PageUtil.getStart(requestDTO.getPageNum() - 1, requestDTO.getPageSize());
            pageSize = requestDTO.getPageSize();
        } else {
            startRow = PageUtil.getStart(requestDTO.getPageNum() - 1, requestDTO.getPageSize());
            // 第一页就超出
            if (requestDTO.getPageNum() == 1 && requestDTO.getPageSize() > viewRange) {
                pageSize = viewRange;
            } else if (requestDTO.getPageNum() * requestDTO.getPageSize() > viewRange
                    && (requestDTO.getPageNum() - 1) * requestDTO.getPageSize() < viewRange) {
                // 最后一页，截断防止超出
                pageSize = viewRange - (requestDTO.getPageNum() - 1) * requestDTO.getPageSize();
            } else {
                // 超出的页
                pageSize = 0;
            }
        }

        // 构造SQL语句
        String selectSql = buildSelectSql(tableInfo.getTableNameDistributed(),
                fields, whereSql, startRow, pageSize, headerList, sortBy, sortingField, null);

        log.info("执行SQL {}", selectSql);

        // 执行SQL，返回结果
        List<Map<String, Object>> result = dataConnection.getJdbcOperations().queryForList(selectSql);
        TableDataDto dto = new TableDataDto();
//        List<TableDataDto.Header> sortedHeaders = headerList.stream()
//                .sorted(Comparator.comparing(TableDataDto.Header::getFieldId)).collect(Collectors.toList());
//        dto.setHeader(sortedHeaders);
        dto.setHeader(headerList);
        dto.setList(result);

        // MySQL才有分页信息
//        String key = whereSql.length() > 1 ? tableRowKey + tableId + "_" + Md5Utils.hash(whereSql) : tableRowKey + tableId;
//        Long rows = tableTotalRowCache.get(key);
//        dto.setTotal(rows == null ? tableInfo.getTotalRow() : rows);

        // 条数
        final String countSql = String.format("SELECT COUNT(1) count FROM %s t_1 %s",
                tableInfo.getTableNameDistributed(), whereSql);
        List<Map<String, Object>> countResult = dataConnection.getJdbcOperations().queryForList(countSql);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(countResult)) {
            dto.setTotal(Long.parseLong(countResult.get(0).get("count").toString()));
        }

        // 没有权限只能查看固定条数
        if (!canViewAll && dto.getTotal() > viewRange) {
            dto.setTotal(viewRange);
        }
        return dto;
    }


    @Override
    public TableDataDto getCmhTableData(GetDataInfoRequestDTO requestDTO) {
        final String fieldIds = requestDTO.getFieldIds();
        final FilterDTO filter = requestDTO.getFilter();

        int MAX = 1000;
        Long tableId = appProperties.getCascadeTableId();
        TableInfo tableInfo = tableInfoService.getById(tableId);
        if (Objects.isNull(tableInfo)) {
            throw new CustomException("CMH产品表不存在!");
        }

        QueryWrapper<TableFieldInfo> wrapper = Wrappers.<TableFieldInfo>query()
                .eq("table_id", tableId)
                .eq("status", 1)
                .orderByAsc("sort");
        if (!StringUtils.isEmpty(fieldIds)) {
            Object[] split = fieldIds.split(",");
            wrapper.in("id", split);
        }
        List<TableFieldInfo> fields = fieldInfoService.list(wrapper);

        // 检查多出的字段 字段库关联实现方式
//        Map<Long, TableFieldInfo> fieldMap = Lambda.buildMap(fields, TableFieldInfo::getRelationColId);
//        List<Long> paramFields = new ArrayList<>();
//        ApplicationSqlUtil.extractFieldId(requestDTO.getFilter(), paramFields);
//        if (CollectionUtils.isNotEmpty(paramFields)) {
//            List<Long> outerFields = paramFields.stream().filter(v -> !fieldMap.containsKey(v)).collect(Collectors.toList());
//            if (CollectionUtils.isNotEmpty(outerFields)) {
//                List<FieldDict> outerNames = fieldDictMapper.selectList(new QueryWrapper<FieldDict>().lambda()
//                        .select(FieldDict::getId, FieldDict::getFieldName, FieldDict::getName)
//                        .in(FieldDict::getId, outerFields));
//                String str = outerNames.stream().map(v -> v.getFieldName() + "(" + v.getName() + ")").collect(Collectors.joining("、"));
//                throw new CustomException("产品标准表中暂无字段 " + str + "，无法筛选数据");
//            }
//        }

        // 表id 字段名 实现方式
        Map<String, TableFieldInfo> fieldMap = Lambda.buildMap(fields, TableFieldInfo::getFieldName);
        List<Long> paramTables = new ArrayList<>();
        List<String> paramFields = new ArrayList<>();
        ApplicationSqlUtil.extractFieldId(requestDTO.getFilter(), paramTables, paramFields);
        if (CollectionUtils.isNotEmpty(paramFields)) {
            List<String> outerFields = paramFields.stream().filter(v -> !fieldMap.containsKey(v)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(outerFields)) {
                List<TableFieldInfo> outerNames = fieldInfoService.list(new QueryWrapper<TableFieldInfo>().lambda()
                        .in(CollectionUtils.isNotEmpty(paramTables), TableFieldInfo::getTableId, paramTables)
                        .in(TableFieldInfo::getFieldName, outerFields)
                );
                String str = outerNames.stream().map(v -> v.getFieldName() + "(" + v.getFieldAlias() + ")").collect(Collectors.joining("、"));
                throw new CustomException("产品标准表中暂无字段 " + str + "，无法筛选数据");
            }
        }


        // 获取表对应的数据库连接
        final DataConnection dataConnection = new DataConnection(tableInfo.getTableName(), slaveDataSource,
                new JdbcTemplate(slaveDataSource), DatabaseEnum.CLICKHOUSE.getFeature());

        // 构建 where 语句
        String whereSql = "";
        if (filter != null) {
            Table table = new Table();
            table.setUniqueId(1L);
            table.setTableId(tableInfo.getId());
            table.setTableName(tableInfo.getTableName());
            table.setFactTable(true);
            table.setSourceName(dataConnection.getSchema());
            Filter targetFilter = new Filter();
            ApplicationSqlUtil.convertToFilter(filter, targetFilter);

            final ClickHouse mySql = new ClickHouse(Collections.singletonList(table), null, null,
                    targetFilter, null, null);
            whereSql = " WHERE " + mySql.getWhereSql();
        }

        List<TableDataDto.Header> headerList = new ArrayList<>(fields.size());

        int startRow = PageUtil.getStart(requestDTO.getPageNum() - 1, requestDTO.getPageSize());
        startRow = Math.min(MAX, startRow);
        int pageSize = Math.min(requestDTO.getPageSize(), MAX - startRow);

        whereSql = whereSql.replace("t_null", "t_1");

        // 构造SQL语句
        String selectSql = ApplicationSqlUtil.buildSelectSql(tableInfo.getTableNameDistributed(),
                fields, whereSql, startRow, pageSize, headerList, "desc", "std_id");

        log.info("执行SQL {}", selectSql);

        // 执行SQL，返回结果
        List<Map<String, Object>> result = dataConnection.getJdbcOperations().queryForList(selectSql);
        TableDataDto dto = new TableDataDto();
        dto.setHeader(headerList);
        dto.setList(result);

        // 条数
        final String countSql = String.format("SELECT COUNT(1) count FROM %s t_1 %s",
                tableInfo.getTableNameDistributed(), whereSql);
        List<Map<String, Object>> countResult = dataConnection.getJdbcOperations().queryForList(countSql);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(countResult)) {
            dto.setRealTotal(Long.parseLong(countResult.get(0).get("count").toString()));
        }

        dto.setTotal(Math.min(MAX, dto.getRealTotal()));
        dto.setSql(selectSql);
        return dto;
    }

    @Override
    public TableInfoDto getTableBaseInfo(Long tableId) {
        TableInfoDto dto;

        // 查询表信息
        TableInfo tableInfo = this.tableInfoService.getById(tableId);
        // 地图目录-查看人数 埋点
        EventReporterUtil.operateLogEvent4View(tableInfo.getId().toString(), tableInfo.getTableName(),
                SecondSubjectTypeEnum.MAP_TABLE_VIEW, null);

        // 查询数据库连接
        DataConnection connection;
        Connection jdbcConnection = null;
        String db;
        try {
            jdbcConnection = slaveDataSource.getConnection();
            db = selfCKProperties.getDatabase();
            connection = new DataConnection(db, slaveDataSource, new JdbcTemplate(slaveDataSource), DatabaseEnum.CLICKHOUSE.getFeature());


            String sql = String.format("SELECT name AS `tableName`, SUM(total_bytes) AS `storeSize`" +
                    "FROM system.tables_all WHERE  name ='%s' AND database = '%s' " +
                    "GROUP BY  name ", tableInfo.getTableName(), db);
            log.info("metadata sql={}", sql);
            dto = connection.getJdbcOperations().queryForObject(sql, new BeanPropertyRowMapper<>(TableInfoDto.class));

            // 测试环境这个SQL执行慢
            String totalSql = String.format("SELECT COUNT(*)  FROM system.columns  WHERE table='%s' AND database = '%s'",
                    tableInfo.getTableName(), selfCKProperties.getDatabase());
            Integer fieldTotal = connection.getJdbcOperations().queryForObject(totalSql, Integer.class);

            String shardTable = StringUtils.replace(tableInfo.getTableName(), "_local", "_shard");
            String totalRowSQL = "SELECT COUNT(*) FROM " + shardTable;
            Long totalRow = connection.getJdbcOperations().queryForObject(totalRowSQL, Long.class);
            dto.setTotalRows(totalRow);

            BeanUtils.copyProperties(tableInfo, Objects.requireNonNull(dto));
            if (null != fieldTotal) {
                dto.setTotalFields(fieldTotal);
            }
            DataDir dataDir = dataDirMapper.selectById(tableInfo.getDirId());
            if (dataDir != null) {
                dto.setDirPath(dataDir.getDirName());
            }
            dto.setTableAlias(tableInfo.getTableAlias());
            dto.setUpdateTime(tableInfo.getUpdateTime());
            dto.setCreateTime(tableInfo.getCreateTime());
            dto.setDirId(tableInfo.getDirId());
            dto.setId(tableInfo.getId());
            dto.setSchemeCycle(tableInfo.getSchemeCycle());
            dto.setSchemeStatus(tableInfo.getSchemeStatus());
            dto.setLeaderName(tableInfo.getLeaderName());
            //处理负责人
            if (StringUtils.isNotEmpty(tableInfo.getLeaderName())) {
                SysUser user = sysUserService.selectUserByUserName(tableInfo.getLeaderName());
                if (user != null && StringUtils.isNotEmpty(user.getOrgUserId())) {
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                    if (sinoPassUserDTO != null) {
                        dto.setLeaderRealName(sinoPassUserDTO.getViewName());
                    }
                }
            }
            dto.setViewUser(tableInfo.getViewUser());

            // 操作权限
            Long userId = SecurityUtils.getUserId();

            List<Integer> permissions = dataDirService.buildTablePermissions(userId, tableInfo);
            dto.setPermissions(permissions.stream().distinct().sorted().collect(Collectors.toList()));

            this.fillPermissionType(tableId, dto, tableInfo, userId);

        } catch (Exception e) {
            log.error("", e);
            return null;
        } finally {
            Optional.ofNullable(jdbcConnection).ifPresent(v -> {
                try {
                    v.close();
                } catch (SQLException e) {
                    log.error("", e);
                }
            });
        }
        return dto;
    }

    private void fillPermissionType(Long tableId, TableInfoDto dto, TableInfo tableInfo, Long userId) {
        if (StringUtils.isNoneBlank(tableInfo.getViewUser())) {
            String[] userIds = tableInfo.getViewUser().split(",");
            List<String> strings = Arrays.asList(userIds);
            if (strings.contains(String.valueOf(userId))) {
                dto.setPermissionType(DataDirConst.PreviewPermissionType.MANAGER);
                return;
            }
        }
        if (Objects.equals(dto.getLeaderName(), SecurityUtils.getUsername())) {
            dto.setPermissionType(DataDirConst.PreviewPermissionType.MANAGER);
            return;
        }

        List<TgApplicationInfo> passInfos = applicationInfoMapper.selectList(new QueryWrapper<TgApplicationInfo>().lambda()
                .eq(TgApplicationInfo::getBaseTableId, tableId)
                .eq(TgApplicationInfo::getCurrentAuditProcessStatus, 2)
                .last(" limit 1"));
        if (CollectionUtils.isNotEmpty(passInfos)) {
            dto.setPermissionType(DataDirConst.PreviewPermissionType.PREVIEW);
            return;
        }
        dto.setPermissionType(DataDirConst.PreviewPermissionType.NONE);
    }

    @Override
    public void syncTableInfo() {
        log.info("同步表基础信息开始");


        // 查询数据库连接
        DataConnection dataConnection;
        String db;
        String tableSql;
//            db = slaveDataSource.getConnection().getSchema();
        db = selfCKProperties.getDatabase();
        tableSql = "SELECT name as tableName, total_rows  AS  totalRows, total_bytes AS storeSize,comment " +
                "FROM system.tables  WHERE name LIKE  '%local%' AND name NOT LIKE '%dim%'" +
                "AND database = '" + db + "'";
        dataConnection = new DataConnection(db, slaveDataSource, new JdbcTemplate(slaveDataSource), DatabaseEnum.CLICKHOUSE.getFeature());

        // 查询出全部的数据目录信息
        Date now = new Date();
        List<TableBaseInfoDto> tables = dataConnection.getJdbcOperations()
                .query(tableSql, new BeanPropertyRowMapper<>(TableBaseInfoDto.class));

        List<TableInfo> tableInfoList = new ArrayList<>(tables.size());

        // 已存在的表
        List<TableInfo> existTableInfoList = tableInfoService.list(Wrappers.<TableInfo>query().eq("status", 1));
        tables.stream()
                .filter(t -> existTableInfoList.stream().noneMatch(e -> e.getTableName().equals(t.getTableName())))
                .forEach(t -> {
                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setTableName(t.getTableName());
                    tableInfo.setTotalRow(t.getTotalRows());
                    tableInfo.setTableAlias(com.sinohealth.common.utils.StringUtils.isEmpty(t.getComment()) ? t.getTableName() : t.getComment());
                    tableInfo.setComment(t.getComment());
                    tableInfo.setTableNameDistributed(t.getTableName().replace("local", "shard"));
                    tableInfo.setStatus(1);
                    tableInfo.setDirId(0L);
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

                    String oldComment = com.sinohealth.common.utils.StringUtils.isEmpty(t.getComment()) ? "" : t.getComment();
                    String newComment = com.sinohealth.common.utils.StringUtils.isEmpty(opt.get().getComment()) ? "" : opt.get().getComment();
                    if (!oldComment.equals(newComment)) {
                        update.set("comment", newComment);
                    }

                    update.eq("id", t.getId());
                    tableInfoService.update(update);
                }
            }

        });

        if (!tableInfoList.isEmpty()) {
            tableInfoService.saveBatch(tableInfoList);
        }

        // 表字段信息
        String fieldSql = "SELECT table as tableName, comment,  name  AS fieldName, " +
                "CASE WHEN type not like '%Nullable%' THEN 0 ELSE 1  END AS empty," +
                "type AS dataType,is_in_partition_key AS primary_key " +
                " from system.columns  WHERE database = '" + db + "' AND   table  LIKE  '%local%' AND table not like '%dim%' ";
        List<TableFieldInfo> fieldInfoList = dataConnection.getJdbcOperations()
                .query(fieldSql, new BeanPropertyRowMapper<>(TableFieldInfo.class));

        if (!ObjectUtils.isEmpty(fieldInfoList)) {
            List<TableFieldInfo> fieldList = fieldInfoService.list(Wrappers.<TableFieldInfo>query().eq("status", 1));

            List<TableFieldInfo> addFields = new ArrayList<>();
            List<TableFieldInfo> updateFields = new ArrayList<>();

            fieldInfoList.forEach(field -> {
                field.setFieldAlias(com.sinohealth.common.utils.StringUtils.isEmpty(field.getComment()) ? field.getFieldName() : field.getComment());
                field.setCreateTime(now);
                field.setDirId(0L);
                field.setStatus(true);
                int index = field.getDataType().indexOf("(");
                if (index > 0) {
                    int index1 = field.getDataType().indexOf(",");
                    if (index1 > 0) {
                        String length = field.getDataType().substring(index + 1, index1);
                        field.setLength(Integer.parseInt(length));
                        String scale = field.getDataType().substring(index1 + 1, field.getDataType().indexOf(")"));
                        field.setScale(Integer.parseInt(scale.trim()));
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
                                if (com.sinohealth.common.utils.StringUtils.isNotBlank(field.getComment())) {
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
            /*if (!ObjectUtils.isEmpty(fieldInfoList) && !ObjectUtils.isEmpty(fieldList)) {
                List<Long> deleteFields = fieldList.stream().filter(t -> !fieldInfoList.stream().anyMatch(a -> t.getTableId().equals(a.getTableId()) && a.getFieldName().equals(t.getFieldName()))).map(t -> t.getId()).collect(Collectors.toList());
                if (!ObjectUtils.isEmpty(deleteFields)) {
                    fieldInfoService.removeByIds(deleteFields);
                }
            }*/
        }

        log.info("同步表基础信息结束");
    }

    /**
     * @return data 中要有id 前端依赖
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<?> createTable(String localSql, String tableName, Boolean dropTableBeforeCreate) {
        if (StringUtils.isBlank(tableName) || !tableName.endsWith("_local")) {
            return AjaxResult.error("仅支持创建 _local 结尾的表");
        }

        TableInfo oldTable = tableInfoService.selectTableInfoByTableName(tableName);
        if (Objects.nonNull(oldTable) && !dropTableBeforeCreate) {
            return AjaxResult.error("表名已存在，请考虑是否重建表: " + tableName);
        }

        String db;
        DataConnection dataConnection;
        String distributedTableName = StringUtils.replaceLast(tableName, "local", "shard");
        try {
            db = selfCKProperties.getDatabase();
            dataConnection = new DataConnection(slaveDataSource.getConnection().getSchema(), slaveDataSource,
                    new JdbcTemplate(slaveDataSource), DatabaseEnum.CLICKHOUSE.getFeature());

            String tableSql = String.format("SELECT name AS tableName, total_rows AS totalRows, total_bytes AS storeSize,comment" +
                    " FROM system.tables WHERE name = '%s' and database = '%s' ", tableName, db);

            // 创建表之前先删除 或 替换
            if (dropTableBeforeCreate && Objects.nonNull(oldTable)) {
                Integer ref = templateInfoMapper.selectCount(new QueryWrapper<TgTemplateInfo>().lambda()
                        .eq(TgTemplateInfo::getBaseTableId, oldTable.getId()));
                if (Objects.isNull(ref) || ref == 0) {
                    // 直接删除库表，然后走新建表逻辑
                    log.info("删除旧库表: id={} name={}", oldTable.getId(), tableName);
                    this.deleteOldTable(tableName, dataConnection, oldTable, distributedTableName);
                } else {
                    // 替换表，新增字段
                    return this.replaceOldTable(tableName, dataConnection, tableSql, localSql, db, oldTable, distributedTableName);
                }
            }

            // 创建全新的表
            TableInfo newTable = this.createNewTable(localSql, tableName, dataConnection, db, distributedTableName, tableSql, oldTable);
            return AjaxResult.success(newTable);
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 新建CK库表，新建字段元数据，更新表元数据
     */
    private TableInfo createNewTable(String localSql, String tableName, DataConnection dataConnection, String db,
                                     String distributedTableName, String tableSql, TableInfo oldTable) {
        TableBaseInfoDto tableBaseInfoDto;
        // 新建表
        tableBaseInfoDto = createCKTableAndGetInfo(localSql, tableName, dataConnection, db, distributedTableName, tableSql);

        // 查询出全部的数据目录信息
        Date now = DateUtils.getNowDate();
        TableInfo tableInfo = new TableInfo();
        if (Objects.nonNull(oldTable)) {
            tableInfo.setId(oldTable.getId());
        }

        tableInfo.setTableName(tableBaseInfoDto.getTableName());
        tableInfo.setTotalRow(tableBaseInfoDto.getTotalRows());
        tableInfo.setTableAlias(StringUtils.isEmpty(tableBaseInfoDto.getComment()) ? tableBaseInfoDto.getTableName() : tableBaseInfoDto.getComment());
        tableInfo.setComment(tableBaseInfoDto.getComment());
        tableInfo.setTableNameDistributed(tableBaseInfoDto.getTableName().replace("local", "shard"));
        tableInfo.setStatus(1);
        tableInfo.setDirId(0L);
        tableInfo.setLocalSql(localSql);
        tableInfo.setViewTotal(10);
        tableInfo.setCreateTime(now);
        tableInfo.setLeaderName(SecurityUtils.getUsername());
        tableInfo.setDataLength(tableBaseInfoDto.getStoreSize() == null ? 0 : tableBaseInfoDto.getStoreSize());
        assetsSortService.fillDefaultDisSort(tableInfo);
        tableInfoService.saveOrUpdate(tableInfo);

        // 获取新建表
        TableInfo currentTable = tableInfoService.selectTableInfoByTableName(tableName);
        // 表字段信息
        List<TableFieldInfo> fieldInfoList = getMetaFields(tableName, dataConnection, db);

        if (CollectionUtils.isNotEmpty(fieldInfoList)) {
            for (int i = 0; i < fieldInfoList.size(); i++) {
                TableFieldInfo field = fieldInfoList.get(i);
//                    log.info("处理字段 {}", field.getFieldName());
                field.setFieldAlias(StringUtils.isEmpty(field.getComment()) ? field.getFieldName() : field.getComment());
                field.setCreateTime(now);
                field.setDirId(0L);
                field.setTableId(currentTable.getId());
                field.setStatus(true);
                field.setSort(i);

                ApplicationSqlUtil.fillLengthAndType(field);

                field.setTableId(tableInfo.getId());
                //数值型的就是 指标;其他的就是纬度
                if (Arrays.asList(numTypes).contains(field.getDataType())) {
                    field.setFieldType("数值");
                    field.setDimIndex(TableConst.DimIndexType.METRIC);
                } else if (Arrays.asList(dateTypes).contains(field.getDataType())) {
                    field.setFieldType("日期");
                    field.setDimIndex(TableConst.DimIndexType.DIMENSIONS);
                } else {
                    field.setFieldType("文本");
                    field.setDimIndex(TableConst.DimIndexType.DIMENSIONS);
                }
            }
            fieldInfoService.saveBatch(fieldInfoList);
        }

        return tableInfo;
    }

    private static TableBaseInfoDto createCKTableAndGetInfo(String localSql, String tableName, DataConnection dataConnection, String db, String distributedTableName, String tableSql) {
        TableBaseInfoDto tableBaseInfoDto;
        // 执行本地表建表SQL语句
        dataConnection.getJdbcOperations().execute(localSql);
        // 执行分布式表建表SQL语句
        String distributedSqlPattern = "CREATE TABLE IF NOT EXISTS %s.%s ON cluster default_cluster as %s.%s ENGINE = Distributed(default_cluster, %s, %s, rand()) ;";
        String distributedSql = String.format(distributedSqlPattern, db, distributedTableName, db, tableName, db, tableName);
        dataConnection.getJdbcOperations().execute(distributedSql);

        // 获取新建表信息
        tableBaseInfoDto = dataConnection.getJdbcOperations().queryForObject(tableSql,
                new BeanPropertyRowMapper<>(TableBaseInfoDto.class));
        return tableBaseInfoDto;
    }

    private void deleteOldTable(String tableName, DataConnection dataConnection, TableInfo oldTable, String distributedTableName) {
        if (Objects.isNull(oldTable)) {
            return;
        }
        String userName = ThreadContextHolder.getSysUser().getUserName();
        if (!oldTable.getLeaderName().equals(userName)) {
            throw new CustomException("该表不属于当前用户, 无法删除");
        }

        // 删除CK中的表
        dataConnection.getJdbcOperations().execute("DROP TABLE IF EXISTS " + distributedTableName + " ON CLUSTER default_cluster sync");
        dataConnection.getJdbcOperations().execute("DROP TABLE IF EXISTS " + tableName + " ON CLUSTER default_cluster sync");

        // 删除属性
        fieldInfoService.removeByMap(new HashMap<String, Object>() {{
            put("table_id", oldTable.getId());
        }});
    }

    /**
     * 替换已有的表，更新业务元数据（表，字段）
     */
    private AjaxResult replaceOldTable(String tableName, DataConnection dataConnection, String tableSql,
                                       String localSql, String db, TableInfo oldTable, String distributedTableName) {

        List<TableFieldInfo> existFields = Optional.of(oldTable).map(TableInfo::getId)
                .map(fieldInfoService::getFieldsByTableId)
                .orElse(Collections.emptyList());
        Map<String, TableFieldInfo> existFieldMap = Lambda.buildMap(existFields, TableFieldInfo::getFieldName);

        TableBaseInfoDto tableBaseInfoDto = dataConnection.getJdbcOperations().queryForObject(tableSql,
                new BeanPropertyRowMapper<>(TableBaseInfoDto.class));
        if (Objects.isNull(tableBaseInfoDto)) {
            return AjaxResult.error(tableName + " 表在库中不存在");
        }
        log.info("替换旧库表 {}", tableName);

//        String userName = ThreadContextHolder.getSysUser().getUserName();
//        if (!oldTable.getLeaderName().equals(userName)) {
//            throw new CustomException("该表不属于当前用户, 无法删除");
//        }

        String tempName = "ck_create_tmp_" + StrUtil.randomAlpha(8);
        String tempLocal = localSql.replace(tableName, tempName);
        try {
            dataConnection.getJdbcOperations().execute(tempLocal);
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error("建表SQL异常，请检查输入");
        }

        try {
            List<TableFieldInfo> tempFields = getMetaFields(tempName, dataConnection, db);
            Map<String, TableFieldInfo> tempFieldMap = Lambda.buildMap(tempFields, TableFieldInfo::getFieldName);
            boolean existBroke = existFieldMap.values().stream().anyMatch(v -> {
                TableFieldInfo meta = tempFieldMap.get(v.getFieldName());
                if (Objects.isNull(meta)) {
                    log.warn("字段缺失: fieldName={}", v.getFieldName());
                    return true;
                }
                String originDataType = RegExUtils.replaceFirst(meta.getDataType(), "^Nullable\\((.*)\\)", "$1");
                boolean ignoreDecimal = Objects.equals(v.getDataType(), "Decimal") && meta.getDataType().contains(v.getDataType());
                if (!Objects.equals(originDataType, v.getDataType()) && !ignoreDecimal) {
                    log.warn("类型不匹配: fieldName={}", v.getFieldName());
                    return true;
                }
                return false;
            });
            dataConnection.getJdbcOperations().execute("DROP TABLE IF EXISTS " + tempName + " ON CLUSTER default_cluster sync");
            if (existBroke) {
                return AjaxResult.error("重建表失败，不允许删除和修改字段,仅支持新增字段");
            }

            // 重建表，更新字段信息
            dataConnection.getJdbcOperations().execute("DROP TABLE IF EXISTS " + distributedTableName + " ON CLUSTER default_cluster sync");
            dataConnection.getJdbcOperations().execute("DROP TABLE IF EXISTS " + tableName + " ON CLUSTER default_cluster sync");

            tableBaseInfoDto = createCKTableAndGetInfo(localSql, tableName, dataConnection, db, distributedTableName, tableSql);

            // 查询出全部的数据目录信息
            Date now = DateUtils.getNowDate();
            TableInfo tableInfo = new TableInfo();
            tableInfo.setId(oldTable.getId());
            tableInfo.setTotalRow(tableBaseInfoDto.getTotalRows());
            tableInfo.setTableAlias(StringUtils.isEmpty(tableBaseInfoDto.getComment()) ? tableBaseInfoDto.getTableName() : tableBaseInfoDto.getComment());
            tableInfo.setComment(tableBaseInfoDto.getComment());
            tableInfo.setLocalSql(localSql);
            tableInfo.setLeaderName(SecurityUtils.getUsername());
            tableInfo.setDataLength(tableBaseInfoDto.getStoreSize() == null ? 0 : tableBaseInfoDto.getStoreSize());
            tableInfoService.updateById(tableInfo);

            // 表字段信息
            List<TableFieldInfo> fieldInfoList = getMetaFields(tableName, dataConnection, db);

            if (CollectionUtils.isNotEmpty(fieldInfoList)) {
                List<Long> existIds = fieldInfoList.stream().map(v -> Optional.ofNullable(existFieldMap.get(v.getFieldName()))
                        .map(TableFieldInfo::getId).orElse(null)).filter(Objects::nonNull).collect(Collectors.toList());
                List<TableFieldInfo> existRelationFields = fieldInfoService.getBaseMapper().selectList(new QueryWrapper<TableFieldInfo>().lambda()
                        .select(TableFieldInfo::getId, TableFieldInfo::getRelationColId)
                        .in(TableFieldInfo::getId, existIds));
                Map<Long, Long> relationMap = Lambda.buildMap(existRelationFields, TableFieldInfo::getId, TableFieldInfo::getRelationColId,
                        v -> Objects.nonNull(v.getRelationColId()));

                for (int i = 0; i < fieldInfoList.size(); i++) {
                    TableFieldInfo field = fieldInfoList.get(i);
                    field.setFieldAlias(StringUtils.isEmpty(field.getComment()) ? field.getFieldName() : field.getComment());

                    field.setDirId(0L);
                    field.setTableId(oldTable.getId());
                    field.setStatus(true);
                    field.setSort(i);

                    field.setId(Optional.ofNullable(existFieldMap.get(field.getFieldName())).map(TableFieldInfo::getId).orElse(null));
                    field.setRelationColId(relationMap.get(field.getId()));
                    if (Objects.nonNull(field.getId())) {
                        field.setCreateTime(now);
                    }

                    ApplicationSqlUtil.fillLengthAndType(field);

                    field.setTableId(tableInfo.getId());
                    //数值型的就是 指标;其他的就是纬度
                    if (Arrays.asList(numTypes).contains(field.getDataType())) {
                        field.setFieldType("数值");
                        field.setDimIndex(TableConst.DimIndexType.METRIC);
                    } else if (Arrays.asList(dateTypes).contains(field.getDataType())) {
                        field.setFieldType("日期");
                        field.setDimIndex(TableConst.DimIndexType.DIMENSIONS);
                    } else {
                        field.setFieldType("文本");
                        field.setDimIndex(TableConst.DimIndexType.DIMENSIONS);
                    }
                }
                fieldInfoService.saveOrUpdateBatch(fieldInfoList);
            }
            log.info("重建表成功：{}", tableName);
            return AjaxResult.success(tableInfo);
        } catch (Exception e) {
            log.info("", e);
            return AjaxResult.error("数据表更新异常");
        }
    }

    private static List<TableFieldInfo> getMetaFields(String tableName, DataConnection dataConnection, String db) {
        String fieldSql = "SELECT table as tableName, comment, name AS fieldName," +
                "CASE WHEN type not like '%Nullable%' THEN 0 ELSE 1  END AS empty," +
                "type AS dataType,is_in_partition_key AS primary_key " +
                "from system.columns where database = '" + db + "' AND table = '" + tableName + "'";
        List<TableFieldInfo> fieldInfoList = dataConnection.getJdbcOperations()
                .query(fieldSql, new BeanPropertyRowMapper<>(TableFieldInfo.class));
        return fieldInfoList;
    }

    @Override
    public AjaxResult dropTable(String tableName) throws SQLException {
        String distributedTableName = StringUtils.replaceLast(tableName, "local", "shard");
        DataConnection dataConnection = new DataConnection(slaveDataSource.getConnection().getSchema(), slaveDataSource, new JdbcTemplate(slaveDataSource), DatabaseEnum.CLICKHOUSE.getFeature());
        TableInfo tableInfo1 = tableInfoService.selectTableInfoByTableName(tableName);
        log.info("删除旧有表单");
        if (tableInfo1.getLeaderName().equals(ThreadContextHolder.getSysUser().getUserName())) {
            fieldInfoService.removeByMap(new HashMap<String, Object>() {{
                put("table_id", tableInfo1.getId());
            }});
            tableInfoService.removeById(tableInfo1.getId());
            dataConnection.getJdbcOperations().execute("DROP TABLE IF EXISTS " + distributedTableName + " ON CLUSTER default_cluster sync");
            dataConnection.getJdbcOperations().execute("DROP TABLE IF EXISTS " + tableName + " ON CLUSTER default_cluster sync");
        } else {
            throw new CustomException("不是该表的创建人, 无法删除该表");
        }
        return AjaxResult.success();
    }

    @Override
    public void updateStatus(String tableName) {
        TableInfo tableInfo = tableInfoService.selectTableInfoByTableName(tableName);
        // 如果存在有效申请,则无法将其禁用,提示必须禁用所有有效申请

    }

    @Override
    public DataManageDto getAllTree() {
        List<DataDir> list = dataDirMapper.selectList(Wrappers.<DataDir>query().eq("target", CommonConstants.DATA_DIR));
        DataManageDto dto = new DataManageDto();
        dto.setId(0L);
        dto.setDirName("根节点");
        findTreeChildren(list, dto, null);
        return dto;
    }

    private void findTreeChildren(List<DataDir> list, DataManageDto dto, List<DataManageDto> endDirList) {
        List<DataManageDto> children = list.stream().filter(d -> d.getParentId().equals(dto.getId())).map(d -> {
                    DataManageDto child = new DataManageDto();
                    BeanUtils.copyProperties(d, child);
                    findTreeChildren(list, child, endDirList);
                    return child;
                }
        ).sorted(Comparator.comparing(DataManageDto::getSort)).collect(Collectors.toList());

        if (endDirList != null && children.isEmpty()) {
            endDirList.add(dto);
        }
        dto.setChildren(children);
    }

    private String buildSelectSql(String tableName, List<TableFieldInfo> fields, String whereSql,
                                  Integer startRow, Integer pageSize,
                                  List<TableDataDto.Header> headerList, String sortBy, String sortingField,
                                  List<TableMappingDTO> mappingList) {
        if (StringUtils.isBlank(sortBy) && !StringUtils.isBlank(sortingField)) {
            throw new CustomException("sortingField存在,sortBy不能为空");
        }
        if (!StringUtils.isBlank(sortBy) && StringUtils.isBlank(sortingField)) {
            throw new CustomException("sortBy存在,,sortingField不能为空");
        }

        StringBuffer sql = new StringBuffer();
        String id = null;
        AtomicInteger tableIndex = new AtomicInteger();
        for (TableFieldInfo field : fields) {
            if (field.isPrimaryKey()) {
                id = field.getFieldName();
            }
            if (sql.length() == 0) {
                sql.append("select ");

            } else {
                sql.append(" , ");
            }
            sql.append("t_1.`").append(field.getFieldName()).append("`");

            // 头部信息
            if (headerList != null) {
                headerList.add(new TableDataDto.Header(field.getId(), field.getFieldName(), field.getComment(),
                        field.getDataType(), field.isPrimaryKey(), null, null, null, field.getDefaultShow()));
            }

            // 字段映射
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(mappingList)) {
                for (TableMappingDTO tableMappingDTO : mappingList) {
                    if (tableMappingDTO.getFieldId().equals(field.getId())) {
                        tableIndex.getAndIncrement();
                        sql.append(", ").append(tableMappingDTO.getRelationTableName()).append(tableIndex.get()).append(".")
                                .append("`").append(tableMappingDTO.getMappingFieldName()).append("`");

                        if (headerList != null) {
                            headerList.add(new TableDataDto.Header(tableMappingDTO.getFieldId(), tableMappingDTO.getMappingFieldName(),
                                    tableMappingDTO.getMappingFieldAlias(), tableMappingDTO.getDataType(),
                                    tableMappingDTO.isPrimaryKey(), null, null, null, "y"));
                        }
                    }
                }
            }
        }

        if (StringUtils.isBlank(sql)) {
            sql.append("select * ");
        }
        sql.append(" from ").append(tableName).append(" t_1 ");
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(mappingList)) {
            AtomicInteger index = new AtomicInteger();
            mappingList.forEach(tableMappingDTO -> {
                index.getAndIncrement();
                sql.append(String.format(" LEFT JOIN %s.%s %s ON t_1.%s = %s.%s ", tableMappingDTO.getRelationSourceName(),
                        tableMappingDTO.getRelationTableName(), tableMappingDTO.getRelationTableName() + index.get(),
                        tableMappingDTO.getFieldName(), tableMappingDTO.getRelationTableName() + index.get(),
                        tableMappingDTO.getRelationFieldName()));
            });
        }
        sql.append(whereSql);

        if (!StringUtils.isBlank(sortBy) && !StringUtils.isBlank(sortingField)) {
            SortingFieldEnum sortingFieldEnum = SortingFieldEnum.valueOfCode(sortBy);
            if (sortingFieldEnum == null) {
                throw new CustomException("sortBy值不合法");
            }
            List<String> sortingFieldList = Arrays.stream(sortingField.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            sql.append(" ORDER BY ");
            sortingFieldList.forEach(sortField -> {
                sql.append("t_1.").append(sortField).append(" ");
                sql.append(sortBy);
                sql.append(", ");
            });

            sql.replace(sql.length() - 2, sql.length(), "");

        } else if (id != null) {
            sql.append(" ORDER BY t_1.");
            sql.append(id);
            sql.append(" DESC ");
        }
        if (startRow != null && pageSize != null) {
            sql.append(" LIMIT ");
            sql.append(startRow);
            sql.append(",");
            sql.append(pageSize);
        }

        return sql.toString();
    }

}
