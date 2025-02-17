package com.sinohealth.system.util;

import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.fastjson.JSON;
import com.sinohealth.bi.data.Filter;
import com.sinohealth.bi.data.MySql;
import com.sinohealth.bi.data.Table;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.dto.analysis.FilterDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-12-01 17:55
 */
@Slf4j
public class ApplicationSqlUtilTest {


    @Test
    public void testHandleHighPrecision() throws Exception {
        String tableDDL = "CREATE TABLE tgysg.tg_assets_wd_hjcD_7072_20240516171826_snap20240516171827_tmp\n" +
                "(\n" +
                "    `period_str` String,\n" +
                "    `t_1_sc_old_label` Nullable(String),\n" +
                "    `t_1_otc_rx` String,\n" +
                "    `放大销售额` Decimal(17, 5),\n" +
                "    `放大销售量` Decimal(38, 11),\n" +
                "    `铺货率` Decimal(76, 38),\n" +
                "    `加权铺货率` Decimal(76, 38),\n" +
                "    `累计可服用天数` Nullable(Decimal(38, 11)),\n" +
                "    `装量` Decimal(76, 38),\n" +
                "    `日服用量` Decimal(76, 38),\n" +
                "    `样本销售额` Decimal(38, 11),\n" +
                "    `平均单价` Nullable(Decimal(38, 11)),\n" +
                "    `销售片数` Decimal(76, 49)\n" +
                ")\n" +
                "ENGINE = MergeTree\n" +
                "ORDER BY period_new\n" +
                "SETTINGS index_granularity = 8192";

        String result = ApplicationSqlUtil.convertCkSqlByDruid(tableDDL, Collections.emptyMap(), null, "TEST_DB");
        System.out.println(result);
    }

    @Test
    public void testConvertCkSqlByDruid() throws Exception {
        String sql = "CREATE TABLE qa_tgysg_dev.xxxxxx_tmp_local\n" +
                "(\n" +
                "    `period_str` String,\n" +
                "    `period_new` DateTime,\n" +
                "    `period_type` String,\n" +
                "    `t_1_city_co_name` String,\n" +
                "    `t_1_zone_name` String,\n" +
                "    `t_1_period_year` String,\n" +
                "    `t_1_province` String,\n" +
                "    `t_1_std_id` Int32,\n" +
                "    `t_1_sort4` String,\n" +
                "    `t_1_spm` String,\n" +
                "    `t_1_otc_rx` String,\n" +
                "    `t_1_pm_all` String,\n" +
                "    `t_1_otherstag` Int32,\n" +
                "    `t_1_prodcode` String,\n" +
                "    `t_1_cj` String,\n" +
                "    `t_1_zx` String,\n" +
                "    `t_1_dx` String,\n" +
                "    `t_1_jx` String,\n" +
                "    `t_1_tym` String,\n" +
                "    `t_1_pm` String,\n" +
                "    `t_1_gg` String,\n" +
                "    `t_1_sort1` String,\n" +
                "    `t_1_company_rights` String,\n" +
                "    `t_1_sort2` String,\n" +
                "    `t_1_short_cj` String,\n" +
                "    `t_1_sort3` String,\n" +
                "    `t_1_short_brand` String,\n" +
                "    `t_1_brand` String,\n" +
                "    `t_1_sc_old_label` Nullable(String),\n" +
                "    `放大销售额` Decimal(38, 11),\n" +
                "    `放大销售量` Decimal(38, 11),\n" +
                "    `铺货率` Nullable(Float64),\n" +
                "    `加权铺货率` Nullable(Float64),\n" +
                "    `累计可服用天数` Nullable(Decimal(38, 11)),\n" +
                "    `装量` Nullable(Float64),\n" +
                "    `日服用量` Nullable(Float64),\n" +
                "    `样本销售额` Decimal(38, 11),\n" +
                "    `平均单价` Nullable(Decimal(38, 11)),\n" +
                "    `销售片数` Nullable(Float64)\n" +
                ")\n" +
                "ENGINE = ReplicatedMergeTree('/clickhouse/table/{shard}/qa_tgysg_dev/xxxxxx_tmp_local', '{replica}')\n" +
                "ORDER BY tuple()\n" +
                "SETTINGS index_granularity = 8192";
        String result = ApplicationSqlUtil.convertCkSqlByDruid(sql, Collections.emptyMap(), "xx", "TEST_DB");
        System.out.println(result);
    }

    @Test
    public void testNullableDecimal() throws Exception {
        String sql = "CREATE TABLE qa_tgysg_dev.tg_assets_wd_fZtH_1189_20240524105932_snap20240524105933_tmp\n" +
                "(\n" +
                "    `period_str` String,\n" +
                "    `period_new` DateTime,\n" +
                "    `period_type` String,\n" +
                "    `放大销售额` Decimal(38, 2),\n" +
                "    `放大销售量` Decimal(38, 2),\n" +
                "    `铺货率` Nullable(Float64),\n" +
                "    `加权铺货率` Nullable(Float64),\n" +
                "    `自定义指标！！！` Nullable(Decimal(18, 4)),\n" +
                "    `t_1_prodcode` String,\n" +
                "    `t_1_std_id` Int32,\n" +
                "    `t_1_cj` String,\n" +
                "    `t_1_zx` String,\n" +
                "    `t_1_dx` String,\n" +
                "    `t_1_zone_name` String,\n" +
                "    `t_1_jx` String,    `t_1_tym` String,\n" +
                "    `t_1_pm` String,\n" +
                "    `t_1_province` String,\n" +
                "    `t_1_brand` String,\n" +
                "    `t_1_sort4` String,\n" +
                "    `t_1_spm` String,\n" +
                "    `t_1_otc_rx` String,\n" +
                "    `t_1_pm_all` String,    `t_1_otherstag` Int32,\n" +
                "    `t_1_period` Date\n" +
                ")\n" +
                "ENGINE = MergeTree\n" +
                "ORDER BY period_new\n" +
                "SETTINGS index_granularity = 8192";
        String result = ApplicationSqlUtil.convertCkSqlByDruid(sql, Collections.emptyMap(), "xx", "TEST_DB");
        System.out.println(result);
    }

    @Test
    public void testAppendDb() throws Exception {
        String sql = ApplicationSqlUtil.appendDb("select * FROM " + ApplicationSqlUtil.ASSETS_TABLE_PREFIX + "xx", "dd");
        Assert.assertEquals(sql, "select * FROM dd.tg_assets_xx");
    }

    @Test
    public void testSQL() throws Exception {
        String f = "{\"filters\":[{\"filters\":[{\"filterItem\":{\"andOr\":\"and\",\"filters\":[{\"fatherId\":\"BFFX1669885206921\",\"filters\":[{\"filterItem\":{\"andOr\":\"and\",\"filters\":[{\"fatherId\":\"AiBQ1669886109441\",\"filters\":[{\"filterItem\":{\"andOr\":\"and\",\"fieldId\":57589,\"fieldName\":\"t_1_avg_dj\",\"functionalOperator\":\"equalTo\",\"id\":\"RhAz1669886110852\",\"isItself\":2,\"tableAlias\":\"template\",\"tableId\":10016,\"uniqueId\":1,\"value\":\"0\"}},{\"filterItem\":{\"andOr\":\"and\",\"fieldId\":57589,\"fieldName\":\"t_1_avg_dj\",\"functionalOperator\":\"equalTo\",\"id\":\"73mf1669886110852\",\"isItself\":2,\"tableAlias\":\"template\",\"tableId\":10016,\"uniqueId\":1,\"value\":\"0\"}}],\"id\":\"iQ3S1669886110852\",\"logicalOperator\":\"and\"}],\"functionalOperator\":\"equalTo\",\"id\":\"AiBQ1669886109441\",\"isItself\":2,\"tableAlias\":\"template\",\"tableId\":10016,\"uniqueId\":1,\"value\":\"\"},\"isFather\":1},{\"filterItem\":{\"andOr\":\"and\",\"fieldId\":57589,\"fieldName\":\"t_1_avg_dj\",\"functionalOperator\":\"equalTo\",\"id\":\"2j661669886109441\",\"isItself\":2,\"tableAlias\":\"template\",\"tableId\":10016,\"uniqueId\":1,\"value\":\"0\"}}],\"id\":\"QbtB1669886109441\",\"logicalOperator\":\"and\"}],\"functionalOperator\":\"equalTo\",\"id\":\"BFFX1669885206921\",\"isItself\":2,\"tableAlias\":\"template\",\"tableId\":10016,\"uniqueId\":1,\"value\":\"\"},\"isFather\":1},{\"filterItem\":{\"andOr\":\"and\",\"fieldId\":57590,\"fieldName\":\"t_1_fdxsl\",\"functionalOperator\":\"equalTo\",\"id\":\"AhY41669886108020\",\"isItself\":2,\"tableAlias\":\"template\",\"tableId\":10016,\"uniqueId\":1,\"value\":\"0\"}}],\"id\":\"1\",\"logicalOperator\":\"and\"}],\"logicalOperator\":\"\"}";
        FilterDTO filterDTO = JSON.parseObject(f, FilterDTO.class);

        Filter targetFilter = new Filter();
        ApplicationSqlUtil.fillFilterV2(filterDTO, targetFilter);

        Table table = new Table();
        table.setUniqueId(1L);
        table.setFactTable(true);
        final MySql mySql = new MySql(Collections.singletonList(table), targetFilter);
        String whereSql = mySql.getWhereSql();
        System.out.println(whereSql);
    }

    @Test
    public void testFieldType() throws Exception {
        TableFieldInfo info = new TableFieldInfo();
        info.setDataType("Nullable(Decimal(18,2))");
        ApplicationSqlUtil.fillLengthAndType(info);
        log.info("info={}", info.getDataType());

        Assert.assertEquals(ApplicationSqlUtil.trimLengthAndType("Nullable(Decimal(18,2))"), "Decimal");
        Assert.assertEquals(ApplicationSqlUtil.trimLengthAndType("Nullable(Float32)"), "Float32");
        Assert.assertEquals(ApplicationSqlUtil.trimLengthAndType("Float32"), "Float32");
    }

    @Test
    public void testReplaceRelation() throws Exception {
        Optional<String> result = ApplicationSqlUtil.replaceRelationTable("SELECT `t_1_sort1`, `t_1_sort2`, `t_1_sort3`, `period_type`, `period_str`, `t_1_province`, `t_1_city`, `t_1_months`, `销售额`, `单一渠道_销售额_渠道占比`, `销售量`, `品类市场权重`\n" +
                        "FROM ( SELECT * FROM tg_assets_wd_NsfA_577_20230826175059_shard ) tt",
                "tg_assets_wd_NsfA_577_20230826175059_shard",
                "tg_assets_wd_xxxxx_577_20230826175059_shard");

        assertThat(result.isPresent(), equalTo(true));
        assertThat(result.get(), equalTo("SELECT `t_1_sort1`, `t_1_sort2`, `t_1_sort3`, `period_type`, `period_str`, `t_1_province`, `t_1_city`, `t_1_months`, `销售额`, `单一渠道_销售额_渠道占比`, `销售量`, `品类市场权重`\n" +
                "FROM ( SELECT * FROM tg_assets_wd_xxxxx_577_20230826175059_shard ) tt"));


        result = ApplicationSqlUtil.replaceRelationTable("SELECT `t_1_sort1`, `t_1_sort2`, `t_1_sort3`, `period_type`, `period_str`, `t_1_province`, `t_1_city`, `t_1_months`, `销售额`, `单一渠道_销售额_渠道占比`, `销售量`, `品类市场权重`\n" +
                        "FROM ( SELECT * FROM tg_assets_wd_Nsf_577_20230826175059_shard ) tt",
                "tg_assets_wd_NsfA_577_20230826175059_shard",
                "tg_assets_wd_xxxxx_577_20230826175059_shard");
        assertThat(result.isPresent(), equalTo(false));
    }

    @Test
    public void testReplaceAny() throws Exception {
        Optional<String> result = ApplicationSqlUtil.replaceRelationTable("SELECT `obj_type_class`, `ttype`, `obj_type`, `region_type`, `region`, `period_type`, `period_str`, `sex`, `ages`, `freq_qj`, `new_old_type`, `orders`, `xse`, `xsl`, `xsl_p`, `cards`, `cards_rto`, `cards_fg`, `fg_rto`, `dot`, `xse_per`, `pieces_per`, `pieces_order`, `dot_per`, `dot_order`, `kedan_avg`, `kepin_avg`, `pindan_avg`, `orders_per`\n" +
                        "FROM ( SELECT * FROM tg_assets_pjDD_1747_20231212221856_snap ) tt\n" +
                        "WHERE (`period_str` IN ('YTD202309','YTD202209','YTD202109','MAT202109','MAT202209','MAT202309'))",
                "tg_assets_wd_xxxxx_577_20230826175059_shard");
        assertThat(result.isPresent(), equalTo(true));

        String v2 = result.get();

        assertEquals(result.get(), "SELECT `obj_type_class`, `ttype`, `obj_type`, `region_type`, `region`, `period_type`, `period_str`, `sex`, `ages`, `freq_qj`, `new_old_type`, `orders`, `xse`, `xsl`, `xsl_p`, `cards`, `cards_rto`, `cards_fg`, `fg_rto`, `dot`, `xse_per`, `pieces_per`, `pieces_order`, `dot_per`, `dot_order`, `kedan_avg`, `kepin_avg`, `pindan_avg`, `orders_per`\n" +
                "FROM ( SELECT * FROM tg_assets_wd_xxxxx_577_20230826175059_shard ) tt\n" +
                "WHERE (`period_str` IN ('YTD202309','YTD202209','YTD202109','MAT202109','MAT202209','MAT202309'))");
        result = ApplicationSqlUtil.replaceRelationTable(v2, "tg_assets_wd_7832_2023082600000_shard");
        assertThat(result.isPresent(), equalTo(true));
        assertEquals(result.get(), "SELECT `obj_type_class`, `ttype`, `obj_type`, `region_type`, `region`, `period_type`, `period_str`, `sex`, `ages`, `freq_qj`, `new_old_type`, `orders`, `xse`, `xsl`, `xsl_p`, `cards`, `cards_rto`, `cards_fg`, `fg_rto`, `dot`, `xse_per`, `pieces_per`, `pieces_order`, `dot_per`, `dot_order`, `kedan_avg`, `kepin_avg`, `pindan_avg`, `orders_per`\n" +
                "FROM ( SELECT * FROM tg_assets_wd_7832_2023082600000_shard ) tt\n" +
                "WHERE (`period_str` IN ('YTD202309','YTD202209','YTD202109','MAT202109','MAT202209','MAT202309'))");
    }

    @Test
    public void testReplaceAnyFail() throws Exception {
        Optional<String> result = ApplicationSqlUtil.replaceRelationTable("SELECT `obj_type_class`, `ttype`, `obj_type`, `region_type`, `region`, `period_type`, `period_str`, `sex`, `ages`, `freq_qj`, `new_old_type`, `orders`, `xse`, `xsl`, `xsl_p`, `cards`, `cards_rto`, `cards_fg`, `fg_rto`, `dot`, `xse_per`, `pieces_per`, `pieces_order`, `dot_per`, `dot_order`, `kedan_avg`, `kepin_avg`, `pindan_avg`, `orders_per`\n" +
                        "FROM ( SELECT * FROM tg_assets_pjDD_1747_20231212221856_snap )tt\n" +
                        "WHERE (`period_str` IN ('YTD202309','YTD202209','YTD202109','MAT202109','MAT202209','MAT202309'))",
                "tg_assets_wd_xxxxx_577_20230826175059_shard");
        assertThat(result.isPresent(), equalTo(false));
    }

    @Test
    public void testParseColumnSort() throws Exception {

        Map<String, Integer> sortMap = ApplicationSqlUtil.parseCKColumnSort("CREATE TABLE sign_up_guest_meeting_local on cluster default_cluster (\n" +
                " id bigint  COMMENT 'id',\n" +
                "  sign_id bigint  COMMENT '报名信息表id',\n" +
                "  meeting_id bigint  COMMENT '会议id',\n" +
                "  meeting_type smallint  COMMENT '服务助理',\n" +
                "  meeting_session bigint  COMMENT '会议届次',\n" +
                "  ticket_id bigint  COMMENT '门票id',\n" +
                "  created_by bigint  COMMENT '创建人',\n" +
                "  created_at datetime  COMMENT '创建时间',\n" +
                "  updated_by bigint  COMMENT '更新人',\n" +
                "  updated_at datetime  COMMENT '更新时间',\n" +
                "  sign_info_id bigint  COMMENT 'sign_up_info.id',\n" +
                "  report_status smallint  COMMENT '报到状态',\n" +
                "  sign_up_status smallint  COMMENT '报名状态',\n" +
                "  meeting_assistant_id bigint  COMMENT '会议助理',\n" +
                "  service_assistant_ids varchar(1024)  COMMENT '服务助理',\n" +
                "  check_date datetime ,\n" +
                "  channel int  COMMENT '报名渠道类型，1小程序，2管理后台',\n" +
                "  PRIMARY KEY (id)\n" +
                ") engine = ReplicatedMergeTree( '/clickhouse/table/{shard}/test_tgysg/sign_up_guest_meeting_local','{replica}')\n" +
                "ORDER BY ( id, meeting_id) \n" +
                "SETTINGS index_granularity = 8192;");

        System.out.println(sortMap);
        sortMap.forEach((k, v) -> System.out.println(v + " " + k));
    }


    @Test
    public void testReverse() throws Exception {
        String result = ApplicationSqlUtil.reverseSql2("a=0 and b=2 or c=3");
        System.out.println(result);

        String complex = "(\n" +
                "    (toYear(t_1.`period`) * 100 + toMonth(t_1.`period`) >=\n" +
                "        toYear(toDate('2023-05-01')) * 100 + toMonth(toDate('2023-05-01')) and\n" +
                "        toYear(t_1.`period`) * 100 + toMonth(t_1.`period`) <=\n" +
                "        toYear(toDate('2023-09-01')) * 100 + toMonth(toDate('2023-09-01') )\n" +
                "         or\n" +
                "        toYear(t_1.`period`) * 100 + toQuarter(t_1.`period`) <=\n" +
                "        toYear(toDate('2022-04-01')) * 100 + toQuarter(toDate('2022-04-01')))\n" +
                "\n" +
                "           and\n" +
                "       (t_1.`zx` LIKE '%健康食品%' OR t_1.`zx` LIKE '%医疗器械%') and t_1.`province` = '' and\n" +
                "       (t_1.`avg_dj` >= '200' or (t_1.`fd_xsl` < '10000000' or t_1.`tv` IS NULL or t_1.`vpd` IS NOT NULL) or\n" +
                "        (t_1.`short_cj` NOT LIKE '%阿胶%')))";
        ;
        System.out.println(ApplicationSqlUtil.reverseSql2(complex));
    }


    @Test
    public void testParseApply() throws Exception {
        ApplicationSqlUtil.parseApplyInfo(
                "/home/zk/tg_easy_fetch__1714388693569.csv",
                "/home/zk/apply-res4.csv");
    }

    // antlr
    @Test
    public void testWhere() throws Exception {
        MySqlStatementParser parser = new MySqlStatementParser("SELECT *\n" +
                "FROM (SELECT * FROM tg_assets_BJyw_599_20230829153912_shard) t\n" +
                "WHERE ((ptype = 'mat' or ((dtype = '全国' and period = '') and period = '')) or obj_type = '保肝护肝类' or product_rel = '心脑血管疾病用药(不含高血压)--脑血管疾病用药')\n" +
                "LIMIT 50 OFFSET 0");

        SQLStatement sqlStatement = parser.parseSelect();
        System.out.println(sqlStatement);

        MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) ((SQLSelectStatement) sqlStatement).getSelect().getQuery();
        SQLBinaryOpExpr where = (SQLBinaryOpExpr) query.getWhere();
        System.out.println(JsonUtils.format(where));

    }

    @Test
    public void testMockFilter() throws Exception {

    }

    @Test
    public void testCheckQuote() throws Exception {
        Pair<String, String> result = ApplicationSqlUtil.checkSql("select distinct pm||pm1 as aa from cmh_dw_standard_collection_shard  where pm<>''");
        log.info("result={}", result);

        result = ApplicationSqlUtil.checkSql("select distinct concat(pm,pm1) as aa from cmh_dw_standard_collection_shard  where pm<>''");
        log.info("result={}", result);

    }
}
