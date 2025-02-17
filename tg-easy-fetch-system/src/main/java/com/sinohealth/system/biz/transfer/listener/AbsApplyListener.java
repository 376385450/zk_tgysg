package com.sinohealth.system.biz.transfer.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.sinohealth.common.config.TransferProperties;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.AssetType;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.SpringContextUtils;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.biz.application.dto.TemplateGranularityDetailDto;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.service.AssetsUpgradeTriggerService;
import com.sinohealth.system.biz.dict.dao.FieldDictDAO;
import com.sinohealth.system.biz.dict.dao.MetricsDictDAO;
import com.sinohealth.system.biz.dict.domain.FieldDict;
import com.sinohealth.system.biz.project.mapper.ProjectMapper;
import com.sinohealth.system.biz.project.service.ProjectService;
import com.sinohealth.system.biz.transfer.dto.CrExcelVO;
import com.sinohealth.system.config.ApplicationConfigTypeConstant;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgAssetInfo;
import com.sinohealth.system.domain.TgTemplateInfo;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.dto.analysis.FilterDTO;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import com.sinohealth.system.service.IApplicationService;
import com.sinohealth.system.service.IAssetService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.IntergrateProcessDefService;
import com.sinohealth.system.util.HistoryApplyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Kuangcp
 * 2024-07-23 10:20
 */
@Slf4j
public abstract class AbsApplyListener<T extends CrExcelVO> implements ReadListener<T> {

    final DataSourceTransactionManager dataSourceTransactionManager;
    final TransactionDefinition transactionDefinition;
    final Validator validator;
    final HttpServletRequest req;

    final TransferProperties transferProperties;
    final RedisTemplate redisTemplate;

    final ProjectMapper projectMapper;
    final TgTemplateInfoMapper templateMapper;
    final FieldDictDAO fieldDictDAO;
    final MetricsDictDAO metricsDictDAO;
    final TgApplicationInfoMapper applicationInfoMapper;


    final ProjectService projectService;
    final ISysUserService userService;
    final UserDetailsService userDetailsService;
    final IAssetService assetService;
    final IApplicationService applicationService;
    final AssetsUpgradeTriggerService assetsUpgradeTriggerService;
    private final IntergrateProcessDefService flowDefService;

    final List<T> list = new ArrayList<>();

    final Map<Long, String> fieldDictNameMap = new HashMap<>();
    final Map<Long, String> fieldDictFeildNameMap = new HashMap<>();
    final Map<String, TgTemplateInfo> templateMap = new HashMap<>();
    final Map<Long, TgAssetInfo> assetsMap = new HashMap<>();

    private final Map<String, Integer> flowIdMap = new ConcurrentHashMap<>();

    public AbsApplyListener(DataSourceTransactionManager dataSourceTransactionManager, TransactionDefinition transactionDefinition, Validator validator, HttpServletRequest req) {
        this.dataSourceTransactionManager = dataSourceTransactionManager;
        this.transactionDefinition = transactionDefinition;
        this.validator = validator;
        this.req = req;

        this.redisTemplate = SpringContextUtils.getBean("redisTemplate", RedisTemplate.class);
        this.transferProperties = SpringContextUtils.getBean(TransferProperties.class);

        this.projectMapper = SpringContextUtils.getBean(ProjectMapper.class);
        this.templateMapper = SpringContextUtils.getBean(TgTemplateInfoMapper.class);
        this.fieldDictDAO = SpringContextUtils.getBean(FieldDictDAO.class);
        this.metricsDictDAO = SpringContextUtils.getBean(MetricsDictDAO.class);
        this.applicationInfoMapper = SpringContextUtils.getBean(TgApplicationInfoMapper.class);

        this.userService = SpringContextUtils.getBean(ISysUserService.class);
        this.userDetailsService = SpringContextUtils.getBean("userDetailsService", UserDetailsService.class);
        this.projectService = SpringContextUtils.getBean(ProjectService.class);
        this.applicationService = SpringContextUtils.getBean(IApplicationService.class);
        this.assetService = SpringContextUtils.getBean(IAssetService.class);
        this.assetsUpgradeTriggerService = SpringContextUtils.getBean(AssetsUpgradeTriggerService.class);
        this.flowDefService = SpringContextUtils.getBean(IntergrateProcessDefService.class);
    }


    /**
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context analysis context
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        list.add(data);
    }

    void handleTransaction(Runnable act) {
        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            act.run();
            dataSourceTransactionManager.commit(transactionStatus);
        } catch (Exception e) {
            log.error("", e);
            dataSourceTransactionManager.rollback(transactionStatus);
        }
    }


    ApplyCheckCtx preCheck() {
        // 导入指定id
        List<Integer> excelIds;
        String ids = req.getParameter("ids");
        if (StringUtils.isNotBlank(ids)) {
            excelIds = Arrays.stream(ids.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        } else {
            excelIds = Collections.emptyList();
        }

        // 忽略指定id
        List<Integer> ignoreIds;
        String igs = req.getParameter("ignoreIds");
        if (StringUtils.isNotBlank(igs)) {
            ignoreIds = Arrays.stream(igs.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        } else {
            ignoreIds = Collections.emptyList();
        }

        Object flag = redisTemplate.opsForValue().get(RedisKeys.Apply.DEBUG_MODE);
        boolean debug = Objects.nonNull(flag);
        if (!debug) {
            log.warn("Submit New Apply Mode");
        }

        boolean invalidParam = false;
        int rowId = 0;
        for (T p : list) {
            rowId++;
            if (CollectionUtils.isNotEmpty(excelIds) && !excelIds.contains(rowId)) {
                continue;
            }
            if (CollectionUtils.isNotEmpty(ignoreIds) && ignoreIds.contains(rowId)) {
                continue;
            }
            Set<ConstraintViolation<T>> set = validator.validate(p);
            if (CollectionUtils.isNotEmpty(set)) {
                invalidParam = true;
                log.info("{} {} : {}", rowId, p.getProjectName(), set.stream()
                        .map(ConstraintViolation::getMessage).collect(Collectors.joining(",")));
            }
        }
        if (invalidParam) {
            return null;
        }


        return ApplyCheckCtx.builder()
                .excelIds(excelIds)
                .ignoreIds(ignoreIds)
                .debug(debug)
                .build();
    }


    /**
     * 通用模板 设置工作流id 到申请单
     */
    void fillFlowId(String flowName, TgTemplateInfo template, TgApplicationInfo info) {
        if (!Objects.equals(template.getTemplateType(), TemplateTypeEnum.customized.name())) {
            return;
        }

        Integer flowId = flowIdMap.computeIfAbsent(flowName, v -> {
            AjaxResult listResult = flowDefService.queryProcessDefinitionList(1, 10, flowName, 1);
            if (listResult.getCode() != 0) {
                log.error("ERROR flowResult: {}", listResult);
                return null;
            }

            LinkedHashMap data = (LinkedHashMap) listResult.get("data");
            ArrayList<LinkedHashMap> list = (ArrayList<LinkedHashMap>) data.get("totalList");

            return (Integer) list.stream()
                    .filter(i -> Objects.equals(i.get("name"), flowName))
                    .map(i -> i.get("id"))
                    .findAny().orElse(null);
        });

        if (Objects.isNull(flowId)) {
            log.warn("工作流匹配失败: name={}", flowName);
            throw new RuntimeException("工作流匹配失败: " + flowName);
        }
        info.setWorkflowId(flowId);
        info.setConfigType(ApplicationConfigTypeConstant.WORK_FLOW_TYPE);
    }

    TgTemplateInfo queryTemplate(String name) {
        TgTemplateInfo cache = templateMap.get(name);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        List<TgTemplateInfo> templateInfoList = templateMapper.selectList(new QueryWrapper<TgTemplateInfo>().lambda()
                .eq(TgTemplateInfo::getTemplateName, name));
        if (CollectionUtils.size(templateInfoList) == 1) {
            TgTemplateInfo template = templateInfoList.get(0);

            JsonBeanConverter.convert2Obj(template);
            List<Long> fieldDictIds = template.getGranularity().stream().flatMap(v -> {
                List<TemplateGranularityDetailDto> details = v.getDetails();
                if (CollectionUtils.isEmpty(details)) {
                    return Stream.empty();
                }
                return details.stream().flatMap(d -> Stream.of(d.getRequired(), d.getOptions())
                        .filter(CollectionUtils::isNotEmpty).flatMap(Collection::stream)
                );
            }).collect(Collectors.toList());

            List<FieldDict> fieldDictList = Lambda.queryListIfExist(fieldDictIds, fieldDictDAO.getBaseMapper()::selectBatchIds);
            Map<Long, String> tmp = Lambda.buildMap(fieldDictList, FieldDict::getId, FieldDict::getName);
            fieldDictNameMap.putAll(tmp);
            fieldDictFeildNameMap.putAll(Lambda.buildMap(fieldDictList, FieldDict::getId, FieldDict::getFieldName));

            templateMap.put(name, template);
            return template;

        }
        log.info("template list={}", templateInfoList);
        throw new CustomException("提数模板错误 " + name);
    }

    Optional<FilterDTO> parseForFront(String sql) {
        return HistoryApplyUtil.parseForFront(sql, transferProperties.getWhereConvertUrl());
    }

    TgAssetInfo queryAssets(Long tempId) {
        TgAssetInfo cache = assetsMap.get(tempId);
        if (Objects.nonNull(cache)) {
            return cache;
        }
        TgAssetInfo asset = assetService.queryOne(tempId, AssetType.MODEL);
        if (Objects.isNull(asset)) {
            throw new CustomException("常规模板未上架");
        }
        assetsMap.put(tempId, asset);
        return asset;
    }

    /**
     * 模拟Token认证
     */
    void fillUserAuthById(HttpServletRequest request, Long userId) {
        SysUser sysUser = userService.selectUserById(userId);
        ThreadContextHolder.setSysUser(sysUser);
        UserDetails userDetails = userDetailsService.loadUserByUsername(sysUser.getUserName());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        SinoPassUserDTO orgUserInfo;
        try {
            if (ObjectUtils.isNotNull(sysUser.getOrgUserId())) {
                orgUserInfo = Optional.ofNullable(SinoipaasUtils.mainEmployeeSelectbyid(sysUser.getOrgUserId())).orElse(createEmptyOrgUser());
            } else {
                orgUserInfo = createEmptyOrgUser();
            }
        } catch (NullPointerException e) {
            log.error("异常捕获", e);
            orgUserInfo = createEmptyOrgUser();
        }

        ThreadContextHolder.getParams().put(CommonConstants.ORG_USER_INFO, orgUserInfo);
    }

    private SinoPassUserDTO createEmptyOrgUser() {
        return new SinoPassUserDTO();
    }
}
