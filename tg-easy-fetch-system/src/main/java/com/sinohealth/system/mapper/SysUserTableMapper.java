package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.SysUserTable;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author dataplatform
 * @date 2021-04-21
 */
public interface SysUserTableMapper extends BaseMapper<SysUserTable> {

    @Select("select t.table_id,COUNT(t.table_id)  accessType ,t.concern from sys_user_table t GROUP BY t.table_id,t.concern")
    public List<SysUserTable> groupByUserTable();
    @Select("select count(*) from sys_user_table t where t.user_id=#{userId}")
    public Long  getCountTableByUserId(@Param("userId") Long userId);
}
