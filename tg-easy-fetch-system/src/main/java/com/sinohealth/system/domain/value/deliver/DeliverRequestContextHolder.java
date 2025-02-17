package com.sinohealth.system.domain.value.deliver;

import cn.hutool.core.lang.Assert;
import com.google.common.collect.Lists;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.utils.spring.SpringUtils;
import com.sinohealth.system.biz.dir.dto.node.AssetsNode;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.domain.value.deliver.datasource.ApplicationDataSource;
import com.sinohealth.system.domain.value.deliver.datasource.CharAnalysisDataSource;
import com.sinohealth.system.domain.value.deliver.datasource.PanelDataSource;
import com.sinohealth.system.dto.application.deliver.request.DeliverPackBaseReq;
import com.sinohealth.system.mapper.ArkbiAnalysisMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-29 14:06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverRequestContextHolder {

    private List<ApplicationDataSource> applicationDataSources;

    private List<CharAnalysisDataSource> charAnalysisDataSources;

    private List<PanelDataSource> panelDataSources;

    private List<DataSource> dataSources = null;

    private DeliverResourceType type;

    private Boolean pack;

    private String packName;

    /**
     * {@link com.sinohealth.system.domain.constant.AsyncTaskConst.SINK_TYPE}
     */
    private Integer sinkType;

    /**
     * {dirId, {son-dirId}}
     */
    @Deprecated
    private Map<DataDir, List<DataDir>> dirMap = new HashMap<>();

    public boolean isComplex() {
        if (isSingleApplication()) {
            return false;
        }
        if (isSingleChartAnalysis()) {
            return false;
        }
        if (isSinglePanel()) {
            return false;
        }
        return true;
    }

    public boolean isSingleApplication() {
        if (CollectionUtils.isNotEmpty(applicationDataSources) && CollectionUtils.isEmpty(charAnalysisDataSources) && CollectionUtils.isEmpty(panelDataSources)) {
            return true;
        }
        return false;
    }

    public boolean isSinglePanel() {
        if (CollectionUtils.isEmpty(applicationDataSources) && CollectionUtils.isEmpty(charAnalysisDataSources) && CollectionUtils.isNotEmpty(panelDataSources)) {
            return true;
        }
        return false;
    }

    public boolean isSingleChartAnalysis() {
        if (CollectionUtils.isEmpty(applicationDataSources) && CollectionUtils.isNotEmpty(charAnalysisDataSources) && CollectionUtils.isEmpty(panelDataSources)) {
            return true;
        }
        return false;
    }

    public void checkTypeThrows() {
        List<DeliverResourceType> supportResourceType = DeliverHelper.getSupportResourceType(this);
        Assert.isTrue(supportResourceType.contains(type), "请检查勾选的项目！不支持的交付类型: " + type.name());
    }

    public List<DataSource> getDataSources() {
        if (dataSources != null) {
            return dataSources;
        }
        List<DataSource> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(applicationDataSources)) {
            list.addAll(applicationDataSources);
        }
        if (CollectionUtils.isNotEmpty(charAnalysisDataSources)) {
            list.addAll(charAnalysisDataSources);
        }
        if (CollectionUtils.isNotEmpty(panelDataSources)) {
            list.addAll(panelDataSources);
        }
        this.dataSources = list;
        return dataSources;
    }

    public static DeliverRequestContextHolder build(DeliverPackBaseReq baseReq, DeliverResourceType type) {
        if (baseReq.getAssetsId() != null) {
            return buildRequestContextHolder(baseReq.getAssetsId(), baseReq.getPack(), baseReq.getPackName(), type);
        }
        return buildRequestContextHolder(baseReq.getNodeIds(), baseReq.getPack(), baseReq.getPackName(), type);
    }


    private static DeliverRequestContextHolder buildRequestContextHolder(List<String> nodeIds, Boolean pack,
                                                                         String packName, DeliverResourceType type) {
//        List<DataDir> dataDirs = SpringUtils.getBean(IMyDataDirService.class).listByIds(nodeIds);

        List<Pair<Long, String>> idPairs = nodeIds.stream().filter(Objects::nonNull).map(AssetsNode::parseId).filter(Objects::nonNull).collect(Collectors.toList());

        ArkbiAnalysisMapper mapper = SpringUtils.getBean(ArkbiAnalysisMapper.class);
        // 根据icon区分申请、仪表盘、图表分析
        List<ApplicationDataSource> applicationDataSources = new ArrayList<>();
        List<PanelDataSource> panelDataSources = new ArrayList<>();
        List<CharAnalysisDataSource> charAnalysisDataSources = new ArrayList<>();

        for (Pair<Long, String> idPair : idPairs) {
            if (CommonConstants.ICON_DATA_ASSETS.equals(idPair.getValue())) {
                applicationDataSources.add(new ApplicationDataSource(idPair.getKey()));
            } else if (CommonConstants.ICON_CHART.equals(idPair.getValue())) {
                ArkbiAnalysis ark = mapper.selectById(idPair.getKey());
                charAnalysisDataSources.add(new CharAnalysisDataSource(idPair.getKey(), Optional.ofNullable(ark).map(ArkbiAnalysis::getName).orElse("")));
            } else if (CommonConstants.ICON_DASHBOARD.equals(idPair.getValue())) {
                ArkbiAnalysis ark = mapper.selectById(idPair.getKey());
                panelDataSources.add(new PanelDataSource(idPair.getKey(), Optional.ofNullable(ark).map(ArkbiAnalysis::getName).orElse("")));
            }
        }
//        dataDirs.stream()
//                // 我的数据目录、启用
//                .filter(dir -> Objects.equals(dir.getTarget(), CommonConstants.MY_DATA_DIR) && Objects.equals(dir.getStatus(), 1))
//                .forEach(dir -> {
//                    if (CommonConstants.ICON_DATA_ASSETS.equals(dir.getIcon())) {
//                        applicationDataSources.add(new ApplicationDataSource(dir.getNodeId()));
//                    } else if (CommonConstants.ICON_CHART.equals(dir.getIcon())) {
//                        charAnalysisDataSources.add(new CharAnalysisDataSource(dir.getNodeId(), dir.getDirName()));
//                    } else if (CommonConstants.ICON_DASHBOARD.equals(dir.getIcon())) {
//                        panelDataSources.add(new PanelDataSource(dir.getNodeId(), dir.getDirName()));
//                    }
//                });
        DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.builder()
                .applicationDataSources(applicationDataSources)
                .charAnalysisDataSources(charAnalysisDataSources)
                .panelDataSources(panelDataSources)
                .type(type)
                .pack(BooleanUtils.isTrue(pack))
                .packName(packName)
//                .dirMap(CustomerApplyAuthRequestContext.buildNodeRelationMap(dataDirs))
                .build();
        return requestContextHolder;
    }

    private static DeliverRequestContextHolder buildRequestContextHolder(Long assetsId, Boolean pack,
                                                                         String packName, DeliverResourceType type) {
//        DataDir dataDir = SpringUtils.getBean(IMyDataDirService.class).getByAssets(assetsId);
        List<Long> applicationIds = Lists.newArrayList(assetsId);
        List<ApplicationDataSource> applicationDataSources = applicationIds.stream().map(ApplicationDataSource::new)
                .collect(Collectors.toList());
        DeliverRequestContextHolder requestContextHolder = DeliverRequestContextHolder.builder()
                .applicationDataSources(applicationDataSources)
                .charAnalysisDataSources(Collections.emptyList())
                .panelDataSources(Collections.emptyList())
                .type(type)
                .pack(BooleanUtils.isTrue(pack))
                .packName(packName)
//                .dirMap(CustomerApplyAuthRequestContext.buildNodeRelationMap(Collections.singletonList(dataDir)))
                .build();
        return requestContextHolder;
    }

}

