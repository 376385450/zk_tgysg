package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.TableMapping;
import com.sinohealth.system.dto.TableMappingDTO;
import com.sinohealth.system.dto.analysis.SaveTableMappingRequestDTO;

import java.util.List;

/**
 * 表字段映射信息接口
 *
 * @author linkaiwei
 * @date 2021/11/04 10:11
 * @since 1.6.1.0
 */
public interface ITableMappingService extends IService<TableMapping> {

    List<TableMappingDTO> listMapping(Long tableId, Long fieldId);

    void saveMapping(SaveTableMappingRequestDTO dto);

    void deleteMapping(Long mappingId);

}
