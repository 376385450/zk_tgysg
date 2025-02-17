package com.sinohealth.system.domain.catalogue;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Description 资产目录
 * @Author shallwetalk
 * @Date 2023/8/4
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("assets_catalogue")
public class AssetsCatalogue {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("parent_id")
    private Integer parentId;

    @TableField("code")
    private String code;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("global_sort")
    private Double globalSort;

    @TableField("description")
    private String description;

    @TableField("icon")
    private String icon;

    @TableField("path")
    private String path;

    @TableField("level")
    private Integer level;

    @TableField("catalogue_flow_id")
    private Long catalogueFlowId;

    @TableField("service_flow_id")
    private Long serviceFlowId;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_by")
    private Long updatedBy;

    @TableField("updated_at")
    private Date updatedAt;

    @TableLogic
    private Long deleted;

}
