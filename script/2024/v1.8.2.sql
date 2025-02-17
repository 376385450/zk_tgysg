-- 索引和虚拟字段
alter table tg_node_mapping add index idx_applicant_id(applicant_id);
alter table tg_node_mapping add index idx_node_id(node_id);
alter table tg_acceptance_record add index idx_assets_version(assets_id, version);
alter table tg_acceptance_record add index idx_a_version(assets_version);

alter table tg_acceptance_record add column assets_version varchar(255) generated always as (CONCAT(assets_id,'#', version));


alter table tg_application_data_update_record add column version        int        default 1                        null comment '版本';
alter table tg_application_data_update_record add column assets_version varchar(255) generated always as (CONCAT(assets_id,'#', version));
alter table tg_application_data_update_record add index idx_a_version(assets_version);

create index tg_application_info_new_application_id_index
    on tg_application_info (new_application_id);

create index tg_asset_whitelist_info
    on tg_asset_whitelist_info (asset_id, expiration_date, staff_type, staff_id);

CREATE TABLE `tg_user_data_assets_bi_view` (
                                               `id` int NOT NULL AUTO_INCREMENT,
                                               `view_id` varchar(255) DEFAULT NULL COMMENT 'BI 视图id',
    `assets_id` bigint DEFAULT NULL COMMENT '数据资产id',
    `version` int DEFAULT NULL COMMENT '版本',
    `assets_version` varchar(255) GENERATED ALWAYS AS (concat(`assets_id`,_utf8mb4'#',`version`)) VIRTUAL,
    `data_state` varchar(8) DEFAULT 'normal',
    PRIMARY KEY (`id`),
    KEY `idx_a_version` (`assets_version`)
    ) ENGINE=InnoDB COMMENT='资产-BI视图 映射表';

CREATE TABLE `tg_user_file_assets` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'key',
                                       `name` varchar(511) NOT NULL,
    `path` varchar(511) NOT NULL,
    `pdf_path` varchar(511) DEFAULT NULL,
    `dir_id` int DEFAULT NULL COMMENT '目录ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator` bigint NOT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB   COMMENT='用户文件资产表';

INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status,
                     create_by, create_time, update_by, update_time, remark)
VALUES (19, '删除BI过期视图', 'DEFAULT', 'arkBiService.checkDeleteExpireView()', '0 0 2 * * ?', '1', '1',
        '0', 'admin', '2023-10-18 19:35:03', 'admin', '2023-11-15 17:35:22', '');

-- 如果任务不能正常执行，需要手动插入该数据 问题未知
INSERT INTO qrtz_job_details (sched_name, job_name, job_group, description, job_class_name, is_durable,
                              is_nonconcurrent, is_update_data, requests_recovery, job_data)
VALUES ('dataplatformScheduler', 'TASK_CLASS_NAME19', 'DEFAULT', null,
        'com.sinohealth.quartz.util.QuartzDisallowConcurrentExecution', '0', '1', '0', '0',
        0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C7708000000100000000174000F5441534B5F50524F5045525449455373720023636F6D2E73696E6F6865616C74682E71756172747A2E646F6D61696E2E5379734A6F6200000000000000010200084C000A636F6E63757272656E747400124C6A6176612F6C616E672F537472696E673B4C000E63726F6E45787072657373696F6E71007E00094C000C696E766F6B6554617267657471007E00094C00086A6F6247726F757071007E00094C00056A6F6249647400104C6A6176612F6C616E672F4C6F6E673B4C00076A6F624E616D6571007E00094C000D6D697366697265506F6C69637971007E00094C000673746174757371007E00097872002C636F6D2E73696E6F6865616C74682E636F6D6D6F6E2E636F72652E646F6D61696E2E42617365456E7469747900000000000000010200074C0008637265617465427971007E00094C000A63726561746554696D657400104C6A6176612F7574696C2F446174653B4C0006706172616D7371007E00034C000672656D61726B71007E00094C000B73656172636856616C756571007E00094C0008757064617465427971007E00094C000A75706461746554696D6571007E000C787074000561646D696E7372000E6A6176612E7574696C2E44617465686A81014B59741903000078707708000001827C00A7D0787371007E00053F4000000000000077080000001000000000787400007070707400013174000D30203134203134202A202A203F74003564656661756C7453796E6348656C7065722E7570646174654170706C69636174696F6E57686963684E65656432557064617465282974000744454641554C547372000E6A6176612E6C616E672E4C6F6E673B8BE490CC8F23DF0200014A000576616C7565787200106A6176612E6C616E672E4E756D62657286AC951D0B94E08B02000078700000000000000009740012E689ABE68F8FE69BB4E696B0E8A1A8E58D9574000131740001307800);






-- 快速查询

--------------------------------------------- 删除过期快照表
select *  from tg_user_data_assets where expire_type in ('delete', 'delete_data') and asset_table_name is null ;


select * from tg_user_data_assets where expire_type in ('delete', 'delete_data');
select * from tg_user_data_assets_snapshot where expire_type in ('delete', 'delete_data');

select asset_table_name from tg_user_data_assets where expire_type in ('delete');

select count(*) from tg_user_data_assets_snapshot where expire_type in ('delete_data');
select count(*) from tg_user_data_assets where expire_type in ('delete_data');

-- wait
select * from tg_user_data_assets_snapshot where expire_type in ('delete');
select * from tg_user_data_assets where expire_type in ('delete');



select * from tg_user_data_assets_snapshot where expire_type is null;

-- active
select asset_table_name from tg_user_data_assets where expire_type not in ('delete', 'delete_data') or expire_type is null;
select asset_table_name from tg_user_data_assets_snapshot where expire_type not in ('delete', 'delete_data') or expire_type is null;

---------------------------------------------
