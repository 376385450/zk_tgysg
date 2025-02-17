-- 上线前从开发环境 复制
-- create table tg_application_task_config
-- create table tg_biz_data_dict_define
-- create table tg_biz_data_dict_val
-- create table tg_field_dict
-- create table tg_project
-- create table tg_user_data_assets
-- create table tg_metrics_dict

-- 模版表
CREATE TABLE `tg_data_range_template` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `data_range_config` text COMMENT '数据范围自定义配置（JSON字符串）',
    `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
    `updater` varchar(64) DEFAULT NULL COMMENT '修改人',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4;


--  ALTER
alter table tg_application_info add column granularity_json             json                                            null;
alter table tg_application_info add column top_setting_json             json                                            null;
alter table tg_application_info add column custom_ext                   json                                            null;
alter table tg_application_info add column pm                           varchar(255)                                    null;
alter table tg_application_info add column deliver_delay                int                                             null;
alter table tg_application_info add column deliver_time                 varchar(255)                                    null;
alter table tg_application_info add column deliver_time_type            varchar(255)                                    null;
alter table tg_application_info add column project_id                   bigint                                          null;
alter table tg_application_info add column scheduler_id                 bigint                                          null;
alter table tg_application_info add column apply_remark                 varchar(512)                                          null;
alter table tg_application_info add column expect_time                  datetime                                        null;
alter table tg_application_info add column assets_id                    bigint                                          null comment '数据资产id';
alter table tg_application_info add column config_type                  tinyint(2)                                      null comment '资产配置模式：0(SQL模式) 1(工作流模式)';
alter table tg_application_info add column config_sql                   text                                            null comment '资产配置的SQL';
alter table tg_application_info add column workflow_id                  int                                             null comment '资产配置的工作流ID';
alter table tg_application_info add column expect_delivery_time         datetime                                        null;
alter table tg_application_info add column assets_create_time           datetime                                        null;


alter table tg_template_info add column top_setting_json              json                                      null;
alter table tg_template_info add column granularity_json              json                                      null;
alter table tg_template_info add column template_type                 varchar(255)                              null;
alter table tg_template_info add column biz_type                      varchar(255)                              null;
alter table tg_template_info add column custom_ext                    json                                      null;
alter table tg_template_info add column dir_id                        bigint                                    null;
alter table tg_template_info add column scheduler_id                  bigint                                    null;
alter table tg_template_info add column custom_granularity            smallint                                  null;
alter table tg_template_info add column temp_comment                  text                                      null comment '说明';


-- 添加资产配置相关字段

alter table tg_deliver_email_record add column assets_id bigint null comment '数据资产id';
alter table tg_data_description add column assets_id bigint null comment '数据资产id';
alter table arkbi_analysis add column assets_id tinytext     null comment '资产ID,仪表板可能为多值,逗号分隔';

alter table tg_application_data_update_record add column assets_id bigint null comment '数据资产id';
alter table tg_application_data_update_record add column cause           text                               null comment '异常原因';


alter table table_field_info add column relation_col_id bigint null;
alter table table_info add  biz_type varchar(255) null comment '';
alter table table_info add column process_id int unsigned null comment '流程ID';

alter table tg_deliver_customer_record add assets_id          bigint                             null comment '数据资产id';

alter table tg_deliver_download_record add assets_id          bigint                             null comment '数据资产id';
alter table tg_deliver_email_record add assets_id          bigint                             null comment '数据资产id';
alter table tg_table_application_mapping_info add assets_id          bigint                             null comment '数据资产id';

alter table tg_application_info modify  template_metrics_json json null comment '维度数据信息(JSON序列化)';
alter table tg_application_info modify apply_data_range_info_json json null comment '申请数据范围信息(JSON序列化)';

alter table tg_customer_apply_auth add assets_id               bigint                             null comment '数据资产id';

alter table tg_audit_process_info add index idx_process_id (process_id);
alter table tg_data_description drop application_id;


-- 索引
alter table  arkbi_analysis add index idx_user (create_by);
alter table tg_node_mapping add index idx_node_id_icon(node_id,icon);
alter table tg_application_data_update_record add index idx_apply_id(application_id);


alter table table_info add index idx_diy(is_diy);

alter table tg_doc_info add index idx_name(name);
alter table tg_application_info
    add enable_scheduled_task boolean null comment '是否启用定时任务';

alter table tg_application_info
    add cron varchar(64) null comment '定时任务cron表达式';

alter table tg_application_task_config
    add  zdy_member text    null comment '自定义会员' after  zdy_product;

alter table tg_application_task_config
    add  zdy_member text    null comment '自定义会员' after  zdy_product;
alter table tg_application_task_config_snapshot
    add  zdy_member text    null comment '自定义会员' after  zdy_product;

alter table tg_application_info
    add config_sql_workflow_id int null comment '配置SQL生成的工作流ID';
