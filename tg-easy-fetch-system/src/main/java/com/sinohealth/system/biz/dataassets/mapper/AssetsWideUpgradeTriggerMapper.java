package com.sinohealth.system.biz.dataassets.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.dataassets.domain.AssetsWideUpgradeTrigger;
import com.sinohealth.system.biz.dataassets.service.impl.UserDataAssetsServiceImpl;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-23 17:30
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface AssetsWideUpgradeTriggerMapper extends BaseMapper<AssetsWideUpgradeTrigger> {

    /**
     * 触发记录中 没有大于 工作流执行完成时间 的数据存在，才会被创建新的触发任务
     *
     * @see UserDataAssetsServiceImpl#createNewAssets 注意新创建出来的资产 finish_time 字段没有值，因此可以被过滤掉
     */
    @Select("select table_id from tg_assets_wide_upgrade_trigger where table_id = #{tableId} " +
            " group by table_id having max(finish_time) > #{endTime}")
    Long queryNeedTableIds(@Param("tableId") Long tableId, @Param("endTime") String endTime);
}
