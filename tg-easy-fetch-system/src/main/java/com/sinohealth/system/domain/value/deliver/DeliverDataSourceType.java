package com.sinohealth.system.domain.value.deliver;

import com.sinohealth.common.constant.CommonConstants;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-28 17:38
 */
public enum DeliverDataSourceType {

    ASSETS,

    CHART_ANALYSIS,

    PANEL,

    /**
     * 客户资产数据
     */
    CUSTOMER_APPLY



    ;


    public String toIcon() {
        switch (this) {
            case ASSETS: return CommonConstants.ICON_DATA_ASSETS;
            case CHART_ANALYSIS: return CommonConstants.ICON_CHART;
            case PANEL: return CommonConstants.ICON_DASHBOARD;
            default: return null;
        }
    }

    public static DeliverDataSourceType fromIcon(String icon) {
        if (CommonConstants.ICON_DATA_ASSETS.equals(icon)) {
            return ASSETS;
        }
        if (CommonConstants.ICON_CHART.equals(icon)) {
            return CHART_ANALYSIS;
        }
        if (CommonConstants.ICON_DASHBOARD.equals(icon)) {
            return PANEL;
        }
        return null;
    }


}
