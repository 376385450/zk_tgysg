package com.sinohealth.system.biz.dataassets.dto.compare;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-12-20 11:17
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandAssetsCompareVO implements CompareModel {
    /**
     * excel id
     */
    private Long id;
    /**
     * 数据总量
     */
    private Long total;

    /**
     * 数值铺货率：平均 6位
     */
    private String szPhl;

    /**
     * 加权铺货率：平均 6位
     */
    private String jqPhl;
    /**
     * 放大销售额: 求和 4位
     */
    private String fdXse;

    /**
     * 放大销售量： 求和 4位
     */
    private String fdXsl;

    /**
     * 平均单价: 平均 4位精度
     */
    private String avgDj;

    /**
     * 放大销售额长尾：求和 4位
     */
    private String fdXseCw;
    /**
     * 放大销售量长尾：求和 4位
     */
    private String fdXslCw;
    /**
     * 平均单价 不含长尾： 求和 2位
     */
    private String avgDjCw;
    /**
     * 品牌销售片数：求和 2位
     */
    private String xsPs;

    @Override
    public void fillQueryVal(List<LinkedHashMap<String, Object>> result) {
        try {
            LinkedHashMap<String, Object> map = result.get(0);
            this.setTotal(Long.parseLong(map.get("total").toString()));
            this.setSzPhl(Optional.ofNullable(map.get("A4")).map(Object::toString).orElse(null));
            this.setJqPhl(Optional.ofNullable(map.get("A5")).map(Object::toString).orElse(null));
            this.setFdXse(Optional.ofNullable(map.get("A3")).map(Object::toString).orElse(null));
            this.setFdXsl(Optional.ofNullable(map.get("A2")).map(Object::toString).orElse(null));
            this.setAvgDj(Optional.ofNullable(map.get("A1")).map(Object::toString).orElse(null));
            this.setFdXseCw(Optional.ofNullable(map.get("A8")).map(Object::toString).orElse(null));
            this.setFdXslCw(Optional.ofNullable(map.get("A7")).map(Object::toString).orElse(null));
            this.setAvgDjCw(Optional.ofNullable(map.get("A6")).map(Object::toString).orElse(null));
            this.setXsPs(Optional.ofNullable(map.get("A9")).map(Object::toString).orElse(null));
        } catch (Exception e) {
            log.error("", e);
            this.setTotal(-1L);
        }
    }
}
