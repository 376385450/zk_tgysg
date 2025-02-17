package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.system.biz.dir.entity.DisplaySort;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "提数文档信息表(TgDocInfo)实体类")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_doc_info")
public class TgDocInfo extends Model<TgDocInfo> implements IAssetBindingData, DisplaySort {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("所有人ID")
    private Long ownerId;

    @ApiModelProperty("所有人显示名")
    @TableField(exist = false)
    private String ownerName;

    @ApiModelProperty("文档名称")
    private String name;

    @ApiModelProperty("已上传的文件 Obs 路径")
    private String path;

    @ApiModelProperty("已上传的文件 Obs 真实路径")
    @TableField(exist = false)
    private String realPath;

    @ApiModelProperty("PDF文件 OBS 路径")
    private String pdfPath;

    @ApiModelProperty("已上传的文件 Obs 类型")
    private String type;

    @ApiModelProperty("注释")
    private String comment;

    @ApiModelProperty("白名单ID")
    @TableField(exist = false)
    private List<WhiteListUser> whitelistUsers;

    @ApiModelProperty("白名单人员权限JSON")
    @JsonIgnore
    private String whitelistUserJson;

    @ApiModelProperty("是否需要审核,默认需要")
    private Boolean need2Audit = true;

    @ApiModelProperty("能否下载源文件,默认不能")
    private Boolean canDownloadSourceFile = false;

    @ApiModelProperty("能否下载PDF,默认不能")
    private Boolean canDownloadPdf = false;

    @ApiModelProperty("目录ID")
    private Long dirId;

    @ApiModelProperty("目录名称")
    @TableField(exist = false)
    private String dirName;

    @ApiModelProperty("流程ID")
    private Long processId;

    @ApiModelProperty("流程版本号")
    private Integer processVersion;

    @ApiModelProperty("创建人名称")
    private String creator;

    @ApiModelProperty("更新人名称")
    private String updater;

    @ApiModelProperty("创建时间")
    private String createTime;

    @ApiModelProperty("更新时间")
    private String updateTime;

    @ApiModelProperty("申请次数")
    private Integer applyTimes = 0;

    @ApiModelProperty("申请成功次数")
    private Integer successfulApplyTimes = 0;

    @ApiModelProperty("阅读次数")
    private Integer readTimes = 0;

    @ApiModelProperty("下载PDF次数")
    private Integer pdfDownloadTimes = 0;

    @ApiModelProperty("下载源文件次数")
    private Integer sourceFileDownloadTimes = 0;

    @ApiModelProperty("是否用于上传文档集初始化")
    @TableField(exist = false)
    @JsonIgnore
    private Boolean isInit = false;

    @ApiModelProperty("状态,0禁用,1正常")
    private Integer status = CommonConstants.NORMAL;
    /**
     * 资产地图展示排序
     */
    private Integer disSort;

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    public static TgDocInfo newInstance() {
        return new TgDocInfo();
    }

    @Override
    public void fillDisSort(Integer sort) {
        this.disSort = sort;
    }
}
