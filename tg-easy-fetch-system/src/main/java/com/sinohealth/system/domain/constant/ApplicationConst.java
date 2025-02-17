package com.sinohealth.system.domain.constant;

import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.EnumVO;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.domain.ckpg.CkPgJavaDataType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 提数申请 常量
 *
 * @author kuangchengping@sinohealth.cn
 * 2022-11-28 18:58
 */
public interface ApplicationConst {

    int SQL_MODE_INNER_QUERY = 1;

    int SQL_MODE_SINGLE = 2;

    interface FieldName {

        String SOURCE_ID = "source_id";
    }

    /**
     * 我的资产 树结构
     */
    interface AssetsIcon {
        String CUSTOMER = "customer";
        String PROJECT = "project";
        String DATA = CommonConstants.ICON_DATA_ASSETS;
        String FILE = CommonConstants.ICON_FILE_ASSETS;
        String CHART = CommonConstants.ICON_CHART;
        String DASHBOARD = CommonConstants.ICON_DASHBOARD;
    }

    interface BIEditType {
        int EDIT = 1;
        int PREVIEW = 2;
    }

    interface ApplicationType {
        // 模板申请
        String DATA_APPLICATION = "data";

        String DOC_APPLICATION = "doc";
        // 数据申请 原始表
        String TABLE_APPLICATION = "table";

        // 数据交换
        String DATA_SYNC_APPLICATION = "sync";

        List<String> ALL_TYPE = Arrays.asList(DATA_APPLICATION, DOC_APPLICATION);
    }

    interface DirItemType {
        String TEMPLATE_APPLICATION = "template";
        String DATA_APPLICATION = "table";
        String DOC_APPLICATION = "doc";

        List<String> ALL_TYPE = Arrays.asList(DATA_APPLICATION, DOC_APPLICATION);
    }

    enum DirItemTypeEnum {
        DOC(DirItemType.DOC_APPLICATION, "文档"),
        DATA(DirItemType.DATA_APPLICATION, "数据"),
        TEMPLATE(DirItemType.TEMPLATE_APPLICATION, "模板");
        private final String type;
        private final String desc;

        DirItemTypeEnum(String code, String desc) {
            this.type = code;
            this.desc = desc;
        }

        public String getType() {
            return type;
        }

        public String getDesc() {
            return desc;
        }

        public static Optional<DirItemTypeEnum> getByType(String type) {
            return Arrays.stream(values()).filter(v -> Objects.equals(v.getType(), type)).findFirst();
        }

        /**
         * 资产地图 展示顺序
         */
        public static final List<EnumVO<String>> ALL_TYPE = Stream.of(TEMPLATE, DATA, DOC)
                .map(DirItemTypeEnum::toVO).collect(Collectors.toList());

        public EnumVO<String> toVO() {
            return EnumVO.<String>builder().type(this.type).desc(this.desc).build();
        }
    }

    interface ApplyStatus {
        /**
         * 申请数据 启用
         */
        int ENABLE = 1;
        /**
         * 申请数据 禁用
         */
        int DISABLE = 0;
    }

    /**
     * 页面展示状态
     */
    interface ApplyViewStatusType {
        int ENABLE = ApplyStatus.ENABLE;
        int DISABLE = ApplyStatus.DISABLE;
        /**
         * 重新申请审核中
         */
        int AUDITING = 2;
    }

    /**
     * 申请审核状态
     */
    interface AuditStatus {
        /**
         * 草稿
         */
        int DRAFT = 0;
        /**
         * 待审核
         */
        int AUDITING = 1;
        /**
         * 审核通过
         */
        int AUDIT_PASS = 2;
        /**
         * 驳回
         */
        int AUDIT_FAIL = 3;
        int NOT_IN_PROCESS = 4;
        /**
         * 撤销
         */
        int WITHDRAW_APPLICATION = 5;
        /**
         * 已作废
         */
        int INVALID_APPLICATION = 6;

        List<Integer> notShow = Arrays.asList(DRAFT, WITHDRAW_APPLICATION);
    }

    interface ErrorMsg {
        String MEMORY_LIMIT_EXCEEDED = "申请数据量过大，请缩小数据范围再尝试";
        String INVALID_SQL = "数据预览异常(SQL)";

        static String buildCkMsg(Exception e, boolean copy, String originName) {
            if (copy) {
                return "当前主资产【" + originName + "】已发生变更，当前数据无法兼容，请重新另存为，谢谢";
            }

            if (StringUtils.contains(e.getMessage(), "MEMORY_LIMIT_EXCEEDED")) {
                return ApplicationConst.ErrorMsg.MEMORY_LIMIT_EXCEEDED;
            }

            return ApplicationConst.ErrorMsg.INVALID_SQL;
        }

        static String buildCkMsg(Exception e) {
            return buildCkMsg(e, false, null);
        }
    }

    interface PeriodField {
        /**
         * 构造出的新的时间字段，并且列表展示和Excel导出时 隐藏日期格式的时间字段
         */
        String PERIOD_NEW_ALIAS = "时间筛选项";
        String PERIOD_STR_ALIAS = "时间";
        String PERIOD_TYPE_ALIAS = "时间粒度";

        String PERIOD_NEW = "period_new";
        String PERIOD_STR = "period_str";
        String PERIOD_TYPE = "period_type";

        long PERIOD_NEW_ID = -1;
        long PERIOD_STR_ID = -2;
        long PERIOD_TYPE_ID = -3;

        Map<Long, String> idToNameMap = new HashMap<Long, String>() {{
            put(PERIOD_NEW_ID, PERIOD_NEW);
            put(PERIOD_TYPE_ID, PERIOD_TYPE);
            put(PERIOD_STR_ID, PERIOD_STR);
        }};
    }

    enum PeriodFieldEnum {
        PERIOD_NEW(PeriodField.PERIOD_NEW_ID, PeriodField.PERIOD_NEW, PeriodField.PERIOD_NEW_ALIAS, CkPgJavaDataType.DateTime.toString()),
        PERIOD_STR(PeriodField.PERIOD_STR_ID, PeriodField.PERIOD_STR, PeriodField.PERIOD_STR_ALIAS, ApplicationConst.PeriodField.PERIOD_STR),
        PERIOD_TYPE(PeriodField.PERIOD_TYPE_ID, PeriodField.PERIOD_TYPE, PeriodField.PERIOD_TYPE_ALIAS, ApplicationConst.PeriodField.PERIOD_STR),
        ;
        final long id;
        final String name;
        final String alias;
        /**
         * 前端组件类型
         */
        final String viewType;

        PeriodFieldEnum(long id, String name, String alias, String viewType) {
            this.id = id;
            this.name = name;
            this.alias = alias;
            this.viewType = viewType;
        }

        public static PeriodFieldEnum of(Long id) {
            for (PeriodFieldEnum e : values()) {
                if (Objects.equals(e.id, id)) {
                    return e;
                }
            }
            return null;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getAlias() {
            return alias;
        }

        public String getViewType() {
            return viewType;
        }
    }

    interface RequireTimeType {
        /**
         * 一次性
         */
        int ONCE = 1;

        /**
         * 持续性
         */
        int PERSISTENCE = 2;
    }

    enum RequireTimeTypeEnum {

        ONCE(RequireTimeType.ONCE, "一次性"),
        PERSISTENCE(RequireTimeType.PERSISTENCE, "持续性"),
        ;

        private final int code;
        private final String desc;

        RequireTimeTypeEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static final Map<Integer, String> DESC_MAP = new HashMap<Integer, String>() {{
            put(RequireTimeType.ONCE, "一次性");
            put(RequireTimeType.PERSISTENCE, "持续性");
        }};

    }

    interface JoinType {
        int LEFT_OUTER_JOIN = 1;
        int INNER_JOIN = 2;
        int RIGHT_OUTER_JOIN = 3;
        int FULL_OUTER_JOIN = 4;

        String LEFT_OUTER_JOIN_STR = " GLOBAL LEFT OUTER JOIN ";
        String INNER_JOIN_STR = " GLOBAL INNER JOIN ";
        String RIGHT_OUTER_JOIN_STR = " GLOBAL RIGHT OUTER JOIN ";
        String FULL_OUTER_JOIN_STR = " GLOBAL FULL OUTER JOIN ";

        Map<Integer, String> TYPE_MAP = new HashMap<Integer, String>() {{
            put(LEFT_OUTER_JOIN, LEFT_OUTER_JOIN_STR);
            put(INNER_JOIN, INNER_JOIN_STR);
            put(RIGHT_OUTER_JOIN, RIGHT_OUTER_JOIN_STR);
            put(FULL_OUTER_JOIN, FULL_OUTER_JOIN_STR);
        }};
    }

    interface FieldSource {
        /**
         * 指标库
         */
        int METRICS = 1;
        /**
         * 申请 指标
         */
        int APPLY_METRICS = 2;
    }

    interface RangeTemplate {
        long EMPTY_USE = -1L;
    }

    /**
     * 全局需求管理 操作
     * 审核页面 操作
     */
    interface AuditAction {
        /**
         * 下载
         */
        int DOWNLOAD = 1;
        /**
         * 数据预览
         */
        int PREVIEW = 2;
        /**
         * 验收记录
         */
        int ACCEPTANCE_RECORD = 3;
        /**
         * 作废
         */
        int DEPRECATED = 4;

        /**
         * 详情
         */
        int DETAIL = 9;
        /**
         * 执行
         */
        int RUN = 10;
        /**
         * 配置
         */
        int CONFIG = 11;

        // 以上共用（审核页，全局管理）， 以下全局需求管理使用
        /**
         * 完成
         */
        int FINISH = 5;
        /**
         * 暂停
         */
        int PAUSE = 6;
        /**
         * 恢复
         */
        int RESUME = 7;
        /**
         * 提前处理
         */
        int ENTER_RUN = 8;

    }
}
