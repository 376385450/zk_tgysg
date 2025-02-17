package com.sinohealth.system.biz.dir.dto.node;

import com.sinohealth.common.enums.dataassets.AcceptanceStateEnum;
import com.sinohealth.common.enums.dataassets.AssetsSnapshotTypeEnum;
import com.sinohealth.system.biz.dir.dto.UserDataAssetsDirItemDTO;
import com.sinohealth.system.biz.table.domain.TableInfoSnapshot;
import com.sinohealth.system.domain.constant.ApplicationConst;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kuangchengping@sinohealth.cn
 * 2024-01-11 15:28
 * @see UserDataAssetsDirItemDTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDataAssetsNode implements AssetsNode {

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

    private Long templateId;

    @ApiModelProperty("模板名称")
    private String templateName;

    @ApiModelProperty("需求性质")
    private Integer requireAttr;

    /**
     * @see ApplicationConst.RequireTimeType
     */
    @ApiModelProperty("1：一次性需求、2：持续性需求")
    private Integer requireTimeType;

    @ApiModelProperty("数据有效期")
    private String dataExpire;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    private Long creator;

    @ApiModelProperty("目录id")
    private String parentId;

    @ApiModelProperty("子节点")
    private List<AssetsNode> children = new ArrayList<>();

    @ApiModelProperty("icon")
    private String icon = ApplicationConst.AssetsIcon.DATA;

    @ApiModelProperty("状态")
    private Integer status;

    private String creatorName;

    /**
     * 另存标记
     */
    private Boolean copy;

    /**
     * 复制来源id
     */
    private Long copyFromId;
    /**
     * 复制来源名称
     */
    private String copyFromName;
    /**
     * 时间颗粒度
     */
    private String timeGra;

    /**
     * @see TableInfoSnapshot#remark
     */
    private String tableRemark;

    @ApiModelProperty("按钮列表")
    private List<Integer> actions;

    private Boolean hidden;

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

    private String customerName;

    private String ftpStatus;

    private String ftpErrorMessage;
    private String nId;
    private String pId;

    @Override
    public String getName() {
        return projectName;
    }

    @Override
    public String getNodeId() {
        return AssetsNode.buildId(assetsId, icon);
    }

    @Override
    public Long getBizId() {
        return assetsId;
    }
}
