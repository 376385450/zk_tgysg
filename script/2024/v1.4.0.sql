/*
 1. 在MySQL中创建表
 */
create table tg_customer_apply_auth
(
    id int not null auto_increment primary key,
    apply_id       integer,
    user_id        integer,
    out_table_name varchar(255),
    auth_type      varchar(10),
    update_by      integer,
    update_time    varchar(255),
    status         smallint,
    create_time datetime not null default CURRENT_DATE,
    node_name varchar(100) comment '节点名称',
    icon varchar(56) comment '节点类型',
    parent_id int comment '父节点id',
    node_id bigint comment '当节点不是目录节点时对应的数据id,不同的业务类型对应不同表的主键id，form=tg_application_info.id,chart=arkbi_analysis.id,dashboard=arkbi_analysis.id'
);
create unique index `udx_user_id_icon_node_id` using btree on tg_customer_apply_auth(`user_id`,`icon`,`node_id`);

-- alter table tg_customer_apply_auth add column `create_time` datetime not null default CURRENT_DATE comment '创建时间' after `update_time`;
-- alter table tg_customer_apply_auth add column `node_name` varchar(100) comment '节点名称' after `status`;
-- alter table tg_customer_apply_auth add column `icon` varchar(56) comment '节点类型' after `node_name`;
-- alter table tg_customer_apply_auth add column `parent_id` int comment '父节点id' after `icon`;
-- alter table tg_customer_apply_auth add column `node_id` bigint comment '当节点不是目录节点时对应的数据id' after `parent_id`;

/*
  2. 将原PostgreSQL库中tg_customer_apply_auth表数据导入到MySQL库中的tg_customer_apply_auth
 */

/*
 3. 执行以下SQL
 */
update tg_customer_apply_auth set create_time = update_time where 1 = 1;
update tg_customer_apply_auth set node_name = out_table_name where 1 = 1;
update tg_customer_apply_auth set icon = 'form' where 1 = 1;
update tg_customer_apply_auth set parent_id = 0 where 1 = 1;
update tg_customer_apply_auth set node_id = apply_id where 1 = 1;

-- //////////////////////////////////////////////////////
/*
 1. 在MySQL中创建表
 */
create table tg_table_application_mapping_info
(
    id int not null auto_increment primary key,
    application_id        int comment '申请id',
    table_name            varchar(255),
    current_pg_table_name varchar(255) comment 'pg库表名，弃用',
    data_table_name varchar(255) comment '数据表名',
    data_volume           int default 0 comment '数据条数',
    date_update_time datetime not null default CURRENT_TIMESTAMP() comment '数据表数据更新时间',
    unique key `uk_application_id`(`application_id`)
)comment='客户资产映射表';

/*
 2. 将原PostgreSQL库中tg_table_application_mapping_info表数据导入到MySQL库中的tg_table_application_mapping_info
 */

 /*
  3. 执行以下SQL
  */
update tg_table_application_mapping_info set data_volume = 0 where data_volume is null;
update tg_table_application_mapping_info set data_table_name = current_pg_table_name where 1 = 1;




-- ///////////////////////////////////////////////////////
-- 交付记录相关
-- 邮件交付记录表
create table tg_deliver_email_record(
    id int not null auto_increment primary key,
    application_id bigint null comment '申请id',
    table_id bigint null comment '表单id',
    table_name varchar(255) null comment '表单名称冗余',
    allocate_type smallint not null comment '0不打包，1打包',
    project_name varchar(255) null comment '项目名',
    pack_name varchar(255) null comment '打包名称',
    parent_record_id int null comment '如果是打包交付，记录父节点的id',
    node_id bigint null comment '业务id',
    icon varchar(255) null comment '业务类型： form、chart、dashboard',
    receiver json null comment '接收邮箱列表',
    title varchar(1024) null comment '邮件标题',
    content text null comment '邮件内容',
    create_by bigint not null comment '创建人id',
    send_time datetime null comment '发送时间',
    create_time datetime not null default CURRENT_TIMESTAMP() comment '创建时间',
    key `idx_table_id`(`table_id`)
)engine='innodb' comment='邮件交付记录表';
-- 下载/导出记录表
create table tg_deliver_download_record(
    id int not null auto_increment primary key,
    application_id bigint null comment '申请id',
    table_id bigint null comment '表单id',
    table_name varchar(255) null comment '表单名称冗余',
    allocate_type smallint not null comment '0不打包，1打包',
    project_name varchar(255) null comment '项目名',
    pack_name varchar(255) null comment '打包名称',
    parent_record_id int null comment '如果是打包交付，记录父节点的id',
    node_id bigint null comment '业务id',
    icon varchar(255) null comment '业务类型： form、chart、dashboard',
    download_type varchar(255) not null comment '下载/导出类型',
    download_time datetime not null comment '下载/导出时间',
    create_by bigint not null comment '创建人id',
    create_time datetime not null default CURRENT_TIMESTAMP() comment '创建时间'
)engine='innodb' comment='下载/导出记录表';
-- 交付客户记录
create table tg_deliver_customer_record(
   id int not null auto_increment primary key,
   application_id bigint null comment '申请id',
   table_id bigint null comment '表单id',
   table_name varchar(255) null comment '表单名称冗余',
   allocate_type smallint not null comment '0不打包，1打包',
   allocate_user_id bigint not null comment '分配给客户的id',
   allocate_user_name varchar(255) not null comment '分配给客户的名称',
   auth_type varchar(255) not null comment '分配的权限，0查看，1下载',
   project_name varchar(255) null comment '项目名',
   pack_name varchar(255) null comment '打包名称',
   parent_record_id int null comment '如果是打包交付，记录父节点的id',
   node_id bigint null comment '业务id',
   icon varchar(255) null comment '业务类型： form、chart、dashboard',
   create_by bigint not null comment '创建人id',
   create_time datetime not null default CURRENT_TIMESTAMP() comment '创建时间'
)engine='innodb' comment='交付客户记录';
-- 邮件模板
create table tg_deliver_email_template(
    id int not null auto_increment primary key,
    title varchar(1024) null comment '邮件标题',
    content text null comment '邮件内容',
    receive_mails json null comment '接收邮箱',
    identify_id varchar(128) not null comment '唯一标识',
    identify_content json null comment '用于计算唯一标识的内容',
    create_time datetime not null default CURRENT_TIMESTAMP,
    create_by bigint null comment '创建人',
    update_time datetime not null default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(),
    update_by bigint null comment '更新人',
    unique key `udx_identify_id`(`identify_id`)
)engine='innodb' comment='交付邮件模板';

alter table tg_customer_apply_auth modify column node_id bigint null comment '业务节点id';

alter table tg_application_info add column copy tinyint not null default 0 comment '是否复制';
alter table tg_application_info add column copy_from_id bigint null comment '另存提数申请id';


-- /// 数据说明文档
create table tg_data_description(
    id int not null auto_increment primary key,
    application_id bigint not null comment '提数申请id',
    doc_name varchar(1024) not null comment '说明文档名称',
    data_quota text null comment '数据指标说明',
    data_desc json null comment '数据说明',
    base_target json null comment '基础指标',
    create_time datetime not null default CURRENT_TIMESTAMP comment '创建时间',
    update_time datetime not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    create_by bigint not null comment '创建人',
    unique key `udx_application_id`(`application_id`)
)engine='innodb' comment='数据说明';


create table tg_application_data_update_record
(
    id                 int auto_increment
        primary key,
    application_id     bigint                             null comment '申请id',
    start_time         datetime                             null comment '开始更新时间',
    finish_time         datetime                             null comment '完成时间',

    update_state         int                       null comment '更新状态',
    update_count         int                       null comment '更新数据量',
    updater_id         bigint                       null comment '更新人 id',
    trigger_type         int                       null comment '触发同步的类型',
    create_time        datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
comment '申请数据更新记录';


update tg_application_info set data_expir = null where data_expir = '';
alter table tg_application_info modify column data_expir datetime null comment '数据有效截止时间';

alter table arkbi_analysis
    add parent_id int null comment '父级ID,代表这条数据是从这个父级复制(推送到外网)而来';



--
alter table tg_message_record_dim add column application_type varchar(100) null comment '申请类型： 文档、提数';

update tg_message_record_dim a
inner join tg_application_info b on a.application_id = b.id
set a.application_type = b.application_type
where 1 = 1;

