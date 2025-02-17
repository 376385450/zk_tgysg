package com.sinohealth.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.AjaxResult;
import com.sinohealth.common.core.domain.entity.DataDir;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.enums.application.TemplateTypeEnum;
import com.sinohealth.common.utils.DateUtils;
import com.sinohealth.common.utils.DirCache;
import com.sinohealth.common.utils.SecurityUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.dto.Node;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.biz.application.util.Lambda;
import com.sinohealth.system.biz.dataassets.dao.UserDataAssetsDAO;
import com.sinohealth.system.biz.dir.bo.DocItemBO;
import com.sinohealth.system.biz.dir.dto.DirPageQueryRequest;
import com.sinohealth.system.biz.dir.dto.GetMyDataDirTreeParam;
import com.sinohealth.system.biz.dir.entity.DataDirView;
import com.sinohealth.system.biz.dir.vo.DataDirListVO;
import com.sinohealth.system.biz.dir.vo.HomeDataDirVO;
import com.sinohealth.system.dao.DataDirDAO;
import com.sinohealth.system.domain.SysUserTable;
import com.sinohealth.system.domain.TableInfo;
import com.sinohealth.system.domain.TgApplicationInfo;
import com.sinohealth.system.domain.TgDocInfo;
import com.sinohealth.system.domain.WhiteListUser;
import com.sinohealth.system.domain.constant.ApplicationConst;
import com.sinohealth.system.domain.constant.DataDirConst;
import com.sinohealth.system.domain.converter.JsonBeanConverter;
import com.sinohealth.system.dto.DataDirDto;
import com.sinohealth.system.dto.DataDirUpdateReqDTO;
import com.sinohealth.system.dto.DocDataDirItemDto;
import com.sinohealth.system.dto.TableDataDirItemDto;
import com.sinohealth.system.dto.TableInfoDto;
import com.sinohealth.system.dto.application.TgNodeMapping;
import com.sinohealth.system.dto.table_manage.DataManageFormDto;
import com.sinohealth.system.dto.template.TemplateAuditProcessEasyDto;
import com.sinohealth.system.filter.ThreadContextHolder;
import com.sinohealth.system.mapper.DataDirMapper;
import com.sinohealth.system.mapper.TgApplicationInfoMapper;
import com.sinohealth.system.mapper.TgDocInfoMapper;
import com.sinohealth.system.mapper.TgNodeMappingMapper;
import com.sinohealth.system.mapper.TgTemplateInfoMapper;
import com.sinohealth.system.service.IDataDirService;
import com.sinohealth.system.service.IDocService;
import com.sinohealth.system.service.IGroupDataDirService;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.ITableInfoService;
import com.sinohealth.system.service.ITemplateService;
import com.sinohealth.system.util.ListUtil;
import com.sinohealth.system.util.TreeUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 数据目录Service业务层处理
 *
 * @author jingjun
 * @date 2021-04-16
 */
@Slf4j
@Service
public class DataDirServiceImpl extends ServiceImpl<DataDirMapper, DataDir> implements IDataDirService {

    @Autowired
    private IGroupDataDirService groupDataDirService;
    @Autowired
    private ITableInfoService tableInfoService;
    @Autowired
    private IDocService docService;
    @Autowired
    private ITemplateService templateService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private DataDirMapper dataDirMapper;
    @Autowired
    private DataDirDAO dataDirDAO;
    @Autowired
    private TgDocInfoMapper docInfoMapper;
    @Autowired
    private TgApplicationInfoMapper applicationInfoMapper;
    @Autowired
    private UserDataAssetsDAO userDataAssetsDAO;
    @Autowired
    private TgTemplateInfoMapper templateInfoMapper;


    @Autowired
    private TgNodeMappingMapper tgNodeMappingMapper;

    private static final Map<Integer, String> DATA_SOURCE_MAP = new ConcurrentHashMap<>(16);

    private static void setBusinessType(HomeDataDirVO d) {
        try {
            DataDir parent = DataDir.newInstance().selectById(d.getDirId());
            DataDir grand = DataDir.newInstance().selectById(parent.getParentId());
            d.setBussinessType(grand.getDirName() + "-" + d.getDirName());
        } catch (NullPointerException e) {
            d.setBussinessType(d.getDirName());
        }
    }


    @Override
    public DataDirDto getTree(Long id) {
        List<DataDir> list = DirCache.getList();
        DataDirDto dto = new DataDirDto();
        dto.setId(0L);
        dto.setDirName("根节点");
        findChildren(list, dto, null);
        return dto;
    }

    private void findChildren(List<DataDir> list, DataDirDto dto, List<DataDirDto> endDirList) {
        List<DataDirDto> children = list.stream().filter(d -> d.getParentId().equals(dto.getId())).map(d -> {
                    DataDirDto child = new DataDirDto();
                    BeanUtils.copyProperties(d, child);
                    findChildren(list, child, endDirList);
                    return child;
                }
        ).sorted((a, b) -> b.getSort().compareTo(a.getSort())).collect(Collectors.toList());

        if (endDirList != null && children.isEmpty()) {
            endDirList.add(dto);
        }
        dto.setChildren(children);
    }

    @Override
    public DataDirDto getGroupTree(List<Long> idList, boolean loadTable, List<SysUserTable> userTableList, boolean isFilter) {

        if (ObjectUtils.isEmpty(idList)) {
            return new DataDirDto();
        }
        List<Long> dirIdList = groupDataDirService.getDirId(idList);

        return getGroupTreeByDirIds(dirIdList, loadTable, userTableList, isFilter);
    }

    @Override
    public DataDirDto getGroupTreeByDirIds(List<Long> dirIdList, boolean loadTable, List<SysUserTable> userTableList, boolean isFilter) {
        DataDirDto dto = new DataDirDto();
        dto.setId(0L);
        dto.setDirName("根节点");

        List<DataDirDto> endDirList = new ArrayList<>();

        if (!ObjectUtils.isEmpty(dirIdList)) {
            dirIdList = dirIdList.stream().distinct().collect(Collectors.toList());
            List<DataDir> dirList = this.listByIds(dirIdList);
            if (ObjectUtil.isNotNull(isFilter) && isFilter) {
                /*
                  v1.1新增
                  根据前端传值，判断是否需要过滤出状态为 1 的数据
                 */
                dirList = dirList.stream().filter(d -> d.getStatus() == 1).collect(Collectors.toList());
            }
            dirList.forEach(d -> d.setSourceName(DATA_SOURCE_MAP.get(d.getDatasourceId())));

            findChildren(dirList, dto, endDirList);
            if (loadTable) {
                List<TableInfo> tableList = tableInfoService.list(Wrappers.<TableInfo>query().select("id", "table_name", "dir_id").in("dir_id", endDirList.stream().map(DataDirDto::getId).collect(Collectors.toList())).eq("status", 1));
                //将表名添加到末节的
                endDirList.forEach(dir -> {
                    dir.setChildren(tableList.stream().filter(t -> t.getDirId().equals(dir.getId())).map(t -> {
                        DataDirDto child = new DataDirDto();
                        child.setIsTable(true);
                        child.setDirName(t.getTableAlias() != null ? String.format("%s (%s)", t.getTableAlias(), t.getTableName()) : t.getTableName());
                        child.setParentId(t.getDirId());
                        child.setId(t.getId());
                        if (userTableList != null) {
                            userTableList.stream().filter(u -> u.getTableId().equals(t.getId())).findFirst().ifPresent(u -> child.setAccessType(u.getAccessType()));
                        }
                        return child;
                    }).collect(Collectors.toList()));

                    if (CollectionUtils.isEmpty(dir.getChildren())) {
                        dir.setDisabled(true);
                    }
                });
            }
        }
        return dto;
    }

    /**
     * 获取所有子集
     */
    public Set<Long> getDirIdsByName(String dirName) {
        Set<Long> dirIdList = new HashSet<>();
        List<DataDir> dataDirList = this.list(Wrappers.<DataDir>query().eq("status", 1).and(t -> t.like("dir_name", dirName)));
        List<Long> filterDirIdList = dataDirList.stream().map(DataDir::getId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterDirIdList)) {
            return dirIdList;
        }
        filterDirIdList.forEach(t -> {
            List<Long> endDirIds = DirCache.getEndDirIds(t);
            dirIdList.addAll(endDirIds);
        });
        return dirIdList;

    }


    /*****************************************************
     * 天宫易数阁代码
     * 责任人: linweiwu
     */

    @Override
    public Integer newDir(DataDir dataDir) {
        dataDir.setApplicantId(ThreadContextHolder.getSysUser().getUserId());
        dataDir.setIcon(CommonConstants.ICON_FILE);
        return dataDirMapper.insertAndGetId(dataDir, CommonConstants.DATA_DIR);
    }

    @Override
    public int update(DataDir dataDir) {
        return dataDirMapper.updateById(dataDir);
    }

    /**
     * 更新数据目录
     * 1、地图目录的层级最大为2
     * 2、只能编辑目录
     * 3、可以新增目录
     * 4、可以删除目录，删除目录级联删除目录下的子孙
     *
     * @param reqDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateV2(DataDirUpdateReqDTO reqDTO) {
        List<DataDirDto> reqTree = DataDirUpdateReqDTO.getDtoTree(reqDTO.getList(), 0L);
        // 参数校验，只接收文件夹节点
        List<DataDirDto> flat = reqDTO.flat(reqTree);
        flat.forEach(dir -> {
            Assert.isTrue(StringUtils.isNotBlank(dir.getIcon()), String.format("目录名称:%s, 类型icon参数缺失", dir.getDirName()));
            Assert.isTrue(StringUtils.equals(CommonConstants.ICON_FILE, dir.getIcon()), String.format("只能编辑目录类型， %s不是目录", dir.getDirName()));
        });

        // 先编辑、新增
        this.doUpdateV2(reqTree, null, 1);

        // 删除
        // 调用跟前端相同的接口
        List<? extends Node> result = getDirTreeGroupV2(0L, "file", null, null);
        List<Node> dirList = Node.flat(result);
        Map<Long, Node> nodeMap = flat.stream().collect(Collectors.toMap(Node::getId, Function.identity()));
        // 如果删除目录下有关联的表则无法删除
        String errorDirNameList = dirList.stream()
                .filter(dir -> !nodeMap.containsKey(dir.getId()))
                .filter(node -> {
                    // 这里强转，是因为如果是目录那么就是DataDirDto类型
                    Integer nums = Optional.ofNullable(((DataDirDto) node).getNums()).orElse(0);
                    return nums > 0;
                })
                .map(Node::getNodeViewName)
                .collect(Collectors.joining(","));
        Assert.isTrue(StringUtils.isBlank(errorDirNameList), String.format("以下目录下关联表文档数大于0无法删除: %s", errorDirNameList));

        List<Long> deleteDirIdList = dirList.stream()
                .filter(dir -> !nodeMap.containsKey(dir.getId()))
                .map(dir -> dir.getId())
                .collect(Collectors.toList());
        deleteDirIdList.forEach(deleteDirId -> delete(deleteDirId));
        log.info(">> 编辑地图目录，删除目录列表{}", JSON.toJSONString(deleteDirIdList));
    }

    private void doUpdateV2(List<DataDirDto> list, DataDirDto parentFolder, int level) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        if (level > 2) {
            throw new IllegalArgumentException("地图目录层级不能超过2");
        }
        int sort = 2;
        for (DataDirDto dataDirDto : list) {
            boolean isNew = dataDirDto.getId() == null;
            boolean isUpdate = !isNew;
            final long parentId = parentFolder == null ? 0 : parentFolder.getId();
            if (isNew) {
                DataDir newEntity = new DataDir();
                newEntity.setParentId(parentId);
                newEntity.setLastUpdate(new Date());
                newEntity.setDirName(dataDirDto.getDirName());
                newEntity.setStatus(1);
                newEntity.setDatasourceId(0);
                newEntity.setTarget(CommonConstants.DATA_DIR);
                newEntity.setApplicantId(SecurityUtils.getUserId());
                newEntity.setIcon(CommonConstants.ICON_FILE);
                newEntity.setSort(sort);
                newEntity.setComment(dataDirDto.getComment());
                dataDirDAO.save(newEntity);
                dataDirDto.setId(newEntity.getId());
            }
            if (isUpdate) {
                if (log.isDebugEnabled()) {
                    DataDir originDir = dataDirDAO.getById(dataDirDto.getId());
                    if (!Objects.equals(originDir.getParentId(), dataDirDto.getParentId())) {
                        log.debug("目录结构修改");
                    }
                }
                DataDir updateEntity = new DataDir();
                updateEntity.setId(dataDirDto.getId());
                updateEntity.setDirName(dataDirDto.getDirName());
                updateEntity.setParentId(parentId);
                updateEntity.setLastUpdate(new Date());
                updateEntity.setSort(sort);
                updateEntity.setComment(dataDirDto.getComment());
                dataDirDAO.updateById(updateEntity);
            }
            sort += 2;
            doUpdateV2(dataDirDto.getChildren(), dataDirDto, level + 1);
        }
    }

    @Override
    public List<? extends Node> getDirTreeGroup(Long id, String type, String searchName, Boolean self) {
        StopWatch watch = new StopWatch();
        watch.start("list");
        // 获取目录节点
        List<Node> treeData = dataDirMapper.selectTreeData(CommonConstants.DATA_DIR,
                ThreadContextHolder.getSysUser().getUserId(), null, null, null, null, null, null);
        watch.stop();

        watch.start("fill table");
        // 处理表节点
        handleTableNodes(type, treeData);
        watch.stop();

        watch.start("fill doc");
        // 处理文档节点
        handleDocNodes(type, treeData);
        watch.stop();

        watch.start("search");
        handleSearch(searchName, treeData);
        watch.stop();
        if (BooleanUtils.isTrue(self)) {
            watch.start("remove self");
            removeNotSelfTable(treeData);
            watch.stop();
        }

        watch.start("tree");
        List<? extends Node> result = TreeUtils.transformTreeGroup(id, treeData, null);
        watch.stop();

        log.info("TREE {} {}", watch.getTotalTimeMillis(), watch.prettyPrint());
        return result;
    }

    /**
     * 修正tableNums和nums
     * 先查询所有，再过滤
     *
     * @param id
     * @param type
     * @param searchName
     * @param self
     * @return
     */
    @Override
    public List<? extends Node> getDirTreeGroupV2(Long id, String type, String searchName, Boolean self) {
        final String allType = "table,doc,file";
        StopWatch watch = new StopWatch();
        watch.start("list");
        // 获取目录节点
        List<Node> treeData = dataDirMapper.selectTreeData(CommonConstants.DATA_DIR,
                ThreadContextHolder.getSysUser().getUserId(), null, null, null, null, null, null);
        watch.stop();

        watch.start("fill table");
        // 处理表节点
        handleTableNodes(allType, treeData);
        watch.stop();

        watch.start("fill doc");
        // 处理文档节点
        handleDocNodes(allType, treeData);
        watch.stop();

        watch.start("search");
        handleSearch(searchName, treeData);
        watch.stop();
        if (BooleanUtils.isTrue(self)) {
            watch.start("remove self");
            removeNotSelfTable(treeData);
            watch.stop();
        }

        watch.start("tree");
        List<? extends Node> result = TreeUtils.transformTreeGroup(id, treeData, null);
        watch.stop();

        log.info("TREE {} {}", watch.getTotalTimeMillis(), watch.prettyPrint());
        TreeUtils.traversalTree(result, null);
        // 根据传参过滤
        TreeUtils.filterNode(result, ListUtil.toList(type, String::valueOf), new GetMyDataDirTreeParam());
        return result;
    }

    private void removeNotSelfTable(List<Node> treeData) {
        if (CollectionUtils.isEmpty(treeData)) {
            return;
        }

        List<Node> remove = new ArrayList<>();
        for (Node node : treeData) {
            if (node instanceof TableDataDirItemDto) {
                TableDataDirItemDto table = (TableDataDirItemDto) node;
                if (!Objects.equals(table.getLeaderName(), SecurityUtils.getUsername())) {
                    remove.add(node);
                }
            }

            removeNotSelfTable(node.getChildren());
        }
        treeData.removeIf(remove::contains);
    }

    private void handleSearch(String searchName, List<Node> treeData) {
        if (StringUtils.isNotBlank(searchName)) {
            List<Node> showNodes = new ArrayList<>();
            // 查找节点
            List<Node> searchNodes = treeData.stream()
                    .filter(n -> null != n.getNodeViewName() && n.getNodeViewName().contains(searchName))
                    .collect(Collectors.toList());
            // 遍历查找关联的父节点
            for (Node node : searchNodes) {
                searchNodeParents(node, showNodes, treeData);
            }
            // 删除不在节点群的节点
            treeData.removeIf(n -> !showNodes.contains(n));
        }
    }

    private void searchNodeParents(Node searchNode, List<Node> nodeArrayList, List<Node> treeData) {

        nodeArrayList.add(searchNode);
        // 0L 说明到了最顶层, 不存在更上层的父节点
        if (searchNode.getParentId().equals(0L)) {
            return;
        } else {
            Node node = treeData.stream()
                    .filter(n -> n.getId().equals(searchNode.getParentId()))
                    .findFirst().get();
            searchNodeParents(node, nodeArrayList, treeData);
        }

    }

    private void handleDocNodes(String type, List<Node> treeData) {
        if (type.contains(CommonConstants.ICON_DOC)) {
            // 这里返回的查询结果只会是 List<TgDocInfo> 的包装, 强转是没有问题的,所以消除检查
            @SuppressWarnings("unchecked")
            AjaxResult<List<TgDocInfo>> docsRsult = (AjaxResult<List<TgDocInfo>>) docService.query(null);
            List<TgDocInfo> docs = docsRsult.getData().stream()
                    .filter(d -> CommonConstants.NORMAL == d.getStatus().intValue()).collect(Collectors.toList());
            QueryWrapper<TgApplicationInfo> qw = new QueryWrapper<>();
            qw.eq("application_type", ApplicationConst.ApplicationType.DOC_APPLICATION);
            qw.eq("applicant_id", ThreadContextHolder.getSysUser().getUserId());
            List<TgApplicationInfo> applicationInfos = TgApplicationInfo.newInstance().selectList(qw);
            List<TgNodeMapping> mappings = tgNodeMappingMapper.queryDocMapping();

            List<DocItemBO> items = new ArrayList<>();
            for (TgDocInfo d : docs) {
                Optional<TgNodeMapping> mapping = mappings.stream()
                        .filter((m) -> m.getNodeId().equals(d.getId()) && m.getIcon().equals(CommonConstants.ICON_DOC))
                        .findFirst();

                JsonBeanConverter.convert2Obj(d);
                // 如果没有映射并且表状态正常, 将其加入 TreeNode 和 Mapping
                if (!mapping.isPresent()
                        && d.getStatus().equals(CommonConstants.NORMAL)) {
                    addNewTreeNodeAndMapping(treeData, d, applicationInfos);
                } else if (mapping.isPresent() && d.getStatus().equals(CommonConstants.NORMAL)
                        && treeData.size() > 0) {
                    // mappings 里存在对应节点, 组合 DocDataDirItemDto
                    DocItemBO item = this.replaceNormalTreeNode(treeData, d, mapping.get());
                    if (Objects.nonNull(item)) {
                        items.add(item);
                    }
                }
            }

            List<Long> userIds = items.stream().map(v -> v.getDocInfo().getOwnerId()).distinct().collect(Collectors.toList());
            List<SysUser> usersById = sysUserService.selectUserByIds(userIds);
            Map<Long, SysUser> userMap = usersById.stream().collect(Collectors.toMap(SysUser::getUserId, v -> v, (front, current) -> current));

            for (DocItemBO item : items) {
                setLeaderInfo(item, userMap);
                setDocAuthorization(item.getDocInfo(), applicationInfos, item.getTreeNode());
            }
        } else {
            // 如果没有查询 doc 节点, 则剪掉 doc 节点
            treeData.removeIf(td -> td.getIcon().equals(CommonConstants.ICON_DOC));
        }
    }

    private void handleTableNodes(String type, List<Node> treeData) {
        if (type.contains(CommonConstants.ICON_TABLE)) {
            // 获取表数据
            List<TableInfo> tableInfos = tableInfoService.findAllNotDiy();

            // 获取表mapping, mapping 实际上是节点占位符映射
            // mapping 中不存在的节点写入 mapping, mapping 中多余的节点删除
            List<TgNodeMapping> mappings = tgNodeMappingMapper.queryTableMapping();
            log.debug("TreeData:{}", treeData);
            log.debug("Mappings:{}", mappings);

            List<TableDataDirItemDto> items = new ArrayList<>();
            for (TableInfo table : tableInfos) {
                Optional<TgNodeMapping> mappingOpt = mappings.stream()
                        .filter((m) -> m.getNodeId().equals(table.getId()) && m.getIcon().equals(CommonConstants.ICON_TABLE))
                        .findFirst();
                log.debug("Table:{}", table);
                log.debug("Mapping:{}", mappingOpt);
                // 如果没有映射并且表状态正常, 将其加入 TreeNode 和 Mapping
                if (!mappingOpt.isPresent()) {
                    this.addNewTreeNodeAndMapping(treeData, table);
                } else if (treeData.size() > 0) {
                    // mappings 里存在对应节点, 组合 TableDirItemDto
                    TableDataDirItemDto item = this.replaceNormalTreeNode2(treeData, table, mappingOpt.get());
                    if (Objects.nonNull(item)) {
                        items.add(item);
                    }
                }
            }

            // TODO user 查询部分合并
            // 替换 replaceNormalTreeNode 实现
            List<String> nameList = items.stream().map(TableDataDirItemDto::getLeaderName).filter(StringUtils::isNoneBlank)
                    .collect(Collectors.toList());
            List<SysUser> usersByName = sysUserService.selectUserByUserNames(nameList);
            Map<String, SysUser> userNameMap = usersByName.stream().collect(Collectors.toMap(SysUser::getUserName, v -> v, (front, current) -> current));

            List<Long> userIds = items.stream()
                    .map(TableDataDirItemDto::getViewUser).filter(StringUtils::isNoneBlank)
                    .flatMap(v -> Arrays.stream(v.split(","))).map(Long::parseLong).collect(Collectors.toList());

            List<SysUser> usersById = sysUserService.selectUserByIds(userIds);
            Map<Long, SysUser> userMap = usersById.stream().collect(Collectors.toMap(SysUser::getUserId, v -> v, (front, current) -> current));

            List<Long> ids = items.stream().map(TableDataDirItemDto::getId).collect(Collectors.toList());
            Map<Long, List<TemplateAuditProcessEasyDto>> tableProcessMap;
            if (CollectionUtils.isNotEmpty(ids)) {
                List<TemplateAuditProcessEasyDto> processList = templateService.queryProcessesByBaseTableIds(ids);
                tableProcessMap = processList.stream().collect(Collectors.groupingBy(TemplateAuditProcessEasyDto::getBaseTableId));
            } else {
                tableProcessMap = Collections.emptyMap();
            }

            for (TableDataDirItemDto item : items) {
                this.setLeaderNameOri(item, userNameMap);
                this.setViewUser(item, userMap);
                this.setTemplateAuditInfo(item, tableProcessMap);
            }
        } else {
            // 如果没有查询 table 节点, 则剪掉 table 节点
            treeData.removeIf(td -> td.getIcon().equals(CommonConstants.ICON_TABLE));
        }
    }

    private TableDataDirItemDto replaceNormalTreeNode2(List<Node> treeData, TableInfo table, TgNodeMapping mapping) {
        Node node = treeData.stream().filter(n -> n.getId().equals(mapping.getDirItemId())).findFirst().orElse(null);
        if (node != null) {
            TableDataDirItemDto treeNode = new TableDataDirItemDto();
            BeanUtils.copyProperties(node, treeNode);
            BeanUtils.copyProperties(table, treeNode);
            treeNode.setId(node.getId());
            treeNode.setTableId(table.getId());
            treeNode.setIcon(CommonConstants.ICON_TABLE);
            treeNode.setNodeViewName(table.getTableName());

            treeData.remove(node);
            treeData.add(treeNode);
            log.debug("-------------------替换旧表单节点---------------------");
            log.debug("旧表单节点: {}", node);
            log.debug("新表单节点: {}", treeNode);
            return treeNode;
        }
        return null;
    }

    private void setTemplateAuditInfo(TableDataDirItemDto treeNode, Map<Long, List<TemplateAuditProcessEasyDto>> processMap) {
        // 处理提数模板+审核流程
        List<TemplateAuditProcessEasyDto> templateAuditProcessEasyDtos = processMap.get(treeNode.getId());
        if (CollectionUtils.isEmpty(templateAuditProcessEasyDtos)) {
            return;
        }

        String tad = templateAuditProcessEasyDtos.stream()
                .map(d -> d.getTemplateName() + "(" + d.getProcessName() + ")")
                .collect(Collectors.joining("、"));
        treeNode.setTemplateAuditInfo(tad);
    }

    private void setViewUser(TableDataDirItemDto treeNode, Map<Long, SysUser> userMap) {
        //处理可查看人员
        if (StringUtils.isNotBlank(treeNode.getViewUser())) {
            String[] userids = treeNode.getViewUser().split(",");
            StringBuilder sb = new StringBuilder();
            for (String userid : userids) {
                SysUser user = userMap.get(Long.parseLong(userid));
                if (user != null && StringUtils.isNotEmpty(user.getOrgUserId())) {
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                    if (sinoPassUserDTO != null) {
                        sb.append(sinoPassUserDTO.getViewName()).append(",");
                    }
                }
            }
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
                treeNode.setViewUser(sb.deleteCharAt(sb.length() - 1).toString());
            }
        }
    }


    private void setLeaderNameOri(TableDataDirItemDto treeNode, Map<String, SysUser> userNameMap) {
        SysUser user = userNameMap.get(treeNode.getLeaderName());
        if (user != null && StringUtils.isNotEmpty(user.getOrgUserId())) {
            SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
            if (sinoPassUserDTO != null) {
                treeNode.setLeaderNameOri(sinoPassUserDTO.getViewName());
            }
        }
    }

    private void replaceNormalTreeNode(List<Node> treeData, TableInfo t, TgNodeMapping mapping) {
        Node node = treeData.stream().filter(n -> n.getId().equals(mapping.getDirItemId())).findFirst().orElse(null);
        if (node != null) {
            TableDataDirItemDto treeNode = new TableDataDirItemDto();
            BeanUtils.copyProperties(node, treeNode);
            BeanUtils.copyProperties(t, treeNode);
            treeNode.setId(node.getId());
            treeNode.setTableId(t.getId());
            treeNode.setIcon(CommonConstants.ICON_TABLE);
            treeNode.setNodeViewName(t.getTableName());
            setLeaderNameOri(treeNode);
            setViewUser(treeNode);
            setTemplateAuditInfo(treeNode);
            treeData.remove(node);
            treeData.add(treeNode);
            log.debug("-------------------替换旧表单节点---------------------");
            log.debug("旧表单节点: {}", node);
            log.debug("新表单节点: {}", treeNode);
        }
    }

    private DocItemBO replaceNormalTreeNode(List<Node> treeData, TgDocInfo d, TgNodeMapping mapping) {
        Node node = treeData.stream().filter(n -> n.getId().equals(mapping.getDirItemId())).findFirst().orElse(null);
        if (Objects.isNull(node)) {
            return null;
        }

        DocDataDirItemDto treeNode = new DocDataDirItemDto();
        BeanUtils.copyProperties(node, treeNode);
        BeanUtils.copyProperties(d, treeNode);
        treeNode.setId(node.getId());
        treeNode.setIcon(CommonConstants.ICON_DOC);
        treeNode.setDocName(d.getName());
        treeNode.setNodeViewName(d.getName());
        treeNode.setAssetType(Optional.ofNullable(d.getType()).map(String::toUpperCase).orElse(""));
        treeNode.setProcessId(d.getProcessId());
        treeNode.setDocId(d.getId());
        treeNode.setParentId(Optional.ofNullable(treeNode.getDirId()).orElse(0L));

//        setLeaderInfo(d, treeNode);

        treeData.remove(node);
        treeData.add(treeNode);
        log.debug("-------------------替换旧文档节点---------------------");
        log.debug("旧文档节点: {}", node);
        log.debug("新文档节点: {}", treeNode);

        return new DocItemBO(d, treeNode);
    }

    private void setLeaderInfo(DocItemBO item, Map<Long, SysUser> userMap) {
        TgDocInfo d = item.getDocInfo();
        DocDataDirItemDto treeNode = item.getTreeNode();

        SysUser user = userMap.get(d.getOwnerId());
        treeNode.setLeaderName(user.getUserName());
        if (StringUtils.isNotEmpty(user.getOrgUserId())) {
            SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
            if (sinoPassUserDTO != null) {
                treeNode.setLeaderNameOri(sinoPassUserDTO.getViewName());
            }
        }
    }

    private void setLeaderInfo(TgDocInfo d, DocDataDirItemDto treeNode) {
        SysUser user = sysUserService.selectUserById(d.getOwnerId());
        treeNode.setLeaderName(user.getUserName());
        if (StringUtils.isNotEmpty(user.getOrgUserId())) {
            SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
            if (sinoPassUserDTO != null) {
                treeNode.setLeaderNameOri(sinoPassUserDTO.getViewName());
            }
        }
    }

    private void setViewUser(TableDataDirItemDto treeNode) {
        //处理可查看人员
        if (StringUtils.isNotBlank(treeNode.getViewUser())) {
            String[] userids = treeNode.getViewUser().split(",");
            StringBuilder sb = new StringBuilder();
            for (String userid : userids) {
                SysUser user = sysUserService.selectUserById(Long.parseLong(userid));
                if (user != null && StringUtils.isNotEmpty(user.getOrgUserId())) {
                    SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
                    if (sinoPassUserDTO != null) {
                        sb.append(sinoPassUserDTO.getViewName()).append(",");
                    }
                }
            }
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
                treeNode.setViewUser(sb.deleteCharAt(sb.length() - 1).toString());
            }
        }
    }

    private void setTemplateAuditInfo(TableDataDirItemDto treeNode) {
        // 处理提数模板+审核流程
        List<TemplateAuditProcessEasyDto> templateAuditProcessEasyDtos = templateService.queryProcessesByBaseTableId(treeNode.getId());
        String tad = templateAuditProcessEasyDtos.stream().map(d -> d.getTemplateName() + "(" + d.getProcessName() + ")").collect(Collectors.joining("、"));
        treeNode.setTemplateAuditInfo(tad);
    }

    private void setLeaderNameOri(TableDataDirItemDto treeNode) {
        SysUser user = sysUserService.selectUserByUserName(treeNode.getLeaderName());
        if (user != null && StringUtils.isNotEmpty(user.getOrgUserId())) {
            SinoPassUserDTO sinoPassUserDTO = SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId());
            if (sinoPassUserDTO != null) {
                treeNode.setLeaderNameOri(sinoPassUserDTO.getViewName());
            }
        }
    }

    private void addNewTreeNodeAndMapping(List<Node> treeData, TableInfo t) {

        DataDir dataDir = getDataDir(t);
        dataDir.insert();
        log.info("-------------------新增表单DataDir和Mapping---------------------");
        log.info("DataDir:{}", dataDir);
        // 想要正常看到申请节点，必须加入映射
        TgNodeMapping mapping = new TgNodeMapping() {{
            setDirItemId(dataDir.getId());
            setNodeId(t.getId());
            setApplicantId(t.getCreateUserId());
            setIcon(CommonConstants.ICON_TABLE);
        }};
        mapping.insert();
        log.info("Mapping:{}", mapping);
        TableDataDirItemDto treeNode = new TableDataDirItemDto();
        BeanUtils.copyProperties(t, treeNode);
        BeanUtils.copyProperties(dataDir, treeNode);
        treeNode.setTableId(t.getId());

        treeData.add(treeNode);
    }

    private DataDir getDataDir(TableInfo t) {
        return new DataDir() {{
            setDirName("");
            setParentId(0L);
            setSort(DataDirConst.DEFAULT_SORT);
            setStatus(1);
            setLastUpdate(DateUtils.getNowDate());
            setTarget(CommonConstants.DATA_DIR);
            setApplicantId(t.getCreateUserId());
            setNodeId(t.getId());
            setIcon(CommonConstants.ICON_TABLE);
        }};
    }

    private void addNewTreeNodeAndMapping(List<Node> treeData, TgDocInfo d, List<TgApplicationInfo> applicationInfos) {
        DataDir dataDir = getDataDir(d);
        dataDir.insert();
        log.debug("-------------------新增文档DataDir和Mapping---------------------");
        log.debug("DataDir: {}", dataDir);
        // 想要正常看到申请节点，必须加入映射
        TgNodeMapping mapping = new TgNodeMapping() {{
            setDirItemId(dataDir.getId());
            setNodeId(d.getId());
            setApplicantId(d.getOwnerId());
            setIcon(CommonConstants.ICON_DOC);
        }};
        mapping.insert();
        log.info("Mapping: {}", mapping);
        DocDataDirItemDto treeNode = new DocDataDirItemDto();
        BeanUtils.copyProperties(d, treeNode);
        BeanUtils.copyProperties(dataDir, treeNode);
        treeNode.setIcon(CommonConstants.ICON_DOC);
        treeNode.setDocName(d.getName());
        treeNode.setAssetType(Optional.ofNullable(d.getType()).map(String::toUpperCase).orElse(""));
        treeNode.setProcessId(d.getProcessId());
        treeNode.setParentId(Optional.ofNullable(treeNode.getDirId()).orElse(0L));
        treeNode.setDocId(d.getId());
        setLeaderInfo(d, treeNode);
        setDocAuthorization(d, applicationInfos, treeNode);
        treeData.add(treeNode);
    }

    private void setDocAuthorization(TgDocInfo d, List<TgApplicationInfo> applicationInfos, DocDataDirItemDto treeNode) {
        Long userId = SecurityUtils.getUserId();
        treeNode.setNeed2Audit(d.getNeed2Audit() && !Objects.equals(userId, d.getOwnerId()));
        if (!d.getNeed2Audit()) {
            treeNode.getAuthorization().add(DataDirConst.DocPermission.CAN_VIEW_PDF);
            if (d.getCanDownloadPdf()) {
                treeNode.getAuthorization().add(DataDirConst.DocPermission.CAN_DOWNLOAD_PDF);
            }
            if (d.getCanDownloadSourceFile()) {
                treeNode.getAuthorization().add(DataDirConst.DocPermission.CAN_DOWNLOAD_SRC);
            }
        }

        if (d.getOwnerId().equals(ThreadContextHolder.getSysUser().getUserId())) {
            treeNode.getAuthorization().addAll(Arrays.asList(DataDirConst.DocPermission.CAN_VIEW_PDF,
                    DataDirConst.DocPermission.CAN_DOWNLOAD_PDF, DataDirConst.DocPermission.CAN_DOWNLOAD_SRC));
        }
        d.getWhitelistUsers().forEach(u -> {
            if (u.getUserId().equals(ThreadContextHolder.getSysUser().getUserId())) {
                treeNode.getAuthorization().addAll(u.getAuthorization());
            }
        });
        val docAuth = applicationInfos.stream()
                .filter(a -> a.getApplicationType().equals(ApplicationConst.ApplicationType.DOC_APPLICATION)
                        && a.getApplicantId().equals(ThreadContextHolder.getSysUser().getUserId())
                        && a.getDocId().equals(d.getId())
                        && a.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.AUDIT_PASS))
                .map(a -> JsonBeanConverter.convert2Obj(a).getDocAuthorization())
                .flatMap(Collection::stream).distinct().sorted().collect(Collectors.toList());
        treeNode.getAuthorization().addAll(docAuth);
        treeNode.setAuthorization(treeNode.getAuthorization().stream().distinct().sorted().collect(Collectors.toList()));
    }

    private List<Integer> buildDocAuthorization(TgDocInfo d, List<TgApplicationInfo> applicationInfos) {
        Long userId = SecurityUtils.getUserId();
        List<Integer> result = new ArrayList<>();
        boolean needAudit = BooleanUtils.isTrue(d.getNeed2Audit());

        JsonBeanConverter.convert2Obj(d);

        if (!needAudit) {
            result.add(DataDirConst.DocPermission.CAN_VIEW_PDF);
            if (d.getCanDownloadPdf()) {
                result.add(DataDirConst.DocPermission.CAN_DOWNLOAD_PDF);
            }
            if (d.getCanDownloadSourceFile()) {
                result.add(DataDirConst.DocPermission.CAN_DOWNLOAD_SRC);
            }
        }

        if (d.getOwnerId().equals(ThreadContextHolder.getSysUser().getUserId())) {
            result.addAll(Arrays.asList(DataDirConst.DocPermission.CAN_VIEW_PDF,
                    DataDirConst.DocPermission.CAN_DOWNLOAD_PDF, DataDirConst.DocPermission.CAN_DOWNLOAD_SRC));
        }
        if (CollectionUtils.isNotEmpty(d.getWhitelistUsers())) {
            for (WhiteListUser u : d.getWhitelistUsers()) {
                if (u.getUserId().equals(userId)) {
                    result.addAll(u.getAuthorization());
                }
            }
        }
        val docAuth = applicationInfos.stream()
                .filter(a -> a.getApplicationType().equals(ApplicationConst.ApplicationType.DOC_APPLICATION)
                        && a.getApplicantId().equals(userId)
                        && a.getDocId().equals(d.getId())
                        && a.getCurrentAuditProcessStatus().equals(ApplicationConst.AuditStatus.AUDIT_PASS))
                .map(a -> JsonBeanConverter.convert2Obj(a).getDocAuthorization())
                .flatMap(Collection::stream).distinct().sorted().collect(Collectors.toList());
        result.addAll(docAuth);
        result = result.stream().distinct().sorted().collect(Collectors.toList());
        if (!new HashSet<>(result).containsAll(DataDirConst.DocPermission.ALL_PERMISSION)) {
            if (needAudit && !Objects.equals(userId, d.getOwnerId())) {
                result.add(DataDirConst.DocPermission.NEED_AUDIT);
            }
        }
        return result;
    }

    private DataDir getDataDir(TgDocInfo d) {
        return new DataDir() {{
            setDirName("");
            setParentId(Optional.ofNullable(d.getDirId()).orElse(0L));
            setSort(DataDirConst.DEFAULT_SORT);
            setStatus(1);
            setLastUpdate(DateUtils.getNowDate());
            setTarget(CommonConstants.DATA_DIR);
            setApplicantId(d.getOwnerId());
            setNodeId(d.getId());
            setIcon(CommonConstants.ICON_DOC);
        }};
    }

    @Override
    public List listTablesByDirId(Long dirId) {
        return tableInfoService.getListByDirId(dirId);
    }

    @Override
    public DataDirListVO selectSonOfParentDir(Long parentId, Integer status) {
        Integer existId = this.dataDirMapper.existOutOfDir();
        List<DataDir> list = dataDirMapper.selectSonOfParentDir(parentId, CommonConstants.DATA_DIR, status);
        return new DataDirListVO(list, Objects.nonNull(existId));
    }

    @Override
    public DataDir getByNodeId(Long nodeId) {
        List<DataDir> dirs = baseMapper.selectList(new QueryWrapper<DataDir>().lambda().eq(DataDir::getNodeId, nodeId));
        int size = org.apache.commons.collections4.CollectionUtils.size(dirs);
        if (size == 0) {
            return null;
        }
        if (size > 1) {
            log.error("exist more than 1: nodeId={}", nodeId);
            return null;
        }

        return dirs.get(0);
    }

    /**
     * @see DataDirServiceImpl#getDirTreeGroup
     */
    @Override
    public AjaxResult<IPage<HomeDataDirVO>> pageQueryDir(DirPageQueryRequest request) {
        request.setPage(Math.max(1, request.getPage()));
        if (Objects.nonNull(request.getDirId()) && request.getDirId() != 0L) {
            DataDirListVO dirs = selectSonOfParentDir(request.getDirId(), DataDirConst.Status.ENABLE);
            List<Long> dirIds = dirs.getDirs().stream().map(DataDir::getId).collect(Collectors.toList());
            dirIds.add(request.getDirId());
            request.setDirIds(dirIds);
            request.setDirId(null);
        }

        IPage<DataDirView> viewPage = this.dataDirMapper.pageQueryDirView(request.buildPage(), request);
        Page<HomeDataDirVO> resultPage = new Page<>();
        List<DataDirView> records = viewPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return AjaxResult.success(resultPage);
        }

        // 用户-表单
        List<String> nameList = records.stream()
                .map(DataDirView::getLeaderName)
                .filter(StringUtils::isNoneBlank)
                .collect(Collectors.toList());
        Map<String, SysUser> userNameMap = Collections.emptyMap();
        if (CollectionUtils.isNotEmpty(nameList)) {
            List<SysUser> usersByName = sysUserService.selectUserByUserNames(nameList);
            userNameMap = usersByName.stream().collect(Collectors.toMap(SysUser::getUserName,
                    v -> v, (front, current) -> current));
        }

        // 用户-文档
        Map<Long, SysUser> userMap = Collections.emptyMap();
        List<Long> userIds = records.stream().map(DataDirView::getOwnerId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(userIds)) {
            List<SysUser> sysUsers = sysUserService.selectUserByIds(userIds);
            userMap = sysUsers.stream().collect(Collectors.toMap(SysUser::getUserId, v -> v, (front, current) -> current));
        }

        AtomicInteger counter = new AtomicInteger((request.getPage() - 1) * request.getSize());
        Map<Long, SysUser> finalUserMap = userMap;
        Map<String, SysUser> finalUserNameMap = userNameMap;
        List<HomeDataDirVO> dirs = records.stream()
                .map(view -> viewToDataDirVO(finalUserNameMap, finalUserMap, view))
                .collect(Collectors.toList());

        Map<Long, DataDirView> viewMap = Lambda.buildMap(records, DataDirView::getId);
        this.fillPermissionsAndType(dirs, viewMap);

        for (HomeDataDirVO dir : dirs) {
            this.setOrgAndApplicantName(dir);
            dir.setSortIndex(counter.incrementAndGet());
        }
        resultPage.setRecords(dirs);
        resultPage.setPages(viewPage.getPages());
        resultPage.setCurrent(viewPage.getCurrent());
        resultPage.setTotal(viewPage.getTotal());

        return AjaxResult.success(resultPage);
    }

    private void fillPermissionsAndType(List<HomeDataDirVO> dirs, Map<Long, DataDirView> viewMap) {
        // 文档 权限
        Map<Long, HomeDataDirVO> docDirMap = dirs.stream()
                .filter(d -> Objects.equals(d.getIcon(), CommonConstants.ICON_DOC))
                .collect(Collectors.toMap(HomeDataDirVO::getId, v -> v, (front, current) -> current));
        if (MapUtils.isNotEmpty(docDirMap)) {
            LambdaQueryWrapper<TgApplicationInfo> wrapper = new QueryWrapper<TgApplicationInfo>().lambda()
                    .eq(TgApplicationInfo::getApplicationType, ApplicationConst.ApplicationType.DOC_APPLICATION)
                    .eq(TgApplicationInfo::getApplicantId, ThreadContextHolder.getSysUser().getUserId())
                    .in(TgApplicationInfo::getDocId, docDirMap.keySet());

            List<TgApplicationInfo> applicationInfos = TgApplicationInfo.newInstance().selectList(wrapper);

            List<TgDocInfo> docs = docInfoMapper.selectBatchIds(docDirMap.keySet());
            for (TgDocInfo doc : docs) {
                HomeDataDirVO vo = docDirMap.get(doc.getId());
                if (Objects.isNull(vo)) {
                    log.warn("mapping error docId={}", doc.getId());
                    continue;
                }
                List<Integer> authorization = this.buildDocAuthorization(doc, applicationInfos);
                vo.setResourceType(CommonConstants.DOC_DISPLAY_NAME);
                vo.setPermissions(authorization);
                vo.setAssetType(Optional.ofNullable(doc.getType()).map(String::toUpperCase).orElse(""));
            }
        }

        // 表单 权限
        Map<Long, HomeDataDirVO> tableMap = dirs.stream()
                .filter(d -> Objects.equals(d.getIcon(), CommonConstants.ICON_TABLE))
                .collect(Collectors.toMap(HomeDataDirVO::getId, v -> v, (front, current) -> current));
        if (MapUtils.isNotEmpty(tableMap)) {
            List<TableInfo> tableInfos = tableInfoService.getBaseMapper().selectBatchIds(tableMap.keySet());
            Long userId = SecurityUtils.getUserId();
            for (TableInfo tableInfo : tableInfos) {
                HomeDataDirVO vo = tableMap.get(tableInfo.getId());
                if (Objects.isNull(vo)) {
                    log.warn("mapping error tableId={}", tableInfo.getId());
                    continue;
                }

                List<Integer> permissions = buildTablePermissions(userId, tableInfo);
                vo.setPermissions(permissions.stream().distinct().sorted().collect(Collectors.toList()));
                vo.setAssetType("表");
                vo.setResourceType(CommonConstants.DATA_DISPLAY_NAME);
            }
        }

        Map<Long, HomeDataDirVO> templateMap = dirs.stream()
                .filter(d -> Objects.equals(d.getIcon(), CommonConstants.ICON_TEMPLATE))
                .collect(Collectors.toMap(HomeDataDirVO::getId, v -> v, (front, current) -> current));
        if (MapUtils.isNotEmpty(templateMap)) {
            Set<Long> templateIds = templateMap.keySet();
            Set<Long> hasAssetsTemplateIds = userDataAssetsDAO.existTemplateId(templateIds);
            for (HomeDataDirVO vo : templateMap.values()) {
                vo.setPermissions(hasAssetsTemplateIds.contains(vo.getId())
                        ? DataDirConst.TemplatePermission.HAVE : DataDirConst.TemplatePermission.NOT);
                DataDirView view = viewMap.get(vo.getId());
                vo.setAssetType(TemplateTypeEnum.of(view.getTemplateType()).map(TemplateTypeEnum::getDesc).orElse("模板"));
                vo.setResourceType(CommonConstants.TEMPLATE_DISPLAY_NAME);
            }
        }

    }

    @Override
    public List<Integer> buildTablePermissions(Long userId, TableInfo tableInfo) {
        // 全表申请
        List<TgApplicationInfo> passApply = TgApplicationInfo.newInstance()
                .selectList(new QueryWrapper<TgApplicationInfo>().lambda()
                        .select(TgApplicationInfo::getBaseTableId)
                        .eq(TgApplicationInfo::getBaseTableId, tableInfo.getId())
                        .eq(TgApplicationInfo::getCurrentAuditProcessStatus, ApplicationConst.AuditStatus.AUDIT_PASS)
                        .eq(TgApplicationInfo::getApplicantId, userId)
                        .eq(TgApplicationInfo::getApplicationType, CommonConstants.ICON_TABLE)
                );

        List<Integer> permissions = new ArrayList<>();
        Boolean hasView = Optional.ofNullable(tableInfo.getViewUser())
                .filter(com.sinohealth.common.utils.StringUtils::isNoneBlank)
                .map(v -> v.split(",")).map(v -> Stream.of(v)
                        .anyMatch(d -> Objects.equals(d, String.valueOf(userId)))).orElse(false);
        permissions.add(DataDirConst.TablePermission.DATA_ASSERTS);
        permissions.add(DataDirConst.TablePermission.APPLICATION_DATA);
        // 无权限且绑定了工作流
        if (!Objects.equals(SecurityUtils.getUsername(), tableInfo.getLeaderName())
                && !hasView
                && CollectionUtils.isEmpty(passApply)
                && Objects.nonNull(tableInfo.getProcessId())) {
            permissions.add(DataDirConst.TablePermission.APPLICATION);
        }
        return permissions;
    }

    private void setOrgAndApplicantName(HomeDataDirVO d) {
        String[] split = d.getLeaderNameOri().split("-");
        if (split.length < 2) {
            return;
        }
        d.setLeaderOri(Optional.ofNullable(split[0]).orElse(""));
        d.setLeaderName(Optional.ofNullable(split[1]).orElse(""));
    }

    private void sort(List<HomeDataDirVO> dirs) {
        Comparator<HomeDataDirVO> comparator = (node1, node2) -> {
            Integer resourceA = CommonConstants.resouceSortMap.get(node1.getResourceType());
            Integer resourceB = CommonConstants.resouceSortMap.get(node2.getResourceType());

            if (resourceA.compareTo(resourceB) == 0) {
//                产品和业务方存在排序分歧, 这里注释掉以备后续改动
                if (node2.getBussinessType().compareTo(node1.getBussinessType()) == 0) {
                    return node1.getDisplayName().compareTo(node2.getDisplayName());
                }
                return node1.getBussinessType().compareTo(node2.getBussinessType());
//                return node1.getDisplayName().compareTo(node2.getDisplayName());
            }
            return resourceA.compareTo(resourceB);
        };
        dirs.sort(comparator);
    }

    private static HomeDataDirVO viewToDataDirVO(Map<String, SysUser> userNameMap,
                                                 Map<Long, SysUser> userMap,
                                                 DataDirView v) {
        Optional<SysUser> userOpt = Optional.ofNullable(userMap.get(v.getOwnerId()));
        String orgName = null;
        if (Objects.equals(v.getIcon(), CommonConstants.ICON_DOC)) {
            orgName = userOpt.map(SysUser::getOrgUserId).map(SinoipaasUtils::mainEmployeeSelectbyid)
                    .map(SinoPassUserDTO::getViewName).orElse("");
        } else if (Objects.equals(v.getIcon(), CommonConstants.ICON_TABLE)) {
            orgName = Optional.ofNullable(userNameMap.get(v.getLeaderName())).map(SysUser::getOrgUserId)
                    .map(SinoipaasUtils::mainEmployeeSelectbyid).map(SinoPassUserDTO::getViewName).orElse("");
        } else if (Objects.equals(v.getIcon(), CommonConstants.ICON_TEMPLATE)) {
            orgName = v.getLeaderName();
        }

        // 目录为0 即根目录 展示为其他
        String dirName = StringUtils.isBlank(v.getDirName()) && Objects.equals(v.getDirId(), 0L) ? "其他" : v.getDirName();
        if (Objects.isNull(dirName)) {
            dirName = "";
        }
        return HomeDataDirVO.builder()
                .id(v.getId())
                .icon(v.getIcon())
                .dirId(v.getDirId())
                .displayName(v.getName())
                .status(v.getStatus())
                .comment(v.getComment())
                .leaderNameOri(orgName)
                .processId(v.getProcessId())
                .tableName(v.getTableName())
                .disSort(v.getDisSort())
                .dirName(dirName)
                .bussinessType(Optional.ofNullable(v.getBizDirType()).orElse(""))
                .dirItemType(ApplicationConst.DirItemTypeEnum.getByType(v.getIcon())
                        .map(ApplicationConst.DirItemTypeEnum::getDesc).orElse(""))
                .updateTime(v.getUpdateTime())
                .build();
    }

    @Override
    public Integer delete(Long dirId) {
        // 级联获取所有目录节点, 修改所有目录下已挂载的文件节点后, 级联删除所有目录节点
        List<? extends Node> dirTreeGroup = getDirTreeGroup(dirId, CommonConstants.ICON_FILE, null, false);
        List<Long> dirs4Deleting = new ArrayList<>();
        dirs4Deleting.add(dirId);
        dirTreeGroup.forEach((d) -> {
            List<Long> res = new ArrayList<>();
            dirs4Deleting.addAll(TreeUtils.traverse4DeletingDir(d, res));
        });

        List<DataManageFormDto> tables = new ArrayList<>();

        dirs4Deleting.forEach((id) -> tables.addAll(tableInfoService.getListByDirId(dirId)));

        tables.forEach((t) -> tableInfoService.updateDirIdOfTableInfo(new TableInfoDto() {{
            setId(t.getId());
            setDirId(null);
        }}));

        return dataDirMapper.deleteBatchIds(dirs4Deleting);
    }

}
