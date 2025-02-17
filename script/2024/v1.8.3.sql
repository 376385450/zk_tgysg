create table tg_asset_info_relate
(
    id              int auto_increment,
    asset_id        bigint not null comment '资产id',
    relate_asset_id bigint not null comment '关联资产id',
    constraint tg_asset_info_relate_pk
        primary key (id)
)
    comment '资产关联';


alter table tg_metadata_info
    add `meta_schema` varchar(50) null after datasource;

alter table tg_asset_info_relate
    add relate_sort int default 1 not null;

alter table tg_asset_info
    add `non_audit_asset_open_services_json` json null after asset_open_services_json;

create table if not exists tg_data_sync_application
(
    id                          int auto_increment
        primary key,
    application_id              bigint       null,
    flow_id                     int          null,
    sync_task_id                int          null,
    sync_task_name              varchar(255) null,
    tenant_id                   varchar(255) null,
    sync_task_cron              varchar(50)  null,
    ck_cluster                  varchar(50)  null,
    ck_engine                   varchar(50)  null,
    ck_sort_key                 varchar(50)  not null,
    target_db_type              varchar(50)  null,
    target_data_source_id       int          null,
    target_data_source_database varchar(50)  null,
    target_data_source_schema   varchar(50)  null,
    target_table_name           varchar(50)  null,
    create_target               tinyint(1)   null,
    sync_type                   int          null,
    filter_sql                  varchar(50)  null
);


create table if not exists tg_data_sync_field_config
(
    id                      int auto_increment
        primary key,
    sync_application_id     int          not null,
    source_column_name      varchar(50)  not null,
    source_column_type      int          null,
    source_column_type_name varchar(50)  not null,
    source_column_remark    varchar(255) null,
    target_column_name      varchar(50)  not null,
    target_column_type      int          null,
    target_column_type_name varchar(50)  not null,
    target_column_remark    varchar(255) null
)
    comment '数据交换字段配置';



INSERT INTO sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES (18, '服务过期下线工作流', 'DEFAULT', 'integrateSyncTaskService.offlineExpireFlow()', '0 0 2 * * ?', '1', '1', '0', 'admin', '2023-10-18 19:35:03', 'admin', '2023-11-15 17:35:22', '');


create index tg_asset_user_relation_asset_id_index
    on tg_asset_user_relation (asset_id);


create index tg_application_info_new_asset_id_index
    on tg_application_info (new_asset_id);
