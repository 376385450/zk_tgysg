package com.sinohealth.system.service;

import com.sinohealth.system.domain.TgDataDescription;
import com.sinohealth.system.dto.DataDescDocDTO;
import com.sinohealth.system.dto.DataDescDocUpdateReqDTO;

import java.util.Collection;
import java.util.Map;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-07 14:20
 */
public interface DataDescriptionService {

    Integer update(DataDescDocUpdateReqDTO reqDTO);

    DataDescDocDTO getDetail(Long assetsId);

    /**
     * 获取默认的说明文档
     */
//    TgDataDescription getDefaultByApplicationId(Long assetsId);

    TgDataDescription getByAssetsId(Long assetsId);

    /**
     * @return applicationId -> descId
     */
    Map<Long, Integer> queryByAssetsIds(Collection<Long> assetsIds);
}
