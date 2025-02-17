package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.config.DataConnection;
import com.sinohealth.common.enums.LogType;
import com.sinohealth.common.enums.StatusTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SqlFilter;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.async.AsyncManager;
import com.sinohealth.system.async.factory.AsyncFactory;
import com.sinohealth.system.biz.ck.constant.CkClusterType;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.dto.TableFieldInfoDto;
import com.sinohealth.system.dto.TableFieldSortDTO;
import com.sinohealth.system.mapper.TableFieldInfoMapper;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.service.ITableFieldInfoService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.ITableLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author dataplatform
 * @date 2021-04-24
 */
@Service
public class TableFieldInfoServiceImpl extends ServiceImpl<TableFieldInfoMapper, TableFieldInfo> implements ITableFieldInfoService {

    private static final Logger log = LoggerFactory.getLogger("table-log");

    @Autowired
    private ITableInfoService tableInfoService;

    @Autowired
    private ITableLogService tableLogService;

    @Autowired
    TgCkProviderMapper tgCkProviderMapper;

    @Resource(name = "slaveDataSource")
    private DataSource slaveDataSource;


    @Override
    @Transactional
    public void updateField(TableFieldInfoDto dto) {
        // 查询表信息
        TableInfo tableInfo = tableInfoService.getById(dto.getTableId());

        // 移除修改逻辑
        // 获取表对应的数据库连接
//        final DataConnection dataConnection = new DataConnection(tableInfo.getTableName(), slaveDataSource,
//                new JdbcTemplate(slaveDataSource), DatabaseEnum.CLICKHOUSE.getFeature());
        Date now = new Date();
        if (dto.getId() == null) {
            this.addField(dto, tableInfo, null, now);
        } else {
            this.modifyField(dto, tableInfo, null, now);
        }

        tableInfoService.update(Wrappers.<TableInfo>update()
                .set("update_time", now)
                .set("update_user_id", SecurityUtils.getUserId())
                .eq("id", tableInfo.getId()));
    }

    private void modifyField(TableFieldInfoDto dto, TableInfo tableInfo, DataConnection dataConnection, Date now) {
        // 表修改字段
        TableFieldInfo fieldInfo = this.getById(dto.getId());
        if (!fieldInfo.getTableId().equals(dto.getTableId())) {
            throw new CustomException("无权限修改字段");
        }
        String logPreContent = getLogPreContent(fieldInfo, tableInfo);
        // 语法差异
        StringBuilder alterSql = new StringBuilder("ALTER TABLE ");

        alterSql.append(tableInfo.getTableName());

        if (fieldInfo.getFieldName().equals(dto.getFieldName())) {
            alterSql.append(" on cluster " + CkClusterType.DEFAULT + " modify column ");
            alterSql.append(String.format("`%s`", fieldInfo.getFieldName()));
            alterSql.append("  ");

            String typeFmt;
            if (BooleanUtils.isTrue(dto.getEmpty())) {
                typeFmt = "%s";
            } else {
                typeFmt = " Nullable(%s) ";
            }
            String type = this.buildType(dto);
            alterSql.append(String.format(typeFmt, type));

            if (!StringUtils.isEmpty(dto.getComment())) {
                alterSql.append(String.format(" COMMENT '%s' ", SqlFilter.filter(dto.getComment())));
            }
        } else {
            alterSql.append(" rename column ");
            alterSql.append(String.format("`%s`", fieldInfo.getFieldName()));
            alterSql.append(" to  ");
            alterSql.append(String.format("`%s`", SqlFilter.filter(dto.getFieldName())));
        }


        String sql = alterSql.toString();
        log.info(sql);
        if (Objects.nonNull(dataConnection)) {
            dataConnection.getJdbcOperations().update(sql);
            //更新分布式表结构
            String disturbSql = sql.replace(tableInfo.getTableName(), tableInfo.getTableNameDistributed());
            log.info(disturbSql);
            dataConnection.getJdbcOperations().update(disturbSql);
        }

        BeanUtils.copyProperties(dto, fieldInfo);
        fieldInfo.setStatus(true);
        fieldInfo.setDirId(tableInfo.getDirId());
        fieldInfo.setTableId(tableInfo.getId());
        fieldInfo.setUpdateTime(now);
        fieldInfo.setUpdateUserId(SecurityUtils.getUserId());
        this.updateById(fieldInfo);
        AsyncManager.me().execute(AsyncFactory.createTableLog(tableInfo, SecurityUtils.getUserId(),
                SecurityUtils.getUsername(), LogType.metadata_update, sql, 1, false, now,
                dto.getComment(), logPreContent));
    }

    private String buildType(TableFieldInfoDto dto) {
        StringBuilder result = new StringBuilder();
        String type = SqlFilter.filter(dto.getDataType());
        result.append(type);
        if (dto.getLength() > 0) {
            result.append("(");
            result.append(dto.getLength());
            if (dto.getScale() > 0) {
                result.append(",");
                result.append(dto.getScale());
            }
            result.append(")");
        }
        return result.toString();
    }

    private void addField(TableFieldInfoDto dto, TableInfo tableInfo, DataConnection dataConnection, Date now) {
        TableFieldInfo field = this.getOne(Wrappers.<TableFieldInfo>query()
                .eq("table_id", dto.getTableId())
                .eq("field_name", dto.getFieldName()));
        if (field != null) {
            throw new CustomException("字段已存在");
        }
        // 新增表字段SQL
        StringBuilder alterSql = new StringBuilder();

        // 语法差异
        alterSql.append("ALTER TABLE ").append(tableInfo.getTableName()).append(" on cluster default_cluster ADD column ");

        String typeFmt;
        if (BooleanUtils.isTrue(dto.getEmpty())) {
            typeFmt = "%s";
        } else {
            typeFmt = " Nullable(%s) ";
        }
        String fieldType = this.buildType(dto);
        alterSql.append(String.format("`%s` ", SqlFilter.filter(dto.getFieldName())))
                .append(String.format(typeFmt, fieldType));

        /* alterSql.append(dto.getEmpty() ? " DEFAULT NULL " : " NOT NULL ");*/
        if (!StringUtils.isEmpty(dto.getComment())) {
            alterSql.append(String.format(" COMMENT '%s' ", SqlFilter.filter(dto.getComment())));
        }

        String sql = alterSql.toString();
        if (Objects.nonNull(dataConnection)) {
            log.info(sql);
            dataConnection.getJdbcOperations().update(sql);
            //更新分布式表结构
            String disturbSql = sql.replace(tableInfo.getTableName(), tableInfo.getTableNameDistributed());
            log.info(disturbSql);
            dataConnection.getJdbcOperations().update(disturbSql);
        }

        TableFieldInfo fieldInfo = new TableFieldInfo();
        BeanUtils.copyProperties(dto, fieldInfo);
        fieldInfo.setCreateTime(now);
        fieldInfo.setStatus(true);
        fieldInfo.setDirId(tableInfo.getDirId());
        fieldInfo.setTableId(tableInfo.getId());
        fieldInfo.setCreateUserId(SecurityUtils.getUserId());
        this.save(fieldInfo);
        AsyncManager.me().execute(AsyncFactory.createTableLog(tableInfo, SecurityUtils.getUserId(),
                SecurityUtils.getUsername(), LogType.metadata_create, sql, 1, false, now, dto.getComment()));
    }

    /**
     * 更新表字段排序
     *
     * @param tableId 表ID
     * @param sortDTO 字段排序列表
     * @return 结果
     * @author linkaiwei
     * @date 2021-11-09 10:27:54
     * @since 1.6.1.0
     */
    @Override
    public Boolean updateFieldSort(Long tableId, TableFieldSortDTO sortDTO) {
        final LambdaQueryWrapper<TableFieldInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(TableFieldInfo::getTableId, tableId);
        queryWrapper.ne(TableFieldInfo::getStatus, StatusTypeEnum.IS_DELETE.getId());
        queryWrapper.orderByAsc(TableFieldInfo::getSort);
        final List<TableFieldInfo> fieldInfoList = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(fieldInfoList)) {
            throw new CustomException("表字段信息不存在");
        }
        final List<TableFieldSortDTO.FieldSortDTO> list = sortDTO.getList();

        // 批量更新排序
        final Date now = new Date();
        final Long userId = SecurityUtils.getUserId();
        fieldInfoList.forEach(tableFieldInfo -> {
            AtomicBoolean update = new AtomicBoolean(false);

            list.forEach(fieldSortDTO -> {
                if (tableFieldInfo.getId().longValue() == fieldSortDTO.getId()
                        && tableFieldInfo.getSort() != null
                        && tableFieldInfo.getSort().intValue() != fieldSortDTO.getSort()) {
                    update.set(true);
                    tableFieldInfo.setSort(fieldSortDTO.getSort());
                    tableFieldInfo.setUpdateTime(now);
                    tableFieldInfo.setUpdateUserId(userId);
                }
            });

            // 更新排序
            if (update.get()) {
                baseMapper.updateById(tableFieldInfo);
            }
        });

        return true;
    }


    public String getLogPreContent(TableFieldInfo fieldInfo, TableInfo tableInfo) {
        StringBuffer alterSql = new StringBuffer(" table ");
        alterSql.append(tableInfo.getTableName());
        alterSql.append("  ");
        alterSql.append(String.format("`%s`", fieldInfo.getFieldName()));

        alterSql.append("  ");
        alterSql.append(SqlFilter.filter(fieldInfo.getDataType()));
        if (fieldInfo.getLength() > 0) {
            alterSql.append("(");
            alterSql.append(fieldInfo.getLength());
            if (fieldInfo.getScale() > 0) {
                alterSql.append(",");
                alterSql.append(fieldInfo.getScale());
            }
            alterSql.append(")");
        }

        alterSql.append(fieldInfo.isEmpty() ? " default null " : " not null ");

        if (!StringUtils.isEmpty(fieldInfo.getComment())) {
            alterSql.append(String.format(" COMMENT '%s' ", SqlFilter.filter(fieldInfo.getComment())));
        }
        return alterSql.toString();
    }


    /**
     * 设置表字段的重点字段
     *
     * @param dto 表字段信息
     * @author linkaiwei
     * @date 2022-02-18 14:53:56
     * @since 1.6.5.0
     */
    @Override
    public void updateMajorField(TableFieldInfoDto dto) {
        final TableFieldInfo tableFieldInfo = baseMapper.selectById(dto.getId());
        if (tableFieldInfo == null) {
            throw new CustomException("表字段信息不存在");
        }

        // 更新字段信息
        tableFieldInfo.setMajorField(dto.isMajorField());
        tableFieldInfo.setUpdateTime(new Date());
        tableFieldInfo.setUpdateUserId(SecurityUtils.getUserId());
        baseMapper.updateById(tableFieldInfo);
    }

    @Override
    public List<TableFieldInfo> findListByIds(List<Long> ids) {
        return baseMapper.findListByIds(ids);
    }

    @Override
    public int getCountByTableId(Long tableId, String fieldName) {
        return baseMapper.getCountByTableId(tableId, fieldName);
    }

    @Override
    public List<String> getFieldsByTableName(String tableName) {
        return baseMapper.getFieldsByTableName(tableName);
    }

    @Override
    public List<TableFieldInfo> findListByFieldIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return baseMapper.findListByFieldIds(ids);
    }

    @Override
    public List<TableFieldInfo> getFieldsByTableId(Long tableId) {
        if (Objects.isNull(tableId)) {
            return Collections.emptyList();
        }
        return baseMapper.selectList(new QueryWrapper<TableFieldInfo>().lambda().eq(TableFieldInfo::getTableId, tableId));
    }

    @Override
    public void deleteByTableId(Long tableId) {
        baseMapper.delete(new QueryWrapper<TableFieldInfo>().lambda().eq(TableFieldInfo::getTableId, tableId));
    }
}
