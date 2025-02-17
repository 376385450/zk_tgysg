package com.sinohealth.system.biz.dir.util;

import com.sinohealth.common.utils.StrUtil;
import com.sinohealth.system.biz.dataassets.dto.request.AssetsDirRequest;
import com.sinohealth.system.biz.dir.dto.node.AssetsNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author kuangchengping@sinohealth.cn 
 * 2024-01-12 11:36
 */
@Slf4j
public class AssetsTreeUtil {

    public static List<AssetsNode> reSort(List<AssetsNode> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return nodes;
        }
        for (AssetsNode node : nodes) {
            List<AssetsNode> children = node.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {
                List<AssetsNode> sorted = reSort(children);
                node.setChildren(sorted);
            }
        }
        return nodes.stream().sorted(Comparator.comparing(AssetsNode::getCreateTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 清理当前节点的所有空目录子节点
     *
     * @return 当前节点是否为空目录节点
     */
    public static boolean cleanEmptyDir(AssetsNode root) {
        if (!root.isContainer()) {
            return false;
        }
        List<AssetsNode> children = root.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            root.setHidden(true);
            return true;
        }

        boolean empty = true;
        for (AssetsNode child : children) {
            if (!cleanEmptyDir(child)) {
                empty = false;
            }
        }
        if (empty) {
            root.setHidden(true);
        }
        children.removeIf(AssetsNode::isHidden);
        return empty;
    }

    /**
     * 树搜索 短路搜索
     */
    public static boolean search(AssetsNode node, AssetsDirRequest request) {
        // 当前节点匹配后默认带出所有层级子节点
        boolean hit = matchNode(node, request);
        if (hit) {
            return true;
        }
        List<AssetsNode> child = node.getChildren();
        if (CollectionUtils.isEmpty(child)) {
            return false;
        }
        List<AssetsNode> remove = new ArrayList<>();
        for (AssetsNode co : child) {
            boolean childHit = search(co, request);
            if (childHit) {
                hit = true;
            } else {
                remove.add(co);
            }
        }
        child.removeAll(remove);
        return hit;
    }

    public static boolean matchNode(AssetsNode node, AssetsDirRequest request) {
        // 没搜索关键字，但是加了类型过滤时
        if (request.noSearch()) {
            return true;
        }

        return StringUtils.containsIgnoreCase(node.getName(), request.getSearch().trim());
//        if (StringUtils.contains(node.getName(), request.getSearch())) {
//            return true;
//        }
//        if (node.isData()) {
//            if (!(node instanceof UserDataAssetsNode)) {
//                log.error("脏数据，节点未替换: node={}", node);
//                return false;
//            }
//            UserDataAssetsNode item = (UserDataAssetsNode) node;
//            if (StringUtils.contains(item.getNewProjectName(), request.getSearch())) {
//                return true;
//            }
//            if (StringUtils.contains(item.getClientNames(), request.getSearch())) {
//                return true;
//            }
//        }
//        return false;
    }

    private static void fillNewNId(AssetsNode node) {
        if (Objects.isNull(node)) {
            return;
        }
        node.setNId(StrUtil.randomAlpha(8));
        if (CollectionUtils.isNotEmpty(node.getChildren())) {
            for (AssetsNode child : node.getChildren()) {
                fillNewNId(child);
            }
        }
    }

    public static void fillChild(AssetsNode node, Map<String, List<AssetsNode>> parentMap, Set<Long> visited) {
        String id = node.getNodeId();
        List<AssetsNode> nodes = parentMap.get(id);
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        if (Objects.isNull(node.getChildren())) {
            node.setChildren(new ArrayList<>());
        }

        // 数据资产的子节点需要做内存复制 因为会出现重复挂载
        // 项目下的数据资产也会重复挂载其在创建的时候就做了复制
        if (node.isData()) {
            boolean firstAdd = visited.add(node.getBizId());
            // 重复访问，深拷贝所有子节点
            if (!firstAdd) {
                List<AssetsNode> childList = new ArrayList<>();
                for (AssetsNode assetsNode : nodes) {
                    try {
                        java.io.ByteArrayOutputStream byteOutput = new java.io.ByteArrayOutputStream();
                        ObjectOutputStream output = new ObjectOutputStream(byteOutput);
                        output.writeObject(assetsNode);

                        ByteArrayInputStream byteInput = new ByteArrayInputStream(byteOutput.toByteArray());
                        ObjectInputStream input = new ObjectInputStream(byteInput);
                        AssetsNode child = (AssetsNode) input.readObject();
                        fillNewNId(child);
                        child.setPId(node.getNId());
                        childList.add(child);
                    } catch (Exception e) {
                        log.error("", e);
                    }
                }
                node.setChildren(childList);
                return;
            }
        }

        node.getChildren().addAll(nodes);
        for (AssetsNode child : nodes) {
            child.setPId(node.getNId());
            fillChild(child, parentMap, visited);
        }
    }

    public static List<AssetsNode> treeToList(List<AssetsNode> tree) {
        List<AssetsNode> list = new ArrayList<>();
        for (AssetsNode node : tree) {
            appendChild(node, list);
        }
        return list;
    }

    public static void appendChild(AssetsNode node, List<AssetsNode> list) {
        if (Objects.isNull(node)) {
            return;
        }
        list.add(node);
        if (Objects.isNull(node.getChildren())) {
            node.setChildren(new ArrayList<>());
        }

        for (AssetsNode child : node.getChildren()) {
            appendChild(child, list);
        }
        node.getChildren().clear();
    }
}
