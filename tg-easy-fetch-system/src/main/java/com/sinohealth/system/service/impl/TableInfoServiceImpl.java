package com.sinohealth.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.sinohealth.bi.data.ClickHouse;
import com.sinohealth.bi.data.Filter;
import com.sinohealth.bi.data.Table;
import com.sinohealth.bi.enums.DatabaseEnum;
import com.sinohealth.common.config.DataConnection;
import com.sinohealth.common.config.DataPlatformConfig;
import com.sinohealth.common.config.DataSourceFactory;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.page.TableSupport;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.LogType;
import com.sinohealth.common.enums.SchemeCycleEnum;
import com.sinohealth.common.enums.SchemeStatusEnum;
import com.sinohealth.common.enums.SortingFieldEnum;
import com.sinohealth.common.enums.SpeedOfProgressType;
import com.sinohealth.common.enums.StatusTypeEnum;
import com.sinohealth.common.enums.TaskType;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.DirCache;
import com.sinohealth.common.utils.HiveUtils;
import com.sinohealth.common.utils.ImpalaUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.SqlFilter;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.common.utils.file.FileUtils;
import com.sinohealth.common.utils.poi.POIUtil;
import com.sinohealth.common.utils.poi.easyexcel.DateSetExcelUtil;
import com.sinohealth.common.utils.poi.easyexcel.SqlTypeContext;
import com.sinohealth.common.utils.sign.Md5Utils;
import com.sinohealth.common.utils.spring.SpringUtils;
import com.sinohealth.common.utils.uuid.IdUtils;
import com.sinohealth.data.common.response.PageInfo;
import com.sinohealth.data.intelligence.api.Result;
import com.sinohealth.data.intelligence.api.metadata.dto.MetaColumnDTO;
import com.sinohealth.data.intelligence.api.metadata.dto.MetaTableAsset;
import com.sinohealth.data.intelligence.api.metadata.dto.MetaTableDTO;
import com.sinohealth.data.intelligence.api.metadata.param.MetaTableAssetGetParam;
import com.sinohealth.data.intelligence.api.metadataRegister.dto.AssetsTableDTO;
import com.sinohealth.data.intelligence.api.metadataRegister.dto.ColumnDTO;
import com.sinohealth.system.async.AsyncManager;
import com.sinohealth.system.async.factory.AsyncFactory;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dir.dto.DirPageQueryRequest;
import com.sinohealth.system.biz.dir.vo.DataDirListVO;
import com.sinohealth.system.biz.dir.vo.TablePageVO;
import com.sinohealth.system.biz.scheduler.dto.request.TypeConvertParam;
import com.sinohealth.system.biz.scheduler.service.IntegrateSyncProcessDefService;
import com.sinohealth.system.biz.table.dto.TablePageQueryRequest;
import com.sinohealth.system.client.DatasourceClient;
import com.sinohealth.system.client.MetadataClient;
import com.sinohealth.system.client.MetadataRegistryClient;
import com.sinohealth.system.dao.DataDirDAO;
import com.sinohealth.system.domain.SysUserGroup;
import com.sinohealth.system.domain.SysUserTable;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TableLog;
import com.sinohealth.system.domain.TableSql;
import com.sinohealth.system.domain.TableTask;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgMetadataInfo;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.dto.BigTableDto;
import com.sinohealth.system.dto.DataSourceDTO;
import com.sinohealth.system.dto.DataTreeDto;
import com.sinohealth.system.dto.ExchangeColumnDTO;
import com.sinohealth.system.dto.ExchangeColumnMapper;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;
import com.sinohealth.system.dto.GroupLeaderDto;
import com.sinohealth.system.dto.PageListReturnDataDTO;
import com.sinohealth.system.dto.TableCopyDto;
import com.sinohealth.system.dto.TableCopyInfoDto;
import com.sinohealth.system.dto.TableCreateDto;
import com.sinohealth.system.dto.TableDTO;
import com.sinohealth.system.dto.TableDataDto;
import com.sinohealth.system.dto.TableFieldInfoDto;
import com.sinohealth.system.dto.TableInfoDto;
import com.sinohealth.system.dto.TableMapDto;
import com.sinohealth.system.dto.TableMappingDTO;
import com.sinohealth.system.dto.TableRelationDto;
import com.sinohealth.system.dto.TableRelationUpdateDto;
import com.sinohealth.system.dto.TableStatisticDto;
import com.sinohealth.system.dto.TableTaskDataDto;
import com.sinohealth.system.dto.UpdateReturnDataRequestDTO;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.table_manage.DataManageFormDto;
import com.sinohealth.system.dto.table_manage.TableInfoManageDto;
import com.sinohealth.system.dto.template.TemplateAuditProcessEasyDto;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.lineage.common.LineageColumn;
import com.sinohealth.system.lineage.common.TreeNode;
import com.sinohealth.system.lineage.druid.LineageUtils;
import com.sinohealth.system.mapper.GroupDataDirMapper;
import com.sinohealth.system.mapper.TableFieldInfoMapper;
import com.sinohealth.system.mapper.TableInfoMapper;
import com.sinohealth.system.mapper.TableMappingMapper;
import com.sinohealth.system.mapper.TgAssetInfoMapper;
import com.sinohealth.system.mapper.TgMetadataInfoMapper;
import com.sinohealth.system.service.IDataDirService;
import com.sinohealth.system.service.IGroupDataDirService;
import com.sinohealth.system.service.ISysUserGroupService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ISysUserTableService;
import com.sinohealth.system.service.ITableFieldInfoService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.ITableLogService;
import com.sinohealth.system.service.ITableRelationService;
import com.sinohealth.system.service.ITableSqlService;
import com.sinohealth.system.service.ITableTaskService;
import com.sinohealth.system.service.ITemplateService;
import com.sinohealth.system.util.ApplicationSqlUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author jingjun
 * @date 2021-04-20
 */
@Service
public class TableInfoServiceImpl extends ServiceImpl<TableInfoMapper, TableInfo> implements ITableInfoService {

    private static final Logger log = LoggerFactory.getLogger("table-log");

    @Autowired
    private IGroupDataDirService groupDataDirService;

    @Autowired
    private MetadataClient metadataClient;

    @Autowired
    private MetadataRegistryClient metadataRegistryClient;

    @Autowired
    private TgMetadataInfoMapper tgMetadataInfoMapper;

    @Autowired
    private ISysUserTableService userTableService;
    @Autowired
    private ITableLogService tableLogService;

    @Autowired
    private TgAssetInfoMapper tgAssetInfoMapper;
    @Autowired
    private ITableFieldInfoService fieldInfoService;
    @Autowired
    private ITableSqlService tableSqlService;
    @Autowired
    private ITableRelationService relationService;
    @Resource(name = "redisTemplate")
    private ValueOperations<String, Long> tableTotalRowCache;
    @Autowired
    private ITableTaskService iTableTaskService;
    @Autowired
    private ITableInfoService iTableInfoService;
    @Autowired
    private ISysUserGroupService iSysUserGroupService;
    @Autowired
    private IDataDirService iDataDirService;
    @Autowired
    private ITableFieldInfoService iTableFieldInfoService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private TableInfoMapper tableInfoMapper;
    @Autowired
    private DataDirDAO dataDirDAO;
    @Autowired
    private IDataDirService dataDirService;

    @Autowired
    private IntegrateSyncProcessDefService integrateSyncProcessDefService;

    @Resource
    private GroupDataDirMapper groupDataDirMapper;

    @Resource
    private TableMappingMapper tableMappingMapper;

    @Resource
    private TableFieldInfoMapper tableFieldInfoMapper;

    @Value("${dataset.dirSourceId}")
    private Long dirDataSourceId;

    @Value("${returnData.tableId}")
    private Long returnDataTableId;

    @Autowired
    private ITemplateService templateService;
    @Autowired
    private DatasourceClient datasourceApi;


    private final static String tableRowKey = "table_row_";
    private final static String copyTableSql = " create table %s  like %s.%s ";
    private final static String copyDataSql = " create table %s select * from %s.%s ";
    private final static String updateTableCommentSql = "alter table %s  comment ? ";

    @Override
    public List<TableInfo> findByTableName(String tableName) {
        List<TableInfo> list;
        PageHelper.startPage(1, 50);
        if (StringUtils.isNotBlank(tableName)) {
            list = this.list(Wrappers.<TableInfo>query().eq("status", 1).and(t -> t.like("table_name", "%" + tableName + "%").or().like("table_alias", "%" + tableName + "%")));
        } else {
            list = this.list(Wrappers.<TableInfo>query().eq("status", 1));
        }

        return list;
    }

    @Override
    public void refreshTableCount(Long dirId, Long tableId, String tableName, String cachekey) {
        DataConnection connection = DataSourceFactory.getDataConnection(dirId);
        Long rows = connection.getJdbcOperations().queryForObject("select count(*) from " + tableName, Long.class);
        tableTotalRowCache.set(cachekey, rows, 10, TimeUnit.MINUTES);
        this.update(Wrappers.<TableInfo>update().set("total_row", rows).eq("id", tableId));
    }

    @Override
    public IPage<TableInfo> getList(Long dirId, String tableName, String fieldName, Integer accessType,
                                    boolean isFilter, Integer pageNum, Integer pageSize) {

        List<Long> tableIds = null;
        List<SysUserTable> userTableList = null;
        if (!ObjectUtils.isEmpty(accessType)) {//查询数据访问权限
            userTableList = userTableService.list(Wrappers.<SysUserTable>query().eq("dir_id", dirId).eq("access_type", accessType));

            if (ObjectUtils.isEmpty(userTableList)) {
                return new Page<>();
            }

            tableIds = userTableList.stream().map(u -> u.getTableId()).collect(Collectors.toList());
        }

        Page<TableInfo> page = new Page<>(1, Long.MAX_VALUE);
        if (pageNum != null && pageSize != null) {
            page = new Page<>(pageNum, pageSize);
        }
        IPage<TableInfo> iPage = this.baseMapper.getList(page, dirId, tableName, fieldName, tableIds);
        List<TableInfo> list = iPage.getRecords();

        if (ObjectUtil.isNotNull(isFilter) && isFilter) {
            /**
             * v1.1新增
             * 根据前端传值，判断是否需要过滤出状态为 1 的数据
             */
            list = list.stream().filter(t -> t.getStatus().intValue() == 1).collect(Collectors.toList());
        }

        if (!ObjectUtils.isEmpty(list)) {
            if (ObjectUtils.isEmpty(userTableList)) {
                userTableList = userTableService.list(Wrappers.<SysUserTable>query().in("table_id", list.stream().map(t -> t.getId()).collect(Collectors.toList())).eq("user_id", SecurityUtils.getUserId()));
            }
            if (!ObjectUtils.isEmpty(userTableList)) {
                for (TableInfo d : list) {
                    userTableList.stream().filter(u -> u.getTableId().equals(d.getId())).findFirst().ifPresent(u -> {
                        d.setAccessType(u.getAccessType());
                        d.setConcern(u.isConcern());
                    });
                }
            }

            // 查到数据源名称
            final DataDir dataDir = iDataDirService.getById(dirId);
            if (!ObjectUtils.isEmpty(dataDir)) {
                // 全部数据源配置信息
                DataConnection connection = DataSourceFactory.getDataConnection(dirId);
                if (!ObjectUtils.isEmpty(connection)) {
                    list.forEach(tableInfo -> {
                        tableInfo.setSourceName(connection.getSchema());
                        tableInfo.setDatabaseType(connection.getDatabaseType());
                    });
                }
            }
        }
        return page;
    }

    @Override
    public TableInfoManageDto getDetail(Long tableId) {
        TableInfoManageDto dto = new TableInfoManageDto();

        // 查询表信息
        if (tableId != null && Objects.nonNull(this.baseMapper)) {
            TableInfo tableInfo = this.baseMapper.selectById(tableId);

            final LambdaQueryWrapper<TgAssetInfo> wq = Wrappers.<TgAssetInfo>lambdaQuery()
                    .eq(TgAssetInfo::getType, AssetType.TABLE)
                    .eq(TgAssetInfo::getRelatedId, tableId)
                    .eq(TgAssetInfo::getDeleted, 0);
            final TgAssetInfo tgAssetInfo = this.tgAssetInfoMapper.selectOne(wq);
            if (tgAssetInfo != null) {
                dto.setQueryLimit(tgAssetInfo.getQueryLimit());
            }

            BeanUtils.copyProperties(tableInfo, dto);
            List<TableFieldInfo> tableFieldInfos = this.getTableFieldMeta(tableId);
            dto.setTableFieldInfos(tableFieldInfos);
            dto.setLocalSql(tableInfo.getLocalSql());
            dto.setRelations(relationService.getList(tableId, null));
            dto.setTemplateAuditProcessEasyDtos(templateService.queryByBaseTableId(tableId));
        }

        return dto;
    }


    @Override
    public List<DataManageFormDto> getListByDirId(Long dirid) {
        // 地图目录全部用户可见,
        List<DataManageFormDto> list = baseMapper.getListByDirId(dirid, 1, 0);

        list.forEach(dataManageFormDto -> {
            // 处理负责人
            if (StringUtils.isNotEmpty(dataManageFormDto.getLeaderName())) {
                SysUser user = sysUserService.selectUserByUserName(dataManageFormDto.getLeaderName());
                if (user != null && StringUtils.isNotEmpty(user.getOrgUserId())) {
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                    if (sinoPassUserDTO != null) {
                        dataManageFormDto.setLeaderNameOri(sinoPassUserDTO.getViewName());
                    }
                }
            }

            // 处理提数模板+审核流程
            List<TemplateAuditProcessEasyDto> templateAuditProcessEasyDtos = templateService.queryProcessesByBaseTableId(dataManageFormDto.getId());
            String tad = templateAuditProcessEasyDtos.stream().map(d -> d.getTemplateName() + "(" + d.getProcessName() + ")").collect(Collectors.joining("、"));
            dataManageFormDto.setTemplateAuditInfo(tad);
        });

        return list;
    }

    @Override
    public List<DataManageFormDto> getListByDirId(Long dirid, Integer menu) {
        List<DataManageFormDto> list = null;
        if (menu == CommonConstants.TABLE_MANAGEMENT && isNotManager()) {
            list = baseMapper.getListByDirId1(dirid, menu, ThreadContextHolder.getSysUser().getUserName());
        } else {
            Integer canView = isNotManager() == false ? 1 : 0;
            list = baseMapper.getListByDirId(dirid, menu, canView);
        }

        list.forEach(dataManageFormDto -> {
            // 处理负责人
            if (StringUtils.isNotEmpty(dataManageFormDto.getLeaderName())) {
                SysUser user = sysUserService.selectUserByUserName(dataManageFormDto.getLeaderName());
                if (user != null && StringUtils.isNotEmpty(user.getOrgUserId())) {
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                    if (sinoPassUserDTO != null) {
                        dataManageFormDto.setLeaderNameOri(sinoPassUserDTO.getViewName());
                    }
                }
            }

            // 处理提数模板+审核流程
            List<TemplateAuditProcessEasyDto> templateAuditProcessEasyDtos = templateService.queryProcessesByBaseTableId(dataManageFormDto.getId());
            String tad = templateAuditProcessEasyDtos.stream().map(d -> d.getTemplateName() + "(" + d.getProcessName() + ")").collect(Collectors.joining("、"));
            dataManageFormDto.setTemplateAuditInfo(tad);
        });

        return list;
    }

    @Override
    public List<String> getOriginTables(Integer sourceId, String dataBase, String schema) {
        final Result<List<String>> tables = metadataClient.getTables(sourceId, dataBase, schema);
        return tables.getResult();
    }

    @Override
    public PageInfo<TableDTO> getMetaTableAsset(Integer pageNum, Integer pageSize, Long tenantId, String realName, String cnName, String dataSourceType) {
        final MetaTableAssetGetParam param = new MetaTableAssetGetParam();
        param.setPageNum(pageNum);
        param.setPageSize(pageSize);
        param.setRealName(realName);
        param.setCnName(cnName);
        param.setTenantId(tenantId);
        param.setDataSourceType(dataSourceType);
        // 获取元数据系统表
        final Result<PageInfo<MetaTableAsset>> metadataTableAssets = metadataClient.getMetadataTableAssets(param);
        final PageInfo<MetaTableAsset> result = metadataTableAssets.getResult();


        // 获取当前系统已经挂接过的表
        final LambdaQueryWrapper<TgMetadataInfo> wq = Wrappers.<TgMetadataInfo>lambdaQuery()
                .like(StringUtils.isNotEmpty(realName), TgMetadataInfo::getMetaDataTable, realName)
                .eq(StringUtils.isNotEmpty(dataSourceType), TgMetadataInfo::getDatabaseType, dataSourceType);

        final List<TgMetadataInfo> tgMetadataInfos = tgMetadataInfoMapper.selectList(wq);

        final List<TableDTO> collect = result.getList().stream()
                .map(asset -> {
                    final TableDTO tableDTO = new TableDTO();
                    final String host = asset.getHost();
                    final Integer port = asset.getPort();
                    final String database = asset.getDatabase();
                    final String schema = asset.getDbSchema();
                    final String tableName = asset.getRealName();

                    tableDTO.setTableId(asset.getId());
                    tableDTO.setTableName(asset.getRealName());
                    tableDTO.setCnName(asset.getCnName());
                    tableDTO.setDataSourceType(asset.getDataSourceType());
                    tableDTO.setDatabase(database);
                    tableDTO.setDbSchema(schema);
                    tableDTO.setOwnerName(asset.getOwnerName());
                    tableDTO.setHost(host);
                    tableDTO.setPort(port);

                    // 是否可选
                    if (tgMetadataInfos.stream().anyMatch(t -> t.getMetaDataDatabase().equals(database) &&
                            t.getMetaDataTable().equals(tableName) &&
                            t.getDatabaseType().equals(dataSourceType) &&
                            t.getIp().equals(host) &&
                            t.getPort().equals(port) &&
                            (StringUtils.isEmpty(schema) ? true : schema.equals(t.getMetaSchema())))) {
                        tableDTO.setSelectable(false);
                    } else {
                        tableDTO.setSelectable(true);
                    }

                    return tableDTO;
                }).collect(Collectors.toList());

        PageInfo<TableDTO> pageInfo = new PageInfo<>(collect, result.getTotal(), Integer.parseInt(Long.toString(result.getPages())), result.getCurrent());

        return pageInfo;
    }

    private boolean isNotManager() {
        return ThreadContextHolder.getSysUser().getRoles() == null || ThreadContextHolder.getSysUser().getRoles().stream().filter(r -> r.getRoleName().contains("管理员")).count() == 0;
    }

    @Override
    public List<TableInfo> findPage(String tableName, Integer pageNum, Integer pageSize) {

        List<TableInfo> list;
        PageHelper.startPage(pageNum, pageSize);
       /* if(StringUtils.isNotEmpty(tableName)){
            list = this.list(Wrappers.<TableInfo>query().eq("status", 1).and(t -> t.like("table_name", "%" + tableName + "%").or().like("table_alias", "%" + tableName + "%")).eq("is_diy",0));
        }else {
            list = this.list(Wrappers.<TableInfo>query().eq("status", 1).eq("is_diy",0));
        }*/
        QueryWrapper queryWrapper;
        if (StringUtils.isNotEmpty(tableName)) {
            queryWrapper = Wrappers.<TableInfo>query().eq("status", 1)
                    .and(t -> t.like("table_name", "%" + tableName + "%").or()
                            .like("table_alias", "%" + tableName + "%")
                    ).eq("is_diy", 0);
        } else {
            queryWrapper = Wrappers.<TableInfo>query().eq("status", 1).eq("is_diy", 0);
        }
        if (!SecurityUtils.getLoginUser().isAdmin()) {
            queryWrapper.eq("leader_name", SecurityUtils.getUsername());
        }
        list = this.list(queryWrapper);
        list.forEach(tableInfo -> {
            DataDir dataDir = iDataDirService.getById(tableInfo.getDirId());
            if (dataDir != null) {
                tableInfo.setDirName(iDataDirService.getById(tableInfo.getDirId()).getDirName());
            }
            //处理负责人
            if (StringUtils.isNotEmpty(tableInfo.getLeaderName())) {
                SysUser user = sysUserService.selectUserByUserName(tableInfo.getLeaderName());
                if (user != null && StringUtils.isNotEmpty(user.getOrgUserId())) {
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                    if (sinoPassUserDTO != null) {
                        tableInfo.setLeaderNameOri(sinoPassUserDTO.getViewName());
                    }
                }
                tableInfo.setLeaderName(sysUserService.selectUserByUserName(tableInfo.getLeaderName()).getRealName());
            }
            tableInfo.setTemplateAuditProcessEasyDtos(templateService.queryProcessesByBaseTableId(tableInfo.getId()));
            //处理可查看人员
            if (StringUtils.isNotBlank(tableInfo.getViewUser())) {
                String[] userids = tableInfo.getViewUser().split(",");
                StringBuffer sb = new StringBuffer();
                for (String userid : userids) {
                    SysUser user = sysUserService.selectUserById(Long.parseLong(userid));
                    if (user != null && StringUtils.isNotEmpty(user.getOrgUserId())) {
                        SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                        if (sinoPassUserDTO != null) {
                            sb.append(sinoPassUserDTO.getViewName()).append(",");
                        }
                    }
                }
                if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
                    tableInfo.setViewUser(sb.deleteCharAt(sb.length() - 1).toString());
                }
            }
        });

        return list;
    }

    @Override
    public List<TableInfo> getMyTableList(Long dirId, String tableName, Integer safeLevel, Boolean concern, Set<Long> dirIdSet, Set<Long> extDirIdSet) {
        return this.baseMapper.getMyTableList(SecurityUtils.getUserId(), dirId, tableName, safeLevel, concern, dirIdSet, extDirIdSet);
    }

    @Override
    public List<ColumnDTO> getMetaColumns(Integer metaTableId) {
        return metadataRegistryClient.getColumns(metaTableId);
    }

    @Override
    @Transactional
    public TableInfoDto getTableBaseInfo(Long tableId) {
        TableInfoDto dto = new TableInfoDto();

        // 查询表信息
        TableInfo tableInfo = this.baseMapper.selectById(tableId);
        BeanUtils.copyProperties(tableInfo, dto);

        // 查询数据库连接
        DataConnection connection = DataSourceFactory.getDataConnection(tableInfo.getDirId());
        if (DatabaseEnum.HIVE2.getFeature().equalsIgnoreCase(connection.getDatabaseType())) {
            // Hive

            final String sql = HiveUtils.descFormattedTable(connection.getSchema()
                    + "." + tableInfo.getTableName());
            List<Map<String, Object>> list = connection.getJdbcOperations().queryForList(sql);
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)) {
                TableInfoDto finalDto = dto;
                list.forEach(map -> {
                    String dataType = (String) map.get("data_type");
                    if (StringUtils.isNotBlank(dataType)) {
                        dataType = dataType.trim();
                        final Object comment = map.get("comment");

                        if ("comment".equals(dataType)) {
                            finalDto.setComment(String.valueOf(comment).trim());

                        } else if ("numRows".equals(dataType)) {
                            finalDto.setTotalRows(Long.parseLong(String.valueOf(comment).trim()));

                        }
                    }
                });

                // 更新表行数信息
                tableInfo.setTotalRow(finalDto.getTotalRows());
                baseMapper.updateById(tableInfo);
            }

        } else if (DatabaseEnum.IMPALA.getFeature().equalsIgnoreCase(connection.getDatabaseType())) {
            // Impala

            final String sql = ImpalaUtils.descFormattedTable(connection.getSchema() + "." + tableInfo.getTableName());
            List<Map<String, Object>> list = connection.getJdbcOperations().queryForList(sql);
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)) {
                TableInfoDto finalDto = dto;
                list.forEach(map -> {
                    String name = (String) map.get("name");
                    if (StringUtils.isNotBlank(name)) {
                        name = name.trim();
                        final Object comment = map.get("comment");

                        if ("comment".equals(name)) {
                            finalDto.setComment(String.valueOf(comment).trim());

                        } else if ("numRows".equals(name)) {
                            finalDto.setTotalRows(Long.parseLong(String.valueOf(comment).trim()));

                        }
                    }
                });

                // 更新表行数信息
                tableInfo.setTotalRow(finalDto.getTotalRows());
                baseMapper.updateById(tableInfo);
            }

        } else if (DatabaseEnum.MYSQL.getFeature().equalsIgnoreCase(connection.getDatabaseType())) {
            // MySQL

            String sql = String.format("select t.TABLE_NAME,t.DATA_LENGTH as storeSize,t.CREATE_TIME,t.UPDATE_TIME,t.TABLE_COMMENT as comment ,(select count(*) from information_schema.`COLUMNS` c where c.TABLE_NAME='%s') as totalFields from information_schema.`TABLES`  t  where t.TABLE_SCHEMA='%s' and t.TABLE_NAME='%s'", tableInfo.getTableName(), connection.getSchema(), tableInfo.getTableName());
            dto = connection.getJdbcOperations().queryForObject(sql, new BeanPropertyRowMapper<>(TableInfoDto.class));

            dto.setDirPath(DirCache.getDir(tableInfo.getDirId()).getDirPath());
            dto.setTableAlias(tableInfo.getTableAlias());
            dto.setUpdateTime(tableInfo.getUpdateTime());
            dto.setSafeLevel(tableInfo.getSafeLevel());
            dto.setDirId(tableInfo.getDirId());
            dto.setId(tableInfo.getId());
            dto.setSchemeCycle(tableInfo.getSchemeCycle());
            dto.setSchemeStatus(tableInfo.getSchemeStatus());
            dto.setGroupName(tableInfo.getGroupName());
            dto.setLeaderName(tableInfo.getLeaderName());
        }

        // 缓存总行数
        String key = tableRowKey + tableId;
        Long rows = tableTotalRowCache.get(key);
        if (rows == null) {
            rows = tableInfo.getTotalRow();
            AsyncManager.me().execute(AsyncFactory.updateTableRow(tableInfo, key));
        }
        dto.setTotalRows(rows);

        // 表权限
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            QueryWrapper<SysUserTable> eq = Wrappers.<SysUserTable>query().eq("table_id", tableId);
            eq.eq("user_id", SecurityUtils.getUserId());
            SysUserTable userTable = userTableService.getOne(eq);
            if (userTable != null) {
                dto.setAccessType(userTable.getAccessType());
                dto.setConcern(userTable.isConcern());
            }
        }

        List<GroupLeaderDto> leaderDtos = groupDataDirService.queryGroupLeader(tableInfo.getDirId());
        if (!ObjectUtils.isEmpty(leaderDtos)) {
            StringBuffer groupName = new StringBuffer();
            StringBuffer leaderName = new StringBuffer();
            leaderDtos.forEach(l -> {
                groupName.append(l.getGroupName());
                groupName.append("、");
                leaderName.append(l.getUserName());
                leaderName.append("、");
            });
            groupName.deleteCharAt(groupName.length() - 1);
            leaderName.deleteCharAt(leaderName.length() - 1);
            dto.setDeptName(groupName.toString());
            dto.setManagerName(leaderName.toString());
        }

        // 1.3.3.0 >> 负责部门和负责人需求调整
        final List<GroupLeaderDto> list = groupDataDirMapper.queryGroupUser(tableInfo.getDirId());
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)) {
            List<Map<String, Object>> mapList = new ArrayList<>();
            // 按照分组名称分组
            final Map<String, List<GroupLeaderDto>> groupMap = list.stream()
                    .collect(Collectors.groupingBy(GroupLeaderDto::getGroupName));
            groupMap.forEach((groupName, groupList) -> {
                // 负责部门和负责人级联列表
                Map<String, Object> map = new HashMap<>();
                map.put("name", groupName);
                map.put("children", groupList.stream()
                        .map(groupLeaderDto -> Collections.singletonMap("name", groupLeaderDto.getUserName()))
                        .collect(Collectors.toList()));
                mapList.add(map);
            });

            dto.setList(mapList);
        }

        // 1.6.4.0 >> 新增参数：是否有回传数据，true有，false没有
        if ("dws_pd_dc_sic_base".equals(tableInfo.getTableName())) {
            dto.setHasReturnData(true);
        }

        return dto;
    }


    @Override
    public List<String> getEnableDataSourceType() {
        final Result<List<String>> result = metadataClient.enableDataSourceTypes();
        return result.getResult();
    }


    /**
     * @param metaTableId
     * @param targetSourceType
     * @param createTarget     true是创建
     * @return
     */
    @Override
    public ExchangeColumnMapper getColumns(Integer metaTableId, String targetSourceType, Boolean createTarget, Integer sourceId, String database, String schema, String table) {
        final Result<MetaTableDTO> dtoResult = metadataClient.getMetaTableDTO(metaTableId);
        final MetaTableDTO tableDTO = dtoResult.getResult();
        final Result<List<MetaColumnDTO>> result = metadataClient.getFullMetaColumn(metaTableId);
        ExchangeColumnMapper mapper = new ExchangeColumnMapper();
        List<ExchangeColumnDTO> sourceColumn = new ArrayList<>();
        List<ExchangeColumnDTO> targetColumn = new ArrayList<>();
        if (result.isSuccess()) {
            final List<MetaColumnDTO> resultResult = result.getResult();
            final String dataSourceType = tableDTO.getDataSourceType();
            if (createTarget) {
                // 创建表
                final TypeConvertParam typeConvertParam = new TypeConvertParam();
                typeConvertParam.setSourceType(dataSourceType);
                typeConvertParam.setTargetType(targetSourceType);
                typeConvertParam.setColumns(resultResult);
                log.info("请求类型转移入参：{}", JSON.toJSONString(typeConvertParam));
                final List<MetaColumnDTO> dtos = integrateSyncProcessDefService.convertType(typeConvertParam);
                log.info("请求类型转移出参：{}", JSON.toJSONString(dtos));
                final Map<String, MetaColumnDTO> nameMap = dtos.stream().collect(Collectors.toMap(MetaColumnDTO::getColumnName, v -> v));
                resultResult.stream()
                        .forEach(dto -> {
                            final ExchangeColumnDTO source = new ExchangeColumnDTO();
                            final ExchangeColumnDTO target = new ExchangeColumnDTO();
                            // find same column name from nameMap and match target type
                            final MetaColumnDTO metaColumnDTO = nameMap.get(dto.getColumnName());
                            source.setColumnName(dto.getColumnName());
                            String typeName = dto.getTypeName();
                            source.setColumnType(getTypeName(typeName, dto.getLength(), dto.getScale()));
                            source.setColumnRemark(dto.getRemake());

                            if (Objects.nonNull(metaColumnDTO)) {
                                target.setColumnName(metaColumnDTO.getColumnName());
                                target.setColumnType(metaColumnDTO.getTypeName());
                                targetColumn.add(target);
                            }

                            sourceColumn.add(source);
                        });
            } else {
                // 已有表
                final Result<List<MetaColumnDTO>> originResult = metadataClient.getOriginColumns(sourceId, database, schema, table);
                if (result.isSuccess()) {
                    result.getResult().forEach(dto -> {
                        final ExchangeColumnDTO source = new ExchangeColumnDTO();
                        source.setColumnName(dto.getColumnName());
                        source.setColumnType(dto.getTypeName());
                        source.setColumnRemark(dto.getRemake());
                        sourceColumn.add(source);
                    });
                }
                if (originResult.isSuccess()) {
                    originResult.getResult()
                            .forEach(dto -> {
                                final ExchangeColumnDTO target = new ExchangeColumnDTO();
                                target.setColumnName(dto.getColumnName());
                                target.setColumnType(dto.getTypeName());
                                targetColumn.add(target);
                            });
                }
            }
            mapper.setSourceColumn(sourceColumn);
            mapper.setTargetColumn(targetColumn);
            return mapper;
        }
        return null;
    }


    private String getTypeName(String typeName, Integer length, Integer scale) {
        final String upType = typeName.toUpperCase();
        if ((StringUtils.equalsIgnoreCase(upType, "DECIMAL") || StringUtils.equalsIgnoreCase(upType, "NUMERIC"))
                && Objects.nonNull(length) && Objects.nonNull(scale)) {
            typeName = typeName + "(" + length + "," + scale + ")";
        } else if (Objects.nonNull(length)) {
            typeName = typeName + "(" + length + ")";
        } else {
            typeName = typeName;
        }
        return typeName;
    }

    // 目标数据源id
    @Override
    public List<ColumnDTO> getWriteColumns(Integer metaTableId, Integer dataSourceId) {
        List<TgMetadataInfo> infos = tgMetadataInfoMapper.selectList(new QueryWrapper<TgMetadataInfo>().lambda()
                .eq(TgMetadataInfo::getMetaDataId, metaTableId));
        if (CollectionUtils.isEmpty(infos)) {
            log.error("源表不存在: metaTableId={}", metaTableId);
            return Collections.emptyList();
        }

        TgMetadataInfo table = infos.get(0);
        Result<com.sinohealth.data.intelligence.api.datasource.dto.DataSourceDTO> dsResult = datasourceApi.detail(dataSourceId);
        if (!dsResult.isSuccess()) {
            log.error("数据源不存在: dsResult={}", dsResult);
            return Collections.emptyList();
        }

        List<ColumnDTO> readerList = metadataRegistryClient.getColumns(metaTableId);
        if (Objects.equals(dsResult.getResult().getType(), table.getDatabaseType())) {
            return readerList;
        }

        return readerList.stream().map(v -> {
            ColumnDTO dto = new ColumnDTO();
            dto.setRealName(v.getRealName());
            // TODO 跨源的类型处理问题
            dto.setDataType(v.getDataType());
            dto.setLength(v.getLength());
            dto.setScale(v.getScale());
            dto.setCnName(v.getCnName());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DataSourceDTO> getDatasource(Long tenantId, String dataSourceType) {
        final List<com.sinohealth.data.intelligence.api.metadata.dto.DataSourceDTO> dataSource = metadataClient.getDataSource(dataSourceType, true, tenantId).getResult();
        return dataSource.stream().map(a -> {
            final DataSourceDTO dataSourceDTO = new DataSourceDTO();
            dataSourceDTO.setId(a.getSourceId());
            dataSourceDTO.setName(a.getSourceName());
            dataSourceDTO.setIp(a.getIp());
            dataSourceDTO.setPort(a.getPort());
            return dataSourceDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> getDatabase(Integer sourceId) {
        final Result<List<String>> result = metadataClient.getDataBase(sourceId);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            throw new CustomException(result.getMessage());
        }
    }

    @Override
    public List<String> getCluster(Integer datasourceId) {
        if (Objects.nonNull(datasourceId)) {
            final Result<List<String>> result = metadataClient.queryClickhouseCluster(datasourceId);
            if (result.isSuccess()) {
                return result.getResult();
            }
        }
        return null;
    }

    @Override
    public List<String> getSchema(Integer sourceId) {
        final Result<List<String>> result = metadataClient.getSchema(sourceId);
        if (result.isSuccess()) {
            return result.getResult();
        } else {
            throw new CustomException(result.getMessage());
        }
    }

    @Override
    public List<TableDTO> getTables(String databaseOrSchema, String ip, Integer port, Long tenantId, String tableName) {
        // 根据ip，端口，库/schema，获取已经挂接过的表
        final LambdaQueryWrapper<TgMetadataInfo> wq = Wrappers.<TgMetadataInfo>lambdaQuery()
                .eq(TgMetadataInfo::getIp, ip)
                .eq(TgMetadataInfo::getPort, port)
                .eq(TgMetadataInfo::getMetaDataDatabase, databaseOrSchema);
        final List<TgMetadataInfo> tgMetadataInfos = tgMetadataInfoMapper.selectList(wq);
        List<String> hitchedTables = tgMetadataInfos.stream().map(TgMetadataInfo::getMetaDataTable).collect(Collectors.toList());
        final List<AssetsTableDTO> tableDTOS = metadataRegistryClient.getTablesByIpPortAndTenantId(tenantId, ip, port, tableName);
        final List<TableDTO> dtos = tableDTOS.stream().map(t -> {
            final TableDTO tableDTO = new TableDTO();
            tableDTO.setTableId(t.getMetaTableId());
            tableDTO.setTableName(t.getMetaTableName());
            // 设置true，false
            if (hitchedTables.contains(t.getMetaTableName())) {
                tableDTO.setSelectable(false);
            } else {
                tableDTO.setSelectable(true);
            }
            return tableDTO;
        }).collect(Collectors.toList());
        return dtos;
    }

    @Override
    public List<TableFieldInfo> getTableFieldMeta(Long tableId) {
        final List<TableFieldInfo> fieldInfoList = fieldInfoService.list(Wrappers.<TableFieldInfo>query()
                .eq("table_id", tableId)
                .eq("status", 1)
                .orderByAsc("sort", "id"));
        TableInfo tableInfo = tableInfoMapper.selectById(tableId);

        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(fieldInfoList)) {
            fieldInfoList.forEach(tableFieldInfo -> {
                if (tableInfo != null) {
                    tableFieldInfo.setTableName(tableInfo.getTableName());
                }
                //处理关联字段逻辑
            });
            /*final List<TableMappingDTO> mappingList = tableMappingMapper.listMapping(tableId, null);

            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(mappingList)) {
                final Map<Long, List<TableMappingDTO>> mappingMap = mappingList.stream()
                        .collect(Collectors.groupingBy(TableMappingDTO::getFieldId));

                fieldInfoList.forEach(tableFieldInfo -> {
                    final List<TableMappingDTO> list = mappingMap.get(tableFieldInfo.getId());
                    if(tableInfo != null){
                        tableFieldInfo.setTableName(tableInfo.getTableName());
                    }
                    if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)) {
                        StringBuilder mappingName = new StringBuilder();
                        list.forEach(tableMapping -> mappingName
                                .append(StringUtils.isNotBlank(tableMapping.getMappingFieldAlias())
                                        ? tableMapping.getMappingFieldAlias()
                                        : tableMapping.getMappingFieldName())
                                .append("、"));
                        mappingName.deleteCharAt(mappingName.length() - 1);

                        // 映射字段名称
                        tableFieldInfo.setMappingName(mappingName.toString());
                    }
                });
            }*/
        }

        return fieldInfoList;
    }

    @Override
    public List<TableMapDto> getTableMap(Long dirId, Long tableId, String tableName, String fieldName, Integer accessType) {

        List<Long> tableIds = null;
        if (!ObjectUtils.isEmpty(accessType)) {//查询数据访问权限
            List<SysUserTable> userTableList = userTableService.list(Wrappers.<SysUserTable>query().eq("dir_id", dirId).eq("user_id", SecurityUtils.getUserId()).eq("access_type", accessType));

            if (ObjectUtils.isEmpty(userTableList)) {
                return new ArrayList<>();
            }

            tableIds = userTableList.stream().map(u -> u.getTableId()).collect(Collectors.toList());
        }

        List<TableMapDto> list = this.baseMapper.getTableMap(dirId, tableId, tableIds, tableName, fieldName);

        if (ObjectUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<TableRelationDto> relations = relationService.getList(tableId, dirId);

        // 去重
        Set<Long> removeSet = new HashSet<>();
        Set<Long> depositSet = new HashSet<>();
        relations.forEach(tableRelationDto -> {
            relations.forEach(tableRelationDto1 -> {
                if (tableRelationDto.getTableId().equals(tableRelationDto1.getRefTableId())
                        && tableRelationDto.getRefTableId().equals(tableRelationDto1.getTableId())
                        && tableRelationDto.getFieldId().equals(tableRelationDto1.getRefFieldId())
                        && tableRelationDto.getRefFieldId().equals(tableRelationDto1.getFieldId())
                        && !depositSet.contains(tableRelationDto.getId())) {

                    removeSet.add(tableRelationDto.getId());
                    depositSet.add(tableRelationDto1.getId());
                }
            });
        });
        relations.removeIf(tableRelationDto -> removeSet.contains(tableRelationDto.getId()));

//        List<Long> relTableIds = new ArrayList<>(relations.size());
//        list.forEach(t -> {
//            t.setLevelId(t.getDirId());
//            t.setRelationList(relations.stream().filter(r -> {
//                boolean result = r.getTableId().equals(t.getId());
//                if (result) {
//                    relTableIds.add(r.getRefTableId());
//                }
//                return result;
//            }).collect(Collectors.toList()));
//        });
        List<Long> relTableIds = relations.stream().map(TableRelationDto::getRefTableId).collect(Collectors.toList());
        List<Long> ids = relTableIds.stream().filter(t -> !list.stream().anyMatch(a -> a.getId().equals(t))).collect(Collectors.toList());
        if (!ObjectUtils.isEmpty(ids)) {
            List<TableMapDto> extList = this.baseMapper.getTableMap(null, null, ids, null, null);
            list.forEach(t -> {
                t.setLevelId(t.getDirId());
            });
            list.addAll(extList);

        }
        list.forEach(t -> {
            t.setLevelId(t.getDirId());
            t.setRelationList(relations.stream().filter(r -> r.getRefTableId().equals(t.getId())).collect(Collectors.toList()));
        });

        //用户权限
        List<SysUserTable> userTables = userTableService.list(Wrappers.<SysUserTable>query().eq("user_id", SecurityUtils.getUserId()));
        list.forEach(t -> {
            userTables.stream().filter(u -> u.getTableId().equals(t.getId())).findFirst().ifPresent(u -> {
                t.setAccessType(u.getAccessType());
            });
        });


        return list;
    }

    @Override
    public List<TableMapDto> getTableLineage(Long tableId, int maxLevel, String fieldName) {
        TableInfo tableInfo = this.getById(tableId);
        List<TableMapDto> dtoList = new ArrayList<>();
        TableMapDto dto = new TableMapDto();
        dto.setLevelId(0L);
        dto.setTableName(tableInfo.getTableName());
        dto.setTableAlias(tableInfo.getTableAlias());
        dto.setSafeLevel(tableInfo.getSafeLevel());
        dto.setId(tableInfo.getId());
        dto.setDirId(tableInfo.getDirId());
        dtoList.add(dto);

        findChildLineage(dtoList, dto, 1L, fieldName);


        return dtoList;
    }

    private void findChildLineage(List<TableMapDto> dtoList, TableMapDto dto, long level, String fieldName) {
        TableSql tableSql = tableSqlService.getById(dto.getId());

        if (tableSql != null) {

            LineageColumn root = new LineageColumn();
            TreeNode<LineageColumn> rootNode = new TreeNode<>(root);
            LineageUtils.columnLineageAnalyzer(tableSql.getGenerateSql(), rootNode);
            List<String> selectColumnList = new ArrayList<>();
            Map<String, TableMapDto> map = new HashMap<>();

            for (TreeNode<LineageColumn> e : rootNode.getChildren()) {
                Set<LineageColumn> leafNodes = e.getAllLeafData();
                selectColumnList.add(e.getData().getTargetColumnName());
                if (!StringUtils.isEmpty(fieldName) && !e.getData().getTargetColumnName().equals(fieldName)) {
                    continue;
                }
                for (LineageColumn f : leafNodes) {
                    if (f.getIsEnd()) {

                        TableMapDto child = null;
                        if (map.containsKey(f.getSourceTableName())) {
                            child = map.get(f.getSourceTableName());
                        } else {
                            child = new TableMapDto();
                            child.setTableName(f.getSourceTableName());
                            TableInfo t = this.getOne(Wrappers.<TableInfo>query().eq("status", 1).eq("table_name", child.getTableName()));
                            if (t == null) {
                                log.warn("cannot found the table {}", child.getTableName());
                                continue;
                            }

                            child.setSafeLevel(t.getSafeLevel());
                            child.setDirId(t.getDirId());
                            child.setTableAlias(t.getTableAlias());
                            child.setLevelId(level);
                            child.setId(t.getId());

                            map.put(f.getSourceTableName(), child);
                            dtoList.add(child);
                        }

                        TableFieldInfo info = fieldInfoService.getOne(Wrappers.<TableFieldInfo>query().eq("field_name", f.getTargetColumnName()).eq("table_id", child.getId()));
                        child.getFields().add(info);


                        if (!dto.getRelationList().stream().anyMatch(d -> d.getRefTableId().equals(info.getTableId()))) {
                            TableRelationDto relationDto = new TableRelationDto();
                            relationDto.setTableId(dto.getId());
                            relationDto.setFieldName(e.getData().getTargetColumnName());
                            relationDto.setRefTableId(child.getId());
                            relationDto.setRefTableAlias(child.getTableAlias());
                            relationDto.setRefTableName(child.getTableName());
                            dto.getRelationList().add(relationDto);
                        }


                    }

                }
            }


            dto.setFields(fieldInfoService.list(Wrappers.<TableFieldInfo>query().in("field_name", selectColumnList).eq("table_id", dto.getId())));

        }
    }

    private String buildSqlSelect(String select, String tableName, String whereSql) {
        return " select " + select + " from " + tableName + whereSql;
    }

    private String buildCountSql(String tableName, String whereSql) {
        return " select count(*) from " + tableName + whereSql;
    }

    private String buildSelectSql(String tableName, List<TableFieldInfo> fields, String whereSql, Integer startRow,
                                  Integer pageSize, List<TableDataDto.Header> headerList, String sortBy,
                                  String sortingField, List<TableMappingDTO> mappingList) {
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
                headerList.add(new TableDataDto.Header(field.getId(), field.getFieldName(), field.getFieldAlias(),
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

    /**
     * 构建 Hive 查询语句
     *
     * @param tableName    表名
     * @param fields       字段信息列表
     * @param whereSql     过滤语句
     * @param startRow     分页，起始页
     * @param pageSize     分页，每页数量
     * @param headerList   表头
     * @param sortBy       排序类型
     * @param sortingField 排序字段
     * @return Hive 查询语句
     * @author linkaiwei
     * @date 2021-10-22 14:37:27
     * @since 1.4.5.0
     */
    private String buildSelectSqlByHive(String tableName, List<TableFieldInfo> fields, String whereSql,
                                        Integer startRow, Integer pageSize, List<TableDataDto.Header> headerList,
                                        String sortBy, String sortingField) {
        if (StringUtils.isBlank(sortBy) && !StringUtils.isBlank(sortingField)) {
            throw new CustomException("sortingField存在，sortBy不能为空");
        }
        if (!StringUtils.isBlank(sortBy) && StringUtils.isBlank(sortingField)) {
            throw new CustomException("sortBy存在，sortingField不能为空");
        }

        StringBuffer sql = new StringBuffer();
        String id = null;
        for (TableFieldInfo field : fields) {
            if (field.isPrimaryKey()) {
                id = field.getFieldName();
            }
            if (sql.length() == 0) {
                sql.append("SELECT ");

            } else {
                sql.append(" , ");
            }
            sql.append("`");
            sql.append(field.getFieldName());
            sql.append("`");

            if (headerList != null) {
                headerList.add(new TableDataDto.Header(field.getId(), field.getFieldName(), field.getFieldAlias(), field.getDataType(),
                        field.isPrimaryKey(), null, null, null, field.getDefaultShow()));
            }
        }

        sql.append(String.format(" FROM (SELECT row_number() OVER (ORDER BY %s) AS row_number, t1.* FROM %s t1 ",
                id, tableName));

        // 过滤
        sql.append(whereSql);

        // 排序
        if (!StringUtils.isBlank(sortBy) && !StringUtils.isBlank(sortingField)) {
            SortingFieldEnum sortingFieldEnum = SortingFieldEnum.valueOfCode(sortBy);
            if (sortingFieldEnum == null) {
                throw new CustomException("sortBy值不合法");
            }

            List<String> sortingFieldList = Arrays.stream(sortingField.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            sql.append(" ORDER BY ");
            sortingFieldList.forEach(sortField -> sql.append(sortField).append(" ").append(sortBy).append(", "));

            sql.replace(sql.length() - 2, sql.length(), "");

        } else if (id != null) {
            sql.append(" ORDER BY ").append(id).append(" DESC ");
        }

        sql.append(" ) t ");

        // 分页
        if (startRow != null && pageSize != null) {
            sql.append(" WHERE row_number > ").append(startRow)
                    .append(" AND row_number <= ").append(startRow + pageSize);
        }

        return sql.toString();
    }

    /**
     * 构建 Hive 查询语句
     *
     * @param tableName    表名
     * @param fields       字段信息列表
     * @param whereSql     过滤语句
     * @param startRow     分页，起始页
     * @param pageSize     分页，每页数量
     * @param headerList   表头
     * @param sortBy       排序类型
     * @param sortingField 排序字段
     * @return Hive 查询语句
     * @author linkaiwei
     * @date 2021-10-22 14:37:27
     * @since 1.4.5.0
     */
    private String buildSelectSqlByHive2(String tableName, List<TableFieldInfo> fields, String whereSql,
                                         Integer startRow, Integer pageSize, List<TableDataDto.Header> headerList,
                                         String sortBy, String sortingField) {
        if (StringUtils.isBlank(sortBy) && !StringUtils.isBlank(sortingField)) {
            throw new CustomException("sortingField存在，sortBy不能为空");
        }
        if (!StringUtils.isBlank(sortBy) && StringUtils.isBlank(sortingField)) {
            throw new CustomException("sortBy存在，sortingField不能为空");
        }

        StringBuffer sql = new StringBuffer("SELECT *");
        String id = null;
        for (TableFieldInfo field : fields) {
            if (field.isPrimaryKey()) {
                id = field.getFieldName();
            }
//            if (sql.length() == 0) {
//                sql.append("SELECT ");
//
//            } else {
//                sql.append(" , ");
//            }
//            sql.append("`");
//            sql.append(field.getFieldName());
//            sql.append("`");

            if (headerList != null) {
                headerList.add(new TableDataDto.Header(field.getId(), field.getFieldName(), field.getFieldAlias(), field.getDataType(),
                        field.isPrimaryKey(), null, null, null, field.getDefaultShow()));
            }
        }

        sql.append(String.format(" FROM %s ", tableName));

        // 过滤
        sql.append(whereSql);

        // 排序
        if (!StringUtils.isBlank(sortBy) && !StringUtils.isBlank(sortingField)) {
            SortingFieldEnum sortingFieldEnum = SortingFieldEnum.valueOfCode(sortBy);
            if (sortingFieldEnum == null) {
                throw new CustomException("sortBy值不合法");
            }

            List<String> sortingFieldList = Arrays.stream(sortingField.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            sql.append(" ORDER BY ");
            sortingFieldList.forEach(sortField -> sql.append(sortField).append(" ").append(sortBy).append(", "));

            sql.replace(sql.length() - 2, sql.length(), "");

//        } else if (id != null) {
//            sql.append(" ORDER BY ").append(id).append(" DESC ");
        }
        if (pageSize != null) {
            sql.append(" LIMIT ");
            sql.append(pageSize);
        }

        return sql.toString();
    }

    /**
     * 构建 Impala 查询语句
     *
     * @param tableName    表名
     * @param fields       字段信息列表
     * @param whereSql     过滤语句
     * @param startRow     分页，起始页
     * @param pageSize     分页，每页数量
     * @param headerList   表头
     * @param sortBy       排序类型
     * @param sortingField 排序字段
     * @return Impala 查询语句
     * @author linkaiwei
     * @date 2021-10-22 14:37:27
     * @since 1.4.5.0
     */
    private String buildSelectSqlByImpala(String tableName, List<TableFieldInfo> fields, String whereSql,
                                          Integer startRow, Integer pageSize, List<TableDataDto.Header> headerList,
                                          String sortBy, String sortingField) {
        if (StringUtils.isBlank(sortBy) && !StringUtils.isBlank(sortingField)) {
            throw new CustomException("sortingField存在，sortBy不能为空");
        }
        if (!StringUtils.isBlank(sortBy) && StringUtils.isBlank(sortingField)) {
            throw new CustomException("sortBy存在，sortingField不能为空");
        }
        if (CollectionUtils.isEmpty(fields)) {
            throw new CustomException("查询字段不能为空");
        }

        StringBuffer sql = new StringBuffer("SELECT *");
        String id = null;
        for (TableFieldInfo field : fields) {
            if (field.isPrimaryKey()) {
                id = field.getFieldName();
            }
//            if (sql.length() == 0) {
//                sql.append("SELECT ");
//
//            } else {
//                sql.append(" , ");
//            }
//            sql.append("`");
//            sql.append(field.getFieldName());
//            sql.append("`");

            if (headerList != null) {
                headerList.add(new TableDataDto.Header(field.getId(), field.getFieldName(), field.getFieldAlias(), field.getDataType(),
                        field.isPrimaryKey(), null, null, null, field.getDefaultShow()));
            }
        }

        sql.append(String.format(" FROM %s t ", tableName));

        // 过滤
        sql.append(whereSql);

        // 排序
        if (!StringUtils.isBlank(sortBy) && !StringUtils.isBlank(sortingField)) {
            SortingFieldEnum sortingFieldEnum = SortingFieldEnum.valueOfCode(sortBy);
            if (sortingFieldEnum == null) {
                throw new CustomException("sortBy值不合法");
            }

            List<String> sortingFieldList = Arrays.stream(sortingField.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            sql.append(" ORDER BY ");
            sortingFieldList.forEach(sortField -> sql.append(sortField).append(" ").append(sortBy).append(", "));

            sql.replace(sql.length() - 2, sql.length(), "");

        } else if (id != null) {
            sql.append(" ORDER BY ").append(id).append(" DESC ");
        }

        // 分页
        if (startRow != null && pageSize != null) {
            sql.append(" LIMIT 50");
        }

        return sql.toString();
    }

    private String buildWhereSql(String fieldName, String condition, String leftVal, String rightVal, List<TableFieldInfo> fields) {
        StringBuffer whereSql = new StringBuffer("");
        leftVal = SqlFilter.filter(leftVal);
        rightVal = SqlFilter.filter(rightVal);
        //添加查询条件
        if (!StringUtils.isEmpty(fieldName) && !StringUtils.isEmpty(condition)
                && (!StringUtils.isEmpty(leftVal)) || "null".equals(condition) || "notNull".equals(condition)) {
            if (fields.stream().anyMatch(t -> t.getFieldName().equals(fieldName))) {
                whereSql.append(" where ");
                whereSql.append("t.").append(fieldName);


                switch (condition) {
                    case "eq":// = 等于
                        whereSql.append(String.format(" = '%s'", leftVal));
                        break;
                    case "lt"://小于
                        whereSql.append(String.format(" < '%s'", leftVal));
                        break;
                    case "le"://小于等于
                        whereSql.append(String.format(" <= '%s'", leftVal));
                        break;
                    case "gt"://大于
                        whereSql.append(String.format(" > '%s'", leftVal));
                        break;
                    case "ge"://大于等于
                        whereSql.append(String.format(" >= '%s'", leftVal));
                        break;
                    case "ne"://不等于
                        whereSql.append(String.format(" <> '%s'", leftVal));
                        break;
                    case "contain"://包含
                        whereSql.append(String.format(" like '%s'", "%" + leftVal + "%"));
                        break;
                    case "startWith"://开头为
                        whereSql.append(String.format(" like '%s'", leftVal + "%"));
                        break;
                    case "endWith"://结尾为
                        whereSql.append(String.format(" like '%s'", "%" + leftVal));
                        break;
                    case "notContain"://不包含
                        whereSql.append(String.format(" not like '%s'", "%" + leftVal + "%"));
                        break;
                    case "null"://空值
                        whereSql.append(" is null ");
                        break;
                    case "notNull"://非空
                        whereSql.append(" is not null ");
                        break;
                    case "(a,b)":
                        if (StringUtils.isEmpty(rightVal)) {
                            throw new CustomException("缺少右边值");
                        }
                        whereSql.append(String.format(" > '%s' and t.%s < '%s'", leftVal, fieldName, rightVal));
                        break;
                    case "(a,b]":
                        if (StringUtils.isEmpty(rightVal)) {
                            throw new CustomException("缺少右边值");
                        }
                        whereSql.append(String.format(" > '%s' and t.%s <= '%s'", leftVal, fieldName, rightVal));
                        break;
                    case "[a,b)":
                        if (StringUtils.isEmpty(rightVal)) {
                            throw new CustomException("缺少右边值");
                        }
                        whereSql.append(String.format(" >= '%s' and t.%s < '%s'", leftVal, fieldName, rightVal));
                        break;
                    case "[a,b]":
                        if (StringUtils.isEmpty(rightVal)) {
                            throw new CustomException("缺少右边值");
                        }
                        whereSql.append(String.format(" >= '%s' and t.%s <='%s'", leftVal, fieldName, rightVal));
                        break;
                    default:
                        throw new CustomException("暂不支持此查询");
                }


            }

        }
        return whereSql.toString();
    }

    /**
     * 导出表数据，返回Excel文档
     *
     * @param tableId      表ID
     * @param fieldName    字段名称
     * @param condition    操作
     * @param leftVal      左边值
     * @param rightVal     右边值
     * @param sortBy       排序
     * @param sortingField 排序字段
     * @return Excel文档名称
     * @author linkaiwei
     * @date 2021-11-08 14:29:44
     * @since 1.6.1.0
     */
    @Override
    public String exportTableData(Long tableId, String fieldName, String condition, String leftVal, String rightVal,
                                  String sortBy, String sortingField) {
        TableInfo tableInfo = this.getById(tableId);
        String dataName = DataSourceFactory.getDataConnection(tableInfo.getDirId()).getSchema();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tableId", tableId);
        jsonObject.put("fieldName", fieldName);
        jsonObject.put("condition", condition);
        jsonObject.put("leftVal", leftVal);
        jsonObject.put("rightVal", rightVal);

        TableTask tableTask = new TableTask();
        tableTask.setTaskType(TaskType.EXPORT.getId());
        tableTask.setParams(jsonObject.toJSONString());
        tableTask.setDirId(tableInfo.getDirId());
        tableTask.setTableId(tableId);
        tableTask.setOperator(SecurityUtils.getUsername());
        tableTask.setOperatorId(SecurityUtils.getUserId());
        tableTask.setCreateTime(DateUtils.getNowDate());
        tableTask.setSpeedOfProgress(SpeedOfProgressType.RUNNING.getId());
        tableTask.setContent("导出 " + dataName + "库" + tableInfo.getTableName() + "表 到本地");
        tableTask.setBatchNumber(IdUtils.generateBusinessSerialNo());
        iTableTaskService.save(tableTask);
        String filename = DateUtils.dateTimeNow("yyyyMMddHHmmssSSS") + "_" + tableInfo.getTableName() + ".xlsx";
        try {
            List<TableFieldInfo> fields = fieldInfoService.list(Wrappers.<TableFieldInfo>query()
                    .eq("table_id", tableId)
                    .eq("status", 1));
            String whereSql = buildWhereSql(fieldName, condition, leftVal, rightVal, fields);

            // 获取数据库的连接
            final JdbcOperations jdbcOperations = DataSourceFactory.getDataConnection(tableInfo.getDirId())
                    .getJdbcOperations();

            // 查询表总数
            Long total = jdbcOperations.queryForObject(buildCountSql(tableInfo.getTableName(), whereSql), Long.class);
            long pageNum = 1;
            long pageSize = 60000;

            List<String> sql = new ArrayList<>();
            while (true) {
                long startRow = pageNum > 0 ? (pageNum - 1) * pageSize : 0L;
                if (total < startRow) {
                    break;
                }
                String selectSql = buildSelectSql(tableInfo.getTableName(), fields, whereSql, (int) startRow,
                        (int) pageSize, null, sortBy, sortingField, null);
                sql.add(selectSql);
                pageNum++;
            }
            List<Map<String, Object>> result = new ArrayList<>();
            sql.parallelStream().forEach(i -> result.addAll(jdbcOperations.queryForList(i)));

            // 异步生成EXCEL文件
            AsyncManager.me().execute(new TimerTask() {
                @Override
                public void run() {
                    exportExcel(tableTask.getId(), filename, tableInfo.getTableName(), fields, result);
                }
            });

            AsyncManager.me().execute(AsyncFactory.createTableLog(tableInfo, SecurityUtils.getUserId(),
                    SecurityUtils.getUsername(), LogType.data_export, "导出数据", 1, false, new Date()));

        } catch (Exception e) {
            log.error(e.getMessage());

            LambdaUpdateWrapper<TableTask> luw = new LambdaUpdateWrapper<>();
            luw.eq(TableTask::getId, tableTask.getId());
            luw.set(TableTask::getRemarks, "导出失败:" + e.getMessage());
            luw.set(TableTask::getCompleteTime, DateUtils.getNowDate());
            luw.set(TableTask::getSpeedOfProgress, SpeedOfProgressType.ERROR.getId());
            iTableTaskService.update(luw);

            throw new CustomException("导出失败！");
        }
        return filename;
    }

    /**
     * 生成EXCEL文件
     *
     * @param taskId    任务ID
     * @param filename  文件名称
     * @param tableName 表名称
     * @param fields    字段列表
     * @param result    数据
     * @author linkaiwei
     * @date 2021-08-11 16:52:15
     * @since 1.3.3.0
     */
    public void exportExcel(Long taskId, String filename, String tableName, List<TableFieldInfo> fields,
                            List<Map<String, Object>> result) {
        try {
            File file = new File(DataPlatformConfig.getDownloadPath() + filename);
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new CustomException("生成EXCEL文件失败");
            }
            if (!file.createNewFile()) {
                throw new CustomException("生成EXCEL文件失败");
            }

            // 生成excel文件，2007版本
            final byte[] bytes = POIUtil.exportExcel2007(tableName,
                    fields.stream()
                            .map(TableFieldInfo::getFieldName)
                            .collect(Collectors.toList())
                            .toArray(new String[fields.size()]),
                    fields.stream()
                            .map(TableFieldInfo::getFieldAlias)
                            .collect(Collectors.toList())
                            .toArray(new String[fields.size()]),
                    result);
            if (bytes == null) {
                throw new CustomException("生成EXCEL文件失败");
            }
            // 写入磁盘文件
            FileUtils.writeByteArrayToFile(file, bytes);

            // 更新 TableTask
            LambdaUpdateWrapper<TableTask> luw = new LambdaUpdateWrapper<>();
            luw.eq(TableTask::getId, taskId);
            luw.set(TableTask::getRemarks, "导出成功");
            luw.set(TableTask::getCompleteTime, DateUtils.getNowDate());
            luw.set(TableTask::getSpeedOfProgress, SpeedOfProgressType.SUCCESS.getId());
            luw.set(TableTask::getResult, filename);
            iTableTaskService.update(luw);

        } catch (IOException e) {
            log.error(e.getMessage());
            LambdaUpdateWrapper<TableTask> luw = new LambdaUpdateWrapper<>();
            luw.eq(TableTask::getId, taskId);
            luw.set(TableTask::getRemarks, "导出失败:" + e.getMessage());
            luw.set(TableTask::getCompleteTime, DateUtils.getNowDate());
            luw.set(TableTask::getSpeedOfProgress, SpeedOfProgressType.ERROR.getId());
            iTableTaskService.update(luw);
            throw new CustomException("生成EXCEL文件失败");
        }
    }

    @Override
    @Transactional
    public void createTable(TableCreateDto dto) {

        // 查询数据库连接
        final DataConnection dataConnection = DataSourceFactory.getDataConnection(dto.getDirId());
        dto.setSchema(dataConnection.getSchema());

        if (DatabaseEnum.HIVE2.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            dto.setDatabaseType(DatabaseEnum.HIVE2.getFeature());
        }

        // 构建并验证SQL语句
        String sql = createTableSqlVerify(dto);

        // 执行SQL语句
        dataConnection.getJdbcOperations().execute(sql);

        TableInfo tableInfo = new TableInfo();
        BeanUtils.copyProperties(dto, tableInfo);
        String prefix = DirCache.getDir(dto.getDirId()).getPrefix();
        if (!StringUtils.isEmpty(prefix)) {
            tableInfo.setTableName(prefix + tableInfo.getTableName());
        }
        Date now = new Date();
        if (StringUtils.isEmpty(tableInfo.getTableAlias())) {
            tableInfo.setTableAlias(tableInfo.getTableName());
        }
        tableInfo.setCreateUserId(SecurityUtils.getUserId());
        tableInfo.setCreateTime(now);
        tableInfo.setStatus(1);
        this.save(tableInfo);
        List<TableFieldInfo> fieldInfoList = dto.getFields().stream().map(f -> {
            TableFieldInfo fieldInfo = new TableFieldInfo();
            BeanUtils.copyProperties(f, fieldInfo);
            fieldInfo.setStatus(true);
            fieldInfo.setCreateTime(now);
            fieldInfo.setCreateUserId(SecurityUtils.getUserId());
            fieldInfo.setTableId(tableInfo.getId());
            fieldInfo.setDirId(tableInfo.getDirId());
            return fieldInfo;
        }).collect(Collectors.toList());

        this.fieldInfoService.saveBatch(fieldInfoList);
        SqlTypeContext.put(SqlTypeContext.TABLE_ID, tableInfo.getId());


        TableLog tableLog = new TableLog();
        tableLog.setTableId(tableInfo.getId());
        tableLog.setDirId(tableInfo.getDirId());
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

    @Override
    public String createTableSqlVerify(TableCreateDto dto) {

        TableInfo table = this.getOne(Wrappers.<TableInfo>query().eq("dir_id", dto.getDirId()).eq("table_name", dto.getTableName()));
        if (table != null) {
            throw new CustomException("表名已被使用");
        }

        String sql = dto.getIsDdl() ? dto.getDdl() : this.generateCreateTableSql(dto);
        if (StringUtils.isEmpty(sql)) {
            throw new CustomException("sql为空");
        }
        if (!sql.trim().toLowerCase().startsWith("create")) {
            throw new CustomException("仅支持建表sql");
        }
        log.info(sql);

        try {
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, DbType.MYSQL.getDb());
            List<SQLStatement> statementList = parser.parseStatementList();
            if (statementList.size() > 1) {
                throw new CustomException("不支持执行多条SQL");
            }
            if (statementList.get(0) instanceof SQLCreateTableStatement) {
                SQLCreateTableStatement statement = (SQLCreateTableStatement) statementList.get(0);

                MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
                statementList.get(0).accept(visitor);

                sql = SQLUtils.toSQLString(statementList, DbType.MYSQL.getDb());
            } else {
                throw new CustomException("仅支持建表sql");
            }


        } catch (ParserException e) {
            throw new CustomException(e.getMessage());
        }
        return sql;
    }

    @Override
    @Transactional
    public String copyTable(TableCopyDto dto) {
        Set<Long> tableIds = dto.getList().stream().map(TableCopyDto.CopyTo::getFromTableId).collect(Collectors.toSet());
        LambdaQueryWrapper<SysUserTable> sutLqw = new LambdaQueryWrapper<>();
        sutLqw.eq(SysUserTable::getUserId, SecurityUtils.getUserId());
        sutLqw.in(SysUserTable::getTableId, tableIds);
        List<SysUserTable> sysUserTableSet = userTableService.list(sutLqw);
        if (CollectionUtils.isEmpty(sysUserTableSet)) {
            return "无权限复制";
        }
        Set<Long> htableIds = sysUserTableSet.stream().filter(i -> i.getAccessType() > 1).map(SysUserTable::getTableId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(sysUserTableSet)) {
            return "无权限复制";
        }
        List<TableCopyDto.CopyTo> list = dto.getList().stream().
                filter(i -> Objects.nonNull(i.getFromTableId())).
                filter(i -> htableIds.contains(i.getFromTableId())).
                filter(i -> Objects.nonNull(i.getToDirId())).
                collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return "无权限复制";
        }
        String fromSchema = DataSourceFactory.getDataConnection(dto.getDirId()).getSchema();

        Map<Long, List<TableTaskDataDto>> sqlMap = new HashMap<>();

        Date now = new Date();
        dto.setList(list);

        List<TableInfo> newTableList = new ArrayList<>();
        String batchNumber = IdUtils.generateBusinessSerialNo();
        dto.getList().stream().forEach(copy -> {
            TableInfo tableInfo = iTableInfoService.getOne(Wrappers.<TableInfo>query().eq("dir_id", copy.getToDirId()).eq("table_name", copy.getToTableName()));
            if (tableInfo != null) {
                throw new CustomException(String.format("表名%s已被使用", copy.getToTableName()));
            }
            TableInfo fromTable = iTableInfoService.getOne(Wrappers.<TableInfo>query().eq("id", copy.getFromTableId()).eq("dir_id", dto.getDirId()));
            String toFromSchema = DataSourceFactory.getDataConnection(copy.getToDirId()).getSchema();
            TableTask tableTask = new TableTask();
            tableTask.setTaskType(TaskType.COPY.getId());
            tableTask.setParams(JSON.toJSONString(dto));
            tableTask.setDirId(dto.getDirId());
            tableTask.setTableId(copy.getFromTableId());
            tableTask.setOperator(SecurityUtils.getUsername());
            tableTask.setOperatorId(SecurityUtils.getUserId());
            tableTask.setCreateTime(DateUtils.getNowDate());
            tableTask.setSpeedOfProgress(SpeedOfProgressType.RUNNING.getId());
            tableTask.setContent("从 " + fromSchema + "库复制表" + fromTable.getTableName() + " 到 " + toFromSchema + "库" + copy.getToTableName() + "表");
            tableTask.setBatchNumber(batchNumber);

            String sql = String.format(copy.isCopyData() ? copyDataSql : copyTableSql, copy.getToTableName(), fromSchema, fromTable.getTableName());
            fromTable.setId(null);
            fromTable.setStatus(1);
            fromTable.setDirId(copy.getToDirId());
            fromTable.setTableName(copy.getToTableName());
            fromTable.setTableAlias(StringUtils.isEmpty(copy.getToTableAlias()) ? copy.getToTableName() : copy.getToTableAlias());
            fromTable.setUpdateUserId(0L);
            fromTable.setCreateUserId(SecurityUtils.getUserId());
            fromTable.setCreateTime(now);
            fromTable.setCopySql(sql);
            if (sqlMap.containsKey(copy.getToDirId())) {
                sqlMap.get(copy.getToDirId()).add(new TableTaskDataDto(sql, tableTask, fromTable));
            } else {
                List<TableTaskDataDto> sqlList = new ArrayList<>();
                sqlList.add(new TableTaskDataDto(sql, tableTask, fromTable));
                sqlMap.put(copy.getToDirId(), sqlList);
            }
            newTableList.add(fromTable);
        });
        List<Long> tableIdss = new ArrayList<>();
        for (Map.Entry<Long, List<TableTaskDataDto>> map : sqlMap.entrySet()) {
            for (int i = 0; i < map.getValue().size(); i++) {
                try {
                    iTableTaskService.save(map.getValue().get(i).getTableTask());
                    DataSourceFactory.getDataConnection(map.getKey()).getJdbcOperations().execute(map.getValue().get(i).getCopytable());
                    LambdaUpdateWrapper<TableTask> luw = new LambdaUpdateWrapper<>();
                    luw.eq(TableTask::getId, map.getValue().get(i).getTableTask().getId());
                    luw.set(TableTask::getRemarks, "复制成功");
                    luw.set(TableTask::getCompleteTime, DateUtils.getNowDate());
                    luw.set(TableTask::getSpeedOfProgress, SpeedOfProgressType.SUCCESS.getId());
                    SpringUtils.getBean(ITableTaskService.class).update(luw);
                    this.save(map.getValue().get(i).getTableInfo());
                    tableIdss.add(map.getValue().get(i).getTableTask().getTableId());
                } catch (Exception e) {
                    LambdaUpdateWrapper<TableTask> luw = new LambdaUpdateWrapper<>();
                    luw.eq(TableTask::getId, map.getValue().get(i).getTableTask().getId());
                    luw.set(TableTask::getRemarks, "复制失败:" + e.getMessage());
                    luw.set(TableTask::getCompleteTime, DateUtils.getNowDate());
                    luw.set(TableTask::getSpeedOfProgress, SpeedOfProgressType.ERROR.getId());
                    iTableTaskService.update(luw);
                }
            }
        }
        if (!CollectionUtils.isEmpty(tableIdss)) {
            List<TableFieldInfo> newFieldList = fieldInfoService.list(Wrappers.<TableFieldInfo>query().eq("dir_id", dto.getDirId()).eq("status", 1).in("table_id", tableIdss));
            newFieldList.forEach(field -> {
                field.setCreateUserId(SecurityUtils.getUserId());
                field.setCreateTime(now);
                field.setUpdateUserId(0L);
                field.setUpdateTime(now);
                field.setId(null);
                dto.getList().stream().filter(t -> t.getFromTableId().equals(field.getTableId())).findFirst().ifPresent(t -> {
                    field.setDirId(t.getToDirId());
                    field.setTableId(newTableList.stream().filter(tab -> tab.getTableName().equals(t.getToTableName())).findFirst().get().getId());
                });
            });
            fieldInfoService.saveBatch(newFieldList);
            newTableList.stream().filter(ii -> tableIdss.contains(ii.getId())).forEach(tableInfo -> {
                TableLog tableLog = new TableLog();
                tableLog.setTableId(tableInfo.getId());
                tableLog.setDirId(tableInfo.getDirId());
                tableLog.setTableAlias(tableInfo.getTableAlias());
                tableLog.setTableName(tableInfo.getTableName());
                tableLog.setLogType(LogType.table_copy.getVal());
                tableLog.setCreateTime(new Date());
                tableLog.setUpdateCount(1);
                tableLog.setDataCount(0);
                tableLog.setOperatorId(SecurityUtils.getUserId());
                tableLog.setOperator(SecurityUtils.getUsername());
                tableLog.setContent(tableInfo.getCopySql());
                tableLogService.save(tableLog);
            });
        }
        return "复制成功" + tableIdss.size() + "个表";

    }

    @Override
    public TableDataDto getTableData(Long tableId, String fieldName, String condition, String leftVal, String rightVal, String fieldIds, String sortBy, String sortingField) {
        TableInfo tableInfo = this.getById(tableId);
        QueryWrapper<TableFieldInfo> wrapper = Wrappers.<TableFieldInfo>query()
                .eq("table_id", tableId)
                .eq("status", 1)
                .orderByAsc("sort");
        if (!StringUtils.isEmpty(fieldIds)) {
            wrapper.in("id", fieldIds.split(","));
        }
        List<TableFieldInfo> fields = fieldInfoService.list(wrapper);

        // 构建 where 语句
        String whereSql = buildWhereSql(fieldName, condition, leftVal, rightVal, fields);

        List<TableDataDto.Header> headerList = new ArrayList<>(fields.size());


        // 查询表字段映射信息
        final List<TableMappingDTO> mappingList = tableMappingMapper.listMapping(tableId, null);


        int startRow = 0;
        int pageSize = 10;
        // 判断权限（没有权限只能查看10条）
        LambdaQueryWrapper<SysUserTable> sutLqw = new LambdaQueryWrapper<>();
        sutLqw.eq(SysUserTable::getUserId, SecurityUtils.getUserId());
        sutLqw.eq(SysUserTable::getTableId, tableId);
        List<SysUserTable> sysUserTableSet = userTableService.list(sutLqw);
        if (!CollectionUtils.isEmpty(sysUserTableSet)) {
            startRow = PageUtil.getStart(TableSupport.getPageDomain().getPageNum() - 1, TableSupport.getPageDomain().getPageSize());
            pageSize = TableSupport.getPageDomain().getPageSize();
        }

        // 获取表对应的数据库连接
        final DataConnection dataConnection = DataSourceFactory.getDataConnection(tableInfo.getDirId());

        // 构造SQL语句
        String selectSql;
        if (DatabaseEnum.MYSQL.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            selectSql = buildSelectSql(tableInfo.getTableName(),
                    fields, whereSql, startRow, pageSize, headerList, sortBy, sortingField, mappingList);

        } else if (DatabaseEnum.HIVE2.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            if (startRow == 1) {
                selectSql = buildSelectSqlByHive2(dataConnection.getSchema() + "." + tableInfo.getTableName(),
                        fields, whereSql, startRow, pageSize, headerList, sortBy, sortingField);

            } else {
                selectSql = buildSelectSqlByHive(dataConnection.getSchema() + "." + tableInfo.getTableName(),
                        fields, whereSql, startRow, pageSize, headerList, sortBy, sortingField);
            }

        } else if (DatabaseEnum.IMPALA.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            // Impala

            // 查询分区
            AtomicReference<String> partitionsField = new AtomicReference<>();
            AtomicReference<Object> partitionsValue = new AtomicReference<>();
            List<Map<String, Object>> result = null;
            try {
                result = dataConnection.getJdbcOperations().queryForList(
                        "SHOW PARTITIONS " + dataConnection.getSchema() + "." + tableInfo.getTableName());

            } catch (DataAccessException e) {
                log.error("Impala查询表分区信息异常：", e);
            }
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(result)) {
                final Map<String, Object> partitionsMap = result.get(0);
                partitionsMap.forEach((key, value) -> {
                    if (partitionsField.get() == null) {
                        partitionsField.set(key);
                    }
                    if (partitionsValue.get() == null) {
                        partitionsValue.set(value);
                    }
                });
            }
            // 如果查询不到分区信息，则不需要按照分区过滤
            if (partitionsField.get() != null && partitionsValue.get() == null) {
                if (StringUtils.isNotBlank(whereSql)) {
                    whereSql = whereSql + String.format(" AND %s = '%s' ", partitionsField.get(), partitionsValue.get());

                } else {
                    whereSql = whereSql + String.format(" WHERE %s = '%s' ", partitionsField.get(), partitionsValue.get());
                }
            }

            selectSql = buildSelectSqlByImpala(dataConnection.getSchema() + "." + tableInfo.getTableName(),
                    fields, whereSql, startRow, pageSize, headerList, sortBy, sortingField);

        } else {
            throw new CustomException("数据库类型不支持");
        }
        log.info("getTableData >> 执行SQL[{}]", selectSql);

        // 执行SQL，返回结果
        List<Map<String, Object>> result = dataConnection.getJdbcOperations().queryForList(selectSql);

        // TODO 临时处理，hive第一页只返回第一页的数据
        if (DatabaseEnum.HIVE2.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType()) && startRow == 1) {
            List<Map<String, Object>> list = new ArrayList<>();

            final Iterator<Map<String, Object>> iterator = result.iterator();
            AtomicInteger index = new AtomicInteger(0);
            while (iterator.hasNext()) {
                final Map<String, Object> next = iterator.next();
                if (index.get() >= pageSize) {
                    iterator.remove();
                } else {
                    Map<String, Object> map = new HashMap<>();
                    next.forEach((key, value) -> map.put(key.replace(tableInfo.getTableName() + ".", ""), value));

                    list.add(map);
                }
                index.getAndIncrement();
            }

            result = list;
        }

        TableDataDto dto = new TableDataDto();
//        dto.setHeader(getTableDataSaturability(tableId));
        dto.setHeader(headerList);
        dto.setList(result);

        // MySQL才有分页信息
        if (DatabaseEnum.MYSQL.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            String key = whereSql.length() > 1 ? tableRowKey + tableId + "_" + Md5Utils.hash(whereSql) : tableRowKey + tableId;
            Long rows = tableTotalRowCache.get(key);
            dto.setTotal(rows == null ? tableInfo.getTotalRow() : rows);

            // 没有权限只能查看10条
            if (CollectionUtils.isEmpty(sysUserTableSet)) {
                dto.setTotal(10);
            }
        }
        return dto;
    }

    @Override
    public TableDataDto getTableData(Long tableId, GetDataInfoRequestDTO requestDTO) {
        final String fieldIds = requestDTO.getFieldIds();
        final String sortingField = requestDTO.getSortingField();
        final String sortBy = requestDTO.getSortBy();
        final FilterDTO filter = requestDTO.getFilter();


        TableInfo tableInfo = this.getById(tableId);
        QueryWrapper<TableFieldInfo> wrapper = Wrappers.<TableFieldInfo>query()
                .eq("table_id", tableId)
                .eq("status", 1)
                .orderByAsc("sort");
        if (!StringUtils.isEmpty(fieldIds)) {
            wrapper.in("id", fieldIds.split(","));
        }
        List<TableFieldInfo> fields = fieldInfoService.list(wrapper);


        // 获取表对应的数据库连接
        final DataConnection dataConnection = DataSourceFactory.getDataConnection(tableInfo.getDirId());

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

            final ClickHouse mySql = new ClickHouse(Collections.singletonList(table), targetFilter);
            whereSql = mySql.getWhereSql();
        }

        List<TableDataDto.Header> headerList = new ArrayList<>(fields.size());


        // 查询表字段映射信息
        final List<TableMappingDTO> mappingList = tableMappingMapper.listMapping(tableId, null);


        int startRow = 0;
        int pageSize = 10;
        // 判断权限（没有权限只能查看10条）
        LambdaQueryWrapper<SysUserTable> sutLqw = new LambdaQueryWrapper<>();
        sutLqw.eq(SysUserTable::getUserId, SecurityUtils.getUserId());
        sutLqw.eq(SysUserTable::getTableId, tableId);
        List<SysUserTable> sysUserTableSet = userTableService.list(sutLqw);
        if (!CollectionUtils.isEmpty(sysUserTableSet)) {
            startRow = PageUtil.getStart(requestDTO.getPageNum() - 1, requestDTO.getPageSize());
            pageSize = requestDTO.getPageSize();
        }

        // 构造SQL语句
        String selectSql;
        if (DatabaseEnum.MYSQL.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            selectSql = buildSelectSql(tableInfo.getTableName(),
                    fields, whereSql, startRow, pageSize, headerList, sortBy, sortingField, mappingList);

        } else if (DatabaseEnum.HIVE2.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            if (startRow == 1) {
                selectSql = buildSelectSqlByHive2(dataConnection.getSchema() + "." + tableInfo.getTableName(),
                        fields, whereSql, startRow, pageSize, headerList, sortBy, sortingField);

            } else {
                selectSql = buildSelectSqlByHive(dataConnection.getSchema() + "." + tableInfo.getTableName(),
                        fields, whereSql, startRow, pageSize, headerList, sortBy, sortingField);
            }

        } else if (DatabaseEnum.IMPALA.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            // Impala

            // 查询分区
            AtomicReference<String> partitionsField = new AtomicReference<>();
            AtomicReference<Object> partitionsValue = new AtomicReference<>();
            List<Map<String, Object>> result = null;
            try {
                result = dataConnection.getJdbcOperations().queryForList(
                        "SHOW PARTITIONS " + dataConnection.getSchema() + "." + tableInfo.getTableName());

            } catch (DataAccessException e) {
                log.error("Impala查询表分区信息异常：", e);
            }
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(result)) {
                final Map<String, Object> partitionsMap = result.get(0);
                partitionsMap.forEach((key, value) -> {
                    if (partitionsField.get() == null) {
                        partitionsField.set(key);
                    }
                    if (partitionsValue.get() == null) {
                        partitionsValue.set(value);
                    }
                });
            }
            // 如果查询不到分区信息，则不需要按照分区过滤
            if (partitionsField.get() != null && partitionsValue.get() == null) {
                if (StringUtils.isNotBlank(whereSql)) {
                    whereSql = whereSql + String.format(" AND %s = '%s' ", partitionsField.get(), partitionsValue.get());

                } else {
                    whereSql = whereSql + String.format(" WHERE %s = '%s' ", partitionsField.get(), partitionsValue.get());
                }
            }

            selectSql = buildSelectSqlByImpala(dataConnection.getSchema() + "." + tableInfo.getTableName(),
                    fields, whereSql, startRow, pageSize, headerList, sortBy, sortingField);

        } else {
            throw new CustomException("数据库类型不支持");
        }
        log.info("getTableData >> 执行SQL[{}]", selectSql);

        // 执行SQL，返回结果
        List<Map<String, Object>> result = dataConnection.getJdbcOperations().queryForList(selectSql);

        // TODO 临时处理，hive第一页只返回第一页的数据
        if (DatabaseEnum.HIVE2.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType()) && startRow == 1) {
            List<Map<String, Object>> list = new ArrayList<>();

            final Iterator<Map<String, Object>> iterator = result.iterator();
            AtomicInteger index = new AtomicInteger(0);
            while (iterator.hasNext()) {
                final Map<String, Object> next = iterator.next();
                if (index.get() >= pageSize) {
                    iterator.remove();
                } else {
                    Map<String, Object> map = new HashMap<>();
                    next.forEach((key, value) -> map.put(key.replace(tableInfo.getTableName() + ".", ""), value));

                    list.add(map);
                }
                index.getAndIncrement();
            }

            result = list;
        }

        TableDataDto dto = new TableDataDto();
        dto.setHeader(headerList);
        dto.setList(result);

        // MySQL才有分页信息
        if (DatabaseEnum.MYSQL.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            String key = whereSql.length() > 1 ? tableRowKey + tableId + "_" + Md5Utils.hash(whereSql) : tableRowKey + tableId;
            Long rows = tableTotalRowCache.get(key);
            dto.setTotal(rows == null ? tableInfo.getTotalRow() : rows);

            // 条数
            final String countSql = String.format("SELECT COUNT(1) count FROM %s t_1 ",
                    dataConnection.getSchema() + "." + tableInfo.getTableName()) + whereSql;
            List<Map<String, Object>> countResult = dataConnection.getJdbcOperations().queryForList(countSql);
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(countResult)) {
                dto.setTotal(Long.parseLong(countResult.get(0).get("count").toString()));
            }

            // 没有权限只能查看10条
            if (CollectionUtils.isEmpty(sysUserTableSet)) {
                dto.setTotal(10);
            }
        }
        return dto;
    }

    @Override
    public List<TableDataDto.Header> getTableDataSaturability(Long tableId) {

        TableInfo tableInfo = this.getById(tableId);
        final DataConnection dataConnection = DataSourceFactory.getDataConnection(tableInfo.getDirId());
        String tableName;
        if (DatabaseEnum.MYSQL.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            tableName = tableInfo.getTableName();

        } else if (DatabaseEnum.HIVE2.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            tableName = dataConnection.getSchema() + "." + tableInfo.getTableName();

        } else {
            throw new CustomException("数据库类型不支持");
        }

        QueryWrapper wrapper = Wrappers.<TableFieldInfo>query().eq("table_id", tableId).eq("status", 1);

        List<TableFieldInfo> fields = fieldInfoService.list(wrapper);
        String whereSql = buildWhereSql("", "", "", "", fields);

        List<TableDataDto.Header> headerList = new ArrayList<>(fields.size());

        buildSelectSql(tableName, fields, whereSql, null, null, headerList, null, null, null);


        String select = "count(*) as counts" + headerList.stream().map(m -> String.format("count(%s) as %s", m.getFiledName(), m.getFiledName())).collect(Collectors.joining(",", ",", ""));
        String selectSql = buildSqlSelect(select, tableName, "");
        Map<String, Object> result = dataConnection.getJdbcOperations().queryForList(selectSql).get(0);
        //计数饱和度

        Long size = (Long) result.get("counts");
        for (TableDataDto.Header header : headerList) {
            String filedName = header.getFiledName();


            if (size != 0) {
                Long count = (Long) result.get(filedName);
                double as = (double) count / (double) size;
                header.setSaturability((int) (as * 100));
                header.setSum(size.intValue());
                header.setNullSum(count.intValue());
                continue;
            }
            header.setSaturability(0);
            header.setSum(0);
            header.setSaturability(100);
        }

        return headerList;
    }

    @Override
    public List<TableInfoDto> getTableBaseInfoIds(List<Long> ids) {

        return ids.stream().map(this::getTableBaseInfo).collect(Collectors.toList());
    }


    @Transactional
    @Override
    public void addData(Long tableId, Map<String, String> body) {
        if (CollectionUtils.isEmpty(body)) {
            throw new CustomException("请填写数据");
        }

        TableInfo tableInfo = this.getById(tableId);
        processFieldStatus(tableId, body);
        final DataConnection dataConnection = DataSourceFactory.getDataConnection(tableInfo.getDirId());
        if (dataConnection == null) {
            throw new CustomException("表信息配置有误");
        }
        String tableName;
        if (DatabaseEnum.MYSQL.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            tableName = tableInfo.getTableName();

        } else if (DatabaseEnum.HIVE2.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            tableName = dataConnection.getSchema() + "." + tableInfo.getTableName();

        } else {
            throw new CustomException("数据库类型暂不支持");
        }

        JdbcOperations jdbcOperations = dataConnection.getJdbcOperations();

        // 拼接SQL
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(tableName).append(" (");
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        for (Map.Entry<String, String> entry : body.entrySet()) {
            key.append(String.format(" %s", entry.getKey())).append(",");
            value.append(" ?").append(",");
        }
        key.replace(key.length() - 1, key.length(), " )");
        value.replace(value.length() - 1, value.length(), " )");
        String sql = sb.append(key).append(" values (").append(value).toString();
        jdbcOperations.update(sql, body.values().toArray());

        for (String v : body.values()) {
            sql = sql.replaceFirst("\\?", StringUtils.isEmpty(v) ? "" : v);
        }

        Date now = new Date();

        // 异步插入日志
        AsyncManager.me().execute(AsyncFactory.createTableLog(tableInfo, SecurityUtils.getUserId(),
                SecurityUtils.getUsername(), LogType.data_create, sql, 1, true, now, null));

        // 更新表信息
        this.update(Wrappers.<TableInfo>update()
                .set("update_time", now)
                .set("update_user_id", SecurityUtils.getUserId())
                .eq("id", tableInfo.getId()));
    }

    @Override
    @Transactional
    public void deleteData(Long tableId, Map<String, String> body) {
        if (CollectionUtils.isEmpty(body)) {
            return;
        }
        TableInfo tableInfo = this.getById(tableId);
        final DataConnection dataConnection = DataSourceFactory.getDataConnection(tableInfo.getDirId());
        if (DatabaseEnum.HIVE2.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            throw new CustomException("Hive数据库类型暂不支持");
        }
        JdbcOperations jdbcOperations = dataConnection.getJdbcOperations();
        TableFieldInfo tableFieldInfo = fieldInfoService.getOne(Wrappers.<TableFieldInfo>query().eq("table_id", tableId).eq("primary_key", true));
        if (tableFieldInfo == null) {
            throw new CustomException("操作错误,找不到主键！");
        }

        if (body.get(tableFieldInfo.getFieldName()) == null) {
            throw new CustomException("请输入主键值！");
        }
        String getPreSql = "select * from " + tableInfo.getTableName() + " where " + tableFieldInfo.getFieldName() + " =?";
        log.info(getPreSql);
        List<Map<String, Object>> maps = jdbcOperations.queryForList(getPreSql, body.get(tableFieldInfo.getFieldName()));
        String preContent = JSONUtil.toJsonStr(maps);

        StringBuilder sb = new StringBuilder();
        sb.append("delete from ").append(tableInfo.getTableName());
        sb.append(" where ");
        sb.append(tableFieldInfo.getFieldName());
        sb.append("=?");


        log.info(sb.toString());
        jdbcOperations.update(sb.toString(), body.get(tableFieldInfo.getFieldName()));


        Date now = new Date();
        AsyncManager.me().execute(AsyncFactory.createTableLog(tableInfo, SecurityUtils.getUserId(),
                SecurityUtils.getUsername(), LogType.data_delete,
                sb.toString().replace("?", body.get(tableFieldInfo.getFieldName())), 1,
                true, now, "表单详情-数据预览-删除数据", preContent));
//        AsyncManager.me().execute(AsyncFactory.createTableLog(tableInfo, SecurityUtils.getUserId(), SecurityUtils.getUsername(), LogType.data_delete, sb.toString().replace("?", body.get(tableFieldInfo.getFieldName())), 1, true, now, JSON.toJSONString(maps)));


        this.update(Wrappers.<TableInfo>update().set("update_time", now).set("update_user_id", SecurityUtils.getUserId()).eq("id", tableInfo.getId()));
    }

    @Override
    public void updateData(Long tableId, LinkedHashMap<String, String> body) {
        if (CollectionUtils.isEmpty(body)) {
            return;
        }

        // 查询主键
        TableFieldInfo tableFieldInfo = fieldInfoService.getOne(Wrappers.<TableFieldInfo>query()
                .eq("table_id", tableId)
                .eq("primary_key", true));
        if (tableFieldInfo == null) {
            throw new CustomException("操作错误,找不到主键！");
        }
        // 移除主键（主键不更新）
        String primaryValue = body.remove(tableFieldInfo.getFieldName());
        if (primaryValue == null) {
            throw new CustomException("操作错误,找不到主键！");
        }
        processFieldStatus(tableId, body);
        // 移除非本表字段
        List<TableFieldInfo> fieldInfoList = fieldInfoService.list(Wrappers.<TableFieldInfo>query()
                .eq("table_id", tableId));
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(fieldInfoList)) {
            final Set<String> fieldNameSet = fieldInfoList.stream()
                    .map(TableFieldInfo::getFieldName)
                    .collect(Collectors.toSet());
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(fieldNameSet)) {
                final Set<Map.Entry<String, String>> entrySet = body.entrySet();
                entrySet.removeIf(next -> !fieldNameSet.contains(next.getKey()));
            }
        }
        TableInfo tableInfo = this.getById(tableId);


        // 数据库连接信息
        final DataConnection dataConnection = DataSourceFactory.getDataConnection(tableInfo.getDirId());
        if (dataConnection == null) {
            throw new CustomException("表信息配置有误");
        }
        // 表名
        String tableName;
        if (DatabaseEnum.MYSQL.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            tableName = tableInfo.getTableName();

        } else if (DatabaseEnum.HIVE2.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            throw new CustomException("Hive数据库类型暂不支持");

        } else {
            throw new CustomException("数据库类型暂不支持");
        }

        // 预先查询SQL
        String getPreSql = "select * from " + tableName + " where " + tableFieldInfo.getFieldName() + " =?";
        log.info(getPreSql);
        List<Map<String, Object>> maps = dataConnection.getJdbcOperations().queryForList(getPreSql, primaryValue);
        String preContent = JSONUtil.toJsonStr(maps);

        // 更新SQL
        StringBuilder sb = new StringBuilder();
        sb.append("update ").append(tableName);
        sb.append(" set ");
        for (Map.Entry<String, String> entry : body.entrySet()) {
            sb.append(entry.getKey()).append(" = ");
            sb.append("?").append(" , ");
        }
        // 含有,和空格
        sb.replace(sb.length() - 2, sb.length(), "");
        sb.append(" where ").append(tableFieldInfo.getFieldName()).append(" = ").append("?");
        // 主键对应的值
        List<String> parameter = new ArrayList<>(body.values());
        parameter.add(primaryValue);
        dataConnection.getJdbcOperations().update(sb.toString(), parameter.toArray());


        String sql = sb.toString();
        for (String v : parameter) {
            sql = sql.replaceFirst("\\?", StringUtils.isEmpty(v) ? "" : v);
        }

        Date now = new Date();
        AsyncManager.me().execute(AsyncFactory.createTableLog(tableInfo, SecurityUtils.getUserId(),
                SecurityUtils.getUsername(), LogType.data_update, sql, 1, true, now,
                JSON.toJSONString(maps), preContent));

        this.update(Wrappers.<TableInfo>update()
                .set("update_time", now)
                .set("update_user_id", SecurityUtils.getUserId())
                .eq("id", tableInfo.getId()));
    }


    /**
     * 非空字段判断
     *
     * @param tableId
     * @param body
     */
    private void processFieldStatus(Long tableId, Map<String, String> body) {
        List<TableFieldInfo> tableFieldInfoList = fieldInfoService.list(Wrappers.<TableFieldInfo>query().eq("table_id", tableId).eq("empty", false));
        tableFieldInfoList.forEach(field -> {
            boolean status = false;
            for (Map.Entry<String, String> entry : body.entrySet()) {
                if (field.getFieldName().equals(entry.getKey()) && !StringUtils.isEmpty(entry.getValue())) {
                    status = true;
                    break;
                }
            }
            if (!field.isPrimaryKey() && !status) {
                throw new CustomException(String.format("%s 字段不允许为空", field.getFieldName()));
            }
        });
    }


    private String generateCreateTableSql(TableCreateDto dto) {
        // 如果是 Hive
        if (DatabaseEnum.HIVE2.getFeature().equalsIgnoreCase(dto.getDatabaseType())) {
            HiveUtils.Table table = new HiveUtils.Table();
            BeanUtils.copyProperties(dto, table);
            // 表名称拼接上数据库名称
            table.setTableName(dto.getSchema() + "." + dto.getTableName());

            // 字段信息
            final List<TableFieldInfoDto> fieldInfoDtoList = dto.getFields();
            if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(fieldInfoDtoList)) {
                List<HiveUtils.TableField> fields = new ArrayList<>();
                fieldInfoDtoList.forEach(tableFieldInfoDto -> {
                    HiveUtils.TableField tableField = new HiveUtils.TableField();
                    BeanUtils.copyProperties(tableFieldInfoDto, tableField);

                    fields.add(tableField);
                });

                table.setFields(fields);
            }

            // 构建 Hive 创建表语句
            return HiveUtils.createTable(table);
        }


        StringBuffer createSql = new StringBuffer();
        createSql.append("create table ");
        createSql.append(dto.getTableName());
        createSql.append(" (");

        String primaryKey = "";
        int len = dto.getFields().size();
        for (int i = 0; i < len; i++) {
            TableFieldInfoDto field = dto.getFields().get(i);
            createSql.append(String.format(" `%s` ", field.getFieldName()));
            createSql.append(field.getDataType());
            switch (field.getDataType()) {
                case "timestamp":
                    break;
                case "date":
                    break;
                case "datetime":
                    break;
                case "float":
                case "double":
                case "decimal":
                    createSql.append(String.format("(%s,%s)", field.getLength(), field.getScale()));
                    break;

                default:
                    createSql.append(String.format("(%s)", field.getLength()));
            }

            if (field.getPrimaryKey()) {
                primaryKey = String.format(" PRIMARY KEY (`%s`) ", field.getFieldName());
            }

            createSql.append(field.getEmpty() ? " DEFAULT NULL " : " NOT NULL ");

            if (!StringUtils.isEmpty(field.getComment())) {
                createSql.append(String.format(" COMMENT '%s' ", field.getComment()));
            }

            if (i < len - 1) {
                createSql.append(",");
            }

        }
        if (!StringUtils.isEmpty(primaryKey)) {
            createSql.append(",");
            createSql.append(primaryKey);
        }
        createSql.append(" ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ");
        if (!StringUtils.isEmpty(dto.getComment())) {
            createSql.append("COMMENT='");
            createSql.append(dto.getComment());
            createSql.append("'");
        }


        return createSql.toString();
    }

    @Override
    @Transactional
    public void updateTableInfo(TableInfoDto dto) {
        TableInfo tableInfo = this.getById(dto.getId());

        StringBuffer change = new StringBuffer();
        StringBuffer preContent = new StringBuffer();

        if (!StringUtils.isEmpty(dto.getTableAlias()) && !dto.getTableAlias().equals(tableInfo.getTableAlias())) {
            preContent.append(String.format(" tableAlias : %s", tableInfo.getTableAlias()));
            tableInfo.setTableAlias(dto.getTableAlias());
            change.append(String.format(" tableAlias : %s", dto.getTableAlias()));
        }

        if (dto.getSafeLevel() != tableInfo.getSafeLevel().intValue()) {
            preContent.append(String.format(" safeLevel : %s", tableInfo.getSafeLevel()));
            tableInfo.setSafeLevel(dto.getSafeLevel());
            change.append(String.format(" safeLevel : %s", dto.getSafeLevel()));
        }

        if (!StringUtils.isEmpty(dto.getComment()) && !dto.getComment().equals(tableInfo.getComment())) {
            preContent.append(String.format(" comment : %s", tableInfo.getComment()));
            tableInfo.setComment(dto.getComment());
            change.append(String.format(" comment : %s", dto.getComment()));

            // 只有MySQL才更新
            final DataConnection dataConnection = DataSourceFactory.getDataConnection(tableInfo.getDirId());
            if (DatabaseEnum.MYSQL.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
                dataConnection.getJdbcOperations().update(String.format(updateTableCommentSql, tableInfo.getTableName()), dto.getComment());
            }
        }


        if (tableInfo.getSchemeCycle() == null || dto.getSchemeCycle() != tableInfo.getSchemeCycle().intValue()) {
            SchemeCycleEnum tableInfoSchemeCycle = SchemeCycleEnum.valueOfCode(tableInfo.getSchemeCycle());
            SchemeCycleEnum dtoSchemeCycle = SchemeCycleEnum.valueOfCode(dto.getSchemeCycle());
            preContent.append(String.format(" 表更新周期 : %s", tableInfoSchemeCycle == null ?
                    "" : tableInfoSchemeCycle.getName()));
            tableInfo.setSchemeCycle(dto.getSchemeCycle());
            change.append(String.format(" 表更新周期 : %s", dtoSchemeCycle == null ? "" : dtoSchemeCycle.getName()));
        }


        if (tableInfo.getSchemeStatus() == null || dto.getSchemeStatus() != tableInfo.getSchemeStatus().intValue()) {
            SchemeStatusEnum tableInfoSchemeStatus = SchemeStatusEnum.valueOfCode(tableInfo.getSchemeStatus());
            SchemeStatusEnum dtoSchemeStatus = SchemeStatusEnum.valueOfCode(dto.getSchemeStatus());
            preContent.append(String.format(" 表更新类型 : %s", tableInfoSchemeStatus == null ?
                    "" : tableInfoSchemeStatus.getName()));
            tableInfo.setSchemeStatus(dto.getSchemeStatus());
            change.append(String.format(" 表更新类型 : %s", dtoSchemeStatus == null ? "" : dtoSchemeStatus.getName()));
        }

        tableInfo.setUpdateUserId(SecurityUtils.getUserId());
        tableInfo.setUpdateTime(new Date());

        tableInfo.setGroupName(dto.getGroupName());
        tableInfo.setLeaderName(dto.getLeaderName());

        this.updateById(tableInfo);

        if (change.length() > 0) {

            TableLog tableLog = new TableLog();
            tableLog.setTableId(dto.getId());
            tableLog.setDirId(tableInfo.getDirId());
            tableLog.setTableAlias(tableInfo.getTableAlias());
            tableLog.setTableName(tableInfo.getTableName());
            tableLog.setLogType(LogType.table_update.getVal());
            tableLog.setCreateTime(tableInfo.getUpdateTime());
            tableLog.setUpdateCount(1);
            //tableLog.setDataCount(DataSourceFactory.getDataConnection(tableInfo.getDirId()).getJdbcOperations().queryForObject("select count(*) from " + tableInfo.getTableName(), Integer.class));
            tableLog.setOperatorId(SecurityUtils.getUserId());
            tableLog.setOperator(SecurityUtils.getUsername());
            tableLog.setContent("更改内容" + change.toString());
            tableLog.setPreContent("更改前" + preContent.toString());
            tableLogService.save(tableLog);
        }

    }

    @Override
    public void updateQueryTime(Long tableId, int addTimes, int addTotalTimes) {
        this.baseMapper.updateQueryTime(tableId, addTimes, addTotalTimes);
    }

    @Override
    public List<BigTableDto> getBigTableTop20(List<Long> dirids) {
        return this.baseMapper.getBigTableTop20(dirids);
    }

    @Override
    public TableStatisticDto getTableStatisticDto(Long dirid) {
        List<Long> endDirIds = DirCache.getEndDirIds(dirid);
        TableStatisticDto dto = this.baseMapper.getTableStatisticDto(endDirIds);
        if (dto == null) {
            return new TableStatisticDto();
        }

        if (dirid == null) {
            dto.setTotalSchema(DirCache.getList().size());
        } else {
            dto.setTotalSchema(1);//默认算上自己
            countSchema(dirid, dto);
        }
        dto.setBigTableList(getBigTableTop20(endDirIds));
        PageHelper.startPage(1, 20, " id desc ");
        dto.setLogList(tableLogService.getList(endDirIds, null, null, null, "metadata"));
        return dto;
    }

    @Override
    public List<TableInfo> findByFieldNames(String fieldName) {
        List<TableInfo> list = new ArrayList<>(0);
        PageHelper.startPage(1, 50);
        List<TableFieldInfo> tableFieldInfoList = fieldInfoService.list(Wrappers.<TableFieldInfo>query().eq("status", 1).and(t -> t.like("field_name", "%" + fieldName + "%").or().like("field_alias", "%" + fieldName + "%")).groupBy("table_id").select("table_id"));
        List<Long> tableIds = tableFieldInfoList.stream().map(TableFieldInfo::getTableId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tableIds)) {
            return list;
        }

        list = this.list(Wrappers.<TableInfo>query().eq("status", 1).in("id", tableIds));

        if (SecurityUtils.getLoginUser().isAdmin()) {
            return list;
        }
        List<Long> dirIds = groupDataDirService.getDirIdByUserId(SecurityUtils.getUserId());
        return list.stream().filter(t -> dirIds.stream().anyMatch(d -> d.equals(t.getDirId()))).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String editTreeStatus(DataTreeDto tree) {
        List<DataDir> dataDirs = new ArrayList<>();
        List<TableInfo> tableInfos = new ArrayList<>();
        findData(tree, dataDirs, tableInfos);
        Long userId = SecurityUtils.getUserId();
        tableInfos.parallelStream().forEach(i -> {
            i.setUpdateTime(DateUtils.getNowDate());
            i.setUpdateUserId(userId);
        });
        if (!CollectionUtils.isEmpty(dataDirs)) {
            iDataDirService.updateBatchById(dataDirs);
        }
        if (!CollectionUtils.isEmpty(dataDirs)) {
            iTableInfoService.updateBatchById(tableInfos);
        }
        return "编辑成功";
    }

    @Override
    public DataTreeDto getDataTreeList() {
        List<Long> idList = iSysUserGroupService.list(Wrappers.<SysUserGroup>query().eq("user_id", SecurityUtils.getUserId())).stream().map(u -> u.getGroupId()).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(idList)) {
            return new DataTreeDto();
        }
        /*List<Long> dirIdList = groupDataDirService.getDirId(idList);
        List<Long> tableIdList = userTableService.list(Wrappers.<SysUserTable>query().eq("user_id", SecurityUtils.getUserId())).stream().map(i -> i.getTableId()).distinct().collect(Collectors.toList());
        LambdaQueryWrapper<DataDir> dirQuery = new LambdaQueryWrapper();
        dirQuery.in(DataDir::getId, dirIdList);*/
        List<DataDir> dataDirs = iDataDirService.list(null);

        /*LambdaQueryWrapper<TableInfo> tableQuery = new LambdaQueryWrapper();
        tableQuery.in(TableInfo::getId, tableIdList);*/
        List<TableInfo> tableInfos = iTableInfoService.list(null);
        Map<Long, List<TableInfo>> tableMap = tableInfos.stream().collect(Collectors.groupingBy(TableInfo::getDirId));

        /*LambdaQueryWrapper<TableFieldInfo> tableFieldQuery = new LambdaQueryWrapper();
        tableFieldQuery.in(TableFieldInfo::getTableId, tableIdList);*/
        List<TableFieldInfo> tableFieldInfoList = iTableFieldInfoService.list(null);
        Map<Long, List<TableFieldInfo>> tableFieldMap = tableFieldInfoList.stream().collect(Collectors.groupingBy(TableFieldInfo::getTableId));
        return getDataTreeList(dataDirs, true, tableMap, tableFieldMap);
    }

    @Override
    public DataTreeDto getDataTreeByTableId(String tableId) {
        DataTreeDto dto = new DataTreeDto();
        dto.setId(0L);
        dto.setDirName("根节点");
        dto.setIsTable(false);
        LambdaQueryWrapper<TableInfo> tableQuery = new LambdaQueryWrapper();
        tableQuery.eq(TableInfo::getId, tableId);
        TableInfo tableInfo = iTableInfoService.getOne(tableQuery);
        if (null == tableInfo) {
            return dto;
        }
        LambdaQueryWrapper<TableFieldInfo> tableFieldQuery = new LambdaQueryWrapper();
        tableFieldQuery.eq(TableFieldInfo::getTableId, tableInfo.getId());
        List<TableFieldInfo> tableFieldInfoList = iTableFieldInfoService.list(tableFieldQuery);
        setTableAndFieldTree(dto, tableInfo, tableFieldInfoList);
        return dto;
    }

    @Override
    @Transactional
    public String editDataFieldStatus(DataTreeDto tree) {
        List<TableFieldInfo> tableFieldInfos = tree.getChildren().get(0).getChildren().stream().filter(i -> Objects.nonNull(getStatus(i.getData(), "status"))).map(i -> {
            TableFieldInfo tableFieldInfo = new TableFieldInfo();
            tableFieldInfo.setId(i.getId());
            tableFieldInfo.setUpdateTime(DateUtils.getNowDate());
            tableFieldInfo.setUpdateUserId(SecurityUtils.getUserId());
            tableFieldInfo.setStatus(StatusTypeEnum.getStatus(getStatus(i.getData(), "status")) == StatusTypeEnum.IS_ENABLE.getId() ? true : false);
            return tableFieldInfo;
        }).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(tableFieldInfos)) {
            iTableFieldInfoService.updateBatchById(tableFieldInfos);
        }
        return "修改成功";
    }

    @Override
    @Transactional
    public void copyTable(TableCopyInfoDto i) {
        try {
            DataSourceFactory.getDataConnection(i.getToDirId()).getJdbcOperations().execute(i.getSql());
            this.save(i.getTableInfo());
            i.getTableLog().setTableId(i.getTableInfo().getId());
            List<TableFieldInfo> newFieldList = fieldInfoService.list(Wrappers.<TableFieldInfo>query().eq("dir_id", i.getDirId()).eq("table_id", i.getFromTableId()));
            newFieldList.forEach(field -> {
                field.setCreateUserId(i.getUserId());
                field.setCreateTime(new Date());
                field.setUpdateUserId(0L);
                field.setUpdateTime(new Date());
                field.setId(null);
                field.setDirId(i.getTableInfo().getDirId());
                field.setTableId(i.getTableInfo().getId());
            });
            fieldInfoService.saveBatch(newFieldList);
            i.setSuccess(true);
            i.setMessage("复制成功");
        } catch (Exception e) {
            i.getTableLog().setTableId(0L);
            i.setSuccess(false);
            i.setMessage("复制失败：" + e.getMessage());
            throw new RuntimeException("复制失败：" + e.getMessage());
        }
    }


    private boolean checkTableExists(Long dirId, String tableName) {
        if (com.sinohealth.common.utils.StringUtils.isBlank(tableName)) {
            throw new CustomException("表名不能为空!");
        }
        boolean result = false;
        try {
            DataConnection dataConnection = DataSourceFactory.getDataConnection(dirId);
            DatabaseMetaData metaData = dataConnection.getDataSource().getConnection().getMetaData();
            String[] types = {"TABLE"};
            ResultSet tabs = metaData.getTables(null, null, tableName, types);
            if (tabs.next()) {
                result = true;
            }
        } catch (Exception e) {
            log.error("check tableName:{} failed", tableName, e);
            throw new CustomException(String.format("check tableName failed:{%s}", e.getMessage()));
        }

        return result;
    }

    /**
     * @param tableName     表名
     * @param fieldInfoList 表和主键和释义
     * @return
     */
    public TableCreateDto buildFieldInfoData(Long dirId, String tableName, String tableAlias, List<Map> fieldInfoList) {
        List<TableFieldInfoDto> tableFieldInfoDtoList = new ArrayList<>(fieldInfoList.size());

        Map<String, Object> fieldInfoMap = fieldInfoList.get(0);
        for (Map.Entry<String, Object> entry : fieldInfoMap.entrySet()) {
            boolean primaryStatus = false;
            boolean emptyStatus = true;
            String key = entry.getKey();
            String value = (String) entry.getValue();
            TableFieldInfoDto tableFieldInfoDto = new TableFieldInfoDto();
            // 数字长度：32位
            // 字符串长度:500
            // 小数点位数：保留后4位
            String dataType = (String) SqlTypeContext.get(key);
            int dataLength = 0;
            if (DateSetExcelUtil.F_VARCHAR.equals(dataType)) {
                dataLength = 500;
            }
            if (DateSetExcelUtil.F_DOUBLE.equals(dataType)) {
                dataLength = 32;
                tableFieldInfoDto.setScale(4);
            }
            tableFieldInfoDto.setFieldName(key);
            tableFieldInfoDto.setFieldAlias(value);
            tableFieldInfoDto.setDataType(dataType);
            tableFieldInfoDto.setComment(value);
            tableFieldInfoDto.setEmpty(emptyStatus);
            tableFieldInfoDto.setLength(dataLength);
            tableFieldInfoDto.setPrimaryKey(primaryStatus);
            tableFieldInfoDtoList.add(tableFieldInfoDto);
        }

        TableCreateDto dto = new TableCreateDto();
        dto.setDirId(dirId);
        dto.setTableAlias(tableAlias);
        dto.setTableName(tableName);
        dto.setComment(tableName);
        dto.setSafeLevel(1);
        dto.setIsDdl(false);
        dto.setFields(tableFieldInfoDtoList);

        return dto;
    }

    /**
     * 执行数据库插入操作
     *
     * @param datas     插入数据表中key为列名和value为列对应的值的Map对象的List集合
     * @param tableName 要插入的数据库的表名
     * @return 影响的行数
     * @throws SQLException SQL异常
     */
    protected int insertAll(Long dirId, String tableName, List<Map> datas) throws SQLException {
        /**影响的行数**/
        int affectRowCount = -1;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            /**从数据库连接池中获取数据库连接**/
            connection = DataSourceFactory.getDataConnection(dirId).getDataSource().getConnection();


            Map<String, Object> valueMap = datas.get(0);
            /**获取数据库插入的Map的键值对的值**/
            Set<String> keySet = valueMap.keySet();
            Iterator<String> iterator = keySet.iterator();
            /**要插入的字段sql，其实就是用key拼起来的**/
            StringBuilder columnSql = new StringBuilder();
            /**要插入的字段值，其实就是？**/
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
            /**开始拼插入的sql语句**/
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ");
            sql.append(tableName);
            sql.append(" (");
            sql.append(columnSql);
            sql.append(" )  VALUES (");
            sql.append(unknownMarkSql);
            sql.append(" )");

            /**执行SQL预编译**/
            preparedStatement = connection.prepareStatement(sql.toString());
            /**设置不自动提交，以便于在出现异常的时候数据库回滚**/
            connection.setAutoCommit(false);
            for (int j = 0; j < datas.size(); j++) {
                for (int k = 0; k < keys.length; k++) {
                    preparedStatement.setObject(k + 1, datas.get(j).get(keys[k]));
                }
                preparedStatement.addBatch();
            }
            int[] arr = preparedStatement.executeBatch();
            connection.commit();
            affectRowCount = arr.length;
            log.info("成功了插入了:{}行", affectRowCount);

        } catch (Exception e) {
            if (connection != null) {
                connection.rollback();
            }
            log.error("插入数据失败!", e);
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


    public void setTableAndFieldTree(DataTreeDto dto, TableInfo tableInfo, List<TableFieldInfo> tableFieldInfos) {
        if (Objects.isNull(tableInfo)) {
            return;
        }
        DataTreeDto dto1 = new DataTreeDto();
        dto1.setId(tableInfo.getId());
        dto1.setDirName(tableInfo.getTableAlias());
        dto1.setIsTable(true);
        dto1.setParentId(dto.getId());
        JSONObject data = new JSONObject();
        data.put("fieldName", tableInfo.getTableName());
        data.put("fieldAlias", tableInfo.getTableAlias());
        dto1.setData(data);
        dto.getChildren().add(dto1);
        if (CollectionUtils.isEmpty(tableFieldInfos)) {
            return;
        }
        dto1.getChildren().addAll(tableFieldInfos.stream().map(i -> {
            DataTreeDto dto2 = new DataTreeDto();
            dto2.setId(i.getId());
            dto2.setDirName(i.getFieldName());
            dto2.setIsTable(false);
            dto2.setParentId(dto1.getId());
            JSONObject data1 = new JSONObject();
            data1.put("fieldName", i.getFieldName());
            data1.put("fieldAlias", i.getFieldAlias());
            data1.put("status", i.getStatus() == false ? new String[]{String.valueOf(StatusTypeEnum.IS_DELETE.getId())} : new String[]{String.valueOf(StatusTypeEnum.IS_ENABLE.getId())});
            dto2.setData(data1);
            return dto2;
        }).collect(Collectors.toList()));

    }

    private DataTreeDto getDataTreeList(List<DataDir> dataDirs, boolean b, Map<Long, List<TableInfo>> tableMap, Map<Long, List<TableFieldInfo>> tableFieldMap) {
        DataTreeDto dto = new DataTreeDto();
        dto.setId(0L);
        dto.setDirName("根节点");
        dto.setIsTable(false);
        dto.setOnly(UUID.randomUUID().toString());
        toTree(dto, dataDirs, b, tableMap, tableFieldMap);
        return dto;
    }

    private void toTree(DataTreeDto dto, List<DataDir> dataDirs, boolean b, Map<Long, List<TableInfo>> tableMap, Map<Long, List<TableFieldInfo>> tableFieldMap) {
        if (Objects.isNull(dto) || CollectionUtils.isEmpty(dataDirs)) {
            return;
        }
        if (0L == dto.getId() || dto.getDirName().equals("根节点")) {
            dataDirs.stream().forEach(i -> {
                if (i.getParentId().longValue() == dto.getId().longValue()) {
                    DataTreeDto dto1 = new DataTreeDto();
                    dto1.setId(i.getId());
                    dto1.setDirName(i.getDirName());
                    dto1.setIsTable(false);
                    dto1.setParentId(dto.getId());
                    dto1.setOnly(UUID.randomUUID().toString());
                    JSONObject json = new JSONObject();
                    json.put("status", setStatus(i.getStatus()));
                    dto1.setData(json);
                    toTree(dto1, dataDirs, b, tableMap, tableFieldMap);
                    dto.getChildren().add(dto1);
                }
            });
            return;
        }
        dataDirs.stream().forEach(i -> {
            if (i.getParentId().longValue() == dto.getId().longValue()) {
                DataTreeDto dto1 = new DataTreeDto();
                dto1.setId(i.getId());
                dto1.setDirName(i.getDirName());
                dto1.setIsTable(false);
                dto1.setParentId(dto.getId());
                dto1.setOnly(UUID.randomUUID().toString());
                JSONObject json = new JSONObject();
                json.put("status", setStatus(i.getStatus()));
                dto1.setData(json);
                toTree(dto1, dataDirs, b, tableMap, tableFieldMap);
                if (b) {
                    loadTable(dto1, tableMap, tableFieldMap);
                }
                dto.getChildren().add(dto1);
                if (b) {
                    loadTable(dto, tableMap, tableFieldMap);
                }
            }
        });
        return;
    }

    public void loadTable(DataTreeDto dto, Map<Long, List<TableInfo>> tableMap, Map<Long, List<TableFieldInfo>> tableFieldMap) {
        List<TableInfo> tableInfos = tableMap.get(dto.getId());
        if (CollectionUtils.isEmpty(tableInfos)) {
            return;
        }
        dto.getChildren().addAll(tableInfos.parallelStream().map(i -> {
            DataTreeDto table = new DataTreeDto();
            table.setId(i.getId());
            table.setDirName(i.getTableName());
            table.setIsTable(true);
            table.setParentId(dto.getId());
            table.setOnly(UUID.randomUUID().toString());
            JSONObject json = new JSONObject();
            json.put("status", setStatus(i.getStatus()));
            if (CollectionUtils.isEmpty(tableFieldMap.get(i.getId()))) {
                json.put("enableCount", 0);
                json.put("disableCount", 0);
            } else {
                json.put("enableCount", tableFieldMap.get(i.getId()).parallelStream().filter(o -> o.getStatus() == true).count());
                json.put("disableCount", tableFieldMap.get(i.getId()).parallelStream().filter(o -> o.getStatus() == false).count());
            }
            JSONObject tableInfo = new JSONObject();
            tableInfo.put("name", i.getTableName());
            tableInfo.put("alias", i.getTableAlias());
            json.put("tableInfo", tableInfo);
            table.setData(json);
            return table;
        }).collect(Collectors.toList()));
    }

    private void findData(DataTreeDto tree, List<DataDir> dataDirs, List<TableInfo> tableInfos) {
        if (null == tree) {
            return;
        }
        if (tree.getId() == 0L || tree.getDirName().equals("根节点")) {
            tree.getChildren().stream().forEach(i -> {
                if (i.getIsTable() == true) {
                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setId(i.getId());
                    if (Objects.nonNull(i.getData())) {
                        tableInfo.setStatus(StatusTypeEnum.getStatus(getStatus(i.getData(), "status")));
                        if (Objects.nonNull(tableInfo.getStatus())) {
                            tableInfos.add(tableInfo);
                        }
                    }
                } else {
                    findData(i, dataDirs, tableInfos);
                }
            });
            return;
        }
        if (tree.getIsTable() == true) {
            TableInfo tableInfo = new TableInfo();
            tableInfo.setId(tree.getId());
            if (null != tree.getData()) {
                tableInfo.setStatus(StatusTypeEnum.getStatus(getStatus(tree.getData(), "status")));
                if (Objects.nonNull(tableInfo.getStatus())) {
                    tableInfos.add(tableInfo);
                }
            }
            return;
        } else {
            DataDir dataDir = new DataDir();
            dataDir.setId(tree.getId());
            if (null != tree.getData()) {
                dataDir.setStatus(StatusTypeEnum.getStatus(getStatus(tree.getData(), "status")));
                if (Objects.nonNull(dataDir.getStatus())) {
                    dataDirs.add(dataDir);
                }
            }
            tree.getChildren().stream().forEach(i -> {
                findData(i, dataDirs, tableInfos);
            });
            return;
        }
    }

    @Override
    public List<TableInfo> findByFieldName(String fieldName) {

        PageHelper.startPage(1, 50);
        List<TableFieldInfo> fieldList = fieldInfoService.list(Wrappers.<TableFieldInfo>query().eq("status", 1).and(t -> t.like("field_name", "%" + fieldName + "%").or().like("field_alias", "%" + fieldName + "%")));

        if (fieldList == null || fieldList.isEmpty()) {
            return null;
        }

        List<TableInfo> list = list(Wrappers.<TableInfo>lambdaQuery().in(TableInfo::getId, fieldList.stream().map(f -> f.getTableId().toString()).distinct().collect(Collectors.toList())));

        if (SecurityUtils.getLoginUser().isAdmin()) {
            return list;
        }

        List<Long> dirIds = groupDataDirService.getDirIdByUserId(SecurityUtils.getUserId());
        return list.stream().filter(t -> dirIds.stream().anyMatch(d -> d.equals(t.getDirId()))).collect(Collectors.toList());
    }


    private void countSchema(Long dirid, TableStatisticDto dto) {
        DirCache.getList().stream().filter(d -> d.getParentId().equals(dirid)).forEach(d -> {
            dto.setTotalSchema(dto.getTotalSchema() + 1);
            countSchema(d.getId(), dto);
        });
    }

    public Integer getStatus(JSONObject data, String key) {
        try {
            if (Objects.isNull(data)) {
                return null;
            }
            List list = data.getObject(key, List.class);
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            return Integer.parseInt(list.get(0).toString());
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> setStatus(Integer val) {
        List<String> list = new ArrayList<>();
        if (Objects.nonNull(val)) {
            list.add(val.toString());
        }
        return list;
    }


    /**
     * 获取Excel数据
     *
     * @param file  上传文件
     * @param limit 展示10条
     * @return 表数据
     * @author linkaiwei
     * @date 2021-11-15 15:58:28
     * @since 1.6.1.0
     */
    private List<Map<String, Object>> getExcelData(MultipartFile file, Boolean limit) {
        // 解析
        List<Map> maps = null;
        try {
            maps = DateSetExcelUtil.importExcel(Map.class, file.getInputStream(), "yyyy-MM-dd HH:mm:ss");

        } catch (IOException e) {
            log.error("", e);
        }
        if (CollectionUtils.isEmpty(maps)) {
            throw new CustomException("excel解析失败!");
        }

        List<Map<String, Object>> list = new ArrayList<>();
        if (limit) {
            maps.stream().limit(10).forEach(list::add);

        } else {
            maps.forEach(list::add);
        }

        return list;
    }

    /**
     * 上传表的回传数据
     *
     * @param tableId 表ID
     * @param file    表的回传数据
     * @author linkaiwei
     * @date 2022-02-14 10:13:39
     * @since 1.6.4.0
     */
    @SneakyThrows
    @Override
    public Boolean uploadReturnData(Long tableId, MultipartFile file) {
        final TableInfo tableInfo = baseMapper.selectById(tableId);
        if (tableInfo == null) {
            throw new CustomException("表信息有误");
        }

        final List<Map<String, Object>> excelData = getExcelData(file, false);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(excelData)) {
            throw new CustomException("Excel数据有误");
        }
        // 表格前两行格式为字段名，字段别名，List格式取前两个即可
//        List<Map<String, Object>> headFieldInfoList = excelData.subList(0, 1);
        // 数据
        List<Map<String, Object>> tailFieldInfoList = excelData.subList(1, excelData.size());

        // 数据插入上传人
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(tailFieldInfoList)) {
            throw new CustomException("Excel数据有误");
        }
        tailFieldInfoList.forEach(map -> {
            map.put("upload_time", new Date());
            map.put("upload_people", SecurityUtils.getLoginUser().getUser().getRealName());
        });

        // 插入数据
        int row = insertAll("sic_upload_revise_test", tailFieldInfoList);
        if (row < 0) {
            throw new CustomException("插入数据行失败!");
        }

        return true;
    }

    /**
     * 执行数据库插入操作
     *
     * @param tableName 要插入的数据库的表名
     * @param excelData 插入数据表中key为列名和value为列对应的值的Map对象的List集合
     * @return 影响的行数
     * @throws SQLException SQL异常
     */
    private int insertAll(String tableName, List<Map<String, Object>> excelData) throws SQLException {
        /* 影响的行数 */
        int affectRowCount = -1;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            /* 从数据库连接池中获取数据库连接 */
            final DataConnection dataConnection = DataSourceFactory.getDataConnection(dirDataSourceId);
            connection = dataConnection.getDataSource().getConnection();


            Map<String, Object> valueMap = excelData.get(0);
            /* 获取数据库插入的Map的键值对的值 */
            Set<String> keySet = valueMap.keySet();
            Iterator<String> iterator = keySet.iterator();
            /* 要插入的字段sql，其实就是用key拼起来的 */
            StringBuilder columnSql = new StringBuilder();
            /* 要插入的字段值，其实就是？ */
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
            /* 开始拼插入的sql语句 */
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ");
            sql.append(dataConnection.getSchema());
            sql.append(".");
            sql.append(tableName);
            sql.append(" (");
            sql.append(columnSql);
            sql.append(" )  VALUES (");
            sql.append(unknownMarkSql);
            sql.append(" )");

            /* 执行SQL预编译 */
            preparedStatement = connection.prepareStatement(sql.toString());
            /* 设置不自动提交，以便于在出现异常的时候数据库回滚 */
            connection.setAutoCommit(false);
//            System.out.println(sql.toString());
            for (int j = 0; j < excelData.size(); j++) {
                for (int k = 0; k < keys.length; k++) {
                    preparedStatement.setObject(k + 1, excelData.get(j).get(keys[k]));
                }
                preparedStatement.addBatch();
            }
            int[] arr = preparedStatement.executeBatch();
            connection.commit();
            affectRowCount = arr.length;
            log.info("成功了插入了:{}行", affectRowCount);

        } catch (Exception e) {
            if (connection != null) {
                connection.rollback();
            }
            log.error("插入数据失败!", e);
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


    /**
     * 回传数据
     *
     * @param pageNum  分页，页码
     * @param pageSize 分页，每页数量
     * @param type     搜索类型，1药监名称，2产品规格，3生产企业，4是否已经处理
     * @param content  搜索内容
     * @return 回传数据
     * @author linkaiwei
     * @date 2022-02-14 15:10:48
     * @since 1.6.4.0
     */
    @Override
    public PageListReturnDataDTO pageListReturnData(Integer pageNum, Integer pageSize, Integer type, String content) {
        String fieldName = null;
        if (type != null && type != 0) {
            if (type == 1) {
                fieldName = "sic_drug_name";

            } else if (type == 2) {
                fieldName = "sic_specification";

            } else if (type == 3) {
                fieldName = "sic_company";

            } else if (type == 4) {
                fieldName = "is_handle";

            } else {
                throw new CustomException("搜索类型不支持");
            }
        }

        //
        TableInfo tableInfo = this.getById(returnDataTableId);
        QueryWrapper<TableFieldInfo> wrapper = Wrappers.<TableFieldInfo>query()
                .eq("table_id", returnDataTableId)
                .eq("status", 1)
                .orderByAsc("sort");
        List<TableFieldInfo> fields = fieldInfoService.list(wrapper);

        // 构建 where 语句
        String whereSql = buildWhereSql(fieldName, "contain", content, null, fields);

        List<TableDataDto.Header> headerList = new ArrayList<>(fields.size());


        // 查询表字段映射信息
        final List<TableMappingDTO> mappingList = tableMappingMapper.listMapping(returnDataTableId, null);


        int startRow = 0;
        // 判断权限（没有权限只能查看10条）
        LambdaQueryWrapper<SysUserTable> sutLqw = new LambdaQueryWrapper<>();
        sutLqw.eq(SysUserTable::getUserId, SecurityUtils.getUserId());
        sutLqw.eq(SysUserTable::getTableId, returnDataTableId);
        List<SysUserTable> sysUserTableSet = userTableService.list(sutLqw);
        if (!CollectionUtils.isEmpty(sysUserTableSet)) {
            startRow = PageUtil.getStart(TableSupport.getPageDomain().getPageNum() - 1,
                    TableSupport.getPageDomain().getPageSize());
            pageSize = TableSupport.getPageDomain().getPageSize();
        }

        // 获取表对应的数据库连接
        final DataConnection dataConnection = DataSourceFactory.getDataConnection(tableInfo.getDirId());

        // 构造SQL语句
        String selectSql;
        if (DatabaseEnum.MYSQL.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            selectSql = buildSelectSql(tableInfo.getTableName(),
                    fields, whereSql, startRow, pageSize, headerList, null, null, mappingList);

        } else {
            throw new CustomException("数据库类型不支持");
        }
        log.info("pageListReturnData >> 执行SQL[{}]", selectSql);

        // 执行SQL，返回结果
        final List<Map<String, Object>> list = dataConnection.getJdbcOperations().queryForList(selectSql);

        // 处理 is_handle
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)) {
            list.forEach(map -> map.put("is_handle", "1".equals(map.get("is_handle"))));
        }

        // 响应参数
        PageListReturnDataDTO pageListReturnDataDTO = new PageListReturnDataDTO();
        pageListReturnDataDTO.setList(list);
        pageListReturnDataDTO.setTotal(0L);

        // MySQL才有分页信息
        if (DatabaseEnum.MYSQL.getFeature().equalsIgnoreCase(dataConnection.getDatabaseType())) {
            String key = whereSql.length() > 1 ? tableRowKey + tableInfo.getId() + "_" + Md5Utils.hash(whereSql)
                    : tableRowKey + tableInfo.getId();
            Long rows = tableTotalRowCache.get(key);
            pageListReturnDataDTO.setTotal(rows == null ? tableInfo.getTotalRow() : rows);
        }

        return pageListReturnDataDTO;
    }

    /**
     * 删除回传数据
     *
     * @param increasCode 自增code
     * @author linkaiwei
     * @date 2022-02-14 10:13:39
     * @since 1.6.4.0
     */
    @Override
    public Boolean deleteReturnData(String increasCode) {
        TableInfo tableInfo = this.getById(returnDataTableId);
        if (tableInfo == null) {
            throw new CustomException("表信息有误");
        }

        // 获取表对应的数据库连接
        final DataConnection dataConnection = DataSourceFactory.getDataConnection(tableInfo.getDirId());

        // 删除数据
        String sql = String.format("delete from %s.%s where %s = '%s'", dataConnection.getSchema(),
                tableInfo.getTableName(), "increas_code", increasCode);
        dataConnection.getJdbcOperations().execute(sql);

        return true;
    }

    /**
     * 更新回传数据
     *
     * @param requestDTO 更新回传数据信息
     * @author linkaiwei
     * @date 2022-02-14 10:13:39
     * @since 1.6.4.0
     */
    @Override
    public Boolean updateReturnData(UpdateReturnDataRequestDTO requestDTO) {
        TableInfo tableInfo = this.getById(returnDataTableId);
        if (tableInfo == null) {
            throw new CustomException("表信息有误");
        }

        // 获取表对应的数据库连接
        final DataConnection dataConnection = DataSourceFactory.getDataConnection(tableInfo.getDirId());

        StringBuilder sql = new StringBuilder("update ");
        sql.append(dataConnection.getSchema()).append(".").append(tableInfo.getTableName()).append(" set ");
        sql.append(" approval_number = '").append(requestDTO.getApproval_number()).append("',");
        sql.append(" sic_drug_name = '").append(requestDTO.getSic_drug_name()).append("',");
        sql.append(" sic_specification = '").append(requestDTO.getSic_specification()).append("',");
        sql.append(" sic_packing = '").append(requestDTO.getSic_packing()).append("',");
        sql.append(" marketing_authorization_holder = '").append(requestDTO.getMarketing_authorization_holder()).append("',");
        sql.append(" sic_company = '").append(requestDTO.getSic_company()).append("',");
        sql.append(" remarks = '").append(requestDTO.getRemarks()).append("',");
        sql.append(" product_code = '").append(requestDTO.getProduct_code()).append("',");
        sql.append(" product_standard_code = '").append(requestDTO.getProduct_standard_code()).append("',");
        sql.append(" unique_id = '").append(requestDTO.getUnique_id()).append("',");
        sql.append(" is_handle = ").append(requestDTO.getIs_handle()).append(",");
        sql.append(" handle_update_time = '").append(DateUtils.getTime()).append("',");
        sql.append(" handle_update_people = '").append(SecurityUtils.getLoginUser().getUser().getRealName()).append("'");

        sql.append(" where increas_code = '").append(requestDTO.getIncreas_code()).append("'");

        // 更新回传数据
        dataConnection.getJdbcOperations().execute(sql.toString());

        return true;
    }

    @Override
    @Transactional
    public void updateTableInfo(TableInfoManageDto dto) {
        TableInfo tableInfo = this.getById(dto.getId());

        StringBuilder change = new StringBuilder();
        StringBuilder preContent = new StringBuilder();

        if (!StringUtils.isEmpty(dto.getTableAlias()) && !dto.getTableAlias().equals(tableInfo.getTableAlias())) {
            preContent.append(String.format(" tableAlias : %s", tableInfo.getTableAlias()));
            tableInfo.setTableAlias(dto.getTableAlias());
            change.append(String.format(" tableAlias : %s", dto.getTableAlias()));
        }

        if (!StringUtils.isEmpty(dto.getComment()) && !dto.getComment().equals(tableInfo.getComment())) {
            preContent.append(String.format(" comment : %s", tableInfo.getComment()));
            tableInfo.setComment(dto.getComment());
            change.append(String.format(" comment : %s", dto.getComment()));
        }

        this.setParentDir(dto, tableInfo);

        tableInfo.setBizType(dto.getBizType());
        tableInfo.setUpdateUserId(SecurityUtils.getUserId());
        tableInfo.setUpdateTime(new Date());
        tableInfo.setLeaderName(dto.getLeaderName());
        tableInfo.setViewTotal(dto.getViewTotal());
        tableInfo.setViewUser(dto.getViewUser());
        tableInfo.setProcessId(dto.getProcessId());

        //处理审核流程
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(dto.getTemplateAuditProcessEasyDtos())) {
            for (TemplateAuditProcessEasyDto processDTO : dto.getTemplateAuditProcessEasyDtos()) {
                templateService.updateProcessInfoById(processDTO.getTemplateId(),
                        processDTO.getProcessId(), processDTO.getSortIndex());
            }
        }

        this.updateById(tableInfo);
        // 单独处理目录id更新
        if (Objects.isNull(tableInfo.getDirId())) {
            tableInfoMapper.update(null, new UpdateWrapper<TableInfo>().lambda()
                    .set(TableInfo::getDirId, null)
                    .eq(TableInfo::getId, tableInfo.getId()));
        }

        if (change.length() > 0) {
            TableLog tableLog = new TableLog();
            tableLog.setTableId(dto.getId());
            tableLog.setDirId(tableInfo.getDirId());
            tableLog.setTableAlias(tableInfo.getTableAlias());
            tableLog.setTableName(tableInfo.getTableName());
            tableLog.setLogType(LogType.table_update.getVal());
            tableLog.setCreateTime(tableInfo.getUpdateTime());
            tableLog.setUpdateCount(1);
            tableLog.setOperatorId(SecurityUtils.getUserId());
            tableLog.setOperator(SecurityUtils.getUsername());
            tableLog.setContent("更改内容" + change);
            tableLog.setPreContent("更改前" + preContent);
            tableLogService.save(tableLog);
        }

        if (dto.getTableFieldInfos() != null && dto.getTableFieldInfos().size() > 0) {
            for (TableFieldInfo tableFieldInfo : dto.getTableFieldInfos()) {
                TableFieldInfoDto fieldInfo = new TableFieldInfoDto();
                BeanUtils.copyProperties(tableFieldInfo, fieldInfo);
                iTableFieldInfoService.updateField(fieldInfo);
            }
        }

        if (dto.getRelations() != null && dto.getRelations().size() > 0) {
            TableRelationUpdateDto tableRelationUpdateDto = new TableRelationUpdateDto();
            tableRelationUpdateDto.setTableId(dto.getId());
            tableRelationUpdateDto.setList(dto.getRelations());
            relationService.updateRelation(tableRelationUpdateDto);
        }
    }

    private void setParentDir(TableInfoManageDto dto, TableInfo tableInfo) {
        if (null != dto.getDirId()) {
            tableInfo.setDirId(dto.getDirId());
            DataDir dataDir = DataDir.newInstance().selectOne(new QueryWrapper<DataDir>()
                    .eq("icon", CommonConstants.ICON_TABLE)
                    .eq("node_id", tableInfo.getId()));
            if (null != dataDir) {
                dataDir.setParentId(dto.getDirId());
                dataDir.updateById();
            }
        }
    }

    /*****************************************************
     * 天宫易数阁代码
     * 责任人: linweiwu
     */

    @Override
    public void updateDirIdOfTableInfo(TableInfoDto tableInfoDto) {
        tableInfoMapper.updateDirIdOfTableInfo(tableInfoDto);
    }

    @Override
    public List<TableInfo> findAllNotDiyAndSelfDiy() {
        List<TableInfo> list = new ArrayList<>();
        list.addAll(this.list(Wrappers.<TableInfo>query().eq("status", 1).eq("is_diy", 0)));
        list.addAll(this.list(Wrappers.<TableInfo>query().eq("status", 1)
                .eq("is_diy", 1).eq("create_user_id", SecurityUtils.getUserId())));
        return list;
    }

    @Override
    public List<TableInfo> findAllDiy(Long userId) {
        return this.list(Wrappers.<TableInfo>query()
                .eq("status", 1)
                .eq("is_diy", 1)
                .eq(Objects.nonNull(userId), "create_user_id", userId));
    }

    @Override
    public List<TableInfo> findAllNotDiy() {
        List<TableInfo> list = new ArrayList<>();
        list.addAll(this.list(Wrappers.<TableInfo>query().eq("is_diy", 0)));
        return list;
    }


    @Override
    public TableInfo selectTableInfoByTableName(String tn) {
        return tableInfoMapper.selectTableInfoByTableName(tn);
    }

    @Override
    public List<DataManageFormDto> getTablesWithoutDir(Integer menu) {
        return tableInfoMapper.getTablesWithoutDir(menu);
    }

    @Override
    public Object updateStatus(Long tableId) {
        TableInfo tableInfo = tableInfoMapper.selectById(tableId);
        int status = tableInfo.getStatus().equals(1) ? 0 : 1;
        tableInfo.setStatus(status);
        tableInfoMapper.updateById(tableInfo);
        return AjaxResult.success();
    }

    @Override
    public List<TableInfo> listAllAssetsTable(String bizType) {
        return this.list(Wrappers.<TableInfo>query().lambda()
                .eq(TableInfo::getStatus, 1)
                .eq(TableInfo::getIsDiy, 0)
                .and(v -> v.eq(TableInfo::getBizType, bizType).or().eq(TableInfo::getBizType, BizTypeEnum.ALL))

        );
    }

    @Override
    public List<TableInfo> queryByTableIds(List<Long> tableIds) {
        QueryWrapper qw = new QueryWrapper() {{
            in("id", tableIds);
        }};
        return baseMapper.selectList(qw);
    }

    @Override
    public List<TgMetadataInfo> queryMetaDataByAssetIds(List<Long> assetIds) {
        if (assetIds.isEmpty()) {
            return Lists.newArrayList();
        }
        return TgMetadataInfo.newInstance().selectList(new QueryWrapper<TgMetadataInfo>() {{
            in("asset_id", assetIds);
        }});
    }

    private void setOrgAndApplicantName(TablePageVO d) {
        String[] split = d.getLeaderNameOri().split("-");
        if (split.length < 2) {
            return;
        }
        d.setLeaderOri(Optional.ofNullable(split[0]).orElse(""));
        d.setLeaderName(Optional.ofNullable(split[1]).orElse(""));
    }

    /**
     * @see DataDirServiceImpl#pageQueryDir(DirPageQueryRequest) 替换实现
     */
    @Override
    public AjaxResult<IPage<TablePageVO>> pageQueryTable(TablePageQueryRequest request) {
        List<Long> dirIds = null;
        Long dirId = request.getDirId();
        if (Objects.nonNull(dirId) && dirId != 0L) {
            DataDirListVO dirs = dataDirService.selectSonOfParentDir(dirId, DataDirConst.Status.ENABLE);
            dirIds = dirs.getDirs().stream().map(DataDir::getId).collect(Collectors.toList());
            dirIds.add(dirId);
            dirId = null;
        }

        SFunction<TableInfo, ?> field;
        if (Objects.equals(request.getOrderField(), "dis_sort")) {
            field = TableInfo::getDisSort;
        } else {
            field = TableInfo::getUpdateTime;
        }
        boolean asc = Objects.equals(request.getOrderSort(), "ASC");
        IPage<TableInfo> tablePage = tableInfoMapper.selectPage(request.buildPage(), new QueryWrapper<TableInfo>().lambda()
                .eq(Objects.nonNull(dirId), TableInfo::getDirId, dirId)
                .in(org.apache.commons.collections4.CollectionUtils.isNotEmpty(dirIds), TableInfo::getDirId, dirIds)
                .eq(TableInfo::getIsDiy, 0)
                .and(StringUtils.isNotBlank(request.getSearchContent()),
                        v -> v.like(TableInfo::getTableAlias, request.getSearchContent()).or()
                                .like(TableInfo::getTableName, request.getSearchContent()))
                .orderBy(Objects.nonNull(field), asc, field)
        );

        List<Long> curDirIds = Lambda.buildList(tablePage.getRecords(), TableInfo::getDirId);

        Map<Long, String> dirMap = dataDirDAO.queryParentMap(curDirIds);

        // 用户-表单
        List<String> nameList = tablePage.getRecords().stream()
                .map(TableInfo::getLeaderName)
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.toList());
        Map<String, SysUser> userNameMap;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(nameList)) {
            List<SysUser> usersByName = sysUserService.selectUserByUserNames(nameList);
            userNameMap = usersByName.stream().collect(Collectors.toMap(SysUser::getUserName,
                    v -> v, (front, current) -> current));
        } else {
            userNameMap = Collections.emptyMap();
        }
        return AjaxResult.success(com.sinohealth.common.utils.bean.PageUtil.convertMap(tablePage, v -> {
            TablePageVO vo = new TablePageVO();
            BeanUtils.copyProperties(v, vo);
            vo.setDisplayName(v.getTableName());
            vo.setBusinessType(dirMap.get(v.getDirId()));
            String orgName = Optional.ofNullable(userNameMap.get(v.getLeaderName())).map(SysUser::getOrgUserId)
                    .map(SinoipaasUtils::mainEmployeeSelectbyid).map(SinoPassUserDTO::getViewName).orElse("");
            vo.setLeaderNameOri(orgName);
            this.setOrgAndApplicantName(vo);
            return vo;
        }));
    }

    @Override
    public List<TableInfo> getUnLinkedData(List<Long> tableAssetIds) {
        return tableInfoMapper.selectList(new QueryWrapper<TableInfo>() {{
            eq("is_diy", 0);
            if (!tableAssetIds.isEmpty()) {
                notIn("id", tableAssetIds);
            }
        }});
    }

}
