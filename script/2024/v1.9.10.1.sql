alter table tg_template_info
    add open_distributed tinyint default 0 null comment '是否开启 分布/分层信息';

alter table tg_template_info
    add distributed_json json null comment '分布信息json';

alter table tg_template_info
    add distributed_description varchar(256) null comment '分层/分布信息 - 提示说明' after open_distributed;



alter table tg_application_info
    add open_distributed tinyint default 0 null comment '是否开启 分布/分层信息';

alter table tg_application_info
    add distributed_json json null comment '分布信息json';

alter table tg_application_task_config
    add zdy_qj text null comment '自定义区间信息';

alter table tg_application_task_config_snapshot
    add zdy_qj text null comment '自定义区间信息';