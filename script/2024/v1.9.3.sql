alter table table_info add column version int default 1 comment '底表版本' after table_name_distributed;

alter table table_info add column sync_time datetime  comment '数据同步时间';

update table_info set sync_time = update_time where table_info.sync_time  is null;


CREATE TABLE `tg_table_info_snapshot` (
  `id` int NOT NULL AUTO_INCREMENT,
  `table_id` int NOT NULL COMMENT '表id',
  `table_name` varchar(255) NOT NULL COMMENT '本地表名',
  `table_name_distributed` varchar(255) DEFAULT NULL COMMENT '分布式表名',
  `version` int DEFAULT '1' COMMENT '底表版本',
  `status` varchar(16) NOT NULL DEFAULT 'normal' COMMENT 'delete 删除 normal 可用 ',
  `push_status` varchar(16) NOT NULL DEFAULT 'none' COMMENT ' none 未推送资产 run 执行中 success failed',
  `pre_version` int DEFAULT NULL COMMENT '对比版本',
  `total_row` bigint unsigned NOT NULL DEFAULT '0' COMMENT '表总行数',
  `version_period` varchar(32) DEFAULT NULL COMMENT '期数版本',
  `latest` tinyint DEFAULT '1' COMMENT '是否最新版本',
  `remark` varchar(1024) DEFAULT NULL,
  `sync_time` datetime DEFAULT NULL COMMENT '数据同步时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `table_version` varchar(255) GENERATED ALWAYS AS (concat(`table_id`,_utf8mb4'#',`version`)) STORED,
  PRIMARY KEY (`id`),
  KEY `idx_table_version` (`table_id`,`version`),
  KEY `idx_tableversion` (`table_version`)
) ENGINE=InnoDB comment '库表 快照';

-- 初始化历史表版本
insert into tg_table_info_snapshot(table_id, table_name, table_name_distributed, status, push_status, total_row, version, latest, sync_time)
select id table_id, table_name, table_name_distributed, 'normal' as `status`, 'none' as push_status ,total_row, version, 1 as latest, sync_time  from table_info where is_diy != 1;


alter table tg_user_data_assets_snapshot add column base_version_period varchar(64) comment '期数版本' after base_version;
alter table tg_user_data_assets add column base_version_period varchar(64) comment '期数版本' after base_version;

alter table tg_user_data_assets add column prod_code varchar(512) comment '关联品类' after version;
alter table tg_user_data_assets_snapshot add column prod_code varchar(512) comment '关联品类' after version;

alter table tg_user_data_assets add base_version int comment '底表版本' after base_table_id;
alter table tg_user_data_assets_snapshot add base_version int comment '底表版本' after base_table_id;

update tg_user_data_assets set base_version = 1 where template_type='wide_table';
update tg_user_data_assets_snapshot set base_version = 1 where template_type='wide_table';

alter table tg_assets_wide_upgrade_trigger add act_version int comment '升级版本' after table_id;
alter table tg_assets_wide_upgrade_trigger add pre_version int comment '历史版本' after act_version;


alter table tg_application_info add column remark_files json comment '需求附件 ,分隔' after apply_remark;
alter table tg_application_info add column push_power_bi tinyint comment '是否推送PowerBI' after remark_files;
alter table tg_application_info add column push_project_name varchar(64) comment '推送需求名' after push_power_bi;


alter table tg_template_info add column push_power_bi tinyint default 0 comment '是否推送PowerBI' after tail_fields_json;
alter table tg_template_info add column push_table_name varchar(64) comment '目标表' after push_power_bi;
alter table tg_template_info add column push_fields_json json comment '推送字段映射' after push_table_name;

alter table tg_template_info_snapshot add column push_power_bi tinyint default 0 comment '是否推送PowerBI' after tail_fields_json;
alter table tg_template_info_snapshot add column push_table_name varchar(64) comment '目标表' after push_power_bi;
alter table tg_template_info_snapshot add column push_fields_json json comment '推送字段映射' after push_table_name;



CREATE TABLE `tg_assets_compare` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'key',
  `base_table_id` bigint DEFAULT NULL COMMENT '字典id',
  `assets_id` bigint DEFAULT NULL COMMENT '字典id',
  `cur_version` int DEFAULT NULL COMMENT '新版本',
  `cur_version_period` varchar(64) DEFAULT NULL COMMENT '新版本',
  `pre_version` int DEFAULT NULL COMMENT '历史版本',
  `pre_version_period` varchar(64) DEFAULT NULL COMMENT '旧版本',
  `result_path` varchar(255) DEFAULT NULL COMMENT 'ftp 结果',
  `create_type` varchar(16) DEFAULT NULL COMMENT '类型 auto manual',
  `state` varchar(16) DEFAULT NULL COMMENT '状态',
  `deleted` tinyint DEFAULT '0',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` bigint NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `updater` bigint NOT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  COMMENT='资产比对任务'



CREATE TABLE `tg_assets_pb_push_batch` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'key',
  `name` text COMMENT '任务名称',
  `template_id` varchar(512) DEFAULT NULL COMMENT '模板id',
  `template_name` text COMMENT '模板名称',
  `state` varchar(16) DEFAULT NULL COMMENT '状态',
  `deleted` tinyint DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` bigint NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='PowerBI推数'

CREATE TABLE `tg_assets_pb_push_batch_detail` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'key',
  `batch_id` bigint DEFAULT NULL COMMENT '批次id',
  `assets_id` bigint DEFAULT NULL COMMENT '资产id',
  `assets_ver` int DEFAULT NULL COMMENT '资产版本',
  `assets_version` varchar(255) GENERATED ALWAYS AS (concat(`assets_id`,_utf8mb4'#',`assets_ver`)) STORED,
  `table_name` varchar(127) DEFAULT NULL COMMENT '资产表名',
  `pre_sql` varchar(512) DEFAULT NULL COMMENT '前置SQL',
  `insert_sql` text COMMENT '同步SQL',
  `run_log` text COMMENT '执行日志',
  `state` varchar(16) DEFAULT NULL COMMENT '状态',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='PowerBI推数明细'


CREATE TABLE `tg_table_push_assets_plan` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'key',
  `table_id` bigint DEFAULT NULL COMMENT '表id',
  `pre_version` int DEFAULT NULL COMMENT '历史版本',
  `state` varchar(16) DEFAULT NULL COMMENT '状态',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator` bigint NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `next_version` int DEFAULT NULL COMMENT '计划版本',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='资产推送计划'


update tg_application_info set push_project_name = project_name, push_power_bi = 1 where application_type = 'data' and project_name in ('百洋-惠氏-月度-氨糖_395','百洋-惠氏-月度-氨糖_396','百洋-惠氏-月度-氨糖_397','百洋-惠氏-月度-钙制剂_392','百洋-惠氏-月度-钙制剂_393','百洋-惠氏-月度-钙制剂_394','拜耳-月度-四大品类-全国数据_230','勃林格-月度-口服降糖overview全国_807','勃林格-月度-口服降糖overview省份_808','勃林格-月度-全国-抗凝血药_727','勃林格-月度-全国-抗帕金森_726','勃林格-月度-全国-口服降糖药DPP4i&SGLT2i_729','勃林格-月度-全国-慢阻肺IPF_728','东阿阿胶-10省份_526','东阿阿胶-2城市_527','东阿阿胶-全国_525','康恩贝-季度-前列腺用药数据-全国_333','康恩贝-季度-前列腺用药数据-全国_333_JD','康恩贝-月度-前列腺用药数据-52城市_334','康恩贝-月度-前列腺用药数据-52城市_334_JD','美纳里尼-ED-15省份_852','美纳里尼-ED-全国_851','美纳里尼-ED-直辖市_853','美纳里尼-祛疤痕-全国季度_849','美纳里尼-祛疤痕-全国季度_849_JD','美纳里尼-去疤痕-31城市季度_850','美纳里尼-去疤痕-31城市季度_850_JD','森世海亚-金纳多定义品类-全国_658','森世海亚-金纳多定义品类-全国_658_JD','森世海亚-金纳多定义品类-省份_659','森世海亚-金纳多定义品类-省份_659_JD','山东达因-维D维AD-城市_735','山东达因-维D维AD-城市_735_JD','山东达因-维D维AD-全国_733','山东达因-维D维AD-全国_733_JD','山东达因-维D维AD-省份_734','山东达因-维D维AD-省份_734_JD','泰德-外用镇痛_416','泰德-外用镇痛_416_JD','汤臣倍健-14城市2-解热镇痛&减肥&眼科&泌尿&肌肉骨骼&肝胆&高血压&止咳&妇科_531','汤臣倍健-14城市2-维矿_528','汤臣倍健-全国-解热镇痛&减肥&眼科&泌尿&肌肉骨骼&肝胆&高血压&止咳&妇科_535','汤臣倍健-14城市2-胃肠道&神经系统&心脑血管_530','汤臣倍健-14城市2-滋补保健_529','汤臣倍健-14城市-解热镇痛&减肥&眼科&泌尿&肌肉骨骼&肝胆&高血压&止咳&妇科_531','汤臣倍健-14城市-维矿_528','汤臣倍健-14城市-胃肠道&神经系统&心脑血管_530','汤臣倍健-14城市-滋补保健_529','汤臣倍健-全国-维矿_532','汤臣倍健-全国-胃肠道&神经系统&心脑血管_534','汤臣倍健-全国-滋补保健_533','以岭-感冒药_236','以岭-感冒药_237','以岭-感冒药_238','以岭-心脑血管_194','以岭-心脑血管_194_JD','以岭-心脑血管_195','以岭-心脑血管_195_JD','以岭-心脑血管_196','以岭-心脑血管_196_JD');


update tg_application_info set push_project_name = trim(TRAILING '_品牌' from project_name ), push_power_bi = 1 where application_type = 'data' and project_name in ('百洋-惠氏-月度-氨糖_395_品牌','百洋-惠氏-月度-氨糖_396_品牌','百洋-惠氏-月度-氨糖_397_品牌','百洋-惠氏-月度-钙制剂_392_品牌','百洋-惠氏-月度-钙制剂_393_品牌','百洋-惠氏-月度-钙制剂_394_品牌','拜耳-月度-四大品类-全国数据_230_品牌','勃林格-月度-口服降糖overview全国_807_品牌','勃林格-月度-口服降糖overview省份_808_品牌','勃林格-月度-全国-抗凝血药_727_品牌','勃林格-月度-全国-抗帕金森_726_品牌','勃林格-月度-全国-口服降糖药DPP4i&SGLT2i_729_品牌','勃林格-月度-全国-慢阻肺IPF_728_品牌','东阿阿胶-10省份_526_品牌','东阿阿胶-2城市_527_品牌','东阿阿胶-全国_525_品牌','康恩贝-季度-前列腺用药数据-全国_333_品牌','康恩贝-季度-前列腺用药数据-全国_333_JD_品牌','康恩贝-月度-前列腺用药数据-52城市_334_品牌','康恩贝-月度-前列腺用药数据-52城市_334_JD_品牌','美纳里尼-ED-15省份_852_品牌','美纳里尼-ED-全国_851_品牌','美纳里尼-ED-直辖市_853_品牌','美纳里尼-祛疤痕-全国季度_849_品牌','美纳里尼-祛疤痕-全国季度_849_JD_品牌','美纳里尼-去疤痕-31城市季度_850_品牌','美纳里尼-去疤痕-31城市季度_850_JD_品牌','森世海亚-金纳多定义品类-全国_658_品牌','森世海亚-金纳多定义品类-全国_658_JD_品牌','森世海亚-金纳多定义品类-省份_659_品牌','森世海亚-金纳多定义品类-省份_659_JD_品牌','山东达因-维D维AD-城市_735_品牌','山东达因-维D维AD-城市_735_JD_品牌','山东达因-维D维AD-全国_733_品牌','山东达因-维D维AD-全国_733_JD_品牌','山东达因-维D维AD-省份_734_品牌','山东达因-维D维AD-省份_734_JD_品牌','泰德-外用镇痛_416_品牌','泰德-外用镇痛_416_JD_品牌','汤臣倍健-14城市2-解热镇痛&减肥&眼科&泌尿&肌肉骨骼&肝胆&高血压&止咳&妇科_531_品牌','汤臣倍健-14城市2-维矿_528_品牌','汤臣倍健-14城市2-胃肠道&神经系统&心脑血管_530_品牌','汤臣倍健-14城市2-滋补保健_529_品牌','汤臣倍健-14城市-解热镇痛&减肥&眼科&泌尿&肌肉骨骼&肝胆&高血压&止咳&妇科_531_品牌','汤臣倍健-14城市-维矿_528_品牌','汤臣倍健-14城市-胃肠道&神经系统&心脑血管_530_品牌','汤臣倍健-14城市-滋补保健_529_品牌','汤臣倍健-全国-解热镇痛&减肥&眼科&泌尿&肌肉骨骼&肝胆&高血压&止咳&妇科_535_品牌','汤臣倍健-全国-维矿_532_品牌','汤臣倍健-全国-胃肠道&神经系统&心脑血管_534_品牌','汤臣倍健-全国-滋补保健_533_品牌','以岭-感冒药_236_品牌','以岭-感冒药_237_品牌','以岭-感冒药_238_品牌','以岭-心脑血管_194_品牌','以岭-心脑血管_194_JD_品牌','以岭-心脑血管_195_品牌','以岭-心脑血管_195_JD_品牌','以岭-心脑血管_196_品牌','以岭-心脑血管_196_JD_品牌');