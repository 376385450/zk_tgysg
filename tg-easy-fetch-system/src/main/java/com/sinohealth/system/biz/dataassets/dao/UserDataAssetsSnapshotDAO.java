package com.sinohealth.system.biz.dataassets.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 17:52
 */
public interface UserDataAssetsSnapshotDAO extends IService<UserDataAssetsSnapshot> {

    int countSnapshot(Long assetsId);

    Map<Long, Integer> batchCountSnapshot(Collection<Long> assetsId);

    UserDataAssetsSnapshot queryByAssetsId(Long assetsId, Integer version);

    List<UserDataAssetsSnapshot> queryByAssetsIds(Collection<Long> assetsIds);

}
