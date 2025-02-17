> 天宫内执行，查看运行结果和实际的差异


```sql
-- 对比差异
select '天宫缺失', a.*
from dm.sop_sku_abnormal_data_ysg a
         left join dm.sop_sku_abnormal_data b
                   on a.std_id = b.std_id
                       and a.city_co_name = b.city_co_name
                       and a.project_name = b.project_name
where b.std_id is null
  and a.project_name <> '项目QC项目合并'
union all
select '易数阁缺失', b.*
from dm.sop_sku_abnormal_data b
         inner join (SELECT project_name, project_scope, application_id as project_id
                     from dm.qc_apply_config
                     where assets_qc = '是' -- 只有填了需要sop 才需要处理
                       and period_granular = '月度') t
                    on t.project_name = b.project_name
         left join dm.sop_sku_abnormal_data_ysg a
                   on a.std_id = b.std_id
                       and a.city_co_name = b.city_co_name
                       and a.project_name = b.project_name
where a.std_id is null;


-- SKU 差异
select gray.project_name, gray.xx, prod.xx
from (select  project_name, count(*) xx
      from dm.sop_sku_abnormal_data_ysg
      group by project_name) gray
         left join (select project_name, count(*) xx from dm.sop_sku_abnormal_data group by project_name) prod
                   on gray.project_name = prod.project_name
where gray.xx != prod.xx;

-- 品牌差异
select gray.project_name, gray.xx, prod.xx
from (select trim(trailing '_品牌' from project_name) project_name, count(*) xx
      from dm.sop_brand_abnormal_data_ysg
      group by project_name) gray
         left join (select project_name, count(*) xx from dm.sop_brand_abnormal_data group by project_name) prod
                   on gray.project_name = prod.project_name
where gray.xx != prod.xx;
```