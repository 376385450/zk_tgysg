package com.sinohealth.web.controller.monitor;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.util.BooleanUtils;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Sets;
import com.sinohealth.common.alert.AlertTemplate;
import com.sinohealth.common.alert.plugin.wechatrobot.message.MarkdownMessage;
import com.sinohealth.common.annotation.IgnoreLog;
import com.sinohealth.common.config.TransferProperties;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.LogConstant;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.FtpStatus;
import com.sinohealth.common.enums.application.ApplyDataStateEnum;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.common.enums.dict.FieldGranularityEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.JsonUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.ip.IpUtils;
import com.sinohealth.data.intelligence.api.metadata.dto.MetaColumnDTO;
import com.sinohealth.data.intelligence.enums.DataSourceType;
import com.sinohealth.framework.config.ContextCopyingTaskDecorator;
import com.sinohealth.quartz.util.JobInvokeUtil;
import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;
import com.sinohealth.system.biz.application.constants.ApplyStateEnum;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.dao.ApplicationFormDAO;
import com.sinohealth.system.biz.application.domain.ApplicationForm;
import com.sinohealth.system.biz.application.domain.ApplicationTaskConfig;
import com.sinohealth.system.biz.application.dto.*;
import com.sinohealth.system.biz.application.dto.request.TransHistoryApplyRequest;
import com.sinohealth.system.biz.application.entity.HistoryApplyQuoteEntity;
import com.sinohealth.system.biz.application.service.ApplicationFormService;
import com.sinohealth.system.biz.application.service.ApplicationTaskConfigService;
import com.sinohealth.system.biz.application.util.CostTimeUtil;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.application.util.WideTableSqlBuilder;
import com.sinohealth.system.biz.ck.adapter.CKClusterAdapter;
import com.sinohealth.system.biz.ck.constant.SnapshotTableStateEnum;
import com.sinohealth.system.biz.ck.dao.SnapshotTableMappingDAO;
import com.sinohealth.system.biz.ck.domain.SnapshotTableMapping;
import com.sinohealth.system.biz.common.FileAdapter;
import com.sinohealth.system.biz.common.RedisSemaphore;
import com.sinohealth.system.biz.dataassets.constant.AssetsQcTypeEnum;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowBatchDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsFlowBatchDetailDAO;
import com.sinohealth.system.biz.dataassets.dao.AssetsWideUpgradeTriggerDAO;
import com.sinohealth.system.biz.dataassets.dao.PowerBiPushDetailDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsFlowBatch;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcDetail;
import com.sinohealth.system.biz.dataassets.domain.AssetsWideUpgradeTrigger;
import com.sinohealth.system.biz.dataassets.domain.PowerBiPushDetail;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;
import com.sinohealth.system.biz.dataassets.dto.AssetsCompareLastSelectDTO;
import com.sinohealth.system.biz.dataassets.dto.AssetsQcPageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsQcPageRequest;
import com.sinohealth.system.biz.dataassets.helper.AssetsCompareInvoker;
import com.sinohealth.system.biz.dataassets.helper.UserDataAssetsUploadFtpHelper;
import com.sinohealth.system.biz.dataassets.service.AssetsCompareService;
import com.sinohealth.system.biz.dataassets.service.AssetsFlowService;
import com.sinohealth.system.biz.dataassets.service.AssetsQcService;
import com.sinohealth.system.biz.dataassets.service.AssetsUpgradeTriggerService;
import com.sinohealth.system.biz.dataassets.service.PowerBiPushService;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.biz.dict.dao.MetricsDictDAO;
import com.sinohealth.system.biz.dict.domain.MetricsDict;
import com.sinohealth.system.biz.dict.util.ExcelUtil;
import com.sinohealth.system.biz.hook.processor.ContextCloseHandler;
import com.sinohealth.system.biz.hook.processor.ServerTStat;
import com.sinohealth.system.biz.monitor.adapter.CompareAdapter;
import com.sinohealth.system.biz.process.dto.CreateAutoProcessRequest;
import com.sinohealth.system.biz.process.facade.TgFlowProcessFacade;
import com.sinohealth.system.biz.process.service.TgFlowProcessCheckService;
import com.sinohealth.system.biz.process.vo.DqcQcLogVO;
import com.sinohealth.system.biz.project.dao.ProjectDAO;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.service.DataPlanService;
import com.sinohealth.system.biz.scheduler.dto.request.TypeConvertParam;
import com.sinohealth.system.biz.scheduler.service.IntegrateSyncProcessDefService;
import com.sinohealth.system.biz.scheduler.service.IntegrateSyncTaskService;
import com.sinohealth.system.biz.table.dao.TableInfoSnapshotDAO;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import com.sinohealth.system.biz.table.service.TableInfoSnapshotService;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.biz.transfer.service.A650ProjectImporter;
import com.sinohealth.system.biz.transfer.service.TransferAdapter;
import com.sinohealth.system.biz.ws.service.WsMsgService;
import com.sinohealth.system.config.AlertBizType;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.domain.*;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.AsyncTaskConst;
import com.sinohealth.system.domain.constant.RequireAttrType;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.dto.BaseDataSourceParamDto;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.dto.application.deliver.request.HistoryQueryRequest;
import com.sinohealth.system.dto.assets.AssetIndicatorQuery;
import com.sinohealth.system.dto.auditprocess.ProcessNodeEasyDto;
import com.sinohealth.system.dto.common.PageRequest;
import com.sinohealth.system.job.application.SnapshotTableSyncJob;
import com.sinohealth.system.mapper.ProjectHelperMapper;
import com.sinohealth.system.mapper.SysUserMapper;
import com.sinohealth.system.mapper.TableFieldInfoMapper;
import com.sinohealth.system.mapper.TableInfoMapper;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import com.sinohealth.system.mapper.TgTemplatePackTailSettingMapper;
import com.sinohealth.system.service.*;
import com.sinohealth.system.service.impl.DefaultSyncHelper;
import com.sinohealth.system.util.ApplicationSqlUtil;
import com.sinohealth.system.util.EasyExcelUtil;
import com.sinohealth.system.util.HistoryApplyUtil;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 任务执行入口
 * 后门调试接口
 */
@Slf4j
@RestController
@RequestMapping({"/task", "/api/task"})
public class TaskController {
    private static final OkHttpClient client = new OkHttpClient.Builder().readTimeout(Duration.ofMillis(500)).callTimeout(Duration.ofMillis(500)).connectTimeout(Duration.ofMillis(500)).build();
    /**
     * 忽略的数据 id值
     */
    private final Set<Long> wideIgnore = new HashSet<>(Arrays.asList(
//            1L,// 临时加入，避免重复创建申请
            27L, 28L, 33L, 34L, 35L, 36L, 37L, 38L, 58L, 59L, 111L, 112L, 113L, 114L, 115L, 116L, 117L, 118L, 119L, 120L, 121L, 122L, 125L, 126L, 127L, 128L, 129L, 130L, 131L, 132L, 133L, 134L, 138L, 139L, 140L, 141L, 142L, 143L, 144L, 145L, 146L, 147L, 148L, 149L, 150L, 151L, 152L, 153L, 154L, 155L, 156L, 157L, 158L, 159L, 171L, 172L, 173L, 174L, 175L, 176L, 202L, 203L, 204L, 205L));
    @Autowired
    private ITaskService taskService;
    @Autowired
    private SnapshotTableSyncJob snapshotTableSyncJob;
    @Autowired
    private IAsyncTaskService asyncTaskService;
    @Autowired
    private DefaultSyncHelper syncHelper;
    @Autowired
    private UserDataAssetsService userDataAssetsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IApplicationService applicationService;
    @Autowired
    private CompareAdapter compareAdapter;
    @Resource
    @Qualifier(ThreadPoolType.POST_MSG)
    private ThreadPoolTaskExecutor postMsgPool;
    @Resource
    @Qualifier(ThreadPoolType.SCHEDULER)
    private ScheduledExecutorService scheduler;
    @Resource
    @Qualifier(ThreadPoolType.ENHANCED_TTL)
    private Executor ttl;
    @Autowired
    private ContextCloseHandler contextCloseHandler;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private ProjectDAO projectDAO;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;
    @Autowired
    private UserDataAssetsUploadFtpHelper userDataAssetsUploadFtpHelper;
    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;
    @Autowired
    private PoolingHttpClientConnectionManager poolingConnectionManager;
    @Autowired
    private AssetsCompareService assetsCompareService;
    @Autowired
    private ProjectHelperMapper projectHelperMapper;
    @Qualifier("sysUserMapper")
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private TgFlowProcessFacade tgFlowProcessFacade;
    @Autowired
    private AssetsFlowService assetsFlowService;
    @Resource
    @Qualifier(AlertBizType.BIZ)
    private AlertTemplate alertTemplate;
    @Autowired
    private TgTemplateInfoMapper templateInfoMapper;
    @Autowired
    private TgTemplatePackTailSettingMapper tgTemplatePackTailSettingMapper;
    @Autowired
    private TableFieldInfoMapper tableFieldInfoMapper;
    @Autowired
    private TableInfoMapper tableInfoMapper;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private MockUserService mockUserService;
    @Autowired
    private TransferProperties transferProperties;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private TransferAdapter transferAdapter;
    @Autowired
    private AssetsUpgradeTriggerService upgradeTriggerService;
    @Autowired
    private IntegrateSyncProcessDefService syncProcessDefService;
    @Autowired
    private CKClusterAdapter ckClusterAdapter;

    // TODO 清空堆积任务
    @Autowired
    private IntegrateSyncTaskService integrateSyncTaskService;
    @Autowired
    private AssetsUpgradeTriggerService assetsUpgradeTriggerService;
    @Autowired
    private IMyDataDirService dirService;
    @Autowired
    private WsMsgService msgService;
    @Autowired
    private A650ProjectImporter a650ProjectImporter;
    @Autowired
    private AssetsCompareInvoker assetsCompareInvoker;
    @Autowired
    private PowerBiPushService powerBiPushService;
    @Autowired
    private ApplicationTaskConfigService applicationTaskConfigService;
    @Autowired
    private ApplicationDAO applicationDAO;
    @Autowired
    private AssetsQcService qcService;
    @Autowired
    private TemplateInfoDAO templateInfoDAO;
    /**
     * 模板变更时，强制变更新SQL
     */
//    @GetMapping("/fixNewSql")
//    public AjaxResult<Void> fixNewSqlForTemplate() throws InterruptedException {
//        List<Long> tempIds = Arrays.asList(108L, 103L, 153L, 152L, 100L);
//
//        List<TgTemplateInfo> tempInfos = templateInfoDAO.getBaseMapper().selectBatchIds(tempIds);
//        Map<Long, Integer> tempMap = Lambda.buildMap(tempInfos, TgTemplateInfo::getId, TgTemplateInfo::getVersion);
//
//        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
//                .in(TgApplicationInfo::getTemplateId, tempIds)
//                .isNull(TgApplicationInfo::getNewApplicationId)
//                .eq(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.AUDIT_PASS)
//                .gt(TgApplicationInfo::getDataExpir, new Date())
//                .list();
//
//        log.info("apply:{} temp:{}", applyList.size(), tempInfos.size());
//
//        TimeUnit.SECONDS.sleep(5);
//
//        for (TgApplicationInfo info : applyList) {
//            ttl.execute(() -> {
//                Integer latest = tempMap.get(info.getTemplateId());
//                if (Objects.equals(info.getTemplateVersion(), latest)) {
//                    log.warn("IGNORE: id={} name={}", info.getId(), info.getProjectName());
//                    return;
//                }
//                JsonBeanConverter.convert2Obj(info);
//                String asql = info.getAsql();
//                String tail = info.getTailSql();
//                boolean fillResult = new WideTableSqlBuilder().fillApplication(info);
//                if (!fillResult) {
//                    throw new RuntimeException("申请失败，请反馈技术人员处理");
//                }
//
//                log.warn("apply: id={} name={} ver={} latest={}", info.getId(), info.getProjectName(),
//                        info.getTemplateVersion(), latest);
//                log.info("main \n{}\n{}\n", asql, info.getAsql());
//
//                if (Objects.isNull(tail) && Objects.nonNull(info.getTailSql())) {
//                    log.error("WARNNNNN tail \n{}\n", info.getTailSql());
//                } else if (Objects.nonNull(tail) && Objects.nonNull(info.getTailSql())) {
//                    log.info("tail \n{}\n{}\n", tail, info.getTailSql());
//                }
//
//                applicationDAO.lambdaUpdate()
//                        .eq(TgApplicationInfo::getId, info.getId())
//                        .set(TgApplicationInfo::getTemplateVersion, latest)
//                        .set(TgApplicationInfo::getAsql, info.getAsql())
//                        .set(TgApplicationInfo::getTailSql, info.getTailSql())
//                        .update();
//
//            });
//        }
//
//        return AjaxResult.succeed();
//    }

    @Autowired
    private AssetsWideUpgradeTriggerDAO assetsWideUpgradeTriggerDAO;
    @Autowired
    private IAssetService assetService;
    @Autowired
    private AssetsQcService assetsQcService;
    @Autowired
    private ApplicationTaskConfigService taskConfigService;
    @Autowired
    private SnapshotTableMappingDAO snapshotTableMappingDAO;
    @Autowired
    private FileAdapter fileAdapter;

    // 1 常规
    // 2 长尾
    // 3 品牌
    @Autowired
    private PowerBiPushDetailDAO powerBiPushDetailDAO;
    @Resource(name = "slaveDataSource")
    private DruidDataSource slaveDataSource;
    @Autowired
    private TgFlowProcessCheckService flowProcessCheckService;
    @Autowired
    private TableInfoSnapshotService tableInfoSnapshotService;
    @Autowired
    private RedisSemaphore redisSemaphore;
    @Autowired
    private MetricsDictDAO metricsDictDAO;
    @Autowired
    private AssetsFlowBatchDetailDAO assetsFlowBatchDetailDAO;
    @Autowired
    private AssetsFlowBatchDAO flowBatchDAO;
    @Autowired
    private TableInfoSnapshotDAO tableInfoSnapshotDAO;

    @IgnoreLog
    @GetMapping("/ips")
    public String serverList() {
        StringBuilder res = new StringBuilder();
        try {
            NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();
            List<Instance> list = namingService.selectInstances("tg-easy-fetch", true);

            for (Instance instance : list) {
                res.append(instance.getIp()).append(":").append(instance.getPort()).append("\n");
                log.info("HOST {} {}", instance.getIp(), instance.getPort());
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return res.toString();
    }

    @IgnoreLog
    @GetMapping("/online")
    public List online() {
        Map map = redisTemplate.opsForHash().entries(RedisKeys.Ws.Router);
        if (MapUtils.isEmpty(map)) {
            return Collections.emptyList();
        }
        List<SysUser> users = userMapper.selectList(new QueryWrapper<SysUser>().lambda().select(SysUser::getUserId, SysUser::getUserName, SysUser::getRealName, SysUser::getPhonenumber).in(SysUser::getUserId, map.keySet()));
        for (SysUser user : users) {
            user.setAvatar(Optional.ofNullable(map.get(user.getUserId())).map(Object::toString).orElse(""));
        }
        return users;
    }

    @IgnoreLog
    @GetMapping("/tstat")
    public String threadStatus() {
        return IpUtils.getHostIp() + "\n" + contextCloseHandler.stat();
    }

    @IgnoreLog
    @GetMapping("/serverStat")
    public ServerTStat serverInfo() {
        ServerTStat server = new ServerTStat();
        server.setStat(contextCloseHandler.statList());
        server.setIp(IpUtils.getHostIp());
        return server;
    }

    @IgnoreLog
    @GetMapping("/allStat")
    public String allThreadStatus() {
        List<String> infos = new ArrayList<>();
        try {
            NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();
            List<Instance> list = namingService.selectInstances("tg-easy-fetch", true);

            for (Instance instance : list) {
                String server = instance.getIp() + ":" + instance.getPort();
                try (Response resp = client.newCall(new Request.Builder().url("http://" + server + "/tg-easy-fetch/task/serverStat").build()).execute()) {
                    String rs = resp.body().string();
                    if (rs.contains("ip")) {
                        ServerTStat stat = JsonUtils.parse(rs, ServerTStat.class);
                        stat.setIp(instance.getIp() + ":" + instance.getPort());
                        infos.add(JsonUtils.format(stat));
                    }
                } catch (Exception e) {
//                    log.error("{} {}", server, e.getMessage());
                }
            }
        } catch (Exception e) {
//            log.error("", e);
        }
        return "[" + String.join(",", infos) + "]";
    }

    // 以上为监控接口
    @GetMapping("/countTableRow")
    public AjaxResult countTableRowByUpdateTime() {
        taskService.countTableRowByUpdateTime(new Date());
        return AjaxResult.success();
    }

    @GetMapping("/useStatics")
    public AjaxResult updateUseStatics() {
        taskService.updateUseStatics(new Date());
        return AjaxResult.success();
    }

    @GetMapping("/syncTableInfo")
    public AjaxResult syncTableInfo() {
        taskService.syncTableInfo();

        return AjaxResult.success();
    }

    /**
     * 更新表热度信息
     */
    @GetMapping("/updateTableHeat")
    public AjaxResult updateTableHeat() {
        taskService.updateTableHeat();
        return AjaxResult.success();
    }

    @GetMapping("/syncOutDateTableData")
    public AjaxResult syncOutDateTableData() throws InterruptedException {
        snapshotTableSyncJob.syncOutDateTableData();
        return AjaxResult.success();
    }

    @GetMapping("/benchmarkSyncShutDown")
    public String benchmarkSyncShutDown() {
        snapshotTableSyncJob.cancelSync();
        return "OK";
    }

    @GetMapping("/rePushToCustomer")
    public Boolean rePushToCustomer(@RequestParam("applyId") Long applyId) {
        boolean finish = syncHelper.syncApplicationTableToCustomerDatasource(applyId, null, 0L);
        log.info("finish={}", finish);
        return finish;
    }

    @GetMapping("/retryDownload")
    public String retryDownload(@RequestParam("id") String id) {
        redisTemplate.opsForList().remove(RedisKeys.TASK_EXECUTING, 1, id);
        redisTemplate.opsForList().rightPush(RedisKeys.TASK_QUEUE, id);
        return "OK";
    }

    // TODO 重推失效任务
    @GetMapping("/retryAllDownload")
    public String retryAllDownload() {
        List<AsyncTask> asyncTasks = asyncTaskService.selectList(AsyncTaskConst.Status.HANGING, null, null);
        for (AsyncTask asyncTask : asyncTasks) {
            redisTemplate.opsForList().remove(RedisKeys.TASK_EXECUTING, 1, asyncTask.getId());
            redisTemplate.opsForList().rightPush(RedisKeys.TASK_QUEUE, asyncTask.getId());
        }
        return "OK";
    }

    @GetMapping("/cleanUserCache")
    public String cleanUserCache() {
        SinoipaasUtils.cache.invalidateAll();
        return "OK";
    }

    @GetMapping("/fillDefault")
    public String fillDefault() {
        List<TgTemplateInfo> infos = new TgTemplateInfo().selectAll();
        for (TgTemplateInfo info : infos) {
            JsonBeanConverter.convert2Obj(info);

            boolean needMod = false;
            List<TemplateGranularityDto> gs = info.getGranularity();
            if (CollectionUtils.isNotEmpty(gs)) {
                for (TemplateGranularityDto g : gs) {
                    Boolean granularityRequired = g.getGranularityRequired();
                    if (Objects.isNull(granularityRequired)) {
                        log.info("info={}", info.getId());
                        g.setGranularityRequired(true);
                        needMod = true;
                    }
                }
            }
            if (needMod) {
                JsonBeanConverter.convert2Json(info);
                info.updateById();
            }
        }
        return "OK";
    }

    @GetMapping("/alert")
    public String alert() {
        MarkdownMessage alert = MarkdownMessage.build("<font color=\"warning" + "\">任务异常</font> \n" + ">1. [系统地址](http://tgysg-dev.sinohealth.cn)\n" + ">1. 工作流名称：<font color=\"info\">CMH</font>");
        alert.setMsgtype("markdown");
        alertTemplate.send(alert);
        return "OK";
    }

    @Transactional
    @GetMapping("/closeTemplateCustom")
    public String closeTemplateCustom() {
        List<TgTemplateInfo> infos = templateInfoMapper.selectList(Wrappers.<TgTemplateInfo>lambdaQuery().ne(TgTemplateInfo::getTemplateType, TemplateTypeEnum.wide_table.name()));
        if (CollectionUtils.isEmpty(infos)) {
            return "EMPTY";
        }
        for (TgTemplateInfo info : infos) {
            JsonBeanConverter.convert2Obj(info);

            // 关闭空明细的自定义设置
            boolean exist = false;
            for (TemplateGranularityDto dto : info.getGranularity()) {
                if (CollectionUtils.isEmpty(dto.getDetails()) && BooleanUtils.isTrue(dto.getEnableRangeTemplate())) {
                    exist = true;
                    dto.setEnableRangeTemplate(false);
                }
            }
            if (exist) {
                log.info("MOD: id={}", info.getId());
                JsonBeanConverter.convert2Json(info);
                templateInfoMapper.update(null, new UpdateWrapper<TgTemplateInfo>().lambda().set(TgTemplateInfo::getGranularityJson, info.getGranularityJson()).eq(TgTemplateInfo::getId, info.getId()));
            }
        }

        return "";
    }

    @GetMapping("/resortField")
    public AjaxResult<List<SortVO>> resortField() {
        List<TableFieldInfo> fields = tableFieldInfoMapper.selectList(new QueryWrapper<TableFieldInfo>().lambda().select(TableFieldInfo::getId, TableFieldInfo::getTableId, TableFieldInfo::getFieldName).eq(TableFieldInfo::getSort, 0));
        Set<Long> tableIds = Lambda.buildSet(fields, TableFieldInfo::getTableId);
        if (CollectionUtils.isEmpty(tableIds)) {
            return AjaxResult.error("EMPTY");
        }

        List<SortVO> result = new ArrayList<>();

        Map<Long, List<TableFieldInfo>> tableFieldMap = Lambda.buildGroupMap(fields, TableFieldInfo::getTableId, v -> Objects.nonNull(v.getTableId()));

        List<TableInfo> tables = tableInfoMapper.selectList(new QueryWrapper<TableInfo>().lambda().in(TableInfo::getId, tableIds).eq(TableInfo::getIsDiy, 0));
        for (TableInfo table : tables) {
            String localSql = table.getLocalSql();
            if (StringUtils.isBlank(localSql)) {
                continue;
            }

            try {
                Map<String, Integer> fieldSort = ApplicationSqlUtil.parseCKColumnSort(localSql);

                List<TableFieldInfo> field = tableFieldMap.get(table.getId());
                for (TableFieldInfo tableFieldInfo : field) {
                    Integer sort = fieldSort.get(tableFieldInfo.getFieldName());
                    if (Objects.isNull(sort) || sort == 0) {
                        continue;
                    }
                    result.add(SortVO.builder().fieldId(tableFieldInfo.getId()).sort(sort).build());
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }

        if (CollectionUtils.isNotEmpty(result)) {
            for (SortVO vo : result) {
                tableFieldInfoMapper.update(null, new UpdateWrapper<TableFieldInfo>().lambda().eq(TableFieldInfo::getId, vo.getFieldId()).set(TableFieldInfo::getSort, vo.getSort()));
            }
        }

        return AjaxResult.success(result);
    }

    // API
    @GetMapping("cal")
    public String cal() {
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            Supplier<Integer> random = () -> new SecureRandom().nextInt(10000000) + 100;
            Math.tanh(Math.log(Math.sqrt(random.get()) + random.get()));
        }
        long end = System.nanoTime();


        return (end - start) + "";
    }

    /**
     * 计算差异
     */
    @GetMapping("/compare/{biz}/{no}/{dryRun}")
    public AjaxResult compare(@PathVariable("no") Long no,
                              @PathVariable("biz") Integer bizTemp) {
        try {
            if (Objects.equals(bizTemp, 1)) {
                return compareAdapter.compareAssetsForSku(no, wideIgnore);
            } else if (Objects.equals(bizTemp, 2)) {
                return compareAdapter.compareAssetsForSkuTail(no, wideIgnore);
            } else if (Objects.equals(bizTemp, 3)) {
                return compareAdapter.compareAssetsForCmhFlow(no.intValue(), wideIgnore);
            }
            return AjaxResult.error("不支持的类型");
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error("失败");
        }
    }

    /**
     * 差异列表
     */
    @GetMapping("/compareList/{biz}")
    public String compareList(@PathVariable("biz") Integer bizTemp) {
        // 处理的宽表需求 Excel Ids
        Set<Long> careWideIds = Sets.newHashSet(302L, 303L, 304L, 305L, 306L, 307L, 308L, 309L, 310L,
                311L, 312L, 313L, 314L, 315L, 316L, 317L, 318L, 319L, 320L, 321L);
        if (Objects.equals(bizTemp, 1)) {
            return compareAdapter.skuResultList(RedisKeys.Apply.TRANS_APPLY_MAP,
                    RedisKeys.Apply.VALIDATE_RESULT_MAP, careWideIds);
        } else if (Objects.equals(bizTemp, 2)) {
            return compareAdapter.skuResultList(RedisKeys.Apply.TRANS_TAIL_APPLY_MAP,
                    RedisKeys.Apply.VALIDATE_TAIL_RESULT_MAP, careWideIds);
        } else if (Objects.equals(bizTemp, 3)) {
            return compareAdapter.cmhFlowCompareList(wideIgnore);
        }
        return "NO";
    }

    /**
     * 迁移项目列表 CMH SKU 宽表
     */
    @GetMapping("/projectCompareList")
    public String skuCompareList() {
        return "<a href=\"/tg-easy-fetch/task/compareList/1\">CMH常规</a><br/>" + "<a href=\"/tg-easy-fetch/task/compareList/2\">CMH长尾</a><br/>" + "<a href=\"/tg-easy-fetch/task/compareList/3\">CMH品牌</a>";
    }

    /**
     * 数据迁移：自动审核提交的申请
     * SKU 品牌 两种模板
     *
     * @param con   并发数
     * @param batch 批量审核的批量数
     * @param no    按单id审核
     */
    @GetMapping("/autoAudit")
    public AjaxResult<Void> auditAll(@RequestParam(value = "batch", required = false) Integer batch,
                                     @RequestParam(value = "no", required = false) Long no,
                                     @RequestParam(value = "con", required = false) Integer con,
                                     @RequestParam(value = "biz") Integer bizTemp, HttpServletRequest request) {
        mockUserService.fillUserAuthById(request, transferProperties.getAuditId());

        log.info("userId:{}", SecurityUtils.getUserId());
        if (Objects.equals(bizTemp, 2)) {
            return applicationService.auditAllTail(batch, no);
        } else if (Objects.equals(bizTemp, 3)) {
            return applicationService.auditFlowSchedule(batch, no, con);
        } else if (Objects.equals(bizTemp, 4)) {
            return applicationService.auditRangeFlowSchedule(batch, no, con);
        } else if (Objects.equals(bizTemp, 5)) {
            return applicationService.auditInsFlowSchedule(batch, no, con);
        } else if (Objects.equals(bizTemp, 6)) {
            return applicationService.auditCusFlowSchedule(batch, no, con);
        } else {
            return applicationService.auditAllSku(batch, no);
        }
    }

    @GetMapping("/executeFlow/{applyId}")
    public AjaxResult executeFlow(@PathVariable("applyId") Long applyId) {
        return userDataAssetsService.executeWorkFlow(applyId);
    }

    @GetMapping("/stopAudit")
    public AjaxResult<Void> stopAuditPool(@RequestParam(value = "biz") Integer bizTemp) {
        if (Objects.equals(bizTemp, 2)) {
        } else if (Objects.equals(bizTemp, 3)) {
            return applicationService.stopFlowSchedulerPool();
        } else {
        }
        return AjaxResult.succeed();
    }

    /**
     * 只支持固定维度的模板创建申请
     * 提交申请 调试申请
     *
     * @see TaskController#batchParseSql 前置解析
     */
    @PostMapping("/transferApply")
    public AjaxResult<Void> transferApply(@RequestBody TransHistoryApplyRequest param, HttpServletRequest request) {
        mockUserService.fillUserAuthById(request, transferProperties.getApplicantId());
        param.setRetry(true);
        param.setMockSameUser(BooleanUtils.isTrue(transferProperties.getMockSameUser()));

        Object debug = redisTemplate.opsForValue().get(RedisKeys.Apply.DEBUG_MODE);
        param.setDebug(Objects.nonNull(debug) || BooleanUtils.isTrue(param.getDebug()));

        param.setIgnoreIds(wideIgnore);
        Object mode = redisTemplate.opsForValue().get(RedisKeys.Apply.TRANSFER_MODE);
        boolean tail = Objects.nonNull(mode) && Objects.equals(mode, "tail");
        if (tail) {
            return applicationService.transferTailApply(request, param);
        } else {
            return applicationService.transferApply(request, param);
        }
    }

    @GetMapping("/switchTrans")
    public String switchTransMode() {
        return transferAdapter.switchTransMode();
    }

    @GetMapping("/switchDebug")
    public String switchDebugMode() {
        return transferAdapter.switchDebugMode();
    }

    @GetMapping("/switchFTP")
    public String switchFTP() {
        return transferAdapter.switchFTP();
    }

    @GetMapping("/applyDetail")
    public AjaxResult<String> applyDetail(@RequestParam("id") Long projectId) {
        return applicationService.applyDetail(projectId);
    }

    @GetMapping("/transferStat")
    public String getTransferStat() {
        return transferAdapter.stat();
    }

    // 迁移后的申请树缺失表id
    @GetMapping("/trans/fixNullTable")
    public String fixNullTable(@RequestParam("ids") String ids) {
        return applicationService.fixNullTable(ids);
    }

    /**
     * 前端树转SQL 验证参考用，不参与实际数据流传
     */
    @PostMapping("/batchBuildSql")
    public AjaxResult<List<OneItem>> batchBuildSql(@RequestBody ParseSqlBatchRequest request) {
        List<OneItem> applyList = request.getApplyList();
        for (OneItem oneItem : applyList) {
            try {
                if (Objects.isNull(oneItem.getId())) {
                    log.warn("ignore null id");
                    continue;
                }
                if (Objects.isNull(oneItem.getFilter())) {
                    log.error("NULL {}", oneItem);
                    continue;
                }
                String sql = HistoryApplyUtil.buildSql(oneItem.getFilter());
                if (Objects.isNull(sql)) {
                    oneItem.setSql("构造SQL失败");
                } else {
                    oneItem.setSql(sql);
                }
            } catch (Exception e) {
                log.error("", e);
                return AjaxResult.error(e.getMessage());
            }
        }

        return AjaxResult.success(applyList);
    }

    @PostMapping("/batchParseSql")
    public AjaxResult<List<OneItem>> batchParseSql(@RequestBody ParseSqlBatchRequest param, HttpServletRequest request) {
        mockUserService.fillUserAuthById(request, transferProperties.getApplicantId());
        return applicationService.batchParseSql(param);
    }

    @GetMapping("/applySQL/{id}")
    public String applySQL(@PathVariable("id") Long id) {
        TgApplicationInfo info = new TgApplicationInfo().selectById(id);
        return info.getAsql() + "<br><br><br>" + Optional.ofNullable(info.getTailSql()).orElse(" ");
    }

    @GetMapping("/applyConfig/{id}")
    public ApplicationTaskConfig applyConfig(@PathVariable("id") Long id) {
        return new ApplicationTaskConfig().selectOne(new QueryWrapper<ApplicationTaskConfig>().lambda().eq(ApplicationTaskConfig::getApplicationId, id));
    }

    /**
     * 转移到用户
     */
    @GetMapping("/allocateToUser")
    public String allocateToUser() {
        return compareAdapter.allocateToUser();
    }

    /**
     * 全部 迁移概览
     */
    @GetMapping("/mostError")
    public String mostError(@RequestParam(value = "x", required = false) String plainText) {
        return compareAdapter.mostError(plainText);
    }

    @GetMapping("/batchTask")
    public void batchTask() throws InterruptedException {
        for (int i = 0; i < 120; i++) {
            int finalI = i;
            postMsgPool.submit(() -> {
                try {
                    Thread.sleep(2500);
                    log.info("run {}", finalI);
                } catch (Exception e) {
                    log.error("", e);
                }
            });
        }

        for (int i = 0; i < 1000; i++) {
            TimeUnit.MILLISECONDS.sleep(200);
            int finalI = i;
            Runnable runnable = () -> {
                try {
                    Thread.sleep(7000);
                    log.info("run {}", finalI);
                } catch (Exception e) {
                    log.error("", e);
                }
            };
            postMsgPool.submit(runnable);
            scheduler.schedule(runnable, 5, TimeUnit.SECONDS);
        }
    }

    @GetMapping("/triggerWide")
    public String triggerWide() {
        upgradeTriggerService.scheduleWideTable();
        return "OK";
    }

    @GetMapping("/triggerFlow")
    public String triggerFlow() {
        upgradeTriggerService.schedulerRunFlow();
        return "OK";
    }

    @GetMapping("/convertType")
    public List<MetaColumnDTO> convertType() {
        TypeConvertParam param = new TypeConvertParam();
        param.setSourceType(DataSourceType.MySQL.name());
        param.setTargetType(DataSourceType.ClickHouse.name());

        MetaColumnDTO column = new MetaColumnDTO();
        column.setColumnName("A");
        column.setLength(12);
        column.setTypeName("varchar");
        MetaColumnDTO b = new MetaColumnDTO();
        b.setColumnName("B");
        b.setLength(12);
        b.setScale(2);
        b.setTypeName("decimal");
        param.setColumns(Arrays.asList(column, b));

        return syncProcessDefService.convertType(param);
    }

    @GetMapping("/upsertDs")
    public BaseDataSourceParamDto upsertDs(@RequestParam("id") Integer id, @RequestParam("schema") String schema, @RequestParam("database") String database) {
        return applicationService.upsertDs(id, schema, database);
    }

    /**
     * 按表备份
     */
    @GetMapping("backupHdfs/{table}")
    public String backupHdfs(@PathVariable("table") String table) {
        ckClusterAdapter.backupToHdfs(table);
        return "OK";
    }

    // 【企微文档】历史持续性项目需求
    //  https://doc.weixin.qq.com/sheet/e3_Ac0AtgZQAHUcbJ1OXISSvCxWQozs5?scode=AD4A7AeNAAowrv0GAJAZ0AtAbLAKc&tab=5u4c09

    /**
     * 备份所有表
     *
     * @param max 最大数量
     */
    @GetMapping("backupAllHdfs")
    public String backupAllHdfs(@RequestParam("max") Integer max) {
        ckClusterAdapter.backupToHdfs(max);
        return "OK";
    }

    @GetMapping("deleteHdfs/{table}")
    public String deleteHdfs(@PathVariable("table") String table) {
        ckClusterAdapter.deleteHdfsTable(table);
        return "OK";
    }

//    @PostMapping("/importCusApply")
//    public String importCusApply(@RequestParam("file") MultipartFile file,
//                                 HttpServletRequest request) {
//        a650ProjectImporter.parseCustomApply(file, request);
//        return "OK";
//    }

    /**
     * 按表恢复
     */
    @GetMapping("recoverHdfs/{table}")
    public String recoverHdfs(@PathVariable("table") String table) {
        ckClusterAdapter.recoverFromHdfs(table);
        return "OK";
    }

    /**
     * 按节点恢复
     */
    @GetMapping("recoverHdfsHost/{host}")
    public String recoverHdfsHost(@PathVariable("host") String host) {
        ckClusterAdapter.recoverFromHdfsForHost(host);
        return "OK";
    }

    @GetMapping("/offline")
    public AjaxResult<Void> offline() {
        return integrateSyncTaskService.offlineExpireFlow();
    }

    @GetMapping("markDeleteAssets/{month}")
    public String markDeleteAssets(@PathVariable("month") Integer month) {
        assetsUpgradeTriggerService.markDeleteAssets(month);
        return "ok";
    }

    @GetMapping("deleteCkTable")
    public String deleteCkTable() {
        assetsUpgradeTriggerService.deleteCkTable();
        return "ok";
    }

    @GetMapping("deleteSnapTable/{table}")
    public String deleteSnapTable(@PathVariable("table") String table) {
        ckClusterAdapter.deleteTable(table);
        return "ok";
    }

    @GetMapping("convertToSnapshot/{max}")
    public String convertToSnapshot(@PathVariable("max") Integer max) {
        assetsUpgradeTriggerService.convertToSnapshotTable(max, null);
        return "OK";
    }

    @GetMapping("pullSaveAs/{id}")
    public String pullSaveAs(@PathVariable("id") Long userId) {
        dirService.pullSaveAs(userId);
        return "OK";
    }

    @GetMapping("/ftpCon")
    public String setFtpCon(@RequestParam(value = "con", required = false) Integer con) {
        if (Objects.isNull(con)) {
            return "Null";
        }
        return userDataAssetsUploadFtpHelper.setFtpConcurrency(con);
    }

    @GetMapping("/ftpRetry")
    public String ftpRetry(@RequestParam(value = "assetsId", required = false) Long assetsId) {
        if (Objects.isNull(assetsId)) {
            return userDataAssetsUploadFtpHelper.retryAllWaitFtp();
        }
        return userDataAssetsUploadFtpHelper.retryFtp(assetsId);
    }

    @GetMapping("/ftpRetryForce/{id}")
    public String ftpRetryForce(@PathVariable("id") Long assetsId) {
        if (Objects.isNull(assetsId)) {
            return "No assetsId";
        }
        return userDataAssetsUploadFtpHelper.retryFtpForce(assetsId);
    }

    @GetMapping("/userAssets/init")
    public String initializeUserDataAssetsFtp(@RequestParam(value = "max", required = false) Integer max, @RequestParam(value = "con", required = false) Integer con, @RequestParam(value = "assetsId", required = false) Long assetsId) {
        log.info("开始上传数据资产到ftp>>>>>>>>>>>>");
        max = Optional.ofNullable(max).orElse(10);
        con = Optional.ofNullable(con).orElse(3);
        // 1. 查询出所有 ftp_status 为空的数据资产
        List<UserDataAssets> list = userDataAssetsDAO.lambdaQuery().and(v -> v.isNull(assetsId == null, UserDataAssets::getFtpStatus).or().eq(UserDataAssets::getFtpStatus, FtpStatus.WAIT)).eq(assetsId != null, UserDataAssets::getId, assetsId).gt(UserDataAssets::getDataExpire, LocalDateTime.now()).last(" limit " + max).orderByDesc(UserDataAssets::getId).list();

        if (CollectionUtils.isEmpty(list)) {
            log.warn("NO Assets");
            return "EMPTY";
        }
        Semaphore semaphore = new Semaphore(con, true);

        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(con);
        pool.setMaxPoolSize(con);
        pool.setKeepAliveSeconds(60);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.setTaskDecorator(new ContextCopyingTaskDecorator());
        pool.setThreadFactory(new BasicThreadFactory.Builder().namingPattern("ftp-%d").build());
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        pool.setQueueCapacity(20);
        pool.initialize();

        postMsgPool.execute(() -> {
            try {
                CountDownLatch countDownLatch = new CountDownLatch(list.size());
                for (UserDataAssets userDataAssets : list) {
                    pool.execute(() -> {
                        try {
                            semaphore.acquire();
                            log.info("上传数据资产assetsId: {}", userDataAssets.getId());
                            userDataAssetsUploadFtpHelper.uploadFtpForAssets(userDataAssets);
                            log.info("上传完成assetsId: {}", userDataAssets.getId());
                        } catch (Exception e) {
                            log.error("", e);
                        } finally {
                            semaphore.release();
                            countDownLatch.countDown();
                        }
                    });
                }

                countDownLatch.await();
            } catch (Exception e) {
                log.error("", e);
            } finally {
                pool.shutdown();
                log.warn("SHUTDOWN POOL");
            }
        });

        return list.size() + "";
    }

    @GetMapping("/userAssetsSnapshot/init")
    public String initializeUserDataAssetsSnapshotFtp(@RequestParam(value = "max", required = false) Integer max, @RequestParam(value = "con", required = false) Integer con, @RequestParam(value = "assetsId", required = false) Long assetsId) {
        log.info("开始上传数据资产【快照】到ftp>>>>>>>>>>>>");
        max = Optional.ofNullable(max).orElse(10);
        con = Optional.ofNullable(con).orElse(3);
        // 1. 查询出所有ftp_path为空的数据资产
        List<UserDataAssetsSnapshot> list = userDataAssetsSnapshotDAO.lambdaQuery().gt(UserDataAssets::getDataExpire, LocalDateTime.now()).isNull(UserDataAssetsSnapshot::getFtpStatus).eq(assetsId != null, UserDataAssetsSnapshot::getAssetsId, assetsId).last(" limit " + max).orderByDesc(UserDataAssetsSnapshot::getId).list();
        if (CollectionUtils.isEmpty(list)) {
            return "EMPTY";
        }
        Semaphore semaphore = new Semaphore(con, true);

        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(con);
        pool.setMaxPoolSize(con);
        pool.setKeepAliveSeconds(60);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.setTaskDecorator(new ContextCopyingTaskDecorator());
        pool.setThreadFactory(new BasicThreadFactory.Builder().namingPattern("ftp-%d").build());
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        pool.setQueueCapacity(20);
        pool.initialize();

        postMsgPool.execute(() -> {
            try {
                CountDownLatch countDownLatch = new CountDownLatch(list.size());
                for (UserDataAssetsSnapshot userDataAssets : list) {
                    pool.execute(() -> {
                        try {
                            semaphore.acquire();
                            log.info("上传数据资产【快照】, id: {}, assetsId: {}, version: {}", userDataAssets.getId(), userDataAssets.getAssetsId(), userDataAssets.getVersion());
                            userDataAssetsUploadFtpHelper.uploadFtpForSnapshot(userDataAssets);
                            log.info("上传完成【快照】, id: {}, assetsId: {}, version: {}", userDataAssets.getId(), userDataAssets.getAssetsId(), userDataAssets.getVersion());
                        } catch (Exception e) {
                            log.error("", e);
                        } finally {
                            semaphore.release();
                            countDownLatch.countDown();
                        }
                    });
                }

                countDownLatch.await();
            } catch (Exception e) {
                log.error("", e);
            } finally {
                pool.shutdown();
                log.warn("SHUTDOWN POOL");
            }
        });
        return list.size() + "";
    }

    @GetMapping("/fillTempSelect")
    public String fillTempSelect() {
        List<TgTemplateInfo> infos = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda().select(TgTemplateInfo::getId, TgTemplateInfo::getCustomMetricsJson));
        for (TgTemplateInfo info : infos) {
            JsonBeanConverter.convert2Obj(info);
            List<CustomMetricsLabelDto> labels = info.getCustomMetrics();
            for (CustomMetricsLabelDto label : labels) {
                label.setSelect(true);
            }
            JsonBeanConverter.convert2Json(info);
            templateInfoMapper.update(null, new UpdateWrapper<TgTemplateInfo>().lambda().set(TgTemplateInfo::getCustomMetricsJson, info.getCustomMetricsJson()).eq(TgTemplateInfo::getId, info.getId()));
        }
        return "OK";
    }

    /**
     * 新版本兼容历史数据【历史数据开启了粒度选择，所有自定列模块，粒度字段设置均需默认开启】
     *
     * @return 是否成功
     */
    @GetMapping("/fixTemplateInfo")
    public String fixTemplateInfo() {
        List<TgTemplateInfo> infos = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda().select(TgTemplateInfo::getId, TgTemplateInfo::getGranularityJson).eq(TgTemplateInfo::getCustomGranularity, true).in(TgTemplateInfo::getTemplateType, Arrays.asList("normal", "customized")));
        for (TgTemplateInfo info : infos) {
            JsonBeanConverter.convert2Obj(info);
            List<TemplateGranularityDto> labels = info.getGranularity();
            for (TemplateGranularityDto label : labels) {
                if (CollectionUtils.isNotEmpty(label.getDetails())) {
                    for (TemplateGranularityDetailDto detail : label.getDetails()) {
                        detail.setCanChoose(true);
                    }
                }
            }
            JsonBeanConverter.convert2Json(info);
            templateInfoMapper.update(null, new UpdateWrapper<TgTemplateInfo>().lambda().set(TgTemplateInfo::getGranularityJson, info.getGranularityJson()).eq(TgTemplateInfo::getId, info.getId()));
        }
        return "OK";
    }

    /**
     * 修复模板业务扩展字段数据
     *
     * @return 是否成功
     */
    @GetMapping("/fixApplicationCustomExt")
    public String fixTemplateCustomExt() {
        List<TgTemplateInfo> infos = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                .select(TgTemplateInfo::getId, TgTemplateInfo::getCustomExt)
                .in(TgTemplateInfo::getTemplateType, Arrays.asList("normal", "customized")));
        for (TgTemplateInfo info : infos) {
            if (StringUtils.isNotBlank(info.getCustomExt())) {
                String customExt = info.getCustomExt();
                JSONArray params = JSON.parseArray(customExt);
                if (params.size() > 0) {
                    for (Object param : params) {
                        JSONObject obj = (JSONObject) param;
                        if (!obj.containsKey("groupSetting")) {
                            obj.put("groupSetting", 1);
                        }
                        if (!obj.containsKey("groupCustom")) {
                            obj.put("groupCustom", false);
                        }
                    }
                    info.setCustomExt(params.toJSONString());
                    templateInfoMapper.update(null, new UpdateWrapper<TgTemplateInfo>().lambda()
                            .set(TgTemplateInfo::getCustomExt, info.getCustomExt()).eq(TgTemplateInfo::getId, info.getId()));
                }
            }
        }
        return "OK";
    }

    /**
     * 新版本兼容历史数据[宽表模板数据，打包配置变为允许有多个]
     *
     * @return 是否成功
     */
    @GetMapping("/fixWideTableTemplateInfo")
    public String fixWideTableTemplateInfo() {
        List<TgTemplateInfo> infos = templateInfoMapper.selectList(new QueryWrapper<TgTemplateInfo>()
                .lambda().select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName, TgTemplateInfo::getPackTail,
                        TgTemplateInfo::getTailFilterJson, TgTemplateInfo::getTailFieldsJson)
                .eq(TgTemplateInfo::getPackTail, true).eq(TgTemplateInfo::getTemplateType, "wide_table"));
        for (TgTemplateInfo info : infos) {
            JsonBeanConverter.convert2Obj(info);

            String packTailName = null;
            if (info.getTemplateName().equals("CMH_城市_单品(常规长尾)")) {
                packTailName = "打包到分类四";
            } else if (info.getTemplateName().equals("CMH_城市_单品(特殊长尾_打包到通用名)")) {
                packTailName = "打包到通用名";
            } else {
                packTailName = "常规长尾";
            }

            TgTemplatePackTailSetting setting = new TgTemplatePackTailSetting();
            setting.setTemplateId(info.getId());
            setting.setName(packTailName);
            setting.setDescription(packTailName);
            setting.setTailFilter(info.getTailFilter());
            setting.setTailFilterJson(info.getTailFilterJson());
            setting.setTailFields(info.getTailFields());
            setting.setTailFieldsJson(info.getTailFieldsJson());

//            JsonBeanConverter.convert2Json(info);
//            templateInfoMapper.update(null, new UpdateWrapper<TgTemplateInfo>().lambda().set(TgTemplateInfo::getTailSettingsJson, info.getTailSettingsJson()).eq(TgTemplateInfo::getId, info.getId()));
            tgTemplatePackTailSettingMapper.insert(setting);
            // TODO 这里要需要把关联的申请单初始化上
            List<TgApplicationInfo> applications = applicationDAO.lambdaQuery()
                    .select(TgApplicationInfo::getId)
                    .eq(TgApplicationInfo::getTemplateId, info.getId()).list();
            if (CollectionUtils.isNotEmpty(applications)) {
                for (TgApplicationInfo application : applications) {
                    applicationDAO.update(null, new UpdateWrapper<TgApplicationInfo>().lambda()
                            .set(TgApplicationInfo::getPackTailSwitch, true)
                            .set(TgApplicationInfo::getPackTailName, packTailName)
                            .set(TgApplicationInfo::getPackTailId, setting.getId())
                            .eq(TgApplicationInfo::getId, application.getId()));
                }
            }
        }
        return "OK";
    }

    @GetMapping("/unread")
    public String unreadMsg(@RequestParam(value = "userId", required = false) Long userId, HttpServletRequest request) {
        Long id = Optional.ofNullable(userId).orElse(1L);
        mockUserService.fillUserAuthById(request, userId);
        msgService.pushUnReadMsg(id);
        msgService.pushAnnouncementMsg(2L);
        msgService.pushAssetsMsg(2L);
        return "OK";
    }

    @GetMapping("/rrt")
    public String rt(HttpServletResponse response) throws InterruptedException {
        response.addHeader("Server", "OKKKK");

        scheduler.scheduleAtFixedRate(() -> {
            for (int i = 0; i < 40; i++) {
                redisTemplate.opsForValue().set("test", "time: " + System.currentTimeMillis());
            }
        }, 1, 4, TimeUnit.SECONDS);
        TimeUnit.MILLISECONDS.sleep(150);
        return "OK";
    }

//    // 注意 先导入成功所有 项目后，才处理需求，避免数据残缺不对应问题
//    @PostMapping("/importProject")
//    public String import650Project(@RequestParam("file") MultipartFile file) {
//        a650ProjectImporter.parseProject(file);
//        return "OK";
//    }
//
//    @PostMapping("/importFlowApply")
//    public String importFlowApply(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
//        a650ProjectImporter.parseFlowApply(file, request);
//        return "OK";
//    }
//
//    @PostMapping("/importRangeApply")
//    public String importRangeApply(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
//        a650ProjectImporter.parseRangeApply(file, request);
//        return "OK";
//    }
//
//    @PostMapping("/importInComApply")
//    public String importInComApply(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
//        a650ProjectImporter.parseInCompleteCustomApply(file, request);
//        return "OK";
//    }
//
//    @PostMapping("/importComFlowApply")
//    public String importComFlowApply(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
//        a650ProjectImporter.parseCustomFlowApply(file, request);
//        return "OK";
//    }

    // gray
    @GetMapping("/ckTimeout")
    public String ckTimeOut() {
        Long count = ckClusterAdapter.mixCount("tg_assets_wd_QwRz_2498_20240318125449_snap", " select distinct t_1_pm_all from tg_assets_wd_QwRz_2498_20240318125449_snap ");
        return count + "";
    }

    @GetMapping("/countAS/{id}")
    public String getAssetsCount(@PathVariable("id") Long id) {
        UserDataAssets domain = userDataAssetsDAO.getById(id);
        userDataAssetsService.asyncUpdateCount(domain);
        return "OK";
    }

    @GetMapping("/initAssetsProdCode")
    public String initAssetsProdCode(@RequestParam(value = "id", required = false) Long assetsId) {
        List<UserDataAssets> allEmptyData = userDataAssetsDAO.lambdaQuery().isNull(UserDataAssets::getProdCode).isNull(UserDataAssets::getCopyFromId).eq(Objects.nonNull(assetsId), UserDataAssets::getId, assetsId).ge(UserDataAssets::getDataExpire, LocalDateTime.now()).list();
        log.info("size={}", allEmptyData.size());
        allEmptyData.forEach(v -> userDataAssetsService.asyncUpdateCount(v));
        return "OK";
    }

    @GetMapping("/alertEx")
    public String alertEx() {
        log.error("xxxxxxxxxxx");
        throw new RuntimeException("mvc");
    }

    @GetMapping("/alertEx2")
    public String alertEx2() {
        postMsgPool.execute(() -> {
            throw new RuntimeException("pool");
        });
        return "OK";
    }

    @GetMapping("/alertEx3")
    public String alertEx3() {
        scheduler.execute(() -> {
            throw new RuntimeException("scheduler");
        });
        return "OK";
    }

    @GetMapping("/alertEx4")
    public String alertEx4() {
        ttl.execute(() -> {
            throw new RuntimeException("ttl");
        });
        return "OK";
    }

    @GetMapping("/nacosEx")
    public String getNacosEx() throws NacosException {
        throw new NacosException(1, "TimeOut");
    }

    @GetMapping("/scheCtx")
    public String scheduleCtx() {
        scheduler.execute(() -> {
            log.info("execute");
        });

        scheduler.schedule(() -> {
            log.info("schedule");
        }, 1, TimeUnit.SECONDS);


        scheduler.schedule(() -> {
            log.info("schedule call");
            return "OK";
        }, 1, TimeUnit.SECONDS);

        return "OK";
    }

    @GetMapping("/EnvKey")
    public String getEnvKey() {
        log.info(RedisKeys.FlowApply.TRANS_APPLY_MAP);
        return "OK";
    }

    @GetMapping("/createAsCompare")
    public String assetsCompare(@RequestParam("assetsId") Long assetsId, @RequestParam("oldVersion") Integer preVersion, @RequestParam("newVersion") Integer curVersion) {
        assetsCompareService.createCompare(assetsId, preVersion, curVersion, true);
        return "OK";
    }

    @GetMapping("/invokeCompare")
    public String invokeCompare(@RequestParam("compareId") String compareId) {
        List<Long> ids = Arrays.stream(compareId.split(",")).filter(StringUtils::isNoneBlank).map(Long::parseLong).collect(Collectors.toList());
        postMsgPool.execute(() -> assetsCompareInvoker.invokeCompareById(ids));
        return "OK";
    }

    @GetMapping("/invokeFileCompare")
    public String invokeFileCompare(@RequestParam("compareId") String compareId) {
        List<Long> ids = Arrays.stream(compareId.split(",")).filter(StringUtils::isNoneBlank).map(Long::parseLong).collect(Collectors.toList());
        postMsgPool.execute(() -> assetsCompareInvoker.invokeFileCompareById(ids));

        return "OK";
    }

    @GetMapping("/compareRepeat")
    public String compareRepeat() {
        Object repeatSwitch = redisTemplate.opsForValue().get(RedisKeys.Assets.COMPARE_REPEAT_RUN);
        Integer repeatInt = Optional.ofNullable(repeatSwitch).map(Object::toString).map(Integer::parseInt).orElse(0);
        if (repeatInt > 0) {
            redisTemplate.opsForValue().set(RedisKeys.Assets.COMPARE_REPEAT_RUN, 0);
            return "Close";
        } else {
            redisTemplate.opsForValue().set(RedisKeys.Assets.COMPARE_REPEAT_RUN, 1);
            return "Open";
        }
    }

    @GetMapping("/download")
    public void download(@RequestParam("ids") String ids, HttpServletResponse response) {
        assetsCompareService.download(ids, "", response);
    }

    @GetMapping("/retryPushPB")
    public AjaxResult<Void> retryPush(Long pushId) {
        return powerBiPushService.replayPush(pushId);
    }

    @GetMapping("/retryTaskConfigData")
//    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> retryTaskConfigData(@RequestParam(value = "batch", required = false) Integer batch, HttpServletRequest request) {
        mockUserService.fillUserAuthById(request, 1L);
        return applicationTaskConfigService.appendSaveApplyConfig(batch);
    }

    @GetMapping("/fixApplyError")
    @Transactional(rollbackFor = Exception.class)
    public String fixApplyError() {
        List<TgApplicationInfo> allApply = applicationDAO.lambdaQuery().select(TgApplicationInfo::getId, TgApplicationInfo::getGranularityJson).eq(TgApplicationInfo::getApplicationType, ApplicationConst.ApplicationType.DATA_APPLICATION).list();
        for (TgApplicationInfo apply : allApply) {
            JsonBeanConverter.convert2Obj(apply);
            boolean mod = false;
            for (ApplicationGranularityDto dto : apply.getGranularity()) {
                if (FieldGranularityEnum.time.name().equals(dto.getGranularity())) {
                    List<SelectFieldDto> fields = dto.getFields();
                    if (CollectionUtils.isEmpty(fields)) {
                        break;
                    }
                    for (SelectFieldDto field : fields) {
                        if (Objects.equals(field.getFieldId(), ApplicationConst.PeriodField.PERIOD_NEW_ID)) {
                            mod = true;
                            field.setFieldId(ApplicationConst.PeriodField.PERIOD_TYPE_ID);
                        }
                    }
                }
            }
            if (mod) {
                JsonBeanConverter.convert2Json(apply);
                applicationDAO.lambdaUpdate().eq(TgApplicationInfo::getId, apply.getId()).set(TgApplicationInfo::getGranularityJson, apply.getGranularityJson()).update();
            }
        }

        return "OK";
    }

    @GetMapping("/fixTransApplyError")
    @Transactional(rollbackFor = Exception.class)
    public String fixTransApplyError() {
        // select id, project_name, asql , tail_sql, base_table_id, granularity_json, create_time
        // from tg_application_info where project_name like '%JD%' and new_application_id is null and base_table_id = 10054;

        // 分开处理 new_application_id 为空和不为空的数据
//        List<Integer> idList = Arrays.asList(1528, 2873, 2875, 2877, 2879, 2881, 2883, 2885, 2887, 2889, 2899, 2901,
//                2918, 2931, 2933, 2945, 2947, 2956, 2961, 2963, 2965, 2970, 2977, 2979, 2981, 2983, 2985, 2987, 2989,
//                2991, 3006, 3008, 3010, 3014, 3016, 3018, 3020, 3022, 3024, 3029, 3031, 3033, 3035, 3037, 3039, 3041,
//                3043, 3045, 3047, 3049, 3053, 3055, 3057, 3059, 3061, 3065, 3072, 3075, 3077, 3079, 3081, 3083, 3085,
//                3087, 3089, 3091, 3101, 3103, 3120, 3132, 3134, 3146, 3148, 3156, 3159, 3166, 3168, 3170, 3172, 3174,
//                3176, 3178, 3180, 3182, 3184, 3188, 3190, 3192, 3194, 3199, 3201, 3203, 3205, 3207, 3209, 3211, 3215,
//                3217, 3219, 3221, 3223, 3227, 3232, 3390, 3392, 3394, 3396, 3398, 3400, 3402, 3404, 3406, 3416, 3418,
//                3435, 3448, 3450, 3462, 3464, 3472, 3477, 3479, 3481, 3486, 3493, 3495, 3497, 3499, 3501, 3503, 3505,
//                3507, 3522, 3524, 3526, 3530, 3532, 3534, 3536, 3538, 3540, 3545, 3547, 3549, 3551, 3553, 3555, 3557,
//                3559, 3561, 3563, 3565, 3569, 3571, 3573, 3575, 3577, 3581, 3588, 3590, 3592, 3594, 3596, 3598, 3600,
//                3602, 3604, 3606, 3616, 3618, 3635, 3647, 3649, 3661, 3663, 3671, 3674, 3681, 3683, 3685, 3687, 3689,
//                3691, 3693, 3695, 3697, 3699, 3703, 3705, 3707, 3709, 3714, 3716, 3718, 3720, 3722, 3724, 3726, 3730,
//                3732, 3734, 3736, 3738, 3742, 3747, 3877, 3956, 3970, 3980, 3982, 3993, 4124, 4140, 4142, 4151, 4532);

        // 实际生效的申请，重点要处理
        List<Integer> idList = Arrays.asList(1528, 1538, 1661, 1720, 2795, 2796, 2797, 2873, 2875, 2877, 2879, 2881, 2883, 2885, 2887, 2889, 2899, 2901, 2918, 2931, 2933, 2945, 2947, 2956, 2961, 2963, 2965, 2970, 2977, 2979, 2981, 2983, 2985, 2987, 2989, 2991, 3006, 3008, 3010, 3014, 3016, 3018, 3020, 3022, 3024, 3029, 3031, 3033, 3035, 3037, 3039, 3041, 3043, 3045, 3047, 3049, 3053, 3055, 3057, 3059, 3061, 3065, 3072, 3075, 3077, 3079, 3081, 3083, 3085, 3087, 3089, 3091, 3101, 3103, 3120, 3132, 3134, 3146, 3148, 3156, 3159, 3166, 3168, 3170, 3172, 3174, 3176, 3178, 3180, 3182, 3184, 3188, 3190, 3192, 3194, 3199, 3201, 3203, 3205, 3207, 3209, 3211, 3215, 3217, 3219, 3221, 3223, 3227, 3232, 3390, 3392, 3394, 3396, 3398, 3400, 3402, 3404, 3406, 3416, 3418, 3435, 3448, 3450, 3462, 3464, 3472, 3477, 3479, 3481, 3486, 3493, 3495, 3497, 3499, 3501, 3503, 3505, 3507, 3522, 3524, 3526, 3530, 3532, 3534, 3536, 3538, 3540, 3545, 3547, 3549, 3551, 3553, 3555, 3557, 3559, 3561, 3563, 3565, 3569, 3571, 3573, 3575, 3577, 3581, 3588, 3590, 3592, 3594, 3596, 3598, 3600, 3602, 3604, 3606, 3616, 3618, 3635, 3647, 3649, 3661, 3663, 3671, 3674, 3681, 3683, 3685, 3687, 3689, 3691, 3693, 3695, 3697, 3699, 3703, 3705, 3707, 3709, 3714, 3716, 3718, 3720, 3722, 3724, 3726, 3730, 3732, 3734, 3736, 3738, 3742, 3747, 3794, 3796, 3798, 3800, 3802, 3804, 3807, 3809, 3811, 3821, 3823, 3840, 3853, 3855, 3867, 3869, 3877, 3882, 3884, 3886, 3891, 3898, 3900, 3902, 3904, 3906, 3908, 3910, 3912, 3927, 3929, 3931, 3935, 3937, 3939, 3941, 3943, 3945, 3950, 3952, 3954, 3956, 3958, 3960, 3962, 3964, 3966, 3968, 3970, 3974, 3976, 3978, 3980, 3982, 3986, 3993, 3994, 3996, 3998, 4000, 4002, 4004, 4006, 4008, 4010, 4020, 4022, 4039, 4051, 4053, 4065, 4067, 4075, 4078, 4085, 4087, 4089, 4091, 4093, 4095, 4097, 4099, 4101, 4103, 4107, 4109, 4111, 4113, 4118, 4120, 4122, 4124, 4126, 4128, 4130, 4134, 4136, 4138, 4140, 4142, 4146, 4151, 4484, 4486, 4531, 4532, 4575, 4601, 4787, 4789, 5022, 5027, 5104, 5105, 5106, 5107, 5189, 5919, 5926);
//        List<Integer> idList = Arrays.asList(1538, 1661, 1720, 2795, 2796, 2797, 3794, 3796, 3798, 3800, 3802, 3804,
//                3807, 3809, 3811, 3821, 3823, 3840, 3853, 3855, 3867, 3869, 3882, 3884, 3886, 3891, 3898, 3900, 3902,
//                3904, 3906, 3908, 3910, 3912, 3927, 3929, 3931, 3935, 3937, 3939, 3941, 3943, 3945, 3950, 3952, 3954,
//                3958, 3960, 3962, 3964, 3966, 3968, 3974, 3976, 3978, 3986, 3994, 3996, 3998, 4000, 4002, 4004, 4006,
//                4008, 4010, 4020, 4022, 4039, 4051, 4053, 4065, 4067, 4075, 4078, 4085, 4087, 4089, 4091, 4093, 4095,
//                4097, 4099, 4101, 4103, 4107, 4109, 4111, 4113, 4118, 4120, 4122, 4126, 4128, 4130, 4134, 4136, 4138,
//                4146, 4484, 4486, 4531, 4575, 4601, 4787, 4789, 5022, 5027, 5104, 5105, 5106, 5107, 5189, 5919, 5926);
        List<Long> ids = idList.stream().map(Integer::longValue).collect(Collectors.toList());
        List<TgApplicationInfo> allApply = applicationDAO.listByIds(ids);
        for (TgApplicationInfo apply : allApply) {
            JsonBeanConverter.convert2Obj(apply);
            List<ApplicationGranularityDto> dtoList = apply.getGranularity();
            if (apply.getGranularity().stream().noneMatch(v -> Objects.equals(v.getGranularity(), FieldGranularityEnum.time.name()))) {
                continue;
            }

            dtoList.stream().filter(v -> Objects.equals(v.getGranularity(), FieldGranularityEnum.time.name())).findAny().ifPresent(v -> {
                FilterDTO filter = v.getFilter();
                List<FilterDTO> pair = filter.getFilters().get(0).getFilters();
                Long applyId = apply.getId();
                String projectName = apply.getProjectName();
                if (pair.size() <= 1) {
                    log.warn("no time config {} {}", applyId, projectName);
                    return;
                }
                FilterDTO end = pair.get(1);
                FilterDTO.FilterItemDTO item = end.getFilterItem();
                if (Objects.equals(item.getValue(), item.getTimeViewName())) {
                    log.warn("correct time {} {}", applyId, projectName);
                    return;
                }

                log.info("id={} pro={} val={} name={}", applyId, projectName, item.getValue(), item.getTimeViewName());
                item.setValue(item.getTimeViewName());

                JsonBeanConverter.convert2Json(apply);

                AjaxResult<TgApplicationInfo> applyResult = this.extractSql(apply);
//                if (StringUtils.isNotBlank(transferProperties.getBaseTableReplace())) {
//                    Map<String, String> kv = transferProperties.parseTableReplace();
//                    for (Map.Entry<String, String> en : kv.entrySet()) {
//                        if (StringUtils.isNotBlank(apply.getTailSql())) {
//                            apply.setTailSql(apply.getTailSql().replace(en.getKey(), en.getValue()));
//                        }
//                        apply.setAsql(apply.getAsql().replace(en.getKey(), en.getValue()));
//                    }
//                }

                if (!applyResult.isSuccess()) {
                    log.warn("{}: {}", applyId, applyResult);
                    return;
                }

                JsonBeanConverter.convert2Json(apply);

                applicationDAO.lambdaUpdate()
                        .set(TgApplicationInfo::getGranularityJson, apply.getGranularityJson())
                        .set(TgApplicationInfo::getAsql, apply.getAsql())
                        .set(TgApplicationInfo::getTailSql, apply.getTailSql())
                        .eq(TgApplicationInfo::getId, applyId)
                        .update();
            });
        }

        return "OK";
    }

    public AjaxResult<TgApplicationInfo> extractSql(TgApplicationInfo applicationInfo) {
        boolean fillResult = new WideTableSqlBuilder().fillApplication(applicationInfo);
        if (!fillResult) {
            return AjaxResult.error("申请失败，请反馈技术人员处理");
        }
        JsonBeanConverter.convert2Obj(applicationInfo);
        return AjaxResult.success(applicationInfo);
    }

    @GetMapping("/testConn")
    public String testConn() {
        List<UserDataAssets> assets = userDataAssetsDAO.lambdaQuery().select(UserDataAssets::getAssetTableName).gt(UserDataAssets::getDataExpire, LocalDateTime.now()).isNull(UserDataAssets::getCopyFromId).list();
        for (UserDataAssets asset : assets) {
            String assetTableName = asset.getAssetTableName();
//            log.info("table={}", assetTableName);
            ttl.execute(() -> ckClusterAdapter.execute(assetTableName, "select * from " + assetTableName + " limit 1000"));
        }

        return "OK";
    }

    @GetMapping("/testPoolDiscard")
    public String testPoolDiscard() {
        for (int i = 0; i < 1000; i++) {

            MDC.put(LogConstant.TRACE_ID, StrUtil.randomAlpha(6));
            postMsgPool.execute(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    log.error("", e);
                }
            });
        }
        return "OK";
    }

    @GetMapping("/finishWide/{batchId}")
    public String finishWide(@PathVariable("batchId") Long batchId) {
        qcService.finishWideUpgrade(batchId);
        return "OK";
    }

    @GetMapping("/qc/all")
    public AjaxResult<Void> qcAll() {
        return qcService.createAllQc(null);
    }

    @PostMapping("/qc/page")
    public AjaxResult<IPage<AssetsQcPageDTO>> pageQuery(@RequestBody AssetsQcPageRequest request) {
        return qcService.pageQuery(request);
    }

    // 补创建
    @GetMapping("/fillCompare/{id}")
    public String fillCompare(@PathVariable("id") Long batchId) {
        List<AssetsWideUpgradeTrigger> triggers = assetsWideUpgradeTriggerDAO.lambdaQuery().eq(AssetsWideUpgradeTrigger::getQcBatchId, batchId).list();
        List<Long> assetsIds = Lambda.buildList(triggers, AssetsWideUpgradeTrigger::getAssetsId);
        Map<Long, Integer> versionMap = userDataAssetsDAO.queryVersion(assetsIds);

        for (AssetsWideUpgradeTrigger trigger : triggers) {
            assetsUpgradeTriggerService.createAssetsCompare(trigger, versionMap.get(trigger.getAssetsId()));
        }

        return "OK";
    }

    @GetMapping("/refixCompare")
    public String refixCompare() {
        List<AssetsWideUpgradeTrigger> triggers = assetsWideUpgradeTriggerDAO.lambdaQuery()
                .eq(AssetsWideUpgradeTrigger::getTableId, 10054L)
                .eq(AssetsWideUpgradeTrigger::getActVersion, 45)
                .eq(AssetsWideUpgradeTrigger::getPreVersion, 39)
                .list();
        List<Long> assetsIds = Lambda.buildList(triggers, AssetsWideUpgradeTrigger::getAssetsId);
        Map<Long, Integer> versionMap = userDataAssetsDAO.queryVersion(assetsIds);
        for (AssetsWideUpgradeTrigger trigger : triggers) {
            assetsUpgradeTriggerService.createAssetsCompare(trigger, versionMap.get(trigger.getAssetsId()));
        }

        return "OK";
    }

    @GetMapping("/findMinAudit")
    public String findMinAudit() {
        List<TgApplicationInfo> all = applicationDAO.lambdaQuery().eq(TgApplicationInfo::getNewAssetId, 29L).ne(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.DRAFT).notLike(TgApplicationInfo::getProjectName, "测试").notLike(TgApplicationInfo::getProjectName, "test").list();

        List<TgApplicationInfo> handleFirstNodeApplicationList = all.stream().filter(a -> {
            JsonBeanConverter.convert2Obj(a);
            return CollectionUtils.isNotEmpty(a.getHandleNode()) && a.getHandleNode().get(0).getHandleStatus().equals(CommonConstants.HANDLED);
        }).collect(Collectors.toList());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 最短申请审批时长
        LongSummaryStatistics summary = handleFirstNodeApplicationList.stream().map(a -> {
            List<ProcessNodeEasyDto> handleNodes = a.getHandleNode();
            if (CollectionUtils.isEmpty(handleNodes)) {
                return null;
            }
            ProcessNodeEasyDto processNodeEasyDto = handleNodes.get(0);
            String handleTime = processNodeEasyDto.getHandleTime();
            String createTime = a.getCreateTime();
            // 解析字符串为 LocalDateTime
            LocalDateTime startTime = LocalDateTime.parse(createTime, formatter);
            LocalDateTime endTime = LocalDateTime.parse(handleTime, formatter);
            // 计算时长
            Duration res = Duration.between(startTime, endTime);
            log.info("id={} name={} {}s", a.getId(), a.getProjectName(), res.getSeconds());
            return res;
        }).filter(Objects::nonNull).filter(v -> !v.isNegative()).mapToLong(Duration::getSeconds).summaryStatistics();


        double minDurationHours = summary.getMin() / 3600.0;


        return summary.toString();
    }

    @GetMapping("/assetsUsage")
    public AjaxResult<?> assetsUsage() {
        AssetIndicatorQuery query = new AssetIndicatorQuery();
        query.setAssetId(29L);
        return assetService.assetIndicatorQuery(query);
    }

    @GetMapping("/findQcDetail")
    public String findQcDetail() {
//        List<AssetsUpgradeTrigger> list = assetsUpgradeTriggerDAO.queryNeedTrigger(10000);
//        Set<Long> applyIds = Lambda.buildSet(list, AssetsUpgradeTrigger::getApplicationId);

        List<Integer> ali = Arrays.asList(4204, 4276, 4304, 4309, 4210, 4252, 4342, 4277, 4341, 4294, 4233, 4257, 4245, 4315, 4266, 4271, 4244, 4293, 4221, 4270, 4334, 4208, 4256, 4320, 4238, 4231, 4326, 4287, 4318, 4249, 4218, 4273, 4343, 4237, 4329, 4344, 4299, 4265, 4216, 4310, 4295, 4269, 4217, 4242, 4248, 4282, 4246, 4301, 4224, 4338, 4296, 4321, 4313, 4345, 4284, 4307, 4336, 4337, 4211, 4268, 4241, 4225, 4330, 4263, 4264, 4222, 4306, 4286, 4300, 4250, 4324, 4348, 4283, 4232, 4206, 4228, 4323, 4227, 4327, 4280, 4328, 4288, 4335, 4272, 4243, 4339, 4226, 4325, 4292, 4347, 4332, 4235, 4254, 4285, 4234, 4236, 4303, 4316, 4312, 4251, 4291, 4308, 4215, 4260, 4262, 4317, 4253, 4302, 4213, 4207, 4274, 4239, 4229, 4219, 4212, 4278, 4322, 4223, 4214, 4275, 4230, 4319, 4247, 4259, 4290, 4298, 4279, 4311, 4346, 4261, 4297, 4255, 4258, 4281, 4220, 4289, 4205, 4209, 4267, 4340, 4314, 4305, 4525, 4351, 4598, 4613, 4615, 4617, 4624);
        List<Long> applyIds = ali.stream().map(v -> (long) v).collect(Collectors.toList());
        List<AssetsQcDetail> details = assetsQcService.buildDetailsByApply(applyIds, AssetsQcTypeEnum.brand);


        return "OK";
    }

    @GetMapping("/fixTaskConfig")
    public String fixTaskConfig(HttpServletRequest request) {
        List<TgApplicationInfo> list = applicationDAO.lambdaQuery().in(TgApplicationInfo::getId, Arrays.asList(4638L, 4593L)).list();
        mockUserService.fillUserAuthById(request, 1L);

        for (TgApplicationInfo info : list) {
            JsonBeanConverter.convert2Obj(info);

            taskConfigService.saveApplicationTaskConfig(info);
        }

        return "OK";
    }

    @GetMapping("/fixNoAudit")
    public String fixNoAudit() {
        List<TgApplicationInfo> list = applicationDAO.lambdaQuery().select(TgApplicationInfo::getId).eq(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.AUDITING).list();
        if (CollectionUtils.isEmpty(list)) {
            return "EMPTY";
        }

        for (TgApplicationInfo info : list) {
            redisTemplate.opsForSet().remove(RedisKeys.Apply.AUDIT_APPLY_SET, info.getId());
        }
        return "OK";
    }

    @GetMapping("/fixBackupIllegal")
    public String fixBackupIllegal() {
        List<SnapshotTableMapping> mapList = snapshotTableMappingDAO.lambdaQuery().eq(SnapshotTableMapping::getState, SnapshotTableStateEnum.copying.name()).list();

        List<Long> ids = Lambda.buildList(mapList, SnapshotTableMapping::getId);

        for (SnapshotTableMapping mapping : mapList) {
            String host = mapping.getCandidateHost();
            String sql = "drop table if exists %s ";
            ckClusterAdapter.executeHost(host, String.format(sql, mapping.getTableName()));
        }
        snapshotTableMappingDAO.lambdaUpdate().in(SnapshotTableMapping::getId, ids).set(SnapshotTableMapping::getState, SnapshotTableStateEnum.create.name()).set(SnapshotTableMapping::getCandidateHost, null).update();
        return "OK";
    }

    @GetMapping("/backupTable")
    public String backupTable() {
        ckClusterAdapter.scheduleToBackup();
        return "OK";
    }

    @GetMapping("/listProdCode")
    public AjaxResult<List<String>> queryAllProdCode() {
        return assetsCompareService.queryAllProdCode();
    }

    @GetMapping("/lastSelect")
    public AjaxResult<AssetsCompareLastSelectDTO> lastSelect() {
        return assetsCompareService.lastSelect(207L);
    }

    @GetMapping("/cleanTmpDir")
    public String cleanTmpDir() {
        fileAdapter.cleanTempFile();
        return "OK";
    }

    @GetMapping("/cleanAttachFile")
    public String cleanAttachFile() {
        return fileAdapter.cleanAttachFile();
    }

    @GetMapping("/opt")
    public String opt() {
        Object val = redisTemplate.opsForValue().get(RedisKeys.Apply.CLEAN_TEMP_USELESS_KEY);
        log.info("val={}", val);
        if (Objects.nonNull(val) && Objects.equals(val, 1)) {
            log.info("open");
        }
        return "OK";
    }

    @PostMapping("/exeSQLS")
    public String executeSQL(@RequestBody String sql) {
        if (StringUtils.isBlank(sql)) {
            return "EMPTY";
        }

        String[] lines = sql.split(";");
        for (String line : lines) {
            log.info("sql={}", line);
            ckClusterAdapter.execute(line);
        }
        return "OK";
    }

    @GetMapping("/fixNoneApplyId")
    @Transactional(rollbackFor = Exception.class)
    public String fixNoneApplyId() {
        List<PowerBiPushDetail> noneList = powerBiPushDetailDAO.lambdaQuery().select(PowerBiPushDetail::getId, PowerBiPushDetail::getAssetsId, PowerBiPushDetail::getAssetsVer).isNull(PowerBiPushDetail::getApplicationId).list();
        if (CollectionUtils.isEmpty(noneList)) {
            return "EMPTY";
        }

        Set<Long> assetsIds = Lambda.buildSet(noneList, PowerBiPushDetail::getAssetsId);
        List<UserDataAssets> latestAssets = userDataAssetsDAO.lambdaQuery().select(UserDataAssets::getId, UserDataAssets::getVersion, UserDataAssets::getSrcApplicationId).in(UserDataAssets::getId, assetsIds).list();
        Map<Long, UserDataAssets> latestMap = Lambda.buildMap(latestAssets, UserDataAssets::getId);


        List<UserDataAssetsSnapshot> snapAssets = userDataAssetsSnapshotDAO.lambdaQuery().select(UserDataAssetsSnapshot::getAssetsId, UserDataAssetsSnapshot::getVersion, UserDataAssetsSnapshot::getSrcApplicationId).in(UserDataAssetsSnapshot::getAssetsId, assetsIds).list();
        Map<Long, List<UserDataAssetsSnapshot>> snapListMap = snapAssets.stream().collect(Collectors.groupingBy(UserDataAssetsSnapshot::getAssetsId));


        for (PowerBiPushDetail detail : noneList) {
            Long assetsId = detail.getAssetsId();
            UserDataAssets assets = latestMap.get(assetsId);
            if (Objects.isNull(assets)) {
                throw new RuntimeException("未匹配到资产: " + assetsId);
            }
            if (Objects.equals(assets.getVersion(), detail.getAssetsVer())) {
                detail.setApplicationId(assets.getSrcApplicationId());
            } else {
                List<UserDataAssetsSnapshot> list = snapListMap.get(assetsId);
                Long applyId = list.stream().filter(v -> Objects.equals(v.getVersion(), detail.getAssetsVer())).findFirst().map(UserDataAssets::getSrcApplicationId).orElseThrow(() -> new RuntimeException("未匹配到申请单 " + detail.getAssetsId() + "-" + detail.getAssetsVer()));
                detail.setApplicationId(applyId);
            }
        }

        for (PowerBiPushDetail detail : noneList) {
            log.info("id={} applyId={}", detail.getId(), detail.getApplicationId());
            powerBiPushDetailDAO.lambdaUpdate().set(PowerBiPushDetail::getApplicationId, detail.getApplicationId()).eq(PowerBiPushDetail::getId, detail.getId()).update();
        }
        return "OK";
    }

    @GetMapping("/getSchema")
    public String getSchema() {
        try {
            log.info("schema={}", slaveDataSource.getConnection().getSchema());
        } catch (Exception e) {
            log.error("", e);
        }

        return "OK";
    }

    @GetMapping("/fixTriggerApplyId")
    public String fixTriggerApplyId() {
        List<AssetsWideUpgradeTrigger> triggers = assetsWideUpgradeTriggerDAO.lambdaQuery().select(AssetsWideUpgradeTrigger::getId, AssetsWideUpgradeTrigger::getAssetsId, AssetsWideUpgradeTrigger::getActVersion).isNull(AssetsWideUpgradeTrigger::getApplyId).isNotNull(AssetsWideUpgradeTrigger::getActVersion).list();

        String idstr = triggers.stream().map(v -> "'" + v.getAssetsId() + "#" + v.getActVersion() + "'").collect(Collectors.joining(","));

        Map<Long, AssetsWideUpgradeTrigger> assetsIds = Lambda.buildMap(triggers, AssetsWideUpgradeTrigger::getAssetsId);
        List<UserDataAssets> lastList = userDataAssetsDAO.lambdaQuery().in(UserDataAssets::getId, assetsIds.keySet()).and(v -> v.apply("concat(id,'#', base_version) in (" + idstr + ")")).list();
        Map<String, Long> latestMap = lastList.stream().collect(Collectors.toMap(v -> v.getId() + "#" + v.getBaseVersion(), UserDataAssets::getSrcApplicationId, (front, current) -> current));

        List<UserDataAssetsSnapshot> snapList = userDataAssetsSnapshotDAO.lambdaQuery().in(UserDataAssetsSnapshot::getAssetsId, assetsIds.keySet()).and(v -> v.apply("concat(assets_id,'#', base_version) in (" + idstr + ")")).list();
        Map<String, Long> snapMap = snapList.stream().collect(Collectors.toMap(v -> v.getAssetsId() + "#" + v.getBaseVersion(), UserDataAssets::getSrcApplicationId, (front, current) -> current));


        log.info("fix: {}", triggers.size());
        for (AssetsWideUpgradeTrigger t : triggers) {
//            userDataAssetsDAO.lambdaQuery()
//                    .select(UserDataAssets::getSrcApplicationId)
//                    .eq(UserDataAssets::getId, t.getAssetsId())
//                    .eq(UserDataAssets::getBaseVersion, t.getActVersion())
//                    .oneOpt()
//                    .ifPresent(v -> t.setApplyId(v.getSrcApplicationId()));
//            if (Objects.isNull(t.getApplyId())) {
//                userDataAssetsSnapshotDAO.lambdaQuery()
//                        .select(UserDataAssets::getSrcApplicationId)
//                        .eq(UserDataAssets::getAssetsId, t.getAssetsId())
//                        .eq(UserDataAssets::getBaseVersion, t.getActVersion())
//                        .oneOpt().ifPresent(v -> t.setApplyId(v.getSrcApplicationId()));
//            }
            String id = t.getAssetsId() + "#" + t.getActVersion();
            Long applyId = Optional.ofNullable(latestMap.get(id)).orElse(snapMap.get(id));
            t.setApplyId(applyId);

        }

        for (AssetsWideUpgradeTrigger t : triggers) {
            if (Objects.isNull(t.getApplyId())) {
                log.warn("Broke {} {}", t.getId(), t.getAssetsId());
            } else {
//                log.info("map {} {} ", t.getId(), t.getApplyId());
                assetsWideUpgradeTriggerDAO.lambdaUpdate().eq(AssetsWideUpgradeTrigger::getId, t.getId()).set(AssetsWideUpgradeTrigger::getApplyId, t.getApplyId()).update();
            }
        }


        return "OK";
    }

    @GetMapping("/fixCopyMainId")
    public String fixCopyMainId() {
        List<UserDataAssets> leaf = userDataAssetsDAO.lambdaQuery().select(UserDataAssets::getId, UserDataAssets::getCopyFromId).isNotNull(UserDataAssets::getCopyFromId).list();
        Map<Long, Long> upMap = new HashMap<>();
        for (UserDataAssets assets : leaf) {
            upMap.put(assets.getId(), assets.getCopyFromId());
        }

        List<Long> ups = Lambda.buildNonNullList(leaf, UserDataAssets::getCopyFromId);
        while (CollectionUtils.isNotEmpty(ups)) {
            List<UserDataAssets> next = userDataAssetsDAO.lambdaQuery().select(UserDataAssets::getId, UserDataAssets::getCopyFromId).in(UserDataAssets::getId, ups).list();
            for (UserDataAssets assets : leaf) {
                upMap.put(assets.getId(), assets.getCopyFromId());
            }
            ups = Lambda.buildNonNullList(next, UserDataAssets::getCopyFromId);
        }

        for (UserDataAssets assets : leaf) {
            Long mainId = findTop(assets.getId(), upMap);
            log.info("id={} main={}", assets.getId(), mainId);
            userDataAssetsDAO.lambdaUpdate().eq(UserDataAssets::getId, assets.getId()).set(UserDataAssets::getCopyMainId, mainId).update();
        }

        return "OK";
    }

    /**
     * 按项目迁移负责人
     */
    @GetMapping("/transSomeApply")
    public String transSomeProject() {
        String pair = "王婷婷\t山德士-孟鲁司特-自定义铺货_1097_长尾\n" + "王婷婷\t山德士-孟鲁司特-自定义铺货_1097\n" + "李定欣\t杰特贝林-人血白蛋白-全国&17省&3直辖市\n" + "李普红\tOverview-全国-TOP集团权益_1001\n" + "李普红\tOverview-大区-TOP集团权益_20-22年_1002\n" + "李普红\tOverview-大区-TOP集团权益_23年及之后_1002\n" + "张静\t晖致-行业数据-季度常规-TOP300企业_1071\n" + "张静\t晖致-行业数据-季度常规-通用名_1071\n" + "王婷婷\t1086_勃林格糖尿病overview\n" + "王婷婷\t1085_勃林格_处方药overview_全国季度\n" + "李普红\t1088_AZ_PPT报告数据\n";

        @Getter
        class Vo {
            String user;
            String name;

            public Vo(String user, String name) {
                this.user = user;
                this.name = name;
            }
        }

        List<Vo> list = new ArrayList<>();
        for (String line : pair.split("\n")) {
            String[] cols = line.split("\t");
            list.add(new Vo(cols[0], cols[1]));
        }
        Set<String> user = Lambda.buildSet(list, Vo::getUser);
        Map<String, Long> userMap = sysUserService.selectUserByRealNames(user);
        if (userMap.size() != user.size()) {
            return "invalid User";
        }

        for (Vo vo : list) {
            applicationDAO.lambdaUpdate().set(TgApplicationInfo::getApplicantId, userMap.get(vo.getUser())).set(TgApplicationInfo::getApplicantName, "一线组织/商用数据事业部-" + vo.getUser()).eq(TgApplicationInfo::getProjectName, vo.getName()).update();

            userDataAssetsDAO.lambdaUpdate().set(UserDataAssets::getApplicantId, userMap.get(vo.getUser())).set(UserDataAssets::getApplicantName, "一线组织/商用数据事业部-" + vo.getUser()).eq(UserDataAssets::getProjectName, vo.getName()).update();

            userDataAssetsSnapshotDAO.lambdaUpdate().set(UserDataAssets::getApplicantId, userMap.get(vo.getUser())).set(UserDataAssets::getApplicantName, "一线组织/商用数据事业部-" + vo.getUser()).eq(UserDataAssets::getProjectName, vo.getName()).update();
        }

        return "OK";
    }

    private Long findTop(Long id, Map<Long, Long> chain) {
        Long tmp = chain.get(id);
        if (Objects.isNull(tmp)) {
            return null;
        }
        while (true) {
            Long next = chain.get(tmp);
            if (Objects.isNull(next)) {
                return tmp;
            }
            tmp = next;
        }
    }

    /**
     * @param origin 离职人
     * @param target 继承人
     */
    @GetMapping("/transUserData")
    @Transactional(rollbackFor = Exception.class)
    public String transUserAll(Long origin, Long target) {
        SysUser oriUser = sysUserMapper.selectUserById(origin);
        SysUser tarUser = sysUserMapper.selectUserById(target);
        if (Objects.isNull(oriUser) || Objects.isNull(tarUser)) {
            return "invalid userId";
        }

        applicationDAO.lambdaUpdate().eq(TgApplicationInfo::getApplicantId, origin).set(TgApplicationInfo::getApplicantId, target).update();

        userDataAssetsDAO.lambdaUpdate().eq(UserDataAssets::getApplicantId, origin).set(UserDataAssets::getApplicantId, target).update();
        userDataAssetsSnapshotDAO.lambdaUpdate().eq(UserDataAssetsSnapshot::getApplicantId, origin).set(UserDataAssetsSnapshot::getApplicantId, target).update();

        projectDAO.lambdaUpdate().eq(Project::getProjectManager, origin).set(Project::getProjectManager, target).update();

        List<ProjectHelper> list = projectHelperMapper.selectList(new QueryWrapper<ProjectHelper>().lambda().in(ProjectHelper::getUserId, Arrays.asList(origin, target)));
        Map<Long, List<Long>> userMap = list.stream().collect(Collectors.groupingBy(ProjectHelper::getUserId, Collectors.mapping(ProjectHelper::getProjectId, Collectors.toList())));

        List<Long> tarList = userMap.get(target);
        List<Long> oriList = userMap.get(origin);
        Set<Long> repeat = oriList.stream().filter(tarList::contains).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(repeat)) {
            projectHelperMapper.delete(new QueryWrapper<ProjectHelper>().lambda().in(ProjectHelper::getProjectId, repeat).eq(ProjectHelper::getUserId, origin));
        }


        projectHelperMapper.update(null, new UpdateWrapper<ProjectHelper>().lambda().in(ProjectHelper::getProjectId, oriList).eq(ProjectHelper::getUserId, origin).set(ProjectHelper::getUserId, target));

        return "OK";
    }

    @PostMapping("/pageQcLog")
    public AjaxResult<IPage<DqcQcLogVO>> pageQcLog(@RequestBody PageRequest pageRequest) {
        return flowProcessCheckService.pageQuery(pageRequest);
    }

    @GetMapping("/checkDqc")
    public AjaxResult<CreateAutoProcessRequest> checkDqc() {
        return AjaxResult.success(flowProcessCheckService.buildReqByCheck().orElse(null));
    }

    @GetMapping("/invokeJobStr")
    public String invokeJob() {
        try {
            JobInvokeUtil.invokeTarget("assetsFlowService.autoCreateBatch(4L)");
        } catch (Exception e) {
            log.error("", e);
        }

        return "OK";
    }

    @GetMapping("/cleanTableAs/{tableId}")
    public String cleanTableAs(@PathVariable("tableId") Long tableId) {
        tableInfoSnapshotService.asyncDeleteQcVersionTable(tableId);
        return "OK";
    }

    @GetMapping("/bench")
    public String benchRun() {
        ExecutorService pool = Executors.newFixedThreadPool(6);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            pool.execute(() -> {
                boolean run = false;
                String cntKey = "tg:a:cnt";
                try {
                    redisSemaphore.acquireBlock(cntKey, 10);
                    run = true;
//                    int ms = ThreadLocalRandom.current().nextInt(10000) + 2000;
                    int ms = 1000;
                    log.info("start run {}", finalI);
                    try {
                        redisTemplate.opsForList().rightPush("tg:a:list", finalI);
                        TimeUnit.MILLISECONDS.sleep(ms);
                        redisTemplate.opsForValue().increment("tg:a:total");
                    } catch (Exception e) {
                        log.error("", e);
                    }
                } catch (Exception e) {
                    log.error("", e);
                } finally {
                    log.info("finish run {}", finalI);
                    if (run) {
                        redisTemplate.opsForList().leftPop("tg:a:list");
                        redisSemaphore.release(cntKey);
                    }

                    if (redisSemaphore.runCount(cntKey) == 0) {
                        log.info("finish ALL");

                        scheduler.schedule(pool::shutdown, 20L, TimeUnit.SECONDS);
                    }
                }
            });
        }

        return "";
    }

    @GetMapping("/copyBatchBaseApply")
    @Transactional(rollbackFor = Exception.class)
    public String copyBatchBaseApply(HttpServletRequest request) {
        List<Integer> applyIds = Arrays.asList(
//                3913,
                3914, 3915, 3917, 3918, 3919, 3921, 3922, 3923, 3925, 3987, 3988, 4293, 4294, 4296, 4297, 4298, 4300, 4301, 4302, 4303, 4305, 4344, 4345, 5300, 5302, 5303, 5304, 5305, 5306);
        for (Integer applyId : applyIds) {
            copyBaseApplyCache(applyId.longValue(), "", request);
        }
        return "OK";
    }

    @GetMapping("/auditApply/{id}")
    public String auditApply(@PathVariable("id") Long id, HttpServletRequest request) {
        mockUserService.fillUserAuthById(request, transferProperties.getAuditId());

        return applicationService.auditOneCache(id, "auto ");
    }

    /**
     * 复制 除粒度，指标 外的全部信息
     */
    @GetMapping("/copyBaseApply")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult copyBaseApply(@RequestParam Long applyId, @RequestParam(required = false) String submit, HttpServletRequest request) {

        return copyBaseApplyCache(applyId, submit, request);
    }

    public AjaxResult copyBaseApplyCache(Long applyId, String submit, HttpServletRequest request) {
        TgApplicationInfo apply = applicationDAO.getById(applyId);
        if (Objects.isNull(apply)) {
            return AjaxResult.error("申请不存在");
        }

        mockUserService.fillUserAuthById(request, apply.getApplicantId());

        Long origin = apply.getTemplateId();
        Long templateId = null;
        if (Objects.equals(origin, 100L)) {
            templateId = 165L;
        } else if (Objects.equals(origin, 154L)) {
            templateId = 172L;
        }
        if (Objects.isNull(templateId)) {
            throw new RuntimeException("不支持的模板");
        }

        TgTemplateInfo template = templateInfoDAO.getById(templateId);
        JsonBeanConverter.convert2Obj(apply);
        JsonBeanConverter.convert2Obj(template);

        List<Long> metricsIds = Lambda.buildList(template.getCustomMetrics(), CustomMetricsLabelDto::getMetricsId);
        List<MetricsDict> metrics = metricsDictDAO.getBaseMapper().selectList(new QueryWrapper<MetricsDict>().lambda().in(MetricsDict::getId, metricsIds));
        Map<Long, String> metricsMap = Lambda.buildMap(metrics, MetricsDict::getId, MetricsDict::getName);

        // 默认勾选全部指标
        List<CustomMetricsLabelDto> labels = metricsIds.stream().map(v -> {
            CustomMetricsLabelDto dto = new CustomMetricsLabelDto();
            dto.setMetricsId(v);
            dto.setAlias(metricsMap.get(v));
            dto.setSelect(true);
            return dto;
        }).collect(Collectors.toList());
        apply.setTemplateId(templateId);
        apply.setCustomMetrics(labels);
        apply.setId(null);
        apply.setAssetsId(null);
        apply.setOldApplicationId(null);
        apply.setNewApplicationId(null);
        apply.setCurrentAuditProcessStatus(1);
        apply.setProjectName(apply.getProjectName() + "-加权单价");
        apply.setProjectId(547L);

        // 指定过期时间
        LocalDateTime expire = LocalDateTime.of(LocalDate.of(2025, 3, 1), LocalTime.MAX);
        Date end = Date.from(expire.atZone(ZoneId.systemDefault()).toInstant());
        apply.setDataExpir(end);


        if (Objects.nonNull(submit)) {
            AjaxResult<TgApplicationInfo> rs = applicationService.addTemplateApplication(apply);
            if (!rs.isSuccess()) {
                log.info("rs={}", rs);
                throw new RuntimeException("复制失败");
            }

            return rs;
        } else {
            return applicationService.tryApplication(apply);
        }
    }

    @GetMapping("/autoCheck")
    public String autoCheck() {
        tgFlowProcessFacade.autoScheduled();
        return "OK";
    }

    @GetMapping("/relateAssets")
    public List<UserDataAssets> relateAssets() {
        List<UserDataAssets> list = userDataAssetsDAO.queryRelateAssets(14L, 1, false, Arrays.asList("P001", "P012"));
        return list;
    }

    @GetMapping("/autoBatchTrigger")
    public String autoBatchTrigger(Long autoId) {
        assetsFlowService.autoCreateBatch(autoId);
        return "OK";
    }

    /**
     * 初始化 出数成本 找出 需求和资产 资产减去需求时间
     */
    @GetMapping("/initCostMin")
    public String initCostMin() {
        List<UserDataAssets> assets = userDataAssetsDAO.lambdaQuery().select(UserDataAssets::getId, UserDataAssets::getCreateTime, UserDataAssets::getProjectName, UserDataAssets::getSrcApplicationId, UserDataAssets::getVersion).isNull(UserDataAssets::getCopyFromId).list();

        List<UserDataAssetsSnapshot> snaps = userDataAssetsSnapshotDAO.lambdaQuery().select(UserDataAssetsSnapshot::getId, UserDataAssetsSnapshot::getAssetsId, UserDataAssets::getProjectName, UserDataAssetsSnapshot::getVersion, UserDataAssetsSnapshot::getSrcApplicationId, UserDataAssets::getCreateTime).isNull(UserDataAssets::getCopyFromId).list();
        assets.addAll(snaps);
        assets.sort(Comparator.comparing(UserDataAssets::getVersion));

        Set<String> first = new HashSet<>();
        Map<Long, LocalDateTime> assetsTimeMap = new HashMap<>();
        for (UserDataAssets asset : assets) {
            String key = asset.getAssetsId() + "-" + asset.getSrcApplicationId();
            if (first.add(key)) {
                assetsTimeMap.put(asset.getSrcApplicationId(), asset.getCreateTime());
            }
        }

        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery().select(TgApplicationInfo::getId, TgApplicationInfo::getCreateTime, TgApplicationInfo::getProjectName).in(TgApplicationInfo::getId, assetsTimeMap.keySet()).list();
        for (TgApplicationInfo info : applyList) {
            String createTime = info.getCreateTime();
            LocalDateTime endTime = assetsTimeMap.get(info.getId());
            long endMs = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            int costMin = CostTimeUtil.calcCostMin(createTime, endMs);
            log.info("{} {} {}", info.getId(), info.getProjectName(), costMin);
            applicationDAO.lambdaUpdate().set(TgApplicationInfo::getDataCostMin, costMin).eq(TgApplicationInfo::getId, info.getId()).update();
        }


        return "OK";
    }

    /**
     * 初始化 资产类型
     */
    @Transactional
    @GetMapping("/initFlowType")
    public String initFlowType() {
        List<UserDataAssets> assets = userDataAssetsDAO.lambdaQuery().select(UserDataAssets::getId, UserDataAssets::getTemplateType, UserDataAssets::getProjectName, UserDataAssets::getFlowDetailId, UserDataAssets::getBaseTableId, UserDataAssets::getBaseVersion).list();

        Set<String> versionList = Lambda.buildSet(assets, UserDataAssets::buildTableVersion);
        List<TableInfoSnapshot> tableList = tableInfoSnapshotDAO.queryByVersion(versionList);
        Map<String, TableInfoSnapshot> tableVerMap = Lambda.buildMap(tableList, TableInfoSnapshot::buildTableVersion);

        for (UserDataAssets asset : assets) {
            Boolean scheduler = TemplateTypeEnum.of(asset.getTemplateType()).map(TemplateTypeEnum::isSchedulerTaskType).orElse(false);
            if (scheduler) {
                Optional<AssetsFlowBatch> batchOpt = Optional.ofNullable(asset.getFlowDetailId()).flatMap(assetsFlowBatchDetailDAO::queryBatchId).map(flowBatchDAO::getById);
                batchOpt.map(AssetsFlowBatch::getFlowProcessType).ifPresent(v -> {
                    log.info("id:{} name:{} type:{}", asset.getId(), asset.getProjectName(), v);
                    userDataAssetsDAO.lambdaUpdate().eq(UserDataAssets::getId, asset.getId()).set(UserDataAssets::getFlowProcessType, v).update();
                });
            } else {
                Optional<TableInfoSnapshot> snapOpt = Optional.ofNullable(tableVerMap.get(asset.buildTableVersion()));
                snapOpt.ifPresent(v -> {
                    log.info("id:{} name:{} type:{}", asset.getId(), asset.getProjectName(), v);
                    userDataAssetsDAO.lambdaUpdate().eq(UserDataAssets::getId, asset.getId()).set(UserDataAssets::getFlowProcessType, v.getFlowProcessType()).update();
                });
            }
        }

        List<UserDataAssetsSnapshot> snaps = userDataAssetsSnapshotDAO.lambdaQuery().select(UserDataAssetsSnapshot::getId, UserDataAssetsSnapshot::getAssetsId, UserDataAssets::getProjectName, UserDataAssetsSnapshot::getVersion, UserDataAssetsSnapshot::getTemplateType, UserDataAssetsSnapshot::getFlowDetailId, UserDataAssets::getBaseTableId, UserDataAssets::getBaseVersion).list();
        for (UserDataAssets asset : snaps) {
            Boolean scheduler = TemplateTypeEnum.of(asset.getTemplateType()).map(TemplateTypeEnum::isSchedulerTaskType).orElse(false);
            if (scheduler) {
                Optional<AssetsFlowBatch> batchOpt = Optional.ofNullable(asset.getFlowDetailId()).flatMap(assetsFlowBatchDetailDAO::queryBatchId).map(flowBatchDAO::getById);
                batchOpt.map(AssetsFlowBatch::getFlowProcessType).ifPresent(v -> {
                    log.info("id:{} name:{} type:{}", asset.getId(), asset.getProjectName(), v);
                    userDataAssetsDAO.lambdaUpdate().eq(UserDataAssets::getId, asset.getAssetsId()).eq(UserDataAssets::getVersion, asset.getVersion()).set(UserDataAssets::getFlowProcessType, v).update();
                });
            } else {
                Optional<TableInfoSnapshot> snapOpt = Optional.ofNullable(tableVerMap.get(asset.buildTableVersion()));
                snapOpt.map(TableInfoSnapshot::getFlowProcessType).ifPresent(v -> {
                    log.info("id:{} name:{} type:{}", asset.getId(), asset.getProjectName(), v);
                    userDataAssetsDAO.lambdaUpdate().eq(UserDataAssets::getId, asset.getAssetsId()).eq(UserDataAssets::getVersion, asset.getVersion()).set(UserDataAssets::getFlowProcessType, v).update();
                });
            }
        }

        return "OK";
    }

    @GetMapping("/fixSaveAs")
    public String fixSaveAs() {
        List<UserDataAssets> saveList = userDataAssetsDAO.lambdaQuery().isNotNull(UserDataAssets::getCopyMainId).isNotNull(UserDataAssets::getAssetTableName).list();
        List<Long> mainIds = Lambda.buildList(saveList, UserDataAssets::getCopyMainId);

        List<UserDataAssets> mainList = userDataAssetsDAO.listByIds(mainIds);
        Map<Long, UserDataAssets> mainMap = Lambda.buildMap(mainList, UserDataAssets::getId);

        int cnt = 0;
        Set<Long> mainId = new HashSet<>();
        for (UserDataAssets save : saveList) {
            String projectName = save.getProjectName();
            if (projectName.contains("test") || projectName.contains("测试")) {
                continue;
            }
            UserDataAssets main = mainMap.get(save.getCopyMainId());
            if (!save.getAssetsSql().contains(main.getAssetTableName())) {
                log.error("NOT LATEST: {} {} V{} [{}->{}] {}", save.getId(), projectName, save.getVersion(), save.getAssetTableName(), main.getAssetTableName(), save.getCreateTime());
                mainId.add(main.getId());
                cnt++;
            } else {
                log.info("MATCH");
            }
        }

        log.info("saveAsCnt={} mainId={}", cnt, mainId);
        for (Long id : mainId) {
            UserDataAssets main = mainMap.get(id);
            userDataAssetsService.handleReplaceSaveAsAssets(main.getAssetTableName(), main.getId(), main.getVersion());
        }
        return "OK";
    }

    @PostMapping("/queryHistoryQuote")
    public AjaxResult<IPage<HistoryApplyQuoteEntity>> queryHistoryQuote(@RequestBody HistoryQueryRequest request) {
        request.setUserId(207L);
        return applicationService.queryHistoryQuote(request);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortVO {
        private Long fieldId;
        private Integer sort;
    }

    @Autowired
    private ApplicationFormDAO applicationFormDAO;
    @Autowired
    private DataPlanService dataPlanService;


    @GetMapping("/initApplyForm")
    @Transactional(rollbackFor = Exception.class)
    public String initApplyForm(@RequestParam(value = "insert", required = false) Boolean insert) {
        List<ApplicationForm> exists = applicationFormDAO.list();
        Set<String> existNos = Lambda.buildSet(exists, ApplicationForm::getApplicationNo);
        List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getApplicationNo, TgApplicationInfo::getTemplateId,
                        TgApplicationInfo::getCurrentAuditProcessStatus, TgApplicationInfo::getDataState,
                        TgApplicationInfo::getNewApplicationId, TgApplicationInfo::getOldApplicationId,
                        TgApplicationInfo::getAssetsId, TgApplicationInfo::getUpdateTime)
                .eq(TgApplicationInfo::getApplicationType, ApplicationConst.ApplicationType.DATA_APPLICATION)
                .notIn(CollectionUtils.isNotEmpty(existNos), TgApplicationInfo::getApplicationNo, existNos)
                // 重新申请，待审核，不能被过滤
//                .notIn(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.notShow)
                .list();

        Set<Long> tids = applyList.stream().map(TgApplicationInfo::getTemplateId).collect(Collectors.toSet());
        Map<Long, TgTemplateInfo> tempMap = Lambda.queryMapIfExist(tids, v -> templateInfoDAO.lambdaQuery()
                .select(TgTemplateInfo::getId, TgTemplateInfo::getTemplateType, TgTemplateInfo::getBizType)
                .in(TgTemplateInfo::getId, v)
                .list(), TgTemplateInfo::getId);

        Set<Long> aids = applyList.stream().map(TgApplicationInfo::getAssetsId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<UserDataAssets> assetsList = userDataAssetsDAO.lambdaQuery()
                .select(UserDataAssets::getId, UserDataAssets::getBaseVersionPeriod)
                .in(UserDataAssets::getId, aids)
                .list();
        Map<Long, String> assetsMap = new HashMap<>();
        for (UserDataAssets assets : assetsList) {
            assetsMap.put(assets.getId(), assets.getBaseVersionPeriod());
        }
//        Map<Long, String> assetsMap = Lambda.buildMap(assetsList, UserDataAssets::getId, UserDataAssets::getBaseVersionPeriod);

        Map<String, List<TgApplicationInfo>> noMap = applyList.stream()
                .collect(Collectors.groupingBy(TgApplicationInfo::getApplicationNo));
        List<ApplicationForm> append = new ArrayList<>();

        List<Integer> watchStates = Arrays.asList(ApplicationConst.AuditStatus.AUDITING,
                ApplicationConst.AuditStatus.AUDIT_PASS,
                ApplicationConst.AuditStatus.AUDIT_FAIL,
                ApplicationConst.AuditStatus.INVALID_APPLICATION);
        for (Map.Entry<String, List<TgApplicationInfo>> entry : noMap.entrySet()) {
            List<TgApplicationInfo> list = entry.getValue();
            if (list.size() == 1) {
                TgApplicationInfo info = list.get(0);
                if (!watchStates.contains(info.getCurrentAuditProcessStatus())) {
                    log.info("{} State {}", info.getId(), info.getCurrentAuditProcessStatus());
                    continue;
                }
//                if (!Objects.equals(ApplicationConst.AuditStatus.AUDIT_PASS, info.getCurrentAuditProcessStatus())
//                        && !Objects.equals(ApplicationConst.AuditStatus.INVALID_APPLICATION, info.getCurrentAuditProcessStatus())) {
//                    continue;
//                }

                String period = assetsMap.get(info.getAssetsId());
                ApplicationForm form = new ApplicationForm();
                form.setApplicationNo(info.getApplicationNo());
                form.setApplicationId(info.getId());
                form.setEnterRun(true);
                fillState(form, info);

                TgTemplateInfo temp = tempMap.get(info.getTemplateId());
                form.setBizType(temp.getBizType());
                if (Objects.nonNull(period)) {
                    form.setPeriod(period);
                } else {
                    AjaxResult<CurrentDataPlanDTO> planRes = dataPlanService.curPeriod(temp.getBizType());
                    String pppp = Optional.ofNullable(planRes).map(AjaxResult::getData).map(CurrentDataPlanDTO::getPeriod)
                            .orElse("");
                    form.setPeriod(pppp);
                }
                append.add(form);
            } else {
                Map<Long, TgApplicationInfo> applyMap = Lambda.buildMap(list, TgApplicationInfo::getId);
                List<TgApplicationInfo> top = list.stream().filter(v -> Objects.isNull(v.getNewApplicationId())).collect(Collectors.toList());
                TgApplicationInfo info = null;
                if (top.size() > 1) {
//                    log.warn("over 1 {}", entry.getKey());
                    List<TgApplicationInfo> lastPass = top.stream()
                            .filter(v -> Objects.equals(v.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDIT_PASS))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(lastPass)) {
                        if (lastPass.size() > 1) {
                            log.error("over 1 pass {}", entry.getKey());
                        } else {
                            info = lastPass.get(0);
                        }
                    } else {
                        Optional<TgApplicationInfo> lastUpdate = top.stream()
                                .sorted(Comparator.comparing(TgApplicationInfo::getUpdateTime).reversed()).findFirst();
                        info = lastUpdate.get();
                    }
                } else if (CollectionUtils.isEmpty(top)) {
                    log.error("empty {}", entry.getKey());
                    continue;
                } else {
                    info = top.get(0);
                }
                if (Objects.nonNull(info)) {
                    if (!Objects.equals(ApplicationConst.AuditStatus.AUDIT_PASS, info.getCurrentAuditProcessStatus())
                            && !Objects.equals(ApplicationConst.AuditStatus.INVALID_APPLICATION, info.getCurrentAuditProcessStatus())) {
                        info = applyMap.get(info.getOldApplicationId());
                    }
                }
                if (Objects.isNull(info)) {
                    log.error("Broke: id={} no={}", top.get(0).getId(), top.get(0).getApplicationNo());
                    continue;
                }

                String period = assetsMap.get(info.getAssetsId());
                ApplicationForm form = new ApplicationForm();
                form.setApplicationNo(info.getApplicationNo());
                form.setApplicationId(info.getId());
                form.setEnterRun(true);
                fillState(form, info);
                TgTemplateInfo temp = tempMap.get(info.getTemplateId());
                form.setBizType(temp.getBizType());
                if (Objects.nonNull(period)) {
                    form.setPeriod(period);
                } else {
                    AjaxResult<CurrentDataPlanDTO> planRes = dataPlanService.curPeriod(temp.getBizType());
                    String pppp = Optional.ofNullable(planRes).map(AjaxResult::getData).map(CurrentDataPlanDTO::getPeriod)
                            .orElse("");
                    form.setPeriod(pppp);
                }
                append.add(form);
            }

//            log.info("={}", entry.getKey());
        }
        log.info("append={}", append.size());
        if (BooleanUtils.isTrue(insert)) {
            applicationFormDAO.saveBatch(append);
        }
        return "OK";
    }

    /**
     * 1.完成：流程状态为【审核通过】且出数状态为【执行成功】。
     * 2.待处理：流程状态为【审核通过】且出数状态为【未执行】。
     * 3.处理中：流程状态为【审核通过】且出数状态为【执行中】。
     * 4.处理失败：流程状态为【审核通过】且出数状态为【执行失败】。
     * 5.已驳回：流程状态为【审核驳回】。
     * 6.待审核：流程状态为【待审核】（非重新申请，重新申请取上一个申请单的状态）。
     */
    private void fillState(ApplicationForm form, TgApplicationInfo info) {
        boolean fail = Objects.equals(info.getDataState(), ApplyDataStateEnum.fail.name());
        boolean run = Objects.equals(info.getDataState(), ApplyDataStateEnum.run.name());
        boolean none = Objects.isNull(info.getDataState())
                || Objects.equals(info.getDataState(), ApplyDataStateEnum.none.name())
                || Objects.equals(info.getDataState(), ApplyDataStateEnum.wait_confirm.name());
        boolean success = Objects.equals(info.getDataState(), ApplyDataStateEnum.success.name());

        boolean inAudit = Objects.equals(info.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDITING);
        boolean reject = Objects.equals(info.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDIT_FAIL);
        boolean pass = Objects.equals(info.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.AUDIT_PASS);
        boolean invalid = Objects.equals(info.getCurrentAuditProcessStatus(), ApplicationConst.AuditStatus.INVALID_APPLICATION);

        form.setApplyState(ApplyStateEnum.normal.name());
        if (pass && success) {
            form.setApplyRunState(ApplyRunStateEnum.finish.name());
        } else if (pass && none) {
            form.setApplyRunState(ApplyRunStateEnum.wait_run.name());
        } else if (pass && run) {
            form.setApplyRunState(ApplyRunStateEnum.running.name());
        } else if (pass && fail) {
            form.setApplyRunState(ApplyRunStateEnum.run_failed.name());
        } else if (reject) {
            form.setApplyState(ApplyStateEnum.none.name());
            form.setApplyRunState(ApplyRunStateEnum.audit_reject.name());
        } else if (inAudit) {
            form.setApplyState(ApplyStateEnum.none.name());
            form.setApplyRunState(ApplyRunStateEnum.wait_audit.name());
        } else if (invalid) {
            form.setApplyState(ApplyStateEnum.deprecated.name());
            form.setApplyRunState(ApplyRunStateEnum.finish.name());
        } else {
            throw new CustomException("不支持 " + info.getId());
        }
    }

    @Autowired
    private ApplicationFormService applicationFormService;

    @GetMapping("/invokeRefreshForm")
    public String invokeRefreshForm() {
        applicationFormService.refreshHandleFormScheduler();
        return "OK";
    }

    @GetMapping("/refreshExpireForm")
    public String refreshExpireForm() {
        applicationFormService.refreshExpireScheduler();
        return "OK";
    }

    @GetMapping("/cleanTest")
    public String cleanTest() {
        fileAdapter.cleanAllAssets();
        return "OK";
    }


    @ApiOperation("导出 全局需求管理")
    @PostMapping("/exportList/{size}")
    public void exportAssetsDistList(@PathVariable("size") Integer size, HttpServletResponse response) throws Exception {
        ExcelWriter excelWriter = null;
        try {
            ExcelWriterBuilder excelWriterBuilder = EasyExcel.write(response.getOutputStream());
            excelWriter = EasyExcelUtil.appendConfig(excelWriterBuilder);

            String[] headers = new String[]{"需求ID", "需求名称", "项目名称", "客户名称", "业务线", "需求类型", "需求性质",
                    "模板名称", "模板类型", "交付周期", "时间粒度", "产品粒度", "申请人", "申请时间", "数据有效期", "需求单状态",
                    "出数人", "预估交付时间", "需求流程状态", "交付方式", "关联表单/工作流", "调度频率", "当前交付期数", "当前版本",
                    "最新版本交付时间", "申请次数", "版本数", "需求个数", "需求份数", "需求成本", "数据条数"};
            WriteSheet sheet = EasyExcel.writerSheet(0, "全局需求管理").head(ExcelUtil.head(headers)).build();

            List<List<Object>> rows = new ArrayList<>();
            List<TgUserDataAssetsDistDto> allList = this.mockData(size);

            Map<String, String> cronMap = assetsFlowService.queryApplyFormScheduler();

            int batch = 1000;
            for (TgUserDataAssetsDistDto dictDTO : allList) {
                List<Object> row = new ArrayList<>();
                row.add(dictDTO.getApplicationNo());
                row.add(dictDTO.getProjectName());
                row.add(dictDTO.getNewProjectName());
                row.add(dictDTO.getClientNames());
                row.add(BizTypeEnum.getDesc(dictDTO.getBizType()));
                row.add(ApplicationConst.RequireTimeTypeEnum.DESC_MAP.get(dictDTO.getRequireTimeType()));
                row.add(RequireAttrType.DESC_MAP.get(dictDTO.getRequireAttr()));


                row.add(dictDTO.getTemplateName());
                row.add(TemplateTypeEnum.getDesc(dictDTO.getTemplateType()));
                row.add(DeliverTimeTypeEnum.getTypeDesc(dictDTO.getDeliverTimeType()));
                row.add(dictDTO.getTimeGra());
                row.add(dictDTO.getProductGra());
                row.add(dictDTO.getApplicantName());
                row.add(dictDTO.getCreateTime());
                row.add(dictDTO.getDataExpir());
                row.add(ApplyStateEnum.getDesc(dictDTO.getApplyState()));
                row.add(dictDTO.getHandleUser());
                row.add(dictDTO.getExpectDeliveryTime());
                row.add(ApplyRunStateEnum.getDesc(dictDTO.getApplyRunState()));
                row.add(dictDTO.getDataType());
                row.add(dictDTO.getTableName());
                String cronCN = cronMap.getOrDefault(dictDTO.getApplicationNo(), "");
                row.add(cronCN);
                row.add(dictDTO.getPeriod());
                row.add(dictDTO.getAssetsCreateTime());
                row.add(dictDTO.getApplyCnt());
                row.add(dictDTO.getDataVersion());
                row.add(dictDTO.getDataAmount());
                row.add(dictDTO.getApplyAmount());
                row.add(dictDTO.getDataTotal());

                rows.add(row);
                if (rows.size() > batch) {
                    excelWriter.write(rows, sheet);
                    rows.clear();
                }
            }
            if (!rows.isEmpty()) {
                excelWriter.write(rows, sheet);
                rows.clear();
            }
        } catch (Exception e) {
            log.error("异常", e);
            throw e;
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    private List<TgUserDataAssetsDistDto> mockData(int amount) {
        List<TgUserDataAssetsDistDto> all = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            String x = "{\n" +
                    "  \"id\": 0,\n" +
                    "  \"templateId\": 0,\n" +
                    "  \"assetsId\": 0,\n" +
                    "  \"applicationNo\": \"applicationNo_2ec465eca3e2\",\n" +
                    "  \"processId\": 0,\n" +
                    "  \"processVersion\": 0,\n" +
                    "  \"templateName\": \"templateName_7b811ae0682f\",\n" +
                    "  \"schedulerId\": 0,\n" +
                    "  \"configSqlWorkflowId\": 0,\n" +
                    "  \"workflowId\": 0,\n" +
                    "  \"templateType\": \"templateType_7f99c5eec705\",\n" +
                    "  \"baseTableId\": 0,\n" +
                    "  \"tableName\": \"tableName_6eda2e4fa674\",\n" +
                    "  \"applicantId\": 0,\n" +
                    "  \"applicantName\": \"applicantName_8b4fb4d7fde3\",\n" +
                    "  \"projectName\": \"projectName_00943312296b\",\n" +
                    "  \"newProjectName\": \"newProjectName_fe0d3748462b\",\n" +
                    "  \"requireAttr\": 0,\n" +
                    "  \"requireTimeType\": 0,\n" +
                    "  \"clientNames\": \"clientNames_e0ea75eb9b36\",\n" +
                    "  \"dataExpir\": \"dataExpir_779ed4565a6b\",\n" +
                    "  \"currentAuditProcessStatus\": 0,\n" +
                    "  \"status\": 0,\n" +
                    "  \"dataState\": \"dataState_5e0bf95be81c\",\n" +
                    "  \"createTime\": \"createTime_8dc51b83d6ae\",\n" +
                    "  \"dataVersion\": 0,\n" +
                    "  \"dataAmount\": 0,\n" +
                    "  \"dataCost\": 0.00,\n" +
                    "  \"dataCostMin\": 0,\n" +
                    "  \"asql\": \"asql_ed228046df32\",\n" +
                    "  \"tailSql\": \"tailSql_0d6e3e82c2df\",\n" +
                    "  \"bizType\": \"bizType_efda8ea557a0\",\n" +
                    "  \"configType\": 0,\n" +
                    "  \"configSql\": \"configSql_ddac3ab2cc17\",\n" +
                    "  \"assetsAttach\": {\n" +
                    "    \"name\": \"name_6aa3a867d4ac\",\n" +
                    "    \"path\": \"path_2356721799b1\"\n" +
                    "  },\n" +
                    "  \"productGra\": \"productGra_d70129c93b7f\",\n" +
                    "  \"timeGra\": \"timeGra_4687629e098d\",\n" +
                    "  \"actionList\": [\n" +
                    "    0\n" +
                    "  ],\n" +
                    "  \"relateDict\": false,\n" +
                    "  \"snapshotType\": \"snapshotType_35d9d1097802\",\n" +
                    "  \"deliverTimeType\": \"deliverTimeType_06ddba7896aa\",\n" +
                    "  \"dataTotal\": 0,\n" +
                    "  \"applyState\": \"applyState_189950da96f1\",\n" +
                    "  \"applyRunState\": \"applyRunState_6a5c65c14f9f\",\n" +
                    "  \"handleNode\": [\n" +
                    "    {\n" +
                    "      \"applicationId\": 0,\n" +
                    "      \"index\": 0,\n" +
                    "      \"nodeName\": \"nodeName_46dd1f4a1797\",\n" +
                    "      \"handlerName\": \"handlerName_af260be80622\",\n" +
                    "      \"handleTime\": \"handleTime_e493dc4dcc1a\",\n" +
                    "      \"handleReason\": \"handleReason_3a45de426eac\",\n" +
                    "      \"deliverDay\": 0,\n" +
                    "      \"status\": 0,\n" +
                    "      \"handleStatus\": 0\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"currentIndex\": 0,\n" +
                    "  \"currentHandlers\": \"currentHandlers_42f1c3468541\",\n" +
                    "  \"handleUser\": \"handleUser_a6f42fc5c66a\",\n" +
                    "  \"cronCN\": \"cronCN_b175c3cda5b6\",\n" +
                    "  \"dataType\": \"dataType_19e8cca967aa\",\n" +
                    "  \"period\": \"period_ec94b170757b\",\n" +
                    "  \"applyCnt\": 0,\n" +
                    "  \"applyAmount\": 0\n" +
                    "}";

            TgUserDataAssetsDistDto dto = JsonUtils.parse(x, TgUserDataAssetsDistDto.class);
            all.add(dto);
        }

        return all;
    }

    @GetMapping("/refreshGraField")
    @Transactional(rollbackFor = Exception.class)
    public String refreshGraField() {
        List<TgApplicationInfo> list = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getGranularityJson)
                .eq(TgApplicationInfo::getApplicationType, ApplicationConst.ApplicationType.DATA_APPLICATION)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return "EMPTY";
        }

        for (TgApplicationInfo info : list) {
            JsonBeanConverter.convert2Obj(info);

//            List<ApplicationGranularityDto> granularity = info.getGranularity();
            info.fillGraInfo();
//            granularity.stream()
//                    .filter(v -> FieldGranularityEnum.time.name().equals(v.getGranularity()))
//                    .findFirst().ifPresent(v -> {
//                        List<String> str = v.getSelectGranularity();
//                        if (CollectionUtils.isEmpty(str)) {
//                            return;
//                        }
//                        String pair = ApplyUtil.GRA_SPLIT + String.join(ApplyUtil.GRA_SPLIT, str) + ApplyUtil.GRA_SPLIT;
//                        info.setTimeGra(pair);
//                    });
//            granularity.stream()
//                    .filter(v -> FieldGranularityEnum.product.name().equals(v.getGranularity())).findFirst()
//                    .ifPresent(v -> {
//                        List<String> str = v.getSelectGranularity();
//                        if (CollectionUtils.isEmpty(str)) {
//                            return;
//                        }
//                        String pair = ApplyUtil.GRA_SPLIT + String.join(ApplyUtil.GRA_SPLIT, str) + ApplyUtil.GRA_SPLIT;
//                        info.setProductGra(pair);
//                    });

            log.info("id={} {} {}", info.getId(), info.getTimeGra(), info.getProductGra());
            if (Objects.nonNull(info.getTimeGra()) && Objects.nonNull(info.getProductGra())) {
                applicationDAO.lambdaUpdate()
                        .set(Objects.nonNull(info.getTimeGra()), TgApplicationInfo::getTimeGra, info.getTimeGra())
                        .set(Objects.nonNull(info.getProductGra()), TgApplicationInfo::getProductGra, info.getProductGra())
                        .eq(TgApplicationInfo::getId, info.getId())
                        .update();
            }
        }

        return "OK";
    }

    @Autowired
    private DataAssetsService dataAssetsService;

    @GetMapping("/timeGra")
    public AjaxResult<List<String>> timeGra() {
        return dataAssetsService.assetsTimeGra();
    }

}
