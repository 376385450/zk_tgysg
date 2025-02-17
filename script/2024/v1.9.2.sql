create table tg_asset_link
(
    id        bigint auto_increment,
    link_type int          null comment '1-资产关联 2-操作指引',
    link_url  varchar(500) null,
    link_name varchar(255) null,
    constraint tg_asset_link_pk
        primary key (id)
);

alter table tg_asset_link
    add asset_id bigint null after id;

alter table tg_asset_info
    add guide int null comment '是否开启操作指引 0 否 1是' after shelf_state;

alter table tg_asset_info
    add guide_desc varchar(1024) null comment '指引说明' after guide;

alter table tg_template_info
    add column custom_any_required smallint default 0 comment '自定义设置项任一项必填';
alter table tg_template_info_snapshot
    add column custom_any_required smallint default 0 comment '自定义设置项任一项必填';
update tg_template_info
set custom_any_required = 1
where biz_type = 'small_ticket' and custom_ext!='[]';

alter table tg_application_info modify `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT '更新日期';
alter table tg_application_info
    add column flow_instance_id varchar(31) comment '工作流实例 uuid';

alter table arkbi_analysis modify `update_time` datetime DEFAULT CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT '更新时间';
update arkbi_analysis set update_time = create_time where update_time is null;