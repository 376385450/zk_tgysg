package com.sinohealth.system.biz.dataassets.dto.compare;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-04-12 10:26
 */
@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CmhFlowCompareResultVO extends AbsCompareResultVO {
    private CmhFlowCompareVO gp;
    private CmhFlowCompareVO ck;

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
                this.compareDecimal("DjCw", gp::getDjCw, ck::getDjCw, 4)
        ).collect(Collectors.toList());
    }

}
