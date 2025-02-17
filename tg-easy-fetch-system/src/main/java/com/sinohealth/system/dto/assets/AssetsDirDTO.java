package com.sinohealth.system.dto.assets;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.utils.dto.Node;
import com.sinohealth.system.biz.dir.dto.DirItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 资产目录树
 */
@Data
@ApiModel("AssetsDirDTO")
public class AssetsDirDTO implements Node<AssetsDirDTO>, DirItem {

    private Long id;

    private String dirName;

    private Long parentId;

    private Integer sort;

    private String icon;

    @ApiModelProperty("查看次数")
    private Long viewCount;

    @ApiModelProperty("数据条数")
    private Long dataCount;

    @ApiModelProperty("最新期数")
    private String latestDate;

    @ApiModelProperty("数据更新时间")
    private String dataUpdateTime;

    @ApiModelProperty("节点业务id")
    private Long nodeId;

    @ApiModelProperty("报表权限：1:查看;2下载")
    private String authType;

    @ApiModelProperty("更新人id")
    private Long updateBy;

    @ApiModelProperty("更新人")
    private String updatedByName;

    private Integer status;

    private Boolean hasDataDesc;


    private List<AssetsDirDTO> children = new ArrayList<>();

    /**
     * 额外的信息
     */
    private Map<String, Object> extMap;

    //
    private String extAnalysisId;

    @TableField(exist = false)
    private Date lastUpdate;

    @TableField(exist = false)
    private String clientNames;

    @TableField(exist = false)
    private Integer requireTimeType;

    @TableField(exist = false)
    private Integer requireAttr;

    private Integer moved;

    @JsonIgnore
    public boolean isArkbiNode() {
        if (StringUtils.equals(CommonConstants.ICON_CHART, icon)) {
            return true;
        }
        if (StringUtils.equals(CommonConstants.ICON_DASHBOARD, icon)) {
            return true;
        }
        return false;
    }

    @JsonIgnore
    public boolean isApplicationNode() {
        return StringUtils.equals(CommonConstants.ICON_DATA_ASSETS, icon);
    }


    @Override
    public String getNodeViewName() {
        return dirName;
    }
}
