update tg_fast_entry t set t.menu_name = '资产目录', t.name = 'AssetDirectory' where t.name = 'DataManagement/mapDirectory'

create table tg_initial_log
(
    id                                    bigint auto_increment primary key,
    type                                  varchar(255) default ''                null comment '模型/库表/文件/申请',
    related_id                            bigint                                 null comment '模型id/库表id/文件id',
    data_json                             json                                   null comment '数据初始化前JSON',
    creator                                varchar(255) default ''                null comment '调用者',
    create_time                           timestamp                             null comment '创建日期',
    update_time                           timestamp                             null comment '更新日期',
    status                               int          default 0                 not null comment '状态:0-待初始化,1-已初始化'
);