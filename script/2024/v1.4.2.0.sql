-- 20210901
-- 分析项目
create table analysis_project
(
    id          bigint auto_increment comment 'ID'
        primary key,
    name        varchar(255) not null comment '分析项目名称',
    create_by   bigint       not null comment '创建人ID',
    create_time datetime     not null comment '创建时间',
    update_by   bigint       not null comment '更新人ID',
    update_time datetime     not null comment '更新时间',
    status      int          not null comment '状态：0删除，1正常，2停用'
)
    comment '分析项目';

-- 分析方案
create table analysis_solution
(
    id          bigint auto_increment comment 'ID'
        primary key,
    project_id  bigint       not null comment '分析项目ID',
    name        varchar(255) not null comment '名称',
    type        varchar(255) not null comment '类型，visual：可视化图表分析方案，zero：零代码分析方案，sql：SQL代码分析方案，python3：PYTHON3代码分析方案',
    `sql`       text         null comment '方案SQL语句',
    remark      varchar(255) null comment '备注',
    create_by   bigint       not null comment '创建人ID',
    create_time datetime     not null comment '创建时间',
    update_by   bigint       not null comment '更新人ID',
    update_time datetime     not null comment '更新时间',
    status      int          not null comment '状态：0删除，1正常，2停用'
)
    comment '分析方案';

-- 分析视图
create table analysis_view
(
    id          bigint auto_increment comment 'ID'
        primary key,
    solution_id bigint       not null comment '分析方案ID',
    name        varchar(255) not null comment '名称',
    type        varchar(255) not null comment '类型，chart：图表，report：报表，text：文本，url：链接',
    data        text         null comment '视图数据',
    `sql`       text         null comment '方案SQL语句',
    remark      varchar(255) null comment '备注',
    create_by   bigint       not null comment '创建人ID',
    create_time datetime     not null comment '创建时间',
    update_by   bigint       not null comment '更新人ID',
    update_time datetime     not null comment '更新时间',
    status      int          not null comment '状态：0删除，1正常，2停用'
)
    comment '分析视图';

-- 分析记录
create table analysis_record
(
    id           bigint auto_increment comment 'ID'
        primary key,
    solution_id  bigint       not null comment '分析方案ID',
    result       varchar(255) not null comment '执行结果，success成功，failure失败',
    sql_type     varchar(255) not null comment '数据源类型',
    `sql`        text         not null comment '分析记录SQL语句',
    execute_time bigint       not null comment '执行耗时（ms）',
    create_by    bigint       not null comment '创建人ID',
    create_time  datetime     not null comment '创建时间',
    update_by    bigint       not null comment '更新人ID',
    update_time  datetime     not null comment '更新时间',
    status       int          not null comment '状态：0删除，1正常，2停用'
)
    comment '分析记录';

-- 分析方案快照
create table analysis_solution_snapshot
(
    id          bigint auto_increment comment 'ID'
        primary key,
    solution_id bigint       not null comment '分析方案ID',
    name        varchar(255) not null comment '分析方案快照名称',
    type        varchar(255) not null comment '分析方案快照类型，static静态，dynamic动态',
    solution    text         null comment '分析方案（零代码分析方案）',
    `sql`       text         not null comment 'SQL语句',
    result      text         null comment '结果集',
    visual      text         null comment '可视化图表',
    create_by   bigint       not null comment '创建人ID',
    create_time datetime     not null comment '创建时间',
    update_by   bigint       not null comment '更新人ID',
    update_time datetime     not null comment '更新时间',
    status      int          not null comment '状态：0删除，1正常，2停用'
)
    comment '分析方案快照';















