ALTER TABLE `table_info`
ADD COLUMN `scheme_status` int(1) DEFAULT NULL COMMENT '表类型:0-静态,1-动态',
ADD COLUMN `scheme_cycle` int(1) DEFAULT NULL COMMENT '表更新周期:1-实时,2-每小时,3-每日,4-不更新';

ALTER TABLE `table_log`
ADD COLUMN `pre_content` varchar(5000) DEFAULT NULL COMMENT '变更前内容';


ALTER TABLE `data_standard_dict_tree`
ADD COLUMN `sort` int(10) DEFAULT NULL COMMENT '显示顺序';
