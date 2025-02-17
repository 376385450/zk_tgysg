create table event_log (
    id bigint not null auto_increment primary key comment '主键',
    user_id bigint null comment '用户id',
    event_type varchar(64) not null comment '事件类型',
    operate_type varchar(64) null comment '操作类型',
    subject_id varchar(64) null comment '操作主体id',
    subject_name varchar(128) null comment '操作主体名称',
    subject_type varchar(64) null comment '一级操作类型',
    second_subject_type varchar(128) null comment '操作主体二级操作名称',
    event_log_data json null comment '业务数据字段',
    method_name varchar(128) null comment '方法名称',
    log_date char(11) not null comment '事件发生的日期 yyyy/MM/dd',
    log_time char(20) not null comment '事件发生的日期时间 yyyy/MM/dd HH:mm:ss',
    create_time timestamp(3) not null default CURRENT_TIMESTAMP(3) comment '创建时间',

    key `idx_subject_id_event_type_subject_type_log_date`(`subject_id`,`event_type`,`subject_type`,`log_date`)

)engine=innodb default CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci AUTO_INCREMENT=1 comment='事件日志表';


alter table tg_application_info
    add column template_metrics_json json not null comment '维度数据信息(JSON序列化)';

alter table tg_application_info
    add column apply_data_range_info_json json not null comment '申请数据范围信息(JSON序列化)';

-- 1.3 后期需求
alter table tg_template_info
    add column application_period_field_json json null comment '限制申请时 日期聚合使用的字段';
alter table tg_template_info
    add column allow_application_period   int(1)                default 1                 null comment '是否限制申请时 日期聚合使用的字段';


alter table tg_template_info
    add column sql_build_mode int null comment 'SQL构造模式 1 子查询 2 单层SQL';


alter table custom_field_info add column hidden_for_apply     bit               default b'0'              null;
