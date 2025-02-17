package com.sinohealth.system.service;

import com.sinohealth.system.dto.GetTableMonitorDataRequestDTO;
import com.sinohealth.system.dto.TableMonitorDataDTO;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-14 9:14 上午
 */
public interface IMonitorDataService {

    /**
     * 获取监控数据
     * @param requestDTO
     * @return
     */
    TableMonitorDataDTO getData(GetTableMonitorDataRequestDTO requestDTO);
}
