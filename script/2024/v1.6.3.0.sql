--
create table integrate_statistics
(
    id              bigint auto_increment comment 'ID'
        primary key,
    project_id      bigint   not null comment '项目ID',
    data_num        text     null comment '数据行数（JSON格式字符串）',
    data_size       text     null comment '数据体量（JSON格式字符串）',
    statistics_date datetime null comment '统计时间',
    create_by       bigint   null comment '创建人ID',
    create_time     datetime null comment '创建时间',
    update_by       bigint   null comment '更新人ID',
    update_time     datetime null comment '更新时间',
    status          int      null comment '状态：0删除，1正常，2停用'
)
    comment '资产统计信息';