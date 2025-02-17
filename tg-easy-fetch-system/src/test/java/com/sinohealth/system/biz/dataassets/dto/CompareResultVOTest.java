package com.sinohealth.system.biz.dataassets.dto;

import org.junit.Test;

import static com.sinohealth.system.biz.dataassets.dto.compare.CompareResultVO.extractNum;
import static com.sinohealth.system.biz.dataassets.dto.compare.CompareResultVO.findDiffIndex;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-10-23 17:09
 */
public class CompareResultVOTest {


    @Test
    public void testSub() throws Exception {
        // 四舍五入
        assertThat(extractNum("1.4999499999", 4), equalTo("1.4999"));
        assertThat(extractNum("348574660.85659822669", 4), equalTo("348574660.8566"));
        assertThat(extractNum("1.4999999999", 4), equalTo("1.5000"));

        assertThat(extractNum("1.6393141093718866", 4), equalTo("1.6393"));
        assertThat(extractNum("1.6393", 4), equalTo("1.6393"));
        assertThat(extractNum("1.639", 4), equalTo("1.6390"));
        assertThat(extractNum("1.63", 4), equalTo("1.6300"));
        assertThat(extractNum("1.60", 4), equalTo("1.6000"));
        assertThat(extractNum("1", 4), equalTo("1.0000"));
    }

    @Test
    public void testDiffIndex() throws Exception {
        assertThat(findDiffIndex("123.0", "123"), equalTo(0));
        assertThat(findDiffIndex("123.34", "123"), equalTo(-1));
        assertThat(findDiffIndex("123.3", "123"), equalTo(-1));
        assertThat(findDiffIndex("123.3", "120.31"), equalTo(1));
        assertThat(findDiffIndex("123.3", "110.31"), equalTo(2));
        assertThat(findDiffIndex("123.3", "12.31"), equalTo(3));
        assertThat(findDiffIndex("12.3", "12.31"), equalTo(-2));
        assertThat(findDiffIndex("12.3", "12.30"), equalTo(0));

        System.out.println(findDiffIndex("12.123", "12.2345"));
        System.out.println(findDiffIndex("123.123", "12.2345"));

        System.out.println(findDiffIndex(extractNum("348574660.85659822669",4), "348574660.8566"));
        System.out.println(findDiffIndex(extractNum("317868.09399999999994428",4), "317868.0940"));
        System.out.println(findDiffIndex(extractNum("86729.999999999",4), "86730.0000"));
    }

}
