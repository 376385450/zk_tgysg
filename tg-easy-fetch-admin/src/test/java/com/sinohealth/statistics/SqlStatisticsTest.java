package com.sinohealth.statistics;

import com.sinohealth.DataPlatFormApplication;
import com.sinohealth.system.monitor.statistics.impl.TableViewPvSqlStatistics;
import com.sinohealth.system.monitor.statistics.impl.TableViewTrendSqlStatistics;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-13 2:11 下午
 */
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataPlatFormApplication.class)
@ActiveProfiles("dev")
public class SqlStatisticsTest {

    @Test
    public void testList() {
        TableViewTrendSqlStatistics sqlStatistics = new TableViewTrendSqlStatistics(113L);
        List<Map<String, Object>> data = sqlStatistics.getData();
        System.out.println(data);
    }

    @Test
    public void testObject() {
        TableViewPvSqlStatistics sqlStatistics = new TableViewPvSqlStatistics(81L);
        Integer data = sqlStatistics.getData();
        System.out.println(data);
    }

}
