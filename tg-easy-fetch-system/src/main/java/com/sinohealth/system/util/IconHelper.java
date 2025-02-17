package com.sinohealth.system.util;

import com.sinohealth.system.domain.value.deliver.DeliverDataSourceType;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-13 10:17
 */
public class IconHelper {

    public static boolean isArkbiNode(String icon) {
        return DeliverDataSourceType.CHART_ANALYSIS.equals(DeliverDataSourceType.fromIcon(icon)) || DeliverDataSourceType.PANEL.equals(DeliverDataSourceType.fromIcon(icon));
    }

    public static boolean isAssetsNode(String icon) {
        return DeliverDataSourceType.ASSETS.equals(DeliverDataSourceType.fromIcon(icon));
    }
}
