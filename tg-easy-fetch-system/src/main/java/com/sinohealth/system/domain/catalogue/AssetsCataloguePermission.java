package com.sinohealth.system.domain.catalogue;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author shallwetalk
 * @Date 2023/8/10
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@TableName("assets_catalogue_permission")
public class AssetsCataloguePermission extends Model<AssetsCataloguePermission> implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("catalogue_id")
    private Integer catalogueId;

    @TableField("user_id")
    private Long userId;

    @TableField("dept_id")
    private String deptId;

    @TableField("type")
    private Integer type;

    @TableField("readable")
    private Integer readable;

    @TableField("assets_manager")
    private Integer assetsManager;

    @TableField("catalogue_manager")
    private Integer catalogueManager;

}
