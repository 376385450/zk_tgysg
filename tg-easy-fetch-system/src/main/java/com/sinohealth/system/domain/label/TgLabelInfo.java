package com.sinohealth.system.domain.label;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author Zhangzifeng
 * @Date 2023/8/18 16:17
 */
@Data
@TableName("tg_label_info")
@Accessors(chain = true)
@ApiModel("标签表")
public class TgLabelInfo {

    @TableId
    private Long id;

    @ApiModelProperty("标签名称")
    private String name;

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

    public static TgLabelInfo newInstance() {
        return new TgLabelInfo();
    }
}
