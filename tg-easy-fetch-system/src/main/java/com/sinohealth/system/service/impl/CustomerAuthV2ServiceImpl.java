package com.sinohealth.system.service.impl;

import cn.hutool.core.lang.Assert;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.dao.CustomerAuthDAO;
import com.sinohealth.system.domain.ArkbiAnalysisDTO;
import com.sinohealth.system.domain.ArkbiAnalysisQuery;
import com.sinohealth.system.domain.TgCustomerApplyAuth;
import com.sinohealth.system.dto.*;
import com.sinohealth.system.dto.assets.SubCustomerAssetsBatchUpdateReqDTO;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.ArkbiAnalysisMapper;
import com.sinohealth.system.mapper.SysCustomerAuthMapper;
import com.sinohealth.system.service.CustomerAuthV2Service;
import com.sinohealth.system.service.ISysCustomerAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author lvheng chen
 * @version 1.0
 * @date 2022-12-13 13:56
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerAuthV2ServiceImpl implements CustomerAuthV2Service {

    private final SysCustomerAuthMapper sysCustomerAuthMapper;

    private final SysCustomerAuthMapper customerAuthMapper;

    private final ArkbiAnalysisMapper arkbiAnalysisMapper;

    private final ISysCustomerAuthService sysCustomerAuthService;

    private final CustomerAuthDAO customerAuthDAO;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(CustomerApplyAuthUpdateReqV2DTO reqV2DTO) {
        for (CustomerApplyAuthUpdateReqV2DTO.CustomerApplyAuthUserItemDTO authUserItemDTO : reqV2DTO.getList()) {
            TgCustomerApplyAuth updateEntity = new TgCustomerApplyAuth();
            updateEntity.setId(authUserItemDTO.getId());
            updateEntity.setAuthType(authUserItemDTO.getAuthType());
            updateEntity.setUpdateBy(ThreadContextHolder.getSysUser().getUserId());
            sysCustomerAuthMapper.updateById(updateEntity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(CustomerApplyAuthBatchUpdateReqDTO reqDTO) {
        List<CustomerApplyAuthBatchUpdateReqDTO.CustomerApplyAuthBatchUpdateItem> flat = reqDTO.flat(reqDTO.getList());
        for (CustomerApplyAuthBatchUpdateReqDTO.CustomerApplyAuthBatchUpdateItem authUserItemDTO : flat) {
            TgCustomerApplyAuth updateEntity = new TgCustomerApplyAuth();
            updateEntity.setId(authUserItemDTO.getId());
            updateEntity.setAuthType(authUserItemDTO.getAuthType());
            updateEntity.setUpdateBy(ThreadContextHolder.getSysUser().getUserId());
            sysCustomerAuthMapper.updateById(updateEntity);
        }
    }

    /**
     * 子账号资产实际上是对父账号资产的复制
     * 这里必须用递归的方式从上到下
     * 1. 复制customer_auth
     *
     * @param reqDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSub(SubCustomerAssetsBatchUpdateReqDTO reqDTO) {
        this.doBatchUpdateSub(reqDTO.getParentUserId(), reqDTO.getSubUserId(), null, reqDTO.getList());
    }

    private void doBatchUpdateSub(Long parentUserId, Long subUserId,
                                  SubCustomerAssetsBatchUpdateReqDTO.SubCustomerAssetsBatchUpdateItem parent,
                                  List<SubCustomerAssetsBatchUpdateReqDTO.SubCustomerAssetsBatchUpdateItem> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (SubCustomerAssetsBatchUpdateReqDTO.SubCustomerAssetsBatchUpdateItem subAuthUserItemDTO : list) {
            TgCustomerApplyAuth parentApplyAuth = sysCustomerAuthMapper.selectById(subAuthUserItemDTO.getId());
            TgCustomerApplyAuth subApplyAuth = customerAuthDAO.getSubByParentAuthId(subUserId, parentApplyAuth.getId());
            final boolean isNew = subApplyAuth == null && StringUtils.isNotBlank(subAuthUserItemDTO.getAuthType());
            final boolean isDelete = subApplyAuth != null && StringUtils.isBlank(subAuthUserItemDTO.getAuthType());
            final boolean isUpdate = subApplyAuth != null && StringUtils.isNotBlank(subAuthUserItemDTO.getAuthType());
            final boolean isPack = StringUtils.equals(parentApplyAuth.getIcon(), CommonConstants.ICON_PACK);
            if (isNew && !isPack) {
                // 新增授权
                TgCustomerApplyAuth newAuth = new TgCustomerApplyAuth();
                newAuth.setAssetsId(parentApplyAuth.getAssetsId());
                newAuth.setUserId(subUserId);
                newAuth.setAuthType(subAuthUserItemDTO.getAuthType());
                newAuth.setUpdateBy(ThreadContextHolder.getSysUser().getUserId());
                newAuth.setUpdateTime(new Date());
                newAuth.setCreateTime(new Date());
                newAuth.setStatus(1);
                newAuth.setOutTableName(parentApplyAuth.getOutTableName());
                newAuth.setNodeName(parentApplyAuth.getNodeName());
                newAuth.setIcon(parentApplyAuth.getIcon());
                if (parentApplyAuth.getParentId() == null || parentApplyAuth.getParentId() == 0) {
                    newAuth.setParentId(0L);
                } else {
                    // 父节点如果是目录节点，需要校验是否已添加，这里是延迟创建
                    TgCustomerApplyAuth _parentAuth = customerAuthDAO.getById(parent.getId());
                    if (StringUtils.equals(CommonConstants.ICON_PACK, _parentAuth.getIcon())) {
                        TgCustomerApplyAuth _parentSubAuth = getOrCreatePackDir(_parentAuth.getId(), subUserId);
                        newAuth.setParentId(_parentSubAuth.getId());
                    } else {
                        TgCustomerApplyAuth subParentApplyAuth = customerAuthDAO.getSubByParentAuthId(subUserId, parentApplyAuth.getParentId());
                        // 说明逻辑出错了
                        Assert.isTrue(subParentApplyAuth != null, "编辑客户报表存在异常情况，请联系管理员!");
                        newAuth.setParentId(subParentApplyAuth.getId());
                    }
                }
                newAuth.setNodeId(parentApplyAuth.getNodeId());
                newAuth.setParentCustomerAuthId(parentApplyAuth.getId());
                customerAuthDAO.save(newAuth);
            } else if (isUpdate && !isPack) {
                // 修改授权
                subApplyAuth.setAuthType(subAuthUserItemDTO.getAuthType());
                subApplyAuth.setUpdateBy(ThreadContextHolder.getSysUser().getUserId());
                subApplyAuth.setUpdateTime(new Date());
                subApplyAuth.setStatus(1);
                customerAuthDAO.updateById(subApplyAuth);
            } else if (isDelete && !isPack) {
                // 禁用
                subApplyAuth.setStatus(0);
                subApplyAuth.setUpdateBy(ThreadContextHolder.getSysUser().getUserId());
                subApplyAuth.setUpdateTime(new Date());
                customerAuthDAO.updateById(subApplyAuth);
            }
            doBatchUpdateSub(parentUserId, subUserId, subAuthUserItemDTO, subAuthUserItemDTO.getChildren());
        }
    }

    private TgCustomerApplyAuth getOrCreatePackDir(Long parentAuthId, Long subUserId) {
        TgCustomerApplyAuth _parentAuth = customerAuthDAO.getById(parentAuthId);
        Assert.isTrue(StringUtils.equals(CommonConstants.ICON_PACK, _parentAuth.getIcon()), "请联系管理员");
        TgCustomerApplyAuth _parentSubApplyAuth = customerAuthDAO.getSubByParentAuthId(subUserId, _parentAuth.getId());
        if (_parentSubApplyAuth == null) {
            // 新增授权
            TgCustomerApplyAuth newAuth = new TgCustomerApplyAuth();
            newAuth.setAssetsId(_parentAuth.getAssetsId());
            newAuth.setUserId(subUserId);
            newAuth.setAuthType("");
            newAuth.setUpdateBy(SecurityUtils.getUserId());
            newAuth.setUpdateTime(new Date());
            newAuth.setCreateTime(new Date());
            newAuth.setStatus(1);
            newAuth.setOutTableName(_parentAuth.getOutTableName());
            newAuth.setNodeName(_parentAuth.getNodeName());
            newAuth.setIcon(_parentAuth.getIcon());
            newAuth.setNodeId(_parentAuth.getNodeId());
            newAuth.setParentCustomerAuthId(_parentAuth.getId());
            if (_parentAuth.getParentId() == null || _parentAuth.getParentId() == 0) {
                newAuth.setParentId(0L);
            } else {
                TgCustomerApplyAuth _parent_parent_auth = getOrCreatePackDir(_parentAuth.getParentId(), subUserId);
                newAuth.setParentId(_parent_parent_auth.getId());
            }
            customerAuthDAO.save(newAuth);
            return newAuth;
        }
        return _parentSubApplyAuth;
    }

    /**
     * 分配客户，查询已交付的资产，额外返回未分配的图标分析数据
     *
     * @param reqDTO
     * @return
     */
    @Override
    public CustomerApplyAuthList queryAuthList(@RequestBody @Valid CustomerApplyAuthListReqDTO reqDTO) {
        // 查询目录
        List<CustomerApplyDTO> dirList = customerAuthMapper.listAuth(reqDTO.getIds(), null, reqDTO.getAssetsId());
        if (CollectionUtils.isEmpty(dirList)) {
            return new CustomerApplyAuthList();
        }
        List<CustomerApplyDTO> authList = dirList.stream()
                .filter(it -> BooleanUtils.isTrue(it.getDisabled()))
                .collect(Collectors.toList());
        // 查询提数申请绑定的图标分析(这里需要额外查询，因为可能是单选提数申请)
        List<Long> applyDirIds = authList.stream()
                .filter(it -> CommonConstants.ICON_DATA_ASSETS.equals(it.getIcon())).map(CustomerApplyDTO::getDirId)
                .collect(Collectors.toList());
        List<CustomerApplyDTO> dbAuthChartList = CollectionUtils.isNotEmpty(applyDirIds)
                ? customerAuthMapper.listChartAuth(applyDirIds, null) : Collections.emptyList();
        Map<Long, CustomerApplyDTO> authMap = authList.stream().collect(Collectors.toMap(CustomerApplyDTO::getDirId,
                Function.identity(), (a, b) -> b));
        dbAuthChartList.stream().filter(it -> !authMap.containsKey(it.getDirId())).forEach(it -> {
            dirList.add(it);
            authList.add(it);
        });
        // 如果此次操作的资产已分配给两个或两个以上的不同用户，则不允许该次操作
        long count = authList.stream()
                .map(CustomerApplyDTO::getUserId).distinct().count();
        Assert.isTrue(count <= 1, "选中的资产已分配给不同的客户，不允许同时操作，每次只允许分配给一个用户");

        Map<String, List<CustomerApplyDTO>> authChartMap = authList.stream()
                .filter(it -> CommonConstants.ICON_CHART.equals(it.getIcon()))
                .collect(Collectors.groupingBy(CustomerApplyDTO::getAssetsId));
        Map<String, List<CustomerApplyDTO>> noAuthChartMap = new HashMap<>();

        // 提取出提数申请，然后查询出数据目录树，查找出关联的图表分析
        List<String> applyIds = dirList.stream().filter(it -> CommonConstants.ICON_DATA_ASSETS.equals(it.getIcon()))
                .map(it -> String.valueOf(it.getNodeId())).distinct().collect(Collectors.toList());
        // 判断applyIds
        ArkbiAnalysisQuery chartQuery = new ArkbiAnalysisQuery()
                .setType(CommonConstants.ICON_CHART)
                .setAssetsIds(applyIds)
                .setParent(false);
        Map<String, List<ArkbiAnalysisDTO>> chartAnalysisMap = (CollectionUtils.isEmpty(applyIds) ?
                new ArrayList<ArkbiAnalysisDTO>() : arkbiAnalysisMapper.list(chartQuery))
                .stream().collect(Collectors.groupingBy(ArkbiAnalysisDTO::getAssetsId));
        dirList.stream()
                .filter(it -> CommonConstants.ICON_DATA_ASSETS.equals(it.getIcon()))
                .forEach(it -> {
                    Map<Long, CustomerApplyDTO> chartMap = authChartMap.getOrDefault(it.getAssetsId(), new ArrayList<>())
                            .stream().collect(Collectors.toMap(CustomerApplyDTO::getNodeId, Function.identity()));
                    List<ArkbiAnalysisDTO> allChartList = chartAnalysisMap.getOrDefault(it.getAssetsId(), Collections.emptyList());
                    allChartList.stream().filter(item -> !chartMap.containsKey(item.getId())).forEach(item -> {
                        CustomerApplyDTO noAuthDTO = new CustomerApplyDTO();
                        noAuthDTO.setAssetsId(item.getAssetsId());
                        noAuthDTO.setAuthType(null);
                        noAuthDTO.setIcon(CommonConstants.ICON_CHART);
                        noAuthDTO.setStatus(null);
                        noAuthDTO.setProjectName(item.getProjectName());
                        noAuthDTO.setDirId(item.getDirId());
                        noAuthDTO.setNodeId(item.getId());
                        noAuthDTO.setDisabled(false);
                        if (!noAuthChartMap.containsKey(item.getAssetsId())) {
                            noAuthChartMap.put(item.getAssetsId(), new ArrayList<>());
                        }
                        noAuthChartMap.get(item.getAssetsId()).add(noAuthDTO);
                    });
                });

        List<CustomerApplyDTO> list = dirList.stream()
                .filter(it -> !CommonConstants.ICON_CHART.equals(it.getIcon()))
                .peek(it -> {
                    if (CommonConstants.ICON_DATA_ASSETS.equals(it.getIcon())) {
                        List<CustomerApplyDTO> authChartList = authChartMap.getOrDefault(it.getAssetsId(), Collections.emptyList());
                        List<CustomerApplyDTO> noAuthChartList = noAuthChartMap.getOrDefault(it.getAssetsId(), Collections.emptyList());
                        List<CustomerApplyDTO> children = new ArrayList<>();
                        children.addAll(authChartList);
                        children.addAll(noAuthChartList);
                        it.setChildren(children);
                    }
                }).collect(Collectors.toList());

        Long userId = null;
        List<Long> ids = null;
        String authType = null;
        String customer = null;
        if (CollectionUtils.isNotEmpty(authList)) {
            userId = authList.get(0).getUserId();
            ids = authList.stream().map(CustomerApplyDTO::getDirId).collect(Collectors.toList());
            authType = authList.get(0).getAuthType();
            customer = authList.get(0).getCustomer();
        }

        List<TgCustomerApplyAuthDto> tgCustomerApplyAuthDtos = new ArrayList<>();
        if (userId != null) {
            List<TgCustomerApplyAuthDto> dtoList = sysCustomerAuthService.queryListV2(userId, reqDTO.getAssetsId(), reqDTO.getIds());
            if (CollectionUtils.isNotEmpty(dtoList)) {
                tgCustomerApplyAuthDtos = Collections.singletonList(dtoList.get(0));
            }
        }

        CustomerApplyAuthList result = new CustomerApplyAuthList()
                .setUserId(userId)
                .setCustomer(customer)
                .setAuthType(authType)
                .setIds(ids)
                .setList(list)
                // 前端组件不改，这里固定一行
                .setAuthList(tgCustomerApplyAuthDtos);
        return result;
    }
}
