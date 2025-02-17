package com.sinohealth.system.biz.dataassets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.entity.UsableDataAssetsEntity;
import com.sinohealth.system.biz.dataassets.dto.UserDataAssetResp;
import com.sinohealth.system.biz.dataassets.dto.request.FlowAssetsAutoPageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.FlowAssetsPageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.MyAssetRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-05-09 17:53
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface UserDataAssetsMapper extends BaseMapper<UserDataAssets> {

    IPage<UserDataAssetResp> pageMyAsset(IPage page, @Param("request") MyAssetRequest request);

    List<UserDataAssetResp> listAssets(@Param("request") MyAssetRequest request);

    List<UsableDataAssetsEntity> pageQueryFlowAssets(@Param("request") FlowAssetsPageRequest request);

    List<UsableDataAssetsEntity> pageQueryFlowAssetsForAuto(@Param("request") FlowAssetsAutoPageRequest request);

}
