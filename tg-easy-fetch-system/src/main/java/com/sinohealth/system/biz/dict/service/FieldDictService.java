package com.sinohealth.system.biz.dict.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.application.dto.ApplicationGranularityDto;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.biz.dict.dto.FieldDictDTO;
import com.sinohealth.system.biz.dict.dto.request.FieldDictBatchSaveRequest;
import com.sinohealth.system.biz.dict.dto.request.FieldDictPageRequest;
import com.sinohealth.system.biz.dict.dto.request.FieldListRequest;
import com.sinohealth.system.dto.analysis.FilterDTO;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:23
 */
public interface FieldDictService {

    AjaxResult<IPage<FieldDictDTO>> pageQuery(FieldDictPageRequest request);

    AjaxResult<Void> edit(FieldDictDTO dictDTO);

    AjaxResult<Void> batchSave(FieldDictBatchSaveRequest saveRequest);

    AjaxResult<Void> deleteById(Long id);

    AjaxResult<List<FieldDictDTO>> listQuery(FieldListRequest request);

    List<FieldDictDTO> listAll();

    Integer countAll();

    List<FieldDict> selectAllIds();

    void fillSort(List<FieldDict> fields);

    void fillFieldNameForFilter(ApplicationGranularityDto... arr);

    void fillFieldNameForFilter(List<FilterDTO> filters);
}
