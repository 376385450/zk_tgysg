alter table data_dir add column `comment` varchar(2000) default null comment '描述信息';

alter table tg_application_info add column `old_application_id` int(10) default null comment '重新申请会有此id,保留重新申请前的id,重新申请状态中可以预览前一申请数据';

alter table tg_application_info add column `new_application_id` int(10) default null comment '重新申请会有此id,保留重新申请的id';

alter table table_field_info add column `default_show` varchar(255) default 'y' comment '是否默认显示字段';