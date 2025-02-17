package com.sinohealth.common.constant;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.*;

/**
 * @Author Rudolph
 * @Date 2022-04-24 15:09
 * @Desc
 */

public class CommonConstants {
    public static final long ROOT = 0;

    public static final String ID = "id";
    public static final String PAGESIZE = "pageSize";
    public static final String PAGENUM = "pageNum";
    public static final String DATA = "data";
    public static final long EXCEL_EXPORT_SIZE = 1048576;
    public static final String SEARCH_TABLE_NAME = "searchTableName";
    public static final String SEARCH_TEMPLATE_NAME = "searchTemplateName";
    public static final String SEARCH_PROCESS_NAME = "searchProcessName";

    public static final String BASE_TABLE_NAME = "base_table_name";
    public static final String TEMPLATE_NAME = "template_name";
    public static final String LINKED_TEMPLATE_NAMES = "linked_template_names";
    public static final String PROCESS_NAME = "process_name";

    public static final int NOT_INCLUDE = 0;
    public static final int INCLUDE = 1;
    public static final int EQ = 2;
    public static final int NE = 3;
    public static final int GT = 4;
    public static final int GE = 5;
    public static final int LT = 6;
    public static final int LE = 7;
    public static final Set<Integer> SUPPORT_HAVING = Sets.newHashSet(EQ, NE, GT, GE, LT, LE);

    public static final Integer OLD_APPLICATION = 2;

    public static final String DOC_DISPLAY_NAME = "文档";
    public static final String DATA_DISPLAY_NAME = "数据";
    public static final String TEMPLATE_DISPLAY_NAME = "模板";
    public static final Map<String, Integer> resouceSortMap = new HashMap<String, Integer>() {{
        put(TEMPLATE_DISPLAY_NAME, 1);
        put(DATA_DISPLAY_NAME, 2);
        put(DOC_DISPLAY_NAME, 3);
    }};
    public static final String BUSINESS_TYPE = "businessType";
    public static final String TOP_NUM = "topNum";
    public static final Integer MOVED = 1;

    public static final String CAN_VIEW_FILE = "阅读文件";
    public static final String CAN_DOWNLOAD_PDF_FILE = "文件PDF下载";
    public static final String CAN_DOWNLOAD_SRC_FILE = "源文件下载";
    public static final String FOLLOW_DIR = "跟随目录";
    public static final String CUSTOMED = "自定义";
    public static final String DIR_ID = "dirId";
    public static final Integer ASSET_MANAGER = 1;
    public static final String SLASH = "/";
    public static final String TABLE_TYPE = "表";
    public static final String META_TYPE = "元数据";
    // 指标常量

    /**
     * @see com.sinohealth.system.dto.application.MetricsInfoDto#computeWay
     */
    public interface ComputeWay {
        @Deprecated
        int CUR = 1;
        int MAX = 2;
        int MIN = 3;
        int SUM = 4;
        int AVG = 5;
        int DIY = 6;
        int COUNT = 7;
        int COUNT_DISTINCT = 8;

        String MAX_STR = "MAX";
        String MIN_STR = "MIN";
        String SUM_STR = "SUM";
        String AVG_STR = "AVG";
        String DIY_STR = "DIY";
        String COUNT_STR = "COUNT";
        String COUNT_DISTINCT_STR = "COUNT_DISTINCT";

        /**
         * 大于此值均为自定义模板表达式
         *
         * @see com.sinohealth.system.domain.CustomFieldTemplate#id
         */
        int CUSTOM_START = 100;
    }

    /**
     * 数据校验时 存于线程上下文 跨多层方法控制逻辑
     * 主要移除指标的round
     */
    public static final String REMOVE_ROUND = "REMOVE_ROUND";
    public static final String TRANSFER = "TRANSFER";


    @Deprecated
    public static final String PHL_AVG = "ROUND(AVG(%s),4) ";
    public static final String ROUND_BLOCK = "ROUND(%s,%s) ";
    public static final String AVG_ROUND_BLOCK = "ROUND(cast(%s as Nullable(Decimal(30,11))),%s) ";

    public enum ComputeWayEnum {
        MAX(ComputeWay.MAX, ComputeWay.MAX_STR, "最大值", "MAX(%s) ", "MAX(%s) "),
        MIN(ComputeWay.MIN, ComputeWay.MIN_STR, "最小值", "MIN(%s) ", "MIN(%s) "),
        SUM(ComputeWay.SUM, ComputeWay.SUM_STR, "求和", "SUM(%s) ", "SUM(%s) "),
        /**
         * 注意精度类型强转问题
         *
         * @see com.sinohealth.system.util.ApplicationSqlUtil#convertCkSqlByDruid
         */
        AVG(ComputeWay.AVG, ComputeWay.AVG_STR, "平均值", "AVG(%s) ", "AVG(%s)"),
        DIY(ComputeWay.DIY, ComputeWay.DIY_STR, "自定义", "%s ", "%s "),
        COUNT(ComputeWay.COUNT, ComputeWay.COUNT_STR, "计数", "COUNT(%s) ", "COUNT(%s) "),
        COUNT_DISTINCT(ComputeWay.COUNT_DISTINCT, ComputeWay.COUNT_DISTINCT_STR, "去重计数",
                "COUNT(DISTINCT %s ) ", "COUNT(DISTINCT %s ) ");

        final int id;
        final String desc;
        final String funcName;
        final String expression;
        final String applyExpression;

        ComputeWayEnum(int id, String funcName, String desc, String expression, String applyExpression) {
            this.id = id;
            this.desc = desc;
            this.funcName = funcName;
            this.expression = expression;
            this.applyExpression = applyExpression;
        }

        /**
         * @param outerLayer   是否最外层SQL
         * @param precisionNum 精度
         */
        public String buildExpression(String fieldName, boolean outerLayer, Integer precisionNum) {
            if (outerLayer) {
                return this.buildApplyPrecisionExpression(fieldName, precisionNum);
            }
            return String.format(this.expression, fieldName);
        }

        /**
         * 申请层做精度处理 降低精度损失
         */
        @Deprecated
        public String buildApplyExpression(String fieldName) {
            if (this.id == ComputeWay.AVG && (fieldName.contains("_phl") || fieldName.contains("铺货率"))) {
                return String.format(PHL_AVG, fieldName);
            }
            return String.format(this.applyExpression, fieldName);
        }

        /**
         * 申请层做精度处理 降低精度损失
         *
         * @param precisionNum null时忽略
         */
        public String buildApplyPrecisionExpression(String fieldName, Integer precisionNum) {
            String expr = String.format(this.applyExpression, fieldName);
            if (this.id == ComputeWay.COUNT || this.id == ComputeWay.COUNT_DISTINCT || Objects.isNull(precisionNum)) {
                return expr;
            }

            // 特殊处理 核对数据的情况，避免误差累积, 并且特殊处理字段又要走round
            String flag = MDC.get(REMOVE_ROUND);
            boolean forceRoundField = fieldName.contains("累计可服用天数") || fieldName.contains(".ddu");
            if (Objects.nonNull(flag) && !forceRoundField) {
                return expr;
            }

            if (StringUtils.containsIgnoreCase(expr, "avg")) {
                return String.format(AVG_ROUND_BLOCK, expr, precisionNum);
            }
            return String.format(ROUND_BLOCK, expr, precisionNum);
        }

        public static Optional<Integer> getByFunc(String func) {
            for (ComputeWayEnum value : values()) {
                if (Objects.equals(value.funcName, func)) {
                    return Optional.of(value.id);
                }
            }
            return Optional.empty();
        }

        public static Optional<ComputeWayEnum> getById(Integer id) {
            for (ComputeWayEnum value : values()) {
                if (Objects.equals(value.id, id)) {
                    return Optional.of(value);
                }
            }
            return Optional.empty();
        }

        public String getDesc() {
            return desc;
        }

        public String getFuncName() {
            return funcName;
        }
    }

    // 数据范围常量

    public static final int IN = 1;
    public static final int NOT_IN = 0;

    public static final int CONDITION = 1;

    public static final String STRTYPE = "string";
    public static final String TIMETYPE = "date|datetime|datetime64";

    public static final int LEFT_OUTER_JOIN = 1;
    public static final int INNER_JOIN = 2;
    public static final int RIGHT_OUTER_JOIN = 3;
    public static final int FULL_OUTER_JOIN = 4;

    public static final String APPLICATION_KEY = "application:data_range:";

    public static final String FIRST_NODE = "1";
    public static final String USER_INFO = "userInfo";
    public static final Object ORG_USER_INFO = "orgUserInfo";
    public static final Object ORG_DEPARTMENT_INFO_LIST = "departments";
    public static final Integer UNHANDLE = 1;
    public static final Integer HANDLED = 2;
    public static final Integer FINISHED = 3;

    public static final Integer INIT_VERSION = 1;
    public static final String DONE = "";

    public static final Integer PUSHED = 2;
    public static final Integer VIEWED = 2;
    public static final Integer NETTY_PORT = 8899;
    public static final String SEARCH_STATUS = "searchStatus";
    public static final String SEARCH_NODE_STATUS = "searchNodeStatus";
    public static final Integer GENERIC = 1;
    public static final Integer NONGENERIC = 2;
    public static final String PROCESS_ID = "process_id";

    public static final Integer POPUPWINDOW = 1;
    public static final Integer MESSAGELIST = 2;
    public static final Integer MESSAGECOUNT = 3;
    public static final int ALL_SELECT = 2;

    public static final Integer EMPLOYEE = 2;
    public static final Integer CLIENT = 3;
    public static final Integer INIT_INDEX = 0;
    public static final String EMAIL = "2";
    public static final String INNER = "1";
    public static final Integer DATA_DIR = 1;
    public static final Integer MY_DATA_DIR = 2;

    /**
     * 文件夹
     */
    public static final String ICON_FILE = "file";

    /**
     * 表单
     */
    public static final String ICON_TABLE = "table";

    /**
     * 模板
     */
    public static final String ICON_TEMPLATE = "template";

    /**
     * 数据资产
     */
    public static final String ICON_DATA_ASSETS = "data_assets";
    /**
     * 文件资产
     */
    public static final String ICON_FILE_ASSETS = "file_assets";

    /**
     * 表单申请
     */
    public static final String ICON_FORM = "form";

    /**
     * 图表分析
     */
    public static final String ICON_CHART = "chart";

    /**
     * 仪表板
     */
    public static final String ICON_DASHBOARD = "dashboard";

    /**
     * 打包类型
     */
    public static final String ICON_PACK = "pack";

    /**
     * 禁用的表单
     */
    @Deprecated
    public static final String ICON_INVALID_FORM = "invalid_form";

    public static final Integer NORMAL = 1;
    public static final Integer ABNORMAL = 0;

    public static final String INSERT = "新增";
    public static final String UPDATE = "修改";

    public static final Integer ASC = 1;
    public static final Integer DESC = 2;

    public static final int TABLE = 0;
    public static final int TEMPLATE = 1;
    public static final int APPLICATION = 2;

    public static final int TABLE_MANAGEMENT = 2;

    public static final String YEAR = "年度";
    public static final String HFYEAR = "半年度";
    public static final String SEASON = "季度";
    public static final String MONTH = "月度";
    public static final String DAYOFMONTH = "日";
    public static final Set<String> TIME_GRANULARITY = Sets.newHashSet(YEAR, HFYEAR, SEASON, MONTH);

    public static final String PDF_VERSION = "pdf";
    public static final String SRC_VERSION = "src";
    public static final String ICON_DOC = "doc";
    public static final String SEARCH_CONTENT = "searchContent";

    public static final int INIT_MAP_SIZE = 16;
    public static final int FILENAME_NORMAL_PART_SIZE = 2;
    public static final int CAN_NOT_LOAD_FILE = 264;
    public static final int NO_DOC_AUTH = 263;
    public static final int WAIT_LOADING = 262;

    public static final int SERVICE_OFFLINE = 267;

    public static final String ADMIN = "管理员";
    public static final String SYSTEM_INIT = "system_init";


    public static final String ARKBI_ACCESS_TOKEN_CACHE_KEY = "arkbi:access_token";
    public static final Integer APPLY_TIMES = 1;
    public static final Integer SUCCESSFUL_APPLY_TIMES = 2;
    public static final Integer READ_TIMES = 3;
    public static final Integer PDF_DOWNLOAD_TIMES = 4;
    public static final Integer SOURCE_FILE_DOWNLOAD_TIMES = 5;
    public static final String CUSTOMER_PREVIEW = "1";
    public static final String CUSTOMER_DOWNLOAD = "2";
    public static final int CAN_NOT_DOWNLOAD_FILE = 266;
    public static final int CAN_NOT_LOAD_AUTH = 265;

    public static String SEARCH_REALNAME = "searchRealName";

    public static final Integer NEVER_ALLOC = 0;
    public static final Integer ALLOC_BEFORE = 1;
    public static final Integer NOT_UPDATE_TASK = 0;
    public static final Integer UPDATING_TASK = 1;
    public static final Integer INNER_UESR = 2;
    public static final Integer CUSTOMER_UESR = 3;

}
