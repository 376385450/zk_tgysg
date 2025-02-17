package com.sinohealth.system.biz.application.util;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * @author Kuangcp
 * 2024-10-18 13:58
 */
public class CostTimeUtilTest {

    @Test
    public void testCalcCostMin() throws Exception {
        int result = CostTimeUtil.calcCostMin("2024-10-18 14:00:29");
        Assert.assertEquals(1, result);
    }

    @Test
    public void testCalcCostMin2() throws Exception {
        Assert.assertEquals(Integer.valueOf(12), CostTimeUtil.calcCostMin(new BigDecimal(0.4)));

        // 四舍五入
        Assert.assertEquals(Integer.valueOf(3), CostTimeUtil.calcCostMin(new BigDecimal(0.116)));
        Assert.assertEquals(Integer.valueOf(4), CostTimeUtil.calcCostMin(new BigDecimal(0.117)));
    }
}
