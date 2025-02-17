package com.sinohealth.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 别问我为什么这么做
 * 因为前端不行
 *
 * @author lvheng chen
 * @version 1.0
 * @date 2023-03-20 13:50
 */
@Data
@ApiModel("DataDirVO")
public class DataDirVO implements Serializable {

    private String id;

    private String dirName;

    private String parentId;

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

    private List<DataDirVO> children = new ArrayList<>();

    private Integer tableNums;

    private Date lastUpdate;

    /**
     * 前端强烈要求添加，显示用
     */
    @ApiModelProperty("数据目录下是否有表，没有就禁用，true禁用，false不禁用，默认false")
    private Boolean disabled = false;

    private String icon;

    private String nodeViewName;

    private Integer nums;

    @ApiModelProperty("地图目录描述信息")
    private String comment;

    private Boolean isNewNode;
}
