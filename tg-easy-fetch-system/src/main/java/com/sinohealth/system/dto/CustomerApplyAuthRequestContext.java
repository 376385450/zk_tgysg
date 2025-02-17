package com.sinohealth.system.dto;

import com.google.common.collect.Lists;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.utils.spring.SpringUtils;
import com.sinohealth.system.domain.value.deliver.DataSource;
import com.sinohealth.system.domain.value.deliver.DeliverDataSourceType;
import com.sinohealth.system.domain.value.deliver.datasource.ApplicationDataSource;
import com.sinohealth.system.domain.value.deliver.datasource.CharAnalysisDataSource;
import com.sinohealth.system.domain.value.deliver.datasource.PanelDataSource;
import com.sinohealth.system.service.IMyDataDirService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-01 09:44
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerApplyAuthRequestContext {

    private List<ApplicationDataSource> applicationDataSources;

    private List<CharAnalysisDataSource> charAnalysisDataSources;

    private List<PanelDataSource> panelDataSources;

    private List<DataSource> dataSources = null;

    private List<CustomerApplyAuthReqV2DTO.CustomerApplyAuthUserItemDTO> authList;

    private List<Integer> deleteAuthIds;

    /**
     * 是否打包交付
     */
    private Boolean pack;

    /**
     * 打包项目名
     */
    private String packName;

    /**
     * 客户资产树的根节点id，默认为0
     */
    private Long parentId;

    /**
     * {dirId, {son-dirId}}
     */
    private Map<DataDir, List<DataDir>> dirMap = new HashMap<>();


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


    public static CustomerApplyAuthRequestContext build(CustomerApplyAuthReqV2DTO reqDTO) {
        if (reqDTO.getAssetsId() != null) {
            DataDir dataDir = SpringUtils.getBean(IMyDataDirService.class).getByAssets(reqDTO.getAssetsId());
            List<Long> assetsIds = Lists.newArrayList(reqDTO.getAssetsId());
            List<ApplicationDataSource> applicationDataSources = assetsIds.stream().map(ApplicationDataSource::new).collect(Collectors.toList());
            CustomerApplyAuthRequestContext requestContext = CustomerApplyAuthRequestContext.builder()
                    .applicationDataSources(applicationDataSources)
                    .pack(reqDTO.getPack())
                    .packName(reqDTO.getPackName())
                    .authList(reqDTO.getList())
                    .deleteAuthIds(reqDTO.getDeleteIds())
                    .dirMap(buildNodeRelationMap(Collections.singletonList(dataDir)))
                    .parentId(0L)
                    .build();
            return requestContext;
        } else {
            List<DataDir> dataDirs = SpringUtils.getBean(IMyDataDirService.class).listByIds(reqDTO.getIds());
            // 根据icon区分申请、仪表盘、图表分析
            List<ApplicationDataSource> applicationDataSources = new ArrayList<>();
            List<PanelDataSource> panelDataSources = new ArrayList<>();
            List<CharAnalysisDataSource> charAnalysisDataSources = new ArrayList<>();

            dataDirs.stream()
                    // 我的数据目录、启用
                    .filter(dir -> Objects.equals(dir.getTarget(), CommonConstants.MY_DATA_DIR) && Objects.equals(dir.getStatus(), 1))
                    .forEach(dir -> {
                        if (CommonConstants.ICON_DATA_ASSETS.equals(dir.getIcon())) {
                            applicationDataSources.add(new ApplicationDataSource(dir.getNodeId()).setName(dir.getDirName()));
                        } else if (CommonConstants.ICON_CHART.equals(dir.getIcon())) {
                            charAnalysisDataSources.add(new CharAnalysisDataSource(dir.getNodeId(), dir.getDirName()));
                        } else if (CommonConstants.ICON_DASHBOARD.equals(dir.getIcon())) {
                            panelDataSources.add(new PanelDataSource(dir.getNodeId(), dir.getDirName()));
                        }
                    });
            CustomerApplyAuthRequestContext requestContext = CustomerApplyAuthRequestContext.builder()
                    .applicationDataSources(applicationDataSources)
                    .charAnalysisDataSources(charAnalysisDataSources)
                    .panelDataSources(panelDataSources)
                    .pack(reqDTO.getPack())
                    .packName(reqDTO.getPackName())
                    .authList(reqDTO.getList())
                    .deleteAuthIds(reqDTO.getDeleteIds())
                    .dirMap(buildNodeRelationMap(dataDirs))
                    .parentId(0L)
                    .build();
            return requestContext;
        }
    }

    /**
     * 图表分析必须是提数申请的孩子节点
     * @return
     */
    public static Map<DataDir, List<DataDir>> buildNodeRelationMap(List<DataDir> dataDirs) {
        if (CollectionUtils.isEmpty(dataDirs)) {
            return Collections.emptyMap();
        }
        Map<Long, List<DataDir>> relationMap = new HashMap<>();
        Map<Long, DataDir> dataDirMap = dataDirs.stream().collect(Collectors.toMap(DataDir::getId, Function.identity()));
        for (DataDir dataDir : dataDirs) {
            // 目前只处理图表分析
            if (!DeliverDataSourceType.CHART_ANALYSIS.equals(DeliverDataSourceType.fromIcon(dataDir.getIcon()))) {
                // 不是图表分析直接放在父节点
                relationMap.put(dataDir.getId(), new ArrayList<>());
                continue;
            }
            if (dataDir.getParentId() != null && dataDirMap.containsKey(dataDir.getParentId())) {
                if (!relationMap.containsKey(dataDir.getParentId())) {
                    relationMap.put(dataDir.getParentId(), new ArrayList<>());
                }
                relationMap.get(dataDir.getParentId()).add(dataDir);
            } else {
                // 没有父节点
                relationMap.put(dataDir.getId(), new ArrayList<>());
            }
        }
        Map<DataDir, List<DataDir>> resultMap = new HashMap<>(relationMap.size());
        relationMap.keySet().forEach(dataDirId -> {
            resultMap.put(dataDirMap.get(dataDirId), relationMap.get(dataDirId));
        });

        return resultMap;
    }


}
