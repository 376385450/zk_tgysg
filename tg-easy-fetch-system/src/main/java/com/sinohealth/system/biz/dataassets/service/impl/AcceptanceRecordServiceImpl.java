package com.sinohealth.system.biz.dataassets.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sinohealth.common.annotation.RegisterCronMethod;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.enums.dataassets.AcceptanceStateEnum;
import com.sinohealth.common.enums.dataassets.AcceptanceTypeEnum;
import com.sinohealth.system.biz.application.constants.ApplyRunStateEnum;
import com.sinohealth.system.biz.application.dao.ApplicationDAO;
import com.sinohealth.system.biz.application.dao.ApplicationFormDAO;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.dao.AcceptanceRecordDAO;
import com.sinohealth.system.biz.dataassets.domain.AcceptanceRecord;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.dto.AcceptanceRecordDTO;
import com.sinohealth.system.biz.dataassets.dto.request.AcceptListRequest;
import com.sinohealth.system.biz.dataassets.dto.request.AcceptRequest;
import com.sinohealth.system.biz.dataassets.service.AcceptanceRecordService;
import com.sinohealth.system.biz.project.dto.CurrentDataPlanDTO;
import com.sinohealth.system.biz.project.service.DataPlanService;
import com.sinohealth.system.biz.template.dao.TemplateInfoDAO;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-08-17 14:46
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Service("acceptanceRecordService")
public class AcceptanceRecordServiceImpl implements AcceptanceRecordService {

    private final AcceptanceRecordDAO acceptanceRecordDAO;
    private final ApplicationFormDAO applicationFormDAO;
    private final ApplicationDAO applicationDAO;
    private final TemplateInfoDAO templateInfoDAO;

    private final ISysUserService userService;
    private final DataPlanService dataPlanService;

    /**
     * 自动验收 定时任务
     */
    @RegisterCronMethod
    public void scheduleAutoAccept() {
        List<AcceptanceRecord> timeoutList = acceptanceRecordDAO.getBaseMapper().selectList(new QueryWrapper<AcceptanceRecord>().lambda()
                .eq(AcceptanceRecord::getState, AcceptanceStateEnum.wait.name())
                .lt(AcceptanceRecord::getCreateTime, LocalDateTime.now().plusDays(-3)));

        if (CollectionUtils.isEmpty(timeoutList)) {
            return;
        }
        List<Long> ids = Lambda.buildList(timeoutList, AcceptanceRecord::getId);
        acceptanceRecordDAO.getBaseMapper().update(null, new UpdateWrapper<AcceptanceRecord>().lambda()
                .in(AcceptanceRecord::getId, ids)
                .set(AcceptanceRecord::getAcceptTime, LocalDateTime.now())
                .set(AcceptanceRecord::getUser, 0L)
                .set(AcceptanceRecord::getState, AcceptanceStateEnum.pass.name())
        );

        List<Long> applyIds = Lambda.buildList(timeoutList, AcceptanceRecord::getApplicationId);
        List<TgApplicationInfo> appList = applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getApplicationNo)
                .in(TgApplicationInfo::getId, applyIds)
                .list();
        Set<String> noList = Lambda.buildSet(appList, TgApplicationInfo::getApplicationNo);
        applicationFormDAO.updateRunState(noList, ApplyRunStateEnum.finish);
    }

    @Override
    public AjaxResult<Void> accept(AcceptRequest acceptRequest) {
        UserDataAssets assets = UserDataAssets.newInstance().selectById(acceptRequest.getAssetsId());

        List<AcceptanceRecord> records = acceptanceRecordDAO.lambdaQuery()
                .eq(AcceptanceRecord::getAssetsId, acceptRequest.getAssetsId())
                .eq(AcceptanceRecord::getVersion, assets.getVersion()).list();
        if (CollectionUtils.isEmpty(records)) {
            log.info("request={} version={}", acceptRequest, assets.getVersion());
            return AjaxResult.error("资产版本不存在");
        }

        AcceptanceRecord record = records.get(0);
        if (!Objects.equals(record.getUser(), acceptRequest.getApplicantId())
                && !Objects.equals(record.getUser(), 0L)) {
            return AjaxResult.error("仅申请人可验收");
        }
        // 0 标识系统，支持二次验收
        if (!Objects.equals(record.getState(), AcceptanceStateEnum.wait.name())
                && !Objects.equals(record.getUser(), 0L)
                && BooleanUtils.isNotTrue(acceptRequest.getForceRetry())) {
            return AjaxResult.error("勿重复验收");
        }

        acceptanceRecordDAO.update(new UpdateWrapper<AcceptanceRecord>().lambda()
                .eq(AcceptanceRecord::getId, record.getId())
                .set(AcceptanceRecord::getRemark, acceptRequest.getRemark())
                .set(AcceptanceRecord::getState, acceptRequest.getState())
                .set(AcceptanceRecord::getUser, acceptRequest.getApplicantId())
                .set(AcceptanceRecord::getAcceptTime, LocalDateTime.now())
        );

        Long templateId = assets.getTemplateId();
        Optional<CurrentDataPlanDTO> planOpt = templateInfoDAO.lambdaQuery()
                .select(TgTemplateInfo::getBizType)
                .eq(TgTemplateInfo::getId, templateId).oneOpt().map(TgTemplateInfo::getBizType)
                .map(dataPlanService::currentPeriod);


        applicationDAO.lambdaQuery()
                .select(TgApplicationInfo::getId, TgApplicationInfo::getApplicationNo)
                .eq(TgApplicationInfo::getId, record.getApplicationId())
                .oneOpt().map(TgApplicationInfo::getApplicationNo).ifPresent(v -> {
                    boolean pass = Objects.equals(acceptRequest.getState(), AcceptanceStateEnum.pass.name());
                    if (pass) {
                        applicationFormDAO.updateRunState(v, ApplyRunStateEnum.finish);
                    } else {
                        applicationFormDAO.acceptReject(v, planOpt.map(CurrentDataPlanDTO::getPeriod).orElse(""));
                    }
                });

        return AjaxResult.succeed();
    }

    @Override
    public AjaxResult<List<AcceptanceRecordDTO>> list(AcceptListRequest request) {
        if (Objects.isNull(request.getAssetsId()) && Objects.isNull(request.getApplicationId())) {
            return AjaxResult.error("参数缺失");
        }

        List<AcceptanceRecord> records = acceptanceRecordDAO.getBaseMapper().selectList(new QueryWrapper<AcceptanceRecord>().lambda()
                .eq(Objects.nonNull(request.getApplicationId()), AcceptanceRecord::getApplicationId, request.getApplicationId())
                .eq(Objects.nonNull(request.getAssetsId()), AcceptanceRecord::getAssetsId, request.getAssetsId())
                .eq(Objects.nonNull(request.getVersion()), AcceptanceRecord::getVersion, request.getVersion())
                .ne(AcceptanceRecord::getState, AcceptanceStateEnum.version_roll.name())
        );

        List<Long> userIds = Lambda.buildList(records, AcceptanceRecord::getUser);
        Map<Long, String> nameMap = Lambda.queryMapIfExist(userIds, userService::selectUserNameMapByIds);

        return AjaxResult.success(records.stream().map(v -> {
            AcceptanceRecordDTO dto = new AcceptanceRecordDTO();
            BeanUtils.copyProperties(v, dto);
            boolean system = v.getUser() == 0L;
            String userName = system ? "系统" : nameMap.get(v.getUser());
            dto.setUser(userName);
            dto.setAcceptType(system ? AcceptanceTypeEnum.system.name() : AcceptanceTypeEnum.manual.name());

            return dto;
        }).collect(Collectors.toList()));
    }
}
