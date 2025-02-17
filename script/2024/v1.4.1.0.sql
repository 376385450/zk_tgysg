-- 20210813
-- 插入自定义库的数据目录
INSERT INTO data_platform.data_dir (dir_name, parent_id, datasource_id, prefix, sort, status) VALUES ('自定义库', 1, 8, null, 4, 1) ;

-- 数据集
create table dataset
(
    id            bigint auto_increment
        primary key,
    name          varchar(255)  not null comment '名称',
    english_name  varchar(255)  null comment '英文名称',
    type          int           not null comment '类型，1自定义数据集，2EXCEL数据集',
    description   varchar(255)  null comment '描述',
    database_type varchar(255)  null comment '数据源对应数据库类型',
    tables        text          null comment '表信息',
    links         text          null comment '表关联信息',
    fields        text          null comment '字段信息',
    filter        text          null comment '过滤信息',
    sorts         text          null comment '排序信息',
    `limit`       int           null comment '行数筛选，默认输出500行',
    dataset_sql   text          not null comment '数据集SQL语句',
    create_by     bigint        null comment '创建人ID',
    create_time   datetime      null comment '创建时间',
    update_by     bigint        null comment '更新人ID',
    update_time   datetime      null comment '更新时间',
    status        int default 1 not null comment '状态：0删除，1正常，2停用'
)
    comment '数据集';







