alter table tg_application_task_config add column assets_qc varchar(8) default '否' comment '是否执行资产QC 是/否';
alter table tg_application_task_config add column project_scope varchar(32)  comment '项目类型';

alter table tg_application_task_config_snapshot add column assets_qc varchar(8) default '否' comment '是否执行资产QC 是/否';
alter table tg_application_task_config_snapshot add column project_scope varchar(32)  comment '项目类型';

alter table tg_application_info add column assets_qc tinyint comment 'qc';
alter table tg_application_info add column project_scope varchar(16) comment '';

alter table tg_template_info add column assets_qc tinyint comment 'qc';
alter table tg_template_info_snapshot add column assets_qc tinyint comment 'qc';
-- alter table tg_template_info add column assets_qc_type varchar(16) comment '';

update tg_application_task_config set assets_qc = '是' where period_granular = '月度';


alter table tg_assets_wide_upgrade_trigger add column qc_batch_id bigint ;
alter table tg_assets_upgrade_trigger add column qc_batch_id bigint ;

update tg_template_info set assets_qc = 1 where id in (100,154);
update tg_application_info a left join tg_application_info b on a.old_application_id = b.id  set a.assets_id = b.assets_id where a.assets_id is null;


-- 初始化
select id, project_name, assets_qc, new_application_id, data_total from tg_application_info
                                                                   where template_id = 100 and new_application_id is null
and project_name not like '%test%' and project_name not like '%测试%' ;

-- update tg_application_info set assets_qc = 1 where template_id = 100 and new_application_id is null
-- and project_name not like '%test%' and project_name not like '%测试%' ;

select id, project_name, data_total from tg_user_data_assets where template_id = 100 ;
select sum(data_total) from tg_user_data_assets where template_id = 100 ;

-- 初始化
-- select id, project_name, assets_qc, new_application_id from tg_application_info where template_id = 154 and new_application_id is null;
-- update  tg_application_info set assets_qc =1 where template_id = 154 and project_name like '%_品牌' and new_application_id is null;

update tg_application_info
set assets_qc     = 1,
    project_scope ='merge'
where project_name in ('百洋-惠氏-月度-氨糖_395_品牌', '百洋-惠氏-月度-氨糖_396_品牌', '百洋-惠氏-月度-氨糖_397_品牌',
                       '马应龙-季度-8城市-肛肠类_140_品牌', '马应龙-季度-皮肤用药_139_品牌',
                       '美纳里尼-去疤痕-31城市季度_850_品牌', '森世海亚-金纳多定义品类-全国_658_品牌',
                       '森世海亚-金纳多定义品类-省份_659_品牌', '泰德-外用镇痛_416_品牌', '以岭-感冒药_236_品牌',
                       '以岭-感冒药_237_品牌', '以岭-感冒药_238_品牌', '以岭-心脑血管_194_品牌',
                       '以岭-心脑血管_195_品牌', '以岭-心脑血管_196_品牌');
update tg_application_info
set assets_qc     = 1,
    project_scope ='split'
where project_name in
      ('GSK-季度-肝炎抗病毒-城市_650_品牌', 'GSK-季度-肝炎抗病毒-全国_649_品牌', 'GSK-季度-肝炎抗病毒-省份_651_品牌',
       'GSK-季度-抗癫痫-城市_653_品牌', 'GSK-季度-抗癫痫-全国_652_品牌', 'GSK-季度-抗癫痫-省份_654_品牌',
       'GSK-季度-慢阻肺-城市_656_品牌', 'GSK-季度-慢阻肺-全国_655_品牌', 'GSK-季度-慢阻肺-省份_657_品牌',
       '百洋-骨化三醇_774_品牌', '百洋-惠氏-月度-钙制剂_392_品牌', '百洋-惠氏-月度-钙制剂_393_品牌',
       '百洋-惠氏-月度-钙制剂_394_品牌', '勃林格-月度-18省份-慢阻肺IPF_744_品牌',
       '勃林格-月度-4直辖市-慢阻肺IPF_745_品牌', '勃林格-月度-口服降糖overview全国_807_品牌',
       '勃林格-月度-口服降糖overview省份_808_品牌', '勃林格-月度-全国-抗凝血药_727_品牌',
       '勃林格-月度-全国-抗帕金森_726_品牌', '勃林格-月度-全国-口服降糖药DPP4i&SGLT2i_729_品牌',
       '勃林格-月度-全国-慢阻肺IPF_728_品牌', '参天-月度-眼科-全国_519_品牌', '东阿阿胶-10省份_526_品牌',
       '东阿阿胶-2城市_527_品牌', '东阿阿胶-全国_525_品牌', '杭州民生-15城市-维矿类数据_854_品牌',
       '杭州民生-15城市-维矿类数据_季度_855_品牌', '吉利德-抗肝炎病毒-城市-月度_390_品牌',
       '吉利德-抗肝炎病毒-全国-月度_389_品牌', '健合-保健品-全国-月度_20-21年_739_品牌',
       '健合-保健品-全国-月度_22-23年_739_品牌', '江中-胃肠道用药-全国-月度_76_品牌',
       '康恩贝-季度-前列腺用药数据-全国_333_品牌', '康恩贝-月度-前列腺用药数据-52城市_334_品牌',
       '罗氏-奥司他韦-全国-月度_824_品牌', '罗氏-季度-血糖仪_419_品牌', '美纳里尼-ED-15省份_852_品牌',
       '美纳里尼-ED-全国_851_品牌', '美纳里尼-ED-直辖市_853_品牌', '美纳里尼-祛疤痕-全国季度_849_品牌',
       '念慈蓭-30城市-止咳祛痰类-月度_834_品牌', '念慈蓭-全国-止咳祛痰类-月度_833_品牌',
       '诺和诺德-糖尿病用药-15省份-季度_839_品牌', '诺和诺德-糖尿病用药-直辖市-季度_840_品牌',
       '山东达因-维D维AD-城市_735_品牌', '山东达因-维D维AD-全国_733_品牌', '山东达因-维D维AD-省份_734_品牌',
       '施维雅-痔疮及静脉曲张-季度-城市_809_品牌',
       '汤臣倍健-14城市2-解热镇痛&减肥&眼科&泌尿&肌肉骨骼&肝胆&高血压&止咳&妇科_531_品牌',
       '汤臣倍健-14城市2-维矿_528_品牌', '汤臣倍健-14城市2-胃肠道&神经系统&心脑血管_530_品牌',
       '汤臣倍健-14城市2-滋补保健_529_品牌',
       '汤臣倍健-14城市-解热镇痛&减肥&眼科&泌尿&肌肉骨骼&肝胆&高血压&止咳&妇科_531_品牌',
       '汤臣倍健-14城市-维矿_528_品牌', '汤臣倍健-14城市-胃肠道&神经系统&心脑血管_530_品牌',
       '汤臣倍健-14城市-滋补保健_529_品牌', '汤臣倍健-7城市-维生素C品类_814_品牌',
       '汤臣倍健-全国-解热镇痛&减肥&眼科&泌尿&肌肉骨骼&肝胆&高血压&止咳&妇科_535_品牌', '汤臣倍健-全国-维矿_532_品牌',
       '汤臣倍健-全国-胃肠道&神经系统&心脑血管_534_品牌', '汤臣倍健-全国-滋补保健_533_品牌',
       '武田-人血白蛋白-城市-季度_830_品牌', '武田-人血白蛋白-全国-季度_828_品牌', '武田-人血白蛋白-省份-季度_829_品牌',
       '雅培-城市-4品类-季度_754_品牌', '雅培-全国-4品类-季度_750_品牌', '雅培-省份-4品类-季度_752_品牌',
       '亿腾-3商品名-16省份-季度_712_品牌', '亿腾-3商品名-5城市-季度_713_品牌', '亿腾-全国-3细分类-季度_731_品牌',
       '益普生-季度-城市止泻类用药_332_品牌', '益普生-季度-全国止泻类用药_330_品牌',
       '益普生-季度-省份止泻类用药_331_品牌', '英诺珐-月度-三品类-自定义品名_362_品牌',
       '汤臣倍健-全国-蜂胶品类_863_品牌', '汤臣倍健-28城市-蜂胶品类_864_品牌',
       '武田-质子泵抑制剂-全国-月度数据_868_品牌');

update tg_application_info
set assets_qc     = 1,
    project_scope ='merge' where project_name in
('百洋-惠氏-月度-氨糖_395','百洋-惠氏-月度-氨糖_396','百洋-惠氏-月度-氨糖_397','北京法伯-18省份-90通用名-季度_831','北京法伯-4直辖市-90通用名-季度_832','广峰投资-月度-阿胶_102','灵北-神经系统用药-2城市-半年度-2022服务_741','灵北-神经系统用药-2城市-月度-SOP_764','灵北-神经系统用药-9省份-半年度-2022服务_740','灵北-神经系统用药-9省份-月度-SOP_763','灵北-神经系统用药-全国-半年度-2022服务_742','灵北-神经系统用药-全国-月度-SOP_765','马应龙-季度-8城市-肛肠类_140','马应龙-季度-皮肤用药_139','美纳里尼-去疤痕-31城市季度_850','赛诺菲-月度-氨糖数据_700','赛诺菲-月度-维矿类数据_699','森世海亚-金纳多定义品类-全国_658','森世海亚-金纳多定义品类-省份_659','泰德-外用镇痛_416','以岭-感冒药_236','以岭-感冒药_237','以岭-感冒药_238','以岭-心脑血管_194','以岭-心脑血管_195','以岭-心脑血管_196')

update tg_application_info
set assets_qc     = 1,
    project_scope ='split' where project_name in
    ('GSK-季度-肝炎抗病毒-城市_650','GSK-季度-肝炎抗病毒-全国_649','GSK-季度-肝炎抗病毒-省份_651','GSK-季度-抗癫痫-城市_653','GSK-季度-抗癫痫-全国_652','GSK-季度-抗癫痫-省份_654','GSK-季度-慢阻肺-城市_656','GSK-季度-慢阻肺-全国_655','GSK-季度-慢阻肺-省份_657','百洋-骨化三醇_774','百洋-惠氏-月度-钙制剂_392','百洋-惠氏-月度-钙制剂_393','百洋-惠氏-月度-钙制剂_394','勃林格-月度-18省份-慢阻肺IPF_744','勃林格-月度-4直辖市-慢阻肺IPF_745','勃林格-月度-口服降糖overview全国_807','勃林格-月度-口服降糖overview省份_808','勃林格-月度-全国-抗凝血药_727','勃林格-月度-全国-抗帕金森_726','勃林格-月度-全国-口服降糖药DPP4i&SGLT2i_729','勃林格-月度-全国-慢阻肺IPF_728','参天-月度-眼科-全国_519','东阿阿胶-10省份_526','东阿阿胶-2城市_527','东阿阿胶-全国_525','杭州民生-15城市-维矿类数据_854','杭州民生-15城市-维矿类数据_季度_855','惠氏-氨糖-全国-月度_626','惠氏-多维-全国-月度_627','吉利德-抗肝炎病毒-城市-月度_390','吉利德-抗肝炎病毒-全国-月度_389','健合-保健品-全国-月度_20-21年_739','健合-保健品-全国-月度_22-23年_739','健合集团-5品类_539','健合集团-蛋白粉-省份-月度_846','江中-胃肠道用药-全国-月度_76','康恩贝（英诺珐）-月度-口腔咽喉类_172','康恩贝（英诺珐）-月度-口腔咽喉类_浙江省_845','康恩贝-季度-前列腺用药数据-全国_333','康恩贝-月度-前列腺用药数据-52城市_334','罗氏-奥司他韦-全国-月度_824','罗氏-季度-血糖仪_419','罗氏-季度-血糖仪_419_半年度','曼秀雷敦-眼科类-45城市_796','美纳里尼-ED-15省份_852','美纳里尼-ED-全国_851','美纳里尼-ED-直辖市_853','美纳里尼-祛疤痕-全国季度_849','默克-甲状腺-102城市季度_314','默克-口服降糖药-全国月度_426','念慈蓭-30城市-止咳祛痰类-月度_834','念慈蓭-全国-止咳祛痰类-月度_833','强生血糖仪_70','赛诺菲-全国-口服抗过敏_214','赛诺菲-月度-保肝药数据_166','赛诺菲-月度-益生菌_432','山德士-孟鲁司特-2城市_723','山东达因-维D维AD-城市_735','山东达因-维D维AD-全国_733','山东达因-维D维AD-省份_734','施维雅-痔疮及静脉曲张-季度-城市_809','汤臣倍健-14城市2-解热镇痛&减肥&眼科&泌尿&肌肉骨骼&肝胆&高血压&止咳&妇科_531','汤臣倍健-14城市2-维矿_528','汤臣倍健-全国-解热镇痛&减肥&眼科&泌尿&肌肉骨骼&肝胆&高血压&止咳&妇科_535','汤臣倍健-14城市2-胃肠道&神经系统&心脑血管_530','汤臣倍健-14城市2-滋补保健_529','汤臣倍健-14城市-解热镇痛&减肥&眼科&泌尿&肌肉骨骼&肝胆&高血压&止咳&妇科_531','汤臣倍健-14城市-维矿_528','汤臣倍健-14城市-胃肠道&神经系统&心脑血管_530','汤臣倍健-14城市-滋补保健_529','汤臣倍健-7城市-维生素C品类_814','汤臣倍健-全国-维矿_532','汤臣倍健-全国-胃肠道&神经系统&心脑血管_534','汤臣倍健-全国-滋补保健_533','武田-人血白蛋白-城市-季度_830','武田-人血白蛋白-全国-季度_828','武田-人血白蛋白-省份-季度_829','香丹清-月度数据-通便导泻_363','香丹清-月度数据-通便导泻_509','雅培-城市-4品类-季度_754','雅培-全国-4品类-季度_750','雅培-省份-4品类-季度_752','亿腾-3商品名-16省份-季度_712','亿腾-3商品名-5城市-季度_713','亿腾-全国-3细分类-季度_731','益普生-季度-城市止泻类用药_332','益普生-季度-激素与内分泌调节_595','益普生-季度-全国止泻类用药_330','益普生-季度-省份止泻类用药_331','益普生-全国-美沙拉嗪通用名季度数据_424','英诺珐-月度-三品类-自定义品名_362','优时比抗过敏月度-全国','正大清江-季度-全国-氨糖药品_786','正大清江-月度-全国-氨糖药品_176','诺和诺德-全国糖尿病品类_542','诺和诺德-糖尿病用药-15省份-季度_839','诺和诺德-糖尿病用药-直辖市-季度_840','汤臣倍健-全国-蜂胶品类_863','汤臣倍健-28城市-蜂胶品类_864','武田-质子泵抑制剂-全国-月度数据_868');

select application_id, application_name, assets_qc, project_scope from tg_application_task_config;
update tg_application_task_config t left join tg_application_info a on t.application_id = a.id  set t.project_scope = '项目拆分' where a.project_scope = 'split';
update tg_application_task_config t left join tg_application_info a on t.application_id = a.id  set t.project_scope = '项目合并' where a.project_scope = 'merge';
update tg_application_task_config t left join tg_application_info a on t.application_id = a.id  set t.assets_qc = '是' where a.assets_qc = 1;


CREATE TABLE `tg_assets_qc_batch` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'key',
  `batch_no` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '批次号',
  `template_id` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '模板id',
  `template_name` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '模板名称',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '状态',
  `sku_state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '状态',
  `brand_state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '状态',
  `flow_instance_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '工作流实例id',
  `act_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '启动类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `creator` bigint NOT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB   COMMENT='Qc批次'


CREATE TABLE `tg_assets_qc_batch_detail` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'key',
  `batch_id` bigint DEFAULT NULL COMMENT '批次id',
  `application_id` bigint DEFAULT NULL COMMENT '申请id',
  `assets_id` bigint DEFAULT NULL COMMENT '资产id',
  `template_id` bigint DEFAULT NULL COMMENT '模板id',
  `assets_ver` int DEFAULT NULL COMMENT '资产版本',
  `table_name` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资产表名',
  `assets_qc_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'qc类型 sku brand',
  `run_log` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '执行日志',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '状态',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB    COMMENT='Qc 明细'


-----------------------
------------ GP -------

CREATE TABLE temp.qc_apply_config_gray (
  application_id bigint DEFAULT NULL,
  project_name varchar(255)  DEFAULT NULL,
  period_granular varchar(255) ,
  assets_qc varchar(1) NOT NULL DEFAULT '',
  project_scope varchar(4) NOT NULL DEFAULT ''
) distributed by (application_id);

grant delete, insert, select,  truncate on temp.qc_apply_config_gray to etl_sync_user;



-- 灰度函数

-- 项目QC ysg定制
CREATE OR REPLACE FUNCTION etl.generate_sop_brand_sku_abnormal_data_ysg_gray() RETURNS text AS $BODY$
DECLARE
-- current_period VARCHAR := (SELECT max(period) from  edw.cmh_fd_data) ;
current_period VARCHAR := (select max(period) from edw.cmh_qc_sop where upper (type) ='CMH' and upper(prodcode) like'P%' and out_prodcode is not null);
-- 20240313 chair 期数识别改为依赖【已完成新期数的品类QC】表
last_period varchar :=(SELECT  to_char(date(current_period||'01') - INTERVAL  '1 months','yyyymm' ));
 BEGIN
	/*     created_at 2024-06-17 by kuangcp 原函数 generate_sop_brand_sku_abnormal_data 修改而来 	*/
	--------------------------sku--------------------------

			-- 1、预处理，获取所需的所有字段
			drop TABLE if EXISTS sop_sku_init_t1;
			CREATE TEMPORARY table sop_sku_init_t1 as
			SELECT v1.project_name,v3.project_id,project_scope,v1.period,province,city_co_name,project_zoneclass
			,v1.std_id,coalesce(v1.brand,'')||coalesce(gg,'') as sku_name ,v1.prodcode ,sort1,sort2,sort3
			,case when v2.prodcode is not null then 'YES'  else 'No' end  as is_focus_brand
-- 			,fd_xsl::numeric(32,0),avg_dj::numeric(32,1),tz_fdxse::numeric(32,0)
			 ,round(fd_xsl-0.005,2)::numeric(32,0)as  fd_xsl, round(avg_dj-0.005,2)::numeric(32,1) as avg_dj,round(tz_fdxse-0.005,2)::numeric(32,0) as tz_fdxse
			from temp.cmh_enlarge_project_ysg_sku_gray v1
			left join (SELECT prodcode,brand  from etl.cmh_fd_data_brand )v2 on v1.prodcode = v2.prodcode and v1.brand=v2.brand
			join (SELECT project_name , project_scope , application_id as project_id
						 from temp.qc_apply_config_gray
						where assets_qc = '是'  -- 只有填了需要sop 才需要处理
						and period_granular = '月度') v3 on v1.project_name =v3.project_name
	-- 			where   period_granular = '月度' and  zone_name in ('全国','省份','城市') -- 2022-10-09 叶秀提议将 period_granular = '月度' 放入V3

			where     project_zoneclass in ('全国','省份','城市')
			and  v1.std_id > 0 -- likz20220624增加 http://wiki.sinohealth.cn/pages/viewpage.action?pageId=93913104  第24条
			distributed by (project_name,std_id,period);


			-- 2、根据城市维度，排列顺序，获取80%的标志，计算份额
			drop TABLE if EXISTS sop_sku_init_t2;
			CREATE TEMPORARY table sop_sku_init_t2 as
			SELECT
			v1.*
			,case when v2.all_tz_fdxse = 0 then 0
						when coalesce(project_scope,'') != '项目合并' then  (tz_fdxse*1.0/v2.all_tz_fdxse)::numeric(32,3)
						when v3.all_tz_fdxse = 0 then 0
						when project_scope = '项目合并' then  (tz_fdxse*1.0/v3.all_tz_fdxse)::numeric(32,3)
						end as market_share
			,case when   coalesce(project_scope,'') != '项目合并' and  px*1.0/v2.cnt <= 0.8 then 'YES'
						when   coalesce(project_scope,'') != '项目合并' then 'No'
						when   project_scope = '项目合并' and  px*1.0/v3.cnt <= 0.8 then 'YES'
						when   project_scope = '项目合并' then 'No'
						end as top_sku_mark
			from (
					SELECT
					*
					,row_number() over(PARTITION by project_name,period,city_co_name order by tz_fdxse desc) as px
					from  sop_sku_init_t1
			-- 		where project_name = '拜耳-月度-四通用名（硝苯地平+阿司匹林+阿卡波糖+利伐沙班）及口服避孕药-省份_1'
			)v1
			--  不是项目合并，到品类
			left join (SELECT project_name,period,city_co_name,prodcode,count(2) as cnt,sum(tz_fdxse) as all_tz_fdxse
					 from  sop_sku_init_t1
					 where  coalesce(project_scope,'') != '项目合并'
			-- 		 and  project_name = '拜耳-月度-四通用名（硝苯地平+阿司匹林+阿卡波糖+利伐沙班）及口服避孕药-省份_1'
					 GROUP BY 1,2,3,4) v2  on v1.project_name =v2.project_name and v1.period=v2.period
																 and v1.city_co_name=v2.city_co_name and v1.prodcode = v2.prodcode
			-- 项目合并到地区
			left join (SELECT project_name,period,city_co_name,count(2) as cnt,sum(tz_fdxse) as all_tz_fdxse
					 from  sop_sku_init_t1
					 where  project_scope = '项目合并'
			-- 		 and project_name = '拜耳-月度-四通用名（硝苯地平+阿司匹林+阿卡波糖+利伐沙班）及口服避孕药-省份_1'
					 GROUP BY 1,2,3) v3  on v1.project_name =v3.project_name and v1.period=v3.period
																	 and v1.city_co_name=v3.city_co_name
			distributed by (project_name,std_id,period);



			-- 3、获取各字段 tz_fdxse,fd_xsl,market_share,dj的同环比 ， 单价同环比差  如 period = '202106'
			drop TABLE if EXISTS sop_sku_init_t3;
			CREATE TEMPORARY table sop_sku_init_t3 as
			SELECT
			v1.project_name,v1.project_id,v1.city_co_name,v1.project_zoneclass,v1.std_id,v1.sku_name
			,v1.prodcode,v1.sort1,v1.sort2,v1.top_sku_mark,v1.is_focus_brand
			,v2.tz_fdxse as last_month_xse,v1.tz_fdxse as xse
			,case when v2.tz_fdxse = 0 then 0 else (v1.tz_fdxse/v2.tz_fdxse - 1)::numeric(32,3)  end  as xse_hb
			,v2.market_share as last_month_market_share,v1.market_share as market_share
			,(v1.market_share - v2.market_share)::numeric(32,3)   as market_share_hb
			,v2.avg_dj as last_month_dj,v1.avg_dj as dj
			,case when v2.avg_dj = 0 then 0 else (v1.avg_dj/v2.avg_dj - 1)::numeric(32,3)  end  as dj_hb
			,v1.avg_dj - v2.avg_dj as dj_diff
			from (SELECT * from sop_sku_init_t2 where period = current_period)v1
			left join (SELECT * from sop_sku_init_t2 where period = last_period) v2
								 on v1.project_name =v2.project_name  and v1.city_co_name=v2.city_co_name and v1.std_id =v2.std_id
			distributed by (project_name,std_id,city_co_name);

			-- 4、获取异常字段
			truncate table temp.sop_sku_abnormal_data_ysg_gray;
			insert into temp.sop_sku_abnormal_data_ysg_gray
			SELECT
			current_period,
			v1.*
			,max(case when  abs(v1.xse_hb) >= v2.xse_hb and index_type = '销售额' then 'YES' else 'No' end)  abnormal_xse_hb
			,max(case when  abs(v1.market_share_hb) >= v2.market_share_hb and index_type = '销售份额' then 'YES' else 'No' end)  abnormal_share_hb
			,max(case when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_hb)>= v2.dj_hb and index_type = '单价'
									 then 'YES' else 'No' end)  abnormal_dj_hb
			,max(case when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_diff) >= v2.dj_diff and index_type = '单价'
									 then 'YES' else 'No' end)  abnormal_dj_diff
			from sop_sku_init_t3 v1
			join ods.cmh_sop_abnormal_threshold v2 on v1.prodcode = v2.prodcode and v1.project_zoneclass=v2.region
			left join ods.cmh_sop_scale_except v3 on v1.prodcode = v3.prodcode and v3.brand_sku = 'SKU'
			-- 2021-08-09 嫚嫚新增字段sort2
			and case when length(v3.sort2)>1 and position(v1.sort2 in v3.sort2) = 0 then false else TRUE end
			 -- 规模除外，不需要展示小规模的数据,全部规模都小于，则不展示
				and case when abs(v1.last_month_xse) < v3.last_month_xse and v1.xse < v3.xse
								 and abs(v1.last_month_market_share) < v3.last_month_market_share
								 and abs(v1.market_share) < v3.market_share   then true
						 else false  end
			where
				-- 指标异常阈值判断
					 case when  abs(v1.xse_hb) >= v2.xse_hb and index_type = '销售额' then true
								 when  abs(v1.market_share_hb) >= v2.market_share_hb and index_type = '销售份额'  then true
								 when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_hb)>= v2.dj_hb  and index_type = '单价' then true
								 when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_diff) >= v2.dj_diff and index_type = '单价'  then true
						 end
					 -- 如果有 project_id 或者 sort2  ，那么，如果不一致，则非异常
					 and case when length(v2.project_id)>1  and position(v1.project_id::varchar in v2.project_id) = 0 then false
								when length(v2.sort2)>1 and position(v1.sort2 in v2.sort2) = 0 then false
					 else true end
			 and v2.brand_sku = 'SKU'
			 and v3.prodcode is null
			 GROUP BY current_period,v1.project_name,v1.project_id,v1.city_co_name,v1.project_zoneclass,v1.std_id,v1.sku_name,v1.prodcode
								,v1.sort1,v1.sort2,v1.top_sku_mark,v1.is_focus_brand,v1.last_month_xse,v1.xse,v1.xse_hb
								,v1.last_month_market_share,v1.market_share,v1.market_share_hb,v1.last_month_dj,v1.dj,v1.dj_hb,v1.dj_diff
		;

		--------------------------brand-------------------------
-- 	1、预处理，获取所需的所有字段
			drop TABLE if EXISTS sop_brand_init_t1;
			CREATE TEMPORARY table sop_brand_init_t1 as
			SELECT v1.project_name,v3.project_id,project_scope,period,province,city_co_name,zone_name as project_zoneclass
			,v1.brand,v1.prodcode ,v1.sort1,v4.sort2
			,case when v2.prodcode is not null then 'YES'  else 'No' end  as is_focus_brand
-- 			,fdxsl::numeric(32,0) as fd_xsl,avg_dj::numeric(32,1),fdxse::numeric(32,0)  as tz_fdxse
			,round(fdxsl-0.005,2)::numeric(32,0) as fdxsl, round(avg_dj-0.005,2)::numeric(32,1) as avg_dj,round(fdxse-0.005,2)::numeric(32,0) as tz_fdxse
			from temp.test_cmh_enlarge_project_ysg_brand v1 -- limit 100
			left join (SELECT prodcode,brand  from etl.cmh_fd_data_brand )v2 on v1.prodcode = v2.prodcode and v1.brand=v2.brand
			join (SELECT project_name , project_scope , application_id as project_id
						 from temp.qc_apply_config_gray
						where assets_qc = '是'  -- 只有填了需要sop 才需要处理
						and period_granular = '月度') v3 on v1.project_name =v3.project_name
			left join (SELECT brand,prodcode,translate(array_agg(distinct sort2 )::VARCHAR,'{}','') as sort2
								from edw.cmh_dw_standard_collection
								where prodcode !='P099' and status not in ('禁用','回收站')
								GROUP BY 1,2
							 )v4 on v1.prodcode = v4.prodcode and v1.brand=v4.brand
-- 			where   period_granular = '月度' and  zone_name in ('全国','省份','城市') -- 2022-10-09 叶秀提议将 period_granular = '月度' 放入V3
      where     zone_name in ('全国','省份','城市')
			and length(v1.brand)>0 -- likz20220624增加 http://wiki.sinohealth.cn/pages/viewpage.action?pageId=93913104  第24条
			distributed by (project_name,brand,period);


-- 2、根据城市维度，排列顺序，获取80%的标志，计算份额
        drop TABLE if EXISTS sop_brand_init_t2;
        CREATE TEMPORARY table sop_brand_init_t2 as
        SELECT
        v1.*
        ,case when v2.all_tz_fdxse = 0 then 0
                    when coalesce(project_scope,'') != '项目合并' then  (tz_fdxse*1.0/v2.all_tz_fdxse)::numeric(32,3)
                    when v3.all_tz_fdxse = 0 then 0
                    when project_scope = '项目合并' then  (tz_fdxse*1.0/v3.all_tz_fdxse)::numeric(32,3)
                    end as market_share
        ,case when   coalesce(project_scope,'') != '项目合并' and  px*1.0/v2.cnt <= 0.8 then 'YES'
                    when   coalesce(project_scope,'') != '项目合并' then 'No'
                    when   project_scope = '项目合并' and  px*1.0/v3.cnt <= 0.8 then 'YES'
                    when   project_scope = '项目合并' then 'No'
                    end as top_brand_mark
        from (
                SELECT
                *
                ,row_number() over(PARTITION by project_name,period,city_co_name order by tz_fdxse desc) as px
                from  sop_brand_init_t1
        -- 		where project_name = '拜耳-月度-四通用名（硝苯地平+阿司匹林+阿卡波糖+利伐沙班）及口服避孕药-省份_1'
        )v1
        --  不是项目合并，到品类
        left join (SELECT project_name,period,city_co_name,prodcode,count(2) as cnt,sum(tz_fdxse) as all_tz_fdxse
                 from  sop_brand_init_t1
                 where  coalesce(project_scope,'') != '项目合并'
        -- 		 and  project_name = '拜耳-月度-四通用名（硝苯地平+阿司匹林+阿卡波糖+利伐沙班）及口服避孕药-省份_1'
                 GROUP BY 1,2,3,4) v2  on v1.project_name =v2.project_name and v1.period=v2.period
                                                             and v1.city_co_name=v2.city_co_name and v1.prodcode = v2.prodcode
        -- 项目合并到地区
        left join (SELECT project_name,period,city_co_name,count(2) as cnt,sum(tz_fdxse) as all_tz_fdxse
                 from  sop_brand_init_t1
                 where  project_scope = '项目合并'
        -- 		 and project_name = '拜耳-月度-四通用名（硝苯地平+阿司匹林+阿卡波糖+利伐沙班）及口服避孕药-省份_1'
                 GROUP BY 1,2,3) v3  on v1.project_name =v3.project_name and v1.period=v3.period
                                                                 and v1.city_co_name=v3.city_co_name
        distributed by (project_name,brand,period);


-- 3、获取各字段 tz_fdxse,fd_xsl,market_share,dj的同环比 ， 单价同环比差
			drop TABLE if EXISTS sop_brand_init_t3;
			CREATE TEMPORARY table sop_brand_init_t3 as
			SELECT
			v1.project_name,v1.project_id,v1.city_co_name,v1.project_zoneclass,v1.brand
			,v1.prodcode,v1.sort1,v1.sort2,v1.top_brand_mark,v1.is_focus_brand
			,v2.tz_fdxse as last_month_xse,v1.tz_fdxse as xse
			,case when v2.tz_fdxse = 0 then 0 else (v1.tz_fdxse/v2.tz_fdxse - 1)::numeric(32,3)  end  as xse_hb
			,v2.market_share as last_month_market_share,v1.market_share as market_share
			,(v1.market_share - v2.market_share)::numeric(32,3)    as market_share_hb
			,v2.avg_dj as last_month_dj,v1.avg_dj as dj
			,case when v2.avg_dj = 0 then 0 else (v1.avg_dj/v2.avg_dj - 1)::numeric(32,3)  end  as dj_hb
			,v1.avg_dj - v2.avg_dj as dj_diff
			from (SELECT * from sop_brand_init_t2 where period = current_period)v1
			left join (SELECT * from sop_brand_init_t2 where period = last_period) v2
								 on v1.project_name =v2.project_name  and v1.city_co_name=v2.city_co_name and v1.brand =v2.brand
			distributed by (project_name,brand,city_co_name);


-- 4、获取异常字段
				truncate table temp.sop_brand_abnormal_data_ysg_gray;
				insert into temp.sop_brand_abnormal_data_ysg_gray
				SELECT
				current_period,
				v1.*
				,max(case when  abs(v1.xse_hb) >= v2.xse_hb and index_type = '销售额' then 'YES' else 'No' end)  abnormal_xse_hb
				,max(case when  abs(v1.market_share_hb) >= v2.market_share_hb and index_type = '销售份额' then 'YES' else 'No' end)  abnormal_share_hb
				,max(case when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_hb)>= v2.dj_hb and index_type = '单价'
										 then 'YES' else 'No' end)  abnormal_dj_hb
				,max(case when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_diff) >= v2.dj_diff and index_type = '单价'
										 then 'YES' else 'No' end)  abnormal_dj_diff
				from sop_brand_init_t3 v1
				join ods.cmh_sop_abnormal_threshold v2 on v1.prodcode = v2.prodcode and v1.project_zoneclass=v2.region
				left join ods.cmh_sop_scale_except v3 on v1.prodcode = v3.prodcode and v3.brand_sku = '品牌'
				-- 2021-08-09 嫚嫚新增字段sort2
			    and case when length(v3.sort2)>1 and position(v1.sort2 in v3.sort2) = 0 then false else TRUE end
				 -- 规模除外，不需要展示小规模的数据,全部规模都小于，则不展示。任一一个大于的，都展示。
					and case when abs(v1.last_month_xse) < v3.last_month_xse and v1.xse < v3.xse
									 and abs(v1.last_month_market_share) < v3.last_month_market_share
									 and abs(v1.market_share) < v3.market_share   then true
							 else false  end
				where
					-- 指标异常阈值判断
						 case when  abs(v1.xse_hb) >= v2.xse_hb and index_type = '销售额' then true
									 when  abs(v1.market_share_hb) >= v2.market_share_hb and index_type = '销售份额'  then true
									 when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_hb)>= v2.dj_hb  and index_type = '单价' then true
									 when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_diff) >= v2.dj_diff and index_type = '单价'  then true
							 end
						 -- 如果有 project_id 或者 sort2  ，那么，如果不一致，则非异常
						 and case when length(v2.project_id)>1 and position(v1.project_id::varchar in v2.project_id) = 0 then false
									when length(v2.sort2)>1 and position(v1.sort2 in v2.sort2) = 0 then false
						 else true end
				 and v2.brand_sku = '品牌'
				 and v3.prodcode is null
				 GROUP BY current_period,v1.project_name,v1.project_id,v1.city_co_name,v1.project_zoneclass,v1.brand,v1.prodcode,v1.sort1,v1.sort2
									,v1.top_brand_mark,v1.is_focus_brand,v1.last_month_xse,v1.xse,v1.xse_hb,v1.last_month_market_share
									,v1.market_share,v1.market_share_hb,v1.last_month_dj,v1.dj,v1.dj_hb,v1.dj_diff;

	RETURN  '生成 sop异常数据表 完毕！ ';

END

$BODY$ LANGUAGE plpgsql volatile;



--------------
CREATE TABLE dm.qc_apply_config (
  application_id bigint DEFAULT NULL,
  project_name varchar(255)  DEFAULT NULL,
  period_granular varchar(255) ,
  assets_qc varchar(1) NOT NULL DEFAULT '',
  project_scope varchar(4) NOT NULL DEFAULT ''
) distributed by (application_id);

grant delete, insert, select,  truncate on dm.qc_apply_config to etl_sync_user;

create table dm.cmh_enlarge_project_ysg_sku
(
    period            varchar(10),
    province          varchar(300),
    city_co_name      varchar(50),
    std_id            numeric,
    prodcode          varchar(255),
    otc_rx            varchar(10),
    sort1             varchar(255),
    sort2             varchar(255),
    sort3             varchar(255),
    sort4             varchar(255),
    zx                varchar(20),
    brand             varchar(500),
    tym               varchar(500),
    pm_all            varchar(500),
    pm                varchar(500),
    cj                varchar(500),
    gg                varchar,
    jx                varchar(50),
    dx                varchar(50),
    avg_dj            numeric,
    fd_xse            numeric,
    tz_fdxse          numeric,
    sample_xsl        numeric,
    sz_phl            numeric,
    jq_phl            numeric,
    sample_xse        numeric,
    project_name      varchar(255),
    project_zoneclass varchar(255),
    period_granular   varchar(255),
    fd_xsl            numeric,
    ddu               numeric(100),
    tv                varchar(255),
    vpd               varchar(255),
    otherstag         integer,
    spm               varchar(255),
    company_right     varchar(255),
    sc_old_label      varchar(4000),
    short_cj          varchar(500),
    short_brand       varchar(500)
)
    distributed by (project_name, period, city_co_name, std_id);

alter table dm.cmh_enlarge_project_ysg_sku
    owner to hufan;

grant delete, insert, references, select, trigger, truncate, update on dm.cmh_enlarge_project_ysg_sku to etl_sync_user;

grant select on dm.cmh_enlarge_project_ysg_sku to zhongqiting;



create table dm.cmh_enlarge_project_ysg_brand
(
    project_name    varchar,
    zone_name       varchar,
    period_granular varchar,
    period          varchar,
    province        varchar,
    city_co_name    varchar,
    prodcode        varchar(10),
    sort1           varchar(20),
    brand           varchar(500),
    avg_dj          numeric,
    fdxsl           numeric,
    fdxse           numeric,
    sz_phl          numeric,
    jq_phl          numeric,
    dj_cw           numeric(32, 2),
    fdxsl_cw        numeric(32, 2),
    fdxse_cw        numeric(32, 2)
)
    distributed randomly;

alter table dm.cmh_enlarge_project_ysg_brand
    owner to hufan;

grant delete, insert, references, select, trigger, truncate, update on dm.cmh_enlarge_project_ysg_brand to zhongqiting;




create table dm.sop_sku_abnormal_data_ysg
(
    current_period          varchar,
    project_name            varchar(255),
    project_id              integer,
    city_co_name            varchar(50),
    project_zoneclass       varchar(255),
    std_id                  numeric,
    sku_name                text,
    prodcode                varchar(255),
    sort1                   varchar(255),
    sort2                   varchar(255),
    top_sku_mark            text,
    is_focus_brand          text,
    last_month_xse          numeric(32),
    xse                     numeric(32),
    xse_hb                  numeric,
    last_month_market_share numeric,
    market_share            numeric,
    market_share_hb         numeric,
    last_month_dj           numeric(32, 1),
    dj                      numeric(32, 1),
    dj_hb                   numeric,
    dj_diff                 numeric,
    abnormal_xse_hb         text,
    abnormal_share_hb       text,
    abnormal_dj_hb          text,
    abnormal_dj_diff        text,
    update_date             timestamp(0) default now()
)
    distributed by (std_id, prodcode, sort2);

create table dm.sop_brand_abnormal_data_ysg
(
    current_period          varchar,
    project_name            varchar,
    project_id              integer,
    city_co_name            varchar,
    project_zoneclass       varchar,
    brand                   varchar(500),
    prodcode                varchar(10),
    sort1                   varchar(20),
    sort2                   text,
    top_brand_mark          text,
    is_focus_brand          text,
    last_month_xse          numeric(32),
    xse                     numeric(32),
    xse_hb                  numeric,
    last_month_market_share numeric,
    market_share            numeric,
    market_share_hb         numeric,
    last_month_dj           numeric(32, 1),
    dj                      numeric(32, 1),
    dj_hb                   numeric,
    dj_diff                 numeric,
    abnormal_xse_hb         text,
    abnormal_share_hb       text,
    abnormal_dj_hb          text,
    abnormal_dj_diff        text,
    update_date             timestamp(0) default now()
)
    distributed by (brand, prodcode, sort2);


grant select on dm.sop_brand_abnormal_data_ysg to zhongqiting;
grant select on dm.sop_sku_abnormal_data_ysg to zhongqiting;


-- 项目QC ysg定制
CREATE OR REPLACE FUNCTION etl.generate_sop_brand_sku_abnormal_data_ysg() RETURNS text AS $BODY$
DECLARE
-- current_period VARCHAR := (SELECT max(period) from  edw.cmh_fd_data) ;
current_period VARCHAR := (select max(period) from edw.cmh_qc_sop where upper (type) ='CMH' and upper(prodcode) like'P%' and out_prodcode is not null);
-- 20240313 chair 期数识别改为依赖【已完成新期数的品类QC】表
last_period varchar :=(SELECT  to_char(date(current_period||'01') - INTERVAL  '1 months','yyyymm' ));
 BEGIN
	/*     created_at 2024-06-17 by kuangcp 原函数 generate_sop_brand_sku_abnormal_data 修改而来 	*/
	--------------------------sku--------------------------

			-- 1、预处理，获取所需的所有字段
			drop TABLE if EXISTS sop_sku_init_t1;
			CREATE TEMPORARY table sop_sku_init_t1 as
			SELECT v1.project_name,v3.project_id,project_scope,v1.period,province,city_co_name,project_zoneclass
			,v1.std_id,coalesce(v1.brand,'')||coalesce(gg,'') as sku_name ,v1.prodcode ,sort1,sort2,sort3
			,case when v2.prodcode is not null then 'YES'  else 'No' end  as is_focus_brand
			,fd_xsl::numeric(32,0),avg_dj::numeric(32,1),tz_fdxse::numeric(32,0)
-- 			 ,round(fd_xsl-0.005,2)::numeric(32,0)as  fd_xsl, round(avg_dj-0.005,2)::numeric(32,1) as avg_dj,round(tz_fdxse-0.005,2)::numeric(32,0) as tz_fdxse
			from dm.cmh_enlarge_project_ysg_sku v1
			left join (SELECT prodcode,brand  from etl.cmh_fd_data_brand )v2 on v1.prodcode = v2.prodcode and v1.brand=v2.brand
			join (SELECT project_name , project_scope , application_id as project_id
						 from dm.qc_apply_config
						where assets_qc = '是'  -- 只有填了需要sop 才需要处理
						and period_granular = '月度') v3 on v1.project_name =v3.project_name
	-- 			where   period_granular = '月度' and  zone_name in ('全国','省份','城市') -- 2022-10-09 叶秀提议将 period_granular = '月度' 放入V3

			where     project_zoneclass in ('全国','省份','城市')
			and  v1.std_id > 0 -- likz20220624增加 http://wiki.sinohealth.cn/pages/viewpage.action?pageId=93913104  第24条
			distributed by (project_name,std_id,period);


			-- 2、根据城市维度，排列顺序，获取80%的标志，计算份额
			drop TABLE if EXISTS sop_sku_init_t2;
			CREATE TEMPORARY table sop_sku_init_t2 as
			SELECT
			v1.*
			,case when v2.all_tz_fdxse = 0 then 0
						when coalesce(project_scope,'') != '项目合并' then  (tz_fdxse*1.0/v2.all_tz_fdxse)::numeric(32,3)
						when v3.all_tz_fdxse = 0 then 0
						when project_scope = '项目合并' then  (tz_fdxse*1.0/v3.all_tz_fdxse)::numeric(32,3)
						end as market_share
			,case when   coalesce(project_scope,'') != '项目合并' and  px*1.0/v2.cnt <= 0.8 then 'YES'
						when   coalesce(project_scope,'') != '项目合并' then 'No'
						when   project_scope = '项目合并' and  px*1.0/v3.cnt <= 0.8 then 'YES'
						when   project_scope = '项目合并' then 'No'
						end as top_sku_mark
			from (
					SELECT
					*
					,row_number() over(PARTITION by project_name,period,city_co_name order by tz_fdxse desc) as px
					from  sop_sku_init_t1
			-- 		where project_name = '拜耳-月度-四通用名（硝苯地平+阿司匹林+阿卡波糖+利伐沙班）及口服避孕药-省份_1'
			)v1
			--  不是项目合并，到品类
			left join (SELECT project_name,period,city_co_name,prodcode,count(2) as cnt,sum(tz_fdxse) as all_tz_fdxse
					 from  sop_sku_init_t1
					 where  coalesce(project_scope,'') != '项目合并'
			-- 		 and  project_name = '拜耳-月度-四通用名（硝苯地平+阿司匹林+阿卡波糖+利伐沙班）及口服避孕药-省份_1'
					 GROUP BY 1,2,3,4) v2  on v1.project_name =v2.project_name and v1.period=v2.period
																 and v1.city_co_name=v2.city_co_name and v1.prodcode = v2.prodcode
			-- 项目合并到地区
			left join (SELECT project_name,period,city_co_name,count(2) as cnt,sum(tz_fdxse) as all_tz_fdxse
					 from  sop_sku_init_t1
					 where  project_scope = '项目合并'
			-- 		 and project_name = '拜耳-月度-四通用名（硝苯地平+阿司匹林+阿卡波糖+利伐沙班）及口服避孕药-省份_1'
					 GROUP BY 1,2,3) v3  on v1.project_name =v3.project_name and v1.period=v3.period
																	 and v1.city_co_name=v3.city_co_name
			distributed by (project_name,std_id,period);



			-- 3、获取各字段 tz_fdxse,fd_xsl,market_share,dj的同环比 ， 单价同环比差  如 period = '202106'
			drop TABLE if EXISTS sop_sku_init_t3;
			CREATE TEMPORARY table sop_sku_init_t3 as
			SELECT
			v1.project_name,v1.project_id,v1.city_co_name,v1.project_zoneclass,v1.std_id,v1.sku_name
			,v1.prodcode,v1.sort1,v1.sort2,v1.top_sku_mark,v1.is_focus_brand
			,v2.tz_fdxse as last_month_xse,v1.tz_fdxse as xse
			,case when v2.tz_fdxse = 0 then 0 else (v1.tz_fdxse/v2.tz_fdxse - 1)::numeric(32,3)  end  as xse_hb
			,v2.market_share as last_month_market_share,v1.market_share as market_share
			,(v1.market_share - v2.market_share)::numeric(32,3)   as market_share_hb
			,v2.avg_dj as last_month_dj,v1.avg_dj as dj
			,case when v2.avg_dj = 0 then 0 else (v1.avg_dj/v2.avg_dj - 1)::numeric(32,3)  end  as dj_hb
			,v1.avg_dj - v2.avg_dj as dj_diff
			from (SELECT * from sop_sku_init_t2 where period = current_period)v1
			left join (SELECT * from sop_sku_init_t2 where period = last_period) v2
								 on v1.project_name =v2.project_name  and v1.city_co_name=v2.city_co_name and v1.std_id =v2.std_id
			distributed by (project_name,std_id,city_co_name);

			-- 4、获取异常字段
			truncate table dm.sop_sku_abnormal_data_ysg;
			insert into dm.sop_sku_abnormal_data_ysg
			SELECT
			current_period,
			v1.*
			,max(case when  abs(v1.xse_hb) >= v2.xse_hb and index_type = '销售额' then 'YES' else 'No' end)  abnormal_xse_hb
			,max(case when  abs(v1.market_share_hb) >= v2.market_share_hb and index_type = '销售份额' then 'YES' else 'No' end)  abnormal_share_hb
			,max(case when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_hb)>= v2.dj_hb and index_type = '单价'
									 then 'YES' else 'No' end)  abnormal_dj_hb
			,max(case when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_diff) >= v2.dj_diff and index_type = '单价'
									 then 'YES' else 'No' end)  abnormal_dj_diff
			from sop_sku_init_t3 v1
			join ods.cmh_sop_abnormal_threshold v2 on v1.prodcode = v2.prodcode and v1.project_zoneclass=v2.region
			left join ods.cmh_sop_scale_except v3 on v1.prodcode = v3.prodcode and v3.brand_sku = 'SKU'
			-- 2021-08-09 嫚嫚新增字段sort2
			and case when length(v3.sort2)>1 and position(v1.sort2 in v3.sort2) = 0 then false else TRUE end
			 -- 规模除外，不需要展示小规模的数据,全部规模都小于，则不展示
				and case when abs(v1.last_month_xse) < v3.last_month_xse and v1.xse < v3.xse
								 and abs(v1.last_month_market_share) < v3.last_month_market_share
								 and abs(v1.market_share) < v3.market_share   then true
						 else false  end
			where
				-- 指标异常阈值判断
					 case when  abs(v1.xse_hb) >= v2.xse_hb and index_type = '销售额' then true
								 when  abs(v1.market_share_hb) >= v2.market_share_hb and index_type = '销售份额'  then true
								 when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_hb)>= v2.dj_hb  and index_type = '单价' then true
								 when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_diff) >= v2.dj_diff and index_type = '单价'  then true
						 end
					 -- 如果有 project_id 或者 sort2  ，那么，如果不一致，则非异常
					 and case when length(v2.project_id)>1  and position(v1.project_id::varchar in v2.project_id) = 0 then false
								when length(v2.sort2)>1 and position(v1.sort2 in v2.sort2) = 0 then false
					 else true end
			 and v2.brand_sku = 'SKU'
			 and v3.prodcode is null
			 GROUP BY current_period,v1.project_name,v1.project_id,v1.city_co_name,v1.project_zoneclass,v1.std_id,v1.sku_name,v1.prodcode
								,v1.sort1,v1.sort2,v1.top_sku_mark,v1.is_focus_brand,v1.last_month_xse,v1.xse,v1.xse_hb
								,v1.last_month_market_share,v1.market_share,v1.market_share_hb,v1.last_month_dj,v1.dj,v1.dj_hb,v1.dj_diff
		;

		--------------------------brand-------------------------
-- 	1、预处理，获取所需的所有字段
			drop TABLE if EXISTS sop_brand_init_t1;
			CREATE TEMPORARY table sop_brand_init_t1 as
			SELECT v1.project_name,v3.project_id,project_scope,period,province,city_co_name,zone_name as project_zoneclass
			,v1.brand,v1.prodcode ,v1.sort1,v4.sort2
			,case when v2.prodcode is not null then 'YES'  else 'No' end  as is_focus_brand
			,fdxsl::numeric(32,0) as fd_xsl,avg_dj::numeric(32,1),fdxse::numeric(32,0)  as tz_fdxse
-- 			,round(fdxsl-0.005,2)::numeric(32,0) as fdxsl, round(avg_dj-0.005,2)::numeric(32,1) as avg_dj,round(fdxse-0.005,2)::numeric(32,0) as tz_fdxse
			from dm.cmh_enlarge_project_ysg_brand v1 -- limit 100
			left join (SELECT prodcode,brand  from etl.cmh_fd_data_brand )v2 on v1.prodcode = v2.prodcode and v1.brand=v2.brand
			join (SELECT project_name , project_scope , application_id as project_id
						 from dm.qc_apply_config
						where assets_qc = '是'  -- 只有填了需要sop 才需要处理
						and period_granular = '月度') v3 on v1.project_name =v3.project_name
			left join (SELECT brand,prodcode,translate(array_agg(distinct sort2 )::VARCHAR,'{}','') as sort2
								from edw.cmh_dw_standard_collection
								where prodcode !='P099' and status not in ('禁用','回收站')
								GROUP BY 1,2
							 )v4 on v1.prodcode = v4.prodcode and v1.brand=v4.brand
-- 			where   period_granular = '月度' and  zone_name in ('全国','省份','城市') -- 2022-10-09 叶秀提议将 period_granular = '月度' 放入V3
      where     zone_name in ('全国','省份','城市')
			and length(v1.brand)>0 -- likz20220624增加 http://wiki.sinohealth.cn/pages/viewpage.action?pageId=93913104  第24条
			distributed by (project_name,brand,period);


-- 2、根据城市维度，排列顺序，获取80%的标志，计算份额
        drop TABLE if EXISTS sop_brand_init_t2;
        CREATE TEMPORARY table sop_brand_init_t2 as
        SELECT
        v1.*
        ,case when v2.all_tz_fdxse = 0 then 0
                    when coalesce(project_scope,'') != '项目合并' then  (tz_fdxse*1.0/v2.all_tz_fdxse)::numeric(32,3)
                    when v3.all_tz_fdxse = 0 then 0
                    when project_scope = '项目合并' then  (tz_fdxse*1.0/v3.all_tz_fdxse)::numeric(32,3)
                    end as market_share
        ,case when   coalesce(project_scope,'') != '项目合并' and  px*1.0/v2.cnt <= 0.8 then 'YES'
                    when   coalesce(project_scope,'') != '项目合并' then 'No'
                    when   project_scope = '项目合并' and  px*1.0/v3.cnt <= 0.8 then 'YES'
                    when   project_scope = '项目合并' then 'No'
                    end as top_brand_mark
        from (
                SELECT
                *
                ,row_number() over(PARTITION by project_name,period,city_co_name order by tz_fdxse desc) as px
                from  sop_brand_init_t1
        -- 		where project_name = '拜耳-月度-四通用名（硝苯地平+阿司匹林+阿卡波糖+利伐沙班）及口服避孕药-省份_1'
        )v1
        --  不是项目合并，到品类
        left join (SELECT project_name,period,city_co_name,prodcode,count(2) as cnt,sum(tz_fdxse) as all_tz_fdxse
                 from  sop_brand_init_t1
                 where  coalesce(project_scope,'') != '项目合并'
        -- 		 and  project_name = '拜耳-月度-四通用名（硝苯地平+阿司匹林+阿卡波糖+利伐沙班）及口服避孕药-省份_1'
                 GROUP BY 1,2,3,4) v2  on v1.project_name =v2.project_name and v1.period=v2.period
                                                             and v1.city_co_name=v2.city_co_name and v1.prodcode = v2.prodcode
        -- 项目合并到地区
        left join (SELECT project_name,period,city_co_name,count(2) as cnt,sum(tz_fdxse) as all_tz_fdxse
                 from  sop_brand_init_t1
                 where  project_scope = '项目合并'
        -- 		 and project_name = '拜耳-月度-四通用名（硝苯地平+阿司匹林+阿卡波糖+利伐沙班）及口服避孕药-省份_1'
                 GROUP BY 1,2,3) v3  on v1.project_name =v3.project_name and v1.period=v3.period
                                                                 and v1.city_co_name=v3.city_co_name
        distributed by (project_name,brand,period);


-- 3、获取各字段 tz_fdxse,fd_xsl,market_share,dj的同环比 ， 单价同环比差
			drop TABLE if EXISTS sop_brand_init_t3;
			CREATE TEMPORARY table sop_brand_init_t3 as
			SELECT
			v1.project_name,v1.project_id,v1.city_co_name,v1.project_zoneclass,v1.brand
			,v1.prodcode,v1.sort1,v1.sort2,v1.top_brand_mark,v1.is_focus_brand
			,v2.tz_fdxse as last_month_xse,v1.tz_fdxse as xse
			,case when v2.tz_fdxse = 0 then 0 else (v1.tz_fdxse/v2.tz_fdxse - 1)::numeric(32,3)  end  as xse_hb
			,v2.market_share as last_month_market_share,v1.market_share as market_share
			,(v1.market_share - v2.market_share)::numeric(32,3)    as market_share_hb
			,v2.avg_dj as last_month_dj,v1.avg_dj as dj
			,case when v2.avg_dj = 0 then 0 else (v1.avg_dj/v2.avg_dj - 1)::numeric(32,3)  end  as dj_hb
			,v1.avg_dj - v2.avg_dj as dj_diff
			from (SELECT * from sop_brand_init_t2 where period = current_period)v1
			left join (SELECT * from sop_brand_init_t2 where period = last_period) v2
								 on v1.project_name =v2.project_name  and v1.city_co_name=v2.city_co_name and v1.brand =v2.brand
			distributed by (project_name,brand,city_co_name);


-- 4、获取异常字段
				truncate table dm.sop_brand_abnormal_data_ysg;
				insert into dm.sop_brand_abnormal_data_ysg
				SELECT
				current_period,
				v1.*
				,max(case when  abs(v1.xse_hb) >= v2.xse_hb and index_type = '销售额' then 'YES' else 'No' end)  abnormal_xse_hb
				,max(case when  abs(v1.market_share_hb) >= v2.market_share_hb and index_type = '销售份额' then 'YES' else 'No' end)  abnormal_share_hb
				,max(case when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_hb)>= v2.dj_hb and index_type = '单价'
										 then 'YES' else 'No' end)  abnormal_dj_hb
				,max(case when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_diff) >= v2.dj_diff and index_type = '单价'
										 then 'YES' else 'No' end)  abnormal_dj_diff
				from sop_brand_init_t3 v1
				join ods.cmh_sop_abnormal_threshold v2 on v1.prodcode = v2.prodcode and v1.project_zoneclass=v2.region
				left join ods.cmh_sop_scale_except v3 on v1.prodcode = v3.prodcode and v3.brand_sku = '品牌'
				-- 2021-08-09 嫚嫚新增字段sort2
			    and case when length(v3.sort2)>1 and position(v1.sort2 in v3.sort2) = 0 then false else TRUE end
				 -- 规模除外，不需要展示小规模的数据,全部规模都小于，则不展示。任一一个大于的，都展示。
					and case when abs(v1.last_month_xse) < v3.last_month_xse and v1.xse < v3.xse
									 and abs(v1.last_month_market_share) < v3.last_month_market_share
									 and abs(v1.market_share) < v3.market_share   then true
							 else false  end
				where
					-- 指标异常阈值判断
						 case when  abs(v1.xse_hb) >= v2.xse_hb and index_type = '销售额' then true
									 when  abs(v1.market_share_hb) >= v2.market_share_hb and index_type = '销售份额'  then true
									 when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_hb)>= v2.dj_hb  and index_type = '单价' then true
									 when  v1.dj>= dj_min and v1.dj< dj_max  and abs(v1.dj_diff) >= v2.dj_diff and index_type = '单价'  then true
							 end
						 -- 如果有 project_id 或者 sort2  ，那么，如果不一致，则非异常
						 and case when length(v2.project_id)>1 and position(v1.project_id::varchar in v2.project_id) = 0 then false
									when length(v2.sort2)>1 and position(v1.sort2 in v2.sort2) = 0 then false
						 else true end
				 and v2.brand_sku = '品牌'
				 and v3.prodcode is null
				 GROUP BY current_period,v1.project_name,v1.project_id,v1.city_co_name,v1.project_zoneclass,v1.brand,v1.prodcode,v1.sort1,v1.sort2
									,v1.top_brand_mark,v1.is_focus_brand,v1.last_month_xse,v1.xse,v1.xse_hb,v1.last_month_market_share
									,v1.market_share,v1.market_share_hb,v1.last_month_dj,v1.dj,v1.dj_hb,v1.dj_diff;

	RETURN  '生成 sop异常数据表 完毕！ ';

END

$BODY$ LANGUAGE plpgsql volatile;




-- 第二批上线


alter table tg_application_info modify column relate_dict tinyint default 1 not null after workflow_id;