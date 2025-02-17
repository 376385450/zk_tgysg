create table tg_intelligence_user_mapping
(
    id          int auto_increment
        primary key,
    tg_user_id  bigint not null,
    ysg_user_id bigint null,
    constraint tg_intelligence_user_mapping_pk
        unique (ysg_user_id)
)
    comment '天宫用户id映射表';

create table tg_notice_info
(
    id          int unsigned auto_increment comment '主键ID'
        primary key,
    notice_type varchar(64)        null comment '类型',
    name        varchar(128)       not null comment '名称',
    content     text               null comment '内容',
    asset_id    int                null comment '需要跳转的资产ID',
    is_top      int      default 0 not null comment '是否置顶，0：不置顶，1：置顶',
    create_time datetime           null comment '创建时间',
    creator     varchar(64)        null comment '创建人',
    update_time datetime           null comment '更新时间',
    updater     varchar(64)        null comment '更新人',
    del_flag    smallint default 0 not null comment '是否删除，0：未删除，1：已删除',
    delete_time datetime           null comment '删除时间'
);

create index idx_name
    on tg_notice_info (name);



create table assets_catalogue
(
    id                int auto_increment
        primary key,
    name              varchar(255)     not null comment '名称',
    parent_id         int              null comment '父节点id',
    code              varchar(255)     null comment '目录编码',
    sort_order        int              null comment '排序',
    global_sort       int              null comment '全局排序',
    path              varchar(1024)    null,
    level             int              null comment '层级',
    description       varchar(1024)    null comment '描述',
    icon              varchar(512)     null comment '图标',
    catalogue_flow_id bigint           null,
    service_flow_id   bigint           null,
    created_by        bigint           null comment '创建人',
    created_at        datetime         null comment '创建时间',
    updated_by        bigint           null comment '更新人',
    updated_at        datetime         null comment '更新时间',
    deleted           bigint default 0 not null
)
    comment '资产目录';


create table assets_catalogue_permission
(
    id                int auto_increment
        primary key,
    catalogue_id      int          not null,
    type              int          null comment '1 部门 2 员工',
    user_id           bigint       null,
    dept_id           varchar(255) null,
    readable          int          null comment '可阅读',
    assets_manager    int          null comment '资产管理权限',
    catalogue_manager int          null comment '目录管理权限'
);

create index assets_catalogue_permission_catalogue_id_index
    on assets_catalogue_permission (catalogue_id);



create table tg_asset_whitelist_info
(
    id                       bigint auto_increment
        primary key,
    asset_id                 bigint                                 null,
    type                     varchar(255) default ''                null comment '模型/库表/文件',
    related_id               bigint                                 null comment '模型id/库表id/文件id',
    staff_type               varchar(100)                           null comment '部门/人员',
    staff_id                 varchar(100)                           null comment '部门id/人员id',
    service_type             varchar(255)                           null comment '资产可见范围/服务申请流程',
    asset_open_services_json json                                   null,
    expiration_date          varchar(255)                           null,
    expirationDate           varchar(255)                           null,
    assetOpenServicesJson    varchar(255)                           null,
    creator                  varchar(100) default ''                null comment '创建者',
    updater                  varchar(100) default ''                null comment '更新者',
    create_time              timestamp    default CURRENT_TIMESTAMP null comment '创建日期',
    update_time              timestamp    default CURRENT_TIMESTAMP null comment '更新日期'
);



ALTER TABLE tg_template_info ADD example_table VARCHAR(255) AFTER biz_type;


create table tg_label_info (
    id int unsigned auto_increment comment '主键ID' primary key,
    name        varchar(64)        not null comment '标签名称',
    create_time datetime           null comment '创建时间',
    creator     varchar(64)        null comment '创建人',
    update_time datetime           null comment '更新时间',
    updater     varchar(64)        null comment '更新人',
    del_flag    smallint default 0 not null comment '是否删除，0：未删除，1：已删除',
    delete_time datetime           null comment '删除时间'
) comment '资产与标签关联表';
create index name_index on tg_label_info (name);

create table tg_asset_label_relation (
    id          int unsigned auto_increment comment '主键ID' primary key,
    label_id    int                not null comment '标签ID',
    asset_id    int                not null comment '资产ID(多个资产ID用逗号隔开)',
    create_time datetime           null comment '创建时间',
    del_flag    smallint default 0 not null comment '是否删除，0：未删除，1：已删除',
    delete_time datetime           null comment '删除时间'
) comment '资产标签表';
create index idx_asset_id on tg_asset_label_relation (asset_id);
create index idx_label_id on tg_asset_label_relation (label_id);


create table tg_asset_user_relation (
    id           int unsigned auto_increment comment '主键ID' primary key,
    asset_id     int           not null comment '资产ID',
    user_id      int           not null comment '用户ID',
    is_collect   int default 0 null comment '是否收藏，1：是，0：否',
    collect_time datetime      null comment '收藏时间',
    forward_num  int default 0 null comment '转发数',
    view_num     int default 0 null,
    updater      varchar(64)   null comment '更新人',
    update_time  datetime      null comment '更新时间'
) comment '资产与用户关联表';
create index idx_user_id on tg_asset_user_relation (user_id);





create table tg_asset_burial_point
(
    id          int auto_increment
        primary key,
    asset_id    bigint   null comment '资产id',
    view_num    int      null comment '浏览次数',
    burial_date datetime null comment '埋点时间',
    constraint tg_asset_burial_point_pk
        unique (asset_id, burial_date)
)
    comment '资产埋点表';

create table tg_asset_info
(
    id                                    bigint auto_increment
        primary key,
    type                                  varchar(255) default ''                null comment '模型/库表/文件',
    related_id                            bigint                                 null comment '模型id/库表id/文件id',
    meta_id                               bigint                                 null comment 'tg_metadata_info 的id',
    asset_name                            varchar(255) default ''                null comment '资产名称',
    asset_code                            varchar(255) default ''                null comment '资产编码',
    asset_sort                            bigint                                 null,
    asset_binding_data_type               varchar(255)                           null,
    asset_binding_data_name               varchar(255)                           null,
    asset_menu_id                         bigint                                 null comment '资产类目ID',
    asset_labels_json                     json                                   null comment '资产标签',
    asset_provider                        varchar(255) default ''                null comment '资产提供方',
    asset_manager_json                    json                                   null comment '资产负责人JSON',
    asset_manager_name                    varchar(255) default ''                null comment '资产负责人名',
    asset_description                     text                                   null comment '资产描述',
    asset_usage                           longtext                               null comment '使用说明',
    is_follow_asset_menu_readable_range   varchar(255) default ''                null comment '资产可见范围, 跟随目录/自定义',
    custom_asset_readable_whitelist_json  json                                   null comment '资产可见范围JSON',
    asset_open_services_json              json                                   null comment '资产可见范围JSON',
    query_limit                           int                                    null comment '查询开放条数',
    process_id                            bigint                                 null comment '流程ID',
    is_follow_service_menu_readable_range varchar(255) default ''                null comment '服务申请流程, 跟随目录/自定义',
    service_whitelist_json                json                                   null comment '服务白名单',
    resource_type                         varchar(100)                           null,
    shelf_state                           varchar(100) default '未上架'          null comment '上架状态：未上架、已上架、已下架',
    creator                               varchar(100) default ''                null comment '创建者',
    updater                               varchar(100) default ''                null comment '更新者',
    create_time                           timestamp    default CURRENT_TIMESTAMP null comment '创建日期',
    update_time                           timestamp    default CURRENT_TIMESTAMP null comment '更新日期',
    deleted                               int          default 0                 not null
);


create table tg_metadata_info
(
    id                 bigint auto_increment
        primary key,
    asset_id           bigint                                 null comment '资产id',
    meta_data_id       int                                    null comment '元数据绑定id',
    mata_data_id       int                                    null comment '元数据绑定id',
    tenant             varchar(255)                           null comment '所属租户',
    database_type      varchar(100)                           null comment '数据库类型',
    datasource         varchar(100)                           null comment '数据源',
    ip                 varchar(255)                           null,
    port               int                                    null,
    meta_data_database varchar(255)                           null,
    meta_data_table    varchar(255)                           null,
    creator            varchar(100) default ''                null comment '创建者',
    updater            varchar(100) default ''                null comment '更新者',
    create_time        timestamp    default CURRENT_TIMESTAMP null comment '创建日期',
    update_time        timestamp    default CURRENT_TIMESTAMP null comment '更新日期'
);



ALTER TABLE tg_application_info ADD new_asset_id BIGINT
    COMMENT '新的资产目录id,存取资产门户中资产表(tg_asset_info)的id' AFTER assets_id;

ALTER TABLE tg_application_info ADD permission_json VARCHAR(255) COMMENT '资产权限控制' AFTER doc_authorization_json;


alter table tg_application_info
    modify new_asset_id int null comment '新的资产目录id,存取资产门户中资产表(tg_asset_info)的id';