package com.sinohealth.system.biz.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.dict.BizTypeEnum;
import com.sinohealth.common.enums.dict.DeliverTimeTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.bean.BeanUtils;
import com.sinohealth.common.utils.bean.PageUtil;
import com.sinohealth.system.biz.alert.service.AlertService;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.constant.FlowProcessTypeEnum;
import com.sinohealth.system.biz.project.dao.DataPlanDAO;
import com.sinohealth.system.biz.project.domain.DataPlan;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.dto.DataPlanBizDTO;
import com.sinohealth.system.biz.project.dto.DataPlanDetailPageDTO;
import com.sinohealth.system.biz.project.dto.request.BizTypePlanVo;
import com.sinohealth.system.biz.project.dto.request.DataPlanDetailPageRequest;
import com.sinohealth.system.biz.project.dto.request.DataPlanDetailUpdateRequest;
import com.sinohealth.system.biz.project.dto.request.DataPlanPageRequest;
import com.sinohealth.system.biz.project.dto.request.DataPlanYearRequest;
import com.sinohealth.system.biz.project.service.DataPlanService;
import com.sinohealth.system.biz.project.util.DataPlanUtil;
import com.sinohealth.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Kuangcp
 * 2024-12-13 14:26
 */
@Slf4j
@Service
public class DataPlanServiceImpl implements DataPlanService {

    static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Autowired
    private DataPlanDAO dataPlanDAO;

    @Autowired
    private AlertService alertService;
    @Autowired
    private ISysUserService userService;

    @Override
    public AjaxResult<CurrentDataPlanDTO> curPeriod(String bizType) {
        if (StringUtils.isBlank(bizType)) {
            return AjaxResult.error("参数缺失");
        }
        if (Objects.equals(BizTypeEnum.main_data.name(), bizType)) {
            CurrentDataPlanDTO dto = new CurrentDataPlanDTO();
            dto.setPeriod("");
            dto.setFlowProcessType(null);
            return AjaxResult.success(dto);
        }

        List<DataPlan> list = dataPlanDAO.lambdaQuery()
                .eq(DataPlan::getBizType, bizType)
                .eq(DataPlan::getDay, LocalDate.now())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            alertService.sendDevNormalMsg(bizType + "的排期未初始化！");
            return AjaxResult.error("初始化异常");
        }

        if (CollectionUtils.size(list) != 1) {
            alertService.sendDevNormalMsg(bizType + "的排期出现数据重复！");
            return AjaxResult.error("初始化异常");
        }
        DataPlan plan = list.get(0);
        CurrentDataPlanDTO dto = new CurrentDataPlanDTO();
        dto.setPeriod(plan.getPeriod());
        dto.setFlowProcessType(plan.getFlowProcessType());
        return AjaxResult.success(dto);
    }

    @Override
    public CurrentDataPlanDTO currentPeriod(String bizType) {
        AjaxResult<CurrentDataPlanDTO> result = this.curPeriod(bizType);
        if (!result.isSuccess()) {
            throw new RuntimeException(result.getMsg());
        }
        return Optional.ofNullable(result.getData()).orElseThrow(() -> new RuntimeException("初始化异常"));
    }

    /**
     * 查询当前周期对应的需要出数资产类型
     */
    @Override
    public List<String> queryDeliverTimeType(String bizType) {
        AjaxResult<CurrentDataPlanDTO> result = this.curPeriod(bizType);
        if (!result.isSuccess()) {
            log.error("error period plan: bizType={}", bizType);
            return Collections.emptyList();
        }

        CurrentDataPlanDTO data = result.getData();
        String period = data.getPeriod();
        return parsePeriod(period, false);
    }

    /**
     * 临近的下一期支持的时间类型
     */
    @Override
    public List<String> queryNextDeliverTimeType(String bizType) {
        AjaxResult<CurrentDataPlanDTO> result = this.curPeriod(bizType);
        if (!result.isSuccess()) {
            log.error("error next period plan: bizType={}", bizType);
            return Collections.emptyList();
        }

        CurrentDataPlanDTO data = result.getData();
        String period = data.getPeriod();
        return parsePeriod(period, true);
    }

    private static List<String> parsePeriod(String period, boolean next) {
        if (StringUtils.isNotBlank(period)) {
            try {
                ArrayList<String> deliverTimeTypes = new ArrayList<>();
                // 月度的每月都要执行
                deliverTimeTypes.add(DeliverTimeTypeEnum.month.name());

                // 解析对应的格式【202409】
                YearMonth yearMonth = YearMonth.parse(period, DataPlanUtil.YM);
                if (next) {
                    yearMonth = yearMonth.plusMonths(1);
                }

                Month month = yearMonth.getMonth();
                if (Month.MARCH.equals(month) || Month.JUNE.equals(month) || Month.SEPTEMBER.equals(month) || Month.DECEMBER.equals(month)) {
                    // 季度
                    deliverTimeTypes.add(DeliverTimeTypeEnum.quarter.name());
                }
                if (Month.JUNE.equals(month) || Month.DECEMBER.equals(month)) {
                    // 半年度
                    deliverTimeTypes.add(DeliverTimeTypeEnum.halfAYear.name());
                }
                if (Month.DECEMBER.equals(month)) {
                    // 年度
                    deliverTimeTypes.add(DeliverTimeTypeEnum.year.name());
                }
                return deliverTimeTypes;
            } catch (Exception e) {
                log.info("全流程当前周期转换异常", e);
            }
        }
        return Collections.emptyList();
    }

    /**
     *
     */
    @Override
    public AjaxResult<IPage<DataPlanBizDTO>> monthSummary(DataPlanPageRequest request) {
        IPage<DataPlan> planPage = dataPlanDAO.page(request.buildPage(), new QueryWrapper<DataPlan>()
                .select(" distinct period ")
                .eq("biz_type", request.getBizType())
                .orderByDesc("day"));

        List<DataPlan> records = planPage.getRecords();
        Map<String, List<DataPlan>> periodMap;
        if (CollectionUtils.isNotEmpty(records)) {
            Set<String> periods = Lambda.buildSet(records, DataPlan::getPeriod);
            List<DataPlan> allData = dataPlanDAO.lambdaQuery()
                    .select(DataPlan::getDay, DataPlan::getPeriod, DataPlan::getFlowProcessType)
                    .in(DataPlan::getPeriod, periods)
                    .eq(DataPlan::getBizType, request.getBizType())
                    .list();
            periodMap = allData.stream().collect(Collectors.groupingBy(DataPlan::getPeriod));
        } else {
            periodMap = Collections.emptyMap();
        }

        return AjaxResult.success(PageUtil.convertMap(planPage, v -> {
            DataPlanBizDTO dto = new DataPlanBizDTO();
            List<DataPlan> dataPlans = periodMap.get(v.getPeriod());

            Map<String, DataPlan> typeMap = dataPlans.stream()
                    .collect(Collectors.toMap(DataPlan::getFlowProcessType, Function.identity(),
                            BinaryOperator.minBy(Comparator.comparing(DataPlan::getDay)))
                    );

            dto.setPeriod(v.getPeriod());
            dto.setQcDate(Optional.ofNullable(typeMap.get(FlowProcessTypeEnum.qc.name())).map(DataPlan::getDay).orElse(null));
            dto.setSopDate(Optional.ofNullable(typeMap.get(FlowProcessTypeEnum.sop.name())).map(DataPlan::getDay).orElse(null));
            dto.setDeliverDate(Optional.ofNullable(typeMap.get(FlowProcessTypeEnum.deliver.name())).map(DataPlan::getDay).orElse(null));
            return dto;
        }));
    }

    @Override
    public Map<String, DataPlanBizDTO> queryNextDeliverDate() {
        LocalDate today = LocalDate.now();
        Map<String, DataPlanBizDTO> result = new HashMap<>();
        for (String biz : BizTypeEnum.PLANS) {
            DataPlanPageRequest req = new DataPlanPageRequest();
            req.setBizType(biz);
            req.setPage(1);
            req.setSize(24);
            AjaxResult<IPage<DataPlanBizDTO>> pageRes = this.monthSummary(req);
            IPage<DataPlanBizDTO> data = pageRes.getData();
            List<DataPlanBizDTO> records = data.getRecords();

            Optional<DataPlanBizDTO> nextDay = records.stream()
                    .filter(v -> Objects.nonNull(v.getDeliverDate()))
                    .sorted(Comparator.comparing(DataPlanBizDTO::getDeliverDate))
                    .filter(v -> !v.getDeliverDate().isBefore(today))
                    .findFirst();
            if (!nextDay.isPresent()) {
                throw new CustomException(biz + " 无有效排期");
            }
            result.put(biz, nextDay.get());
        }
        return result;
    }

    @Override
    public AjaxResult<List<String>> listPeriod() {
        List<DataPlan> periods = dataPlanDAO.list(new QueryWrapper<DataPlan>()
                .select("distinct period")
                .orderByDesc("day")
        );
        return AjaxResult.success(periods.stream().filter(Objects::nonNull)
                .map(DataPlan::getPeriod).collect(Collectors.toList()));
    }

    /**
     *
     */
    @Override
    public AjaxResult<IPage<DataPlanDetailPageDTO>> pageDetail(DataPlanDetailPageRequest request) {
        String start = null;
        String end = null;
        if (Objects.nonNull(request.getStartTime()) || Objects.nonNull(request.getEndTime())) {
            if (Objects.isNull(request.getStartTime()) || Objects.isNull(request.getEndTime())) {
                return AjaxResult.error("请选择完整的时间");
            }

            start = LocalDateTime.ofInstant(request.getStartTime().toInstant(), ZoneOffset.systemDefault()).toLocalDate().format(YMD);
            end = LocalDateTime.ofInstant(request.getEndTime().toInstant(), ZoneOffset.systemDefault()).toLocalDate().format(YMD);
        }

        IPage<DataPlan> pageResult = dataPlanDAO.lambdaQuery()
                .in(CollectionUtils.isNotEmpty(request.getBizType()), DataPlan::getBizType, request.getBizType())
                .in(CollectionUtils.isNotEmpty(request.getPeriod()), DataPlan::getPeriod, request.getPeriod())
                .in(CollectionUtils.isNotEmpty(request.getFlowProcessType()), DataPlan::getFlowProcessType, request.getFlowProcessType())
                .between(Objects.nonNull(request.getStartTime()), DataPlan::getDay, start, end)
                .orderByDesc(DataPlan::getDay)
                .page(request.buildPage());

        List<DataPlan> records = pageResult.getRecords();

        Set<Long> userIds = Lambda.buildSet(records, DataPlan::getUpdater, Objects::nonNull);
        Map<Long, String> userMap = userService.selectUserNameMapByIds(userIds);

        LocalDate today = LocalDate.now();
        return AjaxResult.success(PageUtil.convertMap(pageResult, v -> {
            DataPlanDetailPageDTO dto = new DataPlanDetailPageDTO();
            BeanUtils.copyProperties(v, dto);
            dto.setUpdater(userMap.getOrDefault(v.getUpdater(), "系统"));
            dto.setEditable(today.isBefore(v.getDay()));
            return dto;
        }));
    }

    @Override
    public AjaxResult<Void> updateDetail(DataPlanDetailUpdateRequest request) {
        Long id = request.getId();
        DataPlan plan = dataPlanDAO.getById(id);
        LocalDate today = LocalDate.now();
        if (!today.isBefore(plan.getDay())) {
            return AjaxResult.error("无法编辑历史数据");
        }

        plan.setHoliday(request.getHoliday())
                .setUpdater(SecurityUtils.getUserId())
                .setUpdateTime(LocalDateTime.now());
        plan.updateById();
        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<Void> rePlanYear(DataPlanYearRequest request) {
        List<BizTypePlanVo> plans = request.getPlans();
        Set<String> tmp = new HashSet<>(BizTypeEnum.PLANS);
        Set<String> planTypes = Lambda.buildSet(plans, BizTypePlanVo::getBizType);
        tmp.removeAll(planTypes);
        if (CollectionUtils.isNotEmpty(tmp)) {
            return AjaxResult.error("请补全" + String.join(",", tmp) + " 业务线的配置");
        }
        for (BizTypePlanVo plan : plans) {
            if (Objects.isNull(plan.getQc())) {
                plan.setQc(0);
            }
            if (Objects.isNull(plan.getSop())) {
                plan.setSop(0);
            }
            if ((plan.getQc() != 0 && plan.getSop() != 0 && plan.getQc() >= plan.getSop())
                    || plan.getSop() >= plan.getDeliver()) {
                return AjaxResult.error("请按时间大小先后填写排期");
            }
        }

        log.info("rePlan: request={}", request);
        // 删除当天到年底的数据
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        int initCount = dataPlanDAO.count();
        dataPlanDAO.lambdaUpdate()
                .ge(DataPlan::getDay, tomorrow)
                .remove();
        Long userId = SecurityUtils.getUserId();
        List<DataPlan> dataPlans = DataPlanUtil.buildPlans(userId, 1, plans, initCount == 0);
        dataPlanDAO.saveBatch(dataPlans);

        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<List<BizTypePlanVo>> loadPlan() {
        List<BizTypePlanVo> res = new ArrayList<>();
        List<DataPlan> planTable = dataPlanDAO.lambdaQuery()
                .select(DataPlan::getBizType, DataPlan::getFlowProcessType, DataPlan::getDuration)
                .gt(DataPlan::getDay, LocalDate.now())
                .groupBy(DataPlan::getBizType, DataPlan::getFlowProcessType, DataPlan::getDuration)
                .list();
        Map<String, List<DataPlan>> planMap = planTable.stream().collect(Collectors.groupingBy(DataPlan::getBizType));
        for (Map.Entry<String, List<DataPlan>> entry : planMap.entrySet()) {
            List<DataPlan> value = entry.getValue();
            int size = CollectionUtils.size(value);
            if (size == 0) {
                continue;
            }
            Map<String, Integer> valMap = value.stream().collect(Collectors.toMap(DataPlan::getFlowProcessType,
                    DataPlan::getDuration, (front, current) -> current));
            BizTypePlanVo vo = new BizTypePlanVo();
            vo.setBizType(entry.getKey());
            vo.setQc(valMap.getOrDefault(FlowProcessTypeEnum.qc.name(), 0));
            vo.setSop(valMap.getOrDefault(FlowProcessTypeEnum.sop.name(), 0));
            vo.setDeliver(valMap.getOrDefault(FlowProcessTypeEnum.deliver.name(), 0));
            res.add(vo);
        }
        return AjaxResult.success(res);
    }
}
