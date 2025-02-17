package com.sinohealth.system.biz.scheduler.util;

import com.sinohealth.common.enums.ReleaseState;
import com.sinohealth.common.utils.uuid.UUID;
import com.sinohealth.system.biz.scheduler.dto.ProcessDefVO;

/**
 * 生成工作流定义
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-11-05 17:50
 */
public class GenerateProcessUtil {

    private static String getUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(5, 16);
    }

    /**
     * 脚本方式同步（数据同步管理 预先配置） 工作流
     */
    public static ProcessDefVO buildProcessDefForScript(Integer syncTaskId, String syncTaskName, String desc) {
        String uuid = getUuid();
        String taskId = "task-" + uuid;
        String name = syncTaskName + "-" + uuid;
        String def = "{\"globalParams\":[],\"tasks\":[{\"type\":\"DATAX\",\"id\":\"" + taskId + "\",\"name\":\"" + name + "\",\"code\":\"\"" +
                ",\"params\":{\"dsType\":\"MYSQL\",\"dataSource\":\"\",\"dtType\":\"MYSQL\",\"dataTarget\":\"\"," +
                "\"sql\":\"\",\"targetTable\":\"\",\"ysgFlow\":false,\"jobSpeedByte\":0,\"jobSpeedRecord\":1000," +
                "\"preStatements\":[],\"postStatements\":[],\"xms\":1,\"xmx\":1,\"customConfig\":0," +
                "\"scripts\":[{\"name\":\"" + syncTaskName + "\",\"id\":" + syncTaskId + "}],\"customScript\":1},\"desc\":\"" + desc + "\",\"runFlag\":\"NORMAL\"," +
                "\"conditionResult\":{\"successNode\":[],\"failedNode\":[]},\"dependence\":{},\"maxRetryTimes\":\"0\"," +
                "\"retryInterval\":\"1\",\"delayTime\":\"0\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false}," +
                "\"waitStartTimeout\":{},\"taskInstancePriority\":\"MEDIUM\",\"workerGroup\":\"default\",\"monitorUrl\":\"\"," +
                "\"preTasks\":[],\"depList\":null,\"preTaskNodeList\":[]}],\"tenantId\":-1,\"timeout\":0,\"processType\":0}";
        String location = "{\"" + taskId + "\":{\"name\":\"" + name + "\",\"targetarr\":\"\",\"nodenumber\":\"0\",\"x\":325,\"y\":281}}";
        return ProcessDefVO.builder().taskId(uuid).name(name)
                .releaseState(ReleaseState.ONLINE.getCode())
                .processDefinitionJson(def).locations(location)
                .build();
    }
}
