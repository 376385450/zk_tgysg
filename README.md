# 易数阁 (资产门户)

[![Reliability Rating](http://192.168.16.151:32880/api/project_badges/measure?project=com.sinohealth.tg-easy-fetch-backend&metric=reliability_rating)](http://192.168.16.151:32880/dashboard?id=com.sinohealth.tg-easy-fetch-backend)

[![Lines of Code](http://192.168.16.151:32880/api/project_badges/measure?project=com.sinohealth.tg-easy-fetch-backend&metric=ncloc)](http://192.168.16.151:32880/dashboard?id=com.sinohealth.tg-easy-fetch-backend)
[![Duplicated Lines (%)](http://192.168.16.151:32880/api/project_badges/measure?project=com.sinohealth.tg-easy-fetch-backend&metric=duplicated_lines_density)](http://192.168.16.151:32880/dashboard?id=com.sinohealth.tg-easy-fetch-backend)

[![Bugs](http://192.168.16.151:32880/api/project_badges/measure?project=com.sinohealth.tg-easy-fetch-backend&metric=bugs)](http://192.168.16.151:32880/dashboard?id=com.sinohealth.tg-easy-fetch-backend)
[![Code Smells](http://192.168.16.151:32880/api/project_badges/measure?project=com.sinohealth.tg-easy-fetch-backend&metric=code_smells)](http://192.168.16.151:32880/dashboard?id=com.sinohealth.tg-easy-fetch-backend)
[![Technical Debt](http://192.168.16.151:32880/api/project_badges/measure?project=com.sinohealth.tg-easy-fetch-backend&metric=sqale_index)](http://192.168.16.151:32880/dashboard?id=com.sinohealth.tg-easy-fetch-backend)


# Profile

- dev 内网易数阁开发环境 **废弃** 
- test 内网易数阁测试环境
  - 使用生产CK，独立MySQL
- gray 内网灰度 与测试同K8S
    - 使用生产CK，独立MySQL
- prd 内网易数阁正式
    - 内网 K8S
- cloud 外网易数阁正式 **废弃**
    - 华为云K8S

# 模块划分

- admin： api层&WS层 Controller
- common： 全局共用部分：枚举，常量，过滤器，异常，工具类
- system： 业务逻辑层 dao service
- asset：资产门户 用户相关接口代理逻辑
- api：资产门户api
- quartz：定时调度


## 关键流程

* 零研方提交申请 com.sinohealth.system.service.IApplicationService#addTemplateApplication
* 依据提交的申请 构造出数SQL： com.sinohealth.system.biz.application.util.WideTableSqlBuilder#fillApplication
* 数据方审核通过后跑SQL 从CK宽表出数成资产结果表：com.sinohealth.system.service.impl.AuditProcessServiceImpl#handleDataAssetsForWide
* 表的集群路由和HDFS备份 com.sinohealth.system.biz.ck.adapter.CKClusterAdapter#createTable

应用内数据同步， 如果是宽表同步是尚书台Datax实现
- com.sinohealth.system.service.SyncHelper#createLocalSnapshotTable
- com.sinohealth.system.service.SyncHelper#pushAssetsTableForBI


- 新增上传和下载接口时，需要去改 data-intelligence-asset-portal 服务
  - FeignController 类对应方法的注解中增加对应的 接口 路径， 否则会出现 上传文件参数丢失，下载的文件二进制流混乱
  - [ ] 优化解决这个问题


> 过滤SQL处理
- SQL转后端筛选树 com.sinohealth.system.util.HistoryApplyUtil#parseSql
    - SQL转前端特定筛选树 com.sinohealth.system.util.HistoryApplyUtil.parseForFront
- 前端筛选树转SQL com.sinohealth.system.biz.application.util.WideTableSqlBuilder#buildWhereSql

注意 后端筛选树 实际上已经在数据结构上可满足使用（自递归），但是前端组件限制，对后端筛选树增加了冗余的一些层级（isFather节点）。

# 依赖组件
## Clickhouse

表名前后缀的不同业务意义

- tg_{xx}： BI使用 xx资产id
    - 此表名和BI的视图一一对应，通过资产id和版本关联回资产快照表（非强关联，所以资产快照表可以更换表名）
- dim_{xx}： 自定义表 xx 用户自定义
- tg_assets_{xx}： 常规模式尚书台同步回来的资产表。 xx 资产id 或 申请id
- tg_assets_wd_{xx}： 宽表模式生成的资产表。 xx 资产id 或 申请id（第一个资产版本）
- 无特定前缀和后缀的表：业务宽表 底表
    - 每次同步都会记录在 tg_sync_info_shard
- {xx}_snap: 非分布式表复制表的 资产结果表，手动管理复制和HDFS备份
- ext_hdfs_{xx}: HDFS外部备份表 专用于资产快照表的备份，生命周期和资产表一致


## MySQL

- 单独开放了 tg_easy_synctask 用户给数据方团队，用于读写定时的配置表，以及查询申请数据
- 开放了全库查询权限 给 自定义标签系统，不知道为什么这么设计，历史遗留


## JDK

支持 JDK8 - JDK17。

JDK11及以上需要加上 `--add-opens java.base/java.lang=ALL-UNNAMED` JVM参数 规避Mybatis的警告

*****

# 问题处理

监控生产 对比处理进度 watch -n 10 'curl -s 192.168.16.214:9033/stats'

## 底表更新 版本更新失败

1. 找到报错原因

- 如果是CK忙或者系统OOM 等无业务逻辑错误可直接重试的问题 就可以直接改数据，重新跑调度任务
  ```sql 
    update tg_assets_wide_upgrade_trigger set state = 'wait' where state = 'failed'
    update tg_assets_upgrade_trigger set expect_time = '2024-06-27 09:47:00', state = 0 where application_id in (4204) ;

  ```
- 如果是业务SQL构造失败，重点在于 WideTableSqlBuilder 逻辑实现

## 底表同步工作流 回调时序问题
- 底表的同步 尚书台会依次调用 /dolphin/rollingTable 和 FlowProcessController.callback
- 从易数阁系统角度来看也必须是严格先后关系，否则数据的绑定关系会乱，前者如果表快照记录数据写入晚于后者回调就会有这个问题
- 但是实际使用中 Datax 工作节点后 还有其他节点的使用，能大大降低这个问题发生概率，测试环境则是加了sleep节点

## 离职员工入职
- 天宫体系帐号调整
- sys_user 表 status字段是1 作废，强行改成0
- 