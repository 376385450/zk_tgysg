package com.sinohealth.system.biz.dir.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.sinohealth.system.domain.constant.ApplicationConst;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-02-14 11:03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeDataDirVO {
    /**
     * 表id / 文档id
     * 混用这个字段 通过icon类型来区分业务数据范围
     */
    private Long id;
    /**
     * 元素所在目录id
     */
    private Long dirId;
    private Integer sortIndex;
    @ApiModelProperty("资产类型")
    private String assetType;

    @ApiModelProperty("资产类型显示名")
    private String resourceType;

    @ApiModelProperty("业务分类显示名")
    private String bussinessType;
    /**
     * 展示名： 表别名 文档名
     */
    private String displayName;
    private String icon;
    @TableField(exist = false)
    private String leaderName;
    @TableField(exist = false)
    private String leaderOri;
    private String leaderNameOri;
    private String comment;
    private Integer status;

    /**
     * 资产类型
     *
     * @see ApplicationConst.DirItemTypeEnum
     */
    private String dirItemType;

    @ApiModelProperty("业务分类 目录名")
    private String dirName;

    /**
     * @see com.sinohealth.system.domain.constant.DataDirConst.DocPermission
     * @see com.sinohealth.system.domain.constant.DataDirConst.TablePermission
     */
    @ApiModelProperty("权限组")
    private List<Integer> permissions;

    /**
     * 资产地图展示排序
     */
    private Integer disSort;

    // 文档
    private Long processId;
    // 表单
    private String tableName;

    private Date updateTime;
}
