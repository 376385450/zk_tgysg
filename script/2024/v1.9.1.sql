update tg_application_info set require_attr = 5 where require_attr = 4;
update tg_user_data_assets set require_attr = 5 where require_attr = 4;
update tg_user_data_assets_snapshot set require_attr = 5 where require_attr = 4;

-- 回填空数据
update tg_application_info i , tg_project p  set i.require_attr = p.project_type where p.id = project_id and  i.require_attr is null and application_type ='data';
update tg_user_data_assets i , tg_project p  set i.require_attr = p.project_type where p.id = project_id and  i.require_attr is null;
update tg_user_data_assets_snapshot i , tg_project p  set i.require_attr = p.project_type where p.id = project_id and  i.require_attr is null;



alter table tg_application_info drop index idx_apply_type;
alter table tg_application_info add index idx_apply_type(application_type, data_expir, create_time);

alter table tg_application_task_config add top_period_type varchar(31) comment 'TOP 固定 动态 时间';
alter table tg_application_task_config add top_period varchar(255) comment 'TOP 时间';
alter table tg_application_task_config_snapshot add top_period_type varchar(31) comment 'TOP 固定 动态 时间';
alter table tg_application_task_config_snapshot add top_period varchar(255) comment 'TOP 时间';

-- 公告关联资产
create table tg_notice_relate_asset
(
    id        bigint auto_increment
        primary key,
    notice_id int    null,
    asset_id  bigint null
);

-- 旧数据同步
insert into tg_notice_relate_asset (notice_id, asset_id)
select a.id, a.asset_id from tg_notice_info a
where a.asset_id is not null;

create table tg_notice_read
(
    id                  bigint auto_increment
        primary key,
    user_id             bigint        null,
    notice_id           bigint        null,
    biz_type            int           null comment '2. 通知 ',
    application_id      int           null comment '申请id',
    audit_user_Id       int           null comment '审核人id',
    audit_user_has_read int default 0 not null,
    audit_type          int           null comment '1. 审核通过 2. 驳回 3. 已出数',
    data_state          int           null comment 'success',
    version             int           null,
    has_read            int           null comment '是否已读 0未读 1已读',
    create_time         datetime      null
);

create index tg_notice_read_application_id_index
    on tg_notice_read (application_id);

create index tg_notice_read_audit_user_Id_index
    on tg_notice_read (audit_user_Id);

create index tg_notice_read_user_id_index
    on tg_notice_read (user_id);




alter table tg_template_info add column pack_tail tinyint default 0 comment '开启长尾打包';
alter table tg_template_info add column tail_filter_json json comment '长尾条件';
alter table tg_template_info add column tail_fields_json json comment '处理的维度/指标字段';


alter table tg_template_info_snapshot add column pack_tail tinyint default 0 comment '开启长尾打包';
alter table tg_template_info_snapshot add column tail_filter_json json comment '长尾条件';
alter table tg_template_info_snapshot add column tail_fields_json json comment '处理的维度/指标字段';


alter table tg_message_record_dim
    add notice_type int default 1 not null after applicant_name;