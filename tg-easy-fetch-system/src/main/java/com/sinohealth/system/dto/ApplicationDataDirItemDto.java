package com.sinohealth.system.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.utils.dto.Node;
import com.sinohealth.system.biz.dir.dto.DirItem;
import com.sinohealth.system.domain.constant.ApplicationConst;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author Rudolph
 * @Date 2022-06-16 11:11
 * @Desc
 */
@ApiModel(description = "我的数据-申请DTO")
@Data
public class ApplicationDataDirItemDto implements Node<ApplicationDataDirItemDto>, DirItem {

    @ApiModelProperty("自增ID")
    private Long id;

    @ApiModelProperty("申请ID")
    private Long applicationId;

    @ApiModelProperty("申请人ID")
    private Long applicantId;

    @ApiModelProperty("申请人姓名")
    private String applicantName;

    @ApiModelProperty("项目名称")
    private String projectName;

    @ApiModelProperty("模板名称")
    private String templateName;

    @ApiModelProperty("基础表名")
    private String baseTableName;

    @ApiModelProperty("表别名")
    private String tableAlias;

    @ApiModelProperty("项目描述")
    private String projectDesc;

    @ApiModelProperty("需求性质")
    private Integer requireAttr;

    @ApiModelProperty("需求次数必填")
    private Integer requireTimeType;

    @ApiModelProperty("拟分配客户")
    private String clientNames;

    @ApiModelProperty("数据有效期")
    private String dataExpir;

    @ApiModelProperty("可读用户")
    private String readableUsers;

    @ApiModelProperty("可读用户姓名")
    @TableField(exist = false)
    private String readableUserNames;

    @ApiModelProperty("目录id")
    private Long parentId;

    @ApiModelProperty("序号")
    private Integer sort;

    @ApiModelProperty("是否需要更新")
    private Integer need2Update;

    @ApiModelProperty("是否因底表数据变动，需要更新")
    private Boolean needUpdate;

    @ApiModelProperty("子节点")
    private List<ApplicationDataDirItemDto> children = new ArrayList<>();


    @ApiModelProperty("最后更新时间")
    private Date lastUpdate;

    @ApiModelProperty("icon")
    private String icon = "form";

    @ApiModelProperty("状态")
    private Integer status;

    /**
     * @see ApplicationConst.ApplyViewStatusType
     */
    @ApiModelProperty("前端展示状态")
    private Integer viewStatus;

    @NotEmpty(message = InfoConstants.DIRNAME_REQUIREMENT)
    private String dirName;

    private Integer target;

    @TableField(exist = false)
    private String nodeViewName;

    /**
     * 另存标记
     */
    private Boolean copy;

    /**
     * 复制来源id
     */
    private Long copyFromId;

    @ApiModelProperty("按钮列表")
    private List<Integer> actions;

    private Integer moved;
}
