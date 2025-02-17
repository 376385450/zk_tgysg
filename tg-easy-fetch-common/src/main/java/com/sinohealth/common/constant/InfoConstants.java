package com.sinohealth.common.constant;

/**
 * @Author Rudolph
 * @Date 2022-04-24 13:41
 * @Desc
 */

public class InfoConstants {
    public static final String REQUEST_OK = "请求响应成功";
    public static final String REQUEST_BAD = "请求响应失败";
    public static final String DIRNAME_REQUIREMENT = "请填写目录名";
    public static final String AUDITING = "审核中";
    public static final String AUDIT_PASS = "通过";
    public static final String AUDIT_FIAL = "驳回";
    public static final String PROCESS_ID_REQUIREMENT = "流程ID必填";
    public static final String HANDLE_CHAIN_NOT_EMPTY = "处理链未设置处理节点";
    public static final String UID_REQUIREMENT = "缺少用户信息";
    public static final String DIRNAME_BLANK = "目录名不能为空格";
    public static final String ALLOC_SYNC_TASK_INFO = "数据分配成功, 预估数据同步时间夜间0时";
    public static final String UPDATE_SYNC_TASK_INFO = "更新请求提交成功,预估数据同步时间夜间0时";
    public static final String ALLOC_SUCCESS_INFO = "数据分配成功";
    public static final String NO_NEED_TO_UPDATE_INFO = "数据无需更新";
    public static final String NO_DOC_AUTH = "263-文件权限受限";
    public static final String WAIT_LOADING = "262-预览文件加载中，请稍后重试";
    public static final String CAN_NOT_LOAD_FILE = "264-没有找到对应的文件";

    public static final String SERVICE_OFFLINE = "267-服务下架";
    public static final String EXCEL_LIMIT = "265-数据量已超出Excel数据量限制（" + CommonConstants.EXCEL_EXPORT_SIZE + "），建议缩小数据筛选范围";
    public static final String DATA_EXPIRED = "数据有效期已过,请重新申请";
    public static final String DATA_DISABLE = "数据已被禁用";
    public static final String APPLY_DOC = "申请文档事件";
    public static final String SUCCESSFUL_APPLY_TIMES = "成功申请文档事件";
    public static final String PREVIEW_DOC = "文件预览事件";
    public static final String DOC_PDF_DOWNLOAD = "PDF下载事件";
    public static final String DOC_SRC_DOWNLOAD = "文档源文件下载事件";
    public static final String CAN_NOT_MOVE_FILE_INTO_LOCATION = "无法拖动目标到非目录单位";
    public static final String REFRESH_PAGE = "请刷新[我的数据]再重新进入";
    public static final String DUPLICATED_ASSET_NAME = "资产名称已被占用, 请更换新资产名称";
    public static final String NEED_PROCESS_ID = "请选择审批流程";
    public static final String NEED_TABLE_ID = "表ID缺失, 先创建表, 才能查询并更新表信息";
    public static final String INVALID_PROCESS = "当前审批流无效，请联系资产负责人调整审批流程";
    public static final String UNABLE_DELETE_ASSET = "当前资产已被使用，不允许删除";
}
