package com.sinohealth.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.data.common.response.PageInfo;
import com.sinohealth.system.biz.project.domain.Project;
import com.sinohealth.system.biz.project.mapper.ProjectMapper;
import com.sinohealth.system.biz.project.domain.ProjectDataAssetsRelate;
import com.sinohealth.system.domain.customer.Customer;
import com.sinohealth.system.dto.customer.PageQueryCustomer;
import com.sinohealth.system.dto.customer.SaveCustomerReq;
import com.sinohealth.system.mapper.CustomerMapper;
import com.sinohealth.system.mapper.ProjectDataAssetsRelateMapper;
import com.sinohealth.system.mapper.SysUserMapper;
import com.sinohealth.system.service.CustomerService;
import com.sinohealth.system.vo.CustomerVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Author shallwetalk
 * @Date 2024/1/10
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    CustomerMapper customerMapper;

    @Autowired
    SysUserMapper sysUserMapper;

    @Autowired
    ProjectMapper projectMapper;

    @Autowired
    ProjectDataAssetsRelateMapper projectDataAssetsRelateMapper;

    @Override
    public PageInfo<CustomerVO> page(PageQueryCustomer page) {
        Page<Customer> customerPage = new Page<>();
        customerPage.setCurrent(page.getPageNum());
        customerPage.setSize(page.getPageSize());

        final LambdaQueryWrapper<Customer> wq = Wrappers.<Customer>lambdaQuery()
                .like(StringUtils.isNotBlank(page.getShortName()), Customer::getShortName, page.getShortName())
                .like(StringUtils.isNotBlank(page.getFullName()), Customer::getFullName, page.getFullName())
                .eq(Objects.nonNull(page.getCustomerType()), Customer::getCustomerType, page.getCustomerType())
                .eq(Objects.nonNull(page.getStatus()), Customer::getCustomerStatus, page.getStatus())
                .orderByDesc(Customer::getUpdateTime);

        final Page<Customer> selectPage = customerMapper.selectPage(customerPage, wq);

        final List<Long> up = selectPage.getRecords().stream()
                .map(Customer::getUpdater)
                .collect(Collectors.toList());
        final List<Long> cr = selectPage.getRecords().stream()
                .map(Customer::getCreator)
                .collect(Collectors.toList());
        up.addAll(cr);

        final Map<Long, SysUser> userMap = new HashMap<>();
        if (CollUtil.isNotEmpty(up)) {
            userMap.putAll(sysUserMapper.selectUserByIds(up).stream().collect(Collectors.toMap(k -> k.getUserId(), v -> v)));
        }

        final List<Long> ids = selectPage.getRecords().stream()
                .map(Customer::getId).collect(Collectors.toList());

        // 统计关联项目数
        Map<Long, List<Project>> projectMap = new HashMap<>();
        if (CollUtil.isNotEmpty(ids)) {
            final LambdaQueryWrapper<Project> in = Wrappers.<Project>lambdaQuery()
                    .in(CollUtil.isNotEmpty(ids), Project::getCustomerId, ids);
            projectMap.putAll(projectMapper.selectList(in).stream()
                    .collect(Collectors.groupingBy(Project::getCustomerId)));
        }

        // 统计关联资产数
        final List<Project> list = projectMap.entrySet()
                .stream().flatMap(v -> v.getValue().stream())
                .collect(Collectors.toList());
        Map<Long, Integer> relateMap = new HashMap<>();
        if (CollUtil.isNotEmpty(ids) && CollUtil.isNotEmpty(list)) {
            final LambdaQueryWrapper<ProjectDataAssetsRelate> queryWrapper = Wrappers.<ProjectDataAssetsRelate>lambdaQuery()
                    .in(ProjectDataAssetsRelate::getProjectId, list.stream().map(Project::getId).collect(Collectors.toList()))
                    .isNotNull(ProjectDataAssetsRelate::getUserAssetId);
            final List<ProjectDataAssetsRelate> relateList = projectDataAssetsRelateMapper.selectList(queryWrapper);
            final Map<Long, List<ProjectDataAssetsRelate>> map = relateList.stream().collect(Collectors.groupingBy(ProjectDataAssetsRelate::getProjectId));
            for (Long id : ids) {
                AtomicInteger i = new AtomicInteger();
                projectMap.getOrDefault(id, new ArrayList<>()).forEach(a->{
                    i.addAndGet(CollUtil.isNotEmpty(map.get(a.getId()))?map.get(a.getId()).size():0);
                });
                relateMap.put(id, i.get());
            }
        }


        final List<CustomerVO> vos = selectPage.getRecords().stream()
                .map(a -> {
                    final CustomerVO vo = new CustomerVO();
                    BeanUtils.copyProperties(a, vo);
                    final SysUser user = userMap.get(a.getCreator());
                    if (Objects.nonNull(user)) {
                        vo.setCreatedName(user.getRealName());
                    }
                    final SysUser user1 = userMap.get(a.getUpdater());
                    if (Objects.nonNull(user1)) {
                        vo.setUpdatedName(user1.getRealName());
                    }
                    vo.setRelateAsset(relateMap.getOrDefault(vo.getId(),0));
                    vo.setRelateProject(projectMap.getOrDefault(vo.getId(), Lists.newArrayList()).size());
                    return vo;
                }).collect(Collectors.toList());

        return new PageInfo<>(vos, selectPage.getTotal(), Integer.parseInt(Long.toString(selectPage.getPages())), selectPage.getCurrent());
    }

    @Override
    public void save(SaveCustomerReq req) {
        final String shortName = req.getShortName();
        final LambdaQueryWrapper<Customer> ne = Wrappers.<Customer>lambdaQuery()
                .eq(Customer::getShortName, shortName)
                .ne(Objects.nonNull(req.getId()), Customer::getId, req.getId())
                .eq(Customer::getDeleted, 0);

        final List<Customer> customers = customerMapper.selectList(ne);
        if (CollUtil.isNotEmpty(customers)) {
            throw new CustomException("客户简称重复");
        }

        if (Objects.nonNull(req.getId())) {
            // 更新
            final Customer customer = customerMapper.selectById(req.getId());
            BeanUtils.copyProperties(req, customer);
            customer.setUpdateTime(LocalDateTime.now());
            customer.setUpdater(SecurityUtils.getUserId());
            customerMapper.updateById(customer);
        } else {
            final Customer customer = new Customer();
            BeanUtils.copyProperties(req, customer);
            customer.setCreateTime(LocalDateTime.now());
            customer.setCreator(SecurityUtils.getUserId());
            customer.setUpdateTime(LocalDateTime.now());
            customer.setUpdater(SecurityUtils.getUserId());
            customerMapper.insert(customer);
        }
    }
}
