package com.sinohealth.system.biz.transfer.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.framework.common.utils.AssertUtil;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.StatusTypeEnum;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.biz.project.mapper.ProjectMapper;
import com.sinohealth.system.biz.transfer.dto.CrProjectVO;
import com.sinohealth.system.biz.transfer.util.ValExcelUtil;
import com.sinohealth.system.domain.ProjectHelper;
import com.sinohealth.system.domain.customer.Customer;
import com.sinohealth.system.mapper.CustomerMapper;
import com.sinohealth.system.mapper.ProjectHelperMapper;
import com.sinohealth.system.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-03-08 11:37
 */
@Slf4j
public class CrProjectListener implements ReadListener<CrProjectVO> {

    private final ProjectMapper projectMapper;
    private final CustomerMapper customerMapper;
    private final SysUserMapper sysUserMapper;
    private final ProjectHelperMapper projectHelperMapper;
    private final DataSourceTransactionManager dataSourceTransactionManager;
    private final TransactionDefinition transactionDefinition;
    private final Validator validator;

    private static final int batch = 200;

    private final List<CrProjectVO> list = new ArrayList<>(batch);


    public CrProjectListener(ProjectMapper projectMapper, CustomerMapper customerMapper, SysUserMapper sysUserMapper, ProjectHelperMapper projectHelperMapper, DataSourceTransactionManager dataSourceTransactionManager, TransactionDefinition transactionDefinition, Validator validator) {
        this.projectMapper = projectMapper;
        this.customerMapper = customerMapper;
        this.sysUserMapper = sysUserMapper;
        this.projectHelperMapper = projectHelperMapper;
        this.dataSourceTransactionManager = dataSourceTransactionManager;
        this.transactionDefinition = transactionDefinition;
        this.validator = validator;
    }

    @Override
    public void invoke(CrProjectVO data, AnalysisContext context) {
        log.info("data={}", data);
        list.add(data);
        if (list.size() >= batch) {
            handleTransaction(this::saveToDB);

            list.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        handleTransaction(this::saveToDB);

        list.clear();
    }

    private void handleTransaction(Runnable act) {
        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
        try {
            act.run();
            dataSourceTransactionManager.commit(transactionStatus);
        } catch (Exception e) {
            log.error("", e);
            dataSourceTransactionManager.rollback(transactionStatus);
        }
    }

    private void saveToDB() {
        boolean invalidParam = false;
        for (CrProjectVO p : list) {
            Set<ConstraintViolation<CrProjectVO>> set = validator.validate(p);
            if (CollectionUtils.isNotEmpty(set)) {
                invalidParam = true;
                log.info("{}: {}", p.getName(), set.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(",")));
            }
        }
        if (invalidParam) {
            return;
        }

        Set<String> names = Lambda.buildSet(list, CrProjectVO::getName);

        List<Project> existList = projectMapper.selectList(new QueryWrapper<Project>()
                .lambda().select(Project::getName).in(Project::getName, names));
        Set<String> existNames = Lambda.buildSet(existList, Project::getName);
        log.warn("existNames={}", existNames);

        List<String> userNames = list.stream()
                .filter(v -> !existNames.contains(v.getName()))
                .flatMap(v -> Stream.of(v.getCooperator(), v.getProjectManager()))
                .filter(StringUtils::isNotBlank)
                .flatMap(v -> Stream.of(StringUtils.split(v, ";")))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        List<String> customerNames = list.stream()
                .filter(v -> !existNames.contains(v.getName()))
                .map(CrProjectVO::getCustomer)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        Map<String, Customer> customerNameMap = Lambda.queryMapIfExist(customerNames,
                v -> customerMapper.selectList(new QueryWrapper<Customer>().lambda().in(Customer::getShortName, v)),
                Customer::getShortName);

        Map<String, SysUser> userNameMap = Lambda.queryMapIfExist(userNames,
                v -> sysUserMapper.selectList(new QueryWrapper<SysUser>().lambda().in(SysUser::getRealName, v)),
                SysUser::getRealName);

        for (CrProjectVO v : list) {
            if (existNames.contains(v.getName())) {
                continue;
            }
            Customer customer = customerNameMap.get(v.getCustomer());
            AssertUtil.notNull(customer, v.getName() + " 客户不存在:" + v.getCustomer());
            SysUser user = userNameMap.get(v.getProjectManager());
            AssertUtil.notNull(user, v.getName() + " 项目经理不存在:" + v.getProjectManager());
            Project p = new Project()
                    .setName(v.getName())
                    .setDescription(v.getDescription())
                    .setCustomerId(customer.getId())
                    .setProjectManager(user.getUserId())
                    .setStatus(StatusTypeEnum.IS_ENABLE.getId())
                    .setCreator(1L)
                    .setUpdater(1L);
            projectMapper.insert(p);

            Set<Long> users = new HashSet<>();
            users.add(user.getUserId());
            ProjectHelper ph = new ProjectHelper(p.getId(), user.getUserId());
            projectHelperMapper.insert(ph);

            List<String> who = ValExcelUtil.splitMultiple(v.getCooperator());
            for (String s : who) {
                SysUser w = userNameMap.get(s);
                AssertUtil.notNull(w, v.getName() + " 协作用户不存在:" + s);
                if (users.contains(w.getUserId())) {
                    continue;
                }
                users.add(w.getUserId());
                ph = new ProjectHelper(p.getId(), w.getUserId());
                projectHelperMapper.insert(ph);
            }
        }
    }
}
