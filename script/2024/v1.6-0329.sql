alter table sys_customer add column `customer_type` varchar(255) comment '客户分类';

-- 用户给了一张映射关系表， 需要将其建立成一张映射表

CREATE TABLE `tg_org_brief_name` (
 `org_path_text` varchar(255) DEFAULT NULL,
 `brief_name` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


alter table sys_menu add column `item_level_one_id` varchar(255) comment '面包屑一级id';
alter table sys_menu add column `item_level_two_id` varchar(255) comment '面包屑二级id';


CREATE TABLE `sys_fast_entry_user_menu` (
 `user_id` bigint(20) COMMENT '用户ID',
 `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
 `default` varchar(1) NOT NULL DEFAULT 'N' COMMENT '是否为默认展示项,如果没有设置的话',
 PRIMARY KEY (`user_id`,`menu_id`) USING BTREE,
 KEY `idx_fast_entry_user_menu` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户和快速入口列表关联表';


alter table data_dir add column `moved` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '0 未移动 1 已移动, 移动过的目录将使用 sort 来进行排序 ';


