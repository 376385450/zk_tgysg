package com.sinohealth.system.config;

import com.sinohealth.common.utils.uuid.UUID;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import static com.sinohealth.common.core.redis.RedisKeys.TASK_ID_GENERATOR;

@Slf4j
@ToString
public class TaskCreateConfig {


    private String applicationId;
    private String tgTableName;
    private Long tenantId;
    private String taskId3;
    private Long code3;
    private Long tableId;

    private String sourceDbType;
    private String sourceDb;
    private String targetDbType;
    private String targetDb;
    private String sql;


    public TaskCreateConfig(Long tableId, String applicationId, String tgTableName, String tenantId, String sourceDbType, String sourceDb, String targetDbType, String targetDb, String sql, RedisTemplate<String, Object> redisTemplate) {


        this.tableId = tableId;
        this.applicationId = applicationId;
        this.tgTableName = tgTableName;
        this.tenantId = Long.valueOf(tenantId);
        this.taskId3 = "tasks-" + getUuid();
        this.code3 = redisTemplate.opsForValue().increment(TASK_ID_GENERATOR);
        this.sourceDbType = sourceDbType;
        this.sourceDb = sourceDb;
        this.targetDbType = targetDbType;
        this.targetDb = targetDb;
        this.sql = sql;
    }


    public Long getTableId() {
        return tableId;
    }

    public String getName() {
        return "自动创建-" + applicationId + "-" + getUuid();
    }

    public String getUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public String buildLocation() {
//        return "{\"" + taskId1 + "\":{\"name\":\"参数表同步\",\"targetarr\":\"\",\"nodenumber\":\"2\",\"x\":402,\"y\":162},\"" + taskId3 + "\":{\"name\":\"创建资产表\",\"targetarr\":\"" + taskId2 + "\",\"nodenumber\":\"0\",\"x\":817,\"y\":162},\"" + taskId2 + "\":{\"name\":\"获取动态表名\",\"targetarr\":\"" + taskId1 + "\",\"nodenumber\":\"1\",\"x\":611,\"y\":163}}";
        return "{\"" + taskId3 + "\":{\"name\":\"创建资产表\",\"targetarr\":\"\",\"nodenumber\":\"0\",\"x\":402,\"y\":162}}";

    }


    public String buildConnects() {
//        return "[{\"endPointSourceId\":\"" + taskId1 + "\",\"endPointTargetId\":\"" + taskId2 + "\",\"label\":\"\"},{\"endPointSourceId\":\"" + taskId2 + "\",\"endPointTargetId\":\"" + taskId3 + "\",\"label\":\"\"}]";
        return "[]";

    }

//    private String buildTask1() {
//        return "{\"id\":\"" + taskId1 + "\",\"code\":" + code1 + ",\"version\":1,\"name\":\"参数表同步\",\"desc\":\"\",\"type\":\"DATAX\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"dsType\":\"MYSQL\",\"dataSource\":140,\"dtType\":\"POSTGRESQL\",\"dataTarget\":142,\"sql\":\"select\\nproject_no\\n,project_name\\n,application_id\\n,application_no\\n,application_name\\n,project_user\\n,project_unit\\n,project_manager\\n,project_background\\n,project_type\\n,project_htno\\n,project_customer\\n,business_line\\n,project_is_const\\n,valid_date\\n,expect_date\\n,project_update_fre\\n,regular_lead_period\\n,business_block\\n,period_granular\\n,period_data_col\\n,period_scope\\n,area_granular\\n,area_data_col\\n,area_scope\\n,product_granular\\n,product_data_col\\n,product_scope\\n,member_granular\\n,member_data_col\\n,data_kpi\\n,zdy_period\\n,zdy_area\\n,zdy_product\\n,top_kpi\\n,zdy_param\\n,flow_name\\n,create_time\\n,approve_time\\n,update_time\\n,remark\\n,project_status\\nfromtg_application_task_config\\nwhereapplication_id=${application_id}\",\"targetTable\":\"edw.dim_project_auto\",\"jobSpeedByte\":0,\"jobSpeedRecord\":1000,\"preStatements\":[\"deletefromedw.dim_project_autowhereapplication_id=${application_id}\"],\"postStatements\":[],\"xms\":1,\"xmx\":1,\"customConfig\":0,\"channel\":null,\"splitPk\":null,\"localParams\":[],\"ysgFlow\":false},\"preTasks\":[],\"preTaskNodeList\":[],\"extras\":null,\"depList\":null,\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{\"enable\":false,\"strategy\":null,\"interval\":0},\"delayTime\":0,\"monitorUrl\":null,\"channel\":null,\"splitPk\":null,\"waitStartTimeout\":{}},";
//    }
//
//    private String buildTask2() {
//        return "{\"id\":\"" + taskId2 + "\",\"code\":" + code2 + ",\"version\":1,\"name\":\"获取动态表名\",\"desc\":\"\",\"type\":\"SQL\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"type\":\"MYSQL\",\"datasource\":32,\"sql\":\"select 'edw.dim_project_auto' as table_name\",\"udfs\":\"\",\"sqlType\":\"0\",\"sendEmail\":false,\"displayRows\":10,\"limit\":10000,\"title\":\"\",\"groupId\":null,\"localParams\":[{\"prop\":\"table_name\",\"direct\":\"OUT\",\"type\":\"VARCHAR\",\"value\":\"\"}],\"connParams\":\"\",\"preStatements\":[],\"postStatements\":[],\"sendRobot\":false,\"mentionedMobileList\":\"\",\"robotGroupId\":\"\",\"channel\":null,\"splitPk\":null},\"preTasks\":[\"参数表同步\"],\"preTaskNodeList\":[{\"code\":" + code1 + ",\"name\":\"参数表同步\",\"version\":1}],\"extras\":null,\"depList\":null,\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{\"enable\":false,\"strategy\":null,\"interval\":0},\"delayTime\":0,\"monitorUrl\":null,\"channel\":null,\"splitPk\":null},";
//    }

    private String buildTask3() {
        return "{\"id\":\"" + taskId3 + "\",\"code\":" + code3 + ",\"version\":1,\"name\":\"创建资产表\",\"desc\":\"自动化创建资产表\",\"type\":\"DATAX\",\"runFlag\":\"NORMAL\",\"loc\":null,\"maxRetryTimes\":0,\"retryInterval\":1,\"params\":{\"dsType\":\"" + sourceDbType + "\",\"dataSource\":" + sourceDb + ",\"dtType\":\"" + targetDbType + "\",\"dataTarget\":" + targetDb + ",\"sql\":\"" + sql + "\",\"targetTable\":\"${tg_table_name}\",\"ysgFlow\":true,\"jobSpeedByte\":0,\"jobSpeedRecord\":1000,\"preStatements\":[],\"postStatements\":[],\"xms\":1,\"xmx\":1,\"customConfig\":0,\"channel\":null,\"splitPk\":null,\"localParams\":[]},\"preTasks\":[],\"preTaskNodeList\":[],\"extras\":null,\"depList\":null,\"dependence\":{},\"conditionResult\":{\"successNode\":[\"\"],\"failedNode\":[\"\"]},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"delayTime\":0,\"monitorUrl\":null,\"channel\":null,\"splitPk\":null,\"waitStartTimeout\":{}}";
    }

    private String globalParams(String applicationId, String tgTableName) {
        return "[" +
                "{\"prop\":\"application_id\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"" + applicationId + "\"}" +
                ",{\"prop\":\"tg_table_name\",\"direct\":\"IN\",\"type\":\"VARCHAR\",\"value\":\"" + tgTableName + "\"}" +
                ",{ \"prop\": \"trigger_id\", \"direct\": \"IN\", \"type\": \"VARCHAR\", \"value\": \"0\" }" +
                "]";
    }

    public String buildTaskDefineJson() {
        return "{\"globalParams\":" + globalParams(applicationId, tgTableName) +
                ",\"tasks\":[" + buildTask3() + "],\"tenantId\":" + tenantId + ",\"timeout\":0,\"processType\":0}";
    }

}