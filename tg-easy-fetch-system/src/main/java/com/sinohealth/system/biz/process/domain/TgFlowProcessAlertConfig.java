package com.sinohealth.system.biz.process.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.common.core.domain.IdTable;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 全流程告警设置
 *
 * @author zengjun 2024-08-14 17:05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_flow_process_alert_config")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TgFlowProcessAlertConfig extends Model<TgFlowProcessAlertConfig> implements IdTable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("分类")
    private String category;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("配置名称")
    private String name;

    @ApiModelProperty("成功告警开关")
    private Boolean successAlertSwitch;

    @ApiModelProperty("成功告警webhook")
    private String successWebHook;

    @ApiModelProperty("成功告警群成员手机号")
    private String successMemberNumbers;

    @ApiModelProperty("成功告警标题")
    private String successAlertTitle;

    @ApiModelProperty("成功告警内容")
    private String successAlertContent;

    @ApiModelProperty("失败告警开光")
    private Boolean failAlertSwitch;

    @ApiModelProperty("失败告警webhook")
    private String failWebHook;

    @ApiModelProperty("失败告警群成员手机号")
    private String failMemberNumbers;

    @ApiModelProperty("失败告警标题")
    private String failAlertTitle;

    @ApiModelProperty("失败告警内容")
    private String failAlertContent;
}
