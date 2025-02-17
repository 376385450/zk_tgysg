-- 20210811
alter table table_task add result varchar(1024) null comment '任务结果';

alter table table_info add group_name varchar(255) null comment '负责部门（分组名称）';
alter table table_info add leader_name varchar(255) null comment '负责人（用户名称）';



