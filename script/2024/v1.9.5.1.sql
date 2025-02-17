
alter table tg_template_info add column assets_compare tinyint default 0 comment '资产对比' after assets_qc ;
alter table tg_template_info_snapshot add column assets_compare tinyint default 0 comment '资产对比' after assets_qc ;
update tg_template_info set assets_compare = 0 where assets_compare is null;


alter table tg_user_data_assets add column plan_compare tinyint default  0 not null comment '是否预设数据对比';
alter table tg_user_data_assets_snapshot add column plan_compare tinyint default 0 not null comment '是否预设数据对比';

alter table tg_user_data_assets add column flow_detail_id bigint comment '工作流出数明细id';
alter table tg_user_data_assets_snapshot add column flow_detail_id bigint comment '工作流出数明细id';

alter table tg_user_data_assets add column deprecated tinyint default 0 not null comment '作废' after expire_type;
alter table tg_user_data_assets_snapshot add column deprecated tinyint default 0 not null comment '作废' after expire_type;


CREATE TABLE `tg_assets_flow_batch` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '任务名',
  `qc_batch_id` bigint DEFAULT NULL COMMENT 'QC批次id',
  `biz_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '业务线',
  `template_ids` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '模板id',
  `need_qc` tinyint DEFAULT NULL COMMENT '需要QC',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '状态',
  `remark` varchar(512) DEFAULT NULL COMMENT '版本说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` bigint NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `expect_time` datetime DEFAULT NULL,
  `finish_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB   COMMENT='工作流出数批次';

CREATE TABLE `tg_assets_flow_batch_detail` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'key',
  `batch_id` bigint DEFAULT NULL COMMENT '批次id',
  `template_id` bigint DEFAULT NULL COMMENT '模板id',
  `application_id` bigint DEFAULT NULL COMMENT '申请id',
  `project_id` bigint DEFAULT NULL COMMENT '项目id',
  `project_name` varchar(255) DEFAULT NULL COMMENT '项目',
  `applicant_id` bigint DEFAULT NULL COMMENT '申请人id',
  `applicant_name` varchar(255) DEFAULT NULL COMMENT '申请人',
  `qc_batch_id` bigint DEFAULT NULL COMMENT 'QC 批次id',
  `state` varchar(16) DEFAULT NULL COMMENT '状态',
  `workflow_id` int DEFAULT NULL COMMENT '工作流定义id',
  `workflow_name` varchar(255) DEFAULT NULL COMMENT '工作流',
  `data_expire` datetime DEFAULT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_ba_id` (`batch_id`)
) ENGINE=InnoDB  COMMENT='工作流出数批次 明细';


CREATE TABLE `tg_assets_compare_file` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'key',
  `project_name` varchar(255) DEFAULT NULL COMMENT '需求名',
  `prod_code` varchar(255) DEFAULT NULL COMMENT 'prodcode',
  `data_period` varchar(64) DEFAULT NULL COMMENT '新版本期数',
  `new_file_name` varchar(255) DEFAULT NULL COMMENT '新文件',
  `new_path` varchar(255) DEFAULT NULL COMMENT '新路径',
  `old_file_name` varchar(255) DEFAULT NULL COMMENT '旧文件',
  `old_path` varchar(255) DEFAULT NULL COMMENT '旧路径',
  `result_path` varchar(255) DEFAULT NULL COMMENT 'ftp 结果',
  `state` varchar(16) DEFAULT NULL COMMENT '状态',
  `run_log` text,
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` bigint NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='文件比对任务';

CREATE TABLE `tg_assets_compare_select` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'key',
  `auto_assets_id` varchar(255) DEFAULT NULL COMMENT '资产id集合',
  `manual_assets_id` varchar(255) DEFAULT NULL COMMENT '资产id集合',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` bigint NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uidx_creator` (`creator`)
) ENGINE=InnoDB COMMENT='最近合并下载';

alter table tg_assets_flow_batch_detail add index idx_batch(batch_id);
alter table tg_assets_flow_batch_detail add index idx_apply_id(application_id);
alter table tg_assets_compare_file add index idx_prodcode(prod_code);
alter table tg_assets_compare_select add unique index uidx_creator(creator);

