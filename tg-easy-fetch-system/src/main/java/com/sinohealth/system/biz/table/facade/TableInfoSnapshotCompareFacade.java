package com.sinohealth.system.biz.table.facade;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinohealth.common.annotation.RegisterCronMethod;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.DataSourceType;
import com.sinohealth.common.enums.TableInfoSnapshotCompareDetailCategory;
import com.sinohealth.common.enums.TableInfoSnapshotCompareResultState;
import com.sinohealth.common.enums.TableInfoSnapshotCompareState;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.ck.dto.CkDataSource;
import com.sinohealth.system.biz.common.RedisLock;
import com.sinohealth.system.biz.dataassets.service.AssetsCompareService;
import com.sinohealth.system.biz.process.facade.TgFlowProcessAlertFacade;
import com.sinohealth.system.biz.table.dao.TableInfoSnapshotDAO;
import com.sinohealth.system.biz.table.dao.TgTableInfoSnapshotCompareDAO;
import com.sinohealth.system.biz.table.dao.TgTableInfoSnapshotCompareLimitDAO;
import com.sinohealth.system.biz.table.dao.TgTableInfoSnapshotComparePlanDAO;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import com.sinohealth.system.biz.table.domain.TgTableInfoSnapshotCompare;
import com.sinohealth.system.biz.table.domain.TgTableInfoSnapshotCompareDetail;
import com.sinohealth.system.biz.table.domain.TgTableInfoSnapshotCompareLimit;
import com.sinohealth.system.biz.table.dto.CreateAndWriteCkSkuRequest;
import com.sinohealth.system.biz.table.dto.TableDiffRequest;
import com.sinohealth.system.biz.table.vo.CreateAndCopyResultVO;
import com.sinohealth.system.biz.table.vo.DiffResultVO;
import com.sinohealth.system.biz.table.vo.TableInfoDiffVO;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.TableFieldInfo;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.mapper.TableInfoMapper;
import com.sinohealth.system.mapper.TgCkProviderMapper;
import com.sinohealth.system.mapper.TgCkStreamDao;
import com.sinohealth.system.mapper.TgHiveProviderMapper;
import com.sinohealth.system.service.ITableFieldInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TableInfoSnapshotCompareFacade extends TableInfoSnapshotCompareBaseHandler {

    public static volatile boolean run = false;
    private static final String TAG = "维度";
    private static final String INDEX = "指标";
    private static final String HOSTNAME = "hostname";
    private static final String CK_TABLE_KEY = "ckTable";
    private static final String TEMP_KEY = "temp";
    private final TableInfoMapper tableInfoMapper;
    private final TableInfoSnapshotDAO tableInfoSnapshotDAO;
    private final TgTableInfoSnapshotCompareDAO tgTableInfoSnapshotCompareDAO;
    private final TgTableInfoSnapshotCompareDetailFacade tgTableInfoSnapshotCompareDetailFacade;
    private final ITableFieldInfoService tableFieldInfoService;
    private final TgCkProviderMapper tgCkProviderMapper;
    private final CKClusterAdapter ckClusterAdapter;
    private final TgTableInfoSnapshotCompareLimitDAO tgTableInfoSnapshotCompareLimitDAO;
    private final TgHiveProviderMapper tgHiveProviderMapper;
    private final TgCkStreamDao tgCkStreamDao;
    private final TgTableInfoSnapshotComparePlanDAO tgTableInfoSnapshotComparePlanDAO;
    private final Executor executor;
    @Resource
    @Qualifier(ThreadPoolType.FTP_TASK)
    private ThreadPoolTaskExecutor pool;
    private final AssetsCompareService assetsCompareService;

    private final TgFlowProcessAlertFacade tgFlowProcessAlertFacade;
    private final RedisLock redisLock;
    // 用于create table 表名拼接
    @Value("${spring.profiles.active}")
    private String env;
    @Value("${tg.tableInfo.compare.hdfsPrefix}")
    private String hdfsPath;
    @Value("${tg.tableInfo.compare.batchSize}")
    private Integer compareBatchSize;

    public TableInfoSnapshotCompareFacade(TableInfoMapper tableInfoMapper, TableInfoSnapshotDAO tableInfoSnapshotDAO,
                                          TgTableInfoSnapshotCompareDAO tgTableInfoSnapshotCompareDAO,
                                          TgTableInfoSnapshotCompareDetailFacade tgTableInfoSnapshotCompareDetailFacade,
                                          ITableFieldInfoService tableFieldInfoService, CKClusterAdapter ckClusterAdapter,
                                          TgCkProviderMapper tgCkProviderMapper, TgTableInfoSnapshotCompareLimitDAO tgTableInfoSnapshotCompareLimitDAO,
                                          TgHiveProviderMapper tgHiveProviderMapper, TgCkStreamDao tgCkStreamDao,
                                          TgTableInfoSnapshotComparePlanDAO tgTableInfoSnapshotComparePlanDAO,
                                          @Qualifier(ThreadPoolType.ENHANCED_TTL) @Autowired Executor executor,
                                          @Qualifier(ThreadPoolType.FTP_TASK) @Autowired ThreadPoolTaskExecutor pool,
                                          AssetsCompareService assetsCompareService, RedisLock redisLock,
                                          TgFlowProcessAlertFacade tgFlowProcessAlertFacade) {
        this.tableInfoMapper = tableInfoMapper;
        this.tableInfoSnapshotDAO = tableInfoSnapshotDAO;
        this.tgTableInfoSnapshotCompareDAO = tgTableInfoSnapshotCompareDAO;
        this.tgTableInfoSnapshotCompareDetailFacade = tgTableInfoSnapshotCompareDetailFacade;
        this.tableFieldInfoService = tableFieldInfoService;
        this.tgCkProviderMapper = tgCkProviderMapper;
        this.ckClusterAdapter = ckClusterAdapter;
        this.tgTableInfoSnapshotCompareLimitDAO = tgTableInfoSnapshotCompareLimitDAO;
        this.tgHiveProviderMapper = tgHiveProviderMapper;
        this.tgCkStreamDao = tgCkStreamDao;
        this.tgTableInfoSnapshotComparePlanDAO = tgTableInfoSnapshotComparePlanDAO;
        this.executor = executor;
        this.assetsCompareService = assetsCompareService;
        this.redisLock = redisLock;
        this.tgFlowProcessAlertFacade = tgFlowProcessAlertFacade;
        this.pool = pool;
    }

    private String getCkTableNameSuffix() {
        if (StringUtils.isNotBlank(env)) {
            return "_" + env;
        }
        return env;
    }

    private String getHiveTableNameSuffix() {
        return env;
    }

    private String getCkHdfsPathPrefix() {
        return hdfsPath;
    }

    private String getCkTableNamePrefix() {
        return "hdfs";
    }

    /**
     * 根据关联id获取对应的比对信息
     *
     * @param bizIds 关联id
     * @return 比对信息
     */
    public List<TgTableInfoSnapshotCompare> findByBizIds(List<Long> bizIds) {
        if (!CollectionUtils.isEmpty(bizIds)) {
            LambdaQueryWrapper<TgTableInfoSnapshotCompare> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(TgTableInfoSnapshotCompare::getBizId, bizIds);
            return tgTableInfoSnapshotCompareDAO.list(wrapper);
        }
        return Collections.emptyList();
    }

    @RegisterCronMethod
    @Scheduled(cron = "0 0/10 * * * ?")
    public void scheduled() {
        redisLock.wrapperLock(RedisKeys.DiffCompare.KEY, Duration.ofMinutes(10), () -> {
            TgTableInfoSnapshotCompare one = tgTableInfoSnapshotCompareDAO.getOne(new LambdaQueryWrapper<TgTableInfoSnapshotCompare>().eq(TgTableInfoSnapshotCompare::getState, TableInfoSnapshotCompareState.RUNNING.getType()).last(" limit 1 "));

            if (Objects.nonNull(one)) {
                log.info("有正在执行任务，其余比对任务继续排队");
                return;
            }
            // 有excel任务进行中,直接跳出
            if (pool.getActiveCount() > 0) {
                return;
            }
            // 无正在执行任务,可发起待执行任务
            TgTableInfoSnapshotCompare task = tgTableInfoSnapshotCompareDAO.lambdaQuery()
                    .eq(TgTableInfoSnapshotCompare::getState, TableInfoSnapshotCompareState.WAITING.getType())
                    // 计划执行时间为空 或 计划执行时间比当前时间早
                    .and(queryWrapper -> queryWrapper.isNull(TgTableInfoSnapshotCompare::getPlanExecuteTime).or().lt(TgTableInfoSnapshotCompare::getPlanExecuteTime, new Date()))
                    .orderByAsc(TgTableInfoSnapshotCompare::getCreateTime).last(" limit 1 ").one();
            if (Objects.nonNull(task)) {
                log.info("发起任务：{}", task.getId());
                // 这里加锁，并且查询当前系统变量是否存在
                synchronized (TableInfoSnapshotCompareFacade.class) {
                    TableInfoSnapshotCompareFacade.run = true;
                }
                // 设置为运行状态
                runinng(task);
                executor.execute(() -> execute(task));
            }
        });
    }

    /**
     * 处理逻辑
     *
     * @param request 请求参数
     */
    public void handle(TableDiffRequest request) {
        log.info("开始比对校验，表：{},新版：{},旧版：{}", request.getTableId(), request.getNewVersionId(), request.getOldVersionId());
        Long tableId = request.getTableId();
        TableInfoSnapshot newVersion = getNewVersionSnapshot(request.getNewVersionId(), tableId);
        TableInfoSnapshot oldVersion = getOldVersionSnapshot(request.getOldVersionId(), tableId);
        // 初始化任务信息
        initTask(tableId, newVersion, oldVersion, request);
        // 删除计划
        if (Objects.nonNull(request.getPlanId())) {
            tgTableInfoSnapshotComparePlanDAO.removeById(request.getPlanId());
        }
    }

    /**
     * 处理逻辑
     *
     * @param task 比对任务
     */
    private void execute(TgTableInfoSnapshotCompare task) {
        // 线程池
        ThreadPoolExecutor compareExecutor = null;
        try {
            TableInfo tableInfo = tableInfo(task.getTableId());
            TableInfoSnapshot newVersion = getNewVersionSnapshot(task.getNewVersionId(), task.getTableId());
            TableInfoSnapshot oldVersion = getOldVersionSnapshot(task.getOldVersionId(), task.getTableId());
            // 前置判断
            verifyPrefix(newVersion, oldVersion);
            // 获取表字段
            List<TableFieldInfo> fields = getFields(tableInfo.getId());
            // 动态查询/限制条件
            String conditionSql = buildSelectSql(task.getTableId());

            // 随机选中ck存储了最小的节点
            String hostName = selectCkNode();
            // 唯一键sql
            String uniqueKey = getUniqueKey(fields);
            log.info("唯一键判断：{}", uniqueKey);
            // 校验唯一键是否重复
            verifyUnique(newVersion, oldVersion, uniqueKey, conditionSql);

            compareExecutor = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<>(1), new ThreadPoolExecutor.CallerRunsPolicy());

            // 产品编码
            List<String> prodCodes = distinctProdCode();
            List<Future<CreateAndCopyResultVO>> createAndCopyFatureList = new ArrayList<>(2);
            // 切分对比表【标签与指标】
            createAndCopyFatureList.add(compareExecutor.submit(() -> createAndCopyWriteCkTable(buildCopyReq(newVersion, fields, conditionSql, task.getId(), hostName, uniqueKey, prodCodes))));
            createAndCopyFatureList.add(compareExecutor.submit(() -> createAndCopyWriteCkTable(buildCopyReq(oldVersion, fields, conditionSql, task.getId(), hostName, uniqueKey, prodCodes))));

            // 处理新版信息【复制与切分】
            CreateAndCopyResultVO newInfo = createAndCopyFatureList.get(0).get();
            // 处理旧版信息【复制与切分】
            CreateAndCopyResultVO oldInfo = createAndCopyFatureList.get(1).get();

            log.info("开始生成hive 比对宽表");
            List<Future<String>> hiveDiffList = new ArrayList<>(2);
            // 标签差异表名称
            hiveDiffList.add(compareExecutor.submit(() -> createAndWriteDiffTable(tableInfo, newVersion.getVersion(), oldVersion.getVersion(), newInfo.getTagTableName(), oldInfo.getTagTableName(), fields, hostName, task.getId(), TAG)));
            hiveDiffList.add(compareExecutor.submit(() -> createAndWriteDiffTable(tableInfo, newVersion.getVersion(), oldVersion.getVersion(), newInfo.getIndexTableName(), oldInfo.getIndexTableName(), fields, hostName, task.getId(), INDEX)));
            // 标签差异表
            String tagDiffTableName = hiveDiffList.get(0).get();
            // 指标差异表
            String indexDiffTableName = hiveDiffList.get(1).get();

            // 宽表
            String wideTableShardTempName = tableInfo.getTableNameDistributed() + "_widetable_shard_" + task.getId() + getCkTableNameSuffix();
            TgTableInfoSnapshotCompareDetail wideDetail = createWide(fields, task, hostName, wideTableShardTempName);
            Map<String, Integer> wideFieldSortMap = showTableFieldSort(wideTableShardTempName, hostName);

            // 处理比对数据【差异小表、差异详细表】
            List<Future<DiffResultVO>> diffList = new ArrayList<>(2);
            diffList.add(compareExecutor.submit(() -> diff(fields, task, hostName, tagDiffTableName, wideFieldSortMap, TAG)));
            diffList.add(compareExecutor.submit(() -> diff(fields, task, hostName, indexDiffTableName, wideFieldSortMap, INDEX)));
            // 处理新增与删除数据[前置]
            Future<Boolean> addAndDeleteTask = compareExecutor.submit(() -> handleAddAndDeletedData(fields, task, hostName, tagDiffTableName, indexDiffTableName, wideTableShardTempName));

            // 写入比对信息
            DiffResultVO tag = diffList.get(0).get();
            DiffResultVO index = diffList.get(1).get();

            // 生成detail外部表 通过tag与index的detail表union all 写入
            Future<TgTableInfoSnapshotCompareDetail> detailFuture = compareExecutor.submit(() -> handleDetail(tableInfo, fields, task, hostName, tag, index));

            // 生成extra外部表
            String wideExtraTableName = getCkTableNamePrefix() + "_" + tableInfo.getTableNameDistributed() + "_widetable_extra_info_" + task.getId() + getCkTableNameSuffix();
            handleExtra(task.getId(), hostName, tag, index, wideExtraTableName);

            addAndDeleteTask.get();
            // 处理结果数据
            handleLast(fields, task, tagDiffTableName, indexDiffTableName, wideTableShardTempName, wideDetail, wideExtraTableName);
            log.info("比对完成:{}", task.getId());
            finishTask(task);

            log.info("开始rename:{}", task.getId());
            String wideTableShardName = tableInfo.getTableNameDistributed() + "_widetable_shard" + getCkTableNameSuffix();
            String changeDetailTableShardName = tableInfo.getTableNameDistributed() + "_change_detail_shard" + getCkTableNameSuffix();
            TgTableInfoSnapshotCompareDetail detail = detailFuture.get();
            rename(tableInfo, wideTableShardName, wideTableShardTempName, changeDetailTableShardName, detail.getTableName());

            tgTableInfoSnapshotCompareDetailFacade.updateDetailTableName(wideDetail.getId(), wideTableShardName);
            tgTableInfoSnapshotCompareDetailFacade.updateDetailTableName(detail.getId(), changeDetailTableShardName);

            log.info("开始删除临时表");
            // 删除表
            dropTaskTable(task.getId(), false);
            log.info("任务已完成：{}", task.getId());
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            log.info("对比异常：{}", errorMessage);
            buildFail(task, errorMessage);
            dropTaskTable(task.getId(), true);
        } finally {
            // 这里去除系统变量
            synchronized (TableInfoSnapshotCompareFacade.class) {
                TableInfoSnapshotCompareFacade.run = false;
            }

            if (Objects.nonNull(compareExecutor)) {
                // 释放
                compareExecutor.shutdown();
            }
            // 发送告警
            tgFlowProcessAlertFacade.sendDataCompareAlert(task);
        }
    }

    private void runinng(TgTableInfoSnapshotCompare task) {
        task.setState(TableInfoSnapshotCompareState.RUNNING.getType());
        tgTableInfoSnapshotCompareDAO.updateById(task);
    }

    private void handleLast(List<TableFieldInfo> fields, TgTableInfoSnapshotCompare task, String tagDiffTableName, String indexDiffTableName, String wideTableShardTempName, TgTableInfoSnapshotCompareDetail wideDetail, String wideExtraTableName) {
        StopWatch sw = new StopWatch();
        sw.start();
        // 处理比对数据
        handleCompareData(fields, task, tagDiffTableName, indexDiffTableName, wideTableShardTempName, wideExtraTableName);
        sw.stop();
        tgTableInfoSnapshotCompareDetailFacade.updateCompareDetail(wideDetail, sw.getTotalTimeMillis(), tgHiveProviderMapper.countTable(wideTableShardTempName), null);
    }

    /**
     * 处理比对数据
     *
     * @param fields                 字段信息
     * @param task                   任务
     * @param tagDiffTableName       标签比对表名
     * @param indexDiffTableName     指标比对表名
     * @param wideTableShardTempName 结果宽表临时表名称
     * @param wideExtraTableName     额外信息表名称
     */
    private void handleCompareData(List<TableFieldInfo> fields, TgTableInfoSnapshotCompare task, String tagDiffTableName, String indexDiffTableName, String wideTableShardTempName, String wideExtraTableName) {
        log.info("开始处理比对数据结果：{}", wideTableShardTempName);
        tgHiveProviderMapper.createTableAccordingApplication("set hive.auto.convert.join = false");
        // tgHiveProviderMapper.createTableAccordingApplication("set hive.execution.engine = spark");
        StringBuilder res = new StringBuilder();
        res.append("INSERT INTO ").append(wideTableShardTempName).append(" ( ");
        res.append("new_version ,");
        res.append("old_version ,");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            res.append(field.getFieldName()).append(" ,");
        }
        // 旧记录内容
        for (TableFieldInfo field : fields) {
            res.append("old_").append(field.getFieldName()).append(" ,");
        }
        res.append("change_mark1 ,");
        res.append("change_mark2 ,");
        res.append("change_field ,");
        res.append("new_tag,");
        res.append("old_tag,");
        res.append("create_time ");
        res.append(") ");
        res.append("SELECT ");
        res.append("'").append(task.getNewVersionPeriod()).append("',");
        res.append("'").append(task.getOldVersionPeriod()).append("',");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            // 符合匹配的字段
            boolean match = TAG.equals(field.getDimIndex()) && Objects.nonNull(field.getCompareField()) && field.getCompareField() && !"period".equals(field.getFieldName()) && !"period_year".equals(field.getFieldName());
            if (match) {
                res.append("t4.").append(field.getFieldName()).append(" ,");
            } else {
                res.append("t1.").append(field.getFieldName()).append(" ,");
            }
        }
        // 旧记录内容
        for (TableFieldInfo field : fields) {
            // 符合匹配的字段
            boolean match = TAG.equals(field.getDimIndex()) && Objects.nonNull(field.getCompareField()) && field.getCompareField() && !"period".equals(field.getFieldName()) && !"period_year".equals(field.getFieldName());
            if (match) {
                res.append("t4.").append("old_").append(field.getFieldName()).append(" ,");
            } else {
                res.append("t1.").append("old_").append(field.getFieldName()).append(" ,");
            }
        }
        res.append(" case when t4.change_mark2 is null and t5.change_mark2 is null then '无变化' else '变更' end  ,");
        res.append(" IF(t5.change_mark2 is null,'',t5.change_mark2) || IF(t5.change_mark2 is not null and t4.change_mark2 is not null,'+','') || IF(t4.change_mark2 is null,'',t4.change_mark2)  , ");
        res.append(" IF(t5.change_field is null,'',t5.change_field) || IF(t5.change_field is not null and t4.change_field is not null,'+','') || IF(t4.change_field is null,'',t4.change_field)  , ");
//        res.append(" t4.new_tag, ");
        res.append(" case when t4.change_mark2 is null and t5.change_mark2 is null then '' else t4.new_tag end, ");
//        res.append(" t4.old_tag, ");
        res.append(" case when t4.change_mark2 is null and t5.change_mark2 is null then '' else t4.old_tag end, ");
        res.append(" from_unixtime(unix_timestamp(), 'yyyy-MM-dd HH:mm:ss') as create_time ");
        res.append(" FROM ").append(indexDiffTableName).append(" t1 ");
        res.append(" LEFT JOIN ( SELECT ");
        for (TableFieldInfo field : fields) {
            // 符合匹配的字段
            boolean match = TAG.equals(field.getDimIndex()) && Objects.nonNull(field.getCompareField()) && field.getCompareField() && !"period".equals(field.getFieldName()) && !"period_year".equals(field.getFieldName());
            if (match || "std_id".equals(field.getFieldName())) {
                res.append("t2.").append(field.getFieldName()).append(",");
                res.append("t2.").append("old_").append(field.getFieldName()).append(",");
            }
        }
        res.append(" t3.change_mark1,t3.change_mark2,t3.change_field,t3.new_tag,t3.old_tag ");
        res.append(" FROM ").append(tagDiffTableName).append(" t2 ,").append(wideExtraTableName);
        res.append(" t3 where t2.unique_key = t3.unique_key and t2.state = 'needCompare' and t3.dim_index = 'tag' ) t4 on t1.std_id = t4.std_id ");
        res.append(" LEFT JOIN ").append(wideExtraTableName).append(" t5 on t1.unique_key = t5.unique_key and t5.dim_index = 'index' ");
        res.append(" WHERE  t1.state = 'needCompare' ");
        tgHiveProviderMapper.createTableAccordingApplication(res.toString());
        log.info("比对数据结果处理完成：{}", wideTableShardTempName);
    }

    private void handleExtra(Long taskId, String hostName, DiffResultVO tag, DiffResultVO index, String wideExtraTableName) {
        log.info("生成extra表:{}", wideExtraTableName);
        StopWatch sw = new StopWatch();
        sw.start();
        JSONObject jo = new JSONObject();
        jo.put(HOSTNAME, hostName);
        TgTableInfoSnapshotCompareDetail detail = tgTableInfoSnapshotCompareDetailFacade.initCompareDetail(taskId, TableInfoSnapshotCompareDetailCategory.DIFF_WIDE_EXTRA, DataSourceType.SLAVE, wideExtraTableName, jo.toString());
        createHiveWideExtraTable(wideExtraTableName);
        createCkWideExtraTable(wideExtraTableName, hostName);
        String res = "INSERT INTO " + wideExtraTableName + " SELECT 'tag' as dim_indx, t1.* FROM " + tag.getExtraTableName() + " as t1 " + " UNION ALL " + " SELECT 'index' as dim_indx, t2.* FROM " + index.getExtraTableName() + " as t2 ";
        ckClusterAdapter.executeHost(hostName, res);
        sw.stop();
        log.info("extra表处理完成:{}", wideExtraTableName);
        tgTableInfoSnapshotCompareDetailFacade.updateCompareDetail(detail, sw.getTotalTimeMillis(), ckClusterAdapter.executeCountHost(hostName, "SELECT COUNT(*) FROM " + wideExtraTableName), jo.toString());
    }

    private TgTableInfoSnapshotCompareDetail handleDetail(TableInfo tableInfo, List<TableFieldInfo> fields, TgTableInfoSnapshotCompare task, String hostName, DiffResultVO tag, DiffResultVO index) {
        String changeDetailTableShardCkName = tableInfo.getTableNameDistributed() + "_change_detail_shard_" + task.getId() + getCkTableNameSuffix();
        log.info("生成详细表:{}", changeDetailTableShardCkName);
        StopWatch sw = new StopWatch();
        sw.start();
        JSONObject jo = new JSONObject();
        jo.put(HOSTNAME, hostName);
        jo.put(CK_TABLE_KEY, changeDetailTableShardCkName);
        TgTableInfoSnapshotCompareDetail detail = tgTableInfoSnapshotCompareDetailFacade.initCompareDetail(task.getId(), TableInfoSnapshotCompareDetailCategory.DIFF_DETAIL, DataSourceType.SLAVE, changeDetailTableShardCkName, jo.toString());
        createHiveChangeDetailTable(changeDetailTableShardCkName, fields);
        createCkChangeDetailTable(changeDetailTableShardCkName, fields, hostName);
        String res = "INSERT INTO " + changeDetailTableShardCkName + " SELECT * FROM " + tag.getDetailTableName() + " UNION ALL " + " SELECT * FROM " + index.getDetailTableName();
        ckClusterAdapter.executeHost(hostName, res);
        sw.stop();
        log.info("详细表处理完成:{}", changeDetailTableShardCkName);
        tgTableInfoSnapshotCompareDetailFacade.updateCompareDetail(detail, sw.getTotalTimeMillis(), ckClusterAdapter.executeCountHost(hostName, "SELECT COUNT(*) FROM " + changeDetailTableShardCkName), jo.toString());
        return detail;
    }

    /**
     * 构建复制元数据参数
     *
     * @param version      版本信息
     * @param fields       字段信息
     * @param conditionSql where语句
     * @param taskId       任务信息
     * @param hostName     ck hostName
     * @param uniqueKey    唯一键
     * @return req
     */
    private CreateAndWriteCkSkuRequest buildCopyReq(TableInfoSnapshot version, List<TableFieldInfo> fields, String conditionSql, Long taskId, String hostName, String uniqueKey, List<String> prodCodes) {
        // 准备生成hdfs表.需要判断表是否已经存在了。表不存在就开始解析原表结构，生成ck创表语句，再进行value批量赋值
        String tagTableName = buildHdfsTableName(version, "tag", taskId, getCkTableNameSuffix());
        String indexTableName = buildHdfsTableName(version, "index", taskId, getCkTableNameSuffix());
        log.info("生成复制表名：{},{}", tagTableName, indexTableName);
        CreateAndWriteCkSkuRequest req = new CreateAndWriteCkSkuRequest();
        req.setVersion(version);
        req.setUniqueKey(uniqueKey);
        req.setFields(fields);
        req.setNewTagTableName(tagTableName);
        req.setNewIndexTableName(indexTableName);
        req.setTaskId(taskId);
        req.setHostName(hostName);
        req.setCondition(conditionSql);
        req.setProdCodes(prodCodes);
        return req;
    }

    /**
     * 完成任务
     *
     * @param task 任务信息
     */
    private void finishTask(TgTableInfoSnapshotCompare task) {
        Date date = new Date();
        task.setState(TableInfoSnapshotCompareState.COMPLETED.getType());
        task.setResultState(TableInfoSnapshotCompareResultState.NORMAL.getType());
        task.setFinishTime(date);
        task.setUpdateTime(date);
        tgTableInfoSnapshotCompareDAO.updateById(task);
    }

    /**
     * 写入比对信息
     *
     * @param fields        字段信息
     * @param task          任务
     * @param hostName      ck host
     * @param diffTableName 比对表信息
     */
    private DiffResultVO diff(List<TableFieldInfo> fields, TgTableInfoSnapshotCompare task, String hostName, String diffTableName, Map<String, Integer> wideFieldSortMap, String dimIndex) {
        // 详细表
        String changeDetailTableShardTempName = diffTableName + (TAG.equals(dimIndex) ? "_tag" : "_index") + "_change_detail_temp" + "_" + task.getId() + getCkTableNameSuffix();
        TgTableInfoSnapshotCompareDetail compareDetail = createCompareDetail(task, hostName, changeDetailTableShardTempName, fields);

        // 小表
        String wideExtraTempTableName = diffTableName + (TAG.equals(dimIndex) ? "_tag" : "_index") + "_extra_info_temp" + "_" + task.getId() + getCkTableNameSuffix();
        TgTableInfoSnapshotCompareDetail wideExtraDetail = createWideExtra(task, hostName, wideExtraTempTableName);

        StopWatch sw = new StopWatch();
        sw.start();
        log.info("处理比对数据:{},{}", changeDetailTableShardTempName, wideExtraTempTableName);
        // 处理比对数据
        handleNeedCompareData(fields, task, hostName, diffTableName, changeDetailTableShardTempName, wideExtraTempTableName, dimIndex, wideFieldSortMap);
        sw.stop();
        tgTableInfoSnapshotCompareDetailFacade.updateCompareDetail(wideExtraDetail, sw.getTotalTimeMillis(), ckClusterAdapter.executeCountHost(hostName, "SELECT COUNT(*) FROM " + wideExtraTempTableName), null);
        tgTableInfoSnapshotCompareDetailFacade.updateCompareDetail(compareDetail, sw.getTotalTimeMillis(), ckClusterAdapter.executeCountHost(hostName, "SELECT COUNT(*) FROM " + changeDetailTableShardTempName), null);
        return new DiffResultVO(dimIndex, changeDetailTableShardTempName, wideExtraTempTableName);
    }

    private TgTableInfoSnapshotCompareDetail createWideExtra(TgTableInfoSnapshotCompare task, String hostName, String wideExtraTableName) {
        log.info("生成比对额外信息表：{}", wideExtraTableName);
        createCkWideExtraTempTable(wideExtraTableName, hostName);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(HOSTNAME, hostName);
        return tgTableInfoSnapshotCompareDetailFacade.initCompareDetail(task.getId(), TableInfoSnapshotCompareDetailCategory.DIFF_WIDE_EXTRA_TEMP, DataSourceType.SLAVE, wideExtraTableName, jsonObject.toString());
    }

    private TgTableInfoSnapshotCompareDetail createCompareDetail(TgTableInfoSnapshotCompare task, String hostName, String changeDetailTableShardCkName, List<TableFieldInfo> fields) {
        JSONObject jo = new JSONObject();
        jo.put(HOSTNAME, hostName);
        log.info("生成比对详细表:{}", changeDetailTableShardCkName);
        createCkChangeDetailTempTable(changeDetailTableShardCkName, hostName, fields);
        return tgTableInfoSnapshotCompareDetailFacade.initCompareDetail(task.getId(), TableInfoSnapshotCompareDetailCategory.DIFF_DETAIL_TEMP, DataSourceType.SLAVE, changeDetailTableShardCkName, jo.toString());
    }

    private TgTableInfoSnapshotCompareDetail createWide(List<TableFieldInfo> fields, TgTableInfoSnapshotCompare task, String hostName, String wideTableShardTempName) {
        JSONObject jo = new JSONObject();
        jo.put(HOSTNAME, hostName);
        jo.put(CK_TABLE_KEY, wideTableShardTempName);
        log.info("生成比对临时宽表:{}", wideTableShardTempName);
        createHiveWideTable(fields, wideTableShardTempName);
        createCkWideTable(fields, wideTableShardTempName, hostName);
        return tgTableInfoSnapshotCompareDetailFacade.initCompareDetail(task.getId(), TableInfoSnapshotCompareDetailCategory.DIFF_WIDE, DataSourceType.SLAVE, wideTableShardTempName, jo.toString());
    }

    /**
     * 处理需要比对数据
     *
     * @param fields                         字段信息
     * @param task                           任务信息
     * @param hostName                       ck host
     * @param diffTableName                  差异表名称
     * @param changeDetailTableShardTempName 变更详细表临时名称
     * @param wideExtraTempTableName         宽表额外字段临时表
     */
    private void handleNeedCompareData(List<TableFieldInfo> fields, TgTableInfoSnapshotCompare task, String hostName, String diffTableName, String changeDetailTableShardTempName, String wideExtraTempTableName, String dimIndex, Map<String, Integer> wideFieldSortMap) {
        List<CkDataSource> allHost = ckClusterAdapter.getAllHost();
        int size = allHost.size();
        AtomicInteger num = new AtomicInteger(0);
        ThreadPoolExecutor writeExecutor = new ThreadPoolExecutor(size, size, 0L, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<>(1), r -> new Thread(r, dimIndex + "write_ck_thread|" + num + "|" + allHost.get(num.getAndIncrement() % size).getHostName()), new ThreadPoolExecutor.CallerRunsPolicy());
        try {
            // 宽表字段信息
            String extraSql = buildInsertDiffWideExtraSql(wideExtraTempTableName);
            String detailSql = buildInsertDetailSql(changeDetailTableShardTempName, fields).toString();

            // 顺序key
            List<String> loginKeys = new ArrayList<>();
            for (TableFieldInfo field : fields) {
                if (Objects.nonNull(field.getLogicKey()) && field.getLogicKey()) {
                    loginKeys.add(field.getFieldName());
                }
            }
            // 游标顺序读取处理数据
            final int QUERY_SIZE = compareBatchSize;
            // 只检索需要的字段，且按照顺序排列
            StringBuilder querySql = new StringBuilder();
            querySql.append(" SELECT  ");
            for (TableFieldInfo field : fields) {
                // 非维度，才需要唯一键字段
                boolean flag = !TAG.equals(dimIndex) && Objects.nonNull(field.getLogicKey()) && field.getLogicKey();
                // 符合匹配的字段
                boolean match = dimIndex.equals(field.getDimIndex()) && (Objects.isNull(field.getCompareField()) || field.getCompareField());
                if (flag || match || "std_id".equals(field.getFieldName())) {
                    if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                        continue;
                    }
                    querySql.append(field.getFieldName()).append(",");
                    querySql.append("old_").append(field.getFieldName()).append(",");
                }
            }
            querySql.append(" unique_key ");
            querySql.append(" FROM ").append(diffTableName);
            tgCkStreamDao.fetchCustomBatchHost(hostName, querySql.toString(), " state = 'needCompare' ", QUERY_SIZE, v -> {
                // 异步消费
                return writeExecutor.submit(() -> {
                    List<TableInfoDiffVO> diffs = buildDiff(loginKeys, fields, wideFieldSortMap, dimIndex, v);
                    insertDiffDetail(task, diffs, hostName, detailSql);
                    insertDiffWide(diffs, hostName, extraSql);
                });
            });
        } catch (Exception e) {
            log.info("处理比对数据异常：{}", e.getMessage());
            throw e;
        } finally {
            writeExecutor.shutdown();
        }
    }

    /**
     * 写入对比宽表
     *
     * @param hostName ck hostname
     * @param sql      insert sql
     * @param diffs    比对信息
     */
    private void insertDiffWide(List<TableInfoDiffVO> diffs, String hostName, String sql) {
        if (!CollectionUtils.isEmpty(diffs)) {
            int retryNum = 3;
            while (true) {
                try (Connection connection = ckClusterAdapter.getHost(hostName).getSource().getConnection(); PreparedStatement widePs = connection.prepareStatement(sql)) {
                    widePs.setFetchSize(diffs.size());
                    diffs.forEach(diff -> {
                        try {
                            int i = 0;
                            widePs.setObject(++i, diff.getUniqueKey());
                            widePs.setString(++i, diff.getMark());
                            if (CollectionUtils.isEmpty(diff.getMarkDescription())) {
                                widePs.setString(++i, null);
                            } else {
                                widePs.setString(++i, String.join("+", diff.getMarkDescription()));
                            }
                            List<String> changeFieldCommands = Optional.ofNullable(diff.getChangeDetails()).orElse(new ArrayList<>()).stream().map(TableInfoDiffVO.tableInfoChangeDetail::getChangeFieldCommand).collect(Collectors.toList());
                            if (CollectionUtils.isEmpty(changeFieldCommands)) {
                                widePs.setString(++i, null);
                            } else {
                                widePs.setString(++i, String.join(";", changeFieldCommands));
                            }
                            if (CollectionUtils.isEmpty(diff.getNewTags())) {
                                widePs.setString(++i, null);
                            } else {
                                widePs.setString(++i, String.join(";", diff.getNewTags()));
                            }
                            if (CollectionUtils.isEmpty(diff.getOldTags())) {
                                widePs.setString(++i, null);
                            } else {
                                widePs.setString(++i, String.join(";", diff.getOldTags()));
                            }
                            widePs.addBatch();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    widePs.executeBatch();// 提交批处理
                    return;
                } catch (SQLException e) {
                    if (--retryNum < 0) {
                        log.info("重试3次异常");
                        throw new RuntimeException(Thread.currentThread().getName() + e.getMessage());
                    }
                    log.info("写入小表失败，重试次数剩余{},当前线程名称：{}", retryNum, Thread.currentThread().getName());
                    ThreadUtil.sleep(2000);
                }
            }
        }
    }

    /**
     * 写入详细信息
     *
     * @param task  任务信息
     * @param diffs 差异信息
     */
    private void insertDiffDetail(TgTableInfoSnapshotCompare task, List<TableInfoDiffVO> diffs, String hostName, String sql) {
        boolean anyMatch = diffs.stream().anyMatch(i -> !CollectionUtils.isEmpty(i.getChangeDetails()));
        if (anyMatch) {
            int retryNum = 3;
            while (true) {
                try (Connection detailConnection = ckClusterAdapter.getHost(hostName).getConn().getDataSource().getConnection(); PreparedStatement detailPs = detailConnection.prepareStatement(sql)) {
                    long size = diffs.stream().filter(i -> !CollectionUtils.isEmpty(i.getChangeDetails())).count();
                    detailPs.setFetchSize((int) size);
                    for (TableInfoDiffVO diff : diffs) {
                        boolean hasChange = !CollectionUtils.isEmpty(diff.getChangeDetails());
                        boolean fieldEmpty = CollectionUtils.isEmpty(diff.getFieldValue());
                        if (hasChange && fieldEmpty) {
                            log.warn("invalid data: {}", diff);
                        }
                        if (hasChange && !fieldEmpty) {
                            for (TableInfoDiffVO.tableInfoChangeDetail detail : diff.getChangeDetails()) {
                                int i = 0;
                                detailPs.setString(++i, task.getNewVersionPeriod());
                                detailPs.setString(++i, task.getOldVersionPeriod());
                                for (Object value : diff.getFieldValue().values()) {
                                    detailPs.setObject(++i, value);
                                }
                                detailPs.setString(++i, detail.getType());
                                detailPs.setString(++i, detail.getChangeFieldCommand());
                                detailPs.setString(++i, detail.getChangeField());
                                detailPs.setInt(++i, detail.getSort());
                                detailPs.setString(++i, detail.getNewValue());
                                detailPs.setString(++i, detail.getOldValue());
                                detailPs.addBatch();
                            }
                            // 释放内存，后续不需要使用这个字段
                            diff.setFieldValue(null);
                        }
                    }
                    detailPs.executeBatch();// 提交批处理
                    return;
                } catch (SQLException e) {
                    if (--retryNum < 0) {
                        log.info("重试3次异常");
                        throw new RuntimeException(Thread.currentThread().getName() + e.getMessage());
                    }
                    log.info("写入详细表失败，重试次数剩余{},当前线程名称：{}", retryNum, Thread.currentThread().getName());
                    ThreadUtil.sleep(2000);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            }
        }
    }

    /**
     * 构建差异信息
     *
     * @param fields           对比字段
     * @param wideFieldSortMap 宽表字段
     * @param v                数据
     * @return 差异信息
     */
    private List<TableInfoDiffVO> buildDiff(List<String> loginKeys, List<TableFieldInfo> fields, Map<String, Integer> wideFieldSortMap, String dimIndex, List<LinkedHashMap<String, Object>> v) {
        List<TableInfoDiffVO> diffs = new ArrayList<>();
        for (LinkedHashMap<String, Object> values : v) {
            TableInfoDiffVO diff = new TableInfoDiffVO();
            diff.setUniqueKey(values.get("unique_key").toString());
            for (TableFieldInfo field : fields) {
                // 非维度，才需要唯一键字段
                boolean flag = !TAG.equals(dimIndex) && Objects.nonNull(field.getLogicKey()) && field.getLogicKey();
                // 额外字段，记录在指标表中
                boolean extraField = !TAG.equals(dimIndex) && (Objects.isNull(field.getCompareField()) || !field.getCompareField());
                // 符合匹配的字段
                boolean match = dimIndex.equals(field.getDimIndex()) && (Objects.isNull(field.getCompareField()) || field.getCompareField());
                if (flag || match || extraField) {
                    if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                        continue;
                    }
                    String columnName = field.getFieldName();
                    String oldColumnName = "old_" + columnName;
                    String comment = field.getComment();
                    String dim = field.getDimIndex();
                    Object newValue = values.get(columnName);
                    Object oldValue = values.get(oldColumnName);
                    if (TAG.equals(dim) && TAG.equals(dimIndex)) {
                        if (Objects.nonNull(newValue) && StringUtils.isNotBlank(newValue.toString())) {
                            diff.addNewTags(comment + "=" + newValue);
                        }
                        if (Objects.nonNull(oldValue) && StringUtils.isNotBlank(oldValue.toString())) {
                            diff.addOldTags(comment + "=" + oldValue);
                        }
                    }

                    if (!Objects.equals(newValue, oldValue)) {
                        // 变更
                        TableInfoDiffVO.tableInfoChangeDetail detail = new TableInfoDiffVO.tableInfoChangeDetail();
                        detail.setType(dim);
                        detail.setChangeFieldCommand(comment);
                        detail.setChangeField(columnName);
                        // 获取表字段顺序
                        detail.setSort(wideFieldSortMap.get(columnName));
                        if (Objects.nonNull(newValue)) {
                            detail.setNewValue(newValue.toString());
                        }
                        if (Objects.nonNull(oldValue)) {
                            detail.setOldValue(oldValue.toString());
                        }
                        diff.addChangeDetails(detail);
                        diff.setMark("变更");
                        if ("otherstag".equalsIgnoreCase(columnName)) {
                            if ("0".equalsIgnoreCase(oldValue.toString()) && "1".equalsIgnoreCase(newValue.toString())) {
                                diff.addMarkDescription("长尾释放");
                            }
                        } else {
                            diff.addMarkDescription(dim.equals(INDEX) ? "指标变更" : "标签变更");
                        }
                    }
                }
            }
            if (!CollectionUtils.isEmpty(diff.getChangeDetails())) {
                LinkedHashMap<String, Object> fieldValue = new LinkedHashMap<>();
                for (String loginKey : loginKeys) {
                    Object o = values.get(loginKey);
                    if (Objects.nonNull(o)) {
                        fieldValue.put(loginKey, o);
                    } else {
                        fieldValue.put(loginKey, '-');
                    }
                }
                diff.setFieldValue(fieldValue);
            }
            diffs.add(diff);
        }
        return diffs;
    }

    /**
     * 处理新增数据
     *
     * @param fields             字段信息
     * @param task               对比任务信息
     * @param hostName           ck host
     * @param tagDiffTableName   tag对比表名
     * @param indexDiffTableName index对比表名
     * @param wideTableName      宽表名称
     */
    private boolean handleAddAndDeletedData(List<TableFieldInfo> fields, TgTableInfoSnapshotCompare task, String hostName, String tagDiffTableName, String indexDiffTableName, String wideTableName) {
        log.info("开始处理新增&删除数据：{}", wideTableName);
        StringBuilder res = new StringBuilder();
        res.append("INSERT INTO ").append(wideTableName).append(" ( ");
        res.append("new_version ,");
        res.append("old_version ,");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            res.append(field.getFieldName()).append(" ,");
        }
        // 旧记录内容
        for (TableFieldInfo field : fields) {
            res.append("old_").append(field.getFieldName()).append(" ,");
        }
        res.append("change_mark1 ,");
        res.append("change_mark2 ,");
        res.append("create_time ");
        res.append(") ");
        res.append("SELECT ");
        res.append("'").append(task.getNewVersionPeriod()).append("',");
        res.append("'").append(task.getOldVersionPeriod()).append("',");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            // 符合匹配的字段
            boolean match = TAG.equals(field.getDimIndex()) && Objects.nonNull(field.getCompareField()) && field.getCompareField() && !"period".equals(field.getFieldName()) && !"period_year".equals(field.getFieldName());
            if (match) {
                res.append("t2.").append(field.getFieldName()).append(" ,");
            } else {
                res.append("t1.").append(field.getFieldName()).append(" ,");
            }
        }
        // 旧记录内容
        for (TableFieldInfo field : fields) {
            if (field.getDataType().contains("String")) {
                res.append(" '' as old_").append(field.getFieldName()).append("_alias , ");
            } else {
                res.append(" null as old_").append(field.getFieldName()).append("_alias , ");
            }
        }
        res.append("'新增' as change_mark1,");
        res.append(" case when t2.state = 'add' then '新增产品' else '上架' end as change_mark2 , ");
        res.append(" formatDateTime(now(), '%Y-%m-%d %H:%M:%S') as create_time ");
        res.append(" FROM ").append(indexDiffTableName).append(" t1 ");
        res.append(" left join  ").append(tagDiffTableName).append(" t2 on t1.std_id = t2.std_id ");
        res.append(" WHERE  t1.state = 'add' ");
        res.append(" UNION ALL ");
        res.append("SELECT ");
        res.append("'").append(task.getNewVersionPeriod()).append("',");
        res.append("'").append(task.getOldVersionPeriod()).append("',");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            if (field.getDataType().contains("String")) {
                res.append(" '' as ").append(field.getFieldName()).append("_alias , ");
            } else {
                res.append(" null as ").append(field.getFieldName()).append("_alias , ");
            }
        }
        // 旧记录内容
        for (TableFieldInfo field : fields) {
            // 符合匹配的字段
            boolean match = TAG.equals(field.getDimIndex()) && Objects.nonNull(field.getCompareField()) && field.getCompareField() && !"period".equals(field.getFieldName()) && !"period_year".equals(field.getFieldName());
            if (match) {
                res.append("t2.").append("old_").append(field.getFieldName()).append(" ,");
            } else {
                res.append("t1.").append("old_").append(field.getFieldName()).append(" ,");
            }
        }
        res.append("'减少' as change_mark1,");
        res.append(" case when t3.sort3 = '回收站' then '被回收站打包' when t3.status = '禁用' then 'ID合并' else '下架' end  as " + "change_mark2, ");
        res.append(" formatDateTime(now(), '%Y-%m-%d %H:%M:%S') as create_time ");
        res.append(" FROM ").append(indexDiffTableName).append(" as t1 ");
        res.append(" LEFT JOIN ").append(tagDiffTableName).append(" as t2 on t1.old_std_id = t2.old_std_id ");
        res.append(" left join cmh_dw_standard_collection_shard t3 on t1.old_std_id = t3.std_id ");
        res.append(" WHERE t1.state = 'delete' ");
        ckClusterAdapter.executeHost(hostName, res.toString());
        log.info("新增&删除数据处理完成：{}", wideTableName);
        return true;
    }

    private void copyCkData(String original, String target, String hostName) {
        ckClusterAdapter.executeHost(hostName, "INSERT INTO " + target + " SELECT * FROM " + original);
    }

    /**
     * 创建hive集群表
     *
     * @param fields             表字段信息
     * @param wideTableShardName 分片宽表名称
     */
    private void createHiveWideTable(List<TableFieldInfo> fields, String wideTableShardName) {
        StringBuilder fieldSql = new StringBuilder();
        fieldSql.append("CREATE TABLE IF NOT EXISTS ");
        fieldSql.append(wideTableShardName);
        fieldSql.append("(new_version      String comment '新版版本号',");
        fieldSql.append("old_version       String comment '旧版版本号',");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            fieldSql.append(field.getFieldName()).append(" ").append(buildHiveDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
        }
        // 旧记录内容
        for (TableFieldInfo field : fields) {
            fieldSql.append("old_").append(field.getFieldName()).append(" ").append(buildHiveDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
        }
        fieldSql.append("change_mark1       String comment '变动标识1',");
        fieldSql.append("change_mark2       String comment '变动标识2',");
        fieldSql.append("change_field       String comment '变动字段',");
        fieldSql.append("new_tag       String comment '新版本标签',");
        fieldSql.append("old_tag       String comment '旧版本标签',");
        fieldSql.append("create_time       String comment '创建时间')");
        fieldSql.append(" STORED AS parquet ");
        // 创建表
        tgHiveProviderMapper.createTableAccordingApplication(fieldSql.toString());
    }

    /**
     * 创建hive集群表
     *
     * @param fields             表字段信息
     * @param wideTableShardName 分片宽表名称
     * @param hostName           ck hostname
     */
    private void createCkWideTable(List<TableFieldInfo> fields, String wideTableShardName, String hostName) {
        StringBuilder fieldSql = new StringBuilder();
        fieldSql.append("CREATE TABLE IF NOT EXISTS ");
        fieldSql.append(wideTableShardName);
        fieldSql.append("(new_version       Nullable(String) comment '新版版本号',");
        fieldSql.append("old_version       Nullable(String) comment '旧版版本号',");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            fieldSql.append(field.getFieldName()).append(" ").append(buildCkDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
        }
        // 旧记录内容
        for (TableFieldInfo field : fields) {
            fieldSql.append("old_").append(field.getFieldName()).append(" ").append(buildCkDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
        }
        fieldSql.append("change_mark1       Nullable(String) comment '变动标识1',");
        fieldSql.append("change_mark2       Nullable(String) comment '变动标识2',");
        fieldSql.append("change_field       Nullable(String) comment '变动字段',");
        fieldSql.append("new_tag       Nullable(String) comment '新版本标签',");
        fieldSql.append("old_tag       Nullable(String) comment '旧版本标签',");
        fieldSql.append("create_time       Nullable(String) comment '创建时间')");
        fieldSql.append(" ENGINE = HDFS('").append(getCkHdfsPathPrefix()).append(wideTableShardName).append("/data.parquet','Parquet')");
        // 创建表
        ckClusterAdapter.executeHost(hostName, fieldSql.toString());
    }

    /**
     * 创建ck额外字段表
     *
     * @param wideTableExtraName 分片宽表额外字段表名称
     * @param hostName           ck host名称
     */
    private void createCkWideExtraTempTable(String wideTableExtraName, String hostName) {
        String fieldSql = "CREATE TABLE IF NOT EXISTS " + wideTableExtraName + " (unique_key       String comment '数据唯一键'," + "change_mark1       Nullable(String) comment '变动标识1'," + "change_mark2       Nullable(String) comment '变动标识2'," + "change_field       Nullable(String) comment '变动字段'," + "new_tag       Nullable(String) comment '新版本标签'," + "old_tag       Nullable(String) comment '旧版本标签')" + "ENGINE = MergeTree() ORDER BY unique_key ";
        // 创建集群表
        ckClusterAdapter.executeHost(hostName, fieldSql);
    }

    /**
     * 创建hive额外字段表
     *
     * @param wideTableExtraName 分片宽表额外字段表名称
     */
    private void createHiveWideExtraTable(String wideTableExtraName) {
        String fieldSql = "CREATE TABLE IF NOT EXISTS " + wideTableExtraName + " (dim_index    String comment '类型',unique_key       String comment '数据唯一键'," + "change_mark1       String comment '变动标识1'," + "change_mark2       String comment '变动标识2'," + "change_field       String comment '变动字段'," + "new_tag       String comment '新版本标签'," + "old_tag       String comment '旧版本标签') " + " STORED AS parquet  ";
        // 创建集群表
        tgHiveProviderMapper.createTableAccordingApplication(fieldSql);
    }

    /**
     * 创建ck额外字段表
     *
     * @param wideTableExtraName 分片宽表额外字段表名称
     * @param hostName           ck host名称
     */
    private void createCkWideExtraTable(String wideTableExtraName, String hostName) {
        String fieldSql = "CREATE TABLE IF NOT EXISTS " + wideTableExtraName + " (dim_index    String comment '类型',unique_key       String comment '数据唯一键'," + "change_mark1       Nullable(String) comment '变动标识1'," + "change_mark2       Nullable(String) comment '变动标识2'," + "change_field       Nullable(String) comment '变动字段'," + "new_tag       Nullable(String) comment '新版本标签'," + "old_tag       Nullable(String) comment '旧版本标签') " + " ENGINE = HDFS('" + getCkHdfsPathPrefix() + wideTableExtraName + "/data.parquet','Parquet')";
        // 创建集群表
        ckClusterAdapter.executeHost(hostName, fieldSql);
    }

    /**
     * 创建hive结果详细表
     *
     * @param changeDetailTableShardName 分片详细表名称
     */
    private void createHiveChangeDetailTable(String changeDetailTableShardName, List<TableFieldInfo> fields) {
        // 创建表
        StringBuilder res = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        res.append(changeDetailTableShardName);
        res.append("(new_version       String comment '新版版本号',");
        res.append("old_version       String comment '旧版版本号',");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            if (Objects.nonNull(field.getLogicKey()) && field.getLogicKey()) {
                res.append(field.getFieldName()).append(" ").append(buildHiveDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
            }
        }
        res.append("change_type       String comment '变动类型',");
        res.append("change_field_chinese_name       String comment '变化列中文名',");
        res.append("change_field       String comment '变化列英文名',");
        res.append("sort_num       String comment '列顺序',");
        res.append("new_value       String comment '变化列_新值',");
        res.append("old_value       String comment '变化列_旧值')");
        res.append(" STORED AS parquet ");
        tgHiveProviderMapper.createTableAccordingApplication(res.toString());
    }

    /**
     * 创建hive结果详细表
     *
     * @param changeDetailTableShardName 分片详细表名称
     * @param hostName                   ck hostname
     */
    private void createCkChangeDetailTable(String changeDetailTableShardName, List<TableFieldInfo> fields, String hostName) {
        // 创建表
        StringBuilder res = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        res.append(changeDetailTableShardName);
        res.append("(new_version       Nullable(String) comment '新版版本号',");
        res.append("old_version       Nullable(String) comment '旧版版本号',");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            if (Objects.nonNull(field.getLogicKey()) && field.getLogicKey()) {
                res.append(field.getFieldName()).append(" ").append(buildCkDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
            }
        }
        res.append("change_type       String comment '变动类型',");
        res.append("change_field_chinese_name       Nullable(String) comment '变化列中文名',");
        res.append("change_field       String comment '变化列英文名',");
        res.append("sort_num       Nullable(String) comment '列顺序',");
        res.append("new_value       Nullable(String) comment '变化列_新值',");
        res.append("old_value       Nullable(String) comment '变化列_旧值')");
        res.append(" ENGINE = HDFS('");
        res.append(getCkHdfsPathPrefix());
        res.append(changeDetailTableShardName);
        res.append("/data.parquet','Parquet')");
        ckClusterAdapter.executeHost(hostName, res.toString());
    }

    /**
     * 创建hive结果详细表
     *
     * @param changeDetailTableShardName 分片详细表名称
     * @param hostName                   ck hostname
     */
    private void createCkChangeDetailTempTable(String changeDetailTableShardName, String hostName, List<TableFieldInfo> fields) {
        StringBuilder res = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        res.append(changeDetailTableShardName);
        res.append("(new_version       Nullable(String) comment '新版版本号',");
        res.append("old_version       Nullable(String) comment '旧版版本号',");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            if (Objects.nonNull(field.getLogicKey()) && field.getLogicKey()) {
                res.append(field.getFieldName()).append(" ").append(buildCkDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
            }
        }
        res.append("change_type       String comment '变动类型',");
        res.append("change_field_chinese_name       Nullable(String) comment '变化列中文名',");
        res.append("change_field       String comment '变化列英文名',");
        res.append("sort_num       Nullable(String) comment '列顺序',");
        res.append("new_value       Nullable(String) comment '变化列_新值',");
        res.append("old_value       Nullable(String) comment '变化列_旧值'");
        res.append(") ENGINE = MergeTree() ORDER BY change_field ");
        // 创建表
        ckClusterAdapter.executeHost(hostName, res.toString());
    }

    /**
     * 写入比对表数据
     *
     * @param fields           字段信息
     * @param newDiffTableName 新版本表名
     * @param oldDiffTableName 旧版本表名
     * @param diffTableName    比对表名
     * @param dimIndex         类型表【tag/index】
     */
    private void writeDiffTable(List<TableFieldInfo> fields, String newDiffTableName, String oldDiffTableName, String diffTableName, String dimIndex) {
        StringBuilder res = new StringBuilder();
        res.append("INSERT INTO ").append(diffTableName).append("(");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            if (checkField(dimIndex, field)) {
                if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                    continue;
                }
                res.append(field.getFieldName()).append(",");
            }
        }
        // 旧记录内容
        for (TableFieldInfo field : fields) {
            if (checkField(dimIndex, field)) {
                if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                    continue;
                }
                res.append("old_").append(field.getFieldName()).append(",");
            }
        }
        res.append("unique_key ,");
        res.append("state ");
        res.append(" ) ");
        res.append("SELECT ");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            if (checkField(dimIndex, field)) {
                if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                    continue;
                }
                res.append("newTable.").append(field.getFieldName()).append(" as ").append(field.getFieldName()).append(",");
            }
        }
        // 旧记录内容
        for (TableFieldInfo field : fields) {
            if (checkField(dimIndex, field)) {
                if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                    continue;
                }
                res.append("oldTable.").append(field.getFieldName()).append(" as ").append("old_").append(field.getFieldName()).append(",");
            }
        }
        res.append(" nvl(newTable.unique_key,oldTable.unique_key) as unique_key , ");
        res.append(" case when newTable.unique_key is null then 'delete' when oldTable.unique_key is null then 'add' else 'needCompare' end as state ");
        res.append("FROM ").append(newDiffTableName).append(" as newTable full join ").append(oldDiffTableName).append(" as oldTable on newTable.unique_key = oldTable.unique_key");
        tgHiveProviderMapper.createTableAccordingApplication(res.toString());
    }

    /**
     * 创建hive差异表
     *
     * @param tableInfo         源表信息
     * @param newVersion        新版信息
     * @param originalTableName 新版本复制表名
     * @param oldVersion        旧版信息
     * @param targetTableName   旧版本复制表名
     * @param fields            源表字段信息
     * @param taskId            比对任务编号
     * @param dimIndex          类型表【tag/index】
     * @return 差异表名称
     */
    private String createAndWriteDiffTable(TableInfo tableInfo, Integer newVersion, Integer oldVersion, String originalTableName, String targetTableName, List<TableFieldInfo> fields, String hostName, Long taskId, String dimIndex) {
        String tagDiffName = buildHiveDiffTableName(tableInfo, taskId, newVersion, oldVersion, TAG.equals(dimIndex) ? "tag" : "index");
        createDiff(originalTableName, targetTableName, fields, hostName, taskId, tagDiffName, dimIndex);
        return tagDiffName;
    }

    private void createDiff(String originalTableName, String targetTableName, List<TableFieldInfo> fields, String hostName, Long taskId, String tagDiffName, String dimIndex) {
        // 写入比对详细信息表
        JSONObject attach = new JSONObject();
        attach.put(HOSTNAME, hostName);
        TgTableInfoSnapshotCompareDetail tagDetail = tgTableInfoSnapshotCompareDetailFacade.initCompareDetail(taskId, TableInfoSnapshotCompareDetailCategory.HIVE_DIFF, DataSourceType.HIVE, tagDiffName, attach.toString());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        createDiffTable(fields, tagDiffName, dimIndex);
        // 创建ck外部表
        createCkDiffTable(fields, hostName, tagDiffName, dimIndex);

        // 写入hive比对表数据
        writeDiffTable(fields, originalTableName, targetTableName, tagDiffName, dimIndex);
        stopWatch.stop();
        tgTableInfoSnapshotCompareDetailFacade.updateCompareDetail(tagDetail, stopWatch.getTotalTimeMillis(), tgHiveProviderMapper.countTable(tagDiffName), attach.toString());
    }

    /**
     * 创建ck外部表【映射宽表】
     *
     * @param fields        字段信息
     * @param hostName      ck hostname
     * @param diffTableName 对比宽表名
     * @param dimIndex      类型表【tag/index】
     */
    private void createCkDiffTable(List<TableFieldInfo> fields, String hostName, String diffTableName, String dimIndex) {
        StringBuilder res = new StringBuilder();
        res.append("CREATE TABLE ").append(diffTableName).append("( ");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            if (checkField(dimIndex, field)) {
                if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                    continue;
                }
                res.append(field.getFieldName()).append(" ").append(buildCkDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
            }
        }
        // 旧记录内容
        for (TableFieldInfo field : fields) {
            if (checkField(dimIndex, field)) {
                if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                    continue;
                }
                res.append("old_").append(field.getFieldName()).append(" ").append(buildCkDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
            }
        }
        res.append("unique_key       String comment '数据唯一键',");
        res.append("state       String comment '状态'");
        res.append(") ENGINE = HDFS('").append(getCkHdfsPathPrefix()).append(diffTableName).append("/*','Parquet')");
        ckClusterAdapter.executeHost(hostName, res.toString());
    }

    /**
     * 创建比对表
     *
     * @param diffHiveTableName 比对表名称
     * @param fields            字段信息
     * @param dimIndex          类型表【tag/index】
     */
    private void createDiffTable(List<TableFieldInfo> fields, String diffHiveTableName, String dimIndex) {
        StringBuilder res = new StringBuilder();
        res.append("CREATE TABLE ").append(diffHiveTableName).append("( ");
        // 新记录内容
        for (TableFieldInfo field : fields) {
            if (checkField(dimIndex, field)) {
                if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                    continue;
                }
                res.append(field.getFieldName()).append(" ").append(buildHiveDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
            }
        }
        // 旧记录内容
        for (TableFieldInfo field : fields) {
            if (checkField(dimIndex, field)) {
                if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                    continue;
                }
                res.append("old_").append(field.getFieldName()).append(" ").append(buildHiveDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
            }
        }
        res.append("unique_key       String comment '数据唯一键',");
        res.append("state       String comment '状态'");
        res.append(") ");
        res.append(" STORED AS parquet ");
        tgHiveProviderMapper.createTableAccordingApplication(res.toString());
    }

    private boolean checkField(String dimIndex, TableFieldInfo field) {
        // 非维度，才需要唯一键字段
        boolean flag = !TAG.equals(dimIndex) && Objects.nonNull(field.getLogicKey()) && field.getLogicKey();
        // 额外字段，记录在指标表中
        boolean extraField = !TAG.equals(dimIndex) && (Objects.isNull(field.getCompareField()) || !field.getCompareField());
        // 符合匹配的字段
        boolean match = dimIndex.equals(field.getDimIndex()) && (Objects.isNull(field.getCompareField()) || field.getCompareField());
        return flag || match || extraField || "std_id".equals(field.getFieldName());
    }

    /**
     * 获取产品编码
     *
     * @return 产品编码
     */
    private List<String> distinctProdCode() {
        AjaxResult<List<String>> prodCodeResult = assetsCompareService.queryAllProdCode();
        if (prodCodeResult.isSuccess()) {
            return prodCodeResult.getData();
        }

        throw new CustomException(prodCodeResult.getMsg());
    }

    /**
     * 创建并复制写入数据 到 新的ck表中
     *
     * @param req 请求信息
     */
    private CreateAndCopyResultVO createAndCopyWriteCkTable(CreateAndWriteCkSkuRequest req) {
        List<String> prodCodes = req.getProdCodes();
        // 1.生成对应标签分割表【ck & hive】、2.写入对应数据
        // 3.生成对应指标分割表【ck & hive】、2.写入对应数据
        log.info("标签分割表：{}", req.getNewTagTableName());
        handleOriginalData(req.getTaskId(), req.getVersion().getTableNameDistributed(), req.getNewTagTableName(), req.getUniqueKey(), req.getCondition(), req.getFields(), TAG, req.getHostName(), prodCodes);
        log.info("指标分割表：{}", req.getNewIndexTableName());
        handleOriginalData(req.getTaskId(), req.getVersion().getTableNameDistributed(), req.getNewIndexTableName(), req.getUniqueKey(), req.getCondition(), req.getFields(), INDEX, req.getHostName(), prodCodes);

        return new CreateAndCopyResultVO(req.getNewTagTableName(), req.getNewIndexTableName());
    }

    /**
     * 处理源数据【切分与复制】
     *
     * @param taskId          任务编号
     * @param sourceTableName 数据源表名
     * @param tableName       表名
     * @param uniqueKey       唯一键
     * @param conditionSql    where语句
     * @param fields          字段信息
     * @param dimIndex        维度/指标
     * @param hostname        ck host信息
     * @param prodCodes       产品编码集合
     */
    private void handleOriginalData(Long taskId, String sourceTableName, String tableName, String uniqueKey, String conditionSql, List<TableFieldInfo> fields, String dimIndex, String hostname, List<String> prodCodes) {
        log.info("开始处理复制表信息:{}", tableName);
        String tempName = tableName + "_temp";
        JSONObject attach = new JSONObject();
        attach.put(HOSTNAME, hostname);
        attach.put(TEMP_KEY, tempName);
        long processTime = 0L;
        TgTableInfoSnapshotCompareDetail taskDetail = tgTableInfoSnapshotCompareDetailFacade.initCompareDetail(taskId, TableInfoSnapshotCompareDetailCategory.HDFS_CK_HIVE, DataSourceType.SLAVE, tableName, attach.toString());
        // 先新建 标签分割表
        if (verifyCkTableNotExist(tableName, hostname)) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 创建hive表 标签分割表
            createHiveSkuTable(tableName, dimIndex, fields);
            // 快照表
            createCkSkuTempTable(tempName, dimIndex, fields, hostname);
            // 创建ck表，限制比对列
            createCkSkuTable(tableName, dimIndex, fields, hostname);
            // 复制表值
            ckTableWrite(sourceTableName, uniqueKey, dimIndex, tempName, conditionSql, prodCodes, fields, hostname);
            // 复制表数据
            copyCkData(tempName, tableName, hostname);
            stopWatch.stop();
            processTime = stopWatch.getTotalTimeMillis();
        }
        tgTableInfoSnapshotCompareDetailFacade.updateCompareDetail(taskDetail, processTime, ckClusterAdapter.executeCountHost(hostname, "SELECT COUNT(*) FROM " + tableName), attach.toString());
        log.info("复制表处理成功:{}", tableName);
    }

    /**
     * 写入数据至 —— 新的ck表
     *
     * @param sourceTableName 源表表名
     * @param uniqueKey       唯一键
     * @param dimIndex        维度/指标
     * @param tableName       新表名
     * @param fields          源表字段
     * @param hostName        ck节点信息
     * @param prodCodes       产品编码集合
     */
    private void ckTableWrite(String sourceTableName, String uniqueKey, String dimIndex, String tableName, String conditionSql, List<String> prodCodes, List<TableFieldInfo> fields, String hostName) {
        prodCodes.forEach(i -> {
            StringBuilder res = new StringBuilder();
            res.append("INSERT INTO ").append(tableName).append("( ");
            for (TableFieldInfo field : fields) {
                // 非维度，才需要唯一键字段
                boolean flag = !TAG.equals(dimIndex) && Objects.nonNull(field.getLogicKey()) && field.getLogicKey();
                // 额外字段，记录在指标表中
                boolean extraField = !TAG.equals(dimIndex) && (Objects.isNull(field.getCompareField()) || !field.getCompareField());
                // 符合匹配的字段
                boolean match = dimIndex.equals(field.getDimIndex()) && (Objects.isNull(field.getCompareField()) || field.getCompareField());
                if (flag || match || extraField || "std_id".equals(field.getFieldName())) {
                    if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                        continue;
                    }
                    res.append(field.getFieldName()).append(",");
                }
            }
            res.append("unique_key) ");
            res.append("SELECT ");
            for (TableFieldInfo field : fields) {
                // 非维度，才需要唯一键字段
                boolean flag = !TAG.equals(dimIndex) && Objects.nonNull(field.getLogicKey()) && field.getLogicKey();
                // 额外字段，记录在指标表中
                boolean extraField = !TAG.equals(dimIndex) && (Objects.isNull(field.getCompareField()) || !field.getCompareField());
                // 符合匹配的字段
                boolean match = dimIndex.equals(field.getDimIndex()) && (Objects.isNull(field.getCompareField()) || field.getCompareField());
                if (flag || match || extraField || "std_id".equals(field.getFieldName())) {
                    if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                        continue;
                    }
                    res.append(field.getFieldName()).append(" as ").append(field.getFieldName()).append(",");
                }
            }
            if (TAG.equals(dimIndex)) {
                res.append(" toString(std_id) ").append(" as unique_key ");

            } else {
                res.append(uniqueKey).append(" as unique_key ");

            }
            res.append("FROM ").append(sourceTableName);
            res.append(conditionSql);
            res.append(StringUtils.isNotBlank(conditionSql) ? " AND " : " WHERE ");
            res.append(" prodcode = '").append(i).append("' ");
            if (TAG.equals(dimIndex)) {
                res.append(" GROUP BY ");
                for (TableFieldInfo field : fields) {
                    if (dimIndex.equals(field.getDimIndex()) && Objects.nonNull(field.getCompareField()) && field.getCompareField() && !"period".equals(field.getFieldName()) && !"period_year".equals(field.getFieldName())) {
                        res.append(field.getFieldName()).append(",");
                    }
                }
                res.append("unique_key ");
            }
            ckClusterAdapter.executeHost(hostName, res.toString());
        });
    }

    /**
     * 创建ck表
     *
     * @param tagTableName 标签分割表名
     * @param dimIndex     维度/指标
     * @param fields       源表字段
     * @param hostName     ck节点
     */
    private void createCkSkuTable(String tagTableName, String dimIndex, List<TableFieldInfo> fields, String hostName) {
        StringBuilder res = new StringBuilder();
        res.append("CREATE TABLE ").append(tagTableName).append("( ");
        for (TableFieldInfo field : fields) {
            if (checkField(dimIndex, field)) {
                if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                    continue;
                }
                String columnName = field.getFieldName();
                res.append(columnName).append(" ").append(buildCkDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
            }
        }
        res.append("unique_key       String comment '数据唯一键'");
        res.append(") ENGINE = HDFS('").append(getCkHdfsPathPrefix()).append(tagTableName).append("/data.parquet','Parquet')");
        ckClusterAdapter.executeHost(hostName, res.toString());
    }

    /**
     * 创建ck表
     *
     * @param tempName 标签分割表名
     * @param dimIndex 维度/指标
     * @param fields   源表字段
     * @param hostName ck节点
     */
    private void createCkSkuTempTable(String tempName, String dimIndex, List<TableFieldInfo> fields, String hostName) {
        StringBuilder res = new StringBuilder();
        res.append("CREATE TABLE ").append(tempName).append("( ");
        for (TableFieldInfo field : fields) {
            if (checkField(dimIndex, field)) {
                if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                    continue;
                }
                String columnName = field.getFieldName();
                res.append(columnName).append(" ").append(buildCkDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
            }
        }
        res.append("unique_key       String comment '数据唯一键'");
        res.append(") ENGINE = MergeTree() ORDER BY unique_key");
        ckClusterAdapter.executeHost(hostName, res.toString());
    }

    /**
     * 创建hive 标签分割表
     *
     * @param tagTableName 标签分割表名称
     * @param dimIndex     维度/指标
     * @param fields       源表字段
     */
    private void createHiveSkuTable(String tagTableName, String dimIndex, List<TableFieldInfo> fields) {
        StringBuilder res = new StringBuilder();
        res.append("CREATE TABLE ").append(tagTableName).append("( ");
        for (TableFieldInfo field : fields) {
            if (checkField(dimIndex, field)) {
                if (TAG.equals(dimIndex) && ("period".equals(field.getFieldName()) || "period_year".equals(field.getFieldName()))) {
                    continue;
                }
                String columnName = field.getFieldName();
                res.append(columnName).append(" ").append(buildHiveDataType(field)).append(" COMMENT ").append(" '").append(Optional.ofNullable(field.getComment()).orElse("")).append("',");
            }
        }
        res.append("unique_key       String comment '数据唯一键'");
        res.append(")  stored as Parquet ");
        tgHiveProviderMapper.createTableAccordingApplication(res.toString());
    }

    /**
     * 校验唯一性
     *
     * @param newVersion   新版本
     * @param oldVersion   旧版本
     * @param uniqueKey    唯一键
     * @param conditionSql 动态查询条件
     */
    private void verifyUnique(TableInfoSnapshot newVersion, TableInfoSnapshot oldVersion, String uniqueKey, String conditionSql) {
        verifyUniqueKeyRepeat(uniqueKey, conditionSql, newVersion);
        verifyUniqueKeyRepeat(uniqueKey, conditionSql, oldVersion);
    }

    /**
     * 前置校验
     *
     * @param newVersion 新版本
     * @param oldVersion 旧版本
     */
    private void verifyPrefix(TableInfoSnapshot newVersion, TableInfoSnapshot oldVersion) {
        // 校验版本
        verifyVersion(newVersion, oldVersion);
        // 校验表是否存在
        verifyCkTableExist(newVersion);
        verifyCkTableExist(oldVersion);
        // 校验比对任务是否已存在
        // existTask(newVersion, oldVersion);
    }

    /**
     * 获取表信息
     *
     * @param tableId 表编号
     * @return 表信息
     */
    private TableInfo tableInfo(Long tableId) {
        TableInfo tableInfo = tableInfoMapper.selectById(tableId);
        if (Objects.isNull(tableInfo)) {
            throw new CustomException("表信息不存在");
        }
        return tableInfo;
    }

    /**
     * 获取旧版本信息
     *
     * @param oldVersionId 旧版本编号
     * @param tableId      表编号
     * @return 旧版本信息
     */
    private TableInfoSnapshot getOldVersionSnapshot(Long oldVersionId, Long tableId) {
        TableInfoSnapshot oldVersion = tableInfoSnapshotDAO.getById(oldVersionId);
        if (Objects.isNull(oldVersion)) {
            throw new CustomException("旧版信息不存在");
        }
        if (!Objects.equals(oldVersion.getTableId(), tableId)) {
            throw new CustomException("非资源表对应版本记录");
        }
        return oldVersion;
    }

    /**
     * 获取新版信息
     *
     * @param newVersionId 新版编号
     * @param tableId      表编号
     * @return 新版信息
     */
    private TableInfoSnapshot getNewVersionSnapshot(Long newVersionId, Long tableId) {
        TableInfoSnapshot newVersion;
        if (Objects.isNull(newVersionId)) {
            newVersion = tableInfoSnapshotDAO.getLatest(tableId);
        } else {
            newVersion = tableInfoSnapshotDAO.getById(newVersionId);
        }
        if (Objects.isNull(newVersion)) {
            throw new CustomException("新版信息不存在");
        }
        if (!Objects.equals(newVersion.getTableId(), tableId)) {
            throw new CustomException("非资源表对应版本记录");
        }
        return newVersion;
    }

    /**
     * 比对版本
     *
     * @param newVersion 新版本
     * @param oldVersion 旧版本
     */
    private void verifyVersion(TableInfoSnapshot newVersion, TableInfoSnapshot oldVersion) {
        if (newVersion.getVersion().compareTo(oldVersion.getVersion()) < 0) {
            throw new CustomException("仅能选择比【新版数据】更小的版本号数据");
        } else if (newVersion.getVersion().compareTo(oldVersion.getVersion()) == 0) {
            throw new CustomException("旧版版本号与新版版本号一致，请重新选择");
        }
    }

    /**
     * 校验对比任务是否已存在
     *
     * @param newVersion 新版本号
     * @param oldVersion 旧版本号
     */
    private void existTask(TableInfoSnapshot newVersion, TableInfoSnapshot oldVersion) {
        LambdaQueryWrapper<TgTableInfoSnapshotCompare> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // lambdaQueryWrapper.eq(TgTableInfoSnapshotCompare::getNewVersionId, newVersion.getId());
        // lambdaQueryWrapper.eq(TgTableInfoSnapshotCompare::getOldVersionId, oldVersion.getId());
        // lambdaQueryWrapper.in(TgTableInfoSnapshotCompare::getState,
        // Arrays.asList(TableInfoSnapshotCompareState.COMPLETED.getType(),
        // TableInfoSnapshotCompareState.RUNNING.getType()));
        // lambdaQueryWrapper.ne(TgTableInfoSnapshotCompare::getResultState,
        // TableInfoSnapshotCompareResultState.DELETED.getType());
        // lambdaQueryWrapper.last("limit 1");
        // TgTableInfoSnapshotCompare one = tgTableInfoSnapshotCompareDAO.getOne(lambdaQueryWrapper);
        // if (Objects.nonNull(one)) {
        // throw new CustomException("当前对比任务已存在，请勿重复新建");
        // }

        // lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TgTableInfoSnapshotCompare::getTableId, newVersion.getTableId());
        lambdaQueryWrapper.in(TgTableInfoSnapshotCompare::getState, Collections.singletonList(TableInfoSnapshotCompareState.RUNNING.getType()));
        lambdaQueryWrapper.last("limit 1");
        TgTableInfoSnapshotCompare one = tgTableInfoSnapshotCompareDAO.getOne(lambdaQueryWrapper);
        if (Objects.nonNull(one)) {
            throw new CustomException("已存在【执行中】对比任务，请勿重复新建");
        }
    }

    /**
     * 获取表字段信息
     *
     * @param tableId 表信息
     * @return 表字段信息
     */
    private List<TableFieldInfo> getFields(Long tableId) {
        List<TableFieldInfo> fields = tableFieldInfoService.getFieldsByTableId(tableId);
        if (CollectionUtils.isEmpty(fields)) {
            throw new CustomException("该表字段设置为空");
        }
        if (fields.stream().noneMatch(i -> Objects.nonNull(i.getLogicKey()) && i.getLogicKey())) {
            throw new CustomException("该表未定义逻辑主键");
        }
        if (fields.stream().noneMatch(i -> Objects.nonNull(i.getCompareField()) && i.getCompareField())) {
            throw new CustomException("该表无参与比对字段");
        }
        return fields;
    }

    /**
     * 组装唯一键 key
     *
     * @param fields 表字段信息
     * @return 唯一键
     */
    private String getUniqueKey(List<TableFieldInfo> fields) {
        Set<String> uniqueKeyNames = fields.stream().filter(i -> Objects.nonNull(i.getLogicKey()) && i.getLogicKey()).map(TableFieldInfo::getFieldName).collect(Collectors.toSet());
        StringBuilder uniqueKeyStringBuilder = new StringBuilder();
        for (String uniqueKeyName : uniqueKeyNames) {
            uniqueKeyStringBuilder.append("toString(").append(uniqueKeyName).append(") ").append("||").append("'_'").append("||");
        }
        return uniqueKeyStringBuilder.substring(0, uniqueKeyStringBuilder.length() - 7);
    }

    /**
     * 拼接查询条件
     *
     * @param tableId 表id
     * @return 查询条件
     */
    private String buildSelectSql(Long tableId) {
        LambdaQueryWrapper<TgTableInfoSnapshotCompareLimit> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TgTableInfoSnapshotCompareLimit::getTableId, tableId);
        List<TgTableInfoSnapshotCompareLimit> limits = tgTableInfoSnapshotCompareLimitDAO.list(lambdaQueryWrapper);
        if (!CollectionUtils.isEmpty(limits)) {
            Set<String> conditions = limits.stream().map(TgTableInfoSnapshotCompareLimit::getConditionSql).collect(Collectors.toSet());

            return " where " + StringUtils.join(conditions, " and ");
        }
        return "";
    }

    /**
     * 校验ck表是否存在
     *
     * @param snapshot 快照信息
     */
    private void verifyCkTableExist(TableInfoSnapshot snapshot) {
        String tableNameDistributed = snapshot.getTableNameDistributed();

        Long exists = tgCkProviderMapper.countAllDataFromCk("exists " + tableNameDistributed);
        if (Objects.isNull(exists) || exists < 1) {
            throw new CustomException(String.format("表不存在：【%s】,版本：【%s】", tableNameDistributed, "v" + snapshot.getVersion()));
        }
    }

    /**
     * 判断表是否存在
     *
     * @param tableName ck表名
     * @return 表是否存在
     */
    private boolean verifyCkTableNotExist(String tableName, String hostName) {
        Long exists = ckClusterAdapter.executeCountHost(hostName, "exists " + tableName);
        return Objects.isNull(exists) || exists <= 0;
    }

    /**
     * 判断表是否存在
     *
     * @param tableName ck表名
     * @return 表是否存在
     */
    private boolean verifyHiveTableExist(String tableName) {
        try {
            tgHiveProviderMapper.count("select 1 from   " + tableName + "  limit 1 ");
            return true;
        } catch (Exception e) {
            log.info("hive表不存在：{}", tableName);
            return false;
        }
    }

    /**
     * 判断唯一键是否重复
     *
     * @param uniqueKey    唯一键
     * @param conditionSql 条件sql
     * @param version      版本信息
     */
    private void verifyUniqueKeyRepeat(String uniqueKey, String conditionSql, TableInfoSnapshot version) {
        String uniqueKeyCountSql = "select count(*) from ( select (" + uniqueKey + ")  as unique_key,count(1) as countnum " + " from " + version.getTableNameDistributed() + conditionSql + " group by unique_key having countnum > 1)";
        Long count = tgCkProviderMapper.countAllDataFromCk(uniqueKeyCountSql);
        if (Objects.nonNull(count) && count > 0) {
            throw new CustomException(String.format("数据表：%s逻辑主键重复，重复数：%s", version.getTableNameDistributed(), count));
        }
    }

    /**
     * 初始化比对任务信息
     *
     * @param tableId    表信息
     * @param newVersion 新版本信息
     * @param oldVersion 旧版本信息
     * @param request    请求信息
     * @return 任务信息
     */
    private TgTableInfoSnapshotCompare initTask(Long tableId, TableInfoSnapshot newVersion, TableInfoSnapshot oldVersion, TableDiffRequest request) {
        TgTableInfoSnapshotCompare task = new TgTableInfoSnapshotCompare();
        task.setTableId(tableId);
        task.setBizId(request.getBizId());
        task.setPlanId(request.getPlanId());
        task.setNewVersionId(newVersion.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");

        String newVersionPeriod;
        if (StringUtils.isNotBlank(newVersion.getRemark())) {
            newVersionPeriod = newVersion.getRemark() + "_" + newVersion.getSyncTime().format(formatter);
        } else {
            newVersionPeriod = "v" + newVersion.getVersion() + (StringUtils.isNotEmpty(newVersion.getVersionPeriod()) ? "(" + newVersion.getVersionPeriod() + ")" : "") + "_" + newVersion.getSyncTime().format(formatter);
        }
        task.setNewVersionPeriod(newVersionPeriod);
        task.setNewPeriod(newVersion.getVersionPeriod());
        task.setNewVersion(newVersion.getVersion());
        task.setOldVersionId(oldVersion.getId());
        String oldVersionPeriod;
        if (StringUtils.isNotBlank(oldVersion.getRemark())) {
            oldVersionPeriod = oldVersion.getRemark() + "_" + oldVersion.getSyncTime().format(formatter);
        } else {
            oldVersionPeriod = "v" + oldVersion.getVersion() + (StringUtils.isNotEmpty(oldVersion.getVersionPeriod()) ? "(" + oldVersion.getVersionPeriod() + ")" : "") + "_" + oldVersion.getSyncTime().format(formatter);
        }
        task.setOldVersionPeriod(oldVersionPeriod);
        task.setOldPeriod(oldVersion.getVersionPeriod());
        task.setOldVersion(oldVersion.getVersion());
        task.setState(TableInfoSnapshotCompareState.WAITING.getType());
        task.setResultState(TableInfoSnapshotCompareResultState.INIT.getType());
        task.setPlanExecuteTime(request.getPlanExecuteTime());
        task.setFailReason(null);
        try {
            task.setCreator(SecurityUtils.getUserId());
        } catch (Exception e) {
            task.setCreator(0L);
        }
        task.setCallbackUrl(request.getCallBackUrl());
        // 保存任务
        tgTableInfoSnapshotCompareDAO.save(task);
        return task;
    }

    /**
     * 构建异常结果
     *
     * @param task       任务
     * @param errMessage 异常信息
     */
    private void buildFail(TgTableInfoSnapshotCompare task, String errMessage) {
        Date date = new Date();
        task.setState(TableInfoSnapshotCompareState.FAIL.getType());
        task.setFailReason(errMessage);
        task.setUpdateTime(date);
        task.setFinishTime(date);
        tgTableInfoSnapshotCompareDAO.updateById(task);
    }

    /**
     * 获取字段顺序map
     *
     * @param tableName 表名称
     * @param hostName  ck hostname
     * @return 字段顺序map
     */
    private Map<String, Integer> showTableFieldSort(String tableName, String hostName) {
        List<SQLTableElement> fields = showTableField(tableName, hostName);
        Map<String, Integer> r = new HashMap<>();

        int num = 0;
        for (SQLTableElement field : fields) {
            SQLColumnDefinition def = (SQLColumnDefinition) field;
            String columnName = def.getName().getSimpleName();
            columnName = columnName.replaceAll("`", "");
            r.put(columnName, ++num);
        }
        return r;
    }

    /**
     * 获取表字段信息
     *
     * @param tableName 表名称
     * @param hostName  ck hostname
     * @return 表字段信息
     */
    private List<SQLTableElement> showTableField(String tableName, String hostName) {
        String localSql = ckClusterAdapter.showCreateTable(hostName, "SHOW CREATE TABLE " + tableName);
        log.info("localSql={}", localSql);

        int engineIdx = localSql.indexOf("ENGINE");
        String table = localSql.substring(0, engineIdx);
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(table, JdbcConstants.CLICKHOUSE);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(sqlStatements)) {
            throw new RuntimeException("SQL解析错误");
        }
        SQLCreateTableStatement statement = (SQLCreateTableStatement) sqlStatements.get(0);
        List<SQLTableElement> fields = statement.getTableElementList();
        if (Objects.isNull(fields)) {
            throw new RuntimeException("SELECT查询字段为空");
        }
        return fields;
    }

    /**
     * 选中ck节点
     *
     * @return ck节点host
     */
    private String selectCkNode() {
        return ckClusterAdapter.determineDataSource(null).map(CkDataSource::getHostName).orElseThrow(() -> new CustomException("选择ck节点失败"));
    }

    /**
     * 构建hive差异表名称
     *
     * @param tableInfo  源表信息
     * @param taskId     任务编号
     * @param newVersion 新版
     * @param oldVersion 旧版
     * @param dimIndex   类型表【tag/index】
     * @return hive差异表名称
     */
    private String buildHiveDiffTableName(TableInfo tableInfo, Long taskId, Integer newVersion, Integer oldVersion, String dimIndex) {
        String hiveTableNameSuffix = getHiveTableNameSuffix();
        if (StringUtils.isNotBlank(hiveTableNameSuffix)) {
            hiveTableNameSuffix = "_" + hiveTableNameSuffix;
        }
        return tableInfo.getTableNameDistributed() + "_v" + newVersion + "_v" + oldVersion + "_" + dimIndex + "_diff" + "_" + taskId + hiveTableNameSuffix;
    }

    /**
     * 构建hdfs表名称
     *
     * @param version           版本信息
     * @param category          类型表【tag/index】
     * @param taskId            任务编号
     * @param ckTableNameSuffix ck表后缀
     * @return hdfs表名称
     */
    private String buildHdfsTableName(TableInfoSnapshot version, String category, Long taskId, String ckTableNameSuffix) {
        return getCkTableNamePrefix() + "_" + version.getTableNameDistributed() + "_" + category + "_" + taskId + ckTableNameSuffix;
    }

    /**
     * ck表重命名
     *
     * @param oldName 旧表名称
     * @param newName 新表名称
     */
    private void renameHiveTable(String oldName, String newName) {
        String sql = "ALTER TABLE " + oldName + " RENAME TO " + newName;
        tgHiveProviderMapper.createTableAccordingApplication(sql);
    }

    /**
     * 更名环节
     *
     * @param tableInfo                      表信息
     * @param wideTableShardName             宽表名称
     * @param wideTableShardTempName         宽表临时表名称
     * @param changeDetailTableShardName     比对信息表名称
     * @param changeDetailTableShardTempName 比对信息表临时名称
     */
    private void rename(TableInfo tableInfo, String wideTableShardName, String wideTableShardTempName, String changeDetailTableShardName, String changeDetailTableShardTempName) {
        // rename,先判断是否已存在对应得表了
        if (verifyHiveTableExist(wideTableShardName)) {
            List<TgTableInfoSnapshotCompareDetail> list = tgTableInfoSnapshotCompareDetailFacade.qryCompareTableInfo(wideTableShardName);
            if (!CollectionUtils.isEmpty(list)) {
                renameOldTable(tableInfo, "_wideTable_shard", getCkTableNameSuffix(), list);
            }
        }
        // 直接rename
        renameHiveTable(wideTableShardTempName, wideTableShardName);
        if (verifyHiveTableExist(changeDetailTableShardName)) {
            List<TgTableInfoSnapshotCompareDetail> list = tgTableInfoSnapshotCompareDetailFacade.qryCompareTableInfo(changeDetailTableShardName);
            if (!CollectionUtils.isEmpty(list)) {
                renameOldTable(tableInfo, "_change_detail_shard", getCkTableNameSuffix(), list);
            }
        }
        // 直接rename
        renameHiveTable(changeDetailTableShardTempName, changeDetailTableShardName);
    }

    /**
     * 处理旧表
     *
     * @param tableInfo         表信息
     * @param tableName         表名称
     * @param ckTableNameSuffix 表后缀
     * @param list              旧纪录数据
     */
    private void renameOldTable(TableInfo tableInfo, String tableName, String ckTableNameSuffix, List<TgTableInfoSnapshotCompareDetail> list) {
        for (TgTableInfoSnapshotCompareDetail detail : list) {
            if (verifyHiveTableExist(detail.getTableName())) {
                // 直接rename
                String newName = tableInfo.getTableNameDistributed() + tableName + "_" + detail.getId() + ckTableNameSuffix;
                renameHiveTable(detail.getTableName(), newName);
                tgTableInfoSnapshotCompareDetailFacade.updateDetailTableName(detail.getId(), newName);
            }
        }
    }

    /**
     * 清除临时表
     *
     * @param taskId 任务编号
     */
    public void dropTaskTable(Long taskId, boolean allData) {
        List<TgTableInfoSnapshotCompareDetail> list = tgTableInfoSnapshotCompareDetailFacade.queryByCompareId(taskId);
        if (!CollectionUtils.isEmpty(list)) {
            for (TgTableInfoSnapshotCompareDetail i : list) {
                JSONObject jo = JSONObject.parseObject(i.getAttach());
                String hostname = jo.getString(HOSTNAME);
                String ckTable = jo.getString(CK_TABLE_KEY);
                String tempName = jo.getString(TEMP_KEY);
                boolean needDropHive = true;
                String ckTableName = null;
                String hiveTableName = null;
                boolean deleted = false;
                if (Objects.equals(TableInfoSnapshotCompareDetailCategory.DIFF_DETAIL.getType(), i.getCategory()) || Objects.equals(TableInfoSnapshotCompareDetailCategory.DIFF_WIDE.getType(), i.getCategory())) {
                    if (allData) {
                        deleted = true;
                        hiveTableName = i.getTableName();
                    } else {
                        needDropHive = false;
                    }
                    if (StringUtils.isNotBlank(ckTable)) {
                        ckTableName = ckTable;
                    }
                } else {
                    deleted = true;
                    ckTableName = hiveTableName = i.getTableName();
                }

                if (!verifyCkTableNotExist(ckTableName, hostname)) {
                    ckClusterAdapter.executeHost(hostname, "DROP TABLE " + ckTableName);
                }
                if (needDropHive && verifyHiveTableExist(hiveTableName)) {
                    tgHiveProviderMapper.createTableAccordingApplication("DROP TABLE " + hiveTableName);
                }
                if (StringUtils.isNotBlank(tempName) && !verifyCkTableNotExist(tempName, hostname)) {
                    ckClusterAdapter.executeHost(hostname, "DROP TABLE " + tempName);
                }
                if (deleted) {
                    tgTableInfoSnapshotCompareDetailFacade.remove(i);
                }
            }
        }
    }
}
