-- 20211104 表字段映射信息
create table table_mapping
(
    id                bigint auto_increment comment 'ID'
        primary key,
    table_id          bigint   not null comment '源表ID',
    field_id          bigint   not null comment '源字段ID',
    relation_dir_id   bigint   not null comment '关联表数据目录ID',
    relation_table_id bigint   not null comment '关联表ID',
    relation_field_id bigint   not null comment '关联表字段ID',
    mapping_field_id  bigint   not null comment '映射字段ID',
    create_user_id    bigint   not null comment '创建人ID',
    create_time       datetime not null comment '创建时间',
    update_user_id    bigint   null comment '更新人ID',
    update_time       datetime null comment '更新时间',
    status            int      not null comment '状态：0删除，1正常，2停用'
)
    comment '表字段映射信息';

-- 新增字段
alter table table_field_info add sort int default 0 not null comment '字段排序，从小到大';


