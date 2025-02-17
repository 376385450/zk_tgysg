package com.sinohealth.system.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.common.enums.StaffType;
import com.sinohealth.common.enums.WhitlistServiceType;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.system.dao.TgAssetAuthWhiltlistInfoDAO;
import com.sinohealth.system.domain.TgAssetWhitelistInfo;
import com.sinohealth.system.mapper.TgAssetAuthWhiltlistInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/8/15
 */
@Repository
public class TgAssetAuthWhiltlistInfoDAOImpl implements TgAssetAuthWhiltlistInfoDAO {

    @Autowired
    TgAssetAuthWhiltlistInfoMapper mapper;


    @Override
    public List<TgAssetWhitelistInfo> findWhiteListInfoByUserIdAndDeptId(Long userId, String deptId) {
        final LambdaQueryWrapper<TgAssetWhitelistInfo> wp = Wrappers.<TgAssetWhitelistInfo>lambdaQuery()
                .or(
                        wrapper -> wrapper.eq(TgAssetWhitelistInfo::getStaffType, StaffType.DEPT)
                                .eq(TgAssetWhitelistInfo::getStaffId, deptId)
                ).or(
                        wrapper -> wrapper.eq(TgAssetWhitelistInfo::getStaffType, StaffType.USER)
                                .eq(TgAssetWhitelistInfo::getStaffId, userId)
                );
        return mapper.selectList(wp);
    }

    @Override
    public List<TgAssetWhitelistInfo> findValidServiceWhiteListInfoByAssetIdAndUserIdAndDeptId(Long assetId, Long userId, String deptId) {
        final LambdaQueryWrapper<TgAssetWhitelistInfo> wp = Wrappers.<TgAssetWhitelistInfo>lambdaQuery()
                .eq(TgAssetWhitelistInfo::getServiceType, WhitlistServiceType.SERVICE_AUTH)
                .eq(TgAssetWhitelistInfo::getAssetId, assetId)
                .gt(TgAssetWhitelistInfo::getExpirationDate, DateUtils.getTime())
                .and(
                        wrapper -> wrapper.eq(TgAssetWhitelistInfo::getStaffType, StaffType.DEPT)
                                .eq(TgAssetWhitelistInfo::getStaffId, deptId)
                                .or()
                                .eq(TgAssetWhitelistInfo::getStaffType, StaffType.USER)
                                .eq(TgAssetWhitelistInfo::getStaffId, userId)
                );
        return mapper.selectList(wp);
    }

    @Override
    public List<TgAssetWhitelistInfo> findWhiteListInfoByUserIdAndDeptIdAndType(List<Long> assetId, Long userId, String deptId) {
        final LambdaQueryWrapper<TgAssetWhitelistInfo> wp = Wrappers.<TgAssetWhitelistInfo>lambdaQuery()
                .eq(TgAssetWhitelistInfo::getServiceType, WhitlistServiceType.SERVICE_AUTH)
                .in(TgAssetWhitelistInfo::getAssetId, assetId)
                .gt(TgAssetWhitelistInfo::getExpirationDate, DateUtils.getTime())
                .and(
                        wrapper -> wrapper.eq(TgAssetWhitelistInfo::getStaffType, StaffType.DEPT)
                                .eq(TgAssetWhitelistInfo::getStaffId, deptId)
                                .or()
                                .eq(TgAssetWhitelistInfo::getStaffType, StaffType.USER)
                                .eq(TgAssetWhitelistInfo::getStaffId, userId)
                );
        return mapper.selectList(wp);
    }

    @Override
    public List<TgAssetWhitelistInfo> findWhiteListInfoByUserIdAndDeptIdAndTypeAndRelatedId(Long userId, String deptId, String type, Long relatedId) {
        final LambdaQueryWrapper<TgAssetWhitelistInfo> wp = Wrappers.<TgAssetWhitelistInfo>lambdaQuery()
                .eq(TgAssetWhitelistInfo::getType, type)
                .eq(TgAssetWhitelistInfo::getRelatedId, relatedId)
                .and(
                        wrapper -> wrapper.or(
                                wrapper_1 -> wrapper_1.eq(TgAssetWhitelistInfo::getStaffType, StaffType.DEPT)
                                        .eq(TgAssetWhitelistInfo::getStaffId, deptId)
                        ).or(
                                wrapper_1 -> wrapper_1.eq(TgAssetWhitelistInfo::getStaffType, StaffType.USER)
                                        .eq(TgAssetWhitelistInfo::getStaffId, userId)
                        )
                );
        return mapper.selectList(wp);
    }

    @Override
    public List<TgAssetWhitelistInfo> findByTypeAndRelatedId(String type, Long relatedId) {
        final LambdaQueryWrapper<TgAssetWhitelistInfo> wq = Wrappers.<TgAssetWhitelistInfo>lambdaQuery()
                .eq(TgAssetWhitelistInfo::getType, type)
                .eq(TgAssetWhitelistInfo::getRelatedId, relatedId);
        return mapper.selectList(wq);
    }

}
