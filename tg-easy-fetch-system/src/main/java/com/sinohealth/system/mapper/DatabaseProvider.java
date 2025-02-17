package com.sinohealth.system.mapper;

import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.dto.GetDataInfoRequestDTO;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-05 11:01
 */
public interface DatabaseProvider {

    List<LinkedHashMap<String, Object>> selectApplicationDataFromCk(TgApplicationInfo applicationInfo, String whereSql, GetDataInfoRequestDTO requestDTO);

    Integer selectCountApplicationDataFromCk(TgApplicationInfo applicationInfo);
}
