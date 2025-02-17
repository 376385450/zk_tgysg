
create table tg_assets_upgrade_trigger
(
    id             bigint auto_increment comment 'key'
        primary key,
    application_id bigint                             null,
    state          bigint                             null,
    workflow_id    bigint                             null,
    last_time      datetime                           null comment '上次更新时间',
    expect_time    datetime                           null comment '期望时间',
    create_time    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '资产更新配置表';

alter table tg_assets_upgrade_trigger    add column application_name varchar(100) null comment '需求名称' after application_id;
alter table tg_assets_upgrade_trigger   add column project_id                 bigint                                    null comment '项目ID' after application_name;
alter table tg_assets_upgrade_trigger   add column project_name                 varchar(100)                                    null comment '项目名称' after project_id;
alter table tg_assets_upgrade_trigger    add column deliver_time_type varchar(255) null after application_name;
alter table tg_assets_upgrade_trigger    add column workflow_name                      varchar(255)                              null after workflow_id;
alter table tg_assets_upgrade_trigger    add column biz_type                      varchar(255)                              null;
alter table tg_assets_upgrade_trigger add column  biz_name          varchar(255)                       null;
alter table tg_assets_upgrade_trigger add column  applicant_id                 int unsigned                                    null comment '申请人ID(所有者)';
alter table tg_assets_upgrade_trigger add column  applicant_name               varchar(100)          default ''                null comment '申请人姓名';
alter table tg_assets_upgrade_trigger add column  client_name                 varchar(100)                                    null comment '需求客户名';
alter table tg_assets_upgrade_trigger add column  deliver_delay                int                                             null;
alter table tg_assets_upgrade_trigger add column  data_expire                   datetime                                        null comment '数据有效截止时间';
alter table tg_assets_upgrade_trigger    add column last_start_time datetime null comment '上次任务启动时间' after workflow_name;


alter table tg_assets_upgrade_trigger modify column workflow_id    bigint                             not null comment '工作流id';

alter table tg_assets_upgrade_trigger add unique index u_idx_apply_id(application_id);


alter table tg_user_data_assets add column snapshot_type varchar(64) not null comment '快照类型' after version;
update tg_user_data_assets set snapshot_type='manual_deliver';
alter table sys_job_log add column ip varchar(32) null comment 'ip';



create table tg_acceptance_record
(
    id                bigint auto_increment comment 'key'
        primary key,
    application_id    bigint                             null,
    assets_id  bigint                       null comment '资产id',
    version        int                             null comment '版本',

    state      varchar(16)                       null comment '项目名称',

    user       bigint                             null,

    remark     text                       null,

    accept_time   datetime                           null comment '',


    create_time       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
comment '验收表';



insert into tg_acceptance_record (application_id, assets_id, version, state, user, remark, accept_time,create_time ) select src_application_id, id, version,'wait',creator,null, null,create_time  from tg_user_data_assets;

insert into tg_acceptance_record (application_id, assets_id, version, state, user, remark, accept_time,create_time )
select src_application_id, assets_id, version, 'version_roll', creator, null, null,create_time
from tg_user_data_assets_snapshot;

update tg_acceptance_record d left join tg_application_info t on t.id = d.application_id
    left join tg_template_info tti on t.template_id = tti.id
set d.biz_type = tti.biz_type ;

alter table tg_user_data_assets add column expire_type varchar(32) null comment '过期状态';
alter table tg_user_data_assets_snapshot add column expire_type varchar(32) null comment '过期状态';
update tg_user_data_assets_snapshot set expire_type ='normal';


alter table tg_user_data_assets add column  read_flag smallint null comment '新版本阅读状态';
alter table tg_user_data_assets_snapshot add column  read_flag smallint null comment '新版本阅读状态';

alter table sys_job_log add column ip varchar(32) null comment 'ip';

alter table tg_template_info add dis_sort int not null default 0 comment '资产地图排序';
alter table tg_template_info_snapshot add dis_sort int not null default 0 comment '资产地图排序';
alter table table_info add dis_sort int not null default 0 comment '资产地图排序';
alter table tg_doc_info add dis_sort int not null default 0 comment '资产地图排序';


-- 定时任务配置
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES ('工作流资产自动升级', 'DEFAULT', 'assetsUpgradeTriggerService.scheduleAssetsUpgrade()', '0 * * * * ?', '1', '1', '0', '', null, 'test_7027', '2023-08-25 10:21:03', '');
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES ('自动验收', 'DEFAULT', 'acceptanceRecordService.scheduleAutoAccept()', '0 0 * * * ?', '1', '1', '0', 'admin', '2023-08-22 17:00:04', '', '2023-08-23 17:15:56', '');
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES ('创建宽表任务', 'DEFAULT', 'assetsUpgradeTriggerService.scheduleCreateWideTableTask()', '0 */10 * * * ?', '1', '1', '0', 'admin', '2023-08-23 20:34:00', 'admin', '2023-09-04 14:49:07', '');
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, update_by, update_time, remark) VALUES ('宽表任务执行', 'DEFAULT', 'assetsUpgradeTriggerService.scheduleWideTable()', '0 */2 * * * ?', '1', '1', '0', 'admin', '2023-08-24 14:07:27', 'test_7027', '2023-08-29 18:00:26', '');

alter table tg_user_data_assets add column expire_type varchar(32) null comment '过期状态';
alter table tg_user_data_assets_snapshot add column expire_type varchar(32) null comment '过期状态';
update tg_user_data_assets_snapshot set expire_type ='normal';


-- 初始化 宽表触发记录
insert into tg_assets_wide_upgrade_trigger (table_id, assets_id, state, start_time, finish_time)
select c.table_id, a.id assets_id, 'success',now(),now()
from tg_user_data_assets a
         join tg_cogradient_info c on c.table_id = a.base_table_id;


alter table tg_application_info drop column enable_scheduled_task;
alter table tg_application_info drop column cron;

