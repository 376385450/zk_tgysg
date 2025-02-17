package com.sinohealth.system.biz.dir.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.common.enums.dataassets.AcceptanceStateEnum;
import com.sinohealth.common.enums.dataassets.AssetsSnapshotTypeEnum;
import com.sinohealth.common.utils.dto.Node;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2023-06-12 10:27
 */
@Data
public class UserDataAssetsDirItemDTO implements Node<UserDataAssetsDirItemDTO>, DirItem {
    @ApiModelProperty("自增ID")
    private Long id;

    @ApiModelProperty("资产ID")
    private Long assetsId;

    private Long srcApplicationId;

    @ApiModelProperty("申请人ID")
    private Long applicantId;

    @ApiModelProperty("申请人姓名")
    private String applicantName;

    @ApiModelProperty("需求名称")
    private String projectName;

    @ApiModelProperty("项目名称")
    private String newProjectName;

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
    private String dataExpire;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

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
    private List<UserDataAssetsDirItemDTO> children = new ArrayList<>();


    @ApiModelProperty("最后更新时间")
    private Date lastUpdate;

    @ApiModelProperty("icon")
    private String icon = CommonConstants.ICON_DATA_ASSETS;

    @ApiModelProperty("状态")
    private Integer status;

//    /**
//     * @see ApplicationConst.ApplyViewStatusType
//     */
//    @ApiModelProperty("前端展示状态")
//    private Integer viewStatus;

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

    /**
     * 当前版本
     */
    private Integer version;

    /**
     * 全部版本
     */
    private Integer totalVersion;

    /**
     * @see AssetsSnapshotTypeEnum
     */
    private String snapshotType;

    /**
     * @see AcceptanceStateEnum
     */
    private String state;

    private String user;
}
