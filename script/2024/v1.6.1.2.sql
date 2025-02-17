-- 创建表-数据目录图片信息
create table data_dir_image
(
    id          bigint auto_increment comment 'ID'
        primary key,
    dir_id      bigint        not null comment '数据目录ID',
    image_path  varchar(1024) null comment '图片存储路径',
    remark      text          null comment '备注',
    create_by   bigint        null comment '创建人ID',
    create_time datetime      null comment '创建时间',
    update_by   bigint        null comment '更新人ID',
    update_time datetime      null comment '更新时间',
    status      int default 1 not null comment '状态：0删除，1正常，2停用'
)
    comment '数据目录图片信息';

-- 表图片信息
create table table_info_image
(
    id          bigint auto_increment comment 'ID'
        primary key,
    table_id    bigint        not null comment '表ID',
    image_path  varchar(1024) null comment '图片存储路径',
    remark      text          null comment '备注',
    create_by   bigint        null comment '创建人ID',
    create_time datetime      null comment '创建时间',
    update_by   bigint        null comment '更新人ID',
    update_time datetime      null comment '更新时间',
    status      int default 1 not null comment '状态：0删除，1正常，2停用'
)
    comment '表图片信息';



