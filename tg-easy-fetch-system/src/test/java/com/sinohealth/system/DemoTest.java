package com.sinohealth.system;

import cn.hutool.core.util.ReUtil;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.util.concurrent.RateLimiter;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.framework.config.ContextCopyingTaskDecorator;
import com.sinohealth.system.biz.application.domain.ApplicationTaskConfig;
import com.sinohealth.system.biz.ck.constant.CkClusterType;
import com.sinohealth.system.biz.dataassets.dto.request.AutoFlowBatchPageRequest;
import com.sinohealth.system.biz.dict.dto.BizDataDictValDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.quartz.CronExpression;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-11-01 17:14
 */
@Slf4j
public class DemoTest {

    @Test
    public void testXXX() throws Exception {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get("/home/zk/Documents/data_list_dis.csv"));
        List<String> lines = Files.readAllLines(Paths.get("/home/zk/Documents/data_list.csv"));
        class P {
            String n;
            Integer c;

            public P(String n, Integer c) {
                this.n = n;
                this.c = c;
            }
        }
        List<P> list = lines.stream().map(v -> {
            String[] split = v.split(",");
            return new P(split[0], Integer.parseInt(split[1]));
        }).collect(Collectors.toList());
        Map<Integer, List<P>> map = list.stream().peek(v -> v.c = v.c / 10000).collect(Collectors.groupingBy(v -> v.c));
        for (Map.Entry<Integer, List<P>> e : map.entrySet()) {
            writer.write(String.format("%s,%s\n", e.getKey(), e.getValue().size()));
        }

        writer.close();
    }

    @Test
    public void testTwoYear() throws Exception {
        List<String> y23 = Files.readAllLines(Paths.get("/home/zk/Work/data-intelligence-asset-portal/2023.log"));
        List<String> y24 = Files.readAllLines(Paths.get("/home/zk/Work/data-intelligence-asset-portal/2024.log"));

        class kk {
            String date;
            Integer max;
            Integer total;

            public kk(String date, Integer max, Integer total) {
                this.date = date;
                this.max = max;
                this.total = total;
            }
        }
        class ks {
            int t;
            int max;
            int total;

            @Override
            public String toString() {
                return "ks{" +
                        "t=" + t +
                        ", max=" + max +
                        ", total=" + total + " " + total / t + " " + max / t + '}';
            }
        }
        List<kk> list = y23.stream().map(v -> {
            String[] split = v.split(",");
            return new kk(split[0], Integer.parseInt(split[2]), Integer.parseInt(split[3]));
        }).collect(Collectors.toList());

        ks ks = new ks();
        for (kk k : list) {
            if (k.max < 50 || k.total < 1000) {
                continue;
            }

            ks.t += 1;
            ks.max += k.max;
            ks.total += k.total;
        }

        List<kk> list2 = y24.stream().map(v -> {
            String[] split = v.split(",");
            return new kk(split[0], Integer.parseInt(split[2]), Integer.parseInt(split[3]));
        }).collect(Collectors.toList());

        ks ks2 = new ks();
        for (kk k : list2) {
            if (k.max < 50 || k.total < 1000) {
                continue;
            }

            ks2.t += 1;
            ks2.max += k.max;
            ks2.total += k.total;
        }
        log.info("\n{} \n{}", ks, ks2);
    }


    @Test
    public void testCron() throws Exception {
        Date endDate = AutoFlowBatchPageRequest.TimeType.one_month.getEndDate();

        List<String> list = Arrays.asList("0 0 12 11 2,5,8,11 ?", "0 0 9 10 3,6,9,12 ?",
                "0 0 9 15 * ?", "0 0 9 25 * ?", "0 10 10 11 * ?", "0 10 13 15 2,5,8,11 ?", "0 15 8 8 * ?",
                "0 20 7 11 * ?", "0 40 11 11 * ?", "0 40 16 10 * ?", "0 40 9 11 * ?", "0 5 13 15 * ?",
                "0 5 18 20 2,5,8,11 ?", "0 55 12 11 2,5,8,11 ?", "0 59 10 11 2,5,8,11 ?");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (String cc : list) {
            try {
//                CronExpression cron = new CronExpression("0 15 8 8 * ?");
                CronExpression cron = new CronExpression(cc);
                Date cur = new Date();
                while (true) {
                    Date tmp = cron.getNextValidTimeAfter(cur);
                    if (tmp.after(endDate)) {
                        break;
                    }
                    cur = tmp;
                    log.info("{} {}", df.format(tmp), cc);
                }

            } catch (ParseException e) {
                log.error("", e);
            }
        }
    }

    @Test
    public void testFmtsss() throws Exception {
        String sss = String.format("%s.%s", "测试？|》|\\/?}{^%$%（|、、）", "xlsx");
        System.out.println(sss);
        String fileNameEncode = URLEncoder.encode(sss, "UTF-8").replaceAll("\\+", "%20");
        System.out.println(fileNameEncode);

    }

    @Test
    public void testExtract() throws Exception {
        log.info("start");
        Set<String> tables = new TreeSet<>();
//        byte[] mysqlSQL = Files.readAllBytes(Paths.get("/home/zk/test/kilo/tg_easy_fetch.sql"));
//        String sql = new String(mysqlSQL);
//        log.info("read mysql");

        List<String> lines = Files.readAllLines(Paths.get("/home/zk/Documents/replica-detail.csv"));
        for (String line : lines) {
            String[] cols = line.split(",");
            String name = cols[0];

            boolean tgAssets = name.startsWith("tg_assets_");
            if (tgAssets && name.contains("_2024") && !name.contains("_202409")) {
                tables.add(name);
            }
//            if (tgAssets && name.contains("_2023")) {
//                tables.add(name);
//            }

        }

//        for (String table : tables) {
//            if (sql.contains(table)) {
//                log.warn("IN USE {}", table);
//            }
//        }


        System.out.println();
        System.out.println(tables.size());

        String pair = tables.stream().map(v -> v.replace("_local", "_shard")).map(v -> "'" + v + "'").collect(Collectors.joining(","));
        System.out.println(pair);

        for (String table : tables) {
            System.out.printf("drop table if exists %s on cluster default_cluster;%n", table);
        }
    }


    @Test
    public void testOrderSet() throws Exception {
        TreeSet<Integer> set = new TreeSet<>();
        for (int i = 0; i < 10; i++) {
            set.add(i);
        }

        for (Integer i : set) {
            System.out.println(i);
        }
    }

    @Test
    public void testFmt() throws Exception {
        System.out.println(" (toString(toYear(#)) || " +
                "multiIf(toQuarter(#) = '1', 'Q1'," +
                "toQuarter(#) = '2', 'Q2'," +
                "toQuarter(#) = '3', 'Q3'," +
                "'Q4')" +
                ") period_str, parseDateTimeBestEffort((toString(toYear(#)) || " +
                "multiIf(toQuarter(#) = '1', '01'," +
                "toQuarter(#) = '2', '04'," +
                "toQuarter(#) = '3', '07'," +
                "'10'))) as period_new, '季度' as period_type ");
        System.out.println(String.format("%02d", 12));
    }

    @Test
    public void testForSQL() throws Exception {
        String fmt = "INSERT INTO %s (`period_ytd`,`province`,`city_co_name`,`company_rights`,`放大销售额`,`放大销售量`," +
                "`排名`,`集团权益的份额％`) VALUES(?,?,?,?,?,?,?,?)";

        String sql = String.format(fmt, "AAA");
        System.out.println(sql);

    }

    @Test
    public void testTableConvert() throws Exception {
        String local = "CREATE TABLE qa_tgysg_dev.tg_assets_TAan_554_20230824175011_local\n" +
                "(\n" +
                "    `ptype` String,\n" +
                "    `period` String,\n" +
                "    `dtype` String,\n" +
                "    `diqu` String,\n" +
                "    `obj_type` String,\n" +
                "    `flag_rel` String,\n" +
                "    `ttype` String,\n" +
                "    `product_rel` String,\n" +
                "    `type_rel` String,\n" +
                "    `rn_product_rel` Int64,\n" +
                "    `rn_type` Int64,\n" +
                "    `rel_rate` Decimal(38, 0),\n" +
                "    `orders_tot` Decimal(38, 0),\n" +
                "    `orders_product_rel` Decimal(38, 0),\n" +
                "    `orders_tgt` Decimal(38, 0),\n" +
                "    `rel_support` Decimal(38, 0),\n" +
                "    `rel_confidence` Decimal(38, 0),\n" +
                "    `orders_rel` Decimal(38, 0)\n" +
                ")\n" +
                "ENGINE = ReplicatedMergeTree('/clickhouse/table/{shard}/qa_tgysg_dev/tg_assets_TAan_554_20230824175011_local', '{replica}')\n" +
                "ORDER BY tuple()\n" +
                "SETTINGS index_granularity = 8192";

        String result = local.replaceAll("ReplicatedMergeTree\\(.*\\)", "MergeTree()");
        System.out.println(result);

        System.out.println(local.substring(0, 10));
        System.out.println("sss".substring(0, Math.min(10, "sss".length())));
    }

    @Test
    public void testReplaceI() throws Exception {
        System.out.println("x ON CLUSTER default_cluster x".replaceAll("(?i)ON (?i)cluster " + CkClusterType.DEFAULT, ""));
        System.out.println("x on CLUSTER default_cluster x".replaceAll("(?i)ON (?i)cluster " + CkClusterType.DEFAULT, ""));
        System.out.println("x on cluster default_cluster x".replaceAll("(?i)ON (?i)cluster " + CkClusterType.DEFAULT, ""));
        System.out.println("x on cluster default_cluster x".replaceAll("(?i)ON (?i)cluster " + CkClusterType.DEFAULT, ""));

        System.out.println(("x on cluster " + CkClusterType.BI + " x").replaceAll("(?i)ON (?i)cluster \\w+", ""));


        String x = "jdbc:clickhouse://192.168.56.54:8123/qa_tgysg_dev?socket_timeout=600000";

        URI url = new URI(x.replace("jdbc:", ""));
        System.out.println(url.getHost());
    }


    @Test
    public void testExtrace() throws Exception {
        String x = "     AvgDj: 215.7583(gp) 215.7577(ck) \n      SzPhl: 2.441539(gp) 2.441540(ck) \n        Xse: 829361695.4300(gp) 829363872.0000(ck) \n ";
        String[] parts = x.split("\n");
        for (String part : parts) {
            String[] tmp = part.split(":");
            System.out.println(tmp[0]);
        }
    }

    @Test
    public void testListJsonToCsv() throws Exception {

        byte[] bytes = Files.readAllBytes(Paths.get("/home/zk/metr.json"));
        List<Val> result = JsonUtils.parse(new String(bytes), new TypeReference<List<Val>>() {

        });
        List<String> rs = result.stream().map(Val::toString).collect(Collectors.toList());
        rs.add(0, "\"ID\",\"指标英文名\",\"中文名\",\"指标含义\",\"指标类型\",\"计算公式/逻辑\",\"计算公式/逻辑\",\"字段库 别名\",\"字段库 字段名\",\"字段库 字段id\",\"计算方式\",\"公式\",\"依赖指标\",\"小数精度\",\"除数为0的处理模式\",\"备注\",\"业务线\",\"创建人名称\",\"更新人名称\",\"创建时间\",\"更新时间\"");
        for (Val val : result) {
            System.out.println(val.toString());
        }


        Files.write(Paths.get("me.csv"), rs);
    }


    @Test
    public void testList2JsonToCsv() throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get("/home/zk/field.json"));
        List<FieldVAL> result = JsonUtils.parse(new String(bytes), new TypeReference<List<FieldVAL>>() {
        });
        List<String> rs = result.stream().map(FieldVAL::toString).collect(Collectors.toList());
        rs.add(0, "\"ID\",\"字段英文名\",\"中文名\",\"字段描述\",\"排序\",\"字段数据类型\",\"字段分类 粒度\",\"关联字典id\",\"关联字典\",\"业务线\",\"使用途径\",\"是否启用\",\"创建人名称\",\"更新人名称\",\"创建时间\",\"更新时间\"");


        Files.write(Paths.get("fie.csv"), rs);
    }

    private String replaceHost(String url, String host) {
        return ReUtil.replaceAll(url, "(.*)/\\d+\\.\\d+\\.\\d+\\.\\d+(.*)", "$1/" + host + "$2");
    }


    @Test
    public void testReplace() throws Exception {
        System.out.println(replaceHost("jdbc:clickhouse://192.168.56.50:8123/tgysg?socket_timeout=600000", "192.168.56.56"));
    }

    @Test
    public void testRate() throws Exception {

        RateLimiter rateLimiter = RateLimiter.create(3);

        Stream.of(1, 2, 5, 1, 30, 2).forEach(v -> {
            log.info("start {}", v);
            rateLimiter.acquire(v);
            log.info("end");
        });
    }


    @Test
    public void testReadDump() throws Exception {
        List<String> lines = com.google.common.io.Files.readLines(new File("/home/zk/Desktop/xxxxxxxxx.log"), StandardCharsets.UTF_8);
        long count = lines.stream().distinct().count();
        System.out.println(count);
    }

    @Test
    public void testRandomAlpha() throws Exception {
        long start = System.currentTimeMillis();

        Set<String> s = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
//            TimeUnit.MILLISECONDS.sleep(5);

            String xr = RandomStringUtils.randomAlphabetic(5);
//            String xr = StrUtil.randomAlpha(5);
            boolean add = s.add(xr);
            if (!add) {
                System.out.println("err: " + xr);
            }
        }
        System.out.println(s.size());
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    public void testDis() throws Exception {
        List<BizDataDictValDTO> result = Stream.of("A", "B", "D", "B", "C").distinct().map(BizDataDictValDTO::new).collect(Collectors.toList());
        log.info("result={}", result);
    }

    @Test
    public void test() throws Exception {

        String[] split = "jdbc:postgresql://192.168.16.40:5432/tg_easy_fetch_pg".split("/");
        System.out.println(split);
    }


    @Test
    public void testRunnable() throws Exception {
        ExecutorService as = Executors.newFixedThreadPool(1);

        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        pool.schedule(() -> {
            System.out.println("run");
            throw new RuntimeException("run");
        }, 2, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void testTTL() throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 最大可创建的线程数
        int maxPoolSize = 200;
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(2);
        // 队列最大长度
        int queueCapacity = 100;
        executor.setQueueCapacity(queueCapacity);
        // 线程池维护线程所允许的空闲时间
        int keepAliveSeconds = 30;
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 线程池对拒绝任务(无线程可用)的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setTaskDecorator(new ContextCopyingTaskDecorator());
        executor.initialize();
        Executor pool = TtlExecutors.getTtlExecutor(executor);


        pool.execute(() -> {
            log.info("xx");
            throw new RuntimeException("info");
        });
    }

    @Test
    public void testDay() throws Exception {
        LocalDateTime result = DateUtils.addDaysSkippingWeekends(LocalDateTime.now(), 7);
        System.out.println(result);

    }

    @Test
    public void testTableField() throws Exception {
        this.field(ApplicationTaskConfig.class);
    }

    @Test
    public void testLv() throws Exception {
        String res = new ArrayList<String>().stream().collect(Collectors.joining(","));
        System.out.println(res);
    }

    private void field(Class<?> target) {
        Function<Field, String> sigle = v -> {
            ApiModelProperty annotation = v.getAnnotation(ApiModelProperty.class);

            String comment = Optional.ofNullable(annotation).map(ApiModelProperty::value)
                    .orElse("");
            String t;
            if (v.getType() == Integer.class) {
                t = "int";
            } else if (v.getType() == Long.class) {
                t = "bigint";
            } else if (v.getType() == Boolean.class) {
                t = "int";
            } else if (v.getType() == LocalDateTime.class) {
                t = "datetime";
            } else if (v.getType() == String.class) {
                t = "varchar(255)";
            } else {
                t = "";
            }
            return String.format("%s %s null comment '%s'", humpToLine(v.getName()), t, comment);
        };

        String fields = Arrays.stream(target.getDeclaredFields()).map(sigle)
                .collect(Collectors.joining(",\n"));
        System.out.println(fields);
    }

    private static final Pattern HUMP_PATTERN = Pattern.compile("[A-Z0-9]");

    private String humpToLine(String str) {
        Matcher matcher = HUMP_PATTERN.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Test
    public void testReplaceLast() throws Exception {
        String table = StringUtils.replaceLast("cmh_jcy_cmh_std_sales_phl_shard", "_shard", "_local");
        System.out.println(table);

        table = StringUtils.replaceLast("cmh_jcy_cmh_std_sales_phl_local1", "_local", "_shard");
        System.out.println(table);
    }

    @Test
    public void testNullDecimal() throws Exception {
        String ddl = "CREATE TABLE tgysg.tg_assets_wd_IecN_5270_20240429214737_snap\n" +
                "(\n" +
                "    `period_str` String COMMENT '时间',\n" +
                "    `period_new` DateTime COMMENT '时间筛选项',\n" +
                "    `period_type` String COMMENT '时间颗粒度',\n" +
                "    `平均单价` Nullable(Decimal(38, 11)) COMMENT '平均单价',\n" +
                "    `样本销售额`Decimal(38, 11) COMMENT '样本销售额',\n" +
                "    `放大销售额`Decimal(38, 11) COMMENT '放大销售额',\n" +
                "    `放大销售量` Decimal(38, 11) COMMENT '放大销售量',\n" +
                "    `铺货率` Decimal(76, 38) COMMENT '铺货率',\n" +
                "    `加权铺货率` Decimal(76, 38) COMMENT '加权铺货率',\n" +
                "    `累计可服用天数` Nullable(Decimal(38, 11)) COMMENT '累计可服用天数',\n" +
                "    `装量` Decimal(76, 38) COMMENT '装量',\n" +
                "    `日服用量` Decimal(76, 38) COMMENT '日服用量',\n" +
                "    `t_1_city_co_name` String COMMENT '区域',\n" +
                "    `t_1_zone_name` String COMMENT '区域类型',\n" +
                "    `t_1_period_year` String COMMENT '年份',\n" +
                "    `t_1_province` String COMMENT '上级区域',\n" +
                "    `t_1_std_id` Int32 COMMENT '产品ID',\n" +
                "    `t_1_sort4` String COMMENT '分类四',\n" +
                "    `t_1_spm` String COMMENT '商品名',\n" +
                "    `t_1_pm_all` String COMMENT '品名(含属性)',\n" +
                "    `t_1_otherstag` Int32 COMMENT '长尾标识',\n" +
                "    `t_1_prodcode` String COMMENT '品类',\n" +
                "    `t_1_cj` String COMMENT '厂家',\n" +
                "    `t_1_zx` String COMMENT '中西药属性',\n" +
                "    `t_1_dx` String COMMENT '对象',\n" +
                "    `t_1_jx` String COMMENT '剂型',\n" +
                "    `t_1_tym` String COMMENT '通用名',\n" +
                "    `t_1_pm` String COMMENT '品名',\n" +
                "    `t_1_gg` String COMMENT '规格',\n" +
                "    `t_1_sort1` String COMMENT '分类一',\n" +
                "    `t_1_company_rights` String COMMENT '集团权益',\n" +
                "    `t_1_sort2` String COMMENT '分类二',\n" +
                "    `t_1_short_cj` String COMMENT '简写厂家',\n" +
                "    `t_1_sort3` String COMMENT '分类三',\n" +
                "    `t_1_short_brand` String COMMENT '简写品牌',\n" +
                "    `t_1_brand` String COMMENT '品牌',\n" +
                "    `t_1_sc_old_label` Nullable(String) COMMENT '标签变动(修改前)',\n" +
                "    `t_1_otc_rx` String COMMENT '处方性质',\n" +
                "    `t_1_period` DateTime\n" +
                ")\n" +
                "ENGINE = MergeTree\n" +
                "ORDER BY period_new\n" +
                "SETTINGS index_granularity = 8192";


        System.out.println(replaceForNull(ddl));

    }

    private String replaceForNull(String ddl) {
        ddl = ddl.replace("`Decimal", "` Decimal");
        while (StringUtils.contains(ddl, " Decimal(")) {
            log.info("replace once");
            ddl = ReUtil.replaceAll(ddl, " Decimal\\((\\d+),\\s(\\d+)\\) ", " Nullable(Decimal($1, $2)) ");
        }
        return ddl;
    }

    @Test
    public void testDelTables() throws Exception {
        String th = "tgysg05";
        List<String> actList = Files.readAllLines(Paths.get("/home/zk/Documents/act.csv"));
        Set<String> act = new HashSet<>(actList);
//        select hostname() hostxx, database,name, engine , metadata_modification_time , total_rows
//        from clusterAllReplicas('default_cluster', 'system.tables')
//        where database = 'qa_tgysg_dev' and name like 'tg_assets_%' and name not like 'tg_assets_wd_%'

        List<String> allList = Files.readAllLines(Paths.get("/home/zk/Documents/Result_34.csv"));
        Map<String, Integer> hostMap = new HashMap<>();
        int count = 0;
        for (String s : allList) {
            String[] cols = s.split(",");
            String host = cols[0];
            hostMap.put(host, hostMap.getOrDefault(host, 1) + 1);
            if (!Objects.equals(host, th)) {
                continue;
            }
            String tab = cols[2];
            String type = cols[3];
            boolean replica = type.contains("Replicated") || type.contains("Distributed");
            if (!act.contains(tab)) {
                count++;
                String fmt;
                if (replica) {
                    fmt = "drop table if exists %s on cluster default_cluster;";
                } else {
                    fmt = "drop table if exists %s;";
                }
                System.out.printf((fmt) + "%n", tab);
            }
        }
        System.out.println(count);
        log.info("hostMap={}", hostMap);
    }

    @Test
    public void testDouble() throws Exception {
        String cell = "-1.44379E-16";
        if (ReUtil.isMatch("^[-+]?[0-9]*\\.?[0-9]+$", cell)) {
            System.out.println(Double.parseDouble(cell));
        } else {
            System.out.println("> " + cell);
        }
    }

    @Test
    public void testFindMatch() throws Exception {
        List<String> allList = Files.readAllLines(Paths.get("/home/zk/Work/data-intelligence-asset-portal/all.id.log"));
        List<String> needList = Files.readAllLines(Paths.get("/home/zk/Work/data-intelligence-asset-portal/need.log"));
        List<String> tidList = needList.stream().map(v -> {
            String[] col = v.split(" ");
            return col[2];
        }).collect(Collectors.toList());

        List<String> ids = new ArrayList<>();
        int count = 0;
        for (String row : allList) {
            String tid = row.split(" ")[2];
            if (tidList.contains(tid)) {

                String id = extractId(row);
//                System.out.println(++count + "  " + id);
                ids.add(id);
            }
        }
        System.out.println(String.join(",", ids));

    }

    private String extractId(String param) {
        String id = "";
        try {
            int idx = param.indexOf("\"compareId\":");
            int eIdx = param.indexOf(",\"assetsId\"");
            if (idx != -1 && eIdx != -1) {
                return param.substring(idx + 12, eIdx);
            }
        } catch (Exception ignore) {
        }
        return id;
    }


    /**
     * select prod_code, project_name,data_expire,data_total from tg_user_data_assets
     * where data_expire > now() and copy_from_id is null and project_name not like '%test%' and project_name not like '%测试%';
     */
    @Test
    public void testSem() throws Exception {
        List<String> lines = Files.readAllLines(Paths.get("/home/zk/Documents/tg_easy_fetch_tg_user_data_assets.csv"));

        Map<String, AtomicInteger> res = new HashMap<>();
        Map<String, AtomicInteger> mount = new HashMap<>();
        for (String row : lines) {
            String[] cols = row.split(",");
            String part = cols[0];

            if (StringUtils.isBlank(part)) {
                continue;
            }
            String amount = cols[3];
            for (String ppp : part.split("、")) {
                res.computeIfAbsent(ppp, v -> new AtomicInteger(0)).incrementAndGet();
                mount.computeIfAbsent(ppp, v -> new AtomicInteger(0)).addAndGet(Integer.parseInt(amount));
            }
        }
        log.info("res={}", res);
        log.info("res={}", mount);
        mount.forEach((k, v) -> {
            System.out.println(k + "," + v);
        });

        BufferedWriter writer = Files.newBufferedWriter(Paths.get("/home/zk/Work/springboot-admin/re2.csv"));
        res.forEach((k, v) -> {
            System.out.println(k + "," + v);
            try {
                writer.write(k + "," + v + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        writer.close();

    }


    @Test
    public void testNewPath() throws Exception {
        System.out.println("/ftp/tg-easy-fetch/test/compf/tmp/21b710a2-7659-4ea9-855d-2fdc2150fd69.csv".replace("/compf/tmp/", "/compf/" + 123 + "/"));
    }
}
