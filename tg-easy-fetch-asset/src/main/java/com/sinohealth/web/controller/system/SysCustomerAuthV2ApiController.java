package com.sinohealth.web.controller.system;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.core.controller.BaseController;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.enums.StatusTypeEnum;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.domain.TgCustomerApplyAuth;
import com.sinohealth.system.domain.constant.AsyncTaskConst;
import com.sinohealth.system.domain.value.deliver.DataSource;
import com.sinohealth.system.domain.value.deliver.DeliverDataSourceType;
import com.sinohealth.system.domain.value.deliver.datasource.ApplicationDataSource;
import com.sinohealth.system.domain.value.deliver.datasource.CharAnalysisDataSource;
import com.sinohealth.system.domain.value.deliver.datasource.PanelDataSource;
import com.sinohealth.system.dto.*;
import com.sinohealth.system.dto.application.DeliveryTableTaskParamVO;
import com.sinohealth.system.dto.assets.SubCustomerAssetsBatchUpdateReqDTO;
import com.sinohealth.system.dto.system.AsyncTaskDto;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.SysCustomerAuthMapper;
import com.sinohealth.system.service.*;
import com.sinohealth.system.util.ListUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 分配客户
 *
 * @author lvheng chen
 * @version 1.0
 * @date 2022-11-30 18:13
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "我的资产-交付客户管理")
@RequestMapping("/api")
public class SysCustomerAuthV2ApiController extends BaseController {

    @Autowired
    private SysCustomerAuthMapper customerAuthMapper;

    @Autowired
    private ISysCustomerAuthService customerAuthService;

    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;

    @Autowired
    private DataDeliverRecordService dataDeliverRecordService;

    private final CustomerAuthV2Service customerAuthV2Service;

    private final ArkbiAnalysisService arkbiAnalysisService;

    private final IAsyncTaskService asyncTaskService;

    
    @ApiOperation("交付客户——查询用户已授权资产")
    @PostMapping("/system/customer/auth/query")
    public AjaxResult<CustomerApplyAuthList> queryAuthList(@Valid @RequestBody CustomerApplyAuthListReqDTO reqDTO) {
        CustomerApplyAuthList result = customerAuthV2Service.queryAuthList(reqDTO);
        return AjaxResult.success(result);
    }

    
    @ApiOperation("交付客户——批量编辑授权客户权限")
    @PostMapping("/system/customer/auth/batchUpdate")
    public AjaxResult batchUpdate(@Valid @RequestBody CustomerApplyAuthUpdateReqV2DTO reqDTO) {
        customerAuthV2Service.batchUpdate(reqDTO);
        return AjaxResult.success();
    }

    
    @ApiOperation("批量编辑授权客户权限")
    @PostMapping("/system/customer/auth/batchUpdate2")
    public AjaxResult batchUpdate(@Valid @RequestBody CustomerApplyAuthBatchUpdateReqDTO reqDTO) {
        customerAuthV2Service.batchUpdate(reqDTO);
        return AjaxResult.success();
    }

    
    @ApiOperation("批量修改子账号客户报表")
    @PostMapping("/system/subCustomer/auth/batchUpdate2")
    public AjaxResult batchUpdateSubCustomerAssets(@Valid @RequestBody SubCustomerAssetsBatchUpdateReqDTO reqDTO) {
        customerAuthV2Service.batchUpdateSub(reqDTO);
        return AjaxResult.success();
    }

    
    @ApiOperation("分配客户")
    @PostMapping("/system/customer/auth/batchAdd")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult batchApply(@Valid @RequestBody CustomerApplyAuthReqV2DTO reqDTO) {
        // 参数校验
        validParams(reqDTO);

        CustomerApplyAuthRequestContext authRequestContext = CustomerApplyAuthRequestContext.build(reqDTO);
        // 业务规则校验
        validApplyRule(authRequestContext);
        // 执行分配动作
        doApply(authRequestContext);

        // 分配客户时 提交异步同步数据任务
        Optional.ofNullable(authRequestContext.getApplicationDataSources())
                .ifPresent(v -> v.forEach(applicationSource ->
                        this.saveTask(reqDTO.getPackName(), applicationSource.getName(), applicationSource.getAssetsId()))
                );

        // 图标分析、仪表板
        Optional.ofNullable(authRequestContext.getCharAnalysisDataSources()).filter(CollectionUtils::isNotEmpty)
                .ifPresent(charAnalysisDataSourceList -> {
                    List<Long> arkbiIds = charAnalysisDataSourceList.stream().map(CharAnalysisDataSource::getArkbiId).collect(Collectors.toList());
                    List<Long> assetsIds = arkbiAnalysisService.listByIds(arkbiIds).stream()
                            .filter(it -> StringUtils.isNotBlank(it.getAssetsId()))
                            .flatMap(it -> ListUtil.toList(it.getAssetsId(), Long::parseLong).stream())
                            .distinct()
                            .collect(Collectors.toList());
                    assetsIds.forEach(assetsId -> this.saveTask(reqDTO.getPackName(), "", assetsId));
                });
        Optional.ofNullable(authRequestContext.getPanelDataSources()).filter(CollectionUtils::isNotEmpty)
                .ifPresent(panelDataSourceList -> {
                    List<Long> arkbiIds = panelDataSourceList.stream().map(PanelDataSource::getArkbiId).collect(Collectors.toList());
                    List<Long> assetsIds = arkbiAnalysisService.listByIds(arkbiIds).stream()
                            .filter(it -> StringUtils.isNotBlank(it.getAssetsId()))
                            .flatMap(it -> ListUtil.toList(it.getAssetsId(), Long::parseLong).stream())
                            .distinct()
                            .collect(Collectors.toList());
                    assetsIds.forEach(assetsId -> this.saveTask(reqDTO.getPackName(), "", assetsId));
                });

        // 保存交付记录
        dataDeliverRecordService.saveApplyCustomerRecords(reqDTO, authRequestContext);
        return AjaxResult.success();

    }

    // TODO 资产版本
    private void saveTask(String packName, String applyName, Long assetsId) {
        Long userId = SecurityUtils.getUserId();
        AsyncTaskDto asyncTaskDto = new AsyncTaskDto();
        String projectName = Stream.of(packName, applyName).filter(StringUtils::isNoneBlank).collect(Collectors.joining("-"));
        asyncTaskDto.setProjectName(projectName);
        asyncTaskDto.setBusinessType(AsyncTaskConst.BUSINESS_TYPE.DELIVERY_TABLE);
        asyncTaskDto.setParamJson(JSON.toJSONString(DeliveryTableTaskParamVO.builder().assetsId(assetsId).version(null).userId(userId).build()));
        asyncTaskDto.setUserId(SecurityUtils.getUserId());
        asyncTaskService.addAsyncTask(asyncTaskDto);
    }

    private void validParams(CustomerApplyAuthReqV2DTO reqDTO) {
        Assert.isTrue(reqDTO.getAssetsId() != null || CollectionUtils.isNotEmpty(reqDTO.getIds()), "applyId或者目录id不能都为空");
        Assert.isTrue(BooleanUtils.isFalse(reqDTO.getPack()) || StringUtils.isNotBlank(reqDTO.getPackName()), "打包名称不能为空");
    }

    private void validApplyRule(CustomerApplyAuthRequestContext requestContext) {
        // 1. 校验数据是否分配给其他人，一份数据只允许分配给一个客户
        Assert.isTrue(CollectionUtils.isNotEmpty(requestContext.getAuthList())
                && requestContext.getAuthList().size() == 1, "只允许分配给一个客户");
        // 2. 已分配不允许重新分配, 排除authUser.getUserId
        Map<String, List<Long>> nodeMapParams = requestContext.getDataSources().stream()
                .collect(Collectors.groupingBy(it -> it.support().toIcon(), Collectors.mapping(DataSource::getId, Collectors.toList())));
        List<TgCustomerApplyAuth> alreadyApplyList = customerAuthMapper.getAlreadyApplyList(CustomerAuthAlreadyApplyQuery.build(nodeMapParams),
                requestContext.getAuthList().get(0).getUserId());
        if (CollectionUtils.isNotEmpty(alreadyApplyList)) {
            List<String> names = alreadyApplyList.stream().map(TgCustomerApplyAuth::getNodeName).collect(Collectors.toList());
            throw new IllegalArgumentException("以下数据资产已分配给其它客户，不允许重新分配: " + JSON.toJSONString(names));
        }
        // 3. 数据有效期校验
        if (CollectionUtils.isNotEmpty(requestContext.getApplicationDataSources())) {
            List<Long> applicationIds = requestContext.getApplicationDataSources().stream().map(ApplicationDataSource::getId).collect(Collectors.toList());

            List<UserDataAssets> list = userDataAssetsDAO.getBaseMapper().selectBatchIds(applicationIds);
            for (UserDataAssets item : list) {
                Assert.isTrue(!DateUtils.hasDataExpired(item.getDataExpire()), InfoConstants.DATA_EXPIRED);
            }
        }
    }

    /**
     * 只支持分配给一个客户了！
     *
     * @param requestContext
     */
    @Transactional(rollbackFor = Exception.class)
    public void doApply(CustomerApplyAuthRequestContext requestContext) {
        for (CustomerApplyAuthReqV2DTO.CustomerApplyAuthUserItemDTO authUser : requestContext.getAuthList()) {
            doApplyOne(requestContext, authUser);
        }
        if (CollectionUtils.isNotEmpty(requestContext.getDeleteAuthIds())) {
            customerAuthService.removeByIds(requestContext.getDeleteAuthIds());
            log.info("分配客户-删除授权IDS: {}", JSON.toJSONString(requestContext.getDeleteAuthIds()));
        }
    }

    /**
     * 图表分析特殊处理，图表分析需要挂在提数申请的孩子节点上
     * 1、 取出图表分析
     * 2、 判断请求中是否有图表分析的父节点
     * 3、 先创建父节点，再创建孩子节点
     *
     * @param requestContext
     * @param authUser
     */
    private void doApplyOne(CustomerApplyAuthRequestContext requestContext, CustomerApplyAuthReqV2DTO.CustomerApplyAuthUserItemDTO authUser) {
        TgCustomerApplyAuth rootNode = getDefaultRoot(requestContext);
        if (requestContext.getPack()) {
            rootNode = createPackNodeIfNotExists(requestContext, authUser);
        }
        List<Long> authNodeIds = requestContext.getDataSources().stream().map(DataSource::getId).distinct().collect(Collectors.toList());
        // 已授权客户资产
        Map<DeliverDataSourceType, Map<Long, TgCustomerApplyAuth>> alreadyApplyMap = customerAuthMapper
                .selectList(Wrappers.<TgCustomerApplyAuth>lambdaQuery()
                        .in(TgCustomerApplyAuth::getNodeId, authNodeIds)
                        .eq(TgCustomerApplyAuth::getUserId, authUser.getUserId())
                ).stream().collect(Collectors.groupingBy(it -> DeliverDataSourceType.fromIcon(it.getIcon()),
                        Collectors.toMap(TgCustomerApplyAuth::getNodeId, Function.identity())));

        // 将数据资产对象转换为客户资产对象
        for (Map.Entry<DataDir, List<DataDir>> parentEntry : requestContext.getDirMap().entrySet()) {
            DataDir parentDir = parentEntry.getKey();
            List<DataDir> sonDirs = parentEntry.getValue();
            TgCustomerApplyAuth applyAuth = authCustomer(parentDir, rootNode, alreadyApplyMap, authUser);
            for (DataDir sonDir : sonDirs) {
                authCustomer(sonDir, applyAuth, alreadyApplyMap, authUser);
            }
        }
    }

    private TgCustomerApplyAuth createPackNodeIfNotExists(CustomerApplyAuthRequestContext requestContext, CustomerApplyAuthReqV2DTO.CustomerApplyAuthUserItemDTO authUserItemDTO) {
        // 打包分配需要创建目录节点，校验目录节点是否已存在，不存在则新建，否则挂在原节点下
        String nodeName = requestContext.getPackName();
        Wrapper<TgCustomerApplyAuth> wrapper = Wrappers.<TgCustomerApplyAuth>lambdaQuery()
                .eq(TgCustomerApplyAuth::getNodeName, nodeName)
                .eq(TgCustomerApplyAuth::getUserId, authUserItemDTO.getUserId());
        TgCustomerApplyAuth parentNode = customerAuthService.getOne(wrapper);
        if (parentNode == null) {
            TgCustomerApplyAuth newPackNode = new TgCustomerApplyAuth();
            newPackNode.setAssetsId(null);
            newPackNode.setUserId(authUserItemDTO.getUserId());
            newPackNode.setAuthType(authUserItemDTO.getAuthType());
            newPackNode.setUpdateBy(ThreadContextHolder.getSysUser().getUserId());
            newPackNode.setUpdateTime(new Date());
            newPackNode.setStatus(1);
            newPackNode.setOutTableName(nodeName);
            newPackNode.setNodeName(nodeName);
            newPackNode.setIcon(CommonConstants.ICON_PACK);
            newPackNode.setParentId(requestContext.getParentId());
            customerAuthMapper.insert(newPackNode);
            parentNode = newPackNode;
        }
        return parentNode;
    }

    /**
     * 授权客户
     *
     * @param dataDir         需要授权的数据资产
     * @param parentNode      父节点
     * @param alreadyApplyMap 已授权的客户资产
     * @param authUser        授权客户
     */
    private TgCustomerApplyAuth authCustomer(DataDir dataDir,
                                             TgCustomerApplyAuth parentNode,
                                             Map<DeliverDataSourceType, Map<Long, TgCustomerApplyAuth>> alreadyApplyMap,
                                             CustomerApplyAuthReqV2DTO.CustomerApplyAuthUserItemDTO authUser) {
        DeliverDataSourceType type = DeliverDataSourceType.fromIcon(dataDir.getIcon());
        // 区分更新、新增
        boolean updateFlag = alreadyApplyMap.containsKey(type) && alreadyApplyMap.get(type).containsKey(dataDir.getNodeId());
        if (updateFlag) {
            TgCustomerApplyAuth alreadyApplyAuth = alreadyApplyMap.get(type).get(dataDir.getNodeId());
            alreadyApplyAuth.setAuthType(authUser.getAuthType());
            alreadyApplyAuth.setUpdateBy(ThreadContextHolder.getSysUser().getUserId());
            alreadyApplyAuth.setUpdateTime(new Date());
            customerAuthService.updateById(alreadyApplyAuth);
            log.info("分配客户-更新授权IDS: {}", JSON.toJSONString(alreadyApplyAuth));
            return alreadyApplyAuth;
        } else {
            TgCustomerApplyAuth node = buildNewNode(dataDir, authUser, parentNode);
            customerAuthService.save(node);
            log.info("分配客户-新增授权: {}", JSON.toJSONString(node));
            return node;
        }
    }

    private TgCustomerApplyAuth buildNewNode(DataDir dataDir, CustomerApplyAuthReqV2DTO.CustomerApplyAuthUserItemDTO authUser, TgCustomerApplyAuth parentNode) {
        DeliverDataSourceType type = DeliverDataSourceType.fromIcon(dataDir.getIcon());
        Assert.isTrue(type != null, "不支持的数据资产类型：" + dataDir.getIcon());
        switch (type) {
            case ASSETS: {
                TgCustomerApplyAuth newNode = new TgCustomerApplyAuth();
                newNode.setAssetsId(dataDir.getNodeId());
                newNode.setUserId(authUser.getUserId());
                newNode.setAuthType(authUser.getAuthType());
                newNode.setUpdateBy(ThreadContextHolder.getSysUser().getUserId());
                newNode.setUpdateTime(new Date());
                newNode.setStatus(StatusTypeEnum.IS_ENABLE.getId());
                // 先设置成一样的
                newNode.setOutTableName(dataDir.getDirName());
                newNode.setNodeName(dataDir.getDirName());
                newNode.setIcon(CommonConstants.ICON_DATA_ASSETS);
                newNode.setParentId(parentNode.getId());
                newNode.setNodeId(dataDir.getNodeId());
                return newNode;
            }
            case PANEL: {
                TgCustomerApplyAuth newNode = new TgCustomerApplyAuth();
                newNode.setUserId(authUser.getUserId());
                newNode.setAuthType(authUser.getAuthType());
                newNode.setUpdateBy(ThreadContextHolder.getSysUser().getUserId());
                newNode.setUpdateTime(new Date());
                newNode.setStatus(StatusTypeEnum.IS_ENABLE.getId());
                // 先设置成一样的
                newNode.setOutTableName(dataDir.getDirName());
                newNode.setNodeName(dataDir.getDirName());
                newNode.setIcon(CommonConstants.ICON_DASHBOARD);
                newNode.setParentId(parentNode.getId());
                newNode.setNodeId(dataDir.getNodeId());
                return newNode;
            }
            case CHART_ANALYSIS: {
                TgCustomerApplyAuth newNode = new TgCustomerApplyAuth();
                newNode.setUserId(authUser.getUserId());
                newNode.setAuthType(authUser.getAuthType());
                newNode.setUpdateBy(ThreadContextHolder.getSysUser().getUserId());
                newNode.setUpdateTime(new Date());
                newNode.setStatus(StatusTypeEnum.IS_ENABLE.getId());
                // 先设置成一样的
                newNode.setOutTableName(dataDir.getDirName());
                newNode.setNodeName(dataDir.getDirName());
                newNode.setIcon(CommonConstants.ICON_CHART);
                newNode.setParentId(parentNode.getId());
                newNode.setNodeId(dataDir.getNodeId());
                return newNode;
            }
            default:
                throw new IllegalArgumentException("不支持的数据资产" + type.name());
        }
    }

    private TgCustomerApplyAuth getDefaultRoot(CustomerApplyAuthRequestContext requestContext) {
        TgCustomerApplyAuth root = new TgCustomerApplyAuth();
        root.setId(requestContext.getParentId());
        return root;
    }

}
