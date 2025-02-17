package com.sinohealth.system.biz.dataassets.dto.compare;

import com.sinohealth.common.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2023-12-21 10:33
 */
@Slf4j
@Data
public abstract class AbsCompareResultVO {

    protected Boolean result;

    protected String msg;

    public static String none = "-";


    abstract Long getGpTotal();

    abstract Long getCkTotal();

    public boolean diffTotal() {
        return !Objects.equals(getGpTotal(), getCkTotal());
    }

    String compareDecimal(String field, Supplier<String> aFunc, Supplier<String> bFunc, int num) {
        BigDecimal ad = new BigDecimal(Optional.ofNullable(aFunc.get()).orElse("0")).setScale(num, RoundingMode.HALF_UP);
        BigDecimal bd = new BigDecimal(Optional.ofNullable(bFunc.get()).orElse("0")).setScale(num, RoundingMode.HALF_UP);
        if (ad.compareTo(bd) != 0) {
            this.result = false;
            msg += String.format("%10s: %s(gp) %s(ck) %s(diff) \n", field, ad, bd, ad.add(bd.negate()));
            return String.format("%s(gp) %s(ck) %s(diff) \n", ad, bd, ad.add(bd.negate()));
        }
        return none;
    }

    public static String extractNum(String no, int num) {
        if (Objects.isNull(no)) {
            return "0";
        }
        if (StringUtils.isBlank(no)) {
            return no;
        }
        if (!no.contains(".")) {
            return no + "." + StringUtils.repeat("0", num);
        }

        String[] pair = no.split("\\.");
        // 小数位对整数位进位
        String scale = subAndPadding(pair[1], num);
        if (Objects.equals(scale, "-1")) {
            return (Integer.parseInt(pair[0]) + 1) + "." + StringUtils.repeat("0", num);
        }
        return pair[0] + "." + scale;
    }

    public static String subAndPadding(String no, int num) {
        int len = no.length();
        if (len == num) {
            return no;
        }
        if (len > num) {
            String in = no.substring(0, num);
            char f = no.charAt(num);
            int fi = Integer.parseInt(f + "");
            if (fi < 5) {
                return in;
            } else {
                BigDecimal fv = new BigDecimal("0." + no.substring(0, num + 1)).setScale(num, RoundingMode.HALF_UP);
                if (fv.compareTo(BigDecimal.ONE) >= 0) {
                    return "-1";
                }
                String fn = fv.toString();
                String suffix = fn.substring(2);
                return suffix + StringUtils.repeat("0", num - suffix.length());
            }
        }
        return no + StringUtils.repeat("0", num - len);
    }
}
