package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.dto.TgSyncTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author Rudolph
 * @Date 2022-07-29 9:40
 * @Desc
 */
@Mapper
public interface TgSyncTaskMapper extends BaseMapper<TgSyncTask> {

    TgSyncTask selectTaskNeed2Sync();
}
