package com.sinohealth.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.utils.dto.Node;
import com.sinohealth.system.biz.dir.dto.DirItem;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(description = "地图目录-文档DTO")
@Data
public class DocDataDirItemDto implements Node<DocDataDirItemDto>, DirItem {
    @ApiModelProperty("自增ID")
    private Long id;

    @ApiModelProperty("备注")
    private String comment;

    @ApiModelProperty("目录ID")
    private Long dirId;

    @ApiModelProperty("文档ID")
    private Long docId;

    @ApiModelProperty("流程ID")
    private Long processId;

    @ApiModelProperty("文档名称")
    private String docName;

    @ApiModelProperty("文档所属人")
    private String leaderName;

    @ApiModelProperty("文档所属人组织")
    private String leaderNameOri;

    @ApiModelProperty("资产类型")
    private String assetType;

    @ApiModelProperty("目录id")
    private Long parentId;

    @ApiModelProperty("序号")
    private Integer sort;

    @ApiModelProperty("子节点")
    @JsonIgnore
    private List<DocDataDirItemDto> children = new ArrayList<>();

    @ApiModelProperty("权限组")
    private List<Integer> authorization = new ArrayList<>();

    @ApiModelProperty("icon")
    private String icon = "doc";

    @ApiModelProperty("状态")
    private Integer status;

    @NotEmpty(message = InfoConstants.DIRNAME_REQUIREMENT)
    private String dirName;

    private Integer target;

    private String nodeViewName;

    @ApiModelProperty("是否需要审核,默认需要")
    private Boolean need2Audit;

    private Date lastUpdate;

    private String clientNames;

    private Integer requireTimeType;

    private Integer requireAttr;

    private Integer moved;
}
