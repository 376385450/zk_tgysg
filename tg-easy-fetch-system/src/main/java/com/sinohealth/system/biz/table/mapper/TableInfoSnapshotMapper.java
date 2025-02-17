package com.sinohealth.system.biz.table.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.common.annotation.DataSource;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-04-18 14:20
 */
@Repository
@DataSource(DataSourceType.MASTER)
public interface TableInfoSnapshotMapper extends BaseMapper<TableInfoSnapshot> {

    List<TableInfoSnapshot> queryByVersion(@Param("versionList") Set<String> versionList);
}
