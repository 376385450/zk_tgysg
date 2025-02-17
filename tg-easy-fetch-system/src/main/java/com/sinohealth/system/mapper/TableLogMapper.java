package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sinohealth.system.domain.TableLog;
import com.sinohealth.system.dto.LogCountDto;
import com.sinohealth.system.dto.QueryTableHistoryDto;
import com.sinohealth.system.dto.UserQueryTableLogDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 变更记录Mapper接口
 *
 * @author jingjun
 * @date 2021-04-20
 */
public interface TableLogMapper extends BaseMapper<TableLog> {

    @Select({"<script>", " select t.create_time,t.log_type,t.operator as userName,t.operator_id as user_id from table_log t left join sys_user u on u.user_id=t.operator_id where t.log_type in (43,44) ",
            "<if test=\"dirId !=null  \"> and t.dir_id =#{dirId} </if>", "</script>"})
    public List<UserQueryTableLogDto> getQueryAndExportTableLog(@Param("dirId") Long dirId);

    @Select({"<script>", " select max(t.id) id, date_format(t.create_time, '%Y-%m-%d') createTime,sum(t.update_count) update_count from table_log t where 1=1 ",
            "<if test=\"dirId !=null  \"> and t.dir_id =#{dirId} </if>",
            "<if test=\"tableId !=null  \"> and t.table_id =#{tableId} </if>",
            " <![CDATA[ and t.log_type > 39 and t.log_type<43 and t.create_time>=#{startTime} and t.create_time<#{endTime}  ]]> ",
            " GROUP BY createTime",
            "</script>"})
    public List<TableLog> getOneTableMap(@Param("dirId") Long dirId, @Param("tableId") Long tableId, @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select({"<script>",
            "select  t.log_type,date_format(t.create_time, '%Y-%m-%d') date ,count( t.log_type) times from table_log t where 1=1",
            " <![CDATA[ and t.create_time>=#{startTime} and t.create_time<#{endTime}  ]]> ",
            "<if test=\"dirId !=null  \"> and t.dir_id =#{dirId} </if>",
            "<if test=\"tableName !=null and tableName!=''   \"> and t.table_id in ( select id from table_info where table_name=#{tableName} ) </if>",
            " GROUP BY date,t.log_type",
            "</script>"})
    public List<LogCountDto> groupByQueryAndExport(@Param("dirId") Long dirId, @Param("tableName") String tableName, @Param("startTime") String startTime, @Param("endTime") String endTime);


    @Select("select l.* from table_log l where l.table_id in (select u.table_id from sys_user_table u where u.concern=1 and u.user_id=#{userId} ) and l.log_type<43 order by id desc limit 20")
    public List<TableLog> getMyConcernTableTop20(@Param("userId") Long userId);

    @Select("select MAX(id) id from table_log t where t.operator_id=#{userId} and log_type=43  GROUP BY table_id order by id desc LIMIT 20")
    public List<Long> getLogIdMyQueryTop20(@Param("userId") Long userId);

    @Select({"<script>", " select l.id,t.table_name,t.update_time,t.table_alias,l.table_id,l.create_time queryTime from table_log l left join table_info t on t.id=l.table_id where l.id in ",
            "<foreach  item='item' index='index' collection='ids' open='(' separator=',' close=')'> #{item}  </foreach> ",
            "</script>"})
    public List<QueryTableHistoryDto> getQueryTableHistory(@Param("ids") List<Long> ids);

    @Select({"<script>", "select sum(update_count) from table_log l where <![CDATA[ l.log_type>39 and l.log_type<43  ]]>  and l.table_id in (SELECT t.table_id from sys_user_table t where t.user_id=#{userId})",
            "<if test=\"startTime!=null\"> <![CDATA[  and l.create_time > #{startTime}  ]]></if>",
            "<if test=\"endTime!=null\"> <![CDATA[ and l.create_time < #{endTime} ]]>  </if>",
            "</script>"})
    public Long getMyTableTotalUpate(@Param("userId") Long userId, @Param("startTime") String startTime, @Param("endTime") String endTime);


    @Select("select l.log_type,DATE_FORMAT(l.create_time,'%Y-%m-%d') date,count(l.log_type) times  from table_log l where l.create_time>#{startTime} and l.log_type<43 and EXISTS (select t.table_id from sys_user_table t where t.user_id=#{userId} and t.table_id=l.table_id ) group by date,l.log_type")
    public List<LogCountDto> getLogStatisticGroupByDay(@Param("userId") Long userId, @Param("startTime") String startTime);
}
