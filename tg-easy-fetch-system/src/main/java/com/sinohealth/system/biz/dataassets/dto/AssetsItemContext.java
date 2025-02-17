package com.sinohealth.system.biz.dataassets.dto;

import com.sinohealth.system.biz.dataassets.domain.AcceptanceRecord;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-21 15:54
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetsItemContext {
    private Map<Long, String> userNameMap;
    /**
     * 申请id -> 粒度
     */
    private Map<Long, String> timeGraMap;
    private Map<Long, Integer> versionMap;
    private Map<String, AcceptanceRecord> recordMap;
    private UserDataAssets assets;
}
