package com.sinohealth.system.biz.process.facade;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinohealth.common.annotation.RegisterCronMethod;
import com.sinohealth.common.config.AppProperties;
import com.sinohealth.common.constant.DsConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.TableInfoSnapshotCompareState;
import com.sinohealth.common.enums.dataassets.AssetsUpgradeStateEnum;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.common.enums.process.FlowProcessCategory;
import com.sinohealth.common.enums.process.FlowProcessStateEnum;
import com.sinohealth.common.enums.process.FlowProcessTaskEnum;
import com.sinohealth.common.enums.process.FlowProcessUpdateType;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsCompare;
import com.sinohealth.system.biz.dataassets.domain.AssetsQcBatch;
import com.sinohealth.system.biz.dataassets.domain.AssetsWideUpgradeTrigger;
import com.sinohealth.system.biz.dataassets.domain.PowerBiPushBatch;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.FlowAssetsPageDTO;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsCompareInvokeRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsFlowBatchCreateRequest;
import com.sinohealth.system.biz.dataassets.dto.request.FlowAssetsPageRequest;
import com.sinohealth.system.biz.dataassets.dto.request.PowerBiPushCreateRequest;
import com.sinohealth.system.biz.dataassets.helper.AssetsCompareInvoker;
import com.sinohealth.system.biz.dataassets.service.AssetsCompareService;
import com.sinohealth.system.biz.dataassets.service.AssetsFlowService;
import com.sinohealth.system.biz.dataassets.service.AssetsQcService;
import com.sinohealth.system.biz.dataassets.service.PowerBiPushService;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.biz.dataassets.vo.AssetsFlowBatchDetailVO;
import com.sinohealth.system.biz.dataassets.vo.AssetsFlowBatchVO;
import com.sinohealth.system.biz.dict.constant.KeyDictType;
import com.sinohealth.system.biz.dict.dao.KeyValDictDAO;
import com.sinohealth.system.biz.process.dao.TgFlowProcessAlertConfigDAO;
import com.sinohealth.system.biz.process.dao.TgFlowProcessErrorLogDAO;
import com.sinohealth.system.biz.process.dao.TgFlowProcessManagementDAO;
import com.sinohealth.system.biz.process.domain.TgFlowProcessAlertConfig;
import com.sinohealth.system.biz.process.domain.TgFlowProcessManagement;
import com.sinohealth.system.biz.process.domain.TgFlowProcessSettingBase;
import com.sinohealth.system.biz.process.domain.TgFlowProcessSettingDetail;
import com.sinohealth.system.biz.process.dto.CreateAutoProcessRequest;
import com.sinohealth.system.biz.process.dto.CreateOrUpdateFlowProcessAlertConfigRequest;
import com.sinohealth.system.biz.process.dto.FlowProcessPageRequest;
import com.sinohealth.system.biz.process.dto.FlowProcessSaveSettingDetailRequest;
import com.sinohealth.system.biz.process.dto.FlowProcessSaveSettingRequest;
import com.sinohealth.system.biz.process.service.TgFlowProcessCheckService;
import com.sinohealth.system.biz.process.service.TgFlowProcessManagementService;
import com.sinohealth.system.biz.process.service.TgFlowProcessSettingBaseService;
import com.sinohealth.system.biz.process.service.TgFlowProcessSettingDetailService;
import com.sinohealth.system.biz.process.vo.FlowProcessAlertConfigVO;
import com.sinohealth.system.biz.process.vo.FlowProcessAttachVO;
import com.sinohealth.system.biz.process.vo.FlowProcessSettingBaseVO;
import com.sinohealth.system.biz.process.vo.FlowProcessSettingDetailVO;
import com.sinohealth.system.biz.process.vo.FlowProcessVO;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.service.DataPlanService;
import com.sinohealth.system.biz.project.util.DataPlanUtil;
import com.sinohealth.system.biz.table.constants.TablePushStatusEnum;
import com.sinohealth.system.biz.table.dao.TableInfoSnapshotDAO;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import com.sinohealth.system.biz.table.domain.TgTableInfoSnapshotCompare;
import com.sinohealth.system.biz.table.dto.TableDiffRequest;
import com.sinohealth.system.biz.table.dto.TableSnapshotPushRequest;
import com.sinohealth.system.biz.table.facade.TableInfoSnapshotCompareFacade;
import com.sinohealth.system.biz.table.service.TableInfoSnapshotService;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.mapper.TgAssetInfoMapper;
import com.sinohealth.system.service.AssetInfoService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.IntergrateProcessDefService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.View;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Slf4j
public class TgFlowProcessFacade {
    private final RedisTemplate<Object, Object> redisTemplate;
    private final AppProperties appProperties;

    private final TgFlowProcessManagementService tgFlowProcessManagementService;
    private final TgFlowProcessSettingBaseService tgFlowProcessSettingBaseService;
    private final TgFlowProcessSettingDetailService tgFlowProcessSettingDetailService;
    private final IntergrateProcessDefService intergrateProcessDefService;
    private final AssetsFlowService assetsFlowService;
    private final TgFlowProcessCheckService flowProcessCheckService;
    private final AssetInfoService assetInfoService;
    private final TableInfoSnapshotService tableInfoSnapshotService;
    private final PowerBiPushService powerBiPushService;
    private final ITableInfoService iTableInfoService;
    private final AssetsCompareService assetsCompareService;
    private final AssetsQcService assetsQcService;
    private final AlertService alertService;
    private final UserDataAssetsService userDataAssetsService;
    private final DataPlanService dataPlanService;

    private final TableInfoSnapshotDAO tableInfoSnapshotDAO;
    private final TgFlowProcessManagementDAO flowProcessManagementDAO;
    private final KeyValDictDAO keyValDictDAO;
    private final TgFlowProcessAlertConfigDAO tgFlowProcessAlertConfigDAO;
    private final UserDataAssetsDAO userDataAssetsDAO;
    private final UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;
    private final ApplicationDAO applicationDAO;
    private final TgFlowProcessErrorLogDAO tgFlowProcessErrorLogDAO;
    private final TgAssetInfoMapper assetInfoMapper;

    private final TableInfoSnapshotCompareFacade tableInfoSnapshotCompareFacade;
    private final TgFlowProcessAlertFacade tgFlowProcessAlertFacade;
    private final AssetsCompareInvoker assetsCompareInvoker;
    private final View error;

    /**
     * 每分钟扫描一次该结果待执行任务【再发起】
     */
    @RegisterCronMethod
    @Scheduled(cron = "0/30 * * * * ? ")
    public void waitScheduled() {
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(RedisKeys.FlowProcess.WAIT_PROCESS_LOCK_KEY, 1, 20, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(lock)) {
            List<TgFlowProcessManagement> query = tgFlowProcessManagementService.query(FlowProcessStateEnum.WAIT.getCode(), new Date());
            if (!CollectionUtils.isEmpty(query)) {
                log.info("待执行任务任务条数：{}", query.size());
                this.handleWaitState(query);
            }
        }
    }

    /**
     * 推动状态变化
     */
    @RegisterCronMethod
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void runningScheduled() {
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(RedisKeys.FlowProcess.RUN_PROCESS_LOCK_KEY, 1, 40, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(lock)) {
            return;
        }

        LambdaQueryWrapper<TgFlowProcessManagement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TgFlowProcessManagement::getState, FlowProcessStateEnum.RUNNING.getCode());
        List<TgFlowProcessManagement> query = tgFlowProcessManagementService.query(wrapper);
        if (!CollectionUtils.isEmpty(query)) {
            List<Long> ids = Lambda.buildList(query, TgFlowProcessManagement::getId);
            log.info("全流程-进行中：{}", ids);
            // 资产升级任务
            List<Long> assetsUpdateBizIds = query.stream()
                    .filter(i -> Objects.equals(FlowProcessStateEnum.RUNNING.getCode(), i.getAssetsUpdateState()))
                    .map(TgFlowProcessManagement::getId).collect(Collectors.toList());
            Map<Long, List<AssetsCompare>> compareMap = Collections.emptyMap();
            Map<Long, Boolean> assetsUpdateStateMap = Collections.emptyMap();
            if (!CollectionUtils.isEmpty(assetsUpdateBizIds)) {
                assetsUpdateStateMap = qryAssetsUpdateState(assetsUpdateBizIds);
                List<AssetsCompare> assetsCompares = assetsCompareService.queryByBizIds(assetsUpdateBizIds);
                compareMap = Optional.ofNullable(assetsCompares).orElse(Collections.emptyList())
                        .stream().collect(Collectors.groupingBy(AssetsCompare::getBizId));
            }
            // qc
            Map<Long, AssetsQcBatch> qcDetailMap = buildQcDataMap(query);
            // 数据对比
            Map<Long, List<AssetsCompare>> runningCompareMap = buildPlanCompareDataMap(query);
            // PowerBI
            Map<Long, String> powerBiStateMap = buildPowerBiStateMap(query);
            // 底表对比
            Map<Long, TgTableInfoSnapshotCompare> dataCompareMap = buildDataCompareMap(query);
            // 工作流出数
            Map<Long, AssetsUpgradeStateEnum> workFlowStateMap = this.buildWorkFlowStateMap(query);

            for (TgFlowProcessManagement management : query) {
                LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(TgFlowProcessManagement::getId, management.getId());
                // 工作流出数
                boolean haveChange = handleWorkFlow(workFlowStateMap, management, updateWrapper);
                // 判断资产升级状态: 发起资产升级，库表对比，资产数据对比
                haveChange = handleAssetsUpdate(compareMap, assetsUpdateStateMap, management, updateWrapper) || haveChange;
                // 发起 powerBi
                haveChange = startPushPowerBi(management, updateWrapper) || haveChange;
                // 发起 QC
                haveChange = startQc(management, updateWrapper) || haveChange;

                // 以下为 检查正在异步执行中的任务 更新状态到 成功/失败
                // 处理qc状态
                haveChange = handleQc(qcDetailMap, management, updateWrapper, haveChange);
                // 数据对比
                haveChange = handlePlanCompare(runningCompareMap, management, updateWrapper, haveChange);
                // powerBi
                haveChange = handlePowerBi(powerBiStateMap, management, updateWrapper, haveChange);
                // 底表对比
                haveChange = handleTableDataCompare(dataCompareMap, management, updateWrapper, haveChange);
                // 是否最终状态
                haveChange = handleFinish(management, updateWrapper, haveChange);
                if (haveChange) {
                    flowProcessManagementDAO.update(updateWrapper);
                }
            }
        }
    }

    /**
     * 每1分钟扫描一次自动定时配置
     */
    @RegisterCronMethod
    @Scheduled(cron = "0 0/1 * * * ?")
    public void autoScheduled() {
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(RedisKeys.FlowProcess.AUTO_PROCESS_LOCK_KEY,
                1, 30, TimeUnit.SECONDS);
        if (BooleanUtils.isNotTrue(lock)) {
            return;
        }

        AjaxResult<FlowProcessSettingBaseVO> r = detail(null, FlowProcessCategory.AUTO.getCode());
        if (r.isSuccess() && Objects.nonNull(r.getData())) {
            FlowProcessSettingBaseVO data = r.getData();
            if (this.matchTime(data.getPlanExecutionTime())) {
                log.info("触发自动任务检查");
                this.checkWithRun();
            }
        }
    }

    /**
     * 任务完成回调
     *
     * @param bizId    全流程任务主键
     * @param category 全流程任务类别
     * @param state    状态
     * @param uid      对应任务id
     * @see TableInfoSnapshotService#rollingTable(String) 注意必须在该接口回调后才能回调当前接口
     */
    public void callback(Long bizId, String category, Integer state, String uid) {
        log.info("回调了呦:{},{},{},{}", bizId, category, state, uid);
        TgFlowProcessManagement management = tgFlowProcessManagementService.queryById(bizId);
        if (Objects.isNull(management)) {
            return;
        }
        FlowProcessAttachVO attach = getAttachVO(management);

        if (Objects.equals(category, FlowProcessTaskEnum.SYNC.getCode())) {
            // 底表同步
            AssetsUpgradeStateEnum stateEnum = AssetsUpgradeStateEnum.ofFlowState(state);
            if (AssetsUpgradeStateEnum.success.equals(stateEnum)) {
                TgAssetInfo assetInfo = assetDetail(attach.getBase().getTableAssetId());

                Long tableId = assetInfo.getRelatedId();
                // 回写当前版本
                TableInfoSnapshot latest = tableInfoSnapshotDAO.getLatest(tableId);
                latest.setBizId(bizId);
                latest.setRemark(buildPeriodName(management));
                latest.setFlowProcessType(management.getVersionCategory());
                latest.setUpdateTime(new Date());
                tableInfoSnapshotDAO.updateById(latest);

                // 主流程表生成后
                if (Objects.equals(management.getVersionCategory(), FlowProcessTypeEnum.deliver.name())) {
                    tableInfoSnapshotService.asyncDeleteQcVersionTable(tableId);
                }

                management.setTableSnapshotId(latest.getId());
                management.setTableSnapshotVersion(latest.getVersion());

                // 底表资产升级
                try {
                    this.startPushTable(tableId, management);
                    management.setAssetsUpdateState(FlowProcessStateEnum.RUNNING.getCode());
                } catch (Exception e) {
                    log.info("发起资产升级失败：{}", management.getId());
                    management.setSyncState(FlowProcessStateEnum.FAILED.getCode());
                    management.setAssetsUpdateState(FlowProcessStateEnum.FAILED.getCode());
                    management.setTableDataCompareState(FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                    management.setQcState(FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                    management.setPlanCompareState(FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                    management.setPushPowerBiState(FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                    tgFlowProcessAlertFacade.sendSubMsg(management, FlowProcessTaskEnum.SYNC.getCode(), false);
                }
            } else if (Objects.equals(stateEnum, AssetsUpgradeStateEnum.failed)) {
                management.setSyncState(FlowProcessStateEnum.FAILED.getCode());
                management.setAssetsUpdateState(FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                management.setTableDataCompareState(FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                management.setQcState(FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                management.setPlanCompareState(FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                management.setPushPowerBiState(FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
            }
            management.setUpdateTime(new Date());
            tgFlowProcessManagementService.saveOrUpdate(management);
        }
    }

    /**
     * 定时或手动触发 通过检查QC日志表 触发相应类型的全流程
     */
    public void checkWithRun() {
        Optional<CreateAutoProcessRequest> reqOpt = flowProcessCheckService.buildReqByCheck();
        if (!reqOpt.isPresent()) {
            log.warn("No flow process triggered");
            return;
        }

        this.createAutoProcess(reqOpt.get());
    }

    /**
     * 发起自动流程
     *
     * @param request 参数
     */
    public void createAutoProcess(CreateAutoProcessRequest request) {
        // 自动类型
        TgFlowProcessSettingBase base = findBase(FlowProcessCategory.AUTO.getCode());
        List<TgFlowProcessSettingDetail> details = tgFlowProcessSettingDetailService.getByBaseId(base.getId());
        Optional<TgFlowProcessSettingDetail> first = details.stream().filter(i -> Objects.equals(i.getCategory(), request.getType().name())).findFirst();
        if (!first.isPresent()) {
            log.info("无对应详细配置:{}", request.getType().getDesc());
            return;
        }
        TgFlowProcessSettingDetail detailSetting = first.get();
        TgFlowProcessManagement management = buildManagement(base, detailSetting, request.getProdCodes(), request.getPeriod());
        tgFlowProcessManagementService.saveOrUpdate(management);
    }

    /**
     * 保存全流程配置
     *
     * @param request 参数
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<Void> saveSetting(FlowProcessSaveSettingRequest request) {
        valid(request);
        TgFlowProcessSettingBase base;
        if (Objects.equals(request.getCategory(), FlowProcessCategory.AUTO.getCode())) {
            // 自动类型
            base = findBase(request.getCategory());
        } else {
            base = new TgFlowProcessSettingBase();
        }

        copyValue(request, base);
        // 保存基本信息
        tgFlowProcessSettingBaseService.saveOrUpdate(base);
        List<TgFlowProcessSettingDetail> details = request.getDetails()
                .stream().map(i -> buildDetail(base, i)).collect(Collectors.toList());
        tgFlowProcessSettingDetailService.saveBatch(details);
        // 创建待执行管理任务
        if (Objects.equals(request.getCategory(), FlowProcessCategory.MANUAL_OPERATION.getCode())) {
            CurrentDataPlanDTO plan = dataPlanService.currentPeriod(request.getBizType());
            String period = Optional.ofNullable(plan).map(CurrentDataPlanDTO::getPeriod)
                    .orElseThrow(() -> new CustomException("当前周期信息获取失败"));
            // 查询当前周期
            TgFlowProcessManagement management = buildManagement(base, details.get(0),
                    request.getDetails().get(0).getProdCodes(), period);
            tgFlowProcessManagementService.saveOrUpdate(management);
        }
        return AjaxResult.succeed();
    }

    /**
     * 详情接口
     *
     * @param id       主键
     * @param category 类型
     * @return 详情信息
     */
    public AjaxResult<FlowProcessSettingBaseVO> detail(@Nullable Long id, String category) {
        TgFlowProcessSettingBase base;
        if (Objects.isNull(id) || Objects.equals(category, FlowProcessCategory.AUTO.getCode())) {
            // 自动类型
            base = findBase(category);
        } else {
            base = tgFlowProcessSettingBaseService.detail(id);
        }

        if (Objects.isNull(base) || Objects.isNull(base.getId())) {
            return AjaxResult.success(null, null);
        }
        FlowProcessSettingBaseVO r = new FlowProcessSettingBaseVO();
        BeanUtils.copyProperties(base, r);
        List<TgFlowProcessSettingDetail> details = tgFlowProcessSettingDetailService.getByBaseId(base.getId());
        if (!CollectionUtils.isEmpty(details)) {
            r.setDetails(details.stream().map(i -> {
                FlowProcessSettingDetailVO e = new FlowProcessSettingDetailVO();
                BeanUtils.copyProperties(i, e);
                return e;
            }).collect(Collectors.toList()));
        }
        return AjaxResult.success(r);
    }

    /**
     * 全流程管理-分页
     *
     * @param request 参数
     * @return 全流程记录
     */
    public AjaxResult<IPage<FlowProcessVO>> page(FlowProcessPageRequest request) {
        IPage<TgFlowProcessManagement> page = tgFlowProcessManagementService.page(request);
        List<TgFlowProcessManagement> records = page.getRecords();
        List<FlowProcessVO> datas = new ArrayList<>(request.getSize());
        if (!CollectionUtils.isEmpty(records)) {
            // qc
            Map<Long, AssetsQcBatch> qcDetailMap = buildQcDataMap(records);
            // powerbi
            Map<Long, String> powerBiStateMap = buildPowerBiStateMap(records);
            // 底表对比
            Map<Long, TgTableInfoSnapshotCompare> dataCompareMap = buildDataCompareMap(records);

            records.forEach(i -> {
                FlowProcessVO e = new FlowProcessVO();
                e.setId(i.getId());
                e.setSettingId(i.getSettingId());
                e.setName(i.getName());
                e.setPeriod(i.getPeriod());
                e.setVersionCategory(i.getVersionCategory());
                e.setTableAssetId(i.getTableAssetId());
                e.setTableAssetName(i.getTableAssetName());
                e.setTemplateIds(i.getTemplateIds());
                e.setTemplateNames(i.getTemplateNames());
                e.setPlanExecutionTime(i.getPlanExecutionTime());
                e.setExecutionBeginTime(i.getExecutionBeginTime());
                e.setExecutionFinishTime(i.getExecutionFinishTime());
                e.setAttach(i.getAttach());
                e.setState(i.getState());
                e.setSyncState(i.getSyncState());
                e.setWorkFlowState(i.getWorkFlowState());
                e.setPlanCompareState(i.getPlanCompareState());
                e.setCreateCategory(i.getCreateCategory());
                e.setCreator(i.getCreator());
                e.setCreatorName(i.getCreatorName());
                e.setCreateTime(i.getCreateTime());
                e.setUpdateTime(i.getUpdateTime());

                buildRunningQcState(qcDetailMap, i, e);
                buildRunningPowerBiState(powerBiStateMap, i, e);
                buildRunningDataCompareState(dataCompareMap, i, e);
                if (Objects.equals(FlowProcessStateEnum.SUCCESS.getCode(), i.getState())
                        || Objects.equals(FlowProcessStateEnum.FAILED.getCode(), i.getState())) {
                    if (Objects.nonNull(i.getExecutionBeginTime()) && Objects.nonNull(i.getExecutionFinishTime())) {
                        buildCostTime(i, e);
                    }
                }
                List<String> deliverTimeTypes = getDeliverTimeType(e.getPeriod());
                if (CollectionUtil.isNotEmpty(deliverTimeTypes)) {
                    e.setDeliveryCycle(String.join(",", deliverTimeTypes));
                }

                datas.add(e);
            });
        }

        IPage<FlowProcessVO> r = new Page<>();
        r.setCurrent(page.getCurrent());
        r.setSize(page.getSize());
        r.setTotal(page.getTotal());
        r.setPages(page.getPages());
        r.setRecords(datas);
        return AjaxResult.success(r);
    }

    /**
     * 计算耗时
     *
     * @param i 全流程管理任务信息
     * @param e vo
     */
    private void buildCostTime(TgFlowProcessManagement i, FlowProcessVO e) {
        // 计算时间差的毫秒数
        long diffInMilliseconds = i.getExecutionFinishTime().getTime() - i.getExecutionBeginTime().getTime();

        // 将毫秒数转换为秒数
        long diffInSeconds = diffInMilliseconds / 1000;

        // 计算小时数和分钟数
        int hours = (int) (diffInSeconds / 3600);
        int minutes = (int) ((diffInSeconds % 3600) / 60);
        int seconds = (int) (diffInSeconds % 60);

        // 输出结果
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m");
        }
        if (seconds > 0) {
            sb.append(seconds).append("s");
        }
        // 耗时【时分秒】
        e.setCostTime(sb.toString());
    }

    /**
     * 组装运行中底表对比状态
     *
     * @param dataCompareMap 状态map
     * @param i              当前元素
     * @param e              结果
     */
    private void buildRunningDataCompareState(Map<Long, TgTableInfoSnapshotCompare> dataCompareMap,
                                              TgFlowProcessManagement i, FlowProcessVO e) {
        if (Objects.equals(FlowProcessStateEnum.RUNNING.getCode(), i.getTableDataCompareState())) {
            TgTableInfoSnapshotCompare compare = dataCompareMap.get(i.getId());
            if (Objects.nonNull(compare)) {
                if (Objects.equals(compare.getState(), TableInfoSnapshotCompareState.COMPLETED.getType())) {
                    e.setTableDataCompareState(FlowProcessStateEnum.SUCCESS.getCode());
                } else if (Objects.equals(compare.getState(), TableInfoSnapshotCompareState.FAIL.getType())) {
                    e.setTableDataCompareState(FlowProcessStateEnum.FAILED.getCode());
                } else if (Objects.equals(compare.getState(), TableInfoSnapshotCompareState.RUNNING.getType())) {
                    e.setTableDataCompareState(FlowProcessStateEnum.RUNNING.getCode());
                } else {
                    e.setTableDataCompareState(FlowProcessStateEnum.WAIT.getCode());
                }
            } else {
                e.setTableDataCompareState(FlowProcessStateEnum.FAILED.getCode());
            }
        } else {
            e.setTableDataCompareState(i.getTableDataCompareState());
        }
    }

    /**
     * 组装运行中powerbi状态
     *
     * @param powerBiStateMap 状态map
     * @param i               当前元素
     * @param e               结果
     */
    private void buildRunningPowerBiState(Map<Long, String> powerBiStateMap, TgFlowProcessManagement i, FlowProcessVO e) {
        if (Objects.equals(FlowProcessStateEnum.RUNNING.getCode(), i.getPushPowerBiState())) {
            String biStatus = powerBiStateMap.get(i.getId());
            if (!StringUtils.isEmpty(biStatus)) {
                if (Objects.equals(biStatus, AssetsUpgradeStateEnum.success.name())) {
                    e.setPushPowerBiState(FlowProcessStateEnum.SUCCESS.getCode());
                } else if (Objects.equals(biStatus, AssetsUpgradeStateEnum.failed.name())) {
                    e.setPushPowerBiState(FlowProcessStateEnum.FAILED.getCode());
                } else if (Objects.equals(biStatus, AssetsUpgradeStateEnum.running.name())) {
                    e.setPushPowerBiState(FlowProcessStateEnum.RUNNING.getCode());
                } else {
                    e.setPushPowerBiState(FlowProcessStateEnum.WAIT.getCode());
                }
            } else {
                e.setPushPowerBiState(FlowProcessStateEnum.FAILED.getCode());
            }
        } else {
            e.setPushPowerBiState(i.getPushPowerBiState());
        }
    }

    /**
     * 组装运行中qc状态
     *
     * @param qcDetailMap qc状态map
     * @param i           当前元素
     * @param e           结果
     */
    private void buildRunningQcState(Map<Long, AssetsQcBatch> qcDetailMap, TgFlowProcessManagement i, FlowProcessVO e) {
        if (Objects.equals(FlowProcessStateEnum.RUNNING.getCode(), i.getQcState())) {
            AssetsQcBatch assetsQcBatch = qcDetailMap.get(i.getId());
            if (Objects.nonNull(assetsQcBatch)) {
                if (Objects.equals(assetsQcBatch.getState(), AssetsUpgradeStateEnum.success.name())) {
                    e.setQcState(FlowProcessStateEnum.SUCCESS.getCode());
                } else if (Objects.equals(assetsQcBatch.getState(), AssetsUpgradeStateEnum.failed.name())) {
                    e.setQcState(FlowProcessStateEnum.FAILED.getCode());
                } else if (Objects.equals(assetsQcBatch.getState(), AssetsUpgradeStateEnum.running.name())
                        || Objects.equals(assetsQcBatch.getState(), AssetsUpgradeStateEnum.wait.name())) {
                    e.setQcState(FlowProcessStateEnum.RUNNING.getCode());
                }
            } else {
                e.setQcState(FlowProcessStateEnum.FAILED.getCode());
            }
        } else {
            e.setQcState(i.getQcState());
        }
    }

    /**
     * 删除管理记录
     *
     * @param id 记录编号
     * @return 是否成功
     */
    public AjaxResult<Void> delete(Long id) {
        TgFlowProcessManagement tgFlowProcessManagement = tgFlowProcessManagementService.queryById(id);
        if (Objects.isNull(tgFlowProcessManagement)) {
            throw new CustomException("管理记录不存在");
        }
        if (!Objects.equals(FlowProcessStateEnum.WAIT.getCode(), tgFlowProcessManagement.getState())) {
            throw new CustomException("该记录非待执行状态，不可删除");
        }
        tgFlowProcessManagementService.delete(id);
        return AjaxResult.succeed();
    }

    /**
     * 保存告警配置
     *
     * @param request 参数
     * @return 是否成功
     */
    public AjaxResult<Void> saveAlertConfig(CreateOrUpdateFlowProcessAlertConfigRequest request) {
        List<TgFlowProcessAlertConfig> configs = request.getDetails().stream().map(i -> {
            TgFlowProcessAlertConfig e = new TgFlowProcessAlertConfig();
            BeanUtils.copyProperties(i, e);
            return e;
        }).collect(Collectors.toList());
        tgFlowProcessAlertConfigDAO.saveOrUpdateBatch(configs);
        return AjaxResult.succeed();
    }

    /**
     * 查询告警配置
     *
     * @param category 告警配置类型
     * @return 告警配置信息
     */
    public AjaxResult<List<FlowProcessAlertConfigVO>> queryAlertConfig(String category) {
        List<FlowProcessAlertConfigVO> r = new ArrayList<>();
        List<TgFlowProcessAlertConfig> list = tgFlowProcessAlertConfigDAO.list(category);
        if (!CollectionUtils.isEmpty(list)) {
            for (TgFlowProcessAlertConfig config : list) {
                FlowProcessAlertConfigVO vo = new FlowProcessAlertConfigVO();
                BeanUtils.copyProperties(config, vo);
                r.add(vo);
            }
        }
        return AjaxResult.success(r);
    }

    /**
     * 复制信息
     *
     * @param request 请求信息
     * @param base    基础信息
     */
    private void copyValue(FlowProcessSaveSettingRequest request, TgFlowProcessSettingBase base) {
        base.setName(request.getName());
        base.setPlanExecutionTime(request.getPlanExecutionTime());
        base.setBizType(request.getBizType());
        base.setTableAssetId(request.getTableAssetId());
        String templateIds = request.getModelAssetIds().stream().map(v -> v + "").collect(Collectors.joining(","));
        base.setModelAssetIds(templateIds);
        base.setCategory(request.getCategory());
    }

    /**
     * 组装详细信息
     *
     * @param base 基础信息
     * @param i    源元素
     * @return 详细entity
     */
    private TgFlowProcessSettingDetail buildDetail(TgFlowProcessSettingBase base, FlowProcessSaveSettingDetailRequest i) {
        TgFlowProcessSettingDetail e = new TgFlowProcessSettingDetail();
        e.setId(i.getId());
        e.setBaseId(base.getId());
        e.setCategory(i.getCategory());
        e.setUpdateType(i.getUpdateType());
        e.setPlanCompare(i.getPlanCompare());
        e.setPlanCompareCategory(i.getPlanCompareCategory());
        e.setTableDataCompare(i.getTableDataCompare());
        e.setTableDataCompareCategory(i.getTableDataCompareCategory());
        e.setAssetsQc(i.getAssetsQc());
        e.setPushPowerBi(i.getPushPowerBi());
        if (!CollectionUtils.isEmpty(i.getProdCodes())) {
            e.setAttach(JSONObject.toJSONString(i.getProdCodes()));
        }
        return e;
    }

    /**
     * 校验请求参数是否合规
     *
     * @param request 请求信息
     */
    private void valid(FlowProcessSaveSettingRequest request) {
        if (StringUtils.isNotBlank(request.getName())) {
            // 校验名称是否重复
            List<TgFlowProcessSettingBase> bases = tgFlowProcessSettingBaseService.findByName(request.getName());
            if (!CollectionUtils.isEmpty(bases)) {
                throw new CustomException("流程名称不可重复");
            }
        }
        if (Objects.equals(request.getCategory(), FlowProcessCategory.MANUAL_OPERATION.getCode())
                && request.getDetails().size() > 1) {
            throw new CustomException("手动流程不可设置两个版本类型");
        }
        // 校验流程详细配置是否重复
        Set<String> categorys = new HashSet<>();
        for (FlowProcessSaveSettingDetailRequest detail : request.getDetails()) {
            if (!categorys.add(detail.getCategory())) {
                throw new CustomException("流程配置版本类型不可重复");
            }
        }
    }

    /**
     * 根据类型获取配置信息
     *
     * @param category 类型
     * @return 配置信息
     */
    private TgFlowProcessSettingBase findBase(String category) {
        List<TgFlowProcessSettingBase> list = tgFlowProcessSettingBaseService.findByCategory(category);
        if (CollectionUtils.isEmpty(list)) {
            return new TgFlowProcessSettingBase();
        }
        return list.get(0);
    }

    /**
     * 获取底表名称
     *
     * @param id 资产id
     * @return 底表名称
     */
    private String assetTableName(Long id) {
        TgAssetInfo result = assetDetail(id);
        if (Objects.nonNull(result)) {
            return result.getAssetBindingDataName();
        }
        return null;
    }

    /**
     * 拼接模板资产名称
     *
     * @param modelAssetIds 模板资产编号
     * @return 模板名称
     */
    private String assetModelNames(String modelAssetIds) {
        if (StringUtils.isBlank(modelAssetIds)) {
            return null;
        }
        List<String> list = Arrays.asList(modelAssetIds.split(","));
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.stream().map(Long::valueOf).map(this::assetDetail)
                .filter(Objects::nonNull).map(TgAssetInfo::getAssetName).collect(Collectors.joining(","));
    }

    /**
     * 查询资产信息
     *
     * @param id 资产编号
     * @return 资产信息
     */
    private TgAssetInfo assetDetail(Long id) {
        return assetInfoService.getById(id);
    }

    /**
     * 构建管理记录
     *
     * @param base      配置基本信息
     * @param detail    配置详情信息
     * @param prodCodes 品类
     * @return 管理记录
     */
    private TgFlowProcessManagement buildManagement(TgFlowProcessSettingBase base, TgFlowProcessSettingDetail detail,
                                                    List<String> prodCodes, String period) {
        TgFlowProcessManagement management = new TgFlowProcessManagement();
        management.setBizType(base.getBizType());
        management.setSettingId(base.getId());
        management.setPeriod(period);
        management.setVersionCategory(detail.getCategory());
        management.setTableAssetId(base.getTableAssetId());
        management.setTableAssetName(assetTableName(base.getTableAssetId()));
        management.setTemplateIds(base.getModelAssetIds());
        management.setTemplateNames(assetModelNames(base.getModelAssetIds()));

        String name = StringUtils.isEmpty(base.getName()) ? getName(management, null) : base.getName();
        management.setName(name);
        fillUserAndTime(base, management);

        management.setCreateCategory(base.getCategory());
        management.setExecutionBeginTime(null);
        management.setExecutionFinishTime(null);
        FlowProcessAttachVO attach = new FlowProcessAttachVO();
        attach.setBase(base);
        attach.setDetail(detail);
        attach.setProdCodes(prodCodes);
        management.setAttach(JSONObject.toJSONString(attach));
        management.setState(FlowProcessStateEnum.WAIT.getCode());
        management.setSyncState(FlowProcessStateEnum.WAIT.getCode());
        management.setWorkFlowState(FlowProcessStateEnum.WAIT.getCode());
        management.setAssetsUpdateState(FlowProcessStateEnum.WAIT.getCode());
        management.setQcState(FlowProcessStateEnum.getCode(detail.getAssetsQc()));

        if (BooleanUtils.isTrue(detail.getTableDataCompare())) {
            Optional<TgFlowProcessManagement> dataOpt = flowProcessManagementDAO.findLatest(detail.getTableDataCompareCategory());
            if (dataOpt.isPresent()) {
                management.setTableDataCompareState(FlowProcessStateEnum.WAIT.getCode());
                management.setTableDataCompareBizId(dataOpt.get().getId());
            } else {
                management.setTableDataCompareState(FlowProcessStateEnum.WAIT.getCode());
            }
        } else {
            management.setTableDataCompareState(FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
        }

        if (BooleanUtils.isTrue(detail.getPlanCompare())) {
            Optional<TgFlowProcessManagement> dataOpt = flowProcessManagementDAO.findLatest(detail.getPlanCompareCategory());
            if (dataOpt.isPresent()) {
                management.setPlanCompareState(FlowProcessStateEnum.WAIT.getCode());
                management.setPlanCompareBizId(dataOpt.get().getId());
            } else {
                management.setPlanCompareState(FlowProcessStateEnum.WAIT.getCode());
            }
        } else {
            management.setPlanCompareState(FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
        }
        management.setPushPowerBiState(FlowProcessStateEnum.getCode(detail.getPushPowerBi()));
        management.setCreateTime(new Date());
        management.setUpdateTime(new Date());
        management.setDeleted(0);
        return management;
    }

    private void fillUserAndTime(TgFlowProcessSettingBase base, TgFlowProcessManagement management) {
        if (Objects.equals(FlowProcessCategory.MANUAL_OPERATION.getCode(), base.getCategory())) {
            management.setCreator(SecurityUtils.getUserId());
            management.setCreatorName(SecurityUtils.getRealName());
            Date planExecutionTime = DateUtils.parseDateTime(base.getPlanExecutionTime());
            if (Objects.isNull(planExecutionTime)) {
                throw new CustomException("时间格式错误");
            }
            management.setPlanExecutionTime(planExecutionTime);
        } else {
            management.setCreator(0L);
            management.setCreatorName("系统");
            // 拼接时间
            String time = DateUtils.getDate() + " " + base.getPlanExecutionTime();
            Date date = DateUtils.parseDateTime(time);
            if (Objects.isNull(date)) {
                throw new CustomException("时间格式错误");
            }
            management.setPlanExecutionTime(date);
        }
    }

    /**
     * 获取附加信息
     *
     * @param i 实体
     * @return 附加信息
     */
    private FlowProcessAttachVO getAttachVO(TgFlowProcessManagement i) {
        return JSONObject.parseObject(i.getAttach(), FlowProcessAttachVO.class);
    }

    /**
     * 解析模板id
     *
     * @param modelAssetIds 模板id字符
     * @return 模板id
     */
    private List<Long> getTemplateIds(String modelAssetIds) {
        return Arrays.stream(modelAssetIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
    }

    /**
     * 创建工作流出数
     *
     * @param i 管理记录
     */
    private void createAssetsFlow(TgFlowProcessManagement i) {
        // 工作流出数
        AssetsFlowBatchCreateRequest request = new AssetsFlowBatchCreateRequest();
        String name = getName(i, "assetsFlow-");
        request.setName(name);
        request.setRemark(buildPeriodName(i));
        request.setBizType(i.getBizType());
        // 模板 资产id
        List<Long> templateIds = getTemplateIds(i.getTemplateIds());

        // 转换成模板id
        List<TgAssetInfo> infoList = assetInfoMapper.selectList(new QueryWrapper<TgAssetInfo>().lambda()
                .select(TgAssetInfo::getId, TgAssetInfo::getRelatedId).in(TgAssetInfo::getId, templateIds));
        List<Long> ids = Lambda.buildList(infoList, TgAssetInfo::getRelatedId);
        request.setTemplateIds(ids);

        FlowAssetsPageRequest flowAssetsPageRequest = new FlowAssetsPageRequest();
        flowAssetsPageRequest.setTemplateIds(ids);
        List<FlowAssetsPageDTO> pages = userDataAssetsService.listForCreate(flowAssetsPageRequest);
        if (!CollectionUtils.isEmpty(pages)) {
            request.setApplyIds(pages.stream().map(FlowAssetsPageDTO::getApplyId).collect(Collectors.toList()));
        } else {
            throw new CustomException("无对应申请记录，无法发起工作流推数");
        }

        // 将所有工作流 延迟10分钟 执行，预留时间给GP做中间表处理
        LocalDateTime expectTime = LocalDateTime.ofInstant(i.getPlanExecutionTime().toInstant(), ZoneId.systemDefault());
        request.setExpectTime(expectTime.plusMinutes(10));
        request.setNeedQc(Objects.equals(i.getQcState(), FlowProcessStateEnum.WAIT.getCode()));
//        request.setPeriod(i.getPeriod());
//        request.setFlowProcessType(i.getVersionCategory());
        request.setBizId(i.getId());
        // 根据当前周期获取可执行的申请单时间类型
        request.setDeliverTimeTypes(dataPlanService.queryDeliverTimeType(i.getBizType()));

        AjaxResult<Void> result = assetsFlowService.createBatch(request);
        if (!result.isSuccess()) {
            throw new CustomException(result.getMsg());
        }
    }

    private boolean isNotRunningOrWait(String state) {
        return !Objects.equals(state, FlowProcessStateEnum.RUNNING.getCode()) && !Objects.equals(state, FlowProcessStateEnum.WAIT.getCode());
    }

    /**
     * 同步底表
     *
     * @param detailId 详细信息编号
     * @param attach   附加信息
     * @param flowId   尚书台工作流id
     */
    private String executeWorkFlowForSyncSku(Long detailId, FlowProcessAttachVO attach, Integer flowId) {
        if (Objects.isNull(flowId)) {
            throw new CustomException("尚书台配置为空字符串");
        }

        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        parameters.add("callbackUrl", Optional.ofNullable(appProperties.getDolphinCallHost())
                .map(v -> v + "/tg-easy-fetch/api/flow_process/callback?bizId=" + detailId
                        + "&category=" + FlowProcessTaskEnum.SYNC.getCode()).orElse(""));
        parameters.add("failureStrategy", "CONTINUE");
        parameters.add("processDefinitionId", flowId);
        parameters.add("runMode", "RUN_MODE_SERIAL");
        parameters.add("processInstancePriority", "MEDIUM");
        parameters.add("workerGroup", "default");
        parameters.add("taskDependType", null);
        parameters.add("execType", "");
        parameters.add("warningType", "NONE");
        parameters.add("warningGroupId", 0);

        // 任务串行化执行
        parameters.add("inSerialMode", true);
        parameters.add("timeout", DsConstants.MAX_TASK_TIMEOUT);

        // 工作流启动参数
        Map<String, Object> params = new HashMap<>();
        String prefixSql;
        String conditionSql;
        TgFlowProcessSettingDetail detail = attach.getDetail();
        // 有配置控制
        if (Objects.equals(detail.getUpdateType(), FlowProcessUpdateType.ALL.getCode()) || CollectionUtils.isEmpty(attach.getProdCodes())) {
            params.put("partition_sync", "false");
            prefixSql = "select 1";
            conditionSql = " 1=1 ";
        } else {
            params.put("partition_sync", "true");
            StringBuilder prefixSb = new StringBuilder("alter table cmh_fd_data_sku_local on cluster default_cluster ");
            StringBuilder conditionSb = new StringBuilder();
            for (String prodCode : attach.getProdCodes()) {
                prefixSb.append(" drop partition '").append(prodCode).append("',");
                conditionSb.append(" prodcode = '").append(prodCode).append("'  or ");
            }
            prefixSb.setLength(prefixSb.length() - 1);
            conditionSb.setLength(conditionSb.length() - 4);
            prefixSql = prefixSb.toString();
            conditionSql = conditionSb.toString();
        }

        params.put("ysg_host", Optional.ofNullable(appProperties.getDolphinCallHost()).orElse(""));
        // 查询限制sql
        params.put("conditionSql", conditionSql);
        // 执行flow前置sql语句【自定义】
        params.put("prefixSql", prefixSql);
        parameters.add("startParams", JSON.toJSONString(params));
        AjaxResult ajaxResult = intergrateProcessDefService.execProcessInstance(parameters);
        if (ajaxResult.isDolphinSuccess()) {
            Object data = ajaxResult.getData();
            if (Objects.isNull(data)) {
                log.warn("未返回实例id: ajaxResult={}", ajaxResult);
            } else {
                log.info("发起成功：{}", ajaxResult);
                return ajaxResult.getData().toString();
            }
        } else if (Objects.equals(ajaxResult.getCode(), 50004)) {
            // PROCESS_DEFINE_NOT_RELEASE 工作流下线情况
            AjaxResult processResult = intergrateProcessDefService.queryProcessById(flowId);
            String name;
            if (processResult.isDolphinSuccess()) {
                Map<String, Object> resultMap = (Map<String, Object>) processResult.getData();
                name = Optional.ofNullable(resultMap.get("name")).map(Object::toString).orElse(null);
            } else {
                name = "[" + flowId + "]";
            }
            throw new CustomException("当前工作流【" + name + "】已下线无法执行，请到尚书台上线后执行");
        }
        if (!ajaxResult.isDolphinSuccess()) {
            log.error("exe result={}", ajaxResult);
            throw new CustomException(ajaxResult.getMsg());
        }
        return null;
    }

    /**
     * 获取工作流任务状态
     *
     * @param bizIds 关联编号
     * @return 工作流状态
     */
    private Map<Long, AssetsUpgradeStateEnum> qryWorkFlowState(List<Long> bizIds) {
        Map<Long, AssetsUpgradeStateEnum> r = new HashMap<>();
        List<AssetsFlowBatchVO> batchs = getWorkFlow(bizIds);
        if (!CollectionUtils.isEmpty(batchs)) {
            for (AssetsFlowBatchVO batch : batchs) {
                r.put(batch.getBizId(), AssetsUpgradeStateEnum.match(batch.getState()));
            }
        }
        return r;
    }

    /**
     * 获取工作流任务信息
     *
     * @param bizIds 关联编号
     * @return 工作流信息
     */
    private List<AssetsFlowBatchVO> getWorkFlow(List<Long> bizIds) {
        return assetsFlowService.queryByBizIds(bizIds);
    }

    /**
     * 获取资产升级状态
     *
     * @param bizIds 关联编号
     * @return 资产升级状态
     */
    private Map<Long, Boolean> qryAssetsUpdateState(List<Long> bizIds) {
        Map<Long, Boolean> r = new HashMap<>();
        List<TableInfoSnapshot> list = tableInfoSnapshotDAO.lambdaQuery().in(TableInfoSnapshot::getBizId, bizIds).list();
        if (!CollectionUtils.isEmpty(list)) {
            for (TableInfoSnapshot tableInfoSnapshot : list) {
                if (TablePushStatusEnum.END.contains(tableInfoSnapshot.getPushStatus())) {
                    r.put(tableInfoSnapshot.getBizId(),
                            TablePushStatusEnum.success.name().equals(tableInfoSnapshot.getPushStatus()));
                }
            }
        }
//        List<AssetsWideUpgradeTrigger> triggers = qryAssetsUpdate(bizIds);
//        if (!CollectionUtils.isEmpty(triggers)) {
//            Map<Long, List<AssetsWideUpgradeTrigger>> triggerMap = triggers.stream().collect(Collectors.groupingBy(AssetsWideUpgradeTrigger::getBizId));
//            for (Map.Entry<Long, List<AssetsWideUpgradeTrigger>> entry : triggerMap.entrySet()) {
//                Long key = entry.getKey();
//                List<AssetsWideUpgradeTrigger> value = entry.getValue();
//                boolean allMatch = value.stream().allMatch(i -> {
//                    AssetsUpgradeStateEnum state = AssetsUpgradeStateEnum.match(i.getState());
//                    return AssetsUpgradeStateEnum.success.equals(state) || AssetsUpgradeStateEnum.failed.equals(state);
//                });
//                if (allMatch) {
//                    r.put(key, value.stream().allMatch(i -> AssetsUpgradeStateEnum.success.equals(AssetsUpgradeStateEnum.match(i.getState()))));
//                }
//            }
//        }
        return r;
    }

    /**
     * 获取资产升级信息
     *
     * @param bizIds 关联编号
     * @return 资产升级信息
     */
    private List<AssetsWideUpgradeTrigger> qryAssetsUpdate(List<Long> bizIds) {
        return tableInfoSnapshotService.queryByBizIds(bizIds);
    }

    /**
     * 处理资产升级任务
     *
     * @param manage            管理任务
     * @param assetsUpdateState 资产升级状态
     * @param compareMap        任务信息
     * @param updateWrapper     更新信息
     */
    private void handleAssetsUpdate(TgFlowProcessManagement manage, Boolean assetsUpdateState,
                                    Map<Long, List<AssetsCompare>> compareMap,
                                    LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper) {
        if (Objects.nonNull(assetsUpdateState)) {
            updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
            if (assetsUpdateState) {
                updateWrapper.set(TgFlowProcessManagement::getSyncState, FlowProcessStateEnum.SUCCESS.getCode());
                updateWrapper.set(TgFlowProcessManagement::getAssetsUpdateState, FlowProcessStateEnum.SUCCESS.getCode());

                // 可以开启库表比对
                if (Objects.equals(manage.getTableDataCompareState(), FlowProcessStateEnum.WAIT.getCode())) {
                    startTableDataCompare(manage, updateWrapper);
                }

                // 资产数据对比
                invokeAssetsCompare(manage, compareMap, updateWrapper);
            } else {
                tgFlowProcessErrorLogDAO.save(manage.getId(), FlowProcessTaskEnum.ASSETS_UPDATE.getDesc(), "无对应任务");
                updateWrapper.set(TgFlowProcessManagement::getSyncState, FlowProcessStateEnum.FAILED.getCode());
                updateWrapper.set(TgFlowProcessManagement::getAssetsUpdateState, FlowProcessStateEnum.FAILED.getCode());
                updateWrapper.set(TgFlowProcessManagement::getQcState, FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                updateWrapper.set(TgFlowProcessManagement::getTableDataCompareState, FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                updateWrapper.set(TgFlowProcessManagement::getPlanCompareState, FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                updateWrapper.set(TgFlowProcessManagement::getPushPowerBiState, FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
//                tgFlowProcessAlertFacade.sendSubMsg(manage, FlowProcessTaskEnum.SYNC.getCode(), false);
            }
        }
    }

    private void invokeAssetsCompare(TgFlowProcessManagement manage, Map<Long, List<AssetsCompare>> compareMap,
                                     LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper) {
        if (!Objects.equals(manage.getPlanCompareState(), FlowProcessStateEnum.WAIT.getCode())) {
            return;
        }

        if (Objects.nonNull(manage.getPlanCompareBizId()) && compareMap.containsKey(manage.getId())) {
            updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
            try {
//                invokeCompare(compareMap.get(manage.getId()));
                // 资产升级会自动数据对比，后续做交互即可
                updateWrapper.set(TgFlowProcessManagement::getPlanCompareState, FlowProcessStateEnum.RUNNING.getCode());
            } catch (Exception e) {
                tgFlowProcessErrorLogDAO.save(manage.getId(), FlowProcessTaskEnum.PLAN_COMPARE.getDesc(), e.getMessage());
                updateWrapper.set(TgFlowProcessManagement::getPlanCompareState, FlowProcessStateEnum.FAILED.getCode());
                tgFlowProcessAlertFacade.sendSubMsg(manage, FlowProcessTaskEnum.PLAN_COMPARE.getCode(), false);
            }
        } else {
            updateWrapper.set(TgFlowProcessManagement::getPlanCompareState, FlowProcessStateEnum.FAILED.getCode());
            tgFlowProcessAlertFacade.sendSubMsg(manage, FlowProcessTaskEnum.PLAN_COMPARE.getCode(), false);
        }
    }

    /**
     * 发起powerBi推数任务
     *
     * @param i 管理任务
     */
    private void createPowerBi(TgFlowProcessManagement i) {
        // 发起powerbi
        PowerBiPushCreateRequest request = new PowerBiPushCreateRequest();
        // 统一处理点
        String name = getName(i, "powerBi-");
        request.setName(name);
        // 需要工作流推数 & 资产升级的资产
        List<AssetsFlowBatchVO> workFlows = getWorkFlow(Collections.singletonList(i.getId()));
        List<AssetsWideUpgradeTrigger> triggers = qryAssetsUpdate(Collections.singletonList(i.getId()));
        Set<Long> assertsIds = new HashSet<>();
        if (!CollectionUtils.isEmpty(triggers)) {
            assertsIds.addAll(triggers.stream().map(AssetsWideUpgradeTrigger::getAssetsId).collect(Collectors.toSet()));
        }
        if (!CollectionUtils.isEmpty(workFlows)) {
            List<Long> applicationIds = workFlows.stream()
                    .flatMap(e -> e.getDetails().stream())
                    .map(AssetsFlowBatchDetailVO::getApplicationId).collect(Collectors.toList());

            List<TgApplicationInfo> applyList = applicationDAO.lambdaQuery()
                    .select(TgApplicationInfo::getAssetsId).in(TgApplicationInfo::getId, applicationIds).list();
            Set<Long> applyAssetsIds = Lambda.buildSet(applyList, TgApplicationInfo::getAssetsId);

            assertsIds.addAll(applyAssetsIds);
        }

        request.setAssetsIds(new ArrayList<>(assertsIds));
        request.setBizId(i.getId());
        AjaxResult<Void> r = powerBiPushService.createPush(request);
        if (!r.isSuccess()) {
            throw new CustomException(r.getMsg());
        }
    }

    /**
     * 开启资产升级
     *
     * @param tableId    底表id
     * @param management 流程信息
     */
    private void startPushTable(Long tableId, TgFlowProcessManagement management) {
        TableSnapshotPushRequest request = new TableSnapshotPushRequest();

        boolean wait = Objects.equals(management.getPlanCompareState(), FlowProcessStateEnum.WAIT.getCode());
        if (Objects.nonNull(management.getPlanCompareBizId()) && wait) {
            TgFlowProcessManagement pre = tgFlowProcessManagementService.queryById(management.getPlanCompareBizId());
            request.setPreVersion(pre.getTableSnapshotVersion());
        }
        if (FlowProcessTypeEnum.qc.name().equals(management.getVersionCategory())) {
            request.setSkipAssertsBaseVersionFilter(true);
        }
        request.setTableId(tableId);
        request.setRemark(buildPeriodName(management));
        request.setBizId(management.getId());
        request.setNeedCompare(wait);
        request.setProdCodes(getAttachVO(management).getProdCodes());
//                request.setVersionPeriod(management.getPeriod());
//        request.setFlowProcessType(management.getVersionCategory());
        request.setDeliverTimeTypes(dataPlanService.queryDeliverTimeType(management.getBizType()));

        AjaxResult<Void> result = tableInfoSnapshotService.pushTable(request);
        if (!result.isSuccess()) {
            throw new CustomException(result.getMsg());
        }
    }

    /**
     * 发起底表比对
     *
     * @param tableId      底表id
     * @param oldVersionId 旧版本id
     * @param bizId        关联id
     */
    private void startDiffCompare(Long tableId, Long oldVersionId, Long bizId) {
        TableDiffRequest request = new TableDiffRequest();
        request.setTableId(tableId);
        // 查询对应的版本【怎么查询】
        request.setOldVersionId(oldVersionId);
        request.setBizId(bizId);
        Optional<String> valueOption = keyValDictDAO.queryValue(KeyDictType.snapshotDiffKeyPrefix + tableId);
        if (valueOption.isPresent()) {
            int miniute;
            try {
                miniute = Integer.parseInt(valueOption.get());
                Date date = DateUtils.addMinutes(new Date(), miniute);
                request.setPlanExecuteTime(date);
            } catch (Exception e) {
                log.info("转换number异常{},{}", bizId, valueOption.get());
                // 抛出异常,只影响底表对比任务
                throw new CustomException("转换number异常");
            }
        }
        tableInfoSnapshotCompareFacade.handle(request);
    }

    /**
     * 处理等待状态
     *
     * @param query 查询结果
     */
    private void handleWaitState(List<TgFlowProcessManagement> query) {
        log.info("开始处理");
        // 修改状态 & 发起第一阶段任务
        Date now = new Date();
        query.forEach(i -> {
            LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TgFlowProcessManagement::getId, i.getId());
            updateWrapper.set(TgFlowProcessManagement::getState, FlowProcessStateEnum.RUNNING.getCode());
            updateWrapper.set(TgFlowProcessManagement::getUpdateTime, now);
            updateWrapper.set(TgFlowProcessManagement::getExecutionBeginTime, now);
            flowProcessManagementDAO.update(updateWrapper);
        });

        query.forEach(i -> {
            SecurityUtils.setLocalUserId(i.getCreator());
            LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TgFlowProcessManagement::getId, i.getId());
            // 开启宽表同步 & 工作流出数
            FlowProcessAttachVO attachVO = getAttachVO(i);
            try {
                TgAssetInfo assetInfo = assetDetail(getAttachVO(i).getBase().getTableAssetId());
                Long tableId = assetInfo.getRelatedId();
                TableInfo tableInfo = iTableInfoService.getById(tableId);
                if (Objects.isNull(tableInfo)) {
                    log.info("无对应表信息：{}", i.getId());
                    throw new CustomException("无对应表信息");
                }
                Optional<String> valueOption = keyValDictDAO.queryValue(KeyDictType.syncTableKeyPrefix + tableInfo.getTableNameDistributed());
                if (valueOption.isPresent()) {
                    int flowId;
                    try {
                        flowId = Integer.parseInt(valueOption.get());
                    } catch (Exception e) {
                        log.info("转换number异常{},{}", i.getId(), valueOption.get());
                        throw new CustomException("转换number异常");
                    }
                    log.info("发起底表同步");
                    String syncId = this.executeWorkFlowForSyncSku(i.getId(), attachVO, flowId);
                    i.setSyncId(syncId);
                    updateWrapper.set(TgFlowProcessManagement::getSyncState, FlowProcessStateEnum.RUNNING.getCode());
                } else {
                    log.info("无对应尚书台配置：id - {}，tableId - {}", i.getId(), tableId);
                    throw new CustomException("无对应尚书台配置");
                }
            } catch (Exception e) {
                tgFlowProcessErrorLogDAO.save(i.getId(), FlowProcessTaskEnum.SYNC.getDesc(), e.getMessage());
                log.info("发起底表同步失败：{}", i.getId());
                updateWrapper.set(TgFlowProcessManagement::getSyncState, FlowProcessStateEnum.FAILED.getCode());
                updateWrapper.set(TgFlowProcessManagement::getAssetsUpdateState, FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                updateWrapper.set(TgFlowProcessManagement::getTableDataCompareState, FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                updateWrapper.set(TgFlowProcessManagement::getQcState, FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                updateWrapper.set(TgFlowProcessManagement::getPlanCompareState, FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                updateWrapper.set(TgFlowProcessManagement::getPushPowerBiState, FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());

                // 这里不用改
                tgFlowProcessAlertFacade.sendSubMsg(i, FlowProcessTaskEnum.SYNC.getCode(), false);
            }

            try {
                log.info("创建工作流出数");
                // 创建工作流出数
                this.createAssetsFlow(i);
                // 调整到 com.sinohealth.system.biz.dataassets.service.impl.AssetsUpgradeTriggerServiceImpl.schedulerRunFlow
//                updateWrapper.set(TgFlowProcessManagement::getWorkFlowState, FlowProcessStateEnum.RUNNING.getCode());
            } catch (Exception e) {
                tgFlowProcessErrorLogDAO.save(i.getId(), FlowProcessTaskEnum.WORK_FLOW.getDesc(), e.getMessage());
                log.info("创建工作流失败：{}", i.getId());
                // 异常了直接无效记录
                updateWrapper.set(TgFlowProcessManagement::getWorkFlowState, FlowProcessStateEnum.FAILED.getCode());
                updateWrapper.set(TgFlowProcessManagement::getPushPowerBiState, FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                // 这里不用改
                tgFlowProcessAlertFacade.sendSubMsg(i, FlowProcessTaskEnum.WORK_FLOW.getCode(), false);
            }
            updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
            flowProcessManagementDAO.update(updateWrapper);
            SecurityUtils.removeLocal();
        });
    }

    /**
     * 获取对应key的数值
     *
     * @param key redis key
     * @return 对应数值
     */
    private Long getRedisIncreNum(String key) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForValue().set(key, 0L, 1, TimeUnit.DAYS);
        }
        Object o = redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(o)) {
            Number number = (Number) o;
            long l = number.longValue() + 1;
            redisTemplate.opsForValue().set(key, l, 1, TimeUnit.DAYS);
            return l;
        }
        return 1L;
    }

    /**
     * 获取对应任务名称
     *
     * @param i         管理任务信息
     * @param keyPrefix key前缀
     * @return 任务名称
     */
    private String getName(TgFlowProcessManagement i, String keyPrefix) {
        if (Objects.isNull(keyPrefix)) {
            keyPrefix = "";
        }
        String prefix = "自动触发-" + i.getPeriod() + "-" + FlowProcessTypeEnum.getDescByName(i.getVersionCategory()) + "-" + DateUtils.getDate();
        Long redisIncreNum = getRedisIncreNum(keyPrefix + prefix);
        return prefix + (redisIncreNum < 10 ? "0" + redisIncreNum : String.valueOf(redisIncreNum));
    }

    /**
     * 比较是否同一时刻【只比较时分】
     *
     * @param timeString 时分秒字符
     * @return 是否同一时刻【只比较时分】
     */
    private boolean matchTime(String timeString) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime time = LocalTime.parse(timeString, timeFormatter);
        LocalTime now = LocalTime.now();
        return Objects.equals(time.getHour(), now.getHour()) && Objects.equals(time.getMinute(), now.getMinute());
    }

    /**
     * 发起底表对比
     *
     * @param i             管理记录
     * @param updateWrapper 更新信息
     */
    private void startTableDataCompare(TgFlowProcessManagement i, LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper) {
        try {
            if (Objects.nonNull(i.getTableDataCompareBizId())) {
                TgAssetInfo assetInfo = assetDetail(getAttachVO(i).getBase().getTableAssetId());
                Long tableId = assetInfo.getRelatedId();

                TgFlowProcessManagement pre = tgFlowProcessManagementService.queryById(i.getTableDataCompareBizId());
                // 发起库表版本比对
                startDiffCompare(tableId, pre.getTableSnapshotId(), i.getId());
                updateWrapper.set(TgFlowProcessManagement::getTableDataCompareState, FlowProcessStateEnum.RUNNING.getCode());
            } else {
                updateWrapper.set(TgFlowProcessManagement::getTableDataCompareState, FlowProcessStateEnum.FAILED.getCode());
            }
        } catch (Exception e) {
            tgFlowProcessErrorLogDAO.save(i.getId(), FlowProcessTaskEnum.TABLE_DATA_COMPARE.getDesc(), e.getMessage());
            updateWrapper.set(TgFlowProcessManagement::getTableDataCompareState, FlowProcessStateEnum.FAILED.getCode());
            tgFlowProcessAlertFacade.sendSubMsg(i, FlowProcessTaskEnum.TABLE_DATA_COMPARE.getCode(), false);
        }
    }

    /**
     * 调用数据对比
     *
     * @param compareList 比对记录
     */
    private void invokeCompare(List<AssetsCompare> compareList) {
        if (CollectionUtils.isEmpty(compareList)) {
            throw new CustomException("数据对比任务为空");
        }
        Set<Long> assetsIds = Lambda.buildSet(compareList, AssetsCompare::getAssetsId, Objects::nonNull);
        if (CollectionUtils.isEmpty(assetsIds)) {
            throw new CustomException("数据对比任务资产id为空");
        }
        List<UserDataAssets> assets = userDataAssetsDAO.lambdaQuery()
                .select(UserDataAssets::getId, UserDataAssets::getProjectName, UserDataAssets::getVersion,
                        UserDataAssets::getFtpStatus, UserDataAssets::getFtpPath)
                .in(UserDataAssets::getId, assetsIds).list();
        Map<Long, UserDataAssets> assetsMap = Lambda.buildMap(assets, UserDataAssets::getId);
        Map<Long, String> nameMap = Lambda.buildMap(assets, UserDataAssets::getId, UserDataAssets::getProjectName);

        for (AssetsCompare compare : compareList) {
            UserDataAssets latest = assetsMap.get(compare.getAssetsId());
            UserDataAssets cur;
            if (Objects.equals(latest.getVersion(), compare.getCurVersion())) {
                cur = latest;
            } else {
                cur = userDataAssetsSnapshotDAO.queryByAssetsId(compare.getAssetsId(), compare.getCurVersion());
            }
            UserDataAssets pre = userDataAssetsSnapshotDAO.queryByAssetsId(compare.getAssetsId(), compare.getPreVersion());
            if (!cur.hasValidFtp() || !pre.hasValidFtp()) {
                log.error("存在资产的Excel尚未上传完，请稍后再试 compare:{}", compare);
                alertService.sendDevNormalMsg("存在资产的Excel尚未上传完，触发对比失败 " + compare.getId() + " " + cur.getProjectName());
                continue;
            }

            AssetsCompareInvokeRequest req = AssetsCompareInvokeRequest.builder().compareId(compare.getId())
                    .assetsId(compare.getAssetsId()).projectName(nameMap.get(compare.getAssetsId()))
                    .newPath(cur.getFtpPath()).oldPath(pre.getFtpPath())
                    .callbackUrl(appProperties.getAssetsCompareSelfUrl()).build();
            assetsCompareInvoker.invokeCompareReq(req);
        }
    }

    /**
     * 构建周期名称
     *
     * @param management 管理任务信息
     * @return 周期名称
     */
    private String buildPeriodName(TgFlowProcessManagement management) {
        return management.getPeriod() + "-" + FlowProcessTypeEnum.getDescByName(management.getVersionCategory());
    }

    /**
     * 处理最终状态
     *
     * @param management    管理记录
     * @param updateWrapper 更新信息
     * @param haveChange    是否有变化
     * @return 是否有变化
     */
    private boolean handleFinish(TgFlowProcessManagement management, LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper, boolean haveChange) {
        // 成功的判断
        Boolean syncFlag = Objects.equals(management.getSyncState(), FlowProcessStateEnum.SUCCESS.getCode());
        Boolean assetsUpdateFlag = Objects.equals(management.getAssetsUpdateState(), FlowProcessStateEnum.SUCCESS.getCode());
        Boolean workFlowFlag = Objects.equals(management.getWorkFlowState(), FlowProcessStateEnum.SUCCESS.getCode()) || Objects.equals(management.getWorkFlowState(), FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
        Boolean tableDataCompareFlag = Objects.equals(management.getTableDataCompareState(), FlowProcessStateEnum.SUCCESS.getCode()) || Objects.equals(management.getTableDataCompareState(), FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
        Boolean qcFlag = Objects.equals(management.getQcState(), FlowProcessStateEnum.SUCCESS.getCode()) || Objects.equals(management.getQcState(), FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
        Boolean planCompareFlag = Objects.equals(management.getPlanCompareState(), FlowProcessStateEnum.SUCCESS.getCode()) || Objects.equals(management.getPlanCompareState(), FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
        Boolean powerBiFlag = Objects.equals(management.getPushPowerBiState(), FlowProcessStateEnum.SUCCESS.getCode()) || Objects.equals(management.getPushPowerBiState(), FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
        if (syncFlag && workFlowFlag && assetsUpdateFlag && tableDataCompareFlag && qcFlag && planCompareFlag && powerBiFlag) {
            updateWrapper.set(TgFlowProcessManagement::getState, FlowProcessStateEnum.SUCCESS.getCode());
            updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
            updateWrapper.set(TgFlowProcessManagement::getExecutionFinishTime, new Date());
            tgFlowProcessAlertFacade.sendFlowProcessMsg(management, management.getVersionCategory(), true);
            haveChange = true;
        }

        // 失败的判断
        Boolean syncFailFlag = Objects.equals(management.getSyncState(), FlowProcessStateEnum.FAILED.getCode());
        Boolean assetsUpdateFailFlag = Objects.equals(management.getAssetsUpdateState(), FlowProcessStateEnum.FAILED.getCode());
        Boolean workFlowFailFlag = Objects.equals(management.getWorkFlowState(), FlowProcessStateEnum.FAILED.getCode());
        Boolean tableDataCompareFailFlag = Objects.equals(management.getTableDataCompareState(), FlowProcessStateEnum.FAILED.getCode());
        Boolean qcFailFlag = Objects.equals(management.getQcState(), FlowProcessStateEnum.FAILED.getCode());
        Boolean planCompareFailFlag = Objects.equals(management.getPlanCompareState(), FlowProcessStateEnum.FAILED.getCode());
        Boolean powerBiFailFlag = Objects.equals(management.getPushPowerBiState(), FlowProcessStateEnum.FAILED.getCode());
        if (syncFailFlag || assetsUpdateFailFlag || workFlowFailFlag || tableDataCompareFailFlag || qcFailFlag || planCompareFailFlag || powerBiFailFlag) {
            if (isNotRunningOrWait(management.getSyncState()) && isNotRunningOrWait(management.getWorkFlowState()) && isNotRunningOrWait(management.getAssetsUpdateState()) && isNotRunningOrWait(management.getQcState()) && isNotRunningOrWait(management.getTableDataCompareState()) && isNotRunningOrWait(management.getPlanCompareState()) && isNotRunningOrWait(management.getPushPowerBiState())) {
                updateWrapper.set(TgFlowProcessManagement::getState, FlowProcessStateEnum.FAILED.getCode());
                updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
                updateWrapper.set(TgFlowProcessManagement::getExecutionFinishTime, new Date());
                tgFlowProcessAlertFacade.sendFlowProcessMsg(management, management.getVersionCategory(), false);
                haveChange = true;
            }
        }
        return haveChange;
    }

    /**
     * 处理底表对比任务
     *
     * @param dataCompareMap 底表对比
     * @param management     管理记录
     * @param updateWrapper  更新信息
     * @param haveChange     是否有变化
     * @return 是否有变化
     */
    private boolean handleTableDataCompare(Map<Long, TgTableInfoSnapshotCompare> dataCompareMap, TgFlowProcessManagement management, LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper, boolean haveChange) {
        if (Objects.equals(management.getTableDataCompareState(), FlowProcessStateEnum.RUNNING.getCode())) {
            log.info("开始处理底表对比任务状态：{}", management.getId());
            if (dataCompareMap.containsKey(management.getId())) {
                TgTableInfoSnapshotCompare tgTableInfoSnapshotCompare = dataCompareMap.get(management.getId());
                if (Objects.equals(TableInfoSnapshotCompareState.FAIL.getType(), tgTableInfoSnapshotCompare.getState())) {
                    tgFlowProcessErrorLogDAO.save(management.getId(), FlowProcessTaskEnum.TABLE_DATA_COMPARE.getDesc(), "执行失败");
                    updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
                    updateWrapper.set(TgFlowProcessManagement::getTableDataCompareState, FlowProcessStateEnum.FAILED.getCode());
                    haveChange = true;
                } else if (Objects.equals(TableInfoSnapshotCompareState.COMPLETED.getType(), tgTableInfoSnapshotCompare.getState())) {
                    updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
                    updateWrapper.set(TgFlowProcessManagement::getTableDataCompareState, FlowProcessStateEnum.SUCCESS.getCode());
                    haveChange = true;
                }
            } else {
                tgFlowProcessErrorLogDAO.save(management.getId(), FlowProcessTaskEnum.TABLE_DATA_COMPARE.getDesc(), "无对应任务");
                log.info("无对应底表对比任务：{}", management.getId());
                updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
                updateWrapper.set(TgFlowProcessManagement::getTableDataCompareState, FlowProcessStateEnum.FAILED.getCode());
                tgFlowProcessAlertFacade.sendSubMsg(management, FlowProcessTaskEnum.TABLE_DATA_COMPARE.getCode(), false);
                haveChange = true;
            }
        }
        return haveChange;
    }

    /**
     * 处理powerbi任务
     *
     * @param powerBiStateMap powerbi任务状态
     * @param management      管理记录
     * @param updateWrapper   更新信息
     * @param haveChange      是否有变化
     * @return 是否有变化
     */
    private boolean handlePowerBi(Map<Long, String> powerBiStateMap, TgFlowProcessManagement management, LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper, boolean haveChange) {
        if (Objects.equals(management.getPushPowerBiState(), FlowProcessStateEnum.RUNNING.getCode())) {
            log.info("开始处理 powerbi {}", management.getId());
            if (powerBiStateMap.containsKey(management.getId())) {
                String state = powerBiStateMap.get(management.getId());
                if (Objects.equals(AssetsUpgradeStateEnum.success.name(), state)) {
                    haveChange = true;
                    updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
                    updateWrapper.set(TgFlowProcessManagement::getPushPowerBiState, FlowProcessStateEnum.SUCCESS.getCode());
                } else if (Objects.equals(AssetsUpgradeStateEnum.failed.name(), state)) {
                    haveChange = true;
                    tgFlowProcessErrorLogDAO.save(management.getId(), FlowProcessTaskEnum.PUSH_POWER_BI.getDesc(), "执行失败");
                    updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
                    updateWrapper.set(TgFlowProcessManagement::getPushPowerBiState, FlowProcessStateEnum.FAILED.getCode());
                }
            } else {
                haveChange = true;
                tgFlowProcessErrorLogDAO.save(management.getId(), FlowProcessTaskEnum.PUSH_POWER_BI.getDesc(), "无对应任务");
                log.info("无对应powerBi任务：{}", management.getId());
                updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
                updateWrapper.set(TgFlowProcessManagement::getPushPowerBiState, FlowProcessStateEnum.FAILED.getCode());
                tgFlowProcessAlertFacade.sendSubMsg(management, FlowProcessTaskEnum.PUSH_POWER_BI.getCode(), false);
            }
        }
        return haveChange;
    }

    /**
     * 处理数据对比任务
     *
     * @param runningCompareMap 数据对比任务信息
     * @param management        管理记录
     * @param updateWrapper     更新信息
     * @param haveChange        是否有变化
     * @return 是否有变化
     */
    private boolean handlePlanCompare(Map<Long, List<AssetsCompare>> runningCompareMap, TgFlowProcessManagement management, LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper, boolean haveChange) {
        if (Objects.equals(management.getPlanCompareState(), FlowProcessStateEnum.RUNNING.getCode())) {
            log.info("处理数据对比任务：{}", management.getId());
            if (runningCompareMap.containsKey(management.getId())) {
                List<AssetsCompare> compareList = runningCompareMap.get(management.getId());
                boolean allMatch = compareList.stream().allMatch(i -> {
                    AssetsUpgradeStateEnum state = AssetsUpgradeStateEnum.match(i.getState());
                    return AssetsUpgradeStateEnum.success.equals(state) || AssetsUpgradeStateEnum.failed.equals(state);
                });
                if (allMatch) {
                    updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
                    haveChange = true;
                    if (compareList.stream().allMatch(i -> AssetsUpgradeStateEnum.success.equals(AssetsUpgradeStateEnum.match(i.getState())))) {
                        updateWrapper.set(TgFlowProcessManagement::getPlanCompareState, FlowProcessStateEnum.SUCCESS.getCode());
                    } else {
                        tgFlowProcessErrorLogDAO.save(management.getId(), FlowProcessTaskEnum.PLAN_COMPARE.getDesc(), "执行失败");
                        updateWrapper.set(TgFlowProcessManagement::getPlanCompareState, FlowProcessStateEnum.FAILED.getCode());
                    }
                }
            } else {
                tgFlowProcessErrorLogDAO.save(management.getId(), FlowProcessTaskEnum.PLAN_COMPARE.getDesc(), "无对应任务");
                log.info("无对应数据对比任务：{}", management.getId());
                updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
                updateWrapper.set(TgFlowProcessManagement::getPlanCompareState, FlowProcessStateEnum.FAILED.getCode());
                management.setUpdateTime(new Date());
                haveChange = true;
            }
        }
        return haveChange;
    }

    /**
     * 处理qc任务
     *
     * @param qcDetailMap   qc任务信息
     * @param management    管理记录
     * @param updateWrapper 更新信息
     * @param haveChange    是否有变化
     * @return 是否有变化
     */
    private boolean handleQc(Map<Long, AssetsQcBatch> qcDetailMap, TgFlowProcessManagement management, LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper, boolean haveChange) {
        if (Objects.equals(management.getQcState(), FlowProcessStateEnum.WAIT.getCode())) {
            boolean preFailed = Objects.equals(management.getWorkFlowState(), FlowProcessStateEnum.FAILED.getCode())
                    || Objects.equals(management.getAssetsUpdateState(), FlowProcessStateEnum.FAILED.getCode());
            if (preFailed) {
                tgFlowProcessErrorLogDAO.save(management.getId(), FlowProcessTaskEnum.QC.getDesc(), "前置任务失败");
                log.info("前置任务失败 qc：{}", management.getId());
                updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
                updateWrapper.set(TgFlowProcessManagement::getQcState, FlowProcessStateEnum.FAILED.getCode());
                tgFlowProcessAlertFacade.sendSubMsg(management, FlowProcessTaskEnum.QC.getCode(), false);
                return true;
            }
        }

        if (Objects.equals(management.getQcState(), FlowProcessStateEnum.RUNNING.getCode())) {
            log.info("开始处理qc任务状态：{}", management.getId());
            if (qcDetailMap.containsKey(management.getId())) {
                AssetsQcBatch detail = qcDetailMap.get(management.getId());
                if (Objects.equals(detail.getState(), AssetsUpgradeStateEnum.success.name())) {
                    updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
                    updateWrapper.set(TgFlowProcessManagement::getQcState, FlowProcessStateEnum.SUCCESS.getCode());
                    haveChange = true;
                } else if (Objects.equals(detail.getState(), AssetsUpgradeStateEnum.failed.name())) {
                    tgFlowProcessErrorLogDAO.save(management.getId(), FlowProcessTaskEnum.QC.getDesc(), "执行失败");
                    updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
                    updateWrapper.set(TgFlowProcessManagement::getQcState, FlowProcessStateEnum.FAILED.getCode());
                    haveChange = true;
                }
            } else {
                tgFlowProcessErrorLogDAO.save(management.getId(), FlowProcessTaskEnum.QC.getDesc(), "无对应任务");
                log.info("无对应qc任务：{}", management.getId());
                updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
                updateWrapper.set(TgFlowProcessManagement::getQcState, FlowProcessStateEnum.FAILED.getCode());
                haveChange = true;
                tgFlowProcessAlertFacade.sendSubMsg(management, FlowProcessTaskEnum.QC.getCode(), false);
            }
        }
        return haveChange;
    }

    /**
     * 发起powerbi任务
     *
     * @param management    管理记录
     * @param updateWrapper 更新信息
     * @return 是否有信息发生变化
     */
    private boolean startPushPowerBi(TgFlowProcessManagement management, LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper) {
        boolean finishPre = Objects.equals(management.getWorkFlowState(), FlowProcessStateEnum.SUCCESS.getCode())
                && Objects.equals(management.getAssetsUpdateState(), FlowProcessStateEnum.SUCCESS.getCode());
        boolean needStart = finishPre && Objects.equals(management.getPushPowerBiState(), FlowProcessStateEnum.WAIT.getCode());
        if (!needStart) {
            return false;
        }

        log.info("发起powerbi任务：{}", management.getId());
        updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
        // 这里需要整合资产升级与工作流推数明细的【资产】作整合
        updateWrapper.set(TgFlowProcessManagement::getPushPowerBiState, FlowProcessStateEnum.RUNNING.getCode());
        try {
            SecurityUtils.setLocalUserId(management.getCreator());
            createPowerBi(management);
        } catch (Exception e) {
            tgFlowProcessErrorLogDAO.save(management.getId(), FlowProcessTaskEnum.PUSH_POWER_BI.getDesc(), e.getMessage());
            log.info("创建powerBi异常：{}", e.getMessage());
            updateWrapper.set(TgFlowProcessManagement::getPushPowerBiState, FlowProcessStateEnum.FAILED.getCode());
            tgFlowProcessAlertFacade.sendSubMsg(management, FlowProcessTaskEnum.PUSH_POWER_BI.getCode(), false);
        } finally {
            SecurityUtils.removeLocal();
        }
        return true;
    }

    private boolean startQc(TgFlowProcessManagement management, LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper) {
        // qc、数据对比、powerBI同步
        boolean finishPre = Objects.equals(management.getWorkFlowState(), FlowProcessStateEnum.SUCCESS.getCode())
                && Objects.equals(management.getAssetsUpdateState(), FlowProcessStateEnum.SUCCESS.getCode());
        boolean needStart = finishPre && Objects.equals(management.getQcState(), FlowProcessStateEnum.WAIT.getCode());
        if (!needStart) {
            return false;
        }

        // 资产升级会自动创建qc，后续做交互即可
        try {
            AjaxResult<Void> allQc = assetsQcService.createAllQc(management.getId());
            if (!allQc.isSuccess()) {
                throw new CustomException(allQc.getMsg());
            }
            updateWrapper.set(TgFlowProcessManagement::getQcState, FlowProcessStateEnum.RUNNING.getCode());
        } catch (Exception e) {
            log.info("创建qc失败，全流程id：{}，异常信息：{}", management.getId(), e.getMessage());
            tgFlowProcessErrorLogDAO.save(management.getId(), FlowProcessTaskEnum.QC.getDesc(), e.getMessage());
            updateWrapper.set(TgFlowProcessManagement::getQcState, FlowProcessStateEnum.FAILED.getCode());
            tgFlowProcessAlertFacade.sendSubMsg(management, FlowProcessTaskEnum.QC.getCode(), false);
        }

        return true;
    }


    /**
     * 处理资产升级任务
     *
     * @param compareMap           资产升级任务信息
     * @param assetsUpdateStateMap 资产升级任务状态
     * @param management           管理记录
     * @param updateWrapper        更新信息
     * @return 是否有变化
     */
    private boolean handleAssetsUpdate(Map<Long, List<AssetsCompare>> compareMap, Map<Long, Boolean> assetsUpdateStateMap, TgFlowProcessManagement management, LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper) {
        if (Objects.equals(management.getAssetsUpdateState(), FlowProcessStateEnum.RUNNING.getCode())) {
            Boolean assetsUpdateState = assetsUpdateStateMap.get(management.getId());
            if (Objects.nonNull(assetsUpdateState)) {
                log.info("开始处理资产升级任务状态：{}", management.getId());
                // 处理资产升级
                handleAssetsUpdate(management, assetsUpdateState, compareMap, updateWrapper);
                return true;
            }
        }
        return false;
    }

    /**
     * 处理工作流任务
     *
     * @param workFlowStateMap 工作流任务状态
     * @param management       管理记录
     * @param updateWrapper    更新信息
     * @return 是否字段变化
     */
    private boolean handleWorkFlow(Map<Long, AssetsUpgradeStateEnum> workFlowStateMap,
                                   TgFlowProcessManagement management,
                                   LambdaUpdateWrapper<TgFlowProcessManagement> updateWrapper) {
        // 判断工作流状态
        if (Objects.equals(management.getWorkFlowState(), FlowProcessStateEnum.RUNNING.getCode())) {
            AssetsUpgradeStateEnum workFlowState = workFlowStateMap.get(management.getId());
            // 处理工作流
            updateWrapper.set(TgFlowProcessManagement::getUpdateTime, new Date());
            if (Objects.nonNull(workFlowState)) {
                if (Objects.equals(AssetsUpgradeStateEnum.success, workFlowState)) {
                    updateWrapper.set(TgFlowProcessManagement::getWorkFlowState, FlowProcessStateEnum.SUCCESS.getCode());
                    return true;
                } else if (Objects.equals(AssetsUpgradeStateEnum.failed, workFlowState)) {
                    tgFlowProcessErrorLogDAO.save(management.getId(), FlowProcessTaskEnum.WORK_FLOW.getDesc(), "执行失败");
                    updateWrapper.set(TgFlowProcessManagement::getWorkFlowState, FlowProcessStateEnum.FAILED.getCode());
                    updateWrapper.set(TgFlowProcessManagement::getPushPowerBiState, FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                    return true;
                }
            } else {
                tgFlowProcessErrorLogDAO.save(management.getId(), FlowProcessTaskEnum.WORK_FLOW.getDesc(), "该任务无对应工作流任务信息");
                log.info("该任务无对应工作流任务信息：{}", management.getId());
                updateWrapper.set(TgFlowProcessManagement::getWorkFlowState, FlowProcessStateEnum.FAILED.getCode());
                updateWrapper.set(TgFlowProcessManagement::getPushPowerBiState, FlowProcessStateEnum.NOT_EXECUTION_REQUIRED.getCode());
                tgFlowProcessAlertFacade.sendSubMsg(management, FlowProcessTaskEnum.WORK_FLOW.getCode(), false);
                return true;
            }
        }
        return false;
    }

    /**
     * 构建底表对比任务map
     *
     * @param query 管理记录
     * @return 底表对比任务map
     */
    private Map<Long, TgTableInfoSnapshotCompare> buildDataCompareMap(List<TgFlowProcessManagement> query) {
        List<Long> compareBizIds = query.stream().filter(i -> Objects.equals(FlowProcessStateEnum.RUNNING.getCode(), i.getTableDataCompareState()))
                .map(TgFlowProcessManagement::getId).collect(Collectors.toList());
        Map<Long, TgTableInfoSnapshotCompare> dataCompareMap = Collections.emptyMap();
        if (!CollectionUtils.isEmpty(compareBizIds)) {
            List<TgTableInfoSnapshotCompare> compares = tableInfoSnapshotCompareFacade.findByBizIds(compareBizIds);
            dataCompareMap = compares.stream().collect(Collectors.toMap(TgTableInfoSnapshotCompare::getBizId, Function.identity(), (v1, v2) -> v2));
        }
        return dataCompareMap;
    }

    /**
     * 构建powerBi任务map
     *
     * @param query 管理记录
     * @return powerbi任务信息
     */
    private Map<Long, String> buildPowerBiStateMap(List<TgFlowProcessManagement> query) {
        List<Long> powerBiBizIds = query.stream().filter(i -> Objects.equals(FlowProcessStateEnum.RUNNING.getCode(), i.getPushPowerBiState()))
                .map(TgFlowProcessManagement::getId).collect(Collectors.toList());
        Map<Long, String> powerBiStateMap = Collections.emptyMap();
        if (!CollectionUtils.isEmpty(powerBiBizIds)) {
            List<PowerBiPushBatch> list = powerBiPushService.queryByBizIds(powerBiBizIds);
            powerBiStateMap = Optional.ofNullable(list).orElse(Collections.emptyList()).stream().collect(Collectors.toMap(PowerBiPushBatch::getBizId, PowerBiPushBatch::getState, (v1, v2) -> v2));
        }
        return powerBiStateMap;
    }

    /**
     * 构建数据对比任务map
     *
     * @param query 管理记录
     * @return 数据对比任务信息
     */
    private Map<Long, List<AssetsCompare>> buildPlanCompareDataMap(List<TgFlowProcessManagement> query) {
        List<Long> planCompareBizIds = query.stream().filter(i -> Objects.equals(FlowProcessStateEnum.RUNNING.getCode(), i.getPlanCompareState()))
                .map(TgFlowProcessManagement::getId).collect(Collectors.toList());
        Map<Long, List<AssetsCompare>> runningCompareMap = Collections.emptyMap();
        if (!CollectionUtils.isEmpty(planCompareBizIds)) {
            List<AssetsCompare> assetsCompares = assetsCompareService.queryByBizIds(planCompareBizIds);
            runningCompareMap = Optional.ofNullable(assetsCompares).orElse(Collections.emptyList()).stream().collect(Collectors.groupingBy(AssetsCompare::getBizId));
        }
        return runningCompareMap;
    }

    /**
     * 构建qc任务数据map
     *
     * @param query 管理记录
     * @return qc任务数据
     */
    private Map<Long, AssetsQcBatch> buildQcDataMap(List<TgFlowProcessManagement> query) {
        List<Long> qcBizIds = query.stream().filter(i -> Objects.equals(FlowProcessStateEnum.RUNNING.getCode(), i.getQcState())).map(TgFlowProcessManagement::getId).collect(Collectors.toList());
        Map<Long, AssetsQcBatch> qcDetailMap = Collections.emptyMap();
        if (!CollectionUtils.isEmpty(qcBizIds)) {
            List<AssetsQcBatch> assetsQcDetails = assetsQcService.queryBatchByBizIds(qcBizIds);
            qcDetailMap =
                    Optional.ofNullable(assetsQcDetails).orElse(Collections.emptyList()).stream().collect(Collectors.toMap(AssetsQcBatch::getBizId, Function.identity(), (v1, v2) -> v2));
        }
        return qcDetailMap;
    }

    /**
     * 查询工作流任务状态
     *
     * @param query 管理记录
     * @return 工作流任务状态map
     */
    private Map<Long, AssetsUpgradeStateEnum> buildWorkFlowStateMap(List<TgFlowProcessManagement> query) {
        List<Long> workFlowBizIds = query.stream()
                .filter(i -> Objects.equals(FlowProcessStateEnum.RUNNING.getCode(), i.getWorkFlowState()))
                .map(TgFlowProcessManagement::getId).collect(Collectors.toList());

        Map<Long, AssetsUpgradeStateEnum> workFlowStateMap;
        if (!CollectionUtils.isEmpty(workFlowBizIds)) {
            // 进行中的工作流推数任务
            log.info("工作流推数任务 Flow {}", workFlowBizIds);
            // 工作流推数信息
            workFlowStateMap = qryWorkFlowState(workFlowBizIds);
        } else {
            workFlowStateMap = Collections.emptyMap();
        }
        return workFlowStateMap;
    }

    /**
     * 获取期数对应的交付周期
     *
     * @param period 期数
     * @return 交付周期
     */
    private List<String> getDeliverTimeType(String period) {
        if (StringUtils.isNotBlank(period)) {
            try {
                ArrayList<String> deliverTimeTypes = new ArrayList<>();
                // 月度的每月都要执行
                deliverTimeTypes.add(DeliverTimeTypeEnum.month.getDesc());

                // 解析对应的格式【202409】
                YearMonth yearMonth = YearMonth.parse(period, DataPlanUtil.YM);
                Month month = yearMonth.getMonth();
                if (Month.MARCH.equals(month) || Month.JUNE.equals(month) || Month.SEPTEMBER.equals(month) || Month.DECEMBER.equals(month)) {
                    // 季度
                    deliverTimeTypes.add(DeliverTimeTypeEnum.quarter.getDesc());
                }
                if (Month.JUNE.equals(month) || Month.DECEMBER.equals(month)) {
                    // 半年度
                    deliverTimeTypes.add(DeliverTimeTypeEnum.halfAYear.getDesc());
                }
                if (Month.DECEMBER.equals(month)) {
                    // 年度
                    deliverTimeTypes.add(DeliverTimeTypeEnum.year.getDesc());
                }
                return deliverTimeTypes;
            } catch (Exception e) {
                log.info(e.getMessage());
                log.info("全流程当前周期转换异常");
            }
        }
        return Collections.emptyList();
    }

}
