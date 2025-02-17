package com.sinohealth.system.domain.notice;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/23 14:18
 */
@Data
@Accessors(chain = true)
@ApiModel("公告")
@TableName("tg_notice_info")
public class TgNoticeInfo {

    @ApiModelProperty("主键id")
    @TableId
    private Long id;

    @ApiModelProperty("类型")
    private String noticeType;

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("内容")
    private String content;

    @ApiModelProperty("需要跳转的资产id")
    @TableField(value = "asset_id",updateStrategy = FieldStrategy.IGNORED)
    private Integer assetId;

    @ApiModelProperty("是否置顶，0：不置顶，1：置顶")
    private Integer isTop;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("更新人")
    private String updater;

    @ApiModelProperty("是否删除，0：未删除，1：已删除")
    private Integer delFlag;

    @ApiModelProperty("删除时间")
    private Date deleteTime;
}
