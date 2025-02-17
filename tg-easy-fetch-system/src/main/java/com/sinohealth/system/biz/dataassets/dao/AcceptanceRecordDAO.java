package com.sinohealth.system.biz.dataassets.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.dataassets.domain.AcceptanceRecord;
import com.sinohealth.system.biz.dataassets.domain.AssetsVersion;

import java.util.Collection;
import java.util.Map;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-17 14:34
 */
public interface AcceptanceRecordDAO extends IService<AcceptanceRecord> {

    /**
     * @return key（assetsId#version） -> record
     */
    Map<String, AcceptanceRecord> queryByAssetsIdAndVersion(Collection<? extends AssetsVersion> assets);

    /**
     * @return @return applyId -> record
     */
    Map<Long, AcceptanceRecord> queryLatestStateByApplyId(Collection<Long> applyIds);
}
