package com.sinohealth.system.service;

import com.sinohealth.system.domain.vo.FastEntryVO;
import com.sinohealth.system.dto.FastEntryUpsertRequest;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-04-23 13:50
 */
public interface FastEntryService {

    List<FastEntryVO> queryByUser();

    void upsert(FastEntryUpsertRequest request);
}
