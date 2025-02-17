package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.dto.*;
import com.sinohealth.system.dto.analysis.DatasourceResponseDTO;
import com.sinohealth.system.dto.analysis.ListDatasourceResponseDTO;
import com.sinohealth.system.dto.table_manage.DataManageFormDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Set;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author jingjun
 * @date 2021-04-20
 */
public interface TableInfoMapper extends BaseMapper<TableInfo> {

//    @Select({"<script>", "select t.* from table_info t where t.dir_id =#{dirId} and t.status=1 ",
//            "<if test=\"tableName!=null and tableName!='' \"> and (t.table_name like CONCAT('%',#{tableName},'%') or t.table_alias like CONCAT('%',#{tableName},'%') ) </if>",
//            "<if test=\"fieldName!=null and fieldName!='' \"> and t.id in (select f.table_id from table_field_info f where f.dir_id=#{dirId} and f.field_name like CONCAT('%',#{fieldName},'%') )  </if>",
//            "<if test=\"tableIds!=null \"> and t.id in ",
//            "<foreach  item='item' index='index' collection='tableIds' open='(' separator=',' close=')'> #{item}  </foreach> ",
//            "</if> ORDER BY id DESC</script>"})
    IPage<TableInfo> getList(IPage<TableInfo> iPage, @Param("dirId") Long dirId, @Param("tableName") String tableName,
                            @Param("fieldName") String fieldName, @Param("tableIds") List<Long> tableIds);

    @Select({"<script>", "select t.id, t.table_name,t.table_alias,t.safe_level,t.dir_id,t.comment from table_info t where 1=1 and t.status = 1 ",
            "<if test=\"dirId!=null  \"> and t.dir_id=#{dirId} </if>",
            "<if test=\"tableName!=null and tableName!='' \"> and (t.table_name like CONCAT('%',#{tableName},'%') or t.table_alias like CONCAT('%',#{tableName},'%') ) </if>",
            "<if test=\"fieldName!=null and fieldName!='' \"> and t.id in (select f.table_id from table_field_info f where f.dir_id=#{dirId} and f.field_name like CONCAT('%',#{fieldName},'%') )  </if>",
            "<if test=\"tableId!=null  \"> and  t.id=#{tableId} </if>",
            "<if test=\"ids!=null \"> and t.id in ",
            "<foreach  item='item' index='index' collection='ids' open='(' separator=',' close=')'> #{item}  </foreach> ",
            "</if></script>"})
    public List<TableMapDto> getTableMap(@Param("dirId") Long dirId, @Param("tableId") Long tableId, @Param("ids") List<Long> ids, @Param("tableName") String tableName, @Param("fieldName") String fieldName);


    @Select({"<script>", "select u.access_type,u.concern,t.* from sys_user_table u,table_info t where  u.user_id =#{userId} and  t.id=u.table_id  ",
            "<if test=\"dirId!=null \"> and u.dir_id =#{dirId} </if>",
            "<if test=\"tableName!=null and tableName!='' \"> and (t.table_name like CONCAT('%',#{tableName},'%') or t.table_alias like CONCAT('%',#{tableName},'%') ) </if>",
            "<if test=\"dirIdSet!=null and dirIdSet.size() > 0 \"> and  t.dir_id in " ,
            "<foreach  item='item' index='index' collection='dirIdSet' open='(' separator=',' close=')'> #{item}  </foreach> ",
            "</if>",
            "<if test=\"extDirIdSet!=null and extDirIdSet.size() > 0 \"> and t.dir_id not in " ,
            "<foreach  item='item' index='index' collection='extDirIdSet' open='(' separator=',' close=')'> #{item}  </foreach> ",
            "</if>",
            "<if test=\"saveLevel!=null \"> and t.safe_level =#{saveLevel}   </if>",
            "<if test=\"concern!=null \"> and u.concern =#{concern}   </if>",
            "</script>"})
    public List<TableInfo> getMyTableList(@Param("userId") Long userId, @Param("dirId") Long dirId, @Param("tableName") String tableName, @Param("saveLevel") Integer saveLevel, @Param("concern") Boolean concern, @Param("dirIdSet") Set<Long> dirIdSet, @Param("extDirIdSet") Set<Long> extDirIdSet);


    @Update({"<script>", "update table_info t set t.query_times=t.query_times+#{times} ",
            "<if test=\"totalTimes gt 0 \"> ,t.total_query_times=t.total_query_times+#{totalTimes}   </if>",
            " where t.id = #{tableId}",
            "</script>"})
    public void updateQueryTime(@Param("tableId")Long tableId,@Param("times") int times, @Param("totalTimes") int totalTimes);

    @Select({"<script>", "select t.id tableId,t.table_name,t.table_alias,t.data_length store from table_info t ",
            "<if test=\"dirids!=null \"> where  t.dir_id in " ,
            "<foreach  item='item' index='index' collection='dirids' open='(' separator=',' close=')'> #{item}  </foreach> ",
            "</if>",
            " order by t.data_length desc limit 20",
            "</script>"})
    public List<BigTableDto> getBigTableTop20(@Param("dirids")List<Long> dirids);

    @Select({"<script>", "select count(t.id) totalTable,sum(t.data_length) totalStore from table_info t",
            "<if test=\"dirids!=null \"> where  t.dir_id in " ,
            "<foreach  item='item' index='index' collection='dirids' open='(' separator=',' close=')'> #{item}  </foreach> ",
            "</if>",
            "</script>"})
    public TableStatisticDto getTableStatisticDto(@Param("dirids")List<Long> dirids );

    List<Long> getTableDirIdBySql(String sql);

    List<TableInfoAccessDTO> getTableInfoAccess(@Param("sql") String sql, @Param("userId") Long userId);

    List<TableFieldDTO> getTableField(@Param("sql") String sql);

    IPage<ListDatasourceResponseDTO> getTableInfoByUserAccess(Page<ListDatasourceResponseDTO> page,
                                                              @Param("userId") Long userId,
                                                              @Param("dirIdSet") Set<Long> dirIdSet,
                                                              @Param("tableName") String tableName);

    List<ListDatasourceResponseDTO> getTableInfoByUserAccessAndId(@Param("userId") Long userId,
                                                                  @Param("idSet") Set<Long> idSet);

    IPage<DatasourceResponseDTO> listTableInfoByUserAccess(Page<DatasourceResponseDTO> page,
                                                           @Param("userId") Long userId,
                                                           @Param("dirIdSet") Set<Long> dirIdSet,
                                                           @Param("id") Long id,
                                                           @Param("content") String content);

    List<DataManageFormDto> getListByDirId(@Param("dirId")Long dirId,  @Param("menu")Integer menu,@Param("canViewAbnormal") Integer canView);

    List<DataManageFormDto> getListByDirId1(@Param("dirId")Long dirId, @Param("menu")Integer menu, @Param("username") String username);

    void updateDirIdOfTableInfo(TableInfoDto tableInfoDto);

    TableInfo selectTableInfoByTableName(String tn);

    List<DataManageFormDto> getTablesWithoutDir(@Param("menu")Integer menu);
}
