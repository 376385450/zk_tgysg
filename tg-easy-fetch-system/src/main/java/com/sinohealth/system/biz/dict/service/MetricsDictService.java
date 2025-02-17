package com.sinohealth.system.biz.dict.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dict.domain.MetricsDict;
import com.sinohealth.system.biz.dict.dto.MetricsDictDTO;
import com.sinohealth.system.biz.dict.dto.request.DictCommonPageRequest;
import com.sinohealth.system.biz.dict.dto.request.TableMetricsQueryRequest;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:24
 */
public interface MetricsDictService {

    AjaxResult<IPage<MetricsDictDTO>> pageQuery(DictCommonPageRequest request);

    AjaxResult<List<MetricsDictDTO>> queryAllForDesc(Long assetsId);

    AjaxResult<Void> upsert(MetricsDictDTO request);

    AjaxResult<Void> deleteById(Long id);

    AjaxResult<List<MetricsDictDTO>> queryByTableId(TableMetricsQueryRequest request);

    List<MetricsDictDTO> listAll();

    List<MetricsDict> selectAllIds();

    void fillSort(List<MetricsDict> fields);
}
