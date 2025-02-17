package com.sinohealth.system.biz.dataassets.service;

import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.system.biz.dataassets.dto.AcceptanceRecordDTO;
import com.sinohealth.system.biz.dataassets.dto.request.AcceptListRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AcceptRequest;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-17 14:37
 */
public interface AcceptanceRecordService {

    AjaxResult<Void> accept(AcceptRequest acceptRequest);

    AjaxResult<List<AcceptanceRecordDTO>> list(AcceptListRequest request);
}
