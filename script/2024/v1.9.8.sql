alter table tg_application_info add column data_amount int default  1 null comment '需求个数' after remark_files;
alter table tg_application_info add column data_cost decimal(5,2) default  0 null comment '需求成本' after data_amount;
alter table tg_application_info add column data_cost_min int default  0 null comment '需求成本 min' after data_cost;


alter table tg_user_data_assets add column `flow_process_type` varchar(32) DEFAULT NULL after snapshot_type;
alter table tg_user_data_assets_snapshot add column `flow_process_type` varchar(32) DEFAULT NULL after snapshot_type;


alter table tg_assets_flow_auto_batch add column project_name varchar(256) null comment '查询-项目名称';
alter table tg_assets_flow_auto_batch add column `require_time_type` varchar(100) null  COMMENT '1：一次性需求、2：持续性需求' ;
alter table tg_assets_flow_auto_batch add column  `deliver_time_type` varchar(255) null ;
alter table tg_assets_flow_auto_batch add column flow_name varchar(256) null comment '查询-工作流名称';

alter table tg_template_info add column custom_tag tinyint default 0 not null comment '自定义标签' ;
alter table tg_template_info_snapshot add column custom_tag tinyint default 0 not null comment '自定义标签' ;

alter table tg_application_info add column custom_tag tinyint default 0 not null comment '自定义标签' after data_cost_min;
alter table tg_application_info add column tag_project_name varchar(512) null comment '自定义标签-项目名称' after custom_tag;
alter table tg_application_info add column tag_tags varchar(512) null comment '自定义标签-标签' after tag_project_name;
alter table tg_application_info add column tag_client varchar(512) null comment '自定义标签-关联客户' after tag_tags;
alter table tg_application_info add column tag_table_name varchar(512) null comment '自定义标签-关联应用表名' after tag_client;
alter table tg_application_info add column tag_ids varchar(512) null comment '自定义标签-关联id' after tag_table_name;
alter table tg_application_info add column tag_new_field tinyint default 0 not null comment '自定义标签-新增列' after tag_ids;
alter table tg_application_info add column tag_cascade tinyint default 0 not null comment '自定义标签-级联指标' after tag_new_field;


alter table tg_application_task_config add column custom_tag tinyint default 0 not null comment '自定义标签' after data_expir;
alter table tg_application_task_config add column tag_project_name varchar(512) null comment '自定义标签-项目名称' after custom_tag;
alter table tg_application_task_config add column tag_tags varchar(512) null comment '自定义标签-标签' after tag_project_name;
alter table tg_application_task_config add column tag_client varchar(512) null comment '自定义标签-关联客户' after tag_tags;
alter table tg_application_task_config add column tag_table_name varchar(512) null comment '自定义标签-关联应用表名' after tag_client;
alter table tg_application_task_config add column tag_ids varchar(512) null comment '自定义标签-关联id' after tag_table_name;
alter table tg_application_task_config add column tag_new_field tinyint default 0 not null comment '自定义标签-新增列' after tag_ids;
alter table tg_application_task_config add column tag_cascade tinyint default 0 not null comment '自定义标签-级联指标' after tag_new_field;

alter table tg_application_task_config_snapshot add column custom_tag tinyint default 0 not null comment '自定义标签' after data_expir;
alter table tg_application_task_config_snapshot add column tag_project_name varchar(512) null comment '自定义标签-标签' after custom_tag;
alter table tg_application_task_config_snapshot add column tag_tags varchar(512) null comment '自定义标签-项目名称' after tag_project_name;
alter table tg_application_task_config_snapshot add column tag_client varchar(512) null comment '自定义标签-关联客户' after tag_tags;
alter table tg_application_task_config_snapshot add column tag_table_name varchar(512) null comment '自定义标签-关联应用表名' after tag_client;
alter table tg_application_task_config_snapshot add column tag_ids varchar(512) null comment '自定义标签-关联id' after tag_table_name;
alter table tg_application_task_config_snapshot add column tag_new_field tinyint default 0 not null comment '自定义标签-新增列' after tag_ids;
alter table tg_application_task_config_snapshot add column tag_cascade tinyint default 0 not null comment '自定义标签-级联指标' after tag_new_field;
