package com.sinohealth.system.biz.dataassets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 17:53
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface UserDataAssetsSnapshotMapper extends BaseMapper<UserDataAssetsSnapshot> {

    List<UserDataAssetsSnapshot> groupByAssetsId(@Param("assetsIds") Collection<Long> assetsIds);

    IPage<UserDataAssetsSnapshot> pageWithMain(IPage page, @Param("assetsId") Long assetsId);
}
