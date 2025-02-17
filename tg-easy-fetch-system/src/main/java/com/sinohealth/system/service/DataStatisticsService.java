package com.sinohealth.system.service;

import com.sinohealth.system.dto.DataStatisticsDTO;
import com.sinohealth.system.dto.TgApplicationInfoDTOV2;
import com.sinohealth.system.dto.TgLoginInfoDTO;
import com.sinohealth.system.dto.TgUserAssetDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zhangyanping
 * @date 2023/11/20 17:27
 */
public interface DataStatisticsService {
    /**
     * 日活统计
     */
    Integer duaCount(Date startTime, Date endTime);

    /**
     * 部门统计
     */
    Integer depCount(Date startTime, Date endTime);

    /**
     * 申请统计
     */
    Integer dailyApplicationCount(Date startTime, Date endTime);

    /**
     * 申请通过统计
     */
    Integer dailyPassedApplicationCount(Date startTime, Date endTime);

    /**
     * 需求类型统计
     */
    Map<String, Integer> countByRequireAttr(Date startTime, Date endTime, Boolean isPassed, Integer assetMenuId);


    Map<String, Integer> countByRequireTimeType(Date startTime, Date endTime, Boolean isPassed, Integer assetMenuId);

    /**
     * 日/月 日活统计
     */
    List<DataStatisticsDTO> countByDayOrMonth(String dateFormat, Date startTime, Date endTime);

    /**
     * 按日/月部门日活统计
     */
    List<DataStatisticsDTO> countDepByDayOrMonth(String dateFormat, Date startTime, Date endTime);

    /**
     * 每日部门日活&用户日活统计图
     */
    Map<String, List<DataStatisticsDTO>> countDuaByConditions(String countByDayOrMonth, Date startTime, Date endTime);

    /**
     * 资产类型统计图
     */
    Map<String, List<DataStatisticsDTO>> countByAssetType(String countByDayOrMonth, Date startTime, Date endTime, Integer assetMenuId);

    /**
     * 部门资产统计图
     */
    Map<String, List<DataStatisticsDTO>> countDepAssetByConditions(String countByDayOrMonth, Date startTime, Date endTime, Integer assetMenuId);

    /**
     * 查询日志记录
     */
    List<TgLoginInfoDTO> queryLoginInfoByPage(Date startTime, Date endTime, String searchKey, Integer pageSize);

    List<TgApplicationInfoDTOV2> queryApplicationInfoByPage(Date startTime, Date endTime, String searchKey, String assetName, String assetType, Integer assetMenuId, Integer pageSize, String assetMenuName);

    /**
     * 出数趋势统计
     */
    List<DataStatisticsDTO> countUserDataAsset(String dateFormat, Date startTime, Date endTime, Integer assetMenuId);


    List<TgUserAssetDTO> queryUserAssetInfoByPage(Date startTime, Date endTime, String searchKey, String assetName, String serviceType, Integer assetMenuId, Integer pageSize, String assetMenuName);


    /**
     * 按目录统计需求申请数据出数数据
     * countType 1 申请统计  2出数统计
     */
    Map<String, List<DataStatisticsDTO>> countByCatalogId(String countByDayOrMonth, Date startTime, Date endTime, Integer assetMenuId, Integer countType);

    /**
     * 统计部门下每个目录的需求申请数据出数数据
     * countType 1 申请统计  2出数统计
     */
    Map<String, Map<String, Integer>> countDepApplicationByCatalogId(Date startTime, Date endTime, Integer assetMenuId, Integer countType);

}
