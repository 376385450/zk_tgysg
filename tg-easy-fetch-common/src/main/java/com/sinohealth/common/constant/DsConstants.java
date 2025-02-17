package com.sinohealth.common.constant;

/**
 * DS接口常量信息
 */
public class DsConstants {

    public static final int MAX_TASK_TIMEOUT = 24 * 3600;

    //项目新增
    public static final String PROJECT_ADD = "projects/create";
    //项目修改
    public static final String PROJECT_EDIT = "projects/update";
    //项目详情
    public static final String PROJECT_DETAIL = "projects/query-by-id";
    //项目删除
    public static final String PROJECT_DELETE = "projects/delete";
    //项目列表
    public static final String PROJECT_LIST = "projects/list-paging";
    //任务状态统计
    public static final String PROJECT_TASK_COUNT = "projects/analysis/task-state-count";
    //统计流程实例状态
    public static final String PROJECT_PROCEE_COUNT = "projects/analysis/process-state-count";
    //统计集成源个数
    public static final String PROJECT_SOURCE_COUNT = "projects/analysis/getSourceIds";
    //个人工作流运行情况
    public static final String PROJECT_PROCESS_STATE = "projects/analysis/queryProcessState";
    //工作流新增
    public static final String PROCESS_DEF_ADD = "projects/{projectName}/process/save";
    //工作流修改
    public static final String PROCESS_DEF_EDIT = "projects/{projectName}/process/update";
    //工作流编辑管理信息
    public static final String PROCESS_DEF_UPDATE = "projects/{projectName}/process/updateIntegrateVal";
    //工作流检查
    public static final String PROCESS_DEF_CHECK = "projects/{projectName}/executors/start-check";
    //工作流复制
    public static final String PROCESS_DEF_COPY = "projects/{projectName}/process/copy";
    //工作流名称检验
    public static final String PROCESS_DEF_VERIFY = "projects/{projectName}/process/verify-name";
    //工作流发布流程定义
    public static final String PROCESS_DEF_RELEASE = "projects/{projectName}/process/release";
    //工作流查询流程定义列表
    public static final String PROCESS_DEF_LIST = "projects/{projectName}/process/list";

    //工作流查询流程定义列表-分页
    public static final String PROCESS_DEF_LIST_PAGE = "projects/{projectName}/process/list-paging";

    //工作流查询流程定义通过流程定义ID
    public static final String PROCESS_DEF_DETAIL = "projects/{projectName}/process/select-by-id";
    //工作流分页查询流程定义列表
    public static final String PROCESS_DEF_PAGE = "projects/{projectName}/process/list-paging";
    //工作流树状图
    public static final String PROCESS_DEF_TREE = "projects/{projectName}/process/view-tree";
    //工作流获得任务节点列表通过流程定义ID
    public static final String PROCESS_DEF_TASK = "projects/{projectName}/process/gen-task-list";
    //工作流获得任务节点列表通过流程定义ID
    public static final String PROCESS_DEF_TASK_IDS = "projects/{projectName}/process/get-task-list";
    //工作流删除
    public static final String PROCESS_DEF_DELETE = "projects/{projectName}/process/delete";
    //工作流导出
    public static final String PROCESS_DEF_EXPORT = "projects/{projectName}/process/export";
    //工作流查询流程定义通过项目ID
    public static final String PROCESS_DEF_PROJECT = "projects/{projectName}/process/queryProcessDefinitionAllByProjectId";
    //运行流程实例
    public static final String PROCESS_DEF_START = "projects/{projectName}/executors/start-process-instance";
    //运行流程实例
    public static final String PROCESS_DEF_SCHE_DETAIL = "projects/{projectName}/executors/get-receiver-cc";
    //创建定时
    public static final String PROCESS_DEF_SCHE_CREATE = "projects/{projectName}/schedule/create";
    //更新定时
    public static final String PROCESS_DEF_SCHE_UPDATE = "projects/{projectName}/schedule/update";
    //定时上线
    public static final String PROCESS_DEF_SCHE_ONLINE = "projects/{projectName}/schedule/online";
    //定时下线
    public static final String PROCESS_DEF_SCHE_OFFLINE = "projects/{projectName}/schedule/offline";
    //分页查询定时
    public static final String PROCESS_DEF_SCHE_PAGE = "projects/{projectName}/schedule/list-paging";
    //查询定时列表
    public static final String PROCESS_DEF_SCHE_LIST = "projects/{projectName}/schedule/list";
    //分页查询定时
    public static final String PROCESS_DEF_SCHE_DELETE = "projects/{projectName}/schedule/delete";
    //定时预览
    public static final String PROCESS_DEF_SCHE_PREVIEW = "projects/{projectName}/schedule/preview";

    //查询流程实例通过流程实例ID
    public static final String PROCESS_INS_DETAIL = "projects/{projectName}/instance/select-by-id";
    //删除流程实例通过流程实例ID
    public static final String PROCESS_INS_DELETE = "projects/{projectName}/instance/delete";
    //查询流程实例列表
    public static final String PROCESS_INS_PAGE = "projects/{projectName}/instance/list-paging";
    public static final String PROCESS_INS_UID_DETAIL = "projects/{projectName}/instance/queryInstanceByUid";
    //查询父流程实例信息通过子流程实例ID
    public static final String PROCESS_INS_PARENT = "projects/{projectName}/instance/select-parent-process";
    //查询子流程实例通过任务实例ID
    public static final String PROCESS_INS_SUB = "projects/{projectName}/instance/select-sub-process";
    //通过流程实例ID查询任务列表
    public static final String PROCESS_INS_TASK = "projects/{projectName}/instance/task-list-by-process-id";
    //更新流程实例
    public static final String PROCESS_INS_UPDATE = "projects/{projectName}/instance/update";
    //浏览Gantt图
    public static final String PROCESS_INS_GANTT = "projects/{projectName}/instance/view-gantt";
    //查询流程实例全局变量和局部变量
    public static final String PROCESS_INS_VAR = "projects/{projectName}/instance/view-variables";
    //执行流程实例的各种操作(暂停、停止、重跑、恢复等)
    public static final String PROCESS_INS_EXECUTR = "projects/{projectName}/executors/execute";

    //任务实例列表
    public static final String PROCESS_TASK_PAGE = "projects/{projectName}/task-instance/list-paging";
    //查询任务实例日志
    public static final String PROCESS_TASK_LOG = "log/detail";
    //下载任务实例日志
    public static final String PROCESS_TASK_LOG_DOWNLOAD = "log/download-log";
    //更新实例状态
    public static final String PROCESS_TASK_STATUS = "/projects/system/updateTaskStatus";

    //创建数据源
    public static final String PROCESS_DATASOURCE_ADD = "datasources/create";
    //更新数据源
    public static final String PROCESS_DATASOURCE_UPDATE = "datasources/update";
    //连接数据源
    public static final String PROCESS_DATASOURCE_CONNECT = "datasources/connect";
    //连接数据源测试
    public static final String PROCESS_DATASOURCE_CONNECT_TEST = "datasources/connect-by-id";
    //创建数据源
    public static final String PROCESS_DATASOURCE_DELETE = "datasources/delete";
    //查询数据源列表通过数据源类型
    public static final String PROCESS_DATASOURCE_LIST = "datasources/list";
    //分页查询数据源列表
    public static final String PROCESS_DATASOURCE_PAGE = "datasources/list-paging";
    //查询数据源通过ID
    public static final String PROCESS_DATASOURCE_DETAIL = "datasources/update-ui";
    //验证数据源
    public static final String PROCESS_DATASOURCE_VERIFY = "datasources/verify-name";
    //查询ds任务实例
    public static final String PROCESS_TASK_DEFID = "projects/{projectName}/task-instance/selectByDefId";

    public static final String PROCESS_TASK_DEFIDS = "projects/{projectName}/task-instance/listPagingByDefId";
    /**
     * 实例列表：抽象任务实例状态
     */
    public static final String PROCESS_TASK_DEFIDS_SIM = "projects/{projectName}/task-instance/listSimPagingByDefId";

    public static final String PROCESS_TASK_STATE = "projects/{projectName}/task-instance/queryStateCnt";

    // 同步任务配置
    public interface SyncTask {

        String CREATE = "data-sync-task/create";

        String UPDATE = "data-sync-task/update";

        String QUERY_LIST = "data-sync-task/query-list";
        /**
         * 数据库 字段类型转换
         */
        String CONVERT_TYPE = "data-sync-task/convert-type";
    }

}
