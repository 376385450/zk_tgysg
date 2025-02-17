package com.sinohealth.common.utils;

import cn.hutool.core.util.ReUtil;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.util.JdbcConstants;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.io.Files;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.WhiteListUser;
import com.sinohealth.system.dto.ApplicationDataDto;
import com.sinohealth.system.util.ApplicationSqlUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-11-07 19:25
 */
@Slf4j
public class ExceptionUtilTest {

    @Test
    public void testA() throws Exception {
        QueryWrapper<TableFieldInfo> wrapper = Wrappers.<TableFieldInfo>query();
        QueryWrapper<TableFieldInfo> id = wrapper.in("id", "sd,.fdsfs,dfd".split(","));
        System.out.println(id);
    }

    @Test
    public void testCreateTableFields() throws Exception {
        // 自定义指标
        List<String> list = ApplicationSqlUtil.getSortedFieldList("SELECT AVG(t_1.sz_phl) t_1_sz_phl_avg,AVG(t_1.jq_phl) t_1_jq_phl_avg,SUM(t_1.fdxsl) t_1_fdxsl_sum,SUM(t_1.fdxse) t_1_fdxse_sum,SUM(t_1.fdxsl_cw) t_1_fdxsl_cw_sum,SUM(t_1.fdxse_cw) t_1_fdxse_cw_sum,t_1.zone_name t_1_zone_name,t_1.period t_1_period,t_1.province t_1_province,t_1.city_co_name t_1_city_co_name,t_1.prodcode t_1_prodcode,t_1.sort1 t_1_sort1,t_1.brand t_1_brand FROM cmh_brand_shard t_1 WHERE ((template.zone_name = '全国' and template.brand IN ('1号(人绒毛膜促性腺激素(HCG)检测试剂)','0号(复方利血平氨苯蝶啶片)','21多种维生素片(南昌市昌荣生物保健品有限公司)','21金维他(褪黑素维生素B6片)','360°学生专用液体敷料(陕西为一生药业有限公司)','81味艾灸颈椎痛贴(河南省雷神医疗科技有限公司)','81味医用冷敷贴(郑州市中原福力工贸有限公司)','7DAYS(避孕套)','6S益美肤(活性因子生物敷料)'))) GROUP BY t_1.period,t_1.sort1,t_1.brand,t_1.province,t_1.city_co_name,t_1.zone_name,t_1.prodcode");
        System.out.println(list);

        // 时间聚合
        List<String> sortedFieldList = ApplicationSqlUtil.getSortedFieldList("SELECT  (toString(toYear(t_1_period)) || \n" +
                "multiIf(toMonth(t_1_period) <= '6', 'H1',\n" +
                "\t\t'H2')\n" +
                ") period_str, parseDateTimeBestEffort((toString(toYear(t_1_period)) || \n" +
                "multiIf(toMonth(t_1_period) <= '6', '01',\n" +
                "\t\t'07')\n" +
                ")) as period, '半年度' as period_type ,t_1_sz_phl_avg,t_1_fdxse_sum,t_1_zone_name,t_1_period,t_1_province,t_1_city_co_name,t_1_prodcode,t_1_sort1,t_1_brand,平均单价_diy FROM (SELECT sum(t_1.fdxse) / sum(t_1.fdxsl) `平均单价_diy`,AVG(t_1.sz_phl) t_1_sz_phl_avg,SUM(t_1.fdxse) t_1_fdxse_sum,t_1.zone_name t_1_zone_name,t_1.period t_1_period,t_1.province t_1_province,t_1.city_co_name t_1_city_co_name,t_1.prodcode t_1_prodcode,t_1.sort1 t_1_sort1,t_1.brand t_1_brand,平均单价_diy FROM cmh_brand_shard t_1 WHERE (t_1.zone_name = '全国' and t_1.brand IN ('久保芬(布洛芬缓释胶囊)','优舒芬(右布洛芬口服混悬液)','倍得芬(布洛芬软胶囊)','右布洛芬胶囊(苏州第四制药厂有限公司)','司百得(精氨酸布洛芬片)','司百得(精氨酸布洛芬颗粒)','吉浩(布洛芬混悬液)','同泽安(右布洛芬栓)','大亚芬克(布洛芬缓释片)','安瑞克(布洛芬颗粒)','小儿布洛芬栓(山西同达药业有限公司)','小儿布洛芬栓(湖北东信药业有限公司)','小安瑞克(布洛芬颗粒)','小快克(布洛芬颗粒)','布洛芬乳膏(哈药集团生物工程有限公司)','布洛芬凝胶(湖北康正药业有限公司)','布洛芬口服溶液(石药集团欧意药业有限公司)','布洛芬口服溶液(神威药业集团有限公司)','布洛芬混悬液(北京韩美药品有限公司)','布洛芬混悬液(扬州一洋制药有限公司)','布洛芬混悬液(翔宇药业股份有限公司)','布洛芬片(上海华源安徽仁济制药有限公司)','布洛芬片(上海华源安徽锦辉制药有限公司)','布洛芬片(上海寿如松药业泌阳制药有限公司)','布洛芬片(上海金不换兰考制药有限公司)','布洛芬片(临汾宝珠制药有限公司)','布洛芬片(云南康恩贝希陶药业有限公司)','布洛芬片(云南植物药业有限公司)','布洛芬片(云鹏医药集团有限公司)','布洛芬片(修正药业集团股份有限公司)','布洛芬片(修正药业集团长春高新制药有限公司)','布洛芬片(华中药业股份有限公司)','布洛芬片(吉林万通药业有限公司)','布洛芬片(吉林万通药业集团郑州万通复升药业股份有限公司)','布洛芬片(吉林显锋科技制药有限公司)','布洛芬片(吉林省银诺克药业有限公司)','布洛芬片(哈尔滨凯程制药有限公司)','布洛芬片(天方药业有限公司)','布洛芬片(宁夏启元国药有限公司)','布洛芬片(安徽东盛友邦制药有限公司)','布洛芬片(安徽仁和药业有限公司)','布洛芬片(安徽环球药业股份有限公司)','布洛芬片(宜昌人福药业有限责任公司)','布洛芬片(山东新华制药股份有限公司)','布洛芬片(山东方明药业集团股份有限公司)','布洛芬片(山东鲁西药业有限公司)','布洛芬片(山西亨瑞达制药有限公司)','布洛芬片(山西同达药业有限公司)','布洛芬片(山西国润制药有限公司)','布洛芬片(山西太原药业有限公司)','布洛芬片(山西振东安欣生物制药有限公司)','布洛芬片(山西振东泰盛制药有限公司)','布洛芬片(常州制药厂有限公司)','布洛芬片(广东华南药业集团有限公司)','布洛芬片(广东南国药业有限公司)','布洛芬片(江苏平光制药有限责任公司)','布洛芬片(江苏瑞年前进制药有限公司)','布洛芬片(河北东风药业有限公司)','布洛芬片(河南中杰药业有限公司)','布洛芬片(河南全宇制药股份有限公司)','布洛芬片(济南永宁制药股份有限公司)','布洛芬片(海南制药厂有限公司制药一厂)','布洛芬片(湖北亨迪药业股份有限公司)','布洛芬片(濮阳市汇元药业有限公司)','布洛芬片(焦作福瑞堂制药有限公司)','布洛芬片(特一药业集团股份有限公司)','布洛芬片(甘肃兰药药业有限公司)','布洛芬片(百正药业股份有限公司)','布洛芬片(石药集团欧意药业有限公司)','布洛芬片(葵花药业集团(衡水)得菲尔有限公司)','布洛芬片(赤峰蒙欣药业有限公司)','布洛芬片(重庆科瑞制药(集团)有限公司)','布洛芬片(长春新安药业有限公司)','布洛芬片(青岛黄海制药有限责任公司)','布洛芬糖浆(哈尔滨市龙生北药生物工程股份有限公司)','布洛芬缓释混悬液(四川中方制药有限公司)','布洛芬缓释胶囊(上海信谊天平药业有限公司)','布洛芬缓释胶囊(上海爱的发制药有限公司)','布洛芬缓释胶囊(云鹏医药集团有限公司)','布洛芬缓释胶囊(华北制药股份有限公司)','布洛芬缓释胶囊(南京易亨制药有限公司)','布洛芬缓释胶囊(吉林市吴太感康药业有限公司)','布洛芬缓释胶囊(吉林省力盛制药有限公司)','布洛芬缓释胶囊(吉林省百年六福堂药业有限公司)','布洛芬缓释胶囊(吉林道君药业股份有限公司)','布洛芬缓释胶囊(广州柏赛罗药业有限公司)','布洛芬缓释胶囊(海南妙音春制药有限公司)','布洛芬缓释胶囊(珠海润都制药股份有限公司)','布洛芬缓释胶囊(福建太平洋制药有限公司)','布洛芬缓释胶囊(纽哈伯药业有限公司)','布洛芬缓释胶囊(辅仁药业集团有限公司)','布洛芬缓释胶囊(通化茂祥制药有限公司)','布洛芬缓释胶囊(长春海外制药集团有限公司)','布洛芬美林(退热贴)','布洛芬美贝林(退热贴)','布洛芬胶囊(吉林省银河制药有限公司)','布洛芬胶囊(国药集团同济堂(贵州)制药有限公司)','布洛芬胶囊(江西银涛药业有限公司)','布洛芬胶囊(赤峰万泽药业股份有限公司)','布洛芬胶囊(长春迪瑞制药有限公司)')) GROUP BY t_1.period,t_1.sort1,t_1.brand,t_1.province,t_1.city_co_name,t_1.prodcode,t_1.zone_name) template WHERE template.t_1_brand = '芬必得(布洛芬缓释胶囊)'");

        System.out.println(sortedFieldList);
    }

    @Test
    public void testPool() throws Exception {
        int count = 3;
        int total = 10;
        Semaphore semaphore = new Semaphore(count);
        CountDownLatch latch = new CountDownLatch(total);

        ThreadPoolExecutor pool = new ThreadPoolExecutor(count, count,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>());

        for (int i = 0; i < total; i++) {
            semaphore.acquire();
            pool.submit(() -> {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("OK");

                semaphore.release();
                latch.countDown();
            });
        }

        latch.await();
    }


    @Test
    public void testRandom() throws Exception {
        int i = new SecureRandom().nextInt();
        if (i < 0) {
            i *= -1;
        }
        i %= 999999;
        System.out.println(i);
    }


    @Test
    public void testCreateTmpTable() throws Exception {
        String sql = "SELECT        t_2_sz_phl_max,\n" +
                "       t_1_period_count_distinct,\n" +
                "       COUNT(DISTINCT t_1_sort1)  t_1_sort1_count_distinct,\n" +
                "       t_2_sz_phl_max,\n" +
                "       t_1_zone_name,\n" +
                "       t_1_period_granular,\n" +
                "       t_1_brand,\n" +
                "       t_2_jq_phl,\n" +
                "       t_2_fdxsl,\n" +
                "       t_2_fdxse\n" +
                "FROM (SELECT MAX(t_2.sz_phl)            t_2_sz_phl_max,\n" +
                "             COUNT(DISTINCT t_1.period) t_1_period_count_distinct,\n" +
                "             t_1.zone_name              t_1_zone_name,\n" +
                "             t_1.period_granular        t_1_period_granular,\n" +
                "             t_1.sort1                  t_1_sort1,\n" +
                "             t_1.brand                  t_1_brand,\n" +
                "             t_2.jq_phl                 t_2_jq_phl,\n" +
                "             t_2.fdxsl                  t_2_fdxsl,\n" +
                "             t_2.fdxse                  t_2_fdxse\n" +
                "      FROM kcp_cmh_brand_1_shard t_1 GLOBAL\n" +
                "               LEFT OUTER JOIN cmh_brand_2_shard t_2 ON t_1.zone_name = t_2.zone_name\n" +
                "      GROUP BY t_2.fdxse, t_1.sort1, t_1.brand, t_1.period_granular, t_2.fdxsl, t_1.zone_name, t_2.jq_phl\n" +
                "      HAVING MAX(t_2.sz_phl) = '12') template\n" +
                "GROUP BY t_1_zone_name, t_2_sz_phl_max, t_2_fdxse, t_1_brand, t_2_fdxsl, t_2_jq_phl, t_1_period_granular";


        String result = ReUtil.replaceAll(sql, "FROM (\\w+) t_1", "FROM test.$1 t_1");
        result = ReUtil.replaceAll(result, "LEFT OUTER JOIN (\\w+) t_", "LEFT OUTER JOIN test.$1 t_");


        System.out.println(result);

    }

    @Test
    public void testCluster() throws Exception {
        String content = "CREATE TABLE test.tg_1489_20230105111308_local20230107180217_tmp";
        content = content.replace("test.", "");
        System.out.println(ReUtil.replaceAll(content, "CREATE TABLE (\\w+.\\w+)", "CREATE TABLE $1 ON cluster default_cluster"));


        System.out.println(ReUtil.replaceAll("ENGINE = ReplicatedMergeTree('/clickhouse/table/{shard}/test/tg_1453_20230107181420_local', '{replica}')",
                "\\{shard\\}/(\\w+)/", "table/{shard}/test/"));
    }


    @Test
    public void testBuildSelectByCreate() throws Exception {
        String localSql = "CREATE TABLE tgysg.tg_143_20230112204149_shard\n" +
                "(\n" +
                "    `t_1_fdxsl_sum` Decimal(38, 2) COMMENT '销售量求和',\n" +
                "    `t_1_zone_name` String COMMENT '区域类型',\n" +
                "    `t_1_period_granular` String COMMENT '时间粒度',\n" +
                "    `t_1_period` Date COMMENT '时间',\n" +
                "    `t_1_period_year` String COMMENT '时间_年度',\n" +
                "    `t_1_period_semiannual` String COMMENT '时间_半年度',\n" +
                "    `t_1_period_quarter` String COMMENT '时间_季度',\n" +
                "    `t_1_period_month` String COMMENT '时间_月度',\n" +
                "    `t_1_province` String COMMENT '上级区域',\n" +
                "    `t_1_city_co_name` String COMMENT '区域',\n" +
                "    `t_1_prodcode` String COMMENT '品类',\n" +
                "    `t_1_sort1` String COMMENT '分类一',\n" +
                "    `t_1_brand` String COMMENT '品牌',\n" +
                "    `t_1_sz_phl` Decimal(18, 4) COMMENT '铺货率',\n" +
                "    `t_1_jq_phl` Decimal(18, 4) COMMENT '加权铺货率',\n" +
                "    `t_1_avg_dj` Decimal(18, 2) COMMENT '平均单价',\n" +
                "    `t_1_fdxse` Decimal(18, 2) COMMENT '放大销售额',\n" +
                "    `t_1_dj_cw` Decimal(18, 2) COMMENT '平均单价_不含长尾',\n" +
                "    `t_1_fdxsl_cw` Decimal(18, 2) COMMENT '放大销售量_不含长尾',\n" +
                "    `t_1_fdxse_cw` Decimal(18, 2) COMMENT '放大销售额_不含长尾'\n" +
                ")\n" +
                "ENGINE = Distributed('default_cluster', 'tgysg', 'tg_143_20230112204149_local', rand())";

        int engineIdx = localSql.indexOf("ENGINE");
        String table = localSql.substring(0, engineIdx);
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(table, JdbcConstants.CLICKHOUSE);
        if (CollectionUtils.isEmpty(sqlStatements)) {
            throw new RuntimeException("SQL解析错误");
        }
        SQLCreateTableStatement statement = (SQLCreateTableStatement) sqlStatements.get(0);
        List<SQLTableElement> fields = statement.getTableElementList();
        if (Objects.isNull(fields)) {
            throw new RuntimeException("SELECT查询字段为空");
        }
        StringBuilder res = new StringBuilder();
        for (SQLTableElement field : fields) {
            SQLColumnDefinition def = (SQLColumnDefinition) field;
            String columnName = def.getName().getSimpleName();
            res.append(columnName).append(" AS ").append(" '")
                    .append(Optional.ofNullable(def.getComment()).map(v -> ((SQLCharExpr) v).getText()).orElse("")).append("',");
        }
        String field = res.substring(0, res.length() - 1);

        String querySQL = "SELECT " + field + " FROM " + "r";
        log.info("querySQL={}", querySQL);

//            return "SELECT " + field + " FROM " + dataUpdateRecord.getDataTableName();
    }

    @Test
    public void testComment() throws Exception {

        Map<String, ApplicationDataDto.Header> headerMap = new HashMap<>();

        headerMap.put("t_1_id", new ApplicationDataDto.Header(null, "Comment", "注释", "String"));

        String s = "CREATE TABLE tgysg.tg_134_20230112135406_local20230112135406_tmp\n" +
                "(\n" +
                "    `t_1_zone_name` String,\n" +
                "    `t_1_period_granular` String,\n" +
                "    `t_1_period` Date,\n" +
                "    `t_1_period_year` String,\n" +
                "    `t_1_period_semiannual` String,\n" +
                "    `t_1_period_quarter` String,\n" +
                "    `t_1_period_month` String,\n" +
                "    `t_1_province` String,\n" +
                "    `t_1_city_co_name` String,\n" +
                "    `t_1_std_id` Int32,\n" +
                "    `t_1_otc_rx` String,\n" +
                "    `t_1_zx` String,\n" +
                "    `t_1_jx` String,\n" +
                "    `t_1_dx` String,\n" +
                "    `t_1_prodcode` String,\n" +
                "    `t_1_sort1` String,\n" +
                "    `t_1_sort2` String,\n" +
                "    `t_1_sort3` String,\n" +
                "    `t_1_sort4` String,\n" +
                "    `t_1_tym` String,\n" +
                "    `t_1_brand` String,\n" +
                "    `t_1_spm` String,\n" +
                "    `t_1_pm_all` String,\n" +
                "    `t_1_pm` String,\n" +
                "    `t_1_cj` String,\n" +
                "    `t_1_gg` String,\n" +
                "    `t_1_company_rights` String,\n" +
                "    `t_1_short_cj` String,\n" +
                "    `t_1_short_brand` String,\n" +
                "    `t_1_avg_dj` Decimal(18, 2),\n" +
                "    `t_1_sample_xse` Decimal(18, 2),\n" +
                "    `t_1_tz_fdxse` Decimal(18, 2),\n" +
                "    `t_1_fd_xsl` Decimal(18, 2),\n" +
                "    `t_1_sz_phl` Decimal(18, 4),\n" +
                "    `t_1_jq_phl` Decimal(18, 4),\n" +
                "    `t_1_ddu` Nullable(Decimal(18, 2)),\n" +
                "    `t_1_tv` Decimal(18, 2),\n" +
                "    `t_1_vpd` Nullable(Decimal(18, 2)),\n" +
                "    `t_1_otherstag` Int32,\n" +
                "    `t_1_sc_old_label` String,\n" +
                "    `t_1_std_cwid` Int32\n" +
                ")\n" +
                "ENGINE = ReplicatedMergeTree('/clickhouse/table/{shard}/tgysg/tg_134_20230112135406_local20230112135406_tmp', '{replica}')";

        int engineIdx = s.indexOf("ENGINE");
        String table = s.substring(0, engineIdx);
        String suffix = s.substring(engineIdx);

//        SqlNode sqlNode = SqlParser.create(table, SqlParser.config().withLex(Lex.MYSQL)).parseStmt();
//        SqlKind kind = sqlNode.getKind();

        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(table, JdbcConstants.CLICKHOUSE);
        System.out.println(sqlStatements);

        SQLCreateTableStatement statement = (SQLCreateTableStatement) sqlStatements.get(0);
        List<SQLTableElement> fields = statement.getTableElementList();
        for (SQLTableElement field : fields) {
            SQLColumnDefinition def = (SQLColumnDefinition) field;
            String simpleName = def.getName().getSimpleName();
            System.out.println(simpleName);
        }

        ccjParse(headerMap, table, suffix);
    }

    @Test
    public void testSelect() throws Exception {
        String s = "\n" +
                "SELECT t_1_sz_phl_avg,\n" +
                "       t_1_jq_phl_avg,\n" +
                "       t_1_jx_max,\n" +
                "       t_1_ddu_sum,\n" +
                "       t_1_fd_xsl_sum,\n" +
                "       t_1_tz_fdxse_sum,\n" +
                "       t_1_sample_xse_sum,\n" +
                "       t_1_otc_rx_min,\n" +
                "       t_1_zone_name,\n" +
                "       t_1_period,\n" +
                "       t_1_province,\n" +
                "       t_1_city_co_name,\n" +
                "       t_1_std_id,\n" +
                "       t_1_zx,\n" +
                "       t_1_dx,\n" +
                "       t_1_prodcode,\n" +
                "       t_1_sort1,\n" +
                "       t_1_sort2,\n" +
                "       t_1_sort3,\n" +
                "       t_1_sort4,\n" +
                "       t_1_tym,\n" +
                "       t_1_brand,\n" +
                "       t_1_spm,\n" +
                "       t_1_pm_all,\n" +
                "       t_1_pm,\n" +
                "       t_1_cj,\n" +
                "       t_1_gg,\n" +
                "       t_1_company_rights,\n" +
                "       t_1_short_cj,\n" +
                "       t_1_short_brand,\n" +
                "       t_1_tv,\n" +
                "       t_1_vpd,\n" +
                "       t_1_otherstag,\n" +
                "       t_1_sc_old_label,\n" +
                "       t_1_std_cwid,\n" +
                "       `dj_diy`\n" +
                "FROM (SELECT t_1_tz_fdxse_sum / t_1_fd_xsl_sum `dj_diy`,\n" +
                "             SUM(t_1.sample_xse)               t_1_sample_xse_sum,\n" +
                "             SUM(t_1.tz_fdxse)                 t_1_tz_fdxse_sum,\n" +
                "             SUM(t_1.fd_xsl)                   t_1_fd_xsl_sum,\n" +
                "             AVG(t_1.sz_phl)                   t_1_sz_phl_avg,\n" +
                "             AVG(t_1.jq_phl)                   t_1_jq_phl_avg,\n" +
                "             SUM(t_1.ddu)                      t_1_ddu_sum,\n" +
                "             MAX(t_1.jx)                       t_1_jx_max,\n" +
                "             MIN(t_1.otc_rx)                   t_1_otc_rx_min,\n" +
                "             t_1.zone_name                     t_1_zone_name,\n" +
                "             t_1.period                        t_1_period,\n" +
                "             t_1.province                      t_1_province,\n" +
                "             t_1.city_co_name                  t_1_city_co_name,\n" +
                "             t_1.std_id                        t_1_std_id,\n" +
                "             t_1.zx                            t_1_zx,\n" +
                "             t_1.dx                            t_1_dx,\n" +
                "             t_1.prodcode                      t_1_prodcode,\n" +
                "             t_1.sort1                         t_1_sort1,\n" +
                "             t_1.sort2                         t_1_sort2,\n" +
                "             t_1.sort3                         t_1_sort3,\n" +
                "             t_1.sort4                         t_1_sort4,\n" +
                "             t_1.tym                           t_1_tym,\n" +
                "             t_1.brand                         t_1_brand,\n" +
                "             t_1.spm                           t_1_spm,\n" +
                "             t_1.pm_all                        t_1_pm_all,\n" +
                "             t_1.pm                            t_1_pm,\n" +
                "             t_1.cj                            t_1_cj,\n" +
                "             t_1.gg                            t_1_gg,\n" +
                "             t_1.company_rights                t_1_company_rights,\n" +
                "             t_1.short_cj                      t_1_short_cj,\n" +
                "             t_1.short_brand                   t_1_short_brand,\n" +
                "             t_1.tv                            t_1_tv,\n" +
                "             t_1.vpd                           t_1_vpd,\n" +
                "             t_1.otherstag                     t_1_otherstag,\n" +
                "             t_1.sc_old_label                  t_1_sc_old_label,\n" +
                "             t_1.std_cwid                      t_1_std_cwid,\n" +
                "             `dj_diy`\n" +
                "      FROM cmh_sku_shard t_1\n" +
                "      WHERE (t_1.zone_name IN ('全国', '城市', '省份') and (t_1.prodcode IN ('P009')))\n" +
                "      GROUP BY t_1.period, t_1.brand, t_1.zx, t_1.pm, t_1.tv, t_1.std_id, t_1.zone_name, t_1.spm, t_1.prodcode,\n" +
                "               t_1.pm_all, t_1.otherstag, t_1.vpd, t_1.dx, t_1.short_cj, t_1.sort1, t_1.province, t_1.sort4,\n" +
                "               t_1.city_co_name, t_1.sort2, t_1.sort3, t_1.gg, t_1.cj, t_1.sc_old_label, t_1.std_cwid, t_1.tym,\n" +
                "               t_1.short_brand, t_1.company_rights) template\n" +
                "WHERE (template.t_1_prodcode IN ('P009', 'P014', 'P010', 'P015') and\n" +
                "       template.t_1_sort1 IN ('全身用抗感染药物', '皮肤用药', '肝胆疾病用药') )";
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(s, JdbcConstants.CLICKHOUSE);
        System.out.println(sqlStatements);
    }

    private static void ccjParse(Map<String, ApplicationDataDto.Header> headerMap, String table, String suffix) throws JSQLParserException {
        CreateTable parse = (CreateTable) CCJSqlParserUtil.parse(table);
        System.out.println(parse);

        StringBuilder res = new StringBuilder();
        List<ColumnDefinition> columnDefinitions = parse.getColumnDefinitions();
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            ApplicationDataDto.Header alias = headerMap.get(columnDefinition.getColumnName());
            res.append(columnDefinition.getColumnName()).append(columnDefinition.getColDataType())
                    .append(" COMMENT '").append(Optional.ofNullable(alias).map(ApplicationDataDto.Header::getFiledAlias)
                            .orElse("")).append("',");
        }

        String tableName = parse.getTable().getName().replace("test.", "");
        String field = res.substring(0, res.length() - 1);

        System.out.println(" CREATE TABLE " + tableName + " ON cluster default_cluster (" + field + ") " + suffix);
    }

    @Test
    public void testJoin() throws Exception {
        String res = Stream.of("dddd", "tt").filter(StringUtils::isNotBlank).collect(Collectors.joining(","));
        System.out.println(res);
    }

    @Test
    public void testReadCsv() throws Exception {
        List<WhiteListUser> user;
        List<String> lines = Files.readLines(new File("/home/zk/Documents/sqlResult_1.csv (2)"), StandardCharsets.UTF_8);
        for (String line : lines) {
//            System.out.println(line);
            String[] split = line.split("#");
            String id = split[0];
            String json = split[1];

            List<WhiteListUser> users = JsonUtils.parse(json, new TypeReference<List<WhiteListUser>>() {
            });

            for (WhiteListUser whiteListUser : users) {
                List<Integer> authorization = whiteListUser.getAuthorization();
                authorization.replaceAll(v -> {
                    if (v == 1) {
                        return 101;
                    }
                    if (v == 2) {
                        return 102;
                    }
                    if (v == 3) {
                        return 103;
                    }

                    return v;
                });

            }
            System.out.println("update tg_doc_info set whitelist_user_json='" + JsonUtils.format(users) + "' where id=" + id.trim() + ";");
//            System.out.println(users);
        }
    }
}