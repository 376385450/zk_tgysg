package com.sinohealth.common.core.redis;

import cn.hutool.core.lang.UUID;
import com.sinohealth.common.utils.ip.IpUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * @author kuangchengping@sinohealth.cn 2022-12-20 11:32
 */
@Slf4j
public abstract class RedisKeys {

    public static String PREFIX = "tg-fetch" + Optional.ofNullable(System.getenv("SPRING_PROFILES_ACTIVE"))
            .filter(v -> Objects.equals(v, "gray")).map(v -> "-" + v + ":").orElse(":");
    /**
     * 天宫登录用户redis key
     */
    public static String TG_LOGIN_TOKEN_KEY = PREFIX + "tg_login_tokens:";
    public static String SORT_MODIFY_LOCK = PREFIX + "sort_modify_lock";

    /**
     * 资产下载锁
     */
    public static String ASSETS_DOWNLOAD_LOCK = PREFIX + "assets_download_lock";

    public static final String CACHE_PREFIX = PREFIX + "cache:";

    public interface Cache {
        String ASSET_TREE = "assetTree";
        String STATISTICS = "statistics";
    }

    /**
     * 登录记录
     */
    public static String SYS_LOGIN_LOCK = "sys_login_lock:%s:%s";
    /**
     * 任务中心 使用环境变量区分本地和开发或测试环境，避免抢任务
     */
    private static String TASK_PREFIX =
            PREFIX + "task:" + Optional.ofNullable(System.getenv("dev_local_flag")).map(v -> v + ":").orElse("");
    public static String TASK_LOCK = TASK_PREFIX + "lock";
    public static String TASK_QUEUE = TASK_PREFIX + "queue";
    public static String TASK_EXECUTING = TASK_PREFIX + "executing";
    public static String TASK_FAILED = TASK_PREFIX + "failed";
    public static String TASK_COUNT = TASK_PREFIX + "count";
    public static String TASK_ID_GENERATOR = TASK_PREFIX + "id_generator";

    public static String getAddApplicationKey(Long userId) {
        return PREFIX + "application:add:" + userId;
    }

    public static String getModifyTableLock(String table) {
        return PREFIX + "table-diy:" + table;
    }

    public static abstract class Home {
        public static String ALREADY_INIT = PREFIX + "home:fast-entry:init";
    }

    public static abstract class Common {
        /**
         * 防重提交 redis key
         */
        public static String REPEAT_SUBMIT_KEY = PREFIX + "repeat_submit:";
    }

    public static abstract class Assets {
        private static final String P = PREFIX + "assets:";

        public static String QC_BATCH_LOCK = P + "qc-batch-lock";
        public static String QC_BATCH_FLOW_LOCK = P + "qc-batch-lock";

        public static String TABLE_BACKUP_LOCK = P + "tab-backup-lock";

        public static String UPGRADE_WIDE_LOCK = P + "upgrade-wide-lock";
        // public static String UPGRADE_WIDE_TASK_LOCK = PREFIX + "upgrade-wide-task-lock";
        public static String UPGRADE_WIDE_TASK_CON = P + "upgrade-wide-task-con";
        public static String UPGRADE_WIDE_TASK_FLAG = P + "upgrade-wide-task-flag";

        public static String COMPARE_REPEAT_RUN = P + "compare-repeat";
        /**
         * 对用户屏蔽异步处理流程
         * <p>
         * SET：assetsVersion
         */
        public static String SYNC_HANDLE_CACHE = PREFIX + "sync-cache";

        public static String getMarkApplyKey(Long applyId) {
            return P + "mark:upgrade-apply:" + applyId;
        }
    }

    public static abstract class Ftp {
        private static final String P = PREFIX + "ftp:";
        public static final String BAN_FTP_UPLOAD = P + "ban";
        /**
         * @see com.sinohealth.system.biz.dataassets.helper.UserDataAssetsUploadFtpHelper#scheduleUploadQueue
         */
        public static final String FTP_QUEUE = P + "queue";

        /**
         * 导出模式：block, queue
         */
        public static final String STREAM_MODE = P + "mode";
    }

    /**
     * 工作流
     */
    public static abstract class Workflow {
        private static final String P = PREFIX + "work-flow:";

        /**
         * 创建资产 的 工作流
         */
        public static final String CREATE_ASSETS = P + "create-assets";

        public static String UPGRADE_FLOW_LOCK = P + "upgrade-lock";
        public static String UPGRADE_FLOW_CON = P + "upgrade-con";
        public static String UPGRADE_FLOW_BATCH = P + "upgrade-batch";


        public static String executeApplyLock(Long applyId) {
            return P + "exe-apply:" + applyId;
        }
    }

    /**
     * 数据迁移 宽表
     */
    public static abstract class Apply {
        private static final String P = PREFIX + "apply:";
        private static final String PT = PREFIX + "trans-wide:";

        /**
         * 公共参数 导入模式
         */
        public static final String TRANSFER_MODE = PT + "trans-mode";
        /**
         * 影响 常规 长尾
         */
        public static final String DEBUG_MODE = PT + "debug-mode";

        /**
         * HASH: excel id -> 申请人姓名
         */
        public static final String TRANS_USER_MAP = PT + "trans-user-map";
        /**
         * HASH: excel id -> 项目名
         */
        public static final String TRANS_PRO_MAP = PT + "trans-project-map";
        /**
         * HASH: excel id -> applyId
         */
        public static final String TRANS_APPLY_MAP = PT + "trans-apply-map";
        public static final String TRANS_BRAND_APPLY_MAP = PT + "brand:trans-apply-map";
        public static final String TRANS_TAIL_APPLY_MAP = PT + "tail:trans-apply-map";

        /**
         * SET: 审核通过的申请id
         */
        public static final String AUDIT_APPLY_SET = PT + "trans-apply-set";

        /**
         * HASH: excel id -> obj 校验结果
         *
         * @see com.sinohealth.system.biz.dataassets.dto.CompareResultVO
         */
        public static final String VALIDATE_RESULT_MAP = PT + "validate-map";
        public static final String VALIDATE_TAIL_RESULT_MAP = PT + "tail:validate-map";
        /**
         * @see com.sinohealth.system.biz.dataassets.dto.CompareBrandResultVO
         */
        public static final String VALIDATE_BRAND_RESULT_MAP = PT + "brand:validate-map";
        public static String CLEAN_TEMP_USELESS_KEY = P + "cleanTempUselessField";

        public static String getFieldDictCacheKey(String searchKey) {
            searchKey = UUID.nameUUIDFromBytes(searchKey.getBytes(StandardCharsets.UTF_8)).toString();
            return PREFIX + "application:data_range:dict:" + searchKey;
        }

        public static String getFieldCacheKey(String searchKey) {
            searchKey = UUID.nameUUIDFromBytes(searchKey.getBytes(StandardCharsets.UTF_8)).toString();
            return PREFIX + "application:data_range:" + searchKey;
        }

        public static String createAssetsKey(Long applicationId) {
            return PREFIX + "application:create_assets:" + applicationId;
        }

        public static String previewSqlCacheKey(String md5) {
            return PREFIX + "application:preview:" + md5;
        }
    }

    public static abstract class ApplyForm {
        private static final String P = PREFIX + "form:";

        public static final String EXPIRE_LOCK = P + "expire-lock";
        public static final String WAIT_RUN_LOCK = P + "expire-lock";

    }

    public static abstract class FlowApply {
        private static final String P = PREFIX + "trans-flow:";

        /**
         * ExcelId -> applyId
         */
        public static final String TRANS_APPLY_MAP = P + "apply-map";
        /**
         * HASH: excel id -> obj 校验结果
         *
         * @see com.sinohealth.system.biz.dataassets.dto.compare.CmhFlowCompareResultVO
         */
        public static final String VALIDATE_RESULT_MAP = P + "validate-map";
    }

    /**
     * 宽表 自定义
     */
    public static abstract class CustomApply {
        private static final String P = PREFIX + "trans-custom:";

        /**
         * ExcelId -> applyId
         */
        public static final String TRANS_APPLY_MAP = P + "apply-map";
        /**
         * HASH: excel id -> obj 校验结果
         *
         * @see com.sinohealth.system.biz.dataassets.dto.compare.CmhFlowCompareResultVO
         */
        public static final String VALIDATE_RESULT_MAP = P + "validate-map";
    }

    public static abstract class InCompleteCustomApply {
        private static final String P = PREFIX + "trans-ins-custom:";

        /**
         * ExcelId -> applyId
         */
        public static final String TRANS_APPLY_MAP = P + "apply-map";
        /**
         * HASH: excel id -> obj 校验结果
         *
         * @see com.sinohealth.system.biz.dataassets.dto.compare.CmhFlowCompareResultVO
         */
        public static final String VALIDATE_RESULT_MAP = P + "validate-map";
    }

    public static abstract class NormalCustomApply {
        private static final String P = PREFIX + "trans-nor-custom:";

        /**
         * ExcelId -> applyId
         */
        public static final String TRANS_APPLY_MAP = P + "apply-map";
        /**
         * HASH: excel id -> obj 校验结果
         *
         * @see com.sinohealth.system.biz.dataassets.dto.compare.CmhFlowCompareResultVO
         */
        public static final String VALIDATE_RESULT_MAP = P + "validate-map";
    }

    public static abstract class RangeApply {
        private static final String P = PREFIX + "trans-range:";

        /**
         * ExcelId -> applyId
         */
        public static final String TRANS_APPLY_MAP = P + "apply-map";
        /**
         * HASH: excel id -> obj 校验结果
         *
         * @see com.sinohealth.system.biz.dataassets.dto.compare.CmhFlowCompareResultVO
         */
        public static final String VALIDATE_RESULT_MAP = P + "validate-map";
    }

    public static abstract class Ws {
        /**
         * Map userId -> hostIp
         */
        public static String Router = PREFIX + "ws:router";

        /**
         * List msgJson
         */
        public static String getMsgQueueKey(String host) {
            return PREFIX + "ws:queue-" + host;
        }

        public static String getMsgQueueKey() {
            String hostIp = IpUtils.getHostIp();
            return getMsgQueueKey(hostIp);
        }
    }

    public static abstract class DiffCompare {
        private static final String P = PREFIX + "diff-compare:";

        /**
         * Map userId -> hostIp
         */
        public static String KEY = P + "snapshot:data";
    }

    /**
     * 全流程管理锁信息
     */
    public static abstract class FlowProcess {
        private static final String P = PREFIX + "flowProcess:";

        public static String PROCESS_LOCK_KEY = P + "execute-flowProcess-lock:";
        public static String WAIT_PROCESS_LOCK_KEY = PROCESS_LOCK_KEY + "waitScheduled";
        public static String RUN_PROCESS_LOCK_KEY = PROCESS_LOCK_KEY + "runningScheduled";
        public static String AUTO_PROCESS_LOCK_KEY = PROCESS_LOCK_KEY + "autoScheduled";
        public static String PLAN_LOCK_KEY = P + "plan-lock";
    }

    public static abstract class PowerBi {
        private static final String P = PREFIX + "powerBi:";

        public static String STATE_LOCK_KEY = P + "execute-state-lock";
    }
}
