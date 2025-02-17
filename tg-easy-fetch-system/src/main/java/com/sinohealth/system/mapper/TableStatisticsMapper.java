package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.TableStatistics;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author dataplatform
 * @date 2021-05-07
 */
public interface TableStatisticsMapper extends BaseMapper<TableStatistics> {

    @Select(" select t.id table_id,t.dir_id,t.query_times,t.total_query_times from table_info t")
    public List<TableStatistics> ininTableStatistics();
}
