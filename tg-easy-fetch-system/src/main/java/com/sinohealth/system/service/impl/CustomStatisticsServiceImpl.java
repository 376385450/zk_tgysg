package com.sinohealth.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysCustomer;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.StatusTypeEnum;
import com.sinohealth.common.enums.dataassets.FileTypeEnum;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.ipaas.model.ResMaindatamainDepartmentselectbyidsItemDataItem;
import com.sinohealth.system.biz.dir.dto.DirPageQueryRequest;
import com.sinohealth.system.biz.dir.vo.HomeDataDirVO;
import com.sinohealth.system.biz.homePage.AssetTypeStatistics;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgCustomerApplyAuth;
import com.sinohealth.system.domain.catalogue.AssetsCatalogue;
import com.sinohealth.system.domain.statistic.PieModel;
import com.sinohealth.system.domain.statistic.VerticalModel;
import com.sinohealth.system.mapper.AssetsCatalogueMapper;
import com.sinohealth.system.mapper.SysUserMapper;
import com.sinohealth.system.mapper.TgAssetInfoMapper;
import com.sinohealth.system.service.ArkbiAnalysisService;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.ICustomStatisticsService;
import com.sinohealth.system.service.IDataDirService;
import com.sinohealth.system.service.ISysCustomerAuthService;
import com.sinohealth.system.service.ISysCustomerService;
import com.sinohealth.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Author Rudolph
 * @Date 2022-05-16 13:57
 * @Desc
 */
@Slf4j
@Service
public class CustomStatisticsServiceImpl implements ICustomStatisticsService {

    @Autowired
    private ISysUserService userService;

    @Autowired
    private IDataDirService dataDirService;

    @Autowired
    private IApplicationService applicationService;

    @Autowired
    private ArkbiAnalysisService arkbiAnalysisService;

    @Autowired
    private ISysCustomerService sysCustomerService;

    @Autowired
    private ISysCustomerAuthService sysCustomerAuthService;

    @Autowired
    private TgAssetInfoMapper tgAssetInfoMapper;

    @Autowired
    private SysUserMapper sysUserMapper;


    @Autowired
    private AssetsCatalogueMapper assetsCatalogueMapper;

    static Cache<String, Object> uidOrgBriefNameCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(12L))
            .build();

    @Override
    public PieModel getResourceLayoutModel(Map<String, Object> parameters) {
        // TODO 等模型和项目数据补充完整， 才能够抽取逻辑，要不还不明确逻辑是否一致
        PieModel result = new PieModel();
        DirPageQueryRequest request = new DirPageQueryRequest() {{
            setSize(Integer.MAX_VALUE);
            setStatus(CommonConstants.NORMAL);
        }};
        AjaxResult<IPage<HomeDataDirVO>> data = dataDirService.pageQueryDir(request);
        result.setName("系统资产分布");
        result.setData(new ArrayList<>());
        result.setTotal(data.getData().getTotal());
        result.setOptions(new ArrayList<String>() {{
            add("主业务");
            add("资产类型");
        }});

        if (parameters.containsKey("option") && parameters.get("option").equals("资产类型")) {
            data.getData().getRecords().stream().collect(Collectors.groupingBy(HomeDataDirVO::getResourceType, Collectors.counting()))
                    .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder())).forEach(e -> result.getData().add(new HashMap<String, Object>() {{
                        put("name", e.getKey());
                        put("value", e.getValue());
                    }}));
        } else {
            data.getData().getRecords().stream()
                    .forEach(
                            r -> r.setBussinessType(r.getDirName().equals(r.getBussinessType()) ?
                                    r.getBussinessType() :
                                    r.getBussinessType().replace("-", "").replace(r.getDirName(), ""))
                    );
            data.getData().getRecords().stream().collect(Collectors.groupingBy(HomeDataDirVO::getBussinessType, Collectors.counting()))
                    .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder())).forEach(e -> result.getData().add(new HashMap<String, Object>() {{
                        put("name", e.getKey());
                        put("value", e.getValue());
                    }}));

        }

        return result;
    }

    @Override
    public VerticalModel getResourceTypeModel(Map<String, Object> parameters) {
        VerticalModel result = new VerticalModel();
        DirPageQueryRequest request = new DirPageQueryRequest() {{
            setSize(Integer.MAX_VALUE);
            setStatus(CommonConstants.NORMAL);
        }};
        if (StringUtils.isNotBlank(getOption(parameters)) && !getOption(parameters).equals("全部")) {
            if (getOption(parameters).equals("数据")) {
                request.setApplicationType(CommonConstants.ICON_TABLE);
            } else if (getOption(parameters).equals("文档")) {
                request.setApplicationType(CommonConstants.ICON_DOC);
            } else {
                // TODO 待补充模型
            }
        }
        AjaxResult<IPage<HomeDataDirVO>> data = dataDirService.pageQueryDir(request);
        result.setName("系统资产-主业务");
        result.setX_axis(new ArrayList<>());

        result.setTotal((int) data.getData().getTotal());
//        result.setOptions(new ArrayList<String>() {{add("数据"); add("文档"); add("模型"); add("全部");}});
        result.setOptions(new ArrayList<String>() {{
            add("数据");
            add("文档");
            add("全部");
        }});
        if (getOption(parameters).equals("文档")) {
            result.setY_axis(new ArrayList<>());
            Map<String, Map<String, Long>> docMapMap = data.getData().getRecords().stream().collect(Collectors.groupingBy(h -> FileTypeEnum.getAssetType(h.getAssetType()), Collectors.groupingBy(d ->
                    !d.getBussinessType().contains("-") ? d.getBussinessType() : d.getBussinessType().split("-")[0], Collectors.counting())));
            List<String> businessTypes = data.getData().getRecords().stream().map(d ->
                    !d.getBussinessType().contains("-") ? d.getBussinessType() : d.getBussinessType().split("-")[0]).distinct().collect(Collectors.toList());
            result.setX_axis(businessTypes);
            docMapMap.entrySet().forEach(e1 -> {
                VerticalModel.YAxis yAxis = new VerticalModel.YAxis();
                yAxis.setYName(e1.getKey());
                businessTypes.forEach(b -> yAxis.getData().add(e1.getValue().getOrDefault(b, 0L).intValue()));
                result.getY_axis().add(yAxis);
            });
            return result;
        } else if (getOption(parameters).equals("数据")) {
            result.setY_axis(new ArrayList<VerticalModel.YAxis>() {{
                add(new VerticalModel.YAxis());
                add(new VerticalModel.YAxis());
            }});
            data.getData().getRecords().stream()
                    .forEach(
                            r -> r.setBussinessType(r.getDirName().equals(r.getBussinessType()) ?
                                    r.getBussinessType() :
                                    r.getBussinessType().replace("-", "").replace(r.getDirName(), ""))
                    );
            result.getY_axis().get(0).setYName("业务数据");
            result.getY_axis().get(1).setYName("项目数据");
            data.getData().getRecords().stream().collect(Collectors.groupingBy(HomeDataDirVO::getBussinessType, Collectors.counting()))
                    .entrySet().stream().forEach(e -> {
                        result.getX_axis().add(e.getKey());
                        result.getY_axis().get(0).getData().add(e.getValue().intValue());
                        // TODO 待补充项目数据
                        result.getY_axis().get(1).getData().add(0);
                    });

            return result;
        } else if (getOption(parameters).equals("模型")) {
            // TODO 待补充模型
            return result;
        } else {
            // 全部
            result.setY_axis(new ArrayList<>());
            List<String> businessTypes = data.getData().getRecords().stream().map(d ->
                    !d.getBussinessType().contains("-") ? d.getBussinessType() : d.getBussinessType().split("-")[0]).distinct().collect(Collectors.toList());
            result.setX_axis(businessTypes);
            Map<String, Map<String, Long>> allDataMap = data.getData().getRecords().stream().collect(Collectors.groupingBy(HomeDataDirVO::getResourceType, Collectors.groupingBy(d ->
                    !d.getBussinessType().contains("-") ? d.getBussinessType() : d.getBussinessType().split("-")[0], Collectors.counting())));
            allDataMap.entrySet().forEach(e1 -> {
                VerticalModel.YAxis yAxis = new VerticalModel.YAxis();
                yAxis.setYName(e1.getKey());
                businessTypes.forEach(b -> yAxis.getData().add(e1.getValue().getOrDefault(b, 0L).intValue()));
                result.getY_axis().add(yAxis);
            });

            return result;
        }
    }

    private String getOption(Map<String, Object> parameters) {
        return parameters.containsKey("option") ? parameters.get("option").toString() : "";
    }

    @Override
    public VerticalModel getUserTypeModel(Map<String, Object> parameters) {
        StopWatch watch = new StopWatch();
        log.info(" >>>>>> 初始化结果信息");
        VerticalModel result = new VerticalModel();
        result.setName("用户资产");
        result.setOptions(new ArrayList<String>() {{
            add("分析师");
//            add("客户");
        }});
        result.setX_axis(new ArrayList<>());
        result.setY_axis(new ArrayList<VerticalModel.YAxis>() {{
            IntStream.rangeClosed(0, 2).forEach(n -> add(new VerticalModel.YAxis()));
        }});
        VerticalModel.YAxis formYAxis = result.getY_axis().get(0);
        VerticalModel.YAxis chartYAxis = result.getY_axis().get(1);
        VerticalModel.YAxis dashboardYAxis = result.getY_axis().get(2);
        formYAxis.setYName("数据");
        chartYAxis.setYName("图表");
        dashboardYAxis.setYName("报告");

        if (parameters.containsKey("option") && parameters.get("option").equals("客户")) {
            log.info(" >>>>>> 客户角色统计发放的数据, 按数据form/图表chart/报表dashboard");
            log.info(" >>>>>> 获取客户数据用于统计");
            Wrapper<TgCustomerApplyAuth> wrapper = Wrappers.lambdaQuery(TgCustomerApplyAuth.class)
                    .eq(TgCustomerApplyAuth::getStatus, StatusTypeEnum.IS_ENABLE.getId())
                    .ne(TgCustomerApplyAuth::getIcon, "pack");
            List<TgCustomerApplyAuth> tgCustomerAuthList = sysCustomerAuthService.list(wrapper);

            Map<Long, String> customerTypeMap = sysCustomerService.listAllCustomers().stream().collect(Collectors.toMap(SysCustomer::getUserId, s -> StringUtils.isNotBlank(s.getCustomerType()) ? s.getCustomerType() : "其他"));

            log.info(" >>>>>> 填充X轴和Y轴缺省数据");
            TreeSet<String> customerTypes = tgCustomerAuthList.stream().peek(c -> c.setCustomerType(customerTypeMap.getOrDefault(c.getUserId(), "其他")))
                    .map(TgCustomerApplyAuth::getCustomerType).collect(Collectors.toCollection(TreeSet::new));
            fillAxisDefaultData(result, formYAxis, chartYAxis, dashboardYAxis, customerTypes);

            List<String> xAxis = customerTypes.stream().collect(Collectors.toList());

            log.info(" >>>>>> 设置X轴,Y轴参数");
            tgCustomerAuthList.stream().peek(c -> c.setCustomerType(customerTypeMap.getOrDefault(c.getUserId(), "其他")))
                    .collect(Collectors.groupingBy(TgCustomerApplyAuth::getIcon, Collectors.groupingBy(TgCustomerApplyAuth::getCustomerType, TreeMap::new, Collectors.counting()))).entrySet().forEach(e -> {
                        for (int i = 0; xAxis.size() > i; i++) {
                            String xAxi = xAxis.get(i);
                            int finalIdx = i;
                            e.getValue().entrySet().forEach(m -> {
                                if (e.getKey().equals(CommonConstants.ICON_DATA_ASSETS) && xAxi.equals(m.getKey())) {
                                    formYAxis.getData().set(finalIdx, m.getValue().intValue());
                                }
                                if (e.getKey().equals(CommonConstants.ICON_CHART) && xAxi.equals(m.getKey())) {
                                    chartYAxis.getData().set(finalIdx, m.getValue().intValue());
                                }
                                if (e.getKey().equals(CommonConstants.ICON_DASHBOARD) && xAxi.equals(m.getKey())) {
                                    dashboardYAxis.getData().set(finalIdx, m.getValue().intValue());
                                }
                            });
                        }
                    });

            log.info(" >>>>>> 填充总数");
            result.setTotal(tgCustomerAuthList.size());

        } else {
            log.info(" >>>>>> 分析师角色统计某一目录下申请数据, 按数据form/图表chart/报表dashboard");
            // 统计某一目录底下的资产对应的申请
            List<TgAssetInfo> tgAssetInfos = Lists.newArrayList();
            if (parameters.containsKey("catalogId")) {
                Integer catalogId = (Integer) parameters.get("catalogId");
                AssetTypeStatistics assetTypeStatistics = new AssetTypeStatistics();
                // 获取需要统计的目录-所有
                final List<AssetsCatalogue> assetsCatalogues = assetsCatalogueMapper.selectListInPath(Lists.newArrayList(catalogId));
                // 获取资产总数
                final List<Integer> catalogueIds = assetsCatalogues.stream().map(AssetsCatalogue::getId).collect(Collectors.toList());
                final LambdaQueryWrapper<TgAssetInfo> qw = Wrappers.<TgAssetInfo>lambdaQuery()
                        .in(TgAssetInfo::getAssetMenuId, catalogueIds)
                        .eq(TgAssetInfo::getDeleted, 0);
                tgAssetInfos = tgAssetInfoMapper.selectList(qw);
            }

            List<Long> assetIds = tgAssetInfos.stream().map(TgAssetInfo::getId).collect(Collectors.toList());
            log.info(" >>>>>> 获取申请和BI图表/报表数据数据用于统计");
            int total = 0;

            if (!assetIds.isEmpty()) {
                watch.start("请求bi数据");
                List<ArkbiAnalysis> biData = arkbiAnalysisService.listNormalBiData(assetIds);
                watch.stop();
                watch.start("请求申请数据");
                List<TgApplicationInfo> data = applicationService.listAllNormalDataApplications(assetIds);
                watch.stop();
                watch.start("请求图表数据");
                List<ArkbiAnalysis> charts = biData.stream().filter(ark -> ark.getApplicantId() != CommonConstants.ABNORMAL.longValue()
                        && ark.getType().equals(CommonConstants.ICON_CHART)).collect(Collectors.toList());
                watch.stop();
                watch.start("请求报表数据");
                List<ArkbiAnalysis> dashboards = biData.stream().filter(ark -> ark.getApplicantId() != CommonConstants.ABNORMAL.longValue()
                        && ark.getType().equals(CommonConstants.ICON_DASHBOARD)).collect(Collectors.toList());
                Set<String> allBriefNames = new TreeSet<>();
                watch.stop();
                watch.start("统计");
                log.info(" >>>>>> 通过简称统计申请数据/BI图表/BI报表");
                Map<String, Long> briefNameDataCountRecorder = data.stream().collect(Collectors.groupingBy(a -> getBriefName(a.getApplicantId(), allBriefNames), Collectors.counting()));
                Map<String, Long> briefNameChartCountRecorder = charts.stream().collect(Collectors.groupingBy(a -> getBriefName(a.getApplicantId(), allBriefNames), Collectors.counting()));
                Map<String, Long> briefNameDashboardCountRecorder = dashboards.stream().collect(Collectors.groupingBy(a -> getBriefName(a.getApplicantId(), allBriefNames), Collectors.counting()));
                watch.stop();
                watch.start("填充缺省数据");
                log.info(" >>>>>> 填充X轴和Y轴缺省数据");
                fillAxisDefaultData(result, formYAxis, chartYAxis, dashboardYAxis, allBriefNames);
                List<String> xAxis = allBriefNames.stream().collect(Collectors.toList());
                watch.stop();
                watch.start("填充统计数据");
                log.info(" >>>>>> 填充X轴和Y轴统计数据");
                for (int i = 0; xAxis.size() > i; i++) {
                    String xAxi = xAxis.get(i);
                    int tmpDataCount = Optional.ofNullable(briefNameDataCountRecorder.get(xAxi)).orElse(0L).intValue();
                    int tmpChartCount = Optional.ofNullable(briefNameChartCountRecorder.get(xAxi)).orElse(0L).intValue();
                    int tmpDashboardCount = Optional.ofNullable(briefNameDashboardCountRecorder.get(xAxi)).orElse(0L).intValue();
                    formYAxis.getData().set(i, tmpDataCount);
                    chartYAxis.getData().set(i, tmpChartCount);
                    dashboardYAxis.getData().set(i, tmpDashboardCount);
                    total += tmpDataCount + tmpChartCount + tmpDashboardCount;
                }
                watch.stop();
            }

            log.info(" >>>>>> 填充总数");
            result.setTotal(total);

            log.info("TREE {} {}", watch.getTotalTimeMillis(), watch.prettyPrint());
        }

        return result;
    }

    private void fillAxisDefaultData(VerticalModel result, VerticalModel.YAxis formYAxis, VerticalModel.YAxis chartYAxis, VerticalModel.YAxis dashboardYAxis, Set<String> allBriefNames) {
        allBriefNames.stream().forEach(xAxi -> {
            result.getX_axis().add(xAxi);
            formYAxis.getData().add(0);
            chartYAxis.getData().add(0);
            dashboardYAxis.getData().add(0);
        });
    }

    private String getBriefName(Long uid, Set<String> allBriefNames) {
        String key = "sino_pass:briefNames:" + uid;
        Object result = uidOrgBriefNameCache.getIfPresent(key);
        if (result != null) {
            allBriefNames.add(result.toString());
            return result.toString();
        } else {
            SysUser sysUser = userService.selectUserById(uid);
            SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId());
            // 缺省部门值
            String defaultBriefName = sinoPassUserDTO.getOrgAdminTreePathText().contains("\\/") ?
                    sinoPassUserDTO.getOrgAdminTreePathText().split("\\/")[1] :
                    sinoPassUserDTO.getOrgAdminTreePathText();

            ResMaindatamainDepartmentselectbyidsItemDataItem item = SinoipaasUtils.mainDepartmentSelectbyids(sinoPassUserDTO.getMainOrganizationId());
            String briefName = Optional.ofNullable(item)
                    .map(v -> sysUserMapper.queryOrgBriefName(v.getOrgAdminTreePathText()))
                    .map(v -> v.get("brief_name"))
                    .orElseGet(() -> {
                        log.warn("USE Default {} {}", sinoPassUserDTO.getOrgAdminTreePathText(), defaultBriefName);
                        return defaultBriefName;
                    });
            allBriefNames.add(briefName);
            Optional.ofNullable(briefName).ifPresent(value -> uidOrgBriefNameCache.put(key, value));
            return briefName;
        }
    }

}
