-- 20211110
-- 自定义组件
create table integrate_customize_component
(
    id           bigint auto_increment
        primary key,
    name         varchar(255)  not null comment '组件名称',
    package_name varchar(255)  null comment '组件包名称',
    package_path varchar(255)  null comment '组件包路径',
    size         bigint        null comment '大小',
    `desc`       varchar(1000) null comment '描述',
    create_by    bigint        not null comment '创建人ID',
    create_time  datetime      not null comment '创建时间',
    update_by    bigint        null comment '更新人ID',
    update_time  datetime      null comment '更新时间',
    status       int           not null comment '状态：0删除，1正常，2停用'
)
    comment '自定义组件';

-- 20211124 集成源
-- 集成源
create table integrate_source
(
    id                bigint auto_increment comment 'ID'
        primary key,
    parent_id         bigint       not null comment '上级集成源ID，第一级为0',
    name              varchar(255) not null comment '集成源名称',
    http_url          text         not null comment 'HTTP地址',
    `desc`            text         null comment '描述',
    principal_id      bigint       null comment '负责人ID',
    frequency_num     int          null comment '集成频率',
    frequency_type    varchar(255) null comment '集成频率类型，year年，month月，day日，hour小时',
    num               int          null comment '集成次数',
    related_table_id  text         null comment '关联表单ID，多个逗号分隔',
    related_table_ids text         null comment '预留关联表单ID，多个逗号分隔',
    create_by         bigint       null comment '创建人ID',
    create_time       datetime     null comment '创建时间',
    update_by         bigint       null comment '更新人ID',
    update_time       datetime     null comment '更新时间',
    status            int          null comment '状态：0删除，1正常，2停用'
)
    comment '集成源';

-- 数据源
alter table datasource_config add name varchar(255) null comment '数据源名称' after id;
alter table datasource_config add create_by bigint null comment '创建人ID' after password;
alter table datasource_config add update_by bigint null comment '更新人ID' after create_time;
alter table datasource_config add update_time datetime null comment '更新时间' after update_by;
alter table datasource_config add status int default 1 not null comment '状态：0删除，1正常，2停用' after update_time;
alter table datasource_config add remark varchar(1024) null comment '描述';

