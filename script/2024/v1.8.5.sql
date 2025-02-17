alter table tg_application_info  add column  `export_project_name` tinyint(1) NOT NULL DEFAULT '0';


alter table tg_application_info  add column data_state varchar(31) not null default 'none' comment '出数状态';
alter table tg_message_record_dim  add column data_state varchar(31) not null default 'none' comment '出数状态';


alter table tg_project_custom_field_dict add column application_id bigint comment '申请id';
alter table tg_project_custom_field_dict add column creator bigint comment '创建人';
update tg_project_custom_field_dict set application_id=-1;
create table application_column_setting
(
    id             bigint auto_increment
        primary key,
    application_id bigint       null,
    filed_name     varchar(255) null comment '英文字段名',
    filed_alias    varchar(255) null comment '官方中文名',
    custom_name    varchar(255) null comment '自定义命名',
    data_type      varchar(255) null,
    primary_key    tinyint(1)   null,
    is_show        tinyint(1)   null,
    sort           int          null,
    range_field    tinyint(1)   null,
    default_show   varchar(10)  null
);


