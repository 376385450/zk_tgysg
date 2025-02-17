package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.enums.StatusTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.bean.BeanUtils;
import com.sinohealth.system.domain.TableMapping;
import com.sinohealth.system.dto.analysis.SaveTableMappingRequestDTO;
import com.sinohealth.system.dto.TableMappingDTO;
import com.sinohealth.system.mapper.TableMappingMapper;
import com.sinohealth.system.service.IDataDirService;
import com.sinohealth.system.service.ITableMappingService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 表字段映射信息接口实现
 *
 * @author linkaiwei
 * @date 2021/11/04 10:44
 * @since 1.6.1.0
 */
@Service
public class TableMappingServiceImpl extends ServiceImpl<TableMappingMapper, TableMapping> implements ITableMappingService {

    @Resource
    private TableMappingMapper tableMappingMapper;
    @Autowired
    private IDataDirService dataDirService;


    /**
     * 字段映射列表
     *
     * @param tableId 表ID
     * @return 结果
     * @author linkaiwei
     * @date 2021-11-03 17:06:33
     * @since 1.6.1.0
     */
    @Override
    public List<TableMappingDTO> listMapping(Long tableId, Long fieldId) {
        List<TableMappingDTO> list = tableMappingMapper.listMapping(tableId, fieldId);
        list.forEach(tableMappingDTO -> {
            tableMappingDTO.setDirName(dataDirService.getById(tableMappingDTO.getRelationDirId()).getDirName());
        });
        return list;
    }

    /**
     * 保存字段映射
     *
     * @param requestDTO 详情见 {@link SaveTableMappingRequestDTO}
     * @author linkaiwei
     * @date 2021-11-03 17:06:33
     * @since 1.6.1.0
     */
    @Transactional
    @Override
    public void saveMapping(SaveTableMappingRequestDTO requestDTO) {
        final Long tableId = requestDTO.getTableId();
        final List<TableMappingDTO> list = requestDTO.getList();

        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(tableMappingDTO -> {
                TableMapping tableMapping = new TableMapping();
                BeanUtils.copyProperties(tableMappingDTO, tableMapping);
                tableMapping.setTableId(tableId);
                tableMapping.setCreateUserId(SecurityUtils.getUserId());
                tableMapping.setCreateTime(new Date());
                tableMapping.setUpdateUserId(tableMapping.getCreateUserId());
                tableMapping.setUpdateTime(tableMapping.getCreateTime());
                tableMapping.setStatus(StatusTypeEnum.IS_ENABLE.getId());

                baseMapper.insert(tableMapping);
            });
        }
    }

    /**
     * 删除字段映射
     *
     * @param mappingId 映射字段信息ID
     * @author linkaiwei
     * @date 2021-11-03 17:06:33
     * @since 1.6.1.0
     */
    @Override
    public void deleteMapping(Long mappingId) {
        final TableMapping tableMapping = baseMapper.selectById(mappingId);
        if (tableMapping == null) {
            throw new CustomException("字段映射信息不存在");
        }

        tableMapping.setUpdateUserId(SecurityUtils.getUserId());
        tableMapping.setUpdateTime(new Date());
        tableMapping.setStatus(StatusTypeEnum.IS_DELETE.getId());
        baseMapper.updateById(tableMapping);
    }

}
