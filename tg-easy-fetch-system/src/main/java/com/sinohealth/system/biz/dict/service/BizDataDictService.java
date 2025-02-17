package com.sinohealth.system.biz.dict.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dict.dto.BizDataDictPageDTO;
import com.sinohealth.system.biz.dict.dto.BizDataDictValDTO;
import com.sinohealth.system.biz.dict.dto.request.BizDataDictUpsertRequest;
import com.sinohealth.system.biz.dict.dto.request.DataDictPageRequest;
import com.sinohealth.system.dto.table_manage.DataRangeQueryDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 10:23
 */
public interface BizDataDictService {

    AjaxResult<IPage<BizDataDictPageDTO>> pageQuery(DataDictPageRequest request);

    AjaxResult<Void> upsert(BizDataDictUpsertRequest request);

    AjaxResult<List<BizDataDictValDTO>> listDictVal(Long dictId);


    AjaxResult<Void> deleteById(Long id);

    /**
     * 字典 列值搜索
     */
    AjaxResult dataRangeSearch(DataRangeQueryDto rangeQuery);

    /**
     * 搜索非引用类字典
     */
    List<String> searchDictVal(Long dictId, DataRangeQueryDto rangeQuery);


    /**
     * 搜索非引用类字典 计数分页
     */
    Integer countSearchDictVal(Long dictId, DataRangeQueryDto rangeQuery);

    AjaxResult<List<BizDataDictValDTO>> readExcel(MultipartFile file);
}
