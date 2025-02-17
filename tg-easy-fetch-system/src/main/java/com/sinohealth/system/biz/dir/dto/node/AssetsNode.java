package com.sinohealth.system.biz.dir.dto.node;

import com.alibaba.excel.util.BooleanUtils;
import com.sinohealth.system.domain.constant.ApplicationConst;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 我的资产 抽象树节点
 *
 * @see com.sinohealth.common.utils.dto.Node 旧实现
 * @author kuangchengping@sinohealth.cn 
 * 2024-01-11 14:53
 */
public interface AssetsNode extends Serializable {

    String ROOT = "ROOT";

    Long getBizId();

    String getNodeId();

    /**
     * 前端渲染表格使用，要保证节点唯一
     */
    String getNId();

    void setNId(String nid);

    String getPId();

    void setPId(String pid);

    String getName();

    String getParentId();

    void setParentId(String id);

    /**
     * 注意仅后端构建树时使用
     */
    List<AssetsNode> getChildren();

    void setChildren(List<AssetsNode> child);

    void setIcon(String icon);

    /**
     * @see ApplicationConst.AssetsIcon
     */
    String getIcon();

    void setHidden(Boolean hidden);

    Boolean getHidden();

    LocalDateTime getCreateTime();

    LocalDateTime getUpdateTime();

    Long getCreator();

    String getCreatorName();

    void setCreatorName(String creator);

    static String buildId(Long id, String icon) {
        return id + "#" + icon;
    }

    static Pair<Long, String> parseId(String nodeId) {
        String[] arr = nodeId.split("#");
        if (arr.length != 2) {
            return null;
        }
        try {
            long id = Long.parseLong(arr[0]);
            return Pair.of(id, arr[1]);
        } catch (Exception e) {
            return null;
        }
    }

    default void markRoot() {
        this.setParentId(ROOT);
    }

    default void fillParentId(Long id, String icon) {
        this.setParentId(buildId(id, icon));
    }

    default boolean isHidden() {
        return BooleanUtils.isTrue(getHidden());
    }

    default boolean isContainer() {
        return Objects.equals(getIcon(), ApplicationConst.AssetsIcon.CUSTOMER) || Objects.equals(getIcon(), ApplicationConst.AssetsIcon.PROJECT);
    }

    default boolean isData() {
        return Objects.equals(getIcon(), ApplicationConst.AssetsIcon.DATA);
    }

    default boolean isProject() {
        return Objects.equals(getIcon(), ApplicationConst.AssetsIcon.PROJECT);
    }
}
