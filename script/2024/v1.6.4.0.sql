-- 新增字段：热度
alter table table_info
    add heat tinyint(3) default 4 not null comment '热度，1：0~30天内有使用（查询或者变更过）为高热度；2：30~90天内有使用为 中等温度；3：90天为低热度表；4：365天无人使用为冷表';

-- 新增字段：重点字段
alter table table_field_info
    add major_field bit default 0 not null comment '重点字段，true是，false否';

-- 新增字段：
alter table sys_menu
    add menu_code varchar(255) null;



