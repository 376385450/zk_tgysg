package com.sinohealth.system.util;

import cn.hutool.core.lang.Tuple;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.utils.StringUtils;
import com.sinohealth.common.utils.dto.Node;
import com.sinohealth.system.dto.DataDirDto;
import com.sinohealth.system.biz.dir.dto.GetMyDataDirTreeParam;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Rudolph
 * @Date 2022-06-09 10:46
 * @Desc 通用树状结构工具类
 */

public class TreeUtils {

    /**
     * 将节点列表组成树状列表
     * fixme 太耗时了
     *
     * @param parentId
     * @param treeData
     * @return
     */
    public static List<? extends Node> transformTreeGroup(Long parentId, List<? extends Node> treeData, Map<Long, Node> allNodes) {
        List<Node> roots = treeData.stream().filter(d -> d.getParentId().equals(parentId))
                .sorted(Comparator.comparing(Node::getSort))
                .collect(Collectors.toList());
        List<Node> leaves = treeData.stream().filter(d -> !d.getParentId().equals(parentId))
                .collect(Collectors.toList());
        // 如果leaves缺少对应的roots, 则将其加入到 roots 中
        if (allNodes != null) {
            leaves.forEach(n1 -> {
                List<Long> rootIds = roots.stream().map(n2 -> n2.getId()).collect(Collectors.toList());
                if (parentId != 0L && n1.getParentId() != null && !rootIds.contains(n1.getParentId()) && allNodes.containsKey(n1.getParentId())) {
                    roots.add(allNodes.get(n1.getParentId()));
                }
            });
        }

        roots.stream().forEach((root) -> traverse4BuildingTree(root, leaves));
        return roots;
    }

    /**
     * 遍历组装树
     *
     * @param parent
     * @param leaves
     */
    public static void traverse4BuildingTree(Node parent, List<? extends Node> leaves) {
        List<Node> currentLeaves = leaves.stream()
                .filter(d -> d.getParentId().equals(parent.getId()))
                .sorted(Comparator.comparing(Node::getSort))
                .collect(Collectors.toList());
        if (currentLeaves.size() == 0) {
            return;
        }
        parent.getChildren().addAll(currentLeaves);
        leaves.removeAll(currentLeaves);
        for (Object node : parent.getChildren()) {
            traverse4BuildingTree((Node) node, leaves);
        }
    }

    /**
     * 遍历返回要删除目录的节点id
     *
     * @param parent
     * @param res
     * @return
     */
    public static List<Long> traverse4DeletingDir(Node parent, List<Long> res) {

        if (parent == null) {
            return res;
        }
        res.add(parent.getId());
        for (Object node : parent.getChildren()) {
            traverse4DeletingDir((Node) node, res);
        }
        return res;
    }

    /**
     * 递归统计子孙中的文档数和关联表数量
     *
     * @param parent
     * @return
     */
    public static Tuple traversalTree(List<? extends Node> nodeList, Node parent) {
        if (CollectionUtils.isEmpty(nodeList)) {
            return new Tuple(0, 0);
        }
        int num = 0;
        int tableNum = 0;
        for (Node node : nodeList) {
            Tuple sonTuple = traversalTree(node.getChildren(), node);
            int sonNum = (Integer) sonTuple.get(0);
            int sonTableNum = (Integer) sonTuple.get(1);
            num += sonNum;
            tableNum += sonTableNum;
            if (CommonConstants.ICON_FILE.equals(node.getIcon())) {
                DataDirDto dataDirDto = (DataDirDto) node;
                dataDirDto.setNums(sonNum);
                dataDirDto.setTableNums(sonTableNum);
            } else if (CommonConstants.ICON_TABLE.equals(node.getIcon())) {
                // 表+1
                num += 1;
                tableNum += 1;
            } else if (CommonConstants.ICON_DOC.equals(node.getIcon())) {
                // 文档+1
                num += 1;
            } else if (CommonConstants.ICON_DATA_ASSETS.equals(node.getIcon())) {
                num += 1;
                tableNum += 1;
            } else {
            }
        }
        return new Tuple(num, tableNum);
    }

    public static void filterNode(List<? extends Node> nodeList, List<String> filterIcons, GetMyDataDirTreeParam param) {
        if (CollectionUtils.isEmpty(filterIcons) && ObjectUtils.isNull(param.getRequireAttr()) && ObjectUtils.isNull(param.getRequireTimeType())
                && StringUtils.isBlank(param.getClientNames())) {
            return;
        }
        if (CollectionUtils.isEmpty(nodeList)) {
            return;
        }
        Iterator<? extends Node> iterator = nodeList.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();

            if (!node.getIcon().equals(CommonConstants.ICON_FILE)) {
                if (ObjectUtils.isNotNull(param.getRequireTimeType()) && (ObjectUtils.isNull(node.getRequireTimeType()) || !node.getRequireTimeType().equals(param.getRequireTimeType()))) {
                    iterator.remove();
                    continue;
                }
                if (StringUtils.isNotBlank(param.getClientNames()) && (StringUtils.isBlank(node.getClientNames()) || !node.getClientNames().contains(param.getClientNames()))) {
                    iterator.remove();
                    continue;
                }
                if (ObjectUtils.isNotNull(param.getRequireAttr()) && (ObjectUtils.isNull(node.getRequireAttr()) || !node.getRequireAttr().equals(param.getRequireAttr()))) {
                    iterator.remove();
                    continue;
                }
            }

            if (!filterIcons.isEmpty() && !filterIcons.contains(node.getIcon())) {
                iterator.remove();
            } else {
                filterNode(node.getChildren(), filterIcons, param);
            }
        }
    }


    // BFS排序
    public static void bfsSort(List<Node> nodeList) {
        if (CollectionUtils.isEmpty(nodeList)) {
            return;
        }

        Map<Boolean, List<Node>> moveMap = nodeList.stream()
                .collect(Collectors.groupingBy(v -> Objects.equals(v.getMoved(), CommonConstants.MOVED)));
        List<Node> movedList = moveMap.getOrDefault(true, Collections.emptyList());
        List<Node> unmovedList = moveMap.getOrDefault(false, Collections.emptyList());

        // 对当前层节点排序
        Comparator<Node> movedComparator = Comparator.comparing(Node::getSort);
        Comparator<Node> unmovedComparator = (node1, node2) -> {
            // 没有移动过则按下述规则排
            if (node1.getIcon().compareTo(node2.getIcon()) == 0) {
                if (node2.getLastUpdate() == null || node1.getLastUpdate() == null) {
                    return -1;
                }
                return node2.getLastUpdate().compareTo(node1.getLastUpdate());
            }
            return node1.getIcon().compareTo(node2.getIcon());
        };
        // 1. 将 nodeList 存入 queue
        Queue<Node> queue = new LinkedList<>();
        movedList.forEach(queue::offer);
        // 2. 排序第一层
        Collections.sort(movedList, movedComparator);

        // 3. 循环在 queue 获取一个节点, 对其 children 进行排序
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                Node node = queue.poll();
                if (CollectionUtils.isNotEmpty(node.getChildren())) {
                    Collections.sort(node.getChildren(), movedComparator);
                }
                if (node.getChildren() != null) {
                    for (Object child : node.getChildren()) {
                        queue.offer((Node) child);
                    }
                }
            }
        }


        Collections.sort(unmovedList, unmovedComparator);
        nodeList.clear();
        nodeList.addAll(0, movedList);
        nodeList.addAll(0, unmovedList);

    }


}
