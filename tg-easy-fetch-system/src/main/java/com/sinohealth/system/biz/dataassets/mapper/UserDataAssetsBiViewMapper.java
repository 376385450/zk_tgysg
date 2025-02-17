package com.sinohealth.system.biz.dataassets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsBiView;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-25 16:35
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface UserDataAssetsBiViewMapper extends BaseMapper<UserDataAssetsBiView> {

    List<UserDataAssetsBiView> queryNeedDeleteView();
}
