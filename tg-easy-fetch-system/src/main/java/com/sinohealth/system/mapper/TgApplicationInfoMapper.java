package com.sinohealth.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.system.biz.application.dto.LatestProjectDto;
import com.sinohealth.system.biz.application.dto.request.UserDataAssetsDistRequest;
import com.sinohealth.system.biz.application.entity.HistoryApplyQuoteEntity;
import com.sinohealth.system.biz.homePage.HotAssetsDTO;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.dto.DataStatisticsDTO;
import com.sinohealth.system.dto.TgApplicationInfoDTOV2;
import com.sinohealth.system.biz.application.dto.TgUserDataAssetsDistDto;
import com.sinohealth.system.dto.application.PageQueryFileApplicationRequest;
import com.sinohealth.system.dto.application.PageQueryModelApplicationRequest;
import com.sinohealth.system.dto.application.PageQueryTableApplicationRequest;
import com.sinohealth.system.dto.application.deliver.request.HistoryQueryRequest;
import com.sinohealth.system.vo.ApplicationManageFileListVo;
import com.sinohealth.system.vo.ApplicationManageModelListVo;
import com.sinohealth.system.vo.ApplicationManageTableListVo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
@Repository
public interface TgApplicationInfoMapper extends BaseMapper<TgApplicationInfo> {
    TgApplicationInfo queryApplicationByApplicantIdAndApplicationId(@Param("applicantId") Long userId, @Param("applicationId") Long applicationId);

    TgApplicationInfo queryApplicationByProjectName(@Param("projectName") String projectName);

    List<TgApplicationInfo> queryUsefulApplicationInfo(@Param("catalogueIds") List<Integer> catalogueIds);

    List<TgApplicationInfo> queryApplicationByApplicantId(@Param("applicantId") Long userId, @Param("applicantType") String applicationType);

    List<Long> queryDistinctTableIdByApplicantId(@Param("applicantId") Long userId, @Param("applicantType") String applicationType);

    List<TgApplicationInfo> queryApplicationByApplicantIdAndName(@Param("applicantId") Long userId,
                                                                 @Param("applicantType") String applicationType,
                                                                 @Param("projectName") String projectName,
                                                                 @Param("auditStatus") Integer auditStatus);

    void updateOutTableName(@Param("outTableName") String outTableName, @Param("id") Long id);

    List<TgUserDataAssetsDistDto> queryApplication(TgUserDataAssetsDistDto tgUserDataAssetsDistDto);

    IPage<TgUserDataAssetsDistDto> queryAssetsDistList(IPage page, @Param("param") UserDataAssetsDistRequest request);

    void updateApplicantId(@Param("userId") Long userId, @Param("newUserId") Long newUserId);

    List<String> querySearchTableSource(@Param("applicantId") Long applicantId);

    Integer updateNeedSyncTagByTableName(String tn);

    IPage<HotAssetsDTO> getApplicantCount(Page page,
                                          @Param("id") Long id,
                                          @Param("createdTime") Date createdTime,
                                          @Param("catalogueIds") List<Integer> catalogueIds,
                                          @Param("source") Integer source);

    List<Map<String, Object>> getTopResources(@Param("parameterMap") Map<String, Object> parameterMap);

    List<TgApplicationInfo> listAllNormalDataApplications(@Param("assetIds") List<Long> assetIds);

    @MapKey("project_id")
    Map<Long, Map<String, Long>> groupByProject(@Param("projectIds") Collection<Long> projectIds);

    List<LatestProjectDto> queryLastProjectName(@Param("userId") Long userId,
                                                @Param("templateId") Long templateId);

    Page<ApplicationManageTableListVo> pageQueryTableApplication(IPage page,
                                                                 @Param("pageRequest") PageQueryTableApplicationRequest pageRequest,
                                                                 @Param("userId") Long userId);

    List<TgApplicationInfo> findByDiffType(@Param("moduleIds") List<Integer> moduleIds, @Param("tebleIds") List<Integer> tableIds, @Param("docIds") List<Integer> docIds);

    Page<ApplicationManageFileListVo> pageQueryFileApplication(Page page,
                                                               @Param("pageRequest") PageQueryFileApplicationRequest pageRequest,
                                                               @Param("userId") Long userId);

    Page<ApplicationManageModelListVo> pageQueryModelApplication(Page page,
                                                                 @Param("pageRequest") PageQueryModelApplicationRequest pageRequest,
                                                                 @Param("userId") Long userId);

    List<TgApplicationInfo> getValidApplicationByAssetIdAndUserId(@Param("assetId") Long assetId, @Param("userId") Long userId,
                                                                  @Param("currentTime") String currentTime);


    List<TgApplicationInfo> getValidApplicationByAssetIdsAndUserId(@Param("assetIds") List<Long> assetId, @Param("userId") Long userId,
                                                                   @Param("currentTime") String currentTime);

    /**
     * 根据需求性质统计申请
     */
    @MapKey("require_attr")
    Map<Integer, Map<String, Integer>> countByRequireAttr(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("isPassed") Boolean isPassed, @Param("assetMenuId") Integer assetMenuId);


    /**
     * 根据需求性质统计申请
     */
    @MapKey("require_time_type")
    Map<String, Map<String, Integer>> countByRequireTimeType(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("isPassed") Boolean isPassed, @Param("assetMenuId") Integer assetMenuId);


    /**
     * 用户维度统计通过的申请
     */
    List<DataStatisticsDTO> countPassedApplicationByConditions(@Param("startTime") Date startTime,
                                                               @Param("endTime") Date endTime,
                                                               @Param("assetMenuId") Integer assetMenuId,
                                                               @Param("groupByType") Boolean groupByType,
                                                               @Param("userIds") Set<String> userIds,
                                                               @Param("assetType") String assetType
    );

    /**
     * 查询明细
     */
    List<TgApplicationInfoDTOV2> queryApplicationInfoByPage(@Param("startTime") Date startTime,
                                                            @Param("endTime") Date endTime,
                                                            @Param("searchKey") String searchKey,
                                                            @Param("assetName") String assetName,
                                                            @Param("assetType") String assetType,
                                                            @Param("assetMenuId") Integer assetMenuId,
                                                            @Param("userIds") Set<Long> userIds,
                                                            @Param("assetMenuName") String assetMenuName
    );

    /**
     * 用户维度统计通过的申请
     */
    List<DataStatisticsDTO> countUserDataAsset(@Param("dateFormat") String dateFormat,
                                               @Param("startTime") Date startTime,
                                               @Param("endTime") Date endTime,
                                               @Param("type") String type,
                                               @Param("assetMenuId") Integer assetMenuId
    );

    /**
     *
     */
    List<DataStatisticsDTO> countUserDataAssetByUserId(@Param("startTime") Date startTime,
                                                       @Param("endTime") Date endTime,
                                                       @Param("type") String type,
                                                       @Param("assetMenuId") Integer assetMenuId,
                                                       @Param("userIds") Set<String> userIds
    );

    IPage<HistoryApplyQuoteEntity> pageHistoryQuote(IPage page, @Param("param") HistoryQueryRequest request);

}
