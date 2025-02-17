package com.sinohealth.web.controller.home;

import com.alibaba.fastjson.JSON;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.domain.statistic.PieModel;
import com.sinohealth.system.domain.statistic.VerticalModel;
import com.sinohealth.system.domain.vo.FastEntryVO;
import com.sinohealth.system.dto.FastEntryUpsertRequest;
import com.sinohealth.system.service.FastEntryService;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.ICustomStatisticsService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ITableInfoService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 10:21
 */
@Api("首页")
@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomePageApiController extends BaseController {

    private final ICustomStatisticsService customStaticsService;

    private final IApplicationService applicationService;

    private final ISysUserService userService;

    private final RedisTemplate redisTemplate;

    private final ITableInfoService tableInfoService;
    private final FastEntryService fastEntryService;

    @Autowired
    private AppProperties appProperties;

    @PostMapping("/queryStaticsInfo")
    public AjaxResult queryStaticsInfo(@Valid @RequestBody Map<String, Object> parameterMap) {
        // 业务类型1:resourceLayoutModel
        if (parameterMap.containsKey(CommonConstants.BUSINESS_TYPE) && parameterMap.get(CommonConstants.BUSINESS_TYPE).equals("resourceLayoutModel")) {

            String key = "resourceLayoutModel" + parameterMap.hashCode();
            Object cache = redisTemplate.opsForValue().get(key);
            if (Objects.nonNull(cache)) {
                PieModel val = JSON.parseObject(cache.toString(), PieModel.class);
                return AjaxResult.success(val);
            }

            PieModel model = customStaticsService.getResourceLayoutModel(parameterMap);
            redisTemplate.opsForValue().set(key, JSON.toJSONString(model), 3, TimeUnit.MINUTES);
            return AjaxResult.success(model);
        }

        //业务类型2:resourceTypeModel
        if (parameterMap.containsKey(CommonConstants.BUSINESS_TYPE) && parameterMap.get(CommonConstants.BUSINESS_TYPE).equals("resourceTypeModel")) {
            String key = "resourceTypeModel" + parameterMap.hashCode();
            Object cache = redisTemplate.opsForValue().get(key);
            if (Objects.nonNull(cache)) {
                VerticalModel val = JSON.parseObject(cache.toString(), VerticalModel.class);
                return AjaxResult.success(val);
            }

            VerticalModel val = customStaticsService.getResourceTypeModel(parameterMap);
            redisTemplate.opsForValue().set(key, JSON.toJSONString(val), 3, TimeUnit.MINUTES);
            return AjaxResult.success(val);
        }

        //业务类型3:userTypeModel
        if (parameterMap.containsKey(CommonConstants.BUSINESS_TYPE) && parameterMap.get(CommonConstants.BUSINESS_TYPE).equals("userTypeModel")) {
            String key = "userTypeModel" + parameterMap.hashCode();
            Object cache = redisTemplate.opsForValue().get(key);
            if (Objects.nonNull(cache)) {
                VerticalModel val = JSON.parseObject(cache.toString(), VerticalModel.class);
                return AjaxResult.success(val);
            }

            VerticalModel val = customStaticsService.getUserTypeModel(parameterMap);
            redisTemplate.opsForValue().set(key, JSON.toJSONString(val), 3, TimeUnit.MINUTES);
            return AjaxResult.success(val);
        }
        return AjaxResult.success();
    }

    @PostMapping("/upsertFastEntry")
    @CacheEvict(value = RedisKeys.Cache.STATISTICS, key = "T(com.sinohealth.common.utils.SecurityUtils).getUserIdIgnoreError()+':'+targetClass + '#fastEntry'")
    public AjaxResult upsertFastEntry(@RequestBody FastEntryUpsertRequest request) {
        redisTemplate.opsForSet().add(RedisKeys.Home.ALREADY_INIT, SecurityUtils.getUserId());
        fastEntryService.upsert(request);
        return AjaxResult.success();
    }

    @GetMapping("/queryHomeInfo")
    @Cacheable(value = RedisKeys.Cache.STATISTICS, key = "T(com.sinohealth.common.utils.SecurityUtils).getUserIdIgnoreError()+':'+targetClass + '#fastEntry'")
    public AjaxResult queryHomeInfo(@RequestParam(value = "topNum", defaultValue = "10") Integer topNum) {
        Map<String, Object> result = new HashMap<>(12);
        Map<String, Object> parameterMap = new HashMap<>(12);
//        log.info(" >>>>>> 补充跳转入口信息");
//        parameterMap.put("uid", ThreadContextHolder.getSysUser().getUserId());

//        List<HomeMenuEntranceVo> fastEntryList = new ArrayList<>();
//        HomeMenuEntranceVo menuEntranceVo1 = new HomeMenuEntranceVo("我的数据", "MyData");
//        HomeMenuEntranceVo menuEntranceVo2 = new HomeMenuEntranceVo("资产文档", "DataManagement/mapDirectory");
//        menuEntranceVo2.setItemLevelOneId(String.valueOf(appProperties.getCmhId()));
//        menuEntranceVo2.setApplicationType("doc");
//        HomeMenuEntranceVo menuEntranceVo3 = new HomeMenuEntranceVo("申请数据", "DataManagement/mapDirectory");
//        menuEntranceVo3.setApplicationType("table");
//        HomeMenuEntranceVo menuEntranceVo4 = new HomeMenuEntranceVo("我的申请", "MyApplication");
//        fastEntryList.add(menuEntranceVo1);
//        fastEntryList.add(menuEntranceVo2);
//        fastEntryList.add(menuEntranceVo3);
//        fastEntryList.add(menuEntranceVo4);
//        result.put("fastEntry", fastEntryList);

        List<FastEntryVO> voList = fastEntryService.queryByUser();
        if (CollectionUtils.isEmpty(voList)) {
            final Boolean hasInit = redisTemplate.opsForSet().isMember(RedisKeys.Home.ALREADY_INIT, SecurityUtils.getUserId());
            if (BooleanUtils.isNotTrue(hasInit)) {
                final List<FastEntryVO> defaultList = DataDirConst.DATA_ASSET_DEFAULT_LIST.stream().map(v -> {
                    final FastEntryVO vo = new FastEntryVO();
                    BeanUtils.copyProperties(v, vo);
                    return vo;
                }).collect(Collectors.toList());
                result.put("fastEntry", defaultList);
            }
        } else {
            result.put("fastEntry", voList);
        }

        parameterMap.put(CommonConstants.TOP_NUM, topNum);
        // 业务废弃
//        List<Map<String, Object>> topResources = applicationService.getTopResources(parameterMap);
//        result.put("topResource", topResources);
//
//        topResources.stream()
//                .filter(v -> Objects.equals(v.get("icon"), CommonConstants.ICON_TABLE))
//                .forEach(v -> {
//                    Object table = v.get("displayName");
//                    TableInfo tableInfo = tableInfoService.selectTableInfoByTableName(table.toString());
//                    Optional.ofNullable(tableInfo).map(TableInfo::getTableAlias).ifPresent(d -> v.put("displayName", d));
//                });

        return AjaxResult.success(result);
    }


}
