package com.sinohealth.api.dto.assetManage;

import lombok.Data;

import java.util.List;

/**
 * @Author shallwetalk
 * @Date 2023/7/27
 */
@Data
public class TreeNode {

    private Integer id;

    private String name;

    private List<TreeNode> childNode;

}
