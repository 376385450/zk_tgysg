package com.sinohealth.system.service.impl;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.sinohealth.arkbi.api.ExtAnalysisUserApi;
import com.sinohealth.arkbi.api.FileServiceApi;
import com.sinohealth.arkbi.param.*;
import com.sinohealth.arkbi.response.ApiResponseData;
import com.sinohealth.arkbi.vo.*;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.redis.RedisKeys;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.StopWatch;
import com.sinohealth.common.utils.dto.Node;
import com.sinohealth.common.utils.spring.SpringUtils;
import com.sinohealth.sca.base.basic.util.CollectionsUtils;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.arkbi.service.ArkBiService;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsBiViewDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsSnapshotDAO;
import com.sinohealth.system.biz.dataassets.domain.AssetsVersion;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssets;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsBiView;
import com.sinohealth.system.biz.dataassets.domain.UserDataAssetsSnapshot;
import com.sinohealth.system.biz.dataassets.dto.UserDataAssetsSyncDTO;
import com.sinohealth.system.biz.dataassets.dto.request.DataDirRequest;
import com.sinohealth.system.biz.dataassets.dto.request.UserDataAssetsSyncRequest;
import com.sinohealth.system.biz.dataassets.service.UserDataAssetsService;
import com.sinohealth.system.biz.dir.dto.GetMyDataDirTreeParam;
import com.sinohealth.system.config.ThreadPoolType;
import com.sinohealth.system.dao.ApplicationDataUpdateRecordDAO;
import com.sinohealth.system.dao.DataDirDAO;
import com.sinohealth.system.domain.ApplicationDataUpdateRecord;
import com.sinohealth.system.domain.ArkbiAnalysis;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.domain.constant.SyncTargetType;
import com.sinohealth.system.domain.constant.UpdateRecordStateType;
import com.sinohealth.system.dto.*;
import com.sinohealth.system.dto.application.TgNodeMapping;
import com.sinohealth.system.dto.application.deliver.update.UpdateRecordStatusParam;
import com.sinohealth.system.dto.table_manage.DataManageFormDto;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.*;
import com.sinohealth.system.service.*;
import com.sinohealth.system.util.TreeUtils;
import com.sinohealth.system.vo.ApplicationSelectListVo;
import com.sinohealth.system.vo.ArkBIEditVo;
import feign.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据目录Service业务层处理
 *
 * @author jingjun
 * @date 2021-04-16
 */
@Slf4j
@Service
public class MyDataDirServiceImpl extends ServiceImpl<DataDirMapper, DataDir> implements IMyDataDirService {
    @Autowired
    private ITableInfoService tableInfoService;

    @Autowired
    ISysUserService userService;

    @Autowired
    private DataDirMapper dataDirMapper;
    @Autowired
    private DataDirDAO dataDirDAO;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TgNodeMappingMapper tgNodeMappingMapper;

    @Autowired
    private ArkbiAnalysisMapper arkbiAnalysisMapper;

    @Autowired
    private ExtAnalysisUserApi extAnalysisUserApi;

    @Autowired
    private FileServiceApi fileServiceApi;

    @Autowired
    private ArkbiAnalysisService arkbiAnalysisService;

    @Value("${arkbi.source-id.lan}")
    private String sourceIdLan;

    @Value("${arkbi.source-id.wlan}")
    private String sourceIdWlan;

    @Autowired
    private ApplicationDataUpdateRecordDAO applicationDataUpdateRecordDAO;
    @Autowired
    private DefaultSyncHelper defaultSyncHelper;
    @Autowired
    private IntergrateProcessDefService intergrateProcessDefService;
    @Autowired
    private SysCustomerAuthMapper customerAuthMapper;
    @Autowired
    TgCkProviderMapper tgCkProviderMapper;
    @Autowired
    private ApplicationDataUpdateRecordDAO dataUpdateRecordDAO;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private UserDataAssetsSnapshotDAO userDataAssetsSnapshotDAO;
    @Autowired
    private UserDataAssetsBiViewDAO userDataAssetsBiViewDAO;
    @Autowired
    private UserDataAssetsService userDataAssetsService;
    @Autowired
    private ArkBiService arkBiService;

    @Resource
    @Qualifier(ThreadPoolType.SYNC_CK)
    private ThreadPoolTaskExecutor pool;


    /*****************************************************
     * 天宫易数阁代码
     * 责任人: linweiwu
     * @return
     */

    @Override
    public Integer newDir(DataDir dataDir) {
        dataDir.setApplicantId(ThreadContextHolder.getSysUser().getUserId());
        dataDir.setIcon(CommonConstants.ICON_FILE);
        return dataDirMapper.insertAndGetId(dataDir, CommonConstants.MY_DATA_DIR);
    }

    @Override
    public int update(DataDir dataDir) {
        return dataDirMapper.updateById(dataDir);
    }

    /**
     * 我的数据目录编辑
     * 1、 可以编辑所有类型的节点
     * 2、
     *
     * @param reqDTO
     * @return
     */
    @Override
    public void updateV2(DataDirUpdateReqDTO reqDTO) {
        List<DataDirDto> reqTree = DataDirUpdateReqDTO.getDtoTree(reqDTO.getList(), 0L);
        List<Long> saveIds = reqTree.stream().map(DataDirDto::getId).collect(Collectors.toList());

        // 参数校验
        List<DataDirDto> flat = reqDTO.flat(reqTree);
        flat.forEach(dir -> {
            Assert.isTrue(StringUtils.isNotBlank(dir.getIcon()), String.format("目录名称:%s, 类型icon参数缺失", dir.getDirName()));
        });

        // 这里查询列表用与前端相同的接口
        List<DataDirDto> normal = getDirTreeGroup(0L, null, null, null, null, "normal", null, null, null);
        List<Node> myDataDirList = Node.flat(normal);
        SpringUtils.getBean(MyDataDirServiceImpl.class).doUpdateV2(reqTree, flat, myDataDirList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void doUpdateV2(List<DataDirDto> reqTree, List<DataDirDto> flat, List<Node> myDataDirList) {
        // 先编辑、新增
        this.doUpdateV2(reqTree, null, 1);
        Map<Long, DataDirDto> dirDtoMap = flat.stream().distinct().collect(Collectors.toMap(DataDirDto::getId, Function.identity()));
        // 再删除
        // 校验
        // 如果删除目录，目录下面有数据流
        // 数据库里有，但是参数里没有，说明是删除动作
        String errorDeleteDirNameList = myDataDirList.stream()
                .filter(node -> StringUtils.equals(node.getIcon(), CommonConstants.ICON_FILE))
                .filter(node -> !dirDtoMap.containsKey(node.getId()))
                .filter(node -> {
                    // 这里强转，是因为如果是目录那么就是DataDirDto类型
                    Integer tableNums = Optional.ofNullable(((DataDirDto) node).getTableNums()).orElse(0);
                    return tableNums > 0;
                })
                .map(Node::getNodeViewName)
                .collect(Collectors.joining(","));
        Assert.isTrue(StringUtils.isBlank(errorDeleteDirNameList), String.format("以下目录关联表数量大于0无法删除: %s", errorDeleteDirNameList));

        List<Long> deleteDirIdList = myDataDirList.stream()
                .filter(Objects::nonNull)
                .map(Node::getId)
                .filter(id -> !dirDtoMap.containsKey(id))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteDirIdList)) {
            List<DataDir> dirs = dataDirMapper.selectBatchIds(deleteDirIdList);
            if (CollectionUtils.isEmpty(dirs)) {
                return;
            }
            List<Long> needDeleteIds = dirs.stream().filter(v -> Objects.equals(v.getIcon(), CommonConstants.ICON_FILE))
                    .map(DataDir::getId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(needDeleteIds)) {
                return;
            }
            needDeleteIds.forEach(this::delete);
            log.info(">> 编辑我的数据目录，删除目录列表{}", JSON.toJSONString(deleteDirIdList));
        }
    }


    private void doUpdateV2(List<DataDirDto> list, DataDirDto parent, int level) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        if (level > 4) {
            // 文件夹3层+叶子节点=4层
            throw new IllegalArgumentException("我的数据目录不能超过4层");
        }
        int sort = 2;
        for (DataDirDto dataDirDto : list) {
            boolean isNew = dataDirDto.getId() == null;
            boolean isUpdate = !isNew;
            final long parentId = parent == null ? 0 : parent.getId();
            if (isNew) {
                // 新增目录
                DataDir newEntity = new DataDir();
                newEntity.setParentId(parentId);
                newEntity.setLastUpdate(new Date());
                newEntity.setDirName(dataDirDto.getDirName());
                newEntity.setDatasourceId(0);
                newEntity.setStatus(1);
                newEntity.setTarget(CommonConstants.MY_DATA_DIR);
                newEntity.setApplicantId(SecurityUtils.getUserId());
                newEntity.setIcon(CommonConstants.ICON_FILE);
                newEntity.setSort(sort);
                newEntity.setComment(dataDirDto.getComment());
                dataDirDAO.save(newEntity);
                dataDirDto.setId(newEntity.getId());

                List<DataDirDto> children = dataDirDto.getChildren();
                if (CollectionUtils.isNotEmpty(children)) {
                    for (DataDirDto child : children) {
                        child.setParentId(dataDirDto.getId());
                    }
                }
            }
            if (isUpdate) {
                if (log.isDebugEnabled()) {
                    DataDir originDir = dataDirDAO.getById(dataDirDto.getId());
                    if (!Objects.equals(originDir.getParentId(), dataDirDto.getParentId())) {
                        log.debug("目录修改");
                    }
                }
                DataDir updateEntity = new DataDir();
                updateEntity.setId(dataDirDto.getId());
                updateEntity.setDirName(dataDirDto.getDirName());
                updateEntity.setParentId(dataDirDto.getParentId());
                updateEntity.setLastUpdate(new Date());
                updateEntity.setSort(sort);
                updateEntity.setComment(dataDirDto.getComment());
                updateEntity.setMoved(CommonConstants.MOVED);
                dataDirDAO.updateById(updateEntity);
            }
            sort += 2;
            doUpdateV2(dataDirDto.getChildren(), dataDirDto, level + 1);
        }
    }

    /**
     * 我的数据 仅目录节点
     */
    public List<DataDirDto> getMyDataDirTreeGroup() {
        List<DataDir> dirList = dataDirMapper.selectList(new QueryWrapper<DataDir>().lambda()
                .eq(DataDir::getIcon, CommonConstants.ICON_FILE)
                .eq(DataDir::getApplicantId, SecurityUtils.getUserId())
                .eq(DataDir::getTarget, CommonConstants.MY_DATA_DIR)
                .eq(DataDir::getStatus, 1));
        List<Node> treeData = dirList.stream().map(v -> {
            DataDirDto dto = new DataDirDto();
            BeanUtils.copyProperties(v, dto);
            dto.setNodeViewName(v.getDirName());
            return dto;
        }).collect(Collectors.toList());

        Map<Long, Node> allNodes = dataDirMapper.selectTreeData(CommonConstants.MY_DATA_DIR, ThreadContextHolder.getSysUser().getUserId(),
                        null, null, null, null, null, null)
                .stream().collect(Collectors.toMap(Node::getId, n -> n));

        List result = TreeUtils.transformTreeGroup(0L, treeData, allNodes);
        TreeUtils.traversalTree(result, null);
        TreeUtils.bfsSort(result);
        return result;
    }

    @Override
    public AjaxResult<Void> syncAssetsToBiView(UserDataAssetsSyncRequest request) {
        if (CollectionUtils.isEmpty(request.getAssets())) {
            return AjaxResult.error("请选择资产");
        }
        Long userId = SecurityUtils.getUserId();
        List<String> versions = Lambda.buildList(request.getAssets(), UserDataAssetsSyncDTO::getAssetsVersion);
        List<UserDataAssetsBiView> exist = userDataAssetsBiViewDAO.queryByAssetsVersion(versions);
        Set<String> existVersions = Lambda.buildSet(exist, UserDataAssetsBiView::getAssetsVersion);
        List<UserDataAssetsSyncDTO> needSyncList = request.getAssets().stream()
                .filter(v -> !existVersions.contains(v.getAssetsVersion())
                        && BooleanUtils.isNotTrue(redisTemplate.opsForSet().isMember(RedisKeys.Assets.SYNC_HANDLE_CACHE, v.getAssetsVersion())))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(needSyncList)) {
            return AjaxResult.error("请选择未同步的资产");
        }

        request.setAssets(needSyncList);
        versions = Lambda.buildList(needSyncList, UserDataAssetsSyncDTO::getAssetsVersion);
        if (CollectionUtils.isNotEmpty(versions)) {
            redisTemplate.opsForSet().add(RedisKeys.Assets.SYNC_HANDLE_CACHE, versions.toArray());
            log.warn("cache: ={}", versions.toArray());
        }

        pool.submit(() -> {
            try {
                UserBaseInfoVo biUser = arkBiService.getBIUserBaseInfoVo(userId);
                CreateViewParam param = new CreateViewParam();

                Set<Long> assetsIds = Lambda.buildSet(request.getAssets(), UserDataAssetsSyncDTO::getAssetsId);
                List<UserDataAssets> assets = userDataAssetsDAO.getBaseMapper().selectList(new QueryWrapper<UserDataAssets>().lambda()
                        .select(UserDataAssets::getId, UserDataAssets::getProjectName)
                        .in(UserDataAssets::getId, assetsIds)
                );
                Map<Long, String> projectMap = Lambda.buildMap(assets, UserDataAssets::getId, UserDataAssets::getProjectName);

                List<ExtViewInfo> infos = request.getAssets().stream().map(v -> {
                    try {
                        String sql = this.syncThenPushTableSqlByAssets(v.getAssetsId(), v.getVersion(), userId);
                        ExtViewInfo extViewInfo = new ExtViewInfo();
                        extViewInfo.setExtViewId(String.valueOf(v.getAssetsVersion()));
                        extViewInfo.setExtViewName(AssetsVersion.buildViewName(projectMap.get(v.getAssetsId()), v.getVersion()));
                        extViewInfo.setSourceId(sourceIdLan);
                        extViewInfo.setViewSql(sql);
                        return extViewInfo;
                    } catch (Exception e) {
                        log.error("", e);
                        return null;
                    } finally {
                        redisTemplate.opsForSet().remove(RedisKeys.Assets.SYNC_HANDLE_CACHE, v.getAssetsVersion());
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
                param.setExtViewInfo(infos);

                ApiResponseData<List<ViewVo>> createResult = extAnalysisUserApi.createView(biUser.getLoginToken(), param);
                log.warn("bi create view: createResult={}", createResult);
                if (!createResult.isSuccess()) {
                    return;
                }

                List<ViewVo> views = createResult.getData();
                List<UserDataAssetsBiView> mapping = views.stream()
                        .map(v -> {
                            Pair<Long, Integer> valPair = AssetsVersion.parseAssetsVersion(v.getExtViewId());
                            UserDataAssetsBiView biView = new UserDataAssetsBiView().setViewId(v.getId());
                            biView.setAssetsId(valPair.getKey());
                            biView.setVersion(valPair.getValue());
                            return biView;
                        })
                        .collect(Collectors.toList());
                userDataAssetsBiViewDAO.saveBatch(mapping);
            } catch (Exception e) {
                log.error("", e);
            }
        });

        return AjaxResult.succeed();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DataDirDto> getDirTreeGroup(Long id, Integer searchStatus, String searchProjectName,
                                            String searchBaseTable, Long baseTableId, String expireType,
                                            String clientNames, Integer requireTimeType, Integer requireAttr) {
        StopWatch watch = new StopWatch();
        Map<Long, Node> allNodes = dataDirMapper.selectTreeData(CommonConstants.MY_DATA_DIR, ThreadContextHolder.getSysUser().getUserId(),
                        null, null, null, null, null, null)
                .stream().collect(Collectors.toMap(Node::getId, n -> n));

        watch.start("list");
        // 获取目录数据
        List<Node> treeData = dataDirMapper.selectTreeData(
                CommonConstants.MY_DATA_DIR, ThreadContextHolder.getSysUser().getUserId(),
                searchStatus, searchProjectName, searchBaseTable, baseTableId, null, expireType);
        watch.stop();

        watch.start("apply");
//        this.addApplicationNode(treeData);
        watch.stop();

        watch.start("bi chart");
//        this.addBiChartNode(treeData, CommonConstants.ICON_CHART);
        watch.stop();

        watch.start("bi dashboard");
//        this.addBiChartNode(treeData, CommonConstants.ICON_DASHBOARD);
        watch.stop();

        watch.start("tree");
        // 建立树形结构
        List result = TreeUtils.transformTreeGroup(id, treeData, allNodes);
        watch.stop();

        log.warn("TREE {}ms {}", watch.getTotalTimeMillis(), watch.prettyPrint());
//        TreeUtils.traversalTree(result, null);
//        TreeUtils.bfsSort(result);
        TreeUtils.filterNode(result, Lists.newLinkedList(),
                GetMyDataDirTreeParam.builder().requireTimeType(requireTimeType).requireAttr(requireAttr).clientNames(clientNames).build());
        return result;
    }

    // NOW 删除引用
    //    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public List<DataDirDto> getDirTreeGroup(Long id, Integer searchStatus, String searchProjectName,
//                                            String searchBaseTable, Long baseTableId, String expireType,
//                                            String clientNames, Integer requireTimeType, Integer requireAttr) {
//        StopWatch watch = new StopWatch();
//
//        Map<Long, Node> allNodes = dataDirMapper.selectTreeData(CommonConstants.MY_DATA_DIR, ThreadContextHolder.getSysUser().getUserId(),
//                null, null, null, null, null, null).stream().collect(Collectors.toMap(n -> n.getId(), n -> n));
//
//        watch.start("list");
//        // 获取目录数据
//        List<Node> treeData = dataDirMapper.selectTreeData(
//                CommonConstants.MY_DATA_DIR, ThreadContextHolder.getSysUser().getUserId(),
//                searchStatus, searchProjectName, searchBaseTable, baseTableId, null, expireType);
//        watch.stop();
//
//        watch.start("apply");
//        this.addApplicationNode(treeData);
//        watch.stop();
//
//        watch.start("bi chart");
//        this.addBiChartNode(treeData, CommonConstants.ICON_CHART);
//        watch.stop();
//
//        watch.start("bi dashboard");
//        this.addBiChartNode(treeData, CommonConstants.ICON_DASHBOARD);
//        watch.stop();
//
//        watch.start("tree");
//        // 建立树形结构
//        List result = TreeUtils.transformTreeGroup(id, treeData, allNodes);
//        watch.stop();
//
//        log.warn("TREE {}ms {}", watch.getTotalTimeMillis(), watch.prettyPrint());
//        TreeUtils.traversalTree(result, null);
//        TreeUtils.bfsSort(result);
//        TreeUtils.filterNode(result, Lists.newLinkedList(),
//                GetMyDataDirTreeParam.builder().requireTimeType(requireTimeType).requireAttr(requireAttr).clientNames(clientNames).build());
//        return result;
//        return Collections.emptyList();
//    }

    @Override
    public List<DataDirDto> getDirTree(Long applyId) {
        Wrapper<DataDir> wrapper = Wrappers.lambdaQuery(DataDir.class)
                .eq(DataDir::getNodeId, applyId)
                .eq(DataDir::getTarget, CommonConstants.MY_DATA_DIR)
                .eq(DataDir::getApplicantId, ThreadContextHolder.getSysUser().getUserId())
                .eq(DataDir::getIcon, CommonConstants.ICON_DATA_ASSETS);
        DataDir dataDir = dataDirMapper.selectOne(wrapper);
        Assert.isTrue(dataDir != null, "目录不存在");
        Long parentId = dataDir.getParentId();
        if (parentId == 0) {
            DataDirDto root = new DataDirDto();
            root.setId(0L);
            root.setDirName("无项目");
            return Arrays.asList(root);
        } else {
            DataDir parentDir = dataDirMapper.selectById(parentId);
            DataDirDto parentDirDTO = new DataDirDto();
            BeanUtils.copyProperties(parentDir, parentDirDTO);
            return Arrays.asList(parentDirDTO);
        }
    }

    @Override
    @Deprecated
    public List<ApplicationSelectListVo> getApplicationList() {
        DataDirRequest dataDirRequest = new DataDirRequest();
        dataDirRequest.setApplicantId(ThreadContextHolder.getSysUser().getUserId());
        dataDirRequest.setIcon(CommonConstants.ICON_DATA_ASSETS);
        dataDirRequest.setTarget(CommonConstants.MY_DATA_DIR);

        List<Node> nodes = dataDirMapper.selectAssetsData(dataDirRequest);

        Set<Long> assetsIds = nodes.stream().map(v -> (DataDirDto) v).map(DataDirDto::getNodeId).collect(Collectors.toSet());
        Map<Long, ApplicationDataUpdateRecord> dataUpdateRecordMap = dataUpdateRecordDAO.queryLatestByAssetIds(assetsIds);
        // 获取当前申请人的申请文件数据
        List<UserDataAssets> tgApplicationInfos = userDataAssetsService.queryAllValidAssetsByUserId(false, false);
        LocalDateTime now = LocalDateTime.now();
        return tgApplicationInfos.stream()
                .map(v -> this.toApplicationSelectListVo(v, dataUpdateRecordMap))
                .filter(t -> {
                    if (Objects.isNull(t.getDataExpire())) {
                        return false;
                    }
                    return t.getDataExpire().isAfter(now);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void downloadBIFile(String extAnalysisId, DownloadFileType downloadFileType, OutputStream outputStream) throws Exception {
        Long userId = ThreadContextHolder.getSysUser().getUserId();
        UserBaseInfoVo biUser = arkBiService.getBIUserBaseInfoVo(userId);
        Response response = fileServiceApi.downloadFile(biUser.getLoginToken(), extAnalysisId, downloadFileType, null);
        try (InputStream inputStream = response.body().asInputStream()) {
            IOUtils.copy(inputStream, outputStream);
        }
    }

    private ApplicationSelectListVo toApplicationSelectListVo(UserDataAssets info,
                                                              Map<Long, ApplicationDataUpdateRecord> recordMap) {
        ApplicationSelectListVo vo = new ApplicationSelectListVo();
        vo.setId(info.getId());
        vo.setProjectName(info.getProjectName());
        vo.setDataExpire(info.getDataExpire());
        vo.setUpdateState(Optional.ofNullable(recordMap.get(info.getId()))
                .map(ApplicationDataUpdateRecord::getUpdateState).orElse(UpdateRecordStateType.NONE));
        return vo;
    }


    /**
     * @see ApplicationDataDirItemDto
     */
//    private void addApplicationNode(List<Node> treeData) {
//        StopWatch watch = new StopWatch();
//        watch.start("list");
//        // 获取当前申请人的申请文件数据
//        List<TgApplicationInfo> tgApplicationInfos = applicationService
//                .queryApplicationByUserId(ApplicationConst.ApplicationType.DATA_APPLICATION);
//        if (CollectionUtils.isEmpty(tgApplicationInfos)) {
//            return;
//        }
//
//        Map<Long, String> tableNameMap = new HashMap<>();
//        List<Long> tableIds = tgApplicationInfos.stream().map(TgApplicationInfo::getBaseTableId).distinct().collect(Collectors.toList());
//        if (CollectionUtils.isNotEmpty(tableIds)) {
//            List<TableInfo> infos = tableInfoService.getBaseMapper().selectBatchIds(tableIds);
//            tableNameMap = infos.stream().collect(Collectors.toMap(TableInfo::getId, TableInfo::getTableAlias, (front, current) -> current));
//        }
//
//        List<Long> applyIds = tgApplicationInfos.stream().map(TgApplicationInfo::getId).collect(Collectors.toList());
//        List<ApplicationDataUpdateRecord> updateRecords = dataUpdateRecordDAO.queryCustomerByApplyIds(applyIds);
//        Map<Long, ApplicationDataUpdateRecord> latestSuccessMap = updateRecords.stream()
//                .filter(v -> Objects.equals(v.getUpdateState(), UpdateRecordStateType.SUCCESS))
//                .collect(Collectors.toMap(ApplicationDataUpdateRecord::getApplicationId, v -> v, (front, current) -> {
//                    if (front.getCreateTime().before(current.getCreateTime())) {
//                        return current;
//                    }
//                    return front;
//                }));
//        watch.stop();
//
//        watch.start("date");
//
//        List<String> tables = tgApplicationInfos.stream().map(TgApplicationInfo::getBaseTableName).distinct().collect(Collectors.toList());
//        List<TableUpdateTimeEntity> tableUpdateTimeEntities = tgCkProviderMapper.selectMaxTime(tables);
//        Map<String, Date> updateMap = tableUpdateTimeEntities.stream().collect(Collectors
//                .toMap(TableUpdateTimeEntity::getCkTableName, TableUpdateTimeEntity::getUpdateTime, (front, current) -> current));
////        tgApplicationInfos.stream().map(TgApplicationInfo::getBaseTableName).distinct()
////                .forEach(v -> {
////                    try {
////                        String tab = com.sinohealth.common.utils.StringUtils.replaceLast(v, "_local", "_shard");
////                        Date lastDate = tgCkProviderMapper.selectLastSuccessTime(tab);
////                        updateMap.put(v, lastDate);
////                    } catch (Exception e) {
////                        log.error("", e);
////                    }
////                });
//        watch.stop();
//
////        List<Long> applyIds = tgApplicationInfos.stream().map(TgApplicationInfo::getId).collect(Collectors.toList());
////        // 对客户的映射表
////        List<TgTableApplicationMappingInfo> mappingInfos = tgTableApplicationMappingInfoDAO.list(applyIds);
////
////        Map<Long, Date> syncCustomerMap = mappingInfos.stream().collect(Collectors.toMap(TgTableApplicationMappingInfo::getApplicationId,
////                TgTableApplicationMappingInfo::getDateUpdateTime, (front, current) -> current));
//
//        watch.start("query map");
//        // 获取申请 mapping, mapping 实际上是节点占位符映射
//        // mapping 中不存在的节点写入 mapping, mapping 中多余的节点删除
//        List<TgNodeMapping> mappings = tgNodeMappingMapper.queryApplicationMappingByApplicantId(ThreadContextHolder.getSysUser().getUserId(), null);
//        List<Long> ids = tgApplicationInfos.stream().map(TgApplicationInfo::getTemplateId).distinct().collect(Collectors.toList());
//        List<TgTemplateInfo> templateInfos = templateService.queryNameByIds(ids);
//        Map<Long, String> nameMap = templateInfos.stream().collect(Collectors.toMap(TgTemplateInfo::getId, TgTemplateInfo::getTemplateName, (front, current) -> current));
//        watch.stop();
//
//        watch.start("build tree");
//        for (TgApplicationInfo a : tgApplicationInfos) {
//            Optional<TgNodeMapping> mapping = mappings.stream().filter((m) -> m.getNodeId().equals(a.getId())
//                    && m.getIcon().equals(CommonConstants.ICON_FORM)).findFirst();
//
//            // 如果没有映射并且申请通过, 将其加入 TreeNode 和 Mapping
//            if (!mapping.isPresent() && a.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.AUDIT_PASS)) {
//                // mappings 里不存在对应的节点, 新增 DataDirDto 并且将关系加入 mapping, 最后组合 ApplicationDataDirItemDto
//                ApplicationDataDirItemDto item = this.addNewTreeNodeAndMapping(treeData, a);
//                fillCalculateValue(tableNameMap, latestSuccessMap, updateMap, nameMap, a, item);
//            } else if (mapping.isPresent() && treeData.size() > 0) {
//                // mappings 里存在对应节点, 组合 ApplicationDataDirItemDto
//                ApplicationDataDirItemDto item = null;
//                if (a.getStatus().equals(CommonConstants.NORMAL)) {
//                    item = this.replaceNormalTreeNode(treeData, a, mapping.get());
//                } else {
//                    item = this.replaceAbnormalTreeNode(treeData, a, mapping.get());
//                }
//                fillCalculateValue(tableNameMap, latestSuccessMap, updateMap, nameMap, a, item);
//            }
//        }
//        watch.stop();
//        log.info("APP ={}", watch.prettyPrint());
//    }

    /**
     * 填充需要特殊处理的字段
     */
//    private void fillCalculateValue(Map<Long, String> tableNameMap,
//                                    Map<Long, ApplicationDataUpdateRecord> latestSuccessMap,
//                                    Map<String, Date> updateMap,
//                                    Map<Long, String> nameMap,
//                                    TgApplicationInfo a,
//                                    ApplicationDataDirItemDto item) {
//        if (Objects.isNull(item)) {
//            return;
//        }
//        Long applyId = a.getId();
//        Date lastDate = updateMap.get(a.getBaseTableName());
//
//        if (Objects.isNull(lastDate)) {
//            item.setNeedUpdate(false);
//        } else {
//            Boolean needUpdate = Optional.ofNullable(latestSuccessMap.get(applyId)).map(v -> v.getCreateTime()
//                    .before(lastDate)).orElse(false);
//            item.setNeedUpdate(needUpdate);
//        }
//
//        item.setTemplateName(nameMap.get(a.getTemplateId()));
//        item.setTableAlias(tableNameMap.get(a.getBaseTableId()));
//
//        Integer currentAuditProcessStatus = a.getCurrentAuditProcessStatus();
//        if (Objects.equals(currentAuditProcessStatus, ApplicationConst.AuditStatus.AUDIT_PASS)) {
//            item.setViewStatus(a.getStatus());
//        } else {
//            item.setViewStatus(ApplicationConst.ApplyViewStatusType.AUDITING);
//        }
//
//        //预览：【状态】=“启用”
//        //交付：【申请人】=当前用户 且 【状态】=“启用”
//        //更新：【申请人】=当前用户 且 【状态】=“启用” 且 该份数据已分配至客户账号  且  该份申请数据对应宽表有最新“成功”状态的同步任务
//        //更多：【更多】中可查看的操作功能>=1,则展示
//        //数据说明文档：【申请人】=当前用户 且 【状态】=“启用”
//        //交付记录：【申请人】=当前用户 且 【状态】=“启用”
//        //重新申请：【申请人】=当前用户 且 【状态】=“启用”  且  （该份数据≠另存的项目数据 或 该份数据≠BI分析后的图表/仪表板）
//        //复制项目：【申请人】=当前用户 且 【状态】=“启用”  且  该份数据是BI分析后的图表/仪表板
//        //删除：【申请人】=当前用户 且 【状态】=“启用”  且 （该份数据=另存的项目数据 或 该份数据=BI分析后的图表/仪表板） 且 数据分配客户次数=0
//        Long userId = SecurityUtils.getUserId();
//        List<Integer> actions = new ArrayList<>();
//        if (Objects.equals(item.getViewStatus(), ApplicationConst.ApplyStatus.ENABLE)) {
//            actions.add(DataDirConst.ActionType.PREVIEW);
//            if (Objects.equals(a.getOwnerId(), userId)) {
//                actions.add(DataDirConst.ActionType.DELIVER);
//                actions.add(DataDirConst.ActionType.DOC);
//                actions.add(DataDirConst.ActionType.DELIVER_RECORD);
//
//                if (BooleanUtils.isNotTrue(a.getCopy())) {
//                    actions.add(DataDirConst.ActionType.RE_APPLY);
//                } else {
//                    // 分配客户为0
//                    DataDeliverCustomerEventRequest request = new DataDeliverCustomerEventRequest();
//                    request.setApplicationId(applyId);
//                    Page<TgDeliverCustomerRecordDTO> pageData = deliverCustomerRecordDAO.queryParentList(request);
//                    if (Objects.isNull(pageData) || pageData.isEmpty()) {
//                        actions.add(DataDirConst.ActionType.DELETE);
//                    }
//                }
//            }
//            if (item.getNeedUpdate()) {
//                actions.add(DataDirConst.ActionType.UPDATE);
//            }
//        }
//        item.setActions(actions);
//    }

//    /**
//     * @see ArkbiChartDataDirItemDto
//     */
//    private void addBiChartNode(List<Node> treeData, String iconType) {
//        Map<Long, Node> nodeMap = treeData.stream().collect(Collectors.toMap(Node::getId, Function.identity()));
//        Long userId = ThreadContextHolder.getSysUser().getUserId();
//        // 获取当前申请人的BI图表
//        List<ArkbiAnalysis> arkbiAnalyses = arkbiAnalysisService
//                .lambdaQuery()
//                .eq(ArkbiAnalysis::getCreateBy, userId)
//                .eq(ArkbiAnalysis::getStatus, 1)
//                .eq(ArkbiAnalysis::getType, iconType)
//                .list();
//        // 获取申请 mapping, mapping 实际上是节点占位符映射
//        // mapping 中不存在的节点写入 mapping, mapping 中多余的节点删除
//        Map<Long, TgNodeMapping> nodeIdMapping = tgNodeMappingMapper.queryArkBIMapping(userId, iconType)
//                .stream()
//                .collect(Collectors.toMap(TgNodeMapping::getNodeId, Function.identity()));
//
//        //拿到所有项目的ID
//        List<Long> applicationIds = arkbiAnalyses.stream()
//                .map(ArkbiAnalysis::getApplicationId)
//                .map(ids -> Splitter.on(",").splitToList(ids))
//                .flatMap(Collection::stream)
//                .filter(StringUtils::isNotBlank)
//                .map(Long::valueOf)
//                .collect(Collectors.toList());
//
//        List<TgApplicationInfo> apps = applicationService.queryByIds(applicationIds);
//        Map<Long, TgApplicationInfo> applicationInfoMap = apps.stream()
//                .collect(Collectors.toMap(TgApplicationInfo::getId, Function.identity()));
//
//        for (ArkbiAnalysis info : arkbiAnalyses) {
//            Optional<Node> node = Optional.ofNullable(info.getId())
//                    .filter(nodeIdMapping::containsKey)
//                    //拿到数据映射信息
//                    .map(nodeIdMapping::get)
//                    //拿到数据目录ID
//                    .map(TgNodeMapping::getDirItemId)
//                    .filter(nodeMap::containsKey)
//                    //拿到目录节点,接下来对这个节点进行替换
//                    .map(nodeMap::get);
//
//            // 如果没有映射并且申请通过, 将其加入 TreeNode 和 Mapping
//            if (node.isPresent() && treeData.size() > 0) {
//                // mappings 里存在对应节点, 组合 ApplicationDataDirItemDto
//                replaceNormalTreeNode(treeData, applicationInfoMap, node.get(), info, iconType);
//            }
//        }
//    }

//    private void replaceNormalTreeNode(List<Node> treeData, Map<Long, TgApplicationInfo> applicationInfoMap,
//                                       Node node, ArkbiAnalysis analysis, String iconType) {
////        String icon = analysis.getStatus().equals(CommonConstants.NORMAL) ? iconType : CommonConstants.ICON_INVALID_FORM;
//        String icon = iconType;
//        ArkbiChartDataDirItemDto treeNode = new ArkbiChartDataDirItemDto();
//        BeanUtils.copyProperties(analysis, treeNode);
//        BeanUtils.copyProperties(node, treeNode);
//
//        //赋值关联项目名称
//        String ids = analysis.getApplicationId();
//        if (StringUtils.isNotBlank(ids)) {
//            List<String> appIds = Splitter.on(",").splitToList(ids);
//            List<TgApplicationInfo> infos = appIds.stream()
//                    .filter(StringUtils::isNotBlank)
//                    .map(Long::valueOf)
//                    .filter(applicationInfoMap::containsKey)
//                    .map(applicationInfoMap::get)
//                    .filter(Objects::nonNull).collect(Collectors.toList());
//            infos.forEach(o -> {
//                treeNode.getProjectNames().add(o.getProjectName());
//                treeNode.getApplicationIds().add(o.getId());
//                treeNode.getApplicantIds().add(o.getApplicantId());
//            });
//            infos.stream().map(TgApplicationInfo::getDataExpir).filter(Objects::nonNull)
//                    .min(Comparator.comparing(v -> v))
//                    .ifPresent(v -> treeNode.setDataExpir(DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", v)));
//        }
//
//        treeNode.setActions(DataDirConst.ActionType.BI_DEFAULT_ACTIONS);
//
//        treeNode.setExtAnalysisId(analysis.getAnalysisId());
//        treeNode.setIcon(icon);
//        // 展示节点的名称，有需要则使用
//        treeNode.setNodeViewName("");
//        treeData.remove(node);
//        treeData.add(treeNode);
//    }

//    private ApplicationDataDirItemDto replaceNormalTreeNode(List<Node> treeData, TgApplicationInfo a, TgNodeMapping mapping) {
//        Node node = treeData.stream().filter(n -> n.getId().equals(mapping.getDirItemId())).findFirst().orElse(null);
//        if (Objects.isNull(node)) {
//            return null;
//        }
//
//        ApplicationDataDirItemDto treeNode = new ApplicationDataDirItemDto();
//        a.setReadableUserNames(applicationService.getReadableUsers(a.getReadableUsers()));
//        BeanUtils.copyProperties(a, treeNode);
//        BeanUtils.copyProperties(node, treeNode);
//        Optional.ofNullable(a.getDataExpir()).ifPresent(dateExpire -> {
//            treeNode.setDataExpir(DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", dateExpire));
//        });
//        treeNode.setApplicationId(a.getId());
//        treeNode.setNodeViewName(a.getProjectName());
//        treeNode.setIcon(CommonConstants.ICON_FORM);
//        treeNode.setNeed2Update(a.getNeedSyncTag());
//        treeNode.setRequireAttr(a.getRequireAttr());
//        treeNode.setRequireTimeType(a.getRequireTimeType());
//        treeNode.setClientNames(a.getClientNames());
//        treeNode.setLastUpdate(DateUtils.parseDate(a.getCreateTime()));
//        treeData.remove(node);
//        treeData.add(treeNode);
//        return treeNode;
//    }

//    private ApplicationDataDirItemDto replaceAbnormalTreeNode(List<Node> treeData,
//                                                              TgApplicationInfo applyInfo,
//                                                              TgNodeMapping mapping) {
//        Node node = treeData.stream().filter(n -> n.getId().equals(mapping.getDirItemId())).findFirst().orElse(null);
//        if (Objects.isNull(node)) {
//            return null;
//        }
//        ApplicationDataDirItemDto treeNode = new ApplicationDataDirItemDto();
//        applyInfo.setReadableUserNames(applicationService.getReadableUsers(applyInfo.getReadableUsers()));
//        BeanUtils.copyProperties(applyInfo, treeNode);
//        BeanUtils.copyProperties(node, treeNode);
//        Optional.ofNullable(applyInfo.getDataExpir())
//                .ifPresent(dateExpire -> treeNode.setDataExpir(DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", dateExpire)));
//        treeNode.setApplicationId(applyInfo.getId());
//        treeNode.setNodeViewName(applyInfo.getProjectName());
//        treeNode.setIcon(CommonConstants.ICON_FORM);
//        treeNode.setNeed2Update(applyInfo.getNeedSyncTag());
//        treeNode.setRequireAttr(applyInfo.getRequireAttr());
//        treeNode.setRequireTimeType(applyInfo.getRequireTimeType());
//        treeNode.setClientNames(applyInfo.getClientNames());
//        treeNode.setLastUpdate(DateUtils.parseDate(applyInfo.getCreateTime()));
//        treeData.remove(node);
//        treeData.add(treeNode);
//        return treeNode;
//    }


//    @Transactional(rollbackFor = Exception.class)
//    public ApplicationDataDirItemDto addNewTreeNodeAndMapping(List<Node> treeData, TgApplicationInfo a) {
//
//        DataDir dataDir = new DataDir() {{
//            setDirName(a.getProjectName());
//            setParentId(0L);
//            setSort(DataDirConst.DEFAULT_SORT);
//            setStatus(1);
//            setLastUpdate(DateUtils.getNowDate());
//            setTarget(CommonConstants.MY_DATA_DIR);
//            setApplicantId(a.getApplicantId());
//            setNodeId(a.getId());
//            setApplicationId(a.getId());
//            setIcon(CommonConstants.ICON_FORM);
//        }};
//        dataDir.insert();
//        // 想要正常看到申请节点，必须加入映射
//        TgNodeMapping mapping = new TgNodeMapping() {{
//            setDirItemId(dataDir.getId());
//            setNodeId(a.getId());
//            setApplicantId(a.getApplicantId());
//            setIcon(CommonConstants.ICON_FORM);
//        }};
//        mapping.insert();
//
//        Arrays.stream(a.getReadableUsers().split(",")).forEach((id) -> {
//            if (StringUtils.isNotBlank(id)) {
//                TgNodeMapping readableUsermapping = new TgNodeMapping() {{
//                    setDirItemId(dataDir.getId());
//                    setNodeId(a.getId());
//                    setApplicantId(Long.valueOf(id));
//                }};
//                readableUsermapping.insert();
//            }
//        });
//
//        ApplicationDataDirItemDto treeNode = new ApplicationDataDirItemDto();
//        a.setReadableUserNames(applicationService.getReadableUsers(a.getReadableUsers()));
//        BeanUtils.copyProperties(a, treeNode);
//        BeanUtils.copyProperties(dataDir, treeNode);
//        Optional.ofNullable(a.getDataExpir()).ifPresent(dateExpire -> {
//            treeNode.setDataExpir(DateUtils.parseDateToStr("yyyy-MM-dd HH:mm:ss", dateExpire));
//        });
//        treeNode.setApplicationId(a.getId());
//
//        treeData.add(treeNode);
//        return treeNode;
//    }
    @Override
    public List<DataManageFormDto> listTablesByDirId(Long dirId) {
        return tableInfoService.getListByDirId(dirId);
    }

    @Override
    public List<DataDir> selectSonOfParentDir(Long parentId) {
        return dataDirMapper.selectSonOfParentDir(parentId, CommonConstants.MY_DATA_DIR, null);
    }

    @Override
    public DataDir getByAssets(Long assetsId) {
        Wrapper<DataDir> wrapper = Wrappers.lambdaQuery(DataDir.class)
                .eq(DataDir::getIcon, CommonConstants.ICON_DATA_ASSETS)
                .eq(DataDir::getApplicationId, assetsId);
        return dataDirMapper.selectOne(wrapper);
    }

    @Override
    public ArkBIEditVo createBIChart(Long assetsId, Integer version, Long userId) throws Exception {
        UserDataAssets assets = userDataAssetsDAO.getBaseMapper().selectOne(new QueryWrapper<UserDataAssets>().lambda()
                .select(UserDataAssets::getProjectName, UserDataAssets::getId, UserDataAssets::getVersion)
                .eq(UserDataAssets::getId, assetsId).eq(UserDataAssets::getVersion, version)
        );
        if (Objects.isNull(assets)) {
            assets = userDataAssetsSnapshotDAO.getBaseMapper().selectOne(new QueryWrapper<UserDataAssetsSnapshot>().lambda()
                    .select(UserDataAssetsSnapshot::getProjectName, UserDataAssetsSnapshot::getAssetsId, UserDataAssetsSnapshot::getVersion)
                    .eq(UserDataAssetsSnapshot::getAssetsId, assetsId).eq(UserDataAssetsSnapshot::getVersion, version)
            );
        }
        if (Objects.isNull(assets)) {
            log.error("assetsId={} version={} userId={}", assetsId, version, userId);
            throw new CustomException("该版本资产不存在");
        }

        ArkBIEditVo arkBIEditVo = new ArkBIEditVo();

        // 获取推送到外网CK的表名组成的sql(隐含数据同步)
        String sql = this.getPushTableSqlByAssets(assetsId, assets.getVersion());
        UserBaseInfoVo biUser = this.arkBiService.getBIUserBaseInfoVo(userId);

        // 先创建并保存视图关系
        Optional<UserDataAssetsBiView> viewOpt = userDataAssetsBiViewDAO.queryByAssetsVersion(assets.getAssetsVersion());
        if (!viewOpt.isPresent()) {
            CreateViewParam createViewParam = new CreateViewParam();
            ExtViewInfo info = new ExtViewInfo();
            info.setExtViewId(assets.getAssetsVersion());
            info.setExtViewName(AssetsVersion.buildViewName(assets.getProjectName(), assets.getVersion()));
            info.setSourceId(sourceIdLan);
            info.setViewSql(sql);
            createViewParam.setExtViewInfo(Collections.singletonList(info));
            ApiResponseData<List<ViewVo>> createResult = extAnalysisUserApi.createView(biUser.getLoginToken(), createViewParam);
            log.warn("bi create view: createResult={}", createResult);
            if (!createResult.isSuccess()) {
                throw new CustomException("创建BI视图异常");
            }

            List<ViewVo> views = createResult.getData();
            if (CollectionUtils.isNotEmpty(views)) {
                arkBIEditVo.setViewId(views.get(0).getId());
            }
            List<UserDataAssetsBiView> mapping = views.stream()
                    .map(v -> {
                        Pair<Long, Integer> valPair = AssetsVersion.parseAssetsVersion(v.getExtViewId());
                        UserDataAssetsBiView biView = new UserDataAssetsBiView().setViewId(v.getId());
                        biView.setAssetsId(valPair.getKey());
                        biView.setVersion(valPair.getValue());
                        return biView;
                    })
                    .collect(Collectors.toList());
            userDataAssetsBiViewDAO.saveBatch(mapping);
        } else {
            arkBIEditVo.setViewId(viewOpt.get().getViewId());
        }

        //查找未保存的图表
        ArkbiAnalysis exsitArkbiAnalysis = arkbiAnalysisService
                .lambdaQuery()
                .eq(ArkbiAnalysis::getAssetsId, AssetsVersion.buildAssetsVersion(assetsId, assets.getVersion()))
                .eq(ArkbiAnalysis::getCreateBy, userId)
                .eq(ArkbiAnalysis::getStatus, 0)
                .eq(ArkbiAnalysis::getType, CommonConstants.ICON_CHART)
                .last("limit 1")
                .one();
        if (Objects.isNull(exsitArkbiAnalysis)) {
            //生成bi图表
            ApiResponseData<ExtAnalysisVo> generateChart = extAnalysisUserApi.createGenerateChart(biUser.getLoginToken(), new CreateExtAnalysisParam());
            ExtAnalysisVo extAnalysisVo = generateChart.getDataOrThrow();
            Assert.notNull(extAnalysisVo, "arkbi 生成编辑链接失败");

            //返回用户和编辑链接
            arkBIEditVo.setUrl(extAnalysisVo.getUrl());
            arkBIEditVo.setToken(biUser.getLoginToken());
            arkBIEditVo.setUserInfo(biUser);
            arkBIEditVo.setExtAnalysisId(extAnalysisVo.getExtAnalysisId());

            //保存BI分析记录
            ArkbiAnalysis arkbiAnalysis = new ArkbiAnalysis();
            arkbiAnalysis.setAnalysisId(arkBIEditVo.getExtAnalysisId())
                    .setAssetsId(AssetsVersion.buildAssetsVersion(assetsId, assets.getVersion()))
                    .setEditUrl(arkBIEditVo.getUrl())
                    .setCreateBy(userId)
                    .setType(CommonConstants.ICON_CHART)
                    .setCreateTime(LocalDateTime.now())
                    .setStatus(0);
            //保存编辑链接,下次不用再查询
            arkbiAnalysisService.save(arkbiAnalysis);
        } else {
            //返回用户和编辑链接
            arkBIEditVo.setToken(biUser.getLoginToken());
            arkBIEditVo.setUserInfo(biUser);
            arkBIEditVo.setUrl(exsitArkbiAnalysis.getEditUrl());
            arkBIEditVo.setPreviewUrl(exsitArkbiAnalysis.getPreviewUrl());
            arkBIEditVo.setExtAnalysisId(exsitArkbiAnalysis.getAnalysisId());
        }
        return arkBIEditVo;
    }

    @Override
    public AjaxResult syncData(Long assetId) {
        UserDataAssets application = new UserDataAssets().selectById(assetId);

        ApplicationDataUpdateRecord dataUpdateRecord = applicationDataUpdateRecordDAO
                .querySyncApplication(UpdateRecordStatusParam.builder().assetId(assetId)
                        .version(application.getVersion())
                        .syncTarget(SyncTargetType.SELF_DS)
                        .build());

        if (Objects.isNull(dataUpdateRecord)) {
            log.info("开始创建内网快照表 {}", assetId);
            defaultSyncHelper.asyncPushAssetsTableForBI(assetId, application.getVersion());
            return AjaxResult.success();
        }
        if (Objects.equals(dataUpdateRecord.getUpdateState(), UpdateRecordStateType.UPDATING)) {
            throw new RuntimeException("数据尚未同步完成");
        }
        if (Objects.equals(dataUpdateRecord.getUpdateState(), UpdateRecordStateType.FAILED)) {
            log.info("失败重试创建内网快照表 {}", assetId);
            defaultSyncHelper.asyncPushAssetsTableForBI(assetId, application.getVersion());
            throw new RuntimeException("最近一次数据同步失败，重新同步中，请联系技术人员处理");
        }
        return AjaxResult.success();
    }

    @Override
    @Transactional
    public void updateBIChart(SaveArkbiParam param) throws Exception {
        Long userId = ThreadContextHolder.getSysUser().getUserId();

        //查出分析记录
        ArkbiAnalysis exsitArkbiAnalysis = arkbiAnalysisService
                .lambdaQuery()
                .eq(ArkbiAnalysis::getAnalysisId, param.getExtAnalysisId())
                .eq(ArkbiAnalysis::getCreateBy, userId)
                .eq(ArkbiAnalysis::getType, CommonConstants.ICON_CHART)
                .last("limit 1")
                .one();
        if (Objects.isNull(exsitArkbiAnalysis)) {
            log.error("NOT EXIST: param={}", param);
            return;
        }

        UserBaseInfoVo biUser = arkBiService.getBIUserBaseInfoVo(userId);
        GetExtAnalysisParam extAnalysisParam = new GetExtAnalysisParam();
        extAnalysisParam.setExtAnalysisId(param.getExtAnalysisId());
        ApiResponseData<ExtAnalysisVo> generateChart = extAnalysisUserApi.getGenerateChart(biUser.getLoginToken(), extAnalysisParam);
        ExtAnalysisVo extAnalysisVo = generateChart.getDataOrThrow();

        List<VizVo> vizList = extAnalysisVo.getDatachart();
        List<String> viewIds = Lambda.buildList(vizList, VizVo::getViewId);
        // 保存关系
        List<UserDataAssetsBiView> views = userDataAssetsBiViewDAO.queryByViewIds(viewIds);
        List<String> exists = Lambda.buildList(views, UserDataAssetsBiView::getViewId);

        Pair<Long, Integer> idPair = AssetsVersion.parseAssetsVersion(exsitArkbiAnalysis.getAssetsId());
        viewIds.removeAll(exists);
        if (CollectionUtils.isNotEmpty(viewIds)) {
            List<UserDataAssetsBiView> mapping = viewIds.stream()
                    .map(v -> {
                        UserDataAssetsBiView biView = new UserDataAssetsBiView().setViewId(v);
                        biView.setAssetsId(idPair.getKey());
                        biView.setVersion(idPair.getValue());
                        return biView;
                    })
                    .collect(Collectors.toList());
            userDataAssetsBiViewDAO.saveBatch(mapping);
        }

        if (CollectionUtils.isNotEmpty(views)) {
            exsitArkbiAnalysis.setAssetsId(views.get(0).getAssetsVersion());
        }

        String name = extAnalysisVo.getDatachart().get(0).getName();
        if (exsitArkbiAnalysis.getStatus() == 1) {
            // 如果已经保存过目录节点,那就更新节点名称
            arkbiAnalysisService.getBaseMapper().update(null, new UpdateWrapper<ArkbiAnalysis>().lambda()
                    .set(ArkbiAnalysis::getName, name)
                    .set(ArkbiAnalysis::getAssetsId, exsitArkbiAnalysis.getAssetsId())
                    .eq(ArkbiAnalysis::getId, exsitArkbiAnalysis.getId())
            );
        } else {
            //保存我的数据图表节点
            exsitArkbiAnalysis.setName(name);
            exsitArkbiAnalysis.setStatus(1);
            exsitArkbiAnalysis.setPreviewUrl(extAnalysisVo.getUrl());
            exsitArkbiAnalysis.setShareUrl(extAnalysisVo.getShareUrl());
            arkbiAnalysisService.updateById(exsitArkbiAnalysis);
        }
    }

    /**
     * 交互逻辑调整，先创建空仪表板
     * 获取Arkbi仪表板编辑链接
     *
     * @see this#createEmptyBIDashboard
     */
    @Override
    @Deprecated
    public ArkBIEditVo createBIDashboard(GetDashboardEditParam param, Long userId) throws Exception {
        ArkBIEditVo arkBIEditVo = new ArkBIEditVo();
        List<UserDataAssets> assets = userDataAssetsDAO.getBaseMapper().selectBatchIds(param.getAssetsIds());

        //获取推送到外网CK的表名组成的sql
        //Map<applicationId,sql>
        Map<Long, String> sql = getPushTableSelectSql(assets);

        UserBaseInfoVo biUser = arkBiService.getBIUserBaseInfoVo(userId);

        //查找未保存的图表
        ArkbiAnalysis exsitArkbiAnalysis = arkbiAnalysisService
                .lambdaQuery()
                .eq(ArkbiAnalysis::getAssetsId, Joiner.on(",").join(param.getAssetsIds()))
                .eq(ArkbiAnalysis::getCreateBy, userId)
                .eq(ArkbiAnalysis::getStatus, 0)
                .eq(ArkbiAnalysis::getType, CommonConstants.ICON_DASHBOARD)
                .last("limit 1")
                .one();
        if (Objects.isNull(exsitArkbiAnalysis)) {
            //生成bi图表
            ExtAnalysisVo chartData = this.getExtAnalysisVo(assets, sql, biUser);
            Assert.notNull(chartData, "arkbi 生成编辑链接失败");

            //返回用户和编辑链接
            arkBIEditVo.setUrl(chartData.getUrl());
            arkBIEditVo.setToken(biUser.getLoginToken());
            arkBIEditVo.setUserInfo(biUser);
            arkBIEditVo.setExtAnalysisId(chartData.getExtAnalysisId());

            //保存BI分析记录
            ArkbiAnalysis arkbiAnalysis = new ArkbiAnalysis();
            arkbiAnalysis.setAnalysisId(arkBIEditVo.getExtAnalysisId())
                    .setAssetsId(Joiner.on(",").join(param.getAssetsIds()))
                    .setEditUrl(arkBIEditVo.getUrl())
                    .setCreateBy(userId)
                    .setCreateTime(LocalDateTime.now())
                    .setType(CommonConstants.ICON_DASHBOARD)
                    .setStatus(0);
            //保存编辑链接,下次不用再查询
            arkbiAnalysisService.save(arkbiAnalysis);
        } else {
            //返回用户和编辑链接
            arkBIEditVo.setToken(biUser.getLoginToken());
            arkBIEditVo.setUserInfo(biUser);
            arkBIEditVo.setUrl(exsitArkbiAnalysis.getEditUrl());
            arkBIEditVo.setPreviewUrl(exsitArkbiAnalysis.getPreviewUrl());
            arkBIEditVo.setExtAnalysisId(exsitArkbiAnalysis.getAnalysisId());
        }
        return arkBIEditVo;
    }

    @Override
    public ArkBIEditVo createEmptyBIDashboard(Long userId) throws Exception {
        UserBaseInfoVo biUser = arkBiService.getBIUserBaseInfoVo(userId);

        //生成bi图表
        ExtAnalysisVo chartData = this.getExtAnalysisVo(biUser);
        Assert.notNull(chartData, "arkbi 生成编辑链接失败");

        //返回用户和编辑链接
        ArkBIEditVo arkBIEditVo = new ArkBIEditVo();
        arkBIEditVo.setUrl(chartData.getUrl());
        arkBIEditVo.setToken(biUser.getLoginToken());
        arkBIEditVo.setUserInfo(biUser);
        arkBIEditVo.setExtAnalysisId(chartData.getExtAnalysisId());

        //保存BI分析记录
        ArkbiAnalysis arkbiAnalysis = new ArkbiAnalysis();
        arkbiAnalysis.setAnalysisId(arkBIEditVo.getExtAnalysisId())
                .setEditUrl(arkBIEditVo.getUrl())
                .setCreateBy(userId)
                .setCreateTime(LocalDateTime.now())
                .setType(CommonConstants.ICON_DASHBOARD)
                .setStatus(0);
        arkbiAnalysisService.save(arkbiAnalysis);

        return arkBIEditVo;
    }

    /**
     * 获取BI图表/仪表板编辑链接
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArkBIEditVo getBIModify(String analysisId, Long userId) throws Exception {
        ArkBIEditVo arkBIEditVo = new ArkBIEditVo();

        //查找保存的图表
        ArkbiAnalysis exsitArkbiAnalysis = arkbiAnalysisService
                .lambdaQuery()
                .eq(ArkbiAnalysis::getAnalysisId, analysisId)
                .eq(ArkbiAnalysis::getStatus, 1)
                .last("limit 1")
                .one();

        SysUser sysUser = ThreadContextHolder.getSysUser();
        log.info("type: {} {}", sysUser.getUserInfoType(), sysUser.getUserInfoType().equals(CommonConstants.CUSTOMER_UESR));
        //如果是第三方客户,返回分享链接
        // TODO 区分内网人员和外网人员 分享链接背后的数据源不一致
        if (sysUser.getUserInfoType().equals(CommonConstants.CUSTOMER_UESR)) {
            if (this.isSyncComplete(exsitArkbiAnalysis)) {
                ArkbiAnalysis child = arkbiAnalysisService.getByParentId(exsitArkbiAnalysis.getId());
                if (Objects.nonNull(child)) {
                    arkBIEditVo.setUrl(child.getEditUrl());
                    arkBIEditVo.setShareUrl(child.getShareUrl());
                } else {
                    log.info("use copy");
                    //如果已经有外网副本直接获取
                    ArkbiAnalysis biCopyToCustomer = this.createBICopyForCustomer(exsitArkbiAnalysis.getAnalysisId(), sysUser.getUserId());
                    arkBIEditVo.setUrl(biCopyToCustomer.getEditUrl());
                    arkBIEditVo.setShareUrl(biCopyToCustomer.getShareUrl());
                }
            } else {
                throw new RuntimeException("项目同步未完成,请稍后再试");
            }
        } else {
            UserBaseInfoVo biUser = arkBiService.getBIUserBaseInfoVo(userId);
            //返回用户和编辑链接
            arkBIEditVo.setToken(biUser.getLoginToken());
            arkBIEditVo.setUserInfo(biUser);
            arkBIEditVo.setUrl(exsitArkbiAnalysis.getEditUrl());
            arkBIEditVo.setShareUrl(exsitArkbiAnalysis.getShareUrl());
            arkBIEditVo.setPreviewUrl(exsitArkbiAnalysis.getPreviewUrl());
        }
        arkBIEditVo.setExtAnalysisId(exsitArkbiAnalysis.getAnalysisId());

        return arkBIEditVo;
    }

    // TODO 重写
    private boolean isSyncComplete(ArkbiAnalysis exsitArkbiAnalysis) {
        String assetsId = exsitArkbiAnalysis.getAssetsId();
        List<String> appIds = Optional.ofNullable(assetsId)
                .map(o -> Splitter.on(",").splitToList(o))
                .orElse(Lists.newArrayList());

        Map<Long, Integer> assetsVersion = new HashMap<>();
        List<Long> latest = new ArrayList<>();
        for (String appId : appIds) {
            if (StringUtils.contains(appId, "#")) {
                String[] pair = appId.split("#");
                assetsVersion.put(Long.valueOf(pair[0]), Integer.parseInt(pair[1]));
            } else {
                latest.add(Long.valueOf(appId));
            }
        }
        Map<Long, Integer> latestMap = userDataAssetsDAO.queryVersion(latest);
        assetsVersion.putAll(latestMap);

        //有任何一个项目未完成同步,都视为未完成
        boolean anyMatch = assetsVersion.entrySet().stream().anyMatch(a -> {
            ApplicationDataUpdateRecord existRecord = applicationDataUpdateRecordDAO
                    .querySyncApplication(UpdateRecordStatusParam.builder()
                            .assetId(a.getKey())
                            .version(a.getValue())
                            .syncTarget(SyncTargetType.CUSTOMER_DS)
                            .updateStates(Collections.singletonList(UpdateRecordStateType.SUCCESS))
                            .build());

            boolean notFinish = Objects.isNull(existRecord);
            if (notFinish) {
                log.warn("未同步到客户数据库: assetsId={} version={}", a.getKey(), a.getValue());
            }
            return notFinish;
        });
        return !anyMatch;
    }

    @Override
    @Transactional
    public ArkBIEditVo crateBICopy(String copyExtAnalysisId) throws Exception {
        ArkBIEditVo arkBIEditVo = new ArkBIEditVo();
        ArkbiAnalysis sourceAnalysis = arkbiAnalysisService.lambdaQuery()
                .eq(ArkbiAnalysis::getAnalysisId, copyExtAnalysisId)
                .eq(ArkbiAnalysis::getStatus, 1)
                .one();

        Long userId = ThreadContextHolder.getSysUser().getUserId();
        UserBaseInfoVo biUser = arkBiService.getBIUserBaseInfoVo(userId);

        CopyExtAnalysisParam copyExtAnalysisParam = new CopyExtAnalysisParam();
        copyExtAnalysisParam.setCopyExtAnalysisId(copyExtAnalysisId);

        ApiResponseData<ExtAnalysisVo> copyViz = extAnalysisUserApi.copyViz(biUser.getLoginToken(), copyExtAnalysisParam);
        ExtAnalysisVo extAnalysisVo = copyViz.getDataOrThrow();

        //返回用户和编辑链接
        arkBIEditVo.setUrl(extAnalysisVo.getUrl());
        arkBIEditVo.setToken(biUser.getLoginToken());
        arkBIEditVo.setUserInfo(biUser);
        arkBIEditVo.setExtAnalysisId(extAnalysisVo.getExtAnalysisId());
//        VizVo firstView = extAnalysisVo.getDatachart().get(0);


        //保存复制的BI分析记录
        ArkbiAnalysis arkbiAnalysis = new ArkbiAnalysis();
        arkbiAnalysis.setAnalysisId(arkBIEditVo.getExtAnalysisId())
                .setAssetsId(sourceAnalysis.getAssetsId())
                .setEditUrl(arkBIEditVo.getUrl())
                .setName(sourceAnalysis.getName() + "_副本" + DateUtils.dateTimeNow())
                .setPreviewUrl(arkBIEditVo.getUrl())
                .setShareUrl(extAnalysisVo.getShareUrl())
                .setShareUrlPassword(null)
                .setCreateBy(userId)
                .setCreateTime(LocalDateTime.now())
                .setType(sourceAnalysis.getType())
                .setStatus(1);
        //保存编辑链接,下次不用再查询
        arkbiAnalysisService.save(arkbiAnalysis);

//        if (CommonConstants.ICON_DASHBOARD.equals(arkbiAnalysis.getType())) {
//            //保存我的数据图表节点
//            createDashboardOnDir(arkbiAnalysis, extAnalysisVo, userId);
//        } else if (CommonConstants.ICON_CHART.equals(arkbiAnalysis.getType())) {
//            arkBIEditVo.setViewId(firstView.getViewId());
//            //保存我的数据图表节点
//            createChartOnDir(arkbiAnalysis, firstView.getName(), userId);
//        }

        return arkBIEditVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArkbiAnalysis createBICopyForCustomer(String copyExtAnalysisId, Long customerId) throws Exception {
        ArkBIEditVo arkBIEditVo = new ArkBIEditVo();
        ArkbiAnalysis sourceAnalysis = arkbiAnalysisService.lambdaQuery()
                .eq(ArkbiAnalysis::getAnalysisId, copyExtAnalysisId)
                .eq(ArkbiAnalysis::getStatus, 1)
                .one();
        Long userId = ThreadContextHolder.getSysUser().getUserId();
        UserBaseInfoVo biUser = arkBiService.getBIUserBaseInfoVo(userId);
        UserBaseInfoVo biUserForCustomer = arkBiService.getBIUserBaseInfoVo(customerId);

        CopyToUserExtAnalysisParam copyExtAnalysisParam = new CopyToUserExtAnalysisParam();
        copyExtAnalysisParam.setCopyExtAnalysisId(copyExtAnalysisId);
        copyExtAnalysisParam.setCopyToUserIdentity(String.valueOf(customerId));
        copyExtAnalysisParam.setCopyToSourceId(sourceIdWlan);

        ApiResponseData<ExtAnalysisVo> copyViz = extAnalysisUserApi.copyVizToUser(biUser.getLoginToken(), copyExtAnalysisParam);
        ExtAnalysisVo extAnalysisVo = copyViz.getDataOrThrow();

        //返回用户和编辑链接
        arkBIEditVo.setUrl(extAnalysisVo.getUrl());
        arkBIEditVo.setToken(biUser.getLoginToken());
        arkBIEditVo.setUserInfo(biUser);
        arkBIEditVo.setExtAnalysisId(extAnalysisVo.getExtAnalysisId());

        //保存复制的BI分析记录
        ArkbiAnalysis arkbiAnalysis = new ArkbiAnalysis();
        arkbiAnalysis.setAnalysisId(arkBIEditVo.getExtAnalysisId())
                .setAssetsId(sourceAnalysis.getAssetsId())
                .setEditUrl(arkBIEditVo.getUrl())
                .setPreviewUrl(arkBIEditVo.getUrl())
                .setShareUrl(extAnalysisVo.getShareUrl())
                .setShareUrlPassword(null)
                .setCreateBy(userId)
                .setName(sourceAnalysis.getName() + "_副本" + DateUtils.dateTimeNow())
                .setCreateTime(LocalDateTime.now())
                //保存内网版本的ID
                .setParentId(sourceAnalysis.getId())
                .setType(sourceAnalysis.getType())
                .setStatus(1);
        //保存编辑链接,下次不用再查询
        arkbiAnalysisService.save(arkbiAnalysis);

        //保存我的数据图表节点
        //复制到外网BI的不用保存节点
        //saveDashboardToDir(arkbiAnalysis, extAnalysisVo, customerId);

        return arkbiAnalysis;
    }

    /**
     * BI返回易数阁后，更新BI仪表板数据
     */
    @Override
    public void updateBIDashboard(SaveArkbiParam param) throws Exception {
        Long userId = ThreadContextHolder.getSysUser().getUserId();

        //查出分析记录
        ArkbiAnalysis exsitArkbiAnalysis = arkbiAnalysisService
                .lambdaQuery()
                .eq(ArkbiAnalysis::getAnalysisId, param.getExtAnalysisId())
                .eq(ArkbiAnalysis::getType, CommonConstants.ICON_DASHBOARD)
                .eq(ArkbiAnalysis::getCreateBy, userId)
                .last("limit 1")
                .one();

        if (Objects.isNull(exsitArkbiAnalysis)) {
            log.warn("dashboard not exist. id={}", param.getExtAnalysisId());
            return;
        }

        UserBaseInfoVo biUser = arkBiService.getBIUserBaseInfoVo(userId);
        GetExtAnalysisParam extAnalysisParam = new GetExtAnalysisParam();
        extAnalysisParam.setExtAnalysisId(param.getExtAnalysisId());
        ApiResponseData<ExtAnalysisVo> generateChart = extAnalysisUserApi.getGenerateDashboard(biUser.getLoginToken(), extAnalysisParam);
        ExtAnalysisVo extAnalysisVo = generateChart.getDataOrThrow();

        // 更新仪表板依赖的资产
        ApiResponseData<List<DatachartVo>> depVizList = extAnalysisUserApi.getVizDatachartListByAnalysisId(biUser.getLoginToken(), param.getExtAnalysisId());
        List<DatachartVo> vizList = depVizList.getDataOrThrow();
        List<String> viewIds = Lambda.buildList(vizList, DatachartVo::getViewId);
        List<UserDataAssetsBiView> views = userDataAssetsBiViewDAO.queryByViewIds(viewIds);
        boolean updateAssetsRelation = exsitArkbiAnalysis.fillAssetsVersion(Lambda.buildList(views, UserDataAssetsBiView::getAssetsVersion));

        String vizName = extAnalysisVo.getDatachart().get(0).getName();

        if (exsitArkbiAnalysis.getStatus() == 1) {
            arkbiAnalysisMapper.update(null, new UpdateWrapper<ArkbiAnalysis>().lambda()
                    .set(updateAssetsRelation, ArkbiAnalysis::getAssetsId, exsitArkbiAnalysis.getAssetsId())
                    .set(ArkbiAnalysis::getName, vizName)
                    .eq(ArkbiAnalysis::getId, exsitArkbiAnalysis.getId()));
            pool.submit(() -> checkSaveAsChart(biUser, userId));
            return;
        } else {
            //分析记录状态改为已保存
            exsitArkbiAnalysis.setName(vizName);
            exsitArkbiAnalysis.setStatus(1);
            exsitArkbiAnalysis.setPreviewUrl(extAnalysisVo.getUrl());
            exsitArkbiAnalysis.setShareUrl(extAnalysisVo.getShareUrl());
            arkbiAnalysisService.updateById(exsitArkbiAnalysis);
        }

        pool.submit(() -> checkSaveAsChart(biUser, userId));
    }

    @Override
    public AjaxResult<List<String>> queryDepDashboard(String extId) {
        try {
            if (StringUtils.isBlank(extId)) {
                return AjaxResult.error("参数为空");
            }
            Long userId = ThreadContextHolder.getSysUser().getUserId();
            UserBaseInfoVo biUser = arkBiService.getBIUserBaseInfoVo(userId);

            ApiResponseData<List<DashboardVo>> rsp = extAnalysisUserApi.getVizDashboardListByAnalysisId(biUser.getLoginToken(), extId);
            List<DashboardVo> data = rsp.getDataOrThrow();
            return AjaxResult.success(Lambda.buildList(data, DashboardVo::getName));
        } catch (Exception e) {
            log.error("", e);
            return AjaxResult.error(e.getMessage());
        }

    }

    @Override
    public void pullSaveAs(Long userId) {
        try {
            UserBaseInfoVo biUser = arkBiService.getBIUserBaseInfoVo(userId);
            checkSaveAsChart(biUser, userId);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 检查对比BI系统创建的图表，补创建在资产目录中
     *
     * @see MyDataDirServiceImpl#createChartOnDir
     */
    private void checkSaveAsChart(UserBaseInfoVo biUser, Long userId) {
        try {
            VizDatachartListParam listParam = new VizDatachartListParam();
            Date date = DateUtils.addHours(new Date(), -4);
            listParam.setAfterTiming(date);
            ApiResponseData<List<DatachartVo>> chartResp = extAnalysisUserApi.getVizDatachartList(biUser.getLoginToken(), listParam);
            List<DatachartVo> chartList = chartResp.getDataOrThrow();
            if (CollectionUtils.isEmpty(chartList)) {
                return;
            }

            List<String> viewIds = Lambda.buildList(chartList, DatachartVo::getViewId);
            List<UserDataAssetsBiView> views = Lambda.queryListIfExist(viewIds, userDataAssetsBiViewDAO::queryByViewIds);
            Map<String, UserDataAssetsBiView> viewAssetsMap = Lambda.buildMap(views, UserDataAssetsBiView::getViewId);

            Set<String> ids = Lambda.buildSet(chartList, DatachartVo::getExtAnalysisId);

            Set<String> existSave = arkbiAnalysisService.queryExist(ids);
            chartList.stream().filter(v -> !existSave.contains(v.getExtAnalysisId())).forEach(datachartVo -> {
                ArkbiAnalysis vo = new ArkbiAnalysis();

                UserDataAssetsBiView assets = viewAssetsMap.get(datachartVo.getViewId());
                if (Objects.isNull(assets)) {
                    log.error("图表引用了未保存的视图: viewId={}", datachartVo.getViewId());
                    return;
                }

                //BI过来的字段
                vo.setAnalysisId(datachartVo.getExtAnalysisId());
                //创建链接应该不用,这些都是已创建的图表
                vo.setEditUrl(null);
                vo.setPreviewUrl(datachartVo.getEditUrl());
                vo.setShareUrl(datachartVo.getShareUrl());
                //易数阁的字段
                vo.setName(datachartVo.getName());
                vo.setAssetsId(assets.getAssetsVersion());
                vo.setApplicantId(userId);
                vo.setCreateBy(userId);
                vo.setCreateTime(LocalDateTime.now());
                vo.setStatus(1);
                vo.setType(CommonConstants.ICON_CHART);
                log.info("fetch saveAs chart: vo={}", datachartVo);

                arkbiAnalysisService.save(vo);
//                this.createChartOnDir(vo, datachartVo.getName(), userId);
            });

        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 保存我的数据图表节点
     *
     * @param arkbiAnalysis
     * @param customerId
     * @return
     */
    private DataDir createChartOnDir(ArkbiAnalysis arkbiAnalysis, String vizName, Long customerId) {
        Long userId = Optional.ofNullable(customerId).orElse(ThreadContextHolder.getSysUser().getUserId());
        //生成的图表放在和申请项目下级目录
        Pair<Long, Integer> idPair = AssetsVersion.parseAssetsVersion(arkbiAnalysis.getAssetsId());
        Long parentId = this.getAssetsDirId(idPair.getKey());

        //获取图表名称和预览链接

        DataDir dataDir = new DataDir() {{
            setDirName(vizName);
            setParentId(parentId);
            setSort(DataDirConst.DEFAULT_SORT);
            setStatus(1);
            setLastUpdate(DateUtils.getNowDate());
            setTarget(CommonConstants.MY_DATA_DIR);
            setApplicantId(userId);
            setNodeId(arkbiAnalysis.getId());
            setIcon(CommonConstants.ICON_CHART);
        }};
        dataDirMapper.insert(dataDir);

        TgNodeMapping tgNodeMapping = new TgNodeMapping() {{
            setDirItemId(dataDir.getId());
            setNodeId(arkbiAnalysis.getId());
            setIcon(CommonConstants.ICON_CHART);
            setApplicantId(userId);
        }};

        tgNodeMappingMapper.insert(tgNodeMapping);

        return dataDir;
    }

    private DataDir updateDirName(ArkbiAnalysis exsitArkbiAnalysis, ExtAnalysisVo extAnalysisVo, String icon) {
        LambdaQueryWrapper<DataDir> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DataDir::getNodeId, exsitArkbiAnalysis.getId())
                .eq(DataDir::getIcon, icon)
                .eq(DataDir::getStatus, 1);
        DataDir dataDir = dataDirMapper.selectOne(queryWrapper);
        //获取图表名称和预览链接
        String vizName = extAnalysisVo.getDatachart().get(0).getName();
        dataDir.setDirName(vizName);

        if (Objects.equals(icon, CommonConstants.ICON_CHART)) {
            //生成的图表放在和申请项目下级目录
            Pair<Long, Integer> idPair = AssetsVersion.parseAssetsVersion(exsitArkbiAnalysis.getAssetsId());
            Long parentId = this.getAssetsDirId(idPair.getKey());
            dataDir.setParentId(parentId);
        }
        dataDirMapper.updateById(dataDir);
        return dataDir;
    }

    /**
     * 保存我的数据仪表板节点
     *
     * @param arkbiAnalysis
     * @param analysisVo
     * @param customerId
     * @return
     */
    private DataDir createDashboardOnDir(ArkbiAnalysis arkbiAnalysis, ExtAnalysisVo analysisVo, Long customerId) {
        Long userId = Optional.ofNullable(customerId).orElse(ThreadContextHolder.getSysUser().getUserId());

        //获取图表名称和预览链接
        String vizName = analysisVo.getDatachart().get(0).getName();

        DataDir dataDir = new DataDir() {{
            setDirName(vizName);
            setParentId(null);//仪表板放在顶级目录里??@TODO
            setSort(DataDirConst.DEFAULT_SORT);
            setStatus(1);
            setLastUpdate(DateUtils.getNowDate());
            setTarget(CommonConstants.MY_DATA_DIR);
            setApplicantId(userId);
            setNodeId(arkbiAnalysis.getId());
            setIcon(CommonConstants.ICON_DASHBOARD);
        }};
        dataDirMapper.insert(dataDir);

        TgNodeMapping tgNodeMapping = new TgNodeMapping() {{
            setDirItemId(dataDir.getId());
            setNodeId(arkbiAnalysis.getId());
            setIcon(CommonConstants.ICON_DASHBOARD);
            setApplicantId(userId);
        }};

        tgNodeMappingMapper.insert(tgNodeMapping);

        return dataDir;
    }

    private Long getAssetsDirId(Long assetsId) {
        return tgNodeMappingMapper.queryDirIdByAssetsId(assetsId);
    }

    private ExtAnalysisVo getExtAnalysisVo(UserBaseInfoVo biUser) throws Exception {
        CreateExtAnalysisParam createExtAnalysisParam = new CreateExtAnalysisParam();
        ApiResponseData<ExtAnalysisVo> generateChart = extAnalysisUserApi.createGenerateDashboard(biUser.getLoginToken(), createExtAnalysisParam);
        return generateChart.getDataOrThrow();
    }

    private ExtAnalysisVo getExtAnalysisVo(List<UserDataAssets> assetsList, Map<Long, String> sql, UserBaseInfoVo biUser) throws Exception {
        List<ExtViewInfo> extViewInfos = assetsList.stream().map(assets -> {
            ExtViewInfo extViewInfo = new ExtViewInfo();
            extViewInfo.setExtViewId(assets.getAssetsVersion());
            extViewInfo.setSourceId(sourceIdLan);
            extViewInfo.setExtViewName(AssetsVersion.buildViewName(assets.getProjectName(), assets.getVersion()));
            extViewInfo.setViewSql(sql.getOrDefault(assets.getId(), null));
            return extViewInfo;
        }).collect(Collectors.toList());
        CreateExtAnalysisParam createExtAnalysisParam = new CreateExtAnalysisParam();
        createExtAnalysisParam.setExtViewInfo(extViewInfos);
        ApiResponseData<ExtAnalysisVo> generateChart = extAnalysisUserApi.createGenerateDashboard(biUser.getLoginToken(), createExtAnalysisParam);
        return generateChart.getDataOrThrow();
    }

    /**
     * 数据推送到内网后,要生成一条内网CK的sql语句,这里返回那条语句
     *
     * @param assetsId 申请id
     * @return SQL 带别名
     * @see ApplicationServiceImpl#getApplicationFieldMeta
     */
    @Deprecated
    private String getPushTableSelectSql(Long assetsId) {
        Integer version = userDataAssetsDAO.queryVersion(assetsId);
        return this.getPushTableSqlByAssets(assetsId, version);
    }

    private String syncThenPushTableSqlByAssets(Long assetsId, Integer version, Long userId) {
        ApplicationDataUpdateRecord dataUpdateRecord = applicationDataUpdateRecordDAO
                .querySyncApplication(UpdateRecordStatusParam.builder().assetId(assetsId)
                        .version(version)
                        .syncTarget(SyncTargetType.SELF_DS)
                        .build());
        if (Objects.isNull(dataUpdateRecord)) {
            log.info("开始创建内网快照表 {}", assetsId);
            defaultSyncHelper.pushAssetsTableForBI(assetsId, version, userId);
            dataUpdateRecord = applicationDataUpdateRecordDAO
                    .querySyncApplication(UpdateRecordStatusParam.builder().assetId(assetsId)
                            .version(version)
                            .syncTarget(SyncTargetType.SELF_DS)
                            .build());
        }
        if (Objects.isNull(dataUpdateRecord)) {
            throw new CustomException("同步视图异常");
        }

        if (Objects.equals(dataUpdateRecord.getUpdateState(), UpdateRecordStateType.FAILED)) {
            log.info("失败重试创建内网快照表 {}", assetsId);
            defaultSyncHelper.pushAssetsTableForBI(assetsId, version, userId);
//            throw new RuntimeException("最近一次数据同步失败，重新同步中，请联系技术人员处理");

            dataUpdateRecord = applicationDataUpdateRecordDAO.querySyncApplication(UpdateRecordStatusParam.builder()
                    .assetId(assetsId)
                    .version(version)
                    .syncTarget(SyncTargetType.SELF_DS)
                    .updateState(UpdateRecordStateType.SUCCESS)
                    .build());
        }

        String localSql = tgCkProviderMapper.showCreateTable("SHOW CREATE TABLE " + dataUpdateRecord.getDataTableName());
        log.info("localSql={}", localSql);

        int engineIdx = localSql.indexOf("ENGINE");
        String table = localSql.substring(0, engineIdx);
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(table, JdbcConstants.CLICKHOUSE);
        if (CollectionUtils.isEmpty(sqlStatements)) {
            throw new RuntimeException("SQL解析错误");
        }
        SQLCreateTableStatement statement = (SQLCreateTableStatement) sqlStatements.get(0);
        List<SQLTableElement> fields = statement.getTableElementList();
        if (Objects.isNull(fields)) {
            throw new RuntimeException("SELECT查询字段为空");
        }
        StringBuilder res = new StringBuilder();
        for (SQLTableElement field : fields) {
            SQLColumnDefinition def = (SQLColumnDefinition) field;
            String columnName = def.getName().getSimpleName();
            res.append(columnName).append(" AS ").append(" `")
                    .append(Optional.ofNullable(def.getComment())
                            .map(v -> ((SQLCharExpr) v).getText())
                            .orElse(columnName.replace("`", "")))
                    .append("`,");
        }
        String field = res.substring(0, res.length() - 1);

        String querySQL = "SELECT " + field + " FROM " + dataUpdateRecord.getDataTableName();
        log.info("BI querySQL={}", querySQL);
        return querySQL;
    }

    /**
     * @see MyDataDirServiceImpl#syncThenPushTableSqlByAssets
     */
    private String getPushTableSqlByAssets(Long assetsId, Integer version) {
        ApplicationDataUpdateRecord dataUpdateRecord = applicationDataUpdateRecordDAO
                .querySyncApplication(UpdateRecordStatusParam.builder().assetId(assetsId)
                        .version(version)
                        .syncTarget(SyncTargetType.SELF_DS)
                        .build());
        if (Objects.isNull(dataUpdateRecord)) {
            log.info("开始创建内网快照表 {}", assetsId);
            defaultSyncHelper.asyncPushAssetsTableForBI(assetsId, version);
            throw new CustomException("数据开始同步，尚未同步完成");
        }

        if (Objects.equals(dataUpdateRecord.getUpdateState(), UpdateRecordStateType.UPDATING)) {
            throw new RuntimeException("数据尚未同步完成");
        }
        if (Objects.equals(dataUpdateRecord.getUpdateState(), UpdateRecordStateType.FAILED)) {
            log.info("失败重试创建内网快照表 {}", assetsId);
            defaultSyncHelper.asyncPushAssetsTableForBI(assetsId, version);
            throw new RuntimeException("最近一次数据同步失败，重新同步中，请联系技术人员处理");
        }

        String localSql = tgCkProviderMapper.showCreateTable("SHOW CREATE TABLE " + dataUpdateRecord.getDataTableName());
        log.info("localSql={}", localSql);

        int engineIdx = localSql.indexOf("ENGINE");
        String table = localSql.substring(0, engineIdx);
        List<SQLStatement> sqlStatements = SQLUtils.parseStatements(table, JdbcConstants.CLICKHOUSE);
        if (CollectionUtils.isEmpty(sqlStatements)) {
            throw new RuntimeException("SQL解析错误");
        }
        SQLCreateTableStatement statement = (SQLCreateTableStatement) sqlStatements.get(0);
        List<SQLTableElement> fields = statement.getTableElementList();
        if (Objects.isNull(fields)) {
            throw new RuntimeException("SELECT查询字段为空");
        }
        StringBuilder res = new StringBuilder();
        for (SQLTableElement field : fields) {
            SQLColumnDefinition def = (SQLColumnDefinition) field;
            String columnName = def.getName().getSimpleName();
            res.append(columnName).append(" AS ").append(" `")
                    .append(Optional.ofNullable(def.getComment())
                            .map(v -> ((SQLCharExpr) v).getText())
                            .orElse(columnName.replace("`", "")))
                    .append("`,");
        }
        String field = res.substring(0, res.length() - 1);

        String querySQL = "SELECT " + field + " FROM " + dataUpdateRecord.getDataTableName();
        log.info("BI querySQL={}", querySQL);
        return querySQL;
    }

    /**
     * TODO 作废，检查视图id是否存在
     * 检查快照数据和底表数据（GP同步到CK的那个表）差异性
     *
     * @return 需要触发同步
     */
    @Deprecated
    private boolean isSnapshotTableOutDateWithSyncTable(UserDataAssets assets,
                                                        ApplicationDataUpdateRecord dataUpdateRecord) {
        if (Objects.isNull(dataUpdateRecord) || !Objects.equals(dataUpdateRecord.getUpdateState(), UpdateRecordStateType.SUCCESS)) {
            return false;
        }

        Optional<TemplateTypeEnum> typeOpt = TemplateTypeEnum.of(assets.getTemplateType());
        if (typeOpt.isPresent() && typeOpt.get().isSchedulerTaskType()) {
            return false;
        }

        // 最后一次同步成功的数据
        IPage<TgCogradientDetailDto> syncDetails = intergrateProcessDefService
                .querySyncDetail(null, assets.getBaseTableId().intValue(), 7, 1, 1);
        List<TgCogradientDetailDto> records = syncDetails.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return false;
        }

        // 快照数据晚于 底表同步完成的时间
        boolean outDateWithSync = records.stream().anyMatch(v -> {
            Date date = DateUtils.parseTimezoneDate(v.getEndTime());
            return Optional.ofNullable(date).map(t -> dataUpdateRecord.getCreateTime().before(t)).orElse(false);
        });

        if (outDateWithSync) {
            log.info("底表数据变化，重建内网快照表 {}", assets.getId());
        }
        return outDateWithSync;
    }

    @Deprecated
    private Map<Long, String> getPushTableSelectSql(List<UserDataAssets> applicationInfos) {
        return applicationInfos.stream()
                .collect(Collectors.toMap(UserDataAssets::getId, f -> getPushTableSelectSql(f.getId()))
                );
    }


    @SneakyThrows
    @Override
    public Integer delete(Long dirId) {
        // 级联获取所有目录节点, 修改所有目录下已挂载的文件节点后, 级联删除所有目录节点
        List<DataDirDto> dirTreeGroup = getDirTreeGroup(dirId, null, null, null, null, null, null, null, null);
        Set<Long> dirs4Deleting = new HashSet<>();
        dirs4Deleting.add(dirId);
        dirTreeGroup.forEach((d) -> {
            // 查找特定的子孙节点
            Node node = findDescendantNodeById(d, dirId);
            List<Long> res = new ArrayList<>();
            dirs4Deleting.addAll(TreeUtils.traverse4DeletingDir(node, res));
        });

        // 判断是否已分配给客户
        List<TgCustomerApplyAuthDto> authList = customerAuthMapper.getListV2(dirs4Deleting, null, null);
        Assert.isTrue(CollectionsUtils.isEmpty(authList), authList.stream().map(TgCustomerApplyAuthDto::getProjectName)
                .collect(Collectors.joining(",")) + "已分配客户无法删除");

        List<DataManageFormDto> tables = new ArrayList<>();

        dirs4Deleting.forEach((id) -> tables.addAll(tableInfoService.getListByDirId(dirId)));

        // 删除表单
        tables.forEach((t) -> tableInfoService.updateDirIdOfTableInfo(new TableInfoDto() {{
            setId(t.getId());
            setDirId(null);
        }}));

        Long userId = ThreadContextHolder.getSysUser().getUserId();
        UserBaseInfoVo biUser = arkBiService.getBIUserBaseInfoVo(userId);
        List<DataDir> dirs = dataDirMapper.selectBatchIds(dirs4Deleting);
        for (DataDir dir : dirs) {
            // 删除另存的项目
            if (Objects.equals(dir.getIcon(), CommonConstants.ICON_DATA_ASSETS)) {
                UserDataAssets assets = new UserDataAssets().selectById(dir.getNodeId());
                if (Objects.nonNull(assets) && Objects.nonNull(assets.getCopyFromId())) {
                    assets.deleteById();
                } else {
                    log.error("数据不存在或者不是另存的项目");
                }
            }

            // 删除仪表板
            if (Objects.equals(dir.getIcon(), CommonConstants.ICON_CHART) || Objects.equals(dir.getIcon(), CommonConstants.ICON_DASHBOARD)) {
                ArkbiAnalysis arkbiAnalysis = arkbiAnalysisService.getById(dir.getNodeId());
                if (Objects.nonNull(arkbiAnalysis)) {
                    DeleteExtAnalysisParam extAnalysisParam = new DeleteExtAnalysisParam();
                    extAnalysisParam.setExtAnalysisId(arkbiAnalysis.getAnalysisId());
                    try {
                        extAnalysisUserApi.deleteViz(biUser.getLoginToken(), extAnalysisParam).getDataOrThrow();
                    } catch (Exception e) {
                        if (Objects.equals(e.getMessage(), "Associated data exists , cannot be deleted!")) {
                            log.error("dir={}", dir);
                            throw new CustomException("该图表被引用，不允许删除!");
                        }
                        throw e;
                    }
                    arkbiAnalysisMapper.deleteByPrimaryKey(dir.getNodeId());
                }
            }
        }

        return dataDirMapper.deleteBatchIds(dirs4Deleting);
    }

    public Node findDescendantNodeById(Node rootNode, Long targetDirId) {
        if (rootNode.getId().equals(targetDirId)) {
            return rootNode;
        }

        for (Object child : rootNode.getChildren()) {
            Node result = findDescendantNodeById((Node) child, targetDirId);
            if (result != null) {
                return result;
            }
        }

        return null;
    }


}
