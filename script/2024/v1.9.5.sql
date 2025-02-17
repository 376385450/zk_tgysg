-- auto-generated definition
create table tg_table_info_snapshot_compare
(
    id                 bigint auto_increment comment 'key'
        primary key,
    table_id           bigint                             not null comment '表id',
    new_version_id     bigint                             not null comment '新版id',
    new_version_period varchar(64)                        not null comment '新版期数名称',
    old_version_id     bigint                             not null comment '旧版id',
    old_version_period varchar(64)                        not null comment '旧版期数名称',
    state              varchar(16)                        null comment '状态',
    result_state       varchar(16)                        null comment '结果表状态',
    fail_reason        text                               null comment '失败原因',
    create_time        datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    finish_time        datetime                           null comment '完成时间',
    creator            bigint                             not null comment '创建人',
    update_time        datetime default CURRENT_TIMESTAMP not null
)
    comment '库表快照比对表';

create table tg_table_info_snapshot_compare_detail
(
    id           bigint auto_increment comment 'key',
    compare_id   bigint       not null comment '比对id',
    category     varchar(32)  not null comment '类型',
    table_name   varchar(255) not null comment '表名称',
    data_count   bigint       not null comment '数据统计条数',
    process_time bigint       not null comment '处理时常',
    create_time  datetime     not null comment '创建时间',
    constraint tg_table_info_snapshot_compare_detail_pk
        primary key (id)
) comment '库表快照比对详细表';

CREATE TABLE `tg_table_info_snapshot_compare_limit`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT 'key',
    `table_id`      bigint       NOT NULL COMMENT '表id',
    `condition_sql` varchar(255) NOT NULL COMMENT '条件语句',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='库表快照比对条件表';

alter table table_field_info
    add logic_key int default 0 null comment '逻辑主键';

alter table table_field_info
    add compare_field int default 0 null comment '是否参与比对';

alter table table_field_info
    modify dir_id bigint unsigned null;

INSERT INTO tg_table_info_snapshot_compare_limit (id, table_id, condition_sql)
VALUES (1, 10054, 'otherstag <> 2');

alter table tg_table_info_snapshot_compare_detail
    add data_source varchar(32) not null comment '数据源类型' after category;

alter table tg_table_info_snapshot_compare_detail
    add attach varchar(255) null comment '数据源连接附加信息' after data_source;

alter table tg_table_info_snapshot_compare
    add plan_id bigint null comment '计划编号' after table_id;


CREATE TABLE `tg_table_info_snapshot_compare_plan`
(
    `id`             bigint   NOT NULL AUTO_INCREMENT COMMENT 'key',
    `table_id`       bigint   NOT NULL COMMENT '表id',
    `old_version_id` bigint   NOT NULL COMMENT '旧版id',
    `create_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator`        bigint   NOT NULL COMMENT '创建人',
    `update_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='库表快照比对计划表';

alter table tg_table_info_snapshot_compare_detail
    add deleted tinyint default 0 not null comment '是否删除' after attach;

alter table tg_table_info_snapshot_compare_detail
    add update_time datetime null comment '更新时间';







