package com.sinohealth.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.TableRelation;
import com.sinohealth.system.dto.TableRelationDto;
import com.sinohealth.system.dto.TableRelationUpdateDto;

import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author dataplatform
 * @date 2021-04-27
 */
public interface ITableRelationService extends IService<TableRelation> {

    public List<TableRelationDto> getList(Long tableId,Long dirId);

    public void updateRelation(TableRelationUpdateDto dto);


    boolean delete(Long relationId, String dirId);
}
