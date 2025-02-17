DROP TABLE IF EXISTS `table_task`;
CREATE TABLE `table_task`  (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT,
                               `table_id` bigint(20) NOT NULL COMMENT '表id',
                               `dir_id` int(11) NOT NULL COMMENT '数据目录id',
                               `params` json NULL COMMENT '参数',
                               `task_type` smallint(5) NOT NULL COMMENT '任务类型（1导出，2复制）',
                               `speed_of_progress` int(2) NOT NULL COMMENT '进度（0失败,1成功,2进行中）',
                               `create_time` datetime NOT NULL COMMENT '开始时间',
                               `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
                               `operator_id` bigint(11) NOT NULL COMMENT '创建用户id',
                               `operator` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建用户',
                               `remarks` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '任务备注',
                               `content` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务内容',
                               `batch_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务批号',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '任务' ROW_FORMAT = Dynamic;
SET FOREIGN_KEY_CHECKS = 1;

DROP TABLE IF EXISTS `sys_user_api`;
CREATE TABLE `sys_user_api` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                `user_id` bigint(20) unsigned NOT NULL COMMENT '用户id',
                                `api_id` int(11) unsigned NOT NULL COMMENT 'apiID',
                                `is_manage` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否管理(0否|1是)',
                                `is_subscribe` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否订阅(0否|1是)',
                                `create_time` datetime NOT NULL COMMENT '创建时间',
                                `operator_id` bigint(11) NOT NULL COMMENT '创建用户id',
                                `operator` varchar(255) DEFAULT NULL COMMENT '创建用户名',
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE KEY `user_api_only` (`user_id`,`api_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4513 DEFAULT CHARSET=utf8mb4 COMMENT='用户_API服务_关联表';
SET FOREIGN_KEY_CHECKS = 1;


ALTER TABLE `data_platform`.`sys_user` ADD COLUMN `token` varchar(255) NOT NULL COMMENT '用户token' ;

ALTER TABLE `data_platform`.`data_dir` MODIFY COLUMN `status` TINYINT ( 1 ) UNSIGNED NOT NULL DEFAULT '1' COMMENT '0 删除 1 可用 2 停用';

update
    table_info set `status` = 0
WHERE
        table_name IN (
                       "dim_china_region_code_cnt1",
                       "dim_china_region_code_cnt2",
                       "dim_china_region_code_district_1",
                       "dim_china_region_code_key_word_1",
                       "dim_dsc_data_dsc_user_city",
                       "dim_dsc_data_sys_dict",
                       "dim_fic_fic_intelligence_alias_m_t")

ALTER TABLE `data_platform`.`apply` ADD COLUMN `apply_type` int ( 2 ) UNSIGNED NOT NULL DEFAULT 1 COMMENT '申请类型(1表权限审批，2api审批)';
ALTER TABLE `data_platform`.`apply` ADD COLUMN `extend_info` json  COMMENT '扩展信息';

ALTER TABLE `data_platform`.`apply` ADD COLUMN `applicant` varchar ( 100 ) NULL COMMENT '申请人';
ALTER TABLE `data_platform`.`apply` add COLUMN `approved_by` varchar ( 100 ) NULL COMMENT '审批人';


-- 新增审批与api关联表
CREATE TABLE `apply_api` (
                             `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                             `apply_id` int(10) unsigned NOT NULL,
                             `api_id` bigint(20) unsigned NOT NULL,
                             PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4;

-- 新增审批与用户关联表
CREATE TABLE `apply_group` (
                               `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                               `apply_id` int(10) unsigned NOT NULL COMMENT '申请id',
                               `user_id` bigint(20) unsigned NOT NULL COMMENT '审批组组长',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4;

-- 审批关联api订阅信息
CREATE TABLE `apply_subscribe` (
                                   `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
                                   `apply_id` int(10) unsigned NOT NULL COMMENT '审批id',
                                   `subscribe_id` bigint(20) NOT NULL COMMENT '服务订阅api',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COMMENT='审批关联api订阅信息';

ALTER TABLE `data_platform`.`api_invoke_info` ADD COLUMN `invoke_fail_reason` int ( 2 ) UNSIGNED  DEFAULT 0 COMMENT '展示调用失败原因：0成功，1服务器异常，2输入参数有误，3超过最大输入';;

-- 生成token
update sys_user set token = REPLACE(UUID(),"-","") where token is null or token = ''

-- 20210723 增加字段长度
alter table apply modify table_names varchar(2550) not null;
-- 不太建议直接把SQL传返回结果保存到数据库，数据量会很大
alter table api_invoke_info modify return_result_json mediumtext null comment '返回结果集（json格式保存）';

-- 20210727 新增api服务池相关表
DROP TABLE IF EXISTS `api_base_info`;
CREATE TABLE `api_base_info`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `api_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'api服务名称（发布前的草稿信息名称）',
  `api_desc` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口描述',
  `request_method` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方式（GET/POST/PUT/DELETE）',
  `data_source` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据来源   1-数据仓库源   2-自定义sql数据集',
  `table_id` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '数据仓库源表ID，数据源为2时允许有多张表，用json格式保存表ID和注释信息，示例[{\"tableName\":\"表1\",\"id\":\"1\"},{\"tableName\":\"表2\",\"id\":\"2\"}]',
  `table_field_id` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '仓库表ID包含的字段ID（返回字段ID），用json格式保存。示例[{\"fieldName\":\"字段1\",\"id\":\"1\"},{\"fieldName\":\"字段2\",\"id\":\"2\"}]',
  `request_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口访问路径',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `api_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '0-草稿 1-发布（审核中） 2-已上线（审核通过）3-已驳回（审核不通过）\r\n目前这张表的状态只能是草稿状态',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_by` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `request_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'api接口请求参数(暂时用不上)',
  `api_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'api接口类型1-静态 2-动态（参考目标库表的类型）',
  `api_update_frequency` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口更新周期1-每天 2-每月 3-实时变更（参考目标库表的类型）',
  `api_version_out` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'api版本号（由操作用户定义）',
  `api_name_en` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口英文名称',
  `group_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口关联组ID',
  `sql_statement` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'sql语句',
  `request_param_json` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求参数，json格式保存（无实际意义，目前写死2个参数）',
  `return_result_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '返回结果集数据（暂时用不上）',
  `del_status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '删除标志（0-未删除,1-已删除）',
  `create_id` bigint(50) NOT NULL COMMENT '创建人ID',
  `datasource_group_id` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据库组ID，多个用英文逗号分割',
  `datasource_group_name` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据库组名称，多个用英文逗号分割',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 105 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '接口服务基础信息（草稿信息） 表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

DROP TABLE IF EXISTS `api_group_info`;
CREATE TABLE `api_group_info`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组名称',
  `group_desc` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组描述',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `del_status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '删除标志（0-未删除,1-已删除）',
  `create_id` bigint(50) NOT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '分组信息表（给到 api接口基础信息表 关联）' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

DROP TABLE IF EXISTS `api_info_version`;
CREATE TABLE `api_info_version`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `api_info_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联api服务基础信息ID（字段无实际意义）',
  `api_version_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联api发布后的接口服务ID，用来维护api_version值',
  `api_version` bigint(20) NULL DEFAULT NULL COMMENT 'api版本号(内部维护字段)',
  `api_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'api服务名称（发布后的api接口名）',
  `api_desc` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口描述',
  `request_method` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方式',
  `data_source` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据来源   1-数据仓库源   2-自定义sql数据集',
  `table_id` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '数据仓库源表ID，数据源为2时允许有多张表，用json格式保存表ID和注释信息，示例[{\"tableName\":\"表1\",\"id\":\"1\"},{\"tableName\":\"表2\",\"id\":\"2\"}]',
  `table_field_id` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '仓库表ID包含的字段ID（返回字段ID），用json格式保存。示例[{\"fieldName\":\"字段1\",\"id\":\"1\"},{\"fieldName\":\"字段2\",\"id\":\"2\"}]',
  `request_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口访问路径',
  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `api_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '新状态\r\n0、驳回，\r\n1、通过，\r\n2、待审，\r\n3、撤销，\r\n旧状态\r\n1-发布（审核中） 2-已上线（审核通过）3-已驳回（审核不通过）',
  `publish_fail_message` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发布失败的原因',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `request_param` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'api接口请求参数(暂时用不上，默认3个)',
  `api_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'api接口类型1-静态 2-动态（参考表的类型）',
  `api_update_frequency` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口更新周期（1-每天 2-每月 3-实时变更）',
  `api_version_out` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'api版本号（由操作用户定义）',
  `api_name_en` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口英文名称',
  `group_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口关联组ID',
  `sql_statement` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'sql语句',
  `request_param_json` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求参数，json格式保存(（无实际意义，目前写死2个参数）',
  `return_result_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '返回结果集数据（暂时用不上）',
  `info_update_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '接口变更内容（需要和产品讨论）',
  `publish_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '1-新增  2-更新',
  `del_status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '删除标志（0-未删除,1-已删除）',
  `create_id` bigint(50) NOT NULL COMMENT '创建人ID',
  `datasource_group_id` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据库组ID，多个用英文逗号分割',
  `datasource_group_name` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据库组名称，多个用英文逗号分割',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 163 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'api接口基础信息版本关联表（发布后的接口信息表，区分版本号）' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

DROP TABLE IF EXISTS `api_invoke_info`;
CREATE TABLE `api_invoke_info`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '调用用户ID',
  `api_version_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'api发布后的接口ID',
  `request_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数（目前只支持pageSize和pageNum）',
  `return_result_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '返回结果集（json格式保存）',
  `invoke_status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '调用状态（0-失败  1-成功）',
  `invoke_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '调用结果状态码',
  `invoke_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '调用信息描述（失败时，记录调用失败原因）',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `del_status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '删除标志（0-未删除,1-已删除）',
  `execute_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '调用时长（单位 毫秒）',
  `invoke_ip_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '试用方IP地址',
  `invoke_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '0-试用  1-正式调用',
  `create_id` bigint(50) NOT NULL COMMENT '创建人ID',
  `invoke_fail_reason` int(2) UNSIGNED NOT NULL DEFAULT 0 COMMENT '调用状态码：0成功，1服务器异常，2输入参数有误，3超过最大输入',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 536 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'api调用信息表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

DROP TABLE IF EXISTS `api_subscribe_info`;
CREATE TABLE `api_subscribe_info`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `del_status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '0' COMMENT '删除标志（0-未删除,1-已删除）',
  `subscribe_reason` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '申请订阅原因',
  `subscribe_status` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '新状态\r\n0、驳回，\r\n1、通过，\r\n2、待审，\r\n3、撤销，\r\n旧状态\r\n订阅状态：0--订阅成功  1--正在申请订阅   2-订阅失败',
  `subscribe_status_remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '订阅状态 审批备注',
  `api_version_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'api发布后的接口ID',
  `user_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '申请订阅用户ID',
  `create_id` bigint(50) NOT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 482 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'api接口订阅信息表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;



CREATE TABLE `data_platform`.`api_limiting_group`  (
                                                       `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                                       `limiting_id` bigint(20) NOT NULL COMMENT '规则id',
                                                       `api_version_id` bigint(20) NOT NULL COMMENT 'apiId',
                                                       PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 153 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'api与限流规则关联表' ROW_FORMAT = Dynamic;

CREATE TABLE `data_platform`.`api_limiting`  (
                                                 `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                                 `rule_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规则名称',
                                                 `rule_description` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规则说明',
                                                 `rule_type` int(2) NOT NULL COMMENT '限流类型(1接口限流,2ip限流)',
                                                 `status` int(2) NOT NULL COMMENT '规则状态(0禁用，1启用)',
                                                 `limit_rule` int(2) NULL DEFAULT NULL COMMENT '限制规则单位(0不限制,1年,2月,3周,4日,5时,6分,7秒)',
                                                 `limit_frequency` int(10) NULL DEFAULT NULL COMMENT '限制规则/次(类型为0时不限制次数)',
                                                 `limit_ip_rule` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP限流',
                                                 `create_id` bigint(20) NOT NULL COMMENT '创建人id',
                                                 `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '创建人名称',
                                                 `create_time` datetime NOT NULL COMMENT '创建时间',
                                                 `update_time` datetime NULL DEFAULT NULL COMMENT '修改时间',
                                                 `update_id` bigint(20) NULL DEFAULT NULL COMMENT '修改id',
                                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'api限流规则表' ROW_FORMAT = Dynamic;


CREATE TABLE `data_platform`.`sys_carousel`  (
                                                 `id` int(11) NOT NULL AUTO_INCREMENT,
                                                 `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
                                                 `label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签',
                                                 `content` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '内容',
                                                 `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
                                                 `image_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '轮播图链接',
                                                 `sort` int(5) NULL DEFAULT NULL COMMENT '排序',
                                                 `link_type` int(2) NULL DEFAULT NULL COMMENT '链接类型(0非链接,1内链,2外链)',
                                                 `link` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '跳转链接',
                                                 `status` int(2) NULL DEFAULT NULL COMMENT '是否有效(0无效,1有效)',
                                                 `delete_flag` int(2) NULL DEFAULT NULL COMMENT '是否有效(0未删除,1已删除)',
                                                 `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                                 `create_id` bigint(20) NULL DEFAULT NULL COMMENT '创建人id',
                                                 `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人名称',
                                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '轮播信息' ROW_FORMAT = Dynamic;


CREATE TABLE `data_platform`.`sys_partnership`  (
                                                    `id` int(11) NOT NULL AUTO_INCREMENT,
                                                    `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标题',
                                                    `label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签',
                                                    `content` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '内容',
                                                    `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
                                                    `image_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '大图链接',
                                                    `sort` int(5) NULL DEFAULT NULL COMMENT '排序',
                                                    `link_type` int(2) NULL DEFAULT NULL COMMENT '链接类型(0非链接,1内链,2外链)',
                                                    `link` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '跳转链接',
                                                    `status` int(2) NULL DEFAULT NULL COMMENT '是否有效(0无效,1有效)',
                                                    `delete_flag` int(2) NULL DEFAULT NULL COMMENT '是否有效(0未删除,1已删除)',
                                                    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
                                                    `create_id` bigint(20) NULL DEFAULT NULL COMMENT '创建人id',
                                                    `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人名称',
                                                    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '平台服务(合作伙伴)信息' ROW_FORMAT = Dynamic;
