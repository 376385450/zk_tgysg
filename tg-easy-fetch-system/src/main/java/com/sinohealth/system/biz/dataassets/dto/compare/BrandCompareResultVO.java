package com.sinohealth.system.biz.dataassets.dto.compare;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-12-20 13:57
 */
@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BrandCompareResultVO extends AbsCompareResultVO {

    private BrandAssetsCompareVO gp;

    private BrandAssetsCompareVO ck;

    @Override
    Long getGpTotal() {
        return gp.getTotal();
    }

    @Override
    Long getCkTotal() {
        return ck.getTotal();
    }

    public List<String> colsDiff() {
        this.result = true;
        this.msg = "";
        if (diffTotal()) {
            this.result = false;
        }

        return Stream.of(
                this.compareDecimal("SzPhl", gp::getSzPhl, ck::getSzPhl, 6),
                this.compareDecimal("JqPhl", gp::getJqPhl, ck::getJqPhl, 6),

                this.compareDecimal("FdXse", gp::getFdXse, ck::getFdXse, 4),
                this.compareDecimal("FdXsl", gp::getFdXsl, ck::getFdXsl, 4),
                this.compareDecimal("AvgDj", gp::getAvgDj, ck::getAvgDj, 4),

                this.compareDecimal("FdXseCw", gp::getFdXseCw, ck::getFdXseCw, 4),
                this.compareDecimal("FdXslCw", gp::getFdXslCw, ck::getFdXslCw, 4),
                this.compareDecimal("AvgDjCw", gp::getAvgDjCw, ck::getAvgDjCw, 4)

//                this.compareDecimal("XsPs", gp::getXsPs, ck::getXsPs, 4)
        ).collect(Collectors.toList());
    }

    public void checkDataDiff() {
        this.result = true;
        this.msg = "";
        if (diffTotal()) {
            msg += String.format("%10s: %s(gp) %s(ck) \n", "Total", gp.getTotal(), ck.getTotal());
            this.result = false;
        }

        this.compareDecimal("SzPhl", gp::getSzPhl, ck::getSzPhl, 6);
        this.compareDecimal("JqPhl", gp::getJqPhl, ck::getJqPhl, 6);

        this.compareDecimal("FdXse", gp::getFdXse, ck::getFdXse, 4);
        this.compareDecimal("FdXsl", gp::getFdXsl, ck::getFdXsl, 4);
        this.compareDecimal("AvgDj", gp::getAvgDj, ck::getAvgDj, 4);

        this.compareDecimal("FdXseCw", gp::getFdXseCw, ck::getFdXseCw, 4);
        this.compareDecimal("FdXslCw", gp::getFdXslCw, ck::getFdXslCw, 4);
        this.compareDecimal("AvgDjCw", gp::getAvgDjCw, ck::getAvgDjCw, 4);

//        this.compareDecimal("XsPs", gp::getXsPs, ck::getXsPs, 4);

    }

}
