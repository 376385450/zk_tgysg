package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgAssetWhitelistInfo;
import com.sinohealth.system.dto.DataStatisticsDTO;
import com.sinohealth.system.dto.TgUserAssetDTO;
import com.sinohealth.system.dto.assets.*;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
@Repository
public interface TgAssetInfoMapper extends BaseMapper<TgAssetInfo> {

    @Select({
            "<script>",
            "select * from tg_asset_info where shelf_state = '已上架' and ",
            "<foreach collection='infos' item='item' open='(' separator = 'or' close=')'>",
            "id = #{item.assetId} ",
            "</foreach>",
            "</script>"
    })
    List<TgAssetInfo> findByTgAssetAuthWhiltlistInfo(@Param("infos") List<TgAssetWhitelistInfo> infos);


    @Select({
            "<script>",
            "select t.* from tg_asset_info t ",
            "where t.deleted = 0 and t.shelf_state = '已上架' ",
            " and t.is_follow_asset_menu_readable_range = 'FOLLOW_DIR_AUTH'",
            " and t.asset_menu_id IN ",
            "<foreach collection='ids' item='item' open='(' separator=',' close=')'>",
            "#{item} ",
            "</foreach>",
            "union all ",
            "select t1.*",
            "from tg_asset_info t1",
            "         left join tg_asset_whitelist_info t2 on t1.type = t2.type and t1.related_id = t2.related_id",
            "where t1.deleted = 0",
            "  and t1.shelf_state = '已上架'",
            " and t1.is_follow_asset_menu_readable_range = 'CUSTOM_AUTH'",
            "  and ((t2.staff_type = 'DEPT' and t2.staff_id = #{deptId}) ",
            "    or (t2.staff_type = 'USER' and t2.staff_id = #{userId}))",
            "  and t2.service_type = '资产阅读权限'",
            "  and t1.asset_menu_id IN",
            "<foreach collection='allIds' item='item' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"
    })
    List<TgAssetInfo> findAllByAssetMenuId(@Param("ids") List<Integer> menuIds,
                                           @Param("allIds") List<Integer> allIds,
                                           @Param("userId") Long userId,
                                           @Param("deptId") String deptId);

    int updateBatch(@Param("list") List<TgAssetInfo> list);

    Page<TgAssetFrontTreeQueryResult> frontTreeQuery(Page<TgAssetFrontTreeQueryResult> page,
                                                     @Param("menuIds") List<Integer> menuIds,
                                                     @Param("deptId") String deptId,
                                                     @Param("userId") String userId,
                                                     @Param("queryParams") AssetFrontendPageQuery queryParams,
                                                     @Param("assetIds") List<Integer> assetIds);

    Long selectMaxSort();

    Page<TgAssetMyApplicationPageResult> myApplicaionQuery(Page<TgAssetMyApplicationPageResult> page,
                                                           @Param("queryParams") AssetApplicationPageQuery queryParam, @Param("userId") Long userId);

    @MapKey("type")
    Map<String, Map<String, Object>> myApplicaionCount(@Param("userId") Long userId);

    Page<TgAssetInfo> backendQuery(Page<TgAssetInfo> page,
                                   @Param("manageableAssetMenuIds") List<Integer> manageableAssetMenuIds,
                                   @Param("queryParams") AssetBackendPageQuery queryParam);

    Integer computeCurrentUserApplyCount(@Param("assetId") Long assetId, @Param("userId") Long userId);


    List<DataStatisticsDTO> countByAssetType(@Param("dateFormat") String dateFormat,
                                             @Param("startTime") Date startTime,
                                             @Param("endTime") Date endTime,
                                             @Param("type") String type,
                                             @Param("assetMenuId") Integer assetMenuId

    );

    /**
     * 查询明细
     */
    List<TgUserAssetDTO> queryUserAssetInfoByPage(@Param("startTime") Date startTime,
                                                  @Param("endTime") Date endTime,
                                                  @Param("searchKey") String searchKey,
                                                  @Param("assetName") String assetName,
                                                  @Param("serviceType") String serviceType,
                                                  @Param("assetMenuId") Integer assetMenuId,
                                                  @Param("userIds") Set<Long> userIds,
                                                  @Param("assetMenuName")String assetMenuName
    );

}
