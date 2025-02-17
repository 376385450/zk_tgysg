package com.sinohealth.system.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.sinohealth.common.utils.dto.Node;
import com.sinohealth.system.biz.dir.dto.DirItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Jingjun
 * @since 2021/4/19
 */
@Data
@ApiModel("DataDirDto")
public class DataDirDto implements Node<DataDirDto>, DirItem {

    private Long id;

    private String dirName;

    private Long parentId;

    private Integer datasourceId;

    private Long tableId;

    private Boolean isTable = false;
    @ApiModelProperty("权限类型1-5,1只读，5管理")
    private Integer accessType;

    private Integer sort;
    @ApiModelProperty("表前缀")
    private String prefix;

    @ApiModelProperty("数据源名称")
    private String sourceName;

    @ApiModelProperty("申请ID")
    private Long applicationId;

    @ApiModelProperty("业务ID： 资产id 申请id 文档id")
    private Long nodeId;

    private List<DataDirDto> children = new ArrayList<>();

    @TableField(exist = false)
    private Integer tableNums;

    private Date lastUpdate;

    private String clientNames;

    private Integer requireTimeType;

    private Integer requireAttr;

    private Integer moved;

    /**
     * 前端强烈要求添加，显示用
     */
    @ApiModelProperty("数据目录下是否有表，没有就禁用，true禁用，false不禁用，默认false")
    private Boolean disabled = false;

    @TableField(exist = false)
    private String icon;

    @TableField(exist = false)
    private String nodeViewName;

    @TableField(exist = false)
    private Integer nums;

    @ApiModelProperty("地图目录描述信息")
    private String comment;
}
