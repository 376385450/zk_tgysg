package com.sinohealth.common.utils.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.constant.CommonConstants;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

public interface Node<E> {
    Long getParentId();

    List<E> getChildren();

    Long getId();

    Integer getSort();

    String getNodeViewName();

    void setIcon(String icon);

    String getIcon();

    void setMoved(Integer moved);

    Integer getMoved();

    Date getLastUpdate();

    Integer getRequireTimeType();

    Integer getRequireAttr();

    String getClientNames();

    @JsonIgnore
    default void markDeleteWhileSearch() {
        setMoved(99);
    }

    @JsonIgnore
    default boolean deleteWhileSearch() {
        return Objects.equals(getMoved(), 99);
    }

    @JsonIgnore
    static List<Node> flat(List<? extends Node> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<Node> result = new ArrayList<>();
        for (Node node : list) {
            result.add(node);
            result.addAll(flat(node.getChildren()));
        }
        return result;
    }

    @JsonIgnore
    default boolean dirIcon() {
        return Objects.equals(getIcon(), CommonConstants.ICON_FILE);
    }

    @JsonIgnore
    default boolean assetsIcon() {
        return Objects.equals(getIcon(), CommonConstants.ICON_DATA_ASSETS);
    }
}
