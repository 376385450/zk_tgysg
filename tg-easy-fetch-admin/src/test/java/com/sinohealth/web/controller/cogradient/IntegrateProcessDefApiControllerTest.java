package com.sinohealth.web.controller.cogradient;

import com.sinohealth.DataPlatFormApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author zhangyanping
 * @date 2023/7/4 11:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataPlatFormApplication.class)
@ActiveProfiles("test")
public class IntegrateProcessDefApiControllerTest {

    @Resource
    private IntegrateProcessDefApiController controller;

    @Test
    public void createProcessDefinition() {
        String name = "b2c_jcy_cmh_sales_local";
        Long id =null;
        Long tableId = 10L;
        String processDefinitionJson ="{\"tasks\":[{\"x\":240,\"y\":221,\"id\":\"task-ZQMawefC\",\"itselfType\":1,\"type\":\"DATAX\",\"nodeName\":\"DATAX\",\"isState\":true,\"img\":\"/static/img/datax.5e3a8490.png\",\"name\":\"b2c_jcy_cmh_sales_local\",\"crontab\":\"0 0 * * * ? *\",\"params\":{\"customConfig\":0,\"dsType\":\"CLICKHOUSE\",\"dataSource\":119,\"sql\":\"SELECT years, quarters, months, channel, shop_type2, sort1, sort2, sort3, tym, brand, pm, cj, rights, zx, otc_rx, jx, dx, xsl, xse, create_date\\nFROM b2c_jcy_cmh_sales_shard;\",\"json\":\"\",\"dtType\":\"CLICKHOUSE\",\"dataTarget\":18,\"targetTable\":\"b2c_jcy_cmh_sales_shard\",\"preStatements\":[],\"postStatements\":[\"INSERT INTO tg_sync_info_shard (ck_table_name, ck_sync_state, pg_sync_state, comment, update_time, Sign, Version) VALUES('b2c_jcy_cmh_sales_shard', 1, 0, '', now(), 1, 1)\"],\"localParams\":[{\"prop\":\"\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"\"}],\"jobSpeedByte\":0,\"jobSpeedRecord\":0,\"xms\":1,\"xmx\":1,\"tableId\":10},\"code\":\"\",\"desc\":\"\",\"version\":1,\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"preTasks\":[],\"preTaskNodeList\":[],\"extras\":null,\"depList\":null,\"dependence\":{},\"conditionResult\":{\"successNode\":[],\"failedNode\":[]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"delayTime\":0,\"monitorUrl\":\"\",\"waitStartTimeout\":{}}],\"globalParams\":[],\"tenantId\":1,\"timeout\":0,\"processType\":0}";
        String crontab="{\"startTime\":\"2023-07-04 11:45:15\",\"endTime\":\"2050-11-18 11:45:15\",\"crontab\":\"0 0 * * * ? *\",\"timezoneId\":\"Asia/Shanghai\"}";
        String locations="{\"task-ZQMawefC\":{\"name\":\"b2c_jcy_cmh_sales_local\",\"targetarr\":\"\",\"nodenumber\":0,\"x\":240,\"y\":221}}";
        String connects="[]";
        int releaseState =1;
        controller.createProcessDefinition(name,id,tableId,processDefinitionJson,locations,connects,crontab,releaseState);
    }
}