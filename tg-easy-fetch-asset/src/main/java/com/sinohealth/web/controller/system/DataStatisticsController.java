package com.sinohealth.web.controller.system;

import com.github.pagehelper.PageInfo;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.page.TableSupport;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.utils.poi.ExcelUtil;
import com.sinohealth.system.dto.*;
import com.sinohealth.system.service.DataStatisticsService;
import com.sinohealth.system.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author zhangyanping
 * @date 2023/11/20 17:19
 */
@Api(tags = {"运营数据统计"})
@RestController
@RequestMapping("/api/system/statistics")
public class DataStatisticsController extends BaseController {

    @Resource
    private DataStatisticsService dataStatisticsService;


    @GetMapping("/duaCount")
    @ApiOperation("日活统计")
    @Cacheable(value = RedisKeys.Cache.STATISTICS)
    public AjaxResult<Map<String, Object>> duaCount() {
        Map<String, Object> result = new HashMap<>(4);

        //今日数据
        Integer count1 = dataStatisticsService.duaCount(DateUtil.getToday().getFirst(), DateUtil.getToday().getSecond());
        Integer count2 = dataStatisticsService.depCount(DateUtil.getToday().getFirst(), DateUtil.getToday().getSecond());
        Map<String, Integer> todayDataMap = new HashMap<>(4);
        todayDataMap.put("duaCount", count1);
        todayDataMap.put("depCount", count2);

        //昨日数据
        Integer count3 = dataStatisticsService.duaCount(DateUtil.getYesterday().getFirst(), DateUtil.getYesterday().getSecond());
        Integer count4 = dataStatisticsService.depCount(DateUtil.getYesterday().getFirst(), DateUtil.getYesterday().getSecond());
        Map<String, Integer> yesterdayDataMap = new HashMap<>(4);
        yesterdayDataMap.put("duaCount", count3);
        yesterdayDataMap.put("depCount", count4);

        result.put("today", todayDataMap);
        result.put("yesterday", yesterdayDataMap);
        result.put("duaQOQ", calculationFormula(count1, count3));
        result.put("depQOQ", calculationFormula(count2, count4));
        return AjaxResult.success(result);
    }


    @GetMapping("/duaTotalCount")
    @ApiOperation("累计日活统计")
    @Cacheable(value = RedisKeys.Cache.STATISTICS)
    public AjaxResult<Map<String, Integer>> totalCount() {
        Map<String, Integer> result = new HashMap<>(4);

        //用户活跃统计
        Integer count1 = dataStatisticsService.duaCount(null, null);

        //部门活跃统计
        Integer count2 = dataStatisticsService.depCount(null, null);

        result.put("duaCount", count1);
        result.put("depCount", count2);
        return AjaxResult.success(result);
    }


    @GetMapping("/dailyApplicationCount")
    @ApiOperation("今日申请统计")
    @Cacheable(value = RedisKeys.Cache.STATISTICS)
    public AjaxResult<Map<String, Object>> dailyApplicationCount() {
        Map<String, Object> result = new HashMap<>(4);

        //今日数据
        Integer count1 = dataStatisticsService.dailyApplicationCount(DateUtil.getToday().getFirst(), DateUtil.getToday().getSecond());
        Integer count2 = dataStatisticsService.dailyPassedApplicationCount(DateUtil.getToday().getFirst(), DateUtil.getToday().getSecond());
        Map<String, Integer> todayDataMap = new HashMap<>(4);
        todayDataMap.put("applicationCount", count1);
        todayDataMap.put("passedCount", count2);

        //昨日数据
        Integer count3 = dataStatisticsService.dailyApplicationCount(DateUtil.getYesterday().getFirst(), DateUtil.getYesterday().getSecond());
        Integer count4 = dataStatisticsService.dailyPassedApplicationCount(DateUtil.getYesterday().getFirst(), DateUtil.getYesterday().getSecond());
        Map<String, Integer> yesterdayDataMap = new HashMap<>(4);
        yesterdayDataMap.put("applicationCountCount", count3);
        yesterdayDataMap.put("passedCount", count4);


        result.put("today", todayDataMap);
        result.put("yesterday", yesterdayDataMap);
        result.put("applicationQOQ", calculationFormula(count1, count3));
        result.put("passedQOQ", calculationFormula(count2, count4));
        return AjaxResult.success(result);
    }

    @GetMapping("/applicationTotalCount")
    @ApiOperation("累计申请统计")
    @Cacheable(value = RedisKeys.Cache.STATISTICS)
    public AjaxResult<Map<String, Integer>> applicationTotalCount() {
        Map<String, Integer> result = new HashMap<>(4);

        //申请的
        Integer count1 = dataStatisticsService.dailyApplicationCount(null, null);

        //通过的
        Integer count2 = dataStatisticsService.dailyPassedApplicationCount(null, null);

        result.put("applicationCountCount", count1);
        result.put("passedCount", count2);
        return AjaxResult.success(result);
    }


    @GetMapping("/dailyRequireAttrCount")
    @ApiOperation("今日需求类型统计")
    @Cacheable(value = RedisKeys.Cache.STATISTICS)
    public AjaxResult<Map<String, Integer>> countByRequireAttr() {
        return AjaxResult.success(dataStatisticsService.countByRequireAttr(DateUtil.getToday().getFirst(), DateUtil.getToday().getSecond(), true, null));
    }

    @GetMapping("/requireAttrTotalCount")
    @ApiOperation("累计需求类型统计")
    @Cacheable(value = RedisKeys.Cache.STATISTICS)
    public AjaxResult<Map<String, Integer>> requireAttrTotalCount() {
        return AjaxResult.success(dataStatisticsService.countByRequireAttr(null, null, true, null));
    }


    @GetMapping("/countDuaByConditions")
    @ApiOperation("活跃趋势")
    @Cacheable(value = RedisKeys.Cache.STATISTICS)
    public AjaxResult<Map<String, List<DataStatisticsDTO>>> countDuaByConditions(@RequestParam(value = "countByDayOrMonth", defaultValue = "byDay") String countByDayOrMonth,
                                                                                 @RequestParam(value = "startTime", required = false) String startTime,
                                                                                 @RequestParam(value = "endTime", required = false) String endTime) {

        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, countByDayOrMonth);
        return AjaxResult.success(dataStatisticsService.countDuaByConditions(countByDayOrMonth, pair.getFirst(), pair.getSecond()));
    }

    @GetMapping("/countApplyByConditions")
    @ApiOperation("申请趋势")
    public AjaxResult<Map<String, List<DataStatisticsDTO>>> countByAssetType(@RequestParam(value = "assetMenuId", defaultValue = "0") Integer assetMenuId,
                                                                             @RequestParam(value = "countByDayOrMonth", defaultValue = "byDay") String countByDayOrMonth,
                                                                             @RequestParam(value = "startTime", required = false) String startTime,
                                                                             @RequestParam(value = "endTime", required = false) String endTime) {
        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, countByDayOrMonth);
        return AjaxResult.success(dataStatisticsService.countByAssetType(countByDayOrMonth, pair.getFirst(), pair.getSecond(), assetMenuId));
    }


    @GetMapping("/countApplyByConditionsV2")
    @ApiOperation("需求趋势")
    public AjaxResult<Map<String, List<DataStatisticsDTO>>> countByMenuId(@RequestParam(value = "assetMenuId", defaultValue = "0") Integer assetMenuId,
                                                                          @RequestParam(value = "countByDayOrMonth", defaultValue = "byDay") String countByDayOrMonth,
                                                                          @RequestParam(value = "startTime", required = false) String startTime,
                                                                          @RequestParam(value = "endTime", required = false) String endTime,
                                                                          @RequestParam(value = "countType") Integer countType
    ) {
        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, countByDayOrMonth);
        return AjaxResult.success(dataStatisticsService.countByCatalogId(countByDayOrMonth, pair.getFirst(), pair.getSecond(), assetMenuId, countType));
    }


    @GetMapping("/countDepApplyByConditionsV2")
    @ApiOperation("部门需求分布")
    public AjaxResult<Map<String, Map<String, Integer>>> countDepApplyByConditionsV2(@RequestParam(value = "assetMenuId", defaultValue = "0") Integer assetMenuId,
                                                                                     @RequestParam(value = "countByDayOrMonth", defaultValue = "byDay") String countByDayOrMonth,
                                                                                     @RequestParam(value = "startTime", required = false) String startTime,
                                                                                     @RequestParam(value = "endTime", required = false) String endTime,
                                                                                     @RequestParam(value = "countType") Integer countType
    ) {
        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, countByDayOrMonth);
        return AjaxResult.success(dataStatisticsService.countDepApplicationByCatalogId(pair.getFirst(), pair.getSecond(), assetMenuId, countType));
    }


    @GetMapping("/countDepApplyByConditions")
    @ApiOperation("部门申请趋势")
    public AjaxResult<Map<String, List<DataStatisticsDTO>>> countDepAssetByConditions(@RequestParam(value = "assetMenuId", defaultValue = "0") Integer assetMenuId,
                                                                                      @RequestParam(value = "countByDayOrMonth", defaultValue = "byDay") String countByDayOrMonth,
                                                                                      @RequestParam(value = "startTime", required = false) String startTime,
                                                                                      @RequestParam(value = "endTime", required = false) String endTime) {
        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, countByDayOrMonth);
        return AjaxResult.success(dataStatisticsService.countDepAssetByConditions(countByDayOrMonth, pair.getFirst(), pair.getSecond(), assetMenuId));
    }


    @GetMapping("/countTemplateByConditions")
    @ApiOperation("模版统计申请趋势")
    public AjaxResult<Map<String, Object>> countTemplateByConditions(@RequestParam(value = "assetMenuId", defaultValue = "0") Integer assetMenuId,
                                                                     @RequestParam(value = "countByDayOrMonth", defaultValue = "byDay") String countByDayOrMonth,
                                                                     @RequestParam(value = "startTime", required = false) String startTime,
                                                                     @RequestParam(value = "endTime", required = false) String endTime) {
        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, countByDayOrMonth);

        //展示申请通过 需求性质
        Map<String, Integer> map1 = dataStatisticsService.countByRequireAttr(pair.getFirst(), pair.getSecond(), true, assetMenuId);
        Map<String, Integer> map2 = dataStatisticsService.countByRequireTimeType(pair.getFirst(), pair.getSecond(), true, assetMenuId);
        List<DataStatisticsDTO> list = dataStatisticsService.countUserDataAsset(countByDayOrMonth, pair.getFirst(), pair.getSecond(), assetMenuId);

        Map<String, Object> result = new HashMap<>(4);
        result.put("需求性质", map1);
        result.put("需求类型", map2);
        result.put("出数趋势统计", list);
        return AjaxResult.success(result);
    }


    @GetMapping("/loginInfo/page")
    @ApiOperation("活跃明细-分页")
    public AjaxResult<PageInfo<TgLoginInfoDTO>> queryLoginInfoByPage(@RequestParam(value = "nameSearch", required = false) String nameSearch,
                                                                     @RequestParam(value = "startTime", required = false) String startTime,
                                                                     @RequestParam(value = "endTime", required = false) String endTime,
                                                                     @RequestParam(value = "pageNum") Integer pageNum,
                                                                     @RequestParam(value = "pageSize") Integer pageSize) {

        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, null);
        List<TgLoginInfoDTO> list = dataStatisticsService.queryLoginInfoByPage(pair.getFirst(), pair.getSecond(), nameSearch, pageSize);
        return AjaxResult.success(new PageInfo<>(list));
    }


    @GetMapping("/loginInfo/export")
    @ApiOperation("活跃明细-导出")
    public void exportLoginInfo(@RequestParam(value = "nameSearch", required = false) String nameSearch,
                                @RequestParam(value = "startTime", required = false) String startTime,
                                @RequestParam(value = "endTime", required = false) String endTime,
                                @RequestParam(value = "pageNum") Integer pageNum,
                                @RequestParam(value = "pageSize") Integer pageSize,
                                HttpServletResponse response) {
        TableSupport.setPageSize(1, 100000);
        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, null);
        List<TgLoginInfoDTO> list = dataStatisticsService.queryLoginInfoByPage(pair.getFirst(), pair.getSecond(), nameSearch, 100000);
        ExcelUtil<TgLoginInfoDTO> util = new ExcelUtil<>(TgLoginInfoDTO.class);
        try {
            setHeader(response, "活跃明细");
            util.exportExcelAndDownload(list, "活跃明细", response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/applicationInfo/page")
    @ApiOperation("申请明细-分页")
    public AjaxResult<PageInfo<TgApplicationInfoDTOV2>> queryApplicationInfoByPage(@RequestParam(value = "nameSearch", required = false) String nameSearch,
                                                                                   @RequestParam(value = "startTime", required = false) String startTime,
                                                                                   @RequestParam(value = "endTime", required = false) String endTime,
                                                                                   @RequestParam(value = "assetName", required = false) String assetName,
                                                                                   @RequestParam(value = "assetType", required = false) String assetType,
                                                                                   @RequestParam(value = "pageNum") Integer pageNum,
                                                                                   @RequestParam(value = "pageSize") Integer pageSize,
                                                                                   @RequestParam(value = "assetMenuId") Integer assetMenuId,
                                                                                   @RequestParam(value = "assetMenuName", required = false) String assetMenuName
    ) {
        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, null);
        List<TgApplicationInfoDTOV2> list = dataStatisticsService.queryApplicationInfoByPage(pair.getFirst(), pair.getSecond(), nameSearch, assetName, assetType, assetMenuId, pageSize, assetMenuName);
        return AjaxResult.success(new PageInfo<>(list));
    }


    @GetMapping("/applicationInfo/export")
    @ApiOperation("申请明细-导出")
    public void exportApplicationInfo(@RequestParam(value = "nameSearch", required = false) String nameSearch,
                                      @RequestParam(value = "startTime", required = false) String startTime,
                                      @RequestParam(value = "endTime", required = false) String endTime,
                                      @RequestParam(value = "assetName", required = false) String assetName,
                                      @RequestParam(value = "assetType", required = false) String assetType,
                                      @RequestParam(value = "pageNum") Integer pageNum,
                                      @RequestParam(value = "pageSize") Integer pageSize,
                                      @RequestParam(value = "assetMenuId") Integer assetMenuId,
                                      @RequestParam(value = "assetMenuName", required = false) String assetMenuName,
                                      HttpServletResponse response) {
        TableSupport.setPageSize(1, 100000);
        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, null);
        List<TgApplicationInfoDTOV2> list = dataStatisticsService.queryApplicationInfoByPage(pair.getFirst(), pair.getSecond(), nameSearch, assetName, assetType, assetMenuId, 100000, assetMenuName);
        ExcelUtil<TgApplicationInfoDTOV2> util = new ExcelUtil<>(TgApplicationInfoDTOV2.class);
        try {
            setHeader(response, "申请明细");
            util.exportExcelAndDownload(list, "申请明细", response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/applicationInfoByModel/page")
    @ApiOperation("需求趋势-申请统计-分页")
    public AjaxResult<PageInfo<TgApplicationInfoDTOV2>> queryApplicationInfoByPageV2(@RequestParam(value = "nameSearch", required = false) String nameSearch,
                                                                                     @RequestParam(value = "startTime", required = false) String startTime,
                                                                                     @RequestParam(value = "endTime", required = false) String endTime,
                                                                                     @RequestParam(value = "assetName", required = false) String assetName,
                                                                                     @RequestParam(value = "assetType", required = false) String assetType,
                                                                                     @RequestParam(value = "pageNum") Integer pageNum,
                                                                                     @RequestParam(value = "pageSize") Integer pageSize,
                                                                                     @RequestParam(value = "assetMenuId") Integer assetMenuId,
                                                                                     @RequestParam(value = "assetMenuName", required = false) String assetMenuName
    ) {
        assetType = AssetType.MODEL.getType();
        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, null);
        List<TgApplicationInfoDTOV2> list = dataStatisticsService.queryApplicationInfoByPage(pair.getFirst(), pair.getSecond(), nameSearch, assetName, assetType, assetMenuId, pageSize, assetMenuName);

        PageInfo pageInfo = new PageInfo<>(list);
        List<TgApplicationInfoDTOV3> result = new ArrayList<>();
        for (Object o : pageInfo.getList()) {
            TgApplicationInfoDTOV3 dto = new TgApplicationInfoDTOV3();
            BeanUtils.copyProperties(o, dto);
            result.add(dto);
        }
        pageInfo.setList(result);
        return AjaxResult.success(pageInfo);
    }


    @GetMapping("/applicationInfoByModel/export")
    @ApiOperation("需求趋势-申请统计-导出")
    public void queryApplicationInfoByPageV2(@RequestParam(value = "nameSearch", required = false) String nameSearch,
                                             @RequestParam(value = "startTime", required = false) String startTime,
                                             @RequestParam(value = "endTime", required = false) String endTime,
                                             @RequestParam(value = "assetName", required = false) String assetName,
                                             @RequestParam(value = "assetType", required = false) String assetType,
                                             @RequestParam(value = "pageNum") Integer pageNum,
                                             @RequestParam(value = "pageSize") Integer pageSize,
                                             @RequestParam(value = "assetMenuId") Integer assetMenuId,
                                             @RequestParam(value = "assetMenuName", required = false) String assetMenuName,
                                             HttpServletResponse response) {
        assetType = AssetType.MODEL.getType();
        TableSupport.setPageSize(1, 100000);
        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, null);
        List<TgApplicationInfoDTOV2> list = dataStatisticsService.queryApplicationInfoByPage(pair.getFirst(), pair.getSecond(), nameSearch, assetName, assetType, assetMenuId, 100000, assetMenuName);
        List<TgApplicationInfoDTOV3> result = new ArrayList<>();
        for (Object o : list) {
            TgApplicationInfoDTOV3 dto = new TgApplicationInfoDTOV3();
            BeanUtils.copyProperties(o, dto);
            result.add(dto);
        }

        ExcelUtil<TgApplicationInfoDTOV3> util = new ExcelUtil<>(TgApplicationInfoDTOV3.class);
        try {
            setHeader(response, "申请明细");
            util.exportExcelAndDownload(result, "申请明细", response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/userDataAsset/page")
    @ApiOperation("出数趋势-分页")
    public AjaxResult<PageInfo<TgUserAssetDTO>> queryUserAssetInfoByPage(@RequestParam(value = "nameSearch", required = false) String nameSearch,
                                                                         @RequestParam(value = "startTime", required = false) Date startTime,
                                                                         @RequestParam(value = "endTime", required = false) Date endTime,
                                                                         @RequestParam(value = "assetName", required = false) String assetName,
                                                                         @RequestParam(value = "serviceType", required = false) String serviceType,
                                                                         @RequestParam(value = "pageNum") Integer pageNum,
                                                                         @RequestParam(value = "pageSize") Integer pageSize,
                                                                         @RequestParam(value = "assetMenuId") Integer assetMenuId,
                                                                         @RequestParam(value = "assetMenuName", required = false) String assetMenuName
    ) {

        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, null);
        List<TgUserAssetDTO> list = dataStatisticsService.queryUserAssetInfoByPage(pair.getFirst(), pair.getSecond(), nameSearch, assetName, serviceType, assetMenuId, pageSize, assetMenuName);
        return AjaxResult.success(new PageInfo<>(list));
    }


    @GetMapping("/userDataAsset/export")
    @ApiOperation("出数趋势-导出")
    public void exportUserAssetInfo(@RequestParam(value = "nameSearch", required = false) String nameSearch,
                                    @RequestParam(value = "startTime", required = false) Date startTime,
                                    @RequestParam(value = "endTime", required = false) Date endTime,
                                    @RequestParam(value = "assetName", required = false) String assetName,
                                    @RequestParam(value = "serviceType", required = false) String serviceType,
                                    @RequestParam(value = "pageNum") Integer pageNum,
                                    @RequestParam(value = "pageSize") Integer pageSize,
                                    @RequestParam(value = "assetMenuId") Integer assetMenuId,
                                    @RequestParam(value = "assetMenuName", required = false) String assetMenuName,
                                    HttpServletResponse response) {
        TableSupport.setPageSize(1, 100000);
        Pair<Date, Date> pair = DateUtil.parseDateStr(startTime, endTime, null);
        List<TgUserAssetDTO> list = dataStatisticsService.queryUserAssetInfoByPage(pair.getFirst(), pair.getSecond(), nameSearch, assetName, serviceType, assetMenuId, 100000, assetMenuName);
        ExcelUtil<TgUserAssetDTO> util = new ExcelUtil<>(TgUserAssetDTO.class);
        try {
            setHeader(response, "出数明细");
            util.exportExcelAndDownload(list, "出数明细", response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setHeader(HttpServletResponse response, String name) {

        try {
            String encode = URLEncoder.encode(name, "utf-8");
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + encode + ".xlsx");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
            response.setHeader("Expires", "0");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 环比计算公式 （（本期-上期）/上期 *100） 保留两位小数
     */
    private String calculationFormula(Integer currentCount, Integer preCount) {
        DecimalFormat df = new DecimalFormat("0.00");
        return preCount == 0 ? null : df.format((double) (currentCount - preCount) / preCount * 100);
    }

}
