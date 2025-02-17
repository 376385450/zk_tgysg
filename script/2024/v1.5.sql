-- 客户资产表新增parentCustomerAuthId
alter table `tg_customer_apply_auth` add column `parent_customer_auth_id` bigint null comment '如果是子账号的授权资产，这个字段表示对应的父账号的授权资产id';