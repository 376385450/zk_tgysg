package com.sinohealth.system.biz.dataassets.constant;

import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.ck.constant.CkTableSuffixTable;
import lombok.Getter;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-06-17 17:53
 */
@Getter
public enum AssetsQcTypeEnum {
    /**
     * 将SKU资产合并入SKU公共表
     */
    sku("insert into %s (" +
            "period, province, city_co_name, std_id, prodcode, otc_rx, sort1, sort2," +
            " sort3, sort4, zx, brand, tym, pm_all, pm, cj, gg, jx, dx, avg_dj, fd_xse," +
            " tz_fdxse, sample_xsl, sz_phl, jq_phl, sample_xse, project_name," +
            " project_zoneclass, period_granular, fd_xsl, ddu, tv, vpd, otherstag, spm," +
            " company_right, sc_old_label, short_cj, short_brand) " +
            "select period_str,\n" +
            "       t_1_province,\n" +
            "       t_1_city_co_name,\n" +
            "       t_1_std_id,\n" +
            "       t_1_prodcode,\n" +
            "       t_1_otc_rx,\n" +
            "       t_1_sort1,\n" +
            "       t_1_sort2,\n" +
            "       t_1_sort3,\n" +
            "       t_1_sort4,\n" +
            "       t_1_zx,\n" +
            "       t_1_brand,\n" +
            "       t_1_tym,\n" +
            "       t_1_pm_all,\n" +
            "       t_1_pm,\n" +
            "       t_1_cj,\n" +
            "       t_1_gg,\n" +
            "       t_1_jx,\n" +
            "       t_1_dx,\n" +
            "       `平均单价`,\n" +
            "       null,\n" +
            "       `放大销售额`,\n" +
            "       null,\n" +
            "       `铺货率`,\n" +
            "       `加权铺货率`,\n" +
            "       `样本销售额`,\n" +
            "       '%s',\n" +
            "       t_1_zone_name,\n" +
            "       period_type,\n" +
            "       `放大销售量`,\n" +
            "       `累计可服用天数`,\n" +
            "       toString(`装量`),\n" +
            "       toString(`日服用量`),\n" +
            "       t_1_otherstag,\n" +
            "       t_1_spm,\n" +
            "       t_1_company_rights,\n" +
            "       t_1_sc_old_label,\n" +
            "       t_1_short_cj,\n" +
            "       t_1_short_brand from %s"),
    brand("");

    private final String mapSQL;

    AssetsQcTypeEnum(String mapSQL) {
        this.mapSQL = mapSQL;
    }

    public String buildClean(String mergeTab) {
        if (StringUtils.isBlank(mergeTab)) {
            throw new CustomException("未配置合并表");
        }
        String local = CkTableSuffixTable.getLocal(mergeTab);
        return "truncate table " + local + " on cluster default_cluster";
    }

    public String buildInsert(String mergeTab, String name, String assetsTab) {
        return String.format(this.mapSQL, mergeTab, name, assetsTab);
    }
}
