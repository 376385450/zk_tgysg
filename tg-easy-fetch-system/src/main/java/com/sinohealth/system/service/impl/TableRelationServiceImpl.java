package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.enums.LogType;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.async.AsyncManager;
import com.sinohealth.system.async.factory.AsyncFactory;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TableRelation;
import com.sinohealth.system.dto.TableRelationDto;
import com.sinohealth.system.dto.TableRelationUpdateDto;
import com.sinohealth.system.mapper.TableRelationMapper;
import com.sinohealth.system.service.IDataDirService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.ITableRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author dataplatform
 * @date 2021-04-27
 */
@Service
public class TableRelationServiceImpl extends ServiceImpl<TableRelationMapper, TableRelation> implements ITableRelationService {

    @Autowired
    private ITableInfoService tableInfoService;
    @Autowired
    private IDataDirService dataDirService;


    @Override
    public List<TableRelationDto> getList(Long tableId, Long dirId) {
        List<TableRelationDto> list = baseMapper.getList(tableId, null);
        list.forEach(tableRelationDto -> {
            DataDir dataDir = dataDirService.getById(tableRelationDto.getDirId());
            if (dataDir != null) {
                tableRelationDto.setDirName(dataDir.getDirName());
            }
        });
        return list;
    }

    @Override
    @Transactional
    public void updateRelation(TableRelationUpdateDto dto) {

        if (!ObjectUtils.isEmpty(dto.getList())) {
            TableInfo tableInfo = tableInfoService.getById(dto.getTableId());

            List<TableRelation> tableRelations = this.list(Wrappers.<TableRelation>query().eq("table_id", dto.getTableId()));
            Date now = new Date();
            List<TableRelation> allList = dto.getList().stream().map(u -> {
                if (u.getRefDirId() == null) {
                    throw new CustomException("请选择目录库");
                }
                if (u.getFieldId() == null) {
                    throw new CustomException("请选择本表字段");
                }
                if (u.getRefTableId() == null) {
                    throw new CustomException("请选择关联表");
                }
                if (u.getRefFieldId() == null) {
                    throw new CustomException("请选择关联表字段");
                }
                TableRelation relation = new TableRelation();
                relation.setTableId(dto.getTableId());
                relation.setFieldId(u.getFieldId());
                relation.setDirId(tableInfo.getDirId());
                relation.setRefFieldId(u.getRefFieldId());
                relation.setRefTableId(u.getRefTableId());
                relation.setCreateUserId(SecurityUtils.getUserId());
                relation.setCreateTime(now);
                return relation;
            }).collect(Collectors.toList());


            //逆向
            List<TableRelation> reverseAllList = dto.getList().stream().map(u -> {
                TableRelation copy = new TableRelation();
                copy.setTableId(u.getRefTableId());
                copy.setFieldId(u.getRefFieldId());
                copy.setDirId(u.getRefDirId());
                copy.setRefFieldId(u.getFieldId());
                copy.setRefTableId(dto.getTableId());
                copy.setCreateUserId(SecurityUtils.getUserId());
                copy.setCreateTime(now);
                return copy;
            }).collect(Collectors.toList());
            List<Long> reverseTableId = reverseAllList.stream().map(TableRelation::getTableId).distinct().collect(Collectors.toList());
            List<TableRelation> reverseTableRelations = this.list(Wrappers.<TableRelation>query().in("table_id", reverseTableId));
            if (ObjectUtils.isEmpty(reverseTableRelations)) {
                this.saveBatch(reverseAllList);

                Map<Long, List<TableRelation>> collect = reverseAllList.stream().collect(Collectors.groupingBy(TableRelation::getTableId));
                for (Map.Entry<Long, List<TableRelation>> longListEntry : collect.entrySet()) {
                    StringBuffer saveContent = new StringBuffer();
                    longListEntry.getValue().forEach(t -> {
                        saveContent.append(String.format("添加关联 tableId : %s  fieldId: %s \n", t.getRefTableId(), t.getRefFieldId()));
                    });
                    TableInfo reverseTableInfo = tableInfoService.getById(longListEntry.getKey());

                    AsyncManager.me().execute(AsyncFactory.createTableLog(reverseTableInfo, SecurityUtils.getUserId(), SecurityUtils.getUsername(), LogType.relation_create, saveContent.toString(), 1, false, now, null));
                }


            } else {
                reverseAllList.stream().forEach(a -> {
                    reverseTableRelations.forEach(b -> {
                        if (a.equals(b)) {
                            a.setId(b.getId());
                        }
                    });
                    saveOrUpdate(a);
                });
                Map<Long, List<TableRelation>> collect = reverseAllList.stream().filter(a -> a.getId() == null).collect(Collectors.groupingBy(TableRelation::getTableId));
                for (Map.Entry<Long, List<TableRelation>> longListEntry : collect.entrySet()) {
                    StringBuffer saveContent = new StringBuffer();
                    longListEntry.getValue().forEach(t -> {
                        saveContent.append(String.format("添加关联 tableId : %s  fieldId: %s \n", t.getRefTableId(), t.getRefFieldId()));
                    });
                    TableInfo reverseTableInfo = tableInfoService.getById(longListEntry.getKey());

                    AsyncManager.me().execute(AsyncFactory.createTableLog(reverseTableInfo, SecurityUtils.getUserId(), SecurityUtils.getUsername(), LogType.relation_create, saveContent.toString(), 1, false, now, null));
                }


                //Map<Long, List<TableRelation>> notNullCollect = reverseAllList.stream().filter(a -> a.getId() != null).collect(Collectors.groupingBy(TableRelation::getTableId));
                //for (Map.Entry<Long, List<TableRelation>> longListEntry : notNullCollect.entrySet()) {
                //    StringBuffer updateContent = new StringBuffer();
                //    longListEntry.getValue().forEach(t -> {
                //        updateContent.append(String.format("添加关联 tableId : %s  fieldId: %s \n", t.getRefTableId(), t.getRefFieldId()));
                //    });
                //
                //    StringBuffer preContent = new StringBuffer();
                //    longListEntry.getValue().forEach(t -> {
                //        reverseTableRelations.forEach(b -> {
                //            if (t.getId().equals(b.getId())) {
                //                preContent.append(String.format("前 tableId : %s  fieldId: %s \n", b.getRefTableId(), b.getRefFieldId()));
                //            }
                //        });
                //    });
                //    TableInfo reverseTableInfo = tableInfoService.getById(longListEntry.getKey());
                //
                //    AsyncManager.me().execute(AsyncFactory.createTableLog(reverseTableInfo,SecurityUtils.getUserId(),SecurityUtils.getUsername(),LogType.relation_create,updateContent.toString(),1,false,now,null));
                //}

            }

            //正向
            List<TableRelation> saveList = new ArrayList<>();
            List<TableRelation> deleteList = new ArrayList<>();
            if (ObjectUtils.isEmpty(tableRelations)) {
                this.saveBatch(allList);
                StringBuffer saveContent = new StringBuffer();
                allList.forEach(t -> {
                    saveContent.append(String.format("添加关联 tableId : %s  fieldId: %s \n", t.getRefTableId(), t.getRefFieldId()));
                });

                AsyncManager.me().execute(AsyncFactory.createTableLog(tableInfo, SecurityUtils.getUserId(), SecurityUtils.getUsername(), LogType.relation_create, saveContent.toString(), 1, false, now));

            } else {
                tableRelations.stream().forEach(t -> {
                    if (!allList.stream().anyMatch(a -> a.equals(t))) {
                        deleteList.add(t);
                    }
                });
                allList.stream().forEach(a -> {
                    if (!tableRelations.stream().anyMatch(t -> t.equals(a))) {
                        saveList.add(a);
                    }
                });

                if (!ObjectUtils.isEmpty(deleteList)) {
                    this.removeByIds(deleteList.stream().map(t -> t.getId()).collect(Collectors.toList()));

                    StringBuffer deleteContent = new StringBuffer();
                    deleteList.forEach(t -> {
                        deleteContent.append(String.format("删除关联 tableId : %s  fieldId: %s \n", t.getRefTableId(), t.getRefFieldId()));
                    });
                    AsyncManager.me().execute(AsyncFactory.createTableLog(tableInfo, SecurityUtils.getUserId(), SecurityUtils.getUsername(), LogType.relation_delete, deleteContent.toString(), 1, false, now, "表单详情-修改元数据信息-关联关系::有重复表"));
                }
                if (!ObjectUtils.isEmpty(saveList)) {
                    this.saveBatch(saveList);
                    StringBuffer saveContent = new StringBuffer();
                    saveList.forEach(t -> {
                        saveContent.append(String.format("添加关联 tableId : %s  fieldId: %s \n", t.getRefTableId(), t.getRefFieldId()));
                    });
                    AsyncManager.me().execute(AsyncFactory.createTableLog(tableInfo, SecurityUtils.getUserId(), SecurityUtils.getUsername(), LogType.relation_create, saveContent.toString(), 1, false, now));
                }
            }

            tableInfoService.update(Wrappers.<TableInfo>update().set("update_time", now).set("update_user_id", SecurityUtils.getUserId()).eq("id", tableInfo.getId()));
        }

    }

    @Override
    public boolean delete(Long relationId, String dirId) {

        TableRelation byId = getById(relationId);
        TableInfo tableInfo = tableInfoService.getById(byId.getTableId());
        Map<String, Object> columnMap = new HashMap<>(2);
        columnMap.put("id", relationId);
        columnMap.put("dir_id", dirId);
        boolean b = this.removeByMap(columnMap);

        if (b) {
            String deleteContent = String.format("添加关联 tableId : %s  fieldId: %s \n", byId.getRefTableId(), byId.getRefFieldId());
            Date now = new Date();
            AsyncManager.me().execute(AsyncFactory.createTableLog(tableInfo, SecurityUtils.getUserId(), SecurityUtils.getUsername(), LogType.relation_delete, deleteContent.toString(), 1, false, now, "/metadata/relation/delete接口删除关联表数据"));

        }
        return b;
    }


}
