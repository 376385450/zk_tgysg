package com.sinohealth.system.biz.dataassets.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.sinohealth.common.enums.dataassets.AcceptanceStateEnum;
import com.sinohealth.system.biz.dataassets.service.impl.AcceptanceRecordServiceImpl;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 验收记录
 *
 * @author kuangchengping@sinohealth.cn
 * 2023-08-17 14:18
 * @see AcceptanceRecordServiceImpl#accept
 * @see AcceptanceRecordServiceImpl#scheduleAutoAccept
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tg_acceptance_record")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AcceptanceRecord extends Model<AcceptanceRecord> implements AssetsVersion {

    @ApiModelProperty("主键自增")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long applicationId;

    private Long assetsId;

    /**
     * 资产版本
     */
    private Integer version;

    /**
     * 虚拟字段
     */
    @TableField(updateStrategy = FieldStrategy.NEVER, insertStrategy = FieldStrategy.NEVER)
    private String assetsVersion;

    /**
     * @see AcceptanceStateEnum
     */
    private String state;

    private String bizType;

    /**
     * 0 标识系统
     */
    private Long user;

    private String remark;

    private LocalDateTime acceptTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

}
