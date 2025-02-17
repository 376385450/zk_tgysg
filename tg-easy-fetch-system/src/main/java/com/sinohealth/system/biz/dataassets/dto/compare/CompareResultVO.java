package com.sinohealth.system.biz.dataassets.dto.compare;

import com.sinohealth.common.utils.StringUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-20 15:14
 */
@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CompareResultVO extends AbsCompareResultVO {

    private CompareAssetsVO gp;

    private CompareAssetsVO ck;

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
        List<String> diff = new ArrayList<>();
        diff.add(this.compareDecimal("AvgDj", gp::getAvgDj, ck::getAvgDj, 4));
        diff.add(this.compareDecimal("FdXse", gp::getFdXse, ck::getFdXse, 4));
        diff.add(this.compareDecimal("SzPhl", gp::getSzPhl, ck::getSzPhl, 6));
        diff.add(this.compareDecimal("JqPhl", gp::getJqPhl, ck::getJqPhl, 6));
        diff.add(this.compareDecimal("Xse", gp::getXse, ck::getXse, 4));
        diff.add(this.compareDecimal("Xsl", gp::getXsl, ck::getXsl, 4));
        diff.add(this.compareDecimal("Ddu", gp::getDdu, ck::getDdu, 2));
        diff.add(this.compareDecimal("Tv", gp::getTv, ck::getTv, 2));
        diff.add(this.compareDecimal("Vpd", gp::getVpd, ck::getVpd, 7));
        return diff;
    }

    public void checkDataDiff() {
        this.result = true;
        this.msg = "";
        if (diffTotal()) {
            msg += String.format("%10s: %s(gp) %s(ck) \n", "Total", gp.getTotal(), ck.getTotal());
            this.result = false;
        }

        this.compareDecimal("AvgDj", gp::getAvgDj, ck::getAvgDj, 4);
        this.compareDecimal("FdXse", gp::getFdXse, ck::getFdXse, 4);
        this.compareDecimal("SzPhl", gp::getSzPhl, ck::getSzPhl, 6);
        this.compareDecimal("JqPhl", gp::getJqPhl, ck::getJqPhl, 6);
        this.compareDecimal("Xse", gp::getXse, ck::getXse, 4);
        this.compareDecimal("Xsl", gp::getXsl, ck::getXsl, 4);
        this.compareDecimal("Ddu", gp::getDdu, ck::getDdu, 2);
        this.compareDecimal("Tv", gp::getTv, ck::getTv, 2);
        this.compareDecimal("Vpd", gp::getVpd, ck::getVpd, 7);
    }

    public Map<String, Integer> diffIndex() {
        if (diffTotal()) {
            return Collections.emptyMap();
        }
        Map<String, Integer> result = new HashMap<>();

        int d = findDiffIndex(gp.getAvgDj(), ck.getAvgDj(), 4);
        if (valid(d, 4)) {
            result.put("AvgDj", d);
        }

        d = findDiffIndex(gp.getFdXse(), ck.getFdXse(), 4);
        if (valid(d, 4)) {
            result.put("FdXse", d);
        }
        d = findDiffIndex(gp.getSzPhl(), ck.getSzPhl(), 6);
        if (valid(d, 6)) {
            result.put("SzPhl", d);
        }
        d = findDiffIndex(gp.getJqPhl(), ck.getJqPhl(), 6);
        if (valid(d, 6)) {
            result.put("JqPhl", d);
        }
        d = findDiffIndex(gp.getXse(), ck.getXse(), 4);
        if (valid(d, 4)) {
            result.put("Xse", d);
        }
        d = findDiffIndex(gp.getXsl(), ck.getXsl(), 4);
        if (valid(d, 4)) {
            result.put("Xsl", d);
        }

        d = findDiffIndex(gp.getDdu(), ck.getDdu(), 2);
        if (valid(d, 2)) {
            result.put("Ddu", d);
        }

        d = findDiffIndex(gp.getTv(), ck.getTv(), 2);
        if (valid(d, 2)) {
            result.put("Tv", d);
        }

        d = findDiffIndex(gp.getVpd(), ck.getVpd(), 7);
        if (valid(d, 7)) {
            result.put("Vpd", d);
        }
        return result;
    }

    private static boolean valid(int val, int except) {
        return val > 0 || (val < 0 && -val <= except);
    }

    public static int findDiffIndex(String a, String b, int num) {
        return findDiffIndex(extractNum(a, num), extractNum(b, num));
    }

    public static int findDiffIndex(String a, String b) {
        if (StringUtils.equals(a, b)) {
            return 0;
        }
        if (Objects.isNull(a)) {
            a = "";
        }
        if (Objects.isNull(b)) {
            b = "";
        }
        String[] aPair = a.split("\\.");
        String[] bPair = b.split("\\.");

        String an = aPair[0];
        String bn = bPair[0];

        int length = an.length();
        if (length != bn.length()) {
            return Math.max(length, bn.length());
        }
        for (int i = 0; i < length; i++) {
            if (an.charAt(i) != bn.charAt(i)) {
                return length - i;
            }
        }
        String as;
        if (aPair.length < 2) {
            as = "0";
        } else {
            as = aPair[1];
        }
        String bs;
        if (bPair.length < 2) {
            bs = "0";
        } else {
            bs = bPair[1];
        }

        if (as.length() != bs.length()) {
            int d = Math.abs(as.length() - bs.length());
            if (as.length() < bs.length()) {
                as += StringUtils.repeat("0", d);
            } else {
                bs += StringUtils.repeat("0", d);
            }
        }

        int min = Math.min(as.length(), bs.length());
        for (int i = 0; i < min; i++) {
            if (as.charAt(i) != bs.charAt(i)) {
                return -(i + 1);
            }
        }

        return 0;
    }
}
