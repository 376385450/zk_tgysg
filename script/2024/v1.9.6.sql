
UPDATE table_field_info SET data_type = 'Int64' WHERE table_id = 10054 and field_name = 'std_id';

alter table tg_assets_wide_upgrade_trigger add column apply_id bigint null comment '申请id' after assets_id;

CREATE TABLE `tg_table_info_snapshot_compare` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'key',
  `table_id` bigint NOT NULL COMMENT '表id',
  `biz_id` bigint DEFAULT NULL COMMENT '业务编号',
  `plan_id` bigint DEFAULT NULL COMMENT '计划编号',
  `new_version_id` bigint NOT NULL COMMENT '新版id',
  `new_version` int DEFAULT NULL COMMENT '新版本号',
  `new_period` varchar(64) DEFAULT NULL COMMENT '新周期',
  `new_version_period` varchar(64) NOT NULL COMMENT '新版期数名称',
  `old_version_id` bigint NOT NULL COMMENT '旧版id',
  `old_version` int DEFAULT NULL COMMENT '旧版本号',
  `old_period` varchar(64) DEFAULT NULL COMMENT '旧周期',
  `old_version_period` varchar(64) NOT NULL COMMENT '旧版期数名称',
  `state` varchar(16) DEFAULT NULL COMMENT '状态',
  `result_state` varchar(16) DEFAULT NULL COMMENT '结果表状态',
  `fail_reason` text COMMENT '失败原因',
  `callback_url` varchar(512) DEFAULT NULL COMMENT '回调地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `creator` bigint NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB   COMMENT='库表快照比对表';




alter table tg_assets_pb_push_batch_detail add column   `application_id` bigint DEFAULT NULL COMMENT '申请id' after assets_ver;


alter table tg_assets_flow_batch
    add biz_id bigint null comment '关联业务id' after id;
alter table tg_assets_flow_batch add auto_id bigint after biz_id ;

alter table sys_job modify column `job_name` varchar(80) NOT NULL DEFAULT '' COMMENT '任务名称';
alter table sys_job_log modify column `job_name` varchar(80) NOT NULL DEFAULT '' COMMENT '任务名称';


alter table tg_application_task_config add column push_power_bi tinyint;
alter table tg_application_task_config add column active tinyint ;
alter table tg_application_task_config add column data_expir datetime ;

alter table tg_application_task_config_snapshot add column push_power_bi tinyint;
alter table tg_application_task_config_snapshot add column active tinyint;
alter table tg_application_task_config_snapshot add column data_expir datetime ;

update tg_application_task_config c left join tg_application_info a on c.application_id = a.id
set c.push_power_bi = a.push_power_bi,
    c.data_expir    = a.data_expir, c.active = 1;

update tg_application_task_config c left join tg_application_info a on c.application_id = a.id
set  c.active = 0
where a.current_audit_process_status = 6
;

update tg_application_task_config_snapshot c left join tg_application_info a on c.application_id = a.id
set c.push_power_bi = a.push_power_bi,
    c.data_expir    = a.data_expir, c.active = 1;
CREATE TABLE `tg_assets_flow_auto_batch` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(80) DEFAULT NULL COMMENT '任务名',
  `biz_type` varchar(16) DEFAULT NULL COMMENT '业务线',
  `template_ids` varchar(512) DEFAULT NULL COMMENT '模板id',
  `apply_ids` varchar(512) DEFAULT NULL COMMENT '申请id',
  `cron` varchar(512) DEFAULT NULL COMMENT 'cron',
  `job_id` bigint DEFAULT NULL,
  `flow_process_type` varchar(64) DEFAULT NULL COMMENT '版本',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` bigint NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  COMMENT='工作流出数 自动任务';



alter table tg_table_info_snapshot add column create_by bigint default 0;
alter table tg_assets_flow_batch modify column `name` varchar(128) DEFAULT NULL COMMENT '任务名';

update tg_application_info set deliver_time_type = '' where require_time_type = 1;

create table tgysg.cmh_qc_sop_mirror on cluster default_cluster
(
 period character varying(255),
    prodcode String,
    prodname String,
    qc_people String,
    qc_leader String,
    out_date date,
    out_prodcode Nullable(date),
    out_qc date,
    out_bi date,
    out_ly date,
    sop_time Nullable(date),
    update_date DATETIME,
    update_by String,
    remarks String,
    type String,
    qc_expect_out_date date,
    qc_actual_out_date date,
    sop_expect_out_date date,
    sop_actual_out_date date
) engine PostgreSQL('192.168.50.102:5432', 'cmh', 'cmh_qc_sop', 'u_tg_easy_fetch_prod', 'Kdu48*sue31f', 'cmh');


select out_prodcode ,sop_time,*  from cmh_qc_sop_mirror

where upper (type) = 'CMH'  and upper(prodcode)  like 'P%'  and  period ='202404' ;



CREATE TABLE `tg_flow_process_management` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `setting_id` bigint NOT NULL COMMENT '设置id',
  `name` varchar(64) NOT NULL COMMENT '流程名称',
  `period` varchar(64) NOT NULL COMMENT '期数',
  `version_category` varchar(32) NOT NULL COMMENT '版本类型【QC/SOP/交付版本/临时改数版本】',
  `table_asset_id` bigint NOT NULL COMMENT '底表资产编号',
  `table_snapshot_id` bigint DEFAULT NULL COMMENT '表当前快照id',
  `table_snapshot_version` int DEFAULT NULL COMMENT '表当前快照版本',
  `table_asset_name` varchar(64) DEFAULT NULL COMMENT '底表资产表名',
  `template_ids` varchar(512) NOT NULL COMMENT '模板资产ids',
  `template_names` varchar(512) NOT NULL COMMENT '模板资产名称',
  `plan_execution_time` datetime NOT NULL COMMENT '计划执行时间',
  `execution_begin_time` datetime DEFAULT NULL COMMENT '执行开始时间',
  `execution_finish_time` datetime DEFAULT NULL COMMENT '执行完成时间',
  `attach` varchar(512) DEFAULT NULL COMMENT '附加信息',
  `state` varchar(32) NOT NULL COMMENT '状态',
  `sync_state` varchar(32) DEFAULT NULL COMMENT '宽表执行情况',
  `sync_id` varchar(64) DEFAULT NULL COMMENT '尚书台工作流id',
  `work_flow_state` varchar(32) DEFAULT NULL COMMENT '工作流执行状态',
  `assets_update_state` varchar(32) DEFAULT NULL COMMENT '资产升级状态',
  `table_data_compare_state` varchar(32) DEFAULT NULL COMMENT '库表对比执行情况',
  `table_data_compare_biz_id` bigint DEFAULT NULL COMMENT '库表对比对应版本记录',
  `qc_state` varchar(32) DEFAULT NULL COMMENT '项目qc执行情况',
  `plan_compare_state` varchar(32) DEFAULT NULL COMMENT '数据对比执行情况',
  `plan_compare_biz_id` bigint DEFAULT NULL COMMENT '数据对比对应版本记录',
  `push_power_bi_state` varchar(32) DEFAULT NULL COMMENT 'powerBI执行情况',
  `create_category` varchar(64) NOT NULL COMMENT '创建类型【自动、手动】',
  `creator` bigint NOT NULL DEFAULT '0' COMMENT '创建人',
  `creator_name` varchar(128) NOT NULL DEFAULT '系统' COMMENT '创建人名称',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  COMMENT='全流程记录表';


CREATE TABLE `tg_flow_process_setting_base` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) DEFAULT NULL COMMENT '流程名称【空时为系统自动生成】',
  `plan_execution_time` varchar(128) NOT NULL COMMENT '计划执行时间',
  `biz_type` varchar(32) NOT NULL COMMENT '业务类型【业务线】',
  `table_asset_id` bigint NOT NULL COMMENT '底表资产id',
  `model_asset_ids` varchar(512) NOT NULL COMMENT '模板资产ids',
  `category` varchar(16) NOT NULL COMMENT '类型【auto：自动、manual_operation：手动】',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  COMMENT='流程基础配置表'


CREATE TABLE `tg_flow_process_setting_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `base_id` bigint NOT NULL COMMENT '基础设置id',
  `category` varchar(32) NOT NULL COMMENT '配置类别【qc、sop、交付、临时改数】',
  `update_type` varchar(64) NOT NULL COMMENT '更新方式【全量、按品类、首次全量，之后品类】',
  `plan_compare` tinyint NOT NULL DEFAULT '0' COMMENT '是否开启需求数据对比',
  `plan_compare_category` varchar(32) DEFAULT NULL COMMENT '需求数据对比版本类型',
  `table_data_compare` tinyint NOT NULL DEFAULT '0' COMMENT '是否开启底表数据对比',
  `table_data_compare_category` varchar(32) DEFAULT NULL COMMENT '底表数据对比版本类型',
  `assets_qc` tinyint NOT NULL DEFAULT '0' COMMENT '是否开启qc',
  `push_power_bi` tinyint NOT NULL DEFAULT '0' COMMENT '是否推送powerbi',
  `attach` varchar(256) DEFAULT NULL COMMENT '附件信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  COMMENT='流程配置详细表';

UPDATE table_field_info SET data_type = 'Int64' WHERE table_id = 10054 and field_name = 'std_id';



alter table tg_user_data_assets add column `copy_main_id` bigint DEFAULT NULL after copy_from_id;
alter table tg_user_data_assets_snapshot add column `copy_main_id` bigint DEFAULT NULL after copy_from_id;




alter table tg_table_info_snapshot add column flow_process_type varchar(32) after remark;
alter table tg_table_info_snapshot add column prod_codes varchar(255) after flow_process_type;
alter table tg_table_info_snapshot add column update_type varchar(15) after flow_process_type;



alter table tg_assets_flow_batch
    add biz_id bigint null comment '关联业务id' after id;
alter table tg_assets_flow_batch add `flow_process_type` varchar(64) DEFAULT NULL COMMENT '版本'
alter table tg_assets_flow_batch add `period` varchar(64) DEFAULT NULL COMMENT '期数'

alter table tg_assets_wide_upgrade_trigger
    add biz_id bigint null comment '业务关联id' after id;

alter table tg_assets_wide_upgrade_trigger
    add need_qc tinyint null comment '是否需要qc' after state;

alter table tg_assets_wide_upgrade_trigger
    add need_compare tinyint null comment '是否需要数据对比' after need_qc;

alter table tg_assets_qc_batch_detail
    add biz_id bigint null comment '业务关联id' after id;

alter table tg_assets_compare
    add biz_id bigint null comment '业务关联id' after id;



alter table tg_assets_pb_push_batch_detail
    add biz_id bigint null comment '业务关联编号' after id;
alter table tg_table_info_snapshot add biz_id bigint null comment '业务关联编号' after id;



CREATE TABLE `tg_kv_dict` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` varchar(255) DEFAULT NULL COMMENT '名',
  `val` varchar(512) DEFAULT NULL COMMENT '值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='系统配置字典'


insert into tg_kv_dict(name, val)values('flowPeriod','init'),('flowProcessType','init');


CREATE TABLE `tg_flow_process_alert_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category` varchar(32) NOT NULL COMMENT '分类',
  `code` varchar(32) NOT NULL COMMENT '编码',
  `name` varchar(64) DEFAULT NULL COMMENT '配置名称',
  `success_alert_switch` tinyint NOT NULL DEFAULT '0' COMMENT '成功告警开关',
  `success_web_hook` varchar(256) DEFAULT NULL COMMENT '成功告警webhook',
  `success_member_numbers` varchar(256) DEFAULT NULL COMMENT '成功告警群成员手机号',
  `success_alert_title` varchar(64) DEFAULT NULL COMMENT '成功告警标题',
  `success_alert_content` varchar(256) DEFAULT NULL COMMENT '成功告警内容',
  `fail_alert_switch` tinyint NOT NULL DEFAULT '0' COMMENT '失败告警开光',
  `fail_web_hook` varchar(256) DEFAULT NULL COMMENT '失败告警webhook',
  `fail_member_numbers` varchar(256) DEFAULT NULL COMMENT '失败告警群成员手机号',
  `fail_alert_title` varchar(64) DEFAULT NULL COMMENT '失败告警标题',
  `fail_alert_content` varchar(256) DEFAULT NULL COMMENT '失败告警内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  COMMENT='全流程告警设置';


alter table tg_assets_pb_push_batch
    add biz_id bigint null comment '关联业务id' after id;

alter table tg_assets_qc_batch
    add biz_id bigint null comment '业务关联id' after id;

CREATE TABLE `tg_flow_process_error_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_id` bigint NOT NULL COMMENT '业务id',
  `category` varchar(32) NOT NULL COMMENT '业务类型',
  `error_msg` text NOT NULL COMMENT '异常描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='全流程异常记录表';


INSERT INTO tg_kv_dict (name, val, create_time, update_time) VALUES ('syncTable-cmh_fd_data_sku_shard', '4056', '2024-08-16 13:45:05', '2024-08-16 13:45:07');

-- 初始化分区表
CREATE TABLE qa_tgysg_dev.cmh_fd_data_sku_swap_local on cluster default_cluster
(
    `zone_name` String COMMENT '区域类型',
    `period_year` String COMMENT '年份',
    `period` Date COMMENT '时间',
    `province` String COMMENT '上级区域',
    `city_co_name` String COMMENT '区域',
    `std_id` Int64 COMMENT '产品ID',
    `prodcode` String COMMENT '品类',
    `brand` String COMMENT '品牌',
    `sort1` String COMMENT '分类一',
    `sort2` String COMMENT '分类二',
    `sort3` String COMMENT '分类三',
    `sort4` String COMMENT '分类四',
    `otc_rx` String COMMENT '处方性质',
    `zx` String COMMENT '中西药属性',
    `jx` String COMMENT '剂型',
    `dx` String COMMENT '对象',
    `tym` String COMMENT '通用名',
    `spm` String COMMENT '商品名',
    `pm_all` String COMMENT '品名(含属性)',
    `pm` String COMMENT '品名',
    `cj` String COMMENT '厂家',
    `gg` String COMMENT '规格',
    `company_rights` String COMMENT '集团权益',
    `short_cj` String COMMENT '简写厂家',
    `short_brand` String COMMENT '简写品牌',
    `otherstag` Int32 COMMENT '长尾标识',
    `is_cs` Int16 COMMENT '对外区域数据',
    `sc_old_label` Nullable(String) COMMENT '标签变动(修改前)',
    `sample_xse` Decimal(30, 11) COMMENT '样本销售额',
    `fd_xse` Decimal(30, 11) COMMENT '放大销售额',
    `fd_xsl` Decimal(30, 11) COMMENT '放大销售量',
    `avg_dj` Nullable(Decimal(30, 11)) COMMENT '平均单价',
    `xs_ps` Nullable(Decimal(30, 11)) COMMENT '销售片数',
    `sz_phl` Nullable(Decimal(30, 11)) COMMENT '铺货率',
    `jq_phl` Nullable(Decimal(30, 11)) COMMENT '加权铺货率',
    `ddu` Nullable(Decimal(30, 11)) COMMENT '累计可服用天数',
    `tv` Nullable(Decimal(30, 11)) COMMENT '装量',
    `vpd` Nullable(Decimal(30, 11)) COMMENT '日服用量',
    `zzts` Nullable(Decimal(30, 2)) COMMENT '周转天数指标',
    `zzts_status` String COMMENT '周转天数指标 状态',
    `fdch` Nullable(Decimal(30, 11)) COMMENT '放大存货',
    `fd_jqxsl` Nullable(Decimal(30, 11)) COMMENT '放大销售量(加权)',
    `avg_jqdj` Nullable(Decimal(30, 11)) COMMENT '加权单价',
    `jq_ddu` Nullable(Decimal(30, 11)) COMMENT '累计可服用天数(加权)',
    `jq_xsps` Nullable(Decimal(30, 11)) COMMENT '销售片数(加权)'
    )
    ENGINE = ReplicatedMergeTree('/clickhouse/table/{shard}/qa_tgysg_dev/cmh_fd_data_sku_local_2024_08_26_18_30_01', '{replica}')
    partition by prodcode
    ORDER BY (zone_name, period, province, city_co_name, prodcode)
    SETTINGS index_granularity = 8192;

CREATE TABLE qa_tgysg_dev.cmh_fd_data_sku_swap_shard on cluster default_cluster
(
    `zone_name` String COMMENT '区域类型',
    `period_year` String COMMENT '年份',
    `period` Date COMMENT '时间',
    `province` String COMMENT '上级区域',
    `city_co_name` String COMMENT '区域',
    `std_id` Int64 COMMENT '产品ID',
    `prodcode` String COMMENT '品类',
    `brand` String COMMENT '品牌',
    `sort1` String COMMENT '分类一',
    `sort2` String COMMENT '分类二',
    `sort3` String COMMENT '分类三',
    `sort4` String COMMENT '分类四',
    `otc_rx` String COMMENT '处方性质',
    `zx` String COMMENT '中西药属性',
    `jx` String COMMENT '剂型',
    `dx` String COMMENT '对象',
    `tym` String COMMENT '通用名',
    `spm` String COMMENT '商品名',
    `pm_all` String COMMENT '品名(含属性)',
    `pm` String COMMENT '品名',
    `cj` String COMMENT '厂家',
    `gg` String COMMENT '规格',
    `company_rights` String COMMENT '集团权益',
    `short_cj` String COMMENT '简写厂家',
    `short_brand` String COMMENT '简写品牌',
    `otherstag` Int32 COMMENT '长尾标识',
    `is_cs` Int16 COMMENT '对外区域数据',
    `sc_old_label` Nullable(String) COMMENT '标签变动(修改前)',
    `sample_xse` Decimal(30, 11) COMMENT '样本销售额',
    `fd_xse` Decimal(30, 11) COMMENT '放大销售额',
    `fd_xsl` Decimal(30, 11) COMMENT '放大销售量',
    `avg_dj` Nullable(Decimal(30, 11)) COMMENT '平均单价',
    `xs_ps` Nullable(Decimal(30, 11)) COMMENT '销售片数',
    `sz_phl` Nullable(Decimal(30, 11)) COMMENT '铺货率',
    `jq_phl` Nullable(Decimal(30, 11)) COMMENT '加权铺货率',
    `ddu` Nullable(Decimal(30, 11)) COMMENT '累计可服用天数',
    `tv` Nullable(Decimal(30, 11)) COMMENT '装量',
    `vpd` Nullable(Decimal(30, 11)) COMMENT '日服用量',
    `zzts` Nullable(Decimal(30, 2)) COMMENT '周转天数指标',
    `zzts_status` String COMMENT '周转天数指标 状态',
    `fdch` Nullable(Decimal(30, 11)) COMMENT '放大存货',
    `fd_jqxsl` Nullable(Decimal(30, 11)) COMMENT '放大销售量(加权)',
    `avg_jqdj` Nullable(Decimal(30, 11)) COMMENT '加权单价',
    `jq_ddu` Nullable(Decimal(30, 11)) COMMENT '累计可服用天数(加权)',
    `jq_xsps` Nullable(Decimal(30, 11)) COMMENT '销售片数(加权)'
    )
    ENGINE = Distributed('default_cluster', 'qa_tgysg_dev', 'cmh_fd_data_sku_swap_local', rand());

insert into cmh_fd_data_sku_swap_shard
select * from cmh_fd_data_sku_shard;

rename table cmh_fd_data_sku_local to cmh_fd_data_sku_20240826_local on cluster default_cluster;
rename table cmh_fd_data_sku_swap_local to cmh_fd_data_sku_local on cluster default_cluster;
drop table cmh_fd_data_sku_swap_shard on cluster default_cluster;

-- 初始化全流程记录
INSERT INTO tg_flow_process_management (biz_type, setting_id, name, period, version_category,
                                        table_asset_id, table_snapshot_id, table_snapshot_version,
                                        table_asset_name, template_ids, template_names,
                                        plan_execution_time, execution_begin_time,
                                        execution_finish_time, attach, state, sync_state, sync_id,
                                        work_flow_state, assets_update_state,
                                        table_data_compare_state, table_data_compare_biz_id,
                                        qc_state, plan_compare_state, plan_compare_biz_id,
                                        push_power_bi_state, create_category, creator, creator_name,
                                        create_time, update_time, deleted)
VALUES ('cmh', 1, '初始流程记录', '202406', 'deliver', 3, 80, 12, 'cmh_fd_data_sku_local', '37', 'CMH_城市_通用',
        '2024-08-26 07:00:00', '2024-08-26 07:00:00', '2024-08-26 09:00:00',
        '',
        'success', 'success', null, 'success', 'success', 'success', 146, 'success', 'success', 146, 'success',
        'auto', 0, '系统', '2024-08-26 15:07:56', '2024-08-26 15:07:56', 0);

CREATE TABLE `tg_flow_process_check` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'key',
  `period` varchar(32) DEFAULT NULL COMMENT '期数',
  `qc_prodcode` varchar(255) DEFAULT NULL COMMENT '变化品类',
  `process_type` varchar(255) DEFAULT NULL COMMENT '主流程类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  COMMENT='主流程 DQC扫描记录';

insert into tg_flow_process_check(period, qc_prodcode, process_type) values ('202406', '', 'deliver');
