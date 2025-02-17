package com.sinohealth.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sinohealth.common.constant.InfoConstants;
import com.sinohealth.system.dto.auditprocess.ProcessNodeDetailDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 审核流程表(TgAuditProcessInfo)表实体类
 *
 * @author makejava
 * @since 2022-05-16 11:52:43
 */
@ApiModel(description = "审核流程表(TgAuditProcessInfo)表实体类")
@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_audit_process_info")
public class TgAuditProcessInfo extends Model<TgAuditProcessInfo> {
    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("流程ID")
    private Long processId;

    @ApiModelProperty("流程版本")
    private Integer processVersion;

    @ApiModelProperty("流程名称")
    @Size(max = 100, message = "流程名称长度超出限制")
    @NotBlank(message = "流程名称不能为空白")
    private String processName;

    @ApiModelProperty("关联表单名称")
    @JsonIgnore
    private String linkedTableNames;

    @ApiModelProperty("关联模板ID")
    @TableField(select = false)
    private String templateName;

    @ApiModelProperty("是否通用")
    private String isGeneric;

    @ApiModelProperty("使用次数")
    private Integer usedTimes;

    @ApiModelProperty("创建者")
    private String creator;

    @ApiModelProperty("更新者")
    private String updater;

    @ApiModelProperty("创建日期")
    private String createTime;

    @ApiModelProperty("更新日期")
    private String updateTime;

    @ApiModelProperty("前端用,消息通知")
    @TableField(exist = false)
    private List<List<String>> ways;

    @ApiModelProperty("前端用,消息通知")
    @TableField(exist = false)
    private List<List<String>> sucways;
    @ApiModelProperty("前端用,消息通知")
    @TableField(exist = false)
    private List<List<String>> rejways;

    @ApiModelProperty("流程链明细信息(JSON序列化)")
    @TableField(exist = false)
    @NotEmpty(message = InfoConstants.HANDLE_CHAIN_NOT_EMPTY)
    private List<ProcessNodeDetailDto> processChainDetailInfo;

    @ApiModelProperty("审核通过节点(JSON序列化)")
    @TableField(exist = false)
    private List<ProcessNodeDetailDto> sucessNode;

    @ApiModelProperty("审核拒绝节点(JSON序列化)")
    @TableField(exist = false)
    private List<ProcessNodeDetailDto> rejectNode;

    @ApiModelProperty("流程链明细信息(JSON序列化)")
    @JsonIgnore
    private String processChainDetailJson;
    @ApiModelProperty("审核通过节点(JSON序列化)")
    @JsonIgnore
    private String successNodeJson;
    @ApiModelProperty("审核拒绝节点(JSON序列化)")
    @JsonIgnore
    private String rejectNodeJson;

    @ApiModelProperty("当前流程状态")
    private Integer currentAuditStatus;

    @ApiModelProperty("状态")
    private String status;

    /**
     * 获取主键值
     *
     * @return 主键值
     */
    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    public static TgAuditProcessInfo newInstance() {
        return new TgAuditProcessInfo();
    }

}



