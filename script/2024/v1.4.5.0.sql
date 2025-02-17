-- 20210901
-- 共享项目
create table analysis_share_project
(
    id                bigint auto_increment comment 'ID'
        primary key,
    project_id        bigint        not null comment '原表项目ID',
    share_name        varchar(255)  not null comment '共享项目名称',
    share_by          bigint        not null comment '共享人ID',
    share_time        datetime      not null comment '共享时间',
    update_share_by   bigint        not null comment '共享更新人ID',
    update_share_time datetime      not null comment '共享更新时间',
    status            int           not null comment '状态：0删除，1正常，2停用',
    share_type        int default 3 null comment '分享类型：1指定人，2指定组，3全部用户',
    share_users       text          null comment '分享用户或者用户组ID，多个逗号分隔'
)
    comment '共享项目';

-- 共享方案
create table analysis_share_solution
(
    id                bigint auto_increment comment 'ID'
        primary key,
    share_project_id  bigint       not null comment '共享项目ID',
    name              varchar(255) not null comment '名称',
    type              varchar(255) not null comment '类型，visual：可视化图表分析方案，zero：零代码分析方案，sql：SQL代码分析方案，python3：PYTHON3代码分析方案',
    `sql`             text         null comment '方案SQL语句',
    data              text         null comment '视图数据',
    remark            varchar(255) null comment '备注',
    share_by          bigint       not null comment '共享人ID',
    share_time        datetime     not null comment '共享时间',
    update_share_by   bigint       not null comment '共享更新人ID',
    update_share_time datetime     not null comment '共享更新时间',
    status            int          not null comment '状态：0删除，1正常，2停用'
)
    comment '共享方案';

-- 共享视图
create table analysis_share_view
(
    id                bigint auto_increment comment 'ID'
        primary key,
    share_solution_id bigint       not null comment '共享项目ID',
    name              varchar(255) not null comment '名称',
    type              varchar(255) not null comment '类型，chart：图表，report：报表，text：文本，url：链接',
    data              text         null comment '视图数据',
    `sql`             text         null comment '方案SQL语句',
    remark            varchar(255) null comment '备注',
    share_by          bigint       not null comment '共享人ID',
    share_time        datetime     not null comment '共享时间',
    update_share_by   bigint       not null comment '共享更新人ID',
    update_share_time datetime     not null comment '共享更新时间',
    status            int          not null comment '状态：0删除，1正常，2停用'
)
    comment '共享视图';


-- 20211013 分析方案添加字段
alter table analysis_solution
    add data text null comment '视图数据' after `sql`;
-- 20211020 分析方案快照添加字段
alter table analysis_solution_snapshot
    add data text null comment '视图数据' after `sql`;

-- 20211014
-- 分析方案模板
create table analysis_solution_template
(
    id             bigint auto_increment comment 'ID'
        primary key,
    name           varchar(255) not null comment '模板名称',
    type           varchar(255) not null comment '类型，visual：可视化图表分析方案，zero：零代码分析方案，sql：SQL代码分析方案，python3：PYTHON3代码分析方案',
    `sql`          text         null comment '方案SQL语句',
    data           text         null comment '视图数据',
    template_desc  varchar(255) null comment '模板描述',
    template_image text         null comment '模板图片',
    create_by      bigint       not null comment '创建人ID',
    create_time    datetime     not null comment '创建时间',
    update_by      bigint       not null comment '更新人ID',
    update_time    datetime     not null comment '更新时间',
    status         int          not null comment '状态：0删除，1正常，2停用'
)
    comment '分析方案模板';

-- 分析视图模板
create table analysis_share_view
(
    id                bigint auto_increment comment 'ID'
        primary key,
    share_solution_id bigint       not null comment '共享项目ID',
    name              varchar(255) not null comment '名称',
    type              varchar(255) not null comment '类型，chart：图表，report：报表，text：文本，url：链接',
    data              text         null comment '视图数据',
    `sql`             text         null comment '方案SQL语句',
    remark            varchar(255) null comment '备注',
    share_by          bigint       not null comment '共享人ID',
    share_time        datetime     not null comment '共享时间',
    update_share_by   bigint       not null comment '共享更新人ID',
    update_share_time datetime     not null comment '共享更新时间',
    status            int          not null comment '状态：0删除，1正常，2停用'
)
    comment '共享视图';

-- Hive 数据库配置
INSERT INTO data_platform.datasource_config (id, source_name, url, username, password, create_time, driver_class, type) VALUES (12, 'sinohealth_test', 'jdbc:hive2://192.168.52.21:10000/sinohealth_test?', 'data_tool_hadoop', 'data_tool_hadoop@#123abc', '2021-10-21 17:23:24', 'org.apache.hive.jdbc.HiveDriver', 'hive2');
INSERT INTO data_platform.datasource_config (id, source_name, url, username, password, create_time, driver_class, type) VALUES (13, 'sinohealth_ods', 'jdbc:hive2://192.168.52.21:10000/sinohealth_ods?', 'data_tool_hadoop', 'data_tool_hadoop@#123abc', '2021-10-27 17:13:13', 'org.apache.hive.jdbc.HiveDriver', 'hive2');
--
INSERT INTO data_platform.data_dir (id, dir_name, parent_id, datasource_id, prefix, sort, status) VALUES (47, 'HIVE测试数据(TEST_HIVE)', 7, 12, null, 0, 1);
INSERT INTO data_platform.data_dir (id, dir_name, parent_id, datasource_id, prefix, sort, status) VALUES (48, 'ODS_HIVE数据库(ODS_HIVE)', 3, 13, null, 0, 1);




