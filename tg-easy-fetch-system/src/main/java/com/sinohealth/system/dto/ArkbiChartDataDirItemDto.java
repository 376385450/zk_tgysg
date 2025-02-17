package com.sinohealth.system.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.google.common.collect.Sets;
import com.sinohealth.common.utils.dto.Node;
import com.sinohealth.system.biz.dir.dto.DirItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Author Rudolph
 * @Date 2022-06-16 11:11
 * @Desc
 */
@ApiModel(description = "我的数据-申请DTO")
@Data
public class ArkbiChartDataDirItemDto implements Node<ArkbiChartDataDirItemDto>, DirItem {

    private Long id;

    private String dirName;

    private Long parentId;

    private Integer datasourceId;

    private Boolean isTable = false;
    @ApiModelProperty("权限类型1-5,1只读，5管理")
    private Integer accessType;

    private Integer sort;
    @ApiModelProperty("表前缀")
    private String prefix;

    @ApiModelProperty("数据有效期")
    private String dataExpir;

    @ApiModelProperty("数据源名称")
    private String sourceName;

    private List<ArkbiChartDataDirItemDto> children = new ArrayList<>();

    private Integer tableNums;

    private Date lastUpdate;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private String clientNames;

    @TableField(exist = false)
    private Integer requireTimeType;

    @TableField(exist = false)
    private Integer requireAttr;

    /**
     * 前端强烈要求添加，显示用
     */
    @ApiModelProperty("数据目录下是否有表，没有就禁用，true禁用，false不禁用，默认false")
    private Boolean disabled = false;

    private String icon;

    /**
     * 资产ID
     */
    private Set<Object> assetsIds = Sets.newHashSet();

    /**
     * 申请人
     */
    private Set<Long> applicantIds = Sets.newHashSet();

    /**
     * 预览链接
     */
    private String extAnalysisId;

    /**
     * 源项目名称
     */
    private Set<String> projectNames = Sets.newHashSet();

    @TableField(exist = false)
    private String nodeViewName;

    @ApiModelProperty("按钮列表")
    private List<Integer> actions;

    private Integer moved;
}
