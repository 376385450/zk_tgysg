package com.sinohealth.system.util;

import com.sinohealth.system.dto.analysis.FilterDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-03-14 19:25
 */
@Slf4j
public class HistoryApplyUtilTest {

    public static final String url = "http://192.168.9.155:3000/filterRouter/filter/Api";

    @Test
    public void testConvertFilter() throws Exception {
        FilterDTO productFilter = HistoryApplyUtil.parseSql("period >= '2017-01'and period <= '2024-12'");
        Optional<FilterDTO> filterOpt = HistoryApplyUtil.fillFatherNode(productFilter, url);
        log.info("filter={}", filterOpt);
        assert filterOpt.isPresent();

        String finalSQL = HistoryApplyUtil.buildSql(filterOpt.get());
        assertThat(finalSQL, equalTo("(period >= '2017-01' and period <= '2024-12')"));
    }

    @Test
    public void testInvalid() throws Exception {
        FilterDTO productFilter = HistoryApplyUtil.parseSql("city_co_name in ('上海','重庆','北京','长沙','成都','广州','沈阳','郑州','福州','温州')");
        Optional<FilterDTO> filterOpt = HistoryApplyUtil.fillFatherNode(productFilter, url);
        log.info("filter={}", filterOpt);
        assert filterOpt.isPresent();
        String finalSQL = HistoryApplyUtil.buildSql(filterOpt.get());

        productFilter = HistoryApplyUtil.parseSql("(( prodcode = 'P005' and sort2 = '肠道微生态制剂' ) or  ( prodcode = 'P005' and sort2 = '急性胃肠炎、结肠炎等肠道炎症用药' )  ) and sort3 <> '结肠炎及克罗恩病'");
        filterOpt = HistoryApplyUtil.fillFatherNode(productFilter, url);
        log.info("filter={}", filterOpt);
        assert filterOpt.isPresent();
    }

    @Test
    public void testConvertPeriod() throws Exception {
        String result = HistoryApplyUtil.convertCommonPeriod("period >= '2017-01'and  period <= '2024-12'", true);
        System.out.println(result);


        System.out.println(HistoryApplyUtil.convertCommonPeriod("period_new >= '2018-02' and period_new <= '2024-12'", true));
    }
}
