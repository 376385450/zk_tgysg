package com.sinohealth.system.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.domain.ApplicationDataUpdateRecord;
import com.sinohealth.system.dto.application.deliver.update.UpdateRecordQueryRequest;
import com.sinohealth.system.dto.application.deliver.update.UpdateRecordStatusParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-12-08 17:53
 */
public interface ApplicationDataUpdateRecordDAO extends IService<ApplicationDataUpdateRecord> {


    List<ApplicationDataUpdateRecord> queryCustomerByApplyIds(Collection<Long> assetIds);

    /**
     * TODO 依据资产版本查询同步情况
     * 申请数据的同步记录，限制数量
     *
     * @return applicationId -> record
     */
    Map<Long, ApplicationDataUpdateRecord> queryLatestByAssetIds(Collection<Long> assetIds);

    /**
     * 查询指定资产和版本的最新同步记录
     */
    Map<String, ApplicationDataUpdateRecord> queryLatestByAssetVersions(Collection<String> assetVersions);

    /**
     * TODO 依据资产版本查询同步情况
     */
    IPage<ApplicationDataUpdateRecord> pageQueryByAppId(UpdateRecordQueryRequest request);

    ApplicationDataUpdateRecord querySyncApplication(UpdateRecordStatusParam param);
}
