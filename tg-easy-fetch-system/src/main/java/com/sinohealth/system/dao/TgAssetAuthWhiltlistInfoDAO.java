package com.sinohealth.system.dao;

import com.sinohealth.system.domain.TgAssetWhitelistInfo;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/15
 */
public interface TgAssetAuthWhiltlistInfoDAO {


    List<TgAssetWhitelistInfo> findWhiteListInfoByUserIdAndDeptId(Long userId, String deptId);


    List<TgAssetWhitelistInfo> findValidServiceWhiteListInfoByAssetIdAndUserIdAndDeptId(Long assetId, Long userId, String deptId);

    List<TgAssetWhitelistInfo> findWhiteListInfoByUserIdAndDeptIdAndType(List<Long> assetId, Long userId, String deptId);

    List<TgAssetWhitelistInfo> findWhiteListInfoByUserIdAndDeptIdAndTypeAndRelatedId(Long userId, String deptId, String type, Long relatedId);

    List<TgAssetWhitelistInfo> findByTypeAndRelatedId(String type, Long relatedId);


}
