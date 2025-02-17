package com.sinohealth.system.domain.value.deliver;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 产品的需求里，复合数据源支持的导出比单数据源支持的导出的交集来的少，因此手动设置个映射
 *
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-29 17:15
 */
public class DeliverHelper {

    public static final Map<String, List<DeliverResourceType>> SOURCE_MAP;

    static {
        Map<String, List<DeliverResourceType>> map = new HashMap<String, List<DeliverResourceType>>() {
            {
                // TODO 注意1.9.0 后不会有提数参数进来，交互变了
                // 提数支持的方式
                put(DeliverDataSourceType.ASSETS.name(), Arrays.asList(DeliverResourceType.CUSTOMER,
                        DeliverResourceType.EMAIL, DeliverResourceType.CSV, DeliverResourceType.EXCEL));
                // 图表分析支持的方式
                put(DeliverDataSourceType.CHART_ANALYSIS.name(), Arrays.asList(
                        DeliverResourceType.CSV, DeliverResourceType.EXCEL, DeliverResourceType.PDF, DeliverResourceType.IMAGE));
                // 仪表板支持的方式
                put(DeliverDataSourceType.PANEL.name(), Arrays.asList(DeliverResourceType.CUSTOMER,
                        DeliverResourceType.EMAIL, DeliverResourceType.CSV, DeliverResourceType.EXCEL, DeliverResourceType.PDF,
                        DeliverResourceType.IMAGE));

                // 提数+图表分析 混合支持的方式
                put(DeliverDataSourceType.ASSETS.name() + "-" + DeliverDataSourceType.CHART_ANALYSIS.name(),
                        Arrays.asList(DeliverResourceType.CUSTOMER, DeliverResourceType.EMAIL, DeliverResourceType.CSV, DeliverResourceType.EXCEL));

                // 提数+图表分析+仪表板 混合支持的方式
                put(DeliverDataSourceType.ASSETS.name() + "-" + DeliverDataSourceType.CHART_ANALYSIS.name() + "-" + DeliverDataSourceType.PANEL.name(),
                        Arrays.asList(DeliverResourceType.CUSTOMER, DeliverResourceType.EMAIL, DeliverResourceType.CSV, DeliverResourceType.EXCEL));

                // 图表分析+仪表板 混合支持的方式
                put(DeliverDataSourceType.CHART_ANALYSIS.name() + "-" + DeliverDataSourceType.PANEL.name(),
                        Arrays.asList(DeliverResourceType.CSV, DeliverResourceType.EXCEL, DeliverResourceType.PDF, DeliverResourceType.IMAGE));

                // 提数+仪表板 混合支持的方式
                put(DeliverDataSourceType.ASSETS.name() + "-" + DeliverDataSourceType.PANEL.name(),
                        Arrays.asList(DeliverResourceType.CUSTOMER, DeliverResourceType.EMAIL, DeliverResourceType.CSV, DeliverResourceType.EXCEL));
            }
        };
        SOURCE_MAP = Collections.unmodifiableMap(map);
    }

    public static List<DeliverResourceType> getSupportResourceType(DeliverRequestContextHolder holder) {
        if (!holder.isComplex()) {
            if (holder.isSingleApplication()) {
                return SOURCE_MAP.get(DeliverDataSourceType.ASSETS.name());
            }
            if (holder.isSingleChartAnalysis()) {
                return SOURCE_MAP.get(DeliverDataSourceType.CHART_ANALYSIS.name());
            }
            if (holder.isSinglePanel()) {
                return SOURCE_MAP.get(DeliverDataSourceType.PANEL.name());
            }
        } else {
            if (CollectionUtils.isNotEmpty(holder.getApplicationDataSources())
                    && CollectionUtils.isNotEmpty(holder.getCharAnalysisDataSources())
                    && CollectionUtils.isEmpty(holder.getPanelDataSources())) {
                return SOURCE_MAP.get(DeliverDataSourceType.ASSETS.name() + "-" + DeliverDataSourceType.CHART_ANALYSIS.name());
            }

            if (CollectionUtils.isNotEmpty(holder.getApplicationDataSources())
                    && CollectionUtils.isEmpty(holder.getCharAnalysisDataSources())
                    && CollectionUtils.isNotEmpty(holder.getPanelDataSources())) {
                return SOURCE_MAP.get(DeliverDataSourceType.ASSETS.name() + "-" + DeliverDataSourceType.PANEL.name());
            }

            if (CollectionUtils.isEmpty(holder.getApplicationDataSources())
                    && CollectionUtils.isNotEmpty(holder.getCharAnalysisDataSources())
                    && CollectionUtils.isNotEmpty(holder.getPanelDataSources())) {
                return SOURCE_MAP.get(DeliverDataSourceType.CHART_ANALYSIS.name() + "-" + DeliverDataSourceType.PANEL.name());
            }

            if (CollectionUtils.isNotEmpty(holder.getApplicationDataSources())
                    && CollectionUtils.isNotEmpty(holder.getCharAnalysisDataSources())
                    && CollectionUtils.isNotEmpty(holder.getPanelDataSources())) {
                return SOURCE_MAP.get(DeliverDataSourceType.ASSETS.name() + "-" + DeliverDataSourceType.CHART_ANALYSIS.name() + "-" + DeliverDataSourceType.PANEL.name());
            }
        }
        return Collections.emptyList();
    }


}
