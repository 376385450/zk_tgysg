---增加图片地址
ALTER TABLE `data_dict`
    ADD COLUMN `picture_url` text NULL COMMENT '图片地址' AFTER `status`;


SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_statistical_rules
-- ----------------------------
DROP TABLE IF EXISTS `sys_statistical_rules`;
CREATE TABLE `sys_statistical_rules`
(
    `id`                     bigint(20) NOT NULL AUTO_INCREMENT,
    `job_name`               varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL DEFAULT '' COMMENT '任务名称',
    `job_group`              varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
    `job_invoke_target`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
    `job_cron`               varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL DEFAULT '' COMMENT 'cron执行表达式',
    `job_misfire_policy`     varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
    `job_concurrent`         char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
    `status`                 char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1暂停）',
    `statistics_period_type` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务执行周期类型',
    `statistics_time`        varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务执行时间',
    `statistics_type`        char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '统计类型',
    `statistics_describe`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '统计描述',
    `create_by`              varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time`            datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
    `update_by`              varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time`            datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
    `create_user_id`         bigint(20) UNSIGNED NULL DEFAULT 0,
    `update_user_id`         bigint(20) UNSIGNED NULL DEFAULT 0,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '统计规则表' ROW_FORMAT = DYNAMIC;

SET
FOREIGN_KEY_CHECKS = 1;



SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_statistical_result
-- ----------------------------
DROP TABLE IF EXISTS `sys_statistical_result`;
CREATE TABLE `sys_statistical_result`
(
    `id`               bigint(20) NOT NULL AUTO_INCREMENT,
    `statistical_id`   bigint(20) NOT NULL COMMENT '统计任务id',
    `table_id`         bigint(20) UNSIGNED NOT NULL COMMENT '表id',
    `job_name`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '任务名称',
    `job_group`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci  NOT NULL COMMENT '任务组名',
    `invoke_target`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '调用目标字符串',
    `job_message`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '日志信息',
    `status`           char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '执行状态（0失败,1成功,2进行中）',
    `exception_info`   varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '异常信息',
    `table_name`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `table_alias`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
    `total_rows`       bigint(21) NOT NULL COMMENT '行数',
    `table_make`       bigint(21) NOT NULL COMMENT '体量',
    `statistical_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行类型（1自动，2手动）',
    `create_time`      datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
    `create_by`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
    `create_user_id`   bigint(20) UNSIGNED NULL DEFAULT 0,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '统计结果表' ROW_FORMAT = DYNAMIC;

SET
FOREIGN_KEY_CHECKS = 1;



SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_statistical_table
-- ----------------------------
DROP TABLE IF EXISTS `sys_statistical_table`;
CREATE TABLE `sys_statistical_table`
(
    `statistical_id` bigint(20) NOT NULL COMMENT '统计任务id',
    `table_id`       bigint(20) UNSIGNED NOT NULL COMMENT '表id',
    `id`             bigint(20) NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '统计数据库中间表' ROW_FORMAT = Dynamic;

SET
FOREIGN_KEY_CHECKS = 1;


