alter table tg_metrics_dict add column `sort` int NOT NULL DEFAULT '0' COMMENT '排序';
update tg_metrics_dict set sort=id;

alter table tg_template_info add column version int not null default 0 comment '版本';


alter table tg_template_info_snapshot add unique index u_idx(template_id, version);

update tg_template_info set version = 1;

insert into tg_template_info_snapshot(template_name,process_id,col_attr,join_table_attr,base_table_id,base_table_name,join_json,cols_json,metrics_json,data_range_json,status,used_times,creator,updater,create_time,update_time,period_field,period_type,sql_build_mode,application_period_field_json,allow_application_period,must_select_fields_json,sort_index,custom_metrics_json,top_setting_json,granularity_json,custom_granularity,template_type,biz_type,custom_ext,dir_id,scheduler_id,temp_comment,version, template_id)
select template_name,process_id,col_attr,join_table_attr,base_table_id,base_table_name,join_json,cols_json,metrics_json,data_range_json,status,used_times,creator,updater,create_time,update_time,period_field,period_type,sql_build_mode,application_period_field_json,allow_application_period,must_select_fields_json,sort_index,custom_metrics_json,top_setting_json,granularity_json,custom_granularity,template_type,biz_type,custom_ext,dir_id,scheduler_id,temp_comment,version, id as template_id from tg_template_info;


alter table tg_application_info add column template_version int not null default 0 comment '模板版本' after template_id;
alter table tg_user_data_assets add column template_version int not null default 0 comment '模板版本' after template_id;


update tg_application_info set template_version = 1;
update tg_user_data_assets set template_version = 1;


CREATE TABLE `tg_template_info_snapshot` (
                                             `id` int unsigned NOT NULL AUTO_INCREMENT,
                                             `template_id` int DEFAULT NULL COMMENT '模板id',
                                             `version` int NOT NULL DEFAULT '0' COMMENT '版本',
                                             `template_name` varchar(100) DEFAULT NULL COMMENT '模板名称',
    `process_id` int unsigned DEFAULT NULL COMMENT '流程ID',
    `col_attr` int unsigned DEFAULT '1' COMMENT '1：自选维度、2：固定维度',
    `join_table_attr` int unsigned DEFAULT '1' COMMENT '1：允许新增、2：不允许新增',
    `base_table_id` int unsigned DEFAULT NULL COMMENT '基础表ID',
    `base_table_name` varchar(100) DEFAULT NULL COMMENT '基础表名',
    `join_json` json DEFAULT NULL COMMENT '关联数据信息(JSON序列化)',
    `cols_json` json DEFAULT NULL COMMENT '维度数据信息(JSON序列化)',
    `metrics_json` json DEFAULT NULL COMMENT '指标数据信息(JSON序列化)',
    `data_range_json` json DEFAULT NULL COMMENT '数据范围信息(JSON序列化)',
    `status` int NOT NULL DEFAULT '1' COMMENT '状态',
    `used_times` varchar(100) NOT NULL DEFAULT '' COMMENT '使用次数',
    `creator` varchar(100) NOT NULL DEFAULT '' COMMENT '创建者',
    `updater` varchar(100) NOT NULL DEFAULT '' COMMENT '更新者',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新日期',
    `period_field` varchar(255) DEFAULT NULL COMMENT '用于指定日期聚合使用的字段',
    `period_type` varchar(255) DEFAULT NULL COMMENT '用于指定日期聚合使用的类型(年/半年度/季度/月份/日)',
    `sql_build_mode` int DEFAULT NULL COMMENT 'SQL构造模式 1 子查询 2 单层SQL',
    `application_period_field_json` json DEFAULT NULL COMMENT '限制申请时 日期聚合使用的字段',
    `allow_application_period` int DEFAULT '1' COMMENT '是否限制申请时 日期聚合使用的字段',
    `must_select_fields_json` json DEFAULT NULL COMMENT '自选维度 必选的字段维度',
    `sort_index` int DEFAULT '0' COMMENT '同一个表单下的排序',
    `custom_metrics_json` json DEFAULT NULL COMMENT '指标库引用',
    `top_setting_json` json DEFAULT NULL,
    `granularity_json` json DEFAULT NULL,
    `custom_granularity` smallint DEFAULT NULL,
    `template_type` varchar(255) DEFAULT NULL,
    `biz_type` varchar(255) DEFAULT NULL,
    `dirId` bigint DEFAULT NULL,
    `schedulerId` bigint DEFAULT NULL,
    `custom_ext` json DEFAULT NULL,
    `dir_id` bigint DEFAULT NULL,
    `scheduler_id` bigint DEFAULT NULL,
    `temp_comment` text COMMENT '说明',
    PRIMARY KEY (`id`),
    UNIQUE KEY `u_idx` (`template_id`,`version`)
    ) ENGINE=InnoDB COMMENT='模板快照表';



CREATE TABLE `tg_user_data_assets_snapshot` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'key',
    `assets_id` bigint(20) NOT NULL COMMENT '资产id',
    `snapshot_type` varchar(64) NOT NULL COMMENT '快照类型',
    `src_application_id` bigint(20) DEFAULT NULL,
    `version` int(11) DEFAULT NULL,
    `assets_type` varchar(255) DEFAULT NULL,
    `asset_table_name` varchar(255) DEFAULT NULL,
    `template_id` bigint(20) DEFAULT NULL,
    `template_version` int(11) NOT NULL DEFAULT '0' COMMENT '模板版本',
    `process_id` bigint(20) DEFAULT NULL,
    `process_version` int(11) DEFAULT NULL,
    `template_name` varchar(255) DEFAULT NULL,
    `table_id` bigint(20) DEFAULT NULL,
    `base_table_id` bigint(20) DEFAULT NULL,
    `base_table_name` varchar(255) DEFAULT NULL,
    `all_table_names` varchar(255) DEFAULT NULL,
    `applicant_id` bigint(20) DEFAULT NULL,
    `applicant_name` varchar(255) DEFAULT NULL,
    `applicant_department` varchar(255) DEFAULT NULL,
    `project_name` varchar(255) DEFAULT NULL,
    `project_desc` varchar(255) DEFAULT NULL,
    `require_attr` int(11) DEFAULT NULL,
    `require_time_type` int(11) DEFAULT NULL,
    `client_names` varchar(255) DEFAULT NULL,
    `contract_no` varchar(255) DEFAULT NULL,
    `readable_users` varchar(255) DEFAULT NULL,
    `readable_user_names` varchar(255) DEFAULT NULL,
    `data_expire` datetime DEFAULT NULL,
    `data_total` bigint(20) DEFAULT NULL,
    `copy` int(11) DEFAULT NULL,
    `copy_from_id` bigint(20) DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator` bigint(20) NOT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updater` bigint(20) NOT NULL COMMENT '更新人',
    `assets_sql` text COMMENT '资产 SQL',
    `apply_last_update_time` timestamp NULL DEFAULT NULL COMMENT '申请更新日期',
    `status` int(11) DEFAULT '1' COMMENT '状态, 0停用, 1正常, 2旧申请(重新申请的旧数据)',
    `first_sync_tag` tinyint(3) unsigned DEFAULT '0' COMMENT '分配客户会读取该值, 默认为0， 0 - 初次分配，需要同步， 1 - 无需同步',
    `need_sync_tag` tinyint(3) unsigned DEFAULT '1' COMMENT '判断是否需要更新同步CK数据至PG， 0 - 需要同步， 1 - 无需同步， 默认1',
    `project_id` bigint(20) DEFAULT NULL COMMENT '项目',
    `template_type` varchar(255) DEFAULT NULL COMMENT '模板类型',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB  COMMENT='用户资产表 快照'



CREATE TABLE `tg_range_preset` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
    `name` varchar(255) DEFAULT NULL COMMENT '预设范围名称',
    `biz_type` varchar(255) DEFAULT NULL COMMENT '业务线',
    `template_id` bigint(20) DEFAULT NULL COMMENT '关联模板id',
    `granularity` varchar(255) DEFAULT NULL COMMENT '字段分类 粒度',
    `template_type` varchar(255) DEFAULT NULL COMMENT '模板类型',
    `range_type` varchar(255) DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator` bigint(20) NOT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updater` bigint(20) NOT NULL COMMENT '更新人',
    `filters` text COMMENT '范围',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB COMMENT='范围预设'


CREATE TABLE `tg_range_template_preset` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'key',
    `name` varchar(255) DEFAULT NULL COMMENT '预设范围名称',
    `biz_type` varchar(255) DEFAULT NULL COMMENT '业务线',
    `template_id` bigint(20) DEFAULT NULL COMMENT '关联模板id',
    `group_list` text COMMENT '自定义列分组',
    `granularity` varchar(255) DEFAULT NULL COMMENT '字段分类 粒度',
    `template_type` varchar(255) DEFAULT NULL COMMENT '模板类型',
    `range_type` varchar(255) DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator` bigint(20) NOT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updater` bigint(20) NOT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB COMMENT='自定义列预设'