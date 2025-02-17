alter table tg_assets_compare_file add column   `deleted` tinyint DEFAULT '0';

-- 修复历史数据关联问题
update  tg_assets_wide_upgrade_trigger t join tg_user_data_assets tuda on t.assets_id = tuda.id set t.apply_id = tuda.src_application_id  where apply_id is null and t.create_time  > '2024-01-01';


CREATE TABLE `tg_flow_process_plan` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'key',
    `period` varchar(16) DEFAULT NULL COMMENT '期数',
  `period_date` date DEFAULT NULL COMMENT '期数',
  `qc_date` date DEFAULT NULL COMMENT 'QC时间',
  `sop_date` date DEFAULT NULL COMMENT 'SOP时间',
  `deliver_date` date DEFAULT NULL COMMENT '交付时间',

    current_period tinyint default 0 comment '当前期数',

  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` bigint NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` bigint NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='全流程排期';

alter table tg_application_info add column assets_attach_json varchar(512) null comment '附件' after config_type;
alter table tg_application_info add column evaluation_result varchar(255) null comment '评估结果';

insert into tg_flow_process_plan(period, period_date, qc_date, sop_date, deliver_date, current_period, creator, updater) values('202407', '2024-07-01', '2024-08-20', '2024-09-02', '2024-09-03', 1, 0, 0);

alter table tg_user_data_assets add column   `config_type` tinyint DEFAULT NULL COMMENT '资产配置模式：0(SQL模式) 1(工作流模式) 2 文件' after update_time;
alter table tg_user_data_assets_snapshot add column   `config_type` tinyint DEFAULT NULL COMMENT '资产配置模式：0(SQL模式) 1(工作流模式) 2 文件' after update_time;

alter table tg_application_info add index idx_audit_page(application_type, copy, all_handlers, create_time);

alter table tg_assets_wide_upgrade_trigger add index idx_table(table_id, act_version);